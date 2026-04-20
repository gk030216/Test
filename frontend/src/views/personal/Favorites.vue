<template>
  <div class="favorites-page">
    <h2 class="page-title">我的收藏</h2>

    <!-- 筛选栏：收藏类型切换 + 搜索框 同一行 -->
    <div class="filters-bar">
      <div class="favorites-tabs">
        <div
            class="tab-item"
            :class="{ active: favoriteType === 'all' }"
            @click="switchFavoriteType('all')"
        >
          <i class="el-icon-menu"></i> 全部收藏
        </div>
        <div
            class="tab-item"
            :class="{ active: favoriteType === 'service' }"
            @click="switchFavoriteType('service')"
        >
          <i class="el-icon-service"></i> 服务收藏
        </div>
        <div
            class="tab-item"
            :class="{ active: favoriteType === 'product' }"
            @click="switchFavoriteType('product')"
        >
          <i class="el-icon-goods"></i> 商品收藏
        </div>
        <div
            class="tab-item"
            :class="{ active: favoriteType === 'post' }"
            @click="switchFavoriteType('post')"
        >
          <i class="el-icon-document"></i> 帖子收藏
        </div>
      </div>

      <div class="search-wrapper">
        <i class="el-icon-search search-icon"></i>
        <el-input
            v-model="searchKeyword"
            placeholder="搜索收藏内容"
            size="medium"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
            class="search-input"
        ></el-input>
      </div>
    </div>

    <!-- 全部收藏列表 -->
    <div class="favorites-list" v-loading="allLoading" v-if="favoriteType === 'all'">
      <div class="all-grid">
        <div class="all-card" v-for="item in filteredAllList" :key="item.id" @click="goToDetail(item)">
          <div class="card-image" v-if="item.image">
            <img :src="item.image" :alt="item.name || item.title">
            <div class="favorite-badge" @click.stop="removeFavorite(item)">
              <i class="el-icon-star-on"></i>
            </div>
          </div>
          <div class="card-image placeholder" v-else>
            <i :class="item.type === 'service' ? 'el-icon-service' : (item.type === 'product' ? 'el-icon-goods' : 'el-icon-document')"></i>
            <div class="favorite-badge" @click.stop="removeFavorite(item)">
              <i class="el-icon-star-on"></i>
            </div>
          </div>
          <div class="card-info">
            <h4 class="card-title">{{ item.name || item.title }}</h4>
            <p class="card-desc">{{ truncateText(item.description || item.content, 40) }}</p>
            <div class="card-footer">
              <span class="card-price" v-if="item.price">¥{{ item.price }}</span>
              <span class="card-type" :class="item.type">
                {{ item.type === 'service' ? '服务' : (item.type === 'product' ? '商品' : '帖子') }}
              </span>
            </div>
            <div class="card-actions">
              <el-button type="primary" size="small" plain @click.stop="goToDetail(item)" class="view-btn">
                查看详情
              </el-button>
              <el-button type="danger" size="small" plain @click.stop="removeFavorite(item)" class="unfavorite-btn">
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!allLoading && filteredAllList.length === 0">
        <i class="el-icon-star-off"></i>
        <p>{{ searchKeyword ? '没有找到相关收藏' : '暂无收藏' }}</p>
        <el-button type="primary" size="small" @click="$router.push('/')">去逛逛</el-button>
      </div>

      <div class="pagination" v-if="allTotal > allPageSize">
        <el-pagination
            @current-change="handleAllPageChange"
            :current-page="allPage"
            :page-size="allPageSize"
            layout="prev, pager, next"
            :total="allTotal"
            background
        ></el-pagination>
      </div>
    </div>

    <!-- 帖子收藏列表 -->
    <div class="favorites-list" v-loading="loading" v-if="favoriteType === 'post'">
      <div class="post-grid">
        <div class="post-card" v-for="post in filteredPostList" :key="post.id" @click="goToPostDetail(post.id)">
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

      <div class="empty-state" v-if="!loading && filteredPostList.length === 0">
        <i class="el-icon-document"></i>
        <p>{{ searchKeyword ? '没有找到相关帖子' : '暂无收藏的帖子' }}</p>
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
        <div class="product-card" v-for="product in filteredProductList" :key="product.id" @click="goToProductDetail(product.id)">
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

      <div class="empty-state" v-if="!productLoading && filteredProductList.length === 0">
        <i class="el-icon-goods"></i>
        <p>{{ searchKeyword ? '没有找到相关商品' : '暂无收藏的商品' }}</p>
        <el-button type="primary" size="small" @click="$router.push('/shop')">去逛逛</el-button>
      </div>

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
        <div class="service-card" v-for="service in filteredServiceList" :key="service.id" @click="goToServiceDetail(service.serviceId)">
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

      <div class="empty-state" v-if="!serviceLoading && filteredServiceList.length === 0">
        <i class="el-icon-service"></i>
        <p>{{ searchKeyword ? '没有找到相关服务' : '暂无收藏的服务' }}</p>
        <el-button type="primary" size="small" @click="$router.push('/services')">去逛逛</el-button>
      </div>

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
      allRawList: [],
      // 搜索关键词
      searchKeyword: '',

      // 全部收藏
      allLoading: false,
      allList: [],
      allTotal: 0,
      allPage: 1,
      allPageSize: 9,

      // 帖子收藏
      loading: false,
      postList: [],
      postTotal: 0,
      postPage: 1,
      postPageSize: 9,

      // 商品收藏
      productLoading: false,
      productList: [],
      productTotal: 0,
      productPage: 1,
      productPageSize: 9,

      // 服务收藏
      serviceLoading: false,
      serviceList: [],
      serviceTotal: 0,
      servicePage: 1,
      servicePageSize: 9,

      favoriteType: 'all'
    };
  },
  computed: {
    // 全部收藏过滤
    filteredAllList() {
      // 从缓存的全量数据中过滤
      let filtered = this.allRawList;

      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        filtered = this.allRawList.filter(item => {
          const name = (item.name || item.title || '').toLowerCase();
          const desc = (item.description || item.content || '').toLowerCase();
          return name.includes(keyword) || desc.includes(keyword);
        });
      }

      // 更新总数（用于分页）
      this.allTotal = filtered.length;

      // 前端分页：根据当前页码截取
      const start = (this.allPage - 1) * this.allPageSize;
      const end = start + this.allPageSize;
      return filtered.slice(start, end);
    },
    // 帖子收藏过滤
    filteredPostList() {
      if (!this.searchKeyword) return this.postList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.postList.filter(post => {
        return (post.title || '').toLowerCase().includes(keyword) ||
            (post.content || '').toLowerCase().includes(keyword);
      });
    },
    // 商品收藏过滤
    filteredProductList() {
      if (!this.searchKeyword) return this.productList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.productList.filter(product => {
        return (product.name || '').toLowerCase().includes(keyword) ||
            (product.description || '').toLowerCase().includes(keyword);
      });
    },
    // 服务收藏过滤
    filteredServiceList() {
      if (!this.searchKeyword) return this.serviceList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.serviceList.filter(service => {
        return (service.serviceName || '').toLowerCase().includes(keyword) ||
            (service.serviceDescription || '').toLowerCase().includes(keyword);
      });
    }
  },
  created() {
    this.loadAllFavorites();
    this.loadPostFavorites();
    this.loadProductFavorites();
    this.loadServiceFavorites();
  },
  methods: {
    switchFavoriteType(type) {
      this.favoriteType = type;
      this.searchKeyword = '';
      this.allPage = 1;
      this.allRawList = [];  // 清空缓存，重新加载
      if (type === 'all') {
        this.loadAllFavorites();
      }
    },

    handleSearch() {
      // 搜索逻辑由计算属性自动处理
      if (this.favoriteType === 'post') {
        this.postPage = 1;
        this.loadPostFavorites();
      } else if (this.favoriteType === 'product') {
        this.productPage = 1;
        this.loadProductFavorites();
      } else if (this.favoriteType === 'service') {
        this.servicePage = 1;
        this.loadServiceFavorites();
      } else {
        this.allPage = 1;
        this.loadAllFavorites();
      }
    },

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

    // ========== 全部收藏 ==========
    async loadAllFavorites() {
      this.allLoading = true;
      try {
        // 只在第一页或数据为空时获取
        if (this.allPage === 1 || this.allRawList.length === 0) {
          const [postRes, productRes, serviceRes] = await Promise.all([
            getFavorites({ page: 1, pageSize: 100 }).catch(() => ({ code: 200, data: { list: [] } })),
            getProductFavorites({ page: 1, pageSize: 100 }).catch(() => ({ code: 200, data: { list: [] } })),
            getServiceFavorites({ page: 1, pageSize: 100 }).catch(() => ({ code: 200, data: { list: [] } }))
          ]);

          // 处理数据...
          const postList = (postRes.code === 200 ? (postRes.data.list || []) : []).map(item => ({
            ...item,
            type: 'post',
            id: item.id,
            name: item.title,
            image: item.images ? this.getFirstImage(item.images) : '',
            price: null,
            description: item.content,
            createTime: item.createTime
          }));

          const productList = (productRes.code === 200 ? (productRes.data.list || []) : []).map(item => ({
            ...item,
            type: 'product',
            id: item.id,
            name: item.name,
            image: item.image,
            price: item.price,
            description: item.description,
            createTime: item.createTime
          }));

          const serviceList = (serviceRes.code === 200 ? (serviceRes.data.list || []) : []).map(item => ({
            ...item,
            type: 'service',
            id: item.serviceId,
            name: item.serviceName,
            image: item.serviceImage,
            price: item.servicePrice,
            description: item.serviceDescription,
            createTime: item.createTime
          }));

          const allList = [...postList, ...productList, ...serviceList];
          allList.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));

          this.allRawList = allList;
        }

        // ✅ 移除这里的 allList 赋值，让计算属性处理
        // 触发计算属性重新计算
        this.$forceUpdate();

      } catch (error) {
        console.error('加载全部收藏失败', error);
        this.allRawList = [];
        this.allTotal = 0;
      } finally {
        this.allLoading = false;
      }
    },
    async removeFavorite(item) {
      if (item.type === 'post') {
        await this.removePostFavorite(item);
      } else if (item.type === 'product') {
        await this.removeProductFavorite(item);
      } else if (item.type === 'service') {
        await this.removeServiceFavorite({ serviceId: item.id });
      }
    },

    goToDetail(item) {
      if (item.type === 'post') {
        this.goToPostDetail(item.id);
      } else if (item.type === 'product') {
        this.goToProductDetail(item.id);
      } else if (item.type === 'service') {
        this.goToServiceDetail(item.id);
      }
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
          this.loadAllFavorites();
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
          this.loadAllFavorites();
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
          this.loadAllFavorites();
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
    handleAllPageChange(page) {
      this.allPage = page;
      // 不需要重新请求，计算属性会自动重新计算并截取
      // 触发视图更新
      this.$forceUpdate();
    },

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
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

/* 筛选栏 - 切换按钮和搜索框在同一行 */
.filters-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

/* 切换标签样式 */
.favorites-tabs {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
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

/* 搜索栏样式 */
.search-wrapper {
  position: relative;
  width: 200px;
  flex-shrink: 0;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #909399;
  font-size: 16px;
  z-index: 1;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========== 全部收藏网格样式 ========== */
.all-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.all-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
  cursor: pointer;
}

.all-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-image {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-image.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-image.placeholder i {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.7);
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

.card-info {
  padding: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
}

.card-type {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.card-type.post {
  background: #ecf5ff;
  color: #409EFF;
}

.card-type.product {
  background: #fdf6ec;
  color: #E6A23C;
}

.card-type.service {
  background: #f0f9eb;
  color: #67C23A;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-actions .el-button {
  flex: 1;
  padding: 6px 0;
  font-size: 12px;
  border-radius: 6px;
}

/* ========== 帖子收藏样式 ========== */
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
  -webkit-line-clamp: 1;
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

.post-actions {
  display: flex;
  gap: 8px;
}

.post-actions .el-button {
  flex: 1;
  padding: 6px 0;
  font-size: 12px;
  border-radius: 6px;
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
  -webkit-line-clamp: 1;
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
  border-radius: 6px;
}

.view-btn,
.view-post-btn,
.view-product-btn,
.view-service-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.view-btn:hover,
.view-post-btn:hover,
.view-product-btn:hover,
.view-service-btn:hover {
  color: white;
  background: #409EFF;
  border-color: #409EFF;
}

.unfavorite-btn,
.unfavorite-post-btn,
.unfavorite-product-btn,
.unfavorite-service-btn {
  color: #f56c6c;
  border-color: #fde2e2;
  background: #fef0f0;
}

.unfavorite-btn:hover,
.unfavorite-post-btn:hover,
.unfavorite-product-btn:hover,
.unfavorite-service-btn:hover {
  color: white;
  background: #f56c6c;
  border-color: #f56c6c;
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
  .all-grid,
  .product-grid,
  .service-grid,
  .post-grid {
    grid-template-columns: 1fr;
  }

  .filters-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-wrapper {
    width: 100%;
  }

  .favorites-tabs {
    justify-content: center;
  }

  .post-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>