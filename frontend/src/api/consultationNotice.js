// src/api/consultationNotice.js
import request from '@/utils/request';

/**
 * 获取首页公告列表
 */
export function getHomeNotices() {
    return request({
        url: '/consultation-notice/home',
        method: 'get'
    });
}

/**
 * 获取所有公告列表
 */
export function getNoticeList() {
    return request({
        url: '/consultation-notice/list',
        method: 'get'
    });
}

/**
 * 获取置顶公告
 */
export function getTopNotices() {
    return request({
        url: '/consultation-notice/top',
        method: 'get'
    });
}

/**
 * 获取公告详情
 * @param {Number} id 公告ID
 */
export function getNoticeDetail(id) {
    return request({
        url: `/consultation-notice/${id}`,
        method: 'get'
    });
}

// ========== 管理员接口 ==========

/**
 * 分页查询公告（管理员）
 */
export function getAdminNoticePage(params) {
    return request({
        url: '/admin/consultation-notice/page',
        method: 'get',
        params
    });
}

/**
 * 获取所有公告列表（管理员）
 */
export function getAdminNoticeList() {
    return request({
        url: '/admin/consultation-notice/list',
        method: 'get'
    });
}

/**
 * 添加公告（管理员）
 */
export function addAdminNotice(data) {
    return request({
        url: '/admin/consultation-notice/add',
        method: 'post',
        data
    });
}

/**
 * 更新公告（管理员）
 */
export function updateAdminNotice(data) {
    return request({
        url: '/admin/consultation-notice/update',
        method: 'put',
        data
    });
}

/**
 * 删除公告（管理员）
 */
export function deleteAdminNotice(id) {
    return request({
        url: `/admin/consultation-notice/delete/${id}`,
        method: 'delete'
    });
}

/**
 * 批量删除公告（管理员）
 */
export function batchDeleteAdminNotice(ids) {
    return request({
        url: '/admin/consultation-notice/batchDelete',
        method: 'delete',
        data: ids
    });
}

/**
 * 切换置顶状态（管理员）
 */
export function toggleAdminNoticeTop(id, isTop) {
    return request({
        url: `/admin/consultation-notice/toggleTop/${id}`,
        method: 'put',
        params: { isTop }
    });
}

/**
 * 切换状态（管理员）
 */
export function toggleAdminNoticeStatus(id, status) {
    return request({
        url: `/admin/consultation-notice/toggleStatus/${id}`,
        method: 'put',
        params: { status }
    });
}

/**
 * 更新排序（管理员）
 */
export function updateAdminNoticeSort(id, sortOrder) {
    return request({
        url: `/admin/consultation-notice/updateSort/${id}`,
        method: 'put',
        params: { sortOrder }
    });
}

/**
 * 获取公告详情（管理员）
 */
export function getAdminNoticeDetail(id) {
    return request({
        url: `/admin/consultation-notice/detail/${id}`,
        method: 'get'
    });
}