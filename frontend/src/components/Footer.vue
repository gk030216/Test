<template>
  <footer class="footer">
    <div class="footer-container">
      <!-- 顶部装饰 -->
      <div class="footer-wave">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 120">
          <path fill="#f8f9fa" fill-opacity="1" d="M0,64L48,69.3C96,75,192,85,288,85.3C384,85,480,75,576,74.7C672,75,768,85,864,85.3C960,85,1056,75,1152,69.3C1248,64,1344,64,1392,64L1440,64L1440,120L1392,120C1344,120,1248,120,1152,120C1056,120,960,120,864,120C768,120,672,120,576,120C480,120,384,120,288,120C192,120,96,120,48,120L0,120Z"></path>
        </svg>
      </div>

      <div class="footer-content">
        <!-- 左侧：品牌信息 -->
        <div class="footer-brand">
          <div class="brand-logo">
            <span class="logo-icon">🐾</span>
            <span class="logo-text">{{ siteName }}</span>
          </div>
          <p class="brand-desc">{{ siteDesc }}</p>
          <div class="social-links">
            <a href="#" class="social-link"><i class="el-icon-chat-dot-round"></i></a>
            <a href="#" class="social-link"><i class="el-icon-qq"></i></a>
            <a href="#" class="social-link"><i class="el-icon-microphone"></i></a>
            <a href="#" class="social-link"><i class="el-icon-mobile-phone"></i></a>
          </div>
        </div>

        <!-- 中间：快捷链接 -->
        <div class="footer-links">
          <div class="link-group">
            <h4>关于我们</h4>
            <ul>
              <li><a href="#">公司简介</a></li>
              <li><a href="#">联系我们</a></li>
              <li><a href="#">加入我们</a></li>
              <li><a href="#">合作伙伴</a></li>
            </ul>
          </div>
          <div class="link-group">
            <h4>服务项目</h4>
            <ul>
              <li><a href="#">宠物美容</a></li>
              <li><a href="#">宠物寄养</a></li>
              <li><a href="#">宠物医疗</a></li>
              <li><a href="#">宠物训练</a></li>
            </ul>
          </div>
          <div class="link-group">
            <h4>帮助中心</h4>
            <ul>
              <li><a href="#">常见问题</a></li>
              <li><a href="#">服务协议</a></li>
              <li><a href="#">隐私政策</a></li>
              <li><a href="#">投诉建议</a></li>
            </ul>
          </div>
        </div>

        <!-- 右侧：联系方式 -->
        <div class="footer-contact">
          <h4>联系我们</h4>
          <div class="contact-item">
            <i class="el-icon-phone-outline"></i>
            <span>客服热线：{{ servicePhone }}</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-message"></i>
            <span>客服邮箱：{{ serviceEmail }}</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-time"></i>
            <span>服务时间：9:00 - 21:00</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-location-outline"></i>
            <span>公司地址：XX市XX区XX科技园</span>
          </div>
          <div class="qrcode">
            <div class="qrcode-placeholder">
              <i class="el-icon-mobile-phone"></i>
              <span>扫码关注公众号</span>
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
export default {
  name: 'Footer',
  data() {
    return {
      siteName: '宠物服务系统',
      siteDesc: '用心服务每一个宠物家庭\n让爱与陪伴更简单',
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
      const settings = localStorage.getItem('systemSettings');
      if (settings) {
        try {
          const basic = JSON.parse(settings).basic;
          this.siteName = basic.siteName || '宠物服务系统';
          this.siteDesc = basic.siteDesc || '用心服务每一个宠物家庭\n让爱与陪伴更简单';
          this.copyright = basic.copyright || 'Copyright © 2026 宠物服务系统 版权所有';
          this.icp = basic.icp || '';
          this.servicePhone = basic.servicePhone || '400-888-6666';
          this.serviceEmail = basic.serviceEmail || 'service@petservice.com';
        } catch (e) {
          console.error('解析系统设置失败', e);
        }
      }

      // 监听设置更新
      this.$bus && this.$bus.$on('settings-loaded', (settings) => {
        if (settings && settings.basic) {
          this.siteName = settings.basic.siteName || '宠物服务系统';
          this.siteDesc = settings.basic.siteDesc || '用心服务每一个宠物家庭\n让爱与陪伴更简单';
          this.copyright = settings.basic.copyright || 'Copyright © 2026 宠物服务系统 版权所有';
          this.icp = settings.basic.icp || '';
          this.servicePhone = settings.basic.servicePhone || '400-888-6666';
          this.serviceEmail = settings.basic.serviceEmail || 'service@petservice.com';
        }
      });
    }
  },
  beforeDestroy() {
    this.$bus && this.$bus.$off('settings-loaded');
  }
};
</script>

<style scoped>
/* 保持原有样式不变 */
.footer {
  background: linear-gradient(135deg, #2c3e50 0%, #1a2632 100%);
  color: #fff;
  position: relative;
  margin-top: auto;
}

.footer-wave {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  overflow: hidden;
  line-height: 0;
  transform: translateY(-98%);
}

.footer-wave svg {
  position: relative;
  display: block;
  width: calc(100% + 1.3px);
  height: 60px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 30px 20px;
  position: relative;
  z-index: 1;
}

.footer-content {
  display: grid;
  grid-template-columns: 1fr 2fr 1.5fr;
  gap: 40px;
  margin-bottom: 40px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.logo-icon {
  font-size: 36px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.logo-text {
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #fff, #a8c0ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-desc {
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin-bottom: 20px;
  font-size: 14px;
  white-space: pre-line;
}

.social-links {
  display: flex;
  gap: 15px;
}

.social-link {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  text-decoration: none;
  transition: all 0.3s;
  font-size: 18px;
}

.social-link:hover {
  background: #667eea;
  transform: translateY(-3px);
}

.link-group h4 {
  font-size: 16px;
  margin-bottom: 15px;
  color: white;
  position: relative;
  padding-bottom: 8px;
}

.link-group h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.link-group ul {
  list-style: none;
  padding: 0;
}

.link-group li {
  margin-bottom: 10px;
}

.link-group a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.link-group a:hover {
  color: #667eea;
  padding-left: 5px;
}

.footer-contact h4 {
  font-size: 16px;
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 8px;
}

.footer-contact h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.contact-item i {
  font-size: 18px;
  color: #667eea;
  width: 24px;
}

.qrcode {
  margin-top: 20px;
}

.qrcode-placeholder {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.qrcode-placeholder i {
  font-size: 32px;
  color: #667eea;
}

.qrcode-placeholder span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.qrcode-placeholder:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.05);
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 20px;
  text-align: center;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.footer-bottom p {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  margin: 0;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .footer-links {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .footer-bottom {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .footer-links {
    grid-template-columns: 1fr;
  }

  .footer-container {
    padding: 40px 20px 15px;
  }
}
</style>