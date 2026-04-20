<!-- forget-password.vue 修改后的完整代码 -->
<template>
  <div class="forget-container">
    <!-- 背景 -->
    <div class="bg-pattern"></div>

    <div class="forget-card">
      <!-- 卡片头部 -->
      <div class="card-header">
        <div class="logo">
          <svg class="logo-svg" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M25 5C14.5 5 6 13.5 6 24C6 34.5 14.5 43 25 43C35.5 43 44 34.5 44 24C44 13.5 35.5 5 25 5Z" stroke="#409EFF" stroke-width="2" fill="white"/>
            <circle cx="18" cy="22" r="2" fill="#409EFF"/>
            <circle cx="32" cy="22" r="2" fill="#409EFF"/>
            <path d="M20 32C22 35 28 35 30 32" stroke="#409EFF" stroke-width="2" stroke-linecap="round"/>
            <path d="M16 16L20 20M34 16L30 20" stroke="#409EFF" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span class="logo-text">宠物服务系统</span>
        </div>
        <h2 class="title">找回密码</h2>
        <p class="subtitle">请输入注册邮箱，我们将发送验证码</p>
      </div>

      <!-- 步骤条 -->
      <div class="step-indicator">
        <div class="step-item" :class="{ active: step >= 1, completed: step > 1 }">
          <div class="step-circle">
            <span v-if="step <= 1">1</span>
            <i v-else class="el-icon-check"></i>
          </div>
          <span class="step-text">验证身份</span>
        </div>
        <div class="step-line" :class="{ active: step >= 2 }"></div>
        <div class="step-item" :class="{ active: step >= 2, completed: step > 2 }">
          <div class="step-circle">
            <span v-if="step <= 2">2</span>
            <i v-else class="el-icon-check"></i>
          </div>
          <span class="step-text">重置密码</span>
        </div>
        <div class="step-line" :class="{ active: step >= 3 }"></div>
        <div class="step-item" :class="{ active: step >= 3 }">
          <div class="step-circle">3</div>
          <span class="step-text">完成</span>
        </div>
      </div>

      <!-- 第一步：验证身份 -->
      <el-form v-if="step === 1" :model="form" :rules="rules" ref="form1" label-width="0" class="forget-form">
        <div class="form-tip">
          <i class="el-icon-info"></i>
          <span>我们将向你的注册邮箱发送6位验证码</span>
        </div>

        <el-form-item prop="email">
          <el-input
              v-model="form.email"
              placeholder="请输入注册邮箱"
              size="large"
              prefix-icon="el-icon-message"
              clearable
          ></el-input>
        </el-form-item>

        <el-form-item prop="code">
          <div class="code-group">
            <el-input
                v-model="form.code"
                placeholder="请输入6位验证码"
                size="large"
                prefix-icon="el-icon-key"
                maxlength="6"
                clearable
                class="code-input"
            ></el-input>
            <el-button
                :disabled="!canSendCode"
                @click="sendCode"
                :loading="codeSending"
                size="large"
                class="code-btn"
            >
              {{ codeBtnText }}
            </el-button>
          </div>
        </el-form-item>

        <el-button
            type="primary"
            @click="nextStep"
            :loading="verifying"
            class="action-button"
            :disabled="!form.email || !form.code"
        >
          {{ verifying ? '验证中...' : '下一步' }}
        </el-button>
      </el-form>

      <!-- 第二步：重置密码 -->
      <el-form v-else-if="step === 2" :model="form" :rules="rules" ref="form2" label-width="0" class="forget-form">
        <div class="form-tip">
          <i class="el-icon-lock"></i>
          <span>请设置新密码，长度6-20位</span>
        </div>

        <el-form-item prop="newPassword">
          <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码"
              size="large"
              prefix-icon="el-icon-lock"
              show-password
              clearable
          ></el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              prefix-icon="el-icon-circle-check"
              show-password
              clearable
          ></el-input>
        </el-form-item>

        <el-button
            type="primary"
            @click="doResetPassword"
            :loading="resetting"
            class="action-button"
            :disabled="!form.newPassword || !form.confirmPassword"
        >
          {{ resetting ? '重置中...' : '确认重置' }}
        </el-button>

        <div class="back-link">
          <el-button type="text" @click="step = 1">
            <i class="el-icon-arrow-left"></i> 返回上一步
          </el-button>
        </div>
      </el-form>

      <!-- 第三步：完成 -->
      <div v-else class="complete-step">
        <div class="success-icon">
          <i class="el-icon-circle-check"></i>
        </div>
        <h3 class="success-title">密码重置成功</h3>
        <p class="success-text">您的密码已重置，请使用新密码登录</p>
        <el-button type="primary" @click="goToLogin" class="login-button">立即登录</el-button>
      </div>

      <!-- 底部链接 -->
      <div class="footer-links" v-if="step !== 3">
        <router-link to="/login" class="footer-link">返回登录</router-link>
        <router-link to="/register" class="footer-link">立即注册</router-link>
      </div>
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
          { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
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
      return '获取验证码';
    },
    strengthText() {
      const map = { none: '未设置', weak: '弱', medium: '中', strong: '强' };
      return map[this.passwordStrength];
    },
    strengthWidth() {
      const map = { none: 0, weak: 33, medium: 66, strong: 100 };
      return map[this.passwordStrength];
    }
  },
  methods: {
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
        this.startCountdown();

        sendForgetCode(this.form.email)
            .then(() => {
              this.$message.success('验证码已发送，请查收邮件');
            })
            .catch(() => {
              this.stopCountdown();
              this.$message.error('验证码发送失败');
            });
      });
    },

    startCountdown() {
      this.countdown = 60;
      this.codeSending = true;

      if (this.timer) clearInterval(this.timer);
      this.timer = setInterval(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          this.stopCountdown();
        }
      }, 1000);
    },

    stopCountdown() {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
      this.countdown = 60;
      this.codeSending = false;
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

    doResetPassword() {
      this.$refs.form2.validate(valid => {
        if (!valid) {
          this.$message.warning('请正确填写密码');
          return;
        }

        this.resetting = true;
        resetPassword({
          email: this.form.email,
          newPassword: this.form.newPassword,
          code: this.form.code
        })
            .then(res => {
              this.resetting = false;
              if (res.code === 200) {
                this.$message.success('密码重置成功');

                // 保存邮箱和新密码到 sessionStorage，用于登录页自动填充
                const loginData = {
                  account: this.form.email,      // 邮箱作为账号
                  password: this.form.newPassword
                };
                sessionStorage.setItem('autoFillLogin', JSON.stringify(loginData));

                this.step = 3;
              } else {
                this.$message.error(res.message || '密码重置失败');
              }
            })
            .catch(() => {
              this.resetting = false;
              this.$message.error('密码重置失败，请稍后重试');
            });
      });
    },

    goToLogin() {
      if (this.form.newPassword && this.form.email) {
        const loginData = {
          account: this.form.email,
          password: this.form.newPassword
        };
        sessionStorage.setItem('autoFillLogin', JSON.stringify(loginData));
      }
      this.$router.push('/login');
    }
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer);
  }
};
</script>

<style scoped>
.forget-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8edf2 100%);
  padding: 20px;
  position: relative;
}

/* 背景图案 */
.bg-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(#c0c4cc 1px, transparent 1px);
  background-size: 30px 30px;
  opacity: 0.3;
}

/* 找回密码卡片 */
.forget-card {
  width: 480px;
  max-width: 95%;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
  overflow: hidden;
  animation: slideUp 0.4s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片头部 */
.card-header {
  padding: 32px 32px 20px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 16px;
}

.logo-svg {
  width: 36px;
  height: 36px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  letter-spacing: 1px;
}

.title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #2c3e50;
}

.subtitle {
  color: #909399;
  font-size: 13px;
  margin: 0;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 32px 20px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.step-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f0f0;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s;
}

.step-item.active .step-circle {
  background: #409EFF;
  color: white;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.step-item.completed .step-circle {
  background: #67c23a;
  color: white;
}

.step-text {
  font-size: 12px;
  color: #999;
  transition: color 0.3s;
}

.step-item.active .step-text {
  color: #409EFF;
  font-weight: 500;
}

.step-line {
  width: 50px;
  height: 2px;
  background: #f0f0f0;
  margin: 0 8px;
  transition: background 0.3s;
}

.step-line.active {
  background: #409EFF;
}

/* 表单 */
.forget-form {
  padding: 0 32px 24px;
}

.form-tip {
  background: #ecf5ff;
  border-left: 3px solid #409EFF;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 13px;
}

.form-tip i {
  color: #409EFF;
  font-size: 16px;
}

/* 输入框样式 */
::v-deep .el-input__inner {
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  border-color: #e4e7ed;
  transition: all 0.3s;
}

::v-deep .el-input__inner:hover {
  border-color: #c0c4cc;
}

::v-deep .el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

::v-deep .el-form-item {
  margin-bottom: 20px;
}

/* 验证码组 - 样式与注册页面一致 */
.code-group {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.code-btn {
  min-width: 110px;
  height: 44px;
  background: #409EFF;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  color: #ffffff;
}

.code-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
  color: #ffffff;
}

.code-btn:disabled {
  background: #a0cfff;
  color: #ffffff;
  cursor: not-allowed;
  transform: none;
  opacity: 0.7;
}

.code-btn:focus {
  outline: none;
}

.code-btn span {
  color: #ffffff;
}

.code-btn.is-disabled span,
.code-btn:disabled span {
  color: #ffffff;
}

/* 操作按钮 */
.action-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: #409EFF;
  border: none;
  border-radius: 8px;
  margin-top: 8px;
  transition: all 0.3s;
}

.action-button:hover {
  background: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.action-button:disabled {
  background: #a0cfff;
  transform: none;
  box-shadow: none;
}

/* 返回链接 */
.back-link {
  text-align: center;
  margin-top: 16px;
}

.back-link ::v-deep .el-button {
  color: #909399;
  font-size: 13px;
}

.back-link ::v-deep .el-button:hover {
  color: #409EFF;
}

/* 完成步骤 */
.complete-step {
  text-align: center;
  padding: 32px;
}

.success-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  background: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-icon i {
  font-size: 48px;
  color: white;
}

.success-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.success-text {
  color: #909399;
  font-size: 14px;
  margin-bottom: 28px;
}

.login-button {
  width: 160px;
  height: 44px;
  font-size: 16px;
  background: #409EFF;
  border: none;
  border-radius: 8px;
}

.login-button:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

/* 底部链接 */
.footer-links {
  display: flex;
  justify-content: space-between;
  padding: 20px 32px 24px;
  border-top: 1px solid #f0f0f0;
}

.footer-link {
  color: #909399;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.3s;
}

.footer-link:hover {
  color: #409EFF;
}

/* 响应式 */
@media (max-width: 768px) {
  .forget-card {
    width: 95%;
  }

  .card-header {
    padding: 24px 20px 16px;
  }

  .title {
    font-size: 20px;
  }

  .step-indicator {
    padding: 16px 20px;
  }

  .step-line {
    width: 30px;
  }

  .step-circle {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }

  .step-text {
    font-size: 10px;
  }

  .forget-form {
    padding: 0 20px 20px;
  }

  .code-group {
    flex-direction: column;
  }

  .code-btn {
    width: 100%;
  }

  .footer-links {
    padding: 16px 20px 20px;
  }

  .complete-step {
    padding: 24px;
  }

  .success-icon {
    width: 64px;
    height: 64px;
  }

  .success-icon i {
    font-size: 36px;
  }
}
</style>