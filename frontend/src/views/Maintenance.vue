<template>
  <div class="maintenance-container">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 主内容区 -->
    <div class="maintenance-card">
      <div class="card-inner">
        <!-- 主图标 -->
        <div class="main-icon">
          <div class="icon-ring"></div>
          <div class="icon-ring ring-2"></div>
          <div class="icon-ring ring-3"></div>
          <i class="el-icon-setting icon-emoji"></i>
        </div>

        <!-- 标题 -->
        <h1 class="main-title">系统维护中</h1>

        <p class="main-subtitle">为了给您提供更好的服务，系统正在进行升级维护</p>

        <!-- 进度条 -->
        <div class="progress-wrapper">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
          <p class="progress-text">{{ progress }}%</p>
        </div>

        <!-- 信息卡片 -->
        <div class="info-cards">
          <div class="info-card">
            <i class="el-icon-time"></i>
            <span>预计维护时间：30分钟</span>
          </div>
          <div class="info-card">
            <i class="el-icon-phone-outline"></i>
            <span>客服热线：{{ servicePhone }}</span>
          </div>
        </div>

        <!-- 按钮组 -->
        <div class="action-buttons">
          <!-- 未登录：显示管理员登录 -->
          <el-button
              v-if="!isLoggedIn"
              type="primary"
              @click="goToLogin"
              class="login-btn"
          >
            <i class="el-icon-user"></i> 管理员登录
          </el-button>

          <!-- 已登录且是管理员：显示进入后台 -->
          <el-button
              v-else-if="isAdmin"
              type="primary"
              @click="goToAdmin"
              class="login-btn"
          >
            <i class="el-icon-s-platform"></i> 进入后台
          </el-button>

          <!-- 已登录但不是管理员：显示无权限提示 -->
          <el-button
              v-else
              type="info"
              disabled
              class="login-btn"
          >
            <i class="el-icon-lock"></i> 暂无访问权限
          </el-button>

          <el-button plain @click="refresh" class="refresh-btn">
            <i class="el-icon-refresh"></i> 刷新页面
          </el-button>
        </div>

        <!-- 底部提示 -->
        <p class="footer-tip" v-if="!isAdmin">
          <i class="el-icon-info"></i>
          {{ isLoggedIn ? '系统维护中，请稍后再试' : '系统升级中，请稍后访问' }}
        </p>
        <p class="footer-tip" v-else>
          <i class="el-icon-info"></i>
          管理员，您可正常访问后台
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Maintenance',
  data() {
    return {
      servicePhone: '400-888-6666',
      progress: 0,
      progressTimer: null
    };
  },
  computed: {
    isLoggedIn() {
      return !!localStorage.getItem('token');
    },
    isAdmin() {
      const userInfo = localStorage.getItem('userInfo');
      if (!userInfo) return false;
      try {
        return JSON.parse(userInfo).role === 3;
      } catch (e) {
        return false;
      }
    }
  },
  mounted() {
    this.loadSettings();
    this.startProgress();
  },
  beforeDestroy() {
    if (this.progressTimer) {
      clearInterval(this.progressTimer);
    }
  },
  methods: {
    loadSettings() {
      const settings = localStorage.getItem('systemSettings');
      if (settings) {
        try {
          const basic = JSON.parse(settings).basic;
          this.servicePhone = basic.servicePhone || '400-888-6666';
        } catch (e) {
          console.error('解析系统设置失败', e);
        }
      }
    },
    startProgress() {
      this.progressTimer = setInterval(() => {
        if (this.progress < 100) {
          this.progress += Math.floor(Math.random() * 5) + 1;
          if (this.progress > 100) {
            this.progress = 100;
            clearInterval(this.progressTimer);
          }
        }
      }, 800);
    },
    refresh() {
      window.location.reload();
    },
    goToLogin() {
      this.$router.push('/login');
    },
    goToAdmin() {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        const role = JSON.parse(userInfo).role;
        if (role === 3) {
          this.$router.push('/admin');
        } else if (role === 2) {
          this.$router.push('/staff');
        } else {
          this.$router.push('/');
        }
      } else {
        this.$router.push('/');
      }
    }
  }
};
</script>

<style scoped>
.maintenance-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 动态渐变背景 */
.animated-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: floatOrb 15s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #667eea, #764ba2);
  top: -100px;
  left: -100px;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #f093fb, #f5576c);
  bottom: -150px;
  right: -100px;
  animation-delay: -5s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #4facfe, #00f2fe);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -10s;
}

@keyframes floatOrb {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(50px, -50px) scale(1.1); }
  66% { transform: translate(-50px, 50px) scale(0.9); }
}

/* 主卡片 */
.maintenance-card {
  position: relative;
  z-index: 10;
  width: 520px;
  max-width: 90%;
}

.card-inner {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 40px 32px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  animation: cardAppear 0.5s ease-out;
}

@keyframes cardAppear {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 主图标 */
.main-icon {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
}

.icon-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 50%;
  animation: pulse 2s ease-out infinite;
}

.ring-2 {
  animation-delay: 0.5s;
  border-color: rgba(102, 126, 234, 0.15);
}

.ring-3 {
  animation-delay: 1s;
  border-color: rgba(102, 126, 234, 0.1);
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.3); opacity: 0; }
}

.icon-emoji {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 48px;
  color: #667eea;
}

/* 标题 */
.main-title {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.main-subtitle {
  font-size: 14px;
  color: #909399;
  margin-bottom: 30px;
  line-height: 1.6;
}

/* 进度条 */
.progress-wrapper {
  margin-bottom: 30px;
}

.progress-bar {
  height: 6px;
  background: #e8eaef;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
}

/* 信息卡片 */
.info-cards {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f5f7fa;
  border-radius: 20px;
  color: #606266;
  font-size: 13px;
}

.info-card i {
  font-size: 16px;
  color: #667eea;
}

/* 按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.login-btn {
  background: #409EFF;
  border: none;
  color: white;
  padding: 10px 24px;
  font-size: 14px;
  border-radius: 8px;
  transition: all 0.3s;
}

.login-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

.login-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}

.refresh-btn {
  background: white;
  border: 1px solid #dcdfe6;
  color: #606266;
  padding: 10px 24px;
  font-size: 14px;
  border-radius: 8px;
  transition: all 0.3s;
}

.refresh-btn:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
}

/* 底部提示 */
.footer-tip {
  color: #909399;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
}

.footer-tip i {
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .card-inner {
    padding: 30px 20px;
  }

  .main-title {
    font-size: 24px;
  }

  .main-subtitle {
    font-size: 13px;
  }

  .info-cards {
    flex-direction: column;
    align-items: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .login-btn,
  .refresh-btn {
    width: 100%;
  }
}
</style>