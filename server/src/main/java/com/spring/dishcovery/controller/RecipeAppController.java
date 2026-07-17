package com.spring.dishcovery.controller;

import com.spring.dishcovery.config.CookieUtil;
import com.spring.dishcovery.config.JwtUtil;
import com.spring.dishcovery.entity.RecipeAppVo;
import com.spring.dishcovery.entity.RecipeRequest;
import com.spring.dishcovery.entity.RecipeResponse;
import com.spring.dishcovery.entity.RecipeVo;
import com.spring.dishcovery.service.OpenAiService;
import com.spring.dishcovery.service.RecipeAppService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecipeAppController {

    private final RecipeAppService recipeAppService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    private final OpenAiService aiService;
//
//    @GetMapping("/getAppRecipes")
//    public List<RecipeAppVo> getAppRecipes() {
//        return recipeAppService.getAppRecipes();
//    }

    @GetMapping(value = "/getAppRecipes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RecipeAppVo>> getAppRecipes() {

        List<RecipeAppVo> list = recipeAppService.getAppRecipes();

        if (list == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public RecipeAppVo selectRecipeById(@PathVariable("id") String id) {
        return recipeAppService.selectRecipeById(id);
    }

    @GetMapping("/{id}/steps")
    public List<RecipeAppVo> getRecipeSteps(@PathVariable("id") String id) {
        return recipeAppService.selectStepsByRecipeId(id);
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
