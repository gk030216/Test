<template>
  <div class="pay-return">
    <Navbar />

    <div class="return-content">
      <div class="container">
        <!-- 顶部导航栏 -->
        <div class="top-nav">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/cart' }">购物车</el-breadcrumb-item>
            <el-breadcrumb-item>支付结果</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="return-card" v-loading="loading">
          <div v-if="loading" class="loading-state">
            <i class="el-icon-loading"></i>
            <p>正在验证支付结果...</p>
            <p class="loading-tip">请稍候，正在确认中</p>
          </div>

          <div v-else-if="success" class="success-state">
            <i class="el-icon-circle-check"></i>
            <h2>支付成功</h2>
            <p>{{ successMessage }}</p>
            <div class="order-info" v-if="orderInfo">
              <div class="info-item">
                <span>{{ orderType === 'service' ? '预约编号' : '订单号' }}</span>
                <span>{{ orderInfo.appointmentNo || orderInfo.orderNo }}</span>
              </div>
              <div class="info-item">
                <span>支付金额</span>
                <span class="amount">¥{{ orderInfo.servicePrice || orderInfo.payAmount }}</span>
              </div>
              <div class="info-item">
                <span>支付时间</span>
                <span>{{ orderInfo.payTime ? formatDateTime(orderInfo.payTime) : '刚刚' }}</span>
              </div>
            </div>
            <div class="actions">
              <el-button type="primary" @click="goToOrders">
                {{ orderType === 'service' ? '查看预约' : '查看订单' }}
              </el-button>
              <el-button plain @click="goToHome">
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
                <span>{{ orderType === 'service' ? '预约编号' : '订单号' }}</span>
                <span>{{ orderInfo.appointmentNo || orderInfo.orderNo }}</span>
              </div>
              <div class="info-item">
                <span>支付金额</span>
                <span class="amount">¥{{ orderInfo.servicePrice || orderInfo.payAmount }}</span>
              </div>
            </div>
            <div class="actions">
              <el-button type="primary" @click="retryQuery" :loading="retrying">
                <i class="el-icon-refresh"></i> 重新查询
              </el-button>
              <el-button plain @click="goToOrders">
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
import { getOrderDetail, queryPayResult } from '@/api/order';
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

      localStorage.setItem('lastOrderNo', this.orderNo);

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

    // 指数退避延迟：1.5^n 秒，上限 5 秒
    getRetryDelay() {
      return Math.min(1000 * Math.pow(1.5, this.retryCount), 5000);
    },

    // 重试调度：增加计数后判断是否继续重试
    scheduleRetry(retryFn, errorMsg) {
      this.retryCount++;
      if (this.retryCount < this.maxRetry) {
        const delay = this.getRetryDelay();
        console.log(`支付状态未更新，${delay}ms后重试...`);
        this.retryTimer = setTimeout(retryFn, delay);
      } else {
        this.loading = false;
        this.success = false;
        this.errorMessage = errorMsg;
        console.log('重试次数用尽');
      }
    },

    async checkAppointmentResultWithRetry() {
      this.retryCount = 0;
      await this.doCheckAppointmentResult();
    },

    // 带重试的服务预约支付结果查询
    async doCheckAppointmentResult() {
      try {
        console.log(`查询服务预约 (第${this.retryCount + 1}次):`, this.orderNo);
        const res = await getAppointmentDetailByNo(this.orderNo);
        console.log('预约详情响应:', res);

        if (res.code === 200 && res.data) {
          this.orderInfo = res.data;

          if (res.data.payStatus === 1) {
            this.success = true;
            this.loading = false;
            localStorage.removeItem('lastOrderNo');
            return;
          }

          if (res.data.status === 4 || res.data.status === 5) {
            this.loading = false;
            this.success = false;
            this.errorMessage = res.data.status === 4 ? '预约已取消' : '预约已被拒绝';
            return;
          }
        }

        // 支付状态未更新，主动向支付宝查询
        if (this.retryCount > 0) {
          try {
            console.log('主动查询支付宝支付结果...');
            await queryPayResult({ orderNo: this.orderNo });
            // 重新获取最新状态
            const retryRes = await getAppointmentDetailByNo(this.orderNo);
            if (retryRes.code === 200 && retryRes.data) {
              this.orderInfo = retryRes.data;
              if (retryRes.data.payStatus === 1) {
                this.success = true;
                this.loading = false;
                localStorage.removeItem('lastOrderNo');
                return;
              }
            }
          } catch (e) {
            console.error('主动查询支付宝失败:', e);
          }
        }

        // 指数退避后重试
        this.scheduleRetry(
          () => this.doCheckAppointmentResult(),
          '支付处理中，请稍后查看订单状态'
        );
      } catch (error) {
        console.error('查询预约异常:', error);
        this.scheduleRetry(
          () => this.doCheckAppointmentResult(),
          '查询支付状态失败，请稍后查看订单状态'
        );
      }
    },

    async checkOrderResultWithRetry() {
      this.retryCount = 0;
      await this.doCheckOrderResult();
    },

    // 带重试的商品订单支付结果查询
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
            return;
          }

          if (res.data.orderStatus === 4) {
            this.loading = false;
            this.success = false;
            this.errorMessage = '订单已取消';
            return;
          }
        }

        // 支付状态未更新，主动向支付宝查询
        if (this.retryCount > 0) {
          try {
            console.log('主动查询支付宝支付结果...');
            await queryPayResult({ orderNo: this.orderNo });
            // 重新获取最新状态
            const retryRes = await getOrderDetail(this.orderNo);
            if (retryRes.code === 200 && retryRes.data) {
              this.orderInfo = retryRes.data;
              if (retryRes.data.payStatus === 1) {
                this.success = true;
                this.loading = false;
                localStorage.removeItem('lastOrderNo');
                return;
              }
            }
          } catch (e) {
            console.error('主动查询支付宝失败:', e);
          }
        }

        // 指数退避后重试
        this.scheduleRetry(
          () => this.doCheckOrderResult(),
          '支付处理中，请稍后查看订单状态'
        );
      } catch (error) {
        console.error('查询订单异常:', error);
        this.scheduleRetry(
          () => this.doCheckOrderResult(),
          '查询支付状态失败，请稍后查看订单状态'
        );
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
        // 主动向支付宝查询并更新数据库
        try {
          await queryPayResult({ orderNo: this.orderNo });
        } catch (e) {
          console.error('主动查询支付宝失败:', e);
        }

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
        this.$router.push('/my-appointments');
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
  background: #fdf8f5;
}

.return-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.breadcrumb {
  flex: 1;
}

/* 返回卡片 */
.return-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(180, 120, 90, 0.06);
  border: 1px solid #f5ece6;
}

.loading-state i,
.success-state i,
.failed-state i {
  font-size: 64px;
  margin-bottom: 16px;
}

.loading-state i {
  color: #f59e4b;
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
  font-size: 12px;
  color: #a08c84;
  margin-top: 8px;
}

.return-card h2 {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #3d2e2a;
}

.return-card p {
  color: #7a6a62;
  font-size: 14px;
  margin-bottom: 24px;
}

/* 订单信息 */
.order-info {
  background: #fefbf9;
  border-radius: 8px;
  padding: 16px 20px;
  margin: 20px 0;
  text-align: left;
  border: 1px solid #f5ece6;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  color: #7a6a62;
  font-size: 14px;
}

.info-item .amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

/* 按钮区域 */
.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 20px;
}

.actions .el-button {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
}

.actions .el-button--primary {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  border: none;
}

.actions .el-button--primary:hover {
  background: linear-gradient(135deg, #f7b06a, #f2967e);
  transform: translateY(-1px);
}

.actions .el-button--plain {
  border-color: #f5ece6;
  color: #7a6a62;
}

.actions .el-button--plain:hover {
  border-color: #f0826a;
  color: #f0826a;
}

.tip-text {
  margin-top: 20px;
  font-size: 12px;
  color: #a08c84;
}

.tip-text i {
  margin-right: 4px;
}

/* 响应式 */
@media (max-width: 768px) {
  .return-content {
    padding: 20px 0 40px;
  }

  .top-nav {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .back-btn {
    margin-left: 0;
    padding: 6px 12px;
  }

  .return-card {
    padding: 30px 20px;
  }

  .loading-state i,
  .success-state i,
  .failed-state i {
    font-size: 48px;
  }

  .return-card h2 {
    font-size: 18px;
  }

  .actions {
    flex-direction: column;
  }

  .actions .el-button {
    width: 100%;
  }
}
</style>