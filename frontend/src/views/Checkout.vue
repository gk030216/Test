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
              <el-button type="text" @click="showAddressDialog = true">+ 新增地址</el-button>
            </div>
            <div class="address-list">
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
              </div>
              <div class="add-address" @click="showAddressDialog = true">
                <i class="el-icon-plus"></i>
                <span>新增地址</span>
              </div>
            </div>
          </div>

          <!-- 商品列表 -->
          <div class="product-section">
            <div class="section-header">
              <span>商品清单</span>
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
            <div class="info-row">
              <span>运费</span>
              <span>¥0.00</span>
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
                :disabled="!selectedAddressId || checkoutItems.length === 0"
                @click="createOrder"
                :loading="submitting"
            >
              提交订单
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增地址对话框 -->
    <el-dialog title="新增地址" :visible.sync="showAddressDialog" width="500px">
      <el-form :model="newAddress" :rules="addressRules" ref="addressForm" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="newAddress.receiverName" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="newAddress.receiverPhone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="地区" prop="province">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="newAddress.province" placeholder="省"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="newAddress.city" placeholder="市"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="newAddress.district" placeholder="区/县"></el-input>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="newAddress.detailAddress" type="textarea" rows="2" placeholder="街道、门牌号等"></el-input>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="newAddress.isDefault" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAddress" :loading="addressSubmitting">确定</el-button>
      </span>
    </el-dialog>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getAddressList, addAddress } from '@/api/address';
import { createOrder } from '@/api/order';
import { getCartList } from '@/api/cart';

export default {
  name: 'Checkout',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      submitting: false,
      addressSubmitting: false,
      addressList: [],
      selectedAddressId: null,
      checkoutItems: [],
      showAddressDialog: false,
      newAddress: {
        receiverName: '',
        receiverPhone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: 0
      },
      addressRules: {
        receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
        receiverPhone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
        city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
        district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
        detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
      }
    };
  },
  computed: {
    totalAmount() {
      return this.checkoutItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
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
          // 从立即购买来的数据
          this.checkoutItems = JSON.parse(items);
        } else {
          // 从购物车来的数据 - 获取购物车中选中的商品
          const cartRes = await getCartList();
          if (cartRes.code === 200) {
            // 筛选出选中的商品 (selected === 1)
            this.checkoutItems = cartRes.data.filter(item => item.selected === 1);
          }
        }

        if (this.checkoutItems.length === 0) {
          this.$message.warning('请选择要购买的商品');
          this.$router.push('/cart');
          return;
        }

        // 获取地址列表
        const addressRes = await getAddressList();
        if (addressRes.code === 200) {
          this.addressList = addressRes.data;
          const defaultAddr = this.addressList.find(addr => addr.isDefault === 1);
          if (defaultAddr) {
            this.selectedAddressId = defaultAddr.id;
          }
        }
      } catch (error) {
        console.error('加载数据失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async submitAddress() {
      this.$refs.addressForm.validate(async (valid) => {
        if (!valid) return;
        this.addressSubmitting = true;
        try {
          const res = await addAddress(this.newAddress);
          if (res.code === 200) {
            this.$message.success('添加成功');
            this.showAddressDialog = false;
            // 重新加载地址列表
            const addressRes = await getAddressList();
            if (addressRes.code === 200) {
              this.addressList = addressRes.data;
              const defaultAddr = this.addressList.find(addr => addr.isDefault === 1);
              if (defaultAddr) {
                this.selectedAddressId = defaultAddr.id;
              }
            }
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('添加失败');
        } finally {
          this.addressSubmitting = false;
        }
      });
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
        // 构建订单商品数据
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
          // 清除localStorage中的结算商品
          localStorage.removeItem('checkoutItems');
          // 跳转到支付页面
          this.$router.push(`/pay/${res.data.orderNo}`);
        } else {
          this.$message.error(res.message);
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

.address-section, .product-section, .order-info {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: 500;
}

.address-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.address-item {
  width: 280px;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102,126,234,0.1);
}

.address-item.active {
  border-color: #667eea;
  background: #f8f9ff;
}

.address-name {
  margin-bottom: 8px;
  font-weight: 500;
}

.address-name .phone {
  margin-left: 12px;
  color: #999;
  font-weight: normal;
  font-size: 12px;
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

.add-address {
  width: 280px;
  height: 100px;
  border: 1px dashed #e0e0e0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.add-address:hover {
  border-color: #667eea;
  color: #667eea;
}

.add-address i {
  font-size: 24px;
  margin-bottom: 8px;
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

.total-row {
  padding-top: 15px;
  margin-top: 10px;
  border-top: 1px solid #e0e0e0;
  font-size: 18px;
  font-weight: bold;
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
}

@media (max-width: 768px) {
  .address-item, .add-address {
    width: 100%;
  }

  .product-item {
    flex-wrap: wrap;
  }

  .product-quantity, .product-total {
    width: auto;
    margin-top: 10px;
  }
}
</style>