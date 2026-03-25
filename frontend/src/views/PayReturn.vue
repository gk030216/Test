<template>
  <div class="pay-return">
    <div class="return-card" v-loading="loading">
      <i class="el-icon-loading" v-if="loading"></i>
      <i class="el-icon-success" v-else-if="success"></i>
      <i class="el-icon-warning" v-else></i>
      <h2>{{ title }}</h2>
      <p>{{ message }}</p>
      <div class="actions" v-if="!loading">
        <el-button type="primary" @click="goToOrders">查看订单</el-button>
        <el-button @click="goToShop">继续购物</el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PayReturn',
  data() {
    return {
      loading: true,
      success: false,
      title: '支付处理中',
      message: '请稍候...',
      orderNo: ''
    };
  },
  created() {
    this.checkPayResult();
  },
  methods: {
    async checkPayResult() {
      // 获取URL参数
      const urlParams = new URLSearchParams(window.location.search);
      const outTradeNo = urlParams.get('out_trade_no');
      const tradeNo = urlParams.get('trade_no');

      if (outTradeNo) {
        this.orderNo = outTradeNo;
        this.loading = false;
        this.success = true;
        this.title = '支付成功';
        this.message = '您的订单已支付成功！';
      } else {
        this.loading = false;
        this.success = false;
        this.title = '支付失败';
        this.message = '支付遇到问题，请重试或联系客服';
      }
    },
    goToOrders() {
      this.$router.push('/orders');
    },
    goToShop() {
      this.$router.push('/shop');
    }
  }
};
</script>

<style scoped>
.pay-return {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
}

.return-card {
  background: white;
  border-radius: 24px;
  padding: 60px 80px;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
}

.return-card i {
  font-size: 80px;
  margin-bottom: 20px;
}

.return-card .el-icon-success {
  color: #67c23a;
}

.return-card .el-icon-warning {
  color: #e6a23c;
}

.return-card .el-icon-loading {
  color: #409EFF;
}

.return-card h2 {
  font-size: 24px;
  margin-bottom: 10px;
}

.return-card p {
  color: #666;
  margin-bottom: 30px;
}

.actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}
</style>