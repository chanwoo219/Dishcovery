package com.spring.dishcovery.recipe.application;

import com.spring.dishcovery.recipe.domain.entity.RecipeAppVo;
import com.spring.dishcovery.recipe.domain.entity.RecipeVo;
import com.spring.dishcovery.recipe.domain.mapper.RecipeAppMapper;
import com.spring.dishcovery.shop.domain.mapper.ShopMapper;
import com.spring.dishcovery.user.domain.mapper.UserMapper;
import com.spring.dishcovery.global.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class RecipeAppService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    RecipeAppMapper recipeAppMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ShopMapper shopMapper;

    public List<RecipeAppVo> getAppRecipes() {
        return recipeAppMapper.getAppRecipes();
    }

    public RecipeAppVo selectRecipeById(String recipeId) {
        return recipeAppMapper.selectRecipeById(recipeId);
    }

    public List<RecipeAppVo> selectStepsByRecipeId(String recipeId) {
        return recipeAppMapper.selectStepsByRecipeId(recipeId);
    }

    public List<RecipeVo> getAllRecipes() {
        return recipeAppMapper.getAllRecipes();
    }

    public List<RecipeVo> getAllRecipesSorted(String sort) {
        return recipeAppMapper.getAllRecipesSorted(sort);
    }

    public List<RecipeVo> getSearchRecipes(String searchName) {
        return recipeAppMapper.getSearchRecipes(searchName);
    }

    public List<RecipeVo> getSearchRecipesSorted(String searchName, String sort) {
        return recipeAppMapper.getSearchRecipesSorted(searchName, sort);
    }

    /**
     * ✅ 절대 500 안 나게 방어처리 포함
     * - 대표 이미지가 없을 수 있음 (get(0) 금지)
     * - stepDescriptions null 가능
     * - 빈 step 문자열은 저장 안 함
     */
    public int SaveRecipeData(RecipeVo recipeVo) {
        int result = 0;

        if (recipeVo == null) {
            throw new IllegalArgumentException("레시피 데이터가 비어있습니다.");
        }

        // recipeId 생성(중복 가능성은 낮지만 존재 → 필요하면 DB 중복 체크 추가 가능)
        String dataPart = new SimpleDateFormat("yyMMdd").format(new Date());
        int randomPart = new Random().nextInt(900) + 100;
        String recipeId = "RCP" + dataPart + randomPart;

        recipeVo.setRecipeId(recipeId);

        // ✅ 대표 이미지 저장 (없어도 절대 안 터지게)
        List<String> mainImagePaths = new ArrayList<>();
        try {
            // mainImages가 null이거나 비어있거나 0번 파일이 empty면 저장 시도 안 함
            if (recipeVo.getMainImages() != null
                    && !recipeVo.getMainImages().isEmpty()
                    && recipeVo.getMainImages().get(0) != null
                    && !recipeVo.getMainImages().get(0).isEmpty()) {

                mainImagePaths = FileUploadUtil.saveRecipeFiles(
                        recipeVo.getMainImages(), uploadDir, recipeId
                );
            }
        } catch (Exception e) {
            // 파일 저장 실패해도 등록 자체가 500으로 죽지 않게
            e.printStackTrace();
            mainImagePaths = new ArrayList<>();
        }

        // imgUrl 세팅 (빈 리스트면 null 또는 기본 이미지)
        if (mainImagePaths != null && !mainImagePaths.isEmpty()) {
            recipeVo.setImgUrl(mainImagePaths.get(0));
        } else {
            // 1) DB에서 IMG_URL이 NULL 허용이면 null OK
            recipeVo.setImgUrl(null);

            // 2) 만약 IMG_URL NOT NULL이면 아래 주석 해제해서 기본 이미지로 고정
            // recipeVo.setImgUrl("/images/comm/default_recipe.png");
        }

        // recipe_master insert
        result = recipeAppMapper.SaveRecipeData(recipeVo);

        // ✅ step 저장 (null 방어 + 빈 문자열 제거)
        String[] stepArr = recipeVo.getStepDescriptions();
        List<RecipeVo> stepList = new ArrayList<>();

        if (stepArr != null && stepArr.length > 0) {
            for (int i = 0; i < stepArr.length; i++) {
                String desc = stepArr[i];

                // 빈칸 step은 저장 안 함 (원하면 정책 바꿔도 됨)
                if (desc == null || desc.trim().isEmpty()) {
                    continue;
                }

                RecipeVo steps = new RecipeVo();
                steps.setRecipeId(recipeId);
                steps.setStepOrder(stepList.size() + 1); // 빈칸 제거 후 순서 재정렬
                steps.setStepDescription(desc.trim());
                stepList.add(steps);
            }

            if (!stepList.isEmpty()) {
                result = recipeAppMapper.insertRecipeSteps(stepList);
            }
        }

        return result;
    }

    public List<RecipeVo> getMyRecipes(String userId) {
        return recipeAppMapper.getMyRecipes(userId);
    }

    @Transactional
    public RecipeVo getRecipeDataDetail(String recipeId, String userId) {
        RecipeVo recipeVo;

        // userid가 있을때만 조회수 증가 시키기
        if (userId != null && !userId.isBlank()) {

            // 1) 같은 유저도 "하루 1회"만 카운트
            Integer exists = recipeAppMapper.checkUserView(recipeId, userId);

            if (exists != null && exists > 0) {
                recipeVo = recipeAppMapper.getRecipeDataDetail(recipeId);
                if (recipeVo != null) {
                    recipeVo.setStepList(recipeAppMapper.selectStepList(recipeId));
                }
                return recipeVo; // 이미 조회한 유저 → 증가 X
            }

            // 2) 조회 기록 추가
            recipeAppMapper.insertUserView(recipeId, userId);

            // 3) 조회수 증가
            recipeAppMapper.updateViewCount(recipeId);

            // 4) 조회수 1회당 1포인트 적립 (작성자에게)
            java.util.Map<String, Object> state = recipeAppMapper.selectRecipePointState(recipeId);
            if (state != null) {
                String writerId = String.valueOf(state.get("userId"));

                // 작성자 본인 조회는 제외
                if (writerId != null && !writerId.isBlank() && !writerId.equals(userId)) {
                    userMapper.addUserPoints(writerId, 1);

                    // 원장 기록
                    shopMapper.insertLedger(
                            writerId,
                            1,
                            "EARN_VIEW",
                            "레시피 조회 적립: " + recipeId
                    );
                }
            }
        }

        recipeVo = recipeAppMapper.getRecipeDataDetail(recipeId);
        if (recipeVo != null) {
            recipeVo.setStepList(recipeAppMapper.selectStepList(recipeId));
        }

        return recipeVo;
    }
}
