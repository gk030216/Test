<template>
  <div class="product-detail">
    <Navbar />

    <div class="detail-content">
      <div class="container">
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/shop' }">宠物商城</el-breadcrumb-item>
          <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="product-info" v-loading="loading">
          <div class="product-gallery">
            <div class="main-image">
              <el-image :src="product.image" fit="cover" :preview-src-list="previewImages">
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </div>
            <div class="thumb-images" v-if="thumbImages.length">
              <div
                  v-for="(img, idx) in thumbImages"
                  :key="idx"
                  :class="['thumb-item', { active: currentImage === img }]"
                  @click="currentImage = img"
              >
                <el-image :src="img" fit="cover"></el-image>
              </div>
            </div>
          </div>

          <div class="product-details">
            <h1 class="product-name">{{ product.name }}</h1>
            <div class="product-price">
              <span class="price-label">价格：</span>
              <span class="current-price">¥{{ product.price }}</span>
              <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
            </div>
            <div class="product-sales">
              <span>销量：{{ product.sales || 0 }}件</span>
              <span>库存：{{ product.stock || 0 }}件</span>
              <span class="rating-info" v-if="product.avgRating">
                评分：<el-rate v-model="product.avgRating" disabled show-score text-color="#ff9900"></el-rate>
              </span>
            </div>
            <div class="product-description">
              <div class="desc-label">商品描述：</div>
              <div class="desc-content">{{ product.description }}</div>
            </div>
            <div class="product-quantity">
              <span>数量：</span>
              <el-input-number
                  v-model="quantity"
                  :min="1"
                  :max="product.stock || 999"
                  size="medium"
              ></el-input-number>
              <span class="stock-info">（库存 {{ product.stock || 0 }} 件）</span>
            </div>
            <div class="product-actions">
              <el-button type="primary" size="large" @click="handleBuyNow" class="buy-btn">
                立即购买
              </el-button>
              <el-button size="large" @click="handleAddToCart" class="cart-btn">
                <i class="el-icon-shopping-cart-2"></i> 加入购物车
              </el-button>
            </div>
          </div>
        </div>

        <!-- 商品详情Tab -->
        <div class="product-tabs">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="商品详情" name="detail">
              <div class="detail-content" v-html="product.detailHtml || product.description"></div>
            </el-tab-pane>
            <el-tab-pane label="规格参数" name="spec">
              <div class="spec-content">
                <div class="spec-item" v-for="(value, key) in product.specs" :key="key">
                  <span class="spec-label">{{ key }}：</span>
                  <span class="spec-value">{{ value }}</span>
                </div>
              </div>
            </el-tab-pane>
            <!-- 用户评价Tab -->
            <el-tab-pane label="用户评价" name="comment">
              <div class="comment-content">
                <!-- 评分统计 -->
                <div class="comment-summary" v-if="ratingStats">
                  <div class="rating-score">
                    <span class="score">{{ ratingStats.avg_rating }}</span>
                    <el-rate v-model="ratingStats.avg_rating" disabled show-score text-color="#ff9900"></el-rate>
                    <span class="total">{{ ratingStats.total_count }}条评价</span>
                  </div>
                  <div class="rating-bars">
                    <div class="rating-bar-item" v-for="i in [5,4,3,2,1]" :key="i">
                      <span class="star-label">{{ i }}星</span>
                      <div class="bar">
                        <div class="bar-fill" :style="{ width: getPercent(i) + '%' }"></div>
                      </div>
                      <span class="count">{{ ratingStats['star' + i] || 0 }}</span>
                    </div>
                  </div>
                </div>

                <!-- 评价列表 -->
                <div class="comment-list" v-loading="commentLoading">
                  <div class="comment-item" v-for="comment in commentList" :key="comment.id">
                    <div class="comment-header">
                      <el-avatar :size="40" :src="comment.userAvatar" class="comment-avatar">
                        {{ comment.userName ? comment.userName.charAt(0).toUpperCase() : 'U' }}
                      </el-avatar>
                      <div class="comment-info">
                        <div class="user-info">
                          <span class="user-name">{{ comment.userName }}</span>
                          <el-rate v-model="comment.rating" disabled show-score text-color="#ff9900"></el-rate>
                        </div>
                        <div class="comment-time">{{ formatDate(comment.createTime) }}</div>
                      </div>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                    <div class="comment-images" v-if="comment.images">
                      <el-image
                          v-for="(img, idx) in comment.images.split(',')"
                          :key="idx"
                          :src="img"
                          :preview-src-list="comment.images.split(',')"
                          fit="cover"
                          class="comment-img"
                      ></el-image>
                    </div>
                    <div class="comment-reply" v-if="comment.reply">
                      <div class="reply-header">
                        <i class="el-icon-chat-dot-round"></i>
                        <span>商家回复：</span>
                      </div>
                      <div class="reply-content">{{ comment.reply }}</div>
                      <div class="reply-time">{{ formatDate(comment.replyTime) }}</div>
                    </div>
                  </div>

                  <!-- 空状态 -->
                  <div class="empty-comment" v-if="!commentLoading && commentList.length === 0">
                    <i class="el-icon-chat-dot-round"></i>
                    <p>暂无评价，快来发表第一条评价吧！</p>
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
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 猜你喜欢 -->
        <div class="recommend-section" v-if="recommendProducts.length">
          <h3>猜你喜欢</h3>
          <div class="recommend-grid">
            <div
                v-for="item in recommendProducts"
                :key="item.id"
                class="recommend-card"
                @click="goToDetail(item.id)"
            >
              <img :src="item.image" :alt="item.name">
              <div class="recommend-info">
                <h4>{{ item.name }}</h4>
                <span class="price">¥{{ item.price }}</span>
              </div>
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
import { getProductById, getHotProducts } from '@/api/product';
import { getProductComments, getProductRatingStats } from '@/api/comment';
import { addToCart } from '@/api/cart';

export default {
  name: 'ProductDetail',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      commentLoading: false,
      product: {},
      quantity: 1,
      activeTab: 'detail',
      currentImage: '',
      recommendProducts: [],
      // 评价相关
      commentList: [],
      commentPage: 1,
      commentTotal: 0,
      ratingStats: null
    };
  },
  computed: {
    productId() {
      return this.$route.params.id;
    },
    previewImages() {
      const images = [this.product.image];
      if (this.product.images) {
        images.push(...this.product.images.split(','));
      }
      return images;
    },
    thumbImages() {
      const images = [];
      if (this.product.image) images.push(this.product.image);
      if (this.product.images) {
        images.push(...this.product.images.split(',').slice(0, 4));
      }
      return images;
    }
  },
  created() {
    this.loadProduct();
  },
  methods: {
    async loadProduct() {
      this.loading = true;
      try {
        const res = await getProductById(this.productId);
        if (res.code === 200) {
          this.product = res.data;
          this.currentImage = this.product.image;
          this.loadRecommend();
          this.loadComments();
          this.loadRatingStats();
        } else {
          this.$message.error('商品不存在');
          this.$router.push('/shop');
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadComments() {
      this.commentLoading = true;
      try {
        const res = await getProductComments(this.productId, this.commentPage);
        if (res.code === 200) {
          this.commentList = res.data.list;
          this.commentTotal = res.data.total;
        }
      } catch (error) {
        console.error('加载评价失败', error);
      } finally {
        this.commentLoading = false;
      }
    },

    async loadRatingStats() {
      try {
        const res = await getProductRatingStats(this.productId);
        if (res.code === 200) {
          this.ratingStats = res.data;
        }
      } catch (error) {
        console.error('加载评分统计失败', error);
      }
    },

    getPercent(star) {
      if (!this.ratingStats || this.ratingStats.total_count === 0) return 0;
      return (this.ratingStats['star' + star] / this.ratingStats.total_count) * 100;
    },

    handleCommentPageChange(page) {
      this.commentPage = page;
      this.loadComments();
    },

    async loadRecommend() {
      try {
        const res = await getHotProducts();
        if (res.code === 200) {
          this.recommendProducts = res.data.filter(p => p.id !== this.product.id).slice(0, 4);
        }
      } catch (error) {
        console.error('加载推荐失败', error);
      }
    },

    async handleAddToCart() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.$confirm('请先登录', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login');
        });
        return;
      }

      try {
        const res = await addToCart(this.product.id, this.quantity);
        if (res.code === 200) {
          this.$message.success('已加入购物车');
          this.$bus.$emit('cart-updated');
        }
      } catch (error) {
        this.$message.error(error.message || '添加失败');
      }
    },

    handleBuyNow() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.$confirm('请先登录', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login');
        });
        return;
      }

      const checkoutItem = {
        productId: this.product.id,
        productName: this.product.name,
        productImage: this.product.image,
        price: this.product.price,
        quantity: this.quantity,
        selected: 1
      };

      localStorage.setItem('checkoutItems', JSON.stringify([checkoutItem]));
      this.$router.push('/checkout');
    },

    goToDetail(id) {
      this.$router.push(`/product/${id}`);
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.product-detail {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.detail-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.breadcrumb {
  margin-bottom: 30px;
}

.product-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 50px;
  background: white;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.product-gallery {
  position: sticky;
  top: 100px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 16px;
  overflow: hidden;
  background: #f5f5f5;
}

.main-image .el-image {
  width: 100%;
  height: 100%;
}

.thumb-images {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.thumb-item {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.thumb-item.active {
  border-color: #667eea;
}

.thumb-item .el-image {
  width: 100%;
  height: 100%;
}

.product-details {
  padding: 20px 0;
}

.product-name {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.product-price {
  background: #f8f9fc;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.price-label {
  color: #999;
  font-size: 14px;
}

.current-price {
  font-size: 32px;
  font-weight: bold;
  color: #ff6b6b;
  margin: 0 10px;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 16px;
}

.product-sales {
  display: flex;
  gap: 30px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  color: #666;
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-description {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.desc-label {
  font-weight: 500;
  margin-bottom: 10px;
  color: #333;
}

.desc-content {
  color: #666;
  line-height: 1.6;
}

.product-quantity {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stock-info {
  color: #999;
  font-size: 14px;
}

.product-actions {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}

.buy-btn, .cart-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
}

.buy-btn {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  border: none;
}

.cart-btn {
  background: #fff;
  border-color: #667eea;
  color: #667eea;
}

/* 评价区域样式 */
.product-tabs {
  margin-top: 40px;
  background: white;
  border-radius: 20px;
  padding: 20px;
}

.comment-summary {
  display: flex;
  gap: 40px;
  padding: 20px;
  background: #f8f9fc;
  border-radius: 16px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.rating-score {
  text-align: center;
  min-width: 150px;
}

.score {
  font-size: 48px;
  font-weight: bold;
  color: #ff9900;
}

.rating-bars {
  flex: 1;
}

.rating-bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.star-label {
  width: 40px;
  font-size: 13px;
  color: #666;
}

.bar {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #ff9900;
  border-radius: 4px;
}

.count {
  width: 40px;
  font-size: 13px;
  color: #999;
}

.comment-list {
  max-height: 600px;
  overflow-y: auto;
}

.comment-item {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-header {
  display: flex;
  gap: 15px;
  margin-bottom: 12px;
}

.comment-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  flex-shrink: 0;
}

.comment-info {
  flex: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.comment-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
}

.comment-reply {
  background: #f8f9fc;
  padding: 12px;
  border-radius: 12px;
  margin-top: 12px;
  border-left: 3px solid #67c23a;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #67c23a;
  margin-bottom: 6px;
  font-size: 13px;
}

.reply-content {
  color: #666;
  line-height: 1.5;
}

.reply-time {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
}

.empty-comment {
  text-align: center;
  padding: 60px;
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

/* 猜你喜欢 */
.recommend-section {
  margin-top: 40px;
}

.recommend-section h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.recommend-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.recommend-card img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.recommend-info {
  padding: 12px;
}

.recommend-info h4 {
  font-size: 14px;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recommend-info .price {
  color: #ff6b6b;
  font-weight: bold;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #999;
  font-size: 32px;
}

@media (max-width: 768px) {
  .product-info {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .product-name {
    font-size: 20px;
  }

  .current-price {
    font-size: 24px;
  }

  .comment-summary {
    flex-direction: column;
    text-align: center;
  }

  .rating-score {
    text-align: center;
  }

  .rating-bar-item {
    justify-content: center;
  }
}
</style>