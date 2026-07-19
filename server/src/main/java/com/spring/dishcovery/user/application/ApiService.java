package com.spring.dishcovery.user.application;

import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.user.domain.mapper.ApiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiService {

    @Autowired
    private ApiMapper apiMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // 아이디 중복체크
    public int isUserIdExist(String userId) {

        int cnt = 0;
        cnt = apiMapper.countByUserId(userId);

        return cnt;
    }

    public int saveSignupApi(UserEntity userVo) {
        try {

            String encodedPassword = passwordEncoder.encode(userVo.getUserPswd());
            userVo.setUserPswd(encodedPassword);

            return apiMapper.saveSignupApi(userVo);

        }catch (Exception e) {
            log.error("회원가입 저장 실패: userId={}", userVo.getUserId(), e);
            return 0;
        }
    }
}
