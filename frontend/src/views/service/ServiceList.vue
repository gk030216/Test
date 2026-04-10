<template>
  <div class="service-container">
    <!-- 导航栏 -->
    <Navbar />

    <div class="service-content">
      <div class="container">
        <!-- 页面头部 -->
        <div class="page-header">
          <h2 class="page-title">🐾 宠物服务</h2>
          <p class="page-desc">专业、贴心、全方位的宠物服务</p>
        </div>

        <!-- 分类导航 -->
        <div class="category-nav">
          <div
              :class="['category-item', { active: currentCategory === null }]"
              @click="handleCategoryChange(null)"
          >
            <i class="el-icon-menu"></i>
            <span>全部</span>
          </div>
          <div
              v-for="cat in categories"
              :key="cat.id"
              :class="['category-item', { active: currentCategory === cat.id }]"
              @click="handleCategoryChange(cat.id)"
          >
            <i class="el-icon-star-on"></i>
            <span>{{ cat.name }}</span>
          </div>
        </div>

        <!-- 排序栏 -->
        <div class="sort-bar">
          <div class="sort-left">
            <span
                :class="['sort-item', { active: currentSort === 'default' }]"
                @click="handleSortChange('default')"
            >默认</span>
            <span
                :class="['sort-item', { active: currentSort === 'price_asc' }]"
                @click="handleSortChange('price_asc')"
            >价格升序 <i class="el-icon-arrow-up"></i></span>
            <span
                :class="['sort-item', { active: currentSort === 'price_desc' }]"
                @click="handleSortChange('price_desc')"
            >价格降序 <i class="el-icon-arrow-down"></i></span>
            <span
                :class="['sort-item', { active: currentSort === 'sales' }]"
                @click="handleSortChange('sales')"
            >销量</span>
            <span
                :class="['sort-item', { active: currentSort === 'hot' }]"
                @click="handleSortChange('hot')"
            >热门</span>
          </div>
          <div class="sort-right">
            <el-input
                v-model="keyword"
                placeholder="搜索服务"
                size="small"
                clearable
                @keyup.enter="handleSearch"
                style="width: 200px"
            >
              <i slot="prefix" class="el-icon-search"></i>
            </el-input>
            <el-button size="small" type="primary" @click="handleSearch" style="margin-left: 8px;">搜索</el-button>
          </div>
        </div>

        <!-- 服务网格 -->
        <div class="services-grid" v-loading="loading">
          <div class="service-card" v-for="service in serviceList" :key="service.id" @click="goToDetail(service.id)">
            <div class="service-image">
              <el-image :src="service.image" fit="cover" class="service-img">
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
              <span class="service-tag" v-if="service.isHot === 1">热门</span>
            </div>
            <div class="service-info">
              <h3 class="service-name">{{ service.name }}</h3>
              <p class="service-desc">{{ truncateText(service.description, 60) }}</p>
              <div class="service-footer">
                <div class="service-price">
                  <span class="current-price">¥{{ service.price }}</span>
                  <span class="original-price" v-if="service.originalPrice">¥{{ service.originalPrice }}</span>
                </div>
                <div class="service-meta">
                  <span><i class="el-icon-time"></i> {{ service.duration }}分钟</span>
                  <span><i class="el-icon-s-order"></i> 已售{{ service.sales || 0 }}</span>
                </div>
                <el-button type="primary" size="small" @click.stop="goToBooking(service)" class="book-btn">
                  立即预约
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-if="!loading && serviceList.length === 0">
          <i class="el-icon-service"></i>
          <p>暂无服务</p>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > pageSize">
          <el-pagination
              @current-change="handlePageChange"
              :current-page="page"
              :page-size="pageSize"
              layout="prev, pager, next"
              :total="total"
              background
          ></el-pagination>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getServiceCategories, getServiceList } from '@/api/service';

export default {
  name: 'ServiceList',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      serviceList: [],
      categories: [],
      total: 0,
      page: 1,
      pageSize: 12,
      currentCategory: null,
      currentSort: 'default',
      keyword: ''
    };
  },
  created() {
    this.loadCategories();
    this.loadServices();
  },
  methods: {
    async loadCategories() {
      try {
        const res = await getServiceCategories();
        if (res.code === 200) {
          this.categories = res.data || [];
        }
      } catch (error) {
        console.error('加载分类失败', error);
      }
    },
    async loadServices() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          categoryId: this.currentCategory || undefined,
          keyword: this.keyword || undefined,
          sort: this.currentSort !== 'default' ? this.currentSort : undefined
        };
        const res = await getServiceList(params);
        if (res.code === 200) {
          this.serviceList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    handleCategoryChange(categoryId) {
      this.currentCategory = categoryId;
      this.page = 1;
      this.loadServices();
    },
    handleSortChange(sort) {
      this.currentSort = sort;
      this.page = 1;
      this.loadServices();
    },
    handleSearch() {
      this.page = 1;
      this.loadServices();
    },
    handlePageChange(page) {
      this.page = page;
      this.loadServices();
    },
    goToDetail(id) {
      this.$router.push(`/service/${id}`);
    },
    goToBooking(service) {
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
      this.$router.push(`/service/${service.id}/booking`);
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
.service-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.service-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-desc {
  font-size: 16px;
  color: #7f8c8d;
}

/* 分类导航 */
.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 30px;
  background: white;
  padding: 15px 25px;
  border-radius: 50px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.category-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-size: 14px;
}

.category-item:hover {
  background: #f0f0f0;
  color: #667eea;
}

.category-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 排序栏 */
.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 15px;
}

.sort-left {
  display: flex;
  gap: 25px;
}

.sort-item {
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
  font-size: 14px;
}

.sort-item:hover,
.sort-item.active {
  color: #667eea;
}

/* 服务网格 */
.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 25px;
}

.service-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
}

.service-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.service-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.service-img {
  width: 100%;
  height: 100%;
  transition: transform 0.3s;
}

.service-card:hover .service-img {
  transform: scale(1.05);
}

.service-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.service-info {
  padding: 18px;
}

.service-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.service-desc {
  font-size: 13px;
  color: #7f8c8d;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.service-footer {
  margin-top: 12px;
}

.service-price {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.current-price {
  color: #ff6b6b;
  font-size: 20px;
  font-weight: bold;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 14px;
}

.service-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.service-meta i {
  margin-right: 4px;
}

.book-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 25px;
  padding: 8px 0;
}

.book-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 20px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
}

/* 分页 */
.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
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
  .category-nav {
    flex-wrap: wrap;
    border-radius: 20px;
  }

  .category-item {
    padding: 6px 15px;
    font-size: 12px;
  }

  .sort-bar {
    flex-direction: column;
  }

  .sort-left {
    flex-wrap: wrap;
    justify-content: center;
  }

  .services-grid {
    grid-template-columns: 1fr;
  }
}
</style>