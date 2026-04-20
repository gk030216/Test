<template>
  <div class="pay-return">
    <Navbar />

    <div class="return-content">
      <div class="container">
        <div class="return-card" v-loading="loading">
          <div v-if="loading" class="loading-state">
            <i class="el-icon-loading"></i>
            <p>正在验证支付结果...</p>
            <p class="loading-tip">请稍候，正在确认中</p>
          </div>

          <div v-else-if="success" class="success-state">
            <i class="el-icon-success"></i>
            <h2>支付成功</h2>
            <p>{{ successMessage }}</p>
            <div class="order-info" v-if="orderInfo">
              <div class="info-item">
                <span>{{ orderType === 'service' ? '预约编号：' : '订单号：' }}</span>
                <span>{{ orderInfo.appointmentNo || orderInfo.orderNo }}</span>
              </div>
              <div class="info-item">
                <span>支付金额：</span>
                <span class="amount">¥{{ orderInfo.servicePrice || orderInfo.payAmount }}</span>
              </div>
              <div class="info-item">
                <span>支付时间：</span>
                <span>{{ orderInfo.payTime ? formatDateTime(orderInfo.payTime) : '刚刚' }}</span>
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
            <h2>支付处理中</h2>
            <p>{{ errorMessage || '支付已提交，正在处理中，请稍后查看订单状态' }}</p>
            <div class="order-info" v-if="orderInfo">
              <div class="info-item">
                <span>{{ orderType === 'service' ? '预约编号：' : '订单号：' }}</span>
                <span>{{ orderInfo.appointmentNo || orderInfo.orderNo }}</span>
              </div>
              <div class="info-item">
                <span>支付金额：</span>
                <span class="amount">¥{{ orderInfo.servicePrice || orderInfo.payAmount }}</span>
              </div>
            </div>
            <div class="actions">
              <el-button type="primary" @click="retryQuery" :loading="retrying">
                <i class="el-icon-refresh"></i> 重新查询
              </el-button>
              <el-button @click="goToOrders">
                {{ orderType === 'service' ? '查看预约' : '查看订单' }}
              </el-button>
            </div>
            <div class="tip-text">
              <i class="el-icon-info"></i>
              如果长时间未更新，请联系客服处理
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
import { getOrderDetail } from '@/api/order';
import { getAppointmentDetailByNo } from '@/api/service';

export default {
  name: 'PayReturn',
  components: { Navbar, Footer },
  data() {
    return {
      loading: true,
      retrying: false,
      success: false,
      errorMessage: '',
      orderInfo: null,
      orderType: 'product',
      successMessage: '',
      orderNo: '',
      tradeNo: '',
      retryCount: 0,
      maxRetry: 8,
      retryTimer: null
    };
  },
  created() {
    this.checkPayResult();
  },
  beforeDestroy() {
    if (this.retryTimer) {
      clearTimeout(this.retryTimer);
    }
  },
  methods: {
    async checkPayResult() {
      console.log('========== 支付返回页面 ==========');

      const urlParams = new URLSearchParams(window.location.search);
      this.orderNo = urlParams.get('out_trade_no');
      this.tradeNo = urlParams.get('trade_no');

      console.log('解析到的订单号:', this.orderNo);
      console.log('解析到的交易号:', this.tradeNo);

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

      // 保存订单号到 localStorage，供后续查询使用
      localStorage.setItem('lastOrderNo', this.orderNo);

      // 判断订单类型
      if (this.orderNo.startsWith('AP')) {
        this.orderType = 'service';
        this.successMessage = '您的预约已支付成功，请等待商家确认';
        await this.checkAppointmentResultWithRetry();
      } else {
        this.orderType = 'product';
        this.successMessage = '您的订单已支付成功，我们将尽快为您发货';
        await this.checkOrderResultWithRetry();
      }
    },

    async checkAppointmentResultWithRetry() {
      this.retryCount = 0;
      await this.doCheckAppointmentResult();
    },

    async doCheckAppointmentResult() {
      try {
        console.log(`查询服务预约 (第${this.retryCount + 1}次):`, this.orderNo);
        const res = await getAppointmentDetailByNo(this.orderNo);
        console.log('预约详情响应:', res);

        if (res.code === 200 && res.data) {
          this.orderInfo = res.data;

          // 检查支付状态
          if (res.data.payStatus === 1) {
            this.success = true;
            this.loading = false;
            localStorage.removeItem('lastOrderNo');
            console.log('✅ 预约支付成功！');
            return;
          }

          // 检查预约状态是否为已取消或已拒绝
          if (res.data.status === 4 || res.data.status === 5) {
            this.loading = false;
            this.success = false;
            this.errorMessage = res.data.status === 4 ? '预约已取消' : '预约已被拒绝';
            console.log('❌ 预约状态异常:', res.data.status);
            return;
          }
        }

        // 未支付成功，尝试重试
        this.retryCount++;
        if (this.retryCount < this.maxRetry) {
          const delay = Math.min(1000 * Math.pow(1.5, this.retryCount), 5000);
          console.log(`支付状态未更新，${delay}ms后重试...`);
          this.retryTimer = setTimeout(() => {
            this.doCheckAppointmentResult();
          }, delay);
        } else {
          // 重试次数用尽，显示处理中状态
          this.loading = false;
          this.success = false;
          this.errorMessage = '支付处理中，请稍后查看订单状态';
          console.log('重试次数用尽，请稍后查看');
        }
      } catch (error) {
        console.error('查询预约异常:', error);
        this.retryCount++;
        if (this.retryCount < this.maxRetry) {
          const delay = Math.min(1000 * Math.pow(1.5, this.retryCount), 5000);
          this.retryTimer = setTimeout(() => {
            this.doCheckAppointmentResult();
          }, delay);
        } else {
          this.loading = false;
          this.success = false;
          this.errorMessage = '查询支付状态失败，请稍后查看订单状态';
        }
      }
    },

    async checkOrderResultWithRetry() {
      this.retryCount = 0;
      await this.doCheckOrderResult();
    },

    async doCheckOrderResult() {
      try {
        console.log(`查询商品订单 (第${this.retryCount + 1}次):`, this.orderNo);
        const res = await getOrderDetail(this.orderNo);
        console.log('订单详情响应:', res);

        if (res.code === 200 && res.data) {
          this.orderInfo = res.data;

          if (res.data.payStatus === 1) {
            this.success = true;
            this.loading = false;
            localStorage.removeItem('lastOrderNo');
            console.log('✅ 订单支付成功！');
            return;
          }

          if (res.data.orderStatus === 4) {
            this.loading = false;
            this.success = false;
            this.errorMessage = '订单已取消';
            return;
          }
        }

        this.retryCount++;
        if (this.retryCount < this.maxRetry) {
          const delay = Math.min(1000 * Math.pow(1.5, this.retryCount), 5000);
          console.log(`支付状态未更新，${delay}ms后重试...`);
          this.retryTimer = setTimeout(() => {
            this.doCheckOrderResult();
          }, delay);
        } else {
          this.loading = false;
          this.success = false;
          this.errorMessage = '支付处理中，请稍后查看订单状态';
        }
      } catch (error) {
        console.error('查询订单异常:', error);
        this.retryCount++;
        if (this.retryCount < this.maxRetry) {
          const delay = Math.min(1000 * Math.pow(1.5, this.retryCount), 5000);
          this.retryTimer = setTimeout(() => {
            this.doCheckOrderResult();
          }, delay);
        } else {
          this.loading = false;
          this.success = false;
          this.errorMessage = '查询支付状态失败，请稍后查看订单状态';
        }
      }
    },

    async retryQuery() {
      this.retrying = true;
      this.loading = true;
      this.success = false;
      this.retryCount = 0;

      if (this.retryTimer) {
        clearTimeout(this.retryTimer);
      }

      try {
        if (this.orderType === 'service') {
          const res = await getAppointmentDetailByNo(this.orderNo);
          if (res.code === 200 && res.data) {
            this.orderInfo = res.data;
            if (res.data.payStatus === 1) {
              this.success = true;
              this.loading = false;
              localStorage.removeItem('lastOrderNo');
              this.$message.success('支付成功！');
              this.retrying = false;
              return;
            }
          }
        } else {
          const res = await getOrderDetail(this.orderNo);
          if (res.code === 200 && res.data) {
            this.orderInfo = res.data;
            if (res.data.payStatus === 1) {
              this.success = true;
              this.loading = false;
              localStorage.removeItem('lastOrderNo');
              this.$message.success('支付成功！');
              this.retrying = false;
              return;
            }
          }
        }

        this.loading = false;
        this.success = false;
        this.errorMessage = '支付状态未更新，请稍后查看';
        this.$message.warning('支付状态未更新，请稍后在订单列表中查看');
      } catch (error) {
        this.loading = false;
        this.success = false;
        this.errorMessage = '查询失败，请稍后重试';
      } finally {
        this.retrying = false;
      }
    },

    formatDateTime(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`;
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
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
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

.loading-tip {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
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

.tip-text {
  margin-top: 20px;
  font-size: 13px;
  color: #999;
}

.tip-text i {
  margin-right: 4px;
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