package com.spring.dishcovery.controller;

import com.spring.dishcovery.config.JwtUtil;
import com.spring.dishcovery.entity.UserEntity;
import com.spring.dishcovery.service.UserService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthAppController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // CORS 허용 (필요 시)
    @CrossOrigin(origins = "*")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserEntity request) {

        UserEntity user = userService.getUserData(request.getUserId(), request.getUserPswd());

        if (user == null || !passwordEncoder.matches(request.getUserPswd(), user.getUserPswd())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUserName());

        return ResponseEntity.ok(Map.of("token", token, "username", user.getUserName()));

    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader("Authorization") String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = header.substring(7);
        String username = jwtUtil.getUserNameFromToken(token);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(Map.of("username", username, "message", "프로필 조회 성공"));
    }


}
