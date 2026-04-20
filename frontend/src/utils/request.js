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
        // ✅ 关键：如果是 blob 类型响应，直接返回 Blob 数据
        if (response.config.responseType === 'blob') {
            return response.data;
        }

        const res = response.data;
        if (res.code !== 200) {
            if (res.code === 401) {
                const currentPath = window.location.pathname;
                if (currentPath !== '/login' && currentPath !== '/register' && currentPath !== '/forget-password') {
                    Message.error('登录已过期，请重新登录');
                    localStorage.removeItem('token');
                    localStorage.removeItem('userInfo');
                    window.location.href = '/login';
                }
            } else if (res.code === 403) {
                Message.error('权限不足，无法执行此操作');
            } else {
                Message.error(res.message || '请求失败');
            }
            return Promise.reject(res);
        }
        return res;
    },
    error => {
        console.error('请求错误:', error);
        if (error.response) {
            const { status } = error.response;
            if (status === 401) {
                const currentPath = window.location.pathname;
                if (currentPath !== '/login' && currentPath !== '/register' && currentPath !== '/forget-password') {
                    Message.error('登录已过期，请重新登录');
                    localStorage.removeItem('token');
                    localStorage.removeItem('userInfo');
                    window.location.href = '/login';
                }
            } else if (status === 403) {
                Message.error('权限不足，无法执行此操作');
            } else {
                Message.error(error.response.data?.message || '请求失败，请稍后重试');
            }
        } else if (error.message === 'Network Error') {
            Message.error('网络连接失败，请检查网络');
        } else {
            Message.error('请求失败，请稍后重试');
        }
        return Promise.reject(error);
    }
);

export default service;