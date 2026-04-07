import request from '@/utils/request';

// ========== 前台接口 ==========

// 获取服务分类
export function getServiceCategories() {
    return request({
        url: '/service/category/list',
        method: 'get'
    });
}

// 获取服务列表
export function getServiceList(params) {
    return request({
        url: '/service/list',
        method: 'get',
        params
    });
}

// 获取服务详情
export function getServiceDetail(id) {
    return request({
        url: `/service/${id}`,
        method: 'get'
    });
}

// 获取热门服务
export function getHotServices() {
    return request({
        url: '/service/hot',
        method: 'get'
    });
}

// 创建预约
export function createAppointment(data) {
    return request({
        url: '/service/appointment',
        method: 'post',
        data
    });
}

// 获取用户预约列表
export function getUserAppointments(params) {
    return request({
        url: '/service/appointment/list',
        method: 'get',
        params
    });
}

// 获取预约详情
export function getAppointmentDetail(id) {
    return request({
        url: `/service/appointment/${id}`,
        method: 'get'
    });
}

// 取消预约
export function cancelAppointment(id, reason) {
    return request({
        url: `/service/appointment/cancel/${id}`,
        method: 'put',
        data: { reason }
    });
}

// ========== 后台接口 ==========

// ===== 服务分类管理 =====

// 获取分类列表（分页）
export function getServiceCategoryList(params) {
    return request({
        url: '/admin/service/category/list',
        method: 'get',
        params
    });
}

// 获取所有分类（用于父分类下拉）
export function getAllServiceCategories() {
    return request({
        url: '/admin/service/category/all',
        method: 'get'
    });
}

// 添加分类
export function addServiceCategory(data) {
    return request({
        url: '/admin/service/category/add',
        method: 'post',
        data
    });
}

// 更新分类
export function updateServiceCategory(data) {
    return request({
        url: '/admin/service/category/update',
        method: 'put',
        data
    });
}

// 更新分类状态
export function updateServiceCategoryStatus(id, status) {
    return request({
        url: '/admin/service/category/status',
        method: 'put',
        params: { id, status }
    });
}

// 删除分类
export function deleteServiceCategory(id) {
    return request({
        url: `/admin/service/category/delete/${id}`,
        method: 'delete'
    });
}

// 批量删除分类
export function batchDeleteServiceCategories(ids) {
    return request({
        url: '/admin/service/category/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// ===== 服务项目管理 =====

// 获取服务列表（后台）
export function getAdminItemList(params) {
    return request({
        url: '/admin/service/item/list',
        method: 'get',
        params
    });
}

// 添加服务
export function addItem(data) {
    return request({
        url: '/admin/service/item/add',
        method: 'post',
        data
    });
}

// 更新服务
export function updateItem(data) {
    return request({
        url: '/admin/service/item/update',
        method: 'put',
        data
    });
}

// 更新服务状态
export function updateItemStatus(id, status) {
    return request({
        url: '/admin/service/item/status',
        method: 'put',
        params: { id, status }
    });
}

// 删除服务
export function deleteItem(id) {
    return request({
        url: `/admin/service/item/delete/${id}`,
        method: 'delete'
    });
}

// 批量删除服务
export function batchDeleteItems(ids) {
    return request({
        url: '/admin/service/item/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// ===== 预约管理 =====

// 获取预约列表（后台）
export function getAdminAppointmentList(params) {
    return request({
        url: '/admin/service/appointment/list',
        method: 'get',
        params
    });
}

// 获取预约详情（后台）
export function getAppointmentDetailForAdmin(id) {
    return request({
        url: `/admin/service/appointment/${id}`,
        method: 'get'
    });
}

// 确认预约
export function confirmAppointment(id) {
    return request({
        url: `/admin/service/appointment/confirm/${id}`,
        method: 'put'
    });
}

// 开始服务
export function startAppointment(id) {
    return request({
        url: `/admin/service/appointment/start/${id}`,
        method: 'put'
    });
}

// 完成预约
export function completeAppointment(id) {
    return request({
        url: `/admin/service/appointment/complete/${id}`,
        method: 'put'
    });
}

// 拒绝预约
export function rejectAppointment(id, reason) {
    return request({
        url: `/admin/service/appointment/reject/${id}`,
        method: 'put',
        data: { reason }
    });
}

// 管理员取消预约
export function cancelAppointmentByAdmin(id, reason) {
    return request({
        url: `/admin/service/appointment/cancel/${id}`,
        method: 'put',
        data: { reason }
    });
}

// 获取预约统计
export function getAppointmentStatistics() {
    return request({
        url: '/admin/service/appointment/statistics',
        method: 'get'
    });
}