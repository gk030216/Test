<template>
  <div class="orders-page">
    <div class="page-header">
      <h2 class="page-title">我的订单</h2>
      <p class="page-desc">查看和管理你的订单</p>
    </div>

    <!-- 订单标签页 -->
    <div class="orders-tabs">
      <div
          class="tab-item"
          :class="{ active: activeTab === status.value }"
          v-for="status in tabList"
          :key="status.value"
          @click="activeTab = status.value"
      >
        <i :class="status.icon"></i>
        <span>{{ status.label }}</span>
        <span class="tab-count" v-if="getOrderCount(status.value) > 0">{{ getOrderCount(status.value) }}</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="orders-list" v-loading="loading">
      <div class="empty-state" v-if="orderList.length === 0">
        <i class="el-icon-s-order"></i>
        <p>暂无订单</p>
        <el-button type="primary" size="small" @click="$router.push('/shop')">去购物</el-button>
      </div>

      <div class="order-card" v-for="order in orderList" :key="order.orderNo">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">
              <i class="el-icon-document"></i> {{ order.orderNo }}
            </span>
            <span class="order-time">
              <i class="el-icon-time"></i> {{ formatRelativeTime(order.createTime) }}
            </span>
          </div>
          <div class="order-status" :class="getStatusClass(order.orderStatus)">
            <i :class="getStatusIcon(order.orderStatus)"></i>
            {{ getStatusText(order.orderStatus) }}
          </div>
        </div>

        <!-- 订单商品列表 -->
        <div class="order-products">
          <div
              class="product-item"
              v-for="item in order.items"
              :key="item.id"
              @click="goToProduct(item.productId)"
          >
            <div class="product-image-wrapper">
              <img :src="item.productImage" :alt="item.productName">
            </div>
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
          <div class="footer-left">
            <span class="order-date">{{ formatDate(order.createTime) }}</span>
            <span class="order-amount">
              共 <strong>{{ order.items.length }}</strong> 件商品，实付：
              <span class="total-amount">¥{{ order.payAmount }}</span>
            </span>
          </div>
          <div class="footer-right">
            <!-- 查看订单按钮 -->
            <el-button
                type="info"
                size="small"
                plain
                @click="viewOrderDetail(order)"
                class="action-btn detail-btn"
            >
              <i class="el-icon-document"></i> 查看订单
            </el-button>

            <!-- 待支付 -->
            <template v-if="order.orderStatus === 0">
              <el-button
                  type="danger"
                  size="small"
                  plain
                  @click="cancelOrder(order.orderNo)"
                  class="action-btn cancel-btn"
              >
                <i class="el-icon-close"></i> 取消
              </el-button>
              <el-button
                  type="primary"
                  size="small"
                  @click="goToPay(order.orderNo)"
                  class="action-btn pay-btn"
              >
                <i class="el-icon-money"></i> 支付
              </el-button>
            </template>

            <!-- 已支付/已发货 -->
            <template v-if="order.orderStatus === 1 || order.orderStatus === 2">
              <el-button
                  type="primary"
                  size="small"
                  @click="confirmReceipt(order)"
                  class="action-btn confirm-btn"
              >
                <i class="el-icon-check"></i> 确认收货
              </el-button>
            </template>

            <!-- 已完成 -->
            <template v-if="order.orderStatus === 3">
              <!-- 待评价 -->
              <template v-if="order.commentStatus === 'uncommented'">
                <el-button
                    type="warning"
                    size="small"
                    @click="openCommentDialog(order)"
                    class="action-btn comment-btn"
                >
                  <i class="el-icon-edit"></i> 评价
                </el-button>
              </template>
              <!-- 已评价 -->
              <template v-else>
                <el-button
                    type="info"
                    size="small"
                    plain
                    @click="viewComment(order)"
                    class="action-btn view-comment-btn"
                >
                  <i class="el-icon-view"></i> 查看评价
                </el-button>
              </template>
              <el-button
                  type="primary"
                  size="small"
                  plain
                  @click="buyAgain(order)"
                  class="action-btn buy-btn"
              >
                <i class="el-icon-refresh"></i> 再次购买
              </el-button>
            </template>
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

    <!-- 评价对话框 -->
    <el-dialog
        title="发表评价"
        :visible.sync="commentDialogVisible"
        width="550px"
        :close-on-click-modal="false"
        center
        class="comment-dialog"
    >
      <div class="comment-dialog-content">
        <div class="product-selector" v-if="currentOrder && currentOrder.items.length > 1">
          <div class="selector-label">选择要评价的商品：</div>
          <div class="product-options">
            <div
                v-for="item in currentOrder.items"
                :key="item.productId"
                :class="['product-option', { active: selectedProductId === item.productId }]"
                @click="selectedProductId = item.productId"
            >
              <img :src="item.productImage" :alt="item.productName">
              <span>{{ item.productName }}</span>
              <i v-if="selectedProductId === item.productId" class="el-icon-check"></i>
            </div>
          </div>
        </div>

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

    <!-- 订单详情对话框 -->
    <el-dialog
        title="订单详情"
        :visible.sync="orderDetailVisible"
        width="600px"
        center
        class="order-detail-dialog"
    >
      <div class="order-detail-content" v-if="detailOrder">
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-info"></i> 订单信息
          </div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="订单号">{{ detailOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <span :class="['status-badge', getStatusClass(detailOrder.orderStatus)]">
                {{ getStatusText(detailOrder.orderStatus) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatDateTime(detailOrder.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ formatDateTime(detailOrder.payTime) || '--' }}</el-descriptions-item>
            <el-descriptions-item label="收货人">{{ detailOrder.userName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detailOrder.userPhone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ detailOrder.userAddress }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detailOrder.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-goods"></i> 商品清单
          </div>
          <div class="product-list">
            <div class="product-item-detail" v-for="item in detailOrder.items" :key="item.id">
              <img :src="item.productImage" :alt="item.productName">
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-price">¥{{ item.price }} x {{ item.quantity }}</div>
              </div>
              <div class="item-total">¥{{ item.totalAmount }}</div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-money"></i> 费用信息
          </div>
          <div class="amount-summary">
            <div class="amount-row">
              <span>商品总价</span>
              <span>¥{{ detailOrder.totalAmount }}</span>
            </div>
            <div class="amount-row total">
              <span>实付金额</span>
              <span class="total-price">¥{{ detailOrder.payAmount }}</span>
            </div>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="orderDetailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 查看评价对话框 -->
    <el-dialog
        title="我的评价"
        :visible.sync="viewCommentVisible"
        width="500px"
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
              <span>商家回复</span>
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
    <el-dialog :visible.sync="previewVisible" width="400px" append-to-body>
      <img :src="previewImage" style="width: 100%" alt="预览图片">
    </el-dialog>
  </div>
</template>

<script>
import { getOrderList, cancelOrder, updateOrderStatus } from '@/api/order';
import { addComment, getProductComments } from '@/api/comment';
import {uploadCommentImage as uploadImage} from '@/api/upload';
import {addToCart} from '@/api/cart';

export default {
  name: 'Orders',
  data() {
    return {
      loading: false,
      orderList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      activeTab: '',
      tabList: [
        {value: '', label: '全部', icon: 'el-icon-menu'},
        {value: 0, label: '待支付', icon: 'el-icon-time'},
        {value: 1, label: '已支付', icon: 'el-icon-success'},
        {value: 2, label: '已发货', icon: 'el-icon-truck'},
        {value: 3, label: '已完成', icon: 'el-icon-circle-check'},
        {value: 4, label: '已取消', icon: 'el-icon-circle-close'}
      ],
      orderCounts: {},
      commentDialogVisible: false,
      commentSubmitting: false,
      currentOrder: null,
      currentProduct: null,
      selectedProductId: null,
      commentForm: {
        rating: 5,
        content: '',
        imageList: [],
        imageUrls: []
      },
      commentRules: {
        rating: [{required: true, message: '请选择评分', trigger: 'change'}],
        content: [{required: true, message: '请输入评价内容', trigger: 'blur'}]
      },
      previewVisible: false,
      previewImage: '',
      viewCommentVisible: false,
      viewCommentData: null,
      viewProduct: null,
      orderDetailVisible: false,
      detailOrder: null
    };
  },
  watch: {
    activeTab() {
      this.page = 1;
      this.loadOrders();
    },
    selectedProductId(val) {
      if (val && this.currentOrder) {
        this.currentProduct = this.currentOrder.items.find(item => item.productId === val);
      }
    }
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
        order.commentStatus = 'uncommented';
        order.comment = null;

        if (order.orderStatus === 3 && order.items && order.items.length > 0) {
          for (let product of order.items) {
            try {
              const res = await getProductComments(product.productId, 1, 100);
              if (res.code === 200 && res.data.list && res.data.list.length > 0) {
                const userComment = res.data.list.find(c => c.userId === currentUserId);
                if (userComment) {
                  order.comment = userComment;
                  order.commentStatus = 'commented';
                  break;
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
      const map = {0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消', 5: '已退款'};
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
        3: 'el-icon-circle-check',
        4: 'el-icon-circle-close'
      };
      return map[status] || 'el-icon-info';
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    formatDateTime(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`;
    },

    formatRelativeTime(date) {
      if (!date) return '';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;

      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;

      return this.formatDate(date);
    },

    goToProduct(productId) {
      if (productId) {
        this.$router.push(`/product/${productId}`);
      }
    },

    goToPay(orderNo) {
      this.$router.push(`/pay/${orderNo}`);
    },

    async cancelOrder(orderNo) {
      this.$confirm('确定要取消该订单吗？', '提示', {type: 'warning'}).then(async () => {
        try {
          await cancelOrder(orderNo);
          this.$message.success('取消成功');
          this.loadOrders();
        } catch (error) {
          this.$message.error('取消失败');
        }
      }).catch(() => {
      });
    },

    async confirmReceipt(order) {
      this.$confirm('确认已收到商品？确认后可以评价商品哦！', '提示', {
        type: 'info',
        confirmButtonText: '确认收货',
        cancelButtonText: '再等等'
      }).then(async () => {
        try {
          await updateOrderStatus(order.orderNo, 3);
          this.$message.success('确认收货成功！快去评价吧~');
          await this.loadOrders();
          setTimeout(() => {
            this.openCommentDialog(order);
          }, 500);
        } catch (error) {
          this.$message.error('操作失败');
        }
      }).catch(() => {
      });
    },

    openCommentDialog(order) {
      this.currentOrder = order;
      if (order.items.length > 1) {
        this.selectedProductId = order.items[0].productId;
        this.currentProduct = order.items[0];
      } else {
        this.currentProduct = order.items[0];
      }
      this.resetCommentForm();
      this.commentDialogVisible = true;
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
            this.$message.success('感谢你的评价！');
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
      this.viewCommentData = order.comment;
      this.viewProduct = order.items[0];
      this.viewCommentVisible = true;
    },

    viewOrderDetail(order) {
      this.detailOrder = order;
      this.orderDetailVisible = true;
    },

    async buyAgain(order) {
      try {
        for (const item of order.items) {
          await addToCart(item.productId, item.quantity);
        }
        this.$message.success('已加入购物车');
        this.$bus.$emit('cart-updated');
      } catch (error) {
        this.$message.error('添加失败');
      }
    }
  }
};
</script>

<style scoped>
.orders-page {
  padding: 0;
  background: transparent;
  min-height: 400px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: #999;
}

.orders-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 18px;
  border-radius: 30px;
  transition: all 0.3s;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  background: #f5f5f5;
}

.tab-item i {
  font-size: 14px;
}

.tab-item:hover {
  background: #e8e8e8;
  color: #667eea;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.tab-count {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 2px 8px;
  font-size: 11px;
  margin-left: 4px;
}

.tab-item.active .tab-count {
  background: rgba(255, 255, 255, 0.3);
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 20px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #dcdfe6;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

.order-card {
  background: #fff;
  border-radius: 20px;
  margin-bottom: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
}

.order-info {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #666;
}

.order-info i {
  margin-right: 6px;
  color: #667eea;
}

.order-no {
  font-weight: 600;
  color: #2c3e50;
}

.order-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 20px;
}

.status-pending {
  color: #e6a23c;
  background: #fdf6ec;
}

.status-paid {
  color: #409EFF;
  background: #ecf5ff;
}

.status-shipped {
  color: #67c23a;
  background: #f0f9f4;
}

.status-completed {
  color: #909399;
  background: #f4f4f5;
}

.status-cancelled {
  color: #f56c6c;
  background: #fef0f0;
}

.order-products {
  padding: 16px 24px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.3s;
}

.product-item:hover {
  background: #fafbfc;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image-wrapper {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  overflow: hidden;
  margin-right: 16px;
  flex-shrink: 0;
}

.product-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-item:hover .product-image-wrapper img {
  transform: scale(1.05);
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.product-price {
  color: #909399;
  font-size: 13px;
}

.product-quantity {
  width: 60px;
  text-align: center;
  color: #666;
  font-size: 14px;
}

.product-total {
  width: 100px;
  text-align: right;
  color: #ff6b6b;
  font-weight: 600;
  font-size: 15px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fafbfc;
  border-top: 1px solid #f0f0f0;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.order-date {
  font-size: 12px;
  color: #999;
}

.order-amount {
  font-size: 14px;
  color: #666;
}

.order-amount strong {
  color: #ff6b6b;
  font-size: 15px;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
  margin-left: 8px;
}

.footer-right {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s;
}

.action-btn i {
  margin-right: 4px;
  font-size: 12px;
}

.detail-btn {
  color: #909399;
  border-color: #e9e9eb;
  background: #f4f4f5;
}

.detail-btn:hover {
  color: white;
  background: #909399;
  border-color: #909399;
}

.pay-btn {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  border: none;
  color: white;
}

.pay-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.cancel-btn {
  color: #f56c6c;
  border-color: #fde2e2;
  background: #fef0f0;
}

.cancel-btn:hover {
  color: white;
  background: #f56c6c;
  border-color: #f56c6c;
}

.confirm-btn {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  border: none;
  color: white;
}

.confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
}

.comment-btn {
  background: linear-gradient(135deg, #e6a23c, #f0b45c);
  border: none;
  color: white;
}

.comment-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.4);
}

.view-comment-btn {
  color: #e6a23c;
  border-color: #faecd8;
  background: #fdf6ec;
}

.view-comment-btn:hover {
  color: white;
  background: #e6a23c;
  border-color: #e6a23c;
}

.buy-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.buy-btn:hover {
  color: white;
  background: #409EFF;
  border-color: #409EFF;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 订单详情对话框 */
.order-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.order-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.order-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.order-detail-dialog ::v-deep .el-dialog__close {
  color: white;
}

.order-detail-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  font-size: 14px;
}

.section-title i {
  color: #667eea;
}

.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.status-pending {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-badge.status-paid {
  background: #ecf5ff;
  color: #409EFF;
}

.status-badge.status-shipped {
  background: #f0f9f4;
  color: #67c23a;
}

.status-badge.status-completed {
  background: #f4f4f5;
  color: #909399;
}

.status-badge.status-cancelled {
  background: #fef0f0;
  color: #f56c6c;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-item-detail {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fc;
  border-radius: 12px;
}

.product-item-detail img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 4px;
}

.item-price {
  font-size: 13px;
  color: #909399;
}

.item-total {
  font-weight: 600;
  color: #ff6b6b;
  font-size: 15px;
}

.amount-summary {
  background: #f8f9fc;
  border-radius: 12px;
  padding: 16px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  color: #666;
  font-size: 14px;
}

.amount-row.total {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
  font-weight: 600;
  color: #2c3e50;
}

.total-price {
  color: #ff6b6b;
  font-size: 20px;
  font-weight: bold;
}

/* 评价对话框 */
.product-selector {
  margin-bottom: 20px;
}

.selector-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.product-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.product-option:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.product-option.active {
  border-color: #667eea;
  background: #f8f9ff;
}

.product-option img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
}

.product-option span {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.product-option i {
  color: #67c23a;
  font-size: 18px;
}

.comment-dialog ::v-deep .el-dialog,
.view-comment-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.comment-dialog ::v-deep .el-dialog__header,
.view-comment-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.comment-dialog ::v-deep .el-dialog__title,
.view-comment-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.comment-dialog ::v-deep .el-dialog__close,
.view-comment-dialog ::v-deep .el-dialog__close {
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
  width: 70px;
  height: 70px;
  border-radius: 12px;
  object-fit: cover;
}

.comment-product .product-info h4 {
  font-size: 14px;
  margin-bottom: 6px;
  color: #2c3e50;
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
  color: #999;
  margin-top: 8px;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
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
  width: 70px;
  height: 70px;
  border-radius: 12px;
  object-fit: cover;
}

.view-product .product-info h4 {
  font-size: 14px;
  margin-bottom: 6px;
  color: #2c3e50;
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

/* 响应式 */
@media (max-width: 768px) {
  .orders-tabs {
    padding: 12px 0;
  }

  .tab-item {
    padding: 6px 14px;
    font-size: 12px;
  }

  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .order-info {
    flex-direction: column;
    gap: 8px;
  }

  .product-item {
    flex-wrap: wrap;
    gap: 8px;
  }

  .product-quantity,
  .product-total {
    width: auto;
    margin-top: 8px;
  }

  .order-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .footer-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .footer-right {
    width: 100%;
    justify-content: flex-start;
  }

  .comment-product {
    flex-direction: column;
    text-align: center;
  }

  .comment-product img {
    margin: 0 auto;
  }
}

/* 评价图片 */
.comment-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 12px 0;
}

.comment-img {
  width: 60px !important;
  height: 60px !important;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
  transition: transform 0.3s;
  border: 1px solid #f0f0f0;
}

.comment-img:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 商家回复样式 */
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
  color: #5a6874;
  line-height: 1.5;
  margin-bottom: 6px;
  font-size: 13px;
}

.reply-time {
  font-size: 11px;
  color: #999;
  text-align: right;
}
</style>