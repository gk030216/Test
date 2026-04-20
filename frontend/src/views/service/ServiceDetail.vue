<template>
  <div class="service-detail-container">
    <Navbar />

    <div class="service-detail-content">
      <div class="container">
        <!-- 顶部导航栏：面包屑 + 返回按钮同一行 -->
        <div class="top-nav">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/services' }">宠物服务</el-breadcrumb-item>
            <el-breadcrumb-item>{{ service.name }}</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" class="back-btn">返回</el-button>
        </div>

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
            <div class="product-header">
              <h1 class="service-name">{{ service.name }}</h1>
              <div class="favorite-btn" @click="toggleFavorite">
                <i :class="['el-icon-star-on', { favorited: isFavorited }]"></i>
                <span>{{ isFavorited ? '已收藏' : '收藏' }}</span>
              </div>
            </div>
            <div class="service-price">
              <span class="price-label">价格：</span>
              <span class="current-price">¥{{ service.price }}</span>
              <span class="original-price" v-if="service.originalPrice">¥{{ service.originalPrice }}</span>
            </div>
            <div class="service-meta">
              <span><i class="el-icon-time"></i> {{ service.duration }}分钟</span>
              <span><i class="el-icon-s-order"></i> 已售 {{ service.sales || 0 }}</span>
              <span class="hot-tag" v-if="service.isHot === 1"><i class="el-icon-star-on"></i> 热门</span>
            </div>
            <div class="service-suitable">
              <span class="suitable-label">适用对象：</span>
              <span class="suitable-value">{{ getSuitableText(service.suitableFor) }}</span>
            </div>
            <div class="service-description">
              <div class="desc-label">服务介绍</div>
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
      </div>
    </div>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getServiceDetail, getHotServices } from '@/api/service';
import { addServiceFavorite, removeServiceFavorite, checkServiceFavorite } from '@/api/service';

export default {
  name: 'ServiceDetail',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      service: {},
      currentImage: '',
      recommendServices: [],
      isFavorited: false
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
    this.checkFavoriteStatus();
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

    async checkFavoriteStatus() {
      const token = localStorage.getItem('token');
      if (!token) return;

      try {
        const res = await checkServiceFavorite(this.serviceId);
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
        this.$confirm('请先登录，才能收藏服务', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login');
        }).catch(() => {});
        return;
      }

      try {
        let res;
        if (this.isFavorited) {
          res = await removeServiceFavorite(this.serviceId);
        } else {
          res = await addServiceFavorite(this.serviceId);
        }

        if (res.code === 200) {
          this.isFavorited = !this.isFavorited;
          this.$message.success(this.isFavorited ? '收藏成功' : '已取消收藏');
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        console.error('收藏操作失败', error);
        this.$message.error('操作失败');
      }
    },

    getSuitableText(suitableFor) {
      const map = { all: '全部宠物', dog: '狗狗', cat: '猫咪' };
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
  background: #f5f7fa;
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

/* 顶部导航栏 */
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

/* 服务信息 */
.service-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
  margin-bottom: 20px;
}

.service-gallery {
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
  flex-wrap: wrap;
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

.service-details {
  padding: 0;
}

.product-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.service-name {
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

.service-price {
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

.service-meta {
  display: flex;
  gap: 20px;
  padding: 12px 0;
  border-bottom: 1px solid #eef2f6;
  color: #606266;
  font-size: 13px;
  flex-wrap: wrap;
}

.service-meta i {
  margin-right: 4px;
  color: #409EFF;
}

.hot-tag {
  color: #f56c6c;
}

.service-suitable {
  padding: 12px 0;
  border-bottom: 1px solid #eef2f6;
}

.suitable-label {
  color: #909399;
  font-size: 13px;
}

.suitable-value {
  color: #2c3e50;
  margin-left: 8px;
  font-size: 13px;
}

.service-description {
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

.service-actions {
  margin-top: 24px;
}

.booking-btn {
  background: #409EFF;
  border: none;
  padding: 12px 0;
  font-size: 15px;
  border-radius: 8px;
  width: 100%;
  transition: all 0.3s;
}

.booking-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

/* 预约须知 */
.notice-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eef2f6;
}

.notice-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #2c3e50;
}

.notice-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 13px;
}

.notice-item i {
  color: #409EFF;
  font-size: 16px;
}

.recommend-section h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #2c3e50;
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

.recommend-info .price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 24px;
}

/* 响应式 */
@media (max-width: 768px) {
  .service-detail-content {
    padding: 20px 0 40px;
  }

  .top-nav {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .back-btn {
    margin-left: 0;
    padding: 6px 12px;
  }

  .service-info {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 16px;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .service-name {
    font-size: 20px;
  }

  .current-price {
    font-size: 22px;
  }

  .notice-content {
    grid-template-columns: 1fr;
  }

  .product-header {
    flex-wrap: wrap;
  }

  .favorite-btn {
    margin-left: 0;
    margin-top: 8px;
  }

  .main-image {
    height: 280px;
  }

  .recommend-img {
    height: 120px;
  }
}
</style>