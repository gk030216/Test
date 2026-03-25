import request from '@/utils/request';

// 创建订单
export function createOrder(data) {
    return request({
        url: '/order/create',
        method: 'post',
        data
    });
}

// 获取订单详情
export function getOrderDetail(orderNo) {
    return request({
        url: `/order/detail/${orderNo}`,
        method: 'get'
    });
}

// 获取订单列表
export function getOrderList(params) {
    return request({
        url: '/order/list',
        method: 'get',
        params
    });
}

// 取消订单
export function cancelOrder(orderNo) {
    return request({
        url: `/order/cancel/${orderNo}`,
        method: 'put'
    });
}

// 发起支付
export function payOrder(data) {
    return request({
        url: '/order/pay',
        method: 'post',
        data
    });
}