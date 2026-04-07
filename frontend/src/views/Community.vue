<template>
  <div class="community-container">
    <Navbar />

    <div class="community-content">
      <div class="container">
        <!-- 页面头部 -->
        <div class="page-header">
          <h2 class="page-title">🐾 宠物社区</h2>
          <p class="page-desc">分享与宠物的温馨瞬间，交流养宠经验</p>
          <el-button type="primary" size="medium" @click="showPostDialog = true" class="publish-btn">
            <i class="el-icon-edit"></i> 发布新帖
          </el-button>
        </div>

        <!-- 分类导航 - 添加"其他"分类 -->
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
            >
              <i slot="prefix" class="el-icon-search"></i>
            </el-input>
            <el-button size="small" type="primary" @click="handleSearch" style="margin-left: 8px;">搜索</el-button>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div class="post-list" v-loading="loading">
          <div class="post-item" v-for="post in postList" :key="post.id" @click="goToDetail(post.id)">
            <div class="post-header">
              <div class="user-info">
                <el-avatar :size="40" :src="post.userAvatar" class="user-avatar" :style="post.userAvatar ? {} : { background: getAvatarColor(post.userName) }">
                  {{ !post.userAvatar ? getUserInitial(post.userName) : '' }}
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
                    <span>加载失败</span>
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
                <i :class="['el-icon-collection', { favorited: post.isFavorited }]"></i>
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
            <i class="el-icon-chat-dot-round"></i>
            <p>暂无帖子，快来发布第一条吧！</p>
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
          <el-select v-model="postForm.category" placeholder="请选择分类">
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
      // 添加"其他"分类到 categories 数组
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
          this.postList = res.data.list || [];
          this.total = res.data.total || 0;
        } else {
          this.$message.error(res.message || '加载失败');
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
      if (name) {
        for (let i = 0; i < name.length; i++) {
          index += name.charCodeAt(i);
        }
      }
      return colors[index % colors.length];
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
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('点赞操作失败', error);
        this.$message.error('操作失败');
      }
    },

    async handleFavorite(post) {
      try {
        const res = await toggleFavorite(post.id);
        if (res.code === 200) {
          post.isFavorited = res.data.isFavorited;
          this.$message.success(res.data.isFavorited ? '收藏成功' : '取消收藏');
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
            uid: Date.now() + Math.random(),
            name: file.file.name,
            url: res.data.url
          });
          this.$message.success('上传成功');
        } else {
          this.$message.error(res.message || '上传失败');
        }
      } catch (error) {
        console.error('上传失败', error);
        this.$message.error('上传失败');
      }
    },

    handlePreview(file) {
      this.previewImage = file.url;
      this.previewVisible = true;
    },

    handleRemove(file, fileList) {
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
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('发布失败', error);
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
      if (this.$refs.postForm) {
        this.$refs.postForm.clearValidate();
      }
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
/* 样式保持不变 */
.community-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
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

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 32px;
  color: #333;
  margin-bottom: 10px;
}

.page-desc {
  color: #999;
  margin-bottom: 20px;
}

.publish-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  padding: 10px 24px;
  font-size: 16px;
}

.category-nav {
  display: flex;
  gap: 10px;
  background: white;
  padding: 15px 20px;
  border-radius: 50px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  flex-wrap: wrap;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.category-item:hover {
  background: #f0f0f0;
}

.category-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 12px rgba(102,126,234,0.3);
}

.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sort-left {
  display: flex;
  gap: 20px;
}

.sort-item {
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}

.sort-item:hover, .sort-item.active {
  color: #667eea;
}

.sort-right {
  display: flex;
  align-items: center;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-item {
  background: white;
  border-radius: 20px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.post-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
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
  gap: 12px;
}

.user-avatar {
  flex-shrink: 0;
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

.post-badges {
  display: flex;
  gap: 8px;
}

.post-title {
  font-size: 18px;
  margin-bottom: 12px;
  color: #333;
}

.post-text {
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
}

.post-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.post-img {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  object-fit: cover;
  cursor: pointer;
  background: #f5f5f5;
}

.more-images {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.post-footer {
  display: flex;
  gap: 30px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  cursor: pointer;
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

.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 20px;
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

.post-dialog ::v-deep .el-dialog {
  border-radius: 20px;
}

.post-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px;
  margin: 0;
}

.post-dialog ::v-deep .el-dialog__title {
  color: white;
}

.upload-tip {
  font-size: 12px;
  color: #999;
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
  color: #999;
  font-size: 12px;
}

.image-slot i {
  font-size: 24px;
  margin-bottom: 4px;
}

@media (max-width: 768px) {
  .category-nav {
    flex-wrap: wrap;
    border-radius: 20px;
  }

  .post-img {
    width: 100px;
    height: 100px;
  }

  .post-footer {
    gap: 20px;
  }

  .sort-bar {
    flex-direction: column;
    gap: 10px;
  }

  .sort-right {
    width: 100%;
  }

  .sort-right .el-input {
    flex: 1;
  }
}
</style>