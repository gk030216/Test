<template>
  <div class="comment-manage-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总评价数</div>
          <div class="stat-value">{{ statistics.total || 0 }}</div>
          <div class="stat-sub">服务评价总量</div>
        </div>
      </div>
      <div class="stat-card card-rating">
        <div class="stat-icon">
          <i class="el-icon-star-on"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">平均评分</div>
          <div class="stat-value">{{ statistics.avgRating || 0 }}</div>
          <div class="stat-sub">综合评价分数</div>
        </div>
      </div>
      <div class="stat-card card-reply">
        <div class="stat-icon">
          <i class="el-icon-message"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">已回复</div>
          <div class="stat-value">{{ statistics.replyCount || 0 }}</div>
          <div class="stat-sub">已完成回复</div>
        </div>
      </div>
      <div class="stat-card card-pending">
        <div class="stat-icon">
          <i class="el-icon-chat-line-round"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">待回复</div>
          <div class="stat-value">{{ statistics.unreplyCount || 0 }}</div>
          <div class="stat-sub">等待回复处理</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索服务/用户/内容"
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.rating"
          placeholder="评分筛选"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="5星" :value="5" />
          <el-option label="4星" :value="4" />
          <el-option label="3星" :value="3" />
          <el-option label="2星" :value="2" />
          <el-option label="1星" :value="1" />
        </el-select>
        <el-select
          v-model="searchForm.replyStatus"
          placeholder="回复状态"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="已回复" :value="1" />
          <el-option label="待回复" :value="0" />
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

    <!-- 评价表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="commentList"
        stripe
        row-key="id"
        class="comment-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="服务信息" min-width="200">
          <template slot-scope="scope">
            <div class="service-cell">
              <span class="service-name">{{ scope.row.serviceName || '--' }}</span>
              <span class="service-id">ID: {{ scope.row.serviceId }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="用户" width="150">
          <template slot-scope="scope">
            <div class="user-cell">
              <el-avatar :size="32" :src="scope.row.userAvatar" class="cell-avatar">
                {{ (scope.row.userNickname || scope.row.userName || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="cell-text">{{ scope.row.userNickname || scope.row.userName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评分" width="160" align="center">
          <template slot-scope="scope">
            <el-rate v-model="scope.row.rating" disabled text-color="#f59e4b" />
          </template>
        </el-table-column>

        <el-table-column label="评价内容" min-width="240">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.content" placement="top" :disabled="!scope.row.content || scope.row.content.length <= 40">
              <div class="comment-content">{{ scope.row.content || '--' }}</div>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="服务人员" width="120" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.staffName || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="回复状态" width="100" align="center">
          <template slot-scope="scope">
            <span class="status-tag" :class="scope.row.reply ? 'replied' : 'pending'">
              {{ scope.row.reply ? '已回复' : '待回复' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="显示" width="80" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.status === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleStatusChange(scope.row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="评价时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleView(scope.row)" class="act-view">
                <i class="el-icon-view" /> 查看
              </el-button>
              <el-button type="text" size="small" @click="openReplyDialog(scope.row)" class="act-reply">
                <i class="el-icon-chat-dot-round" /> 回复
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

    <!-- 回复对话框 -->
    <el-dialog
      title="回复评价"
      :visible.sync="replyVisible"
      width="550px"
      :close-on-click-modal="false"
      center
      class="reply-dialog"
    >
      <div class="dialog-body" v-if="currentComment">
        <div class="original-comment">
          <div class="comment-header">
            <div class="comment-user">
              <el-avatar :size="36" :src="currentComment.userAvatar" class="comment-avatar">
                {{ (currentComment.userNickname || currentComment.userName || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="comment-user-info">
                <span class="user-name">{{ currentComment.userNickname || currentComment.userName }}</span>
                <el-rate v-model="currentComment.rating" disabled text-color="#f59e4b" />
              </div>
            </div>
            <span class="comment-time">{{ formatDate(currentComment.createTime) }}</span>
          </div>
          <div class="comment-text">{{ currentComment.content }}</div>
          <div class="service-tag">
            <i class="el-icon-service" />
            {{ currentComment.serviceName }}
          </div>
        </div>

        <div class="reply-section">
          <div class="section-title">
            <i class="el-icon-edit" />
            <span>商家回复</span>
          </div>
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="4"
            :placeholder="currentComment.reply ? '修改回复内容...' : '输入回复内容...'"
            maxlength="500"
            show-word-limit
          />
          <div v-if="currentComment.reply" class="old-reply">
            <span class="old-reply-label">原回复：</span>
            <span>{{ currentComment.reply }}</span>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="replyVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replyLoading" class="warm-btn">
          提交回复
        </el-button>
      </span>
    </el-dialog>

    <!-- 评价详情对话框 -->
    <el-dialog
      title="评价详情"
      :visible.sync="detailVisible"
      width="550px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetailComment">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="d-label">服务名称</span>
            <span class="d-value">{{ currentDetailComment.serviceName || '--' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">服务ID</span>
            <span class="d-value">{{ currentDetailComment.serviceId }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">用户名</span>
            <span class="d-value">{{ currentDetailComment.userNickname || currentDetailComment.userName }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">用户ID</span>
            <span class="d-value">{{ currentDetailComment.userId }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">评分</span>
            <span class="d-value">
              <el-rate v-model="currentDetailComment.rating" disabled text-color="#f59e4b" />
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">评价内容</span>
            <span class="d-value">{{ currentDetailComment.content }}</span>
          </div>
          <div class="detail-item" v-if="currentDetailComment.images">
            <span class="d-label">评价图片</span>
            <span class="d-value">
              <div class="detail-images">
                <el-image
                  v-for="(img, idx) in currentDetailComment.images.split(',')"
                  :key="idx"
                  :src="img"
                  :preview-src-list="currentDetailComment.images.split(',')"
                  fit="cover"
                  class="detail-img"
                >
                  <div slot="error" class="image-slot">
                    <i class="el-icon-picture-outline" />
                  </div>
                </el-image>
              </div>
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">评价时间</span>
            <span class="d-value">{{ formatDate(currentDetailComment.createTime) }}</span>
          </div>
          <div class="detail-item" v-if="currentDetailComment.reply">
            <span class="d-label">回复内容</span>
            <span class="d-value reply-text">{{ currentDetailComment.reply }}</span>
          </div>
          <div class="detail-item" v-if="currentDetailComment.replyTime">
            <span class="d-label">回复时间</span>
            <span class="d-value">{{ formatDate(currentDetailComment.replyTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">显示状态</span>
            <span class="d-value">
              <span class="status-tag" :class="currentDetailComment.status === 1 ? 'replied' : 'pending'">
                {{ currentDetailComment.status === 1 ? '显示' : '隐藏' }}
              </span>
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">回复状态</span>
            <span class="d-value">
              <span class="status-tag" :class="currentDetailComment.reply ? 'replied' : 'pending'">
                {{ currentDetailComment.reply ? '已回复' : '待回复' }}
              </span>
            </span>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getServiceCommentList,
  updateServiceCommentStatus,
  deleteServiceComment,
  batchDeleteServiceComments,
  replyServiceComment,
  getServiceCommentStatistics
} from '@/api/service';

export default {
  name: 'Admin_ServiceCommentManage',
  data() {
    return {
      loading: false,
      replyLoading: false,
      detailVisible: false,
      currentDetailComment: null,
      commentList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', rating: '', replyStatus: '' },
      statistics: { total: 0, avgRating: 0, replyCount: 0, unreplyCount: 0 },
      replyVisible: false,
      replyContent: '',
      currentComment: null
    };
  },
  created() {
    this.loadList();
    this.loadStatistics();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          rating: this.searchForm.rating || undefined,
          replyStatus: this.searchForm.replyStatus !== '' ? this.searchForm.replyStatus : undefined
        };
        const res = await getServiceCommentList(params);
        if (res.code === 200) {
          this.commentList = (res.data.list || []).map(item => ({
            ...item,
            userNickname: item.userNickname || null
          }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载评价列表失败');
      } finally {
        this.loading = false;
      }
    },

    async loadStatistics() {
      try {
        const res = await getServiceCommentStatistics();
        if (res.code === 200) {
          this.statistics = res.data;
        }
      } catch {
        // fallback: compute from current list
        const list = this.commentList;
        const total = list.length;
        let sumRating = 0;
        let replyCount = 0;
        list.forEach(item => {
          sumRating += item.rating || 0;
          if (item.reply) replyCount++;
        });
        this.statistics = {
          total,
          avgRating: total > 0 ? (sumRating / total).toFixed(1) : 0,
          replyCount,
          unreplyCount: total - replyCount
        };
      }
    },

    handleView(row) {
      this.currentDetailComment = row;
      this.detailVisible = true;
    },

    handleSearch() {
      this.page = 1;
      this.loadList();
    },

    handleReset() {
      this.searchForm = { keyword: '', rating: '', replyStatus: '' };
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

    handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '显示' : '隐藏';
      this.$confirm(`确定要${action}该评价吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const res = await updateServiceCommentStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(`${action}成功`);
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '操作失败');
        }
      }).catch(() => {});
    },

    openReplyDialog(row) {
      this.currentComment = row;
      this.replyContent = row.reply || '';
      this.replyVisible = true;
    },

    async submitReply() {
      if (!this.replyContent.trim()) {
        this.$message.warning('请输入回复内容');
        return;
      }
      this.replyLoading = true;
      try {
        const res = await replyServiceComment(this.currentComment.id, this.replyContent);
        if (res.code === 200) {
          this.$message.success('回复成功');
          this.currentComment.reply = this.replyContent;
          this.replyVisible = false;
          this.loadList();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '回复失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '回复失败');
      } finally {
        this.replyLoading = false;
      }
    },

    handleDelete(row) {
      this.$confirm('确定要删除该评价吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteServiceComment(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadList();
            this.loadStatistics();
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '删除失败');
        }
      }).catch(() => {});
    },

    handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条评价吗？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteServiceComments(ids);
          if (res.code === 200) {
            this.$message.success(res.message || '批量删除成功');
            this.selectedRows = [];
            this.loadList();
            this.loadStatistics();
          } else {
            this.$message.error(res.message || '批量删除失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '批量删除失败');
        }
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.comment-manage-page {
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
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(61, 46, 42, 0.10);
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
.card-total .stat-icon   { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-rating .stat-icon  { background: linear-gradient(135deg, #e6a23c, #f7c06b); }
.card-reply .stat-icon   { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-pending .stat-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }

.stat-info { position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 6px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3d2e2a; letter-spacing: -0.5px; }
.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ===== 搜索筛选栏 ===== */
.filter-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px 24px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}
.filter-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.keyword-input { width: 240px; }
.keyword-input ::v-deep .el-input__inner { border-radius: 10px; }
.status-select { width: 120px; }
.status-select ::v-deep .el-input__inner { border-radius: 10px; }

.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  padding: 9px 20px !important;
  font-weight: 500;
}
.warm-btn:hover { opacity: 0.88; transform: translateY(-1px); }

.reset-btn { border-radius: 10px; color: #606266; }
.reset-btn:hover { background: #fdf6f0; border-color: #f5d5c0; }

/* ===== 批量操作浮条 ===== */
.batch-bar {
  background: linear-gradient(135deg, #fef5ee, #fff7f0);
  border: 1px solid #f5d5c0;
  border-left: 4px solid #f59e4b;
  border-radius: 12px;
  padding: 10px 20px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #3d2e2a;
}
.batch-info { font-size: 13px; }
.batch-info strong { color: #f59e4b; font-size: 15px; }
.batch-btn-delete {
  background: rgba(245, 108, 108, 0.12) !important;
  border: 1px solid rgba(245, 108, 108, 0.35) !important;
  color: #f56c6c !important;
  border-radius: 8px;
  padding: 7px 16px;
  font-weight: 500;
}
.batch-btn-delete:hover {
  background: rgba(245, 108, 108, 0.22) !important;
  border-color: #f56c6c !important;
}
.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.comment-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.comment-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.comment-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.comment-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.comment-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.comment-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b;
  border-color: #f59e4b;
}

/* 表格单元格 */
.service-cell { display: flex; flex-direction: column; gap: 4px; }
.service-name { font-weight: 500; color: #3d2e2a; }
.service-id { font-size: 11px; color: #a08c84; }
.user-cell { display: flex; align-items: center; gap: 8px; }
.cell-avatar {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  color: #fff;
}
.cell-text { color: #5c4a42; font-size: 13px; }
.comment-content {
  color: #5c4a42;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
  min-height: 20px;
}
.cell-time { color: #a08c84; font-size: 12px; }

/* 状态标签 */
.status-tag { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-tag.replied { background: #edf7ee; color: #67c23a; }
.status-tag.pending { background: #fdf6f0; color: #e6a23c; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-reply { color: #67c23a !important; }
.act-reply:hover { color: #4a9e1b !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }

/* ===== 分页卡片 ===== */
.pagination-card {
  background: #fff;
  border-radius: 16px;
  margin-top: 16px;
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

/* ===== 回复对话框 ===== */
.reply-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.reply-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.reply-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.reply-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.reply-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.reply-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }

.original-comment {
  background: #fdf8f4;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}
.comment-user { display: flex; align-items: center; gap: 12px; }
.comment-avatar {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  color: #fff;
}
.comment-user-info { display: flex; flex-direction: column; gap: 4px; }
.comment-user-info .user-name { font-weight: 500; color: #3d2e2a; }
.comment-time { font-size: 12px; color: #a08c84; }
.comment-text {
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  color: #5c4a42;
  line-height: 1.6;
  margin-bottom: 12px;
}
.service-tag {
  display: inline-block;
  padding: 4px 12px;
  background: #fef5ee;
  color: #f59e4b;
  border-radius: 20px;
  font-size: 12px;
}
.service-tag i { margin-right: 4px; }

.reply-section { margin-top: 8px; }
.section-title {
  display: flex; align-items: center; gap: 8px;
  font-weight: 500; color: #3d2e2a; margin-bottom: 12px;
}
.section-title i { color: #f59e4b; }
.reply-section ::v-deep .el-textarea__inner { border-radius: 10px; }

.old-reply {
  margin-top: 12px;
  padding: 12px;
  background: #fef9e6;
  border-radius: 8px;
  font-size: 13px;
  color: #5c4a42;
}
.old-reply-label { color: #e6a23c; font-weight: 500; margin-right: 8px; }

.dialog-footer {
  text-align: right;
  padding: 16px 28px 22px;
  border-top: 1px solid #f5ece6;
}
.dialog-footer .el-button {
  border-radius: 10px; padding: 10px 28px; font-weight: 500; font-size: 13px;
}
.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
}
.dialog-footer .el-button--primary:hover { opacity: 0.88; transform: translateY(-1px); }

/* ===== 详情对话框 ===== */
.detail-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.detail-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.detail-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.detail-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.detail-dialog ::v-deep .el-dialog__body { padding: 28px; }

.detail-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  border: 1px solid #f5ece6;
  border-radius: 12px;
  overflow: hidden;
}
.detail-item {
  display: flex; align-items: flex-start;
  padding: 14px 18px;
  border-bottom: 1px solid #f5ece6;
  border-right: 1px solid #f5ece6;
}
.detail-item:nth-child(2n) { border-right: none; }
.detail-item:last-child { border-bottom: none; }
.detail-item:nth-last-child(2):nth-child(odd) { border-bottom: none; }
.d-label { width: 72px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; font-weight: 500; word-break: break-word; }
.reply-text { color: #67c23a; }

.detail-images { display: flex; gap: 10px; flex-wrap: wrap; }
.detail-img {
  width: 80px; height: 80px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #f5ece6;
}
.image-slot {
  display: flex; align-items: center; justify-content: center;
  width: 100%; height: 100%;
  background: #fdf8f4;
  color: #a08c84;
  font-size: 24px;
}

/* 响应式 */
@media (max-width: 768px) {
  .comment-manage-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left { justify-content: center; }
  .keyword-input { width: 100%; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-item { border-right: none; }
  .detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }
  .detail-item:last-child { border-bottom: none; }
}
</style>
