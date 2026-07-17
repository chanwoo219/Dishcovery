package com.spring.dishcovery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadPath;

    public WebConfig() {
        // 배포 환경: 서버 외부 절대 경로 (환경에 맞게 변경)
        String externalPath = "/var/www/dishcovery/uploads/";

        // 개발 환경: 프로젝트 루트 아래 uploads
        String devPath = System.getProperty("user.dir") + "/uploads/";

        // 외부 경로가 쓰기 가능한지 체크
        File externalDir = new File(externalPath);
        if (externalDir.exists() || externalDir.mkdirs()) {
            uploadPath = externalPath;
        } else {
            // 외부 경로 못 쓰면 개발용 경로 사용
            File devDir = new File(devPath);
            if (!devDir.exists()) devDir.mkdirs();
            uploadPath = devPath;
        }

        System.out.println("Uploads folder set to: " + uploadPath);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    public String getUploadPath() {
        return uploadPath;
    }
}
