package com.spring.dishcovery.recipe.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecipeComment {
    private Long commentId;
    private String recipeId;
    private String userId;
    private String content;
    private LocalDateTime createdAt;
}
