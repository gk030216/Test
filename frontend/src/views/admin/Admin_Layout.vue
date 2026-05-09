<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ collapsed: isCollapsed }">
      <!-- Logo 区域 -->
      <div class="sidebar-logo" @click="$router.push('/')">
        <div class="logo-icon-wrap">
          <span class="logo-icon">🐾</span>
        </div>
        <transition name="fade">
          <div class="logo-info" v-show="!isCollapsed">
            <span class="logo-title">PetAdmin</span>
            <span class="logo-subtitle">后台管理系统</span>
          </div>
        </transition>
      </div>

      <!-- 菜单区域 -->
      <div class="sidebar-menu-wrap">
        <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            :collapse="isCollapsed"
            :unique-opened="true"
            background-color="transparent"
            text-color="rgba(255,255,255,0.65)"
            active-text-color="#ffffff"
            @select="handleMenuSelect"
        >
          <!-- 仪表盘 -->
          <el-menu-item index="dashboard">
            <i class="el-icon-s-home"></i>
            <span slot="title">仪表盘</span>
          </el-menu-item>

          <!-- 用户管理 -->
          <el-submenu index="user">
            <template slot="title">
              <i class="el-icon-user-solid"></i>
              <span>用户管理</span>
            </template>
            <el-menu-item index="user-list">用户列表</el-menu-item>
            <el-menu-item index="staff-list">员工列表</el-menu-item>
            <el-menu-item index="user-analysis">用户分析</el-menu-item>
          </el-submenu>

          <!-- 宠物管理 -->
          <el-submenu index="pet">
            <template slot="title">
              <i class="el-icon-s-custom"></i>
              <span>宠物管理</span>
            </template>
            <el-menu-item index="pet-list">宠物列表</el-menu-item>

            <el-menu-item index="health-record">体检记录</el-menu-item>
            <el-menu-item index="vaccine-record">疫苗记录</el-menu-item>
            <el-menu-item index="pet-analysis">宠物分析</el-menu-item>
          </el-submenu>

          <!-- 服务管理 -->
          <el-submenu index="service">
            <template slot="title">
              <i class="el-icon-s-cooperation"></i>
              <span>服务管理</span>
            </template>
            <el-menu-item index="service-category">服务分类</el-menu-item>
            <el-menu-item index="service-list">服务列表</el-menu-item>
            <el-menu-item index="appointment-list">预约管理</el-menu-item>
            <el-menu-item index="appointment-time">预约时间</el-menu-item>
            <el-menu-item index="service-comment-list">服务评价</el-menu-item>
            <el-menu-item index="service-analysis">服务分析</el-menu-item>
          </el-submenu>

          <!-- 商城管理 -->
          <el-submenu index="shop">
            <template slot="title">
              <i class="el-icon-shopping-cart-2"></i>
              <span>商城管理</span>
            </template>
            <el-menu-item index="category-list">商品分类</el-menu-item>
            <el-menu-item index="product-list">商品列表</el-menu-item>
            <el-menu-item index="comment-list">商品评价</el-menu-item>
            <el-menu-item index="product-analysis">商品分析</el-menu-item>
          </el-submenu>

          <!-- 订单管理 -->
          <el-submenu index="order">
            <template slot="title">
              <i class="el-icon-s-order"></i>
              <span>订单管理</span>
            </template>
            <el-menu-item index="order-list">订单列表</el-menu-item>
          </el-submenu>

          <!-- 库存管理 -->
          <el-submenu index="inventory">
            <template slot="title">
              <i class="el-icon-s-management"></i>
              <span>库存管理</span>
            </template>
            <el-menu-item index="product-stock">商品库存</el-menu-item>
            <el-menu-item index="vaccine-stock">疫苗库存</el-menu-item>
          </el-submenu>

          <!-- 社区管理 -->
          <el-submenu index="community">
            <template slot="title">
              <i class="el-icon-chat-dot-square"></i>
              <span>社区管理</span>
            </template>
            <el-menu-item index="post-manage">帖子管理</el-menu-item>
            <el-menu-item index="comment-manage">评论管理</el-menu-item>
          </el-submenu>

          <!-- AI 管理 -->
          <el-submenu index="ai">
            <template slot="title">
              <i class="el-icon-cpu"></i>
              <span>AI 管理</span>
            </template>
            <el-menu-item index="faq-list">常见问题</el-menu-item>
            <el-menu-item index="knowledge-list">知识库管理</el-menu-item>
            <el-menu-item index="chat-history">对话记录</el-menu-item>
            <el-menu-item index="user-chat-history">用户对话</el-menu-item>
          </el-submenu>

          <!-- 内容管理 -->
          <el-submenu index="content">
            <template slot="title">
              <i class="el-icon-picture-outline"></i>
              <span>内容管理</span>
            </template>
            <el-menu-item index="carousel">轮播图管理</el-menu-item>
            <el-menu-item index="notice-manage">公告管理</el-menu-item>
          </el-submenu>

          <!-- 系统管理 -->
          <el-submenu index="system">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>系统管理</span>
            </template>
            <el-menu-item index="feedback">异常反馈</el-menu-item>
            <el-menu-item index="settings">系统设置</el-menu-item>
          </el-submenu>
        </el-menu>
      </div>

      <!-- 侧边栏底部信息 -->
      <div class="sidebar-footer" v-show="!isCollapsed">
        <div class="version-info">
          <span class="version-dot"></span>
          <span>v1.0.0</span>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部导航栏 -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <!-- 折叠按钮 -->
          <span class="collapse-trigger" @click="toggleSidebar">
            <i :class="isCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
          </span>
          <!-- 面包屑 -->
          <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item><i class="el-icon-s-home"></i></el-breadcrumb-item>
            <el-breadcrumb-item>后台管理</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="topbar-right">
          <!-- 全屏切换 -->
          <span class="topbar-action" title="全屏" @click="toggleFullscreen">
            <i :class="isFullscreen ? 'el-icon-close' : 'el-icon-full-screen'"></i>
          </span>

          <!-- 通知 -->
          <el-badge :value="unreadNoticeCount" :hidden="unreadNoticeCount === 0" class="topbar-badge">
            <span class="topbar-action" title="消息通知" @click="$router.push('/admin/notice-manage')">
              <i class="el-icon-bell"></i>
            </span>
          </el-badge>

          <!-- 用户下拉 -->
          <el-dropdown @command="handleUserCommand" trigger="click">
            <div class="topbar-user">
              <el-avatar :size="34" :src="userAvatar" class="user-avatar">
                {{ userInitial }}
              </el-avatar>
              <span class="user-name">{{ userNickname }}</span>
              <el-tag size="mini" type="warning" effect="plain" class="role-tag">管理员</el-tag>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <div class="dropdown-user-header">
                <el-avatar :size="44" :src="userAvatar">{{ userInitial }}</el-avatar>
                <div class="dropdown-user-info">
                  <div class="dropdown-user-name">{{ userNickname }}</div>
                  <div class="dropdown-user-role">超级管理员</div>
                </div>
              </div>
              <el-dropdown-item command="profile" divided>
                <i class="el-icon-user"></i> 个人资料
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <i class="el-icon-setting"></i> 系统设置
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <i class="el-icon-switch-button"></i> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面标签栏 -->
      <div class="page-tags" v-if="visitedPages.length > 0">
        <div class="tags-scroll">
          <span
              v-for="page in visitedPages"
              :key="page.index"
              :class="['tag-item', { active: activeMenu === page.index }]"
              @click="switchToPage(page)"
          >
            <i :class="page.icon || 'el-icon-document'"></i>
            {{ page.title }}
            <i
                class="el-icon-close tag-close"
                v-if="page.index !== 'dashboard'"
                @click.stop="closePage(page)"
            ></i>
          </span>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Admin_Layout',
  data() {
    return {
      isCollapsed: false,
      isFullscreen: false,
      activeMenu: 'dashboard',
      currentTitle: '仪表盘',
      unreadNoticeCount: 0,
      visitedPages: [
        { index: 'dashboard', title: '仪表盘', icon: 'el-icon-s-home' }
      ],
      titleMap: {
        'dashboard': '仪表盘',
        'user-list': '用户列表',
        'staff-list': '员工列表',
        'user-analysis': '用户分析',
        'pet-list': '宠物列表',

        'health-record': '体检记录',
        'vaccine-record': '疫苗记录',
        'pet-analysis': '宠物分析',
        'service-category': '服务分类',
        'service-list': '服务列表',
        'appointment-list': '预约管理',
        'appointment-time': '预约时间',
        'service-comment-list': '服务评价',
        'service-analysis': '服务分析',
        'category-list': '商品分类',
        'product-list': '商品列表',
        'order-list': '订单管理',
        'comment-list': '商品评价',
        'product-analysis': '商品分析',
        'product-stock': '商品库存',
        'vaccine-stock': '疫苗库存',
        'post-manage': '帖子管理',
        'comment-manage': '评论管理',
        'user-chat-history': '用户对话',
        'knowledge-list': '知识库管理',
        'faq-list': '常见问题',
        'chat-history': '对话记录',
        'carousel': '轮播图管理',
        'notice-manage': '公告管理',
        'feedback': '异常反馈',
        'settings': '系统设置'
      },
      iconMap: {
        'dashboard': 'el-icon-s-home',
        'user-list': 'el-icon-user-solid',
        'staff-list': 'el-icon-s-custom',
        'user-analysis': 'el-icon-data-line',
        'pet-list': 'el-icon-menu',

        'health-record': 'el-icon-document',
        'vaccine-record': 'el-icon-first-aid-kit',
        'pet-analysis': 'el-icon-data-line',
        'service-category': 'el-icon-folder',
        'service-list': 'el-icon-s-cooperation',
        'appointment-list': 'el-icon-s-order',
        'appointment-time': 'el-icon-time',
        'service-comment-list': 'el-icon-star-on',
        'service-analysis': 'el-icon-data-analysis',
        'category-list': 'el-icon-menu',
        'product-list': 'el-icon-goods',
        'order-list': 'el-icon-s-order',
        'comment-list': 'el-icon-chat-dot-round',
        'product-analysis': 'el-icon-s-data',
        'product-stock': 'el-icon-s-management',
        'vaccine-stock': 'el-icon-first-aid-kit',
        'product-comment-analysis': 'el-icon-pie-chart',
        'service-comment-analysis': 'el-icon-pie-chart',
        'post-manage': 'el-icon-document',
        'comment-manage': 'el-icon-chat-dot-square',
        'user-chat-history': 'el-icon-chat-dot-round',
        'knowledge-list': 'el-icon-notebook-2',
        'faq-list': 'el-icon-question',
        'chat-history': 'el-icon-chat-line-round',
        'carousel': 'el-icon-picture-outline',
        'notice-manage': 'el-icon-bell',
        'feedback': 'el-icon-warning-outline',
        'settings': 'el-icon-setting'
      }
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
      this.updateFromRoute();
    }
  },
  mounted() {
    this.updateFromRoute();
    document.addEventListener('fullscreenchange', this.onFullscreenChange);
  },
  beforeDestroy() {
    document.removeEventListener('fullscreenchange', this.onFullscreenChange);
  },
  methods: {
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed;
    },

    toggleFullscreen() {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen();
        this.isFullscreen = true;
      } else {
        document.exitFullscreen();
        this.isFullscreen = false;
      }
    },

    onFullscreenChange() {
      this.isFullscreen = !!document.fullscreenElement;
    },

    handleMenuSelect(index) {
      this.activeMenu = index;
      this.currentTitle = this.titleMap[index] || '管理页面';

      // 添加或更新访问记录
      const exists = this.visitedPages.find(p => p.index === index);
      if (!exists) {
        this.visitedPages.push({
          index,
          title: this.titleMap[index] || '管理页面',
          icon: this.iconMap[index] || 'el-icon-document'
        });
        // 最多保留 12 个标签
        if (this.visitedPages.length > 12) {
          this.visitedPages.shift();
        }
      }

      this.$router.push(`/admin/${index}`);
    },

    switchToPage(page) {
      this.activeMenu = page.index;
      this.currentTitle = page.title;
      this.$router.push(`/admin/${page.index}`);
    },

    closePage(page) {
      const idx = this.visitedPages.findIndex(p => p.index === page.index);
      if (idx !== -1) {
        this.visitedPages.splice(idx, 1);
        // 关的是当前页，跳到最后一个
        if (this.activeMenu === page.index) {
          const last = this.visitedPages[this.visitedPages.length - 1];
          if (last) {
            this.switchToPage(last);
          } else {
            this.handleMenuSelect('dashboard');
          }
        }
      }
    },

    updateFromRoute() {
      const path = this.$route.path;
      const menu = path.split('/').pop();
      if (menu && menu !== 'admin' && this.titleMap[menu]) {
        this.activeMenu = menu;
        this.currentTitle = this.titleMap[menu];

        // 自动添加路由页面到标签
        if (!this.visitedPages.find(p => p.index === menu)) {
          this.visitedPages.push({
            index: menu,
            title: this.titleMap[menu],
            icon: this.iconMap[menu] || 'el-icon-document'
          });
          if (this.visitedPages.length > 12) {
            this.visitedPages.shift();
          }
        }
      }
    },

    handleUserCommand(command) {
      if (command === 'profile') {
        this.$router.push('/admin/Profile');
      } else if (command === 'settings') {
        this.$router.push('/admin/settings');
      } else if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '退出确认', {
          confirmButtonText: '确定退出',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          localStorage.removeItem('token');
          localStorage.removeItem('userInfo');
          this.$message.success('已安全退出');
          this.$router.push('/login');
        }).catch(() => {});
      }
    }
  }
};
</script>

<style scoped>
/* ========== 布局容器 ========== */
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f0f2f5;
}

/* ========== 侧边栏 ========== */
.admin-sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1e2130 0%, #1a1d2b 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.28s ease;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
}

.admin-sidebar.collapsed {
  width: 64px;
}

/* --- Logo --- */
.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
  white-space: nowrap;
  transition: padding 0.28s ease;
}

.admin-sidebar.collapsed .sidebar-logo {
  padding: 0 14px;
  justify-content: center;
}

.logo-icon-wrap {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(245, 158, 75, 0.3);
}

.logo-icon {
  font-size: 20px;
  line-height: 1;
}

.logo-info {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo-title {
  font-size: 16px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.5px;
}

.logo-subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 1px;
}

/* --- 菜单区域 --- */
.sidebar-menu-wrap {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu-wrap::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu-wrap::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu-wrap::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
}

.sidebar-menu {
  border-right: none !important;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 240px;
}

/* --- 菜单项样式 --- */
.sidebar-menu ::v-deep .el-menu-item,
.sidebar-menu ::v-deep .el-submenu__title {
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 8px;
  padding-left: 16px !important;
  transition: all 0.2s ease;
  font-size: 14px;
}

.sidebar-menu ::v-deep .el-menu-item:hover,
.sidebar-menu ::v-deep .el-submenu__title:hover {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #ffffff !important;
}

.sidebar-menu ::v-deep .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(245, 158, 75, 0.25), rgba(240, 130, 106, 0.15)) !important;
  color: #ffffff !important;
  position: relative;
}

.sidebar-menu ::v-deep .el-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  background: linear-gradient(180deg, #f59e4b, #f0826a);
  border-radius: 0 3px 3px 0;
}

.sidebar-menu ::v-deep .el-menu-item i,
.sidebar-menu ::v-deep .el-submenu__title i {
  font-size: 17px;
  width: 20px;
  text-align: center;
  margin-right: 10px;
  transition: color 0.2s;
}

.sidebar-menu ::v-deep .el-menu-item.is-active i {
  color: #f59e4b;
}

/* --- 子菜单展开 --- */
.sidebar-menu ::v-deep .el-submenu .el-menu {
  background: rgba(0, 0, 0, 0.15) !important;
  border-radius: 0 0 8px 8px;
  margin: 0 8px;
}

.sidebar-menu ::v-deep .el-submenu .el-menu .el-menu-item {
  height: 40px;
  line-height: 40px;
  padding-left: 52px !important;
  font-size: 13px;
}

.sidebar-menu ::v-deep .el-submenu .el-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.04) !important;
}

.sidebar-menu ::v-deep .el-submenu.is-active > .el-submenu__title {
  color: #ffffff !important;
}

.sidebar-menu ::v-deep .el-submenu.is-active > .el-submenu__title i {
  color: #f59e4b;
}

/* --- 折叠状态 --- */
.admin-sidebar.collapsed .sidebar-menu ::v-deep .el-menu-item,
.admin-sidebar.collapsed .sidebar-menu ::v-deep .el-submenu__title {
  margin: 2px 4px;
  padding-left: 0 !important;
  border-radius: 6px;
}

.admin-sidebar.collapsed .sidebar-menu ::v-deep .el-menu-item i,
.admin-sidebar.collapsed .sidebar-menu ::v-deep .el-submenu__title i {
  margin-right: 0;
}

/* --- 侧边栏底部 --- */
.sidebar-footer {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.version-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.3);
}

.version-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #67c23a;
}

/* ========== 主内容区 ========== */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f0f2f5;
}

/* --- 顶部导航 --- */
.admin-topbar {
  height: 56px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  z-index: 9;
  position: relative;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-trigger {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  color: #606266;
  transition: all 0.2s;
}

.collapse-trigger:hover {
  background: #f5f7fa;
  color: #f0826a;
}

/* --- 面包屑 --- */
.admin-topbar ::v-deep .el-breadcrumb {
  font-size: 13px;
  line-height: 1;
}

.admin-topbar ::v-deep .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #303133;
  font-weight: 500;
}

/* --- 右侧操作 --- */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.topbar-action {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  color: #606266;
  transition: all 0.2s;
}

.topbar-action:hover {
  background: #f5f7fa;
  color: #f0826a;
}

.topbar-badge {
  line-height: 1;
}

.topbar-badge ::v-deep .el-badge__content {
  border: 2px solid #ffffff;
}

/* --- 用户信息 --- */
.topbar-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 4px;
  border-radius: 28px;
  transition: background 0.2s;
  margin-left: 8px;
}

.topbar-user:hover {
  background: #f5f7fa;
}

.user-avatar {
  flex-shrink: 0;
}

.user-name {
  font-size: 13px;
  color: #303133;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-tag {
  font-size: 10px;
  border-color: rgba(245, 158, 75, 0.4);
  color: #e6a23c;
  background: rgba(245, 158, 75, 0.06);
}

.topbar-user > i {
  font-size: 12px;
  color: #909399;
}

/* --- 下拉菜单头部 --- */
.dropdown-user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px 10px;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dropdown-user-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.dropdown-user-role {
  font-size: 12px;
  color: #909399;
}

/* --- 页面标签栏 --- */
.page-tags {
  height: 38px;
  background: #ffffff;
  border-top: 1px solid #f0f2f5;
  display: flex;
  align-items: center;
  padding: 0 8px;
  overflow: hidden;
}

.tags-scroll {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  white-space: nowrap;
  flex: 1;
}

.tags-scroll::-webkit-scrollbar {
  height: 2px;
}

.tags-scroll::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 1px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  background: transparent;
  cursor: pointer;
  transition: all 0.15s;
  border: 1px solid transparent;
  user-select: none;
}

.tag-item:hover {
  background: #f5f7fa;
  color: #303133;
}

.tag-item.active {
  background: #ffffff;
  color: #f0826a;
  border-color: #fde2d0;
  font-weight: 500;
}

.tag-item i {
  font-size: 13px;
}

.tag-close {
  font-size: 11px;
  margin-left: 2px;
  color: #c0c4cc;
  transition: color 0.15s;
}

.tag-close:hover {
  color: #f56c6c;
}

/* ========== 内容区域 ========== */
.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f0f2f5;
}

.admin-content::-webkit-scrollbar {
  width: 6px;
}

.admin-content::-webkit-scrollbar-track {
  background: transparent;
}

.admin-content::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.admin-content::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

/* ========== 动画 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

/* ========== 全局下拉覆盖 ========== */
::v-deep .el-dropdown-menu {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #ebeef5;
  margin-top: 6px;
}

::v-deep .el-dropdown-menu__item {
  padding: 0 16px;
  height: 38px;
  line-height: 38px;
  font-size: 13px;
}

::v-deep .el-dropdown-menu__item i {
  margin-right: 8px;
  color: #909399;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 100;
    width: 240px;
  }

  .admin-sidebar.collapsed {
    width: 0;
  }

  .admin-sidebar.collapsed .sidebar-logo,
  .admin-sidebar.collapsed .sidebar-menu-wrap,
  .admin-sidebar.collapsed .sidebar-footer {
    display: none;
  }

  .admin-topbar {
    padding: 0 12px;
  }

  .admin-content {
    padding: 12px;
  }

  .tag-item {
    font-size: 11px;
    padding: 3px 8px;
  }
}
</style>
