package com.spring.dishcovery.recipe.presentation;

import com.spring.dishcovery.recipe.domain.entity.RecipeVo;
import com.spring.dishcovery.recipe.application.RecipeAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RouletteController {

    private final RecipeAppService recipeAppService;

    @GetMapping("/roulette")
    public List<RecipeVo>  roulette(Model model) {
        List<RecipeVo> recipes = recipeAppService.getAllRecipes();


        return recipes;
    }

}
