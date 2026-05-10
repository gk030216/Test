<template>
  <div class="login-page">
    <!-- 左侧品牌展示区 -->
    <div class="login-left">
      <div class="brand-content">
        <!-- 宠物图标 -->
        <div class="brand-icon">
          <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <!-- 背景圆 -->
            <circle cx="60" cy="60" r="56" fill="rgba(255,255,255,0.15)" stroke="rgba(255,255,255,0.35)" stroke-width="2"/>
            <!-- 猫身体 -->
            <ellipse cx="60" cy="72" rx="32" ry="26" fill="rgba(255,255,255,0.92)"/>
            <!-- 耳朵外 -->
            <polygon points="34,50 20,18 48,42" fill="rgba(255,255,255,0.92)"/>
            <polygon points="86,50 100,18 72,42" fill="rgba(255,255,255,0.92)"/>
            <!-- 耳朵内 -->
            <polygon points="36,47 27,26 47,43" fill="#f9c5b6"/>
            <polygon points="84,47 93,26 73,43" fill="#f9c5b6"/>
            <!-- 眼睛 -->
            <ellipse cx="47" cy="66" rx="6" ry="6.5" fill="#3d3d3d"/>
            <ellipse cx="73" cy="66" rx="6" ry="6.5" fill="#3d3d3d"/>
            <ellipse cx="48" cy="64" rx="2.5" ry="3" fill="white"/>
            <ellipse cx="74" cy="64" rx="2.5" ry="3" fill="white"/>
            <!-- 鼻子 -->
            <ellipse cx="60" cy="75" rx="3.5" ry="2.5" fill="#f4a49b"/>
            <!-- 嘴巴 -->
            <path d="M56 77 Q54 82 51 80" stroke="#f4a49b" stroke-width="1.5" fill="none" stroke-linecap="round"/>
            <path d="M64 77 Q66 82 69 80" stroke="#f4a49b" stroke-width="1.5" fill="none" stroke-linecap="round"/>
            <!-- 胡须 -->
            <line x1="24" y1="68" x2="42" y2="71" stroke="rgba(255,255,255,0.6)" stroke-width="1.3" stroke-linecap="round"/>
            <line x1="24" y1="74" x2="42" y2="74" stroke="rgba(255,255,255,0.6)" stroke-width="1.3" stroke-linecap="round"/>
            <line x1="96" y1="68" x2="78" y2="71" stroke="rgba(255,255,255,0.6)" stroke-width="1.3" stroke-linecap="round"/>
            <line x1="96" y1="74" x2="78" y2="74" stroke="rgba(255,255,255,0.6)" stroke-width="1.3" stroke-linecap="round"/>
            <!-- 腮红 -->
            <ellipse cx="38" cy="74" rx="5" ry="3" fill="rgba(244,164,155,0.35)"/>
            <ellipse cx="82" cy="74" rx="5" ry="3" fill="rgba(244,164,155,0.35)"/>
          </svg>
        </div>
        <h1 class="brand-title">喵汪星球</h1>
        <h2 class="brand-subtitle">宠物服务管理平台</h2>
        <p class="brand-desc">一站式宠物服务 · 让陪伴更长久</p>
        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-icon">🛁</span>
            <span>洗护美容预约</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">💉</span>
            <span>疫苗健康管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🛒</span>
            <span>宠物用品商城</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">💬</span>
            <span>宠主交流社区</span>
          </div>
        </div>
      </div>
      <!-- 底部爪印装饰 -->
      <div class="paw-deco">
        <span class="paw p1">🐾</span>
        <span class="paw p2">🐾</span>
        <span class="paw p3">🐾</span>
        <span class="paw p4">🐾</span>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="form-wrapper">
        <div class="form-header">
          <h3 class="form-title">欢迎回来 🐱</h3>
          <p class="form-subtitle">登录您的账号，查看爱宠的一切</p>
        </div>

        <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginForm"
          class="login-form"
          @keyup.enter.native="handleLogin"
        >
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              placeholder="用户名 / 邮箱"
              size="large"
              clearable
            >
              <template #prefix>
                <i class="el-icon-user input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="登录密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <i class="el-icon-lock input-icon"></i>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberPassword" class="remember-check">
              记住账号
            </el-checkbox>
            <router-link to="/forget-password" class="forget-link">
              忘记密码？
            </router-link>
          </div>

          <el-button
            type="primary"
            @click="handleLogin"
            :loading="loading"
            class="login-btn"
            size="large"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>

          <div class="form-footer">
            <span class="no-account">还没有账号？</span>
            <router-link to="/register" class="register-link">
              带宠物来注册 🐾
            </router-link>
          </div>
        </el-form>

        <!-- 三角色快捷提示 -->
        <div class="role-hint">
          <span class="role-hint-title">测试账号：</span>
          <span class="role-tag tag-user">用户</span>
          <span class="role-tag tag-staff">员工</span>
          <span class="role-tag tag-admin">管理员</span>
        </div>
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
      loading: false,
      rules: {
        account: [
          { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
          { min: 3, max: 50, message: '长度3-50个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
        ]
      }
    };
  },
  mounted() {
    this.loadSavedAccount();
    this.checkAutoFillData();
  },
  methods: {
    checkAutoFillData() {
      const autoFillData = sessionStorage.getItem('autoFillLogin');
      if (autoFillData) {
        try {
          const data = JSON.parse(autoFillData);
          if (data.account && data.password) {
            this.loginForm.account = data.account;
            this.loginForm.password = data.password;
            sessionStorage.removeItem('autoFillLogin');
            if (data.source === 'register') {
              this.$message.success('注册成功，请登录');
            } else if (data.source === 'reset') {
              this.$message.success('密码重置成功，请使用新密码登录');
            }
          }
        } catch (e) {
          console.error('解析自动填充数据失败', e);
        }
      }
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
        if (!valid) return;

        this.loading = true;
        login(this.loginForm)
          .then(res => {
            this.loading = false;
            if (res.code === 200) {
              this.loginSuccess(res.data);
            } else {
              this.$message.error(res.message || '登录失败');
            }
          })
          .catch(error => {
            this.loading = false;
            const message =
              error.response?.data?.message ||
              error.message ||
              '登录失败，请稍后重试';
            this.$message.error(message);
          });
      });
    },

    loginSuccess(data) {
      const token = data.token;
      const userData = data.user;

      localStorage.setItem('token', token);
      localStorage.setItem('userInfo', JSON.stringify(userData));

      if (this.rememberPassword) {
        localStorage.setItem('savedAccount', this.loginForm.account);
      } else {
        localStorage.removeItem('savedAccount');
      }

      this.$message.success(`欢迎回来，${userData.nickname || userData.username}！`);

      setTimeout(() => {
        this.$router.push('/');
      }, 1000);
    }
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  font-family: 'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ========== 左侧品牌区 ========== */
.login-left {
  flex: 0 0 46%;
  position: relative;
  background: linear-gradient(160deg, #f59e4b 0%, #f0826a 30%, #e8647c 65%, #d4527e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 装饰光晕 */
.login-left::before {
  content: '';
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%);
  top: -80px;
  right: -100px;
}

.login-left::after {
  content: '';
  position: absolute;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
  bottom: -40px;
  left: -60px;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #fff;
  padding: 40px;
  max-width: 380px;
}

.brand-icon {
  width: 110px;
  height: 110px;
  margin: 0 auto 20px;
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
  font-size: 38px;
  font-weight: 700;
  margin: 0 0 6px;
  letter-spacing: 6px;
  text-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.brand-subtitle {
  font-size: 16px;
  font-weight: 400;
  margin: 0 0 16px;
  opacity: 0.88;
  letter-spacing: 4px;
}

.brand-desc {
  font-size: 13px;
  opacity: 0.72;
  margin: 0 0 32px;
  line-height: 1.6;
}

.feature-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  opacity: 0.9;
  padding: 10px 14px;
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
  font-size: 18px;
  width: 22px;
  text-align: center;
}

/* 爪印底部装饰 */
.paw-deco {
  position: absolute;
  bottom: 32px;
  right: 28px;
  display: flex;
  gap: 10px;
  opacity: 0.25;
  transform: rotate(22deg);
}

.paw {
  font-size: 24px;
  animation: pawFade 2.5s ease-in-out infinite;
}

.p2 { animation-delay: 0.5s; }
.p3 { animation-delay: 1.0s; }
.p4 { animation-delay: 1.5s; }

@keyframes pawFade {
  0%, 100% { opacity: 0.2; transform: scale(0.85); }
  50% { opacity: 0.55; transform: scale(1.1); }
}

/* ========== 右侧登录表单 ========== */
.login-right {
  flex: 0 0 54%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fdf8f5;
}

.form-wrapper {
  width: 420px;
  max-width: 88%;
  animation: formSlideUp 0.5s ease;
}

@keyframes formSlideUp {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #3d2e2a;
  margin: 0 0 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #a08c84;
  margin: 0;
}

.login-form {
  background: #fff;
  padding: 40px 36px;
  border-radius: 16px;
  box-shadow: 0 2px 18px rgba(180,130,100,0.06), 0 8px 36px rgba(180,120,90,0.04);
}

/* 输入框 */
::v-deep .login-form .el-input__inner {
  height: 48px;
  line-height: 48px;
  border-radius: 10px;
  border: 1.5px solid #f0e0d6;
  background: #fefbf9;
  font-size: 15px;
  padding-left: 44px;
  transition: all 0.25s;
}

::v-deep .login-form .el-input__inner:hover {
  border-color: #e8c8b0;
  background: #fff;
}

::v-deep .login-form .el-input__inner:focus {
  border-color: #f59e4b;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(245,158,75,0.1);
}

::v-deep .login-form .el-input__prefix {
  left: 14px;
}

.input-icon {
  font-size: 18px;
  color: #d0b8a8;
  transition: color 0.25s;
}

::v-deep .login-form .el-input.is-focus .input-icon {
  color: #f59e4b;
}

::v-deep .login-form .el-form-item {
  margin-bottom: 22px;
}

::v-deep .login-form .el-form-item__error {
  padding-left: 4px;
  font-size: 12px;
}

/* 选项行 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 6px 0 28px;
}

.remember-check ::v-deep .el-checkbox__label {
  font-size: 13px;
  color: #8c7068;
}

.forget-link {
  font-size: 13px;
  color: #b8a098;
  text-decoration: none;
  transition: color 0.2s;
}

.forget-link:hover {
  color: #f59e4b;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  transition: all 0.3s;
}

.login-btn:hover {
  background: linear-gradient(135deg, #f7b06a, #f2967e);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(245,158,75,0.35);
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(245,158,75,0.25);
}

.login-btn.is-loading {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
}

/* 底部 */
.form-footer {
  text-align: center;
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px solid #f5ece6;
  font-size: 14px;
}

.no-account {
  color: #b8a098;
}

.register-link {
  color: #f0826a;
  font-weight: 600;
  text-decoration: none;
  margin-left: 6px;
  transition: all 0.2s;
}

.register-link:hover {
  color: #e86450;
  text-decoration: underline;
}

/* 角色提示 */
.role-hint {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #c4b0a6;
}

.role-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
  margin-left: 4px;
  font-size: 12px;
}

.tag-user  { background: #fdf2e9; color: #f0826a; }
.tag-staff { background: #eaf7f0; color: #52b788; }
.tag-admin { background: #f0edfc; color: #7c6dd4; }

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .login-left {
    display: none;
  }

  .login-right {
    flex: 1;
    background: linear-gradient(170deg, #fef6f0 0%, #fdf8f5 50%, #fdf9f6 100%);
  }

  .form-wrapper {
    width: 400px;
  }

  .login-form {
    padding: 32px 28px;
  }

  .form-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .form-wrapper {
    width: 92%;
  }

  .login-form {
    padding: 28px 20px;
    border-radius: 12px;
  }
}
</style>
