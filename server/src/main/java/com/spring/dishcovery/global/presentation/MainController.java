package com.spring.dishcovery.global.presentation;

import com.spring.dishcovery.global.config.CookieUtil;
import com.spring.dishcovery.global.config.JwtUtil;
import com.spring.dishcovery.recipe.domain.entity.RecipeVo;
import com.spring.dishcovery.user.domain.entity.UserEntity;
import com.spring.dishcovery.recipe.application.RecipeAppService;
import com.spring.dishcovery.user.application.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final RecipeAppService service;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @GetMapping("/MainPage")
    public String mainPage(Model model,
                           @RequestParam(required = false, defaultValue = "latest") String sort,
                           @RequestParam(required = false, defaultValue = "1") int page) {

        String sortKey = mapSort(sort);
        List<RecipeVo> recipes = service.getAllRecipesSorted(sortKey, page);
        int totalPages = (int) Math.ceil(service.countAllRecipes() / (double) RecipeAppService.PAGE_SIZE);

        model.addAttribute("recipes", recipes);
        model.addAttribute("sort", sort);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", Math.max(totalPages, 1));
        model.addAttribute("rcpClassNm", "seg-btn active");
        model.addAttribute("rankClassNm", "seg-btn");

        return "mainPage";
    }

    private String mapSort(String sort) {
        if (sort == null) return "latest";
        return switch (sort) {
            case "popular" -> "views";
            case "recommend" -> "random";
            case "latest" -> "latest";
            case "views", "time", "difficulty", "random" -> sort;
            default -> "latest";
        };
    }

    @GetMapping("/myPage")
    public String myPage(Model model, HttpServletRequest request) {

        // JWT 쿠키에서 토큰 가져오기
        String token = cookieUtil.getTokenFromCookies(request, "JWT_TOKEN");
        String userId = token != null ? jwtUtil.getUserIdFromToken(token) : null;
        if (userId == null) {
            return "redirect:/dishcovery_login";
        }

        UserEntity user = userService.findByUserId(userId);

        List<RecipeVo> myRecipes = new ArrayList<>();
        myRecipes = service.getMyRecipes(userId);


        List<UserEntity> userList = new ArrayList<>();
        userList = userService.findRecommUser(userId);

        model.addAttribute("user", user);
        model.addAttribute("myRecipes", myRecipes);
        model.addAttribute("userList", userList);

        return "user/MyPage";
    }

    @GetMapping("/pageGubun")
    public String pageGubun(@RequestParam String gubun,
                            @RequestParam(required = false, defaultValue = "latest") String sort) {

        if ("recipe".equals(gubun)) {
            // 레시피 목록/정렬은 MainPage 쪽 로직이 유일한 기준이 되도록 위임
            return "redirect:/MainPage?sort=" + sort;
        }

        // 상품 조회는 ShopController에서 처리
        return "redirect:/shop";
    }


}
