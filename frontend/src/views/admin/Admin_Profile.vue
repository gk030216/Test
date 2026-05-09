<template>
  <div class="profile-page">
    <div class="page-header">
      <h2 class="page-title">个人资料</h2>
      <p class="page-desc">管理您的个人信息</p>
    </div>

    <div class="profile-content">
      <!-- 左侧：头像区域 -->
      <div class="profile-left">
        <div class="avatar-card">
          <div class="avatar-upload" @click="triggerUpload">
            <el-avatar :size="140" :src="avatarUrl" class="profile-avatar">
              {{ getInitial() }}
            </el-avatar>
            <div class="upload-mask">
              <i class="el-icon-camera"></i>
              <span>更换头像</span>
            </div>
            <input
              type="file"
              ref="avatarInput"
              accept="image/jpeg,image/png,image/jpg"
              style="display: none"
              @change="handleAvatarUpload"
            />
          </div>
          <div class="user-name-display">{{ form.nickname || form.username }}</div>
          <div class="user-role">
            <el-tag :type="roleTagType" size="medium">
              {{ roleText }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 右侧：表单区域 -->
      <div class="profile-right">
        <div class="section-title">
          <i class="el-icon-edit-outline"></i>
          <span>基本信息</span>
        </div>
        <el-form :model="form" :rules="rules" ref="profileForm" label-width="100px" class="profile-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名">
                <el-input v-model="form.username" disabled></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="50"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="性别">
                <el-radio-group v-model="form.gender">
                  <el-radio label="男">男</el-radio>
                  <el-radio label="女">女</el-radio>
                  <el-radio label="保密">保密</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="注册时间">
                <el-input :value="formattedCreateTime" disabled></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button @click="submitForm" :loading="submitLoading" class="warm-btn">
              保存修改
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { getUserInfo, updateUserInfo } from '@/api/user';
import { uploadAvatar } from '@/api/upload';

export default {
  name: 'Admin_Profile',
  data() {
    return {
      submitLoading: false,
      avatarUrl: '',
      form: {
        id: null,
        username: '',
        nickname: '',
        phone: '',
        email: '',
        gender: '保密',
        avatar: '',
        role: null,
        createTime: ''
      },
      rules: {
        nickname: [
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    formattedCreateTime() {
      if (!this.form.createTime) return '';
      const d = new Date(this.form.createTime);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    roleText() {
      if (this.form.role === 3) return '管理员';
      if (this.form.role === 2) return '员工';
      return '普通用户';
    },
    roleTagType() {
      if (this.form.role === 3) return 'danger';
      if (this.form.role === 2) return 'warning';
      return 'info';
    }
  },
  created() {
    this.loadProfile();
  },
  methods: {
    getInitial() {
      const name = this.form.nickname || this.form.username;
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
    },

    async loadProfile() {
      try {
        const res = await getUserInfo();
        if (res.code === 200) {
          this.form = {
            id: res.data.id,
            username: res.data.username,
            nickname: res.data.nickname || '',
            phone: res.data.phone || '',
            email: res.data.email || '',
            gender: res.data.gender || '保密',
            avatar: res.data.avatar || '',
            role: res.data.role,
            createTime: res.data.createTime || ''
          };
          this.avatarUrl = this.form.avatar;
        }
      } catch (error) {
        console.error('加载个人资料失败', error);
        this.$message.error('加载失败');
      }
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

      const formData = new FormData();
      formData.append('file', file);

      try {
        const res = await uploadAvatar(formData);
        if (res.code === 200) {
          this.avatarUrl = res.data.url;
          this.form.avatar = res.data.url;
          this.$message.success('头像上传成功');
          this.$forceUpdate();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
      event.target.value = '';
    },

    resetForm() {
      this.loadProfile();
      this.$message.info('已重置');
    },

    async submitForm() {
      this.$refs.profileForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          const res = await updateUserInfo({
            nickname: this.form.nickname,
            phone: this.form.phone,
            email: this.form.email,
            gender: this.form.gender,
            avatar: this.form.avatar
          });
          if (res.code === 200) {
            this.$message.success('保存成功');
            const userInfo = localStorage.getItem('userInfo');
            if (userInfo) {
              const user = JSON.parse(userInfo);
              user.nickname = this.form.nickname;
              user.avatar = this.form.avatar;
              localStorage.setItem('userInfo', JSON.stringify(user));
            }
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('保存失败');
        } finally {
          this.submitLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.profile-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #3d2e2a;
  margin: 0 0 4px;
}

.page-desc {
  font-size: 13px;
  color: #a08c84;
  margin: 0;
}

.profile-content {
  background: #fff;
  border-radius: 16px;
  display: flex;
  gap: 40px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.05);
  border: 1px solid #f5ece6;
}

/* 左侧头像区域 */
.profile-left {
  width: 260px;
  flex-shrink: 0;
  text-align: center;
}

.avatar-card {
  background: #fefbf9;
  border-radius: 16px;
  padding: 30px 20px;
  text-align: center;
  border: 1px solid #f5ece6;
}

.avatar-upload {
  display: inline-block;
  cursor: pointer;
  position: relative;
}

.profile-avatar {
  background: linear-gradient(135deg, #e8a87c, #f0826a);
  color: white;
  font-size: 56px;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(61, 46, 42, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-upload:hover .upload-mask {
  opacity: 1;
}

.upload-mask i {
  font-size: 28px;
  margin-bottom: 8px;
}

.upload-mask span {
  font-size: 12px;
}

.user-name-display {
  font-size: 16px;
  font-weight: 600;
  color: #3d2e2a;
  margin: 16px 0 4px;
}

.user-role {
  margin-top: 10px;
}

/* 右侧表单区域 */
.profile-right {
  flex: 1;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #3d2e2a;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f5ece6;
}

.section-title i {
  color: #f0826a;
  font-size: 16px;
}

.profile-form {
  margin-top: 0;
}

.profile-form ::v-deep .el-form-item {
  margin-bottom: 20px;
}

.profile-form ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #5c4a42;
  font-size: 13px;
}

.profile-form ::v-deep .el-input__inner {
  border-radius: 10px;
  height: 40px;
  line-height: 40px;
  border-color: #e8e0db;
}

.profile-form ::v-deep .el-input__inner:focus,
.profile-form ::v-deep .el-input__inner:hover {
  border-color: #f59e4b;
}

.profile-form ::v-deep .el-input.is-disabled .el-input__inner {
  background: #fefbf9;
  color: #a08c84;
  border-color: #f5ece6;
}

.profile-form ::v-deep .el-radio__input.is-checked .el-radio__inner {
  border-color: #f59e4b;
  background: #f59e4b;
}

.profile-form ::v-deep .el-radio__input.is-checked + .el-radio__label {
  color: #f59e4b;
}

.profile-form ::v-deep .el-radio-group {
  line-height: 40px;
}

.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  font-weight: 500;
  padding: 10px 30px;
  transition: all 0.25s;
}

.warm-btn:hover {
  opacity: 0.88;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(240, 130, 106, 0.35);
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-page {
    padding: 12px;
  }

  .profile-content {
    flex-direction: column;
    padding: 20px;
  }

  .profile-left {
    width: 100%;
  }

  .avatar-card {
    padding: 20px;
  }
}
</style>
