package com.spring.dishcovery.controller;


import com.spring.dishcovery.config.CookieUtil;
import com.spring.dishcovery.config.JwtUtil;
import com.spring.dishcovery.entity.CodeVO;
import com.spring.dishcovery.entity.RecipeVo;
import com.spring.dishcovery.mapper.CodeMapper;
import com.spring.dishcovery.service.CodeService;
import com.spring.dishcovery.service.RecipeAppService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeAppService service;
    private final CodeService codeService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    @GetMapping("/searchRecipe")
    public String searchRecipe(@RequestParam String searchName,
                               @RequestParam(required = false, defaultValue = "latest") String sort,
                               Model model) {

//        RecipeVo searchVo = new RecipeVo();
//        searchVo.setSearchName(searchName);

        String sortKey = switch (sort) {
            case "popular" -> "views";
            case "recommend" -> "random";
            case "latest" -> "latest";
            case "views", "time", "difficulty", "random" -> sort;
            default -> "latest";
        };

        List<RecipeVo> recipes = service.getSearchRecipesSorted(searchName, sortKey);


        model.addAttribute("recipes", recipes);
        model.addAttribute("searchName", searchName);
        model.addAttribute("sort", sort);

        // 메인 화면 탭(레시피/상점) 스타일 기본값
        model.addAttribute("rcpClassNm", "seg-btn active");
        model.addAttribute("rankClassNm", "seg-btn");
        model.addAttribute("lastClassNm", "seg-btn");

        return "/mainPage";
    }


    @GetMapping("/recipeWrite")
    public String recipeWrite(Model model, HttpServletRequest request) {

        List<CodeVO> categoryList = codeService.codeList("CTG");
        List<CodeVO> levelList = codeService.codeList("LV");


        model.addAttribute("categoryList", categoryList);
        model.addAttribute("levelList", levelList);

        return "recipe/RecipeReg";
    }

    @PostMapping("/SaveRecipeData")
    public String saveRecipeData(@ModelAttribute RecipeVo recipeVo, HttpServletRequest request) {

        String token = cookieUtil.getTokenFromCookies(request, "JWT_TOKEN");
        String userId = token != null ? jwtUtil.getUserIdFromToken(token) : null;
        if (userId == null) {
            return "redirect:/dishcovery_login";
        }

        recipeVo.setUserId(userId);
        service.SaveRecipeData(recipeVo);

        return "redirect:/MainPage";
    }

    @GetMapping("/recipe/detail")
    public String recipeDetail(@RequestParam String recipeId, Model model,HttpServletRequest request) {

        RecipeVo recipe = new RecipeVo();
        String userId ="";
        String token = cookieUtil.getTokenFromCookies(request, "JWT_TOKEN");
        if(token != null) {
            userId = jwtUtil.getUserIdFromToken(token);
        }

        List<RecipeVo> stepList = new ArrayList<>();

        List<CodeVO> categoryList = codeService.codeList("CTG");
        recipe = service.getRecipeDataDetail(recipeId,userId);
        stepList = recipe.getStepList();

        model.addAttribute("recipe", recipe);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("stepList", stepList);

        return "recipe/RecipeDetail";

    }

}
