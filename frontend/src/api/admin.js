import request from '@/utils/request';

// 获取仪表盘统计数据
export function getDashboardStatistics() {
    return request({
        url: '/admin/dashboard/statistics',
        method: 'get'
    });
}

// 获取用户统计数据
export function getUserStatistics() {
    return request({
        url: '/admin/user/statistics',
        method: 'get'
    });
}

// 获取订单统计数据
export function getOrderStatistics() {
    return request({
        url: '/admin/order/statistics',
        method: 'get'
    });
}

// 获取服务统计数据
export function getServiceStatistics() {
    return request({
        url: '/admin/service/statistics',
        method: 'get'
    });
}

// 获取社区统计数据
export function getCommunityStatistics() {
    return request({
        url: '/admin/community/statistics',
        method: 'get'
    });
}