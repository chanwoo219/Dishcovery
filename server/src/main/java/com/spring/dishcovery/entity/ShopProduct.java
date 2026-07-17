package com.spring.dishcovery.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopProduct {
    private String productId;
    private String productName;
    private int productPoint;
    private LocalDateTime createdAt;
    private String mainImage;  // 이미지 URL 필드 추가
}