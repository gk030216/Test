<template>
  <div class="favorites-page">
    <h2 class="page-title">我的收藏</h2>

    <div class="favorites-list" v-loading="loading">
      <div class="favorite-item" v-for="post in favoritesList" :key="post.id" @click="goToDetail(post.id)">
        <div class="post-header">
          <div class="user-info">
            <el-avatar :size="36" :src="post.userAvatar" class="user-avatar">
              {{ post.userName ? post.userName.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
            <div class="user-detail">
              <span class="user-name">{{ post.userName || '匿名用户' }}</span>
              <span class="post-time">{{ formatDate(post.createTime) }}</span>
            </div>
          </div>
          <div class="post-badges">
            <el-tag v-if="post.isTop === 1" size="mini" type="danger" effect="dark">置顶</el-tag>
            <el-tag v-if="post.isEssence === 1" size="mini" type="warning" effect="dark">精华</el-tag>
          </div>
        </div>

        <h3 class="post-title">{{ post.title }}</h3>
        <p class="post-content">{{ truncateText(post.content, 100) }}</p>

        <div class="post-footer">
          <div class="action-item">
            <i class="el-icon-star-on"></i>
            <span>{{ post.likeCount || 0 }}</span>
          </div>
          <div class="action-item">
            <i class="el-icon-chat-dot-round"></i>
            <span>{{ post.commentCount || 0 }}</span>
          </div>
          <div class="action-item" @click.stop="removeFavorite(post)">
            <i class="el-icon-delete"></i>
            <span>取消收藏</span>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && favoritesList.length === 0">
        <i class="el-icon-star-on"></i>
        <p>暂无收藏</p>
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
import { getFavorites, toggleFavorite } from '@/api/community';

export default {
  name: 'Favorites',
  data() {
    return {
      loading: false,
      favoritesList: [],
      total: 0,
      page: 1,
      pageSize: 10
    };
  },
  created() {
    this.loadFavorites();
  },
  methods: {
    async loadFavorites() {
      this.loading = true;
      try {
        const res = await getFavorites({ page: this.page, pageSize: this.pageSize });
        if (res.code === 200) {
          this.favoritesList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async removeFavorite(post) {
      try {
        const res = await toggleFavorite(post.id);
        if (res.code === 200) {
          this.$message.success('已取消收藏');
          this.loadFavorites();
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },
    goToDetail(id) {
      this.$router.push(`/community/post/${id}`);
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },
    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    },
    handlePageChange(page) {
      this.page = page;
      this.loadFavorites();
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

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.favorite-item {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.favorite-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.post-title {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.post-content {
  color: #666;
  line-height: 1.5;
  margin-bottom: 15px;
}

.post-footer {
  display: flex;
  gap: 20px;
  padding-top: 10px;
  border-top: 1px solid #e0e0e0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 13px;
}

.action-item:hover {
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