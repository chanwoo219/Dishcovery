package com.spring.dishcovery.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserEntity {

    private String userMail;

    private String userName;

    private String userId;

    private String userPswd;

    // USER_MASTER.USER_STATUS (Y: 활성/인증완료, N: 이메일 인증 대기)
    private String userStatus;

    private String userImgPath;

    // USER_MASTER.POINT_BALANCE
    private int pointBalance;

}
