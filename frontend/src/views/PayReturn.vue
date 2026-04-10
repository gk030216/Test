<template>
  <div class="pay-return">
    <Navbar />

    <div class="return-content">
      <div class="container">
        <div class="return-card" v-loading="loading">
          <div v-if="loading" class="loading-state">
            <i class="el-icon-loading"></i>
            <p>正在验证支付结果...</p>
          </div>

          <div v-else-if="success" class="success-state">
            <i class="el-icon-success"></i>
            <h2>支付成功</h2>
            <p>{{ successMessage }}</p>
            <div class="order-info" v-if="orderInfo">
              <div class="info-item">
                <span>{{ orderType === 'service' ? '预约编号：' : '订单号：' }}</span>
                <span>{{ orderInfo.orderNo || orderInfo.appointmentNo }}</span>
              </div>
              <div class="info-item">
                <span>支付金额：</span>
                <span class="amount">¥{{ orderInfo.payAmount || orderInfo.servicePrice }}</span>
              </div>
            </div>
            <div class="actions">
              <el-button type="primary" @click="goToOrders">
                {{ orderType === 'service' ? '查看预约' : '查看订单' }}
              </el-button>
              <el-button @click="goToHome">
                {{ orderType === 'service' ? '继续预约' : '继续购物' }}
              </el-button>
            </div>
          </div>

          <div v-else class="failed-state">
            <i class="el-icon-warning"></i>
            <h2>支付失败</h2>
            <p>{{ errorMessage || '支付遇到问题，请重试或联系客服' }}</p>
            <div class="actions">
              <el-button type="primary" @click="retryPay">重新支付</el-button>
              <el-button @click="goToOrders">查看订单</el-button>
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
import { getOrderDetail, payOrder } from '@/api/order';
import { getAppointmentDetailByNo } from '@/api/service';

export default {
  name: 'PayReturn',
  components: { Navbar, Footer },
  data() {
    return {
      loading: true,
      success: false,
      errorMessage: '',
      orderInfo: null,
      orderType: 'product',
      successMessage: '',
      orderNo: '',
      tradeNo: ''
    };
  },
  created() {
    this.checkPayResult();
  },
  methods: {
    async checkPayResult() {
      console.log('========== 支付返回页面 ==========');

      const urlParams = new URLSearchParams(window.location.search);
      this.orderNo = urlParams.get('out_trade_no');
      this.tradeNo = urlParams.get('trade_no');

      console.log('解析到的订单号:', this.orderNo);

      if (!this.orderNo) {
        this.orderNo = localStorage.getItem('lastOrderNo');
        console.log('从localStorage获取订单号:', this.orderNo);
      }

      if (!this.orderNo) {
        this.loading = false;
        this.success = false;
        this.errorMessage = '无效的支付回调，未获取到订单号';
        return;
      }

      // 判断订单类型
      if (this.orderNo.startsWith('AP')) {
        this.orderType = 'service';
        this.successMessage = '您的预约已支付成功，请等待商家确认';
        await this.checkAppointmentResult();
      } else {
        this.orderType = 'product';
        this.successMessage = '您的订单已支付成功，我们将尽快为您发货';
        await this.checkOrderResult();
      }
    },

    async checkOrderResult() {
      try {
        console.log('查询商品订单:', this.orderNo);
        const res = await getOrderDetail(this.orderNo);
        console.log('订单详情响应:', res);

        if (res.code === 200) {
          this.orderInfo = res.data;
          if (this.orderInfo.payStatus === 1) {
            this.success = true;
            localStorage.removeItem('lastOrderNo');
          } else {
            this.success = false;
            this.errorMessage = this.orderInfo.orderStatus === 4 ? '订单已取消' : '订单未支付';
          }
        } else {
          this.success = false;
          this.errorMessage = res.message || '查询订单失败';
        }
      } catch (error) {
        console.error('查询订单异常:', error);
        this.success = false;
        this.errorMessage = '查询订单失败，请稍后查看订单列表';
      } finally {
        this.loading = false;
      }
    },

    async checkAppointmentResult() {
      try {
        console.log('查询服务预约:', this.orderNo);
        const res = await getAppointmentDetailByNo(this.orderNo);
        console.log('预约详情响应:', res);

        if (res.code === 200) {
          this.orderInfo = res.data;
          if (this.orderInfo.payStatus === 1) {
            this.success = true;
            localStorage.removeItem('lastOrderNo');
          } else {
            this.success = false;
            this.errorMessage = this.orderInfo.status === 4 ? '预约已取消' : '预约未支付';
          }
        } else {
          this.success = false;
          this.errorMessage = res.message || '查询预约失败';
        }
      } catch (error) {
        console.error('查询预约异常:', error);
        this.success = false;
        this.errorMessage = '查询预约失败，请稍后查看预约列表';
      } finally {
        this.loading = false;
      }
    },

    goToOrders() {
      if (this.orderType === 'service') {
        this.$router.push('/personal/appointments');
      } else {
        this.$router.push('/personal/orders');
      }
    },

    goToHome() {
      if (this.orderType === 'service') {
        this.$router.push('/services');
      } else {
        this.$router.push('/shop');
      }
    },

    async retryPay() {
      if (!this.orderNo) return;

      // 根据类型跳转到对应的支付页面
      this.$router.push(`/pay/${this.orderNo}`);
    }
  }
};
</script>

<style scoped>
.pay-return {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.return-content {
  flex: 1;
  padding: 60px 0;
}

.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
}

.return-card {
  background: white;
  border-radius: 24px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
}

.loading-state i,
.success-state i,
.failed-state i {
  font-size: 80px;
  margin-bottom: 20px;
}

.loading-state i {
  color: #409EFF;
  animation: rotate 1s linear infinite;
}

.success-state i {
  color: #67c23a;
}

.failed-state i {
  color: #e6a23c;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.return-card h2 {
  font-size: 24px;
  margin-bottom: 10px;
  color: #333;
}

.return-card p {
  color: #666;
  margin-bottom: 30px;
}

.order-info {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 20px;
  margin: 20px 0;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.info-item .amount {
  color: #ff6b6b;
  font-size: 18px;
  font-weight: bold;
}

.actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .return-card {
    padding: 40px 20px;
  }

  .actions {
    flex-direction: column;
  }
}
</style>