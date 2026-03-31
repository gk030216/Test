import request from '@/utils/request';

// ============= 用户端接口 =============

// AI智能问答
export function aiChat(data) {
    return request({
        url: '/ai/chat',
        method: 'post',
        data
    });
}

// 获取对话历史（用户端）
export function getChatHistory(page = 1, pageSize = 20) {
    return request({
        url: '/ai/history',
        method: 'get',
        params: { page, pageSize }
    });
}

// 获取常见问题列表
export function getFaqList() {
    return request({
        url: '/ai/faq',
        method: 'get'
    });
}

// 评价答案
export function rateAnswer(data) {
    return request({
        url: '/ai/rate',
        method: 'post',
        data
    });
}

// ============= 后台管理接口 =============

// 知识库管理
export function getKnowledgeList(params) {
    return request({
        url: '/admin/ai/knowledge/list',
        method: 'get',
        params
    });
}

export function addKnowledge(data) {
    return request({
        url: '/admin/ai/knowledge/add',
        method: 'post',
        data
    });
}

export function updateKnowledge(data) {
    return request({
        url: '/admin/ai/knowledge/update',
        method: 'put',
        data
    });
}

export function updateKnowledgeStatus(id, status) {
    return request({
        url: '/admin/ai/knowledge/status',
        method: 'put',
        params: { id, status }
    });
}

export function deleteKnowledge(id) {
    return request({
        url: `/admin/ai/knowledge/delete/${id}`,
        method: 'delete'
    });
}

export function batchDeleteKnowledge(ids) {
    return request({
        url: '/admin/ai/knowledge/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// 常见问题管理
export function getFaqListAdmin(params) {
    return request({
        url: '/admin/ai/faq/list',
        method: 'get',
        params
    });
}

export function addFaq(data) {
    return request({
        url: '/admin/ai/faq/add',
        method: 'post',
        data
    });
}

// 清空对话历史
export function clearChatHistory() {
    return request({
        url: '/ai/history',
        method: 'delete'
    });
}

export function updateFaq(data) {
    return request({
        url: '/admin/ai/faq/update',
        method: 'put',
        data
    });
}

export function updateFaqStatus(id, status) {
    return request({
        url: '/admin/ai/faq/status',
        method: 'put',
        params: { id, status }
    });
}

export function deleteFaq(id) {
    return request({
        url: `/admin/ai/faq/delete/${id}`,
        method: 'delete'
    });
}

export function batchDeleteFaq(ids) {
    return request({
        url: '/admin/ai/faq/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// 对话记录管理
export function getChatHistoryList(params) {
    return request({
        url: '/admin/ai/history/list',
        method: 'get',
        params
    });
}

export function deleteChatHistory(id) {
    return request({
        url: `/admin/ai/history/delete/${id}`,
        method: 'delete'
    });
}

export function batchDeleteChatHistory(ids) {
    return request({
        url: '/admin/ai/history/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

export function exportChatHistory(params) {
    return request({
        url: '/admin/ai/history/export',
        method: 'get',
        params,
        responseType: 'blob'
    });
}

// 单条记录转为知识库
export function convertToKnowledge(data) {
    return request({
        url: '/admin/ai/history/convert',
        method: 'post',
        data
    });
}

// 批量转为知识库
export function batchConvertToKnowledge(data) {
    return request({
        url: '/admin/ai/history/batch-convert',
        method: 'post',
        data
    });
}