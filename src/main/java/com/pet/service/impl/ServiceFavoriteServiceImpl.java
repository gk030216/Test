package com.pet.service.impl;

import com.pet.entity.ServiceFavorite;
import com.pet.mapper.ServiceFavoriteMapper;
import com.pet.service.ServiceFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceFavoriteServiceImpl implements ServiceFavoriteService {

    @Autowired
    private ServiceFavoriteMapper favoriteMapper;

    @Override
    @Transactional
    public boolean addFavorite(Integer userId, Integer serviceId) {
        // 检查是否已收藏
        if (isFavorited(userId, serviceId)) {
            return true;
        }
        return favoriteMapper.insert(userId, serviceId) > 0;
    }

    @Override
    @Transactional
    public boolean removeFavorite(Integer userId, Integer serviceId) {
        return favoriteMapper.delete(userId, serviceId) > 0;
    }

    @Override
    public boolean isFavorited(Integer userId, Integer serviceId) {
        return favoriteMapper.checkFavorite(userId, serviceId) > 0;
    }

    @Override
    public Map<String, Object> getUserFavorites(Integer userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ServiceFavorite> list = favoriteMapper.getUserFavorites(userId, offset, pageSize);
        int total = favoriteMapper.countUserFavorites(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }
}