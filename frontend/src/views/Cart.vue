<template>
  <div class="cart-container">
    <Navbar />

    <div class="cart-content">
      <div class="container">
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
                    :max="99"
                    size="small"
                    @change="() => updateQuantity(item)"
                ></el-input-number>
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
                  :disabled="selectedCount === 0"
                  @click="goToCheckout"
              >
                去结算
              </el-button>
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
          // 将后端返回的 selected (0/1) 转换为布尔值
          this.cartList = res.data.map(item => ({
            ...item,
            selected: item.selected === 1
          }));
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async updateQuantity(item) {
      try {
        await updateCartQuantity(item.id, item.quantity);
        // 更新成功后触发购物车更新事件
        this.$bus.$emit('cart-updated');
      } catch (error) {
        this.$message.error('更新失败');
        this.loadCart();
      }
    },
    async handleSelectChange(item) {
      try {
        // 将布尔值转换为数字 (true=1, false=0)
        const selectedValue = item.selected ? 1 : 0;
        await updateCartSelected(item.id, selectedValue);
        // 更新成功后触发购物车更新事件
        this.$bus.$emit('cart-updated');
      } catch (error) {
        this.$message.error('更新失败');
        // 失败时重新加载购物车
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
        // 更新成功后触发购物车更新事件
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
      const selectedItems = this.cartList.filter(item => item.selected === true);
      if (selectedItems.length === 0) {
        this.$message.warning('请先选择商品');
        return;
      }
      // 存入结算商品
      localStorage.setItem('checkoutItems', JSON.stringify(selectedItems));
      // 注意：不要清除购物车数据，支付成功后才由后端删除
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
  background: #f8f9fa;
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

.page-title {
  font-size: 28px;
  margin-bottom: 30px;
  color: #333;
}

.empty-cart {
  text-align: center;
  padding: 80px;
  background: white;
  border-radius: 20px;
}

.empty-cart i {
  font-size: 80px;
  color: #ddd;
  margin-bottom: 20px;
}

.empty-cart p {
  font-size: 16px;
  color: #999;
  margin-bottom: 20px;
}

.cart-header {
  display: grid;
  grid-template-columns: 50px 1fr 100px 120px 100px 80px;
  background: #f8f9fc;
  padding: 15px 20px;
  border-radius: 12px;
  margin-bottom: 15px;
  color: #666;
  font-size: 14px;
}

.cart-item {
  display: grid;
  grid-template-columns: 50px 1fr 100px 120px 100px 80px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 15px;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.item-info {
  display: flex;
  gap: 15px;
  cursor: pointer;
}

.item-info img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.item-detail h4 {
  font-size: 14px;
  margin-bottom: 8px;
  color: #333;
}

.item-price, .item-total {
  color: #ff6b6b;
  font-weight: 500;
}

.item-quantity {
  text-align: center;
}

.item-actions .el-button {
  color: #999;
}

.cart-footer {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.total-info {
  text-align: right;
}

.total-info strong {
  font-size: 18px;
  color: #ff6b6b;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
  margin-left: 20px;
}

.checkout-btn {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  border: none;
  padding: 12px 40px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .cart-header, .cart-item {
    grid-template-columns: 40px 1fr 80px;
    gap: 10px;
  }

  .cart-header span:nth-child(3),
  .cart-header span:nth-child(4),
  .cart-header span:nth-child(5),
  .cart-item .item-price,
  .cart-item .item-quantity,
  .cart-item .item-total {
    display: none;
  }

  .cart-footer {
    flex-direction: column;
    gap: 15px;
  }
}
</style>