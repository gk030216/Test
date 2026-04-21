import request from '@/utils/request';

// 前台获取轮播图
export function getCarouselList() {
    return request({
        url: '/carousel/list',
        method: 'get'
    });
}

// 后台获取轮播图列表
export function getAdminCarouselList(params) {
    return request({
        url: '/admin/carousel/list',
        method: 'get',
        params
    });
}

// 获取轮播图详情
export function getCarouselById(id) {
    return request({
        url: `/admin/carousel/${id}`,
        method: 'get'
    });
}

// 添加轮播图
export function addCarousel(data) {
    return request({
        url: '/admin/carousel/add',
        method: 'post',
        data
    });
}

// 更新轮播图
export function updateCarousel(data) {
    return request({
        url: '/admin/carousel/update',
        method: 'put',
        data
    });
}

// 更新状态
export function updateCarouselStatus(id, status) {
    return request({
        url: '/admin/carousel/status',
        method: 'put',
        params: { id, status }
    });
}

// 批量更新状态
export function batchUpdateCarouselStatus(ids, status) {
    return request({
        url: '/admin/carousel/batch-status',
        method: 'put',
        params: { ids, status }
    });
}

// 删除
export function deleteCarousel(id) {
    return request({
        url: `/admin/carousel/delete/${id}`,
        method: 'delete'
    });
}

// 批量删除
export function batchDeleteCarousel(ids) {
    return request({
        url: '/admin/carousel/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

export function batchUpdateCarouselSort(sortList) {
    return request({
        url: '/admin/carousel/batch-sort',
        method: 'put',
        data: sortList
    });
}