<template>
  <div class="pay-container">
    <Navbar />

    <div class="pay-content">
      <div class="container">
        <!-- 顶部导航栏 -->
        <div class="top-nav">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/cart' }">购物车</el-breadcrumb-item>
            <el-breadcrumb-item>订单支付</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" class="back-btn">返回</el-button>
        </div>

        <div class="pay-card" v-loading="loading">
          <div class="pay-header">
            <i class="el-icon-circle-check" v-if="payStatus === 'success'"></i>
            <i class="el-icon-warning" v-else-if="payStatus === 'failed'"></i>
            <i class="el-icon-time" v-else></i>
            <h2>{{ payTitle }}</h2>
            <p>{{ payMessage }}</p>
          </div>

          <!-- 商品订单信息 -->
          <div class="pay-info" v-if="orderType === 'product' && order">
            <div class="info-item">
              <span>订单号</span>
              <span>{{ order.orderNo }}</span>
            </div>
            <div class="info-item">
              <span>商品名称</span>
              <span>{{ getProductNames() }}</span>
            </div>
            <div class="info-item">
              <span>支付金额</span>
              <span class="amount">¥{{ order.payAmount }}</span>
            </div>
            <div class="info-item">
              <span>订单类型</span>
              <el-tag type="primary" size="small">商品订单</el-tag>
            </div>
          </div>

          <!-- 服务预约信息 -->
          <div class="pay-info" v-if="orderType === 'service' && appointment">
            <div class="info-item">
              <span>预约编号</span>
              <span>{{ appointment.appointmentNo }}</span>
            </div>
            <div class="info-item">
              <span>服务名称</span>
              <span>{{ appointment.serviceName }}</span>
            </div>
            <div class="info-item">
              <span>预约宠物</span>
              <span>{{ appointment.petName || '未指定' }}</span>
            </div>
            <div class="info-item">
              <span>预约时间</span>
              <span>{{ formatDate(appointment.appointmentDate) }} {{ appointment.appointmentTime }}</span>
            </div>
            <div class="info-item">
              <span>支付金额</span>
              <span class="amount">¥{{ appointment.servicePrice }}</span>
            </div>
            <div class="info-item">
              <span>订单类型</span>
              <el-tag type="success" size="small">服务预约</el-tag>
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
                <span>支付宝</span>
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
            <el-button size="large" plain @click="cancelOrder">取消订单</el-button>
          </div>

          <div class="pay-actions" v-else-if="payStatus === 'success'">
            <el-button type="primary" size="large" @click="goToOrders">
              {{ orderType === 'service' ? '查看预约' : '查看订单' }}
            </el-button>
            <el-button plain size="large" @click="goToHome">
              {{ orderType === 'service' ? '继续预约' : '继续购物' }}
            </el-button>
          </div>

          <div class="pay-actions" v-else-if="payStatus === 'failed'">
            <el-button type="primary" size="large" @click="retryPay">
              重新支付
            </el-button>
            <el-button plain size="large" @click="goToOrders">
              {{ orderType === 'service' ? '查看预约' : '查看订单' }}
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
import { getAppointmentDetailByNo, cancelAppointment } from '@/api/service';

export default {
  name: 'Pay',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      paying: false,
      order: null,
      appointment: null,
      payType: 1,
      payStatus: 'pending',
      payTitle: '订单待支付',
      payMessage: '请在30分钟内完成支付，超时订单将自动取消',
      pollingTimer: null,
      orderType: 'product'
    };
  },
  computed: {
    orderNo() {
      return this.$route.params.orderNo;
    }
  },
  created() {
    this.detectOrderType();
    this.loadOrder();
    localStorage.setItem('lastOrderNo', this.orderNo);
  },
  beforeDestroy() {
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
    }
  },
  methods: {
    detectOrderType() {
      if (this.orderNo && this.orderNo.startsWith('AP')) {
        this.orderType = 'service';
      } else {
        this.orderType = 'product';
      }
    },

    async loadOrder() {
      this.loading = true;
      try {
        if (this.orderType === 'service') {
          await this.loadAppointment();
        } else {
          await this.loadProductOrder();
        }
      } catch (error) {
        console.error('加载订单失败', error);
        this.$message.error('订单不存在');
        this.payStatus = 'failed';
        this.payTitle = '订单加载失败';
        this.payMessage = '订单不存在或已被删除';
      } finally {
        this.loading = false;
      }
    },

    async loadProductOrder() {
      try {
        const res = await getOrderDetail(this.orderNo);
        if (res.code === 200) {
          this.order = res.data;
          if (this.order.payStatus === 1) {
            this.payStatus = 'success';
            this.payTitle = '支付成功';
            this.payMessage = '您的订单已支付成功，我们将尽快为您发货';
            this.clearPolling();
          } else if (this.order.orderStatus === 4) {
            this.payStatus = 'failed';
            this.payTitle = '订单已取消';
            this.payMessage = '订单已取消，如需购买请重新下单';
          }
        } else {
          throw new Error('订单不存在');
        }
      } catch (error) {
        throw error;
      }
    },

    async loadAppointment() {
      const res = await getAppointmentDetailByNo(this.orderNo);
      console.log('预约详情响应:', res);

      if (res.code === 200 && res.data) {
        this.appointment = res.data;

        if (this.appointment.payStatus === 1) {
          this.payStatus = 'success';
          this.payTitle = '支付成功';
          this.payMessage = '您的预约已支付成功，请等待商家确认';
          this.clearPolling();
        } else if (this.appointment.status === 4) {
          this.payStatus = 'failed';
          this.payTitle = '预约已取消';
          this.payMessage = '预约已取消，如需预约请重新下单';
        } else {
          this.payStatus = 'pending';
        }
      } else {
        throw new Error(res.message || '预约不存在');
      }
    },

    clearPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer);
        this.pollingTimer = null;
      }
      localStorage.removeItem('lastOrderNo');
    },

    getProductNames() {
      if (this.order && this.order.items && this.order.items.length > 0) {
        const names = this.order.items.map(item => item.productName).join('、');
        return names.length > 30 ? names.substring(0, 30) + '...' : names;
      }
      return '商品订单';
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    async handlePay() {
      this.paying = true;
      try {
        const res = await payOrder({
          orderNo: this.orderNo,
          payType: this.payType
        });

        if (res.code === 200) {
          const payForm = res.data;

          const div = document.createElement('div');
          div.style.cssText = 'display:none;';
          div.innerHTML = payForm;
          document.body.appendChild(div);

          setTimeout(() => {
            const form = div.querySelector('form');
            if (form) {
              form.submit();
              console.log('表单已提交');
            }
            setTimeout(() => {
              document.body.removeChild(div);
            }, 5000);
          }, 100);

          this.startPolling();
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

    startPolling() {
      let count = 0;
      this.pollingTimer = setInterval(async () => {
        count++;
        console.log(`轮询第 ${count} 次，查询支付状态...`);

        try {
          let paySuccess = false;

          if (this.orderType === 'service') {
            const res = await getAppointmentDetailByNo(this.orderNo);
            console.log('轮询预约状态:', res);
            paySuccess = res.code === 200 && res.data.payStatus === 1;
            if (paySuccess) {
              console.log('✅ 预约支付成功！');
            }
          } else {
            const res = await getOrderDetail(this.orderNo);
            console.log('轮询订单状态:', res);
            paySuccess = res.code === 200 && res.data.payStatus === 1;
            if (paySuccess) {
              console.log('✅ 订单支付成功！');
            }
          }

          if (paySuccess) {
            this.clearPolling();
            this.payStatus = 'success';
            this.payTitle = '支付成功';
            this.payMessage = this.orderType === 'service'
                ? '您的预约已支付成功，请等待商家确认'
                : '您的订单已支付成功，我们将尽快为您发货';

            setTimeout(() => {
              if (this.orderType === 'service') {
                this.$router.push('/personal/appointments');
              } else {
                this.$router.push('/personal/orders');
              }
            }, 3000);
          } else if (count >= 20) {
            this.clearPolling();
            console.log('轮询超时');
            this.$message.warning('支付处理中，请稍后查看状态');
            setTimeout(() => {
              if (this.orderType === 'service') {
                this.$router.push('/personal/appointments');
              } else {
                this.$router.push('/personal/orders');
              }
            }, 2000);
          }
        } catch (error) {
          console.error('轮询查询失败', error);
          if (count >= 20) {
            this.clearPolling();
            setTimeout(() => {
              if (this.orderType === 'service') {
                this.$router.push('/personal/appointments');
              } else {
                this.$router.push('/personal/orders');
              }
            }, 2000);
          }
        }
      }, 3000);
    },

    async cancelOrder() {
      const message = this.orderType === 'service' ? '确定要取消该预约吗？' : '确定要取消该订单吗？';

      this.$confirm(message, '提示', { type: 'warning' }).then(async () => {
        try {
          if (this.orderType === 'service') {
            await cancelAppointment(this.appointment.id, '用户取消');
          } else {
            await cancelOrder(this.orderNo);
          }
          this.$message.success('已取消');
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
.pay-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.pay-content {
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

/* 支付卡片 */
.pay-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
}

.pay-header i {
  font-size: 64px;
  margin-bottom: 16px;
}

.pay-header .el-icon-circle-check {
  color: #67c23a;
}

.pay-header .el-icon-warning {
  color: #e6a23c;
}

.pay-header .el-icon-time {
  color: #409EFF;
}

.pay-header h2 {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
}

.pay-header p {
  color: #909399;
  font-size: 13px;
}

/* 订单信息 */
.pay-info {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
  margin: 24px 0;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  color: #606266;
  font-size: 14px;
}

.info-item .amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

/* 支付方式 */
.pay-methods {
  margin: 24px 0;
  text-align: left;
}

.method-title {
  margin-bottom: 12px;
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.method-list {
  display: flex;
  gap: 12px;
}

.method-item {
  width: 110px;
  padding: 12px;
  border: 1px solid #eef2f6;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.method-item i {
  font-size: 28px;
  margin-bottom: 6px;
  display: block;
  color: #909399;
}

.method-item span {
  font-size: 13px;
  color: #606266;
}

.method-item:hover {
  border-color: #409EFF;
}

.method-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
}

.method-item.active i {
  color: #409EFF;
}

.method-item.active span {
  color: #409EFF;
}

/* 按钮区域 */
.pay-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 24px;
}

.pay-actions .el-button {
  padding: 10px 30px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
}

.pay-btn {
  background: #409EFF;
  border: none;
}

.pay-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

.pay-actions .el-button--primary {
  background: #409EFF;
  border: none;
}

.pay-actions .el-button--primary:hover {
  background: #66b1ff;
}

.pay-actions .el-button--plain {
  border-color: #eef2f6;
  color: #606266;
}

.pay-actions .el-button--plain:hover {
  border-color: #409EFF;
  color: #409EFF;
}

/* 响应式 */
@media (max-width: 768px) {
  .pay-content {
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

  .pay-card {
    padding: 20px;
  }

  .pay-header i {
    font-size: 48px;
  }

  .pay-header h2 {
    font-size: 18px;
  }

  .method-list {
    flex-direction: column;
  }

  .method-item {
    width: 100%;
  }

  .pay-actions {
    flex-direction: column;
  }

  .pay-actions .el-button {
    width: 100%;
  }
}
</style>