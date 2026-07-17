package com.spring.dishcovery.controller;

import com.spring.dishcovery.config.JwtUtil;
import com.spring.dishcovery.entity.UserEntity;
import com.spring.dishcovery.service.EmailVerificationService;
import com.spring.dishcovery.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailVerificationService emailVerificationService;

    // 로그인/회원가입 페이지
    @GetMapping("/dishcovery_login")
    public String dishcoveryLogin(@RequestParam String gubun, Model model) {

        String loginTab = "";
        String signUpTab = "";
        String loginPanel = "";
        String signUpPanel = "";

        if ("login".equals(gubun)) {
            loginTab = "tab active";
            signUpTab = "tab";
            loginPanel = "panel show";
            signUpPanel = "panel";
        } else {
            loginTab = "tab";
            signUpTab = "tab active";
            loginPanel = "panel";
            signUpPanel = "panel show";
        }

        model.addAttribute("loginTab", loginTab);
        model.addAttribute("signUpTab", signUpTab);
        model.addAttribute("loginPanel", loginPanel);
        model.addAttribute("signUpPanel", signUpPanel);

        return "user/login";
    }

    // 회원가입
    @PostMapping("/save_user")
    public String saveUser(@ModelAttribute UserEntity user, RedirectAttributes redirectAttributes) {

        // ✅ 1) 아이디 중복 체크 (DB에 중복 있으면 selectOne() 터지기 때문에 사전 차단)
        UserEntity existId = userService.findByUserId(user.getUserId());
        if (existId != null) {
            redirectAttributes.addAttribute("gubun", "signup");
            redirectAttributes.addFlashAttribute("msg", "이미 존재하는 아이디입니다.");
            return "redirect:/dishcovery_login";
        }

        // ✅ 2) 저장 (UserService에서 이메일 중복이면 -2 리턴하도록 이미 구현됨)
        int result = userService.saveUserData(user);

        if (result > 0) {
            // 가입 성공 → 이메일 인증 코드 발송 → 인증 페이지로 이동
            emailVerificationService.sendVerificationCode(user);
            redirectAttributes.addAttribute("userId", user.getUserId());
            redirectAttributes.addFlashAttribute("msg", "회원가입이 완료되었습니다. 이메일 인증 코드를 확인해주세요.");
            return "redirect:/verify";
        }

        // ✅ 이메일 중복
        if (result == -2) {
            redirectAttributes.addAttribute("gubun", "signup");
            redirectAttributes.addFlashAttribute("msg", "이미 가입된 이메일입니다.");
            return "redirect:/dishcovery_login";
        }

        // 기타 실패
        redirectAttributes.addAttribute("gubun", "signup");
        redirectAttributes.addFlashAttribute("msg", "회원가입이 실패하였습니다.");
        return "redirect:/dishcovery_login";
    }

    // 로그인
    @PostMapping("/userLogin")
    public String userLogin(@RequestParam String userId,
                            @RequestParam String userPswd,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {

        // 1) 사용자 존재 확인
        UserEntity exist = userService.findByUserId(userId);
        if (exist == null) {
            redirectAttributes.addAttribute("gubun", "login");
            redirectAttributes.addFlashAttribute("msg", "로그인 실패 아이디 또는 비밀번호 확인.");
            return "redirect:/dishcovery_login";
        }

        // 2) 이메일 인증 여부
        if (exist.getUserStatus() != null && !"Y".equalsIgnoreCase(exist.getUserStatus())) {
            redirectAttributes.addAttribute("userId", userId);
            redirectAttributes.addFlashAttribute("msg", "이메일 인증 후 로그인할 수 있습니다.");
            return "redirect:/verify";
        }

        // 3) 비밀번호 확인
        UserEntity user = userService.getUserData(userId, userPswd);
        if (user != null) {
            // JWT 발급
            String token = jwtUtil.generateToken(user.getUserId(), user.getUserName());

            // JWT를 HttpOnly 쿠키로 브라우저에 저장
            Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600);
            response.addCookie(jwtCookie);

            return "redirect:/MainPage";
        }

        redirectAttributes.addAttribute("gubun", "login");
        redirectAttributes.addFlashAttribute("msg", "로그인 실패 아이디 또는 비밀번호 확인.");
        return "redirect:/dishcovery_login";
    }

    @GetMapping("/verify")
    public String verifyPage(@RequestParam String userId, Model model) {
        UserEntity user = userService.findByUserId(userId);
        model.addAttribute("userId", userId);
        if (user != null) {
            model.addAttribute("userMail", user.getUserMail());
        }
        return "user/verify";
    }

    @PostMapping("/verify_email")
    public String verifyEmail(@RequestParam String userId,
                              @RequestParam String code,
                              RedirectAttributes redirectAttributes) {
        boolean ok = emailVerificationService.verifyCode(userId, code);
        if (ok) {
            userService.updateUserStatus(userId, "Y");
            redirectAttributes.addAttribute("gubun", "login");
            redirectAttributes.addFlashAttribute("msg", "이메일 인증이 완료되었습니다. 로그인 해주세요.");
            return "redirect:/dishcovery_login";
        }
        redirectAttributes.addAttribute("userId", userId);
        redirectAttributes.addFlashAttribute("msg", "인증 코드가 올바르지 않거나 만료되었습니다.");
        return "redirect:/verify";
    }

    @PostMapping("/resend_code")
    public String resendCode(@RequestParam String userId, RedirectAttributes redirectAttributes) {
        UserEntity user = userService.findByUserId(userId);
        if (user != null) {
            emailVerificationService.sendVerificationCode(user);
        }
        redirectAttributes.addAttribute("userId", userId);
        redirectAttributes.addFlashAttribute("msg", "인증 코드를 다시 보냈습니다.");
        return "redirect:/verify";
    }
}
