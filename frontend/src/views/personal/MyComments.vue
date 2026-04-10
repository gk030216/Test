<template>
  <div class="my-comments-page">
    <h2 class="page-title">我的评论</h2>

    <div class="comments-list" v-loading="loading">
      <div class="comment-card" v-for="comment in commentsList" :key="comment.id">
        <!-- 原帖信息 -->
        <div class="post-section" @click="goToDetail(comment.postId)">
          <div class="post-header">
            <div class="post-author">
              <el-avatar :size="32" :src="comment.postAuthorAvatar" class="author-avatar">
                {{ getAuthorInitial(comment.postAuthorName) }}
              </el-avatar>
              <span class="author-name">{{ comment.postAuthorName || '匿名用户' }}</span>
              <span class="post-tag">的原帖</span>
            </div>
            <span class="post-time">{{ formatRelativeTime(comment.postCreateTime) }}</span>
          </div>
          <h4 class="post-title">{{ comment.postTitle || '无标题' }}</h4>
          <p class="post-content">{{ truncateText(comment.postContent, 100) }}</p>
        </div>

        <!-- 我的评论 -->
        <div class="my-comment-section">
          <div class="comment-label">
            <i class="el-icon-chat-line-round"></i>
            <span>我的评论</span>
            <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>

          <div class="comment-footer">
            <div class="footer-left">
              <div class="stat-item">
                <i class="el-icon-star-on"></i>
                <span>{{ comment.likeCount || 0 }}</span>
              </div>
            </div>
            <div class="footer-right">
              <el-button
                  type="primary"
                  size="small"
                  plain
                  @click.stop="goToDetail(comment.postId)"
                  class="action-btn view-btn"
              >
                <i class="el-icon-view"></i> 查看原帖
              </el-button>
              <el-button
                  type="danger"
                  size="small"
                  plain
                  @click.stop="deleteComment(comment)"
                  class="action-btn delete-btn"
              >
                <i class="el-icon-delete"></i> 删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 作者回复 -->
        <div class="reply-section" v-if="comment.reply">
          <div class="reply-header">
            <i class="el-icon-chat-dot-round"></i>
            <span class="reply-author">{{ comment.postAuthorName || '作者' }}</span>
            <span>回复</span>
          </div>
          <div class="reply-content">{{ comment.reply }}</div>
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
      this.$confirm('确定要删除这条评论吗？', '提示', {type: 'warning'}).then(async () => {
        try {
          const res = await deleteComment(comment.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadComments();
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {
      });
    },

    goToDetail(postId) {
      if (postId) {
        this.$router.push(`/community/post/${postId}`);
      }
    },

    getAuthorInitial(name) {
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
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

    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    },

    handlePageChange(page) {
      this.page = page;
      this.loadComments();
    }
  }
};
</script>

<style scoped>
.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #2c3e50;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-card {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.comment-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
}

/* 原帖区域 */
.post-section {
  padding: 20px 24px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}

.post-section:hover {
  background: #f5f7fa;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  flex-shrink: 0;
}

.author-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.post-tag {
  color: #999;
  font-size: 13px;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.post-content {
  color: #5a6874;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 我的评论区域 */
.my-comment-section {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  color: #667eea;
  font-weight: 500;
  font-size: 13px;
}

.comment-label i {
  font-size: 16px;
}

.comment-time {
  margin-left: auto;
  font-weight: normal;
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #2c3e50;
  font-size: 15px;
  line-height: 1.7;
  margin-bottom: 16px;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

.stat-item i {
  font-size: 16px;
  color: #ff9900;
}

.footer-right {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s;
}

.action-btn i {
  margin-right: 4px;
  font-size: 13px;
}

.view-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.view-btn:hover {
  color: white;
  background: #409EFF;
  border-color: #409EFF;
}

.delete-btn {
  color: #f56c6c;
  border-color: #fde2e2;
  background: #fef0f0;
}

.delete-btn:hover {
  color: white;
  background: #f56c6c;
  border-color: #f56c6c;
}

/* 作者回复区域 */
.reply-section {
  padding: 16px 24px;
  background: #f0f9f4;
  border-left: 4px solid #67c23a;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  color: #67c23a;
  font-size: 13px;
  font-weight: 500;
}

.reply-header i {
  font-size: 14px;
}

.reply-author {
  font-weight: 600;
}

.reply-content {
  color: #5a6874;
  font-size: 14px;
  line-height: 1.6;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #dcdfe6;
}

.empty-state p {
  margin-bottom: 20px;
  font-size: 16px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .post-section,
  .my-comment-section,
  .reply-section {
    padding: 16px;
  }

  .comment-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .footer-right {
    width: 100%;
    justify-content: flex-end;
  }

  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>