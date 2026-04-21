<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
import { getSettings } from '@/api/settings';

export default {
  name: 'App',
  created() {
    // 只有在已登录的情况下才从后端加载设置
    const token = localStorage.getItem('token');
    if (token) {
      this.loadSettings();
    } else {
      // 未登录时使用默认设置，不发起 API 请求
      this.setDefaultSettings();
    }
  },
  methods: {
    setDefaultSettings() {
      const defaultSettings = {
        basic: {
          siteName: '宠物服务系统',
          siteLogo: '',
          siteDesc: '用心服务每一个宠物家庭',
          copyright: '© 2026 宠物服务系统',
          icp: '',
          servicePhone: '400-888-6666',
          serviceEmail: 'service@petservice.com',
          maintenanceMode: false
        }
      };
      localStorage.setItem('systemSettings', JSON.stringify(defaultSettings));
      document.title = defaultSettings.basic.siteName;

      // 触发全局事件，通知其他组件设置已加载
      this.$bus && this.$bus.$emit('settings-loaded', defaultSettings);
    },

    async loadSettings() {
      try {
        const res = await getSettings();
        if (res.code === 200) {
          const settings = res.data;
          localStorage.setItem('systemSettings', JSON.stringify(settings));
          if (settings.basic && settings.basic.siteName) {
            document.title = settings.basic.siteName;
          }
          this.$bus && this.$bus.$emit('settings-loaded', settings);
        }
      } catch (error) {
        console.error('加载系统设置失败', error);
        this.setDefaultSettings();
      }
    }
  }
};
</script>

<style>
/* 样式保持不变 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

/* 维护模式遮罩 */
.maintenance-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #fff;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.maintenance-mask i {
  font-size: 80px;
  color: #667eea;
  margin-bottom: 20px;
}

.maintenance-mask h2 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 10px;
}

.maintenance-mask p {
  color: #999;
  font-size: 16px;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a67d8, #6b46a0);
}

/* 全局消息框样式 */
.el-message {
  border-radius: 12px !important;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15) !important;
}

.el-notification {
  border-radius: 16px !important;
}
</style>