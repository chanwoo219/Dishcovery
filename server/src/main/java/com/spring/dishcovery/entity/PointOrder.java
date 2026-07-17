package com.spring.dishcovery.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointOrder {
    private Long orderId;
    private String userId;
    private Long productId;
    private int qty;
    private int totalPoints;
    private String status; // ORDERED / CANCELLED
    private String orderedAt;
}
