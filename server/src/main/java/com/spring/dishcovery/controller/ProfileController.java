package com.spring.dishcovery.controller;

import com.spring.dishcovery.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    private static final String VERIFIED_KEY = "PROFILE_VERIFIED";
    private static final String VERIFIED_EMAIL_KEY = "PROFILE_VERIFIED_EMAIL";

    /** 인증 페이지 */
    @GetMapping("/verify")
    public String verifyPage(HttpSession session, Model model) {
        // 이미 인증했으면 메뉴 페이지로 보내도 됨(원하면)
        model.addAttribute("verified", Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY)));
        return "profile/verify";
    }

    /** 인증 코드 보내기 */
    @PostMapping("/send-code")
    public String sendCode(@RequestParam("targetEmail") String targetEmail,
                           HttpSession session,
                           Model model) {
        profileService.sendVerificationCode(targetEmail);

        // 인증 시도 이메일 세션에 저장(검증 때 비교용)
        session.setAttribute(VERIFIED_EMAIL_KEY, targetEmail);

        model.addAttribute("msg", "인증 코드가 전송되었습니다.");
        model.addAttribute("targetEmail", targetEmail);
        return "profile/verify";
    }

    /** 인증 코드 검증 */
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam("targetEmail") String targetEmail,
                             @RequestParam("code") String code,
                             HttpSession session,
                             Model model) {
        String savedEmail = (String) session.getAttribute(VERIFIED_EMAIL_KEY);
        if (savedEmail == null || !savedEmail.equals(targetEmail)) {
            model.addAttribute("msg", "인증 이메일이 일치하지 않습니다. 다시 인증해주세요.");
            model.addAttribute("targetEmail", targetEmail);
            session.removeAttribute(VERIFIED_KEY);
            return "profile/verify";
        }

        boolean ok = profileService.verifyCode(targetEmail, code);
        if (!ok) {
            model.addAttribute("msg", "인증 코드가 올바르지 않습니다.");
            model.addAttribute("targetEmail", targetEmail);
            session.removeAttribute(VERIFIED_KEY);
            return "profile/verify";
        }

        session.setAttribute(VERIFIED_KEY, true);
        model.addAttribute("msg", "인증 완료! 이제 변경 페이지로 이동할 수 있어요.");
        model.addAttribute("targetEmail", targetEmail);
        model.addAttribute("verified", true);
        return "profile/verify";
    }

    /** 이메일 변경 페이지 (인증 필수) */
    @GetMapping("/email")
    public String emailChangePage(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/profile/verify";
        }
        return "profile/change_email";
    }

    /** 이메일 변경 저장 */
    @PostMapping("/email")
    public String changeEmail(@RequestParam("newEmail") String newEmail,
                              HttpSession session,
                              Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/profile/verify";
        }

        profileService.changeEmail(newEmail);

        // 변경 1번 하고 인증 초기화(보안)
        session.removeAttribute(VERIFIED_KEY);
        session.removeAttribute(VERIFIED_EMAIL_KEY);

        model.addAttribute("msg", "이메일이 변경되었습니다.");
        return "profile/change_email";
    }

    /** 비밀번호 변경 페이지 (인증 필수) */
    @GetMapping("/password")
    public String passwordChangePage(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/profile/verify";
        }
        return "profile/change_password";
    }

    /** 비밀번호 변경 저장 */
    @PostMapping("/password")
    public String changePassword(@RequestParam("newPassword") String newPassword,
                                 @RequestParam("newPasswordConfirm") String newPasswordConfirm,
                                 HttpSession session,
                                 Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(VERIFIED_KEY))) {
            return "redirect:/profile/verify";
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            model.addAttribute("msg", "비밀번호 확인이 일치하지 않습니다.");
            return "profile/change_password";
        }

        profileService.changePassword(newPassword);

        // 변경 1번 하고 인증 초기화(보안)
        session.removeAttribute(VERIFIED_KEY);
        session.removeAttribute(VERIFIED_EMAIL_KEY);

        model.addAttribute("msg", "비밀번호가 변경되었습니다.");
        return "profile/change_password";
    }
}
