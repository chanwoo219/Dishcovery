package com.spring.dishcovery.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class FileUploadUtil {

    // ✅ 파일 크기 제한 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 레시피 파일 저장 (날짜별 + 레시피ID 폴더 구조)
     *
     * @param files 업로드할 MultipartFile 리스트
     * @param baseUploadDir 프로젝트 내부 절대경로 (예: /Users/jinhee/project/uploads)
     * @param recipeId 레시피 ID
     * @return 웹 접근 가능한 상대경로 리스트
     */
    public static List<String> saveRecipeFiles(List<MultipartFile> files, String baseUploadDir, String recipeId) {
        List<String> filePaths = new ArrayList<>();

        // ✅ baseUploadDir 절대경로 보장
        File baseDir = new File(baseUploadDir);
        if (!baseDir.isAbsolute()) {
            baseUploadDir = baseDir.getAbsolutePath();
        }

        try {
            // ✅ 날짜별 + 레시피ID 폴더 생성 (예: 251110_RCP1234)
            String today = new SimpleDateFormat("yyMMdd").format(new Date());
            String folderName = today + "_" + recipeId;
            Path uploadPath = Paths.get(baseUploadDir, folderName);

            // ✅ 경로 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("디렉토리 생성: {}", uploadPath);
            }

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                // ✅ 크기 제한
                if (file.getSize() > MAX_FILE_SIZE) {
                    log.warn("파일 크기 초과: {}", file.getOriginalFilename());
                    continue; // 초과 파일은 무시
                }

                // ✅ 파일명 랜덤 지정
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                // ✅ 저장 경로
                Path filePath = uploadPath.resolve(fileName);

                // ✅ 파일 저장
                file.transferTo(filePath.toFile());

                // ✅ 웹 접근 가능한 상대경로 추가 (Spring 정적 리소스 매핑 전제)
                String webPath = "/uploads/" + folderName + "/" + fileName;
                filePaths.add(webPath);

                log.info("업로드 완료: {}", webPath);
            }

        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생", e);
            return Collections.emptyList();
        }

        return filePaths;
    }

    /**
     * 프로필 사진 저장 (uploads/profile/ 아래)
     *
     * @param file 업로드할 이미지 파일
     * @param baseUploadDir 프로젝트 내부 절대경로
     * @param userId 유저 ID (파일명 접두어로 사용)
     * @return 웹 접근 가능한 상대경로 (실패 시 null)
     */
    public static String saveProfileImage(MultipartFile file, String baseUploadDir, String userId) {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("파일 크기 초과: {}", file.getOriginalFilename());
            return null;
        }

        File baseDir = new File(baseUploadDir);
        if (!baseDir.isAbsolute()) {
            baseUploadDir = baseDir.getAbsolutePath();
        }

        try {
            Path uploadPath = Paths.get(baseUploadDir, "profile");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = userId + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            return "/uploads/profile/" + fileName;
        } catch (IOException e) {
            log.error("프로필 사진 업로드 중 오류 발생", e);
            return null;
        }
    }
}
