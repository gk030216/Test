<template>
  <div class="login-container">
    <div class="bg-pattern"></div>

    <div class="login-card">
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
        <h2 class="title">登录</h2>
        <p class="subtitle">欢迎回来，请登录您的账号</p>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="0" class="login-form">
        <el-form-item prop="account">
          <el-input
              v-model="loginForm.account"
              placeholder="用户名 / 邮箱"
              size="large"
              prefix-icon="el-icon-user"
              clearable
          ></el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="el-icon-lock"
              show-password
              clearable
          ></el-input>
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
          <router-link to="/forget-password" class="forget-link">忘记密码？</router-link>
        </div>

        <el-button
            type="primary"
            @click="handleLogin"
            :loading="loading"
            class="login-button"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>

        <div class="register-link">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-btn">立即注册</router-link>
        </div>
      </el-form>
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
          { min: 3, max: 20, message: '长度在3-20个字符', trigger: 'blur' }
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
    // 检查是否有从注册页或找回密码页传来的自动填充数据
    this.checkAutoFillData();
  },
  methods: {
    // 检查自动填充数据（支持注册页和找回密码页）
    checkAutoFillData() {
      const autoFillData = sessionStorage.getItem('autoFillLogin');
      if (autoFillData) {
        try {
          const data = JSON.parse(autoFillData);
          if (data.account && data.password) {
            this.loginForm.account = data.account;
            this.loginForm.password = data.password;
            // 清除临时存储，避免刷新页面后再次填充
            sessionStorage.removeItem('autoFillLogin');

            // 根据来源显示不同提示
            if (data.source === 'register') {
              this.$message.success('注册成功，请登录');
            } else if (data.source === 'reset') {
              this.$message.success('密码重置成功，请使用新密码登录');
            } else {
              this.$message.info('已自动填充账号密码');
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
            .catch(() => {
              this.loading = false;
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
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8edf2 100%);
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

.login-card {
  width: 440px;
  max-width: 90%;
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
  padding: 40px 40px 24px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}

.logo-svg {
  width: 40px;
  height: 40px;
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

.login-form {
  padding: 30px 40px 40px;
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

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0 28px;
}

.forget-link {
  color: #909399;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.3s;
}

.forget-link:hover {
  color: #409EFF;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: #409EFF;
  border: none;
  border-radius: 8px;
  transition: all 0.3s;
}

.login-button:hover {
  background: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.login-button:active {
  transform: translateY(0);
}

.register-link {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #606266;
}

.register-btn {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
  transition: color 0.3s;
}

.register-btn:hover {
  color: #66b1ff;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .login-card {
    width: 90%;
  }

  .card-header {
    padding: 30px 24px 20px;
  }

  .title {
    font-size: 22px;
  }

  .login-form {
    padding: 24px 24px 30px;
  }
}
</style>