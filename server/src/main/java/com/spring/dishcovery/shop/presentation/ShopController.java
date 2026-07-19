package com.spring.dishcovery.shop.presentation;

import com.spring.dishcovery.global.config.JwtUtil;
import com.spring.dishcovery.shop.application.ShopService;
import com.spring.dishcovery.shop.domain.entity.PurchaseHistoryVo;
import com.spring.dishcovery.shop.domain.entity.ShopProduct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final JwtUtil jwtUtil;

    @GetMapping("/shop")
    public String shopList(@RequestParam(required = false) String searchName,
                           @RequestParam(required = false, defaultValue = "1") int page,
                           Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null || userId.isBlank()) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/dishcovery_login";
        }

        model.addAttribute("loginUserId", userId);
        model.addAttribute("rcpClassNm", "seg-btn");
        model.addAttribute("rankClassNm", "seg-btn active");
        model.addAttribute("searchName", searchName);

        // ShopService가 실패 시 이미 빈 목록을 반환하므로 여기서는 그대로 사용한다.
        boolean hasSearch = searchName != null && !searchName.isBlank();
        var products = hasSearch
                ? shopService.searchProductsPaged(searchName, page)
                : shopService.getProductsPaged(page);
        model.addAttribute("products", products);

        int totalCount = hasSearch ? shopService.countSearchProducts(searchName) : shopService.countProducts();
        int totalPages = (int) Math.ceil(totalCount / (double) ShopService.PAGE_SIZE);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", Math.max(totalPages, 1));

        return "shop/ShopList";
    }

    @GetMapping("/shop/purchase-history")
    public String purchaseHistory(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String userId = shopService.getLoginUserId(request);
        if (userId == null || userId.isBlank()) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/dishcovery_login";
        }

        var purchases = shopService.listPurchaseHistory(userId);
        int totalQty = purchases.stream().mapToInt(PurchaseHistoryVo::getQty).sum();

        model.addAttribute("purchases", purchases);
        model.addAttribute("totalQty", totalQty);
        model.addAttribute("myPoint", shopService.getUserPoint(userId));

        return "shop/PurchaseHistory";
    }

    @GetMapping("/shop/product/{productId}")
    public String productDetail(@PathVariable String productId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String userId = shopService.getLoginUserId(request);
        if (userId == null || userId.isBlank()) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/dishcovery_login";
        }

        // ShopService의 조회 메서드들은 실패 시 이미 로그를 남기고 null/빈 값을 반환하므로 여기서 다시 감싸지 않는다.
        ShopProduct product = shopService.getProduct(productId);
        model.addAttribute("product", product);
        if (product == null) {
            model.addAttribute("errorMessage", "상품 정보를 불러오지 못했습니다.");
        }

        model.addAttribute("myPoint", shopService.getUserPoint(userId));
        model.addAttribute("hasPurchased", shopService.hasPurchased(userId, productId));
        model.addAttribute("recommended", shopService.listRecommended(productId));

        var reviews = shopService.listReviews(productId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", shopService.getAverageRating(reviews));
        model.addAttribute("inquiries", shopService.listInquiries(productId));

        return "shop/ProductDetail";
    }

    @PostMapping("/shop/product/{productId}/review")
    public String addReview(@PathVariable String productId,
                            @RequestParam int rating,
                            @RequestParam String content,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        String userId = shopService.getLoginUserId(request);
        if (userId == null || userId.isBlank()) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/dishcovery_login";
        }

        try {
            shopService.addReview(userId, productId, rating, content);
        } catch (IllegalArgumentException e) {
            return "redirect:/shop/product/" + productId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }

        return "redirect:/shop/product/" + productId;
    }

    @PostMapping("/shop/product/{productId}/inquiry")
    public String addInquiry(@PathVariable String productId,
                             @RequestParam String content,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        String userId = shopService.getLoginUserId(request);
        if (userId == null || userId.isBlank()) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/dishcovery_login";
        }

        try {
            shopService.addInquiry(userId, productId, content);
        } catch (IllegalArgumentException e) {
            return "redirect:/shop/product/" + productId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }

        return "redirect:/shop/product/" + productId;
    }

    @PostMapping("/shop/purchase")
    public String purchase(@RequestParam String productId,
                           @RequestParam(defaultValue = "1") int qty,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {

        String userId = shopService.getLoginUserId(request);
        if (userId == null || userId.isBlank()) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/dishcovery_login";
        }

        try {
            shopService.purchase(userId, productId, qty);
            return "redirect:/shop/product/" + productId + "?purchased=1";
        } catch (IllegalArgumentException e) {
            return "redirect:/shop/product/" + productId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("구매 처리 실패: userId={}, productId={}, qty={}", userId, productId, qty, e);
            return "redirect:/shop/product/" + productId + "?error=" + URLEncoder.encode("구매 처리 중 오류가 발생했습니다", StandardCharsets.UTF_8);
        }
    }
}
