package com.pet.service.impl;

import com.pet.entity.Carousel;
import com.pet.mapper.CarouselMapper;
import com.pet.service.CarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarouselServiceImpl implements CarouselService {

    @Autowired
    private CarouselMapper carouselMapper;

    @Override
    public List<Carousel> getHomeCarousels() {
        return carouselMapper.getEnabledCarousels();
    }

    @Override
    public Map<String, Object> getCarouselList(Integer page, Integer pageSize, String keyword, Integer status) {
        int offset = (page - 1) * pageSize;
        List<Carousel> list = carouselMapper.getCarouselList(offset, pageSize, keyword, status);
        int total = carouselMapper.countCarousel(keyword, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Carousel getById(Integer id) {
        return carouselMapper.getById(id);
    }

    @Override
    public boolean addCarousel(Carousel carousel) {
        return carouselMapper.insert(carousel) > 0;
    }

    @Override
    public boolean updateCarousel(Carousel carousel) {
        return carouselMapper.update(carousel) > 0;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        return carouselMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean batchUpdateStatus(List<Integer> ids, Integer status) {
        return carouselMapper.batchUpdateStatus(ids, status) > 0;
    }

    @Override
    public boolean deleteCarousel(Integer id) {
        return carouselMapper.deleteById(id) > 0;
    }

    @Override
    public boolean batchDelete(List<Integer> ids) {
        return carouselMapper.batchDelete(ids) > 0;
    }
}