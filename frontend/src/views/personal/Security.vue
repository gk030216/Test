<template>
  <div class="security-page">
    <h2 class="page-title">账号安全</h2>

    <div class="security-section">
      <div class="section-item">
        <div class="item-info">
          <div class="item-title">修改密码</div>
          <div class="item-desc">定期更换密码，保护账号安全</div>
        </div>
        <el-button type="primary" plain @click="showPasswordDialog = true">修改</el-button>
      </div>

      <div class="section-item">
        <div class="item-info">
          <div class="item-title">绑定手机</div>
          <div class="item-desc">{{ userInfo.phone ? '已绑定：' + userInfo.phone : '未绑定' }}</div>
        </div>
        <el-button type="primary" plain @click="showPhoneDialog = true">{{ userInfo.phone ? '更换' : '绑定' }}</el-button>
      </div>

      <div class="section-item">
        <div class="item-info">
          <div class="item-title">绑定邮箱</div>
          <div class="item-desc">{{ userInfo.email ? '已绑定：' + userInfo.email : '未绑定' }}</div>
        </div>
        <el-button type="primary" plain @click="showEmailDialog = true">{{ userInfo.email ? '更换' : '绑定' }}</el-button>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" :visible.sync="showPasswordDialog" width="450px" center>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="updatePassword" :loading="passwordLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 绑定手机对话框 -->
    <el-dialog title="绑定手机" :visible.sync="showPhoneDialog" width="450px" center>
      <el-form :model="phoneForm" :rules="phoneRules" ref="phoneForm" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="phoneForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-wrapper">
            <el-input v-model="phoneForm.code" placeholder="请输入验证码"></el-input>
            <el-button :disabled="!canSendCode" @click="sendPhoneCode">{{ codeBtnText }}</el-button>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showPhoneDialog = false">取消</el-button>
        <el-button type="primary" @click="bindPhone" :loading="phoneLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 绑定邮箱对话框 -->
    <el-dialog title="绑定邮箱" :visible.sync="showEmailDialog" width="450px" center>
      <el-form :model="emailForm" :rules="emailRules" ref="emailForm" label-width="80px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="emailForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-wrapper">
            <el-input v-model="emailForm.code" placeholder="请输入验证码"></el-input>
            <el-button :disabled="!canSendEmailCode" @click="sendEmailCode">{{ emailCodeBtnText }}</el-button>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showEmailDialog = false">取消</el-button>
        <el-button type="primary" @click="bindEmail" :loading="emailLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { updatePassword, bindPhone, bindEmail, sendBindCode } from '@/api/user';

export default {
  name: 'Security',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

    return {
      userInfo: {},
      showPasswordDialog: false,
      showPhoneDialog: false,
      showEmailDialog: false,
      passwordLoading: false,
      phoneLoading: false,
      emailLoading: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      phoneForm: {
        phone: '',
        code: ''
      },
      phoneRules: {
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      emailForm: {
        email: '',
        code: ''
      },
      emailRules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      countdown: 60,
      timer: null,
      codeSending: false,
      emailCountdown: 60,
      emailTimer: null,
      emailCodeSending: false
    };
  },
  computed: {
    canSendCode() {
      return this.phoneForm.phone && !this.codeSending && this.countdown === 60;
    },
    codeBtnText() {
      if (this.countdown < 60) return `${this.countdown}秒后重试`;
      if (this.codeSending) return '发送中...';
      return '获取验证码';
    },
    canSendEmailCode() {
      return this.emailForm.email && !this.emailCodeSending && this.emailCountdown === 60;
    },
    emailCodeBtnText() {
      if (this.emailCountdown < 60) return `${this.emailCountdown}秒后重试`;
      if (this.emailCodeSending) return '发送中...';
      return '获取验证码';
    }
  },
  created() {
    this.loadUserInfo();
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer);
    if (this.emailTimer) clearInterval(this.emailTimer);
  },
  methods: {
    loadUserInfo() {
      const info = localStorage.getItem('userInfo');
      if (info) {
        this.userInfo = JSON.parse(info);
      }
    },
    async updatePassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (!valid) return;
        this.passwordLoading = true;
        try {
          const res = await updatePassword(this.passwordForm);
          if (res.code === 200) {
            this.$message.success('密码修改成功，请重新登录');
            this.showPasswordDialog = false;
            setTimeout(() => {
              localStorage.removeItem('token');
              localStorage.removeItem('userInfo');
              this.$router.push('/login');
            }, 1500);
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('修改失败');
        } finally {
          this.passwordLoading = false;
        }
      });
    },
    async sendPhoneCode() {
      this.codeSending = true;
      try {
        const res = await sendBindCode(this.phoneForm.phone, 'phone');
        if (res.code === 200) {
          this.$message.success('验证码已发送');
          this.startCountdown();
        } else {
          this.$message.error(res.message);
          this.codeSending = false;
        }
      } catch (error) {
        this.$message.error('发送失败');
        this.codeSending = false;
      }
    },
    startCountdown() {
      this.countdown = 60;
      if (this.timer) clearInterval(this.timer);
      this.timer = setInterval(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          clearInterval(this.timer);
          this.timer = null;
          this.countdown = 60;
          this.codeSending = false;
        }
      }, 1000);
    },
    async bindPhone() {
      this.$refs.phoneForm.validate(async (valid) => {
        if (!valid) return;
        this.phoneLoading = true;
        try {
          const res = await bindPhone(this.phoneForm);
          if (res.code === 200) {
            this.$message.success('绑定成功');
            this.userInfo.phone = this.phoneForm.phone;
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
            this.showPhoneDialog = false;
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('绑定失败');
        } finally {
          this.phoneLoading = false;
        }
      });
    },
    async sendEmailCode() {
      this.emailCodeSending = true;
      try {
        const res = await sendBindCode(this.emailForm.email, 'email');
        if (res.code === 200) {
          this.$message.success('验证码已发送');
          this.startEmailCountdown();
        } else {
          this.$message.error(res.message);
          this.emailCodeSending = false;
        }
      } catch (error) {
        this.$message.error('发送失败');
        this.emailCodeSending = false;
      }
    },
    startEmailCountdown() {
      this.emailCountdown = 60;
      if (this.emailTimer) clearInterval(this.emailTimer);
      this.emailTimer = setInterval(() => {
        this.emailCountdown--;
        if (this.emailCountdown <= 0) {
          clearInterval(this.emailTimer);
          this.emailTimer = null;
          this.emailCountdown = 60;
          this.emailCodeSending = false;
        }
      }, 1000);
    },
    async bindEmail() {
      this.$refs.emailForm.validate(async (valid) => {
        if (!valid) return;
        this.emailLoading = true;
        try {
          const res = await bindEmail(this.emailForm);
          if (res.code === 200) {
            this.$message.success('绑定成功');
            this.userInfo.email = this.emailForm.email;
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
            this.showEmailDialog = false;
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('绑定失败');
        } finally {
          this.emailLoading = false;
        }
      });
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

.security-section {
  background: #f8f9fc;
  border-radius: 16px;
}

.section-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.section-item:last-child {
  border-bottom: none;
}

.item-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.item-desc {
  font-size: 13px;
  color: #999;
}

.code-wrapper {
  display: flex;
  gap: 10px;
}

.code-wrapper .el-input {
  flex: 1;
}

.code-wrapper .el-button {
  width: 100px;
}
</style>