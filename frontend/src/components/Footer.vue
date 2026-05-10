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

        <!-- 右侧：联系方式 -->
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
      siteName: '喵汪星球',
      siteDesc: '一站式宠物服务平台 · 让陪伴更长久',
      copyright: 'Copyright © 2026 喵汪星球 版权所有',
      icp: '',
      servicePhone: '400-888-6666',
      serviceEmail: 'service@petcare.com'
    };
  },
  created() {
    this.loadSettings();
  },
  methods: {
    loadSettings() {
      const settings = localStorage.getItem('systemSettings');
      if (settings) {
        try {
          const basic = JSON.parse(settings).basic;
          this.siteName = basic.siteName || '喵汪星球';
          this.siteDesc = basic.siteDesc || '一站式宠物服务平台 · 让陪伴更长久';
          this.copyright = basic.copyright || 'Copyright © 2026 喵汪星球 版权所有';
          this.icp = basic.icp || '';
          this.servicePhone = basic.servicePhone || '400-888-6666';
          this.serviceEmail = basic.serviceEmail || 'service@petcare.com';
        } catch (e) {
          console.error('解析系统设置失败', e);
        }
      }
      this.fetchSettings();
      this.$bus && this.$bus.$on('settings-loaded', (settings) => {
        if (settings && settings.basic) {
          this.siteName = settings.basic.siteName || '喵汪星球';
          this.siteDesc = settings.basic.siteDesc || '一站式宠物服务平台 · 让陪伴更长久';
          this.copyright = settings.basic.copyright || 'Copyright © 2026 喵汪星球 版权所有';
          this.icp = settings.basic.icp || '';
          this.servicePhone = settings.basic.servicePhone || '400-888-6666';
          this.serviceEmail = settings.basic.serviceEmail || 'service@petcare.com';
        }
      });
    },

    async fetchSettings() {
      try {
        const res = await getSettings();
        if (res.code === 200) {
          localStorage.setItem('systemSettings', JSON.stringify(res.data));
          const basic = res.data.basic;
          this.siteName = basic.siteName || '喵汪星球';
          this.siteDesc = basic.siteDesc || '一站式宠物服务平台 · 让陪伴更长久';
          this.copyright = basic.copyright || 'Copyright © 2026 喵汪星球 版权所有';
          this.icp = basic.icp || '';
          this.servicePhone = basic.servicePhone || '400-888-6666';
          this.serviceEmail = basic.serviceEmail || 'service@petcare.com';
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
  background: #2d2420;
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
  color: #fff;
}

.brand-desc {
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.5;
  margin-bottom: 20px;
  font-size: 13px;
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
  color: #fff;
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
  background: #f59e4b;
}

.link-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.link-list a {
  color: rgba(255, 255, 255, 0.5);
  text-decoration: none;
  font-size: 13px;
  transition: all 0.3s;
  white-space: nowrap;
}

.link-list a:hover {
  color: #f59e4b;
}

/* 联系方式 */
.footer-contact h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #fff;
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
  background: #f59e4b;
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
  color: rgba(255, 255, 255, 0.55);
  font-size: 12px;
}

.contact-item i {
  font-size: 16px;
  color: #f59e4b;
  width: 20px;
  flex-shrink: 0;
}

/* 底部版权 */
.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
}

.footer-bottom p {
  color: rgba(255, 255, 255, 0.35);
  font-size: 12px;
  margin: 0;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .footer-content {
    grid-template-columns: 1fr 1.5fr;
    gap: 30px;
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
}
</style>
