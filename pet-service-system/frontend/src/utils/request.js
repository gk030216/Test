import axios from 'axios';
import { Message } from 'element-ui';

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 通过 vue.config.js 代理到后端
  timeout: 10000
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 可在此添加 token 等
    return config;
  },
  error => Promise.reject(error)
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      Message.error(res.message || '请求失败');
      return Promise.reject(res);
    }
    return res;
  },
  error => {
    Message.error('网络错误，请稍后重试');
    return Promise.reject(error);
  }
);

export default service;

