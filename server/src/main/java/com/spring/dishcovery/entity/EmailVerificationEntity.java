package com.spring.dishcovery.entity;

import lombok.Data;

@Data
public class EmailVerificationEntity {
    private String userId;
    private String userMail;
    private String code;
    private String expiresAt; // yyyy-MM-dd HH:mm:ss (DB 기준)
    private String verifiedYn; // Y/N
}
