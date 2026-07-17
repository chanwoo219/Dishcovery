package com.spring.dishcovery.mapper;

import com.spring.dishcovery.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    int saveUserData(UserEntity user);

    UserEntity findByUserId(String userId);

    int updateUserStatus(@Param("userId") String userId, @Param("userStatus") String userStatus);

    UserEntity findByUserMail(@Param("userMail") String userMail);

    int updateUserPassword(@Param("userId") String userId,
                           @Param("userPswd") String userPswd);

    int updateUserEmail(@Param("userId") String userId,
                        @Param("userMail") String userMail);

    int addUserPoints(@Param("userId") String userId,
                      @Param("amount") int amount);

    int deductUserPoints(@Param("userId") String userId,
                         @Param("amount") int amount);
    int countByUserMail(@Param("userMail") String userMail);

    List<UserEntity> findRecommUser(String userId);
}
