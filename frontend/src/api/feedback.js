import request from '@/utils/request';

// 获取反馈列表
export function getFeedbackList(params) {
    return request({
        url: '/staff/feedback/list',
        method: 'get',
        params
    });
}

// 获取反馈统计
export function getFeedbackStatistics() {
    return request({
        url: '/staff/feedback/statistics',
        method: 'get'
    });
}

// 开始处理反馈
export function processFeedback(id) {
    return request({
        url: `/staff/feedback/process/${id}`,
        method: 'put'
    });
}

// 解决反馈
export function resolveFeedback(id, processResult) {
    return request({
        url: `/staff/feedback/resolve/${id}`,
        method: 'put',
        data: { processResult }
    });
}


// 添加处理记录
export function addProcessRecord(data) {
    return request({
        url: '/staff/feedback/record',
        method: 'post',
        data: {
            feedbackId: data.feedbackId,
            content: data.content
        }
    });
}
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

