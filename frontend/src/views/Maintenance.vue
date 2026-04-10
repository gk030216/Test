<template>
  <div class="maintenance-container">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="particles">
        <span v-for="i in 20" :key="i" :style="getParticleStyle()"></span>
      </div>
    </div>

    <!-- 漂浮的宠物元素 -->
    <div class="floating-pets">
      <div class="pet pet-dog">🐕</div>
      <div class="pet pet-cat">🐈</div>
      <div class="pet pet-bunny">🐇</div>
      <div class="pet pet-bird">🐦</div>
      <div class="pet pet-fish">🐟</div>
      <div class="pet pet-paw">🐾</div>
    </div>

    <!-- 主内容区 -->
    <div class="maintenance-card">
      <div class="card-inner">
        <!-- 齿轮动画 -->
        <div class="gear-wrapper">
          <div class="gear gear-1">
            <i class="el-icon-setting"></i>
          </div>
          <div class="gear gear-2">
            <i class="el-icon-setting"></i>
          </div>
          <div class="gear gear-3">
            <i class="el-icon-setting"></i>
          </div>
        </div>

        <!-- 主图标 -->
        <div class="main-icon">
          <div class="icon-ring"></div>
          <div class="icon-ring ring-2"></div>
          <div class="icon-ring ring-3"></div>
          <span class="icon-emoji">🔧</span>
        </div>

        <!-- 标题 -->
        <h1 class="main-title">
          <span class="title-word">系</span>
          <span class="title-word">统</span>
          <span class="title-word">升</span>
          <span class="title-word">级</span>
          <span class="title-word">中</span>
        </h1>

        <p class="main-subtitle">汪汪队正在努力搬砖，马上就好！</p>

        <!-- 进度条动画 -->
        <div class="progress-wrapper">
          <div class="progress-bar">
            <div class="progress-fill"></div>
          </div>
          <div class="progress-pets">
            <span class="progress-pet" :class="{ active: progress >= 20 }">🐕</span>
            <span class="progress-pet" :class="{ active: progress >= 40 }">🐈</span>
            <span class="progress-pet" :class="{ active: progress >= 60 }">🐇</span>
            <span class="progress-pet" :class="{ active: progress >= 80 }">🐦</span>
            <span class="progress-pet" :class="{ active: progress >= 100 }">🎉</span>
          </div>
          <p class="progress-text">{{ progress }}%</p>
        </div>

        <!-- 信息卡片 -->
        <div class="info-cards">
          <div class="info-card">
            <i class="el-icon-time"></i>
            <span>预计 30 分钟</span>
          </div>
          <div class="info-card">
            <i class="el-icon-phone-outline"></i>
            <span>{{ servicePhone }}</span>
          </div>
        </div>

        <!-- 按钮组 -->
        <div class="action-buttons">
          <!-- 未登录：显示管理员登录 -->
          <el-button
              v-if="!isLoggedIn"
              type="primary"
              round
              @click="goToLogin"
              class="login-btn"
          >
            <i class="el-icon-user"></i> 管理员登录
          </el-button>

          <!-- 已登录且是管理员：显示进入后台 -->
          <el-button
              v-else-if="isAdmin"
              type="primary"
              round
              @click="goToAdmin"
              class="login-btn"
          >
            <i class="el-icon-s-platform"></i> 进入后台
          </el-button>

          <!-- 已登录但不是管理员：显示无权限提示 -->
          <el-button
              v-else
              type="info"
              round
              disabled
              class="login-btn"
          >
            <i class="el-icon-lock"></i> 暂无访问权限
          </el-button>

          <el-button round plain @click="refresh" class="refresh-btn">
            <i class="el-icon-refresh"></i> 刷新页面
          </el-button>
        </div>

        <!-- 底部提示 -->
        <p class="footer-tip" v-if="!isAdmin">
          <i class="el-icon-chat-dot-round"></i>
          {{ isLoggedIn ? '系统维护中，请稍后再试' : '铲屎官别着急，我们在给服务器做 Spa ~' }}
        </p>
        <p class="footer-tip" v-else>
          <i class="el-icon-chat-dot-round"></i>
          管理员，你可以正常访问后台
        </p>
      </div>
    </div>

    <!-- 角落装饰 -->
    <div class="corner-decoration corner-tl">
      <span>🐾</span><span>🐾</span>
    </div>
    <div class="corner-decoration corner-tr">
      <span>🐾</span><span>🐾</span>
    </div>
    <div class="corner-decoration corner-bl">
      <span>🐾</span><span>🐾</span>
    </div>
    <div class="corner-decoration corner-br">
      <span>🐾</span><span>🐾</span>
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
    getParticleStyle() {
      const size = Math.random() * 8 + 3;
      const left = Math.random() * 100;
      const top = Math.random() * 100;
      const duration = Math.random() * 10 + 10;
      const delay = Math.random() * 5;
      return {
        width: size + 'px',
        height: size + 'px',
        left: left + '%',
        top: top + '%',
        animationDuration: duration + 's',
        animationDelay: delay + 's'
      };
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
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
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
  opacity: 0.5;
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

/* 粒子效果 */
.particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particles span {
  position: absolute;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  animation: particleFloat 10s linear infinite;
}

@keyframes particleFloat {
  0% { transform: translateY(100vh); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translateY(-100px); opacity: 0; }
}

/* 漂浮的宠物 */
.floating-pets {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.pet {
  position: absolute;
  font-size: 40px;
  opacity: 0.3;
  animation: petFloat 8s ease-in-out infinite;
}

.pet-dog { top: 15%; left: 10%; animation-delay: 0s; }
.pet-cat { top: 70%; left: 15%; animation-delay: -2s; font-size: 50px; }
.pet-bunny { top: 20%; right: 15%; animation-delay: -4s; font-size: 35px; }
.pet-bird { top: 60%; right: 10%; animation-delay: -6s; font-size: 45px; }
.pet-fish { bottom: 20%; left: 20%; animation-delay: -3s; }
.pet-paw { bottom: 15%; right: 25%; animation-delay: -5s; font-size: 50px; }

@keyframes petFloat {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
}

/* 主卡片 */
.maintenance-card {
  position: relative;
  z-index: 10;
  width: 550px;
  max-width: 90%;
  perspective: 1000px;
}

.card-inner {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 40px;
  padding: 45px 40px;
  text-align: center;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: cardAppear 0.8s ease-out;
}

@keyframes cardAppear {
  0% { opacity: 0; transform: rotateX(-15deg) translateY(30px); }
  100% { opacity: 1; transform: rotateX(0) translateY(0); }
}

/* 齿轮动画 */
.gear-wrapper {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 20px;
}

.gear {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  animation: spin 4s linear infinite;
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.gear-1 { animation-duration: 3s; }
.gear-2 { animation-duration: 4s; animation-direction: reverse; }
.gear-3 { animation-duration: 5s; }

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 主图标 */
.main-icon {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 20px auto 30px;
}

.icon-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 3px solid rgba(102, 126, 234, 0.3);
  border-radius: 50%;
  animation: pulse 2s ease-out infinite;
}

.ring-2 {
  animation-delay: 0.5s;
  border-color: rgba(240, 147, 251, 0.3);
}

.ring-3 {
  animation-delay: 1s;
  border-color: rgba(79, 172, 254, 0.3);
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

.icon-emoji {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 60px;
  animation: wobble 3s ease-in-out infinite;
}

@keyframes wobble {
  0%, 100% { transform: translate(-50%, -50%) rotate(0deg); }
  25% { transform: translate(-50%, -50%) rotate(-15deg); }
  75% { transform: translate(-50%, -50%) rotate(15deg); }
}

/* 标题 */
.main-title {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 15px;
}

.title-word {
  font-size: 36px;
  font-weight: 700;
  color: white;
  text-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  animation: titleBounce 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
}

.title-word:nth-child(1) { animation-delay: 0.1s; }
.title-word:nth-child(2) { animation-delay: 0.2s; }
.title-word:nth-child(3) { animation-delay: 0.3s; }
.title-word:nth-child(4) { animation-delay: 0.4s; }
.title-word:nth-child(5) { animation-delay: 0.5s; }

@keyframes titleBounce {
  0% { opacity: 0; transform: translateY(20px); }
  60% { opacity: 1; transform: translateY(-5px); }
  100% { opacity: 1; transform: translateY(0); }
}

.main-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 35px;
  animation: fadeIn 1s ease-out 0.8s both;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 进度条 */
.progress-wrapper {
  margin-bottom: 30px;
}

.progress-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 15px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
  border-radius: 20px;
  width: 0%;
  animation: progressFill 20s ease-out forwards;
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.6);
}

@keyframes progressFill {
  0% { width: 0%; }
  100% { width: 100%; }
}

.progress-pets {
  display: flex;
  justify-content: space-around;
  margin-bottom: 10px;
}

.progress-pet {
  font-size: 24px;
  opacity: 0.3;
  transition: all 0.3s;
}

.progress-pet.active {
  opacity: 1;
  transform: scale(1.2);
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
}

.progress-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  font-weight: 600;
}

/* 信息卡片 */
.info-cards {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 30px;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 40px;
  color: white;
  font-size: 14px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.info-card i {
  font-size: 18px;
  color: #f093fb;
}

/* 按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-bottom: 25px;
}

.login-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  padding: 12px 30px;
  font-size: 16px;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 25px rgba(102, 126, 234, 0.5);
}

.refresh-btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 12px 30px;
  font-size: 16px;
}

.refresh-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 底部提示 */
.footer-tip {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.footer-tip i {
  color: #f093fb;
}

/* 角落装饰 */
.corner-decoration {
  position: absolute;
  display: flex;
  gap: 5px;
  opacity: 0.4;
  z-index: 5;
}

.corner-decoration span {
  font-size: 30px;
  animation: pawBounce 2s ease-in-out infinite;
}

.corner-decoration span:nth-child(2) {
  animation-delay: 0.3s;
}

.corner-tl { top: 20px; left: 20px; }
.corner-tr { top: 20px; right: 20px; }
.corner-bl { bottom: 20px; left: 20px; }
.corner-br { bottom: 20px; right: 20px; }

@keyframes pawBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 响应式 */
@media (max-width: 768px) {
  .card-inner {
    padding: 35px 25px;
  }

  .main-title {
    gap: 5px;
  }

  .title-word {
    font-size: 28px;
  }

  .main-subtitle {
    font-size: 16px;
  }

  .info-cards {
    flex-direction: column;
    gap: 10px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .gear-wrapper {
    top: -15px;
  }

  .gear {
    width: 30px;
    height: 30px;
    font-size: 16px;
  }
}
</style>