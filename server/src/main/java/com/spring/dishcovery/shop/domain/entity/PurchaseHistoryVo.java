package com.spring.dishcovery.shop.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PurchaseHistoryVo {
    private Long purchaseId;
    private String productId;
    private String productName;
    private String mainImage;
    private int productPoint;
    private int qty;
    private LocalDateTime purchaseDate;
}
