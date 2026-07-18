package com.spring.dishcovery.user.application;

import com.spring.dishcovery.user.domain.entity.EmailVerificationEntity;
import com.spring.dishcovery.user.domain.mapper.EmailVerificationMapper;
import com.spring.dishcovery.user.domain.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationMapper emailVerificationMapper;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public void sendVerificationCode(String userId, String targetEmail) {
        String code = generateCode();

        EmailVerificationEntity entity = new EmailVerificationEntity();
        entity.setUserId(userId);
        entity.setUserMail(targetEmail);
        entity.setCode(code);
        emailVerificationMapper.insertVerification(entity);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(targetEmail);
        message.setSubject("[Dishcovery] 인증 코드");
        message.setText("인증 코드: " + code + "\n\n10분 이내에 인증을 완료해주세요.");
        mailSender.send(message);
    }

    public boolean verifyCode(String userId, String code) {
        Integer cnt = emailVerificationMapper.countValidCode(userId, code);
        if (cnt != null && cnt > 0) {
            emailVerificationMapper.markVerified(userId, code);
            return true;
        }
        return false;
    }

    public void changeEmail(String userId, String newEmail) {
        userMapper.updateUserEmail(userId, newEmail);
    }

    public void changePassword(String userId, String newPassword) {
        userMapper.updateUserPassword(userId, passwordEncoder.encode(newPassword));
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }
}
