import request from '@/utils/request';

/**
 * 获取未读消息数量
 */
export function getUnreadCount() {
    return request({
        url: '/notification/unread/count',
        method: 'get'
    });
}

/**
 * 获取消息通知列表
 * @param {Object} params - { page, pageSize }
 */
export function getNotificationList(params) {
    return request({
        url: '/notification/list',
        method: 'get',
        params
    });
}

/**
 * 标记单条消息为已读
 * @param {Number} id - 消息ID
 */
export function markAsRead(id) {
    return request({
        url: `/notification/read/${id}`,
        method: 'put'
    });
}

/**
 * 标记所有消息为已读
 */
export function markAllAsRead() {
    return request({
        url: '/notification/read/all',
        method: 'put'
    });
}

/**
 * 删除消息通知
 * @param {Number} id - 消息ID
 */
export function deleteNotification(id) {
    return request({
        url: `/notification/delete/${id}`,
        method: 'delete'
    });
}