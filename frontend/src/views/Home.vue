<template>
  <div class="home-container">
    <Navbar />
    <main class="main-content">
      <!-- 轮播图区域 -->
      <section class="hero-section">
        <el-carousel :interval="5000" arrow="always" height="500px" :autoplay="banners.length > 0">
          <el-carousel-item v-for="(banner, index) in banners" :key="banner.id || index">
            <div class="banner-item" :style="{ backgroundImage: `url(${banner.image})` }">
              <div class="banner-content">
                <h2>{{ banner.title || '宠物服务系统' }}</h2>
                <p v-if="banner.linkUrl" class="banner-link" @click="goToLink(banner.linkUrl)">点击查看详情</p>
              </div>
            </div>
          </el-carousel-item>
          <!-- 默认轮播图（如果没有数据时显示） -->
          <el-carousel-item v-if="banners.length === 0">
            <div class="banner-item default-banner">
              <div class="banner-content">
                <h2>宠物服务系统</h2>
                <p>用心服务每一个宠物家庭</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </section>

      <!-- 服务展示区域 -->
      <section class="services-section">
        <div class="container">
          <div class="section-header">
            <h2>我们的服务</h2>
            <p>专业、贴心、全方位的宠物服务</p>
            <router-link to="/services" class="view-more">查看更多 <i class="el-icon-arrow-right"></i></router-link>
          </div>
          <div class="services-grid" v-loading="servicesLoading">
            <div class="service-card" v-for="service in services" :key="service.id" @click="goToServiceDetail(service.id)">
              <div class="service-image" v-if="service.image">
                <img :src="service.image" :alt="service.name">
              </div>
              <div class="service-icon" v-else>
                <i class="el-icon-service"></i>
              </div>
              <h3>{{ service.name }}</h3>
              <p>{{ service.description || '专业宠物服务，用心呵护每一只宠物' }}</p>
              <span class="service-price">¥{{ service.price }}起</span>
              <span class="service-duration" v-if="service.duration">
                <i class="el-icon-time"></i> {{ service.duration }}分钟
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- 热门商品 -->
      <section class="products-section">
        <div class="container">
          <div class="section-header">
            <h2>热门商品</h2>
            <p>精选宠物好物，放心选购</p>
            <router-link to="/shop" class="view-more">查看更多 <i class="el-icon-arrow-right"></i></router-link>
          </div>
          <div class="products-grid" v-loading="productsLoading">
            <div class="product-card" v-for="product in hotProducts" :key="product.id" @click="goToProductDetail(product.id)">
              <div class="product-image">
                <img :src="product.image" :alt="product.name">
                <span class="product-tag" v-if="product.isHot === 1">热卖</span>
                <span class="product-tag new-tag" v-else-if="product.isNew === 1">新品</span>
              </div>
              <div class="product-info">
                <h4>{{ product.name }}</h4>
                <p class="product-desc">{{ product.description || '优质宠物商品' }}</p>
                <div class="product-price">
                  <span class="current-price">¥{{ product.price }}</span>
                  <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
                </div>
                <div class="product-sales">
                  <span>已售 {{ product.sales || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 社区动态 -->
      <section class="community-section">
        <div class="container">
          <div class="section-header">
            <h2>宠物社区</h2>
            <p>分享与宠物的温馨瞬间</p>
            <router-link to="/community" class="view-more">更多动态 <i class="el-icon-arrow-right"></i></router-link>
          </div>
          <div class="community-grid" v-loading="postsLoading">
            <div class="community-card" v-for="post in communityPosts" :key="post.id" @click="goToPostDetail(post.id)">
              <div class="post-header">
                <el-avatar :size="40" :src="post.userAvatar" :style="post.userAvatar ? {} : { background: getAvatarColor(post.userName) }">
                  {{ getUserInitial(post.userName) }}
                </el-avatar>
                <div class="post-author">
                  <span class="author-name">{{ post.userName || '匿名用户' }}</span>
                  <span class="post-time">{{ formatTime(post.createTime) }}</span>
                </div>
              </div>
              <div class="post-content">
                <h4 class="post-title">{{ post.title }}</h4>
                <p>{{ truncateText(post.content, 80) }}</p>
                <div class="post-images" v-if="post.images && post.images.length > 0">
                  <img
                      v-for="(img, idx) in getImageList(post.images).slice(0, 3)"
                      :key="idx"
                      :src="img"
                      alt=""
                  >
                  <span v-if="getImageList(post.images).length > 3" class="more-images">
                    +{{ getImageList(post.images).length - 3 }}
                  </span>
                </div>
              </div>
              <div class="post-footer">
                <span><i class="el-icon-star-on"></i> {{ post.likeCount || 0 }}</span>
                <span><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
                <span><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- AI咨询入口 -->
      <section class="ai-section">
        <div class="container">
          <div class="ai-banner" @click="goToAIChat">
            <div class="ai-content">
              <div class="ai-icon">
                <i class="el-icon-cpu"></i>
                <span class="ai-pulse"></span>
              </div>
              <div class="ai-text">
                <h3>AI智能宠物顾问</h3>
                <p>24小时在线，解答养宠疑问</p>
              </div>
              <el-button type="primary" round>立即咨询 <i class="el-icon-arrow-right"></i></el-button>
            </div>
          </div>
        </div>
      </section>
    </main>
    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getCarouselList } from '@/api/carousel';
import { getHotServices } from '@/api/service';
import { getHotProducts } from '@/api/product';
import { getPostList } from '@/api/community';

export default {
  name: 'Home',
  components: {
    Navbar,
    Footer
  },
  data() {
    return {
      banners: [],
      services: [],
      hotProducts: [],
      communityPosts: [],
      servicesLoading: false,
      productsLoading: false,
      postsLoading: false
    };
  },
  created() {
    this.loadCarousels();
    this.loadServices();
    this.loadProducts();
    this.loadPosts();
  },
  methods: {
    async loadCarousels() {
      try {
        const res = await getCarouselList();
        if (res.code === 200 && res.data && res.data.length > 0) {
          this.banners = res.data.filter(item => item.status === 1);
        }
      } catch (error) {
        console.error('加载轮播图失败', error);
      }
    },

    async loadServices() {
      this.servicesLoading = true;
      try {
        const res = await getHotServices();
        if (res.code === 200) {
          this.services = res.data || [];
        }
      } catch (error) {
        console.error('加载服务失败', error);
        // 使用默认数据
        this.services = [
          { id: 1, name: '宠物美容', description: '专业美容师，让爱宠更美丽', price: 88, duration: 60 },
          { id: 2, name: '宠物寄养', description: '温馨环境，24小时看护', price: 68, duration: 1440 },
          { id: 3, name: '宠物医疗', description: '专业兽医，健康保障', price: 50, duration: 30 },
          { id: 4, name: '宠物训练', description: '行为矫正，技能培养', price: 199, duration: 90 },
          { id: 5, name: '宠物摄影', description: '记录美好瞬间', price: 299, duration: 120 },
          { id: 6, name: '上门喂养', description: '贴心服务，安心出行', price: 49, duration: 30 }
        ];
      } finally {
        this.servicesLoading = false;
      }
    },

    async loadProducts() {
      this.productsLoading = true;
      try {
        const res = await getHotProducts();
        if (res.code === 200) {
          this.hotProducts = res.data || [];
        }
      } catch (error) {
        console.error('加载商品失败', error);
      } finally {
        this.productsLoading = false;
      }
    },

    async loadPosts() {
      this.postsLoading = true;
      try {
        const res = await getPostList({ page: 1, pageSize: 3, sort: 'hot' });
        if (res.code === 200) {
          this.communityPosts = res.data.list || [];
        }
      } catch (error) {
        console.error('加载帖子失败', error);
      } finally {
        this.postsLoading = false;
      }
    },

    getImageList(images) {
      if (!images) return [];
      if (typeof images === 'string') {
        return images.split(',').filter(img => img && img.trim().length > 0);
      }
      return images;
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
      for (let i = 0; i < name.length; i++) {
        index += name.charCodeAt(i);
      }
      return colors[index % colors.length];
    },

    getUserInitial(name) {
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
    },

    formatTime(date) {
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
    },

    goToLink(url) {
      if (url) {
        if (url.startsWith('http')) {
          window.open(url, '_blank');
        } else {
          this.$router.push(url);
        }
      }
    },

    goToServiceDetail(id) {
      this.$router.push(`/service/${id}`);
    },

    goToProductDetail(id) {
      this.$router.push(`/product/${id}`);
    },

    goToPostDetail(id) {
      this.$router.push(`/community/post/${id}`);
    },

    goToAIChat() {
      this.$router.push('/ai-chat');
    }
  }
};
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.main-content {
  flex: 1;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 轮播图 */
.hero-section {
  margin-bottom: 60px;
}

.banner-item {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
}

.default-banner {
  background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.default-banner::before {
  background: rgba(0, 0, 0, 0.2);
}

.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.banner-content h2 {
  font-size: 48px;
  margin-bottom: 20px;
  animation: fadeInUp 0.8s;
}

.banner-content p {
  font-size: 18px;
  margin-bottom: 30px;
  animation: fadeInUp 0.8s 0.2s both;
  cursor: pointer;
}

.banner-content p:hover {
  text-decoration: underline;
}

/* 区域头部 */
.section-header {
  text-align: center;
  margin-bottom: 50px;
  position: relative;
}

.section-header h2 {
  font-size: 32px;
  color: #333;
  margin-bottom: 10px;
}

.section-header p {
  color: #666;
  font-size: 16px;
}

.view-more {
  position: absolute;
  right: 0;
  top: 10px;
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.view-more:hover {
  color: #764ba2;
}

.view-more i {
  margin-left: 5px;
  transition: transform 0.3s;
}

.view-more:hover i {
  transform: translateX(5px);
}

/* 服务网格 */
.services-section {
  padding: 60px 0;
  background: white;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
}

.service-card {
  background: white;
  border-radius: 16px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
}

.service-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(102, 126, 234, 0.2);
}

.service-image {
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
  border-radius: 50%;
  overflow: hidden;
}

.service-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.service-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.service-icon i {
  font-size: 36px;
  color: white;
}

.service-card h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
}

.service-card p {
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.service-price {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 18px;
  display: block;
  margin-bottom: 5px;
}

.service-duration {
  color: #999;
  font-size: 13px;
}

.service-duration i {
  margin-right: 4px;
}

/* 商品网格 */
.products-section {
  padding: 60px 0;
  background: #f8f9fa;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 30px;
}

.product-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.new-tag {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.product-info {
  padding: 15px;
}

.product-info h4 {
  font-size: 16px;
  margin-bottom: 8px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.current-price {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 18px;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 13px;
}

.product-sales {
  font-size: 12px;
  color: #999;
}

/* 社区网格 */
.community-section {
  padding: 60px 0;
  background: white;
}

.community-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
}

.community-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
  cursor: pointer;
}

.community-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.author-name {
  font-weight: 500;
  color: #333;
  display: block;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-content .post-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.post-content p {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  position: relative;
}

.post-images img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.more-images {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.post-footer {
  display: flex;
  gap: 20px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  color: #999;
  font-size: 13px;
}

.post-footer span {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  transition: color 0.3s;
}

.post-footer span:hover {
  color: #667eea;
}

/* AI咨询区域 */
.ai-section {
  padding: 60px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.ai-banner {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 30px;
  padding: 40px;
  cursor: pointer;
  transition: all 0.3s;
}

.ai-banner:hover {
  transform: scale(1.02);
  background: rgba(255, 255, 255, 0.2);
}

.ai-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 20px;
}

.ai-icon {
  position: relative;
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-icon i {
  font-size: 40px;
  color: #667eea;
}

.ai-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  animation: pulse 2s infinite;
  transform: translate(-50%, -50%);
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.5;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

.ai-text h3 {
  font-size: 24px;
  color: white;
  margin-bottom: 5px;
}

.ai-text p {
  color: rgba(255, 255, 255, 0.9);
}

.ai-banner .el-button {
  background: white;
  color: #667eea;
  border: none;
  font-weight: 500;
}

.ai-banner .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .banner-content h2 {
    font-size: 28px;
  }

  .section-header h2 {
    font-size: 24px;
  }

  .view-more {
    position: static;
    display: inline-block;
    margin-top: 10px;
  }

  .services-grid,
  .products-grid,
  .community-grid {
    grid-template-columns: 1fr;
  }

  .ai-content {
    flex-direction: column;
    text-align: center;
  }
}
</style>