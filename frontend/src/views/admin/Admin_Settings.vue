<template>
  <div class="settings-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">系统设置</h2>
      <p class="page-desc">配置网站基础信息、邮件、支付等参数</p>
    </div>

    <!-- 设置卡片 -->
    <div class="settings-card">
      <el-tabs v-model="activeTab" class="settings-tabs">
        <!-- 基础设置 -->
        <el-tab-pane label="基础设置" name="basic">
          <div class="tab-content">
            <el-form :model="basicForm" :rules="basicRules" ref="basicForm" label-width="120px">
              <el-form-item label="网站名称" prop="siteName">
                <el-input v-model="basicForm.siteName" placeholder="请输入网站名称" maxlength="50" />
              </el-form-item>

              <el-form-item label="网站Logo">
                <div class="upload-wrapper">
                  <el-upload
                    class="logo-upload"
                    action="#"
                    :http-request="uploadLogo"
                    :show-file-list="false"
                    :before-upload="beforeLogoUpload"
                  >
                    <div class="logo-preview-wrapper">
                      <img v-if="basicForm.siteLogo" :src="basicForm.siteLogo" class="logo-preview" />
                      <i v-else class="el-icon-plus logo-uploader-icon" />
                    </div>
                  </el-upload>
                  <div class="upload-actions">
                    <span class="upload-tip">建议尺寸 200x60px，PNG/JPG，≤ 2MB</span>
                    <el-button
                      v-if="basicForm.siteLogo"
                      type="danger"
                      size="small"
                      plain
                      @click="removeLogo"
                    >
                      <i class="el-icon-delete" /> 删除Logo
                    </el-button>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="网站描述" prop="siteDesc">
                <el-input
                  v-model="basicForm.siteDesc"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入网站描述"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="关键词" prop="siteKeywords">
                <el-input v-model="basicForm.siteKeywords" placeholder="多个关键词用逗号分隔" />
              </el-form-item>

              <el-form-item label="版权信息" prop="copyright">
                <el-input v-model="basicForm.copyright" placeholder="© 2026 宠物服务系统" />
              </el-form-item>

              <el-form-item label="备案号" prop="icp">
                <el-input v-model="basicForm.icp" placeholder="京ICP备12345678号" />
              </el-form-item>

              <el-form-item label="客服电话" prop="servicePhone">
                <el-input v-model="basicForm.servicePhone" placeholder="400-888-6666" />
              </el-form-item>

              <el-form-item label="客服邮箱" prop="serviceEmail">
                <el-input v-model="basicForm.serviceEmail" placeholder="service@petservice.com" />
              </el-form-item>

              <el-form-item label="维护模式">
                <el-switch
                  v-model="basicForm.maintenanceMode"
                  active-color="#f59e4b"
                  active-text="开启"
                  inactive-text="关闭"
                />
                <span class="form-tip">开启后仅管理员可访问网站</span>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveBasic" :loading="basicLoading" class="warm-btn">保存设置</el-button>
                <el-button @click="resetBasic" class="reset-btn">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 邮件配置 -->
        <el-tab-pane label="邮件配置" name="email">
          <div class="tab-content">
            <el-form :model="emailForm" :rules="emailRules" ref="emailForm" label-width="120px">
              <el-form-item label="SMTP服务器" prop="smtpHost">
                <el-input v-model="emailForm.smtpHost" placeholder="smtp.qq.com" />
              </el-form-item>

              <el-form-item label="端口" prop="smtpPort">
                <el-input v-model="emailForm.smtpPort" placeholder="465" />
              </el-form-item>

              <el-form-item label="发件邮箱" prop="senderEmail">
                <el-input v-model="emailForm.senderEmail" placeholder="xxx@qq.com" />
              </el-form-item>

              <el-form-item label="授权码" prop="senderPassword">
                <el-input
                  v-model="emailForm.senderPassword"
                  type="password"
                  placeholder="邮箱授权码"
                  show-password
                />
                <div class="form-tip">
                  <a href="https://service.mail.qq.com/detail/0/75" target="_blank">如何获取QQ邮箱授权码？</a>
                </div>
              </el-form-item>

              <el-form-item label="SSL加密">
                <el-switch v-model="emailForm.sslEnable" active-color="#f59e4b" />
              </el-form-item>

              <el-form-item>
                <el-button @click="testEmail" :loading="testEmailLoading">
                  <i class="el-icon-message" /> 发送测试邮件
                </el-button>
                <el-button type="primary" @click="saveEmail" :loading="emailLoading" class="warm-btn">保存配置</el-button>
              </el-form-item>
            </el-form>

            <!-- 测试邮件对话框 -->
            <el-dialog title="发送测试邮件" :visible.sync="testEmailVisible" width="420px" append-to-body class="test-email-dialog">
              <div class="test-email-body">
                <el-form label-width="80px">
                  <el-form-item label="测试邮箱">
                    <el-input
                      v-model="testEmailAddress"
                      placeholder="请输入接收测试邮件的邮箱"
                      @keyup.enter="sendTestEmail"
                    />
                  </el-form-item>
                </el-form>
              </div>
              <span slot="footer">
                <el-button @click="testEmailVisible = false">取 消</el-button>
                <el-button type="primary" @click="sendTestEmail" :loading="sendingTest" class="warm-btn">发 送</el-button>
              </span>
            </el-dialog>
          </div>
        </el-tab-pane>

        <!-- 支付配置 -->
        <el-tab-pane label="支付配置" name="pay">
          <div class="tab-content">
            <el-form :model="payForm" ref="payForm" label-width="130px">
              <el-form-item label="支付宝AppId">
                <el-input v-model="payForm.appId" placeholder="请输入支付宝AppId" />
              </el-form-item>

              <el-form-item label="支付宝公钥">
                <el-input v-model="payForm.alipayPublicKey" type="textarea" :rows="4" placeholder="请输入支付宝公钥" />
              </el-form-item>

              <el-form-item label="商户私钥">
                <el-input v-model="payForm.appPrivateKey" type="textarea" :rows="6" placeholder="请输入商户私钥" />
                <div class="form-tip form-tip-warning">私钥请妥善保管，不要泄露给他人</div>
              </el-form-item>

              <el-form-item label="异步通知地址">
                <el-input v-model="payForm.notifyUrl" placeholder="https://xxx.cpolar.top/api/pay/alipay/notify" />
                <div class="form-tip">需要使用内网穿透工具（如cpolar），将本地服务暴露到公网</div>
              </el-form-item>

              <el-form-item label="同步跳转地址">
                <el-input v-model="payForm.returnUrl" placeholder="http://localhost:8081/pay/return" />
              </el-form-item>

              <el-form-item label="支付开关">
                <el-switch v-model="payForm.payEnabled" active-color="#f59e4b" active-text="开启" inactive-text="关闭" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="savePay" :loading="payLoading" class="warm-btn">保存配置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- AI配置 -->
        <el-tab-pane label="AI配置" name="ai">
          <div class="tab-content">
            <el-form :model="aiForm" ref="aiForm" label-width="140px">
              <el-form-item label="AI服务商">
                <el-select v-model="aiForm.provider" placeholder="请选择">
                  <el-option label="DeepSeek" value="deepseek" />
                  <el-option label="OpenAI" value="openai" />
                  <el-option label="通义千问" value="qwen" />
                </el-select>
              </el-form-item>

              <el-form-item label="API Key">
                <el-input v-model="aiForm.apiKey" type="password" placeholder="请输入API Key" show-password />
              </el-form-item>

              <el-form-item label="接口地址">
                <el-input v-model="aiForm.baseUrl" placeholder="https://api.deepseek.com" />
              </el-form-item>

              <el-form-item label="模型名称">
                <el-input v-model="aiForm.model" placeholder="deepseek-chat" />
              </el-form-item>

              <el-form-item label="知识库匹配阈值">
                <el-slider v-model="aiForm.matchThreshold" :min="0" :max="100" show-input />
                <span class="form-tip">匹配分数达到阈值时使用知识库答案</span>
              </el-form-item>

              <el-form-item label="本地知识库">
                <el-switch v-model="aiForm.localEnabled" active-color="#f59e4b" active-text="启用" inactive-text="禁用" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveAI" :loading="aiLoading" class="warm-btn">保存配置</el-button>
                <el-button @click="testAI" :loading="testAILoading">测试连接</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 上传配置 -->
        <el-tab-pane label="上传配置" name="upload">
          <div class="tab-content">
            <el-form :model="uploadForm" ref="uploadForm" label-width="140px">
              <el-form-item label="上传路径">
                <el-input v-model="uploadForm.uploadPath" placeholder="D:/pet_uploads/" />
                <div class="form-tip">建议使用绝对路径，确保目录有写入权限</div>
              </el-form-item>

              <el-form-item label="图片大小限制">
                <el-input-number v-model="uploadForm.maxSize" :min="1" :max="10" :step="0.5" />
                <span class="form-tip-inline">MB（建议不超过 2MB）</span>
              </el-form-item>

              <el-form-item label="支持格式">
                <el-checkbox-group v-model="uploadForm.allowedTypes">
                  <el-checkbox label="jpg">JPG</el-checkbox>
                  <el-checkbox label="jpeg">JPEG</el-checkbox>
                  <el-checkbox label="png">PNG</el-checkbox>
                  <el-checkbox label="gif">GIF</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item label="轮播图数量限制">
                <el-input-number v-model="uploadForm.carouselLimit" :min="1" :max="20" />
                <span class="form-tip-inline">个</span>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveUpload" :loading="uploadLoading" class="warm-btn">保存配置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import {
  getSettings,
  saveBasicSettings,
  saveEmailSettings,
  savePaySettings,
  saveAISettings,
  saveUploadSettings,
  testEmailConfig,
  testAIConfig
} from '@/api/settings';
import { uploadImage } from '@/api/upload';

export default {
  name: 'Admin_Settings',
  data() {
    return {
      activeTab: 'basic',
      basicLoading: false,
      basicForm: {
        siteName: '', siteLogo: '', siteDesc: '', siteKeywords: '',
        copyright: '', icp: '', servicePhone: '', serviceEmail: '',
        maintenanceMode: false
      },
      basicRules: {
        siteName: [{ required: true, message: '请输入网站名称', trigger: 'blur' }]
      },
      emailLoading: false,
      testEmailLoading: false,
      testEmailVisible: false,
      testEmailAddress: '',
      sendingTest: false,
      emailForm: { smtpHost: '', smtpPort: '', senderEmail: '', senderPassword: '', sslEnable: true },
      emailRules: {
        smtpHost: [{ required: true, message: '请输入SMTP服务器', trigger: 'blur' }],
        smtpPort: [{ required: true, message: '请输入端口', trigger: 'blur' }],
        senderEmail: [
          { required: true, message: '请输入发件邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        senderPassword: [{ required: true, message: '请输入授权码', trigger: 'blur' }]
      },
      payLoading: false,
      payForm: { appId: '', alipayPublicKey: '', appPrivateKey: '', notifyUrl: '', returnUrl: '', payEnabled: true },
      aiLoading: false,
      testAILoading: false,
      aiForm: { provider: 'deepseek', apiKey: '', baseUrl: '', model: '', matchThreshold: 30, localEnabled: true },
      uploadLoading: false,
      uploadForm: { uploadPath: 'D:/pet_uploads/', maxSize: 2, allowedTypes: ['jpg', 'jpeg', 'png', 'gif'], carouselLimit: 5 }
    };
  },
  created() {
    this.loadSettings();
  },
  methods: {
    async loadSettings() {
      try {
        const res = await getSettings();
        if (res.code === 200) {
          const data = res.data;
          this.basicForm = { ...this.basicForm, ...data.basic };
          this.emailForm = { ...this.emailForm, ...data.email };
          this.payForm = { ...this.payForm, ...data.pay };
          this.aiForm = { ...this.aiForm, ...data.ai };
          this.uploadForm = { ...this.uploadForm, ...data.upload };
        }
      } catch (e) {
        this.$message.error(e?.message || '加载设置失败，请稍后重试');
      }
    },

    beforeLogoUpload(file) {
      if (!file.type.startsWith('image/')) {
        this.$message.error('只能上传图片文件');
        return false;
      }
      if (file.size / 1024 / 1024 > 2) {
        this.$message.error('图片大小不能超过 2MB');
        return false;
      }
      return true;
    },

    async uploadLogo(options) {
      const formData = new FormData();
      formData.append('file', options.file);
      try {
        const res = await uploadImage(formData);
        if (res.code === 200) {
          this.basicForm.siteLogo = res.data.url;
          this.$message.success('Logo 上传成功');
        } else {
          this.$message.error(res.message || '上传失败，请稍后重试');
        }
      } catch (e) {
        this.$message.error(e?.message || '上传失败，请稍后重试');
      }
    },

    removeLogo() {
      this.$confirm('确定要删除当前 Logo 吗？', '提示', { type: 'warning' }).then(() => {
        this.basicForm.siteLogo = '';
        this.$message.success('已删除 Logo，保存后生效');
      }).catch(() => {});
    },

    async saveBasic() {
      try {
        const valid = await this.$refs.basicForm.validate().catch(() => false);
        if (!valid) return;
        this.basicLoading = true;
        const res = await saveBasicSettings(this.basicForm);
        if (res.code === 200) {
          this.$message.success('基础设置保存成功');
          this.refreshSettings();
        } else {
          this.$message.error(res.message || '保存失败，请稍后重试');
        }
      } catch (e) {
        this.$message.error(e?.message || '保存失败，请稍后重试');
      } finally {
        this.basicLoading = false;
      }
    },

    resetBasic() { this.loadSettings(); },

    async refreshSettings() {
      try {
        const res = await getSettings();
        if (res.code === 200) {
          localStorage.setItem('systemSettings', JSON.stringify(res.data));
          this.$bus && this.$bus.$emit('settings-loaded', res.data);
        }
      } catch (e) {
        // non-critical; silently fail
      }
    },

    testEmail() {
      this.$refs.emailForm.validate(async (valid) => {
        if (!valid) { this.$message.warning('请先填写完整的邮件配置'); return; }
        this.testEmailAddress = '';
        this.testEmailVisible = true;
      });
    },

    async sendTestEmail() {
      if (!this.testEmailAddress) {
        this.$message.warning('请输入测试邮箱');
        return;
      }
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.testEmailAddress)) {
        this.$message.warning('请输入正确的邮箱格式');
        return;
      }
      this.sendingTest = true;
      try {
        const res = await testEmailConfig({ ...this.emailForm, testEmail: this.testEmailAddress });
        if (res.code === 200) {
          this.$message.success('测试邮件发送成功，请查收');
          this.testEmailVisible = false;
        } else {
          this.$message.error(res.message || '发送失败，请检查配置');
        }
      } catch (e) {
        this.$message.error(e?.message || '发送失败，请检查网络或配置');
      } finally {
        this.sendingTest = false;
      }
    },

    async saveEmail() {
      try {
        const valid = await this.$refs.emailForm.validate().catch(() => false);
        if (!valid) return;
        this.emailLoading = true;
        const res = await saveEmailSettings(this.emailForm);
        if (res.code === 200) {
          this.$message.success('邮件配置保存成功');
        } else {
          this.$message.error(res.message || '保存失败，请稍后重试');
        }
      } catch (e) {
        this.$message.error(e?.message || '保存失败，请稍后重试');
      } finally {
        this.emailLoading = false;
      }
    },

    async savePay() {
      this.payLoading = true;
      try {
        const res = await savePaySettings(this.payForm);
        if (res.code === 200) {
          this.$message.success('支付配置保存成功');
        } else {
          this.$message.error(res.message || '保存失败，请稍后重试');
        }
      } catch (e) {
        this.$message.error(e?.message || '保存失败，请稍后重试');
      } finally {
        this.payLoading = false;
      }
    },

    async saveAI() {
      this.aiLoading = true;
      try {
        const res = await saveAISettings(this.aiForm);
        if (res.code === 200) {
          this.$message.success('AI 配置保存成功');
        } else {
          this.$message.error(res.message || '保存失败，请稍后重试');
        }
      } catch (e) {
        this.$message.error(e?.message || '保存失败，请稍后重试');
      } finally {
        this.aiLoading = false;
      }
    },

    async testAI() {
      this.testAILoading = true;
      try {
        const res = await testAIConfig(this.aiForm);
        if (res.code === 200) {
          this.$message.success('连接成功，AI 服务正常');
        } else {
          this.$message.error(res.message || '连接失败，请检查配置');
        }
      } catch (e) {
        this.$message.error(e?.message || '连接失败，请检查配置');
      } finally {
        this.testAILoading = false;
      }
    },

    async saveUpload() {
      this.uploadLoading = true;
      try {
        const res = await saveUploadSettings(this.uploadForm);
        if (res.code === 200) {
          this.$message.success('上传配置保存成功');
        } else {
          this.$message.error(res.message || '保存失败，请稍后重试');
        }
      } catch (e) {
        this.$message.error(e?.message || '保存失败，请稍后重试');
      } finally {
        this.uploadLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.settings-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 页面头部 ===== */
.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #3d2e2a;
  margin-bottom: 6px;
}

.page-desc {
  font-size: 13px;
  color: #b8a8a0;
}

/* ===== 设置卡片 ===== */
.settings-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 28px;
  border: 1px solid #f5ece6;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

/* ===== 标签页 ===== */
.settings-tabs ::v-deep .el-tabs__header {
  margin-bottom: 20px;
  border-bottom: 1px solid #f5ece6;
}

.settings-tabs ::v-deep .el-tabs__item {
  font-size: 14px;
  font-weight: 500;
  color: #8a7a72;
}

.settings-tabs ::v-deep .el-tabs__item.is-active {
  color: #f59e4b;
}

.settings-tabs ::v-deep .el-tabs__active-bar {
  background-color: #f59e4b;
}

.settings-tabs ::v-deep .el-tabs__nav-wrap::after {
  background-color: #f5ece6;
}

.tab-content {
  max-width: 800px;
  padding: 4px 0;
}

/* ===== 表单通用 ===== */
.form-tip {
  font-size: 12px;
  color: #b8a8a0;
  margin-top: 6px;
}

.form-tip a {
  color: #f59e4b;
  text-decoration: none;
}

.form-tip a:hover {
  text-decoration: underline;
}

.form-tip-inline {
  font-size: 12px;
  color: #b8a8a0;
  margin-left: 10px;
}

.form-tip-warning {
  color: #e6a23c;
}

/* ===== 按钮 ===== */
.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important;
  color: #fff !important;
  transition: all 0.2s;
}

.warm-btn:hover { opacity: 0.88; transform: translateY(-1px); }

.reset-btn:hover { background: #fdf6f0; border-color: #f5d5c0; }

/* ===== Logo上传 ===== */
.upload-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.upload-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.logo-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 10px;
  cursor: pointer;
  width: 240px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s, background 0.3s;
  overflow: hidden;
  background: #fafaf8;
  flex-shrink: 0;
}

.logo-upload:hover {
  border-color: #f59e4b;
  background: #fdf6f0;
}

.logo-upload ::v-deep .el-upload {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-preview-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-preview {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  padding: 4px;
}

.logo-uploader-icon {
  font-size: 32px;
  color: #c0b8b0;
}

.upload-tip {
  font-size: 12px;
  color: #b8a8a0;
}

/* ===== focus 环 ===== */
.tab-content ::v-deep .el-input__inner:focus,
.tab-content ::v-deep .el-textarea__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 2px rgba(245, 158, 75, 0.08);
}

.tab-content ::v-deep .el-select .el-input.is-focus .el-input__inner {
  border-color: #f59e4b;
}

/* ===== el-slider ===== */
.tab-content ::v-deep .el-slider__bar {
  background-color: #f59e4b;
}

.tab-content ::v-deep .el-slider__button {
  border-color: #f59e4b;
}

/* ===== checkbox ===== */
.tab-content ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #f59e4b;
  border-color: #f59e4b;
}

.tab-content ::v-deep .el-checkbox__input.is-checked + .el-checkbox__label {
  color: #f59e4b;
}

/* ===== 测试邮件对话框 ===== */
.test-email-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.test-email-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 18px 24px;
  margin: 0;
}

.test-email-dialog ::v-deep .el-dialog__title {
  color: #fff;
  font-weight: 600;
  font-size: 18px;
}

.test-email-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 20px; }

.test-email-body { padding: 10px 0; }

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .settings-page { padding: 12px; }
  .settings-card { padding: 16px; }
  .tab-content { max-width: 100%; }
  .upload-wrapper { flex-direction: column; align-items: flex-start; }
}
</style>
