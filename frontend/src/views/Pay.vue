<template>
  <div class="pay-container">
    <Navbar />

    <div class="pay-content">
      <div class="container">
        <div class="pay-card" v-loading="loading">
          <div class="pay-header">
            <i class="el-icon-success" v-if="payStatus === 'success'"></i>
            <i class="el-icon-warning" v-else-if="payStatus === 'failed'"></i>
            <i class="el-icon-time" v-else></i>
            <h2>{{ payTitle }}</h2>
            <p>{{ payMessage }}</p>
          </div>

          <div class="pay-info" v-if="order">
            <div class="info-item">
              <span>订单号：</span>
              <span>{{ order.orderNo }}</span>
            </div>
            <div class="info-item">
              <span>支付金额：</span>
              <span class="amount">¥{{ order.payAmount }}</span>
            </div>
          </div>

          <div class="pay-methods" v-if="payStatus === 'pending'">
            <div class="method-title">选择支付方式</div>
            <div class="method-list">
              <div
                  class="method-item"
                  :class="{ active: payType === 1 }"
                  @click="payType = 1"
              >
                <i class="el-icon-ali-pay"></i>
                <span>支付宝支付</span>
              </div>
            </div>
          </div>

          <div class="pay-actions" v-if="payStatus === 'pending'">
            <el-button
                type="primary"
                size="large"
                class="pay-btn"
                @click="handlePay"
                :loading="paying"
            >
              立即支付
            </el-button>
            <el-button size="large" @click="cancelOrder">取消订单</el-button>
          </div>

          <div class="pay-actions" v-else-if="payStatus === 'success'">
            <el-button type="primary" size="large" @click="$router.push('/orders')">
              查看订单
            </el-button>
            <el-button size="large" @click="$router.push('/shop')">
              继续购物
            </el-button>
          </div>

          <div class="pay-actions" v-else-if="payStatus === 'failed'">
            <el-button type="primary" size="large" @click="retryPay">
              重新支付
            </el-button>
            <el-button size="large" @click="$router.push('/orders')">
              查看订单
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
import { getOrderDetail, payOrder, cancelOrder } from '@/api/order';

export default {
  name: 'Pay',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      paying: false,
      order: null,
      payType: 1,
      payStatus: 'pending',
      payTitle: '订单待支付',
      payMessage: '请在30分钟内完成支付，超时订单将自动取消'
    };
  },
  computed: {
    orderNo() {
      return this.$route.params.orderNo;
    }
  },
  created() {
    this.loadOrder();
    this.checkPayResult();
  },
  methods: {
    async loadOrder() {
      this.loading = true;
      try {
        const res = await getOrderDetail(this.orderNo);
        if (res.code === 200) {
          this.order = res.data;
          if (this.order.payStatus === 1) {
            this.payStatus = 'success';
            this.payTitle = '支付成功';
            this.payMessage = '您的订单已支付成功，我们将尽快为您发货';
          } else if (this.order.orderStatus === 4) {
            this.payStatus = 'failed';
            this.payTitle = '订单已取消';
            this.payMessage = '订单已取消，如需购买请重新下单';
          }
        } else {
          this.$message.error('订单不存在');
          this.$router.push('/orders');
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async handlePay() {
      this.paying = true;
      try {
        const res = await payOrder({ orderNo: this.orderNo, payType: this.payType });

        if (res.code === 200) {
          const payForm = res.data;

          // 方法1：直接替换当前页面内容
          document.write(payForm);
          document.close();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        console.error('支付异常:', error);
        this.$message.error(error.message || '支付失败');
      } finally {
        this.paying = false;
      }
    },
    checkPayResult() {
      const urlParams = new URLSearchParams(window.location.search);
      const tradeNo = urlParams.get('trade_no');
      const outTradeNo = urlParams.get('out_trade_no');
      if (tradeNo && outTradeNo) {
        this.loadOrder();
      }
    },
    async cancelOrder() {
      this.$confirm('确定要取消该订单吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          await cancelOrder(this.orderNo);
          this.$message.success('订单已取消');
          this.loadOrder();
        } catch (error) {
          this.$message.error('取消失败');
        }
      }).catch(() => {});
    },
    retryPay() {
      this.payStatus = 'pending';
      this.payTitle = '订单待支付';
      this.payMessage = '请在30分钟内完成支付，超时订单将自动取消';
    }
  }
};
</script>

<style scoped>
.pay-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.pay-content {
  flex: 1;
  padding: 60px 0;
}

.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
}

.pay-card {
  background: white;
  border-radius: 24px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
}

.pay-header i {
  font-size: 80px;
  margin-bottom: 20px;
}

.pay-header .el-icon-success {
  color: #67c23a;
}

.pay-header .el-icon-warning {
  color: #e6a23c;
}

.pay-header .el-icon-time {
  color: #409EFF;
}

.pay-header h2 {
  font-size: 24px;
  margin-bottom: 10px;
  color: #333;
}

.pay-header p {
  color: #999;
  font-size: 14px;
}

.pay-info {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 20px;
  margin: 30px 0;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.info-item .amount {
  color: #ff6b6b;
  font-size: 20px;
  font-weight: bold;
}

.pay-methods {
  margin: 30px 0;
  text-align: left;
}

.method-title {
  margin-bottom: 15px;
  font-weight: 500;
  color: #333;
}

.method-list {
  display: flex;
  gap: 15px;
}

.method-item {
  width: 120px;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.method-item i {
  font-size: 32px;
  margin-bottom: 8px;
  display: block;
}

.method-item.active {
  border-color: #667eea;
  background: #f8f9ff;
}

.pay-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 30px;
}

.pay-btn {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  border: none;
  padding: 12px 50px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .pay-card {
    padding: 30px 20px;
  }

  .method-list {
    flex-direction: column;
  }

  .method-item {
    width: 100%;
  }
}
</style>