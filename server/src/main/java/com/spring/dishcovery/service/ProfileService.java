package com.spring.dishcovery.service;

import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    public void sendVerificationCode(String email) {
        // TODO: 너가 쓰는 이메일 발송 로직 호출
        // 예) emailService.sendCode(email);
    }

    public boolean verifyCode(String email, String code) {
        // TODO: 너가 쓰는 인증코드 검증 로직 호출
        // 예) return emailService.verify(email, code);
        return true;
    }

    public void changeEmail(String newEmail) {
        // TODO: 로그인 유저 기준으로 DB 업데이트
        // 예) userMapper.updateEmail(userId, newEmail);
    }

    public void changePassword(String newPassword) {
        // TODO: 비밀번호 암호화 후 업데이트
        // 예) userMapper.updatePassword(userId, passwordEncoder.encode(newPassword));
    }
}