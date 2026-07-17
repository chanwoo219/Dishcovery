package com.spring.dishcovery.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import com.spring.dishcovery.config.JwtUtil;

@ControllerAdvice   // 모든 컨트롤러에 공통 적용
public class GlobalModelAdvice {

    @Autowired
    private JwtUtil jwtUtil;

    @ModelAttribute
    public void addUserInfoToModel(HttpServletRequest request, Model model) {
        String token = null;

        // JWT 쿠키 찾기
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3. JWT 유효성 체크 / 토큰 검증
        if (token != null && jwtUtil.validateToken(token)) {
            String userId = jwtUtil.getUserIdFromToken(token);
            String userName = jwtUtil.getUserNameFromToken(token);

            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userId", userId);
            model.addAttribute("userName", userName);
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }
}
