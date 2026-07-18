package com.spring.dishcovery.user.application;

import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.user.domain.mapper.ApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            e.printStackTrace();
            return 0;
        }
    }
}
