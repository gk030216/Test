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
            <el-tab-pane label="用户评价" name="comment">
              <div class="comment-content">
                <div class="comment-summary">
                  <div class="rating-score">
                    <span class="score">{{ product.avgRating || 5.0 }}</span>
                    <el-rate v-model="product.avgRating || 5" disabled show-score text-color="#ff9900"></el-rate>
                    <span>{{ product.commentCount || 0 }}条评价</span>
                  </div>
                </div>
                <div class="comment-list" v-if="comments.length">
                  <div class="comment-item" v-for="comment in comments" :key="comment.id">
                    <div class="comment-header">
                      <el-avatar :size="32" :src="comment.avatar">{{ comment.nickname.charAt(0) }}</el-avatar>
                      <div class="comment-info">
                        <span class="nickname">{{ comment.nickname }}</span>
                        <el-rate v-model="comment.rating" disabled></el-rate>
                        <span class="time">{{ formatDate(comment.createTime) }}</span>
                      </div>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                    <div class="comment-images" v-if="comment.images">
                      <img v-for="(img, idx) in comment.images.split(',')" :key="idx" :src="img" />
                    </div>
                  </div>
                </div>
                <div class="empty-comment" v-else>
                  <i class="el-icon-chat-dot-round"></i>
                  <p>暂无评价，快来发表第一条评价吧！</p>
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
import { addToCart } from '@/api/cart';

export default {
  name: 'ProductDetail',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      product: {},
      quantity: 1,
      activeTab: 'detail',
      currentImage: '',
      comments: [],
      recommendProducts: []
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
    // 修复立即购买方法 - 直接跳转到结算页面
    async handleBuyNow() {
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

      // 直接创建结算商品数据，跳转到结算页面
      const checkoutItem = {
        productId: this.product.id,
        productName: this.product.name,
        productImage: this.product.image,
        price: this.product.price,
        quantity: this.quantity,
        selected: 1
      };

      // 将商品存入 localStorage，供结算页面使用
      localStorage.setItem('checkoutItems', JSON.stringify([checkoutItem]));

      // 跳转到结算页面
      this.$router.push('/checkout');
    },
    goToDetail(id) {
      this.$router.push(`/product/${id}`);
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
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

.product-tabs {
  margin-top: 40px;
  background: white;
  border-radius: 20px;
  padding: 20px;
}

.detail-content {
  padding: 20px;
  line-height: 1.8;
  color: #666;
}

.spec-content {
  padding: 20px;
}

.spec-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.spec-label {
  width: 120px;
  color: #999;
}

.spec-value {
  color: #333;
}

.comment-summary {
  padding: 20px;
  background: #f8f9fc;
  border-radius: 12px;
  margin-bottom: 30px;
}

.rating-score {
  display: flex;
  align-items: center;
  gap: 15px;
}

.score {
  font-size: 32px;
  font-weight: bold;
  color: #ff9900;
}

.comment-item {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-info {
  flex: 1;
}

.nickname {
  font-weight: 500;
  color: #333;
  margin-right: 12px;
}

.time {
  font-size: 12px;
  color: #999;
  margin-left: 12px;
}

.comment-images {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.comment-images img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
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
}
</style>