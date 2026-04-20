<template>
  <div class="product-detail">
    <Navbar />

    <div class="detail-content">
      <div class="container">
        <!-- 顶部导航栏：面包屑 + 返回按钮同一行 -->
        <div class="top-nav">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/shop' }">宠物商城</el-breadcrumb-item>
            <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" class="back-btn">返回</el-button>
        </div>

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
            <div class="product-header">
              <h1 class="product-name">{{ product.name }}</h1>
              <div class="favorite-btn" @click="toggleFavorite">
                <i :class="['el-icon-star-on', { favorited: isFavorited }]"></i>
                <span>{{ isFavorited ? '已收藏' : '收藏' }}</span>
              </div>
            </div>
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
              <span class="favorite-count" v-if="product.favoriteCount">
                <i class="el-icon-star-on"></i> {{ product.favoriteCount }}人收藏
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
              <el-button
                  size="large"
                  @click="handleCartAction"
                  class="cart-btn"
                  :class="{ 'in-cart': isInCart }"
                  :disabled="cartActionLoading"
                  :loading="cartActionLoading"
              >
                <i :class="isInCart ? 'el-icon-check' : 'el-icon-shopping-cart-2'"></i>
                {{ isInCart ? '已在购物车' : '加入购物车' }}
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
            <!-- 用户评价Tab -->
            <el-tab-pane label="用户评价" name="comment">
              <div class="comment-content">
                <!-- 评分统计 -->
                <div class="comment-summary" v-if="ratingStats">
                  <div class="rating-score">
                    <span class="score">{{ ratingStats.avg_rating || 0 }}</span>
                    <el-rate v-model="ratingStats.avg_rating" disabled show-score text-color="#ff9900"></el-rate>
                    <span class="total">{{ ratingStats.total_count || 0 }}条评价</span>
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

                  <div class="empty-comment" v-if="!commentLoading && commentList.length === 0">
                    <i class="el-icon-chat-dot-round"></i>
                    <p>暂无评价，快来发表第一条评价吧！</p>
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
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 猜你喜欢（协同过滤推荐） -->
        <div class="recommend-section" v-if="recommendProducts.length">
          <h3>
            <i class="el-icon-thumb"></i> 猜你喜欢
            <span class="recommend-tip">根据你的浏览和收藏推荐</span>
          </h3>
          <div class="recommend-grid">
            <div
                v-for="item in recommendProducts"
                :key="item.id"
                class="recommend-card"
                @click.stop="goToDetail(item.id)"
            >
              <div class="recommend-image">
                <img :src="item.image" :alt="item.name">
                <span class="recommend-score" v-if="item.score > 0">
                  匹配度 {{ Math.round(item.score * 100) }}%
                </span>
              </div>
              <div class="recommend-info">
                <h4>{{ item.name }}</h4>
                <div class="recommend-footer">
                  <span class="price">¥{{ item.price }}</span>
                  <span class="sales">已售 {{ item.sales || 0 }}</span>
                </div>
                <div class="recommend-reason" v-if="item.reason">
                  <i class="el-icon-info"></i> {{ item.reason }}
                </div>
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
import { getProductById, getHotProducts, addFavorite, removeFavorite, checkFavorite } from '@/api/product';
import { getRecommendProducts } from '@/api/product';
import { getProductComments, getProductRatingStats } from '@/api/comment';
import { addToCart, getCartList, deleteCartItem } from '@/api/cart';

export default {
  name: 'ProductDetail',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      commentLoading: false,
      cartActionLoading: false,
      product: {},
      quantity: 1,
      activeTab: 'detail',
      currentImage: '',
      recommendProducts: [],
      isFavorited: false,
      commentList: [],
      commentPage: 1,
      commentTotal: 0,
      ratingStats: null,
      isInCart: false,
      cartItemId: null
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
    this.checkFavoriteStatus();
    this.checkCartStatus();
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

    // 检查商品是否已在购物车中
    async checkCartStatus() {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          this.isInCart = false;
          this.cartItemId = null;
          return;
        }

        const res = await getCartList();
        console.log('========== 检查购物车状态 ==========');
        console.log('购物车列表:', res);

        if (res.code === 200 && res.data) {
          // ✅ 使用 == 进行宽松比较，或者手动转换类型
          const cartItem = res.data.find(item => Number(item.productId) === Number(this.productId));
          console.log('当前商品ID:', this.productId);
          console.log('找到的购物车项:', cartItem);

          if (cartItem) {
            this.isInCart = true;
            this.cartItemId = Number(cartItem.id);
            console.log('商品已在购物车, cartItemId:', this.cartItemId);
          } else {
            this.isInCart = false;
            this.cartItemId = null;
            console.log('商品不在购物车');
          }
        }
      } catch (error) {
        console.error('检查购物车状态失败', error);
        this.isInCart = false;
        this.cartItemId = null;
      }
    },

    async checkFavoriteStatus() {
      const token = localStorage.getItem('token');
      if (!token) return;

      try {
        const res = await checkFavorite(this.productId);
        if (res.code === 200) {
          this.isFavorited = res.data;
        }
      } catch (error) {
        console.error('检查收藏状态失败', error);
      }
    },

    async toggleFavorite() {
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
        let res;
        if (this.isFavorited) {
          res = await removeFavorite(this.productId);
        } else {
          res = await addFavorite(this.productId);
        }

        if (res.code === 200) {
          this.isFavorited = !this.isFavorited;
          this.$message.success(this.isFavorited ? '收藏成功' : '已取消收藏');
          if (this.product.favoriteCount !== undefined) {
            this.product.favoriteCount += this.isFavorited ? 1 : -1;
          }
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
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
        const token = localStorage.getItem('token');
        let res;

        if (token) {
          res = await getRecommendProducts({ productId: this.productId, limit: 8 });
        } else {
          res = await getHotProducts();
        }

        if (res.code === 200) {
          if (token) {
            this.recommendProducts = res.data.filter(p => p.id !== parseInt(this.productId)).slice(0, 4);
          } else {
            this.recommendProducts = res.data.filter(p => p.id !== parseInt(this.productId)).slice(0, 4).map(p => ({
              ...p,
              score: 0,
              reason: '热门推荐'
            }));
          }
        }
      } catch (error) {
        console.error('加载推荐失败', error);
        try {
          const res = await getHotProducts();
          if (res.code === 200) {
            this.recommendProducts = res.data.filter(p => p.id !== parseInt(this.productId)).slice(0, 4).map(p => ({
              ...p,
              score: 0,
              reason: '热门推荐'
            }));
          }
        } catch (e) {
          console.error('加载热门商品失败', e);
        }
      }
    },

    // 购物车操作：如果已在购物车则删除，否则添加
// 购物车操作：如果已在购物车则删除，否则添加
    async handleCartAction() {
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

      // 检查库存（添加时）
      if (!this.isInCart && this.product.stock <= 0) {
        this.$message.warning('商品库存不足，无法加入购物车');
        return;
      }

      this.cartActionLoading = true;

      try {
        if (this.isInCart) {
          // ✅ 打印调试信息
          console.log('========== 删除购物车商品 ==========');
          console.log('isInCart:', this.isInCart);
          console.log('cartItemId:', this.cartItemId);
          console.log('cartItemId 类型:', typeof this.cartItemId);
          console.log('productId:', this.productId);

          // 已在购物车，执行删除操作
          if (!this.cartItemId) {
            // 如果 cartItemId 为空，重新获取
            const cartRes = await getCartList();
            console.log('重新获取购物车列表:', cartRes);

            if (cartRes.code === 200 && cartRes.data) {
              const cartItem = cartRes.data.find(item => item.productId === this.productId);
              console.log('找到的购物车项:', cartItem);

              if (cartItem) {
                this.cartItemId = Number(cartItem.id);
              } else {
                this.$message.error('购物车信息异常，请刷新页面重试');
                this.cartActionLoading = false;
                return;
              }
            } else {
              this.$message.error('获取购物车信息失败');
              this.cartActionLoading = false;
              return;
            }
          }

          // ✅ 确保传递数字类型
          console.log('最终删除的 cartItemId:', this.cartItemId);
          const res = await deleteCartItem(Number(this.cartItemId));
          console.log('删除结果:', res);

          if (res.code === 200) {
            this.isInCart = false;
            this.cartItemId = null;
            this.$message.success('已从购物车移出');
            this.$bus.$emit('cart-updated');
          } else {
            this.$message.error(res.message || '移出失败');
          }
        } else {
          // 不在购物车，执行添加操作
          console.log('========== 添加商品到购物车 ==========');
          console.log('productId:', this.product.id);
          console.log('quantity:', this.quantity);

          const res = await addToCart(this.product.id, this.quantity);
          console.log('添加结果:', res);

          if (res.code === 200) {
            // 添加成功后需要重新获取购物车列表以获取 cartItemId
            const cartRes = await getCartList();
            if (cartRes.code === 200 && cartRes.data) {
              const cartItem = cartRes.data.find(item => item.productId === this.productId);
              if (cartItem) {
                this.cartItemId = Number(cartItem.id);
              }
            }
            this.isInCart = true;
            this.$message.success('已加入购物车');
            this.$bus.$emit('cart-updated');
          } else {
            this.$message.error(res.message || '添加失败');
          }
        }
      } catch (error) {
        console.error('购物车操作失败:', error);
        this.$message.error(error.message || '操作失败');
      } finally {
        this.cartActionLoading = false;
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

      if (this.product.stock <= 0) {
        this.$message.warning('商品库存不足，无法购买');
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
      if (id) {
        if (id == this.productId) {
          this.loadProduct();
          window.scrollTo({ top: 0, behavior: 'smooth' });
        } else {
          this.$router.push(`/product/${id}`);
        }
      }
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
  background: #f5f7fa;
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

.product-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
}

.product-gallery {
  position: sticky;
  top: 100px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.main-image .el-image {
  width: 100%;
  height: 100%;
}

.thumb-images {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.thumb-item {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.thumb-item.active {
  border-color: #409EFF;
}

.thumb-item .el-image {
  width: 100%;
  height: 100%;
}

.product-details {
  padding: 0;
}

.product-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.product-name {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  flex: 1;
}

.favorite-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  color: #909399;
  background: #f5f7fa;
  margin-left: 16px;
  border: 1px solid #eef2f6;
}

.favorite-btn:hover {
  background: #fef0f0;
  color: #f56c6c;
}

.favorite-btn i {
  font-size: 20px;
}

.favorite-btn i.favorited {
  color: #f56c6c;
}

.favorite-btn span {
  font-size: 11px;
}

.product-price {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.price-label {
  color: #909399;
  font-size: 13px;
}

.current-price {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
  margin: 0 8px;
}

.original-price {
  color: #c0c4cc;
  text-decoration: line-through;
  font-size: 14px;
}

.product-sales {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-bottom: 1px solid #eef2f6;
  color: #606266;
  font-size: 13px;
  flex-wrap: wrap;
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.favorite-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f56c6c;
}

.favorite-count i {
  font-size: 14px;
}

.product-description {
  padding: 16px 0;
  border-bottom: 1px solid #eef2f6;
}

.desc-label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #2c3e50;
  font-size: 14px;
}

.desc-content {
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
}

.product-quantity {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #eef2f6;
}

.stock-info {
  color: #909399;
  font-size: 13px;
}

.product-actions {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.buy-btn, .cart-btn {
  flex: 1;
  height: 44px;
  font-size: 15px;
  border-radius: 8px;
}

.buy-btn {
  background: #f56c6c;
  border: none;
}

.buy-btn:hover {
  background: #f78989;
}

.cart-btn {
  background: white;
  border-color: #409EFF;
  color: #409EFF;
  transition: all 0.3s;
  cursor: pointer;
}

.cart-btn:hover {
  background: #ecf5ff;
}

.cart-btn.in-cart {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.cart-btn.in-cart:hover {
  background: #85ce61;
}

.cart-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* 评价区域样式 */
.product-tabs {
  margin-top: 30px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #eef2f6;
}

.product-tabs ::v-deep .el-tabs__header {
  margin-bottom: 16px;
}

.product-tabs ::v-deep .el-tabs__item {
  font-size: 14px;
}

.product-tabs ::v-deep .el-tabs__item.is-active {
  color: #409EFF;
}

.product-tabs ::v-deep .el-tabs__active-bar {
  background-color: #409EFF;
}

.comment-summary {
  display: flex;
  gap: 30px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.rating-score {
  text-align: center;
  min-width: 130px;
}

.score {
  font-size: 40px;
  font-weight: bold;
  color: #ff9900;
}

.rating-bars {
  flex: 1;
}

.rating-bar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.star-label {
  width: 35px;
  font-size: 12px;
  color: #606266;
}

.bar {
  flex: 1;
  height: 6px;
  background: #e8eaef;
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #ff9900;
  border-radius: 3px;
}

.count {
  width: 35px;
  font-size: 12px;
  color: #909399;
}

.comment-list {
  max-height: 500px;
  overflow-y: auto;
}

.comment-item {
  padding: 16px;
  border-bottom: 1px solid #eef2f6;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
}

.comment-avatar {
  background: #409EFF;
  color: white;
  flex-shrink: 0;
}

.comment-info {
  flex: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
  font-size: 13px;
}

.comment-time {
  font-size: 11px;
  color: #909399;
}

.comment-content {
  color: #606266;
  line-height: 1.5;
  margin-bottom: 10px;
  font-size: 13px;
}

.comment-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.comment-img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
}

.comment-reply {
  background: #f5f7fa;
  padding: 10px 12px;
  border-radius: 8px;
  margin-top: 10px;
  border-left: 2px solid #67c23a;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #67c23a;
  margin-bottom: 6px;
  font-size: 12px;
}

.reply-content {
  color: #606266;
  line-height: 1.5;
  font-size: 12px;
}

.reply-time {
  font-size: 10px;
  color: #c0c4cc;
  margin-top: 4px;
}

.empty-comment {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.empty-comment i {
  font-size: 48px;
  margin-bottom: 12px;
  color: #c0c4cc;
}

.comment-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

/* 猜你喜欢 */
.recommend-section {
  margin-top: 30px;
}

.recommend-section h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommend-section h3 i {
  color: #409EFF;
}

.recommend-tip {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
  margin-left: 8px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.recommend-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eef2f6;
}

.recommend-card * {
  pointer-events: none;
}

.recommend-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #e0e0e0;
}

.recommend-image {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: #f5f7fa;
}

.recommend-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.recommend-card:hover .recommend-image img {
  transform: scale(1.05);
}

.recommend-score {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
}

.recommend-info {
  padding: 10px;
}

.recommend-info h4 {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #2c3e50;
}

.recommend-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recommend-footer .price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}

.recommend-footer .sales {
  font-size: 11px;
  color: #c0c4cc;
}

.recommend-reason {
  margin-top: 6px;
  font-size: 10px;
  color: #ff9900;
  background: #fff8e6;
  padding: 2px 6px;
  border-radius: 12px;
  display: inline-block;
}

.recommend-reason i {
  margin-right: 2px;
  font-size: 10px;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 28px;
}

/* 响应式 */
@media (max-width: 768px) {
  .detail-content {
    padding: 20px 0 40px;
  }

  .product-info {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 16px;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .product-name {
    font-size: 18px;
  }

  .current-price {
    font-size: 22px;
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

  .product-header {
    flex-wrap: wrap;
  }

  .favorite-btn {
    margin-left: 0;
    margin-top: 8px;
  }

  .product-actions {
    flex-direction: column;
  }

  .recommend-image {
    height: 130px;
  }

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