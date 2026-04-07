<template>
  <div class="profile-page">
    <h2 class="page-title">个人资料</h2>

    <el-form :model="form" :rules="rules" ref="profileForm" label-width="100px" class="profile-form">
      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled></el-input>
        <div class="form-tip">用户名不可修改</div>
      </el-form-item>

      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20"></el-input>
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
      </el-form-item>

      <el-form-item label="个性签名">
        <el-input
            v-model="form.signature"
            type="textarea"
            :rows="3"
            placeholder="介绍一下自己吧"
            maxlength="100"
            show-word-limit
        ></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">保存修改</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { getUserInfo, updateUserInfo } from '@/api/user';

export default {
  name: 'Profile',
  data() {
    return {
      loading: false,
      form: {
        username: '',
        nickname: '',
        email: '',
        phone: '',
        signature: ''
      },
      rules: {
        nickname: [
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
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
          signature: user.signature || ''
        };
      }
    },
    async submitForm() {
      this.$refs.profileForm.validate(async (valid) => {
        if (!valid) return;

        this.loading = true;
        try {
          const res = await updateUserInfo(this.form);
          if (res.code === 200) {
            // 更新本地存储
            const userInfo = JSON.parse(localStorage.getItem('userInfo'));
            Object.assign(userInfo, this.form);
            localStorage.setItem('userInfo', JSON.stringify(userInfo));
            this.$message.success('保存成功');
            this.$bus.$emit('user-info-updated');
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('保存失败');
        } finally {
          this.loading = false;
        }
      });
    },
    resetForm() {
      this.loadUserInfo();
      this.$refs.profileForm.clearValidate();
    }
  }
};
</script>

<style scoped>
.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.profile-form {
  max-width: 500px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>