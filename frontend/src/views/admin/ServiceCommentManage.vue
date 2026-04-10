<template>
  <div class="service-comment-manage">

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
            class="batch-btn"
        >
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索服务/用户/内容"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          ></el-input>
        </div>
        <el-select
            v-model="searchForm.rating"
            placeholder="评分"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="5星" :value="5"></el-option>
          <el-option label="4星" :value="4"></el-option>
          <el-option label="3星" :value="3"></el-option>
          <el-option label="2星" :value="2"></el-option>
          <el-option label="1星" :value="1"></el-option>
        </el-select>
        <el-select
            v-model="searchForm.replyStatus"
            placeholder="回复状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="已回复" :value="1"></el-option>
          <el-option label="待回复" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
      </div>
    </div>

    <!-- 评价表格 -->
    <el-table
        v-loading="loading"
        :data="commentList"
        stripe
        class="comment-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <el-table-column label="服务信息" min-width="200">
        <template slot-scope="scope">
          <div class="service-info">
            <span class="service-name">{{ scope.row.serviceName || '--' }}</span>
            <span class="service-id">ID: {{ scope.row.serviceId }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="用户" width="150">
        <template slot-scope="scope">
          <div class="user-info">
            <el-avatar :size="32" :src="scope.row.userAvatar" class="user-avatar">
              {{ scope.row.userName ? scope.row.userName.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
            <span class="user-name">{{ scope.row.userName }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="评分" width="120" align="center">
        <template slot-scope="scope">
          <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900"></el-rate>
        </template>
      </el-table-column>

      <el-table-column label="评价内容" min-width="250">
        <template slot-scope="scope">
          <div class="comment-content">{{ scope.row.content }}</div>
        </template>
      </el-table-column>

      <el-table-column label="回复状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.reply ? 'success' : 'info'" size="small">
            {{ scope.row.reply ? '已回复' : '待回复' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
          ></el-switch>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="评价时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button
                size="small"
                type="primary"
                plain
                circle
                @click="openReplyDialog(scope.row)"
                class="action-icon-btn"
                title="回复"
            >
              <i class="el-icon-chat-dot-round"></i>
            </el-button>
            <el-button
                size="small"
                type="danger"
                plain
                circle
                @click="handleDelete(scope.row)"
                class="action-icon-btn"
                title="删除"
            >
              <i class="el-icon-delete"></i>
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          :current-page="page"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
      ></el-pagination>
    </div>

    <!-- 回复对话框 -->
    <el-dialog
        title="回复评价"
        :visible.sync="replyVisible"
        width="550px"
        center
        class="reply-dialog"
    >
      <div class="dialog-content" v-if="currentComment">
        <div class="original-comment">
          <div class="comment-header">
            <div class="user-info">
              <el-avatar :size="36" :src="currentComment.userAvatar">
                {{ currentComment.userName ? currentComment.userName.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ currentComment.userName }}</span>
                <el-rate v-model="currentComment.rating" disabled text-color="#ff9900"></el-rate>
              </div>
            </div>
            <span class="comment-time">{{ formatDate(currentComment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ currentComment.content }}</div>
          <div class="service-tag">
            <i class="el-icon-service"></i>
            {{ currentComment.serviceName }}
          </div>
        </div>

        <div class="reply-section">
          <div class="section-title">
            <i class="el-icon-edit"></i>
            <span>商家回复</span>
          </div>
          <el-input
              v-model="replyContent"
              type="textarea"
              :rows="4"
              :placeholder="currentComment.reply ? '修改回复内容...' : '输入回复内容...'"
              maxlength="500"
              show-word-limit
          ></el-input>
          <div v-if="currentComment.reply" class="old-reply">
            <span class="old-reply-label">原回复：</span>
            <span>{{ currentComment.reply }}</span>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replyLoading">
          <i class="el-icon-check"></i> 提交回复
        </el-button>
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
  replyServiceComment
} from '@/api/service';

export default {
  name: 'ServiceCommentManage',
  data() {
    return {
      loading: false,
      replyLoading: false,
      commentList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        rating: '',
        replyStatus: ''
      },
      statistics: {
        total: 0,
        avgRating: 0,
        replyCount: 0,
        unreplyCount: 0
      },
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
          this.commentList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载评价列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    loadStatistics() {
      // 从当前列表计算统计数据（临时方案）
      this.updateStatistics();
    },

    updateStatistics() {
      const list = this.commentList;
      const total = list.length;
      let sumRating = 0;
      let replyCount = 0;

      list.forEach(item => {
        sumRating += item.rating || 0;
        if (item.reply) replyCount++;
      });

      this.statistics = {
        total: total,
        avgRating: total > 0 ? (sumRating / total).toFixed(1) : 0,
        replyCount: replyCount,
        unreplyCount: total - replyCount
      };
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

    handlePageChange(page) {
      this.page = page;
      this.loadList();
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadList();
    },

    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      try {
        const res = await updateServiceCommentStatus(row.id, newStatus);
        if (res.code === 200) {
          row.status = newStatus;
          this.$message.success(newStatus === 1 ? '显示成功' : '隐藏成功');
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
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
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('回复失败');
      } finally {
        this.replyLoading = false;
      }
    },

    async handleDelete(row) {
      this.$confirm('确定要删除该评价吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deleteServiceComment(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;

      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条评价吗？`, '警告', { type: 'warning' })
          .then(async () => {
            try {
              const res = await batchDeleteServiceComments(ids);
              if (res.code === 200) {
                this.$message.success(res.message);
                this.selectedRows = [];
                this.loadList();
              } else {
                this.$message.error(res.message);
              }
            } catch (error) {
              this.$message.error('批量删除失败');
            }
          }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.service-comment-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
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
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

.stat-rating { background: linear-gradient(135deg, #e6a23c, #f0b45c); }
.stat-reply { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-pending-reply { background: linear-gradient(135deg, #409EFF, #66b1ff); }

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
  justify-content: space-between;
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
  width: 220px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.status-select {
  width: 110px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  border-radius: 8px;
  padding: 8px 16px;
}

.comment-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}

.service-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.service-name {
  font-weight: 500;
  color: #2c3e50;
}

.service-id {
  font-size: 11px;
  color: #999;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
}

.comment-content {
  color: #5a6874;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-icon-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  transition: all 0.2s;
}

.action-icon-btn:hover {
  transform: scale(1.1);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
}

/* 回复对话框 */
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

.original-comment {
  background: #f8f9fc;
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

.comment-header .user-info {
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.original-comment .comment-content {
  margin: 12px 0;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
}

.service-tag {
  display: inline-block;
  padding: 4px 12px;
  background: #e8f4fd;
  color: #409EFF;
  border-radius: 20px;
  font-size: 12px;
}

.service-tag i {
  margin-right: 4px;
}

.reply-section {
  margin-top: 8px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 12px;
}

.section-title i {
  color: #667eea;
}

.old-reply {
  margin-top: 12px;
  padding: 12px;
  background: #fef9e6;
  border-radius: 8px;
  font-size: 13px;
}

.old-reply-label {
  color: #e6a23c;
  font-weight: 500;
  margin-right: 8px;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
}

@media (max-width: 768px) {
  .service-comment-manage {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
  }

  .action-right {
    justify-content: center;
  }
}
</style>