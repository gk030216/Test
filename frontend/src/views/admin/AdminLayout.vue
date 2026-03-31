<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ 'collapsed': isCollapsed }">
      <div class="sidebar-header">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🐾</span>
          <span class="logo-text" v-show="!isCollapsed">宠物服务系统</span>
        </div>
        <div class="collapse-btn" @click="toggleSidebar">
          <i :class="isCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
        </div>
      </div>

      <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :collapse="isCollapsed"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          @select="handleMenuSelect"
      >
        <el-menu-item index="dashboard">
          <i class="el-icon-s-home"></i>
          <span slot="title">仪表盘</span>
        </el-menu-item>

        <el-submenu index="user">
          <template slot="title">
            <i class="el-icon-user"></i>
            <span slot="title">用户管理</span>
          </template>
          <el-menu-item index="user-list">用户列表</el-menu-item>
          <el-menu-item index="staff-list">员工列表</el-menu-item>
        </el-submenu>

        <el-submenu index="service">
          <template slot="title">
            <i class="el-icon-s-order"></i>
            <span slot="title">服务管理</span>
          </template>
          <el-menu-item index="service-list">服务列表</el-menu-item>
          <el-menu-item index="service-category">服务分类</el-menu-item>
          <el-menu-item index="appointment-list">预约管理</el-menu-item>
        </el-submenu>

        <el-submenu index="pet">
          <template slot="title">
            <i class="el-icon-s-custom"></i>
            <span slot="title">宠物档案</span>
          </template>
          <el-menu-item index="pet-list">宠物列表</el-menu-item>
          <el-menu-item index="pet-health">健康记录</el-menu-item>
        </el-submenu>

        <el-submenu index="product">
          <template slot="title">
            <i class="el-icon-goods"></i>
            <span slot="title">商品管理</span>
          </template>
          <el-menu-item index="category-list">
            <i class="el-icon-menu"></i>
            <span slot="title">商品分类</span>
          </el-menu-item>
          <el-menu-item index="product-list">
            <i class="el-icon-goods"></i>
            <span slot="title">商品管理</span>
          </el-menu-item>
          <el-menu-item index="order-list">
            <i class="el-icon-s-order"></i>
            <span slot="title">订单管理</span>
          </el-menu-item>
          <el-menu-item index="comment-list">
            <i class="el-icon-chat-dot-round"></i>
            <span slot="title">评价管理</span>
          </el-menu-item>
          <el-menu-item index="data-analysis">
            <i class="el-icon-data-line"></i>
            <span slot="title">数据分析</span>
          </el-menu-item>
        </el-submenu>

        <el-submenu index="community">
          <template slot="title">
            <i class="el-icon-chat-dot-round"></i>
            <span slot="title">社区管理</span>
          </template>
          <el-menu-item index="post-list">帖子管理</el-menu-item>
          <el-menu-item index="comment-list">评论管理</el-menu-item>
        </el-submenu>

        <el-menu-item index="carousel">
          <i class="el-icon-picture"></i>
          <span slot="title">轮播图管理</span>
        </el-menu-item>

        <el-menu-item index="statistics">
          <i class="el-icon-data-line"></i>
          <span slot="title">数据统计</span>
        </el-menu-item>

        <el-submenu index="ai">
          <template slot="title">
            <i class="el-icon-cpu"></i>
            <span slot="title">AI咨询管理</span>
          </template>
          <el-menu-item index="knowledge-list">
            <i class="el-icon-document"></i>
            <span slot="title">知识库管理</span>
          </el-menu-item>
          <el-menu-item index="faq-list">
            <i class="el-icon-question"></i>
            <span slot="title">常见问题管理</span>
          </el-menu-item>
          <el-menu-item index="chat-history">
            <i class="el-icon-chat-dot-round"></i>
            <span slot="title">对话记录</span>
          </el-menu-item>
        </el-submenu>

        <el-menu-item index="settings">
          <i class="el-icon-setting"></i>
          <span slot="title">系统设置</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部栏 -->
      <div class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>后台管理</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userAvatar">{{ userInitial }}</el-avatar>
              <span>{{ userNickname }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminLayout',
  data() {
    return {
      isCollapsed: false,
      activeMenu: 'dashboard',
      currentTitle: '仪表盘'
    };
  },
  computed: {
    userInfo() {
      const info = localStorage.getItem('userInfo');
      return info ? JSON.parse(info) : null;
    },
    userNickname() {
      return this.userInfo?.nickname || this.userInfo?.username || '管理员';
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
      this.updateActiveMenu();
    }
  },
  mounted() {
    this.updateActiveMenu();
  },
  methods: {
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed;
    },
    handleMenuSelect(index) {
      this.activeMenu = index;
      // 根据菜单更新标题
      const titles = {
        'dashboard': '仪表盘',
        'user-list': '用户列表',
        'staff-list': '员工列表',
        'service-list': '服务列表',
        'service-category': '服务分类',
        'appointment-list': '预约管理',
        'pet-list': '宠物列表',
        'pet-health': '健康记录',
        'product-list': '商品列表',
        'product-category': '商品分类',
        'order-list': '订单管理',
        'post-list': '帖子管理',
        'comment-list': '评论管理',
        'carousel': '轮播图管理',
        'knowledge-list': '知识库管理',
        'faq-list': '常见问题管理',
        'chat-history': '对话记录',
        'statistics': '数据统计',
        'settings': '系统设置'
      };
      this.currentTitle = titles[index] || '管理页面';

      // 跳转到对应的子页面
      this.$router.push(`/admin/${index}`);
    },
    updateActiveMenu() {
      const path = this.$route.path;
      const menu = path.split('/').pop();
      if (menu && menu !== 'admin') {
        this.activeMenu = menu;
        const titles = {
          'dashboard': '仪表盘',
          'user-list': '用户列表',
          'staff-list': '员工列表',
          'service-list': '服务列表',
          'service-category': '服务分类',
          'appointment-list': '预约管理',
          'pet-list': '宠物列表',
          'pet-health': '健康记录',
          'product-list': '商品列表',
          'product-category': '商品分类',
          'order-list': '订单管理',
          'post-list': '帖子管理',
          'comment-list': '评论管理',
          'carousel': '轮播图管理',
          'statistics': '数据统计',
          'settings': '系统设置'
        };
        this.currentTitle = titles[menu] || '管理页面';
      }
    },
    handleUserCommand(command) {
      if (command === 'profile') {
        this.$message.info('个人中心功能开发中');
      } else if (command === 'logout') {
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
      }
    }
  }
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.admin-sidebar {
  width: 220px;
  background: #304156;
  transition: width 0.3s;
  overflow-y: auto;
  overflow-x: hidden;
}

.admin-sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  background: #2b3a4a;
  border-bottom: 1px solid #1f2d3a;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: white;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.collapse-btn {
  cursor: pointer;
  color: #bfcbd9;
  font-size: 18px;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #409EFF;
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f0f2f6;
}

.admin-header {
  height: 60px;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f5f5;
}

.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 滚动条样式 */
.admin-sidebar::-webkit-scrollbar {
  width: 6px;
}

.admin-sidebar::-webkit-scrollbar-track {
  background: #2b3a4a;
}

.admin-sidebar::-webkit-scrollbar-thumb {
  background: #4a5a6a;
  border-radius: 3px;
}

.admin-content::-webkit-scrollbar {
  width: 8px;
}

.admin-content::-webkit-scrollbar-track {
  background: #f0f2f6;
}

.admin-content::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

.admin-content::-webkit-scrollbar-thumb:hover {
  background: #909399;
}
</style>