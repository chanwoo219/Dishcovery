package com.spring.dishcovery.user.application;

import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.user.domain.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public static final int NICKNAME_MAX_LENGTH = 6;

    public void changeNickname(String userId, String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        String trimmed = newName.trim();
        if (trimmed.length() > NICKNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("닉네임은 최대 " + NICKNAME_MAX_LENGTH + "자까지 입력할 수 있습니다.");
        }
        userMapper.updateUserName(userId, trimmed);
    }

    /** 비밀번호 확인 후 회원 탈퇴 처리(소프트 삭제). 비밀번호가 틀리면 false. */
    public boolean withdraw(String userId, String password) {
        UserEntity user = userMapper.findByUserId(userId);
        if (user == null || !passwordEncoder.matches(password, user.getUserPswd())) {
            return false;
        }
        userMapper.updateUserStatus(userId, "W");
        return true;
    }
}
