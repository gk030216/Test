import axios from 'axios';
import { Message } from 'element-ui';

// 创建axios实例
const service = axios.create({
    baseURL: '/api',
    timeout: 10000
});

// 请求拦截器 - 添加 Token
service.interceptors.request.use(
    config => {
        // 从 localStorage 获取 token
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token;
        }
        return config;
    },
    error => Promise.reject(error)
);

// 响应拦截器 - 处理 Token 过期
service.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code !== 200) {
            // 401 未授权，Token 过期或无效
            if (res.code === 401) {
                Message.error('登录已过期，请重新登录');
                localStorage.removeItem('token');
                localStorage.removeItem('userInfo');
                // 跳转到登录页
                window.location.href = '/login';
            } else {
                Message.error(res.message || '请求失败');
            }
            return Promise.reject(res);
        }
        return res;
    },
    error => {
        if (error.response && error.response.status === 401) {
            Message.error('登录已过期，请重新登录');
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            window.location.href = '/login';
        } else {
            Message.error('网络错误，请稍后重试');
        }
        return Promise.reject(error);
    }
);

export default service;