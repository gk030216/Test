import request from '@/utils/request';

// 获取购物车列表
export function getCartList() {
    return request({
        url: '/cart/list',
        method: 'get'
    });
}

// 获取购物车统计
export function getCartSummary() {
    return request({
        url: '/cart/summary',
        method: 'get'
    });
}

// 添加商品到购物车
export function addToCart(productId, quantity = 1) {
    return request({
        url: '/cart/add',
        method: 'post',
        params: { productId, quantity }
    });
}

// 更新购物车数量
export function updateCartQuantity(cartId, quantity) {
    return request({
        url: '/cart/quantity',
        method: 'put',
        params: { cartId, quantity }
    });
}

// 更新选中状态
export function updateCartSelected(cartId, selected) {
    return request({
        url: '/cart/selected',
        method: 'put',
        params: { cartId, selected }
    });
}

// 删除购物车商品
export function deleteCartItem(cartId) {
    return request({
        url: `/cart/delete/${cartId}`,
        method: 'delete'
    });
}

// 删除已选中的商品
export function deleteSelected() {
    return request({
        url: '/cart/delete-selected',
        method: 'delete'
    });
}