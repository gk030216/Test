import request from '@/utils/request';

// 获取分类列表（分页，后台用）
export function getCategoryList(params) {
    return request({
        url: '/admin/category/list',
        method: 'get',
        params
    });
}

// 获取所有启用的分类（前台用）
export function getAllCategories() {
    return request({
        url: '/admin/category/all',
        method: 'get'
    });
}

// 添加分类
export function addCategory(data) {
    return request({
        url: '/admin/category/add',
        method: 'post',
        data
    });
}

// 更新分类
export function updateCategory(data) {
    return request({
        url: '/admin/category/update',
        method: 'put',
        data
    });
}

// 更新分类状态
export function updateCategoryStatus(id, status) {
    return request({
        url: '/admin/category/status',
        method: 'put',
        params: { id, status }
    });
}

// 删除分类
export function deleteCategory(id) {
    return request({
        url: `/admin/category/delete/${id}`,
        method: 'delete'
    });
}

// 批量删除分类
export function batchDeleteCategories(ids) {
    return request({
        url: '/admin/category/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// 批量更新分类排序
export function batchUpdateCategorySort(sortList) {
    return request({
        url: '/admin/category/batch-sort',
        method: 'put',
        data: sortList
    });
}

// 导出分类列表
export function exportCategoryList(params) {
    return request({
        url: '/admin/category/export',
        method: 'get',
        params: params,
        responseType: 'blob'
    });
}

