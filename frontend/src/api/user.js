import request from '@/utils/request';

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  });
}

// 用户注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  });
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/user/reset-password',
    method: 'post',
    data
  });
}

// 检查用户名是否存在
export function checkUsername(username) {
  return request({
    url: '/user/check-username',
    method: 'get',
    params: { username }
  });
}

// 检查邮箱是否存在
export function checkEmail(email) {
  return request({
    url: '/user/check-email',
    method: 'get',
    params: { email }
  });
}

// 发送注册验证码
export function sendRegisterCode(email) {
  return request({
    url: '/code/register',
    method: 'post',
    params: { email }
  });
}

// 发送找回密码验证码
export function sendForgetCode(email) {
  return request({
    url: '/code/forget',
    method: 'post',
    params: { email }
  });
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/current-user',
    method: 'get'
  });
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  });
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/change-password',
    method: 'post',
    data
  });
}

// 发送修改邮箱验证码
export function sendEmailCode(email) {
  return request({
    url: '/code/change-email',
    method: 'post',
    params: { email }
  });
}

// 修改邮箱（确认）
export function updateEmail(data) {
  return request({
    url: '/user/update-email',
    method: 'post',
    data
  });
}
// ============= 管理员接口 =============

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  });
}

// 添加用户
export function addUser(data) {
  return request({
    url: '/admin/user/add',
    method: 'post',
    data
  });
}

// 更新用户
export function updateUser(data) {
  return request({
    url: '/admin/user/update',
    method: 'put',
    data
  });
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: '/admin/user/status',
    method: 'put',
    params: { id, status }
  });
}

// 批量更新用户状态
export function batchUpdateUserStatus(ids, status) {
  return request({
    url: '/admin/user/batch-status',
    method: 'put',
    params: { ids, status }
  });
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/admin/user/delete/${id}`,
    method: 'delete'
  });
}

// 批量删除用户
export function batchDeleteUsers(ids) {
  return request({
    url: '/admin/user/batch-delete',
    method: 'delete',
    params: { ids }
  });
}

// 获取用户头像
export function getUserAvatar(userId) {
  return request({
    url: `/user/avatar/${userId}`,
    method: 'get'
  });
}

//
export function getAllStaff() {
  return request({
    url: '/admin/user/staff/all',
    method: 'get'
  });
}

// ============= 用户分析接口 =============

// 获取用户统计数据
export function getUserStatistics() {
  return request({
    url: '/admin/user/statistics',
    method: 'get'
  });
}

// 获取用户增长趋势
export function getUserTrend(params) {
  return request({
    url: '/admin/user/trend',
    method: 'get',
    params
  });
}

// 获取用户角色分布
export function getUserRoleDistribution() {
  return request({
    url: '/admin/user/role-distribution',
    method: 'get'
  });
}

// 获取用户性别分布
export function getUserGenderDistribution() {
  return request({
    url: '/admin/user/gender-distribution',
    method: 'get'
  });
}

// 获取用户活跃度分布
export function getUserActivityDistribution() {
  return request({
    url: '/admin/user/activity-distribution',
    method: 'get'
  });
}