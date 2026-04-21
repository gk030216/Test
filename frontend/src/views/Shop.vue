<template>
  <div class="shop-container">
    <Navbar />

    <div class="shop-content">
      <div class="container">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="header-content">
            <div class="header-left">
              <h2 class="page-title">宠物商城</h2>
              <p class="page-desc">精选宠物好物，放心选购</p>
            </div>
          </div>
        </div>

        <!-- 分类导航 -->
        <div class="category-nav">
          <div class="category-list">
            <span
                :class="['category-item', { active: currentCategory === null }]"
                @click="handleCategoryChange(null)"
            >
              全部
            </span>
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
                prefix-icon="el-icon-search"
            >
              <el-button slot="append" @click="handleSearch">搜索</el-button>
            </el-input>
          </div>
        </div>

        <!-- 二级分类 -->
        <div class="sub-category-nav" v-if="currentCategory !== null && subCategories.length > 0">
          <div class="sub-category-title">子分类：</div>
          <div class="sub-category-list">
            <span
                :class="['sub-category-item', { active: currentSubCategory === null }]"
                @click="handleSubCategoryChange(null)"
            >
              全部
            </span>
            <span
                v-for="cat in subCategories"
                :key="cat.id"
                :class="['sub-category-item', { active: currentSubCategory === cat.id }]"
                @click="handleSubCategoryChange(cat.id)"
            >
              {{ cat.name }}
            </span>
          </div>
        </div>

        <!-- 排序栏 -->
        <div class="sort-bar">
          <span :class="['sort-item', { active: sortType === 'default' }]" @click="handleSort('default')">默认</span>
          <span :class="['sort-item', { active: sortType === 'price_asc' }]" @click="handleSort('price_asc')">价格升序 <i class="el-icon-arrow-up"></i></span>
          <span :class="['sort-item', { active: sortType === 'price_desc' }]" @click="handleSort('price_desc')">价格降序 <i class="el-icon-arrow-down"></i></span>
          <span :class="['sort-item', { active: sortType === 'sales' }]" @click="handleSort('sales')">销量</span>
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
              <span class="stock-tag" v-if="product.stock <= 0">缺货</span>
            </div>
            <div class="product-info">
              <h4>{{ product.name }}</h4>
              <p class="product-desc">{{ truncateText(product.description, 40) }}</p>
              <div class="product-price">
                <span class="current-price">¥{{ product.price }}</span>
                <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
              </div>
              <div class="product-footer">
                <div class="footer-left">
                  <span class="sales">已售 {{ product.sales || 0 }}</span>
                  <span class="stock" :class="{ 'stock-warning': product.stock <= 5 && product.stock > 0, 'stock-out': product.stock <= 0 }">
                    <i class="el-icon-goods"></i> 库存 {{ product.stock || 0 }}
                  </span>
                </div>
                <el-tooltip
                    :content="getCartButtonTooltip(product)"
                    placement="top"
                >
                  <el-button
                      size="small"
                      :type="getCartButtonType(product)"
                      :plain="!isInCart(product.id) && product.stock > 0"
                      circle
                      @click.stop="handleCartAction($event, product)"
                      :disabled="(addingProductId === product.id) || (!isInCart(product.id) && product.stock <= 0)"
                      :loading="addingProductId === product.id"
                      class="add-cart-icon"
                  >
                    <i :class="getCartButtonIcon(product)"></i>
                  </el-button>
                </el-tooltip>
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
          />
        </div>
      </div>
    </div>

    <!-- 圆形悬浮购物车 -->
    <div class="floating-cart" @click="goToCart" ref="floatingCart">
      <div class="cart-icon-wrapper">
        <i class="el-icon-shopping-cart-2"></i>
        <span class="cart-badge" v-if="cartCount > 0">{{ cartCount > 99 ? '99+' : cartCount }}</span>
      </div>
      <div class="cart-total" v-if="cartTotal > 0">
        ¥{{ cartTotal }}
      </div>
    </div>

    <!-- 飞行动画元素 -->
    <div
        v-for="flyItem in flyingItems"
        :key="flyItem.id"
        class="flying-item"
        :style="{
          left: flyItem.startX + 'px',
          top: flyItem.startY + 'px',
          '--end-x': (flyItem.endX - flyItem.startX) + 'px',
          '--end-y': (flyItem.endY - flyItem.startY) + 'px'
        }"
    >
      <img :src="flyItem.image" :alt="flyItem.name">
    </div>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getProductList } from '@/api/product';
import { getAllCategories } from '@/api/category';
import { addToCart, getCartSummary, getCartList, deleteCartItem } from '@/api/cart';

export default {
  name: 'Shop',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      productList: [],
      allCategories: [],
      topCategories: [],
      subCategories: [],
      currentCategory: null,
      currentSubCategory: null,
      keyword: '',
      sortType: 'default',
      page: 1,
      pageSize: 12,
      total: 0,
      cartCount: 0,
      cartTotal: 0,
      flyingItems: [],
      nextFlyId: 1,
      cartProductMap: new Map(),
      addingProductId: null
    };
  },
  computed: {
    // 判断是否登录
    isLoggedIn() {
      return !!localStorage.getItem('token');
    }
  },
  created() {
    this.loadCategories();
    // ✅ 只有登录后才加载购物车相关数据
    if (this.isLoggedIn) {
      this.loadCartSummary();
      this.loadCartList();
    }
    this.$bus.$on('cart-updated', this.refreshCartData);
  },
  beforeDestroy() {
    this.$bus.$off('cart-updated', this.refreshCartData);
  },
  methods: {
    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    },

    isInCart(productId) {
      return this.cartProductMap.has(productId);
    },

    getCartItemId(productId) {
      return this.cartProductMap.get(productId);
    },

    getCartButtonTooltip(product) {
      // ✅ 未登录时显示请先登录
      if (!this.isLoggedIn) return '请先登录';
      if (product.stock <= 0) return '库存不足';
      if (this.isInCart(product.id)) return '移出购物车';
      return '加入购物车';
    },

    getCartButtonType(product) {
      // ✅ 未登录时按钮为灰色
      if (!this.isLoggedIn) return 'info';
      if (this.isInCart(product.id)) return 'danger';
      if (product.stock <= 0) return 'info';
      return 'primary';
    },

    getCartButtonIcon(product) {
      // ✅ 未登录时显示用户图标
      if (!this.isLoggedIn) return 'el-icon-user';
      if (this.isInCart(product.id)) return 'el-icon-delete';
      if (product.stock <= 0) return 'el-icon-close';
      return 'el-icon-shopping-cart-2';
    },

    async loadCartList() {
      // ✅ 未登录不调用
      if (!this.isLoggedIn) return;
      try {
        const res = await getCartList();
        if (res.code === 200) {
          this.cartProductMap.clear();
          res.data.forEach(item => {
            this.cartProductMap.set(item.productId, item.id);
          });
        }
      } catch (error) {
        console.error('加载购物车列表失败', error);
      }
    },

    async refreshCartData() {
      // ✅ 未登录不调用
      if (!this.isLoggedIn) return;
      await this.loadCartSummary();
      await this.loadCartList();
      await this.loadProducts();
    },

    async loadCategories() {
      try {
        const res = await getAllCategories();
        if (res.code === 200) {
          this.allCategories = res.data;
          this.topCategories = this.allCategories.filter(cat => cat.parentId === 0);
        }
      } catch (error) {
        console.error('加载分类失败', error);
      }
      this.loadProducts();
    },

    async loadCartSummary() {
      // ✅ 未登录时不调用接口，使用本地存储
      if (!this.isLoggedIn) {
        const cart = JSON.parse(localStorage.getItem('cart') || '[]');
        this.cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);
        this.cartTotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
        return;
      }
      try {
        const res = await getCartSummary();
        if (res.code === 200) {
          this.cartCount = res.data.totalCount || 0;
          this.cartTotal = res.data.totalAmount || 0;
        }
      } catch (error) {
        console.error('加载购物车数量失败', error);
      }
    },

    getSubCategories(parentId) {
      return this.allCategories.filter(cat => cat.parentId === parentId);
    },

    getCategoryIdForQuery() {
      if (this.currentSubCategory !== null && this.currentSubCategory !== undefined) {
        return this.currentSubCategory;
      } else if (this.currentCategory !== null && this.currentCategory !== undefined) {
        return this.currentCategory;
      }
      return null;
    },

    async loadProducts() {
      this.loading = true;
      try {
        let categoryId = this.getCategoryIdForQuery();
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.keyword || undefined,
        };

        console.log('请求商品列表参数:', params);  // ✅ 移到对象外面

        if (categoryId !== null && categoryId !== undefined) {
          params.categoryId = categoryId;
        }

        if (this.sortType === 'price_asc') params.sort = 'price_asc';
        if (this.sortType === 'price_desc') params.sort = 'price_desc';
        if (this.sortType === 'sales') params.sort = 'sales';

        const res = await getProductList(params);
        console.log('商品列表返回:', res);  // ✅ 移到正确位置

        if (res.code === 200) {
          this.productList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
        console.error('加载商品失败:', error);
      } finally {
        this.loading = false;
      }
    },

    handleCategoryChange(categoryId) {
      this.currentCategory = categoryId !== undefined ? categoryId : null;
      this.currentSubCategory = null;
      if (categoryId !== null && categoryId !== undefined) {
        this.subCategories = this.getSubCategories(categoryId);
      } else {
        this.subCategories = [];
      }
      this.page = 1;
      this.loadProducts();
    },

    handleSubCategoryChange(subId) {
      this.currentSubCategory = subId;
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

    goToCart() {
      // ✅ 未登录时提示登录
      if (!this.isLoggedIn) {
        this.$confirm('请先登录，查看购物车', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login');
        });
        return;
      }
      this.$router.push('/cart');
    },

    async handleCartAction(event, product) {
      // ✅ 未登录时提示登录
      if (!this.isLoggedIn) {
        this.$confirm('请先登录，加入购物车', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login');
        });
        return;
      }

      if (product.stock <= 0) {
        this.$message.warning('商品库存不足');
        return;
      }

      if (!this.isInCart(product.id) && product.stock <= 0) {
        this.$message.warning('商品库存不足，无法加入购物车');
        return;
      }

      this.addingProductId = product.id;

      try {
        if (this.isInCart(product.id)) {
          const cartItemId = this.getCartItemId(product.id);
          if (!cartItemId) {
            this.$message.error('购物车信息异常，请刷新页面重试');
            this.addingProductId = null;
            return;
          }

          const res = await deleteCartItem(cartItemId);
          if (res.code === 200) {
            this.cartProductMap.delete(product.id);
            this.$message.success('已从购物车移出');

            if (event && event.currentTarget) {
              const btn = event.currentTarget;
              btn.classList.add('btn-removed');
              setTimeout(() => {
                if (btn) {
                  btn.classList.remove('btn-removed');
                }
              }, 300);
            }

            await this.loadCartSummary();
            await this.loadCartList();
            await this.loadProducts();
            this.$bus.$emit('cart-updated');
          } else {
            this.$message.error(res.message || '移出失败');
          }
        } else {
          this.addToCartWithAnimation(event, product);
        }
      } catch (error) {
        console.error('购物车操作失败:', error);
        this.$message.error(error.message || '操作失败');
      } finally {
        this.addingProductId = null;
      }
    },

    addToCartWithAnimation(event, product) {
      if (!event || !event.currentTarget) {
        this.addToCart(product);
        return;
      }

      const cartIcon = this.$refs.floatingCart;
      if (!cartIcon) {
        this.addToCart(product);
        return;
      }

      const btnRect = event.currentTarget.getBoundingClientRect();
      const cartRect = cartIcon.getBoundingClientRect();

      const startX = btnRect.left + btnRect.width / 2 - 20;
      const startY = btnRect.top + btnRect.height / 2 - 20;
      const endX = cartRect.left + cartRect.width / 2 - 20;
      const endY = cartRect.top + cartRect.height / 2 - 20;

      const flyId = this.nextFlyId++;
      this.flyingItems.push({
        id: flyId,
        startX: startX,
        startY: startY,
        endX: endX,
        endY: endY,
        image: product.image,
        name: product.name
      });

      setTimeout(() => {
        this.flyingItems = this.flyingItems.filter(item => item.id !== flyId);
      }, 500);

      this.addToCart(product);
    },

    async addToCart(product) {
      if (this.isInCart(product.id)) {
        this.$message.info('该商品已在购物车中');
        return;
      }

      try {
        const res = await addToCart(product.id);
        if (res.code === 200) {
          const cartRes = await getCartList();
          if (cartRes.code === 200) {
            this.cartProductMap.clear();
            cartRes.data.forEach(item => {
              this.cartProductMap.set(item.productId, item.id);
            });
          }

          this.$message.success('已加入购物车');
          await this.loadCartSummary();
          this.$bus.$emit('cart-updated');

          const cartIcon = this.$refs.floatingCart;
          if (cartIcon) {
            cartIcon.classList.add('cart-bounce');
            setTimeout(() => {
              if (cartIcon) {
                cartIcon.classList.remove('cart-bounce');
              }
            }, 500);
          }
        }
      } catch (error) {
        console.error('添加购物车失败:', error);
        this.$message.error(error.message || '添加购物车失败');
      }
    }
  }
};
</script>

<style scoped>
/* 样式与之前相同，添加移出动画 */
.shop-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.shop-content {
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

/* 分类导航 */
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
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
  color: #606266;
  font-weight: 500;
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

/* 二级分类 */
.sub-category-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 10px 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #eef2f6;
  flex-wrap: wrap;
}

.sub-category-title {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.sub-category-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.sub-category-item {
  padding: 4px 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
  color: #606266;
  font-size: 12px;
  border: 1px solid #eef2f6;
}

.sub-category-item:hover {
  border-color: #409EFF;
  color: #409EFF;
  background: #ecf5ff;
}

.sub-category-item.active {
  background: #409EFF;
  color: white;
  border: none;
}

/* 排序栏 */
.sort-bar {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f6;
}

.sort-item {
  cursor: pointer;
  color: #909399;
  font-size: 13px;
  transition: color 0.3s;
}

.sort-item:hover,
.sort-item.active {
  color: #409EFF;
}

/* 商品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
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
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #e0e0e0;
}

.product-image {
  position: relative;
  height: 200px;
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

.stock-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #909399;
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 11px;
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
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

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.sales {
  font-size: 11px;
  color: #c0c4cc;
}

.stock {
  font-size: 11px;
  color: #c0c4cc;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stock-warning {
  color: #e6a23c;
}

.stock-out {
  color: #f56c6c;
}

/* 图标按钮样式 */
.add-cart-icon {
  width: 32px;
  height: 32px;
  padding: 0;
  font-size: 14px;
  transition: all 0.2s;
}

.add-cart-icon:hover {
  transform: scale(1.1);
}

.add-cart-icon.btn-removed {
  animation: shake 0.3s ease;
}

@keyframes shake {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(0.9); }
}

/* 圆形悬浮购物车 */
.floating-cart {
  position: fixed;
  bottom: 100px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s;
}

.floating-cart:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.floating-cart.cart-bounce {
  animation: bounce 0.5s ease;
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

.cart-icon-wrapper {
  position: relative;
}

.cart-icon-wrapper i {
  font-size: 24px;
  color: white;
}

.cart-badge {
  position: absolute;
  top: -10px;
  right: -16px;
  background: #f56c6c;
  color: white;
  font-size: 10px;
  font-weight: bold;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.cart-total {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 2px;
}

/* 飞行动画元素 */
.flying-item {
  position: fixed;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  z-index: 1001;
  pointer-events: none;
  animation: flyToCart 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

.flying-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

@keyframes flyToCart {
  0% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
  50% {
    transform: scale(0.8) rotate(15deg);
    opacity: 0.9;
  }
  100% {
    transform: translate(var(--end-x), var(--end-y)) scale(0.3) rotate(30deg);
    opacity: 0;
  }
}

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

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .shop-content {
    padding: 20px 0 40px;
  }

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
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
  }

  .product-image {
    height: 160px;
  }

  .product-info h4 {
    font-size: 13px;
  }

  .current-price {
    font-size: 14px;
  }

  .floating-cart {
    bottom: 70px;
    right: 15px;
    width: 50px;
    height: 50px;
  }

  .cart-icon-wrapper i {
    font-size: 20px;
  }

  .cart-badge {
    top: -8px;
    right: -14px;
    font-size: 9px;
    min-width: 16px;
    height: 16px;
  }

  .cart-total {
    font-size: 9px;
  }

  .add-cart-icon {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }

  .footer-left {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>