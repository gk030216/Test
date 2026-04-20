<template>
  <div class="home-container">
    <Navbar />
    <main class="main-content">
      <!-- 轮播图区域 -->
      <section class="hero-section">
        <div class="carousel-container">
          <div class="carousel-wrapper">
            <div class="carousel-arrow left" @click="prevSlide">
              <i class="el-icon-arrow-left"></i>
            </div>
            <div class="carousel-content">
              <div
                  v-for="(item, index) in displayItems"
                  :key="index"
                  :class="['carousel-item', {
                  center: index === 1,
                  left: index === 0,
                  right: index === 2
                }]"
                  @click="goToLink(item.linkUrl)"
              >
                <div class="item-image" :style="{ backgroundImage: `url(${item.image})` }">
                  <div class="item-overlay"></div>
                  <div class="item-content">
                    <h3>{{ item.title || '宠物服务系统' }}</h3>
                    <p v-if="item.linkUrl">点击查看详情</p>
                  </div>
                </div>
              </div>
            </div>
            <div class="carousel-arrow right" @click="nextSlide">
              <i class="el-icon-arrow-right"></i>
            </div>
          </div>
          <div class="carousel-dots">
            <span
                v-for="(item, index) in banners"
                :key="index"
                :class="['dot', { active: currentIndex === index }]"
                @click="goToSlide(index)"
            ></span>
          </div>
        </div>
      </section>

      <!-- 快速入口区域 -->
      <section class="quick-entry-section">
        <div class="container">
          <div class="quick-entry-grid">
            <div class="quick-entry-item" @click="goToServices">
              <div class="quick-icon">
                <i class="el-icon-s-order"></i>
              </div>
              <div class="quick-text">
                <h4>在线预约</h4>
                <p>快捷预约服务</p>
              </div>
            </div>
            <div class="quick-entry-item" @click="goToShop">
              <div class="quick-icon">
                <i class="el-icon-shopping-cart-2"></i>
              </div>
              <div class="quick-text">
                <h4>宠物商城</h4>
                <p>精选好物推荐</p>
              </div>
            </div>
            <div class="quick-entry-item" @click="goToCommunity">
              <div class="quick-icon">
                <i class="el-icon-chat-dot-round"></i>
              </div>
              <div class="quick-text">
                <h4>宠物社区</h4>
                <p>分享养宠经验</p>
              </div>
            </div>
            <div class="quick-entry-item" @click="goToAIChat">
              <div class="quick-icon">
                <i class="el-icon-cpu"></i>
              </div>
              <div class="quick-text">
                <h4>AI智能顾问</h4>
                <p>24小时在线答疑</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 公告+服务并行区域 -->
      <section class="info-section">
        <div class="container">
          <div class="info-wrapper">
            <!-- 左侧：咨询公告 -->
            <div class="announcement-box" v-if="notices.length > 0">
              <div class="box-header">
                <div class="header-icon">
                  <i class="el-icon-megaphone"></i>
                  <h3>最新公告</h3>
                </div>
                <router-link to="/notices" class="more-btn">
                  查看全部 <i class="el-icon-arrow-right"></i>
                </router-link>
              </div>
              <div class="announcement-list">
                <div
                    v-for="notice in displayNotices"
                    :key="notice.id"
                    class="announcement-item"
                    @click="showNoticeDetail(notice)"
                >
                  <div class="announcement-badge" :class="getNoticeBadgeClass(notice.type)">
                    {{ getNoticeTypeText(notice.type) }}
                  </div>
                  <div class="announcement-info">
                    <div class="announcement-title">{{ notice.title }}</div>
                    <div class="announcement-summary">{{ truncateText(notice.content, 50) }}</div>
                  </div>
                  <div class="announcement-time">{{ formatNoticeTime(notice.createTime) }}</div>
                </div>
              </div>
              <div class="announcement-footer" v-if="notices.length > 3">
                <router-link to="/notices" class="footer-link">
                  查看更多公告 <i class="el-icon-arrow-right"></i>
                </router-link>
              </div>
            </div>

            <!-- 右侧：热门服务 -->
            <div class="services-box">
              <div class="box-header">
                <div class="header-icon">
                  <i class="el-icon-star-on"></i>
                  <h3>热门推荐</h3>
                </div>
                <router-link to="/services" class="more-btn">
                  查看全部 <i class="el-icon-arrow-right"></i>
                </router-link>
              </div>
              <div class="services-list" v-loading="servicesLoading">
                <div class="service-item" v-for="service in hotServices" :key="service.id" @click="goToServiceDetail(service.id)">
                  <div class="service-avatar" v-if="service.image">
                    <img :src="service.image" :alt="service.name">
                  </div>
                  <div class="service-avatar service-default-icon" v-else>
                    <i class="el-icon-service"></i>
                  </div>
                  <div class="service-info">
                    <h4 class="service-name">{{ service.name }}</h4>
                    <p class="service-desc">{{ service.description || '专业宠物服务，用心呵护每一只宠物' }}</p>
                  </div>
                </div>
              </div>
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
                <el-avatar :size="40" :src="post.userAvatar" class="user-avatar">
                  {{ getUserInitial(post.userNickname || post.userName) }}
                </el-avatar>
                <div class="post-author">
                  <span class="author-name">{{ post.userNickname || post.userName || '匿名用户' }}</span>
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
    </main>
    <Footer />

    <!-- 公告详情弹窗 -->
    <el-dialog
        :title="currentNotice.title"
        :visible.sync="dialogVisible"
        width="600px"
        class="notice-dialog"
    >
      <div class="notice-detail">
        <div class="notice-meta">
          <span :class="['notice-type', getNoticeTypeClass(currentNotice.type)]">
            {{ getNoticeTypeText(currentNotice.type) }}
          </span>
          <span class="notice-date">
            <i class="el-icon-date"></i>
            {{ formatNoticeTime(currentNotice.createTime, 'full') }}
          </span>
        </div>
        <div class="notice-content-detail" v-html="currentNotice.content"></div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getCarouselList } from '@/api/carousel';
import { getHotServiceList  } from '@/api/service';
import { getHotProducts } from '@/api/product';
import { getPostList } from '@/api/community';
import { getHomeNotices } from '@/api/consultationNotice';

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
      hotServices: [],
      hotProducts: [],
      communityPosts: [],
      notices: [],
      servicesLoading: false,
      productsLoading: false,
      postsLoading: false,
      currentIndex: 0,
      autoplayTimer: null,
      dialogVisible: false,
      currentNotice: {}
    };
  },
  computed: {
    displayItems() {
      if (this.banners.length === 0) {
        return [
          { image: '', title: '宠物服务系统', linkUrl: '' },
          { image: '', title: '宠物服务系统', linkUrl: '' },
          { image: '', title: '宠物服务系统', linkUrl: '' }
        ];
      }

      const items = [];
      const len = this.banners.length;
      const leftIndex = (this.currentIndex - 1 + len) % len;
      const centerIndex = this.currentIndex;
      const rightIndex = (this.currentIndex + 1) % len;

      items.push(this.banners[leftIndex]);
      items.push(this.banners[centerIndex]);
      items.push(this.banners[rightIndex]);

      return items;
    },

    displayNotices() {
      return this.notices.slice(0, 4);
    }
  },
  created() {
    this.loadCarousels();
    this.loadServices();
    this.loadProducts();
    this.loadPosts();
    this.loadNotices();
    this.startAutoplay();
  },
  beforeDestroy() {
    this.stopAutoplay();
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
        const res = await getHotServiceList();
        if (res.code === 200) {
          this.services = res.data || [];
          this.hotServices = this.services.slice(0, 4).map((service, index) => ({
            ...service,
            rank: index + 1
          }));
        }
      } catch (error) {
        console.error('加载服务失败', error);
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
      // 移除登录判断，未登录也能查看社区帖子
      this.postsLoading = true;
      try {
        const res = await getPostList({ page: 1, pageSize: 3, sort: 'hot' });
        if (res.code === 200) {
          this.communityPosts = res.data.list || [];
        }
      } catch (error) {
        console.error('加载帖子失败', error);
        // 未登录时可能返回401，设置为空数组
        this.communityPosts = [];
      } finally {
        this.postsLoading = false;
      }
    },

    async loadNotices() {
      try {
        const res = await getHomeNotices();
        if (res.code === 200) {
          this.notices = res.data || [];
        }
      } catch (error) {
        console.error('加载公告失败', error);
      }
    },

    showNoticeDetail(notice) {
      this.currentNotice = notice;
      this.dialogVisible = true;
    },

    // 公告类型样式类（用于详情弹窗）
    getNoticeTypeClass(type) {
      const typeMap = {
        1: 'type-service',
        2: 'type-shop',
        3: 'type-community',
        4: 'type-system',
        5: 'type-other'
      };
      return typeMap[type] || 'type-system';
    },

    // 公告标签样式类（用于列表中的badge）
    getNoticeBadgeClass(type) {
      const typeMap = {
        1: 'badge-service',
        2: 'badge-shop',
        3: 'badge-community',
        4: 'badge-system',
        5: 'badge-other'
      };
      return typeMap[type] || 'badge-system';
    },

    // 公告类型文本
    getNoticeTypeText(type) {
      const typeMap = {
        1: '服务公告',
        2: '商城公告',
        3: '社区公告',
        4: '系统公告',
        5: '其他公告'
      };
      return typeMap[type] || '公告';
    },

    formatNoticeTime(date, format = 'simple') {
      if (!date) return '';
      const d = new Date(date);
      if (format === 'full') {
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
      }
      const now = new Date();
      const diff = now - d;
      if (diff < 86400000) return '今日';
      if (diff < 172800000) return '昨日';
      return `${Math.floor(diff / 86400000)}天前`;
    },

    getImageList(images) {
      if (!images) return [];
      if (typeof images === 'string') {
        return images.split(',').filter(img => img && img.trim().length > 0);
      }
      return images;
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
    },

    goToServices() {
      this.$router.push('/services');
    },

    goToShop() {
      this.$router.push('/shop');
    },

    goToCommunity() {
      this.$router.push('/community');
    },

    prevSlide() {
      this.currentIndex = (this.currentIndex - 1 + this.banners.length) % this.banners.length;
      this.resetAutoplay();
    },

    nextSlide() {
      this.currentIndex = (this.currentIndex + 1) % this.banners.length;
      this.resetAutoplay();
    },

    goToSlide(index) {
      this.currentIndex = index;
      this.resetAutoplay();
    },

    startAutoplay() {
      this.autoplayTimer = setInterval(() => {
        this.nextSlide();
      }, 5000);
    },

    stopAutoplay() {
      if (this.autoplayTimer) {
        clearInterval(this.autoplayTimer);
        this.autoplayTimer = null;
      }
    },

    resetAutoplay() {
      this.stopAutoplay();
      this.startAutoplay();
    }
  }
};
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.main-content {
  flex: 1;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* ========== 轮播图样式 ========== */
.hero-section {
  margin-bottom: 30px;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8eaef 100%);
  padding: 20px 0;
}

.carousel-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
}

.carousel-wrapper {
  display: flex;
  align-items: center;
  position: relative;
}

.carousel-arrow {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  z-index: 10;
  color: #2c3e50;
  font-size: 18px;
}

.carousel-arrow:hover {
  background: #409EFF;
  color: white;
  transform: scale(1.05);
}

.carousel-arrow.left {
  position: absolute;
  left: -20px;
}

.carousel-arrow.right {
  position: absolute;
  right: -20px;
}

.carousel-content {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  width: 100%;
  overflow: visible;
}

.carousel-item {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  position: relative;
}

.carousel-item.center {
  flex: 1.4;
  transform: scale(1);
  opacity: 1;
  z-index: 3;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.carousel-item.left,
.carousel-item.right {
  flex: 0.7;
  opacity: 0.7;
  transform: scale(0.85);
  filter: brightness(0.95);
  z-index: 1;
}

.carousel-item.center .item-image {
  height: 320px;
}

.carousel-item.left .item-image,
.carousel-item.right .item-image {
  height: 260px;
}

.item-image {
  position: relative;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  transition: all 0.3s ease;
}

.item-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.4));
  transition: all 0.3s;
}

.carousel-item.center:hover .item-overlay {
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.15), rgba(0, 0, 0, 0.5));
}

.item-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  color: white;
  text-align: center;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
}

.carousel-item.center .item-content h3 {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 8px;
}

.carousel-item.left .item-content h3,
.carousel-item.right .item-content h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.carousel-item.center .item-content p {
  font-size: 14px;
}

.carousel-item.left .item-content p,
.carousel-item.right .item-content p {
  font-size: 12px;
}

.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c0c4cc;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dot:hover {
  background: #909399;
  transform: scale(1.2);
}

.dot.active {
  width: 24px;
  border-radius: 4px;
  background: #409EFF;
}

/* ========== 快速入口区域 ========== */
.quick-entry-section {
  padding: 0 0 30px 0;
}

.quick-entry-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.quick-entry-item {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.quick-entry-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.15);
  border-color: #409EFF;
}

.quick-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-icon i {
  font-size: 24px;
  color: white;
}

.quick-text h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.quick-text p {
  font-size: 12px;
  color: #909399;
}

/* ========== 公告+服务并行区域 ========== */
.info-section {
  padding: 0 0 50px 0;
}

.info-wrapper {
  display: flex;
  gap: 24px;
}

/* 左侧公告区域 */
.announcement-box {
  flex: 1;
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #eef2f6;
}

/* 右侧服务区域 */
.services-box {
  flex: 1;
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #eef2f6;
}

/* 盒子头部 */
.box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409EFF;
}

.header-icon {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon i {
  font-size: 22px;
  color: #409EFF;
}

.header-icon h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.more-btn {
  color: #909399;
  text-decoration: none;
  font-size: 12px;
  transition: all 0.3s;
}

.more-btn:hover {
  color: #409EFF;
}

.more-btn i {
  margin-left: 4px;
  transition: transform 0.3s;
}

.more-btn:hover i {
  transform: translateX(3px);
}

/* 公告列表 */
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.announcement-item:hover {
  background: #e8f4ff;
  transform: translateX(3px);
}

/* 公告标签样式 - 5种类型 */
.badge-service {
  background: #67C23A;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  white-space: nowrap;
}

.badge-shop {
  background: #E6A23C;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  white-space: nowrap;
}

.badge-community {
  background: #409EFF;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  white-space: nowrap;
}

.badge-system {
  background: #909399;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  white-space: nowrap;
}

.badge-other {
  background: #F56C6C;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  white-space: nowrap;
}

.announcement-info {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.announcement-summary {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.announcement-time {
  font-size: 11px;
  color: #c0c4cc;
  white-space: nowrap;
}

.announcement-footer {
  margin-top: 16px;
  padding-top: 12px;
  text-align: center;
  border-top: 1px solid #eef2f6;
}

.footer-link {
  color: #409EFF;
  text-decoration: none;
  font-size: 13px;
}

.footer-link i {
  margin-left: 4px;
}

/* 服务列表 */
.services-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.service-item:hover {
  background: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.service-avatar {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  overflow: hidden;
  background: #e8f4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.service-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.service-default-icon i {
  font-size: 32px;
  color: #409EFF;
}

.service-info {
  flex: 1;
  min-width: 0;
}

.service-name {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.service-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 其他区域样式 */
.section-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
}

.section-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.section-header p {
  color: #909399;
  font-size: 14px;
}

.view-more {
  position: absolute;
  right: 0;
  top: 8px;
  color: #409EFF;
  text-decoration: none;
  font-size: 13px;
  transition: all 0.3s;
}

.view-more:hover {
  color: #66b1ff;
}

.view-more i {
  margin-left: 4px;
  transition: transform 0.3s;
}

.view-more:hover i {
  transform: translateX(4px);
}

.products-section {
  padding: 50px 0;
  background: #f5f7fa;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid #eef2f6;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.product-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: #f5f7fa;
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
  background: #f56c6c;
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 11px;
}

.new-tag {
  background: #67c23a;
}

.product-info {
  padding: 14px;
}

.product-info h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.current-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.original-price {
  color: #c0c4cc;
  text-decoration: line-through;
  font-size: 12px;
}

.product-sales {
  font-size: 11px;
  color: #c0c4cc;
}

.community-section {
  padding: 50px 0;
  background: white;
}

.community-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.community-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  border: 1px solid #eef2f6;
  cursor: pointer;
}

.community-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  background: #409EFF;
  color: white;
}

.author-name {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
  display: block;
}

.post-time {
  font-size: 11px;
  color: #c0c4cc;
}

.post-content .post-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.post-content p {
  color: #606266;
  line-height: 1.5;
  margin-bottom: 12px;
  font-size: 13px;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.post-images img {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  object-fit: cover;
}

.more-images {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

.post-footer {
  display: flex;
  gap: 20px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef2f6;
  color: #c0c4cc;
  font-size: 12px;
}

.post-footer span {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: color 0.3s;
}

.post-footer span:hover {
  color: #409EFF;
}

/* 公告详情弹窗样式 */
.notice-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border-radius: 8px 8px 0 0;
  padding: 20px;
}

.notice-dialog >>> .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.notice-dialog >>> .el-dialog__headerbtn .el-dialog__close {
  color: white;
}

.notice-dialog >>> .el-dialog__body {
  padding: 20px;
}

.notice-detail .notice-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eef2f6;
}

.notice-detail .notice-type {
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 600;
}

/* 详情弹窗中的类型标签颜色 */
.notice-detail .notice-type.type-service {
  background: #f0f9eb;
  color: #67C23A;
}

.notice-detail .notice-type.type-shop {
  background: #fdf6ec;
  color: #E6A23C;
}

.notice-detail .notice-type.type-community {
  background: #ecf5ff;
  color: #409EFF;
}

.notice-detail .notice-type.type-system {
  background: #f4f4f5;
  color: #909399;
}

.notice-detail .notice-type.type-other {
  background: #fef0f0;
  color: #F56C6C;
}

.notice-date {
  font-size: 13px;
  color: #909399;
}

.notice-content-detail {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}

/* 响应式 */
@media (max-width: 992px) {
  .quick-entry-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .info-wrapper {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .carousel-content {
    gap: 12px;
  }

  .carousel-arrow.left {
    left: -12px;
  }

  .carousel-arrow.right {
    right: -12px;
  }

  .carousel-arrow {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }

  .carousel-item.center .item-image {
    height: 240px;
  }

  .carousel-item.left .item-image,
  .carousel-item.right .item-image {
    height: 200px;
  }

  .carousel-item.left,
  .carousel-item.right {
    display: none;
  }

  .carousel-item.center {
    flex: 1;
  }

  .carousel-item.center .item-content h3 {
    font-size: 18px;
  }

  .quick-entry-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .quick-entry-item {
    padding: 12px;
  }

  .quick-icon {
    width: 40px;
    height: 40px;
  }

  .quick-icon i {
    font-size: 20px;
  }

  .quick-text h4 {
    font-size: 14px;
  }

  .quick-text p {
    font-size: 11px;
  }

  .section-header h2 {
    font-size: 22px;
  }

  .view-more {
    position: static;
    display: inline-block;
    margin-top: 10px;
  }

  .products-grid,
  .community-grid {
    grid-template-columns: 1fr;
  }
}
</style>