package com.spring.dishcovery.mapper;

import com.spring.dishcovery.entity.EmailVerificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailVerificationMapper {
    int insertVerification(EmailVerificationEntity entity);

    Integer countValidCode(@Param("userId") String userId, @Param("code") String code);

    int markVerified(@Param("userId") String userId, @Param("code") String code);
}
