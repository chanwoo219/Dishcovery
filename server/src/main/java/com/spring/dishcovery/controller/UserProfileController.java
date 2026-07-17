package com.spring.dishcovery.controller;

import com.spring.dishcovery.service.ProfileService;
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

    private static final String VERIFIED_KEY = "PROFILE_VERIFIED";
    private static final String VERIFIED_EMAIL_KEY = "PROFILE_VERIFIED_EMAIL";

    @GetMapping("/verify")
    public String verifyPage(HttpSession session, Model model) {
        model.addAttribute("verified", Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY)));
        model.addAttribute("targetEmail", session.getAttribute(VERIFIED_EMAIL_KEY));
        return "user/verify";
    }

    @PostMapping("/send-code")
    public String sendCode(@RequestParam("targetEmail") String targetEmail,
                           HttpSession session,
                           Model model) {
        profileService.sendVerificationCode(targetEmail);
        session.setAttribute(VERIFIED_EMAIL_KEY, targetEmail);

        model.addAttribute("msg", "인증 코드가 전송되었습니다.");
        model.addAttribute("targetEmail", targetEmail);
        model.addAttribute("verified", false);
        return "user/verify";
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam("targetEmail") String targetEmail,
                             @RequestParam("code") String code,
                             HttpSession session,
                             Model model) {
        String savedEmail = (String) session.getAttribute(VERIFIED_EMAIL_KEY);
        if (savedEmail == null || !savedEmail.equals(targetEmail)) {
            session.removeAttribute(VERIFIED_KEY);
            model.addAttribute("msg", "인증 이메일이 일치하지 않습니다. 다시 인증해주세요.");
            model.addAttribute("targetEmail", targetEmail);
            model.addAttribute("verified", false);
            return "user/verify";
        }

        boolean ok = profileService.verifyCode(targetEmail, code);
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

    @GetMapping("/email")
    public String emailChangePage(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/user/verify";
        }
        return "user/changeEmail";
    }

    @PostMapping("/email")
    public String changeEmail(@RequestParam("newEmail") String newEmail,
                              HttpSession session,
                              Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/user/verify";
        }

        profileService.changeEmail(newEmail);

        // 1회 변경 후 인증 초기화(보안)
        session.removeAttribute(VERIFIED_KEY);
        session.removeAttribute(VERIFIED_EMAIL_KEY);

        model.addAttribute("msg", "이메일이 변경되었습니다.");
        return "user/changeEmail";
    }

    @GetMapping("/password")
    public String passwordChangePage(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/user/verify";
        }
        return "user/changePassword";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam("newPassword") String newPassword,
                                 @RequestParam("newPasswordConfirm") String newPasswordConfirm,
                                 HttpSession session,
                                 Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/user/verify";
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            model.addAttribute("msg", "비밀번호 확인이 일치하지 않습니다.");
            return "user/changePassword";
        }

        profileService.changePassword(newPassword);

        session.removeAttribute(VERIFIED_KEY);
        session.removeAttribute(VERIFIED_EMAIL_KEY);

        model.addAttribute("msg", "비밀번호가 변경되었습니다.");
        return "user/changePassword";
    }
}
