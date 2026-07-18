package com.spring.dishcovery.user.application;

import com.spring.dishcovery.user.domain.entity.EmailVerificationEntity;
import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.user.domain.mapper.EmailVerificationMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationMapper emailVerificationMapper;
    private final JavaMailSender mailSender;

    public void sendVerificationCode(UserEntity user) {
        String code = generateCode(6);

        EmailVerificationEntity entity = new EmailVerificationEntity();
        entity.setUserId(user.getUserId());
        entity.setUserMail(user.getUserMail());
        entity.setCode(code);

        emailVerificationMapper.insertVerification(entity);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getUserMail());
            message.setSubject("[Dishcovery] 이메일 인증 코드");
            message.setText("인증 코드: " + code + "\n\n10분 이내에 인증을 완료해주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage(), e);
            // 메일 전송 실패여도 DB에는 코드가 남아있을 수 있음 (필요시 롤백/삭제 로직 추가)
            throw e;
        }
    }

    public boolean verifyCode(String userId, String code) {
        Integer cnt = emailVerificationMapper.countValidCode(userId, code);
        if (cnt != null && cnt > 0) {
            emailVerificationMapper.markVerified(userId, code);
            return true;
        }
        return false;
    }

    private String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
