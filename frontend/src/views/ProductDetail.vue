<template>
  <div class="product-detail">
    <Navbar />

    <div class="detail-content">
      <div class="container">
        <!-- 顶部导航栏 -->
        <div class="top-nav">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/shop' }">宠物商城</el-breadcrumb-item>
            <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" class="back-btn">返回</el-button>
        </div>

        <!-- 商品信息 -->
        <div class="product-info" v-loading="loading">
          <div class="product-gallery">
            <div class="main-image-wrapper" @mouseenter="pauseAutoPlay" @mouseleave="startAutoPlay">
              <el-carousel
                  ref="carousel"
                  :interval="4000"
                  arrow="always"
                  height="400px"
                  indicator-position="outside"
                  @change="handleCarouselChange"
              >
                <el-carousel-item v-for="(img, idx) in previewImages" :key="idx">
                  <el-image
                      :src="img"
                      fit="cover"
                      class="carousel-image"
                      :preview-src-list="previewImages"
                      :initial-index="idx"
                  >
                    <div slot="error" class="image-slot">
                      <i class="el-icon-picture-outline"></i>
                    </div>
                  </el-image>
                </el-carousel-item>
              </el-carousel>
            </div>

            <div class="thumb-images" v-if="thumbImages.length">
              <div
                  v-for="(img, idx) in thumbImages"
                  :key="idx"
                  :class="['thumb-item', { active: currentCarouselIndex === idx }]"
                  @click="goToSlide(idx)"
              >
                <el-image :src="img" fit="cover">
                  <div slot="error" class="image-slot">
                    <i class="el-icon-picture-outline"></i>
                  </div>
                </el-image>
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
              <span><i class="el-icon-s-order"></i> 销量：{{ product.sales || 0 }}件</span>
              <span><i class="el-icon-box"></i> 库存：{{ product.stock || 0 }}件</span>
              <span class="rating-info" v-if="product.avgRating">
                <i class="el-icon-star-on"></i> 评分：{{ product.avgRating }}
              </span>
              <span class="favorite-count" v-if="product.favoriteCount">
                <i class="el-icon-star-on"></i> {{ product.favoriteCount }}人收藏
              </span>
            </div>

            <div class="product-description">
              <div class="desc-label">商品描述</div>
              <div class="desc-content">{{ product.description }}</div>
            </div>

            <div class="product-quantity">
              <span class="quantity-label">数量：</span>
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
            <el-tab-pane label="用户评价" name="comment">
              <div class="comment-content">
                <div class="comment-summary" v-if="ratingStats">
                  <div class="rating-score">
                    <span class="score">{{ ratingStats.avg_rating || 0 }}</span>
                    <el-rate v-model="ratingStats.avg_rating" disabled text-color="#ff9900"></el-rate>
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

                <div class="comment-list" v-loading="commentLoading">
                  <div class="comment-item" v-for="comment in commentList" :key="comment.id">
                    <div class="comment-header">
                      <el-avatar :size="40" :src="comment.userAvatar" class="comment-avatar">
                        {{ comment.displayName ? comment.displayName.charAt(0).toUpperCase() : 'U' }}
                      </el-avatar>
                      <div class="comment-info">
                        <div class="user-info">
                          <span class="user-name">{{ comment.displayName }}</span>
                          <el-rate v-model="comment.rating" disabled text-color="#ff9900"></el-rate>
                        </div>
                        <div class="comment-time">{{ formatDate(comment.createTime) }}</div>
                      </div>
                    </div>
                    <div class="comment-text">{{ comment.content }}</div>
                    <div class="comment-images" v-if="comment.imageList && comment.imageList.length">
                      <el-image
                          v-for="(img, idx) in comment.imageList"
                          :key="idx"
                          :src="img"
                          :preview-src-list="comment.imageList"
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

        <!-- 算法说明（可展开/折叠） -->
        <div class="algorithm-info-section">
          <div class="algorithm-header" @click="algoExpanded = !algoExpanded">
            <i :class="algoExpanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
            <span>推荐算法说明</span>
            <span class="algo-tip">了解"猜你喜欢"如何为你推荐商品</span>
          </div>
          <div class="algorithm-body" v-show="algoExpanded">
            <div class="algorithm-content">
              <h4>什么是协同过滤？</h4>
              <p>协同过滤（Collaborative Filtering）是推荐系统中最经典的算法之一，其核心思想是"物以类聚，人以群分"——利用用户群体行为数据来发现物品之间的相似性或用户之间的相似性，从而为目标用户推荐其可能感兴趣的物品。登录用户的推荐采用<b>混合推荐策略</b>，同时结合基于用户的协同过滤和基于物品的协同过滤，以平衡推荐多样性与准确性。未登录用户则使用基于物品的协同过滤或热门推荐策略。</p>
              <h4>基于用户 vs 基于物品</h4>
              <p><b>基于用户的协同过滤</b>通过找到与当前用户兴趣相似的其他用户，将这些相似用户喜欢而当前用户未接触的商品推荐给当前用户。系统使用Jaccard相似度计算用户之间的兴趣相似程度。<b>基于物品的协同过滤</b>通过分析商品被同一用户收藏的共现关系来计算商品之间的相似度，采用余弦相似度度量两个商品之间的相似程度。两种算法各有优势，混合推荐综合两者以提升覆盖率和准确率。</p>
              <h4>步骤一：构建评分矩阵</h4>
              <p>系统将用户的收藏和购买行为转化为评分数据，收藏行为赋予权重1，购买行为赋予权重2。设用户集合 U = {u₁, u₂, ..., uₘ}，商品集合 I = {i₁, i₂, ..., iₙ}，构建 m×n 维评分矩阵 R，其中 R(u,i) 表示用户 u 对商品 i 的综合评分。</p>
              <h4>步骤二：计算相似度矩阵</h4>
              <p><b>用户相似度（加权Jaccard）：</b>加权交集 = Σ min(R(a,i), R(b,i))，加权并集 = Σ max(R(a,i), R(b,i))，J(a,b) = 加权交集 / 加权并集。值越接近1表示两个用户兴趣越相似。相比于传统二值Jaccard，加权版本能更好地区分"轻度交互用户"和"深度交互用户"之间的相似性差异。</p>
              <p><b>商品相似度（加权共现乘积）：</b>对于商品 p 和商品 q，相似度 sim(p,q) = Σ R(u,p) × R(u,q)，即所有共同用户评分乘积之和。购买行为的权重2使得购买过的商品之间的相似度贡献是收藏行为的4倍（2×2 vs 1×1），更符合"购买意愿 > 收藏意愿"的业务场景。对商品集合中每一对商品计算相似度后，得到 n×n 维的商品相似度矩阵 S。</p>
              <h4>步骤三：生成 Top-N 推荐列表</h4>
              <p>用户对候选商品的预测评分，由该用户已交互过的商品加权求和得到（权重为商品间相似度），或由相似用户的评分加权得到（权重为用户间相似度）。最后，对所有候选商品按预测评分降序排列，取前 N 个商品作为推荐结果。</p>
              <h4>技术参数与冷启动处理</h4>
              <p><b>参数设置：</b>本系统设置近邻数 K = 50（用户相似度计算）、K = 20（商品相似度计算），推荐列表长度 N = 10。为提高实时性能，商品相似度矩阵采用离线预计算方式，每日定时更新一次，在线推荐阶段仅需查询相似度矩阵并进行加权计算，单次推荐响应时间控制在 100 ms 以内。</p>
              <p><b>冷启动处理：</b>当用户未登录或协同过滤数据不足（如新用户缺少足够的收藏/购买记录、新上架商品缺少足够的收藏记录）时，系统自动降级为<b>热门推荐</b>策略，基于商品的销量和热度排序提供非个性化推荐，确保用户始终能获得有效的推荐内容。</p>
            </div>
          </div>
        </div>

        <!-- 猜你喜欢 -->
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
import { getProductById, getHotProducts, addFavorite, removeFavorite, checkFavorite, getRecommendProducts } from '@/api/product';
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
      activeTab: 'comment',
      recommendProducts: [],
      isFavorited: false,
      commentList: [],
      commentPage: 1,
      commentTotal: 0,
      ratingStats: null,
      isInCart: false,
      cartItemId: null,
      currentCarouselIndex: 0,
      algoExpanded: false
    };
  },
  computed: {
    productId() {
      return this.$route.params.id;
    },
    previewImages() {
      const images = [this.product.image].filter(Boolean);
      if (this.product.images) {
        const list = typeof this.product.images === 'string'
          ? this.product.images.split(',').filter(Boolean)
          : this.product.images;
        images.push(...list);
      }
      return images;
    },
    thumbImages() {
      const images = [];
      if (this.product.image) images.push(this.product.image);
      if (this.product.images) {
        const list = typeof this.product.images === 'string'
          ? this.product.images.split(',').filter(Boolean)
          : this.product.images;
        images.push(...list.slice(0, 4));
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
          this.currentCarouselIndex = 0;
          this.checkFavoriteStatus();
          this.checkCartStatus();
          this.loadComments();
          this.loadRatingStats();
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

    handleCarouselChange(index) {
      this.currentCarouselIndex = index;
    },

    goToSlide(index) {
      this.currentCarouselIndex = index;
      if (this.$refs.carousel) {
        this.$refs.carousel.setActiveItem(index);
      }
    },

    pauseAutoPlay() {
      if (this.$refs.carousel) {
        this.$refs.carousel.stopAutoplay();
      }
    },

    startAutoPlay() {
      if (this.$refs.carousel && this.previewImages.length > 1) {
        this.$refs.carousel.startAutoplay();
      }
    },

    async checkCartStatus() {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          this.isInCart = false;
          this.cartItemId = null;
          return;
        }
        const res = await getCartList();
        if (res.code === 200 && res.data) {
          const cartItem = res.data.find(item => Number(item.productId) === Number(this.productId));
          if (cartItem) {
            this.isInCart = true;
            this.cartItemId = Number(cartItem.id);
          } else {
            this.isInCart = false;
            this.cartItemId = null;
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
        }).catch(() => {});
        return;
      }

      try {
        const res = this.isFavorited
          ? await removeFavorite(this.productId)
          : await addFavorite(this.productId);

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
          this.commentList = (res.data.list || []).map(comment => ({
            ...comment,
            displayName: comment.userNickname || comment.userName || '匿名用户',
            rating: Number(comment.rating) || 0,
            imageList: comment.images
              ? (typeof comment.images === 'string' ? comment.images.split(',') : comment.images)
              : []
          }));
          this.commentTotal = res.data.total || 0;
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
        const res = await getRecommendProducts({ productId: this.productId, limit: 10 });
        if (res.code === 200) {
          this.recommendProducts = res.data
            .filter(p => Number(p.id) !== Number(this.productId))
            .slice(0, 4);
        }
      } catch (error) {
        console.error('加载推荐失败', error);
        try {
          const res = await getHotProducts();
          if (res.code === 200) {
            this.recommendProducts = res.data
              .filter(p => Number(p.id) !== Number(this.productId))
              .slice(0, 4)
              .map(p => ({ ...p, score: 0, reason: '热门推荐' }));
          }
        } catch (e) {
          console.error('加载热门商品失败', e);
        }
      }
    },

    async handleCartAction() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.$confirm('请先登录', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login');
        }).catch(() => {});
        return;
      }

      if (!this.isInCart && this.product.stock <= 0) {
        this.$message.warning('商品库存不足，无法加入购物车');
        return;
      }

      this.cartActionLoading = true;
      try {
        if (this.isInCart) {
          const cartRes = await getCartList();
          if (cartRes.code === 200 && cartRes.data) {
            const cartItem = cartRes.data.find(item => Number(item.productId) === Number(this.productId));
            if (!cartItem) {
              this.$message.error('购物车信息异常，请刷新页面重试');
              return;
            }
            this.cartItemId = Number(cartItem.id);
          }

          const res = await deleteCartItem(this.cartItemId);
          if (res.code === 200) {
            this.isInCart = false;
            this.cartItemId = null;
            this.$message.success('已从购物车移出');
            this.$bus.$emit('cart-updated');
          }
        } else {
          const res = await addToCart(this.product.id, this.quantity);
          if (res.code === 200) {
            const cartRes = await getCartList();
            if (cartRes.code === 200 && cartRes.data) {
              const cartItem = cartRes.data.find(item => Number(item.productId) === Number(this.productId));
              if (cartItem) {
                this.cartItemId = Number(cartItem.id);
              }
            }
            this.isInCart = true;
            this.$message.success('已加入购物车');
            this.$bus.$emit('cart-updated');
          }
        }
      } catch (error) {
        console.error('购物车操作失败:', error);
        this.$message.error('操作失败');
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
        }).catch(() => {});
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
      if (Number(id) === Number(this.productId)) {
        this.loadProduct();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else {
        this.$router.push(`/product/${id}`);
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
  background: #fdf8f5;
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

/* ========== 顶部导航 ========== */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.back-btn {
  border-radius: 8px;
  color: #7a6a62;
  background: white;
  border: 1px solid #f5ece6;
  padding: 8px 16px;
  font-size: 13px;
  transition: all 0.3s;
  flex-shrink: 0;
  margin-left: 16px;
}

.back-btn:hover {
  color: #f0826a;
  border-color: #f0826a;
  background: #fef6f0;
}

/* ========== 商品信息 ========== */
.product-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 1px 4px rgba(180, 120, 90, 0.06);
  border: 1px solid #f5ece6;
}

.product-gallery {
  position: sticky;
  top: 100px;
}

.main-image-wrapper {
  border-radius: 12px;
  overflow: hidden;
  background: #fefbf9;
}

.el-carousel ::v-deep .el-carousel__container {
  height: 400px;
}

.carousel-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  cursor: pointer;
}

.el-carousel ::v-deep .el-carousel__arrow {
  background-color: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  width: 36px;
  height: 36px;
}

.el-carousel ::v-deep .el-carousel__arrow:hover {
  background-color: rgba(240, 130, 106, 0.8);
}

.el-carousel ::v-deep .el-carousel__indicators {
  bottom: -30px;
}

.el-carousel ::v-deep .el-carousel__indicator .el-carousel__button {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #d0b8a8;
}

.el-carousel ::v-deep .el-carousel__indicator.is-active .el-carousel__button {
  background-color: #f0826a;
  width: 20px;
  border-radius: 4px;
}

/* 缩略图 */
.thumb-images {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.thumb-item {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
  background: #fefbf9;
}

.thumb-item:hover {
  transform: translateY(-2px);
}

.thumb-item.active {
  border-color: #f0826a;
  box-shadow: 0 2px 8px rgba(240, 130, 106, 0.3);
}

.thumb-item .el-image {
  width: 100%;
  height: 100%;
}

.thumb-item .el-image__inner {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ========== 商品详情右侧 ========== */
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
  color: #3d2e2a;
  margin: 0;
  flex: 1;
  line-height: 1.3;
}

.favorite-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  color: #b8a098;
  background: #fefbf9;
  margin-left: 16px;
  border: 1px solid #f5ece6;
  flex-shrink: 0;
}

.favorite-btn:hover {
  background: #fef0f0;
  color: #f56c6c;
  border-color: #fcd0d0;
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
  background: #fefbf9;
  padding: 16px 20px;
  border-radius: 10px;
  margin-bottom: 16px;
  border: 1px solid #f5ece6;
}

.price-label {
  color: #a08c84;
  font-size: 13px;
}

.current-price {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
  margin: 0 8px;
}

.original-price {
  color: #c4b0a6;
  text-decoration: line-through;
  font-size: 14px;
}

.product-sales {
  display: flex;
  gap: 20px;
  padding: 12px 0;
  border-bottom: 1px solid #f5ece6;
  color: #7a6a62;
  font-size: 13px;
  flex-wrap: wrap;
}

.product-sales i {
  margin-right: 4px;
  color: #d0b8a8;
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f59e4b;
}

.favorite-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f56c6c;
}

.product-description {
  padding: 16px 0;
  border-bottom: 1px solid #f5ece6;
}

.desc-label {
  font-weight: 600;
  margin-bottom: 8px;
  color: #3d2e2a;
  font-size: 14px;
}

.desc-content {
  color: #7a6a62;
  line-height: 1.7;
  font-size: 14px;
}

.product-quantity {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f5ece6;
}

.quantity-label {
  color: #3d2e2a;
  font-size: 14px;
}

.stock-info {
  color: #a08c84;
  font-size: 13px;
}

.product-actions {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.buy-btn,
.cart-btn {
  flex: 1;
  height: 46px;
  font-size: 15px;
  border-radius: 10px;
  font-weight: 500;
}

.buy-btn {
  background: linear-gradient(135deg, #f56c6c, #e85d5d);
  border: none;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.25);
}

.buy-btn:hover {
  background: linear-gradient(135deg, #f78989, #f07070);
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(245, 108, 108, 0.35);
}

.cart-btn {
  background: white;
  border: 1px solid #f0826a;
  color: #f0826a;
  transition: all 0.3s;
}

.cart-btn:hover {
  background: #fef6f0;
}

.cart-btn.in-cart {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  border-color: transparent;
  color: white;
}

.cart-btn.in-cart:hover {
  background: linear-gradient(135deg, #7dd44a, #95d87a);
}

/* ========== 评价区域 ========== */
.product-tabs {
  margin-top: 20px;
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  border: 1px solid #f5ece6;
  box-shadow: 0 1px 4px rgba(180, 120, 90, 0.05);
}

.product-tabs ::v-deep .el-tabs__header {
  margin-bottom: 20px;
}

.product-tabs ::v-deep .el-tabs__item {
  font-size: 14px;
  color: #7a6a62;
}

.product-tabs ::v-deep .el-tabs__item.is-active {
  color: #f0826a;
}

.product-tabs ::v-deep .el-tabs__active-bar {
  background-color: #f0826a;
}

.comment-summary {
  display: flex;
  gap: 30px;
  padding: 16px 20px;
  background: #fefbf9;
  border-radius: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  border: 1px solid #f5ece6;
}

.rating-score {
  text-align: center;
  min-width: 130px;
}

.score {
  font-size: 40px;
  font-weight: bold;
  color: #f59e4b;
  display: block;
  line-height: 1;
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
  color: #7a6a62;
}

.bar {
  flex: 1;
  height: 6px;
  background: #f5ece6;
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #f59e4b;
  border-radius: 3px;
}

.count {
  width: 35px;
  font-size: 12px;
  color: #a08c84;
}

.comment-list {
  max-height: 500px;
  overflow-y: auto;
}

.comment-item {
  padding: 16px;
  border-bottom: 1px solid #f5ece6;
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
  background: linear-gradient(135deg, #f59e4b, #f0826a);
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
  color: #3d2e2a;
  font-size: 13px;
}

.comment-time {
  font-size: 11px;
  color: #b8a098;
}

.comment-text {
  color: #7a6a62;
  line-height: 1.6;
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
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #f5ece6;
}

.comment-reply {
  background: #fefbf9;
  padding: 10px 14px;
  border-radius: 10px;
  margin-top: 10px;
  border-left: 3px solid #67c23a;
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
  color: #7a6a62;
  line-height: 1.5;
  font-size: 12px;
}

.reply-time {
  font-size: 10px;
  color: #c4b0a6;
  margin-top: 4px;
}

.empty-comment {
  text-align: center;
  padding: 40px;
  color: #b8a098;
}

.empty-comment i {
  font-size: 48px;
  margin-bottom: 12px;
  color: #d0b8a8;
}

.comment-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

/* ========== 猜你喜欢 ========== */
/* ========== 算法说明折叠面板 ========== */
.algorithm-info-section {
  margin-top: 30px;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f5ece6;
  box-shadow: 0 1px 4px rgba(180, 120, 90, 0.05);
}

.algorithm-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 24px;
  cursor: pointer;
  transition: background 0.3s;
  user-select: none;
}

.algorithm-header:hover {
  background: #fefbf9;
}

.algorithm-header i {
  color: #f59e4b;
  font-size: 14px;
}

.algorithm-header span {
  font-size: 15px;
  font-weight: 600;
  color: #3d2e2a;
}

.algo-tip {
  font-size: 12px !important;
  color: #a08c84 !important;
  font-weight: 400 !important;
  margin-left: 6px;
}

.algorithm-body {
  border-top: 1px solid #f5ece6;
}

.algorithm-content {
  padding: 20px 24px;
  color: #7a6a62;
  font-size: 13px;
  line-height: 1.8;
}

.algorithm-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: #3d2e2a;
  margin: 16px 0 8px 0;
}

.algorithm-content h4:first-child {
  margin-top: 0;
}

.algorithm-content p {
  margin: 0 0 10px 0;
}

.recommend-section {
  margin-top: 30px;
}

.recommend-section h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #3d2e2a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommend-section h3 i {
  color: #f0826a;
}

.recommend-tip {
  font-size: 12px;
  color: #a08c84;
  font-weight: 400;
  margin-left: 8px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.recommend-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f5ece6;
  box-shadow: 0 1px 4px rgba(180, 120, 90, 0.05);
}

.recommend-card * {
  pointer-events: none;
}

.recommend-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(180, 120, 90, 0.12);
  border-color: #e8c8b0;
}

.recommend-image {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: #fefbf9;
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
  background: rgba(240, 130, 106, 0.85);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
}

.recommend-info {
  padding: 12px;
}

.recommend-info h4 {
  font-size: 13px;
  font-weight: 600;
  margin: 0 0 6px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #3d2e2a;
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
  color: #c4b0a6;
}

.recommend-reason {
  margin-top: 6px;
  font-size: 10px;
  color: #e8785a;
  background: #fef6f0;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #fef0e8;
  color: #d0b8a8;
  font-size: 28px;
}

/* ========== 响应式 ========== */
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
    flex-wrap: wrap;
  }

  .back-btn {
    margin-left: 0;
    padding: 6px 12px;
  }
}
</style>
