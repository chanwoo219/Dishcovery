package com.spring.dishcovery.shop.domain.mapper;

import com.spring.dishcovery.shop.domain.entity.ShopProduct;
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

    List<ShopProduct> listRecommended(@Param("excludeId") String excludeId);

    void insertLedger(@Param("userId") String userId,
                      @Param("point") int point,
                      @Param("transactionType") String transactionType,
                      @Param("description") String description);
}
