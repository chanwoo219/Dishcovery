package com.spring.dishcovery.user.application;

import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.user.domain.mapper.PasswordResetMapper;
import com.spring.dishcovery.user.domain.mapper.UserMapper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    private final PasswordResetMapper resetMapper;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetMapper resetMapper,
                                UserMapper userMapper,
                                JavaMailSender mailSender,
                                PasswordEncoder passwordEncoder) {
        this.resetMapper = resetMapper;
        this.userMapper = userMapper;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    /** 1️⃣ 인증코드 발송 (이메일만 사용) */
    public void sendResetCode(String userMail) {
        UserEntity user = userMapper.findByUserMail(userMail);
        if (user == null) return;
        if (!"Y".equalsIgnoreCase(user.getUserStatus())) return;

        String code = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        resetMapper.insertCode(user.getUserId(), userMail, code, expiresAt);
        sendMail(userMail, code);
    }

    /** 2️⃣ 코드 검증만 (비밀번호는 아직 바꾸지 않음) */
    public boolean verifyResetCode(String userMail, String code) {
        UserEntity user = userMapper.findByUserMail(userMail);
        if (user == null) return false;
        return resetMapper.findUserIdByValidCode(user.getUserId(), code) != null;
    }

    /** 3️⃣ 코드 검증 + 비밀번호 변경 */
    public boolean resetPasswordByMail(String userMail, String code, String newPassword) {
        UserEntity user = userMapper.findByUserMail(userMail);
        if (user == null) return false;

        String validUserId =
                resetMapper.findUserIdByValidCode(user.getUserId(), code);

        if (validUserId == null) return false;

        String encoded = passwordEncoder.encode(newPassword);
        userMapper.updateUserPassword(user.getUserId(), encoded);
        resetMapper.markCodeUsed(user.getUserId(), code);
        return true;
    }

    private void sendMail(String to, String code) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[Dishcovery] 비밀번호 재설정 인증코드");
            helper.setText(
                    "<p>아래 인증코드를 입력해주세요. (10분 유효)</p>" +
                            "<h1 style='letter-spacing:4px'>" + code + "</h1>",
                    true
            );
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
