import request from '@/utils/request';

// ============= 前台接口 =============

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

// 获取订单列表（用户）
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

// ============= 管理员接口 =============

// 获取订单列表（后台）
export function getAdminOrderList(params) {
    return request({
        url: '/admin/order/list',
        method: 'get',
        params
    });
}

// 更新订单状态
export function updateOrderStatus(orderNo, orderStatus) {
    return request({
        url: '/admin/order/status',
        method: 'put',
        params: { orderNo, orderStatus }
    });
}

// 删除订单
export function deleteOrder(id) {
    return request({
        url: `/admin/order/delete/${id}`,
        method: 'delete'
    });
}

// 批量删除订单
export function batchDeleteOrders(ids) {
    return request({
        url: '/admin/order/batch-delete',
        method: 'delete',
        params: { ids }
    });
}