<template>
  <div class="service-detail-container">
    <Navbar />

    <div class="service-detail-content">
      <div class="container">
        <!-- 面包屑导航 -->
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/services' }">宠物服务</el-breadcrumb-item>
          <el-breadcrumb-item>{{ service.name }}</el-breadcrumb-item>
        </el-breadcrumb>

        <!-- 服务信息 -->
        <div class="service-info" v-loading="loading">
          <div class="service-gallery">
            <div class="main-image">
              <el-image :src="service.image" fit="cover" :preview-src-list="previewImages">
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

          <div class="service-details">
            <h1 class="service-name">{{ service.name }}</h1>
            <div class="service-price">
              <span class="price-label">价格：</span>
              <span class="current-price">¥{{ service.price }}</span>
              <span class="original-price" v-if="service.originalPrice">¥{{ service.originalPrice }}</span>
            </div>
            <div class="service-meta">
              <span><i class="el-icon-time"></i> 服务时长：{{ service.duration }}分钟</span>
              <span><i class="el-icon-s-order"></i> 已售：{{ service.sales || 0 }}次</span>
              <span class="hot-tag" v-if="service.isHot === 1"><i class="el-icon-star-on"></i> 热门推荐</span>
            </div>
            <div class="service-suitable">
              <span class="suitable-label">适用对象：</span>
              <span class="suitable-value">{{ getSuitableText(service.suitableFor) }}</span>
            </div>
            <div class="service-description">
              <div class="desc-label">服务介绍：</div>
              <div class="desc-content">{{ service.description }}</div>
            </div>
            <div class="service-actions">
              <el-button type="primary" size="large" @click="handleBooking" class="booking-btn">
                立即预约
              </el-button>
            </div>
          </div>
        </div>

        <!-- 预约须知 -->
        <div class="notice-section">
          <h3>预约须知</h3>
          <div class="notice-content">
            <div class="notice-item">
              <i class="el-icon-info"></i>
              <span>请提前24小时预约，以便我们为您安排服务</span>
            </div>
            <div class="notice-item">
              <i class="el-icon-time"></i>
              <span>预约成功后，我们会通过短信通知您</span>
            </div>
            <div class="notice-item">
              <i class="el-icon-phone-outline"></i>
              <span>如需取消预约，请至少提前2小时联系我们</span>
            </div>
            <div class="notice-item">
              <i class="el-icon-warning"></i>
              <span>迟到超过30分钟，预约将自动取消</span>
            </div>
          </div>
        </div>

        <!-- 猜你喜欢 -->
        <div class="recommend-section" v-if="recommendServices.length">
          <h3>猜你喜欢</h3>
          <div class="recommend-grid">
            <div
                v-for="item in recommendServices"
                :key="item.id"
                class="recommend-card"
                @click="goToDetail(item.id)"
            >
              <el-image :src="item.image" fit="cover" class="recommend-img">
                <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
              </el-image>
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
import { getServiceDetail, getHotServices } from '@/api/service';

export default {
  name: 'ServiceDetail',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      service: {},
      currentImage: '',
      recommendServices: []
    };
  },
  computed: {
    serviceId() {
      return this.$route.params.id;
    },
    previewImages() {
      const images = [this.service.image];
      if (this.service.images) {
        images.push(...this.service.images.split(','));
      }
      return images;
    },
    thumbImages() {
      const images = [];
      if (this.service.image) images.push(this.service.image);
      if (this.service.images) {
        images.push(...this.service.images.split(',').slice(0, 4));
      }
      return images;
    }
  },
  created() {
    this.loadService();
    this.loadRecommend();
  },
  methods: {
    async loadService() {
      this.loading = true;
      try {
        const res = await getServiceDetail(this.serviceId);
        if (res.code === 200) {
          this.service = res.data;
          this.currentImage = this.service.image;
        } else {
          this.$message.error('服务不存在');
          this.$router.push('/services');
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async loadRecommend() {
      try {
        const res = await getHotServices();
        if (res.code === 200) {
          this.recommendServices = res.data.filter(s => s.id !== this.serviceId).slice(0, 4);
        }
      } catch (error) {
        console.error('加载推荐失败', error);
      }
    },
    getSuitableText(suitableFor) {
      const map = { all: '全部宠物', dog: '仅限狗狗', cat: '仅限猫咪' };
      return map[suitableFor] || '全部宠物';
    },
    handleBooking() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.$confirm('请先登录，才能预约服务', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login');
        }).catch(() => {});
        return;
      }
      this.$router.push(`/service/${this.serviceId}/booking`);
    },
    goToDetail(id) {
      this.$router.push(`/service/${id}`);
    }
  }
};
</script>

<style scoped>
.service-detail-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.service-detail-content {
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

/* 服务信息 */
.service-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 50px;
  background: white;
  border-radius: 24px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
}

.service-gallery {
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
  flex-wrap: wrap;
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

.service-details {
  padding: 20px 0;
}

.service-name {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 20px;
}

.service-price {
  background: #f8f9fc;
  padding: 20px;
  border-radius: 16px;
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

.service-meta {
  display: flex;
  gap: 30px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  color: #666;
  flex-wrap: wrap;
}

.service-meta i {
  margin-right: 5px;
  color: #667eea;
}

.hot-tag {
  color: #ff6b6b;
}

.service-suitable {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.suitable-label {
  color: #999;
  font-weight: 500;
}

.suitable-value {
  color: #2c3e50;
  margin-left: 10px;
}

.service-description {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.desc-label {
  font-weight: 600;
  margin-bottom: 12px;
  color: #2c3e50;
}

.desc-content {
  color: #666;
  line-height: 1.8;
}

.service-actions {
  margin-top: 30px;
}

.booking-btn {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  border: none;
  padding: 14px 40px;
  font-size: 16px;
  border-radius: 40px;
  width: 100%;
}

.booking-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 107, 107, 0.4);
}

/* 预约须知 */
.notice-section {
  background: white;
  border-radius: 20px;
  padding: 25px 30px;
  margin-bottom: 30px;
}

.notice-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #2c3e50;
}

.notice-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 15px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #666;
  font-size: 14px;
}

.notice-item i {
  color: #667eea;
  font-size: 18px;
}

/* 猜你喜欢 */
.recommend-section {
  margin-top: 30px;
}

.recommend-section h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #2c3e50;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.recommend-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.recommend-img {
  width: 100%;
  height: 160px;
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
  font-size: 24px;
}

@media (max-width: 768px) {
  .service-info {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .service-name {
    font-size: 22px;
  }

  .current-price {
    font-size: 24px;
  }

  .notice-content {
    grid-template-columns: 1fr;
  }
}
</style>