package com.spring.dishcovery.recipe.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecipeAppVo {

    private String recipeId;
    private String userId;
    private String categoryId;
    private String title;
    private String rcpDisc;
    private String cookTime;
    private String cookDfct;
    private String rgtDate;
    private String updDate;
    private String imgUrl;
    private String recipeIngr;
    private String recipeTip;
    private String recipeTag;

    private String stepDescription;
    private int stepOrder;

    List<RecipeAppVo> stepList;

}
