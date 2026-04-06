import request from '@/utils/request';

// ========== 帖子相关 ==========

// 获取帖子列表
export function getPostList(params) {
    return request({
        url: '/community/posts',
        method: 'get',
        params
    });
}

// 获取帖子详情
export function getPostDetail(id) {
    return request({
        url: `/community/post/${id}`,
        method: 'get'
    });
}

// 发布帖子
export function addPost(data) {
    return request({
        url: '/community/post',
        method: 'post',
        data
    });
}

// 更新帖子
export function updatePost(data) {
    return request({
        url: '/community/post',
        method: 'put',
        data
    });
}

// 删除帖子
export function deletePost(id) {
    return request({
        url: `/community/post/${id}`,
        method: 'delete'
    });
}

// 获取用户帖子列表
export function getUserPosts(params) {
    return request({
        url: '/community/user/posts',
        method: 'get',
        params
    });
}

// 获取收藏列表
export function getFavorites(params) {
    return request({
        url: '/community/favorites',
        method: 'get',
        params
    });
}

// 点赞/取消点赞
export function toggleLike(id) {
    return request({
        url: `/community/post/${id}/like`,
        method: 'post'
    });
}

// 收藏/取消收藏
export function toggleFavorite(id) {
    return request({
        url: `/community/post/${id}/favorite`,
        method: 'post'
    });
}

// ========== 评论相关 ==========

// 获取帖子评论
export function getComments(postId, params) {
    return request({
        url: `/community/post/${postId}/comments`,
        method: 'get',
        params
    });
}

// 添加评论
export function addComment(data) {
    return request({
        url: '/community/comment',
        method: 'post',
        data
    });
}

// 删除评论
export function deleteComment(id) {
    return request({
        url: `/community/comment/${id}`,
        method: 'delete'
    });
}

// ========== 管理员接口 ==========

// 获取帖子列表（后台）
export function getAdminPostList(params) {
    return request({
        url: '/admin/community/posts',
        method: 'get',
        params
    });
}

// 更新帖子状态
export function updatePostStatus(id, status) {
    return request({
        url: '/admin/community/post/status',
        method: 'put',
        params: { id, status }
    });
}

// 置顶/取消置顶
export function updatePostTop(id, isTop) {
    return request({
        url: '/admin/community/post/top',
        method: 'put',
        params: { id, isTop }
    });
}

// 精华/取消精华
export function updatePostEssence(id, isEssence) {
    return request({
        url: '/admin/community/post/essence',
        method: 'put',
        params: { id, isEssence }
    });
}

// 管理员删除帖子
export function adminDeletePost(id) {
    return request({
        url: `/admin/community/post/${id}`,
        method: 'delete'
    });
}