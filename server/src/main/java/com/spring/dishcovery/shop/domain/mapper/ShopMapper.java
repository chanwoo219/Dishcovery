package com.spring.dishcovery.shop.domain.mapper;

import com.spring.dishcovery.shop.domain.entity.PurchaseHistoryVo;
import com.spring.dishcovery.shop.domain.entity.ShopInquiry;
import com.spring.dishcovery.shop.domain.entity.ShopProduct;
import com.spring.dishcovery.shop.domain.entity.ShopReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShopMapper {

    List<ShopProduct> listProductsOrderByPointDesc();

    List<ShopProduct> searchProductsOrderByPointDesc(@Param("searchName") String searchName);

    ShopProduct getProduct(@Param("productId") String productId);

    void updateUserPoint(@Param("userId") String userId, @Param("point") int point);

    void insertPurchaseHistory(@Param("userId") String userId, @Param("productId") String productId);

    int countPurchases(@Param("userId") String userId, @Param("productId") String productId);

    List<PurchaseHistoryVo> listPurchaseHistory(@Param("userId") String userId);

    List<ShopProduct> listRecommended(@Param("excludeId") String excludeId);

    void insertLedger(@Param("userId") String userId,
                      @Param("point") int point,
                      @Param("transactionType") String transactionType,
                      @Param("description") String description);

    List<ShopReview> listReviews(@Param("productId") String productId);

    void insertReview(@Param("productId") String productId,
                      @Param("userId") String userId,
                      @Param("rating") int rating,
                      @Param("content") String content);

    List<ShopInquiry> listInquiries(@Param("productId") String productId);

    void insertInquiry(@Param("productId") String productId,
                       @Param("userId") String userId,
                       @Param("content") String content);
}
