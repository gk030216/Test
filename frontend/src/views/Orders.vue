<template>
  <div class="orders-container">
    <Navbar />

    <div class="orders-content">
      <div class="container">
        <h2 class="page-title">我的订单</h2>

        <div class="orders-tabs">
          <div
              class="tab-item"
              :class="{ active: activeTab === status }"
              v-for="status in tabList"
              :key="status.value"
              @click="activeTab = status.value"
          >
            {{ status.label }}
          </div>
        </div>

        <div class="orders-list" v-loading="loading">
          <div class="empty-orders" v-if="orderList.length === 0">
            <i class="el-icon-s-order"></i>
            <p>暂无订单</p>
            <el-button type="primary" @click="$router.push('/shop')">去购物</el-button>
          </div>

          <div class="order-item" v-for="order in orderList" :key="order.orderNo">
            <div class="order-header">
              <div class="order-info">
                <span class="order-no">订单号：{{ order.orderNo }}</span>
                <span class="order-time">{{ formatDate(order.createTime) }}</span>
              </div>
              <div class="order-status" :class="getStatusClass(order.orderStatus)">
                {{ getStatusText(order.orderStatus) }}
              </div>
            </div>

            <div class="order-products">
              <div class="product-item" v-for="item in order.items" :key="item.id">
                <img :src="item.productImage" :alt="item.productName">
                <div class="product-info">
                  <h4>{{ item.productName }}</h4>
                  <div class="product-price">¥{{ item.price }}</div>
                </div>
                <div class="product-quantity">x{{ item.quantity }}</div>
                <div class="product-total">¥{{ item.totalAmount }}</div>
              </div>
            </div>

            <div class="order-footer">
              <div class="order-total">
                共 <strong>{{ order.items.length }}</strong> 件商品，实付：
                <span class="total-amount">¥{{ order.payAmount }}</span>
              </div>
              <div class="order-actions">
                <el-button
                    v-if="order.orderStatus === 0"
                    type="primary"
                    size="small"
                    @click="goToPay(order.orderNo)"
                >
                  去支付
                </el-button>
                <el-button
                    v-if="order.orderStatus === 0"
                    size="small"
                    @click="cancelOrder(order.orderNo)"
                >
                  取消订单
                </el-button>
                <el-button
                    v-if="order.orderStatus === 1 || order.orderStatus === 2"
                    size="small"
                    @click="confirmReceipt(order.orderNo)"
                >
                  确认收货
                </el-button>
                <el-button
                    v-if="order.orderStatus === 3"
                    size="small"
                    @click="goToComment(order.orderNo)"
                >
                  评价
                </el-button>
              </div>
            </div>
          </div>

          <div class="pagination" v-if="total > pageSize">
            <el-pagination
                @current-change="handlePageChange"
                :current-page="page"
                :page-size="pageSize"
                layout="prev, pager, next"
                :total="total"
                background
            >
            </el-pagination>
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
import { getOrderList, cancelOrder } from '@/api/order';

export default {
  name: 'Orders',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      orderList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      activeTab: '',
      tabList: [
        { value: '', label: '全部订单' },
        { value: 0, label: '待支付' },
        { value: 1, label: '已支付' },
        { value: 2, label: '已发货' },
        { value: 3, label: '已完成' },
        { value: 4, label: '已取消' }
      ]
    };
  },
  created() {
    this.loadOrders();
  },
  methods: {
    async loadOrders() {
      this.loading = true;
      try {
        const res = await getOrderList({
          page: this.page,
          pageSize: this.pageSize,
          status: this.activeTab !== '' ? this.activeTab : undefined
        });
        if (res.code === 200) {
          this.orderList = res.data.list;
          this.total = res.data.total;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    handlePageChange(page) {
      this.page = page;
      this.loadOrders();
    },
    getStatusText(status) {
      const map = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消',
        5: '已退款'
      };
      return map[status] || '未知';
    },
    getStatusClass(status) {
      const map = {
        0: 'status-pending',
        1: 'status-paid',
        2: 'status-shipped',
        3: 'status-completed',
        4: 'status-cancelled'
      };
      return map[status] || '';
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    goToPay(orderNo) {
      this.$router.push(`/pay/${orderNo}`);
    },
    async cancelOrder(orderNo) {
      this.$confirm('确定要取消该订单吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          await cancelOrder(orderNo);
          this.$message.success('取消成功');
          this.loadOrders();
        } catch (error) {
          this.$message.error('取消失败');
        }
      }).catch(() => {});
    },
    confirmReceipt(orderNo) {
      this.$confirm('确认已收到商品？', '提示', { type: 'info' }).then(async () => {
        // 调用确认收货接口
        this.$message.success('确认收货成功');
        this.loadOrders();
      }).catch(() => {});
    },
    goToComment(orderNo) {
      this.$message.info('评价功能开发中');
    }
  },
  watch: {
    activeTab() {
      this.page = 1;
      this.loadOrders();
    }
  }
};
</script>

<style scoped>
.orders-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.orders-content {
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

.orders-tabs {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  background: white;
  padding: 15px 30px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.tab-item {
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
  padding: 5px 0;
}

.tab-item:hover {
  color: #667eea;
}

.tab-item.active {
  color: #667eea;
  font-weight: 500;
  border-bottom: 2px solid #667eea;
}

.empty-orders {
  text-align: center;
  padding: 80px;
  background: white;
  border-radius: 20px;
}

.empty-orders i {
  font-size: 80px;
  color: #ddd;
  margin-bottom: 20px;
}

.order-item {
  background: white;
  border-radius: 16px;
  margin-bottom: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fc;
  border-bottom: 1px solid #f0f0f0;
}

.order-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.order-no {
  font-weight: 500;
}

.order-status {
  font-weight: 500;
}

.status-pending { color: #e6a23c; }
.status-paid { color: #409EFF; }
.status-shipped { color: #67c23a; }
.status-completed { color: #909399; }
.status-cancelled { color: #f56c6c; }

.order-products {
  padding: 20px;
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

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fc;
  border-top: 1px solid #f0f0f0;
}

.order-total {
  font-size: 14px;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
  margin-left: 5px;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .orders-tabs {
    overflow-x: auto;
    gap: 15px;
    padding: 12px 20px;
  }

  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .product-item {
    flex-wrap: wrap;
  }

  .product-quantity, .product-total {
    width: auto;
    margin-top: 10px;
  }

  .order-footer {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
}
</style>