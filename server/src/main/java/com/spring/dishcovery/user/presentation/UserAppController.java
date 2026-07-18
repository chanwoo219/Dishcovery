package com.spring.dishcovery.user.presentation;

import com.spring.dishcovery.global.config.JwtUtil;
import com.spring.dishcovery.user.application.ProfileService;
import com.spring.dishcovery.user.application.UserService;
import com.spring.dishcovery.user.domain.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserAppController {

    private final UserService userService;
    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    private String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return jwtUtil.getUserIdFromToken(authHeader.substring(7));
    }

    // 본인 프로필만 이메일/포인트 포함
    private Map<String, Object> toSelfMap(UserEntity u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", u.getUserId());
        map.put("userName", u.getUserName());
        map.put("userMail", u.getUserMail());
        map.put("userImgPath", u.getUserImgPath());
        map.put("pointBalance", u.getPointBalance());
        return map;
    }

    // 다른 유저에게는 이메일을 노출하지 않음
    private Map<String, Object> toPublicMap(UserEntity u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", u.getUserId());
        map.put("userName", u.getUserName());
        map.put("userImgPath", u.getUserImgPath());
        return map;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = extractUserId(authHeader);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UserEntity user = userService.findByUserId(userId);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toSelfMap(user));
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<Map<String, Object>>> recommended(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = extractUserId(authHeader);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Map<String, Object>> list = userService.findRecommUser(userId).stream()
                .map(this::toPublicMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> publicProfile(@PathVariable String userId) {
        UserEntity user = userService.findByUserId(userId);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toPublicMap(user));
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("image") MultipartFile image) {
        String userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            String path = profileService.changeProfileImage(userId, image);
            return ResponseEntity.ok(Map.of("message", "프로필 사진이 변경되었습니다.", "imgUrl", path));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/nickname")
    public ResponseEntity<Map<String, String>> changeNickname(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        String userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            profileService.changeNickname(userId, body.get("userName"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        String trimmed = body.get("userName").trim();
        // 앱 헤더의 닉네임도 JWT 토큰 claim 기반이라 새 토큰을 함께 내려줌
        String token = jwtUtil.generateToken(userId, trimmed);
        return ResponseEntity.ok(Map.of(
                "message", "닉네임이 변경되었습니다.",
                "token", token,
                "userName", trimmed
        ));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, String>> withdraw(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        String userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        boolean ok = profileService.withdraw(userId, body.get("password"));
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", "비밀번호가 일치하지 않습니다."));
        }
        return ResponseEntity.ok(Map.of("message", "탈퇴가 완료되었습니다."));
    }
}
