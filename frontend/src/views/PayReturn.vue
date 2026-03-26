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
            <p>您的订单已支付成功，我们将尽快为您发货</p>
            <div class="order-info" v-if="order">
              <div class="info-item">
                <span>订单号：</span>
                <span>{{ order.orderNo }}</span>
              </div>
              <div class="info-item">
                <span>支付金额：</span>
                <span class="amount">¥{{ order.payAmount }}</span>
              </div>
            </div>
            <div class="actions">
              <el-button type="primary" @click="goToOrders">查看订单</el-button>
              <el-button @click="goToShop">继续购物</el-button>
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

export default {
  name: 'PayReturn',
  components: { Navbar, Footer },
  data() {
    return {
      loading: true,
      success: false,
      errorMessage: '',
      order: null,
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
      console.log('当前URL:', window.location.href);

      // 获取URL参数
      const urlParams = new URLSearchParams(window.location.search);
      this.orderNo = urlParams.get('out_trade_no');
      this.tradeNo = urlParams.get('trade_no');

      console.log('解析到的订单号:', this.orderNo);

      // 如果从URL获取不到，从localStorage获取
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

      try {
        console.log('开始查询订单详情:', this.orderNo);
        const res = await getOrderDetail(this.orderNo);
        console.log('订单详情响应:', res);

        if (res.code === 200) {
          this.order = res.data;
          if (this.order.payStatus === 1) {
            this.success = true;
            localStorage.removeItem('lastOrderNo');
          } else {
            this.success = false;
            this.errorMessage = this.order.orderStatus === 4 ? '订单已取消' : '订单未支付';
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
    goToOrders() {
      this.$router.push('/orders');
    },
    goToShop() {
      this.$router.push('/shop');
    },
    async retryPay() {
      if (!this.orderNo) return;
      this.loading = true;
      try {
        const res = await payOrder({ orderNo: this.orderNo, payType: 1 });
        if (res.code === 200) {
          const payForm = res.data;
          const div = document.createElement('div');
          div.innerHTML = payForm;
          document.body.appendChild(div);
          const form = div.getElementsByTagName('form')[0];
          if (form) form.submit();
        } else {
          this.$message.error(res.message);
          this.loading = false;
        }
      } catch (error) {
        this.$message.error('重新支付失败');
        this.loading = false;
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
.return-content { flex: 1; padding: 60px 0; }
.container { max-width: 600px; margin: 0 auto; padding: 0 20px; }
.return-card { background: white; border-radius: 24px; padding: 60px 40px; text-align: center; box-shadow: 0 8px 30px rgba(0,0,0,0.08); }
.loading-state i, .success-state i, .failed-state i { font-size: 80px; margin-bottom: 20px; }
.loading-state i { color: #409EFF; animation: rotate 1s linear infinite; }
.success-state i { color: #67c23a; }
.failed-state i { color: #e6a23c; }
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.return-card h2 { font-size: 24px; margin-bottom: 10px; color: #333; }
.return-card p { color: #666; margin-bottom: 30px; }
.order-info { background: #f8f9fc; border-radius: 16px; padding: 20px; margin: 20px 0; text-align: left; }
.info-item { display: flex; justify-content: space-between; padding: 8px 0; }
.info-item .amount { color: #ff6b6b; font-size: 18px; font-weight: bold; }
.actions { display: flex; gap: 20px; justify-content: center; margin-top: 20px; }
@media (max-width: 768px) {
  .return-card { padding: 40px 20px; }
  .actions { flex-direction: column; }
}
</style>