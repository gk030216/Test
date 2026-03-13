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

