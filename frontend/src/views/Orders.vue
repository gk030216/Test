<template>
  <div class="orders-container">
    <Navbar />

    <div class="orders-content">
      <div class="container">
        <h2 class="page-title">我的订单</h2>

        <!-- 订单标签页 -->
        <div class="orders-tabs">
          <div
              class="tab-item"
              :class="{ active: activeTab === status }"
              v-for="status in tabList"
              :key="status.value"
              @click="activeTab = status.value"
          >
            {{ status.label }}
            <span class="tab-count" v-if="getOrderCount(status.value) > 0">{{ getOrderCount(status.value) }}</span>
          </div>
        </div>

        <!-- 订单列表 -->
        <div class="orders-list" v-loading="loading">
          <div class="empty-orders" v-if="orderList.length === 0">
            <i class="el-icon-s-order"></i>
            <p>暂无订单</p>
            <el-button type="primary" @click="$router.push('/shop')">去购物</el-button>
          </div>

          <div class="order-item" v-for="order in orderList" :key="order.orderNo">
            <!-- 订单头部 -->
            <div class="order-header">
              <div class="order-info">
                <span class="order-no">订单号：{{ order.orderNo }}</span>
                <span class="order-time"><i class="el-icon-time"></i> {{ formatDate(order.createTime) }}</span>
              </div>
              <div class="order-status" :class="getStatusClass(order.orderStatus)">
                <i :class="getStatusIcon(order.orderStatus)"></i>
                {{ getStatusText(order.orderStatus) }}
              </div>
            </div>

            <!-- 订单商品列表 -->
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

            <!-- 订单底部 -->
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
                  <i class="el-icon-money"></i> 去支付
                </el-button>
                <el-button
                    v-if="order.orderStatus === 0"
                    size="small"
                    @click="cancelOrder(order.orderNo)"
                >
                  <i class="el-icon-close"></i> 取消订单
                </el-button>
                <el-button
                    v-if="order.orderStatus === 1 || order.orderStatus === 2"
                    size="small"
                    type="success"
                    @click="confirmReceipt(order.orderNo)"
                >
                  <i class="el-icon-check"></i> 确认收货
                </el-button>
                <el-button
                    v-if="order.orderStatus === 3 && order.commentStatus === 'uncommented'"
                    size="small"
                    type="warning"
                    @click="openCommentDialog(order)"
                >
                  <i class="el-icon-edit"></i> 评价
                </el-button>
                <el-button
                    v-if="order.orderStatus === 3 && order.commentStatus === 'commented'"
                    size="small"
                    type="info"
                    plain
                    @click="viewComment(order)"
                >
                  <i class="el-icon-view"></i> 查看评价
                </el-button>
              </div>
            </div>

            <!-- 已评价内容展示 -->
            <div class="order-comment" v-if="order.orderStatus === 3 && order.comment">
              <div class="comment-header">
                <span class="comment-title">我的评价</span>
                <span class="comment-time">{{ formatDate(order.comment.createTime) }}</span>
              </div>
              <div class="comment-content">
                <div class="comment-rating">
                  <el-rate v-model="order.comment.rating" disabled show-score text-color="#ff9900"></el-rate>
                </div>
                <div class="comment-text">{{ order.comment.content }}</div>
                <div class="comment-images" v-if="order.comment.images">
                  <el-image
                      v-for="(img, idx) in order.comment.images.split(',')"
                      :key="idx"
                      :src="img"
                      :preview-src-list="order.comment.images.split(',')"
                      fit="cover"
                      class="comment-img"
                  ></el-image>
                </div>
                <div class="comment-reply" v-if="order.comment.reply">
                  <div class="reply-header">
                    <i class="el-icon-chat-dot-round"></i>
                    <span>商家回复：</span>
                  </div>
                  <div class="reply-content">{{ order.comment.reply }}</div>
                  <div class="reply-time" v-if="order.comment.replyTime">{{ formatDate(order.comment.replyTime) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
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
        width="600px"
        :close-on-click-modal="false"
        center
        class="comment-dialog"
    >
      <div class="comment-dialog-content">
        <div class="comment-product" v-if="currentProduct">
          <img :src="currentProduct.productImage" :alt="currentProduct.productName">
          <div class="product-info">
            <h4>{{ currentProduct.productName }}</h4>
            <div class="product-price">¥{{ currentProduct.price }}</div>
          </div>
        </div>

        <el-form :model="commentForm" :rules="commentRules" ref="commentForm" label-width="80px">
          <el-form-item label="评分" prop="rating">
            <div class="rating-wrapper">
              <el-rate
                  v-model="commentForm.rating"
                  :texts="['很差', '较差', '一般', '推荐', '超赞']"
                  show-text
                  :colors="['#f56c6c', '#e6a23c', '#67c23a']"
              ></el-rate>
            </div>
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

      <span slot="footer" class="dialog-footer">
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="commentSubmitting">
          <i class="el-icon-check"></i> 提交评价
        </el-button>
      </span>
    </el-dialog>

    <!-- 查看评价对话框 -->
    <el-dialog
        title="我的评价"
        :visible.sync="viewCommentVisible"
        width="550px"
        center
        class="view-comment-dialog"
    >
      <div class="view-comment-content" v-if="viewCommentData">
        <div class="view-product" v-if="viewProduct">
          <img :src="viewProduct.productImage" :alt="viewProduct.productName">
          <div class="product-info">
            <h4>{{ viewProduct.productName }}</h4>
            <div class="product-price">¥{{ viewProduct.price }}</div>
          </div>
        </div>

        <div class="view-comment">
          <div class="comment-rating">
            <el-rate v-model="viewCommentData.rating" disabled show-score text-color="#ff9900"></el-rate>
          </div>
          <div class="comment-text">{{ viewCommentData.content }}</div>
          <div class="comment-images" v-if="viewCommentData.images">
            <el-image
                v-for="(img, idx) in viewCommentData.images.split(',')"
                :key="idx"
                :src="img"
                :preview-src-list="viewCommentData.images.split(',')"
                fit="cover"
                class="comment-img"
            ></el-image>
          </div>
          <div class="comment-time">{{ formatDate(viewCommentData.createTime) }}</div>
          <div class="comment-reply" v-if="viewCommentData.reply">
            <div class="reply-header">
              <i class="el-icon-chat-dot-round"></i>
              <span>商家回复：</span>
            </div>
            <div class="reply-content">{{ viewCommentData.reply }}</div>
            <div class="reply-time">{{ formatDate(viewCommentData.replyTime) }}</div>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="viewCommentVisible = false">关闭</el-button>
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
import { addComment, getProductComments } from '@/api/comment';
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
      orderCounts: {},
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
      previewImage: '',
      viewCommentVisible: false,
      viewCommentData: null,
      viewProduct: null
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
          await this.loadOrderComments();
        }
      } catch (error) {
        console.error('加载订单失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadOrderComments() {
      const currentUserId = this.getCurrentUserId();

      for (let order of this.orderList) {
        // 初始化评论状态
        order.commentStatus = 'uncommented';
        order.comment = null;

        // 只有已完成状态的订单才需要检查评价
        if (order.orderStatus === 3 && order.items && order.items.length > 0) {
          for (let product of order.items) {
            try {
              const res = await getProductComments(product.productId, 1, 100);
              if (res.code === 200 && res.data.list && res.data.list.length > 0) {
                // 查找当前用户的评价
                const userComment = res.data.list.find(c => c.userId === currentUserId);
                if (userComment) {
                  order.comment = userComment;
                  order.commentStatus = 'commented';
                  break; // 找到一个评价就停止，一个订单只需显示一个评价
                }
              }
            } catch (error) {
              console.error('获取商品评价失败', error);
            }
          }
        }
      }
    },

    getCurrentUserId() {
      const userInfo = localStorage.getItem('userInfo');
      return userInfo ? JSON.parse(userInfo).id : null;
    },

    getOrderCount(status) {
      return this.orderCounts[status] || 0;
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
      const map = {
        0: 'status-pending',
        1: 'status-paid',
        2: 'status-shipped',
        3: 'status-completed',
        4: 'status-cancelled'
      };
      return map[status] || '';
    },

    getStatusIcon(status) {
      const map = {
        0: 'el-icon-time',
        1: 'el-icon-success',
        2: 'el-icon-truck',
        3: 'el-icon-check',
        4: 'el-icon-close'
      };
      return map[status] || 'el-icon-info';
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
      if (order.items.length > 1) {
        this.$confirm('请选择要评价的商品', '选择商品', {
          distinguishCancelAndClose: true,
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }).then(() => {
          this.currentProduct = order.items[0];
          this.resetCommentForm();
          this.commentDialogVisible = true;
        }).catch(() => {});
      } else {
        this.currentProduct = order.items[0];
        this.resetCommentForm();
        this.commentDialogVisible = true;
      }
    },

    resetCommentForm() {
      this.commentForm = {
        rating: 5,
        content: '',
        imageList: [],
        imageUrls: []
      };
      this.$nextTick(() => {
        if (this.$refs.commentForm) {
          this.$refs.commentForm.clearValidate();
        }
      });
    },

    async uploadCommentImage(file) {
      const formData = new FormData();
      formData.append('file', file.file);

      try {
        const res = await uploadImage(formData);
        if (res.code === 200) {
          this.commentForm.imageUrls.push(res.data.url);
          this.commentForm.imageList.push({
            uid: Date.now() + Math.random(),
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
            this.$message.success('评价成功！感谢您的反馈');
            this.commentDialogVisible = false;
            await this.loadOrders();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('提交评价失败', error);
          this.$message.error('评价失败');
        } finally {
          this.commentSubmitting = false;
        }
      });
    },

    viewComment(order) {
      if (order.comment) {
        this.viewCommentData = order.comment;
        this.viewProduct = order.items[0];
        this.viewCommentVisible = true;
      }
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
/* 样式保持不变，与之前相同 */
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
  font-weight: 600;
}

.orders-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 30px;
  background: white;
  padding: 12px 24px;
  border-radius: 50px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  flex-wrap: wrap;
}

.tab-item {
  cursor: pointer;
  padding: 8px 24px;
  border-radius: 30px;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  position: relative;
}

.tab-item:hover {
  color: #667eea;
  background: #f5f7fa;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.tab-count {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 2px 8px;
  font-size: 12px;
  margin-left: 6px;
}

.tab-item.active .tab-count {
  background: rgba(255, 255, 255, 0.3);
}

.empty-orders {
  text-align: center;
  padding: 80px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.empty-orders i {
  font-size: 80px;
  color: #ddd;
  margin-bottom: 20px;
}

.empty-orders p {
  font-size: 16px;
  color: #999;
  margin-bottom: 20px;
}

.order-item {
  background: white;
  border-radius: 20px;
  margin-bottom: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.order-item:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #f8f9fc 0%, #f0f2f6 100%);
  border-bottom: 1px solid #eef2f6;
}

.order-info {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #666;
}

.order-no {
  font-weight: 600;
  color: #333;
  font-family: monospace;
}

.order-time i {
  margin-right: 4px;
  color: #999;
}

.order-status {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.order-status i {
  font-size: 16px;
}

.status-pending { color: #e6a23c; }
.status-paid { color: #409EFF; }
.status-shipped { color: #67c23a; }
.status-completed { color: #909399; }
.status-cancelled { color: #f56c6c; }

.order-products {
  padding: 20px 24px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.product-item:last-child {
  border-bottom: none;
}

.product-item img {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
  margin-right: 16px;
  cursor: pointer;
  transition: transform 0.3s;
}

.product-item img:hover {
  transform: scale(1.05);
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin-bottom: 8px;
  font-size: 15px;
  font-weight: 500;
  color: #333;
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
  width: 120px;
  text-align: right;
  color: #ff6b6b;
  font-weight: 600;
  font-size: 16px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fafbfc;
  border-top: 1px solid #f0f0f0;
}

.order-total {
  font-size: 14px;
  color: #666;
}

.order-total strong {
  color: #ff6b6b;
  font-size: 16px;
}

.total-amount {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
  margin-left: 8px;
}

.order-actions {
  display: flex;
  gap: 12px;
}

.order-actions .el-button {
  border-radius: 24px;
  padding: 8px 20px;
}

.order-comment {
  padding: 16px 24px;
  background: #fef9e6;
  border-top: 1px solid #ffe4b5;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.comment-title {
  font-weight: 600;
  color: #e6a23c;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-rating {
  margin-bottom: 10px;
}

.comment-text {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.comment-img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
  transition: transform 0.3s;
}

.comment-img:hover {
  transform: scale(1.05);
}

.comment-reply {
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  margin-top: 12px;
  border-left: 3px solid #67c23a;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #67c23a;
  margin-bottom: 8px;
  font-size: 13px;
}

.reply-content {
  color: #666;
  line-height: 1.5;
  margin-bottom: 6px;
}

.reply-time {
  font-size: 11px;
  color: #999;
  text-align: right;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.comment-dialog ::v-deep .el-dialog {
  border-radius: 24px;
  overflow: hidden;
}

.comment-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
}

.comment-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.comment-dialog ::v-deep .el-dialog__close {
  color: white;
}

.comment-dialog-content {
  padding: 10px 0;
}

.comment-product {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 16px;
  margin-bottom: 24px;
}

.comment-product img {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
}

.comment-product .product-info h4 {
  font-size: 14px;
  margin-bottom: 8px;
  color: #333;
}

.comment-product .product-price {
  color: #ff6b6b;
  font-weight: 600;
}

.rating-wrapper {
  padding: 5px 0;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
}

.view-comment-dialog ::v-deep .el-dialog {
  border-radius: 24px;
}

.view-comment-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
}

.view-comment-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
}

.view-comment-content {
  padding: 10px 0;
}

.view-product {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 16px;
  margin-bottom: 24px;
}

.view-product img {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
}

.view-product .product-info h4 {
  font-size: 14px;
  margin-bottom: 8px;
  color: #333;
}

.view-comment {
  padding: 16px;
}

.view-comment .comment-text {
  margin: 12px 0;
  padding: 12px;
  background: #f8f9fc;
  border-radius: 12px;
  line-height: 1.6;
}

.view-comment .comment-time {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .orders-tabs {
    gap: 8px;
    padding: 12px 16px;
    border-radius: 24px;
  }

  .tab-item {
    padding: 6px 16px;
    font-size: 12px;
  }

  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .order-info {
    flex-direction: column;
    gap: 4px;
  }

  .product-item {
    flex-wrap: wrap;
    gap: 8px;
  }

  .product-quantity, .product-total {
    width: auto;
    margin-top: 8px;
  }

  .order-footer {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .order-actions {
    justify-content: center;
  }

  .comment-product {
    flex-direction: column;
    text-align: center;
  }

  .comment-product img {
    margin: 0 auto;
  }
}
</style>