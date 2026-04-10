<template>
  <div class="feedback-page">
    <div class="page-header">
      <h2 class="page-title">意见反馈</h2>
      <p class="page-desc">遇到问题了吗？请告诉我们，我们会尽快处理</p>
    </div>

    <div class="feedback-content">
      <!-- 提交反馈 -->
      <div class="submit-section">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-edit"></i> 提交反馈
          </div>
          <el-form :model="feedbackForm" :rules="rules" ref="feedbackForm" label-width="80px">
            <el-form-item label="反馈类型" prop="type">
              <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型" style="width: 100%">
                <el-option label="服务问题" value="service"></el-option>
                <el-option label="预约问题" value="appointment"></el-option>
                <el-option label="支付问题" value="payment"></el-option>
                <el-option label="其他问题" value="other"></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="反馈标题" prop="title">
              <el-input v-model="feedbackForm.title" placeholder="请输入反馈标题" maxlength="50"></el-input>
            </el-form-item>

            <el-form-item label="详细描述" prop="content">
              <el-input
                  v-model="feedbackForm.content"
                  type="textarea"
                  :rows="5"
                  placeholder="请详细描述你遇到的问题..."
                  maxlength="500"
                  show-word-limit
              ></el-input>
            </el-form-item>

            <el-form-item label="联系方式" prop="contact">
              <el-input v-model="feedbackForm.contact" placeholder="手机号/邮箱，方便我们联系你"></el-input>
            </el-form-item>

            <el-form-item label="上传图片">
              <el-upload
                  action="#"
                  :http-request="uploadImage"
                  list-type="picture-card"
                  :on-preview="handlePreview"
                  :on-remove="handleRemove"
                  :file-list="imageList"
                  :limit="5"
              >
                <i class="el-icon-plus"></i>
              </el-upload>
              <div class="upload-tip">支持 JPG、PNG 格式，最多5张</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitFeedback" :loading="submitting">
                提交反馈
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 我的反馈历史 -->
      <div class="history-section">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-document"></i> 我的反馈记录
          </div>
          <div class="history-list" v-loading="loading">
            <div
                class="history-item"
                v-for="item in feedbackList"
                :key="item.id"
                @click="viewDetail(item)"
            >
              <div class="history-header">
                <div class="history-info">
                  <el-tag :type="getTypeTag(item.type)" size="small">{{ getTypeName(item.type) }}</el-tag>
                  <span class="history-title">{{ item.title }}</span>
                </div>
                <div class="history-status">
                  <el-tag :type="getStatusTag(item.status)" size="small">
                    {{ getStatusName(item.status) }}
                  </el-tag>
                </div>
              </div>
              <div class="history-content">{{ truncateText(item.content, 80) }}</div>
              <div class="history-time">{{ formatDate(item.createTime) }}</div>

              <!-- 处理结果简略 -->
              <div class="history-result-preview" v-if="item.status === 2">
                <i class="el-icon-check"></i> 已处理，点击查看详情
              </div>
            </div>
            <div class="empty-history" v-if="!loading && feedbackList.length === 0">
              <i class="el-icon-chat-line-round"></i>
              <p>暂无反馈记录</p>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 反馈详情对话框 -->
    <el-dialog
        title="反馈详情"
        :visible.sync="detailVisible"
        width="600px"
        center
        class="detail-dialog"
    >
      <div class="detail-content" v-if="currentFeedback">
        <div class="detail-header">
          <div class="detail-tags">
            <el-tag :type="getTypeTag(currentFeedback.type)" size="small">{{ getTypeName(currentFeedback.type) }}</el-tag>
            <el-tag :type="getStatusTag(currentFeedback.status)" size="small">{{ getStatusName(currentFeedback.status) }}</el-tag>
          </div>
          <h3 class="detail-title">{{ currentFeedback.title }}</h3>
          <div class="detail-meta">
            <span><i class="el-icon-time"></i> {{ formatDate(currentFeedback.createTime) }}</span>
            <span v-if="currentFeedback.contact"><i class="el-icon-phone"></i> {{ currentFeedback.contact }}</span>
          </div>
        </div>

        <div class="detail-body">
          <div class="detail-section">
            <div class="section-label">反馈内容</div>
            <div class="section-content">{{ currentFeedback.content }}</div>
          </div>

          <div class="detail-section" v-if="currentFeedback.images">
            <div class="section-label">相关图片</div>
            <div class="detail-images">
              <el-image
                  v-for="(img, idx) in currentFeedback.images.split(',')"
                  :key="idx"
                  :src="img"
                  :preview-src-list="currentFeedback.images.split(',')"
                  fit="cover"
                  class="detail-img"
              ></el-image>
            </div>
          </div>

          <div class="detail-section" v-if="currentFeedback.orderNo || currentFeedback.appointmentNo">
            <div class="section-label">关联信息</div>
            <div class="section-content">
              <p v-if="currentFeedback.orderNo">
                <i class="el-icon-document"></i> 关联订单：{{ currentFeedback.orderNo }}
              </p>
              <p v-if="currentFeedback.appointmentNo">
                <i class="el-icon-s-order"></i> 关联预约：{{ currentFeedback.appointmentNo }}
              </p>
            </div>
          </div>

          <!-- 处理信息 -->
          <div class="detail-section process-section" v-if="currentFeedback.status !== 0">
            <div class="section-label">
              <i class="el-icon-user"></i> 处理信息
            </div>
            <div class="section-content">
              <p v-if="currentFeedback.handlerName">
                <i class="el-icon-user-solid"></i> 处理人：{{ currentFeedback.handlerName }}
              </p>
              <p v-if="currentFeedback.processTime">
                <i class="el-icon-time"></i> 处理时间：{{ formatDate(currentFeedback.processTime) }}
              </p>
            </div>
          </div>

          <div class="detail-section result-section" v-if="currentFeedback.status === 2 && currentFeedback.processResult">
            <div class="section-label">
              <i class="el-icon-circle-check"></i> 处理结果
            </div>
            <div class="section-content result-content">
              {{ currentFeedback.processResult }}
            </div>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog :visible.sync="previewVisible" width="400px" append-to-body>
      <img :src="previewImage" style="width: 100%" alt="预览图片">
    </el-dialog>
  </div>
</template>

<script>
import { submitFeedback, getMyFeedbacks } from '@/api/feedback';
import { uploadFeedbackImage } from '@/api/upload';

export default {
  name: 'Feedback',
  data() {
    return {
      loading: false,
      submitting: false,
      feedbackForm: {
        type: 'other',
        title: '',
        content: '',
        contact: ''
      },
      rules: {
        type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
        title: [{ required: true, message: '请输入反馈标题', trigger: 'blur' }],
        content: [{ required: true, message: '请详细描述问题', trigger: 'blur' }],
        contact: [{ required: true, message: '请留下联系方式', trigger: 'blur' }]
      },
      imageList: [],
      imageUrls: [],
      feedbackList: [],
      previewVisible: false,
      previewImage: '',
      detailVisible: false,
      currentFeedback: null
    };
  },
  created() {
    this.loadMyFeedbacks();
  },
  methods: {
    async loadMyFeedbacks() {
      this.loading = true;
      try {
        const res = await getMyFeedbacks({ page: 1, pageSize: 10 });
        if (res.code === 200) {
          this.feedbackList = res.data || [];
        }
      } catch (error) {
        console.error('加载反馈记录失败', error);
      } finally {
        this.loading = false;
      }
    },

    async uploadImage(file) {
      const formData = new FormData();
      formData.append('file', file.file);
      try {
        const res = await uploadFeedbackImage(formData);
        if (res.code === 200) {
          this.imageUrls.push(res.data.url);
          this.imageList.push({
            uid: Date.now(),
            name: file.file.name,
            url: res.data.url
          });
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
    },

    handlePreview(file) {
      this.previewImage = file.url;
      this.previewVisible = true;
    },

    handleRemove(file) {
      const index = this.imageList.findIndex(f => f.uid === file.uid);
      if (index !== -1) {
        this.imageList.splice(index, 1);
        this.imageUrls.splice(index, 1);
      }
    },

    async submitFeedback() {
      this.$refs.feedbackForm.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        try {
          const res = await submitFeedback({
            ...this.feedbackForm,
            images: this.imageUrls.join(',')
          });
          if (res.code === 200) {
            this.$message.success('反馈提交成功，我们会尽快处理');
            this.resetForm();
            this.loadMyFeedbacks();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('提交失败');
        } finally {
          this.submitting = false;
        }
      });
    },

    resetForm() {
      this.feedbackForm = {type: 'other', title: '', content: '', contact: ''};
      this.imageList = [];
      this.imageUrls = [];
      this.$refs.feedbackForm.clearValidate();
    },

    viewDetail(item) {
      this.currentFeedback = item;
      this.detailVisible = true;
    },

    getTypeName(type) {
      const map = {service: '服务问题', appointment: '预约问题', payment: '支付问题', other: '其他'};
      return map[type] || type;
    },

    getTypeTag(type) {
      const map = {service: 'warning', appointment: 'primary', payment: 'danger', other: 'info'};
      return map[type] || 'info';
    },

    getStatusName(status) {
      const map = {0: '待处理', 1: '处理中', 2: '已解决'};
      return map[status] || '未知';
    },

    getStatusTag(status) {
      const map = {0: 'danger', 1: 'warning', 2: 'success'};
      return map[status] || 'info';
    },

    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.feedback-page {
  padding: 0;
  min-height: 400px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: #999;
}

.feedback-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.card-header i {
  color: #667eea;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.history-list {
  max-height: 500px;
  overflow-y: auto;
}

.history-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}

.history-item:hover {
  background: #f8f9fc;
}

.history-item:last-child {
  border-bottom: none;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.history-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.history-title {
  font-weight: 500;
  color: #2c3e50;
}

.history-content {
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}

.history-time {
  font-size: 12px;
  color: #999;
}

.history-result-preview {
  margin-top: 10px;
  font-size: 12px;
  color: #67c23a;
}

.history-result-preview i {
  margin-right: 4px;
}

.empty-history {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-history i {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 详情对话框 */
.detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.detail-dialog ::v-deep .el-dialog__close {
  color: white;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.detail-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #999;
}

.detail-meta i {
  margin-right: 4px;
}

.detail-section {
  margin-bottom: 20px;
}

.section-label {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.section-content {
  color: #666;
  line-height: 1.6;
}

.detail-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
}

.process-section {
  background: #f8f9fc;
  padding: 12px;
  border-radius: 8px;
}

.result-section {
  background: #f0f9f4;
  padding: 16px;
  border-radius: 8px;
  border-left: 3px solid #67c23a;
}

.result-section .section-label {
  color: #67c23a;
}

.result-content {
  color: #666;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .feedback-content {
    grid-template-columns: 1fr;
  }

  .detail-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>