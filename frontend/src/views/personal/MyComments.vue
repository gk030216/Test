<template>
  <div class="my-comments-page">
    <h2 class="page-title">我的评论</h2>

    <div class="comments-grid" v-loading="loading">
      <div class="comment-card" v-for="comment in commentsList" :key="comment.id">
        <!-- 原帖信息 -->
        <div class="post-section" @click="goToDetail(comment.postId)">
          <div class="post-header">
            <div class="post-author">
              <el-avatar :size="32" :src="comment.postAuthorAvatar" class="author-avatar">
                {{ getAuthorInitial(comment.postAuthorName) }}
              </el-avatar>
              <div class="author-info">
                <span class="author-name">{{ comment.postAuthorName || '匿名用户' }}</span>
                <span class="post-tag">的原帖</span>
              </div>
            </div>
            <span class="post-time">{{ formatRelativeTime(comment.postCreateTime) }}</span>
          </div>
          <h4 class="post-title">{{ comment.postTitle || '无标题' }}</h4>
          <p class="post-content">{{ truncateText(comment.postContent, 80) }}</p>
        </div>

        <!-- 我的评论区域 -->
        <div class="my-comment-section">
          <div class="comment-label">
            <i class="el-icon-chat-line-round"></i>
            <span>我的评论</span>
            <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
          </div>

          <!-- 如果是子评论，显示回复的是哪条主评论 -->
          <div class="parent-comment-info" v-if="comment.parentId !== 0 && comment.parentId !== null">
            <div class="parent-comment-header">
              <i class="el-icon-chat-dot-round"></i>
              <span class="parent-comment-label">回复</span>
              <span class="parent-comment-author">
                @{{ comment.parentCommentAuthorName || comment.replyToUserName || '用户' }}
              </span>
            </div>
            <div class="parent-comment-text" v-if="comment.parentCommentContent">
              “{{ truncateText(comment.parentCommentContent, 60) }}”
            </div>
            <div class="parent-comment-text deleted-text" v-else>
              <i class="el-icon-info"></i> 原评论内容已不可见
            </div>
          </div>

          <!-- 我的评论内容 -->
          <div class="comment-content">{{ comment.content }}</div>

          <!-- 评论底部操作栏 - 按钮样式与我的收藏一致 -->
          <div class="comment-footer">
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
        />
      </div>
    </div>
  </div>
</template>

<script>
import { getUserComments, adminDeleteComment } from '@/api/community';

export default {
  name: 'MyComments',
  data() {
    return {
      loading: false,
      commentsList: [],
      total: 0,
      page: 1,
      pageSize: 12
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

          console.log('===== 评论列表调试信息 =====');
          this.commentsList.forEach(comment => {
            console.log({
              id: comment.id,
              content: comment.content,
              parentId: comment.parentId,
              parentCommentContent: comment.parentCommentContent,
              parentCommentAuthorName: comment.parentCommentAuthorName,
              replyToUserName: comment.replyToUserName
            });
          });
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async deleteComment(comment) {
      this.$confirm('确定要删除这条评论吗？', '提示', { type: 'warning' })
          .then(async () => {
            const res = await adminDeleteComment(comment.id);
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadComments();
            } else {
              this.$message.error(res.message || '删除失败');
            }
          })
          .catch(() => {});
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
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
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
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.comments-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.comment-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  border: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.comment-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

/* 原帖区域 */
.post-section {
  padding: 16px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.post-section:hover {
  background: #f5f7fa;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
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

.author-info {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.author-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 13px;
}

.post-tag {
  color: #999;
  font-size: 12px;
}

.post-time {
  font-size: 11px;
  color: #999;
  flex-shrink: 0;
}

.post-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-content {
  color: #999;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 我的评论区域 */
.my-comment-section {
  padding: 16px;
  flex: 1;
}

.comment-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  color: #667eea;
  font-weight: 500;
  font-size: 12px;
}

.comment-label i {
  font-size: 14px;
}

.comment-time {
  margin-left: auto;
  font-weight: normal;
  color: #999;
  font-size: 11px;
}

.comment-content {
  color: #333;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 主评论信息（被回复的评论） */
.parent-comment-info {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
  border-left: 3px solid #667eea;
}

.parent-comment-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.parent-comment-header i {
  font-size: 12px;
  color: #667eea;
}

.parent-comment-label {
  font-size: 11px;
  color: #999;
}

.parent-comment-author {
  font-size: 12px;
  font-weight: 500;
  color: #667eea;
}

.parent-comment-text {
  font-size: 12px;
  color: #888;
  font-style: italic;
  line-height: 1.5;
  word-break: break-word;
}

.parent-comment-text.deleted-text {
  color: #f56c6c;
  font-style: normal;
}

.parent-comment-text.deleted-text i {
  margin-right: 4px;
}

/* 评论底部操作栏 - 按钮样式与我的收藏一致 */
.comment-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
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
  padding: 12px 16px;
  background: #f0f9f4;
  border-left: 3px solid #67c23a;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  color: #67c23a;
  font-size: 12px;
  font-weight: 500;
}

.reply-header i {
  font-size: 12px;
}

.reply-author {
  font-weight: 600;
}

.reply-content {
  color: #666;
  font-size: 12px;
  line-height: 1.5;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #ddd;
}

.pagination {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 900px) {
  .comments-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .comments-grid {
    grid-template-columns: 1fr;
  }

  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .footer-right {
    flex-wrap: wrap;
    justify-content: flex-end;
  }
}
</style>