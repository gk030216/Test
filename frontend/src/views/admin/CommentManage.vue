<template>
  <div class="comment-manage">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="danger" plain @click="handleBatchDelete" :disabled="selectedRows.length === 0">
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户/内容"
            clearable
            size="medium"
            style="width: 200px"
            @keyup.enter="handleSearch"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
        <el-select
            v-model="searchForm.rating"
            placeholder="评分"
            clearable
            size="medium"
            style="width: 100px"
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
            style="width: 100px"
            @change="handleSearch"
        >
          <el-option label="显示" :value="1"></el-option>
          <el-option label="隐藏" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch">搜索</el-button>
        <el-button size="medium" @click="handleReset">重置</el-button>
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
      <el-table-column prop="productId" label="商品ID" width="80" align="center"></el-table-column>
      <el-table-column prop="userName" label="用户" width="120">
        <template slot-scope="scope">
          <div class="user-info">
            <el-avatar :size="32" class="user-avatar">{{ scope.row.userName.charAt(0) }}</el-avatar>
            <span>{{ scope.row.userName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="评分" width="120" align="center">
        <template slot-scope="scope">
          <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900"></el-rate>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="250">
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
      <el-table-column label="商家回复" width="150">
        <template slot-scope="scope">
          <div v-if="scope.row.reply" class="reply-text">
            {{ scope.row.reply }}
            <div class="reply-time">{{ formatDate(scope.row.replyTime) }}</div>
          </div>
          <el-button v-else size="small" type="text" @click="openReplyDialog(scope.row)">回复</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="small" type="text" @click="openReplyDialog(scope.row)">回复</el-button>
          <el-button size="small" type="text" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
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
    <el-dialog title="回复评价" :visible.sync="replyVisible" width="450px" center>
      <div class="reply-dialog">
        <div class="original-comment" v-if="currentComment">
          <div class="comment-user">
            <el-avatar :size="32">{{ currentComment.userName ? currentComment.userName.charAt(0) : 'U' }}</el-avatar>
            <div class="comment-info">
              <span class="user-name">{{ currentComment.userName }}</span>
              <el-rate v-model="currentComment.rating" disabled></el-rate>
            </div>
          </div>
          <div class="comment-content">{{ currentComment.content }}</div>
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
      <span slot="footer">
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replyLoading">确定</el-button>
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
        status: ''
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
          status: this.searchForm.status || undefined
        };
        const res = await getCommentList(params);
        if (res.code === 200) {
          this.commentList = res.data.list;
          this.total = res.data.total;
        }
      } catch (error) {
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
      this.searchForm = { keyword: '', rating: '', status: '' };
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
          this.$message.success(res.message);
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
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

.action-left, .action-right {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.comment-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
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

.comment-content {
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.comment-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
}

.reply-text {
  color: #67c23a;
  font-size: 13px;
}

.reply-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
}

.reply-dialog {
  padding: 10px 0;
}

.original-comment {
  background: #f8f9fc;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 20px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.comment-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 500;
  color: #333;
}
</style>