<template>
  <div class="shop-container">
    <Navbar />

    <div class="shop-content">
      <div class="container">
        <!-- 分类导航 - 只显示一级分类 -->
        <div class="category-nav">
          <div class="category-list">
            <span
                v-for="cat in topCategories"
                :key="cat.id"
                :class="['category-item', { active: currentCategory === cat.id }]"
                @click="handleCategoryChange(cat.id)"
            >
              {{ cat.name }}
            </span>
          </div>
          <div class="search-box">
            <el-input
                v-model="keyword"
                placeholder="搜索商品"
                size="medium"
                @keyup.enter="handleSearch"
                clearable
            >
              <i slot="prefix" class="el-icon-search"></i>
              <el-button slot="append" @click="handleSearch">搜索</el-button>
            </el-input>
          </div>
        </div>

        <!-- 二级分类（当选中一级分类时显示） -->
        <div class="sub-category-nav" v-if="currentCategory && subCategories.length > 0">
          <div class="sub-category-title">子分类：</div>
          <div class="sub-category-list">
            <span
                v-for="cat in subCategories"
                :key="cat.id"
                :class="['sub-category-item', { active: currentSubCategory === cat.id }]"
                @click="handleSubCategoryChange(cat.id)"
            >
              {{ cat.name }}
            </span>
            <span
                v-if="currentSubCategory"
                class="sub-category-item clear"
                @click="handleClearSubCategory"
            >
              清除筛选
            </span>
          </div>
        </div>

        <!-- 排序栏 -->
        <div class="sort-bar">
          <span
              :class="['sort-item', { active: sortType === 'default' }]"
              @click="handleSort('default')"
          >默认</span>
          <span
              :class="['sort-item', { active: sortType === 'price_asc' }]"
              @click="handleSort('price_asc')"
          >价格升序 <i class="el-icon-arrow-up"></i></span>
          <span
              :class="['sort-item', { active: sortType === 'price_desc' }]"
              @click="handleSort('price_desc')"
          >价格降序 <i class="el-icon-arrow-down"></i></span>
          <span
              :class="['sort-item', { active: sortType === 'sales' }]"
              @click="handleSort('sales')"
          >销量</span>
        </div>

        <!-- 商品网格 -->
        <div class="products-grid" v-loading="loading">
          <div
              v-for="product in productList"
              :key="product.id"
              class="product-card"
              @click="goToDetail(product.id)"
          >
            <div class="product-image">
              <img :src="product.image" :alt="product.name">
              <span class="product-tag" v-if="product.isHot === 1">热卖</span>
              <span class="product-tag new-tag" v-else-if="product.isNew === 1">新品</span>
            </div>
            <div class="product-info">
              <h4>{{ product.name }}</h4>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-price">
                <span class="current-price">¥{{ product.price }}</span>
                <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
              </div>
              <div class="product-footer">
                <span class="sales">已售 {{ product.sales }}</span>
                <el-button size="small" type="primary" plain @click.stop="addToCart(product)">加入购物车</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-if="!loading && productList.length === 0">
          <i class="el-icon-shopping-cart-2"></i>
          <p>暂无商品</p>
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
          >
          </el-pagination>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getProductList } from '@/api/product';
import { getAllCategories } from '@/api/category';
import { addToCart } from '@/api/cart';

export default {
  name: 'Shop',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      productList: [],
      allCategories: [],      // 所有分类（用于构建树形结构）
      topCategories: [],      // 一级分类
      subCategories: [],      // 当前选中的一级分类下的二级分类
      currentCategory: null,  // 当前选中的一级分类ID
      currentSubCategory: null, // 当前选中的二级分类ID
      keyword: '',
      sortType: 'default',
      page: 1,
      pageSize: 12,
      total: 0
    };
  },
  created() {
    this.loadCategories();
  },
  methods: {
    // 加载分类并构建树形结构
    async loadCategories() {
      try {
        const res = await getAllCategories();
        if (res.code === 200) {
          this.allCategories = res.data;
          // 获取一级分类（parentId === 0）
          this.topCategories = this.allCategories.filter(cat => cat.parentId === 0);
        }
      } catch (error) {
        console.error('加载分类失败', error);
        // 使用默认分类
        this.topCategories = [
          { id: 1, name: '狗粮', parentId: 0 },
          { id: 2, name: '猫粮', parentId: 0 },
          { id: 3, name: '玩具', parentId: 0 },
          { id: 4, name: '窝垫', parentId: 0 },
          { id: 5, name: '洗护', parentId: 0 }
        ];
      }
    },

    // 获取子分类
    getSubCategories(parentId) {
      return this.allCategories.filter(cat => cat.parentId === parentId);
    },

    // 获取包含子分类的所有分类ID（用于查询商品）
    getCategoryIdsForQuery() {
      if (this.currentSubCategory) {
        // 如果选中了二级分类，只返回该二级分类ID
        return [this.currentSubCategory];
      } else if (this.currentCategory) {
        // 如果只选中了一级分类，返回该一级分类及其所有子分类ID
        const ids = [this.currentCategory];
        const children = this.getSubCategories(this.currentCategory);
        ids.push(...children.map(c => c.id));
        return ids;
      }
      return null;
    },

    async loadProducts() {
      this.loading = true;
      try {
        const categoryIds = this.getCategoryIdsForQuery();
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.keyword || undefined,
          categoryId: categoryIds ? categoryIds.join(',') : undefined  // 传递逗号分隔的字符串
        };

        if (this.sortType === 'price_asc') params.sort = 'price_asc';
        if (this.sortType === 'price_desc') params.sort = 'price_desc';
        if (this.sortType === 'sales') params.sort = 'sales';

        const res = await getProductList(params);
        if (res.code === 200) {
          this.productList = res.data.list;
          this.total = res.data.total;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    handleCategoryChange(categoryId) {
      this.currentCategory = categoryId;
      this.currentSubCategory = null;
      // 获取该一级分类下的子分类
      this.subCategories = this.getSubCategories(categoryId);
      this.page = 1;
      this.loadProducts();
    },

    handleSubCategoryChange(subId) {
      this.currentSubCategory = subId;
      this.page = 1;
      this.loadProducts();
    },

    handleClearSubCategory() {
      this.currentSubCategory = null;
      this.page = 1;
      this.loadProducts();
    },

    handleSearch() {
      this.page = 1;
      this.loadProducts();
    },

    handleSort(type) {
      this.sortType = type;
      this.page = 1;
      this.loadProducts();
    },

    handlePageChange(page) {
      this.page = page;
      this.loadProducts();
    },

    goToDetail(id) {
      this.$router.push(`/product/${id}`);
    },

    async addToCart(product) {
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
        const res = await addToCart(product.id);
        if (res.code === 200) {
          this.$message.success('已加入购物车');
          this.$bus.$emit('cart-updated');
        }
      } catch (error) {
        this.$message.error(error.message || '添加失败');
      }
    }
  },
  watch: {
    currentCategory() {
      // 当一级分类变化时，重置分页
      this.page = 1;
    }
  }
};
</script>

<style scoped>
.shop-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.shop-content {
  flex: 1;
  padding: 30px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 一级分类导航 */
.category-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 20px;
  background: white;
  padding: 15px 25px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.category-list {
  display: flex;
  gap: 25px;
  flex-wrap: wrap;
}

.category-item {
  padding: 8px 20px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f5f5;
  color: #666;
  font-weight: 500;
}

.category-item:hover {
  background: #e8e8e8;
  transform: translateY(-2px);
}

.category-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 12px rgba(102,126,234,0.3);
}

/* 二级分类导航 */
.sub-category-nav {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding: 12px 20px;
  background: #f8f9fc;
  border-radius: 12px;
  flex-wrap: wrap;
}

.sub-category-title {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

.sub-category-list {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.sub-category-item {
  padding: 5px 16px;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  color: #666;
  font-size: 13px;
  border: 1px solid #e0e0e0;
}

.sub-category-item:hover {
  border-color: #667eea;
  color: #667eea;
}

.sub-category-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
}

.sub-category-item.clear {
  background: #f5f5f5;
  border-color: #ddd;
  color: #999;
}

.sub-category-item.clear:hover {
  background: #e8e8e8;
  color: #666;
}

.search-box {
  width: 280px;
}

.sort-bar {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.sort-item {
  cursor: pointer;
  color: #666;
  transition: color 0.3s;
}

.sort-item:hover {
  color: #667eea;
}

.sort-item.active {
  color: #667eea;
  font-weight: 500;
}

/* 商品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

.product-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: all 0.3s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.1);
}

.product-image {
  position: relative;
  height: 220px;
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
  top: 12px;
  left: 12px;
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
  padding: 16px;
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
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
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

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sales {
  font-size: 12px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .category-nav {
    flex-direction: column;
  }

  .search-box {
    width: 100%;
  }

  .sub-category-nav {
    flex-direction: column;
    align-items: flex-start;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}
</style>