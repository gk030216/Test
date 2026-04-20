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
export function getUserPosts(page, pageSize) {
    return request({
        url: '/community/user/posts',
        method: 'get',
        params: { page, pageSize }
    });
}

// 获取收藏列表
export function getFavorites(page, pageSize) {
    return request({
        url: '/community/favorites',
        method: 'get',
        params: { page, pageSize }
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

// 取消收藏（别名，功能相同）
export function removeFavorite(id) {
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

// 删除评论（用户）
export function deleteComment(id) {
    return request({
        url: `/community/comment/${id}`,
        method: 'delete'
    });
}

// 获取用户评论列表
export function getUserComments(page, pageSize) {
    return request({
        url: '/community/user/comments',
        method: 'get',
        params: { page, pageSize }
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

// 获取帖子统计数据
export function getPostStatistics() {
    return request({
        url: '/admin/community/statistics',
        method: 'get'
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

// 批量置顶
export function batchUpdatePostTop(ids, isTop) {
    return request({
        url: '/admin/community/posts/batch-top',
        method: 'put',
        params: { ids, isTop }
    });
}

// 批量精华
export function batchUpdatePostEssence(ids, isEssence) {
    return request({
        url: '/admin/community/posts/batch-essence',
        method: 'put',
        params: { ids, isEssence }
    });
}

// 批量删除帖子
export function batchDeletePosts(ids) {
    return request({
        url: '/admin/community/posts/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// 管理员删除帖子
export function adminDeletePost(id) {
    return request({
        url: `/admin/community/post/${id}`,
        method: 'delete'
    });
}

// ========== 管理员评论管理接口 ==========

// 获取评论列表（后台）
export function getAdminCommentList(params) {
    return request({
        url: '/admin/community/comments',
        method: 'get',
        params
    });
}

// 获取评论统计
export function getCommentStatistics() {
    return request({
        url: '/admin/community/comment-statistics',
        method: 'get'
    });
}

// 更新评论状态
export function updateCommentStatus(id, status) {
    return request({
        url: '/admin/community/comment/status',
        method: 'put',
        params: { id, status }
    });
}

// 管理员删除评论（软删除）
export function adminDeleteComment(id) {
    return request({
        url: `/admin/community/comment/${id}`,
        method: 'delete'
    });
}

// 批量删除评论
export function batchDeleteComments(ids) {
    return request({
        url: '/admin/community/comments/batch-delete',
        method: 'delete',
        params: { ids }
    });
}

// 恢复评论
export function restoreComment(id) {
    return request({
        url: `/admin/community/comment/restore/${id}`,
        method: 'put'
    });
}


// 获取评论的回复列表
export function getCommentReplies(commentId) {
    return request({
        url: `/admin/community/comment/${commentId}/replies`,
        method: 'get'
    });
}



