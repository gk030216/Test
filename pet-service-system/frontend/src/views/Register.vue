<template>
  <div class="register-container">
    <!-- 背景动画 -->
    <div class="bg-animation">
      <div class="floating-paw" v-for="n in 12" :key="n">🐾</div>
    </div>

    <div class="register-card" data-aos="fade-up" data-aos-duration="800">
      <!-- 卡片头部 -->
      <div class="card-header">
        <div class="header-icon">
          <span class="paw-icon">🐾</span>
          <span class="plus-icon">+</span>
          <span class="paw-icon">🐾</span>
        </div>
        <h2 class="title">
          <span class="gradient-text">📝 创建新账号</span>
        </h2>
        <p class="subtitle">加入宠物大家庭，开启美好时光</p>
      </div>

      <!-- 步骤指示器 -->
      <div class="step-indicator">
        <div class="step-item" :class="{ active: step >= 1 }">
          <div class="step-circle">1</div>
          <span class="step-text">填写信息</span>
        </div>
        <div class="step-line" :class="{ active: step >= 2 }"></div>
        <div class="step-item" :class="{ active: step >= 2 }">
          <div class="step-circle">2</div>
          <span class="step-text">验证邮箱</span>
        </div>
        <div class="step-line" :class="{ active: step >= 3 }"></div>
        <div class="step-item" :class="{ active: step >= 3 }">
          <div class="step-circle">3</div>
          <span class="step-text">完成注册</span>
        </div>
      </div>

      <!-- 注册表单 -->
      <el-form :model="registerForm" :rules="rules" ref="registerForm" label-width="0" class="register-form">
        <!-- 用户名 -->
        <el-form-item prop="username">
          <div class="input-label">
            <i class="el-icon-user"></i>
            <span>用户名</span>
          </div>
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名（3-20个字符）"
              size="large"
              clearable
              @focus="handleFocus('username')"
              @blur="handleBlur"
          >
            <i slot="prefix" class="el-icon-user" :class="{ 'active-icon': focusedField === 'username' }"></i>
          </el-input>
          <transition name="fade">
            <div class="input-tip" v-if="!registerForm.username && focusedField !== 'username'">
              <i class="el-icon-info"></i> 用于登录的唯一标识
            </div>
          </transition>
        </el-form-item>

        <!-- 邮箱 + 验证码组合 -->
        <el-form-item prop="email">
          <div class="input-label">
            <i class="el-icon-message"></i>
            <span>邮箱地址</span>
          </div>
          <div class="email-wrapper">
            <el-input
                v-model="registerForm.email"
                placeholder="请输入常用邮箱"
                size="large"
                clearable
                class="email-input"
                @focus="handleFocus('email')"
                @blur="handleBlur"
            >
              <i slot="prefix" class="el-icon-message" :class="{ 'active-icon': focusedField === 'email' }"></i>
            </el-input>
            <el-button
                :disabled="!canSendCode"
                @click="sendCode"
                :class="['code-btn', { 'code-sending': codeSending }]"
                size="large"
            >
              {{ codeBtnText }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item prop="code">
          <div class="input-label">
            <i class="el-icon-key"></i>
            <span>验证码</span>
          </div>
          <el-input
              v-model="registerForm.code"
              placeholder="请输入6位验证码"
              size="large"
              maxlength="6"
              @focus="handleFocus('code')"
              @blur="handleBlur"
          >
            <i slot="prefix" class="el-icon-key" :class="{ 'active-icon': focusedField === 'code' }"></i>
          </el-input>
          <transition name="fade">
            <div class="input-tip" v-if="!registerForm.code && focusedField !== 'code'">
              <i class="el-icon-info"></i> 请查收邮件中的6位数字验证码
            </div>
          </transition>
        </el-form-item>

        <!-- 密码和确认密码（双列布局） -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item prop="password">
              <div class="input-label">
                <i class="el-icon-lock"></i>
                <span>密码</span>
              </div>
              <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="6-20位"
                  size="large"
                  show-password
                  @focus="handleFocus('password')"
                  @blur="handleBlur"
                  @input="checkPasswordStrength"
              >
                <i slot="prefix" class="el-icon-lock" :class="{ 'active-icon': focusedField === 'password' }"></i>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="confirmPassword">
              <div class="input-label">
                <i class="el-icon-circle-check"></i>
                <span>确认密码</span>
              </div>
              <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="再次输入"
                  size="large"
                  show-password
                  @focus="handleFocus('confirmPassword')"
                  @blur="handleBlur"
              >
                <i slot="prefix" class="el-icon-circle-check" :class="{ 'active-icon': focusedField === 'confirmPassword' }"></i>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 密码强度指示器 -->
        <div class="password-strength" v-if="registerForm.password">
          <div class="strength-label">
            <span>密码强度：</span>
            <span :class="'strength-' + passwordStrength">
              {{ strengthText }}
            </span>
          </div>
          <div class="strength-bar">
            <div
                class="strength-progress"
                :style="{ width: strengthWidth + '%' }"
                :class="'strength-' + passwordStrength"
            ></div>
          </div>
        </div>

        <!-- 手机号 -->
        <el-form-item prop="phone">
          <div class="input-label">
            <i class="el-icon-phone"></i>
            <span>手机号码</span>
          </div>
          <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号（选填）"
              size="large"
              clearable
              @focus="handleFocus('phone')"
              @blur="handleBlur"
          >
            <i slot="prefix" class="el-icon-phone" :class="{ 'active-icon': focusedField === 'phone' }"></i>
          </el-input>
        </el-form-item>

        <!-- 昵称 -->
        <el-form-item prop="nickname">
          <div class="input-label">
            <i class="el-icon-edit"></i>
            <span>昵称</span>
          </div>
          <el-input
              v-model="registerForm.nickname"
              placeholder="给自己起个好听的名字吧"
              size="large"
              clearable
              @focus="handleFocus('nickname')"
              @blur="handleBlur"
          >
            <i slot="prefix" class="el-icon-edit" :class="{ 'active-icon': focusedField === 'nickname' }"></i>
          </el-input>
        </el-form-item>

        <!-- 用户协议 -->
        <div class="agreement">
          <el-checkbox v-model="agreeProtocol">
            <span style="color: #666;">我已阅读并同意</span>
            <a href="#" target="_blank" class="protocol-link">《用户协议》</a>
            <span style="color: #666;">和</span>
            <a href="#" target="_blank" class="protocol-link">《隐私政策》</a>
          </el-checkbox>
        </div>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              @click="handleRegister"
              :loading="loading"
              class="register-button"
              :disabled="!agreeProtocol || !isFormValid"
          >
            <span v-if="!loading">📝 立即注册</span>
            <span v-else>注册中...</span>
          </el-button>
        </el-form-item>

        <!-- 登录链接 -->
        <div class="form-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">
            立即登录 <i class="el-icon-arrow-right"></i>
          </router-link>
        </div>

        <!-- 社交注册 -->
        <div class="social-register">
          <div class="divider">
            <span class="divider-text">其他方式注册</span>
          </div>
          <div class="social-icons">
            <div class="social-icon wechat" @click="socialRegister('wechat')">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <div class="social-icon qq" @click="socialRegister('qq')">
              <i class="el-icon-qq"></i>
            </div>
            <div class="social-icon weibo" @click="socialRegister('weibo')">
              <i class="el-icon-microphone"></i>
            </div>
          </div>
        </div>
      </el-form>

      <!-- 底部装饰 -->
      <div class="card-footer"></div>
    </div>
  </div>
</template>

<script>
import { register, checkUsername, checkEmail, sendRegisterCode } from '@/api/user';

export default {
  name: 'Register',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入用户名'));
        return;
      }
      checkUsername(value).then(res => {
        if (res.data) {
          callback(new Error('❌ 用户名已存在'));
        } else {
          callback();
        }
      }).catch(() => {
        callback();
      });
    };

    const validateEmail = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入邮箱'));
        return;
      }
      checkEmail(value).then(res => {
        if (res.data) {
          callback(new Error('❌ 邮箱已被注册'));
        } else {
          callback();
        }
      }).catch(() => {
        callback();
      });
    };

    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.registerForm.password) {
        callback(new Error('❌ 两次输入密码不一致'));
      } else {
        callback();
      }
    };

    const validatePhone = (rule, value, callback) => {
      if (!value) {
        callback();
        return;
      }
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (!phoneRegex.test(value)) {
        callback(new Error('❌ 手机号格式不正确'));
      } else {
        callback();
      }
    };

    return {
      step: 1,
      registerForm: {
        username: '',
        email: '',
        code: '',
        password: '',
        confirmPassword: '',
        phone: '',
        nickname: ''
      },
      agreeProtocol: true,
      focusedField: '',
      passwordStrength: 'none',
      loading: false,
      codeSending: false,
      countdown: 60,
      timer: null,
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在3-20个字符', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' },
          { validator: validateUsername, trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
          { validator: validateEmail, trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码为6位数字', trigger: 'blur' },
          { pattern: /^\d{6}$/, message: '验证码必须是数字', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        phone: [
          { validator: validatePhone, trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    // 修改 canSendCode：确保倒计时期间不可点击
    canSendCode() {
      return this.registerForm.email &&
          !this.codeSending &&
          this.countdown === 60 && // 关键：倒计时必须等于60才能发送
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.registerForm.email);
    },
    // 修改按钮文字显示：倒计时优先
    codeBtnText() {
      if (this.countdown < 60) {
        return `${this.countdown}秒后重试`;
      }
      if (this.codeSending) {
        return '发送中...';
      }
      return '获取验证码';
    },
    isFormValid() {
      return this.registerForm.username &&
          this.registerForm.email &&
          this.registerForm.code &&
          this.registerForm.password &&
          this.registerForm.confirmPassword;
    },
    strengthText() {
      const map = {
        none: '未设置',
        weak: '弱',
        medium: '中',
        strong: '强'
      };
      return map[this.passwordStrength];
    },
    strengthWidth() {
      const map = {
        none: 0,
        weak: 33,
        medium: 66,
        strong: 100
      };
      return map[this.passwordStrength];
    }
  },
  methods: {
    handleFocus(field) {
      this.focusedField = field;
    },
    handleBlur() {
      this.focusedField = '';
    },
    checkPasswordStrength() {
      const pwd = this.registerForm.password;
      if (!pwd) {
        this.passwordStrength = 'none';
        return;
      }

      let score = 0;
      if (pwd.length >= 8) score++;
      if (pwd.length >= 12) score++;
      if (/\d/.test(pwd)) score++;
      if (/[a-z]/.test(pwd)) score++;
      if (/[A-Z]/.test(pwd)) score++;
      if (/[^a-zA-Z0-9]/.test(pwd)) score++;

      if (score < 3) this.passwordStrength = 'weak';
      else if (score < 5) this.passwordStrength = 'medium';
      else this.passwordStrength = 'strong';
    },
    // 修改 sendCode 方法
    sendCode() {
      this.$refs.registerForm.validateField('email', errorMsg => {
        if (errorMsg) {
          this.$message.warning('请先填写正确的邮箱地址');
          return;
        }

        this.codeSending = true;
        sendRegisterCode(this.registerForm.email)
            .then(() => {
              this.$message.success('📧 验证码已发送，请查收邮件');
              this.startCountdown(); // 启动倒计时
            })
            .catch(() => {
              this.codeSending = false;
              this.countdown = 60; // 重置倒计时
              this.$message.error('验证码发送失败');
            });
      });
    },
    // 修改 startCountdown 方法
    startCountdown() {
      this.countdown = 60; // 设置为60，触发倒计时显示
      this.codeSending = true; // 保持发送中状态用于禁用按钮

      if (this.timer) {
        clearInterval(this.timer);
      }

      this.timer = setInterval(() => {
        this.countdown -= 1;
        if (this.countdown <= 0) {
          clearInterval(this.timer);
          this.timer = null;
          this.countdown = 60;
          this.codeSending = false; // 允许重新发送
        }
      }, 1000);
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (!valid) {
          this.$message.warning('请正确填写所有必填项');
          return;
        }

        this.loading = true;
        const { confirmPassword, ...registerData } = this.registerForm;

        register(registerData)
            .then(res => {
              this.loading = false;
              if (res.code === 200) {
                this.step = 3;
                this.$alert('注册成功！欢迎加入宠物大家庭', '🎉 恭喜', {
                  confirmButtonText: '去登录',
                  type: 'success',
                  center: true,
                  roundButton: true,
                  callback: () => {
                    this.$router.push('/login');
                  }
                });
              } else {
                this.$message.error(res.message || '注册失败');
              }
            })
            .catch(() => {
              this.loading = false;
              this.$message.error('注册失败，请稍后重试');
            });
      });
    },
    socialRegister(platform) {
      this.$message.info(`${platform} 注册功能开发中...`);
    }
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }
};
</script>

<style scoped>
/* 主容器 */
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 背景动画 */
.bg-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.floating-paw {
  position: absolute;
  font-size: 40px;
  opacity: 0.1;
  animation: float 20s infinite linear;
}

.floating-paw:nth-child(1) { top: 10%; left: 5%; animation-duration: 25s; }
.floating-paw:nth-child(2) { top: 20%; right: 10%; animation-duration: 30s; font-size: 60px; }
.floating-paw:nth-child(3) { bottom: 30%; left: 15%; animation-duration: 22s; }
.floating-paw:nth-child(4) { bottom: 10%; right: 20%; animation-duration: 35s; font-size: 50px; }
.floating-paw:nth-child(5) { top: 50%; left: 30%; animation-duration: 28s; }
.floating-paw:nth-child(6) { top: 70%; right: 30%; animation-duration: 24s; font-size: 55px; }
.floating-paw:nth-child(7) { top: 40%; left: 60%; animation-duration: 32s; }
.floating-paw:nth-child(8) { bottom: 20%; left: 50%; animation-duration: 26s; }
.floating-paw:nth-child(9) { top: 15%; left: 80%; animation-duration: 29s; font-size: 45px; }
.floating-paw:nth-child(10) { top: 85%; left: 10%; animation-duration: 33s; font-size: 35px; }
.floating-paw:nth-child(11) { top: 45%; left: 85%; animation-duration: 27s; font-size: 48px; }
.floating-paw:nth-child(12) { bottom: 5%; right: 5%; animation-duration: 31s; font-size: 42px; }

@keyframes float {
  0% { transform: rotate(0deg) translateY(0); }
  50% { transform: rotate(180deg) translateY(-20px); }
  100% { transform: rotate(360deg) translateY(0); }
}

/* 注册卡片 */
.register-card {
  width: 700px;
  max-width: 95%;
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
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 15px;
}

.paw-icon {
  font-size: 32px;
  animation: bounce 2s infinite;
}

.plus-icon {
  font-size: 24px;
  color: #667eea;
  font-weight: 600;
}

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

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 40px 30px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f0f0;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  transition: all 0.3s;
}

.step-item.active .step-circle {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transform: scale(1.1);
}

.step-text {
  font-size: 12px;
  color: #999;
  transition: color 0.3s;
}

.step-item.active .step-text {
  color: #667eea;
  font-weight: 500;
}

.step-line {
  width: 60px;
  height: 2px;
  background: #f0f0f0;
  margin: 0 10px;
  transition: background 0.3s;
}

.step-line.active {
  background: linear-gradient(90deg, #667eea, #764ba2);
}

/* 注册表单 */
.register-form {
  padding: 0 40px 30px;
}

/* 输入标签 */
.input-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.input-label i {
  font-size: 16px;
  color: #b3b3b3;
  transition: color 0.3s;
}

.input-label i.active-icon {
  color: #667eea;
}

/* 自定义输入框样式 */
::v-deep .el-input__inner {
  height: 48px;
  line-height: 48px;
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
  top: 0px;
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

/* 邮箱输入组合 */
.email-wrapper {
  display: flex;
  gap: 12px;
}

.email-input {
  flex: 1;
}

.code-btn {
  min-width: 120px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  padding: 0 15px;
}

.code-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.code-btn:active {
  transform: translateY(0);
}

.code-btn:disabled {
  opacity: 0.5;
  filter: grayscale(50%);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.code-sending {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

/* 密码强度指示器 */
.password-strength {
  margin: -10px 0 20px;
  padding: 0 12px;
}

.strength-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.strength-label span:last-child {
  font-weight: 600;
}

.strength-weak {
  color: #f56c6c;
}

.strength-medium {
  color: #e6a23c;
}

.strength-strong {
  color: #67c23a;
}

.strength-bar {
  height: 4px;
  background: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.strength-progress {
  height: 100%;
  transition: width 0.3s ease, background 0.3s ease;
}

.strength-progress.strength-weak {
  background: linear-gradient(90deg, #f56c6c, #f78989);
}

.strength-progress.strength-medium {
  background: linear-gradient(90deg, #e6a23c, #f0b45c);
}

.strength-progress.strength-strong {
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

/* 用户协议 */
.agreement {
  margin: 20px 0 25px;
  text-align: center;
}

::v-deep .el-checkbox__label {
  color: #666;
  font-size: 14px;
}

.protocol-link {
  color: #667eea;
  text-decoration: none;
  transition: color 0.3s;
  margin: 0 3px;
}

.protocol-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 注册按钮 */
.register-button {
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

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(102, 126, 234, 0.4);
}

.register-button:active {
  transform: translateY(0);
}

.register-button:disabled {
  opacity: 0.6;
  filter: grayscale(30%);
  transform: none;
  box-shadow: none;
}

/* 表单底部 */
.form-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.login-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
  transition: color 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.login-link:hover {
  color: #764ba2;
}

.login-link i {
  font-size: 12px;
  transition: transform 0.3s;
}

.login-link:hover i {
  transform: translateX(3px);
}

/* 社交注册 */
.social-register {
  margin-top: 30px;
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

/* 卡片底部装饰 */
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

/* 响应式设计 */
@media (max-width: 768px) {
  .register-card {
    padding: 0;
  }

  .card-header {
    padding: 30px 20px 10px;
  }

  .title {
    font-size: 24px;
  }

  .step-indicator {
    padding: 10px 20px 20px;
  }

  .step-line {
    width: 30px;
  }

  .step-circle {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }

  .step-text {
    font-size: 10px;
  }

  .register-form {
    padding: 0 20px 20px;
  }

  .email-wrapper {
    flex-direction: column;
  }

  .code-btn {
    width: 100%;
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

/* 自定义消息框样式 */
::v-deep .el-message-box {
  border-radius: 20px;
  padding: 20px;
}

::v-deep .el-message-box__title {
  color: #667eea;
  font-size: 20px;
}

::v-deep .el-message-box__message {
  color: #666;
  font-size: 16px;
  padding: 15px 0;
}

::v-deep .el-message-box__btns {
  padding-top: 15px;
}

::v-deep .el-button--success {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 25px;
  padding: 12px 30px;
  font-size: 16px;
}
</style>