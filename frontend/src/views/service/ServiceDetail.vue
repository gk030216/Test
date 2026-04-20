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

        <!-- 服务详情Tab -->
        <div class="service-tabs">
          <el-tabs v-model="activeTab">

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
                        {{ comment.displayName ? comment.displayName.charAt(0).toUpperCase() : 'U' }}
                      </el-avatar>
                      <div class="comment-info">
                        <div class="user-info">
                          <span class="user-name">{{ comment.displayName }}</span>
                          <el-rate v-model="comment.rating" disabled show-score text-color="#ff9900"></el-rate>
                        </div>
                        <div class="comment-time">{{ formatDate(comment.createTime) }}</div>
                      </div>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
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
import { getServiceComments, getServiceRatingStats } from '@/api/service';

export default {
  name: 'ServiceDetail',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      service: {},
      currentImage: '',
      recommendServices: [],
      isFavorited: false,
      // 评价相关
      commentLoading: false,
      commentList: [],
      commentPage: 1,
      commentTotal: 0,
      ratingStats: null,
      activeTab: 'detail',
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
    this.loadComments();
    this.loadRatingStats();
  },
  methods: {
    async loadService() {
      this.loading = true;
      try {
        const res = await getServiceDetail(this.serviceId);
        console.log('服务详情数据:', res.data);
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

    // 加载评价列表
// 加载评价列表
    async loadComments() {
      this.commentLoading = true;
      try {
        const res = await getServiceComments(this.serviceId, this.commentPage);
        if (res.code === 200) {
          // ✅ 处理评论数据，优先使用昵称
          this.commentList = (res.data.list || []).map(comment => ({
            ...comment,
            // 显示名称：优先昵称，其次用户名，最后默认
            displayName: comment.userNickname || comment.userName || '匿名用户',
            // 确保 rating 是数字
            rating: Number(comment.rating) || 0,
            // 处理图片
            imageList: comment.images ? (typeof comment.images === 'string' ? comment.images.split(',') : comment.images) : []
          }));
          this.commentTotal = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载评价失败', error);
        this.$message.error('加载评价失败');
      } finally {
        this.commentLoading = false;
      }
    },

// 加载评分统计
    async loadRatingStats() {
      try {
        const res = await getServiceRatingStats(this.serviceId);
        if (res.code === 200) {
          this.ratingStats = res.data;
        }
      } catch (error) {
        console.error('加载评分统计失败', error);
      }
    },

// 计算百分比
    getPercent(star) {
      if (!this.ratingStats || this.ratingStats.total_count === 0) return 0;
      return (this.ratingStats['star' + star] / this.ratingStats.total_count) * 100;
    },

// 分页切换
    handleCommentPageChange(page) {
      this.commentPage = page;
      this.loadComments();
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

/* 服务详情Tab */
.service-tabs {
  margin-top: 20px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #eef2f6;
}

.service-tabs ::v-deep .el-tabs__header {
  margin-bottom: 16px;
}

.service-tabs ::v-deep .el-tabs__item {
  font-size: 14px;
}

.service-tabs ::v-deep .el-tabs__item.is-active {
  color: #409EFF;
}

.service-tabs ::v-deep .el-tabs__active-bar {
  background-color: #409EFF;
}

.service-full-description {
  color: #606266;
  line-height: 1.8;
  font-size: 14px;
}

/* 评价区域样式 */
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
</style>