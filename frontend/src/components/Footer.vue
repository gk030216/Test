<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-content">
        <!-- 左侧：品牌信息 -->
        <div class="footer-brand">
          <div class="brand-logo">
            <span class="logo-icon">🐾</span>
            <span class="logo-text">{{ siteName }}</span>
          </div>
          <p class="brand-desc">{{ siteDesc }}</p>
        </div>

        <!-- 中间：快捷链接 -->
        <div class="footer-links">
          <div class="link-group">
            <h4>关于我们</h4>
            <div class="link-list">
              <a href="#">公司简介</a>
              <a href="#">联系我们</a>
              <a href="#">加入我们</a>
              <a href="#">合作伙伴</a>
            </div>
          </div>
          <div class="link-group">
            <h4>服务项目</h4>
            <div class="link-list">
              <a href="#">宠物美容</a>
              <a href="#">宠物寄养</a>
              <a href="#">宠物医疗</a>
              <a href="#">宠物训练</a>
            </div>
          </div>
          <div class="link-group">
            <h4>帮助中心</h4>
            <div class="link-list">
              <a href="#">常见问题</a>
              <a href="#">服务协议</a>
              <a href="#">隐私政策</a>
              <a href="#">投诉建议</a>
            </div>
          </div>
        </div>

        <!-- 右侧：联系方式 - 两行两列 -->
        <div class="footer-contact">
          <h4>联系我们</h4>
          <div class="contact-grid">
            <div class="contact-left">
              <div class="contact-item">
                <i class="el-icon-phone-outline"></i>
                <span>客服热线：{{ servicePhone }}</span>
              </div>
              <div class="contact-item">
                <i class="el-icon-time"></i>
                <span>服务时间：9:00 - 21:00</span>
              </div>
            </div>
            <div class="contact-right">
              <div class="contact-item">
                <i class="el-icon-message"></i>
                <span>客服邮箱：{{ serviceEmail }}</span>
              </div>
              <div class="contact-item">
                <i class="el-icon-location-outline"></i>
                <span>公司地址：XX市XX区XX科技园</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部版权 -->
      <div class="footer-bottom">
        <p>{{ copyright }}</p>
        <p v-if="icp">{{ icp }}</p>
      </div>
    </div>
  </footer>
</template>

<script>
import { getSettings } from '@/api/settings';

export default {
  name: 'Footer',
  data() {
    return {
      siteName: '宠物服务系统',
      siteDesc: '用心服务每一个宠物家庭',
      copyright: 'Copyright © 2026 宠物服务系统 版权所有',
      icp: '',
      servicePhone: '400-888-6666',
      serviceEmail: 'service@petservice.com'
    };
  },
  created() {
    this.loadSettings();
  },
  methods: {
    loadSettings() {
      // 先尝试从缓存读取
      const settings = localStorage.getItem('systemSettings');
      if (settings) {
        try {
          const basic = JSON.parse(settings).basic;
          this.siteName = basic.siteName || '宠物服务系统';
          this.siteDesc = basic.siteDesc || '用心服务每一个宠物家庭';
          this.copyright = basic.copyright || 'Copyright © 2026 宠物服务系统 版权所有';
          this.icp = basic.icp || '';
          this.servicePhone = basic.servicePhone || '400-888-6666';
          this.serviceEmail = basic.serviceEmail || 'service@petservice.com';
        } catch (e) {
          console.error('解析系统设置失败', e);
        }
      }

      // 从后端获取最新设置
      this.fetchSettings();

      // 监听设置更新事件
      this.$bus && this.$bus.$on('settings-loaded', (settings) => {
        if (settings && settings.basic) {
          this.siteName = settings.basic.siteName || '宠物服务系统';
          this.siteDesc = settings.basic.siteDesc || '用心服务每一个宠物家庭';
          this.copyright = settings.basic.copyright || 'Copyright © 2026 宠物服务系统 版权所有';
          this.icp = settings.basic.icp || '';
          this.servicePhone = settings.basic.servicePhone || '400-888-6666';
          this.serviceEmail = settings.basic.serviceEmail || 'service@petservice.com';
        }
      });
    },

// 新增方法
    async fetchSettings() {
      try {
        const res = await getSettings();
        if (res.code === 200) {
          localStorage.setItem('systemSettings', JSON.stringify(res.data));
          const basic = res.data.basic;
          this.siteName = basic.siteName || '宠物服务系统';
          this.siteDesc = basic.siteDesc || '用心服务每一个宠物家庭';
          this.copyright = basic.copyright || 'Copyright © 2026 宠物服务系统 版权所有';
          this.icp = basic.icp || '';
          this.servicePhone = basic.servicePhone || '400-888-6666';
          this.serviceEmail = basic.serviceEmail || 'service@petservice.com';
        }
      } catch (error) {
        console.error('获取系统设置失败', error);
      }
    },
  },
  beforeDestroy() {
    this.$bus && this.$bus.$off('settings-loaded');
  }
};
</script>

<style scoped>
.footer {
  background: #1a2632;
  color: #fff;
  position: relative;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 30px 20px;
  position: relative;
  z-index: 1;
}

.footer-content {
  display: grid;
  grid-template-columns: 1fr 2fr 1.5fr;
  gap: 40px;
  margin-bottom: 30px;
}

/* 品牌区域 */
.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.brand-desc {
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.5;
  margin-bottom: 20px;
  font-size: 13px;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-link {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s;
  font-size: 16px;
}

.social-link:hover {
  background: #409EFF;
  color: white;
}

/* 链接区域 */
.footer-links {
  display: flex;
  justify-content: space-between;
  gap: 30px;
}

.link-group {
  flex: 1;
  min-width: 0;
}

.link-group h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  color: white;
  position: relative;
  padding-bottom: 8px;
}

.link-group h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 28px;
  height: 2px;
  background: #409EFF;
}

.link-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.link-list a {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: 13px;
  transition: all 0.3s;
  white-space: nowrap;
}

.link-list a:hover {
  color: #409EFF;
}

/* 联系方式区域 - 两行两列布局 */
.footer-contact h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  color: white;
  position: relative;
  padding-bottom: 8px;
}

.footer-contact h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 28px;
  height: 2px;
  background: #409EFF;
}

.contact-grid {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.contact-left,
.contact-right {
  flex: 1;
  min-width: 0;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}

.contact-item i {
  font-size: 16px;
  color: #409EFF;
  width: 20px;
  flex-shrink: 0;
}

.contact-item span {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 二维码 */
.qrcode {
  margin-top: 4px;
}

.qrcode-placeholder {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.qrcode-placeholder i {
  font-size: 24px;
  color: #409EFF;
}

.qrcode-placeholder span {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.qrcode-placeholder:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: #409EFF;
}

/* 底部版权 */
.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
}

.footer-bottom p {
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
  margin: 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .footer-content {
    grid-template-columns: 1fr 1.5fr;
    gap: 30px;
  }

  .footer-links {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .footer-container {
    padding: 30px 20px 16px;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .footer-links {
    flex-direction: column;
    gap: 24px;
  }

  .link-list {
    flex-wrap: wrap;
  }

  .link-list a {
    white-space: normal;
  }

  .contact-grid {
    flex-direction: column;
    gap: 0;
  }

  .footer-bottom {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .footer-container {
    padding: 24px 16px 16px;
  }

  .link-list {
    gap: 12px;
  }

  .qrcode-placeholder {
    width: 100%;
    justify-content: center;
  }
}
</style>