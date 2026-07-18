package com.spring.dishcovery.user.application;

import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.user.domain.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public int saveUserData(UserEntity user) {

        try {
            // ✅ 이메일 중복이면 회원가입 막기
            int mailCnt = userMapper.countByUserMail(user.getUserMail());
            if (mailCnt > 0) {
                return -2; // 이메일 중복
            }

            String encodedPassword = passwordEncoder.encode(user.getUserPswd());
            user.setUserPswd(encodedPassword);

            return userMapper.saveUserData(user);

        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage(), e);
            return -1;
        }
    }


    public UserEntity getUserData(String userId, String userPswd) {

        UserEntity user = userMapper.findByUserId(userId);

        if(user != null && passwordEncoder.matches(userPswd, user.getUserPswd())) {
            // 이메일 인증 대기(USER_STATUS=N)면 로그인 불가
            if (user.getUserStatus() != null && !"Y".equalsIgnoreCase(user.getUserStatus())) {
                return null;
            }
            return user;
        }
        return null;
    }

    public int updateUserStatus(String userId, String userStatus) {
        return userMapper.updateUserStatus(userId, userStatus);
    }

    public UserEntity findByUserId(String userId) {
        return  userMapper.findByUserId(userId);
    }

    public boolean isEmailTaken(String userMail) {
        return userMapper.countByUserMail(userMail) > 0;
    }

    public int updateUserEmail(String userId, String userMail) {
        return userMapper.updateUserEmail(userId, userMail);
    }

    public int updateUserPassword(String userId, String newRawPassword) {
        String encoded = passwordEncoder.encode(newRawPassword);
        return userMapper.updateUserPassword(userId, encoded);
    }

    public List<UserEntity> findRecommUser(String userId) {
        return userMapper.findRecommUser(userId);

    }



}
