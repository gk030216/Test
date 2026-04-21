<template>
  <div class="admin-comment-manage">
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
              placeholder="搜索帖子/用户/内容"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          ></el-input>
        </div>
        <el-select
            v-model="searchForm.status"
            placeholder="状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="正常" :value="1"></el-option>
          <el-option label="已隐藏" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
      </div>
    </div>

    <!-- 评论表格 -->
    <el-table
        v-loading="loading"
        :data="commentList"
        stripe
        class="comment-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <el-table-column label="帖子信息" min-width="200">
        <template slot-scope="scope">
          <div class="post-info">
            <span class="post-title" @click="goToPost(scope.row.postId)">
              {{ scope.row.postTitle || '查看原帖' }}
            </span>
            <span class="post-id">帖子ID: {{ scope.row.postId }}</span>
          </div>
        </template>
      </el-table-column>

      <!-- 修改：评论用户列，显示昵称 -->
      <el-table-column label="评论用户" width="160">
        <template slot-scope="scope">
          <div class="user-info">
            <el-avatar :size="32" :src="scope.row.userAvatar" class="user-avatar">
              {{ (scope.row.userNickname || scope.row.userName) ? (scope.row.userNickname || scope.row.userName).charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
            <div class="user-detail">
              <span class="user-name">{{ scope.row.userNickname || scope.row.userName || '匿名用户' }}</span>
              <span class="user-id">ID: {{ scope.row.userId }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="评论内容" min-width="280">
        <template slot-scope="scope">
          <div class="comment-content">{{ scope.row.content }}</div>
          <div class="reply-info" v-if="scope.row.parentId !== 0">
            <i class="el-icon-chat-line-round"></i>
            <span>回复 @{{ scope.row.replyToUserName }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
          ></el-switch>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="评论时间" width="160">
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
                @click="viewDetail(scope.row)"
                class="action-icon-btn"
                title="详情"
            >
              <i class="el-icon-view"></i>
            </el-button>
            <el-button
                v-if="scope.row.status === 1"
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
            <el-button
                v-else
                size="small"
                type="success"
                plain
                circle
                @click="handleRestore(scope.row)"
                class="action-icon-btn"
                title="恢复"
            >
              <i class="el-icon-refresh"></i>
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

    <!-- 评论详情对话框 -->
    <el-dialog
        title="评论详情"
        :visible.sync="detailVisible"
        width="550px"
        center
        class="detail-dialog"
    >
      <div class="detail-content" v-if="currentComment">
        <!-- 帖子信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-document"></i>
            <span>所属帖子</span>
          </div>
          <div class="post-card" @click="goToPost(currentComment.postId)">
            <h4>{{ currentComment.postTitle }}</h4>
          </div>
        </div>

        <!-- 评论信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-chat-dot-round"></i>
            <span>评论信息</span>
          </div>
          <div class="comment-card">
            <div class="comment-header">
              <!-- ✅ 修改：显示昵称 -->
              <div class="user-info">
                <el-avatar :size="36" :src="currentComment.userAvatar">
                  {{ (currentComment.userNickname || currentComment.userName) ? (currentComment.userNickname || currentComment.userName).charAt(0).toUpperCase() : 'U' }}
                </el-avatar>
                <div class="user-detail">
                  <span class="user-name">{{ currentComment.userNickname || currentComment.userName || '匿名用户' }}</span>
                  <span class="user-id">ID: {{ currentComment.userId }}</span>
                </div>
              </div>
              <span class="comment-time">{{ formatDate(currentComment.createTime) }}</span>
            </div>
            <div class="comment-content">{{ currentComment.content }}</div>
            <div class="comment-meta" v-if="currentComment.parentId !== 0">
              <span><i class="el-icon-chat-line-round"></i> 回复 @{{ currentComment.replyToUserName }}</span>
            </div>
          </div>
        </div>

        <!-- 子回复列表 -->
        <div class="detail-section" v-if="replies.length > 0">
          <div class="section-title">
            <i class="el-icon-chat-line-round"></i>
            <span>回复列表 ({{ replies.length }})</span>
          </div>
          <div class="replies-list">
            <div class="reply-item" v-for="reply in replies" :key="reply.id">
              <!-- ✅ 修改：显示昵称 -->
              <div class="reply-header">
                <span class="reply-user">{{ reply.userNickname || reply.userName || '匿名用户' }}</span>
                <span class="reply-to" v-if="reply.replyToUserName">
                  回复 @{{ reply.replyToUserName }}
                </span>
                <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
              </div>
              <div class="reply-content">{{ reply.content }}</div>
              <div class="reply-actions">
                <el-button
                    v-if="reply.status === 1"
                    type="text"
                    size="small"
                    class="delete-btn"
                    @click="handleDelete(reply)"
                >
                  删除
                </el-button>
                <el-button
                    v-else
                    type="text"
                    size="small"
                    class="restore-btn"
                    @click="handleRestore(reply)"
                >
                  恢复
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminCommentList,
  updateCommentStatus,
  adminDeleteComment,
  batchDeleteComments,
  restoreComment,
  getCommentReplies
} from '@/api/community';

export default {
  name: 'AdminCommentManage',
  data() {
    return {
      loading: false,
      commentList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        status: ''
      },
      detailVisible: false,
      currentComment: null,
      replies: []
    };
  },
  created() {
    this.loadList();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined
        };
        const res = await getAdminCommentList(params);
        if (res.code === 200) {
          this.commentList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载评论列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    handleSearch() {
      this.page = 1;
      this.loadList();
    },

    handleReset() {
      this.searchForm = { keyword: '', status: '' };
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
        const res = await updateCommentStatus(row.id, newStatus);
        if (res.code === 200) {
          row.status = newStatus;
          this.$message.success(newStatus === 1 ? '已显示' : '已隐藏');
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    async viewDetail(row) {
      this.currentComment = row;
      this.detailVisible = true;

      if (row.parentId === 0) {
        try {
          const res = await getCommentReplies(row.id);
          if (res.code === 200) {
            this.replies = res.data || [];
          }
        } catch (error) {
          console.error('加载回复失败', error);
        }
      } else {
        this.replies = [];
      }
    },

    goToPost(postId) {
      if (postId) {
        window.open(`/community/post/${postId}`, '_blank');
      }
    },

    async handleDelete(row) {
      this.$confirm('确定要删除该评论吗？删除后无法恢复！', '警告', { type: 'warning' }).then(async () => {
        try {
          const res = await adminDeleteComment(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadList();
            if (this.detailVisible) {
              this.viewDetail(this.currentComment);
            }
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    async handleRestore(row) {
      this.$confirm('确定要恢复该评论吗？', '提示', { type: 'info' }).then(async () => {
        try {
          const res = await restoreComment(row.id);
          if (res.code === 200) {
            this.$message.success('恢复成功');
            this.loadList();
            if (this.detailVisible) {
              this.viewDetail(this.currentComment);
            }
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('恢复失败');
        }
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;

      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条评论吗？删除后无法恢复！`, '警告', { type: 'warning' })
          .then(async () => {
            try {
              const res = await batchDeleteComments(ids);
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
    },
  }
};
</script>

<style scoped>
/* 样式保持不变，添加用户详情样式 */
.admin-comment-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.action-bar {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-left {
  display: flex;
  gap: 12px;
}

.action-right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.batch-btn {
  border-radius: 8px;
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

.reset-btn {
  border-radius: 8px;
  padding: 8px 16px;
}

.comment-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.post-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.post-title {
  font-weight: 500;
  color: #409EFF;
  cursor: pointer;
}

.post-title:hover {
  text-decoration: underline;
}

.post-id {
  font-size: 11px;
  color: #999;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  flex-shrink: 0;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
  font-size: 13px;
}

.user-id {
  font-size: 11px;
  color: #999;
}

.comment-content {
  color: #5a6874;
  line-height: 1.5;
}

.reply-info {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.reply-info i {
  margin-right: 4px;
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
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

.detail-section {
  margin-bottom: 24px;
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

.post-card {
  padding: 12px 16px;
  background: #f8f9fc;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.3s;
}

.post-card:hover {
  background: #e8f4fd;
}

.post-card h4 {
  margin: 0;
  font-size: 15px;
  color: #409EFF;
}

.comment-card {
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 10px;
}

.comment-header .user-info {
  gap: 12px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-card .comment-content {
  margin-bottom: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
}

.comment-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.comment-meta i {
  margin-right: 4px;
}

.replies-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-item {
  padding: 12px 16px;
  background: #f8f9fc;
  border-radius: 12px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  flex-wrap: wrap;
}

.reply-user {
  font-weight: 500;
  color: #2c3e50;
}

.reply-to {
  color: #667eea;
}

.reply-time {
  margin-left: auto;
  font-size: 12px;
  color: #999;
}

.reply-content {
  color: #5a6874;
  line-height: 1.5;
  margin-bottom: 8px;
}

.reply-actions {
  text-align: right;
}

.delete-btn {
  color: #f56c6c;
}

.restore-btn {
  color: #67c23a;
}

@media (max-width: 768px) {
  .admin-comment-manage {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
  }

  .action-left, .action-right {
    width: 100%;
    justify-content: center;
  }

  .reply-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .reply-time {
    margin-left: 0;
  }
}
</style>