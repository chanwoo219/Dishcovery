package com.spring.dishcovery.recipe.application;

import com.spring.dishcovery.recipe.domain.entity.RecipeAppVo;
import com.spring.dishcovery.recipe.domain.entity.RecipeComment;
import com.spring.dishcovery.recipe.domain.entity.RecipeVo;
import com.spring.dishcovery.recipe.domain.mapper.RecipeAppMapper;
import com.spring.dishcovery.shop.domain.mapper.ShopMapper;
import com.spring.dishcovery.user.domain.mapper.UserMapper;
import com.spring.dishcovery.global.util.FileUploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class RecipeAppService {

    public static final int PAGE_SIZE = 12;

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

    public List<RecipeVo> getAllRecipesSorted(String sort, int page) {
        int offset = Math.max(page - 1, 0) * PAGE_SIZE;
        return recipeAppMapper.getAllRecipesSorted(sort, offset, PAGE_SIZE);
    }

    public int countAllRecipes() {
        return recipeAppMapper.countAllRecipes();
    }

    public List<RecipeVo> getSearchRecipes(String searchName) {
        return recipeAppMapper.getSearchRecipes(searchName);
    }

    public List<RecipeVo> getSearchRecipesSorted(String searchName, String sort, int page) {
        int offset = Math.max(page - 1, 0) * PAGE_SIZE;
        return recipeAppMapper.getSearchRecipesSorted(searchName, sort, offset, PAGE_SIZE);
    }

    public int countSearchRecipes(String searchName) {
        return recipeAppMapper.countSearchRecipes(searchName);
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
            log.error("레시피 이미지 저장 실패: recipeId={}", recipeId, e);
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
            awardPointsToWriter(recipeId, userId, "EARN_VIEW", "레시피 조회 적립: " + recipeId);
        }

        recipeVo = recipeAppMapper.getRecipeDataDetail(recipeId);
        if (recipeVo != null) {
            recipeVo.setStepList(recipeAppMapper.selectStepList(recipeId));
        }

        return recipeVo;
    }

    public List<RecipeComment> listComments(String recipeId) {
        try {
            return Optional.ofNullable(recipeAppMapper.listComments(recipeId)).orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("댓글 목록 조회 실패: recipeId={}", recipeId, e);
            return Collections.emptyList();
        }
    }

    public void addComment(String userId, String recipeId, String content) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("로그인이 필요합니다");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("댓글 내용을 입력해주세요");

        recipeAppMapper.insertComment(recipeId, userId, content.trim());

        // 댓글 1개당 1포인트 적립 (작성자에게, 댓글 삭제해도 회수 안 함)
        awardPointsToWriter(recipeId, userId, "EARN_COMMENT", "레시피 댓글 적립: " + recipeId);
    }

    // 댓글 작성자 또는 레시피 작성자만 삭제 가능 (권한 확인은 WHERE 절에서 처리)
    public void deleteComment(String userId, Long commentId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("로그인이 필요합니다");

        int deleted = recipeAppMapper.deleteComment(commentId, userId);
        if (deleted == 0) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }
    }

    public int getLikeCount(String recipeId) {
        return recipeAppMapper.countLikes(recipeId);
    }

    public boolean isLiked(String userId, String recipeId) {
        if (userId == null || userId.isBlank()) return false;
        return recipeAppMapper.existsLike(recipeId, userId) > 0;
    }

    public void toggleLike(String userId, String recipeId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("로그인이 필요합니다");

        if (recipeAppMapper.existsLike(recipeId, userId) > 0) {
            recipeAppMapper.deleteLike(recipeId, userId);
        } else {
            recipeAppMapper.insertLike(recipeId, userId);
            // 좋아요 1개당 1포인트 적립 (작성자에게, 좋아요 취소해도 회수 안 함)
            awardPointsToWriter(recipeId, userId, "EARN_LIKE", "레시피 좋아요 적립: " + recipeId);
        }
    }

    // 레시피 작성자에게 포인트 적립 (본인 행동은 제외)
    private void awardPointsToWriter(String recipeId, String actorUserId, String ledgerType, String description) {
        java.util.Map<String, Object> state = recipeAppMapper.selectRecipePointState(recipeId);
        if (state == null) return;

        String writerId = String.valueOf(state.get("userId"));
        if (writerId == null || writerId.isBlank() || writerId.equals(actorUserId)) return;

        userMapper.addUserPoints(writerId, 1);
        shopMapper.insertLedger(writerId, 1, ledgerType, description);
    }

    // 조회수/포인트 증가 없이 수정 화면용으로만 조회
    public RecipeVo getRecipeForEdit(String recipeId) {
        RecipeVo recipeVo = recipeAppMapper.getRecipeDataDetail(recipeId);
        if (recipeVo != null) {
            recipeVo.setStepList(recipeAppMapper.selectStepList(recipeId));
        }
        return recipeVo;
    }

    @Transactional
    public void updateRecipeData(RecipeVo recipeVo, String userId) {
        if (recipeVo == null) {
            throw new IllegalArgumentException("레시피 데이터가 비어있습니다.");
        }

        recipeVo.setUserId(userId);

        // 새 이미지가 업로드된 경우에만 교체, 아니면 폼에 넘어온 기존 imgUrl 유지
        try {
            if (recipeVo.getMainImages() != null
                    && !recipeVo.getMainImages().isEmpty()
                    && recipeVo.getMainImages().get(0) != null
                    && !recipeVo.getMainImages().get(0).isEmpty()) {

                List<String> mainImagePaths = FileUploadUtil.saveRecipeFiles(
                        recipeVo.getMainImages(), uploadDir, recipeVo.getRecipeId()
                );
                if (mainImagePaths != null && !mainImagePaths.isEmpty()) {
                    recipeVo.setImgUrl(mainImagePaths.get(0));
                }
            }
        } catch (Exception e) {
            log.error("레시피 이미지 교체 실패: recipeId={}", recipeVo.getRecipeId(), e);
        }

        int updated = recipeAppMapper.updateRecipeData(recipeVo);
        if (updated == 0) {
            throw new IllegalArgumentException("본인 레시피만 수정할 수 있습니다.");
        }

        // 조리 단계는 전체 삭제 후 재등록
        recipeAppMapper.deleteRecipeSteps(recipeVo.getRecipeId());

        String[] stepArr = recipeVo.getStepDescriptions();
        List<RecipeVo> stepList = new ArrayList<>();

        if (stepArr != null && stepArr.length > 0) {
            for (String desc : stepArr) {
                if (desc == null || desc.trim().isEmpty()) {
                    continue;
                }

                RecipeVo step = new RecipeVo();
                step.setRecipeId(recipeVo.getRecipeId());
                step.setStepOrder(stepList.size() + 1);
                step.setStepDescription(desc.trim());
                stepList.add(step);
            }

            if (!stepList.isEmpty()) {
                recipeAppMapper.insertRecipeSteps(stepList);
            }
        }
    }

    public void deleteRecipeData(String recipeId, String userId) {
        int deleted = recipeAppMapper.deleteRecipe(recipeId, userId);
        if (deleted == 0) {
            throw new IllegalArgumentException("본인 레시피만 삭제할 수 있습니다.");
        }
    }
}
