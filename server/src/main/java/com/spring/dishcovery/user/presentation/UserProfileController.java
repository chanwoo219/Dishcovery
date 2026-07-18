package com.spring.dishcovery.user.presentation;

import com.spring.dishcovery.global.config.CookieUtil;
import com.spring.dishcovery.global.config.JwtUtil;
import com.spring.dishcovery.user.application.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProfileController {

    private final ProfileService profileService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    private static final String VERIFIED_KEY = "PROFILE_VERIFIED";
    private static final String VERIFIED_EMAIL_KEY = "PROFILE_VERIFIED_EMAIL";

    private String currentUserId(HttpServletRequest request) {
        String token = cookieUtil.getTokenFromCookies(request, "JWT_TOKEN");
        return token != null ? jwtUtil.getUserIdFromToken(token) : null;
    }

    /** 인증 페이지 */
    @GetMapping("/verify")
    public String verifyPage(HttpServletRequest request, HttpSession session, Model model) {
        if (currentUserId(request) == null) return "redirect:/dishcovery_login";

        model.addAttribute("verified", Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY)));
        model.addAttribute("targetEmail", session.getAttribute(VERIFIED_EMAIL_KEY));
        return "user/verify";
    }

    /** 인증 코드 보내기 */
    @PostMapping("/send-code")
    public String sendCode(@RequestParam("targetEmail") String targetEmail,
                           HttpServletRequest request,
                           HttpSession session,
                           Model model) {
        String userId = currentUserId(request);
        if (userId == null) return "redirect:/dishcovery_login";

        profileService.sendVerificationCode(userId, targetEmail);
        session.setAttribute(VERIFIED_EMAIL_KEY, targetEmail);

        model.addAttribute("msg", "인증 코드가 전송되었습니다.");
        model.addAttribute("targetEmail", targetEmail);
        model.addAttribute("verified", false);
        return "user/verify";
    }

    /** 인증 코드 검증 */
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam("targetEmail") String targetEmail,
                             @RequestParam("code") String code,
                             HttpServletRequest request,
                             HttpSession session,
                             Model model) {
        String userId = currentUserId(request);
        if (userId == null) return "redirect:/dishcovery_login";

        String savedEmail = (String) session.getAttribute(VERIFIED_EMAIL_KEY);
        if (savedEmail == null || !savedEmail.equals(targetEmail)) {
            session.removeAttribute(VERIFIED_KEY);
            model.addAttribute("msg", "인증 이메일이 일치하지 않습니다. 다시 인증해주세요.");
            model.addAttribute("targetEmail", targetEmail);
            model.addAttribute("verified", false);
            return "user/verify";
        }

        boolean ok = profileService.verifyCode(userId, code);
        if (!ok) {
            session.removeAttribute(VERIFIED_KEY);
            model.addAttribute("msg", "인증 코드가 올바르지 않습니다.");
            model.addAttribute("targetEmail", targetEmail);
            model.addAttribute("verified", false);
            return "user/verify";
        }

        session.setAttribute(VERIFIED_KEY, true);
        model.addAttribute("msg", "인증 완료! 변경 페이지로 이동할 수 있어요.");
        model.addAttribute("targetEmail", targetEmail);
        model.addAttribute("verified", true);
        return "user/verify";
    }

    /** 이메일 변경 페이지 (인증 필수) */
    @GetMapping("/email")
    public String emailChangePage(HttpServletRequest request, HttpSession session) {
        if (currentUserId(request) == null) return "redirect:/dishcovery_login";
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/user/verify";
        }
        return "user/changeEmail";
    }

    /** 이메일 변경 저장 */
    @PostMapping("/email")
    public String changeEmail(@RequestParam("newEmail") String newEmail,
                              HttpServletRequest request,
                              HttpSession session,
                              Model model) {
        String userId = currentUserId(request);
        if (userId == null) return "redirect:/dishcovery_login";
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/user/verify";
        }

        profileService.changeEmail(userId, newEmail);

        session.removeAttribute(VERIFIED_KEY);
        session.removeAttribute(VERIFIED_EMAIL_KEY);

        model.addAttribute("msg", "이메일이 변경되었습니다.");
        return "user/changeEmail";
    }

    /** 비밀번호 변경 페이지 (인증 필수) */
    @GetMapping("/password")
    public String passwordChangePage(HttpServletRequest request, HttpSession session) {
        if (currentUserId(request) == null) return "redirect:/dishcovery_login";
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/user/verify";
        }
        return "user/changePassword";
    }

    /** 비밀번호 변경 저장 */
    @PostMapping("/password")
    public String changePassword(@RequestParam("newPassword") String newPassword,
                                 @RequestParam("newPasswordConfirm") String newPasswordConfirm,
                                 HttpServletRequest request,
                                 HttpSession session,
                                 Model model) {
        String userId = currentUserId(request);
        if (userId == null) return "redirect:/dishcovery_login";
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/user/verify";
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            model.addAttribute("msg", "비밀번호 확인이 일치하지 않습니다.");
            return "user/changePassword";
        }

        profileService.changePassword(userId, newPassword);

        session.removeAttribute(VERIFIED_KEY);
        session.removeAttribute(VERIFIED_EMAIL_KEY);

        model.addAttribute("msg", "비밀번호가 변경되었습니다.");
        return "user/changePassword";
    }
}
