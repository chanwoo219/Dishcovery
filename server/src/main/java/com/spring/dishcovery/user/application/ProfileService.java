package com.spring.dishcovery.user.application;

import com.spring.dishcovery.global.util.FileUploadUtil;
import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.user.domain.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

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

    /** 프로필 사진 업로드 후 경로 저장. 업로드 실패 시 예외. */
    public String changeProfileImage(String userId, MultipartFile image) {
        String path = FileUploadUtil.saveProfileImage(image, uploadDir, userId);
        if (path == null) {
            throw new IllegalArgumentException("이미지 업로드에 실패했습니다.");
        }
        userMapper.updateUserImgPath(userId, path);
        return path;
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
