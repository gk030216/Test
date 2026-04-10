// @/api/settings.js
import request from '@/utils/request';

// 获取所有设置
export function getSettings() {
    return request({
        url: '/admin/settings',
        method: 'get'
    });
}

// 保存基础设置
export function saveBasicSettings(data) {
    return request({
        url: '/admin/settings/basic',
        method: 'put',
        data
    });
}

// 保存邮件配置
export function saveEmailSettings(data) {
    return request({
        url: '/admin/settings/email',
        method: 'put',
        data
    });
}

// 测试邮件配置
export function testEmailConfig(data) {
    return request({
        url: '/admin/settings/email/test',
        method: 'post',
        data
    });
}

// 保存支付配置
export function savePaySettings(data) {
    return request({
        url: '/admin/settings/pay',
        method: 'put',
        data
    });
}

// 保存AI配置
export function saveAISettings(data) {
    return request({
        url: '/admin/settings/ai',
        method: 'put',
        data
    });
}

// 测试AI连接
export function testAIConfig(data) {
    return request({
        url: '/admin/settings/ai/test',
        method: 'post',
        data
    });
}

// 保存上传配置
export function saveUploadSettings(data) {
    return request({
        url: '/admin/settings/upload',
        method: 'put',
        data
    });
}