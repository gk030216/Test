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

// 获取服务统计数据（新增）
export function getServiceStatistics(params) {
    return request({
        url: '/admin/service/item/statistics',
        method: 'get',
        params
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

// 确认预约（支持管理员分配员工）
export function confirmAppointment(id, staffId) {
    // 如果传入了 staffId，则放在请求体中
    if (staffId) {
        return request({
            url: `/admin/service/appointment/confirm/${id}`,
            method: 'put',
            data: { staffId: staffId }  // ✅ 确保是 data，不是 params
        });
    } else {
        // 员工自己确认，不传 staffId
        return request({
            url: `/admin/service/appointment/confirm/${id}`,
            method: 'put'
        });
    }
}

/**
 * 检查用户时间是否有冲突
 */
export function checkTimeConflict(data) {
    return request({
        url: '/service/check-time-conflict',
        method: 'post',
        data
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
        method: 'put',
        headers: {
            'Content-Type': 'application/json'
        }
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

// ========== 服务评价接口 ==========

// 添加服务评价
export function addServiceComment(data) {
    return request({ url: '/service/comment/add', method: 'post', data });
}

// 根据预约ID获取评价
export function getServiceCommentByAppointment(appointmentId) {
    return request({ url: `/service/comment/appointment/${appointmentId}`, method: 'get' });
}

// 获取服务的评价列表
export function getServiceComments(serviceId, page = 1, pageSize = 10) {
    return request({
        url: `/service/comment/service/${serviceId}`,
        method: 'get',
        params: { page, pageSize }
    });
}

// 获取服务评分统计
export function getServiceRatingStats(serviceId) {
    return request({ url: `/service/comment/service/${serviceId}/stats`, method: 'get' });
}

// ========== 后台服务评价管理 ==========

// 获取服务评价列表（后台）
export function getServiceCommentList(params) {
    return request({
        url: '/service/comment/admin/list',
        method: 'get',
        params
    });
}

// 获取服务评价统计
export function getServiceCommentStatistics() {
    return request({
        url: '/admin/service/statistics',
        method: 'get'
    });
}

// 更新评价状态
export function updateServiceCommentStatus(id, status) {
    return request({
        url: '/service/comment/admin/status',
        method: 'put',
        params: { id, status }
    });
}

// 回复评价
export function replyServiceComment(id, reply) {
    return request({
        url: '/service/comment/admin/reply',
        method: 'put',
        data: { id, reply }
    });
}

// 删除评价
export function deleteServiceComment(id) {
    return request({
        url: `/service/comment/admin/delete/${id}`,
        method: 'delete'
    });
}

// 批量删除评价
export function batchDeleteServiceComments(ids) {
    return request({
        url: '/service/comment/admin/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// 根据预约编号获取预约详情
export function getAppointmentDetailByNo(appointmentNo) {
    return request({
        url: `/service/appointment/no/${appointmentNo}`,
        method: 'get'
    });
}

// ========== 服务收藏 ==========

// 收藏服务
export function addServiceFavorite(serviceId) {
    return request({
        url: `/service/favorite/${serviceId}`,
        method: 'post'
    });
}

// 取消收藏服务
export function removeServiceFavorite(serviceId) {
    return request({
        url: `/service/favorite/${serviceId}`,
        method: 'delete'
    });
}

// 检查服务收藏状态
export function checkServiceFavorite(serviceId) {
    return request({
        url: `/service/favorite/${serviceId}/check`,
        method: 'get'
    });
}

// 获取服务收藏列表
export function getServiceFavorites(params) {
    return request({
        url: '/service/favorite/list',
        method: 'get',
        params
    });
}

// 批量更新服务分类排序
export function batchUpdateServiceCategorySort(sortList) {
    return request({
        url: '/admin/service/category/batch-sort',
        method: 'put',
        data: sortList
    });
}
// 批量更新服务分类状态
export function batchUpdateServiceCategoryStatus(ids, status) {
    return request({
        url: '/admin/service/category/batch-status',
        method: 'put',
        params: { ids, status }
    });
}

// 导出服务列表
export function exportServiceList(params) {
    return request({
        url: '/admin/service/item/export',
        method: 'get',
        params: params,
        responseType: 'blob',  // 必须设置
        timeout: 30000  // 增加超时时间
    });
}

// 导出预约列表
export function exportAppointmentList(params) {
    return request({
        url: '/admin/service/appointment/export',
        method: 'get',
        params: params,
        responseType: 'blob',
        timeout: 30000
    });
}

// 获取带匹配度的员工列表
export function getStaffWithMatchScore(params) {
    return request({
        url: '/admin/service/staff/match',
        method: 'get',
        params: params
    });
}

// ========== 服务评价分析接口 ==========

// 获取服务评价统计（用于服务评价分析页面）
export function getServiceCommentStatisticsById(serviceId) {
    return request({
        url: `/admin/service/comment/service/${serviceId}/statistics`,
        method: 'get'
    });
}

// 获取服务评价列表（用于服务评价分析页面）
export function getServiceCommentsById(serviceId, params) {
    return request({
        url: `/admin/service/comment/service/${serviceId}/list`,
        method: 'get',
        params
    });
}
// 员工获取服务评价统计
export function getStaffServiceCommentStatistics() {
    return request({
        url: '/admin/service/statistics',  // 改为这个路径
        method: 'get'
    });
}

// 新增接口 - 获取完整的热门服务信息
export function getHotServiceList() {
    return request({
        url: '/service/hot/list',
        method: 'get'
    });
}

// 获取用户的所有服务评价
export function getUserServiceComments(params) {
    return request({
        url: '/service/comment/user/comments',
        method: 'get',
        params
    });
}

// 真实删除预约（物理删除）
export function realDeleteAppointment(id) {
    return request({
        url: `/admin/service/real-delete/${id}`,  // ✅ 加上 /service
        method: 'delete'
    });
}

// 批量真实删除预约
export function batchRealDeleteAppointments(ids) {
    return request({
        url: '/admin/service/batch-real-delete',  // ✅ 加上 /service
        method: 'delete',
        params: { ids }
    });
}