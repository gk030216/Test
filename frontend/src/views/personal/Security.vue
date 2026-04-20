<template>
  <div class="security-page">
    <div class="page-header">
      <h2 class="page-title">账号安全</h2>
      <p class="page-desc">管理你的账号安全设置</p>
    </div>

    <div class="security-section">
      <!-- 修改密码 -->
      <div class="section-item">
        <div class="item-info">
          <div class="item-icon">
            <i class="el-icon-lock"></i>
          </div>
          <div class="item-content">
            <div class="item-title">登录密码</div>
            <div class="item-desc">定期更换密码可以保护账号安全</div>
          </div>
        </div>
        <el-button type="primary" plain size="small" @click="showPasswordDialog = true" class="action-btn">
          修改
        </el-button>
      </div>

      <!-- 绑定邮箱 -->
      <div class="section-item">
        <div class="item-info">
          <div class="item-icon">
            <i class="el-icon-message"></i>
          </div>
          <div class="item-content">
            <div class="item-title">绑定邮箱</div>
            <div class="item-desc">
              <span v-if="userInfo.email" class="email-text">{{ userInfo.email }}</span>
              <span v-else class="not-bind">未绑定</span>
            </div>
          </div>
        </div>
        <el-button type="primary" plain size="small" @click="showEmailDialog = true" class="action-btn">
          {{ userInfo.email ? '更换' : '绑定' }}
        </el-button>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
        title="修改密码"
        :visible.sync="showPasswordDialog"
        width="450px"
        center
        class="security-dialog"
        :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                prefix-icon="el-icon-lock"
                show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20位）"
                prefix-icon="el-icon-key"
                show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                prefix-icon="el-icon-check"
                show-password
            />
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
          确认修改
        </el-button>
      </span>
    </el-dialog>

    <!-- 绑定/更换邮箱对话框 -->
    <el-dialog
        :title="userInfo.email ? '更换邮箱' : '绑定邮箱'"
        :visible.sync="showEmailDialog"
        width="500px"
        center
        class="security-dialog"
        :close-on-click-modal="false"
        @closed="resetEmailForm"
    >
      <div class="dialog-content">
        <!-- 步骤条 -->
        <el-steps :active="emailStep" align-center finish-status="success" class="email-steps">
          <el-step title="验证身份" />
          <el-step title="输入新邮箱" />
        </el-steps>

        <!-- 步骤1：验证身份 -->
        <div v-if="emailStep === 0">
          <div class="step-desc">
            <i class="el-icon-info"></i>
            为了你的账号安全，请先验证当前邮箱
          </div>
          <el-form :model="verifyForm" :rules="verifyRules" ref="verifyForm" label-width="80px">
            <el-form-item label="当前邮箱">
              <el-input v-model="userInfo.email" disabled />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="code-wrapper">
                <el-input v-model="verifyForm.code" placeholder="请输入验证码" maxlength="6" />
                <el-button
                    :disabled="!canSendVerifyCode"
                    @click="sendVerifyCode"
                    :loading="verifyCodeSending"
                    class="code-btn"
                >
                  {{ verifyCodeBtnText }}
                </el-button>
              </div>
            </el-form-item>
          </el-form>
          <div class="step-actions">
            <el-button @click="showEmailDialog = false">取消</el-button>
            <el-button type="primary" @click="nextToNewEmail" :loading="verifyLoading">
              下一步
            </el-button>
          </div>
        </div>

        <!-- 步骤2：输入新邮箱 -->
        <div v-if="emailStep === 1">
          <div class="step-desc">
            <i class="el-icon-info"></i>
            请输入新的邮箱地址
          </div>
          <el-form :model="emailForm" :rules="emailRules" ref="emailForm" label-width="80px">
            <el-form-item label="新邮箱" prop="email">
              <el-input v-model="emailForm.email" placeholder="请输入新邮箱" prefix-icon="el-icon-message" />
            </el-form-item>
          </el-form>
          <div class="step-actions">
            <el-button @click="emailStep = 0">上一步</el-button>
            <el-button type="primary" @click="handleBindEmail" :loading="emailLoading">
              确认更换
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { changePassword, sendEmailCode, updateEmail } from '@/api/user';

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
      showEmailDialog: false,
      passwordLoading: false,
      emailLoading: false,
      verifyLoading: false,
      emailStep: 0,

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

      verifyForm: {
        code: ''
      },
      verifyRules: {
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码为6位数字', trigger: 'blur' }
        ]
      },

      emailForm: {
        email: ''
      },
      emailRules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ]
      },

      verifyCountdown: 60,
      verifyTimer: null,
      verifyCodeSending: false
    };
  },
  computed: {
    canSendVerifyCode() {
      return this.userInfo.email && !this.verifyCodeSending && this.verifyCountdown === 60;
    },
    verifyCodeBtnText() {
      if (this.verifyCountdown < 60) return `${this.verifyCountdown}秒后重试`;
      if (this.verifyCodeSending) return '发送中...';
      return '获取验证码';
    }
  },
  created() {
    this.loadUserInfo();
  },
  beforeDestroy() {
    if (this.verifyTimer) clearInterval(this.verifyTimer);
  },
  methods: {
    loadUserInfo() {
      const info = localStorage.getItem('userInfo');
      if (info) {
        this.userInfo = JSON.parse(info);
      }
    },

    async handleChangePassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (!valid) return;

        this.passwordLoading = true;
        try {
          const res = await changePassword({
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          });

          if (res.code === 200) {
            this.$message.success('密码修改成功，请重新登录');
            this.showPasswordDialog = false;
            setTimeout(() => {
              localStorage.removeItem('token');
              localStorage.removeItem('userInfo');
              this.$router.push('/login');
            }, 1500);
          } else {
            this.$message.error(res.message || '修改失败');
          }
        } catch (error) {
          console.error('修改密码失败:', error);
          this.$message.error('修改失败，请稍后重试');
        } finally {
          this.passwordLoading = false;
        }
      });
    },

    async sendVerifyCode() {
      if (!this.userInfo.email) {
        this.$message.warning('请先绑定邮箱');
        return;
      }

      this.verifyCodeSending = true;
      try {
        const res = await sendEmailCode(this.userInfo.email);
        if (res.code === 200) {
          this.$message.success('验证码已发送，请查收邮件');
          this.startVerifyCountdown();
        } else {
          this.$message.error(res.message || '发送失败');
          this.verifyCodeSending = false;
        }
      } catch (error) {
        console.error('发送验证码失败:', error);
        this.$message.error('发送失败');
        this.verifyCodeSending = false;
      }
    },

    startVerifyCountdown() {
      this.verifyCountdown = 60;
      if (this.verifyTimer) clearInterval(this.verifyTimer);
      this.verifyTimer = setInterval(() => {
        this.verifyCountdown--;
        if (this.verifyCountdown <= 0) {
          clearInterval(this.verifyTimer);
          this.verifyTimer = null;
          this.verifyCountdown = 60;
          this.verifyCodeSending = false;
        }
      }, 1000);
    },

    async nextToNewEmail() {
      this.$refs.verifyForm.validate(async (valid) => {
        if (!valid) return;
        this.emailStep = 1;
      });
    },

    async handleBindEmail() {
      this.$refs.emailForm.validate(async (valid) => {
        if (!valid) return;

        if (this.emailForm.email === this.userInfo.email) {
          this.$message.warning('新邮箱不能与当前邮箱相同');
          return;
        }

        this.emailLoading = true;
        try {
          const res = await updateEmail({
            currentEmail: this.userInfo.email,
            newEmail: this.emailForm.email,
            code: this.verifyForm.code
          });

          if (res.code === 200) {
            this.$message.success('邮箱更换成功');
            this.userInfo.email = this.emailForm.email;
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
            this.showEmailDialog = false;
            this.resetEmailForm();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          console.error('绑定邮箱失败:', error);
          this.$message.error(error.message || '操作失败，请稍后重试');
        } finally {
          this.emailLoading = false;
        }
      });
    },

    resetEmailForm() {
      this.emailStep = 0;
      this.verifyForm.code = '';
      this.emailForm.email = '';
      this.verifyCountdown = 60;
      this.verifyCodeSending = false;
      if (this.verifyTimer) clearInterval(this.verifyTimer);
      this.$nextTick(() => {
        if (this.$refs.verifyForm) this.$refs.verifyForm.clearValidate();
        if (this.$refs.emailForm) this.$refs.emailForm.clearValidate();
      });
    }
  }
};
</script>

<style scoped>
.security-page {
  padding: 0;
  background: transparent;
  min-height: 400px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.security-section {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.section-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s;
}

.section-item:last-child {
  border-bottom: none;
}

.section-item:hover {
  background: #fafbfc;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-icon i {
  font-size: 24px;
  color: white;
}

.item-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-title {
  font-weight: 600;
  color: #2c3e50;
  font-size: 15px;
}

.item-desc {
  font-size: 13px;
  color: #909399;
}

.email-text {
  color: #667eea;
  font-weight: 500;
}

.not-bind {
  color: #e6a23c;
}

.action-btn {
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 13px;
}

/* 对话框样式 */
.security-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.security-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.security-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.security-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.security-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.email-steps {
  margin-bottom: 30px;
}

.email-steps ::v-deep .el-step__title {
  font-size: 14px;
}

.step-desc {
  background: #f0f7ff;
  border-left: 4px solid #667eea;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 13px;
}

.step-desc i {
  color: #667eea;
  font-size: 16px;
}

.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.code-wrapper {
  display: flex;
  gap: 10px;
}

.code-wrapper .el-input {
  flex: 1;
}

.code-btn {
  min-width: 110px;
  border-radius: 8px;
}

.security-dialog ::v-deep .el-form-item {
  margin-bottom: 20px;
}

.security-dialog ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #2c3e50;
}

.security-dialog ::v-deep .el-input__inner {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.security-dialog ::v-deep .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.dialog-footer {
  text-align: right;
  padding: 16px 24px 20px;
  border-top: 1px solid #eef2f6;
}

.dialog-footer .el-button {
  border-radius: 8px;
  padding: 9px 24px;
  font-weight: 500;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.dialog-footer .el-button--primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 响应式 */
@media (max-width: 768px) {
  .section-item {
    padding: 16px;
    flex-wrap: wrap;
    gap: 12px;
  }

  .item-info {
    gap: 12px;
  }

  .item-icon {
    width: 40px;
    height: 40px;
  }

  .item-icon i {
    font-size: 20px;
  }

  .item-title {
    font-size: 14px;
  }

  .code-wrapper {
    flex-direction: column;
  }

  .code-btn {
    width: 100%;
  }

  .step-actions {
    flex-direction: column;
  }

  .step-actions .el-button {
    width: 100%;
  }
}
</style>