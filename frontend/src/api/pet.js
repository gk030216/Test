import request from '@/utils/request';

// ========== 宠物档案 ==========

// 获取用户宠物列表
export function getPetList() {
    return request({
        url: '/pet/list',
        method: 'get'
    });
}

// 获取默认宠物
export function getDefaultPet() {
    return request({
        url: '/pet/default',
        method: 'get'
    });
}

// 获取宠物详情
export function getPetDetail(id) {
    return request({
        url: `/pet/${id}`,
        method: 'get'
    });
}

// 添加宠物
export function addPet(data) {
    return request({
        url: '/pet/add',
        method: 'post',
        data
    });
}

// 更新宠物
export function updatePet(data) {
    return request({
        url: '/pet/update',
        method: 'put',
        data
    });
}

// 删除宠物
export function deletePet(id) {
    return request({
        url: `/pet/delete/${id}`,
        method: 'delete'
    });
}

// 设置默认宠物
export function setDefaultPet(id) {
    return request({
        url: `/pet/default/${id}`,
        method: 'put'
    });
}

// ========== 疫苗记录 ==========

// 获取疫苗记录
export function getVaccineRecords(petId) {
    return request({
        url: `/pet/vaccine/list/${petId}`,
        method: 'get'
    });
}

// 添加疫苗记录
export function addVaccineRecord(data) {
    return request({
        url: '/pet/vaccine/add',
        method: 'post',
        data
    });
}

// 删除疫苗记录
export function deleteVaccineRecord(id) {
    return request({
        url: `/pet/vaccine/delete/${id}`,
        method: 'delete'
    });
}

// 获取即将到期的疫苗
export function getUpcomingVaccines() {
    return request({
        url: '/pet/vaccine/upcoming',
        method: 'get'
    });
}

// ========== 体检记录 ==========

// 获取体检记录
export function getHealthRecords(petId) {
    return request({
        url: `/pet/health/list/${petId}`,
        method: 'get'
    });
}

// 添加体检记录
export function addHealthRecord(data) {
    return request({
        url: '/pet/health/add',
        method: 'post',
        data
    });
}

// 删除体检记录
export function deleteHealthRecord(id) {
    return request({
        url: `/pet/health/delete/${id}`,
        method: 'delete'
    });
}

// ========== 管理员接口 ==========

// 获取所有宠物列表（后台）
export function getAdminPetList(params) {
    return request({
        url: '/admin/pet/list',
        method: 'get',
        params
    });
}

// 获取宠物统计数据
export function getPetStatistics() {
    return request({
        url: '/admin/pet/statistics',
        method: 'get'
    });
}

// 批量删除宠物
export function batchDeletePets(ids) {
    return request({
        url: '/admin/pet/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// 获取所有疫苗记录
export function getAllVaccineRecords(params) {
    return request({
        url: '/admin/pet/vaccine/all',
        method: 'get',
        params
    });
}

// 获取所有体检记录
export function getAllHealthRecords(params) {
    return request({
        url: '/admin/pet/health/all',
        method: 'get',
        params
    });
}

// 更新疫苗记录
export function updateVaccineRecord(data) {
    return request({
        url: '/admin/pet/vaccine/update',
        method: 'put',
        data
    });
}

// 更新体检记录
export function updateHealthRecord(data) {
    return request({
        url: '/admin/pet/health/update',
        method: 'put',
        data
    });
}

// 批量删除疫苗记录
export function batchDeleteVaccineRecords(ids) {
    return request({
        url: '/admin/pet/vaccine/batch-delete',
        method: 'delete',
        data: ids
    });
}

// 批量删除体检记录
export function batchDeleteHealthRecords(ids) {
    return request({
        url: '/admin/pet/health/batch-delete',
        method: 'delete',
        data: ids
    });
}

// 获取即将到期的宠物数量（7天内需要接种疫苗的宠物）
export function getUpcomingPetsCount() {
    return request({
        url: '/admin/pet/vaccine/upcoming/pets/count',
        method: 'get'
    });
}

// 获取有疫苗记录的正常宠物数量（去重）
export function getDistinctPetsWithVaccineCount() {
    return request({
        url: '/admin/pet/vaccine/pets/count',
        method: 'get'
    });
}

// 获取有体检记录的正常宠物数量（去重）
export function getDistinctPetsWithHealthRecordCount() {
    return request({
        url: '/admin/pet/health/pets/count',
        method: 'get'
    });
}

// 获取正常宠物的平均体重
export function getAvgWeight() {
    return request({
        url: '/admin/pet/health/avg-weight',
        method: 'get'
    });
}

// ========== 疫苗库存管理 ==========

// 获取疫苗库存列表
export function getVaccineStockList(params) {
    return request({
        url: '/admin/pet/vaccine/stock/list',
        method: 'get',
        params
    });
}

// 新增疫苗
export function addVaccine(data) {
    return request({
        url: '/admin/pet/vaccine/add',
        method: 'post',
        data
    });
}

// 更新疫苗信息
export function updateVaccine(data) {
    return request({
        url: '/admin/pet/vaccine/update',
        method: 'put',
        data
    });
}

// 更新疫苗库存（入库/出库）
export function updateVaccineStock(data) {
    return request({
        url: '/admin/pet/vaccine/stock/update',
        method: 'put',
        data
    });
}

// 获取疫苗库存列表（用于选择）
export function getVaccineInventoryList(params) {
    return request({
        url: '/admin/inventory/list',
        method: 'get',
        params: { ...params, type: 'vaccine' }
    });
}