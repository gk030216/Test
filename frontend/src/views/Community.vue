<template>
  <div class="community-container">
    <Navbar />

    <div class="community-content">
      <div class="container">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="header-content">
            <div class="header-left">
              <h2 class="page-title">宠物社区</h2>
              <p class="page-desc">分享养宠经验，交流萌宠日常</p>
            </div>
            <el-button type="primary" size="large" @click="showPostDialog = true" class="publish-btn">
              <i class="el-icon-edit"></i> 发布新帖
            </el-button>
          </div>
        </div>

        <!-- 分类导航 -->
        <div class="category-nav">
          <div
              v-for="cat in categories"
              :key="cat.value"
              :class="['category-item', { active: currentCategory === cat.value }]"
              @click="handleCategoryChange(cat.value)"
          >
            <i :class="cat.icon"></i>
            <span>{{ cat.label }}</span>
          </div>
        </div>

        <!-- 排序栏 -->
        <div class="sort-bar">
          <div class="sort-left">
            <span
                :class="['sort-item', { active: currentSort === 'latest' }]"
                @click="handleSortChange('latest')"
            >最新</span>
            <span
                :class="['sort-item', { active: currentSort === 'hot' }]"
                @click="handleSortChange('hot')"
            >最热</span>
          </div>
          <div class="sort-right">
            <el-input
                v-model="searchKeyword"
                placeholder="搜索帖子"
                size="small"
                clearable
                @keyup.enter="handleSearch"
                @clear="handleSearch"
                style="width: 200px"
                prefix-icon="el-icon-search"
            ></el-input>
            <el-button size="small" type="primary" @click="handleSearch" style="margin-left: 8px;">搜索</el-button>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div class="post-list" v-loading="loading">
          <div class="post-item" v-for="post in postList" :key="post.id" @click="goToDetail(post.id)">
            <div class="post-header">
              <div class="user-info">
                <el-avatar :size="40" :src="post.userAvatar" class="user-avatar">
                  {{ !post.userAvatar ? getUserInitial(post.userNickname || post.userName) : '' }}
                </el-avatar>
                <div class="user-detail">
                  <span class="user-name">{{ post.userNickname || post.userName || '匿名用户' }}</span>
                  <span class="post-time">{{ formatDate(post.createTime) }}</span>
                </div>
              </div>
              <div class="post-badges">
                <el-tag v-if="post.isTop === 1" size="mini" type="danger">置顶</el-tag>
                <el-tag v-if="post.isEssence === 1" size="mini" type="warning">精华</el-tag>
              </div>
            </div>

            <div class="post-content">
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-text">{{ truncateText(post.content, 150) }}</p>
              <div class="post-images" v-if="post.images && post.images.length > 0">
                <el-image
                    v-for="(img, idx) in getImageList(post.images).slice(0, 3)"
                    :key="idx"
                    :src="img"
                    fit="cover"
                    class="post-img"
                    :preview-src-list="getImageList(post.images)"
                    @click.stop
                >
                  <div slot="error" class="image-slot">
                    <i class="el-icon-picture-outline"></i>
                  </div>
                </el-image>
                <span v-if="getImageList(post.images).length > 3" class="more-images">
                  +{{ getImageList(post.images).length - 3 }}
                </span>
              </div>
            </div>

            <div class="post-footer">
              <div class="action-item" @click.stop="handleLike(post)">
                <i :class="['el-icon-star-on', { liked: post.isLiked }]"></i>
                <span>{{ post.likeCount || 0 }}</span>
              </div>
              <div class="action-item" @click.stop="goToDetail(post.id)">
                <i class="el-icon-chat-dot-round"></i>
                <span>{{ post.commentCount || 0 }}</span>
              </div>
              <div class="action-item" @click.stop="handleFavorite(post)">
                <i :class="['el-icon-star-off', { favorited: post.isFavorited }]"></i>
                <span>{{ post.isFavorited ? '已收藏' : '收藏' }}</span>
              </div>
              <div class="action-item">
                <i class="el-icon-view"></i>
                <span>{{ post.viewCount || 0 }}</span>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div class="empty-state" v-if="!loading && postList.length === 0">
            <i class="el-icon-document"></i>
            <p>暂无帖子</p>
            <el-button type="primary" size="small" @click="showPostDialog = true">发布第一条</el-button>
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
    </div>

    <!-- 发布帖子对话框 -->
    <el-dialog title="发布新帖" :visible.sync="showPostDialog" width="700px" center class="post-dialog">
      <el-form :model="postForm" :rules="postRules" ref="postForm" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入标题" maxlength="100" show-word-limit></el-input>
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="postForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="宠物日常" value="pet_daily"></el-option>
            <el-option label="健康分享" value="health"></el-option>
            <el-option label="美食分享" value="food"></el-option>
            <el-option label="领养信息" value="adopt"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
              v-model="postForm.content"
              type="textarea"
              :rows="6"
              placeholder="分享你的养宠经验、萌宠日常..."
              maxlength="5000"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="图片">
          <el-upload
              action="#"
              :http-request="uploadImage"
              list-type="picture-card"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :file-list="imageList"
              :limit="9"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
          <div class="upload-tip">支持 JPG、PNG 格式，最多9张，每张不超过2MB</div>
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPost" :loading="postLoading">发布</el-button>
      </span>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog :visible.sync="previewVisible" width="600px">
      <img :src="previewImage" style="width: 100%" alt="预览图片">
    </el-dialog>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getPostList, addPost, toggleLike, toggleFavorite } from '@/api/community';
import { uploadPostImage } from '@/api/upload';

export default {
  name: 'Community',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      postLoading: false,
      postList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      currentCategory: 'all',
      currentSort: 'latest',
      searchKeyword: '',
      categories: [
        { value: 'all', label: '全部', icon: 'el-icon-menu' },
        { value: 'pet_daily', label: '宠物日常', icon: 'el-icon-camera' },
        { value: 'health', label: '健康分享', icon: 'el-icon-first-aid-kit' },
        { value: 'food', label: '美食分享', icon: 'el-icon-food' },
        { value: 'adopt', label: '领养信息', icon: 'el-icon-heart' },
        { value: 'other', label: '其他', icon: 'el-icon-more' }
      ],
      showPostDialog: false,
      postForm: {
        title: '',
        content: '',
        category: 'pet_daily'
      },
      postRules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在2-100个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' }
        ]
      },
      imageList: [],
      imageUrls: [],
      previewVisible: false,
      previewImage: ''
    };
  },
  created() {
    this.loadPosts();
  },
  methods: {
    async loadPosts() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          category: this.currentCategory === 'all' ? null : this.currentCategory,
          keyword: this.searchKeyword || null,
          sort: this.currentSort
        };
        const res = await getPostList(params);
        if (res.code === 200) {
          this.postList = (res.data.list || []).map(post => ({
            ...post,
            isLiked: post.isLiked === true || post.isLiked === 1,
            isFavorited: post.isFavorited === true || post.isFavorited === 1
          }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    getImageList(images) {
      if (!images) return [];
      if (typeof images === 'string') {
        return images.split(',').filter(img => img && img.trim().length > 0);
      }
      return [];
    },

    handleCategoryChange(category) {
      this.currentCategory = category;
      this.page = 1;
      this.loadPosts();
    },

    handleSortChange(sort) {
      this.currentSort = sort;
      this.page = 1;
      this.loadPosts();
    },

    handleSearch() {
      this.page = 1;
      this.loadPosts();
    },

    handlePageChange(page) {
      this.page = page;
      this.loadPosts();
    },

    getUserInitial(name) {
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
    },

    async handleLike(post) {
      try {
        const res = await toggleLike(post.id);
        if (res.code === 200) {
          post.isLiked = res.data.isLiked;
          post.likeCount += post.isLiked ? 1 : -1;
          this.$message.success(res.data.isLiked ? '点赞成功' : '取消点赞');
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    async handleFavorite(post) {
      try {
        const res = await toggleFavorite(post.id);
        if (res.code === 200) {
          // ✅ 直接使用后端返回的状态
          const newStatus = res.data.isFavorited;
          post.isFavorited = newStatus;

          // ✅ 同时更新列表中的状态
          const targetIndex = this.postList.findIndex(p => p.id === post.id);
          if (targetIndex !== -1) {
            this.$set(this.postList[targetIndex], 'isFavorited', newStatus);
          }

          this.$message.success(newStatus ? '收藏成功' : '取消收藏');
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('收藏操作失败', error);
        this.$message.error('操作失败，请稍后重试');
      }
    },
    async uploadImage(file) {
      const formData = new FormData();
      formData.append('file', file.file);

      try {
        const res = await uploadPostImage(formData);
        if (res.code === 200) {
          this.imageUrls.push(res.data.url);
          this.imageList.push({
            uid: Date.now(),
            name: file.file.name,
            url: res.data.url
          });
          this.$message.success('上传成功');
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
    },

    handlePreview(file) {
      this.previewImage = file.url;
      this.previewVisible = true;
    },

    handleRemove(file) {
      const index = this.imageList.findIndex(f => f.uid === file.uid);
      if (index !== -1) {
        this.imageList.splice(index, 1);
        this.imageUrls.splice(index, 1);
      }
    },

    async submitPost() {
      this.$refs.postForm.validate(async (valid) => {
        if (!valid) return;

        this.postLoading = true;
        try {
          const data = {
            title: this.postForm.title,
            content: this.postForm.content,
            category: this.postForm.category,
            images: this.imageUrls.join(',')
          };
          const res = await addPost(data);
          if (res.code === 200) {
            this.$message.success('发布成功');
            this.showPostDialog = false;
            this.resetForm();
            this.loadPosts();
          }
        } catch (error) {
          this.$message.error('发布失败');
        } finally {
          this.postLoading = false;
        }
      });
    },

    resetForm() {
      this.postForm = {
        title: '',
        content: '',
        category: 'pet_daily'
      };
      this.imageList = [];
      this.imageUrls = [];
      this.$nextTick(() => {
        if (this.$refs.postForm) {
          this.$refs.postForm.clearValidate();
        }
      });
    },

    goToDetail(id) {
      this.$router.push(`/community/post/${id}`);
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
    },

    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    }
  }
};
</script>

<style scoped>
.community-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.community-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.publish-btn {
  background: #409EFF;
  border: none;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
}

.publish-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

/* 分类导航 */
.category-nav {
  display: flex;
  gap: 8px;
  background: white;
  padding: 12px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
  flex-wrap: wrap;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: #606266;
  font-size: 13px;
  background: #f5f7fa;
}

.category-item:hover {
  background: #ecf5ff;
  color: #409EFF;
}

.category-item.active {
  background: #409EFF;
  color: white;
}

/* 排序栏 */
.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sort-left {
  display: flex;
  gap: 24px;
}

.sort-item {
  cursor: pointer;
  color: #909399;
  font-size: 14px;
  transition: color 0.3s;
}

.sort-item:hover,
.sort-item.active {
  color: #409EFF;
}

.sort-right {
  display: flex;
  align-items: center;
}

.sort-right .el-input {
  width: 200px;
}

/* 帖子列表 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
}

.post-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #d0d0d0;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: #409EFF;
  color: white;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.post-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.post-badges {
  display: flex;
  gap: 6px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #2c3e50;
}

.post-text {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
  font-size: 14px;
}

.post-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.post-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  background: #f5f5f5;
  border: 1px solid #eef2f6;
}

.more-images {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
  border: 1px solid #eef2f6;
}

.post-footer {
  display: flex;
  gap: 24px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef2f6;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
  cursor: pointer;
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

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 12px;
  color: #909399;
  border: 1px solid #eef2f6;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 对话框 */
.post-dialog ::v-deep .el-dialog {
  border-radius: 16px;
}

.post-dialog ::v-deep .el-dialog__header {
  background: #409EFF;
  padding: 20px;
  margin: 0;
  border-radius: 16px 16px 0 0;
}

.post-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
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
  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .header-left {
    text-align: center;
  }

  .category-nav {
    border-radius: 8px;
    padding: 10px 16px;
  }

  .category-item {
    padding: 4px 12px;
    font-size: 12px;
  }

  .post-img {
    width: 80px;
    height: 80px;
  }

  .more-images {
    width: 80px;
    height: 80px;
    font-size: 12px;
  }

  .post-footer {
    gap: 16px;
    flex-wrap: wrap;
  }

  .sort-bar {
    flex-direction: column;
    gap: 10px;
  }

  .sort-right {
    width: 100%;
  }

  .sort-right .el-input {
    width: 100%;
  }

  .empty-state {
    padding: 40px;
  }
}

/* 对话框关闭按钮白色 */
.post-dialog ::v-deep .el-dialog__headerbtn {
  position: absolute;
  top: 20px;
  right: 20px;
}

.post-dialog ::v-deep .el-dialog__headerbtn .el-dialog__close {
  color: white !important;
  font-size: 18px;
  font-weight: bold;
}

.post-dialog ::v-deep .el-dialog__headerbtn .el-dialog__close:hover {
  color: #f0f0f0 !important;
}
</style>