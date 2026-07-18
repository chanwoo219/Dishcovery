package com.spring.dishcovery.shop.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopReview {
    private Long reviewId;
    private String productId;
    private String userId;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
}
