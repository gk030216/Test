<template>
  <div class="checkout-container">
    <Navbar />

    <div class="checkout-content">
      <div class="container">
        <h2 class="page-title">订单确认</h2>

        <div class="checkout-main" v-loading="loading">
          <!-- 收货地址 -->
          <div class="address-section">
            <div class="section-header">
              <span>收货地址</span>
              <el-button type="text" @click="goToAddressManage">
                <i class="el-icon-setting"></i> 管理地址
              </el-button>
            </div>

            <!-- 有地址时显示地址列表 -->
            <div class="address-list" v-if="addressList.length > 0">
              <div
                  v-for="addr in addressList"
                  :key="addr.id"
                  :class="['address-item', { active: selectedAddressId === addr.id }]"
                  @click="selectedAddressId = addr.id"
              >
                <div class="address-info">
                  <div class="address-name">
                    <span>{{ addr.receiverName }}</span>
                    <span class="phone">{{ addr.receiverPhone }}</span>
                    <span class="default-tag" v-if="addr.isDefault === 1">默认</span>
                  </div>
                  <div class="address-detail">
                    {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
                  </div>
                </div>
                <i class="el-icon-check" v-if="selectedAddressId === addr.id"></i>
              </div>
            </div>

            <!-- 无地址时显示提示 -->
            <div class="no-address" v-else>
              <i class="el-icon-location-outline"></i>
              <p>还没有收货地址</p>
              <el-button type="primary" size="small" @click="goToAddressManage">去添加地址</el-button>
            </div>
          </div>

          <!-- 商品列表 -->
          <div class="product-section">
            <div class="section-header">
              <span>商品清单</span>
              <span class="item-count">共 {{ checkoutItems.length }} 件商品</span>
            </div>
            <div class="product-list">
              <div class="product-item" v-for="item in checkoutItems" :key="item.productId">
                <img :src="item.productImage" :alt="item.productName">
                <div class="product-info">
                  <h4>{{ item.productName }}</h4>
                  <div class="product-price">¥{{ item.price }}</div>
                </div>
                <div class="product-quantity">x{{ item.quantity }}</div>
                <div class="product-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              </div>
            </div>
          </div>

          <!-- 订单信息 -->
          <div class="order-info">
            <div class="info-row">
              <span>商品总价</span>
              <span>¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="info-row total-row">
              <span>实付金额</span>
              <span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="submit-section">
            <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :disabled="!canSubmit"
                @click="createOrder"
                :loading="submitting"
            >
              {{ submitBtnText }}
            </el-button>
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
import { getAddressList } from '@/api/address';
import { createOrder } from '@/api/order';
import { getCartList } from '@/api/cart';

export default {
  name: 'Checkout',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      submitting: false,
      addressList: [],
      selectedAddressId: null,
      checkoutItems: []
    };
  },
  computed: {
    totalAmount() {
      return this.checkoutItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
    },
    canSubmit() {
      return this.selectedAddressId && this.checkoutItems.length > 0;
    },
    submitBtnText() {
      if (this.addressList.length === 0) return '请先添加收货地址';
      if (!this.selectedAddressId) return '请选择收货地址';
      if (this.checkoutItems.length === 0) return '请选择商品';
      return '提交订单';
    }
  },
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        // 获取结算商品
        const items = localStorage.getItem('checkoutItems');
        if (items) {
          this.checkoutItems = JSON.parse(items);
        } else {
          const cartRes = await getCartList();
          if (cartRes.code === 200) {
            this.checkoutItems = cartRes.data.filter(item => item.selected === 1);
          }
        }

        if (this.checkoutItems.length === 0) {
          this.$message.warning('请选择要购买的商品');
          this.$router.push('/cart');
          return;
        }

        // 获取地址列表
        await this.loadAddressList();
      } catch (error) {
        console.error('加载数据失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadAddressList() {
      try {
        const res = await getAddressList();
        if (res.code === 200) {
          this.addressList = res.data || [];
          // 自动选中默认地址
          const defaultAddr = this.addressList.find(addr => addr.isDefault === 1);
          if (defaultAddr) {
            this.selectedAddressId = defaultAddr.id;
          } else if (this.addressList.length > 0) {
            this.selectedAddressId = this.addressList[0].id;
          }
        }
      } catch (error) {
        console.error('加载地址失败', error);
      }
    },

    goToAddressManage() {
      // 保存当前状态，添加地址后返回
      this.$router.push('/personal/address');
    },

    async createOrder() {
      if (!this.selectedAddressId) {
        this.$message.warning('请选择收货地址');
        return;
      }

      if (this.checkoutItems.length === 0) {
        this.$message.warning('请选择要购买的商品');
        return;
      }

      this.submitting = true;
      try {
        const orderItems = this.checkoutItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity
        }));

        const res = await createOrder({
          addressId: this.selectedAddressId,
          items: orderItems,
          remark: ''
        });

        if (res.code === 200) {
          localStorage.removeItem('checkoutItems');
          this.$message.success('订单创建成功，即将跳转支付');
          this.$router.push(`/pay/${res.data.orderNo}`);
        } else {
          this.$message.error(res.message || '创建订单失败');
        }
      } catch (error) {
        console.error('创建订单失败', error);
        this.$message.error(error.message || '创建订单失败');
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
.checkout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.checkout-content {
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

.address-section,
.product-section,
.order-info {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: 500;
  font-size: 16px;
}

.item-count {
  font-size: 13px;
  color: #999;
  font-weight: normal;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.address-item.active {
  border-color: #667eea;
  background: #f8f9ff;
}

.address-item .el-icon-check {
  color: #667eea;
  font-size: 20px;
  font-weight: bold;
}

.address-info {
  flex: 1;
}

.address-name {
  margin-bottom: 8px;
  font-weight: 500;
}

.address-name .phone {
  margin-left: 12px;
  color: #999;
  font-weight: normal;
  font-size: 13px;
}

.default-tag {
  margin-left: 12px;
  background: #667eea;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
}

.address-detail {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.no-address {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.no-address i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #ddd;
}

.no-address p {
  margin-bottom: 20px;
  font-size: 14px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.product-item:last-child {
  border-bottom: none;
}

.product-item img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 15px;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.product-price {
  color: #999;
  font-size: 13px;
}

.product-quantity {
  width: 80px;
  text-align: center;
  color: #666;
}

.product-total {
  width: 100px;
  text-align: right;
  color: #ff6b6b;
  font-weight: 500;
}

.order-info {
  background: #f8f9fc;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  color: #666;
}

.free-shipping {
  color: #67c23a;
}

.total-row {
  padding-top: 15px;
  margin-top: 10px;
  border-top: 1px solid #e0e0e0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.total-price {
  color: #ff6b6b;
  font-size: 24px;
}

.submit-section {
  text-align: right;
  margin-top: 20px;
}

.submit-btn {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  border: none;
  padding: 14px 60px;
  font-size: 18px;
  border-radius: 40px;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .address-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .address-item .el-icon-check {
    align-self: flex-end;
  }

  .product-item {
    flex-wrap: wrap;
  }

  .product-quantity,
  .product-total {
    width: auto;
    margin-top: 10px;
  }

  .submit-btn {
    width: 100%;
  }
}
</style>