package com.spring.dishcovery.recipe.presentation;

import com.spring.dishcovery.global.config.CookieUtil;
import com.spring.dishcovery.global.config.JwtUtil;
import com.spring.dishcovery.recipe.domain.entity.RecipeAppVo;
import com.spring.dishcovery.recipe.application.RecipeRequest;
import com.spring.dishcovery.recipe.application.RecipeResponse;
import com.spring.dishcovery.recipe.domain.entity.RecipeComment;
import com.spring.dishcovery.recipe.domain.entity.RecipeVo;
import com.spring.dishcovery.recipe.application.OpenAiService;
import com.spring.dishcovery.recipe.application.RecipeAppService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecipeAppController {

    private final RecipeAppService recipeAppService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    private final OpenAiService aiService;

    @GetMapping(value = "/getAppRecipes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RecipeAppVo>> getAppRecipes() {

        List<RecipeAppVo> list = recipeAppService.getAppRecipes();

        if (list == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        return ResponseEntity.ok(list);
    }

    @GetMapping(value = "/myRecipe", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RecipeVo>> getMyRecipes(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String userId = userIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<RecipeVo> myRecipes = recipeAppService.getMyRecipes(userId);
        return ResponseEntity.ok(myRecipes == null ? Collections.emptyList() : myRecipes);
    }

    @GetMapping("/byUser/{userId}")
    public List<RecipeVo> getRecipesByUser(@PathVariable String userId) {
        return recipeAppService.getMyRecipes(userId);
    }

    @GetMapping("/{id}")
    public RecipeAppVo selectRecipeById(@PathVariable("id") String id) {
        return recipeAppService.selectRecipeById(id);
    }

    @GetMapping("/{id}/steps")
    public List<RecipeAppVo> getRecipeSteps(@PathVariable("id") String id) {
        return recipeAppService.selectStepsByRecipeId(id);
    }

    @GetMapping("/{id}/comments")
    public List<RecipeComment> getComments(@PathVariable("id") String id) {
        return recipeAppService.listComments(id);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Void> addComment(@PathVariable("id") String id,
                                           @RequestBody Map<String, String> body,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = userIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            recipeAppService.addComment(userId, id, body.get("content"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") String id,
                                              @PathVariable Long commentId,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = userIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            recipeAppService.deleteComment(userId, commentId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/like")
    public Map<String, Object> getLikeStatus(@PathVariable("id") String id,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = userIdFromAuthHeader(authHeader);
        return Map.of("likeCount", recipeAppService.getLikeCount(id), "liked", recipeAppService.isLiked(userId, id));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable("id") String id,
                                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = userIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        recipeAppService.toggleLike(userId, id);
        return ResponseEntity.ok(Map.of("likeCount", recipeAppService.getLikeCount(id), "liked", recipeAppService.isLiked(userId, id)));
    }

    @GetMapping("/{id}/edit")
    public ResponseEntity<RecipeVo> getRecipeForEdit(@PathVariable("id") String id,
                                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = userIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RecipeVo recipe = recipeAppService.getRecipeForEdit(id);
        if (recipe == null || !userId.equals(recipe.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(recipe);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<Void> updateRecipe(@PathVariable("id") String id,
                                             @ModelAttribute RecipeVo recipeVo,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = userIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        recipeVo.setRecipeId(id);
        try {
            recipeAppService.updateRecipeData(recipeVo, userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable("id") String id,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = userIdFromAuthHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            recipeAppService.deleteRecipeData(id, userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public List<RecipeVo> searchRecipes(@RequestParam String searchName,
                                        @RequestParam(required = false, defaultValue = "latest") String sort,
                                        @RequestParam(required = false, defaultValue = "1") int page) {
        return recipeAppService.getSearchRecipesSorted(searchName, sort, page);
    }

    private String userIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return jwtUtil.getUserIdFromToken(authHeader.substring(7));
    }

    @GetMapping("/getRecipes")
    public List<RecipeVo> getRecipes() {
        return recipeAppService.getAllRecipes();
    }

    @PostMapping("/SaveRecipeData")
    public int SaveRecipeData(HttpServletRequest request
                             ,@ModelAttribute RecipeVo recipe
                             ,@RequestHeader("Authorization") String authHeader) throws Exception {


        String token = authHeader.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(token); // JWT에서 사용자 식별

        recipe.setUserId(userId);

        return recipeAppService.SaveRecipeData(recipe);
    }


    @PostMapping("/recommend")
    public RecipeResponse recommend(@RequestBody RecipeRequest req) throws Exception {
        RecipeResponse res = new RecipeResponse();
        res.recipe = aiService.getRecipe(req.ingredients);
        return res;
    }

}
