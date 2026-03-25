package com.pet.controller;

import com.pet.entity.Carousel;
import com.pet.service.CarouselService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/carousel")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    /**
     * 获取首页轮播图
     */
    @GetMapping("/list")
    public Result<List<Carousel>> getCarouselList() {
        try {
            List<Carousel> list = carouselService.getHomeCarousels();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}