package com.spring.dishcovery.recipe.domain.mapper;

import com.spring.dishcovery.recipe.domain.entity.RecipeAppVo;
import com.spring.dishcovery.recipe.domain.entity.RecipeComment;
import com.spring.dishcovery.recipe.domain.entity.RecipeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecipeAppMapper {

    List<RecipeAppVo> getAppRecipes();

    RecipeAppVo selectRecipeById(String recipeId);

    List<RecipeAppVo> selectStepsByRecipeId(String recipeId);

    List<RecipeVo> getAllRecipes();

    List<RecipeVo> getAllRecipesSorted(@Param("sort") String sort);

    List<RecipeVo> getSearchRecipes(String searchName);

    List<RecipeVo> getSearchRecipesSorted(@Param("searchName") String searchName,
                                          @Param("sort") String sort);

    int SaveRecipeData(RecipeVo recipeVo);

    List<RecipeVo> getMyRecipes(String userId);

    int insertRecipeSteps(List<RecipeVo> stepList);

    RecipeVo getRecipeDataDetail(String recipeId);

    List<RecipeVo> selectStepList(String recipeId);

    // 유저가 이미 조회했는지 체크
    Integer checkUserView(@Param("recipeId") String recipeId, @Param("userId") String userId);
    // 조회 기록 저장
    int insertUserView(@Param("recipeId") String recipeId,  @Param("userId") String userId);

    // 레시피 조회수 증가
    int updateViewCount(@Param("recipeId") String recipeId);

    java.util.Map<String, Object> selectRecipePointState(@Param("recipeId") String recipeId);

    int updateClaimedPoint(@Param("recipeId") String recipeId,
                           @Param("claimedPoint") int claimedPoint);

    List<RecipeComment> listComments(@Param("recipeId") String recipeId);

    void insertComment(@Param("recipeId") String recipeId,
                       @Param("userId") String userId,
                       @Param("content") String content);

    int countLikes(@Param("recipeId") String recipeId);

    int existsLike(@Param("recipeId") String recipeId, @Param("userId") String userId);

    void insertLike(@Param("recipeId") String recipeId, @Param("userId") String userId);

    void deleteLike(@Param("recipeId") String recipeId, @Param("userId") String userId);

    int updateRecipeData(RecipeVo recipeVo);

    void deleteRecipeSteps(@Param("recipeId") String recipeId);

    int deleteRecipe(@Param("recipeId") String recipeId, @Param("userId") String userId);

    // 댓글 작성자 또는 레시피 작성자만 삭제 가능
    int deleteComment(@Param("commentId") Long commentId, @Param("userId") String userId);

}
