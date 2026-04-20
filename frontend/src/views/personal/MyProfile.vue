<template>
  <div class="profile-page">
    <div class="panel-header">
      <h3>个人资料</h3>
      <div class="header-actions">
        <el-button v-if="!isEditing" type="primary" size="small" @click="startEdit">编辑资料</el-button>
        <template v-else>
          <el-button size="small" @click="cancelEdit">取消</el-button>
          <el-button type="primary" size="small" @click="submitForm" :loading="loading">保存</el-button>
        </template>
      </div>
    </div>

    <el-form :model="form" :rules="rules" ref="profileForm" label-width="100px" class="profile-form">
      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled></el-input>
        <div class="form-tip">用户名不可修改</div>
      </el-form-item>

      <el-form-item label="昵称" prop="nickname">
        <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
            maxlength="20"
            :disabled="!isEditing"
        ></el-input>
      </el-form-item>

      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender" :disabled="!isEditing">
          <el-radio label="男">男</el-radio>
          <el-radio label="女">女</el-radio>
          <el-radio label="保密">保密</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            disabled
        ></el-input>
        <div class="form-tip">
          邮箱不可修改，如需更换请前往
          <router-link to="/personal/security" class="link">安全设置</router-link>
        </div>
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            :disabled="!isEditing"
        ></el-input>
      </el-form-item>

      <el-form-item v-if="isEditing">
        <el-button type="primary" @click="submitForm" :loading="loading">保存修改</el-button>
        <el-button @click="cancelEdit">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { updateUserInfo } from '@/api/user';

export default {
  name: 'Profile',
  data() {
    return {
      loading: false,
      isEditing: false,
      form: {
        gender: '保密',
        username: '',
        nickname: '',
        email: '',
        phone: ''
      },
      originalForm: {
        gender: '保密',
        username: '',
        nickname: '',
        email: '',
        phone: ''
      },
      rules: {
        nickname: [
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ]
      }
    };
  },
  created() {
    this.loadUserInfo();
  },
  methods: {
    loadUserInfo() {
      const info = localStorage.getItem('userInfo');
      if (info) {
        const user = JSON.parse(info);
        this.form = {
          username: user.username || '',
          nickname: user.nickname || '',
          email: user.email || '',
          phone: user.phone || '',
          gender: user.gender || '保密',
        };
        this.originalForm = { ...this.form };
      }
    },

    startEdit() {
      this.isEditing = true;
    },

    cancelEdit() {
      this.isEditing = false;
      this.form = { ...this.originalForm };
      this.$nextTick(() => {
        if (this.$refs.profileForm) {
          this.$refs.profileForm.clearValidate();
        }
      });
    },

    async submitForm() {
      this.$refs.profileForm.validate(async (valid) => {
        if (!valid) return;

        this.loading = true;
        try {
          // 只提交可修改的字段
          const submitData = {
            nickname: this.form.nickname,
            phone: this.form.phone,
            gender: this.form.gender
          };

          const res = await updateUserInfo(submitData);
          if (res.code === 200) {
            // 更新本地存储
            const userInfo = JSON.parse(localStorage.getItem('userInfo'));
            userInfo.nickname = this.form.nickname;
            userInfo.phone = this.form.phone;
            userInfo.gender = this.form.gender;
            localStorage.setItem('userInfo', JSON.stringify(userInfo));

            this.originalForm = { ...this.form };
            this.isEditing = false;

            this.$message.success('保存成功');
            this.$bus.$emit('user-info-updated');
          } else {
            this.$message.error(res.message || '保存失败');
          }
        } catch (error) {
          console.error('保存失败:', error);
          this.$message.error('保存失败');
        } finally {
          this.loading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.profile-page {
  padding: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.profile-form {
  max-width: 480px;
}

.profile-form ::v-deep .el-form-item {
  margin-bottom: 22px;
}

.profile-form ::v-deep .el-form-item__label {
  color: #555;
  font-weight: 500;
}

.profile-form ::v-deep .el-input__inner {
  border-radius: 8px;
}

.profile-form ::v-deep .el-input.is-disabled .el-input__inner {
  background-color: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  line-height: 1.5;
}

.form-tip .link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.form-tip .link:hover {
  text-decoration: underline;
}

.profile-form ::v-deep .el-form-item:last-child {
  margin-top: 32px;
  margin-bottom: 0;
}

.profile-form ::v-deep .el-button {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 500;
}
</style>