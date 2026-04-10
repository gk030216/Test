<template>
  <nav class="navbar">
    <div class="navbar-container">
      <!-- Logo区域 - 使用系统设置 -->
      <div class="logo" @click="goToHome">
        <img v-if="siteLogo" :src="siteLogo" class="logo-img" alt="Logo">
        <span class="logo-icon" v-else>🐾</span>
        <span class="logo-text">{{ siteName }}</span>
      </div>

      <!-- 导航菜单（PC端） -->
      <div class="nav-menu" :class="{ 'active': mobileMenuOpen }">
        <div class="nav-item" @click="goToHome">
          <i class="el-icon-s-home"></i>
          <span>首页</span>
        </div>
        <div class="nav-item" @click="goToServices">
          <i class="el-icon-service"></i>
          <span>服务预约</span>
        </div>
        <div class="nav-item" @click="goToShop">
          <i class="el-icon-shopping-cart-2"></i>
          <span>宠物商城</span>
        </div>
        <div class="nav-item" @click="goToCommunity">
          <i class="el-icon-chat-dot-round"></i>
          <span>宠物社区</span>
        </div>
        <div class="nav-item" @click="goToMyPets">
          <i class="el-icon-s-custom"></i>
          <span>我的宠物</span>
        </div>
        <div class="nav-item" @click="goToAIChat">
          <i class="el-icon-cpu"></i>
          <span>AI咨询</span>
        </div>
      </div>

      <!-- 右侧用户区域 -->
      <div class="user-area">
        <!-- 购物车图标 -->
        <div class="cart-icon" @click="goToCart">
          <i class="el-icon-shopping-cart-2"></i>
          <span class="cart-badge" v-if="cartCount > 0">{{ cartCount }}</span>
        </div>

        <!-- 未登录状态 -->
        <div v-if="!isLoggedIn" class="auth-buttons">
          <router-link to="/login" class="login-btn">登录</router-link>
          <router-link to="/register" class="register-btn">注册</router-link>
        </div>

        <!-- 已登录状态 -->
        <el-dropdown v-else trigger="click" class="user-dropdown" @command="handleUserMenuCommand">
          <div class="user-info">
            <el-avatar :size="40" :src="userAvatar" class="user-avatar">
              {{ userInitial }}
            </el-avatar>
            <span class="username">{{ userNickname }}</span>
            <i class="el-icon-arrow-down"></i>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="profile">
              <i class="el-icon-user"></i> 个人中心
            </el-dropdown-item>
<!--            <el-dropdown-item command="appointments">-->
<!--              <i class="el-icon-s-order"></i> 我的预约-->
<!--            </el-dropdown-item>-->
<!--            <el-dropdown-item command="orders">-->
<!--              <i class="el-icon-s-order"></i> 我的订单-->
<!--            </el-dropdown-item>-->
<!--            <el-dropdown-item command="favorites">-->
<!--              <i class="el-icon-star-on"></i> 我的收藏-->
<!--            </el-dropdown-item>-->
<!--            <el-dropdown-item command="posts">-->
<!--              <i class="el-icon-document"></i> 我的帖子-->
<!--            </el-dropdown-item>-->
<!--            <el-dropdown-item command="comments">-->
<!--              <i class="el-icon-chat-dot-round"></i> 我的评论-->
<!--            </el-dropdown-item>-->
<!--            <el-dropdown-item command="address">-->
<!--              <i class="el-icon-location"></i> 收货地址-->
<!--            </el-dropdown-item>-->
<!--            <el-dropdown-item command="security">-->
<!--              <i class="el-icon-lock"></i> 账号安全-->
<!--            </el-dropdown-item>-->
            <!-- 员工显示员工工作台 -->
            <el-dropdown-item v-if="userRole === 2" command="staff" divided>
              <i class="el-icon-s-platform"></i> 员工工作台
            </el-dropdown-item>
            <!-- 管理员显示后台管理 -->
            <el-dropdown-item v-if="userRole === 3" command="admin" divided>
              <i class="el-icon-setting"></i> 后台管理
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <i class="el-icon-switch-button"></i> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

        <!-- 移动端菜单按钮 -->
        <div class="mobile-menu-btn" @click="toggleMobileMenu">
          <i :class="mobileMenuOpen ? 'el-icon-close' : 'el-icon-menu'"></i>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
import { getCartSummary } from '@/api/cart';

export default {
  name: 'Navbar',
  data() {
    return {
      mobileMenuOpen: false,
      cartCount: 0,
      siteName: '宠物服务系统',
      siteLogo: ''
    };
  },
  computed: {
    isLoggedIn() {
      return !!localStorage.getItem('token');
    },
    userInfo() {
      const info = localStorage.getItem('userInfo');
      return info ? JSON.parse(info) : null;
    },
    userRole() {
      return this.userInfo?.role || 1;
    },
    userNickname() {
      return this.userInfo?.nickname || this.userInfo?.username || '用户';
    },
    userAvatar() {
      return this.userInfo?.avatar || '';
    },
    userInitial() {
      return this.userNickname.charAt(0).toUpperCase();
    }
  },
  watch: {
    '$route'() {
      this.mobileMenuOpen = false;
    }
  },
  created() {
    this.loadSettings();
  },
  mounted() {
    this.loadCartCount();
    this.$bus.$on('cart-updated', this.loadCartCount);
    this.$bus.$on('settings-loaded', (settings) => {
      if (settings && settings.basic) {
        this.siteName = settings.basic.siteName || '宠物服务系统';
        this.siteLogo = settings.basic.siteLogo || '';
      }
    });
  },
  beforeDestroy() {
    this.$bus.$off('cart-updated', this.loadCartCount);
    this.$bus.$off('settings-loaded');
  },
  methods: {
    loadSettings() {
      const settings = localStorage.getItem('systemSettings');
      if (settings) {
        try {
          const basic = JSON.parse(settings).basic;
          this.siteName = basic.siteName || '宠物服务系统';
          this.siteLogo = basic.siteLogo || '';
        } catch (e) {
          console.error('解析系统设置失败', e);
        }
      }
    },
    goToHome() {
      this.$router.push('/');
    },
    goToServices() {
      this.$router.push('/services');
    },
    goToShop() {
      this.$router.push('/shop');
    },
    goToCommunity() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.$confirm('请先登录，才能访问宠物社区', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login');
        }).catch(() => {});
        return;
      }
      this.$router.push('/community');
    },
    goToMyPets() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.$confirm('请先登录，才能管理宠物档案', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login');
        }).catch(() => {});
        return;
      }
      this.$router.push('/my-pets');
    },
    goToAIChat() {
      this.$router.push('/ai-chat');
    },
    goToCart() {
      this.$router.push('/cart');
    },
    async loadCartCount() {
      try {
        if (this.isLoggedIn) {
          const res = await getCartSummary();
          if (res.code === 200) {
            this.cartCount = res.data.totalCount || 0;
          }
        } else {
          const cart = JSON.parse(localStorage.getItem('cart') || '[]');
          this.cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);
        }
      } catch (error) {
        console.error('加载购物车数量失败', error);
      }
    },
    handleUserMenuCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/personal');
          break;
        case 'appointments':
          this.$router.push('/personal/appointments');
          break;
        case 'orders':
          this.$router.push('/personal/orders');
          break;
        case 'favorites':
          this.$router.push('/personal/favorites');
          break;
        case 'posts':
          this.$router.push('/personal/posts');
          break;
        case 'comments':
          this.$router.push('/personal/comments');
          break;
        case 'address':
          this.$router.push('/personal/address');
          break;
        case 'security':
          this.$router.push('/personal/security');
          break;
        case 'staff':
          this.$router.push('/staff');
          break;
        case 'admin':
          this.$router.push('/admin');
          break;
        case 'logout':
          this.handleLogout();
          break;
      }
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        this.$message.success('已退出登录');
        this.$router.push('/login');
      }).catch(() => {});
    },
    toggleMobileMenu() {
      this.mobileMenuOpen = !this.mobileMenuOpen;
    }
  }
};
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: 70px;
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-img {
  height: 36px;
  width: auto;
}

.logo-icon {
  font-size: 32px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: white;
  letter-spacing: 1px;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  margin-left: 50px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  border-radius: 40px;
  transition: all 0.3s;
  font-size: 15px;
  cursor: pointer;
}

.nav-item i {
  font-size: 18px;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  transform: translateY(-2px);
}

.user-area {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cart-icon {
  position: relative;
  color: white;
  font-size: 24px;
  text-decoration: none;
  transition: transform 0.3s;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.cart-icon:hover {
  transform: scale(1.1);
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -12px;
  background: #ff6b6b;
  color: white;
  font-size: 12px;
  font-weight: bold;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

.login-btn, .register-btn {
  padding: 8px 20px;
  border-radius: 40px;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.login-btn {
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: white;
}

.register-btn {
  background: white;
  color: #667eea;
  font-weight: 500;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px;
  border-radius: 40px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.2);
}

.user-avatar {
  background: rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: bold;
}

.username {
  color: white;
  font-size: 14px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info i {
  color: white;
  font-size: 12px;
}

.mobile-menu-btn {
  display: none;
  font-size: 24px;
  color: white;
  cursor: pointer;
  margin-left: 15px;
}

@media (max-width: 1024px) {
  .nav-menu {
    margin-left: 20px;
  }

  .nav-item span {
    display: none;
  }

  .nav-item i {
    font-size: 20px;
  }

  .nav-item {
    padding: 8px 12px;
  }
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 0 15px;
  }

  .logo-text {
    display: none;
  }

  .nav-menu {
    position: fixed;
    top: 70px;
    left: -100%;
    width: 100%;
    height: calc(100vh - 70px);
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    flex-direction: column;
    align-items: stretch;
    padding: 20px;
    transition: left 0.3s ease;
    margin-left: 0;
    overflow-y: auto;
    z-index: 999;
  }

  .nav-menu.active {
    left: 0;
  }

  .nav-item {
    padding: 12px 16px;
    justify-content: center;
  }

  .nav-item span {
    display: inline;
  }

  .mobile-menu-btn {
    display: block;
  }

  .username {
    display: none;
  }

  .cart-icon {
    font-size: 22px;
  }

  .auth-buttons {
    gap: 8px;
  }

  .login-btn, .register-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>