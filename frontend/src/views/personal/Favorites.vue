<template>
  <div class="favorites-page">
    <h2 class="page-title">我的收藏</h2>

    <!-- 收藏类型切换 -->
    <div class="favorites-tabs">
      <div
          class="tab-item"
          :class="{ active: favoriteType === 'post' }"
          @click="favoriteType = 'post'"
      >
        <i class="el-icon-document"></i> 帖子收藏
      </div>
      <div
          class="tab-item"
          :class="{ active: favoriteType === 'product' }"
          @click="favoriteType = 'product'"
      >
        <i class="el-icon-goods"></i> 商品收藏
      </div>
      <div
          class="tab-item"
          :class="{ active: favoriteType === 'service' }"
          @click="favoriteType = 'service'"
      >
        <i class="el-icon-service"></i> 服务收藏
      </div>
    </div>

    <!-- 帖子收藏列表 - 网格布局 -->
    <div class="favorites-list" v-loading="loading" v-if="favoriteType === 'post'">
      <div class="post-grid">
        <div class="post-card" v-for="post in postList" :key="post.id" @click="goToPostDetail(post.id)">
          <div class="post-image" v-if="post.images && getFirstImage(post.images)">
            <img :src="getFirstImage(post.images)" :alt="post.title">
            <div class="favorite-badge" @click.stop="removePostFavorite(post)">
              <i class="el-icon-star-on"></i>
            </div>
          </div>
          <div class="post-image placeholder" v-else>
            <i class="el-icon-document"></i>
            <div class="favorite-badge" @click.stop="removePostFavorite(post)">
              <i class="el-icon-star-on"></i>
            </div>
          </div>
          <div class="post-info">
            <h4 class="post-title">{{ post.title }}</h4>
            <p class="post-desc">{{ truncateText(post.content, 40) }}</p>
            <div class="post-footer">
              <span><i class="el-icon-star-on"></i> {{ post.likeCount || 0 }}</span>
              <span><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
              <span><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
            </div>
            <div class="post-actions">
              <el-button type="primary" size="small" plain @click.stop="goToPostDetail(post.id)" class="view-post-btn">
                查看详情
              </el-button>
              <el-button type="danger" size="small" plain @click.stop="removePostFavorite(post)" class="unfavorite-post-btn">
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && postList.length === 0">
        <i class="el-icon-document"></i>
        <p>暂无收藏的帖子</p>
        <el-button type="primary" size="small" @click="$router.push('/community')">去逛逛</el-button>
      </div>

      <div class="pagination" v-if="postTotal > postPageSize">
        <el-pagination
            @current-change="handlePostPageChange"
            :current-page="postPage"
            :page-size="postPageSize"
            layout="prev, pager, next"
            :total="postTotal"
            background
        ></el-pagination>
      </div>
    </div>

    <!-- 商品收藏列表 -->
    <div class="favorites-list" v-loading="productLoading" v-if="favoriteType === 'product'">
      <div class="product-grid">
        <div class="product-card" v-for="product in productList" :key="product.id" @click="goToProductDetail(product.id)">
          <div class="product-image">
            <img :src="product.image" :alt="product.name">
            <div class="favorite-badge" @click.stop="removeProductFavorite(product)">
              <i class="el-icon-star-on"></i>
            </div>
          </div>
          <div class="product-info">
            <h4 class="product-name">{{ product.name }}</h4>
            <p class="product-desc">{{ truncateText(product.description, 40) }}</p>
            <div class="product-footer">
              <span class="product-price">¥{{ product.price }}</span>
              <span class="product-sales">已售 {{ product.sales || 0 }}</span>
            </div>
            <div class="product-actions">
              <el-button type="primary" size="small" plain @click.stop="goToProductDetail(product.id)" class="view-product-btn">
                查看详情
              </el-button>
              <el-button type="danger" size="small" plain @click.stop="removeProductFavorite(product)" class="unfavorite-product-btn">
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!productLoading && productList.length === 0">
        <i class="el-icon-goods"></i>
        <p>暂无收藏的商品</p>
        <el-button type="primary" size="small" @click="$router.push('/shop')">去逛逛</el-button>
      </div>

      <!-- 商品分页 -->
      <div class="pagination" v-if="productTotal > productPageSize">
        <el-pagination
            @current-change="handleProductPageChange"
            :current-page="productPage"
            :page-size="productPageSize"
            layout="prev, pager, next"
            :total="productTotal"
            background
        ></el-pagination>
      </div>
    </div>

    <!-- 服务收藏列表 -->
    <div class="favorites-list" v-loading="serviceLoading" v-if="favoriteType === 'service'">
      <div class="service-grid">
        <div class="service-card" v-for="service in serviceList" :key="service.id" @click="goToServiceDetail(service.serviceId)">
          <div class="service-image">
            <img :src="service.serviceImage" :alt="service.serviceName">
            <div class="favorite-badge" @click.stop="removeServiceFavorite(service)">
              <i class="el-icon-star-on"></i>
            </div>
          </div>
          <div class="service-info">
            <h4 class="service-name">{{ service.serviceName }}</h4>
            <p class="service-desc">{{ truncateText(service.serviceDescription, 40) }}</p>
            <div class="service-footer">
              <span class="service-price">¥{{ service.servicePrice }}</span>
              <span class="service-duration" v-if="service.serviceDuration">
                <i class="el-icon-time"></i> {{ service.serviceDuration }}分钟
              </span>
            </div>
            <div class="service-actions">
              <el-button type="primary" size="small" plain @click.stop="goToServiceDetail(service.serviceId)" class="view-service-btn">
                立即预约
              </el-button>
              <el-button type="danger" size="small" plain @click.stop="removeServiceFavorite(service)" class="unfavorite-service-btn">
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!serviceLoading && serviceList.length === 0">
        <i class="el-icon-service"></i>
        <p>暂无收藏的服务</p>
        <el-button type="primary" size="small" @click="$router.push('/services')">去逛逛</el-button>
      </div>

      <!-- 服务分页 -->
      <div class="pagination" v-if="serviceTotal > servicePageSize">
        <el-pagination
            @current-change="handleServicePageChange"
            :current-page="servicePage"
            :page-size="servicePageSize"
            layout="prev, pager, next"
            :total="serviceTotal"
            background
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import { getFavorites, toggleFavorite } from '@/api/community';
import { getProductFavorites, removeProductFavorite } from '@/api/product';
import { getServiceFavorites, removeServiceFavorite } from '@/api/service';

export default {
  name: 'Favorites',
  data() {
    return {
      // 帖子收藏
      loading: false,
      postList: [],
      postTotal: 0,
      postPage: 1,
      postPageSize: 10,

      // 商品收藏
      productLoading: false,
      productList: [],
      productTotal: 0,
      productPage: 1,
      productPageSize: 12,

      // 服务收藏
      serviceLoading: false,
      serviceList: [],
      serviceTotal: 0,
      servicePage: 1,
      servicePageSize: 12,

      favoriteType: 'post'
    };
  },
  created() {
    this.loadPostFavorites();
  },
  watch: {
    favoriteType(val) {
      if (val === 'product' && this.productList.length === 0) {
        this.loadProductFavorites();
      }
      if (val === 'service' && this.serviceList.length === 0) {
        this.loadServiceFavorites();
      }
    }
  },
  methods: {

    getFirstImage(images) {
      if (!images) return '';
      if (typeof images === 'string') {
        const arr = images.split(',');
        return arr[0] || '';
      }
      if (Array.isArray(images)) {
        return images[0] || '';
      }
      return '';
    },

    // ========== 帖子收藏 ==========
    async loadPostFavorites() {
      this.loading = true;
      try {
        const res = await getFavorites({ page: this.postPage, pageSize: this.postPageSize });
        if (res.code === 200) {
          this.postList = res.data.list || [];
          this.postTotal = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async removePostFavorite(post) {
      try {
        const res = await toggleFavorite(post.id);
        if (res.code === 200) {
          this.$message.success('已取消收藏');
          this.loadPostFavorites();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    // ========== 商品收藏 ==========
    async loadProductFavorites() {
      this.productLoading = true;
      try {
        const res = await getProductFavorites({ page: this.productPage, pageSize: this.productPageSize });
        if (res.code === 200) {
          this.productList = res.data.list || [];
          this.productTotal = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.productLoading = false;
      }
    },

    async removeProductFavorite(product) {
      try {
        const res = await removeProductFavorite(product.id);
        if (res.code === 200) {
          this.$message.success('已取消收藏');
          this.loadProductFavorites();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    // ========== 服务收藏 ==========
    async loadServiceFavorites() {
      this.serviceLoading = true;
      try {
        const res = await getServiceFavorites({ page: this.servicePage, pageSize: this.servicePageSize });
        if (res.code === 200) {
          this.serviceList = res.data.list || [];
          this.serviceTotal = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.serviceLoading = false;
      }
    },

    async removeServiceFavorite(service) {
      try {
        const res = await removeServiceFavorite(service.serviceId);
        if (res.code === 200) {
          this.$message.success('已取消收藏');
          this.loadServiceFavorites();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    // ========== 导航方法 ==========
    goToPostDetail(id) {
      this.$router.push(`/community/post/${id}`);
    },

    goToProductDetail(id) {
      this.$router.push(`/product/${id}`);
    },

    goToServiceDetail(id) {
      this.$router.push(`/service/${id}`);
    },

    // ========== 分页方法 ==========
    handlePostPageChange(page) {
      this.postPage = page;
      this.loadPostFavorites();
    },

    handleProductPageChange(page) {
      this.productPage = page;
      this.loadProductFavorites();
    },

    handleServicePageChange(page) {
      this.servicePage = page;
      this.loadServiceFavorites();
    },

    // ========== 工具方法 ==========
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
.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.favorites-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  border-radius: 30px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  background: #fff;
  border: 1px solid #e0e0e0;
  transition: all 0.3s;
}

.tab-item i {
  font-size: 16px;
}

.tab-item:hover {
  border-color: #667eea;
  color: #667eea;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: transparent;
  color: white;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========== 帖子收藏样式 ========== */
.favorite-item {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.favorite-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  flex-shrink: 0;
  color: white;
  font-weight: 500;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-badges {
  display: flex;
  gap: 6px;
}

.post-body {
  cursor: pointer;
  margin-bottom: 16px;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #2c3e50;
  transition: color 0.3s;
}

.post-body:hover .post-title {
  color: #667eea;
}

.post-content {
  color: #5a6874;
  line-height: 1.7;
  font-size: 14px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.footer-left {
  display: flex;
  gap: 24px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
  transition: color 0.3s;
  cursor: pointer;
}

.action-item i {
  font-size: 16px;
}

.action-item:hover {
  color: #667eea;
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

.unfavorite-btn {
  color: #f56c6c;
  border-color: #fde2e2;
  background: #fef0f0;
}

.unfavorite-btn:hover {
  color: white;
  background: #f56c6c;
  border-color: #f56c6c;
}

/* ========== 商品/服务网格样式 ========== */
.product-grid,
.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.product-card,
.service-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
  cursor: pointer;
}

.product-card:hover,
.service-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.product-image,
.service-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.product-image img,
.service-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img,
.service-card:hover .service-image img {
  transform: scale(1.05);
}

.favorite-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f56c6c;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
}

.favorite-badge:hover {
  background: #f56c6c;
  color: white;
}

.product-info,
.service-info {
  padding: 16px;
}

.product-name,
.service-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-desc,
.service-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-footer,
.service-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.product-price,
.service-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
}

.product-sales,
.service-duration {
  font-size: 12px;
  color: #999;
}

.product-actions,
.service-actions {
  display: flex;
  gap: 8px;
}

.product-actions .el-button,
.service-actions .el-button {
  flex: 1;
  padding: 6px 0;
  font-size: 12px;
}

.view-product-btn,
.view-service-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.unfavorite-product-btn,
.unfavorite-service-btn {
  color: #f56c6c;
  border-color: #fde2e2;
  background: #fef0f0;
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

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .product-grid,
  .service-grid {
    grid-template-columns: 1fr;
  }

  .post-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .footer-right {
    width: 100%;
    justify-content: flex-end;
  }

  .favorite-item {
    padding: 16px;
  }

  .footer-left {
    gap: 16px;
  }

  .user-info {
    flex-wrap: wrap;
  }

  .favorites-tabs {
    justify-content: center;
  }
}

/* 帖子网格样式 */
.post-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.post-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.post-image {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.post-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-image.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-image.placeholder i {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.7);
}

.post-info {
  padding: 16px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 12px;
  color: #999;
}

.post-footer span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-footer i {
  font-size: 13px;
}

.post-actions {
  display: flex;
  gap: 8px;
}

.post-actions .el-button {
  flex: 1;
  padding: 6px 0;
  font-size: 12px;
}

.view-post-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.unfavorite-post-btn {
  color: #f56c6c;
  border-color: #fde2e2;
  background: #fef0f0;
}

@media (max-width: 768px) {
  .post-grid {
    grid-template-columns: 1fr;
  }
}
</style>