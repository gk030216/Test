<template>
  <div class="register-container">
    <!-- 背景 -->
    <div class="bg-pattern"></div>

    <div class="register-card">
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
        <h2 class="title">注册新账号</h2>
        <p class="subtitle">加入我们，享受专业宠物服务</p>
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
          <el-input
              v-model="registerForm.username"
              placeholder="用户名（3-20个字符）"
              size="large"
              prefix-icon="el-icon-user"
              clearable
          ></el-input>
        </el-form-item>

        <!-- 昵称 -->
        <el-form-item prop="nickname">
          <el-input
              v-model="registerForm.nickname"
              placeholder="昵称"
              size="large"
              prefix-icon="el-icon-edit"
              clearable
          ></el-input>
        </el-form-item>

        <!-- 邮箱 + 验证码 -->
        <el-form-item prop="email">
          <div class="email-group">
            <el-input
                v-model="registerForm.email"
                placeholder="邮箱地址"
                size="large"
                prefix-icon="el-icon-message"
                clearable
                class="email-input"
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

        <!-- 验证码 -->
        <el-form-item prop="code">
          <el-input
              v-model="registerForm.code"
              placeholder="请输入6位验证码"
              size="large"
              prefix-icon="el-icon-key"
              maxlength="6"
              clearable
          ></el-input>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码（6-20位）"
              size="large"
              prefix-icon="el-icon-lock"
              show-password
              clearable
          ></el-input>
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              prefix-icon="el-icon-circle-check"
              show-password
              clearable
          ></el-input>
        </el-form-item>

        <!-- 手机号（必填） -->
        <el-form-item prop="phone">
          <el-input
              v-model="registerForm.phone"
              placeholder="手机号"
              size="large"
              prefix-icon="el-icon-phone"
              clearable
          ></el-input>
        </el-form-item>

        <!-- 性别 -->
        <el-form-item prop="gender">
          <el-select v-model="registerForm.gender" placeholder="请选择性别" size="large" style="width: 100%">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
            <el-option label="保密" value="保密"></el-option>
          </el-select>
        </el-form-item>

        <!-- 用户协议 -->
        <div class="agreement">
          <el-checkbox v-model="agreeProtocol">
            <span>我已阅读并同意</span>
            <a href="#" class="protocol-link">《用户协议》</a>
            <span>和</span>
            <a href="#" class="protocol-link">《隐私政策》</a>
          </el-checkbox>
        </div>

        <!-- 注册按钮 -->
        <el-button
            type="primary"
            @click="handleRegister"
            :loading="loading"
            class="register-button"
            :disabled="!agreeProtocol"
        >
          {{ loading ? '注册中...' : '注 册' }}
        </el-button>

        <!-- 登录链接 -->
        <div class="form-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { register, checkUsername, checkEmail, sendRegisterCode } from '@/api/user';

export default {
  name: 'Register',
  data() {
    // 自定义验证：用户名唯一性
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入用户名'));
        return;
      }
      checkUsername(value).then(res => {
        if (res.data) {
          callback(new Error('用户名已存在'));
        } else {
          callback();
        }
      }).catch(() => {
        callback();
      });
    };

    // 自定义验证：邮箱唯一性
    const validateEmail = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入邮箱'));
        return;
      }
      checkEmail(value).then(res => {
        if (res.data) {
          callback(new Error('邮箱已被注册'));
        } else {
          callback();
        }
      }).catch(() => {
        callback();
      });
    };

    // 确认密码验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.registerForm.password) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

    // 手机号验证（必填）
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入手机号'));
        return;
      }
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (!phoneRegex.test(value)) {
        callback(new Error('手机号格式不正确'));
      } else {
        callback();
      }
    };

    return {
      step: 1,
      registerForm: {
        username: '',
        nickname: '',
        email: '',
        code: '',
        password: '',
        confirmPassword: '',
        phone: '',
        gender: '保密'
      },
      agreeProtocol: true,
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
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
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
        ],
        gender: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ]
      }
    };
  },
  computed: {
    canSendCode() {
      // 倒计时期间不能发送
      return this.registerForm.email &&
          !this.codeSending &&
          this.countdown === 60 &&
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.registerForm.email);
    },
    codeBtnText() {
      // 倒计时显示优先
      if (this.countdown < 60) {
        return `${this.countdown}秒后重试`;
      }
      return '获取验证码';
    },
    isFormValid() {
      return this.registerForm.username &&
          this.registerForm.nickname &&
          this.registerForm.email &&
          this.registerForm.code &&
          this.registerForm.password &&
          this.registerForm.confirmPassword &&
          this.registerForm.phone &&
          this.registerForm.gender;
    }
  },
  methods: {
    sendCode() {
      this.$refs.registerForm.validateField('email', errorMsg => {
        if (errorMsg) {
          this.$message.warning('请先填写正确的邮箱地址');
          return;
        }

        // ✅ 开始倒计时
        this.codeSending = true;
        this.startCountdown();

        sendRegisterCode(this.registerForm.email)
            .then(() => {
              this.$message.success('验证码已发送，请查收邮件');
            })
            .catch(() => {
              // 发送失败时停止倒计时
              this.stopCountdown();

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
          this.stopCountdown();
        }
      }, 1000);
    },

    // 停止倒计时的方法
    stopCountdown() {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
      this.countdown = 60;
      this.codeSending = false;
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

                // 保存注册信息，用于登录页自动填充
                const loginData = {
                  account: this.registerForm.username,
                  password: this.registerForm.password
                };

                this.$alert('注册成功！欢迎加入', '恭喜', {
                  confirmButtonText: '去登录',
                  type: 'success',
                  center: true,
                  callback: () => {
                    // 将账号密码存入 sessionStorage（临时存储，关闭标签页即清除）
                    sessionStorage.setItem('autoFillLogin', JSON.stringify(loginData));
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
/* 样式保持不变 */
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8edf2 100%);
  padding: 40px 20px;
  position: relative;
}

.bg-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(#c0c4cc 1px, transparent 1px);
  background-size: 30px 30px;
  opacity: 0.3;
}

.register-card {
  width: 520px;
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
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

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

.register-form {
  padding: 0 32px 32px;
}

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

.email-group {
  display: flex;
  gap: 12px;
}

.email-input {
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

::v-deep .el-select .el-input__inner {
  height: 44px;
}

.agreement {
  margin: 20px 0 24px;
  text-align: center;
}

::v-deep .el-checkbox__label {
  color: #606266;
  font-size: 13px;
}

.protocol-link {
  color: #409EFF;
  text-decoration: none;
  margin: 0 3px;
}

.protocol-link:hover {
  text-decoration: underline;
}

.register-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: #409EFF;
  border: none;
  border-radius: 8px;
  transition: all 0.3s;
}

.register-button:hover {
  background: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.register-button:disabled {
  background: #a0cfff;
  transform: none;
  box-shadow: none;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #606266;
}

.login-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.login-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .register-card {
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

  .register-form {
    padding: 0 20px 24px;
  }

  .email-group {
    flex-direction: column;
  }

  .code-btn {
    width: 100%;
  }
}
</style>