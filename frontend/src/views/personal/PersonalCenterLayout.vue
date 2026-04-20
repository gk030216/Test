<template>
  <div class="personal-center">
    <Navbar />

    <div class="center-content">
      <div class="container">

        <div class="center-layout">
          <!-- 左侧菜单 -->
          <div class="center-sidebar">
            <div class="user-card">
              <div class="user-avatar-wrapper" @click="showAvatarUpload = true">
                <el-avatar :size="80" :src="userInfo.avatar" class="user-avatar" :style="userInfo.avatar ? {} : { background: getAvatarColor(userInfo.nickname || userInfo.username) }">
                  {{ !userInfo.avatar ? getUserInitial(userInfo.nickname || userInfo.username) : '' }}
                </el-avatar>
                <div class="avatar-mask">
                  <i class="el-icon-camera"></i>
                  <span>更换头像</span>
                </div>
              </div>
              <h3 class="user-name">{{ userInfo.nickname || userInfo.username }}</h3>
              <p class="user-role">{{ getUserRoleName(userInfo.role) }}</p>
            </div>

            <el-menu
                :default-active="activeMenu"
                class="sidebar-menu"
                @select="handleMenuSelect"
            >
              <el-menu-item index="/personal/profile">
                <i class="el-icon-user"></i>
                <span>个人资料</span>
              </el-menu-item>
              <el-menu-item index="/personal/posts">
                <i class="el-icon-document"></i>
                <span>我的帖子</span>
              </el-menu-item>
              <el-menu-item index="/personal/favorites">
                <i class="el-icon-star-on"></i>
                <span>我的收藏</span>
              </el-menu-item>
              <el-menu-item index="/personal/comments">
                <i class="el-icon-chat-dot-round"></i>
                <span>我的评论</span>
              </el-menu-item>
              <el-menu-item index="/personal/appointments">
                <i class="el-icon-s-order"></i>
                <span>我的预约</span>
              </el-menu-item>
              <el-menu-item index="/personal/orders">
                <i class="el-icon-s-order"></i>
                <span>我的订单</span>
              </el-menu-item>
              <el-menu-item index="/personal/notifications">
                <i class="el-icon-bell"></i>
                <span>消息通知</span>
                <span class="notification-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
              </el-menu-item>
              <el-menu-item index="/personal/feedback">
                <i class="el-icon-warning"></i>
                <span>意见反馈</span>
              </el-menu-item>
              <el-menu-item index="/personal/address">
                <i class="el-icon-location"></i>
                <span>收货地址</span>
              </el-menu-item>
              <el-menu-item index="/personal/security">
                <i class="el-icon-lock"></i>
                <span>安全设置</span>
              </el-menu-item>
            </el-menu>
          </div>

          <!-- 右侧内容区 -->
          <div class="center-content-area">
            <div class="content-panel">
              <router-view />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog title="更换头像" :visible.sync="showAvatarUpload" width="400px" center>
      <div class="avatar-upload-area">
        <div class="avatar-preview">
          <el-avatar :size="120" :src="avatarPreview" class="preview-avatar">
            {{ !avatarPreview ? getUserInitial(userInfo.nickname || userInfo.username) : '' }}
          </el-avatar>
        </div>
        <div class="upload-btn-wrapper">
          <el-button type="primary" @click="triggerAvatarUpload">选择图片</el-button>
          <input
              type="file"
              ref="avatarInput"
              accept="image/jpeg,image/jpg,image/png"
              style="display: none"
              @change="handleAvatarUpload"
          />
        </div>
        <div class="upload-tip">支持 JPG、PNG 格式，≤2MB</div>
      </div>
      <span slot="footer">
        <el-button @click="showAvatarUpload = false">取消</el-button>
        <el-button type="primary" @click="saveAvatar" :loading="avatarLoading">确定</el-button>
      </span>
    </el-dialog>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { uploadAvatar } from '@/api/upload';
import { updateUserInfo } from '@/api/user';

export default {
  name: 'PersonalCenterLayout',
  components: { Navbar, Footer },
  data() {
    return {
      userInfo: {},
      showAvatarUpload: false,
      avatarPreview: '',
      newAvatar: null,
      avatarLoading: false,
      unreadCount: 0
    };
  },
  computed: {
    activeMenu() {
      return this.$route.path;
    }
  },
  created() {
    this.loadUserInfo();
  },
  methods: {
    loadUserInfo() {
      const info = localStorage.getItem('userInfo');
      if (info) {
        this.userInfo = JSON.parse(info);
      }
    },
    getUserRoleName(role) {
      if (role === 1) return '普通用户';
      if (role === 2) return '员工';
      if (role === 3) return '管理员';
      return '用户';
    },
    getAvatarColor(name) {
      if (!name) return 'linear-gradient(135deg, #667eea, #764ba2)';
      const colors = [
        'linear-gradient(135deg, #667eea, #764ba2)',
        'linear-gradient(135deg, #f093fb, #f5576c)',
        'linear-gradient(135deg, #4facfe, #00f2fe)',
        'linear-gradient(135deg, #43e97b, #38f9d7)',
        'linear-gradient(135deg, #fa709a, #fee140)'
      ];
      let index = 0;
      for (let i = 0; i < name.length; i++) {
        index += name.charCodeAt(i);
      }
      return colors[index % colors.length];
    },
    getUserInitial(name) {
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
    },
    handleMenuSelect(path) {
      if (this.$route.path !== path) {
        this.$router.push(path);
      }
    },
    triggerAvatarUpload() {
      this.$refs.avatarInput.click();
    },
    handleAvatarUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      if (!file.type.startsWith('image/')) {
        this.$message.error('只支持图片格式');
        return;
      }
      if (file.size > 2 * 1024 * 1024) {
        this.$message.error('图片不能超过2MB');
        return;
      }
      const reader = new FileReader();
      reader.onload = (e) => {
        this.avatarPreview = e.target.result;
      };
      reader.readAsDataURL(file);
      this.newAvatar = file;
    },
    async saveAvatar() {
      if (!this.newAvatar) {
        this.showAvatarUpload = false;
        return;
      }
      this.avatarLoading = true;
      const formData = new FormData();
      formData.append('file', this.newAvatar);

      try {
        // 1. 上传头像图片
        const uploadRes = await uploadAvatar(formData);
        if (uploadRes.code !== 200) {
          this.$message.error(uploadRes.message || '上传失败');
          return;
        }

        const avatarUrl = uploadRes.data.url;

        // 2. 更新用户信息到数据库
        const updateRes = await updateUserInfo({
          avatar: avatarUrl,
          nickname: this.userInfo.nickname,
          email: this.userInfo.email,
          phone: this.userInfo.phone
        });

        if (updateRes.code === 200) {
          // 3. 更新本地存储
          this.userInfo.avatar = avatarUrl;
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo));

          // 4. 触发全局事件，让其他组件更新头像
          this.$bus.$emit('user-info-updated', this.userInfo);

          this.$message.success('头像更新成功');
          this.showAvatarUpload = false;
          this.avatarPreview = '';
          this.newAvatar = null;
        } else {
          this.$message.error(updateRes.message || '更新失败');
        }
      } catch (error) {
        console.error('上传失败', error);
        this.$message.error('操作失败');
      } finally {
        this.avatarLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.personal-center {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.center-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.page-desc {
  color: #999;
}

.center-layout {
  display: flex;
  gap: 30px;
}

.center-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.user-card {
  background: white;
  border-radius: 20px;
  padding: 30px 20px;
  text-align: center;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.user-avatar-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 15px;
  cursor: pointer;
}

.user-avatar {
  width: 100%;
  height: 100%;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.user-avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}

.avatar-mask i {
  font-size: 20px;
  margin-bottom: 4px;
}

.avatar-mask span {
  font-size: 12px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.user-role {
  font-size: 13px;
  color: #999;
}

.sidebar-menu {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: none;
}

.sidebar-menu ::v-deep .el-menu-item {
  height: 50px;
  line-height: 50px;
}

.sidebar-menu ::v-deep .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.center-content-area {
  flex: 1;
}

.content-panel {
  background: white;
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  min-height: 500px;
}

/* 子页面样式覆盖 - 让子页面内容适配 */
.content-panel ::v-deep .page-title {
  display: none; /* 隐藏子页面的标题，因为布局已有 */
}

.content-panel ::v-deep .profile-form,
.content-panel ::v-deep .post-list,
.content-panel ::v-deep .comment-list,
.content-panel ::v-deep .address-list,
.content-panel ::v-deep .security-section,
.content-panel ::v-deep .my-appointments {
  padding: 0;
}

.content-panel ::v-deep h2 {
  margin-top: 0;
}

/* 头像上传对话框 */
.avatar-upload-area {
  text-align: center;
}

.avatar-preview {
  margin-bottom: 20px;
}

.preview-avatar {
  margin: 0 auto;
}

.upload-btn-wrapper {
  margin-bottom: 10px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
}

@media (max-width: 768px) {
  .center-layout {
    flex-direction: column;
  }

  .center-sidebar {
    width: 100%;
  }
}
</style>