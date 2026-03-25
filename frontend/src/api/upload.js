import request from '@/utils/request';

// 上传头像
export function uploadAvatar(data) {
    return request({
        url: '/upload/avatar',
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });


}

// 上传轮播图
export function uploadCarouselImage(data) {
    return request({
        url: '/upload/carousel',
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}