package com.spring.dishcovery.controller;

import com.spring.dishcovery.config.CookieUtil;
import com.spring.dishcovery.config.JwtUtil;
import com.spring.dishcovery.entity.RecipeVo;
import com.spring.dishcovery.entity.UserEntity;
import com.spring.dishcovery.service.RecipeAppService;
import com.spring.dishcovery.service.UserService;
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
                           @RequestParam(required = false, defaultValue = "latest") String sort) {

        String sortKey = mapSort(sort);
        List<RecipeVo> recipes = service.getAllRecipesSorted(sortKey);

        model.addAttribute("recipes", recipes);
        model.addAttribute("sort", sort);
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
    public String pageGubun(@RequestParam String gubun, Model model, HttpServletRequest request) {

        List<RecipeVo> recipes = new ArrayList<>();

        String rcpClassNm = "";
        String rankClassNm = "";
        String url = "";

        if("recipe".equals(gubun)) {
            rcpClassNm = "seg-btn active";
            rankClassNm = "seg-btn";

            recipes = service.getAllRecipes();
            url = "/mainPage";

        }else{
            rcpClassNm = "seg-btn";
            rankClassNm = "seg-btn active";

            // 상품 조회는 ShopController에서 처리
            url = "redirect:/shop";
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("gubun", gubun);
        model.addAttribute("rcpClassNm", rcpClassNm);
        model.addAttribute("rankClassNm", rankClassNm);

        return url;
    }


}
