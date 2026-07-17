package com.spring.dishcovery.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
                System.out.println("[FileUploadUtil] 디렉토리 생성: " + uploadPath);
            }

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                // ✅ 크기 제한
                if (file.getSize() > MAX_FILE_SIZE) {
                    System.err.println("[FileUploadUtil] 파일 크기 초과: " + file.getOriginalFilename());
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

                System.out.println("[FileUploadUtil] 업로드 완료: " + webPath);
            }

        } catch (IOException e) {
            System.err.println("[FileUploadUtil] 파일 업로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }

        return filePaths;
    }
}
