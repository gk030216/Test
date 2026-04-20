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
          <div class="user-role">
            <el-tag :type="form.role === 3 ? 'danger' : (form.role === 2 ? 'warning' : 'primary')" size="medium">
              {{ form.role === 3 ? '管理员' : (form.role === 2 ? '员工' : '普通用户') }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 右侧：表单区域 -->
      <div class="profile-right">
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
            <el-button type="primary" @click="submitForm" :loading="submitLoading" class="submit-btn">
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
  name: 'Profile',
  data() {
    return {
      submitLoading: false,
      avatarUrl: '',  // 单独存储头像URL用于响应式更新
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
          {min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur'}
        ],
        phone: [
          {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur'}
        ],
        email: [
          {type: 'email', message: '请输入正确的邮箱', trigger: 'blur'}
        ]
      }
    };
  },
  computed: {
    // 格式化注册时间
    formattedCreateTime() {
      if (!this.form.createTime) return '';
      const d = new Date(this.form.createTime);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
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
          // 更新头像显示
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
          // 更新头像URL
          this.avatarUrl = res.data.url;
          this.form.avatar = res.data.url;
          this.$message.success('头像上传成功');

          // 强制刷新头像显示
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
            // 更新本地存储的用户信息
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
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: #909399;
}

.profile-content {
  background: #fff;
  border-radius: 20px;
  display: flex;
  gap: 40px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 左侧头像区域 */
.profile-left {
  width: 260px;
  flex-shrink: 0;
  text-align: center;
}

.avatar-card {
  background: #f8f9fc;
  border-radius: 20px;
  padding: 30px 20px;
  text-align: center;
}

.avatar-upload {
  display: inline-block;
  cursor: pointer;
  position: relative;
}

.profile-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
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
  background: rgba(0, 0, 0, 0.6);
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

.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 16px;
  margin-bottom: 16px;
}

.user-role {
  margin-top: 10px;
}

/* 右侧表单区域 */
.profile-right {
  flex: 1;
}

.profile-form {
  margin-top: 0;
}

.profile-form ::v-deep .el-form-item {
  margin-bottom: 20px;
}

.profile-form ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #2c3e50;
}

.profile-form ::v-deep .el-input__inner {
  border-radius: 8px;
  height: 40px;
  line-height: 40px;
}

.profile-form ::v-deep .el-input.is-disabled .el-input__inner {
  background-color: #f5f7fa;
  color: #909399;
}

.profile-form ::v-deep .el-radio-group {
  line-height: 40px;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  padding: 10px 30px;
  border-radius: 8px;
  font-weight: 500;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 响应式 */
@media (max-width: 768px) {
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