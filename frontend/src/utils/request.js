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

// 不需要登录验证的接口路径列表（首页所有接口）
const publicApis = [
    // 轮播图
    '/carousel/list',

    // 服务相关
    '/service/hot',
    '/service/hot/list',
    '/service/list',
    '/service/category/list',

    // 商品相关（商城）
    '/product/hot',           // 热门商品
    '/product/new',           // 新品商品
    '/product/list',          // 商品列表
    '/product/page',          // 商品分页
    '/product/category/list', // 商品分类
    '/product/search',        // 商品搜索
    '/product/',              // 商品详情 /api/product/123

    // 公告相关
    '/consultation-notice/home',
    '/consultation-notice/list',
    '/consultation-notice/',

    // 社区相关
    '/community/post/list',
    '/community/post/',
    '/community/posts',

    // 其他
    '/settings',
];

// 检查是否为公开接口
const isPublicApi = (url) => {
    if (!url) return false;
    return publicApis.some(api => url.includes(api));
};

// 响应拦截器 - 处理 Token 过期
service.interceptors.response.use(
    response => {
        // 如果是 blob 类型响应，直接返回 Blob 数据
        if (response.config.responseType === 'blob') {
            return response.data;
        }

        const res = response.data;

        // 401 未授权
        if (res.code === 401) {
            const currentPath = window.location.pathname;
            const isPublic = isPublicApi(response.config.url);

            // 如果是公开接口，不跳转，直接返回数据（或空数据）
            if (isPublic) {
                console.warn('公开接口返回401，可能后端未配置白名单，返回空数据');
                // 返回空数据结构
                if (Array.isArray(res.data)) {
                    return { code: 200, data: [] };
                }
                if (res.data && typeof res.data === 'object') {
                    return { code: 200, data: { list: [], total: 0 } };
                }
                return { code: 200, data: [] };
            }

            // 非公开接口，需要登录
            if (currentPath !== '/login' && currentPath !== '/register' && currentPath !== '/forget-password' && currentPath !== '/') {
                Message.error('登录已过期，请重新登录');
                localStorage.removeItem('token');
                localStorage.removeItem('userInfo');
                window.location.href = '/login';
            }
            return Promise.reject(res);
        }

        if (res.code !== 200) {
            if (res.code === 403) {
                Message.error('权限不足，无法执行此操作');
            }
            return Promise.reject(res);
        }
        return res;
    },
    error => {
        console.error('请求错误:', error);
        if (error.response) {
            const { status, config } = error.response;
            if (status === 401) {
                const currentPath = window.location.pathname;
                const isPublic = isPublicApi(config?.url);

                if (isPublic) {
                    // 公开接口返回空数据
                    return Promise.resolve({ code: 200, data: [] });
                }

                if (currentPath !== '/login' && currentPath !== '/register' && currentPath !== '/forget-password' && currentPath !== '/') {
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