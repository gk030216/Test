<template>
  <div class="feedback-page">
    <h2 class="page-title">意见反馈</h2>
    <p class="page-desc">遇到问题了吗？请告诉我们，我们会尽快处理</p>

    <div class="feedback-content">
      <!-- 提交反馈 -->
      <div class="submit-section">
        <div class="section-card">
          <div class="card-header">
            <i class="el-icon-edit"></i>
            <span>提交反馈</span>
          </div>
          <el-form :model="feedbackForm" :rules="rules" ref="feedbackForm" label-width="80px">
            <el-form-item label="反馈类型" prop="type">
              <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型" style="width: 100%">
                <el-option label="服务问题" value="service" />
                <el-option label="预约问题" value="appointment" />
                <el-option label="支付问题" value="payment" />
                <el-option label="其他问题" value="other" />
              </el-select>
            </el-form-item>

            <el-form-item label="反馈标题" prop="title">
              <el-input v-model="feedbackForm.title" placeholder="请输入反馈标题" maxlength="50" />
            </el-form-item>

            <el-form-item label="详细描述" prop="content">
              <el-input v-model="feedbackForm.content" type="textarea" :rows="5" placeholder="请详细描述你遇到的问题..." maxlength="500" show-word-limit />
            </el-form-item>

            <!-- 根据类型动态显示关联字段 -->
            <el-form-item label="关联订单" v-if="showOrderNo">
              <el-input v-model="feedbackForm.orderNo" placeholder="请输入订单号" />
              <div class="form-tip">选填，方便我们更快定位问题</div>
            </el-form-item>

            <el-form-item label="关联预约" v-if="showAppointmentNo">
              <el-input v-model="feedbackForm.appointmentNo" placeholder="请输入预约号" />
              <div class="form-tip">选填，方便我们更快定位问题</div>
            </el-form-item>

            <el-form-item label="联系方式" prop="contact">
              <el-input v-model="feedbackForm.contact" placeholder="手机号/邮箱，方便我们联系你" />
            </el-form-item>

            <el-form-item label="上传图片">
              <el-upload action="#" :http-request="uploadImage" list-type="picture-card" :on-preview="handlePreview" :on-remove="handleRemove" :file-list="imageList" :limit="5">
                <i class="el-icon-plus" />
              </el-upload>
              <div class="upload-tip">支持 JPG、PNG 格式，最多5张</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitFeedback" :loading="submitting" class="submit-btn">提交反馈</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 我的反馈历史 -->
      <div class="history-section">
        <div class="section-card">
          <div class="card-header">
            <i class="el-icon-document"></i>
            <span>我的反馈记录</span>
            <div class="header-actions">
              <el-select v-model="filterStatus" placeholder="全部状态" clearable size="small" style="width: 100px" @change="handleFilterChange">
                <el-option label="全部" :value="null" />
                <el-option label="待处理" :value="0" />
                <el-option label="处理中" :value="1" />
                <el-option label="已解决" :value="2" />
              </el-select>
            </div>
          </div>
          <div class="history-list" v-loading="loading">
            <div class="history-item" v-for="item in filteredFeedbackList" :key="item.id" @click="viewDetail(item)">
              <div class="history-header">
                <div class="history-info">
                  <el-tag :type="getTypeTag(item.type)" size="small">{{ getTypeName(item.type) }}</el-tag>
                  <span class="history-title">{{ item.title }}</span>
                </div>
                <div class="history-status">
                  <el-tag :type="getStatusTag(item.status)" size="small">{{ getStatusName(item.status) }}</el-tag>
                </div>
              </div>
              <div class="history-content">{{ truncateText(item.content, 80) }}</div>
              <div class="history-footer">
                <span class="history-time"><i class="el-icon-time"></i> {{ formatDate(item.createTime) }}</span>
                <span v-if="item.status === 2" class="history-resolved"><i class="el-icon-check"></i> 已处理</span>
              </div>
            </div>
            <div class="empty-history" v-if="!loading && filteredFeedbackList.length === 0">
              <i class="el-icon-chat-line-round"></i>
              <p>暂无反馈记录</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 反馈详情对话框 -->
    <el-dialog title="反馈详情" :visible.sync="detailVisible" width="600px" center class="detail-dialog">
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

          <div class="detail-section" v-if="currentFeedback.orderNo">
            <div class="section-label">关联订单</div>
            <div class="section-content">{{ currentFeedback.orderNo }}</div>
          </div>

          <div class="detail-section" v-if="currentFeedback.appointmentNo">
            <div class="section-label">关联预约</div>
            <div class="section-content">{{ currentFeedback.appointmentNo }}</div>
          </div>

          <div class="detail-section" v-if="currentFeedback.images">
            <div class="section-label">相关图片</div>
            <div class="detail-images">
              <el-image v-for="(img, idx) in currentFeedback.images.split(',')" :key="idx" :src="img" :preview-src-list="currentFeedback.images.split(',')" fit="cover" class="detail-img" />
            </div>
          </div>

          <div class="detail-section process-section" v-if="currentFeedback.status !== 0">
            <div class="section-label"><i class="el-icon-user"></i> 处理信息</div>
            <div class="section-content">
              <p v-if="currentFeedback.handlerName"><i class="el-icon-user-solid"></i> 处理人：{{ currentFeedback.handlerName }}</p>
              <p v-if="currentFeedback.processTime"><i class="el-icon-time"></i> 处理时间：{{ formatDate(currentFeedback.processTime) }}</p>
            </div>
          </div>

          <div class="detail-section result-section" v-if="currentFeedback.status === 2 && currentFeedback.processResult">
            <div class="section-label"><i class="el-icon-circle-check"></i> 处理结果</div>
            <div class="section-content result-content">{{ currentFeedback.processResult }}</div>
          </div>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="detailVisible = false">关闭</el-button></span>
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
      filterStatus: null,
      feedbackForm: {
        type: 'other',
        title: '',
        content: '',
        contact: '',
        orderNo: '',
        appointmentNo: ''
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
  computed: {
    // 是否显示关联订单
    showOrderNo() {
      return this.feedbackForm.type === 'payment';
    },
    // 是否显示关联预约
    showAppointmentNo() {
      return this.feedbackForm.type === 'service' || this.feedbackForm.type === 'appointment';
    },
    filteredFeedbackList() {
      if (this.filterStatus === null || this.filterStatus === '') {
        return this.feedbackList;
      }
      return this.feedbackList.filter(item => item.status === this.filterStatus);
    }
  },
  watch: {
    'feedbackForm.type'() {
      // 切换类型时清空关联字段
      this.feedbackForm.orderNo = '';
      this.feedbackForm.appointmentNo = '';
    }
  },
  created() {
    this.loadMyFeedbacks();
  },
  methods: {
    async loadMyFeedbacks() {
      this.loading = true;
      try {
        const res = await getMyFeedbacks({ page: 1, pageSize: 100 });
        if (res.code === 200) {
          this.feedbackList = res.data.list || res.data || [];
        }
      } catch (error) {
        console.error('加载反馈记录失败', error);
      } finally {
        this.loading = false;
      }
    },
    handleFilterChange() {
      // 筛选变化时不需要重新请求，computed 会自动处理
    },
    async uploadImage(file) {
      const formData = new FormData();
      formData.append('file', file.file);
      try {
        const res = await uploadFeedbackImage(formData);
        if (res.code === 200) {
          this.imageUrls.push(res.data.url);
          this.imageList.push({ uid: Date.now(), name: file.file.name, url: res.data.url });
        } else {
          this.$message.error(res.message || '上传失败');
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
            this.$message.error(res.message || '提交失败');
          }
        } catch (error) {
          this.$message.error('提交失败');
        } finally {
          this.submitting = false;
        }
      });
    },
    resetForm() {
      this.feedbackForm = {
        type: 'other',
        title: '',
        content: '',
        contact: '',
        orderNo: '',
        appointmentNo: ''
      };
      this.imageList = [];
      this.imageUrls = [];
      this.$refs.feedbackForm?.clearValidate();
    },
    viewDetail(item) {
      this.currentFeedback = item;
      this.detailVisible = true;
    },
    getTypeName(type) {
      const map = {
        service: '服务问题',
        appointment: '预约问题',
        payment: '支付问题',
        other: '其他'
      };
      return map[type] || type;
    },
    getTypeTag(type) {
      const map = {
        service: 'warning',
        appointment: 'primary',
        payment: 'danger',
        other: 'info'
      };
      return map[type] || 'info';
    },
    getStatusName(status) {
      const map = { 0: '待处理', 1: '处理中', 2: '已解决' };
      return map[status] || '未知';
    },
    getStatusTag(status) {
      const map = { 0: 'danger', 1: 'warning', 2: 'success' };
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
  background: transparent;
  min-height: 400px;
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
  margin-bottom: 24px;
}

.feedback-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* 卡片样式 */
.section-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  transition: all 0.3s;
}

.section-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
}

.card-header i {
  font-size: 18px;
  color: #667eea;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 表单区域 */
.submit-section .section-card {
  padding: 0 20px 20px;
}

.submit-section .card-header {
  margin: 0 -20px 16px;
}

.submit-section ::v-deep .el-form-item {
  margin-bottom: 20px;
}

.submit-section ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #2c3e50;
}

.submit-section ::v-deep .el-input__inner,
.submit-section ::v-deep .el-textarea__inner {
  border-radius: 8px;
  border-color: #e4e7ed;
  transition: all 0.3s;
}

.submit-section ::v-deep .el-input__inner:focus,
.submit-section ::v-deep .el-textarea__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.submit-section ::v-deep .el-select .el-input__inner {
  border-radius: 8px;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 500;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 历史记录区域 */
.history-list {
  max-height: 500px;
  overflow-y: auto;
  padding: 4px 0;
}

.history-list::-webkit-scrollbar {
  width: 6px;
}

.history-list::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 3px;
}

.history-list::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.history-item {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
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
  flex-wrap: wrap;
  gap: 8px;
}

.history-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.history-title {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.history-content {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.history-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.history-time {
  color: #999;
}

.history-time i {
  margin-right: 4px;
}

.history-resolved {
  color: #67c23a;
}

.history-resolved i {
  margin-right: 4px;
}

.empty-history {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-history i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #dcdfe6;
}

.empty-history p {
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
  padding-right: 8px;
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
  flex-wrap: wrap;
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
  font-size: 14px;
}

.section-content {
  color: #666;
  line-height: 1.6;
  font-size: 14px;
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
  border: 1px solid #f0f0f0;
}

.process-section {
  background: #f8f9fc;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.process-section .section-label {
  margin-bottom: 12px;
  color: #667eea;
}

.process-section .section-content p {
  margin: 6px 0;
}

.result-section {
  background: #f0f9f4;
  padding: 16px;
  border-radius: 12px;
  border-left: 3px solid #67c23a;
  margin-bottom: 20px;
}

.result-section .section-label {
  color: #67c23a;
  margin-bottom: 8px;
}

/* 响应式 */
@media (max-width: 900px) {
  .feedback-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}

@media (max-width: 600px) {
  .history-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .history-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .detail-meta {
    flex-direction: column;
    gap: 8px;
  }

  .card-header {
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>