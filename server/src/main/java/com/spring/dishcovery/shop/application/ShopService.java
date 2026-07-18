package com.spring.dishcovery.shop.application;

import com.spring.dishcovery.global.config.CookieUtil;
import com.spring.dishcovery.global.config.JwtUtil;
import com.spring.dishcovery.shop.domain.entity.ShopInquiry;
import com.spring.dishcovery.shop.domain.entity.ShopProduct;
import com.spring.dishcovery.shop.domain.entity.ShopReview;
import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.shop.domain.mapper.ShopMapper;
import com.spring.dishcovery.user.domain.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ShopService {

    private final ShopMapper shopMapper;
    private final UserMapper userMapper;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    public ShopService(ShopMapper shopMapper, UserMapper userMapper, CookieUtil cookieUtil, JwtUtil jwtUtil) {
        this.shopMapper = shopMapper;
        this.userMapper = userMapper;
        this.cookieUtil = cookieUtil;
        this.jwtUtil = jwtUtil;
    }

    public List<ShopProduct> getProductsOrderByPointDesc() {
        try {
            return Optional.ofNullable(shopMapper.listProductsOrderByPointDesc())
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<ShopProduct> searchProducts(String searchName) {
        try {
            return Optional.ofNullable(shopMapper.searchProductsOrderByPointDesc(searchName))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public ShopProduct getProduct(String productId) {
        if (productId == null || productId.isBlank()) return null;
        try {
            return shopMapper.getProduct(productId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ShopProduct> listRecommended(String excludeId) {
        try {
            return Optional.ofNullable(shopMapper.listRecommended(excludeId))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public String getLoginUserId(HttpServletRequest request) {
        if (request == null) return null;
        String token = cookieUtil.getTokenFromCookies(request, "JWT_TOKEN");
        return token != null ? jwtUtil.getUserIdFromToken(token) : null;
    }

    public int getUserPoint(String userId) {
        if (userId == null || userId.isBlank()) return 0;
        try {
            UserEntity user = userMapper.findByUserId(userId);
            return user != null ? user.getPointBalance() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Transactional
    public void purchase(String userId, String productId, int qty) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("로그인이 필요합니다");
        if (productId == null || productId.isBlank()) throw new IllegalArgumentException("상품 ID가 올바르지 않습니다");
        if (qty <= 0) qty = 1;

        ShopProduct product = getProduct(productId);
        if (product == null) throw new IllegalArgumentException("상품이 존재하지 않습니다");

        int unitPoint = product.getProductPoint(); // int라 가정
        int totalPoint = Math.multiplyExact(unitPoint, qty);

        int myPoint = getUserPoint(userId);
        if (myPoint < totalPoint) throw new IllegalArgumentException("포인트가 부족합니다");

        shopMapper.updateUserPoint(userId, -totalPoint);
        shopMapper.insertPurchaseHistory(userId, productId);

        try {
            shopMapper.insertLedger(
                    userId,
                    -totalPoint,
                    "PURCHASE",
                    "상품 구매: " + Objects.toString(product.getProductName(), "상품")
            );
        } catch (Exception ignored) {
            // ledger 없어도 구매는 진행
        }
    }

    public List<ShopReview> listReviews(String productId) {
        try {
            return Optional.ofNullable(shopMapper.listReviews(productId))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public double getAverageRating(List<ShopReview> reviews) {
        if (reviews == null || reviews.isEmpty()) return 0;
        return reviews.stream().mapToInt(ShopReview::getRating).average().orElse(0);
    }

    public void addReview(String userId, String productId, int rating, String content) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("로그인이 필요합니다");
        if (productId == null || productId.isBlank()) throw new IllegalArgumentException("상품 ID가 올바르지 않습니다");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("리뷰 내용을 입력해주세요");
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("별점은 1~5 사이여야 합니다");

        shopMapper.insertReview(productId, userId, rating, content);
    }

    public List<ShopInquiry> listInquiries(String productId) {
        try {
            return Optional.ofNullable(shopMapper.listInquiries(productId))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void addInquiry(String userId, String productId, String content) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("로그인이 필요합니다");
        if (productId == null || productId.isBlank()) throw new IllegalArgumentException("상품 ID가 올바르지 않습니다");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("문의 내용을 입력해주세요");

        shopMapper.insertInquiry(productId, userId, content);
    }
}
