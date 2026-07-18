package com.spring.dishcovery.shop.presentation;

import com.spring.dishcovery.global.config.JwtUtil;
import com.spring.dishcovery.shop.application.ShopService;
import com.spring.dishcovery.shop.domain.entity.ShopInquiry;
import com.spring.dishcovery.shop.domain.entity.ShopProduct;
import com.spring.dishcovery.shop.domain.entity.ShopReview;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShopAppController {

    private final ShopService shopService;
    private final JwtUtil jwtUtil;

    private String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return jwtUtil.getUserIdFromToken(authHeader.substring(7));
    }

    @GetMapping("/products")
    public List<ShopProduct> listProducts(@RequestParam(required = false) String searchName) {
        if (searchName != null && !searchName.isBlank()) {
            return shopService.searchProducts(searchName);
        }
        return shopService.getProductsOrderByPointDesc();
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ShopProduct> getProduct(@PathVariable String productId) {
        ShopProduct product = shopService.getProduct(productId);
        if (product == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(product);
    }

    @GetMapping("/products/{productId}/recommended")
    public List<ShopProduct> recommended(@PathVariable String productId) {
        return shopService.listRecommended(productId);
    }

    @GetMapping("/point")
    public ResponseEntity<Map<String, Integer>> myPoint(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = extractUserId(authHeader);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("point", shopService.getUserPoint(userId)));
    }

    @PostMapping("/purchase")
    public ResponseEntity<Map<String, String>> purchase(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        String userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }
        try {
            int qty = 1;
            if (body.get("qty") != null && !body.get("qty").isBlank()) {
                qty = Integer.parseInt(body.get("qty"));
            }
            shopService.purchase(userId, body.get("productId"), qty);
            return ResponseEntity.ok(Map.of("message", "구매가 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/products/{productId}/reviews")
    public List<ShopReview> listReviews(@PathVariable String productId) {
        return shopService.listReviews(productId);
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<Map<String, String>> addReview(
            @PathVariable String productId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        String userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }
        try {
            int rating = body.get("rating") != null ? Integer.parseInt(body.get("rating")) : 0;
            shopService.addReview(userId, productId, rating, body.get("content"));
            return ResponseEntity.ok(Map.of("message", "리뷰가 등록되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/products/{productId}/inquiries")
    public List<ShopInquiry> listInquiries(@PathVariable String productId) {
        return shopService.listInquiries(productId);
    }

    @PostMapping("/products/{productId}/inquiries")
    public ResponseEntity<Map<String, String>> addInquiry(
            @PathVariable String productId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        String userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }
        try {
            shopService.addInquiry(userId, productId, body.get("content"));
            return ResponseEntity.ok(Map.of("message", "문의가 등록되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
