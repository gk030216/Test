<template>
  <div class="feedback-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-pending">
        <div class="stat-icon"><i class="el-icon-warning" /></div>
        <div class="stat-info">
          <div class="stat-label">待处理</div>
          <div class="stat-value">{{ stats.pending || 0 }}</div>
          <div class="stat-sub">等待处理中</div>
        </div>
      </div>
      <div class="stat-card card-processing">
        <div class="stat-icon"><i class="el-icon-loading" /></div>
        <div class="stat-info">
          <div class="stat-label">处理中</div>
          <div class="stat-value">{{ stats.processing || 0 }}</div>
          <div class="stat-sub">正在跟进处理</div>
        </div>
      </div>
      <div class="stat-card card-resolved">
        <div class="stat-icon"><i class="el-icon-circle-check" /></div>
        <div class="stat-info">
          <div class="stat-label">已解决</div>
          <div class="stat-value">{{ stats.resolved || 0 }}</div>
          <div class="stat-sub">已完成处理归档</div>
        </div>
      </div>
      <div class="stat-card card-today">
        <div class="stat-icon"><i class="el-icon-date" /></div>
        <div class="stat-info">
          <div class="stat-label">今日新增</div>
          <div class="stat-value">{{ stats.today || 0 }}</div>
          <div class="stat-sub">当日提交的反馈</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索用户/内容..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.type"
          placeholder="反馈类型"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="服务问题" value="service" />
          <el-option label="预约问题" value="appointment" />
          <el-option label="支付问题" value="payment" />
          <el-option label="其他问题" value="other" />
        </el-select>
        <el-select
          v-model="searchForm.status"
          placeholder="处理状态"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="待处理" :value="0" />
          <el-option label="处理中" :value="1" />
          <el-option label="已解决" :value="2" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 反馈表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="feedbackList"
        stripe
        row-key="id"
        class="feedback-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="用户" min-width="150">
          <template slot-scope="scope">
            <div class="user-cell">
              <el-avatar :size="36" :src="scope.row.userAvatar" class="cell-avatar">
                {{ scope.row.userAvatar ? '' : (scope.row.userNickname || scope.row.userName || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="user-name">{{ scope.row.userNickname || scope.row.userName || '匿名用户' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getTypeTag(scope.row.type)" size="small" effect="dark">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="反馈标题" min-width="180" show-overflow-tooltip />

        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <span :class="['status-tag', getStatusClass(scope.row.status)]">
              {{ getStatusName(scope.row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="contact" label="联系方式" width="140" align="center" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.contact || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="提交时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-text cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="viewDetail(scope.row)" class="act-view">
                <i class="el-icon-view" /> 详情
              </el-button>
              <el-button
                v-if="scope.row.status === 0"
                type="text"
                size="small"
                @click="handleProcess(scope.row)"
                class="act-process"
              >
                <i class="el-icon-check" /> 处理
              </el-button>
              <el-button
                v-if="scope.row.status === 1"
                type="text"
                size="small"
                @click="openResolveDialog(scope.row)"
                class="act-resolve"
              >
                <i class="el-icon-circle-check" /> 标记解决
              </el-button>
              <el-button type="text" size="small" @click="handleDelete(scope.row)" class="act-del">
                <i class="el-icon-delete" /> 删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-card">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :current-page="page"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
      />
    </div>

    <!-- 反馈详情对话框 -->
    <el-dialog
      title="反馈详情"
      :visible.sync="detailVisible"
      width="650px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentFeedback">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">用户</span>
            <span class="detail-value">{{ currentFeedback.userNickname || currentFeedback.userName || '匿名用户' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">类型</span>
            <el-tag :type="getTypeTag(currentFeedback.type)" size="small">{{ getTypeName(currentFeedback.type) }}</el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <span :class="['status-tag', getStatusClass(currentFeedback.status)]">{{ getStatusName(currentFeedback.status) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">提交时间</span>
            <span class="detail-value">{{ formatDate(currentFeedback.createTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">联系方式</span>
            <span class="detail-value">{{ currentFeedback.contact || '未提供' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">关联订单</span>
            <span class="detail-value">{{ currentFeedback.orderNo || '无' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">关联预约</span>
            <span class="detail-value">{{ currentFeedback.appointmentNo || '无' }}</span>
          </div>
          <div class="detail-item detail-full">
            <span class="detail-label">反馈标题</span>
            <span class="detail-value detail-title">{{ currentFeedback.title }}</span>
          </div>
          <div class="detail-item detail-full">
            <span class="detail-label">反馈内容</span>
            <span class="detail-value detail-content-text">{{ currentFeedback.content }}</span>
          </div>
        </div>

        <!-- 图片附件 -->
        <div class="detail-images" v-if="currentFeedback.images">
          <div class="detail-section-title"><i class="el-icon-picture" /> 图片附件</div>
          <div class="images-row">
            <el-image
              v-for="(img, idx) in currentFeedback.images.split(',')"
              :key="idx"
              :src="img"
              :preview-src-list="currentFeedback.images.split(',')"
              fit="cover"
              class="detail-img"
            >
              <div slot="error" class="img-error"><i class="el-icon-picture-outline" /></div>
            </el-image>
          </div>
        </div>

        <!-- 处理结果 -->
        <div class="detail-section" v-if="currentFeedback.status === 2 && currentFeedback.processResult">
          <div class="detail-section-title"><i class="el-icon-circle-check" /> 处理结果</div>
          <div class="process-result">
            <div class="result-header">
              <span class="result-handler"><i class="el-icon-user" /> {{ currentFeedback.handlerNickname || currentFeedback.handlerName || '员工' }}</span>
              <span class="result-time"><i class="el-icon-time" /> {{ formatDate(currentFeedback.processTime) }}</span>
            </div>
            <div class="result-content">{{ currentFeedback.processResult }}</div>
          </div>
        </div>

        <!-- 处理记录 -->
        <div class="detail-section" v-if="currentFeedback.processRecords && currentFeedback.processRecords.length > 0">
          <div class="detail-section-title"><i class="el-icon-document-copy" /> 处理记录 ({{ currentFeedback.processRecords.length }})</div>
          <div class="process-list">
            <div class="process-item" v-for="record in currentFeedback.processRecords" :key="record.id">
              <div class="process-header">
                <span class="process-user"><i class="el-icon-user" /> {{ record.handlerNickname || record.handlerName || '员工' }}</span>
                <span class="process-time"><i class="el-icon-time" /> {{ formatDate(record.createTime) }}</span>
              </div>
              <div class="process-body">{{ record.content }}</div>
            </div>
          </div>
        </div>
      </div>

      <span slot="footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
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
      <div class="resolve-body">
        <div class="resolve-tip">
          <i class="el-icon-info" />
          <span>请填写处理结果记录</span>
        </div>
        <el-input
          v-model="resolveContent"
          type="textarea"
          :rows="4"
          placeholder="请描述处理结果..."
          maxlength="500"
          show-word-limit
        />
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="resolveDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmResolve" :loading="resolveLoading" class="warm-btn">
          <i class="el-icon-check" /> 确认解决
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
  resolveFeedback,
  deleteFeedback,
  batchDeleteFeedbacks
} from '@/api/feedback';

export default {
  name: 'Admin_Feedback',
  data() {
    return {
      loading: false,
      resolveLoading: false,
      feedbackList: [],
      selectedRows: [],
      total: 0,
      page: 1,
      pageSize: 10,
      searchForm: { keyword: '', type: '', status: '' },
      stats: { pending: 0, processing: 0, resolved: 0, today: 0 },
      detailVisible: false,
      resolveDialogVisible: false,
      resolveContent: '',
      resolveFeedbackId: null,
      currentFeedback: null
    };
  },
  created() {
    this.loadList();
    this.loadStats();
  },
  methods: {
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
          this.feedbackList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (e) {
        this.$message.error(e?.message || '加载反馈列表失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },

    async loadStats() {
      try {
        const res = await getAdminFeedbackStatistics();
        if (res.code === 200) {
          this.stats = { pending: 0, processing: 0, resolved: 0, today: 0, ...res.data };
        }
      } catch (e) {
        // stats load silently, non-critical
      }
    },

    getTypeName(type) {
      const m = { service: '服务问题', appointment: '预约问题', payment: '支付问题', other: '其他问题' };
      return m[type] || '未知';
    },
    getTypeTag(type) {
      const m = { service: 'warning', appointment: 'primary', payment: 'danger', other: 'info' };
      return m[type] || 'info';
    },
    getStatusName(status) {
      const m = { 0: '待处理', 1: '处理中', 2: '已解决' };
      return m[status] !== undefined ? m[status] : '未知';
    },
    getStatusClass(status) {
      const m = { 0: 's-pending', 1: 's-processing', 2: 's-resolved' };
      return m[status] || '';
    },

    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() {
      this.searchForm = { keyword: '', type: '', status: '' };
      this.page = 1;
      this.loadList();
    },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    viewDetail(row) {
      this.currentFeedback = row;
      this.detailVisible = true;
    },

    async handleProcess(row) {
      try {
        await this.$confirm('确认开始处理该反馈吗？', '提示', { type: 'info' });
        const res = await processFeedback(row.id);
        if (res.code === 200) {
          this.$message.success('已开始处理');
          this.loadList();
          this.loadStats();
        } else {
          this.$message.error(res.message || '操作失败，请稍后重试');
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error(e?.message || '操作失败，请稍后重试');
      }
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
        const res = await resolveFeedback(this.resolveFeedbackId, this.resolveContent.trim());
        if (res.code === 200) {
          this.$message.success('已标记为解决');
          this.resolveDialogVisible = false;
          this.loadList();
          this.loadStats();
        } else {
          this.$message.error(res.message || '操作失败，请稍后重试');
        }
      } catch (e) {
        this.$message.error(e?.message || '操作失败，请稍后重试');
      } finally {
        this.resolveLoading = false;
      }
    },

    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除该反馈吗？删除后无法恢复！`, '警告', { type: 'warning' });
        const res = await deleteFeedback(row.id);
        if (res.code === 200) {
          this.$message.success('删除成功');
          this.loadList();
          this.loadStats();
        } else {
          this.$message.error(res.message || '删除失败，请稍后重试');
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error(e?.message || '删除失败，请稍后重试');
      }
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条反馈吗？`, '警告', { type: 'warning' });
        const res = await batchDeleteFeedbacks(ids);
        if (res.code === 200) {
          this.$message.success(res.message || '批量删除成功');
          this.selectedRows = [];
          this.loadList();
          this.loadStats();
        } else {
          this.$message.error(res.message || '批量删除失败，请稍后重试');
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error(e?.message || '批量删除失败，请稍后重试');
      }
    }
  }
};
</script>

<style scoped>
.feedback-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 统计卡片 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.05);
  transition: transform 0.25s, box-shadow 0.25s;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(61, 46, 42, 0.10);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.card-pending .stat-icon  { background: linear-gradient(135deg, #f56c6c, #ff8e8e); }
.card-processing .stat-icon { background: linear-gradient(135deg, #e6a23c, #f0b45c); }
.card-resolved .stat-icon  { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-today .stat-icon     { background: linear-gradient(135deg, #f59e4b, #f0826a); }

.stat-info { position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 6px; }
.stat-value { font-size: 30px; font-weight: 700; color: #3d2e2a; line-height: 1.1; letter-spacing: -0.5px; }
.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ===== 搜索筛选栏 ===== */
.filter-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px 22px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  border: 1px solid #f5ece6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.keyword-input { width: 220px; }
.status-select { width: 130px; }

.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important;
  color: #fff !important;
  transition: all 0.2s;
}

.warm-btn:hover { opacity: 0.88; transform: translateY(-1px); }

.reset-btn:hover { background: #fdf6f0; border-color: #f5d5c0; }

/* ===== 批量操作浮条 ===== */
.batch-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  padding: 14px 22px;
  margin-bottom: 16px;
  border-radius: 14px;
  border: 1px solid #f5ece6;
  border-left: 4px solid #f59e4b;
  box-shadow: 0 4px 16px rgba(245, 158, 75, 0.12);
}

.batch-info strong { color: #f59e4b; font-size: 15px; }

.batch-btn-delete {
  background: #fff !important;
  border: 1px solid #f56c6c !important;
  color: #f56c6c !important;
}

.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 表格 ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  padding: 10px 16px;
  border: 1px solid #f5ece6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.feedback-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5a4034;
  font-weight: 600;
  border-bottom: 2px solid #f5ece6;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cell-avatar {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  color: #fff;
  flex-shrink: 0;
}

.user-name { color: #3d2e2a; font-size: 13px; }

.cell-text { color: #5a5048; font-size: 13px; }
.cell-time { color: #9a8e86; font-size: 12px; }

.status-tag {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.s-pending    { background: #fef0f0; color: #f56c6c; }
.s-processing { background: #fef6e6; color: #e6a23c; }
.s-resolved   { background: #e8f7ef; color: #67c23a; }

/* ===== 操作按钮 ===== */
.action-btns { display: flex; gap: 4px; justify-content: center; }

.act-view    { color: #909399 !important; }
.act-view:hover    { color: #f59e4b !important; }
.act-process { color: #e6a23c !important; }
.act-process:hover { color: #d48a1c !important; }
.act-resolve { color: #67c23a !important; }
.act-resolve:hover { color: #4ea81a !important; }
.act-del     { color: #f56c6c !important; }
.act-del:hover     { color: #e04444 !important; }

/* ===== 分页 ===== */
.pagination-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px 22px;
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  border: 1px solid #f5ece6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

/* ===== 详情对话框 ===== */
.detail-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 18px 24px;
  margin: 0;
}

.detail-dialog ::v-deep .el-dialog__title {
  color: #fff;
  font-weight: 600;
  font-size: 18px;
}

.detail-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 20px; }

.detail-body { max-height: 60vh; overflow-y: auto; padding: 4px; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  border: 1px solid #f5ece6;
  border-radius: 12px;
  overflow: hidden;
}

.detail-item {
  display: flex;
  padding: 12px 16px;
  border-bottom: 1px solid #f5ece6;
  border-right: 1px solid #f5ece6;
}

.detail-item:nth-child(even) { border-right: none; }
.detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }

.detail-full {
  grid-column: 1 / -1;
  border-right: none;
  flex-direction: column;
  gap: 6px;
}

.detail-label {
  font-size: 13px;
  color: #9a8e86;
  min-width: 60px;
  flex-shrink: 0;
}

.detail-value {
  font-size: 13px;
  color: #3d2e2a;
  word-break: break-word;
}

.detail-content-text {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #5a5048;
}

.detail-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #3d2e2a;
  margin: 18px 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-section-title i { color: #f59e4b; }

.detail-images { margin-top: 16px; }

.images-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-img {
  width: 90px;
  height: 90px;
  border-radius: 10px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #f5ece6;
}

.img-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f8f5f2;
  color: #c0b8b0;
  font-size: 28px;
}

.process-result {
  background: #e8f7ef;
  border-radius: 12px;
  padding: 16px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.result-content {
  color: #3d2e2a;
  line-height: 1.6;
  white-space: pre-wrap;
}

.process-list { display: flex; flex-direction: column; gap: 10px; }

.process-item {
  background: #faf8f6;
  border-radius: 10px;
  padding: 14px 16px;
}

.process-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
  gap: 8px;
}

.process-body { color: #5a5048; line-height: 1.6; font-size: 13px; white-space: pre-wrap; }

/* ===== 标记解决对话框 ===== */
.resolve-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.resolve-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 18px 24px;
  margin: 0;
}

.resolve-dialog ::v-deep .el-dialog__title {
  color: #fff;
  font-weight: 600;
  font-size: 18px;
}

.resolve-dialog ::v-deep .el-dialog__close { color: #fff; }

.resolve-body { padding: 8px 0; }

.resolve-tip {
  background: #fdf6f0;
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f59e4b;
  font-size: 13px;
}

.resolve-dialog ::v-deep .el-textarea__inner {
  border-radius: 10px;
  border: 1px solid #e4e7ed;
}

.resolve-dialog ::v-deep .el-textarea__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 2px rgba(245, 158, 75, 0.1);
}

.dialog-footer { text-align: right; }

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .feedback-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left { flex-wrap: wrap; }
  .keyword-input { width: 100%; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-item { border-right: none; }
  .detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }
}
</style>
