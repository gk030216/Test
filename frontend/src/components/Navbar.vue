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
        <!-- 消息提醒图标 -->
        <div class="notification-icon" @click="goToNotifications">
          <i class="el-icon-bell"></i>
          <span class="notification-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
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
            <el-dropdown-item command="notifications" divided>
              <i class="el-icon-bell"></i> 消息通知
              <span v-if="unreadCount > 0" class="dropdown-badge">{{ unreadCount }}</span>
            </el-dropdown-item>
            <el-dropdown-item command="feedback" divided>
              <i class="el-icon-edit-outline"></i> 意见反馈
            </el-dropdown-item>
            <el-dropdown-item v-if="userRole === 2" command="staff" divided>
              <i class="el-icon-s-platform"></i> 员工工作台
            </el-dropdown-item>
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

    <!-- 消息通知下拉面板（可选，增强体验） -->
    <div class="notification-dropdown" v-click-outside="closeNotificationPanel" v-show="showNotificationPanel">
      <div class="notification-header">
        <span>消息通知</span>
        <el-button type="text" size="small" @click="markAllAsRead" v-if="unreadCount > 0">全部已读</el-button>
      </div>
      <div class="notification-list" v-loading="notificationLoading">
        <div v-for="item in notificationList" :key="item.id" class="notification-item" :class="{ unread: !item.isRead }" @click="handleNotificationClick(item)">
          <div class="notification-icon">
            <i :class="getNotificationIcon(item.type)"></i>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ item.title }}</div>
            <div class="notification-desc">{{ item.content }}</div>
            <div class="notification-time">{{ formatTime(item.createTime) }}</div>
          </div>
          <div class="notification-close" v-if="!item.isRead" @click.stop="markAsRead(item.id)">
            <i class="el-icon-close"></i>
          </div>
        </div>
        <div v-if="notificationList.length === 0 && !notificationLoading" class="notification-empty">
          <i class="el-icon-bell"></i>
          <span>暂无消息通知</span>
        </div>
      </div>
      <div class="notification-footer" v-if="total > notificationList.length">
        <el-button type="text" size="small" @click="loadMoreNotifications">查看更多</el-button>
      </div>
    </div>
  </nav>
</template>

<script>
import { getUnreadCount, getNotificationList, markAsRead, markAllAsRead } from '@/api/notification';

export default {
  name: 'Navbar',
  directives: {
    // 点击外部关闭下拉面板的指令
    'click-outside': {
      bind(el, binding, vnode) {
        el.clickOutsideEvent = function(event) {
          if (!(el === event.target || el.contains(event.target))) {
            vnode.context[binding.expression]();
          }
        };
        document.body.addEventListener('click', el.clickOutsideEvent);
      },
      unbind(el) {
        document.body.removeEventListener('click', el.clickOutsideEvent);
      }
    }
  },
  data() {
    return {
      mobileMenuOpen: false,
      siteName: '宠物服务系统',
      siteLogo: '',
      // 消息通知相关
      unreadCount: 0,
      showNotificationPanel: false,
      notificationList: [],
      notificationLoading: false,
      notificationPage: 1,
      notificationPageSize: 5,
      total: 0,
      pollingTimer: null
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
      this.showNotificationPanel = false;
    }
  },
  created() {
    this.loadSettings();
  },
  mounted() {
    this.loadUnreadCount();
    this.$bus.$on('settings-loaded', (settings) => {
      if (settings && settings.basic) {
        this.siteName = settings.basic.siteName || '宠物服务系统';
        this.siteLogo = settings.basic.siteLogo || '';
      }
    });
    this.$bus.$on('new-notification', () => {
      this.loadUnreadCount();
    });
    // 定时轮询未读消息数量（每30秒）
    if (this.isLoggedIn) {
      this.pollingTimer = setInterval(() => {
        this.loadUnreadCount();
      }, 30000);
    }
  },
  beforeDestroy() {
    this.$bus.$off('settings-loaded');
    this.$bus.$off('new-notification');
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
    }
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

    // ========== 消息通知相关方法 ==========

    async loadUnreadCount() {
      if (!this.isLoggedIn) return;
      try {
        const res = await getUnreadCount();
        if (res.code === 200) {
          this.unreadCount = res.data || 0;
        }
      } catch (error) {
        console.error('获取未读消息数失败', error);
      }
    },

    async loadNotifications() {
      if (!this.isLoggedIn) return;
      this.notificationLoading = true;
      try {
        const res = await getNotificationList({
          page: this.notificationPage,
          pageSize: this.notificationPageSize
        });
        if (res.code === 200) {
          if (this.notificationPage === 1) {
            this.notificationList = res.data.list || [];
          } else {
            this.notificationList = [...this.notificationList, ...(res.data.list || [])];
          }
          this.total = res.data.total || 0;
        }
      } catch (error) {
        console.error('获取消息列表失败', error);
      } finally {
        this.notificationLoading = false;
      }
    },

    async markAsRead(notificationId) {
      try {
        const res = await markAsRead(notificationId);
        if (res.code === 200) {
          // 更新本地列表状态
          const item = this.notificationList.find(n => n.id === notificationId);
          if (item) {
            item.isRead = 1;
          }
          this.unreadCount = Math.max(0, this.unreadCount - 1);
        }
      } catch (error) {
        console.error('标记已读失败', error);
      }
    },

    async markAllAsRead() {
      try {
        const res = await markAllAsRead();
        if (res.code === 200) {
          this.notificationList.forEach(item => {
            item.isRead = 1;
          });
          this.unreadCount = 0;
          this.$message.success('已全部标记为已读');
        }
      } catch (error) {
        console.error('全部标记已读失败', error);
      }
    },

    loadMoreNotifications() {
      this.notificationPage++;
      this.loadNotifications();
    },

    toggleNotificationPanel() {
      if (!this.isLoggedIn) {
        this.$router.push('/login');
        return;
      }
      this.showNotificationPanel = !this.showNotificationPanel;
      if (this.showNotificationPanel && this.notificationList.length === 0) {
        this.notificationPage = 1;
        this.loadNotifications();
      }
    },

    closeNotificationPanel() {
      this.showNotificationPanel = false;
    },

    goToNotifications() {
      this.$router.push('/personal/notifications');
    },

    handleNotificationClick(item) {
      if (!item.isRead) {
        this.markAsRead(item.id);
      }
      // 根据类型跳转到不同页面
      switch (item.type) {
        case 'appointment':
          this.$router.push('/personal/appointments');
          break;
        case 'order':
          this.$router.push('/personal/orders');
          break;
        case 'comment':
          this.$router.push('/personal/comments');
          break;
        case 'system':
        default:
          this.$router.push('/personal/notifications');
          break;
      }
      this.showNotificationPanel = false;
    },

    getNotificationIcon(type) {
      const iconMap = {
        appointment: 'el-icon-s-order',
        order: 'el-icon-shopping-cart-2',
        comment: 'el-icon-chat-dot-round',
        system: 'el-icon-info'
      };
      return iconMap[type] || 'el-icon-bell';
    },

    formatTime(date) {
      if (!date) return '';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
      return `${d.getMonth() + 1}/${d.getDate()}`;
    },

    // ========== 导航方法 ==========

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

    handleUserMenuCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/personal');
          break;
        case 'notifications':
          this.$router.push('/personal/notifications');
          break;
        case 'staff':
          this.$router.push('/staff');
          break;
        case 'feedback':
          this.$router.push('/personal/feedback');
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
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
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

/* 消息提醒图标样式 */
.notification-icon {
  position: relative;
  color: white;
  font-size: 22px;
  cursor: pointer;
  padding: 6px;
  border-radius: 50%;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-icon:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.05);
}

.notification-badge {
  position: absolute;
  top: -2px;
  right: -4px;
  background: #ff6b6b;
  color: white;
  font-size: 10px;
  font-weight: bold;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.3);
}

/* 消息下拉面板 */
.notification-dropdown {
  position: absolute;
  top: 70px;
  right: 100px;
  width: 360px;
  max-height: 450px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
  color: #333;
}

.notification-header .el-button {
  color: #667eea;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  max-height: 350px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.3s;
}

.notification-item:hover {
  background: #f8f9fc;
}

.notification-item.unread {
  background: #f0f7ff;
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-icon i {
  font-size: 18px;
  color: #667eea;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  font-size: 14px;
}

.notification-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-time {
  font-size: 11px;
  color: #ccc;
}

.notification-close {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #999;
  cursor: pointer;
  flex-shrink: 0;
}

.notification-close:hover {
  background: #f0f0f0;
  color: #f56c6c;
}

.notification-empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.notification-empty i {
  font-size: 48px;
  margin-bottom: 12px;
  display: block;
}

.notification-footer {
  padding: 10px 16px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

.notification-footer .el-button {
  color: #667eea;
}

.dropdown-badge {
  display: inline-block;
  background: #ff6b6b;
  color: white;
  font-size: 10px;
  border-radius: 10px;
  padding: 0 6px;
  margin-left: 8px;
  line-height: 16px;
}

/* 其他原有样式保持不变 */
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

  .notification-dropdown {
    right: 60px;
    width: 320px;
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

  .notification-icon {
    font-size: 20px;
  }

  .notification-dropdown {
    position: fixed;
    top: 70px;
    right: 10px;
    left: 10px;
    width: auto;
    max-width: none;
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