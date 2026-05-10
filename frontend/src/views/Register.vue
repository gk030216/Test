<template>
  <div class="register-page">
    <!-- 左侧品牌区（与登录页统一） -->
    <div class="register-left">
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
            <line x1="24" y1="68" x2="42" y2="71" stroke="rgba(255,255,255,0.6)" stroke-width="1.3" stroke-linecap="round"/>
            <line x1="24" y1="74" x2="42" y2="74" stroke="rgba(255,255,255,0.6)" stroke-width="1.3" stroke-linecap="round"/>
            <line x1="96" y1="68" x2="78" y2="71" stroke="rgba(255,255,255,0.6)" stroke-width="1.3" stroke-linecap="round"/>
            <line x1="96" y1="74" x2="78" y2="74" stroke="rgba(255,255,255,0.6)" stroke-width="1.3" stroke-linecap="round"/>
            <ellipse cx="38" cy="74" rx="5" ry="3" fill="rgba(244,164,155,0.35)"/>
            <ellipse cx="82" cy="74" rx="5" ry="3" fill="rgba(244,164,155,0.35)"/>
          </svg>
        </div>
        <h1 class="brand-title">喵汪星球</h1>
        <h2 class="brand-subtitle">宠物服务管理平台</h2>
        <p class="brand-desc">加入我们，给爱宠最好的照顾</p>
        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-icon">🐱</span>
            <span>建立宠物档案</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">📅</span>
            <span>在线预约服务</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">💉</span>
            <span>疫苗健康提醒</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🏪</span>
            <span>宠物用品选购</span>
          </div>
        </div>
      </div>
      <div class="paw-deco">
        <span class="paw p1">🐾</span>
        <span class="paw p2">🐾</span>
        <span class="paw p3">🐾</span>
        <span class="paw p4">🐾</span>
      </div>
    </div>

    <!-- 右侧注册表单 -->
    <div class="register-right">
      <div class="form-wrapper">
        <div class="form-header">
          <h3 class="form-title">创建账号 🐶</h3>
          <p class="form-subtitle">注册喵汪星球，开启宠物服务之旅</p>
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

        <el-form :model="registerForm" :rules="rules" ref="registerForm" label-width="0" class="register-form">
          <!-- 头像上传 -->
          <div class="avatar-section">
            <div class="avatar-upload-wrapper" @click="triggerUpload">
              <div class="avatar-preview">
                <el-avatar :size="72" :src="avatarPreview" class="preview-avatar">
                  {{ !avatarPreview ? getInitial() : '' }}
                </el-avatar>
                <div class="upload-mask">
                  <i class="el-icon-camera"></i>
                  <span>上传头像</span>
                </div>
              </div>
              <input
                type="file"
                ref="avatarInput"
                accept="image/jpeg,image/png,image/jpg"
                style="display: none"
                @change="handleAvatarUpload"
              />
            </div>
            <p class="avatar-tip">支持 JPG、PNG，≤2MB（选填）</p>
          </div>

          <!-- 用户名 -->
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名（3-20个字符）"
              size="large"
              clearable
            >
              <template #prefix>
                <i class="el-icon-user input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <!-- 昵称 -->
          <el-form-item prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="昵称"
              size="large"
              clearable
            >
              <template #prefix>
                <i class="el-icon-edit input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <!-- 邮箱 + 验证码 -->
          <el-form-item prop="email">
            <div class="email-group">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱地址"
                size="large"
                clearable
                class="email-input"
              >
                <template #prefix>
                  <i class="el-icon-message input-icon"></i>
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

          <!-- 验证码 -->
          <el-form-item prop="code">
            <el-input
              v-model="registerForm.code"
              placeholder="请输入6位验证码"
              size="large"
              maxlength="6"
              clearable
            >
              <template #prefix>
                <i class="el-icon-key input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码（6-20位）"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <i class="el-icon-lock input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <i class="el-icon-circle-check input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <!-- 手机号 -->
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="手机号"
              size="large"
              clearable
            >
              <template #prefix>
                <i class="el-icon-phone input-icon"></i>
              </template>
            </el-input>
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
            class="register-btn"
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
  </div>
</template>

<script>
import { register, checkUsername, checkEmail, sendRegisterCode } from '@/api/user';
import { uploadAvatar } from '@/api/upload';

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
          callback(new Error('用户名已存在'));
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
          callback(new Error('邮箱已被注册'));
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
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

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
        gender: '保密',
        avatar: ''
      },
      avatarPreview: '',
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
      return this.registerForm.email &&
        !this.codeSending &&
        this.countdown === 60 &&
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.registerForm.email);
    },
    codeBtnText() {
      if (this.countdown < 60) {
        return `${this.countdown}秒后重试`;
      }
      return '获取验证码';
    }
  },
  methods: {
    getInitial() {
      const name = this.registerForm.nickname || this.registerForm.username;
      if (!name) return '🐾';
      return name.charAt(0).toUpperCase();
    },

    triggerUpload() {
      this.$refs.avatarInput.click();
    },

    async handleAvatarUpload(event) {
      const file = event.target.files[0];
      if (!file) return;

      const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
      if (!allowedTypes.includes(file.type)) {
        this.$message.error('只支持 JPG、PNG 格式的图片');
        return;
      }

      if (file.size > 2 * 1024 * 1024) {
        this.$message.error('图片大小不能超过 2MB');
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        this.avatarPreview = e.target.result;
      };
      reader.readAsDataURL(file);

      const formData = new FormData();
      formData.append('file', file);

      try {
        const res = await uploadAvatar(formData);
        if (res.code === 200) {
          this.registerForm.avatar = res.data.url;
          this.$message.success('头像上传成功');
        } else {
          this.$message.error(res.message || '上传失败');
          this.avatarPreview = '';
        }
      } catch (error) {
        console.error('头像上传失败', error);
        this.$message.error('上传失败，请稍后重试');
        this.avatarPreview = '';
      }
      event.target.value = '';
    },

    sendCode() {
      this.$refs.registerForm.validateField('email', errorMsg => {
        if (errorMsg) {
          this.$message.warning('请先填写正确的邮箱地址');
          return;
        }

        this.codeSending = true;
        this.startCountdown();

        sendRegisterCode(this.registerForm.email)
          .then(() => {
            this.$message.success('验证码已发送，请查收邮件');
          })
          .catch(() => {
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

              const loginData = {
                account: this.registerForm.username,
                password: this.registerForm.password
              };

              this.$alert('注册成功！欢迎加入喵汪星球 🎉', '恭喜', {
                confirmButtonText: '去登录',
                type: 'success',
                center: true,
                callback: () => {
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
.register-page {
  min-height: 100vh;
  display: flex;
  font-family: 'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ========== 左侧品牌区 ========== */
.register-left {
  flex: 0 0 40%;
  position: relative;
  background: linear-gradient(160deg, #f59e4b 0%, #f0826a 30%, #e8647c 65%, #d4527e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.register-left::before {
  content: '';
  position: absolute;
  width: 380px;
  height: 380px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%);
  top: -60px;
  right: -80px;
}

.register-left::after {
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
  margin: 0 0 6px;
  letter-spacing: 6px;
  text-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.brand-subtitle {
  font-size: 15px;
  font-weight: 400;
  margin: 0 0 14px;
  opacity: 0.88;
  letter-spacing: 4px;
}

.brand-desc {
  font-size: 13px;
  opacity: 0.72;
  margin: 0 0 28px;
  line-height: 1.6;
}

.feature-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  opacity: 0.9;
  padding: 9px 12px;
  background: rgba(255,255,255,0.12);
  border-radius: 10px;
  backdrop-filter: blur(4px);
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255,255,255,0.22);
  transform: translateX(3px);
}

.feature-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

/* 爪印 */
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
.p4 { animation-delay: 1.5s; }

@keyframes pawFade {
  0%, 100% { opacity: 0.2; transform: scale(0.85); }
  50% { opacity: 0.55; transform: scale(1.1); }
}

/* ========== 右侧表单区 ========== */
.register-right {
  flex: 0 0 60%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fdf8f5;
  padding: 40px 20px;
}

.form-wrapper {
  width: 500px;
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

/* 注册表单 */
.register-form {
  background: #fff;
  padding: 32px 30px;
  border-radius: 16px;
  box-shadow: 0 2px 18px rgba(180,130,100,0.06), 0 8px 36px rgba(180,120,90,0.04);
}

/* 头像上传 */
.avatar-section {
  text-align: center;
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5ece6;
}

.avatar-upload-wrapper {
  display: inline-block;
  cursor: pointer;
}

.avatar-preview {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(180,120,90,0.15);
  transition: all 0.3s;
  margin: 0 auto;
}

.avatar-preview:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(245,158,75,0.3);
}

.preview-avatar {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #fef0e8 0%, #fde0d4 100%);
  color: #f0826a;
  font-weight: 600;
  font-size: 28px;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.55);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 50%;
}

.avatar-preview:hover .upload-mask {
  opacity: 1;
}

.upload-mask i {
  font-size: 18px;
  margin-bottom: 3px;
}

.upload-mask span {
  font-size: 11px;
}

.avatar-tip {
  font-size: 12px;
  color: #b8a098;
  margin-top: 8px;
  margin-bottom: 0;
}

/* 输入框 */
::v-deep .register-form .el-input__inner {
  height: 44px;
  line-height: 44px;
  border-radius: 10px;
  border: 1.5px solid #f0e0d6;
  background: #fefbf9;
  font-size: 14px;
  padding-left: 40px;
  transition: all 0.25s;
}

::v-deep .register-form .el-input__inner:hover {
  border-color: #e8c8b0;
  background: #fff;
}

::v-deep .register-form .el-input__inner:focus {
  border-color: #f59e4b;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(245,158,75,0.1);
}

::v-deep .register-form .el-input__prefix {
  left: 12px;
}

.input-icon {
  font-size: 17px;
  color: #d0b8a8;
  transition: color 0.25s;
}

::v-deep .register-form .el-input.is-focus .input-icon {
  color: #f59e4b;
}

::v-deep .register-form .el-form-item {
  margin-bottom: 18px;
}

::v-deep .register-form .el-form-item__error {
  padding-left: 4px;
  font-size: 12px;
}

/* 邮箱 + 验证码 */
.email-group {
  display: flex;
  gap: 10px;
}

.email-input {
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

.code-btn.is-disabled span,
.code-btn:disabled span {
  color: #fff;
}

/* 下拉框 */
::v-deep .register-form .el-select .el-input__inner {
  height: 44px;
}

/* 协议 */
.agreement {
  margin: 18px 0 22px;
  text-align: center;
}

::v-deep .el-checkbox__label {
  color: #8c7068;
  font-size: 13px;
}

.protocol-link {
  color: #f0826a;
  text-decoration: none;
  margin: 0 3px;
}

.protocol-link:hover {
  text-decoration: underline;
}

/* 注册按钮 */
.register-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  transition: all 0.3s;
}

.register-btn:hover {
  background: linear-gradient(135deg, #f7b06a, #f2967e);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(245,158,75,0.35);
}

.register-btn:disabled {
  background: #f5d5b8;
  transform: none;
  box-shadow: none;
}

.register-btn.is-loading {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
}

/* 底部 */
.form-footer {
  text-align: center;
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid #f5ece6;
  font-size: 14px;
  color: #8c7068;
}

.login-link {
  color: #f0826a;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
}

.login-link:hover {
  text-decoration: underline;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .register-left {
    display: none;
  }

  .register-right {
    flex: 1;
    background: linear-gradient(170deg, #fef6f0 0%, #fdf8f5 50%, #fdf9f6 100%);
  }

  .form-wrapper {
    width: 460px;
  }

  .register-form {
    padding: 28px 24px;
  }

  .form-title {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .form-wrapper {
    width: 94%;
  }

  .register-form {
    padding: 24px 18px;
    border-radius: 12px;
  }

  .email-group {
    flex-direction: column;
  }

  .code-btn {
    width: 100%;
  }

  .avatar-preview {
    width: 64px;
    height: 64px;
  }

  .preview-avatar {
    font-size: 24px;
  }
}
</style>
