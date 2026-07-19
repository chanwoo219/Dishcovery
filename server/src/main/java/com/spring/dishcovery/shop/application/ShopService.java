package com.spring.dishcovery.shop.application;

import com.spring.dishcovery.global.config.JwtUtil;
import com.spring.dishcovery.shop.domain.entity.PurchaseHistoryVo;
import com.spring.dishcovery.shop.domain.entity.ShopInquiry;
import com.spring.dishcovery.shop.domain.entity.ShopProduct;
import com.spring.dishcovery.shop.domain.entity.ShopReview;
import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.shop.domain.mapper.ShopMapper;
import com.spring.dishcovery.user.domain.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ShopService {

    public static final int PAGE_SIZE = 12;

    private final ShopMapper shopMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public ShopService(ShopMapper shopMapper, UserMapper userMapper, JwtUtil jwtUtil) {
        this.shopMapper = shopMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    public List<ShopProduct> getProductsOrderByPointDesc() {
        try {
            return Optional.ofNullable(shopMapper.listProductsOrderByPointDesc())
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("상품 목록 조회 실패", e);
            return Collections.emptyList();
        }
    }

    public List<ShopProduct> searchProducts(String searchName) {
        try {
            return Optional.ofNullable(shopMapper.searchProductsOrderByPointDesc(searchName))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("상품 검색 실패: searchName={}", searchName, e);
            return Collections.emptyList();
        }
    }

    public List<ShopProduct> getProductsPaged(int page) {
        int offset = Math.max(page - 1, 0) * PAGE_SIZE;
        try {
            return Optional.ofNullable(shopMapper.listProductsPaged(offset, PAGE_SIZE))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("상품 목록 페이징 조회 실패: page={}", page, e);
            return Collections.emptyList();
        }
    }

    public int countProducts() {
        try {
            return shopMapper.countProducts();
        } catch (Exception e) {
            log.error("상품 총개수 조회 실패", e);
            return 0;
        }
    }

    public List<ShopProduct> searchProductsPaged(String searchName, int page) {
        int offset = Math.max(page - 1, 0) * PAGE_SIZE;
        try {
            return Optional.ofNullable(shopMapper.searchProductsPaged(searchName, offset, PAGE_SIZE))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("상품 검색 페이징 조회 실패: searchName={}, page={}", searchName, page, e);
            return Collections.emptyList();
        }
    }

    public int countSearchProducts(String searchName) {
        try {
            return shopMapper.countSearchProducts(searchName);
        } catch (Exception e) {
            log.error("상품 검색 결과 개수 조회 실패: searchName={}", searchName, e);
            return 0;
        }
    }

    public ShopProduct getProduct(String productId) {
        if (productId == null || productId.isBlank()) return null;
        try {
            return shopMapper.getProduct(productId);
        } catch (Exception e) {
            log.error("상품 조회 실패: productId={}", productId, e);
            return null;
        }
    }

    public List<ShopProduct> listRecommended(String excludeId) {
        try {
            return Optional.ofNullable(shopMapper.listRecommended(excludeId))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("추천 상품 조회 실패: excludeId={}", excludeId, e);
            return Collections.emptyList();
        }
    }

    public String getLoginUserId(HttpServletRequest request) {
        return request != null ? jwtUtil.getUserIdFromRequest(request) : null;
    }

    public int getUserPoint(String userId) {
        if (userId == null || userId.isBlank()) return 0;
        try {
            UserEntity user = userMapper.findByUserId(userId);
            return user != null ? user.getPointBalance() : 0;
        } catch (Exception e) {
            log.error("사용자 포인트 조회 실패: userId={}", userId, e);
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
        shopMapper.insertPurchaseHistory(userId, productId, qty);

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

    public boolean hasPurchased(String userId, String productId) {
        if (userId == null || userId.isBlank() || productId == null || productId.isBlank()) return false;
        try {
            return shopMapper.countPurchases(userId, productId) > 0;
        } catch (Exception e) {
            log.error("구매 여부 조회 실패: userId={}, productId={}", userId, productId, e);
            return false;
        }
    }

    public List<PurchaseHistoryVo> listPurchaseHistory(String userId) {
        try {
            return Optional.ofNullable(shopMapper.listPurchaseHistory(userId))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("구매 내역 조회 실패: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    public List<ShopReview> listReviews(String productId) {
        try {
            return Optional.ofNullable(shopMapper.listReviews(productId))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("리뷰 목록 조회 실패: productId={}", productId, e);
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
        if (shopMapper.countPurchases(userId, productId) <= 0) throw new IllegalArgumentException("구매한 상품만 리뷰를 작성할 수 있습니다");

        shopMapper.insertReview(productId, userId, rating, content);
    }

    public List<ShopInquiry> listInquiries(String productId) {
        try {
            return Optional.ofNullable(shopMapper.listInquiries(productId))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("문의 목록 조회 실패: productId={}", productId, e);
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
