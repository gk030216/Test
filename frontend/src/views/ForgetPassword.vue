<template>
  <div class="forget-page">
    <!-- 左侧品牌区 -->
    <div class="forget-left">
      <div class="brand-content">
        <div class="brand-icon">
          <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="60" cy="60" r="56" fill="rgba(255,255,255,0.15)" stroke="rgba(255,255,255,0.35)" stroke-width="2"/>
            <ellipse cx="60" cy="72" rx="32" ry="26" fill="rgba(255,255,255,0.92)"/>
            <polygon points="34,50 20,18 48,42" fill="rgba(255,255,255,0.92)"/>
            <polygon points="86,50 100,18 72,42" fill="rgba(255,255,255,0.92)"/>
            <polygon points="36,47 27,26 47,43" fill="#f9c5b6"/>
            <polygon points="84,47 93,26 73,43" fill="#f9c5b6"/>
            <ellipse cx="47" cy="66" rx="6" ry="6.5" fill="#3d3d3d"/>
            <ellipse cx="73" cy="66" rx="6" ry="6.5" fill="#3d3d3d"/>
            <ellipse cx="48" cy="64" rx="2.5" ry="3" fill="white"/>
            <ellipse cx="74" cy="64" rx="2.5" ry="3" fill="white"/>
            <ellipse cx="60" cy="75" rx="3.5" ry="2.5" fill="#f4a49b"/>
            <path d="M56 77 Q54 82 51 80" stroke="#f4a49b" stroke-width="1.5" fill="none" stroke-linecap="round"/>
            <path d="M64 77 Q66 82 69 80" stroke="#f4a49b" stroke-width="1.5" fill="none" stroke-linecap="round"/>
          </svg>
        </div>
        <h1 class="brand-title">喵汪星球</h1>
        <p class="brand-desc">别担心，找回密码很简单</p>
      </div>
      <div class="paw-deco">
        <span class="paw p1">🐾</span>
        <span class="paw p2">🐾</span>
        <span class="paw p3">🐾</span>
      </div>
    </div>

    <!-- 右侧表单 -->
    <div class="forget-right">
      <div class="form-wrapper">
        <div class="form-header">
          <h3 class="form-title">找回密码 🔑</h3>
          <p class="form-subtitle">验证身份后即可重置密码</p>
        </div>

        <!-- 步骤指示器 -->
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
            <i class="el-icon-message"></i>
            <span>将向您的注册邮箱发送 6 位验证码</span>
          </div>

          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入注册邮箱"
              size="large"
              clearable
            >
              <template #prefix>
                <i class="el-icon-message input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="code">
            <div class="code-group">
              <el-input
                v-model="form.code"
                placeholder="6位验证码"
                size="large"
                maxlength="6"
                clearable
                class="code-input"
              >
                <template #prefix>
                  <i class="el-icon-key input-icon"></i>
                </template>
              </el-input>
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
            class="action-btn"
            :disabled="!form.email || !form.code"
          >
            {{ verifying ? '验证中...' : '下一步' }}
          </el-button>
        </el-form>

        <!-- 第二步：重置密码 -->
        <el-form v-else-if="step === 2" :model="form" :rules="rules" ref="form2" label-width="0" class="forget-form">
          <div class="form-tip">
            <i class="el-icon-lock"></i>
            <span>请设置新密码，长度 6-20 位</span>
          </div>

          <el-form-item prop="newPassword">
            <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="新密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <i class="el-icon-lock input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入新密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <i class="el-icon-circle-check input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <el-button
            type="primary"
            @click="doResetPassword"
            :loading="resetting"
            class="action-btn"
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
          <h3 class="success-title">密码重置成功 🎉</h3>
          <p class="success-text">请使用新密码登录喵汪星球</p>
          <el-button type="primary" @click="goToLogin" class="to-login-btn">立即登录</el-button>
        </div>

        <!-- 底部链接 -->
        <div class="footer-links" v-if="step !== 3">
          <router-link to="/login" class="footer-link">返回登录</router-link>
          <router-link to="/register" class="footer-link">立即注册</router-link>
        </div>
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
    }
  },
  methods: {
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

              const loginData = {
                account: this.form.email,
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
.forget-page {
  min-height: 100vh;
  display: flex;
  font-family: 'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ========== 左侧品牌区 ========== */
.forget-left {
  flex: 0 0 40%;
  position: relative;
  background: linear-gradient(160deg, #f59e4b 0%, #f0826a 30%, #e8647c 65%, #d4527e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.forget-left::before {
  content: '';
  position: absolute;
  width: 380px;
  height: 380px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%);
  top: -60px;
  right: -80px;
}

.forget-left::after {
  content: '';
  position: absolute;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
  bottom: -30px;
  left: -40px;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #fff;
  padding: 40px;
  max-width: 340px;
}

.brand-icon {
  width: 90px;
  height: 90px;
  margin: 0 auto 16px;
  animation: gentleBounce 3s ease-in-out infinite;
}

@keyframes gentleBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.brand-icon svg {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 34px;
  font-weight: 700;
  margin: 0 0 10px;
  letter-spacing: 6px;
  text-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.brand-desc {
  font-size: 14px;
  opacity: 0.75;
  margin: 0;
  line-height: 1.6;
}

.paw-deco {
  position: absolute;
  bottom: 28px;
  right: 24px;
  display: flex;
  gap: 10px;
  opacity: 0.25;
  transform: rotate(22deg);
}

.paw {
  font-size: 22px;
  animation: pawFade 2.5s ease-in-out infinite;
}

.p2 { animation-delay: 0.5s; }
.p3 { animation-delay: 1.0s; }

@keyframes pawFade {
  0%, 100% { opacity: 0.2; transform: scale(0.85); }
  50% { opacity: 0.55; transform: scale(1.1); }
}

/* ========== 右侧表单区 ========== */
.forget-right {
  flex: 0 0 60%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fdf8f5;
  padding: 40px 20px;
}

.form-wrapper {
  width: 460px;
  max-width: 95%;
  animation: formSlideUp 0.5s ease;
}

@keyframes formSlideUp {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

.form-header {
  text-align: center;
  margin-bottom: 20px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: #3d2e2a;
  margin: 0 0 6px;
}

.form-subtitle {
  font-size: 14px;
  color: #a08c84;
  margin: 0;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px 22px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.step-circle {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #f0e8e2;
  color: #b8a098;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.3s;
}

.step-item.active .step-circle {
  background: #f59e4b;
  color: white;
  box-shadow: 0 2px 8px rgba(245,158,75,0.35);
}

.step-item.completed .step-circle {
  background: #67c23a;
  color: white;
}

.step-text {
  font-size: 12px;
  color: #b8a098;
  transition: color 0.3s;
}

.step-item.active .step-text {
  color: #f0826a;
  font-weight: 500;
}

.step-line {
  width: 44px;
  height: 2px;
  background: #f0e8e2;
  margin: 0 8px;
  transition: background 0.3s;
}

.step-line.active {
  background: #f59e4b;
}

/* 表单 */
.forget-form {
  background: #fff;
  padding: 28px;
  border-radius: 16px;
  box-shadow: 0 2px 18px rgba(180,130,100,0.06), 0 8px 36px rgba(180,120,90,0.04);
}

.form-tip {
  background: #fef6f0;
  border-left: 3px solid #f59e4b;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 22px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8c7068;
  font-size: 13px;
}

.form-tip i {
  color: #f59e4b;
  font-size: 16px;
}

/* 输入框 */
::v-deep .forget-form .el-input__inner {
  height: 44px;
  line-height: 44px;
  border-radius: 10px;
  border: 1.5px solid #f0e0d6;
  background: #fefbf9;
  font-size: 14px;
  padding-left: 40px;
  transition: all 0.25s;
}

::v-deep .forget-form .el-input__inner:hover {
  border-color: #e8c8b0;
  background: #fff;
}

::v-deep .forget-form .el-input__inner:focus {
  border-color: #f59e4b;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(245,158,75,0.1);
}

::v-deep .forget-form .el-input__prefix {
  left: 12px;
}

.input-icon {
  font-size: 17px;
  color: #d0b8a8;
  transition: color 0.25s;
}

::v-deep .forget-form .el-input.is-focus .input-icon {
  color: #f59e4b;
}

::v-deep .forget-form .el-form-item {
  margin-bottom: 18px;
}

::v-deep .forget-form .el-form-item__error {
  padding-left: 4px;
  font-size: 12px;
}

/* 验证码组 */
.code-group {
  display: flex;
  gap: 10px;
}

.code-input {
  flex: 1;
}

.code-btn {
  min-width: 108px;
  height: 44px;
  background: #f59e4b;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  transition: all 0.3s;
}

.code-btn:hover {
  background: #f7b06a;
  transform: translateY(-1px);
}

.code-btn:disabled {
  background: #f5d5b8;
  color: #fff;
  cursor: not-allowed;
  transform: none;
  opacity: 0.8;
}

.code-btn span {
  color: #fff;
}

/* 操作按钮 */
.action-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  margin-top: 4px;
  transition: all 0.3s;
}

.action-btn:hover {
  background: linear-gradient(135deg, #f7b06a, #f2967e);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(245,158,75,0.35);
}

.action-btn:disabled {
  background: #f5d5b8;
  transform: none;
  box-shadow: none;
}

.action-btn.is-loading {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
}

/* 返回上一步 */
.back-link {
  text-align: center;
  margin-top: 14px;
}

.back-link ::v-deep .el-button {
  color: #b8a098;
  font-size: 13px;
}

.back-link ::v-deep .el-button:hover {
  color: #f0826a;
}

/* 完成步骤 */
.complete-step {
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  text-align: center;
  box-shadow: 0 2px 18px rgba(180,130,100,0.06), 0 8px 36px rgba(180,120,90,0.04);
}

.success-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  background: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-icon i {
  font-size: 42px;
  color: white;
}

.success-title {
  font-size: 20px;
  font-weight: 600;
  color: #3d2e2a;
  margin-bottom: 8px;
}

.success-text {
  color: #a08c84;
  font-size: 14px;
  margin-bottom: 24px;
}

.to-login-btn {
  width: 160px;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  transition: all 0.3s;
}

.to-login-btn:hover {
  background: linear-gradient(135deg, #f7b06a, #f2967e);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(245,158,75,0.35);
}

/* 底部链接 */
.footer-links {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  padding: 0 4px;
}

.footer-link {
  color: #b8a098;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s;
}

.footer-link:hover {
  color: #f0826a;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .forget-left {
    display: none;
  }

  .forget-right {
    flex: 1;
    background: linear-gradient(170deg, #fef6f0 0%, #fdf8f5 50%, #fdf9f6 100%);
  }

  .form-wrapper {
    width: 440px;
  }

  .forget-form {
    padding: 24px 22px;
  }

  .form-title {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .form-wrapper {
    width: 94%;
  }

  .forget-form {
    padding: 22px 16px;
    border-radius: 12px;
  }

  .code-group {
    flex-direction: column;
  }

  .code-btn {
    width: 100%;
  }

  .complete-step {
    padding: 32px 20px;
    border-radius: 12px;
  }
}
</style>
