<template>
  <div class="cart-container">
    <Navbar />

    <div class="cart-content">
      <div class="container">
        <!-- 顶部导航 -->
        <div class="top-nav">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/shop' }">宠物商城</el-breadcrumb-item>
            <el-breadcrumb-item>购物车</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" class="back-btn">返回</el-button>
        </div>

        <h2 class="page-title">购物车</h2>

        <div class="cart-main" v-loading="loading">
          <!-- 空购物车 -->
          <div class="empty-cart" v-if="cartList.length === 0">
            <i class="el-icon-shopping-cart-2"></i>
            <p>购物车还是空的</p>
            <el-button type="primary" @click="$router.push('/shop')">去逛逛</el-button>
          </div>

          <!-- 购物车列表 -->
          <div class="cart-list" v-else>
            <div class="cart-header">
              <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
              <span>商品信息</span>
              <span>单价</span>
              <span>数量</span>
              <span>小计</span>
              <span>操作</span>
            </div>

            <div class="cart-item" v-for="item in cartList" :key="item.id">
              <el-checkbox v-model="item.selected" @change="() => handleSelectChange(item)"></el-checkbox>
              <div class="item-info" @click="goToDetail(item.productId)">
                <img :src="item.productImage" :alt="item.productName">
                <div class="item-detail">
                  <h4>{{ item.productName }}</h4>
                </div>
              </div>
              <div class="item-price">¥{{ item.price }}</div>
              <div class="item-quantity">
                <el-input-number
                    v-model="item.quantity"
                    :min="1"
                    :max="item.maxStock || 99"
                    size="small"
                    @change="() => updateQuantity(item)"
                ></el-input-number>
                <!-- 库存不足提示 -->
                <div class="stock-warning" v-if="item.quantity > (item.stock || 99)">
                  <i class="el-icon-warning"></i> 库存不足，最多可购 {{ item.stock }} 件
                </div>
              </div>
              <div class="item-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              <div class="item-actions">
                <el-button type="text" @click="deleteItem(item.id)">删除</el-button>
              </div>
            </div>
          </div>

          <!-- 购物车底部 -->
          <div class="cart-footer" v-if="cartList.length > 0">
            <div class="footer-left">
              <el-button type="text" @click="deleteSelected">删除选中</el-button>
            </div>
            <div class="footer-right">
              <div class="total-info">
                <span>已选商品 <strong>{{ selectedCount }}</strong> 件</span>
                <span class="total-amount">合计：¥{{ totalAmount.toFixed(2) }}</span>
              </div>
              <el-button
                  type="primary"
                  size="large"
                  class="checkout-btn"
                  :disabled="selectedCount === 0 || hasOutOfStock"
                  @click="goToCheckout"
              >
                去结算
              </el-button>
            </div>
            <!-- 库存不足提示 -->
            <div class="stock-error-tip" v-if="hasOutOfStock">
              <i class="el-icon-warning"></i>
              部分商品库存不足，请调整数量后再结算
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
import { getCartList, updateCartQuantity, updateCartSelected, deleteCartItem, deleteSelected } from '@/api/cart';
import { getProductById } from '@/api/product';

export default {
  name: 'Cart',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      cartList: []
    };
  },
  computed: {
    selectAll: {
      get() {
        if (this.cartList.length === 0) return false;
        return this.cartList.every(item => item.selected === true);
      },
      set(val) {
        this.handleSelectAll(val);
      }
    },
    selectedCount() {
      return this.cartList.filter(item => item.selected === true).reduce((sum, item) => sum + item.quantity, 0);
    },
    totalAmount() {
      return this.cartList.filter(item => item.selected === true).reduce((sum, item) => sum + item.price * item.quantity, 0);
    },
    // ✅ 检查是否有商品超出库存或库存不足
    hasOutOfStock() {
      return this.cartList.some(item => {
        if (!item.selected) return false;
        // 如果商品库存小于购买数量，返回 true
        return item.stock !== undefined && item.quantity > item.stock;
      });
    }
  },
  created() {
    this.loadCart();
  },
  methods: {
    async loadCart() {
      this.loading = true;
      try {
        const res = await getCartList();
        if (res.code === 200) {
          const cartData = res.data.map(item => ({
            ...item,
            selected: item.selected === 1
          }));

          // ✅ 获取每个商品的实时库存
          for (let item of cartData) {
            try {
              const productRes = await getProductById(item.productId);
              if (productRes.code === 200) {
                item.stock = productRes.data.stock || 0;
                item.maxStock = item.stock;  // 用于限制输入框最大值

                // 如果购物车数量超过库存，自动调整为库存数量
                if (item.quantity > item.stock && item.stock > 0) {
                  item.quantity = item.stock;
                  await updateCartQuantity(item.id, item.quantity);
                }
              }
            } catch (err) {
              console.error('获取商品库存失败', err);
            }
          }

          this.cartList = cartData;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async updateQuantity(item) {
      // ✅ 库存检查
      if (item.stock !== undefined && item.quantity > item.stock) {
        this.$message.warning(`商品"${item.productName}"库存不足，最多可购 ${item.stock} 件`);
        item.quantity = item.stock;
        return;
      }

      if (item.quantity < 1) {
        item.quantity = 1;
        return;
      }

      try {
        await updateCartQuantity(item.id, item.quantity);
        this.$bus.$emit('cart-updated');
        // this.$message.success('数量已更新');
      } catch (error) {
        this.$message.error('更新失败');
        this.loadCart();
      }
    },

    async handleSelectChange(item) {
      try {
        const selectedValue = item.selected ? 1 : 0;
        await updateCartSelected(item.id, selectedValue);
        this.$bus.$emit('cart-updated');
      } catch (error) {
        this.$message.error('更新失败');
        this.loadCart();
      }
    },

    async handleSelectAll(selected) {
      try {
        const promises = this.cartList.map(item => {
          item.selected = selected;
          const selectedValue = selected ? 1 : 0;
          return updateCartSelected(item.id, selectedValue);
        });
        await Promise.all(promises);
        this.$bus.$emit('cart-updated');
      } catch (error) {
        this.$message.error('更新失败');
        this.loadCart();
      }
    },

    async deleteItem(id) {
      this.$confirm('确定要删除该商品吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          await deleteCartItem(id);
          this.$message.success('删除成功');
          this.loadCart();
          this.$bus.$emit('cart-updated');
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    async deleteSelected() {
      if (this.selectedCount === 0) {
        this.$message.warning('请先选择商品');
        return;
      }
      this.$confirm('确定要删除选中的商品吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          await deleteSelected();
          this.$message.success('删除成功');
          this.loadCart();
          this.$bus.$emit('cart-updated');
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    goToDetail(id) {
      this.$router.push(`/product/${id}`);
    },

    goToCheckout() {
      // ✅ 结算前再次检查库存
      const outOfStockItems = this.cartList.filter(item => {
        return item.selected && item.stock !== undefined && item.quantity > item.stock;
      });

      if (outOfStockItems.length > 0) {
        const names = outOfStockItems.map(i => i.productName).join('、');
        this.$message.error(`商品【${names}】库存不足，请调整数量后再结算`);
        return;
      }

      const selectedItems = this.cartList.filter(item => item.selected === true);
      if (selectedItems.length === 0) {
        this.$message.warning('请先选择商品');
        return;
      }

      localStorage.setItem('checkoutItems', JSON.stringify(selectedItems));
      this.$router.push('/checkout');
    }
  }
};
</script>

<style scoped>
.cart-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.cart-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航 */
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

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 24px;
}

/* 空购物车 */
.empty-cart {
  text-align: center;
  padding: 80px;
  background: white;
  border-radius: 12px;
  border: 1px solid #eef2f6;
}

.empty-cart i {
  font-size: 80px;
  color: #c0c4cc;
  margin-bottom: 20px;
}

.empty-cart p {
  font-size: 16px;
  color: #909399;
  margin-bottom: 20px;
}

.empty-cart .el-button {
  background: #409EFF;
  border: none;
  border-radius: 8px;
}

.empty-cart .el-button:hover {
  background: #66b1ff;
}

/* 购物车表头 */
.cart-header {
  display: grid;
  grid-template-columns: 50px 1fr 100px 120px 100px 80px;
  background: #f8f9fc;
  padding: 14px 20px;
  border-radius: 8px;
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #eef2f6;
}

/* 购物车商品项 */
.cart-item {
  display: grid;
  grid-template-columns: 50px 1fr 100px 120px 100px 80px;
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 12px;
  align-items: center;
  border: 1px solid #eef2f6;
  transition: all 0.3s;
  position: relative;
}

.cart-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border-color: #e0e0e0;
}

/* 商品信息 */
.item-info {
  display: flex;
  gap: 16px;
  cursor: pointer;
}

.item-info img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f7fa;
}

.item-detail h4 {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #2c3e50;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 价格 */
.item-price, .item-total {
  color: #f56c6c;
  font-weight: 500;
  font-size: 15px;
}

/* 数量区域 */
.item-quantity {
  text-align: center;
}

.item-quantity ::v-deep .el-input-number {
  width: 100px;
}

.item-quantity ::v-deep .el-input-number__decrease,
.item-quantity ::v-deep .el-input-number__increase {
  background: #f5f7fa;
  border-color: #eef2f6;
}

/* 库存不足警告 */
.stock-warning {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  color: #f56c6c;
  background: #fef0f0;
  padding: 2px 8px;
  border-radius: 12px;
  white-space: nowrap;
}

.stock-warning i {
  margin-right: 4px;
}

/* 删除按钮 */
.item-actions .el-button {
  color: #909399;
  font-size: 13px;
}

.item-actions .el-button:hover {
  color: #f56c6c;
}

/* 购物车底部 */
.cart-footer {
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  border: 1px solid #eef2f6;
}

.footer-left .el-button {
  color: #909399;
}

.footer-left .el-button:hover {
  color: #f56c6c;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-info {
  text-align: right;
}

.total-info span {
  font-size: 14px;
  color: #606266;
}

.total-info strong {
  font-size: 16px;
  color: #f56c6c;
  margin: 0 4px;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
  margin-left: 16px;
}

/* 库存错误提示 */
.stock-error-tip {
  width: 100%;
  margin-top: 12px;
  padding: 8px 12px;
  background: #fef0f0;
  border-radius: 8px;
  color: #f56c6c;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.stock-error-tip i {
  font-size: 14px;
}

/* 结算按钮 */
.checkout-btn {
  background: #409EFF;
  border: none;
  padding: 10px 32px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s;
}

.checkout-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

.checkout-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
  transform: none;
}

/* 复选框样式优化 */
::v-deep .el-checkbox__inner {
  border-radius: 3px;
  border-color: #dcdfe6;
}

::v-deep .el-checkbox__inner:hover {
  border-color: #409EFF;
}

::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #409EFF;
  border-color: #409EFF;
}

/* 响应式 */
@media (max-width: 768px) {
  .cart-content {
    padding: 20px 0 40px;
  }

  .page-title {
    font-size: 20px;
    margin-bottom: 16px;
  }

  .cart-header, .cart-item {
    grid-template-columns: 40px 1fr 80px;
    gap: 12px;
  }

  .cart-header span:nth-child(3),
  .cart-header span:nth-child(4),
  .cart-header span:nth-child(5),
  .cart-item .item-price,
  .cart-item .item-quantity,
  .cart-item .item-total {
    display: none;
  }

  .item-info img {
    width: 60px;
    height: 60px;
  }

  .item-detail h4 {
    font-size: 13px;
  }

  .cart-footer {
    flex-direction: column;
    gap: 16px;
  }

  .footer-right {
    width: 100%;
    justify-content: space-between;
  }

  .checkout-btn {
    padding: 10px 24px;
  }

  .empty-cart {
    padding: 40px;
  }

  .empty-cart i {
    font-size: 60px;
  }

  .stock-warning {
    position: static;
    transform: none;
    margin-top: 8px;
    white-space: normal;
  }
}

@media (max-width: 480px) {
  .footer-right {
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;
  }

  .total-info {
    text-align: center;
    width: 100%;
  }

  .checkout-btn {
    width: 100%;
  }
}
</style>