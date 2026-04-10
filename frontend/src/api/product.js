import request from '@/utils/request';

// ============= 前台接口 =============

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

// ============= 管理员接口 =============

// 获取商品列表（后台）
export function getAdminProductList(params) {
    return request({
        url: '/admin/product/list',
        method: 'get',
        params
    });
}

// 添加商品
export function addProduct(data) {
    return request({
        url: '/admin/product/add',
        method: 'post',
        data
    });
}

// 更新商品
export function updateProduct(data) {
    return request({
        url: '/admin/product/update',
        method: 'put',
        data
    });
}

// 更新商品状态
export function updateProductStatus(id, status) {
    return request({
        url: '/admin/product/status',
        method: 'put',
        params: { id, status }
    });
}

// 批量更新商品状态
export function batchUpdateProductStatus(ids, status) {
    return request({
        url: '/admin/product/batch-status',
        method: 'put',
        params: { ids, status }
    });
}

// 删除商品
export function deleteProduct(id) {
    return request({
        url: `/admin/product/delete/${id}`,
        method: 'delete'
    });
}

// 批量删除商品
export function batchDeleteProducts(ids) {
    return request({
        url: '/admin/product/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// 添加收藏
export function addFavorite(productId) {
    return request({
        url: `/product/favorite/${productId}`,
        method: 'post'
    });
}

// 取消收藏
export function removeFavorite(productId) {
    return request({
        url: `/product/favorite/${productId}`,
        method: 'delete'
    });
}

// 检查收藏状态
export function checkFavorite(productId) {
    return request({
        url: `/product/favorite/${productId}/check`,
        method: 'get'
    });
}

// 获取协同过滤推荐
export function getRecommendProducts(productId, limit = 8) {
    return request({
        url: '/product/recommend',
        method: 'get',
        params: { productId, limit }
    });
}

// @/api/product.js

// 获取商品收藏列表
export function getProductFavorites(params) {
    return request({
        url: '/product/favorites',
        method: 'get',
        params
    });
}

// 取消商品收藏
export function removeProductFavorite(productId) {
    return request({
        url: `/product/favorite/${productId}`,
        method: 'delete'
    });
}