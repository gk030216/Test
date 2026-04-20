<template>
  <div class="post-detail-container">
    <Navbar />

    <div class="post-detail-content">
      <div class="container">
        <!-- 顶部导航栏：面包屑 + 返回按钮同一行 -->
        <div class="top-nav">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/community' }">宠物社区</el-breadcrumb-item>
            <el-breadcrumb-item>{{ post.title }}</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" class="back-btn">返回</el-button>
        </div>

        <!-- 帖子内容 -->
        <div class="post-card" v-loading="loading">
          <div class="post-header">
            <div class="user-info">
              <el-avatar :size="50" :src="post.userAvatar" class="user-avatar">
                {{ !post.userAvatar ? getUserInitial(post.userNickname || post.userName) : '' }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ post.userNickname || post.userName || '匿名用户' }}</span>
                <span class="post-time">{{ formatDate(post.createTime) }}</span>
              </div>
            </div>
            <div class="post-badges">
              <el-tag v-if="post.isTop === 1" size="small" type="danger">置顶</el-tag>
              <el-tag v-if="post.isEssence === 1" size="small" type="warning">精华</el-tag>
            </div>
          </div>

          <h1 class="post-title">{{ post.title }}</h1>

          <div class="post-content" v-html="formatContent(post.content)"></div>

          <div class="post-images" v-if="post.images && post.images.length > 0">
            <el-image
                v-for="(img, idx) in getImageList(post.images)"
                :key="idx"
                :src="img"
                fit="cover"
                class="detail-img"
                :preview-src-list="getImageList(post.images)"
                @click.stop
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </div>

          <div class="post-actions">
            <div class="action-item" @click="handleLike">
              <i :class="['el-icon-star-on', { liked: post.isLiked }]"></i>
              <span>{{ post.likeCount || 0 }} 点赞</span>
            </div>
            <div class="action-item" @click="handleFavorite">
              <i :class="['el-icon-star-off', { favorited: post.isFavorited }]"></i>
              <span>{{ post.isFavorited ? '已收藏' : '收藏' }}</span>
            </div>
            <div class="action-item">
              <i class="el-icon-view"></i>
              <span>{{ post.viewCount || 0 }} 浏览</span>
            </div>
          </div>
        </div>

        <!-- 评论区域 -->
        <div class="comment-section">
          <h3 class="comment-title">
            <i class="el-icon-chat-dot-round"></i>
            评论 ({{ commentTotal }})
          </h3>

          <div class="comment-input-wrapper">
            <el-avatar :size="40" :src="currentUserAvatar" class="comment-avatar">
              {{ !currentUserAvatar ? getUserInitial(currentUserNickname || currentUserName) : '' }}
            </el-avatar>
            <div class="comment-input-area">
              <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="3"
                  placeholder="写下你的评论..."
                  maxlength="500"
                  show-word-limit
              ></el-input>
              <div class="comment-actions">
                <el-button type="primary" size="small" @click="submitComment" :loading="commentLoading">
                  发表评论
                </el-button>
              </div>
            </div>
          </div>

          <div class="comment-list" v-loading="commentListLoading">
            <div class="comment-item" v-for="comment in commentList" :key="comment.id">
              <el-avatar :size="36" :src="comment.userAvatar" class="comment-avatar">
                {{ !comment.userAvatar ? getUserInitial(comment.userNickname || comment.userName) : '' }}
              </el-avatar>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="user-name">{{ comment.userNickname || comment.userName || '匿名用户' }}</span>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-footer">
                  <span class="reply-btn" @click="showReplyInput(comment)">回复</span>
                  <span v-if="comment.userId === currentUserId" class="delete-btn" @click="deleteComment(comment.id)">删除</span>
                </div>

                <div class="reply-input-wrapper" v-if="replyTarget && replyTarget.id === comment.id">
                  <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="`回复 ${comment.userNickname || comment.userName}：`"
                      size="small"
                  ></el-input>
                  <div class="reply-actions">
                    <el-button size="mini" @click="cancelReply">取消</el-button>
                    <el-button type="primary" size="mini" @click="submitReply(comment)" :loading="replyLoading">
                      回复
                    </el-button>
                  </div>
                </div>

                <div class="reply-list" v-if="comment.replies && comment.replies.length">
                  <div class="reply-item" v-for="reply in comment.replies" :key="reply.id">
                    <div class="reply-header">
                      <span class="reply-user">{{ reply.userNickname || reply.userName || '匿名用户' }}</span>
                      <span class="reply-to" v-if="reply.replyToUserName">
                        回复 @{{ reply.replyToUserName }}
                      </span>
                      <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
                    </div>
                    <div class="reply-content">{{ reply.content }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="empty-comment" v-if="!commentListLoading && commentList.length === 0">
              <i class="el-icon-chat-dot-round"></i>
              <p>暂无评论，快来抢沙发！</p>
            </div>

            <div class="comment-pagination" v-if="commentTotal > 10">
              <el-pagination
                  @current-change="handleCommentPageChange"
                  :current-page="commentPage"
                  :page-size="10"
                  layout="prev, pager, next"
                  :total="commentTotal"
                  small
              ></el-pagination>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import {
  getPostDetail,
  toggleLike,
  toggleFavorite,
  getComments,
  addComment,
  adminDeleteComment
} from '@/api/community';

export default {
  name: 'PostDetail',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      commentListLoading: false,
      commentLoading: false,
      replyLoading: false,
      post: {
        id: null,
        title: '',
        content: '',
        images: '',
        userName: '',
        userNickname: '',
        userAvatar: '',
        likeCount: 0,
        viewCount: 0,
        commentCount: 0,
        isLiked: false,
        isFavorited: false,
        isTop: 0,
        isEssence: 0,
        createTime: ''
      },
      commentList: [],
      commentTotal: 0,
      commentPage: 1,
      commentContent: '',
      replyContent: '',
      replyTarget: null
    };
  },
  computed: {
    postId() {
      return this.$route.params.id;
    },
    currentUserId() {
      const userInfo = localStorage.getItem('userInfo');
      return userInfo ? JSON.parse(userInfo).id : null;
    },
    currentUserName() {
      const userInfo = localStorage.getItem('userInfo');
      return userInfo ? JSON.parse(userInfo).username : '用户';
    },
    currentUserNickname() {
      const userInfo = localStorage.getItem('userInfo');
      return userInfo ? JSON.parse(userInfo).nickname : null;
    },
    currentUserAvatar() {
      const userInfo = localStorage.getItem('userInfo');
      return userInfo ? JSON.parse(userInfo).avatar : '';
    }
  },
  created() {
    this.loadPost();
    this.loadComments();
  },
  methods: {
    getImageList(images) {
      if (!images) return [];
      if (typeof images === 'string') {
        return images.split(',').filter(img => img && img.trim().length > 0);
      }
      return [];
    },

    getUserInitial(name) {
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
    },

    formatContent(content) {
      if (!content) return '';
      return content.replace(/\n/g, '<br>');
    },

    async loadPost() {
      this.loading = true;
      try {
        const res = await getPostDetail(this.postId);
        if (res.code === 200) {
          this.post = res.data;
          this.post.isLiked = res.data.isLiked === true || res.data.isLiked === 1;
          this.post.isFavorited = res.data.isFavorited === true || res.data.isFavorited === 1;
        } else {
          this.$message.error('帖子不存在');
          this.$router.push('/community');
        }
      } catch (error) {
        console.error('加载帖子失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadComments() {
      this.commentListLoading = true;
      try {
        const res = await getComments(this.postId, { page: this.commentPage, pageSize: 10 });
        if (res.code === 200) {
          this.commentList = res.data.list || [];
          this.commentTotal = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载评论失败', error);
      } finally {
        this.commentListLoading = false;
      }
    },

    async handleLike() {
      try {
        const res = await toggleLike(this.postId);
        if (res.code === 200) {
          this.post.isLiked = res.data.isLiked;
          this.post.likeCount += this.post.isLiked ? 1 : -1;
          this.$message.success(res.data.isLiked ? '点赞成功' : '取消点赞');
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    async handleFavorite() {
      try {
        const res = await toggleFavorite(this.postId);
        if (res.code === 200) {
          this.post.isFavorited = res.data.isFavorited;
          this.$message.success(res.data.isFavorited ? '收藏成功' : '取消收藏');
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    async submitComment() {
      if (!this.commentContent.trim()) {
        this.$message.warning('请输入评论内容');
        return;
      }

      this.commentLoading = true;
      try {
        const res = await addComment({
          postId: this.postId,
          content: this.commentContent,
          parentId: 0
        });
        if (res.code === 200) {
          this.$message.success('评论成功');
          this.commentContent = '';
          this.loadComments();
          this.loadPost();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('评论失败');
      } finally {
        this.commentLoading = false;
      }
    },

    showReplyInput(comment) {
      this.replyTarget = comment;
    },

    cancelReply() {
      this.replyTarget = null;
      this.replyContent = '';
    },

    async submitReply(parentComment) {
      if (!this.replyContent.trim()) {
        this.$message.warning('请输入回复内容');
        return;
      }

      this.replyLoading = true;
      try {
        const res = await addComment({
          postId: this.postId,
          content: this.replyContent,
          parentId: parentComment.id,
          replyToUserId: parentComment.userId,
          replyToUserName: parentComment.userNickname || parentComment.userName
        });
        if (res.code === 200) {
          this.$message.success('回复成功');
          this.replyContent = '';
          this.replyTarget = null;
          this.loadComments();
          this.loadPost();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('回复失败');
      } finally {
        this.replyLoading = false;
      }
    },

    async deleteComment(commentId) {
      this.$confirm('确定要删除这条评论吗？', '提示', { type: 'warning' })
          .then(async () => {
            const res = await adminDeleteComment(commentId);
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadComments();
            } else {
              this.$message.error(res.message || '删除失败');
            }
          })
          .catch(() => {});
    },

    handleCommentPageChange(page) {
      this.commentPage = page;
      this.loadComments();
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.post-detail-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.post-detail-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航栏：面包屑和返回按钮同一行 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.breadcrumb {
  flex: 1;
}

.back-btn {
  border-radius: 8px;
  color: #606266;
  background: white;
  border: 1px solid #eef2f6;
  padding: 8px 16px;
  font-size: 13px;
  transition: all 0.3s;
  flex-shrink: 0;
  margin-left: 16px;
}

.back-btn:hover {
  color: #409EFF;
  border-color: #409EFF;
  background: #ecf5ff;
}

/* 帖子卡片 */
.post-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  background: #409EFF;
  color: white;
}

.user-name {
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
}

.post-time {
  font-size: 13px;
  color: #909399;
  margin-left: 10px;
}

.post-badges {
  display: flex;
  gap: 8px;
}

.post-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #2c3e50;
}

.post-content {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 20px;
  font-size: 15px;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.detail-img {
  width: 180px;
  height: 180px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  background: #f5f5f5;
}

.post-actions {
  display: flex;
  gap: 30px;
  padding-top: 20px;
  border-top: 1px solid #eef2f6;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #909399;
  font-size: 14px;
  transition: color 0.3s;
}

.action-item:hover {
  color: #409EFF;
}

.action-item i.liked {
  color: #f56c6c;
}

.action-item i.favorited {
  color: #409EFF;
}

/* 评论区域 */
.comment-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  border: 1px solid #eef2f6;
}

.comment-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #2c3e50;
}

.comment-title i {
  margin-right: 8px;
  color: #409EFF;
}

.comment-input-wrapper {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eef2f6;
}

.comment-avatar {
  background: #409EFF;
  color: white;
  flex-shrink: 0;
}

.comment-input-area {
  flex: 1;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}

.comment-list {
  max-height: 600px;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 16px 0;
  border-bottom: 1px solid #eef2f6;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.comment-header .user-name {
  font-weight: 600;
  font-size: 14px;
  color: #2c3e50;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
  font-size: 14px;
}

.comment-footer {
  display: flex;
  gap: 16px;
}

.reply-btn, .delete-btn {
  font-size: 12px;
  color: #909399;
  cursor: pointer;
  transition: color 0.3s;
}

.reply-btn:hover {
  color: #409EFF;
}

.delete-btn:hover {
  color: #f56c6c;
}

.reply-input-wrapper {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.reply-actions {
  margin-top: 8px;
  text-align: right;
}

.reply-list {
  margin-top: 12px;
  padding-left: 20px;
  border-left: 2px solid #eef2f6;
}

.reply-item {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
  flex-wrap: wrap;
}

.reply-user {
  font-weight: 600;
  color: #2c3e50;
}

.reply-to {
  color: #409EFF;
}

.reply-time {
  font-size: 11px;
  color: #909399;
}

.reply-content {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

.empty-comment {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.empty-comment i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.comment-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #909399;
  font-size: 12px;
}

.image-slot i {
  font-size: 24px;
  margin-bottom: 4px;
}

/* 响应式 */
@media (max-width: 768px) {
  .post-card {
    padding: 20px;
  }

  .post-title {
    font-size: 20px;
  }

  .detail-img {
    width: 100px;
    height: 100px;
  }

  .comment-input-wrapper {
    flex-direction: column;
  }

  .reply-list {
    padding-left: 10px;
  }

  .post-actions {
    gap: 20px;
    flex-wrap: wrap;
  }

  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  /* 移动端返回按钮调整 */
  .top-nav {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .back-btn {
    margin-left: 0;
    padding: 6px 12px;
  }
}
</style>