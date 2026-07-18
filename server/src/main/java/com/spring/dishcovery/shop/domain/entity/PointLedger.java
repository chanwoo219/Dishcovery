package com.spring.dishcovery.shop.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointLedger {
    private Long ledgerId;
    private String userId;
    private String type; // EARN_VIEW / SPEND_PURCHASE
    private Integer amount; // +/-
    private String refType; // RECIPE / ORDER
    private String refId;
    private String createdAt;
}
