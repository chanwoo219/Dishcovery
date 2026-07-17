package com.spring.dishcovery.controller;

import com.spring.dishcovery.service.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    private final PasswordResetService resetService;

    public PasswordResetController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    /** 1️⃣ 이메일 입력 */
    @GetMapping("/reset_password")
    public String resetPage() {
        return "user/reset_request";
    }

    /** 2️⃣ 인증코드 발송 */
    @PostMapping("/reset_password")
    public String sendCode(@RequestParam String userMail,
                           RedirectAttributes ra) {
        resetService.sendResetCode(userMail);
        ra.addFlashAttribute("msg", "인증코드를 이메일로 보냈습니다.");
        ra.addFlashAttribute("userMail", userMail);
        return "redirect:/reset_password/verify";
    }

    /** 3️⃣ 코드 + 새 비밀번호 입력 */
    @GetMapping("/reset_password/verify")
    public String verifyPage() {
        return "user/reset_verify";
    }

    /** 4️⃣ 검증 */
    @PostMapping("/reset_password/verify")
    public String verify(@RequestParam String userMail,
                         @RequestParam String code,
                         @RequestParam String newPassword,
                         @RequestParam String confirmPassword,
                         Model model,
                         RedirectAttributes ra) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("msg", "비밀번호가 일치하지 않습니다.");
            return "user/reset_verify";
        }

        boolean ok = resetService.resetPasswordByMail(userMail, code, newPassword);
        if (!ok) {
            model.addAttribute("msg", "인증코드가 올바르지 않거나 만료되었습니다.");
            return "user/reset_verify";
        }

        ra.addAttribute("gubun", "login");
        ra.addFlashAttribute("msg", "비밀번호가 변경되었습니다. 로그인 해주세요.");
        return "redirect:/dishcovery_login";
    }
}
