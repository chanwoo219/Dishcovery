package com.spring.dishcovery.controller;

import com.spring.dishcovery.entity.CodeVO;
import com.spring.dishcovery.entity.DataRequest;
import com.spring.dishcovery.entity.UserEntity;
import com.spring.dishcovery.service.ApiService;
import com.spring.dishcovery.service.CodeService;
import com.spring.dishcovery.service.UserService;
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
            //return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공!");
           //return ResponseEntity.ok("회원가입 성공!");
        } else {
            response.put("message","회원가입실패!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패!");
        }

    }

    @GetMapping("/categoryList")
    public List<CodeVO> getCategoriList() {
        return codeService.codeList("CTG");
    }

}