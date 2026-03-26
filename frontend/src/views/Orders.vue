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
                    v-if="order.orderStatus === 3 && !order.hasCommented"
                    size="small"
                    type="success"
                    @click="openCommentDialog(order)"
                >
                  评价
                </el-button>
                <el-button
                    v-if="order.orderStatus === 3 && order.hasCommented"
                    size="small"
                    type="info"
                    disabled
                >
                  已评价
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

    <!-- 评价对话框 -->
    <el-dialog
        title="发表评价"
        :visible.sync="commentDialogVisible"
        width="550px"
        :close-on-click-modal="false"
        center
    >
      <div class="comment-dialog">
        <div class="comment-product" v-if="currentOrder">
          <img :src="currentProduct?.productImage" :alt="currentProduct?.productName">
          <div class="product-info">
            <h4>{{ currentProduct?.productName }}</h4>
            <div class="product-price">¥{{ currentProduct?.price }}</div>
          </div>
        </div>

        <el-form :model="commentForm" :rules="commentRules" ref="commentForm" label-width="80px">
          <el-form-item label="评分" prop="rating">
            <el-rate
                v-model="commentForm.rating"
                :texts="['很差', '较差', '一般', '推荐', '超赞']"
                show-text
                :colors="['#f56c6c', '#e6a23c', '#67c23a']"
            ></el-rate>
          </el-form-item>

          <el-form-item label="评价内容" prop="content">
            <el-input
                type="textarea"
                v-model="commentForm.content"
                rows="4"
                placeholder="说说你的使用感受..."
                maxlength="500"
                show-word-limit
            ></el-input>
          </el-form-item>

          <el-form-item label="上传图片">
            <el-upload
                action="#"
                :http-request="uploadCommentImage"
                list-type="picture-card"
                :on-preview="handlePictureCardPreview"
                :on-remove="handleRemove"
                :file-list="commentForm.imageList"
                :limit="5"
            >
              <i class="el-icon-plus"></i>
            </el-upload>
            <div class="upload-tip">支持 JPG、PNG 格式，最多5张，每张不超过2MB</div>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer">
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="commentSubmitting">提交评价</el-button>
      </span>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog :visible.sync="previewVisible" width="400px">
      <img :src="previewImage" style="width: 100%" alt="预览图片">
    </el-dialog>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getOrderList, cancelOrder, updateOrderStatus } from '@/api/order';
import { addComment } from '@/api/comment';
import { uploadCommentImage as uploadImage } from '@/api/upload';

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
      ],
      commentDialogVisible: false,
      commentSubmitting: false,
      currentOrder: null,
      currentProduct: null,
      commentForm: {
        rating: 5,
        content: '',
        imageList: [],
        imageUrls: []
      },
      commentRules: {
        rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
        content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
      },
      previewVisible: false,
      previewImage: ''
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
      const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消', 5: '已退款' };
      return map[status] || '未知';
    },
    getStatusClass(status) {
      const map = { 0: 'status-pending', 1: 'status-paid', 2: 'status-shipped', 3: 'status-completed', 4: 'status-cancelled' };
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
    async confirmReceipt(orderNo) {
      this.$confirm('确认已收到商品？', '提示', { type: 'info' }).then(async () => {
        try {
          await updateOrderStatus(orderNo, 3);
          this.$message.success('确认收货成功');
          this.loadOrders();
        } catch (error) {
          this.$message.error('操作失败');
        }
      }).catch(() => {});
    },
    openCommentDialog(order) {
      this.currentOrder = order;
      // 如果有多个商品，让用户选择评价哪个商品
      if (order.items.length > 1) {
        this.$confirm('请选择要评价的商品', '选择商品', {
          distinguishCancelAndClose: true,
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }).then(() => {
          // 简化处理：评价第一个商品
          this.currentProduct = order.items[0];
          this.commentForm = {
            rating: 5,
            content: '',
            imageList: [],
            imageUrls: []
          };
          this.commentDialogVisible = true;
        }).catch(() => {});
      } else {
        this.currentProduct = order.items[0];
        this.commentForm = {
          rating: 5,
          content: '',
          imageList: [],
          imageUrls: []
        };
        this.commentDialogVisible = true;
      }
    },
    async uploadCommentImage(file) {
      const formData = new FormData();
      formData.append('file', file.file);

      try {
        const res = await uploadImage(formData);
        if (res.code === 200) {
          this.commentForm.imageUrls.push(res.data.url);
          this.commentForm.imageList.push({
            name: file.file.name,
            url: res.data.url
          });
        } else {
          this.$message.error('上传失败');
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
    },
    handlePictureCardPreview(file) {
      this.previewImage = file.url;
      this.previewVisible = true;
    },
    handleRemove(file, fileList) {
      const index = this.commentForm.imageList.findIndex(f => f.uid === file.uid);
      if (index !== -1) {
        this.commentForm.imageList.splice(index, 1);
        this.commentForm.imageUrls.splice(index, 1);
      }
    },
    async submitComment() {
      this.$refs.commentForm.validate(async (valid) => {
        if (!valid) return;

        this.commentSubmitting = true;
        try {
          const data = {
            orderNo: this.currentOrder.orderNo,
            productId: this.currentProduct.productId,
            rating: this.commentForm.rating,
            content: this.commentForm.content,
            images: this.commentForm.imageUrls.join(',')
          };

          const res = await addComment(data);
          if (res.code === 200) {
            this.$message.success('评价成功');
            this.commentDialogVisible = false;
            // 刷新订单列表
            this.loadOrders();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('评价失败');
        } finally {
          this.commentSubmitting = false;
        }
      });
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

/* 评价对话框样式 */
.comment-dialog {
  padding: 10px 0;
}

.comment-product {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f8f9fc;
  border-radius: 12px;
  margin-bottom: 20px;
}

.comment-product img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.comment-product .product-info h4 {
  font-size: 14px;
  margin-bottom: 8px;
  color: #333;
}

.comment-product .product-price {
  color: #ff6b6b;
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
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