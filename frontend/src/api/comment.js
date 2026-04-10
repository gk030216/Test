import request from '@/utils/request';

// 获取评价列表（后台）
export function getCommentList(params) {
    return request({
        url: '/admin/comment/list',
        method: 'get',
        params
    });
}

// 获取评价统计（数据分析）
export function getCommentStatistics() {
    return request({
        url: '/admin/comment/statistics',
        method: 'get'
    });
}

// 获取最新评价
export function getRecentComments(limit = 5) {
    return request({
        url: '/admin/comment/recent',
        method: 'get',
        params: { limit }
    });
}

// 更新评价状态
export function updateCommentStatus(id, status) {
    return request({
        url: '/admin/comment/status',
        method: 'put',
        params: { id, status }
    });
}

// 删除评价
export function deleteComment(id) {
    return request({
        url: `/admin/comment/delete/${id}`,
        method: 'delete'
    });
}

// 批量删除评价
export function batchDeleteComments(ids) {
    return request({
        url: '/admin/comment/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// 回复评价
export function replyComment(id, reply) {
    return request({
        url: '/admin/comment/reply',
        method: 'post',
        data: { id, reply }
    });
}

// 添加评价（用户端）
export function addComment(data) {
    return request({
        url: '/comment/add',
        method: 'post',
        data
    });
}

// 获取商品评价列表（用户端）
export function getProductComments(productId, page = 1, pageSize = 10) {
    return request({
        url: `/comment/product/${productId}`,
        method: 'get',
        params: { page, pageSize }
    });
}

// 获取商品评分统计
export function getProductRatingStats(productId) {
    return request({
        url: `/comment/product/${productId}/stats`,
        method: 'get'
    });
}

