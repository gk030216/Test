import request from '@/utils/request';

// 获取库存列表（支持按类型筛选）
export function getInventoryList(params) {
    return request({
        url: '/admin/inventory/list',
        method: 'get',
        params
    });
}

// 获取疫苗库存列表（type=vaccine）
export function getVaccineInventoryList(params) {
    return request({
        url: '/admin/inventory/list',
        method: 'get',
        params: { ...params, type: 'vaccine' }
    });
}

// 获取商品库存列表（type=product）
export function getProductInventoryList(params) {
    return request({
        url: '/admin/inventory/list',
        method: 'get',
        params: { ...params, type: 'product' }
    });
}

// 添加库存
export function addInventory(data) {
    return request({
        url: '/admin/inventory/add',
        method: 'post',
        data
    });
}

// 更新库存信息
export function updateInventory(data) {
    return request({
        url: '/admin/inventory/update',
        method: 'put',
        data
    });
}

// 入库（增加库存）
export function addStock(id, quantity) {
    return request({
        url: `/admin/inventory/add-stock/${id}`,
        method: 'put',
        data: { quantity }
    });
}

// 删除库存
export function deleteInventory(id) {
    return request({
        url: `/admin/inventory/delete/${id}`,
        method: 'delete'
    });
}

// 获取库存统计
export function getInventoryStatistics(type) {
    return request({
        url: '/admin/inventory/statistics',
        method: 'get',
        params: { type }
    });
}

// 导出疫苗库存列表
export function exportVaccineList(params) {
    return request({
        url: '/admin/inventory/vaccine/export',
        method: 'get',
        params,
        responseType: 'blob'
    });
}

// 导出商品库存列表
export function exportProductInventoryList(params) {
    return request({
        url: '/admin/inventory/product/export',
        method: 'get',
        params,
        responseType: 'blob'
    });
}