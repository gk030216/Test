<template>
  <div class="service-container">
    <Navbar />

    <div class="service-content">
      <div class="container">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="header-content">
            <div class="header-left">
              <h2 class="page-title">宠物服务</h2>
              <p class="page-desc">专业、贴心、全方位的宠物服务</p>
            </div>
          </div>
        </div>

        <!-- 分类导航 + 搜索框（同一行） -->
        <div class="category-nav">
          <div class="category-list">
            <div
                :class="['category-item', { active: currentCategory === null }]"
                @click="handleCategoryChange(null)"
            >
              全部
            </div>
            <div
                v-for="cat in categories"
                :key="cat.id"
                :class="['category-item', { active: currentCategory === cat.id }]"
                @click="handleCategoryChange(cat.id)"
            >
              {{ cat.name }}
            </div>
          </div>
          <div class="search-box">
            <el-input
                v-model="keyword"
                placeholder="搜索服务"
                size="medium"
                @keyup.enter="handleSearch"
                clearable
                prefix-icon="el-icon-search"
            >
              <el-button slot="append" @click="handleSearch">搜索</el-button>
            </el-input>
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
                  <span><i class="el-icon-s-order"></i> 已售 {{ service.sales || 0 }}</span>
                </div>
                <el-button type="primary" size="small" plain @click.stop="goToBooking(service)" class="book-btn">
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
          />
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
  background: #f5f7fa;
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
  margin-bottom: 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* 分类导航 + 搜索框（同一行） */
.category-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 20px;
  background: white;
  padding: 12px 20px;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
}

.category-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: #606266;
  font-size: 13px;
}

.category-item:hover {
  background: #ecf5ff;
  color: #409EFF;
}

.category-item.active {
  background: #409EFF;
  color: white;
}

.search-box {
  width: 260px;
}

.search-box ::v-deep .el-input-group__append {
  background: #409EFF;
  border-color: #409EFF;
}

.search-box ::v-deep .el-input-group__append .el-button {
  background: #409EFF;
  border: none;
  color: white;
}

.search-box ::v-deep .el-input-group__append .el-button:hover {
  background: #66b1ff;
}

/* 排序栏 */
.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f6;
}

.sort-left {
  display: flex;
  gap: 24px;
}

.sort-item {
  cursor: pointer;
  color: #909399;
  transition: color 0.3s;
  font-size: 13px;
}

.sort-item:hover,
.sort-item.active {
  color: #409EFF;
}

/* 服务网格 */
.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.service-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid #eef2f6;
}

.service-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #e0e0e0;
}

.service-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: #f5f7fa;
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
  top: 10px;
  left: 10px;
  background: #f56c6c;
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 11px;
}

.service-info {
  padding: 16px;
}

.service-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.service-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.service-footer {
  margin-top: 8px;
}

.service-price {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.current-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.original-price {
  color: #c0c4cc;
  text-decoration: line-through;
  font-size: 12px;
}

.service-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #c0c4cc;
  margin-bottom: 12px;
}

.service-meta i {
  margin-right: 2px;
}

.book-btn {
  width: 100%;
  background: white;
  border-color: #409EFF;
  color: #409EFF;
  border-radius: 8px;
  padding: 6px 0;
  font-size: 13px;
  transition: all 0.3s;
}

.book-btn:hover {
  background: #ecf5ff;
  transform: translateY(-1px);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 12px;
  color: #909399;
  border: 1px solid #eef2f6;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
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
  .service-content {
    padding: 20px 0 40px;
  }

  .category-nav {
    flex-direction: column;
    align-items: stretch;
  }

  .category-list {
    justify-content: center;
  }

  .search-box {
    width: 100%;
  }

  .sort-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .sort-left {
    flex-wrap: wrap;
    justify-content: center;
  }

  .services-grid {
    grid-template-columns: 1fr;
  }

  .service-image {
    height: 160px;
  }
}
</style>