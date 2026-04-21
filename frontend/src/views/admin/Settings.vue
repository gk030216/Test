<template>
  <div class="settings-page">
    <div class="page-header">
      <h2 class="page-title">系统设置</h2>
      <p class="page-desc">配置网站基础信息、邮件、支付等参数</p>
    </div>

    <div class="settings-content">
      <el-tabs v-model="activeTab" type="card" class="settings-tabs">

        <!-- 基础设置 -->
        <el-tab-pane label="基础设置" name="basic">
          <div class="tab-content">
            <el-form :model="basicForm" :rules="basicRules" ref="basicForm" label-width="120px">
              <el-form-item label="网站名称" prop="siteName">
                <el-input v-model="basicForm.siteName" placeholder="请输入网站名称" maxlength="50"></el-input>
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
                      <img v-if="basicForm.siteLogo" :src="basicForm.siteLogo" class="logo-preview">
                      <i v-else class="el-icon-plus logo-uploader-icon"></i>
                    </div>
                  </el-upload>
                  <div class="upload-actions">
                    <div class="upload-tip">建议尺寸：200x60px，支持PNG、JPG格式</div>
                    <el-button
                        v-if="basicForm.siteLogo"
                        type="danger"
                        size="small"
                        plain
                        @click="removeLogo"
                        class="remove-logo-btn">
                      <i class="el-icon-delete"></i> 删除Logo
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
                    show-word-limit>
                </el-input>
              </el-form-item>

              <el-form-item label="关键词" prop="siteKeywords">
                <el-input v-model="basicForm.siteKeywords" placeholder="多个关键词用逗号分隔"></el-input>
              </el-form-item>

              <el-form-item label="版权信息" prop="copyright">
                <el-input v-model="basicForm.copyright" placeholder="© 2026 宠物服务系统"></el-input>
              </el-form-item>

              <el-form-item label="备案号" prop="icp">
                <el-input v-model="basicForm.icp" placeholder="京ICP备12345678号"></el-input>
              </el-form-item>

              <el-form-item label="客服电话" prop="servicePhone">
                <el-input v-model="basicForm.servicePhone" placeholder="400-888-6666"></el-input>
              </el-form-item>

              <el-form-item label="客服邮箱" prop="serviceEmail">
                <el-input v-model="basicForm.serviceEmail" placeholder="service@petservice.com"></el-input>
              </el-form-item>

              <el-form-item label="维护模式">
                <el-switch
                    v-model="basicForm.maintenanceMode"
                    active-text="开启"
                    inactive-text="关闭">
                </el-switch>
                <span class="form-tip">开启后仅管理员可访问网站</span>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveBasic" :loading="basicLoading">保存设置</el-button>
                <el-button @click="resetBasic">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 邮件配置 -->
        <el-tab-pane label="邮件配置" name="email">
          <div class="tab-content">
            <el-form :model="emailForm" :rules="emailRules" ref="emailForm" label-width="120px">
              <el-form-item label="SMTP服务器" prop="smtpHost">
                <el-input v-model="emailForm.smtpHost" placeholder="smtp.qq.com"></el-input>
              </el-form-item>

              <el-form-item label="端口" prop="smtpPort">
                <el-input v-model="emailForm.smtpPort" placeholder="465"></el-input>
              </el-form-item>

              <el-form-item label="发件邮箱" prop="senderEmail">
                <el-input v-model="emailForm.senderEmail" placeholder="xxx@qq.com"></el-input>
              </el-form-item>

              <el-form-item label="授权码" prop="senderPassword">
                <el-input
                    v-model="emailForm.senderPassword"
                    type="password"
                    placeholder="邮箱授权码"
                    show-password>
                </el-input>
                <div class="form-tip">
                  <a href="https://service.mail.qq.com/detail/0/75" target="_blank">如何获取QQ邮箱授权码？</a>
                </div>
              </el-form-item>

              <el-form-item label="SSL加密">
                <el-switch v-model="emailForm.sslEnable"></el-switch>
              </el-form-item>

              <el-form-item>
                <el-button @click="testEmail" :loading="testEmailLoading">
                  <i class="el-icon-message"></i> 发送测试邮件
                </el-button>
                <el-button type="primary" @click="saveEmail" :loading="emailLoading">保存配置</el-button>
              </el-form-item>
            </el-form>

            <!-- 测试邮件对话框 -->
            <el-dialog title="发送测试邮件" :visible.sync="testEmailVisible" width="400px" append-to-body>
              <el-form label-width="80px">
                <el-form-item label="测试邮箱">
                  <el-input
                      v-model="testEmailAddress"
                      placeholder="请输入接收测试邮件的邮箱"
                      @keyup.enter="sendTestEmail">
                  </el-input>
                </el-form-item>
              </el-form>
              <span slot="footer">
                <el-button @click="testEmailVisible = false">取消</el-button>
                <el-button type="primary" @click="sendTestEmail" :loading="sendingTest">发送</el-button>
              </span>
            </el-dialog>
          </div>
        </el-tab-pane>

        <!-- 支付配置 -->
        <el-tab-pane label="支付配置" name="pay">
          <div class="tab-content">
            <el-form :model="payForm" ref="payForm" label-width="120px">
              <el-form-item label="支付宝AppId" prop="appId">
                <el-input v-model="payForm.appId" placeholder="请输入支付宝AppId"></el-input>
              </el-form-item>

              <el-form-item label="支付宝公钥" prop="alipayPublicKey">
                <el-input
                    v-model="payForm.alipayPublicKey"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入支付宝公钥">
                </el-input>
              </el-form-item>

              <el-form-item label="商户私钥" prop="appPrivateKey">
                <el-input
                    v-model="payForm.appPrivateKey"
                    type="textarea"
                    :rows="6"
                    placeholder="请输入商户私钥">
                </el-input>
                <div class="form-tip warning">⚠️ 私钥请妥善保管，不要泄露给他人</div>
              </el-form-item>

              <el-form-item label="异步通知地址" prop="notifyUrl">
                <el-input v-model="payForm.notifyUrl" placeholder="https://xxx.cpolar.top/api/pay/alipay/notify"></el-input>
                <div class="form-tip">
                  需要使用内网穿透工具（如cpolar），将本地服务暴露到公网
                </div>
              </el-form-item>

              <el-form-item label="同步跳转地址" prop="returnUrl">
                <el-input v-model="payForm.returnUrl" placeholder="http://localhost:8081/pay/return"></el-input>
              </el-form-item>

              <el-form-item label="支付开关">
                <el-switch v-model="payForm.payEnabled" active-text="开启" inactive-text="关闭"></el-switch>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="savePay" :loading="payLoading">保存配置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- AI配置 -->
        <el-tab-pane label="AI配置" name="ai">
          <div class="tab-content">
            <el-form :model="aiForm" ref="aiForm" label-width="120px">
              <el-form-item label="AI服务商">
                <el-select v-model="aiForm.provider" placeholder="请选择">
                  <el-option label="DeepSeek" value="deepseek"></el-option>
                  <el-option label="OpenAI" value="openai"></el-option>
                  <el-option label="通义千问" value="qwen"></el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="API Key" prop="apiKey">
                <el-input
                    v-model="aiForm.apiKey"
                    type="password"
                    placeholder="请输入API Key"
                    show-password>
                </el-input>
              </el-form-item>

              <el-form-item label="接口地址" prop="baseUrl">
                <el-input v-model="aiForm.baseUrl" placeholder="https://api.deepseek.com"></el-input>
              </el-form-item>

              <el-form-item label="模型名称" prop="model">
                <el-input v-model="aiForm.model" placeholder="deepseek-chat"></el-input>
              </el-form-item>

              <el-form-item label="知识库匹配阈值">
                <el-slider v-model="aiForm.matchThreshold" :min="0" :max="100" show-input></el-slider>
                <span class="form-tip">匹配分数达到阈值时使用知识库答案</span>
              </el-form-item>

              <el-form-item label="本地知识库">
                <el-switch v-model="aiForm.localEnabled" active-text="启用" inactive-text="禁用"></el-switch>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveAI" :loading="aiLoading">保存配置</el-button>
                <el-button @click="testAI" :loading="testAILoading">测试连接</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 上传配置 -->
        <el-tab-pane label="上传配置" name="upload">
          <div class="tab-content">
            <el-form :model="uploadForm" ref="uploadForm" label-width="120px">
              <el-form-item label="上传路径">
                <el-input v-model="uploadForm.uploadPath" placeholder="D:/pet_uploads/"></el-input>
                <div class="form-tip">建议使用绝对路径，确保目录有写入权限</div>
              </el-form-item>

              <el-form-item label="图片大小限制">
                <el-input-number v-model="uploadForm.maxSize" :min="1" :max="10" :step="0.5"></el-input-number>
                <span class="form-tip">MB（建议不超过2MB）</span>
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
                <el-input-number v-model="uploadForm.carouselLimit" :min="1" :max="20"></el-input-number>
                <span class="form-tip">个</span>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveUpload" :loading="uploadLoading">保存配置</el-button>
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
import { uploadImage  } from '@/api/upload';

export default {
  name: 'Settings',
  data() {
    return {
      activeTab: 'basic',

      // 基础设置
      basicLoading: false,
      basicForm: {
        siteName: '',
        siteLogo: '',
        siteDesc: '',
        siteKeywords: '',
        copyright: '',
        icp: '',
        servicePhone: '',
        serviceEmail: '',
        maintenanceMode: false
      },
      basicRules: {
        siteName: [{ required: true, message: '请输入网站名称', trigger: 'blur' }]
      },

      // 邮件配置
      emailLoading: false,
      testEmailLoading: false,
      testEmailVisible: false,
      testEmailAddress: '',
      sendingTest: false,
      emailForm: {
        smtpHost: '',
        smtpPort: '',
        senderEmail: '',
        senderPassword: '',
        sslEnable: true
      },
      emailRules: {
        smtpHost: [{ required: true, message: '请输入SMTP服务器', trigger: 'blur' }],
        smtpPort: [{ required: true, message: '请输入端口', trigger: 'blur' }],
        senderEmail: [
          { required: true, message: '请输入发件邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        senderPassword: [{ required: true, message: '请输入授权码', trigger: 'blur' }]
      },

      // 支付配置
      payLoading: false,
      payForm: {
        appId: '',
        alipayPublicKey: '',
        appPrivateKey: '',
        notifyUrl: '',
        returnUrl: '',
        payEnabled: true
      },

      // AI配置
      aiLoading: false,
      testAILoading: false,
      aiForm: {
        provider: 'deepseek',
        apiKey: '',
        baseUrl: '',
        model: '',
        matchThreshold: 30,
        localEnabled: true
      },

      // 上传配置
      uploadLoading: false,
      uploadForm: {
        uploadPath: 'D:/pet_uploads/',
        maxSize: 2,
        allowedTypes: ['jpg', 'jpeg', 'png', 'gif'],
        carouselLimit: 5
      }
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
      } catch (error) {
        console.error('加载设置失败', error);
      }
    },

// 上传Logo
    beforeLogoUpload(file) {
      const isImage = file.type.startsWith('image/');
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        this.$message.error('只能上传图片文件！');
        return false;
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过2MB！');
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
          this.$message.success('Logo上传成功');
        } else {
          this.$message.error(res.message || '上传失败');
        }
      } catch (error) {
        console.error('上传失败', error);
        this.$message.error('上传失败');
      }
    },

    // 删除Logo
    removeLogo() {
      this.basicForm.siteLogo = '';
      this.$message.success('已删除Logo');
    },

    // 保存基础设置
    async saveBasic() {
      this.$refs.basicForm.validate(async (valid) => {
        if (!valid) return;
        this.basicLoading = true;
        try {
          // siteLogo 可以为空，不需要额外验证
          const res = await saveBasicSettings(this.basicForm);
          if (res.code === 200) {
            this.$message.success('保存成功');
            await this.refreshSettings();
          } else {
            this.$message.error(res.message || '保存失败');
          }
        } catch (error) {
          console.error('保存失败', error);
          this.$message.error('保存失败');
        } finally {
          this.basicLoading = false;
        }
      });
    },

    resetBasic() {
      this.loadSettings();
    },

    // 刷新系统设置
    async refreshSettings() {
      try {
        const res = await getSettings();
        if (res.code === 200) {
          localStorage.setItem('systemSettings', JSON.stringify(res.data));
          this.$bus && this.$bus.$emit('settings-loaded', res.data);
        }
      } catch (error) {
        console.error('刷新设置失败', error);
      }
    },

    // 测试邮件
    testEmail() {
      this.$refs.emailForm.validate(async (valid) => {
        if (!valid) {
          this.$message.warning('请先填写完整的邮件配置');
          return;
        }
        this.testEmailAddress = '';
        this.testEmailVisible = true;
      });
    },

    async sendTestEmail() {
      if (!this.testEmailAddress) {
        this.$message.warning('请输入测试邮箱');
        return;
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.testEmailAddress)) {
        this.$message.warning('请输入正确的邮箱格式');
        return;
      }

      this.sendingTest = true;
      try {
        const res = await testEmailConfig({
          ...this.emailForm,
          testEmail: this.testEmailAddress
        });
        if (res.code === 200) {
          this.$message.success('测试邮件发送成功！请查收');
          this.testEmailVisible = false;
        } else {
          this.$message.error(res.message || '发送失败，请检查配置');
        }
      } catch (error) {
        console.error('测试邮件失败', error);
        this.$message.error('发送失败，请检查网络或配置');
      } finally {
        this.sendingTest = false;
      }
    },

    // 保存邮件配置
    async saveEmail() {
      this.$refs.emailForm.validate(async (valid) => {
        if (!valid) return;
        this.emailLoading = true;
        try {
          const res = await saveEmailSettings(this.emailForm);
          if (res.code === 200) {
            this.$message.success('邮件配置保存成功');
          } else {
            this.$message.error(res.message || '保存失败');
          }
        } catch (error) {
          console.error('保存邮件配置失败', error);
          this.$message.error('保存失败');
        } finally {
          this.emailLoading = false;
        }
      });
    },

    // 保存支付配置
    async savePay() {
      this.payLoading = true;
      try {
        const res = await savePaySettings(this.payForm);
        if (res.code === 200) {
          this.$message.success('保存成功');
        } else {
          this.$message.error(res.message || '保存失败');
        }
      } catch (error) {
        console.error('保存失败', error);
        this.$message.error('保存失败');
      } finally {
        this.payLoading = false;
      }
    },

    // 保存AI配置
    async saveAI() {
      this.aiLoading = true;
      try {
        const res = await saveAISettings(this.aiForm);
        if (res.code === 200) {
          this.$message.success('保存成功');
        } else {
          this.$message.error(res.message || '保存失败');
        }
      } catch (error) {
        console.error('保存失败', error);
        this.$message.error('保存失败');
      } finally {
        this.aiLoading = false;
      }
    },

    // 测试AI连接
    async testAI() {
      this.testAILoading = true;
      try {
        const res = await testAIConfig(this.aiForm);
        if (res.code === 200) {
          this.$message.success('连接成功！AI服务正常');
        } else {
          this.$message.error(res.message || '连接失败');
        }
      } catch (error) {
        console.error('测试AI失败', error);
        this.$message.error('连接失败，请检查配置');
      } finally {
        this.testAILoading = false;
      }
    },

    // 保存上传配置
    async saveUpload() {
      this.uploadLoading = true;
      try {
        const res = await saveUploadSettings(this.uploadForm);
        if (res.code === 200) {
          this.$message.success('保存成功');
        } else {
          this.$message.error(res.message || '保存失败');
        }
      } catch (error) {
        console.error('保存失败', error);
        this.$message.error('保存失败');
      } finally {
        this.uploadLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.settings-page {
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
  color: #999;
}

.settings-content {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.settings-tabs ::v-deep .el-tabs__header {
  margin-bottom: 24px;
}

.settings-tabs ::v-deep .el-tabs__item {
  font-size: 14px;
  font-weight: 500;
}

.tab-content {
  max-width: 800px;
  padding: 10px 0;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

.form-tip.warning {
  color: #e6a23c;
}

.form-tip a {
  color: #409EFF;
  text-decoration: none;
}

.form-tip a:hover {
  text-decoration: underline;
}

/* Logo上传 */
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
  border-radius: 8px;
  cursor: pointer;
  width: 240px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
  overflow: hidden;
  background: #fafafa;
  flex-shrink: 0;
}

.logo-upload:hover {
  border-color: #409EFF;
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
  color: #8c939d;
}

.remove-logo-btn {
  width: fit-content;
  margin-top: 0;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 768px) {
  .settings-page {
    padding: 12px;
  }

  .settings-content {
    padding: 16px;
  }

  .tab-content {
    max-width: 100%;
  }

  .upload-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }

  .upload-actions {
    flex-direction: row;
    align-items: center;
    flex-wrap: wrap;
  }
}
</style>