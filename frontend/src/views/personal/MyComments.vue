<template>
  <div class="my-comments-page">
    <h2 class="page-title">我的评论</h2>

    <div class="comments-list" v-loading="loading">
      <div class="comment-item" v-for="comment in commentsList" :key="comment.id">
        <div class="comment-header">
          <div class="post-info">
            <span class="post-title" @click="goToDetail(comment.postId)">{{ comment.postTitle }}</span>
            <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
          </div>
          <div class="comment-actions" @click.stop>
            <el-button type="text" size="small" class="delete-btn" @click="deleteComment(comment)">
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </div>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div class="comment-reply" v-if="comment.reply">
          <div class="reply-label">
            <i class="el-icon-chat-dot-round"></i>
            <span>作者回复：</span>
          </div>
          <div class="reply-text">{{ comment.reply }}</div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && commentsList.length === 0">
        <i class="el-icon-chat-dot-round"></i>
        <p>暂无评论</p>
        <el-button type="primary" size="small" @click="$router.push('/community')">去逛逛</el-button>
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
    </div>
  </div>
</template>

<script>
import { getUserComments, deleteComment } from '@/api/community';

export default {
  name: 'MyComments',
  data() {
    return {
      loading: false,
      commentsList: [],
      total: 0,
      page: 1,
      pageSize: 10
    };
  },
  created() {
    this.loadComments();
  },
  methods: {
    async loadComments() {
      this.loading = true;
      try {
        const res = await getUserComments({ page: this.page, pageSize: this.pageSize });
        if (res.code === 200) {
          this.commentsList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async deleteComment(comment) {
      this.$confirm('确定要删除这条评论吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deleteComment(comment.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadComments();
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },
    goToDetail(postId) {
      this.$router.push(`/community/post/${postId}`);
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s;
}

.comment-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.post-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.post-title {
  font-weight: 600;
  color: #409EFF;
  cursor: pointer;
}

.post-title:hover {
  text-decoration: underline;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  color: #555;
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-reply {
  background: #e8f8f0;
  border-radius: 12px;
  padding: 12px 16px;
  margin-top: 10px;
}

.reply-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #67c23a;
  margin-bottom: 6px;
}

.reply-text {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.delete-btn {
  color: #f56c6c;
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>