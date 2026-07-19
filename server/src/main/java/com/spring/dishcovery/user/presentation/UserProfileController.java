package com.spring.dishcovery.user.presentation;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final RecipeAppService recipeAppService;
    private final JwtUtil jwtUtil;

    private String currentUserId(HttpServletRequest request) {
        return jwtUtil.getUserIdFromRequest(request);
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

    /** 프로필 사진 변경 */
    @PostMapping("/profile-image")
    public String uploadProfileImage(@RequestParam("image") MultipartFile image,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        String userId = currentUserId(request);
        if (userId == null) return "redirect:/dishcovery_login";

        try {
            profileService.changeProfileImage(userId, image);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
        }

        return "redirect:/myPage";
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

        String trimmed;
        try {
            profileService.changeNickname(userId, userName);
            trimmed = userName.trim();
        } catch (IllegalArgumentException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("currentName", userName);
            return "user/changeNickname";
        }

        // 헤더에 표시되는 닉네임은 JWT 토큰의 claim이라 바로 반영되도록 토큰을 재발급
        String token = jwtUtil.generateToken(userId, trimmed);
        Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(jwtUtil.getExpirationSeconds());
        response.addCookie(jwtCookie);

        return "redirect:/myPage";
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
