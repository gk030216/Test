<template>
  <div class="post-detail-container">
    <Navbar />

    <div class="post-detail-content">
      <div class="container">
        <!-- 面包屑导航 -->
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/community' }">宠物社区</el-breadcrumb-item>
          <el-breadcrumb-item>{{ post.title }}</el-breadcrumb-item>
        </el-breadcrumb>

        <!-- 帖子内容 -->
        <div class="post-card" v-loading="loading">
          <div class="post-header">
            <div class="user-info">
              <!-- 修复：使用数据库头像 -->
              <el-avatar :size="50" :src="post.userAvatar" class="user-avatar" :style="post.userAvatar ? {} : { background: getAvatarColor(post.userName) }">
                {{ !post.userAvatar ? getUserInitial(post.userName) : '' }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ post.userName || '匿名用户' }}</span>
                <span class="post-time">{{ formatDate(post.createTime) }}</span>
              </div>
            </div>
            <div class="post-badges">
              <el-tag v-if="post.isTop === 1" size="small" type="danger" effect="dark">置顶</el-tag>
              <el-tag v-if="post.isEssence === 1" size="small" type="warning" effect="dark">精华</el-tag>
            </div>
          </div>

          <h1 class="post-title">{{ post.title }}</h1>

          <div class="post-content" v-html="formatContent(post.content)"></div>

          <!-- 修复：图片显示 -->
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
                <span>加载失败</span>
              </div>
            </el-image>
          </div>

          <div class="post-actions">
            <div class="action-item" @click="handleLike">
              <i :class="['el-icon-star-on', { liked: post.isLiked }]"></i>
              <span>{{ post.likeCount || 0 }} 点赞</span>
            </div>
            <div class="action-item" @click="handleFavorite">
              <i :class="['el-icon-collection', { favorited: post.isFavorited }]"></i>
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

          <!-- 评论输入框 -->
          <div class="comment-input-wrapper">
            <el-avatar :size="40" :src="currentUserAvatar" class="comment-avatar" :style="currentUserAvatar ? {} : { background: getAvatarColor(currentUserName) }">
              {{ !currentUserAvatar ? getUserInitial(currentUserName) : '' }}
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

          <!-- 评论列表 -->
          <div class="comment-list" v-loading="commentListLoading">
            <div class="comment-item" v-for="comment in commentList" :key="comment.id">
              <el-avatar :size="36" :src="comment.userAvatar" class="comment-avatar" :style="comment.userAvatar ? {} : { background: getAvatarColor(comment.userName) }">
                {{ !comment.userAvatar ? getUserInitial(comment.userName) : '' }}
              </el-avatar>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="user-name">{{ comment.userName || '匿名用户' }}</span>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-footer">
                  <span class="reply-btn" @click="showReplyInput(comment)">回复</span>
                  <span v-if="comment.userId === currentUserId" class="delete-btn" @click="deleteComment(comment.id)">删除</span>
                </div>

                <!-- 回复输入框 -->
                <div class="reply-input-wrapper" v-if="replyTarget && replyTarget.id === comment.id">
                  <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="`回复 ${comment.userName}：`"
                      size="small"
                  ></el-input>
                  <div class="reply-actions">
                    <el-button size="mini" @click="cancelReply">取消</el-button>
                    <el-button type="primary" size="mini" @click="submitReply(comment)" :loading="replyLoading">
                      回复
                    </el-button>
                  </div>
                </div>

                <!-- 子回复列表 -->
                <div class="reply-list" v-if="comment.replies && comment.replies.length">
                  <div class="reply-item" v-for="reply in comment.replies" :key="reply.id">
                    <div class="reply-header">
                      <span class="reply-user">{{ reply.userName || '匿名用户' }}</span>
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

            <!-- 空状态 -->
            <div class="empty-comment" v-if="!commentListLoading && commentList.length === 0">
              <i class="el-icon-chat-dot-round"></i>
              <p>暂无评论，快来抢沙发！</p>
            </div>

            <!-- 分页 -->
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
import { getPostDetail, toggleLike, toggleFavorite, getComments, addComment, deleteComment } from '@/api/community';

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
      return userInfo ? (JSON.parse(userInfo).nickname || JSON.parse(userInfo).username) : '用户';
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
    // 获取图片列表
    getImageList(images) {
      if (!images) return [];
      if (typeof images === 'string') {
        return images.split(',').filter(img => img && img.trim().length > 0);
      }
      return [];
    },

    getAvatarColor(name) {
      if (!name) return 'linear-gradient(135deg, #667eea, #764ba2)';
      const colors = [
        'linear-gradient(135deg, #667eea, #764ba2)',
        'linear-gradient(135deg, #f093fb, #f5576c)',
        'linear-gradient(135deg, #4facfe, #00f2fe)',
        'linear-gradient(135deg, #43e97b, #38f9d7)',
        'linear-gradient(135deg, #fa709a, #fee140)',
        'linear-gradient(135deg, #a18cd1, #fbc2eb)'
      ];
      let index = 0;
      for (let i = 0; i < name.length; i++) {
        index += name.charCodeAt(i);
      }
      return colors[index % colors.length];
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
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('点赞操作失败', error);
        this.$message.error('操作失败');
      }
    },

    async handleFavorite() {
      try {
        const res = await toggleFavorite(this.postId);
        if (res.code === 200) {
          this.post.isFavorited = res.data.isFavorited;
          this.$message.success(res.data.isFavorited ? '收藏成功' : '取消收藏');
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('收藏操作失败', error);
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
        console.error('评论失败', error);
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
          replyToUserName: parentComment.userName
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
        console.error('回复失败', error);
        this.$message.error('回复失败');
      } finally {
        this.replyLoading = false;
      }
    },

    async deleteComment(id) {
      this.$confirm('确定要删除这条评论吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deleteComment(id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadComments();
            this.loadPost();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('删除评论失败', error);
          this.$message.error('删除失败');
        }
      }).catch(() => {});
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
/* 保持原有样式，添加图片加载失败样式 */
.post-detail-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
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

.breadcrumb {
  margin-bottom: 20px;
}

.post-card {
  background: white;
  border-radius: 24px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
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
  flex-shrink: 0;
}

.user-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.post-time {
  font-size: 13px;
  color: #999;
  margin-left: 10px;
}

.post-title {
  font-size: 26px;
  margin-bottom: 20px;
  color: #333;
}

.post-content {
  color: #555;
  line-height: 1.8;
  margin-bottom: 20px;
  font-size: 16px;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.detail-img {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  object-fit: cover;
  cursor: pointer;
  background: #f5f5f5;
}

.post-actions {
  display: flex;
  gap: 40px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}

.action-item:hover {
  color: #667eea;
}

.action-item i.liked {
  color: #ff6b6b;
}

.action-item i.favorited {
  color: #ff9900;
}

.comment-section {
  background: white;
  border-radius: 24px;
  padding: 30px;
}

.comment-title {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
}

.comment-title i {
  margin-right: 8px;
  color: #667eea;
}

.comment-input-wrapper {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-avatar {
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
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-header .user-name {
  font-weight: 600;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  color: #555;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-footer {
  display: flex;
  gap: 15px;
}

.reply-btn, .delete-btn {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.reply-btn:hover {
  color: #667eea;
}

.delete-btn:hover {
  color: #f56c6c;
}

.reply-input-wrapper {
  margin-top: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 12px;
}

.reply-actions {
  margin-top: 8px;
  text-align: right;
}

.reply-list {
  margin-top: 12px;
  padding-left: 20px;
  border-left: 2px solid #e0e0e0;
}

.reply-item {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
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
  color: #333;
}

.reply-to {
  color: #667eea;
}

.reply-time {
  font-size: 11px;
  color: #999;
}

.reply-content {
  color: #666;
  font-size: 13px;
}

.empty-comment {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-comment i {
  font-size: 48px;
  margin-bottom: 16px;
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
  color: #999;
  font-size: 12px;
}

.image-slot i {
  font-size: 24px;
  margin-bottom: 4px;
}

@media (max-width: 768px) {
  .post-card {
    padding: 20px;
  }

  .post-title {
    font-size: 20px;
  }

  .detail-img {
    width: 120px;
    height: 120px;
  }

  .comment-input-wrapper {
    flex-direction: column;
  }

  .reply-list {
    padding-left: 10px;
  }

  .post-actions {
    gap: 20px;
  }
}
</style>