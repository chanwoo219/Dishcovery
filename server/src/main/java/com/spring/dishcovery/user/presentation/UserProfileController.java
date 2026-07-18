package com.spring.dishcovery.user.presentation;

import com.spring.dishcovery.global.config.CookieUtil;
import com.spring.dishcovery.global.config.JwtUtil;
import com.spring.dishcovery.recipe.application.RecipeAppService;
import com.spring.dishcovery.user.application.ProfileService;
import com.spring.dishcovery.user.application.UserService;
import com.spring.dishcovery.user.domain.entity.UserEntity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final RecipeAppService recipeAppService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    private String currentUserId(HttpServletRequest request) {
        String token = cookieUtil.getTokenFromCookies(request, "JWT_TOKEN");
        return token != null ? jwtUtil.getUserIdFromToken(token) : null;
    }

    /** 다른 유저의 공개 프로필 */
    @GetMapping("/{userId}/profile")
    public String publicProfile(@PathVariable String userId, HttpServletRequest request, Model model) {
        if (currentUserId(request) == null) return "redirect:/dishcovery_login";

        UserEntity profileUser = userService.findByUserId(userId);
        if (profileUser == null) {
            return "redirect:/myPage";
        }

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("recipes", recipeAppService.getMyRecipes(userId));
        return "user/PublicProfile";
    }

    /** 닉네임 변경 페이지 */
    @GetMapping("/nickname")
    public String nicknameChangePage(HttpServletRequest request, Model model) {
        String userId = currentUserId(request);
        if (userId == null) return "redirect:/dishcovery_login";

        UserEntity user = userService.findByUserId(userId);
        model.addAttribute("currentName", user != null ? user.getUserName() : "");
        return "user/changeNickname";
    }

    /** 닉네임 변경 저장 */
    @PostMapping("/nickname")
    public String changeNickname(@RequestParam("userName") String userName,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model) {
        String userId = currentUserId(request);
        if (userId == null) return "redirect:/dishcovery_login";

        if (userName == null || userName.isBlank()) {
            model.addAttribute("msg", "닉네임을 입력해주세요.");
            model.addAttribute("currentName", userName);
            return "user/changeNickname";
        }

        String trimmed = userName.trim();
        profileService.changeNickname(userId, trimmed);

        // 헤더에 표시되는 닉네임은 JWT 토큰의 claim이라 바로 반영되도록 토큰을 재발급
        String token = jwtUtil.generateToken(userId, trimmed);
        Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        response.addCookie(jwtCookie);

        model.addAttribute("msg", "닉네임이 변경되었습니다.");
        model.addAttribute("currentName", trimmed);
        return "user/changeNickname";
    }

    /** 회원 탈퇴 페이지 */
    @GetMapping("/withdraw")
    public String withdrawPage(HttpServletRequest request) {
        if (currentUserId(request) == null) return "redirect:/dishcovery_login";
        return "user/withdraw";
    }

    /** 회원 탈퇴 처리 (비밀번호 확인 필수) */
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("password") String password,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           Model model) {
        String userId = currentUserId(request);
        if (userId == null) return "redirect:/dishcovery_login";

        boolean ok = profileService.withdraw(userId, password);
        if (!ok) {
            model.addAttribute("msg", "비밀번호가 일치하지 않습니다.");
            return "user/withdraw";
        }

        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        return "redirect:/MainPage?withdrawn=1";
    }
}
