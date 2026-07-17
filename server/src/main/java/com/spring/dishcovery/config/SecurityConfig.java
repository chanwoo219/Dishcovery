package com.spring.dishcovery.config;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
//@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    //HTTP 보안(인증·인가·CSRF·로그아웃·필터 등)을 모두 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) //WT토큰을 사용하는 REST API 방식이기에 disable 처리
                //요청권한 설정
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/MainPage",
                                        "/",           // 루트
                                        "/signup",
                                        "/login",
                                        "/logout",
                                        "/userLogin",
                                        "/SaveRecipeData",
                                        "/css/**",
                                        "/js/**",
                                        "/images/**",
                                        "/api/**"
                                ).permitAll()
                                .anyRequest().permitAll() // 개발 중엔 전체 허용
                        //.anyRequest().authenticated() // 나머지는 인증이되어야 접근가능

                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)


                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // JWT 쿠키 삭제
                            Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
                            jwtCookie.setHttpOnly(true);
                            jwtCookie.setPath("/");
                            jwtCookie.setMaxAge(0);
                            response.addCookie(jwtCookie);

                            // 메인 페이지로 리다이렉트
                            response.sendRedirect("/MainPage");
                        })
                )
                .formLogin(FormLoginConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable);
        //.httpBasic(basic -> basic.disable());
        //.formLogin(form -> form.disable())

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
