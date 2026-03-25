<template>
  <div class="forget-container">
    <!-- 背景动画 -->
    <div class="bg-animation">
      <div class="floating-paw" v-for="n in 8" :key="n">🐾</div>
    </div>

    <div class="forget-card" data-aos="fade-up" data-aos-duration="800">
      <!-- 卡片头部 -->
      <div class="card-header">
        <div class="header-icon">
          <span class="lock-wrapper">
            <i class="el-icon-lock"></i>
          </span>
        </div>
        <h2 class="title">
          <span class="gradient-text">找回密码</span>
        </h2>
        <p class="subtitle">别担心，我们帮你重新设置密码</p>
      </div>

      <!-- 自定义步骤条 -->
      <div class="steps-container">
        <div class="step-item" :class="{ active: step >= 1, completed: step > 1 }">
          <div class="step-marker">
            <span v-if="step <= 1">1</span>
            <i v-else class="el-icon-check"></i>
          </div>
          <span class="step-label">验证身份</span>
        </div>
        <div class="step-connector" :class="{ active: step >= 2 }"></div>
        <div class="step-item" :class="{ active: step >= 2, completed: step > 2 }">
          <div class="step-marker">
            <span v-if="step <= 2">2</span>
            <i v-else class="el-icon-check"></i>
          </div>
          <span class="step-label">重置密码</span>
        </div>
        <div class="step-connector" :class="{ active: step >= 3 }"></div>
        <div class="step-item" :class="{ active: step >= 3 }">
          <div class="step-marker">
            <span>3</span>
          </div>
          <span class="step-label">完成</span>
        </div>
      </div>

      <!-- 第一步：验证身份 -->
      <el-form v-if="step === 1" :model="form" :rules="rules" ref="form1" label-width="0" class="forget-form">
        <div class="form-tip">
          <i class="el-icon-info"></i>
          <span>我们将向你的注册邮箱发送6位验证码</span>
        </div>

        <el-form-item prop="email">
          <div class="input-label">
            <i class="el-icon-message"></i>
            <span>邮箱地址</span>
          </div>
          <div class="email-group">
            <el-input
                v-model="form.email"
                placeholder="请输入注册邮箱"
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
                :class="['code-button', { 'is-sending': codeSending }]"
                size="large"
            >
              {{ codeBtnText }}
            </el-button>
          </div>
          <transition name="fade">
            <div class="input-hint" v-if="!form.email && focusedField !== 'email'">
              <i class="el-icon-info"></i> 请输入你注册时使用的邮箱
            </div>
          </transition>
        </el-form-item>

        <el-form-item prop="code">
          <div class="input-label">
            <i class="el-icon-key"></i>
            <span>验证码</span>
          </div>
          <el-input
              v-model="form.code"
              placeholder="请输入6位验证码"
              size="large"
              maxlength="6"
              @focus="handleFocus('code')"
              @blur="handleBlur"
          >
            <i slot="prefix" class="el-icon-key" :class="{ 'active-icon': focusedField === 'code' }"></i>
          </el-input>
          <transition name="fade">
            <div class="input-hint" v-if="!form.code && focusedField !== 'code'">
              <i class="el-icon-info"></i> 请查收邮件中的6位数字验证码
            </div>
          </transition>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="nextStep"
              :loading="verifying"
              class="action-button"
              :disabled="!form.email || !form.code"
          >
            <span v-if="!verifying">下一步 <i class="el-icon-arrow-right"></i></span>
            <span v-else>验证中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 第二步：重置密码 -->
      <el-form v-else-if="step === 2" :model="form" :rules="rules" ref="form2" label-width="0" class="forget-form">
        <div class="form-tip">
          <i class="el-icon-lock"></i>
          <span>请设置新密码，长度至少6位</span>
        </div>

        <el-form-item prop="newPassword">
          <div class="input-label">
            <i class="el-icon-unlock"></i>
            <span>新密码</span>
          </div>
          <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码"
              size="large"
              show-password
              @focus="handleFocus('newPassword')"
              @blur="handleBlur"
              @input="checkPasswordStrength"
          >
            <i slot="prefix" class="el-icon-unlock" :class="{ 'active-icon': focusedField === 'newPassword' }"></i>
          </el-input>
        </el-form-item>

        <!-- 密码强度指示器 -->
        <div class="password-strength" v-if="form.newPassword">
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

        <el-form-item prop="confirmPassword">
          <div class="input-label">
            <i class="el-icon-circle-check"></i>
            <span>确认密码</span>
          </div>
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              @focus="handleFocus('confirmPassword')"
              @blur="handleBlur"
          >
            <i slot="prefix" class="el-icon-circle-check" :class="{ 'active-icon': focusedField === 'confirmPassword' }"></i>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="doResetPassword"
              :loading="resetting"
              class="action-button"
              :disabled="!form.newPassword || !form.confirmPassword"
          >
            <span v-if="!resetting">确认重置 <i class="el-icon-arrow-right"></i></span>
            <span v-else>重置中...</span>
          </el-button>
        </el-form-item>

        <div class="back-link">
          <el-button type="text" @click="step = 1">
            <i class="el-icon-arrow-left"></i> 返回上一步
          </el-button>
        </div>
      </el-form>

      <!-- 第三步：完成 -->
      <div v-else class="complete-step">
        <div class="success-animation">
          <div class="success-circle">
            <i class="el-icon-check"></i>
          </div>
        </div>
        <h3 class="success-title">密码重置成功</h3>
        <p class="success-text">您的密码已重置，请使用新密码登录</p>

        <div class="success-tips">
          <div class="tip-item">
            <i class="el-icon-time"></i>
            <span>建议定期更换密码</span>
          </div>
          <div class="tip-item">
            <i class="el-icon-lock"></i>
            <span>不要与其他网站使用相同密码</span>
          </div>
        </div>

        <el-button type="primary" @click="goToLogin" class="login-button">
          <i class="el-icon-arrow-left"></i> 立即登录
        </el-button>
      </div>

      <!-- 底部链接 -->
      <div class="footer-links" v-if="step !== 3">
        <router-link to="/login" class="footer-link">
          <i class="el-icon-arrow-left"></i> 返回登录
        </router-link>
        <router-link to="/register" class="footer-link">
          立即注册 <i class="el-icon-arrow-right"></i>
        </router-link>
      </div>

      <!-- 底部装饰 -->
      <div class="card-footer"></div>
    </div>
  </div>
</template>

<script>
import { sendForgetCode, resetPassword, checkEmail } from '@/api/user';

export default {
  name: 'ForgetPassword',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.form.newPassword) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

    const validateEmailExists = (rule, value, callback) => {
      if (!value) {
        callback();
        return;
      }
      checkEmail(value).then(res => {
        if (!res.data) {
          callback(new Error('该邮箱未注册'));
        } else {
          callback();
        }
      }).catch(() => {
        callback();
      });
    };

    return {
      step: 1,
      focusedField: '',
      form: {
        email: '',
        code: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordStrength: 'none',
      rules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
          { validator: validateEmailExists, trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码为6位数字', trigger: 'blur' },
          { pattern: /^\d{6}$/, message: '验证码必须是数字', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9!@#$%^&*]+$/, message: '密码包含非法字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      codeSending: false,
      countdown: 60,
      timer: null,
      verifying: false,
      resetting: false
    };
  },
  computed: {
    canSendCode() {
      return this.form.email &&
          !this.codeSending &&
          this.countdown === 60 &&
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.form.email);
    },
    codeBtnText() {
      if (this.countdown < 60) {
        return `${this.countdown}秒后重试`;
      }
      if (this.codeSending) {
        return '发送中...';
      }
      return '获取验证码';
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
      const pwd = this.form.newPassword;
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
    sendCode() {
      this.$refs.form1.validateField('email', errorMsg => {
        if (errorMsg) {
          this.$message.warning('请先填写正确的邮箱地址');
          return;
        }

        this.codeSending = true;
        sendForgetCode(this.form.email)
            .then(() => {
              this.$message.success({
                message: '📧 验证码已发送，请查收邮件',
                duration: 3000
              });
              this.startCountdown();
            })
            .catch(error => {
              this.codeSending = false;
              this.countdown = 60;
              this.$message.error(error.message || '验证码发送失败');
            });
      });
    },
    startCountdown() {
      this.countdown = 60;
      this.codeSending = true;

      if (this.timer) {
        clearInterval(this.timer);
      }

      this.timer = setInterval(() => {
        this.countdown -= 1;
        if (this.countdown <= 0) {
          clearInterval(this.timer);
          this.timer = null;
          this.countdown = 60;
          this.codeSending = false;
        }
      }, 1000);
    },
    nextStep() {
      this.$refs.form1.validate(valid => {
        if (!valid) {
          this.$message.warning('请正确填写验证信息');
          return;
        }
        this.step = 2;
      });
    },
    // ✅ 修复：使用真实的 resetPassword API
    doResetPassword() {
      this.$refs.form2.validate(valid => {
        if (!valid) {
          this.$message.warning('请正确填写密码');
          return;
        }

        this.resetting = true;

        // 调用真实的 API
        resetPassword({
          email: this.form.email,
          newPassword: this.form.newPassword,
          code: this.form.code
        })
            .then(res => {
              this.resetting = false;
              if (res.code === 200) {
                this.$message.success('🎉 密码重置成功');
                this.step = 3;
              } else {
                this.$message.error(res.message || '密码重置失败');
              }
            })
            .catch(error => {
              this.resetting = false;
              this.$message.error(error.message || '密码重置失败，请稍后重试');
            });
      });
    },
    goToLogin() {
      this.$router.push('/login');
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
.forget-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
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

@keyframes float {
  0% { transform: rotate(0deg) translateY(0); }
  50% { transform: rotate(180deg) translateY(-20px); }
  100% { transform: rotate(360deg) translateY(0); }
}

/* 找回密码卡片 */
.forget-card {
  width: 520px;
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
  margin-bottom: 15px;
}

.lock-wrapper {
  display: inline-flex;
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
  animation: pulse 2s infinite;
}

.lock-wrapper i {
  font-size: 32px;
  color: white;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
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

/* 步骤条 */
.steps-container {
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
  flex: 1;
}

.step-marker {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f0f0;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 18px;
  transition: all 0.3s;
}

.step-item.active .step-marker {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transform: scale(1.1);
}

.step-item.completed .step-marker {
  background: #67c23a;
  color: white;
}

.step-label {
  font-size: 12px;
  color: #999;
  transition: color 0.3s;
}

.step-item.active .step-label {
  color: #667eea;
  font-weight: 500;
}

.step-connector {
  width: 60px;
  height: 2px;
  background: #f0f0f0;
  margin: 0 5px;
  transition: background 0.3s;
}

.step-connector.active {
  background: linear-gradient(90deg, #667eea, #764ba2);
}

/* 表单 */
.forget-form {
  padding: 0 40px 20px;
}

.form-tip {
  background: #f0f7ff;
  border-left: 4px solid #667eea;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.form-tip i {
  color: #667eea;
  font-size: 18px;
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

/* 输入框样式 */
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
.input-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
  padding-left: 12px;
}

.input-hint i {
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

/* 邮箱输入组 */
.email-group {
  display: flex;
  gap: 12px;
}

.email-input {
  flex: 1;
}

.code-button {
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

.code-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.code-button:active {
  transform: translateY(0);
}

.code-button:disabled {
  opacity: 0.5;
  filter: grayscale(50%);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.code-button.is-sending {
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

/* 操作按钮 */
.action-button {
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
  margin-top: 20px;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(102, 126, 234, 0.4);
}

.action-button:active {
  transform: translateY(0);
}

.action-button:disabled {
  opacity: 0.6;
  filter: grayscale(30%);
  transform: none;
  box-shadow: none;
}

/* 返回链接 */
.back-link {
  text-align: center;
  margin-top: 15px;
}

.back-link ::v-deep .el-button {
  color: #999;
  font-size: 14px;
}

.back-link ::v-deep .el-button:hover {
  color: #667eea;
}

/* 完成步骤 */
.complete-step {
  text-align: center;
  padding: 30px 40px 20px;
}

.success-animation {
  margin-bottom: 30px;
}

.success-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 10px 30px rgba(103, 194, 58, 0.3);
  animation: scaleIn 0.5s ease;
}

@keyframes scaleIn {
  0% { transform: scale(0); }
  70% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.success-circle i {
  font-size: 50px;
  color: white;
}

.success-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.success-text {
  color: #999;
  margin-bottom: 30px;
  font-size: 16px;
}

.success-tips {
  background: #f8f9fa;
  border-radius: 16px;
  padding: 20px;
  margin: 30px 0;
  text-align: left;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  color: #666;
  font-size: 14px;
}

.tip-item i {
  color: #667eea;
  font-size: 16px;
}

.login-button {
  width: 200px;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(102, 126, 234, 0.4);
}

/* 底部链接 */
.footer-links {
  display: flex;
  justify-content: space-between;
  padding: 20px 40px 30px;
  border-top: 1px solid #f0f0f0;
}

.footer-link {
  color: #999;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  gap: 5px;
}

.footer-link:hover {
  color: #667eea;
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
  .forget-card {
    padding: 0;
  }

  .card-header {
    padding: 30px 20px 10px;
  }

  .title {
    font-size: 24px;
  }

  .steps-container {
    padding: 10px 20px 20px;
  }

  .step-connector {
    width: 30px;
  }

  .step-marker {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .step-label {
    font-size: 10px;
  }

  .forget-form {
    padding: 0 20px 15px;
  }

  .email-group {
    flex-direction: column;
  }

  .code-button {
    width: 100%;
  }

  .success-circle {
    width: 80px;
    height: 80px;
  }

  .success-circle i {
    font-size: 40px;
  }

  .success-title {
    font-size: 20px;
  }

  .footer-links {
    padding: 15px 20px 20px;
  }
}

/* 自定义消息样式 */
::v-deep .el-message {
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  min-width: 300px;
}

::v-deep .el-message--success {
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border: 1px solid #91d5ff;
}

::v-deep .el-message--error {
  background: linear-gradient(135deg, #fff2f0 0%, #fff1f0 100%);
  border: 1px solid #ffccc7;
}

::v-deep .el-message--warning {
  background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
  border: 1px solid #ffe58f;
}
</style>