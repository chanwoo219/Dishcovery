package com.spring.dishcovery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({
        "com.spring.dishcovery.user.domain.mapper",
        "com.spring.dishcovery.recipe.domain.mapper",
        "com.spring.dishcovery.shop.domain.mapper",
        "com.spring.dishcovery.code.domain.mapper"
})
public class DishcoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DishcoveryApplication.class, args);
    }

}