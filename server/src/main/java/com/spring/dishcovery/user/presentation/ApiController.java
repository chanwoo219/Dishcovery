package com.spring.dishcovery.user.presentation;

import com.spring.dishcovery.code.domain.entity.CodeVO;
import com.spring.dishcovery.user.application.DataRequest;
import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.user.application.ApiService;
import com.spring.dishcovery.code.application.CodeService;
import com.spring.dishcovery.user.application.EmailVerificationService;
import com.spring.dishcovery.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;
    private final CodeService codeService;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot! test완료";
    }

    @PostMapping("/data")
    public String receiveData(@RequestBody DataRequest request) {
        return "Received: " + request.getMessage();
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signupApi(@RequestBody UserEntity userVo) {
        // DB 저장 로직 (예: service.save(user))

        Map<String, String> response = new HashMap<>();


        // 1. 아이디 중복체크
        if (apiService.isUserIdExist(userVo.getUserId()) > 0) {
            response.put("message", "이미 존재하는 아이디입니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        int result = apiService.saveSignupApi(userVo);

        if (result > 0) {
            response.put("message", "회원가입 성공!");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("message","회원가입실패!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    /** 앱 회원가입 1단계: 계정 생성(미인증 상태) + 인증 메일 발송 */
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, String>> sendVerification(@RequestBody UserEntity userVo) {
        Map<String, String> response = new HashMap<>();

        if (apiService.isUserIdExist(userVo.getUserId()) > 0) {
            response.put("message", "이미 존재하는 아이디입니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        if (userService.isEmailTaken(userVo.getUserMail())) {
            response.put("message", "이미 가입된 이메일입니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        int result = userService.saveUserData(userVo);
        if (result <= 0) {
            response.put("message", "회원가입 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        emailVerificationService.sendVerificationCode(userVo);
        response.put("message", "인증 메일이 발송되었습니다.");
        return ResponseEntity.ok(response);
    }

    /** 앱 회원가입 2단계: 인증 코드 확인 후 계정 활성화 */
    @PostMapping("/verify-and-signup")
    public ResponseEntity<Map<String, String>> verifyAndSignup(@RequestBody Map<String, String> body) {
        Map<String, String> response = new HashMap<>();

        String userId = body.get("userId");
        String code = body.get("verificationCode");

        boolean ok = emailVerificationService.verifyCode(userId, code);
        if (!ok) {
            response.put("message", "인증 코드가 올바르지 않거나 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        userService.updateUserStatus(userId, "Y");
        response.put("message", "회원가입이 완료되었습니다!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categoryList")
    public List<CodeVO> getCategoriList() {
        return codeService.codeList("CTG");
    }

}