package com.spring.dishcovery.user.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PasswordResetMapper {

    int insertCode(@Param("userId") String userId,
                   @Param("userMail") String userMail,
                   @Param("code") String code,
                   @Param("expiresAt") LocalDateTime expiresAt);

    String findUserIdByValidCode(@Param("userId") String userId,
                                 @Param("code") String code);

    int markCodeUsed(@Param("userId") String userId,
                     @Param("code") String code);
}
