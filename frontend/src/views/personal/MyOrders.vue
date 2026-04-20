<template>
  <div class="orders-page">
    <h2 class="page-title">我的订单</h2>

    <!-- 订单类型切换 -->
    <div class="orders-type-tabs">
      <div class="type-tab" :class="{ active: orderType === 'product' }" @click="orderType = 'product'">
        <i class="el-icon-goods"></i> 商品订单
      </div>
      <div class="type-tab" :class="{ active: orderType === 'service' }" @click="orderType = 'service'">
        <i class="el-icon-service"></i> 服务预约
      </div>
    </div>

    <!-- 订单状态标签页 -->
    <div class="orders-tabs">
      <div class="tab-item" :class="{ active: activeTab === status.value }" v-for="status in currentTabList" :key="status.value" @click="activeTab = status.value">
        <i :class="status.icon"></i>
        <span>{{ status.label }}</span>
        <span class="tab-count" v-if="getOrderCount(status.value) > 0">{{ getOrderCount(status.value) }}</span>
      </div>
    </div>

    <!-- 订单网格 -->
    <div class="orders-grid" v-loading="loading">
      <!-- 商品订单卡片 -->
      <div class="order-card" v-for="order in orderList" :key="order.orderNo" v-if="orderType === 'product'">
        <div class="card-header">
          <div class="order-info">
            <span class="order-no"><i class="el-icon-document"></i> {{ order.orderNo }}</span>
            <span class="order-time"><i class="el-icon-time"></i> {{ formatRelativeTime(order.createTime) }}</span>
          </div>
          <div class="order-status" :class="getStatusClass(order.orderStatus)">
            <i :class="getStatusIcon(order.orderStatus)"></i> {{ getStatusText(order.orderStatus) }}
          </div>
        </div>
        <div class="card-body">
          <div class="product-image" v-if="order.items && order.items[0]">
            <img :src="order.items[0].productImage" :alt="order.items[0].productName">
            <span class="product-count" v-if="order.items.length > 1">+{{ order.items.length - 1 }}</span>
          </div>
          <div class="order-info-detail">
            <div class="product-name">{{ order.items && order.items[0] ? order.items[0].productName : '商品' }}</div>
            <div class="order-amount">共 {{ order.items ? order.items.length : 0 }} 件商品</div>
            <div class="order-total">实付：<span class="total-price">¥{{ order.payAmount }}</span></div>
            <div class="cancel-reason" v-if="order.orderStatus === 4 && order.cancelReason">
              <i class="el-icon-info"></i>
              <span>取消原因：{{ order.cancelReason }}</span>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <el-button size="small" plain @click="viewOrderDetail(order)">查看详情</el-button>

          <!-- 待支付 -->
          <template v-if="order.orderStatus === 0">
            <el-button size="small" type="danger" plain @click="openCancelDialog(order, 'product')">取消</el-button>
            <el-button size="small" type="primary" @click="goToPay(order.orderNo)">去支付</el-button>
          </template>

          <!-- 已支付 - 待发货，可申请退款 -->
          <template v-if="order.orderStatus === 1">
            <el-tag type="info" size="small" class="status-tag">待发货</el-tag>
            <el-button size="small" type="danger" plain @click="openCancelDialog(order, 'product')">申请退款</el-button>
          </template>

          <!-- 已发货 - 确认收货 -->
          <template v-if="order.orderStatus === 2">
            <el-button size="small" type="primary" @click="confirmReceipt(order)">确认收货</el-button>
          </template>

          <!-- 已完成 - 评价 -->
          <template v-if="order.orderStatus === 3">
            <el-button v-if="order.commentStatus === 'uncommented'" size="small" type="warning" @click="openCommentDialog(order)">评价</el-button>
            <el-button v-if="order.commentStatus === 'commented'" size="small" type="info" plain @click="viewComment(order)">查看评价</el-button>
            <el-button size="small" plain @click="buyAgain(order)">再次购买</el-button>
          </template>

          <!-- 已取消/已退款 - 显示标签 -->
          <template v-if="order.orderStatus === 4 || order.orderStatus === 5">
            <el-tag type="danger" size="small" class="status-tag">
              {{ order.orderStatus === 4 ? '已取消' : '已退款' }}
            </el-tag>
          </template>
        </div>
      </div>

      <!-- 服务预约卡片 -->
      <div class="order-card" v-for="appointment in appointmentList" :key="appointment.id" v-if="orderType === 'service'">
        <div class="card-header">
          <div class="order-info">
            <span class="order-no"><i class="el-icon-document"></i> {{ appointment.appointmentNo }}</span>
            <span class="order-time"><i class="el-icon-time"></i> {{ formatRelativeTime(appointment.createTime) }}</span>
          </div>
          <div class="order-status" :class="getAppointmentStatusClass(appointment.status)">
            <i :class="getAppointmentStatusIcon(appointment.status)"></i> {{ getAppointmentStatusText(appointment.status) }}
          </div>
        </div>
        <div class="card-body">
          <div class="product-image">
            <img v-if="appointment.serviceImage" :src="appointment.serviceImage" :alt="appointment.serviceName">
            <div v-else class="image-placeholder"><i class="el-icon-service"></i></div>
          </div>
          <div class="order-info-detail">
            <div class="product-name">{{ appointment.serviceName }}</div>
            <div class="appointment-info">
              <span><i class="el-icon-s-custom"></i> {{ appointment.petName || '未指定' }}</span>
              <span><i class="el-icon-date"></i> {{ formatDate(appointment.appointmentDate) }} {{ appointment.appointmentTime }}</span>
            </div>
            <!-- 备注信息 -->
            <div class="remark-info" v-if="appointment.remark">
              <i class="el-icon-edit"></i>
              <span>备注：{{ appointment.remark }}</span>
            </div>
            <div class="order-total">实付：<span class="total-price">¥{{ appointment.servicePrice }}</span></div>
            <!-- 取消原因显示 -->
            <div class="cancel-reason" v-if="appointment.status === 4 && appointment.cancelReason">
              <i class="el-icon-info"></i>
              <span>取消原因：{{ appointment.cancelReason }}</span>
            </div>
            <!-- ✅ 拒绝原因显示 -->
            <div class="cancel-reason" v-if="appointment.status === 5 && appointment.cancelReason">
              <i class="el-icon-info"></i>
              <span>拒绝原因：{{ appointment.cancelReason }}</span>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <el-button size="small" plain @click="viewAppointmentDetail(appointment)">查看详情</el-button>

          <!-- 待确认/未支付 -->
          <template v-if="appointment.status === 0 && appointment.payStatus !== 1">
            <el-button size="small" type="danger" plain @click="openCancelDialog(appointment, 'service')">取消</el-button>
            <el-button size="small" type="primary" @click="goToAppointmentPay(appointment)">去支付</el-button>
          </template>

          <!-- 已确认/已支付 - 可申请退款 -->
          <template v-if="appointment.status === 1 && appointment.payStatus === 1">
            <el-tag type="info" size="small" class="status-tag">待服务</el-tag>
            <el-button size="small" type="danger" plain @click="openCancelDialog(appointment, 'service')">申请退款</el-button>
          </template>

          <!-- 服务中 -->
          <template v-if="appointment.status === 2">
            <el-tag type="success" size="small" class="status-tag">服务中</el-tag>
          </template>

          <!-- 已完成 - 评价 -->
          <template v-if="appointment.status === 3">
            <el-button v-if="!appointment.comment" size="small" type="warning" @click="openServiceCommentDialog(appointment)">评价</el-button>
            <el-button v-if="appointment.comment" size="small" type="info" plain @click="viewServiceComment(appointment)">查看评价</el-button>
          </template>

          <!-- 已取消/已拒绝 - 显示标签，无操作按钮 -->
          <template v-if="appointment.status === 4 || appointment.status === 5">
            <el-tag type="danger" size="small" class="status-tag">
              {{ appointment.status === 4 ? '已取消' : '已拒绝' }}
            </el-tag>
          </template>
        </div>
      </div>
      <div class="empty-state" v-if="!loading && ((orderType === 'product' && orderList.length === 0) || (orderType === 'service' && appointmentList.length === 0))">
        <i class="el-icon-s-order"></i>
        <p>暂无{{ orderType === 'product' ? '订单' : '预约' }}</p>
        <el-button type="primary" size="small" @click="orderType === 'product' ? $router.push('/shop') : $router.push('/services')">
          {{ orderType === 'product' ? '去购物' : '去预约' }}
        </el-button>
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination @current-change="handlePageChange" :current-page="page" :page-size="pageSize" layout="prev, pager, next" :total="total" background />
      </div>
    </div>

    <!-- 取消/退款对话框 - 动态原因选项 -->
    <el-dialog :title="dialogTitle" :visible.sync="cancelDialogVisible" width="450px" center class="cancel-dialog">
      <div class="cancel-content">
        <div class="cancel-info">
          <i class="el-icon-warning"></i>
          <span>订单号：{{ currentCancelItem?.orderNo || currentCancelItem?.appointmentNo }}</span>
        </div>
        <el-form label-width="80px">
          <el-form-item label="取消原因">
            <!-- 服务预约原因选项 -->
            <el-select v-if="currentCancelType === 'service'" v-model="cancelReason" placeholder="请选择取消原因" style="width: 100%">
              <el-option label="时间冲突，改天再约" value="时间冲突，改天再约" />
              <el-option label="宠物身体不适" value="宠物身体不适" />
              <el-option label="已经找到其他服务" value="已经找到其他服务" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
            <!-- 商品订单原因选项 -->
            <el-select v-else v-model="cancelReason" placeholder="请选择取消原因" style="width: 100%">
              <el-option label="不想要了" value="不想要了" />
              <el-option label="信息填写错误" value="信息填写错误" />
              <el-option label="价格太贵" value="价格太贵" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
          </el-form-item>
          <el-form-item label="详细说明" v-if="cancelReason === '其他原因'">
            <el-input v-model="customReason" type="textarea" :rows="3" placeholder="请输入详细原因" maxlength="200" show-word-limit />
          </el-form-item>
          <div class="refund-tip" v-if="currentCancelItem && ((currentCancelItem.orderStatus === 1) || (currentCancelItem.status === 1 && currentCancelItem.payStatus === 1))">
            <i class="el-icon-info"></i>
            <span>该订单已支付，取消后将自动退款，金额将原路返回</span>
          </div>
        </el-form>
      </div>
      <span slot="footer">
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCancel" :loading="cancelLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 商品评价对话框 -->
    <el-dialog title="发表评价" :visible.sync="commentDialogVisible" width="550px" :close-on-click-modal="false" center class="comment-dialog">
      <div class="comment-dialog-content">
        <div class="product-selector" v-if="currentOrder && currentOrder.items && currentOrder.items.length > 1">
          <div class="selector-label">选择要评价的商品：</div>
          <div class="product-options">
            <div v-for="item in currentOrder.items" :key="item.productId" :class="['product-option', { active: selectedProductId === item.productId }]" @click="selectedProductId = item.productId">
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
            <div class="rating-wrapper"><el-rate v-model="commentForm.rating" :texts="['很差', '较差', '一般', '推荐', '超赞']" show-text :colors="['#f56c6c', '#e6a23c', '#67c23a']" /></div>
          </el-form-item>
          <el-form-item label="评价内容" prop="content">
            <el-input type="textarea" v-model="commentForm.content" rows="4" placeholder="说说你的使用感受..." maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="上传图片">
            <el-upload action="#" :http-request="uploadCommentImage" list-type="picture-card" :on-preview="handlePictureCardPreview" :on-remove="handleRemove" :file-list="commentForm.imageList" :limit="5">
              <i class="el-icon-plus" />
            </el-upload>
            <div class="upload-tip">支持 JPG、PNG 格式，最多5张，每张不超过2MB</div>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer"><el-button @click="commentDialogVisible = false">取消</el-button><el-button type="primary" @click="submitComment" :loading="commentSubmitting">提交评价</el-button></span>
    </el-dialog>

    <!-- 服务评价对话框 -->
    <el-dialog title="评价服务" :visible.sync="serviceCommentDialogVisible" width="550px" center class="comment-dialog" :close-on-click-modal="false">
      <div class="comment-dialog-content">
        <div class="comment-service" v-if="currentAppointment">
          <div class="service-image-wrapper">
            <img v-if="currentAppointment.serviceImage" :src="currentAppointment.serviceImage" class="service-img">
            <div v-else class="image-placeholder"><i class="el-icon-service"></i></div>
          </div>
          <div class="service-info">
            <h4>{{ currentAppointment.serviceName }}</h4>
            <div class="service-meta"><span><i class="el-icon-s-custom"></i> {{ currentAppointment.petName }}</span><span class="price">¥{{ currentAppointment.servicePrice }}</span></div>
          </div>
        </div>
        <el-form :model="serviceCommentForm" :rules="commentRules" ref="serviceCommentForm" label-width="80px">
          <el-form-item label="评分" prop="rating">
            <div class="rating-wrapper"><el-rate v-model="serviceCommentForm.rating" :texts="['很差', '较差', '一般', '推荐', '超赞']" show-text :colors="['#f56c6c', '#e6a23c', '#67c23a']" /></div>
          </el-form-item>
          <el-form-item label="评价内容" prop="content">
            <el-input type="textarea" v-model="serviceCommentForm.content" rows="4" placeholder="说说你的服务体验..." maxlength="500" show-word-limit />
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer"><el-button @click="serviceCommentDialogVisible = false">取消</el-button><el-button type="primary" @click="submitServiceComment" :loading="serviceCommentSubmitting">提交评价</el-button></span>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="orderDetailVisible" width="600px" center class="order-detail-dialog">
      <div class="order-detail-content" v-if="detailOrder">
        <div class="detail-section"><div class="section-title"><i class="el-icon-info"></i> 订单信息</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="订单号">{{ detailOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态"><span :class="['status-badge', getStatusClass(detailOrder.orderStatus)]">{{ getStatusText(detailOrder.orderStatus) }}</span></el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatDateTime(detailOrder.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ formatDateTime(detailOrder.payTime) || '--' }}</el-descriptions-item>
            <el-descriptions-item label="收货人">{{ detailOrder.userName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detailOrder.userPhone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ detailOrder.userAddress }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detailOrder.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-section"><div class="section-title"><i class="el-icon-goods"></i> 商品清单</div>
          <div class="product-list"><div class="product-item-detail" v-for="item in detailOrder.items" :key="item.id"><img :src="item.productImage"><div class="item-info"><div class="item-name">{{ item.productName }}</div><div class="item-price">¥{{ item.price }} x {{ item.quantity }}</div></div><div class="item-total">¥{{ item.totalAmount }}</div></div></div>
        </div>
        <div class="detail-section"><div class="section-title"><i class="el-icon-money"></i> 费用信息</div>
          <div class="amount-summary"><div class="amount-row"><span>商品总价</span><span>¥{{ detailOrder.totalAmount }}</span></div><div class="amount-row total"><span>实付金额</span><span class="total-price">¥{{ detailOrder.payAmount }}</span></div></div>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="orderDetailVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 预约详情对话框 -->
    <el-dialog title="预约详情" :visible.sync="appointmentDetailVisible" width="500px" center class="detail-dialog">
      <div class="detail-content" v-if="currentAppointment">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="预约编号">{{ currentAppointment.appointmentNo }}</el-descriptions-item>
          <el-descriptions-item label="服务名称">{{ currentAppointment.serviceName }}</el-descriptions-item>
          <el-descriptions-item label="服务金额"><span class="detail-price">¥{{ currentAppointment.servicePrice }}</span></el-descriptions-item>
          <el-descriptions-item label="预约宠物">{{ currentAppointment.petName }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ formatDate(currentAppointment.appointmentDate) }} {{ currentAppointment.appointmentTime }}</el-descriptions-item>
          <el-descriptions-item label="服务人员">{{ currentAppointment.staffName || '待分配' }}</el-descriptions-item>
          <el-descriptions-item label="预约状态"><span :class="['status-badge', getAppointmentStatusClass(currentAppointment.status)]">{{ getAppointmentStatusText(currentAppointment.status) }}</span></el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDateTime(currentAppointment.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentAppointment.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer"><el-button type="primary" @click="appointmentDetailVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 商品订单查看评价对话框 -->
    <el-dialog title="我的评价" :visible.sync="viewCommentVisible" width="500px" center class="view-comment-dialog">
      <div class="view-comment-content" v-if="viewCommentData">
        <div class="view-product" v-if="viewProduct">
          <img :src="viewProduct.productImage" :alt="viewProduct.productName">
          <div class="product-info">
            <h4>{{ viewProduct.productName }}</h4>
            <div class="product-price">¥{{ viewProduct.price }}</div>
          </div>
        </div>
        <div class="view-comment">
          <div class="comment-rating"><el-rate v-model="viewCommentData.rating" disabled show-score text-color="#ff9900" /></div>
          <div class="comment-text">{{ viewCommentData.content }}</div>
          <div class="comment-images" v-if="viewCommentData.images">
            <el-image v-for="(img, idx) in viewCommentData.images.split(',')" :key="idx" :src="img" :preview-src-list="viewCommentData.images.split(',')" fit="cover" class="comment-img" />
          </div>
          <div class="comment-time">{{ formatDate(viewCommentData.createTime) }}</div>
          <div class="comment-reply" v-if="viewCommentData.reply">
            <div class="reply-header"><i class="el-icon-chat-dot-round"></i><span>商家回复</span></div>
            <div class="reply-content">{{ viewCommentData.reply }}</div>
            <div class="reply-time">{{ formatDate(viewCommentData.replyTime) }}</div>
          </div>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="viewCommentVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 服务预约查看评价对话框 -->
    <el-dialog title="我的评价" :visible.sync="viewServiceCommentVisible" width="500px" center class="view-comment-dialog">
      <div class="view-comment-content" v-if="viewServiceCommentData">
        <div class="view-product" v-if="viewServiceCommentData">
          <img v-if="viewServiceCommentData.serviceImage" :src="viewServiceCommentData.serviceImage" :alt="viewServiceCommentData.serviceName">
          <div v-else class="image-placeholder"><i class="el-icon-service"></i></div>
          <div class="product-info">
            <h4>{{ viewServiceCommentData.serviceName }}</h4>
            <div class="product-price">¥{{ viewServiceCommentData.servicePrice }}</div>
          </div>
        </div>
        <div class="view-comment">
          <div class="comment-rating"><el-rate v-model="viewServiceCommentData.rating" disabled show-score text-color="#ff9900" /></div>
          <div class="comment-text">{{ viewServiceCommentData.content }}</div>
          <div class="comment-time">{{ formatDate(viewServiceCommentData.createTime) }}</div>
          <div class="comment-reply" v-if="viewServiceCommentData.reply">
            <div class="reply-header"><i class="el-icon-chat-dot-round"></i><span>商家回复</span></div>
            <div class="reply-content">{{ viewServiceCommentData.reply }}</div>
            <div class="reply-time">{{ formatDate(viewServiceCommentData.replyTime) }}</div>
          </div>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="viewServiceCommentVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog :visible.sync="previewVisible" width="400px" append-to-body><img :src="previewImage" style="width: 100%" alt="预览图片"></el-dialog>
  </div>
</template>

<script>
import { getOrderList, cancelOrder, updateOrderStatus, refundOrder } from '@/api/order';
import { getUserAppointments, cancelAppointment, addServiceComment, getServiceCommentByAppointment, refundAppointment } from '@/api/service';
import { addComment, getProductComments } from '@/api/comment';
import { uploadCommentImage as uploadImage } from '@/api/upload';
import { addToCart } from '@/api/cart';

export default {
  name: 'Orders',
  data() {
    return {
      loading: false,
      commentSubmitting: false,
      serviceCommentSubmitting: false,
      cancelLoading: false,
      orderType: 'product',
      orderList: [],
      appointmentList: [],
      total: 0,
      page: 1,
      pageSize: 12,
      activeTab: '',
      productTabList: [
        { value: '', label: '全部', icon: 'el-icon-menu' },
        { value: 0, label: '待支付', icon: 'el-icon-time' },
        { value: 1, label: '已支付', icon: 'el-icon-success' },
        { value: 2, label: '已发货', icon: 'el-icon-truck' },
        { value: 3, label: '已完成', icon: 'el-icon-circle-check' },
        { value: 4, label: '已取消', icon: 'el-icon-circle-close' },
        { value: 5, label: '已退款', icon: 'el-icon-circle-close' }
      ],
      serviceTabList: [
        { value: '', label: '全部', icon: 'el-icon-menu' },
        { value: 0, label: '待确认', icon: 'el-icon-time' },
        { value: 1, label: '已确认', icon: 'el-icon-success' },
        { value: 2, label: '服务中', icon: 'el-icon-loading' },
        { value: 3, label: '已完成', icon: 'el-icon-circle-check' },
        { value: 4, label: '已取消', icon: 'el-icon-circle-close' },
        { value: 5, label: '已拒绝', icon: 'el-icon-circle-close' }  // 新增
      ],
      orderCounts: {},
      commentDialogVisible: false,
      serviceCommentDialogVisible: false,
      viewServiceCommentVisible: false,
      cancelDialogVisible: false,
      currentOrder: null,
      currentAppointment: null,
      currentCancelItem: null,
      currentCancelType: null,
      currentProduct: null,
      selectedProductId: null,
      viewServiceCommentData: null,
      cancelReason: '',
      customReason: '',
      commentForm: { rating: 5, content: '', imageList: [], imageUrls: [] },
      serviceCommentForm: { rating: 5, content: '' },
      commentRules: {
        rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
        content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
      },
      previewVisible: false,
      previewImage: '',
      viewCommentVisible: false,
      viewCommentData: null,
      viewProduct: null,
      orderDetailVisible: false,
      appointmentDetailVisible: false,
      detailOrder: null
    };
  },
  computed: {
    currentTabList() {
      return this.orderType === 'product' ? this.productTabList : this.serviceTabList;
    },
    dialogTitle() {
      if (!this.currentCancelItem) return '取消申请';
      if (this.currentCancelType === 'product') {
        return this.currentCancelItem.orderStatus === 1 ? '申请退款' : '取消订单';
      } else {
        return this.currentCancelItem.status === 1 && this.currentCancelItem.payStatus === 1 ? '申请退款' : '取消预约';
      }
    }
  },
  watch: {
    activeTab() { this.page = 1; this.loadData(); },
    orderType() { this.activeTab = ''; this.page = 1; this.loadData(); }
  },
  created() { this.loadData(); },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        if (this.orderType === 'product') {
          await this.loadOrders();
        } else {
          await this.loadAppointments();
        }
      } catch (error) {
        console.error('加载失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async loadOrders() {
      const res = await getOrderList({ page: this.page, pageSize: this.pageSize, status: this.activeTab !== '' ? this.activeTab : undefined });
      if (res.code === 200) {
        this.orderList = res.data.list;
        this.total = res.data.total;
        await this.loadOrderComments();
      }
    },
    async loadAppointments() {
      const res = await getUserAppointments({ page: this.page, pageSize: this.pageSize, status: this.activeTab !== '' ? this.activeTab : undefined });
      if (res.code === 200) {
        this.appointmentList = res.data.list || [];
        this.total = res.data.total || 0;
        await this.loadAppointmentComments();
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
              if (res.code === 200 && res.data.list) {
                const userComment = res.data.list.find(c => c.userId === currentUserId);
                if (userComment) { order.comment = userComment; order.commentStatus = 'commented'; break; }
              }
            } catch (error) { console.error('获取评价失败', error); }
          }
        }
      }
    },
    async loadAppointmentComments() {
      for (let appointment of this.appointmentList) {
        appointment.comment = null;
        if (appointment.status === 3) {
          try {
            const res = await getServiceCommentByAppointment(appointment.id);
            if (res.code === 200 && res.data) {
              appointment.comment = res.data;
              appointment.comment.serviceImage = appointment.serviceImage;
              appointment.comment.serviceName = appointment.serviceName;
              appointment.comment.servicePrice = appointment.servicePrice;
            }
          } catch (error) { console.error('获取服务评价失败', error); }
        }
      }
    },
    getCurrentUserId() {
      const userInfo = localStorage.getItem('userInfo');
      return userInfo ? JSON.parse(userInfo).id : null;
    },
    getOrderCount(status) { return this.orderCounts[status] || 0; },
    handlePageChange(page) { this.page = page; this.loadData(); },

    getStatusText(status) {
      const map = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消',
        5: '已退款'  // ✅ 新增
      };
      return map[status] || '未知';
    },
    getStatusClass(status) {
      const map = {
        0: 'status-pending',
        1: 'status-paid',
        2: 'status-shipped',
        3: 'status-completed',
        4: 'status-cancelled',
        5: 'status-refunded'
      };
      return map[status] || '';
    },
    getStatusIcon(status) { const map = {0: 'el-icon-time', 1: 'el-icon-success', 2: 'el-icon-truck', 3: 'el-icon-circle-check', 4: 'el-icon-circle-close'}; return map[status] || 'el-icon-info'; },

    getAppointmentStatusText(status) { const map = {0: '待确认', 1: '已确认', 2: '服务中', 3: '已完成', 4: '已取消', 5: '已拒绝'}; return map[status] || '未知'; },
    getAppointmentStatusClass(status) { const map = {0: 'status-pending', 1: 'status-confirmed', 2: 'status-servicing', 3: 'status-completed', 4: 'status-cancelled', 5: 'status-rejected'}; return map[status] || ''; },
    getAppointmentStatusIcon(status) { const map = {0: 'el-icon-time', 1: 'el-icon-success', 2: 'el-icon-loading', 3: 'el-icon-circle-check', 4: 'el-icon-circle-close', 5: 'el-icon-close'}; return map[status] || 'el-icon-info'; },

    formatDate(date) { if (!date) return ''; const d = new Date(date); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`; },
    formatDateTime(date) { if (!date) return ''; const d = new Date(date); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`; },
    formatRelativeTime(date) { if (!date) return ''; const d = new Date(date); const now = new Date(); const diff = now - d; if (diff < 60000) return '刚刚'; if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`; if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`; if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`; return this.formatDate(date); },

    openCancelDialog(item, type) {
      this.currentCancelItem = item;
      this.currentCancelType = type;
      this.cancelReason = '';
      this.customReason = '';
      this.cancelDialogVisible = true;
    },

    async submitCancel() {
      let reason = this.cancelReason;
      if (reason === '其他原因') {
        reason = this.customReason;
        if (!reason) {
          this.$message.warning('请输入取消原因');
          return;
        }
      }
      if (!reason) {
        this.$message.warning('请选择取消原因');
        return;
      }

      this.cancelLoading = true;
      try {
        let res;
        if (this.currentCancelType === 'product') {
          res = await cancelOrder(this.currentCancelItem.orderNo, reason);
        } else {
          res = await cancelAppointment(this.currentCancelItem.id, reason);
        }

        if (res.code === 200) {
          this.$message.success(res.message || '操作成功');
          this.cancelDialogVisible = false;
          await this.loadData();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('取消失败', error);
        this.$message.error(error.message || '操作失败');
      } finally {
        this.cancelLoading = false;
      }
    },

    goToProduct(productId) { if (productId) this.$router.push(`/product/${productId}`); },
    goToPay(orderNo) { this.$router.push(`/pay/${orderNo}`); },

    async confirmReceipt(order) {
      if (order.orderStatus !== 2) {
        this.$message.warning('当前状态无法确认收货');
        return;
      }
      this.$confirm('确认已收到商品？确认后可以评价商品哦！', '提示', {
        type: 'info',
        confirmButtonText: '确认收货',
        cancelButtonText: '再等等'
      }).then(async () => {
        try {
          await updateOrderStatus(order.orderNo, 3);
          this.$message.success('确认收货成功！快去评价吧~');
          await this.loadData();
          setTimeout(() => this.openCommentDialog(order), 500);
        } catch (error) {
          this.$message.error('操作失败');
        }
      }).catch(() => {});
    },

    openCommentDialog(order) { this.currentOrder = order; this.selectedProductId = order.items[0]?.productId; this.currentProduct = order.items[0]; this.resetCommentForm(); this.commentDialogVisible = true; },
    resetCommentForm() { this.commentForm = { rating: 5, content: '', imageList: [], imageUrls: [] }; this.$nextTick(() => { this.$refs.commentForm?.clearValidate(); }); },

    async uploadCommentImage(file) { const formData = new FormData(); formData.append('file', file.file); try { const res = await uploadImage(formData); if (res.code === 200) { this.commentForm.imageUrls.push(res.data.url); this.commentForm.imageList.push({ uid: Date.now(), name: file.file.name, url: res.data.url }); } } catch (error) { this.$message.error('上传失败'); } },
    handlePictureCardPreview(file) { this.previewImage = file.url; this.previewVisible = true; },
    handleRemove(file) { const index = this.commentForm.imageList.findIndex(f => f.uid === file.uid); if (index !== -1) { this.commentForm.imageList.splice(index, 1); this.commentForm.imageUrls.splice(index, 1); } },

    async submitComment() { this.$refs.commentForm.validate(async (valid) => { if (!valid) return; this.commentSubmitting = true; try { const res = await addComment({ orderNo: this.currentOrder.orderNo, productId: this.currentProduct.productId, rating: this.commentForm.rating, content: this.commentForm.content, images: this.commentForm.imageUrls.join(',') }); if (res.code === 200) { this.$message.success('评价成功！'); this.commentDialogVisible = false; await this.loadData(); } } catch (error) { this.$message.error('评价失败'); } finally { this.commentSubmitting = false; } }); },

    viewComment(order) { this.viewCommentData = order.comment; this.viewProduct = order.items[0]; this.viewCommentVisible = true; },
    viewOrderDetail(order) { this.detailOrder = order; this.orderDetailVisible = true; },

    async buyAgain(order) { try { for (const item of order.items) { await addToCart(item.productId, item.quantity); } this.$message.success('已加入购物车'); this.$bus.$emit('cart-updated'); } catch (error) { this.$message.error('添加失败'); } },

    goToAppointmentPay(appointment) { this.$router.push(`/pay/${appointment.appointmentNo}`); },

    openServiceCommentDialog(appointment) { this.currentAppointment = appointment; this.serviceCommentForm = { rating: 5, content: '' }; this.serviceCommentDialogVisible = true; this.$nextTick(() => { this.$refs.serviceCommentForm?.clearValidate(); }); },

    async submitServiceComment() { this.$refs.serviceCommentForm.validate(async (valid) => { if (!valid) return; this.serviceCommentSubmitting = true; try { const res = await addServiceComment({ appointmentId: this.currentAppointment.id, serviceId: this.currentAppointment.serviceId, rating: this.serviceCommentForm.rating, content: this.serviceCommentForm.content }); if (res.code === 200) { this.$message.success('评价成功！'); this.serviceCommentDialogVisible = false; await this.loadData(); } } catch (error) { this.$message.error('评价失败'); } finally { this.serviceCommentSubmitting = false; } }); },

    viewServiceComment(appointment) { this.viewServiceCommentData = appointment.comment; if (this.viewServiceCommentData) { this.viewServiceCommentData.serviceImage = appointment.serviceImage; this.viewServiceCommentData.serviceName = appointment.serviceName; this.viewServiceCommentData.servicePrice = appointment.servicePrice; } this.viewServiceCommentVisible = true; },
    viewAppointmentDetail(appointment) { this.currentAppointment = appointment; this.appointmentDetailVisible = true; }
  }
};
</script>

<style scoped>
.orders-page { padding: 0; background: transparent; min-height: 400px; }
.page-title { font-size: 24px; font-weight: 600; color: #2c3e50; margin-bottom: 24px; padding-bottom: 12px; border-bottom: 2px solid #f0f0f0; }

/* 订单类型切换 */
.orders-type-tabs { display: flex; gap: 12px; margin-bottom: 20px; }
.type-tab { display: flex; align-items: center; gap: 6px; padding: 8px 24px; border-radius: 30px; cursor: pointer; font-size: 14px; font-weight: 500; color: #666; background: #fff; border: 1px solid #e0e0e0; transition: all 0.3s; }
.type-tab:hover { border-color: #667eea; color: #667eea; }
.type-tab.active { background: linear-gradient(135deg, #667eea, #764ba2); border-color: transparent; color: white; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3); }

/* 订单状态标签页 */
.orders-tabs { display: flex; gap: 8px; margin-bottom: 24px; flex-wrap: wrap; }
.tab-item { display: flex; align-items: center; gap: 6px; cursor: pointer; padding: 6px 16px; border-radius: 30px; transition: all 0.3s; font-size: 13px; font-weight: 500; color: #666; background: #f5f5f5; }
.tab-item i { font-size: 14px; }
.tab-item:hover { background: #e8e8e8; color: #667eea; }
.tab-item.active { background: linear-gradient(135deg, #667eea, #764ba2); color: white; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3); }
.tab-count { background: rgba(255,255,255,0.2); border-radius: 20px; padding: 2px 6px; font-size: 11px; margin-left: 4px; }

/* 订单网格 */
.orders-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.order-card { background: #fff; border-radius: 12px; overflow: hidden; transition: all 0.3s; border: 1px solid #eee; display: flex; flex-direction: column; }
.order-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1); }

.card-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #fafbfc; border-bottom: 1px solid #f0f0f0; gap: 12px; }
.order-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; flex: 1; }
.order-no { font-weight: 600; color: #2c3e50; font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.order-no i { margin-right: 4px; color: #667eea; }
.order-time { font-size: 11px; color: #999; white-space: nowrap; }
.order-status { display: inline-flex; align-items: center; gap: 4px; font-weight: 500; font-size: 11px; padding: 4px 10px; border-radius: 20px; white-space: nowrap; flex-shrink: 0; }
.status-pending { color: #e6a23c; background: #fdf6ec; }
.status-paid { color: #409EFF; background: #ecf5ff; }
.status-shipped { color: #67c23a; background: #f0f9f4; }
.status-completed { color: #909399; background: #f4f4f5; }
.status-cancelled { color: #f56c6c; background: #fef0f0; }
.status-servicing { color: #67c23a; background: #f0f9f4; }
.status-rejected { color: #f56c6c; background: #fef0f0; }
.status-confirmed { color: #409EFF; background: #ecf5ff; }

.card-body { display: flex; padding: 16px; gap: 16px; flex: 1; }
.product-image { position: relative; width: 80px; height: 80px; flex-shrink: 0; border-radius: 8px; overflow: hidden; background: #f5f5f5; }
.product-image img { width: 100%; height: 100%; object-fit: cover; }
.product-count { position: absolute; bottom: 4px; right: 4px; background: rgba(0,0,0,0.6); color: white; font-size: 10px; padding: 2px 6px; border-radius: 10px; }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #f5f7fa, #e8eaef); color: #bbb; font-size: 32px; }
.order-info-detail { flex: 1; }
.product-name { font-size: 14px; font-weight: 600; color: #2c3e50; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.order-amount, .appointment-info { font-size: 12px; color: #999; margin-bottom: 4px; }
.appointment-info { display: flex; flex-direction: column; gap: 2px; }
.appointment-info i { margin-right: 4px; color: #667eea; }
.order-total { font-size: 13px; color: #666; margin-top: 6px; }
.total-price { color: #ff6b6b; font-weight: 600; font-size: 15px; }

/* 取消原因样式 */
.cancel-reason {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  color: #f56c6c;
  font-size: 12px;
}

.cancel-reason i {
  font-size: 14px;
  color: #f56c6c;
}

.card-footer { display: flex; gap: 8px; padding: 12px 16px; border-top: 1px solid #f0f0f0; flex-wrap: wrap; align-items: center; }
.card-footer .el-button { flex: 1; padding: 6px 0; font-size: 12px; }
.status-tag { flex: 1; text-align: center; }

.empty-state { grid-column: 1 / -1; text-align: center; padding: 60px; background: #fff; border-radius: 12px; color: #999; }
.empty-state i { font-size: 64px; margin-bottom: 16px; color: #ddd; }
.pagination { grid-column: 1 / -1; display: flex; justify-content: center; margin-top: 20px; }

/* 取消对话框 */
.cancel-dialog ::v-deep .el-dialog { border-radius: 20px; overflow: hidden; }
.cancel-dialog ::v-deep .el-dialog__header { background: linear-gradient(135deg, #667eea, #764ba2); padding: 20px 24px; margin: 0; }
.cancel-dialog ::v-deep .el-dialog__title { color: white; font-weight: 600; font-size: 18px; }
.cancel-content { padding: 10px 0; }
.cancel-info { display: flex; align-items: center; gap: 8px; padding: 12px; background: #f8f9fc; border-radius: 8px; margin-bottom: 20px; color: #666; font-size: 13px; }
.cancel-info i { color: #e6a23c; font-size: 18px; }
.refund-tip { display: flex; align-items: center; gap: 8px; padding: 12px; background: #fef0f0; border-radius: 8px; margin-top: 16px; color: #f56c6c; font-size: 12px; }
.refund-tip i { font-size: 16px; }

/* 对话框通用样式 */
.comment-dialog ::v-deep .el-dialog,
.order-detail-dialog ::v-deep .el-dialog,
.detail-dialog ::v-deep .el-dialog,
.view-comment-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}
.comment-dialog ::v-deep .el-dialog__header,
.order-detail-dialog ::v-deep .el-dialog__header,
.detail-dialog ::v-deep .el-dialog__header,
.view-comment-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}
.comment-dialog ::v-deep .el-dialog__title,
.order-detail-dialog ::v-deep .el-dialog__title,
.detail-dialog ::v-deep .el-dialog__title,
.view-comment-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}
.comment-dialog-content { padding: 10px 0; }
.comment-product, .comment-service { display: flex; gap: 16px; padding: 16px; background: #f8f9fc; border-radius: 16px; margin-bottom: 24px; }
.comment-product img, .service-img { width: 70px; height: 70px; border-radius: 12px; object-fit: cover; }
.product-options { display: flex; flex-direction: column; gap: 10px; }
.product-option { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border: 1px solid #e0e0e0; border-radius: 12px; cursor: pointer; transition: all 0.3s; }
.product-option:hover, .product-option.active { border-color: #667eea; background: #f8f9ff; }
.product-option img { width: 50px; height: 50px; border-radius: 8px; object-fit: cover; }
.product-option i { color: #67c23a; font-size: 18px; }
.upload-tip { font-size: 12px; color: #999; margin-top: 8px; }
.detail-content { padding: 10px; }
.detail-price { color: #ff6b6b; font-weight: 600; }
.status-badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.product-list { display: flex; flex-direction: column; gap: 12px; }
.product-item-detail { display: flex; align-items: center; gap: 12px; padding: 12px; background: #f8f9fc; border-radius: 12px; }
.product-item-detail img { width: 60px; height: 60px; border-radius: 8px; object-fit: cover; }
.amount-summary { background: #f8f9fc; border-radius: 12px; padding: 16px; }
.amount-row { display: flex; justify-content: space-between; padding: 8px 0; }
.amount-row.total { margin-top: 8px; padding-top: 12px; border-top: 1px solid #e0e0e0; font-weight: 600; }
.total-price { color: #ff6b6b; font-size: 20px; font-weight: bold; }

/* 查看评价对话框 - 图片样式 */
.view-comment-content { padding: 10px 0; }
.view-product { display: flex; gap: 16px; padding: 16px; background: #f8f9fc; border-radius: 16px; margin-bottom: 24px; align-items: center; }
.view-product img { width: 70px; height: 70px; border-radius: 12px; object-fit: cover; flex-shrink: 0; }
.view-product .image-placeholder { width: 70px; height: 70px; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #f5f7fa, #e8eaef); border-radius: 12px; color: #bbb; font-size: 32px; flex-shrink: 0; }
.view-product .product-info h4 { font-size: 14px; margin-bottom: 6px; color: #2c3e50; }
.view-product .product-price { color: #ff6b6b; font-weight: 600; }

.view-comment { padding: 16px; }
.view-comment .comment-text { margin: 12px 0; padding: 12px; background: #f8f9fc; border-radius: 12px; line-height: 1.6; font-size: 14px; }
.view-comment .comment-time { font-size: 12px; color: #999; margin-top: 8px; text-align: right; }

/* 评价图片 - 限制60x60 */
.view-comment .comment-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 12px 0;
  align-items: flex-start;
}
.view-comment .comment-img {
  width: 60px !important;
  height: 60px !important;
  min-width: 60px;
  max-width: 60px;
  min-height: 60px;
  max-height: 60px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
  border: 1px solid #f0f0f0;
  flex-shrink: 0;
  overflow: hidden;
  display: inline-block;
}
.view-comment .comment-img >>> .el-image__inner,
.view-comment .comment-img /deep/ .el-image__inner,
.view-comment .comment-img ::v-deep .el-image__inner {
  width: 60px !important;
  height: 60px !important;
  object-fit: cover !important;
  border-radius: 8px;
}
.view-comment .comment-img:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.view-comment .comment-reply { background: #fff; border-radius: 12px; padding: 12px 16px; margin-top: 12px; border-left: 3px solid #67c23a; }
.view-comment .reply-header { display: flex; align-items: center; gap: 6px; font-weight: 500; color: #67c23a; margin-bottom: 8px; font-size: 13px; }
.view-comment .reply-content { color: #5a6874; line-height: 1.5; margin-bottom: 6px; font-size: 13px; }
.view-comment .reply-time { font-size: 11px; color: #999; text-align: right; }

@media (max-width: 900px) { .orders-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) {
  .orders-grid { grid-template-columns: 1fr; }
  .card-body { flex-direction: column; align-items: center; text-align: center; }
  .view-product { flex-direction: column; text-align: center; }
  .view-comment .comment-images { justify-content: center; }
}
/* 备注信息样式 */
.remark-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.remark-info i {
  color: #667eea;
  font-size: 12px;
}
.status-refunded {
  color: #909399;
  background: #f4f4f5;
}
</style>