package com.spring.dishcovery.shop.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopInquiry {
    private Long inquiryId;
    private String productId;
    private String userId;
    private String content;
    private LocalDateTime createdAt;
}
