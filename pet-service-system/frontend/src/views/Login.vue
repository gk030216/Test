<template>
  <div class="login-container">
    <!-- 背景动画 -->
    <div class="bg-animation">
      <div class="pet-paw" v-for="n in 8" :key="n">🐾</div>
    </div>

    <div class="login-card" data-aos="fade-up" data-aos-duration="800">
      <!-- 卡片顶部装饰 -->
      <div class="card-header">
        <div class="paw-print">
          <span>🐱</span>
          <span>🐶</span>
        </div>
        <h2 class="title">
          <span class="gradient-text">宠物服务管理系统</span>
        </h2>
        <p class="subtitle">用心服务每一个宠物家庭</p>
      </div>

      <!-- 登录表单 -->
      <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="0" class="login-form">
        <!-- 账号输入框 -->
        <el-form-item prop="account">
          <div class="input-wrapper">
            <el-input
                v-model="loginForm.account"
                placeholder="用户名 / 邮箱"
                size="large"
                clearable
                @focus="handleFocus"
                @blur="handleBlur"
            >
              <template slot="prefix">
                <i class="el-icon-user" :class="{ 'active-icon': focusedField === 'account' }"></i>
              </template>
            </el-input>
            <transition name="fade">
              <div class="input-tip" v-if="!loginForm.account && focusedField !== 'account'">
                <i class="el-icon-info"></i> 请输入你的账号
              </div>
            </transition>
          </div>
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <div class="input-wrapper">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                size="large"
                show-password
                clearable
                @focus="handleFocus"
                @blur="handleBlur"
            >
              <template slot="prefix">
                <i class="el-icon-lock" :class="{ 'active-icon': focusedField === 'password' }"></i>
              </template>
            </el-input>
            <transition name="fade">
              <div class="input-tip" v-if="!loginForm.password && focusedField !== 'password'">
                <i class="el-icon-info"></i> 密码长度至少6位
              </div>
            </transition>
          </div>
        </el-form-item>

        <!-- 登录选项 -->
        <div class="login-options">
          <el-checkbox v-model="rememberPassword" class="remember-checkbox">
            <span style="color: #666;">记住密码</span>
          </el-checkbox>
          <router-link to="/forget-password" class="forget-link">
            忘记密码？<i class="el-icon-arrow-right"></i>
          </router-link>
        </div>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              @click="handleLogin"
              :loading="loading"
              class="login-button"
              :disabled="!isFormValid"
          >
            <span v-if="!loading">🔑 安全登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>

        <!-- 其他登录方式 -->
        <div class="other-login">
          <div class="divider">
            <span class="divider-text">其他方式登录</span>
          </div>
          <div class="social-icons">
            <div class="social-icon wechat" @click="socialLogin('wechat')">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <div class="social-icon qq" @click="socialLogin('qq')">
              <i class="el-icon-qq"></i>
            </div>
            <div class="social-icon weibo" @click="socialLogin('weibo')">
              <i class="el-icon-microphone"></i>
            </div>
          </div>
        </div>

        <!-- 注册链接 -->
        <div class="register-link">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-btn">
            立即注册 <i class="el-icon-arrow-right"></i>
          </router-link>
        </div>
      </el-form>

      <!-- 底部装饰 -->
      <div class="card-footer">
        <div class="wave"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/user';

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        account: '',
        password: ''
      },
      rememberPassword: false,
      focusedField: '',
      loading: false,
      rules: {
        account: [
          { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在3-20个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    isFormValid() {
      return this.loginForm.account && this.loginForm.password.length >= 6;
    }
  },
  mounted() {
    this.loadSavedAccount();
  },
  methods: {
    handleFocus(event) {
      this.focusedField = event.target.placeholder.includes('用户名') ? 'account' : 'password';
    },
    handleBlur() {
      this.focusedField = '';
    },
    loadSavedAccount() {
      const saved = localStorage.getItem('savedAccount');
      if (saved) {
        this.loginForm.account = saved;
        this.rememberPassword = true;
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) {
          this.showValidationError();
          return;
        }

        this.loading = true;
        login(this.loginForm)
            .then(res => {
              this.loading = false;
              if (res.code === 200) {
                this.loginSuccess(res.data);
              } else {
                this.loginFailed(res.message || '登录失败');
              }
            })
            .catch(error => {
              this.loading = false;
              this.loginFailed(error.message || '网络错误');
            });
      });
    },
    loginSuccess(userData) {
      localStorage.setItem('userInfo', JSON.stringify(userData));
      if (this.rememberPassword) {
        localStorage.setItem('savedAccount', this.loginForm.account);
      } else {
        localStorage.removeItem('savedAccount');
      }

      this.$notify({
        title: '欢迎回来',
        message: `亲爱的用户，${userData.nickname || userData.username} 欢迎登录！`,
        type: 'success',
        duration: 3000,
        customClass: 'welcome-notification'
      });

      setTimeout(() => {
        this.$router.push('/');
      }, 1500);
    },
    loginFailed(message) {
      this.$alert(message, '登录失败', {
        confirmButtonText: '重新输入',
        type: 'error',
        center: true,
        roundButton: true
      });

      // 输入框抖动效果
      const inputs = document.querySelectorAll('.el-input__inner');
      inputs.forEach(input => {
        input.classList.add('shake-animation');
        setTimeout(() => {
          input.classList.remove('shake-animation');
        }, 500);
      });
    },
    showValidationError() {
      this.$message({
        message: '请正确填写登录信息',
        type: 'warning',
        duration: 2000,
        showClose: true
      });
    },
    socialLogin(platform) {
      this.$message.info(`${platform} 登录功能开发中...`);
    }
  }
};
</script>

<style scoped>
/* 主容器 */
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(145deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 背景动画 */
.bg-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.pet-paw {
  position: absolute;
  font-size: 40px;
  opacity: 0.1;
  animation: float 20s infinite linear;
}

.pet-paw:nth-child(1) { top: 10%; left: 5%; animation-duration: 25s; }
.pet-paw:nth-child(2) { top: 20%; right: 10%; animation-duration: 30s; font-size: 60px; }
.pet-paw:nth-child(3) { bottom: 30%; left: 15%; animation-duration: 22s; }
.pet-paw:nth-child(4) { bottom: 10%; right: 20%; animation-duration: 35s; font-size: 50px; }
.pet-paw:nth-child(5) { top: 50%; left: 30%; animation-duration: 28s; }
.pet-paw:nth-child(6) { top: 70%; right: 30%; animation-duration: 24s; font-size: 55px; }
.pet-paw:nth-child(7) { top: 40%; left: 60%; animation-duration: 32s; }
.pet-paw:nth-child(8) { bottom: 20%; left: 50%; animation-duration: 26s; }

@keyframes float {
  0% { transform: rotate(0deg) translateY(0); }
  50% { transform: rotate(180deg) translateY(-20px); }
  100% { transform: rotate(360deg) translateY(0); }
}

/* 登录卡片 */
.login-card {
  width: 460px;
  max-width: 90%;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-radius: 40px;
  box-shadow: 0 30px 70px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 10;
  overflow: hidden;
  animation: slideUp 0.8s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片头部 */
.card-header {
  padding: 40px 40px 20px;
  text-align: center;
  position: relative;
}

.paw-print {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 15px;
}

.paw-print span {
  font-size: 32px;
  animation: bounce 2s infinite;
}

.paw-print span:nth-child(1) { animation-delay: 0s; }
.paw-print span:nth-child(2) { animation-delay: 0.5s; }

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.title {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 600;
}

.gradient-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: #999;
  font-size: 14px;
  margin: 0;
  letter-spacing: 1px;
}

/* 登录表单 */
.login-form {
  padding: 0 40px 30px;
}

/* 输入框容器 */
.input-wrapper {
  position: relative;
  margin-bottom: 5px;
}

/* 自定义输入框样式 */
::v-deep .el-input__inner {
  height: 54px;
  line-height: 54px;
  border: 2px solid #f0f0f0;
  border-radius: 16px;
  font-size: 15px;
  padding-left: 45px;
  transition: all 0.3s ease;
  background: #fafbfc;
}

::v-deep .el-input__inner:hover {
  border-color: #b3c0ff;
  background: #ffffff;
}

::v-deep .el-input__inner:focus {
  border-color: #667eea;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

::v-deep .el-input__prefix {
  left: 15px;
  top: 1px;
}

::v-deep .el-input__prefix i {
  font-size: 20px;
  color: #b3b3b3;
  transition: all 0.3s;
}

::v-deep .el-input__prefix i.active-icon {
  color: #667eea;
  transform: scale(1.1);
}

/* 输入提示 */
.input-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
  padding-left: 12px;
}

.input-tip i {
  margin-right: 4px;
  font-size: 12px;
}

/* 淡入淡出动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}

/* 登录选项 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 15px 0 25px;
}

::v-deep .remember-checkbox .el-checkbox__inner {
  border-radius: 4px;
  border-color: #d9d9d9;
}

::v-deep .remember-checkbox .el-checkbox__inner:hover {
  border-color: #667eea;
}

::v-deep .remember-checkbox.is-checked .el-checkbox__inner {
  background-color: #667eea;
  border-color: #667eea;
}

.forget-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.forget-link:hover {
  color: #764ba2;
}

.forget-link i {
  font-size: 12px;
  transition: transform 0.3s;
}

.forget-link:hover i {
  transform: translateX(3px);
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 54px;
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 27px;
  transition: all 0.3s ease;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(102, 126, 234, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.6;
  filter: grayscale(30%);
  transform: none;
  box-shadow: none;
}

/* 其他登录方式 */
.other-login {
  margin: 30px 0 20px;
}

.divider {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #ddd, transparent);
}

.divider-text {
  position: relative;
  background: white;
  padding: 0 15px;
  color: #999;
  font-size: 13px;
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.social-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 24px;
  color: #fff;
}

.social-icon.wechat {
  background: linear-gradient(135deg, #09b83e 0%, #2ecc71 100%);
  box-shadow: 0 5px 15px rgba(9, 184, 62, 0.3);
}

.social-icon.qq {
  background: linear-gradient(135deg, #12b7f5 0%, #1da1f2 100%);
  box-shadow: 0 5px 15px rgba(18, 183, 245, 0.3);
}

.social-icon.weibo {
  background: linear-gradient(135deg, #e4405f 0%, #f15b6c 100%);
  box-shadow: 0 5px 15px rgba(228, 64, 95, 0.3);
}

.social-icon:hover {
  transform: translateY(-3px) scale(1.1);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

/* 注册链接 */
.register-link {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.register-btn {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
  transition: color 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.register-btn:hover {
  color: #764ba2;
}

.register-btn i {
  font-size: 12px;
  transition: transform 0.3s;
}

.register-btn:hover i {
  transform: translateX(3px);
}

/* 卡片底部 */
.card-footer {
  height: 8px;
  background: linear-gradient(90deg, #667eea, #764ba2, #667eea);
  background-size: 200% 100%;
  animation: gradientMove 3s infinite;
}

@keyframes gradientMove {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 抖动动画 */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

.shake-animation {
  animation: shake 0.5s ease-in-out;
  border-color: #f56c6c !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    width: 95%;
    margin: 20px;
  }

  .card-header {
    padding: 30px 20px 15px;
  }

  .title {
    font-size: 24px;
  }

  .login-form {
    padding: 0 20px 20px;
  }

  .social-icons {
    gap: 15px;
  }

  .social-icon {
    width: 42px;
    height: 42px;
    font-size: 20px;
  }
}

/* 自定义通知样式 */
::v-deep .welcome-notification {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  border: none !important;
  border-radius: 12px !important;
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4) !important;
}

::v-deep .welcome-notification .el-notification__title {
  color: white !important;
}

::v-deep .welcome-notification .el-notification__content {
  color: rgba(255, 255, 255, 0.9) !important;
}
</style>