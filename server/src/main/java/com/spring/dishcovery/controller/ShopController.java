package com.spring.dishcovery.controller;

import com.spring.dishcovery.config.CookieUtil;
import com.spring.dishcovery.config.JwtUtil;
import com.spring.dishcovery.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @GetMapping("/shop")
    public String shopList(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        // 1) 토큰 체크
        String token = cookieUtil.getTokenFromCookies(request, "JWT_TOKEN");
        if (token == null || token.isBlank()) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/dishcovery_login";
        }

        // 2) 토큰에서 userId 추출 (여기서 터져도 로그인으로)
        String userId;
        try {
            userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null || userId.isBlank()) {
                redirectAttributes.addFlashAttribute("loginMessage", "로그인 정보가 유효하지 않습니다. 다시 로그인 해주세요.");
                return "redirect:/dishcovery_login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인 정보가 만료되었거나 유효하지 않습니다. 다시 로그인 해주세요.");
            return "redirect:/dishcovery_login";
        }

        model.addAttribute("loginUserId", userId);

        // 3) 상품 조회 (절대 null/예외로 화면 죽지 않게)
        try {
            var products = shopService.getProductsOrderByPointDesc();
            if (products == null) products = Collections.emptyList();
            model.addAttribute("products", products);
        } catch (Exception e) {
            model.addAttribute("products", Collections.emptyList());
            model.addAttribute("errorMessage", "상품 목록을 불러오지 못했습니다. (서버/DB 확인 필요)");
            // 필요하면 로그 찍기
            e.printStackTrace();
        }

        return "shop/ShopList";
    }

    @GetMapping("/shop/product/{productId}")
    public String productDetail(@PathVariable String productId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String userId;
        try {
            userId = shopService.getLoginUserId(request);
            if (userId == null || userId.isBlank()) {
                redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
                return "redirect:/dishcovery_login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/dishcovery_login";
        }

        try {
            model.addAttribute("product", shopService.getProduct(productId));
        } catch (Exception e) {
            model.addAttribute("product", null);
            model.addAttribute("errorMessage", "상품 정보를 불러오지 못했습니다.");
            e.printStackTrace();
        }

        try {
            model.addAttribute("myPoint", shopService.getUserPoint(userId));
        } catch (Exception e) {
            model.addAttribute("myPoint", 0);
            e.printStackTrace();
        }

        try {
            var recommended = shopService.listRecommended(productId);
            if (recommended == null) recommended = Collections.emptyList();
            model.addAttribute("recommended", recommended);
        } catch (Exception e) {
            model.addAttribute("recommended", Collections.emptyList());
            e.printStackTrace();
        }

        return "shop/productDetail";
    }

    @PostMapping("/shop/purchase")
    public String purchase(@RequestParam String productId,
                           @RequestParam(defaultValue = "1") int qty,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {

        String userId;
        try {
            userId = shopService.getLoginUserId(request);
            if (userId == null || userId.isBlank()) {
                redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
                return "redirect:/dishcovery_login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/dishcovery_login";
        }

        try {
            shopService.purchase(userId, productId, qty);
            return "redirect:/shop/product/" + productId + "?purchased=1";
        } catch (IllegalArgumentException e) {
            return "redirect:/shop/product/" + productId + "?error=" + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/shop/product/" + productId + "?error=구매 처리 중 오류가 발생했습니다";
        }
    }
}
