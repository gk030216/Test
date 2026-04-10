<template>
  <div class="staff-feedback">
<!--    <div class="page-header">-->
<!--      <h2 class="page-title">异常反馈</h2>-->
<!--      <p class="page-desc">处理用户在服务过程中遇到的问题和反馈</p>-->
<!--    </div>-->

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-warning"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.pending || 0 }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-processing">
            <i class="el-icon-loading"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.processing || 0 }}</div>
            <div class="stat-label">处理中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-resolved">
            <i class="el-icon-circle-check"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.resolved || 0 }}</div>
            <div class="stat-label">已解决</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-today">
            <i class="el-icon-date"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.today || 0 }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索用户/内容"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          ></el-input>
        </div>
        <el-select
            v-model="searchForm.type"
            placeholder="反馈类型"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="服务问题" value="service"></el-option>
          <el-option label="预约问题" value="appointment"></el-option>
          <el-option label="支付问题" value="payment"></el-option>
          <el-option label="其他问题" value="other"></el-option>
        </el-select>
        <el-select
            v-model="searchForm.status"
            placeholder="处理状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="待处理" :value="0"></el-option>
          <el-option label="处理中" :value="1"></el-option>
          <el-option label="已解决" :value="2"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
      </div>
    </div>

    <!-- 反馈列表 -->
    <div class="feedback-list" v-loading="loading">
      <div class="empty-state" v-if="feedbackList.length === 0">
        <i class="el-icon-warning"></i>
        <p>暂无异常反馈</p>
      </div>

      <div class="feedback-card" v-for="item in feedbackList" :key="item.id">
        <div class="card-header">
          <div class="feedback-info">
            <el-tag :type="getTypeTag(item.type)" size="small">{{ getTypeName(item.type) }}</el-tag>
            <span class="feedback-user">
              <i class="el-icon-user"></i> {{ item.userName }}
            </span>
            <span class="feedback-time">
              <i class="el-icon-time"></i> {{ formatRelativeTime(item.createTime) }}
            </span>
          </div>
          <div class="feedback-status">
            <el-tag :type="getStatusTag(item.status)" size="small">
              {{ getStatusName(item.status) }}
            </el-tag>
          </div>
        </div>

        <div class="card-body">
          <div class="feedback-title">{{ item.title }}</div>
          <div class="feedback-content">{{ item.content }}</div>
          <div class="feedback-images" v-if="item.images">
            <el-image
                v-for="(img, idx) in item.images.split(',')"
                :key="idx"
                :src="img"
                :preview-src-list="item.images.split(',')"
                fit="cover"
                class="feedback-img"
            ></el-image>
          </div>
          <div class="feedback-meta">
            <span v-if="item.orderNo">
              <i class="el-icon-document"></i> 关联订单：{{ item.orderNo }}
            </span>
            <span v-if="item.appointmentNo">
              <i class="el-icon-s-order"></i> 关联预约：{{ item.appointmentNo }}
            </span>
            <span>
              <i class="el-icon-phone"></i> 联系方式：{{ item.contact }}
            </span>
          </div>
        </div>

        <!-- 处理记录 -->
        <div class="process-section" v-if="item.processRecords && item.processRecords.length > 0">
          <div class="process-title">
            <i class="el-icon-document"></i> 处理记录
          </div>
          <div class="process-list">
            <div class="process-item" v-for="record in item.processRecords" :key="record.id">
              <div class="process-header">
                <span class="process-user">{{ record.handlerName }}</span>
                <span class="process-time">{{ formatDate(record.createTime) }}</span>
              </div>
              <div class="process-content">{{ record.content }}</div>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <el-button
              v-if="item.status === 0"
              type="primary"
              size="small"
              @click="handleProcess(item)"
          >
            <i class="el-icon-check"></i> 开始处理
          </el-button>
          <el-button
              v-if="item.status === 1"
              type="success"
              size="small"
              @click="handleResolve(item)"
          >
            <i class="el-icon-circle-check"></i> 标记解决
          </el-button>
          <el-button
              size="small"
              plain
              @click="openReplyDialog(item)"
          >
            <i class="el-icon-edit"></i> 添加记录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
          @current-change="handlePageChange"
          :current-page="page"
          :page-size="pageSize"
          layout="prev, pager, next"
          :total="total"
          background
      ></el-pagination>
    </div>

    <!-- 处理记录对话框 -->
    <el-dialog
        title="添加处理记录"
        :visible.sync="replyVisible"
        width="500px"
        center
        class="reply-dialog"
    >
      <div class="dialog-content">
        <el-form label-width="80px">
          <el-form-item label="处理内容">
            <el-input
                v-model="processContent"
                type="textarea"
                :rows="4"
                placeholder="请输入处理记录..."
                maxlength="500"
                show-word-limit
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer">
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess" :loading="processLoading">
          提交
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getFeedbackList,
  getFeedbackStatistics,
  processFeedback,
  resolveFeedback,
  addProcessRecord
} from '@/api/feedback';

export default {
  name: 'StaffFeedback',
  data() {
    return {
      loading: false,
      processLoading: false,
      feedbackList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      searchForm: {
        keyword: '',
        type: '',
        status: ''
      },
      statistics: {
        pending: 0,
        processing: 0,
        resolved: 0,
        today: 0
      },
      replyVisible: false,
      processContent: '',
      currentFeedback: null,
      userInfo: {}
    };
  },
  created() {
    this.loadUserInfo();
    this.loadList();
    this.loadStatistics();
  },
  methods: {
    loadUserInfo() {
      const info = localStorage.getItem('userInfo');
      if (info) {
        this.userInfo = JSON.parse(info);
      }
    },

    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          type: this.searchForm.type || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined
        };
        const res = await getFeedbackList(params);
        if (res.code === 200) {
          this.feedbackList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载反馈列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadStatistics() {
      try {
        const res = await getFeedbackStatistics();
        if (res.code === 200) {
          this.statistics = res.data || {};
        }
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },

    getTypeName(type) {
      const map = {
        'service': '服务问题',
        'appointment': '预约问题',
        'payment': '支付问题',
        'other': '其他问题'
      };
      return map[type] || '未知类型';
    },

    getTypeTag(type) {
      const map = {
        'service': 'warning',
        'appointment': 'primary',
        'payment': 'danger',
        'other': 'info'
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

    handleSearch() {
      this.page = 1;
      this.loadList();
    },

    handleReset() {
      this.searchForm = { keyword: '', type: '', status: '' };
      this.page = 1;
      this.loadList();
    },

    handlePageChange(page) {
      this.page = page;
      this.loadList();
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    formatRelativeTime(date) {
      if (!date) return '';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;

      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;

      return this.formatDate(date);
    },

    async handleProcess(row) {
      this.$confirm('确认开始处理该反馈吗？', '提示', { type: 'info' }).then(async () => {
        try {
          await processFeedback(row.id);
          this.$message.success('已开始处理');
          this.loadList();
          this.loadStatistics();
        } catch (error) {
          this.$message.error('操作失败');
        }
      }).catch(() => {});
    },

    async handleResolve(row) {
      this.$confirm('确认该问题已解决吗？', '提示', { type: 'info' }).then(async () => {
        // 打开输入对话框让员工填写处理结果
        this.$prompt('请输入处理结果', '标记解决', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputType: 'textarea',
          inputPlaceholder: '请描述处理结果...'
        }).then(async ({ value }) => {
          if (!value) {
            this.$message.warning('请输入处理结果');
            return;
          }
          try {
            await resolveFeedback(row.id, value);
            this.$message.success('已标记为解决');
            this.loadList();
            this.loadStatistics();
          } catch (error) {
            this.$message.error('操作失败');
          }
        }).catch(() => {});
      }).catch(() => {});
    },

    openReplyDialog(row) {
      this.currentFeedback = row;
      this.processContent = '';
      this.replyVisible = true;
    },

    async submitProcess() {
      if (!this.processContent.trim()) {
        this.$message.warning('请输入处理内容');
        return;
      }

      this.processLoading = true;
      try {
        await addProcessRecord({
          feedbackId: this.currentFeedback.id,
          content: this.processContent
        });
        this.$message.success('记录已添加');
        this.replyVisible = false;
        this.loadList();
      } catch (error) {
        this.$message.error('操作失败');
      } finally {
        this.processLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.staff-feedback {
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

.stat-card {
  border-radius: 16px;
}

.stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f56c6c, #ff8e8e);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

.stat-processing { background: linear-gradient(135deg, #e6a23c, #f0b45c); }
.stat-resolved { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-today { background: linear-gradient(135deg, #409EFF, #66b1ff); }

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.action-bar {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin: 20px 0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.action-right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-wrapper {
  position: relative;
  display: inline-block;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #909399;
  font-size: 16px;
  z-index: 1;
}

.search-input {
  width: 200px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.status-select {
  width: 120px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  border-radius: 8px;
  padding: 8px 16px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feedback-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.feedback-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.feedback-info {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #666;
  font-size: 13px;
}

.feedback-info i {
  margin-right: 4px;
  color: #667eea;
}

.card-body {
  margin-bottom: 16px;
}

.feedback-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
}

.feedback-content {
  color: #5a6874;
  line-height: 1.6;
  margin-bottom: 12px;
}

.feedback-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.feedback-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
}

.feedback-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.feedback-meta i {
  margin-right: 4px;
}

.process-section {
  background: #f8f9fc;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.process-title {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 12px;
}

.process-title i {
  margin-right: 6px;
  color: #667eea;
}

.process-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.process-item {
  padding-bottom: 12px;
  border-bottom: 1px solid #e0e0e0;
}

.process-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.process-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.process-user {
  font-weight: 500;
  color: #2c3e50;
}

.process-time {
  font-size: 12px;
  color: #999;
}

.process-content {
  color: #666;
  line-height: 1.5;
  font-size: 14px;
}

.card-footer {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 20px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #dcdfe6;
}

.empty-state p {
  font-size: 16px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 对话框样式 */
.reply-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.reply-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.reply-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.dialog-content {
  padding: 10px 0;
}

@media (max-width: 768px) {
  .staff-feedback {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
  }

  .action-right {
    justify-content: center;
  }

  .feedback-info {
    flex-wrap: wrap;
    gap: 8px;
  }

  .feedback-meta {
    flex-direction: column;
    gap: 8px;
  }

  .card-footer {
    flex-wrap: wrap;
  }
}
</style>