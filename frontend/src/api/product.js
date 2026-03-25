import request from '@/utils/request';

// 获取商品列表
export function getProductList(params) {
    return request({
        url: '/product/list',
        method: 'get',
        params
    });
}

// 获取商品详情
export function getProductById(id) {
    return request({
        url: `/product/${id}`,
        method: 'get'
    });
}

// 获取热门商品
export function getHotProducts() {
    return request({
        url: '/product/hot',
        method: 'get'
    });
}

// 获取新品推荐
export function getNewProducts() {
    return request({
        url: '/product/new',
        method: 'get'
    });
}