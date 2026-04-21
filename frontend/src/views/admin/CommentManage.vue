<template>
  <div class="comment-manage">
    <!-- 顶部操作栏 -->
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
              placeholder="搜索商品/用户/内容"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          >
          </el-input>
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
            v-model="searchForm.status"
            placeholder="状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="全部" :value="''"></el-option>
          <el-option label="显示" :value="1"></el-option>
          <el-option label="隐藏" :value="0"></el-option>
        </el-select>
        <el-select
            v-model="searchForm.replyStatus"
            placeholder="回复状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="全部" :value="''"></el-option>
          <el-option label="未回复" :value="0"></el-option>
          <el-option label="已回复" :value="1"></el-option>
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
        style="width: 100%"
        class="comment-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <!-- 商品信息列 -->
      <el-table-column label="商品信息" min-width="280">
        <template slot-scope="scope">
          <div class="product-info-cell">
            <el-image
                :src="scope.row.productImage"
                class="product-img"
                fit="cover"
                :preview-src-list="[scope.row.productImage]"
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
                <span>暂无图片</span>
              </div>
            </el-image>
            <div class="product-detail">
              <div class="product-name">{{ scope.row.productName || '商品已删除' }}</div>
              <div class="product-price">¥{{ scope.row.productPrice || '0.00' }}</div>
              <div class="product-id">商品ID: {{ scope.row.productId }}</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 用户列 -->
      <el-table-column label="用户" width="160">
        <template slot-scope="scope">
          <div class="user-info">
            <el-avatar
                :size="40"
                :src="scope.row.userAvatar"
                class="user-avatar"
            >
              {{ (scope.row.userNickname || scope.row.userName || 'U').charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="user-detail">
              <span class="user-name">{{ scope.row.userNickname || scope.row.userName }}</span>
              <span class="user-id">ID: {{ scope.row.userId }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 评分列 -->
      <el-table-column label="评分" width="120" align="center">
        <template slot-scope="scope">
          <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900"></el-rate>
        </template>
      </el-table-column>

      <!-- 评价内容列 -->
      <el-table-column label="评价内容" min-width="280">
        <template slot-scope="scope">
          <div class="comment-content">{{ scope.row.content }}</div>
          <div class="comment-images" v-if="scope.row.images">
            <el-image
                v-for="(img, idx) in scope.row.images.split(',')"
                :key="idx"
                :src="img"
                :preview-src-list="scope.row.images.split(',')"
                fit="cover"
                class="comment-img"
            ></el-image>
          </div>
        </template>
      </el-table-column>

      <!-- ✅ 回复状态列 -->
      <el-table-column label="回复状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.reply ? 'success' : 'info'" size="small">
            {{ scope.row.reply ? '已回复' : '未回复' }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 状态列 -->
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
              :loading="scope.row.statusLoading"
          >
          </el-switch>
        </template>
      </el-table-column>

      <!-- 评价时间 -->
      <el-table-column prop="createTime" label="评价时间" width="170">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>

      <!-- ❌ 删除商家回复列 -->

      <!-- 操作列 -->
      <el-table-column label="操作" width="140" align="center" fixed="right">
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
    <el-dialog title="回复评价" :visible.sync="replyVisible" width="550px" center class="reply-dialog">
      <div class="dialog-content">
        <!-- 商品信息 -->
        <div class="product-info-section">
          <div class="section-title">
            <i class="el-icon-goods"></i>
            <span>商品信息</span>
          </div>
          <div class="product-info-card" v-if="currentComment">
            <el-image
                :src="currentComment.productImage"
                class="product-img"
                fit="cover"
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
            <div class="product-detail">
              <div class="product-name">{{ currentComment.productName || '商品已删除' }}</div>
              <div class="product-price">¥{{ currentComment.productPrice || '0.00' }}</div>
            </div>
          </div>
        </div>

        <!-- 用户评价 -->
        <div class="user-comment-section">
          <div class="section-title">
            <i class="el-icon-chat-dot-round"></i>
            <span>用户评价</span>
          </div>
          <div class="original-comment" v-if="currentComment">
            <div class="comment-header">
              <div class="user-info">
                <el-avatar
                    :size="36"
                    :src="currentComment.userAvatar"
                    class="user-avatar"
                >
                  {{ (currentComment.userNickname || currentComment.userName || 'U').charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="user-detail">
                  <span class="user-name">{{ currentComment.userNickname || currentComment.userName }}</span>
                  <el-rate v-model="currentComment.rating" disabled text-color="#ff9900"></el-rate>
                </div>
              </div>
              <div class="comment-time">{{ formatDate(currentComment.createTime) }}</div>
            </div>
            <div class="comment-content">{{ currentComment.content }}</div>
            <div class="comment-images" v-if="currentComment.images">
              <el-image
                  v-for="(img, idx) in currentComment.images.split(',')"
                  :key="idx"
                  :src="img"
                  fit="cover"
                  class="comment-img"
              ></el-image>
            </div>
          </div>
        </div>

        <!-- 回复输入 -->
        <div class="reply-input-section">
          <div class="section-title">
            <i class="el-icon-edit"></i>
            <span>商家回复</span>
          </div>
          <el-input
              type="textarea"
              v-model="replyContent"
              rows="4"
              placeholder="请输入回复内容..."
              maxlength="500"
              show-word-limit
          ></el-input>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replyLoading">
          <i class="el-icon-check"></i> 确定回复
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getCommentList,
  updateCommentStatus,
  deleteComment,
  batchDeleteComments,
  replyComment
} from '@/api/comment';

export default {
  name: 'CommentManage',
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
        status: '',
        replyStatus: ''
      },
      replyVisible: false,
      replyContent: '',
      currentComment: null
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
          rating: this.searchForm.rating || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined,
          replyStatus: this.searchForm.replyStatus !== '' ? this.searchForm.replyStatus : undefined
        };
        const res = await getCommentList(params);
        if (res.code === 200) {
          // ✅ 过滤掉商品已删除的评论（productName 为 null 或空）
          this.commentList = (res.data.list || [])
              .filter(item => item.productName !== null && item.productName !== '')
              .map(item => ({
                ...item,
                userNickname: item.userNickname || null,
                statusLoading: false
              }));
          this.total = this.commentList.length;
        }
      } catch (error) {
        console.error('加载评价列表失败', error);
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
      this.searchForm = {
        keyword: '',
        rating: '',
        status: '',
        replyStatus: ''
      };
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
      const action = newStatus === 1 ? '显示' : '隐藏';

      this.$confirm(`确定要${action}该评价吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: newStatus === 1 ? 'info' : 'warning'
      }).then(async () => {
        row.statusLoading = true;
        try {
          const res = await updateCommentStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(`${action}成功`);
            // 如果当前筛选状态与修改后的状态不符，刷新列表
            if (this.searchForm.status !== '' && this.searchForm.status !== newStatus) {
              this.loadList();
            }
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('操作失败');
        } finally {
          row.statusLoading = false;
        }
      }).catch(() => {});
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除该评价吗？删除后无法恢复！`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteComment(row.id);
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
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条评价吗？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
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
        const res = await replyComment(this.currentComment.id, this.replyContent);
        if (res.code === 200) {
          this.$message.success('回复成功');
          this.replyVisible = false;
          this.loadList();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        console.error('回复失败', error);
        this.$message.error('回复失败');
      } finally {
        this.replyLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.comment-manage {
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
  flex-wrap: wrap;
  align-items: center;
}

.action-right {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.batch-btn {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s;
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
  width: 240px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.status-select {
  width: 100px;
}

.status-select ::v-deep .el-input__inner {
  border-radius: 8px;
}

.search-btn, .reset-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.comment-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.comment-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
}

/* 商品信息样式 */
.product-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #f5f5f5;
  object-fit: cover;
  flex-shrink: 0;
}

.product-detail {
  flex: 1;
}

.product-name {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 4px;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  color: #ff6b6b;
  font-size: 14px;
  font-weight: 600;
}

.product-id {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
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
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.user-id {
  font-size: 11px;
  color: #909399;
}

/* 评价内容样式 */
.comment-content {
  color: #5a6874;
  line-height: 1.6;
  margin-bottom: 8px;
  word-break: break-word;
}

.comment-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.comment-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
  transition: transform 0.2s;
}

.comment-img:hover {
  transform: scale(1.05);
}

/* 回复样式 */
.reply-text {
  background: #f8f9fc;
  padding: 10px 12px;
  border-radius: 12px;
}

.reply-content {
  color: #67c23a;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 6px;
}

.reply-time {
  font-size: 11px;
  color: #999;
  text-align: right;
}

/* 操作按钮 */
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

/* 分页 */
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.reply-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.reply-dialog ::v-deep .el-dialog__close {
  color: white;
}

.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eef2f6;
}

.section-title i {
  color: #667eea;
  font-size: 18px;
}

.product-info-card {
  display: flex;
  gap: 12px;
  background: #f8f9fc;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 20px;
}

.product-info-card .product-img {
  width: 70px;
  height: 70px;
}

.product-info-card .product-name {
  font-size: 14px;
  margin-bottom: 6px;
}

.original-comment {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 20px;
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
  flex: 1;
}

.comment-header .comment-time {
  font-size: 12px;
  color: #999;
}

.original-comment .comment-content {
  margin: 12px 0;
  padding: 8px 12px;
  background: white;
  border-radius: 12px;
}

.reply-input-section {
  margin-top: 8px;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
  border-top: 1px solid #eef2f6;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #999;
  font-size: 12px;
}

.image-slot i {
  font-size: 24px;
  margin-bottom: 4px;
}

/* 响应式 */
@media (max-width: 768px) {
  .comment-manage {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .action-left, .action-right {
    justify-content: center;
  }

  .product-info-cell {
    flex-direction: column;
    text-align: center;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
  }

  .user-detail {
    align-items: center;
  }

  .comment-header {
    flex-direction: column;
  }
}
</style>