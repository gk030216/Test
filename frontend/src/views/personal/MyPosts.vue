<template>
  <div class="my-posts-page">
    <h2 class="page-title">我的帖子</h2>

    <div class="posts-list" v-loading="loading">
      <div class="post-item" v-for="post in postsList" :key="post.id" @click="goToDetail(post.id)">
        <div class="post-header">
          <div class="post-info">
            <span class="post-time">{{ formatDate(post.createTime) }}</span>
            <div class="post-badges">
              <el-tag v-if="post.isTop === 1" size="mini" type="danger" effect="dark">置顶</el-tag>
              <el-tag v-if="post.isEssence === 1" size="mini" type="warning" effect="dark">精华</el-tag>
              <el-tag v-if="post.status === 0" size="mini" type="info">已删除</el-tag>
            </div>
          </div>
          <div class="post-actions" @click.stop>
            <el-button type="text" size="small" @click="deletePost(post)" v-if="post.status === 1">
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </div>
        </div>

        <h3 class="post-title">{{ post.title }}</h3>
        <p class="post-content">{{ truncateText(post.content, 120) }}</p>

        <div class="post-footer">
          <div class="action-item">
            <i class="el-icon-star-on"></i>
            <span>{{ post.likeCount || 0 }}</span>
          </div>
          <div class="action-item">
            <i class="el-icon-chat-dot-round"></i>
            <span>{{ post.commentCount || 0 }}</span>
          </div>
          <div class="action-item">
            <i class="el-icon-view"></i>
            <span>{{ post.viewCount || 0 }}</span>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && postsList.length === 0">
        <i class="el-icon-document"></i>
        <p>暂无帖子</p>
        <el-button type="primary" size="small" @click="$router.push('/community')">去发布</el-button>
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
import { getUserPosts, deletePost } from '@/api/community';

export default {
  name: 'MyPosts',
  data() {
    return {
      loading: false,
      postsList: [],
      total: 0,
      page: 1,
      pageSize: 10
    };
  },
  created() {
    this.loadPosts();
  },
  methods: {
    async loadPosts() {
      this.loading = true;
      try {
        const res = await getUserPosts({ page: this.page, pageSize: this.pageSize });
        if (res.code === 200) {
          this.postsList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async deletePost(post) {
      this.$confirm(`确定要删除帖子 "${post.title}" 吗？`, '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deletePost(post.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadPosts();
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
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
      this.loadPosts();
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

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-item {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.post-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.post-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.post-time {
  font-size: 13px;
  color: #999;
}

.post-badges {
  display: flex;
  gap: 8px;
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