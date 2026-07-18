package com.spring.dishcovery.global.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public String getTokenFromCookies(HttpServletRequest request, String tokenName) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(tokenName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
