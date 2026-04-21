<template>
  <div class="staff-feedback">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-pending">
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
      <div class="action-left">
        <h3 class="section-title">异常反馈列表</h3>
      </div>
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
          <i class="el-icon-search"></i> 搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
      </div>
    </div>

    <!-- 反馈列表 -->
    <div class="feedback-list" v-loading="loading">
      <div class="empty-state" v-if="feedbackList.length === 0">
        <i class="el-icon-warning-outline"></i>
        <p>暂无异常反馈</p>
        <span>所有反馈都已处理完毕</span>
      </div>

      <div class="feedback-card" v-for="item in feedbackList" :key="item.id">
        <div class="card-header">
          <div class="feedback-info">
            <el-tag :type="getTypeTag(item.type)" size="small" effect="dark">
              {{ getTypeName(item.type) }}
            </el-tag>
            <div class="feedback-user">
              <el-avatar :size="28" :src="item.userAvatar" class="user-avatar">
                {{ (item.userNickname || item.userName || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <span>{{ item.userNickname || item.userName || '匿名用户' }}</span>
            </div>
            <div class="feedback-time">
              <i class="el-icon-time"></i> {{ formatRelativeTime(item.createTime) }}
            </div>
          </div>
          <div class="feedback-status">
            <span :class="['status-badge', getStatusClass(item.status)]">
              <i :class="getStatusIcon(item.status)"></i>
              {{ getStatusName(item.status) }}
            </span>
          </div>
        </div>

        <div class="card-body">
          <div class="feedback-title">
            <i class="el-icon-chat-line-round"></i>
            {{ item.title }}
          </div>
          <div class="feedback-content">{{ item.content }}</div>
          <div class="feedback-images" v-if="item.images">
            <el-image
                v-for="(img, idx) in item.images.split(',')"
                :key="idx"
                :src="img"
                :preview-src-list="item.images.split(',')"
                fit="cover"
                class="feedback-img"
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </div>
          <div class="feedback-meta">
            <el-tooltip content="关联订单" placement="top">
              <span v-if="item.orderNo" class="meta-item">
                <i class="el-icon-document"></i> {{ item.orderNo }}
              </span>
            </el-tooltip>
            <el-tooltip content="关联预约" placement="top">
              <span v-if="item.appointmentNo" class="meta-item">
                <i class="el-icon-s-order"></i> {{ item.appointmentNo }}
              </span>
            </el-tooltip>
            <el-tooltip content="联系方式" placement="top">
              <span class="meta-item">
                <i class="el-icon-phone"></i> {{ item.contact || '未提供' }}
              </span>
            </el-tooltip>
          </div>
        </div>

        <!-- 处理记录 -->
        <!-- 处理记录 -->
        <div class="process-section" v-if="item.processRecords && item.processRecords.length > 0">
          <div class="process-title">
            <i class="el-icon-document-copy"></i> 处理记录
            <span class="process-count">{{ item.processRecords.length }}</span>
          </div>
          <div class="process-list">
            <div class="process-item" v-for="record in item.processRecords" :key="record.id">
              <div class="process-header">
                <div class="process-user">
                  <i class="el-icon-user"></i> {{ record.handlerNickname || record.handlerName || '员工' }}
                </div>
                <div class="process-time">
                  <i class="el-icon-time"></i> {{ formatDate(record.createTime) }}
                </div>
              </div>
              <div class="process-content">{{ record.content }}</div>
            </div>
          </div>
        </div>
        <!-- 卡片底部按钮 -->
        <div class="card-footer">
          <!-- 查看详情按钮（所有状态都有） -->
          <el-button
              size="small"
              plain
              class="action-btn"
              @click="viewDetail(item)"
          >
            <i class="el-icon-view"></i> 查看详情
          </el-button>

          <!-- 待处理：显示"开始处理"按钮 -->
          <el-button
              v-if="item.status === 0"
              type="primary"
              size="small"
              class="action-btn"
              @click="handleProcess(item)"
          >
            <i class="el-icon-check"></i> 开始处理
          </el-button>

          <!-- 处理中：显示"标记解决"按钮 -->
          <el-button
              v-if="item.status === 1"
              type="success"
              size="small"
              class="action-btn"
              @click="openResolveDialog(item)"
          >
            <i class="el-icon-circle-check"></i> 标记解决
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
          @current-change="handlePageChange"
          :current-page="page"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          background
      ></el-pagination>
    </div>

    <!-- 反馈详情对话框 -->
    <el-dialog
        title="反馈详情"
        :visible.sync="detailVisible"
        width="650px"
        center
        class="detail-dialog"
    >
      <div class="detail-content" v-if="currentFeedback">
        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">用户：</span>
                <span class="detail-value">{{ currentFeedback.userNickname || currentFeedback.userName || '匿名用户' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">类型：</span>
                <span class="detail-value">{{ getTypeName(currentFeedback.type) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">状态：</span>
                <el-tag :type="getStatusTag(currentFeedback.status)" size="small">
                  {{ getStatusName(currentFeedback.status) }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">创建时间：</span>
                <span class="detail-value">{{ formatDate(currentFeedback.createTime) }}</span>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="detail-item">
                <span class="detail-label">内容：</span>
                <span class="detail-value detail-content-text">{{ currentFeedback.content }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 联系方式 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-phone"></i>
            <span>联系方式</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">手机号：</span>
                <span class="detail-value">{{ currentFeedback.contact || '未提供' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">关联订单：</span>
                <span class="detail-value">{{ currentFeedback.orderNo || '无' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">关联预约：</span>
                <span class="detail-value">{{ currentFeedback.appointmentNo || '无' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 图片附件 -->
        <div class="detail-section" v-if="currentFeedback.images">
          <div class="section-title">
            <i class="el-icon-picture"></i>
            <span>图片附件</span>
          </div>
          <div class="detail-images">
            <el-image
                v-for="(img, idx) in currentFeedback.images.split(',')"
                :key="idx"
                :src="img"
                :preview-src-list="currentFeedback.images.split(',')"
                fit="cover"
                class="detail-img"
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </div>
        </div>

        <!-- 处理信息 -->
        <!-- 处理信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-document-copy"></i>
            <span>处理信息</span>
          </div>
          <div class="process-result-item" v-if="currentFeedback.status === 2 && currentFeedback.processResult">
            <div class="process-result-header">
              <i class="el-icon-circle-check"></i>
              <span class="process-result-title">处理结果</span>
              <span class="process-result-time">{{ formatDate(currentFeedback.processTime) }}</span>
            </div>
            <div class="process-result-content">{{ currentFeedback.processResult }}</div>
            <div class="process-result-handler">
              <i class="el-icon-user"></i> 处理人：{{ currentFeedback.handlerNickname || currentFeedback.handlerName || '员工' }}
            </div>
          </div>
          <div class="process-list-detail" v-if="currentFeedback.processRecords && currentFeedback.processRecords.length > 0">
            <div class="process-item-detail" v-for="record in currentFeedback.processRecords" :key="record.id">
              <div class="process-header-detail">
        <span class="process-user-detail">
          <i class="el-icon-user"></i> {{ record.handlerNickname || record.handlerName || '员工' }}
        </span>
                <span class="process-time-detail">
          <i class="el-icon-time"></i> {{ formatDate(record.createTime) }}
        </span>
              </div>
              <div class="process-content-detail">{{ record.content }}</div>
            </div>
          </div>
          <div class="empty-process" v-else-if="currentFeedback.status !== 2">
            暂无处理信息
          </div>
        </div>
      </div>

      <span slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 标记解决对话框 -->
    <el-dialog
        title="标记解决"
        :visible.sync="resolveDialogVisible"
        width="480px"
        center
        class="resolve-dialog"
        :close-on-click-modal="false"
    >
      <div class="resolve-content">
        <div class="resolve-tip">
          <i class="el-icon-info"></i>
          <span>请填写处理结果，这将作为反馈的处理记录保存</span>
        </div>
        <el-input
            v-model="resolveContent"
            type="textarea"
            :rows="4"
            placeholder="请描述处理结果..."
            maxlength="500"
            show-word-limit
        ></el-input>
      </div>
      <span slot="footer">
        <el-button @click="resolveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResolve" :loading="resolveLoading">
          <i class="el-icon-check"></i> 确认解决
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminFeedbackList,
  getAdminFeedbackStatistics,
  processFeedback,
  resolveFeedback
} from '@/api/feedback';

export default {
  name: 'StaffFeedback',
  data() {
    return {
      loading: false,
      resolveLoading: false,
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
      detailVisible: false,
      resolveDialogVisible: false,
      resolveContent: '',
      resolveFeedbackId: null,
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
        const res = await getAdminFeedbackList(params);
        if (res.code === 200) {
          this.feedbackList = (res.data.list || []).map(item => ({
            ...item,
            userNickname: item.userNickname || null,
            // 处理 processRecords 中的昵称
            processRecords: (item.processRecords || []).map(record => ({
              ...record,
              handlerNickname: record.handlerNickname || record.handlerName || null
            }))
          }));
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
        const res = await getAdminFeedbackStatistics();
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

    getStatusClass(status) {
      const map = { 0: 'status-pending', 1: 'status-processing', 2: 'status-resolved' };
      return map[status] || '';
    },

    getStatusIcon(status) {
      const map = { 0: 'el-icon-warning', 1: 'el-icon-loading', 2: 'el-icon-circle-check' };
      return map[status] || '';
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

    openResolveDialog(row) {
      this.resolveFeedbackId = row.id;
      this.resolveContent = '';
      this.resolveDialogVisible = true;
    },

    async confirmResolve() {
      if (!this.resolveContent.trim()) {
        this.$message.warning('请输入处理结果');
        return;
      }

      this.resolveLoading = true;
      try {
        await resolveFeedback(this.resolveFeedbackId, this.resolveContent.trim());
        this.$message.success('已标记为解决');
        this.resolveDialogVisible = false;
        this.loadList();
        this.loadStatistics();
      } catch (error) {
        this.$message.error('操作失败');
      } finally {
        this.resolveLoading = false;
      }
    },

    viewDetail(row) {
      // 确保处理记录中的昵称字段存在
      this.currentFeedback = {
        ...row,
        processRecords: (row.processRecords || []).map(record => ({
          ...record,
          handlerNickname: record.handlerNickname || record.handlerName || null
        }))
      };
      this.detailVisible = true;
    },
  }
};
</script>

<style scoped>
.staff-feedback {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 统计卡片 */
.stat-card {
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  padding: 22px 20px;
}

.stat-icon {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon i {
  font-size: 26px;
  color: white;
}

.stat-pending { background: linear-gradient(135deg, #f56c6c, #ff8e8e); }
.stat-processing { background: linear-gradient(135deg, #e6a23c, #f0b45c); }
.stat-resolved { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-today { background: linear-gradient(135deg, #409EFF, #66b1ff); }

.stat-number {
  font-size: 30px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 操作栏 */
.action-bar {
  background: #fff;
  border-radius: 20px;
  padding: 16px 24px;
  margin: 20px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-left .section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
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
  width: 220px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
}

.search-input ::v-deep .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.status-select {
  width: 120px;
}

.status-select ::v-deep .el-input__inner {
  border-radius: 10px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  border-radius: 10px;
  padding: 9px 18px;
}

.reset-btn {
  border-radius: 10px;
  border-color: #dcdfe6;
}

/* 反馈列表 */
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
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  flex-wrap: wrap;
  gap: 12px;
}

.feedback-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.feedback-user {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 13px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.feedback-time {
  color: #999;
  font-size: 12px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending { background: #fef0f0; color: #f56c6c; }
.status-processing { background: #fef6e6; color: #e6a23c; }
.status-resolved { background: #e8f7ef; color: #67c23a; }

.card-body {
  margin-bottom: 18px;
}

.feedback-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.feedback-title i {
  color: #667eea;
  font-size: 18px;
}

.feedback-content {
  color: #5a6874;
  line-height: 1.6;
  margin-bottom: 14px;
  padding-left: 26px;
}

.feedback-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
  padding-left: 26px;
}

.feedback-img {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  object-fit: cover;
  cursor: pointer;
}

.feedback-img:hover {
  transform: scale(1.05);
}

.feedback-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  padding-left: 26px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #909399;
  background: #f8f9fc;
  padding: 4px 12px;
  border-radius: 20px;
}

/* 处理记录 */
.process-section {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 18px;
}

.process-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.process-count {
  background: #667eea;
  color: white;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 20px;
}

.process-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.process-item {
  padding-bottom: 12px;
  border-bottom: 1px solid #e8eef2;
}

.process-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.process-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.process-user {
  font-weight: 500;
  color: #2c3e50;
  font-size: 13px;
}

.process-user i {
  color: #667eea;
  margin-right: 4px;
}

.process-time {
  font-size: 11px;
  color: #999;
}

.process-content {
  color: #666;
  line-height: 1.5;
  font-size: 13px;
  padding-left: 20px;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  border-radius: 10px;
  padding: 8px 18px;
  transition: all 0.2s;
}

.action-btn:hover {
  transform: translateY(-1px);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 20px;
  color: #999;
}

.empty-state i {
  font-size: 72px;
  margin-bottom: 20px;
  color: #dcdfe6;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 20px;
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
  font-size: 20px;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 10px;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-section:last-child {
  border-bottom: none;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 15px;
}

.section-title i {
  color: #667eea;
}

.detail-item {
  margin-bottom: 12px;
}

.detail-label {
  display: inline-block;
  width: 80px;
  font-size: 13px;
  color: #909399;
}

.detail-value {
  font-size: 13px;
  color: #2c3e50;
}

.detail-content-text {
  word-break: break-word;
  white-space: pre-wrap;
}

.detail-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
}

/* 处理结果 */
.process-result-item {
  background: #e8f7ef;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.process-result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.process-result-header i {
  color: #67c23a;
  font-size: 16px;
}

.process-result-title {
  font-weight: 600;
  color: #67c23a;
}

.process-result-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.process-result-content {
  color: #2c3e50;
  line-height: 1.6;
  margin-bottom: 10px;
  padding-left: 24px;
}

.process-result-handler {
  font-size: 12px;
  color: #909399;
  padding-left: 24px;
}

/* 处理记录详情 */
.process-list-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.process-item-detail {
  background: #f8f9fc;
  padding: 12px 16px;
  border-radius: 12px;
}

.process-header-detail {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.process-user-detail {
  color: #667eea;
  font-weight: 500;
}

.process-time-detail {
  color: #999;
}

.process-content-detail {
  color: #666;
  line-height: 1.5;
  font-size: 13px;
  padding-left: 20px;
}

.empty-process {
  text-align: center;
  padding: 30px;
  color: #999;
  background: #f8f9fc;
  border-radius: 12px;
}

/* 标记解决对话框 */
.resolve-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.resolve-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 18px 24px;
  margin: 0;
}

.resolve-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.resolve-dialog ::v-deep .el-dialog__close {
  color: white;
}

.resolve-content {
  padding: 10px 0;
}

.resolve-tip {
  background: #f0f7ff;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409EFF;
  font-size: 13px;
}

.resolve-tip i {
  font-size: 16px;
}

.resolve-dialog ::v-deep .el-textarea__inner {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.resolve-dialog ::v-deep .el-textarea__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}
.resolve-content {
  padding: 10px 0;
}

.resolve-tip {
  background: #e8f7ef;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
  font-size: 13px;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .staff-feedback {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .action-right {
    justify-content: center;
  }

  .search-input {
    width: 180px;
  }

  .feedback-info {
    flex-wrap: wrap;
  }

  .feedback-meta {
    flex-direction: column;
    gap: 8px;
  }

  .card-footer {
    flex-wrap: wrap;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>