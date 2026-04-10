<template>
  <div class="staff-layout">
    <!-- 侧边栏 -->
    <aside class="staff-sidebar" :class="{ 'collapsed': isCollapsed }">
      <div class="sidebar-header">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🐾</span>
          <span class="logo-text" v-show="!isCollapsed">员工工作台</span>
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
          <span slot="title">工作台</span>
        </el-menu-item>

        <el-menu-item index="appointments">
          <i class="el-icon-s-order"></i>
          <span slot="title">预约管理</span>
        </el-menu-item>

        <el-submenu index="pet">
          <template slot="title">
            <i class="el-icon-s-custom"></i>
            <span slot="title">宠物管理</span>
          </template>
          <el-menu-item index="pet-list">
            <i class="el-icon-menu"></i>
            <span slot="title">宠物列表</span>
          </el-menu-item>
          <el-menu-item index="pet-health">
            <i class="el-icon-first-aid-kit"></i>
            <span slot="title">健康记录</span>
          </el-menu-item>
        </el-submenu>

        <el-menu-item index="evaluations">
          <i class="el-icon-star-on"></i>
          <span slot="title">服务评价</span>
        </el-menu-item>

        <el-menu-item index="feedback">
          <i class="el-icon-warning"></i>
          <span slot="title">异常反馈</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="staff-main">
      <!-- 顶部栏 -->
      <div class="staff-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>员工工作台</el-breadcrumb-item>
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
      <div class="staff-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StaffLayout',
  data() {
    return {
      isCollapsed: false,
      activeMenu: 'dashboard',
      currentTitle: '工作台'
    };
  },
  computed: {
    userInfo() {
      const info = localStorage.getItem('userInfo');
      return info ? JSON.parse(info) : null;
    },
    userNickname() {
      return this.userInfo?.nickname || this.userInfo?.username || '员工';
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
      const titles = {
        'dashboard': '工作台',
        'appointments': '预约管理',
        'pet-list': '宠物列表',
        'pet-health': '健康记录',
        'evaluations': '服务评价',
        'feedback': '异常反馈'
      };
      this.currentTitle = titles[index] || '管理页面';
      this.$router.push(`/staff/${index}`);
    },
    updateActiveMenu() {
      const path = this.$route.path;
      const menu = path.split('/').pop();
      if (menu && menu !== 'staff') {
        this.activeMenu = menu;
        const titles = {
          'dashboard': '工作台',
          'appointments': '预约管理',
          'pet-list': '宠物列表',
          'pet-health': '健康记录',
          'evaluations': '服务评价',
          'feedback': '异常反馈'
        };
        this.currentTitle = titles[menu] || '管理页面';
      }
    },
    handleUserCommand(command) {
      if (command === 'profile') {
        this.$router.push('/personal/profile');
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
.staff-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.staff-sidebar {
  width: 220px;
  background: #304156;
  transition: width 0.3s;
  overflow-y: auto;
  overflow-x: hidden;
}

.staff-sidebar.collapsed {
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

.sidebar-menu ::v-deep .el-submenu .el-menu-item {
  padding-left: 50px !important;
}

.staff-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f0f2f6;
}

.staff-header {
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

.staff-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 滚动条样式 */
.staff-sidebar::-webkit-scrollbar {
  width: 6px;
}

.staff-sidebar::-webkit-scrollbar-track {
  background: #2b3a4a;
}

.staff-sidebar::-webkit-scrollbar-thumb {
  background: #4a5a6a;
  border-radius: 3px;
}

.staff-content::-webkit-scrollbar {
  width: 8px;
}

.staff-content::-webkit-scrollbar-track {
  background: #f0f2f6;
}

.staff-content::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

.staff-content::-webkit-scrollbar-thumb:hover {
  background: #909399;
}
</style>