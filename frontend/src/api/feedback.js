import request from '@/utils/request';

// ============= 用户端接口 =============

// 用户提交反馈
export function submitFeedback(data) {
    return request({
        url: '/feedback/submit',
        method: 'post',
        data
    });
}

// 获取我的反馈
export function getMyFeedbacks(params) {
    return request({
        url: '/feedback/my',
        method: 'get',
        params
    });
}

// ============= 后台管理接口 =============

// 获取反馈列表（后台）
export function getAdminFeedbackList(params) {
    return request({
        url: '/feedback/admin/list',
        method: 'get',
        params
    });
}

// 获取反馈统计（后台）
export function getAdminFeedbackStatistics() {
    return request({
        url: '/feedback/admin/statistics',
        method: 'get'
    });
}

// 开始处理反馈
export function processFeedback(id) {
    return request({
        url: `/feedback/admin/process/${id}`,
        method: 'put'
    });
}

// 解决反馈
export function resolveFeedback(id, processResult) {
    return request({
        url: `/feedback/admin/resolve/${id}`,
        method: 'put',
        data: { processResult }
    });
}

// 添加处理记录
export function addProcessRecord(data) {
    return request({
        url: '/feedback/admin/record',
        method: 'post',
        data
    });
}

// 删除反馈（管理员）
export function deleteFeedback(id) {
    return request({
        url: `/feedback/admin/delete/${id}`,
        method: 'delete'
    });
}

// 批量删除反馈
export function batchDeleteFeedbacks(ids) {
    return request({
        url: '/feedback/admin/batch-delete',
        method: 'delete',
        params: { ids }
    });
}