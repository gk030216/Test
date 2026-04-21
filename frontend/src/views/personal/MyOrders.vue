<template>
  <div class="orders-page">
    <h2 class="page-title">我的订单</h2>

    <!-- 订单类型栏 -->
    <div class="orders-type-bar">
      <div class="type-tabs">
        <div class="type-tab" :class="{ active: orderType === 'all' }" @click="showAllOrders">
          <i class="el-icon-menu"></i> 全部订单
        </div>
        <div class="type-tab" :class="{ active: orderType === 'product' }" @click="orderType = 'product'">
          <i class="el-icon-goods"></i> 商品订单
        </div>
        <div class="type-tab" :class="{ active: orderType === 'service' }" @click="orderType = 'service'">
          <i class="el-icon-service"></i> 服务预约
        </div>
      </div>

      <div class="search-wrapper">
        <i class="el-icon-search search-icon"></i>
        <el-input
            v-model="searchKeyword"
            placeholder="搜索订单号/商品名/服务名"
            size="medium"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
            class="search-input"
        ></el-input>
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
      <!-- 全部订单卡片 -->
      <div class="order-card" v-for="order in allOrdersList" :key="order.orderNo || order.id" v-if="orderType === 'all'">
        <!-- 商品订单卡片 -->
        <template v-if="order._type === 'product'">
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
              <img :src="order.items[0].productImage" :alt="order.items[0].productName" @error="handleImageError">
              <span class="product-count" v-if="order.items.length > 1">+{{ order.items.length - 1 }}</span>
            </div>
            <div class="order-info-detail">
              <div class="product-name">{{ order.items && order.items[0] ? order.items[0].productName : '商品' }}</div>
              <div class="service-meta">
                <span><i class="el-icon-shopping-cart-2"></i> 共 {{ order.items ? order.items.length : 0 }} 件商品</span>
                <span class="price"><i class="el-icon-money"></i> ¥{{ order.payAmount }}</span>
              </div>
              <div class="cancel-reason" v-if="order.orderStatus === 4 && order.cancelReason">
                <i class="el-icon-info"></i>
                <span>取消原因：{{ order.cancelReason }}</span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <el-button size="small" plain @click="viewOrderDetail(order)">查看详情</el-button>
            <template v-if="order.orderStatus === 0">
              <el-button size="small" plain class="cancel-btn" @click="openCancelDialog(order, 'product')">取消</el-button>
              <el-button size="small" class="pay-btn" @click="goToPay(order.orderNo)">去支付</el-button>
            </template>
            <template v-if="order.orderStatus === 1">
              <el-tag type="info" size="small" class="status-tag">待发货</el-tag>
              <el-button size="small" plain class="refund-btn" @click="openCancelDialog(order, 'product')">申请退款</el-button>
            </template>
            <template v-if="order.orderStatus === 2">
              <el-button size="small" class="confirm-btn" @click="confirmReceipt(order)">确认收货</el-button>
            </template>
            <template v-if="order.orderStatus === 3">
              <el-button v-if="order.commentStatus === 'uncommented'" size="small" plain class="comment-btn" @click="openCommentDialog(order)">评价</el-button>
              <el-button v-if="order.commentStatus === 'commented'" size="small" plain class="view-btn" @click="viewComment(order)">查看评价</el-button>
              <el-button size="small" plain class="buyagain-btn" @click="buyAgain(order)">再次购买</el-button>
            </template>
            <template v-if="order.orderStatus === 4 || order.orderStatus === 5">
              <el-tag :type="order.orderStatus === 4 ? 'danger' : 'info'" size="small" class="status-tag">
                {{ order.orderStatus === 4 ? '已取消' : '已退款' }}
              </el-tag>
            </template>
          </div>
        </template>

        <!-- 服务预约卡片 -->
        <template v-else>
          <div class="card-header">
            <div class="order-info">
              <span class="order-no"><i class="el-icon-document"></i> {{ order.appointmentNo }}</span>
              <span class="order-time"><i class="el-icon-time"></i> {{ formatRelativeTime(order.createTime) }}</span>
            </div>
            <div class="order-status" :class="getAppointmentStatusClass(order.status)">
              <i :class="getAppointmentStatusIcon(order.status)"></i> {{ getAppointmentStatusText(order.status) }}
            </div>
          </div>
          <div class="card-body">
            <div class="product-image">
              <img v-if="order.serviceImage" :src="order.serviceImage" :alt="order.serviceName" @error="handleImageError">
              <div v-else class="image-placeholder"><i class="el-icon-service"></i></div>
            </div>
            <div class="order-info-detail">
              <div class="product-name">{{ order.serviceName }}</div>
              <div class="service-meta">
                <span><i class="el-icon-s-custom"></i> {{ order.petName || '未指定' }}</span>
                <span class="price"><i class="el-icon-money"></i> ¥{{ order.servicePrice }}</span>
              </div>
              <div class="appointment-time-info">
                <i class="el-icon-date"></i>
                <span>{{ formatDate(order.appointmentDate) }} {{ order.appointmentTime }}</span>
              </div>
              <div class="staff-info" v-if="order.staffName">
                <i class="el-icon-user"></i>
                <span>{{ order.staffName }}</span>
              </div>
              <div class="remark-info" v-if="order.remark">
                <i class="el-icon-edit"></i>
                <span>{{ order.remark }}</span>
              </div>
              <div class="cancel-reason" v-if="order.status === 4 && order.cancelReason">
                <i class="el-icon-info"></i>
                <span>取消原因：{{ order.cancelReason }}</span>
              </div>
              <div class="cancel-reason" v-if="order.status === 5 && order.cancelReason">
                <i class="el-icon-info"></i>
                <span>拒绝原因：{{ order.cancelReason }}</span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <el-button size="small" plain @click="viewAppointmentDetail(order)">查看详情</el-button>
            <template v-if="order.status === 0 && order.payStatus !== 1">
              <el-button size="small" plain class="cancel-btn" @click="openCancelDialog(order, 'service')">取消</el-button>
              <el-button size="small" class="pay-btn" @click="goToAppointmentPay(order)">去支付</el-button>
            </template>
            <template v-if="order.status === 1 && order.payStatus === 1">
              <el-tag type="info" size="small" class="status-tag">待服务</el-tag>
              <el-button size="small" plain class="refund-btn" @click="openCancelDialog(order, 'service')">申请退款</el-button>
            </template>
            <template v-if="order.status === 2">
              <el-tag type="success" size="small" class="status-tag">服务中</el-tag>
            </template>
            <template v-if="order.status === 3">
              <el-button v-if="!order.comment" size="small" plain class="comment-btn" @click="openServiceCommentDialog(order)">评价</el-button>
              <el-button v-if="order.comment" size="small" plain class="view-btn" @click="viewServiceComment(order)">查看评价</el-button>
            </template>
            <template v-if="order.status === 4 || order.status === 5">
              <el-tag type="danger" size="small" class="status-tag">{{ order.status === 4 ? '已取消' : '已拒绝' }}</el-tag>
            </template>
          </div>
        </template>
      </div>

      <!-- 商品订单卡片 -->
      <div class="order-card" v-for="order in filteredOrderList" :key="order.orderNo" v-if="orderType === 'product'">
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
            <img :src="order.items[0].productImage" :alt="order.items[0].productName" @error="handleImageError">
            <span class="product-count" v-if="order.items.length > 1">+{{ order.items.length - 1 }}</span>
          </div>
          <div class="order-info-detail">
            <div class="product-name">{{ order.items && order.items[0] ? order.items[0].productName : '商品' }}</div>
            <div class="service-meta">
              <span><i class="el-icon-shopping-cart-2"></i> 共 {{ order.items ? order.items.length : 0 }} 件商品</span>
              <span class="price"><i class="el-icon-money"></i> ¥{{ order.payAmount }}</span>
            </div>
            <div class="cancel-reason" v-if="order.orderStatus === 4 && order.cancelReason">
              <i class="el-icon-info"></i>
              <span>取消原因：{{ order.cancelReason }}</span>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <el-button size="small" plain @click="viewOrderDetail(order)">查看详情</el-button>
          <template v-if="order.orderStatus === 0">
            <el-button size="small" plain class="cancel-btn" @click="openCancelDialog(order, 'product')">取消</el-button>
            <el-button size="small" class="pay-btn" @click="goToPay(order.orderNo)">去支付</el-button>
          </template>
          <template v-if="order.orderStatus === 1">
            <el-tag type="info" size="small" class="status-tag">待发货</el-tag>
            <el-button size="small" plain class="refund-btn" @click="openCancelDialog(order, 'product')">申请退款</el-button>
          </template>
          <template v-if="order.orderStatus === 2">
            <el-button size="small" class="confirm-btn" @click="confirmReceipt(order)">确认收货</el-button>
          </template>
          <template v-if="order.orderStatus === 3">
            <el-button v-if="order.commentStatus === 'uncommented'" size="small" plain class="comment-btn" @click="openCommentDialog(order)">评价</el-button>
            <el-button v-if="order.commentStatus === 'commented'" size="small" plain class="view-btn" @click="viewComment(order)">查看评价</el-button>
            <el-button size="small" plain class="buyagain-btn" @click="buyAgain(order)">再次购买</el-button>
          </template>
          <template v-if="order.orderStatus === 4 || order.orderStatus === 5">
            <el-tag :type="order.orderStatus === 4 ? 'danger' : 'info'" size="small" class="status-tag">
              {{ order.orderStatus === 4 ? '已取消' : '已退款' }}
            </el-tag>
          </template>
        </div>
      </div>

      <!-- 服务预约卡片 -->
      <div class="order-card" v-for="appointment in filteredAppointmentList" :key="appointment.id" v-if="orderType === 'service'">
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
            <img v-if="appointment.serviceImage" :src="appointment.serviceImage" :alt="appointment.serviceName" @error="handleImageError">
            <div v-else class="image-placeholder"><i class="el-icon-service"></i></div>
          </div>
          <div class="order-info-detail">
            <div class="product-name">{{ appointment.serviceName }}</div>
            <div class="service-meta">
              <span><i class="el-icon-s-custom"></i> {{ appointment.petName || '未指定' }}</span>
              <span class="price"><i class="el-icon-money"></i> ¥{{ appointment.servicePrice }}</span>
            </div>
            <div class="appointment-time-info">
              <i class="el-icon-date"></i>
              <span>{{ formatDate(appointment.appointmentDate) }} {{ appointment.appointmentTime }}</span>
            </div>
            <div class="staff-info" v-if="appointment.staffName">
              <i class="el-icon-user"></i>
              <span>{{ appointment.staffName }}</span>
            </div>
            <div class="remark-info" v-if="appointment.remark">
              <i class="el-icon-edit"></i>
              <span>{{ appointment.remark }}</span>
            </div>
            <div class="cancel-reason" v-if="appointment.status === 4 && appointment.cancelReason">
              <i class="el-icon-info"></i>
              <span>取消原因：{{ appointment.cancelReason }}</span>
            </div>
            <div class="cancel-reason" v-if="appointment.status === 5 && appointment.cancelReason">
              <i class="el-icon-info"></i>
              <span>拒绝原因：{{ appointment.cancelReason }}</span>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <el-button size="small" plain @click="viewAppointmentDetail(appointment)">查看详情</el-button>
          <template v-if="appointment.status === 0 && appointment.payStatus !== 1">
            <el-button size="small" plain class="cancel-btn" @click="openCancelDialog(appointment, 'service')">取消</el-button>
            <el-button size="small" class="pay-btn" @click="goToAppointmentPay(appointment)">去支付</el-button>
          </template>
          <template v-if="appointment.status === 1 && appointment.payStatus === 1">
            <el-tag type="info" size="small" class="status-tag">待服务</el-tag>
            <el-button size="small" plain class="refund-btn" @click="openCancelDialog(appointment, 'service')">申请退款</el-button>
          </template>
          <template v-if="appointment.status === 2">
            <el-tag type="success" size="small" class="status-tag">服务中</el-tag>
          </template>
          <template v-if="appointment.status === 3">
            <el-button v-if="!appointment.comment" size="small" plain class="comment-btn" @click="openServiceCommentDialog(appointment)">评价</el-button>
            <el-button v-if="appointment.comment" size="small" plain class="view-btn" @click="viewServiceComment(appointment)">查看评价</el-button>
          </template>
          <template v-if="appointment.status === 4 || appointment.status === 5">
            <el-tag type="danger" size="small" class="status-tag">{{ appointment.status === 4 ? '已取消' : '已拒绝' }}</el-tag>
          </template>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && ((orderType === 'product' && filteredOrderList.length === 0) || (orderType === 'service' && filteredAppointmentList.length === 0) || (orderType === 'all' && allOrdersList.length === 0))">
        <i class="el-icon-s-order"></i>
        <p>{{ searchKeyword ? '没有找到相关订单' : '暂无' + (orderType === 'product' ? '订单' : (orderType === 'service' ? '预约' : '订单')) }}</p>
        <el-button type="primary" size="small" @click="orderType === 'product' ? $router.push('/shop') : (orderType === 'service' ? $router.push('/services') : $router.push('/'))">
          {{ orderType === 'product' ? '去购物' : (orderType === 'service' ? '去预约' : '去逛逛') }}
        </el-button>
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination @current-change="handlePageChange" :current-page="page" :page-size="pageSize" layout="prev, pager, next" :total="total" background />
      </div>
    </div>

    <!-- 取消/退款对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="cancelDialogVisible" width="450px" center class="cancel-dialog" :close-on-click-modal="false">
      <div class="cancel-content">
        <div class="cancel-info">
          <i class="el-icon-warning"></i>
          <span>订单号：{{ currentCancelItem?.orderNo || currentCancelItem?.appointmentNo }}</span>
        </div>
        <el-form label-width="80px">
          <el-form-item label="取消原因">
            <el-select v-if="currentCancelType === 'service'" v-model="cancelReason" placeholder="请选择取消原因" style="width: 100%">
              <el-option label="时间冲突，改天再约" value="时间冲突，改天再约" />
              <el-option label="宠物身体不适" value="宠物身体不适" />
              <el-option label="已经找到其他服务" value="已经找到其他服务" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
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
      <span slot="footer"><el-button @click="cancelDialogVisible = false">取消</el-button><el-button type="primary" @click="submitCancel" :loading="cancelLoading">确定</el-button></span>
    </el-dialog>

    <!-- 商品评价对话框（支持上传图片） -->
    <el-dialog title="评价商品" :visible.sync="commentDialogVisible" width="550px" :close-on-click-modal="false" center class="comment-dialog">
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

    <!-- 服务评价对话框（支持上传图片） -->
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
          <el-form-item label="上传图片">
            <el-upload action="#" :http-request="uploadServiceCommentImage" list-type="picture-card" :on-preview="handlePictureCardPreview" :on-remove="handleServiceRemove" :file-list="serviceCommentForm.imageList" :limit="5">
              <i class="el-icon-plus" />
            </el-upload>
            <div class="upload-tip">支持 JPG、PNG 格式，最多5张，每张不超过2MB</div>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer"><el-button @click="serviceCommentDialogVisible = false">取消</el-button><el-button type="primary" @click="submitServiceComment" :loading="serviceCommentSubmitting">提交评价</el-button></span>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="orderDetailVisible" width="600px" center class="detail-dialog" :close-on-click-modal="false">
      <div class="detail-content" v-if="detailOrder">
        <div class="detail-section">
          <div class="section-title"><i class="el-icon-info"></i> 订单信息</div>
          <el-row :gutter="16">
            <el-col :span="12"><div class="info-item"><span class="info-label">订单号：</span><span class="info-value">{{ detailOrder.orderNo }}</span></div></el-col>
            <el-col :span="12"><div class="info-item"><span class="info-label">订单状态：</span><span :class="['status-badge', getStatusClass(detailOrder.orderStatus)]">{{ getStatusText(detailOrder.orderStatus) }}</span></div></el-col>
            <el-col :span="12"><div class="info-item"><span class="info-label">下单时间：</span><span class="info-value">{{ formatDateTime(detailOrder.createTime) }}</span></div></el-col>
            <el-col :span="12"><div class="info-item"><span class="info-label">支付时间：</span><span class="info-value">{{ formatDateTime(detailOrder.payTime) || '--' }}</span></div></el-col>
            <el-col :span="12"><div class="info-item"><span class="info-label">收货人：</span><span class="info-value">{{ detailOrder.userName }}</span></div></el-col>
            <el-col :span="12"><div class="info-item"><span class="info-label">联系电话：</span><span class="info-value">{{ detailOrder.userPhone }}</span></div></el-col>
            <el-col :span="24"><div class="info-item"><span class="info-label">收货地址：</span><span class="info-value">{{ detailOrder.userAddress }}</span></div></el-col>
            <el-col :span="24"><div class="info-item"><span class="info-label">备注：</span><span class="info-value">{{ detailOrder.remark || '无' }}</span></div></el-col>
          </el-row>
        </div>
        <div class="detail-section">
          <div class="section-title"><i class="el-icon-goods"></i> 商品清单</div>
          <div class="product-list">
            <div class="product-item-detail" v-for="item in detailOrder.items" :key="item.id">
              <img :src="item.productImage">
              <div class="item-info"><div class="item-name">{{ item.productName }}</div><div class="item-price">¥{{ item.price }} x {{ item.quantity }}</div></div>
              <div class="item-total">¥{{ item.totalAmount }}</div>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title"><i class="el-icon-money"></i> 费用信息</div>
          <div class="amount-summary">
            <div class="amount-row"><span>商品总价</span><span>¥{{ detailOrder.totalAmount }}</span></div>
            <div class="amount-row total"><span>实付金额</span><span class="total-price">¥{{ detailOrder.payAmount }}</span></div>
          </div>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="orderDetailVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 预约详情对话框 -->
    <el-dialog title="预约详情" :visible.sync="appointmentDetailVisible" width="600px" center class="detail-dialog" :close-on-click-modal="false">
      <div class="detail-content" v-if="currentAppointment">
        <div class="detail-section">
          <div class="section-title"><i class="el-icon-service"></i> 服务信息</div>
          <div class="service-card">
            <div class="service-img-wrapper">
              <img v-if="currentAppointment.serviceImage" :src="currentAppointment.serviceImage" :alt="currentAppointment.serviceName">
              <div v-else class="img-placeholder"><i class="el-icon-service"></i></div>
            </div>
            <div class="service-card-info">
              <h3>{{ currentAppointment.serviceName }}</h3>
              <div class="price-tag">¥{{ currentAppointment.servicePrice }}</div>
              <div class="staff-tag" v-if="currentAppointment.staffName"><i class="el-icon-user"></i> 服务人员：{{ currentAppointment.staffName }}</div>
              <div class="staff-tag" v-else><i class="el-icon-user"></i> 服务人员：待分配</div>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title"><i class="el-icon-info"></i> 预约信息</div>
          <el-row :gutter="16">
            <el-col :span="12"><div class="info-item"><span class="info-label">预约编号：</span><span class="info-value">{{ currentAppointment.appointmentNo }}</span></div></el-col>
            <el-col :span="12"><div class="info-item"><span class="info-label">预约状态：</span><span :class="['status-badge', getAppointmentStatusClass(currentAppointment.status)]">{{ getAppointmentStatusText(currentAppointment.status) }}</span></div></el-col>
            <el-col :span="12"><div class="info-item"><span class="info-label">预约宠物：</span><span class="info-value">{{ currentAppointment.petName || '未指定' }}</span></div></el-col>
            <el-col :span="12"><div class="info-item"><span class="info-label">预约时间：</span><span class="info-value">{{ formatDate(currentAppointment.appointmentDate) }} {{ currentAppointment.appointmentTime }}</span></div></el-col>
            <el-col :span="12"><div class="info-item"><span class="info-label">下单时间：</span><span class="info-value">{{ formatDateTime(currentAppointment.createTime) }}</span></div></el-col>
            <el-col :span="24"><div class="info-item" v-if="currentAppointment.remark"><span class="info-label">备注：</span><span class="info-value">{{ currentAppointment.remark }}</span></div></el-col>
            <el-col :span="24" v-if="currentAppointment.cancelReason"><div class="info-item"><span class="info-label">取消/拒绝原因：</span><span class="info-value cancel-text">{{ currentAppointment.cancelReason }}</span></div></el-col>
          </el-row>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="appointmentDetailVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 商品订单查看评价对话框 -->
    <el-dialog title="我的评价" :visible.sync="viewCommentVisible" width="550px" center class="view-comment-dialog" :close-on-click-modal="false">
      <div class="view-comment-content" v-if="viewCommentData">
        <div class="detail-section">
          <div class="section-title"><i class="el-icon-goods"></i> 商品信息</div>
          <div class="view-product">
            <img :src="viewProduct.productImage" :alt="viewProduct.productName">
            <div class="product-info">
              <h4>{{ viewProduct.productName }}</h4>
              <div class="product-price">¥{{ viewProduct.price }}</div>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title"><i class="el-icon-star-on"></i> 评价内容</div>
          <div class="rating-row"><span class="rating-label">评分：</span><el-rate v-model="viewCommentData.rating" disabled show-score text-color="#ff9900" /></div>
          <div class="comment-text">{{ viewCommentData.content }}</div>
          <div class="comment-time">{{ formatDate(viewCommentData.createTime) }}</div>
          <div class="comment-images" v-if="viewCommentData.images">
            <div class="images-title">评价图片：</div>
            <div class="image-list">
              <el-image v-for="(img, idx) in viewCommentData.images.split(',')" :key="idx" :src="img" :preview-src-list="viewCommentData.images.split(',')" fit="cover" class="comment-img" />
            </div>
          </div>
        </div>
        <div class="detail-section" v-if="viewCommentData.reply">
          <div class="section-title"><i class="el-icon-chat-dot-round"></i> 商家回复</div>
          <div class="reply-content">{{ viewCommentData.reply }}</div>
          <div class="reply-time">{{ formatDate(viewCommentData.replyTime) }}</div>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="viewCommentVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 服务预约查看评价对话框 -->
    <el-dialog title="我的评价" :visible.sync="viewServiceCommentVisible" width="550px" center class="view-comment-dialog" :close-on-click-modal="false">
      <div class="view-comment-content" v-if="viewServiceCommentData">
        <div class="detail-section">
          <div class="section-title"><i class="el-icon-service"></i> 服务信息</div>
          <div class="view-product">
            <img v-if="viewServiceCommentData.serviceImage" :src="viewServiceCommentData.serviceImage" :alt="viewServiceCommentData.serviceName">
            <div v-else class="image-placeholder"><i class="el-icon-service"></i></div>
            <div class="product-info">
              <h4>{{ viewServiceCommentData.serviceName }}</h4>
              <div class="product-price">¥{{ viewServiceCommentData.servicePrice }}</div>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title"><i class="el-icon-star-on"></i> 评价内容</div>
          <div class="rating-row"><span class="rating-label">评分：</span><el-rate v-model="viewServiceCommentData.rating" disabled show-score text-color="#ff9900" /></div>
          <div class="comment-text">{{ viewServiceCommentData.content }}</div>
          <div class="comment-time">{{ formatDate(viewServiceCommentData.createTime) }}</div>
          <div class="comment-images" v-if="viewServiceCommentData.images">
            <div class="images-title">评价图片：</div>
            <div class="image-list">
              <el-image v-for="(img, idx) in viewServiceCommentData.images.split(',')" :key="idx" :src="img" :preview-src-list="viewServiceCommentData.images.split(',')" fit="cover" class="comment-img" />
            </div>
          </div>
        </div>
        <div class="detail-section" v-if="viewServiceCommentData.reply">
          <div class="section-title"><i class="el-icon-chat-dot-round"></i> 商家回复</div>
          <div class="reply-content">{{ viewServiceCommentData.reply }}</div>
          <div class="reply-time">{{ formatDate(viewServiceCommentData.replyTime) }}</div>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="viewServiceCommentVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog :visible.sync="previewVisible" width="400px" append-to-body><img :src="previewImage" style="width: 100%" alt="预览图片"></el-dialog>
  </div>
</template>

<script>
import { getOrderList, cancelOrder, updateOrderStatus } from '@/api/order';
import { getUserAppointments, cancelAppointment, addServiceComment, getServiceCommentByAppointment } from '@/api/service';
import { addComment, getProductComments } from '@/api/comment';
import { uploadCommentImage as uploadImage } from '@/api/upload';
import { addToCart } from '@/api/cart';

export default {
  name: 'Orders',
  data() {
    return {
      allRawOrders: [],
      allRawTotal: 0,
      loading: false,
      commentSubmitting: false,
      serviceCommentSubmitting: false,
      cancelLoading: false,
      orderType: 'all',
      orderList: [],
      appointmentList: [],
      total: 0,
      page: 1,
      pageSize: 9,
      activeTab: '',
      searchKeyword: '',
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
        { value: 5, label: '已拒绝', icon: 'el-icon-circle-close' }
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
      serviceCommentForm: { rating: 5, content: '', imageList: [], imageUrls: [] },
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
      if (this.orderType === 'all') {
        const allTabs = [...this.productTabList, ...this.serviceTabList];
        return allTabs.filter((v, i, a) => a.findIndex(t => t.value === v.value) === i);
      }
      return this.orderType === 'product' ? this.productTabList : this.serviceTabList;
    },
    dialogTitle() {
      if (!this.currentCancelItem) return '取消申请';
      if (this.currentCancelType === 'product') {
        return this.currentCancelItem.orderStatus === 1 ? '申请退款' : '取消订单';
      } else {
        return this.currentCancelItem.status === 1 && this.currentCancelItem.payStatus === 1 ? '申请退款' : '取消预约';
      }
    },
    allOrdersList() {
      let filtered = this.allRawOrders;
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        filtered = this.allRawOrders.filter(item => {
          const orderNo = (item.orderNo || item.appointmentNo || '').toLowerCase();
          const name = (item.items?.[0]?.productName || item.serviceName || '').toLowerCase();
          return orderNo.includes(keyword) || name.includes(keyword);
        });
        this.total = filtered.length;
      } else {
        this.total = this.allRawTotal;
      }
      const start = (this.page - 1) * this.pageSize;
      const end = start + this.pageSize;
      return filtered.slice(start, end);
    },
    filteredOrderList() {
      if (!this.searchKeyword) return this.orderList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.orderList.filter(order => {
        const orderNo = (order.orderNo || '').toLowerCase();
        const productName = (order.items && order.items[0] && order.items[0].productName || '').toLowerCase();
        return orderNo.includes(keyword) || productName.includes(keyword);
      });
    },
    filteredAppointmentList() {
      if (!this.searchKeyword) return this.appointmentList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.appointmentList.filter(appointment => {
        const appointmentNo = (appointment.appointmentNo || '').toLowerCase();
        const serviceName = (appointment.serviceName || '').toLowerCase();
        return appointmentNo.includes(keyword) || serviceName.includes(keyword);
      });
    }
  },
  watch: {
    activeTab() { this.page = 1; this.loadData(); },
    orderType() { this.activeTab = ''; this.page = 1; this.searchKeyword = ''; this.loadData(); }
  },
  created() { this.loadData(); },
  methods: {
    handleImageError(e) {
      e.target.src = 'data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' viewBox=\'0 0 100 100\'%3E%3Crect width=\'100\' height=\'100\' fill=\'%23f0f0f0\'/%3E%3Ctext x=\'50\' y=\'55\' text-anchor=\'middle\' fill=\'%23999\' font-size=\'14\'%3E暂无图片%3C/text%3E%3C/svg%3E';
    },
    handleSearch() {},
    showAllOrders() {
      this.orderType = 'all';
      this.activeTab = '';
      this.searchKeyword = '';
      this.page = 1;
      this.loadData();
    },
    async loadData() {
      this.loading = true;
      try {
        if (this.orderType === 'all') {
          if (this.page === 1 || this.allRawOrders.length === 0) {
            const ordersRes = await getOrderList({ page: 1, pageSize: 1000, status: this.activeTab !== '' ? this.activeTab : undefined });
            const appointmentsRes = await getUserAppointments({ page: 1, pageSize: 1000, status: this.activeTab !== '' ? this.activeTab : undefined });
            const orders = (ordersRes.code === 200 ? ordersRes.data.list : []).map(o => ({ ...o, _type: 'product' }));
            const appointments = (appointmentsRes.code === 200 ? appointmentsRes.data.list : []).map(a => ({ ...a, _type: 'service' }));
            const all = [...orders, ...appointments];
            all.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
            this.allRawOrders = all;
            this.allRawTotal = all.length;
            this.total = this.allRawTotal;
          }
        } else if (this.orderType === 'product') {
          await this.loadOrders(true);
        } else {
          await this.loadAppointments(true);
        }
      } catch (error) {
        console.error('加载失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async loadOrders(loadComments = true) {
      const res = await getOrderList({ page: this.page, pageSize: this.pageSize, status: this.activeTab !== '' ? this.activeTab : undefined });
      if (res.code === 200) {
        this.orderList = res.data.list;
        this.total = res.data.total;
        if (loadComments) await this.loadOrderComments();
      }
    },
    async loadAppointments(loadComments = true) {
      const res = await getUserAppointments({
        page: this.page,
        pageSize: this.pageSize,
        status: this.activeTab !== '' ? this.activeTab : undefined
      });
      if (res.code === 200) {
        this.appointmentList = (res.data.list || []).map(item => ({ ...item, serviceImage: item.serviceImage || '' }));
        this.total = res.data.total || 0;
        if (loadComments) await this.loadAppointmentComments();
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
    handlePageChange(page) {
      this.page = page;
      if (this.orderType === 'all') {
        this.$forceUpdate();
      } else {
        this.loadData();
      }
    },
    getStatusText(status) {
      const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消', 5: '已退款' };
      return map[status] || '未知';
    },
    getStatusClass(status) {
      const map = { 0: 'status-pending', 1: 'status-paid', 2: 'status-shipped', 3: 'status-completed', 4: 'status-cancelled', 5: 'status-refunded' };
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
        if (!reason) { this.$message.warning('请输入取消原因'); return; }
      }
      if (!reason) { this.$message.warning('请选择取消原因'); return; }
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
    goToPay(orderNo) { this.$router.push(`/pay/${orderNo}`); },
    goToAppointmentPay(appointment) { this.$router.push(`/pay/${appointment.appointmentNo}`); },
    async confirmReceipt(order) {
      if (order.orderStatus !== 2) { this.$message.warning('当前状态无法确认收货'); return; }
      this.$confirm('确认已收到商品？确认后可以评价商品哦！', '提示', { type: 'info', confirmButtonText: '确认收货', cancelButtonText: '再等等' }).then(async () => {
        try {
          await updateOrderStatus(order.orderNo, 3);
          this.$message.success('确认收货成功！快去评价吧~');
          await this.loadData();
          setTimeout(() => this.openCommentDialog(order), 500);
        } catch (error) { this.$message.error('操作失败'); }
      }).catch(() => {});
    },
    openCommentDialog(order) { this.currentOrder = order; this.selectedProductId = order.items[0]?.productId; this.currentProduct = order.items[0]; this.resetCommentForm(); this.commentDialogVisible = true; },
    resetCommentForm() { this.commentForm = { rating: 5, content: '', imageList: [], imageUrls: [] }; this.$nextTick(() => { this.$refs.commentForm?.clearValidate(); }); },
    async uploadCommentImage(file) { const formData = new FormData(); formData.append('file', file.file); try { const res = await uploadImage(formData); if (res.code === 200) { this.commentForm.imageUrls.push(res.data.url); this.commentForm.imageList.push({ uid: Date.now(), name: file.file.name, url: res.data.url }); } } catch (error) { this.$message.error('上传失败'); } },
    handlePictureCardPreview(file) { this.previewImage = file.url; this.previewVisible = true; },
    handleRemove(file) { const index = this.commentForm.imageList.findIndex(f => f.uid === file.uid); if (index !== -1) { this.commentForm.imageList.splice(index, 1); this.commentForm.imageUrls.splice(index, 1); } },
    async uploadServiceCommentImage(file) { const formData = new FormData(); formData.append('file', file.file); try { const res = await uploadImage(formData); if (res.code === 200) { this.serviceCommentForm.imageUrls.push(res.data.url); this.serviceCommentForm.imageList.push({ uid: Date.now(), name: file.file.name, url: res.data.url }); } } catch (error) { this.$message.error('上传失败'); } },
    handleServiceRemove(file) { const index = this.serviceCommentForm.imageList.findIndex(f => f.uid === file.uid); if (index !== -1) { this.serviceCommentForm.imageList.splice(index, 1); this.serviceCommentForm.imageUrls.splice(index, 1); } },
    async submitComment() { this.$refs.commentForm.validate(async (valid) => { if (!valid) return; this.commentSubmitting = true; try { const res = await addComment({ orderNo: this.currentOrder.orderNo, productId: this.currentProduct.productId, rating: this.commentForm.rating, content: this.commentForm.content, images: this.commentForm.imageUrls.join(',') }); if (res.code === 200) { this.$message.success('评价成功！'); this.commentDialogVisible = false; await this.loadData(); } } catch (error) { this.$message.error('评价失败'); } finally { this.commentSubmitting = false; } }); },
    viewComment(order) { this.viewCommentData = order.comment; this.viewProduct = order.items[0]; this.viewCommentVisible = true; },
    viewOrderDetail(order) { this.detailOrder = order; this.orderDetailVisible = true; },
    async buyAgain(order) { try { for (const item of order.items) { await addToCart(item.productId, item.quantity); } this.$message.success('已加入购物车'); if (this.$bus) { this.$bus.$emit('cart-updated'); } } catch (error) { this.$message.error('添加失败'); } },
    openServiceCommentDialog(appointment) { this.currentAppointment = appointment; this.serviceCommentForm = { rating: 5, content: '', imageList: [], imageUrls: [] }; this.serviceCommentDialogVisible = true; this.$nextTick(() => { this.$refs.serviceCommentForm?.clearValidate(); }); },
    async submitServiceComment() { this.$refs.serviceCommentForm.validate(async (valid) => { if (!valid) return; this.serviceCommentSubmitting = true; try { const res = await addServiceComment({ appointmentId: this.currentAppointment.id, serviceId: this.currentAppointment.serviceId, rating: this.serviceCommentForm.rating, content: this.serviceCommentForm.content, images: this.serviceCommentForm.imageUrls.join(',') }); if (res.code === 200) { this.$message.success('评价成功！'); this.serviceCommentDialogVisible = false; await this.loadData(); } } catch (error) { this.$message.error('评价失败'); } finally { this.serviceCommentSubmitting = false; } }); },
    viewServiceComment(appointment) { this.viewServiceCommentData = appointment.comment; if (this.viewServiceCommentData) { this.viewServiceCommentData.serviceImage = appointment.serviceImage; this.viewServiceCommentData.serviceName = appointment.serviceName; this.viewServiceCommentData.servicePrice = appointment.servicePrice; } this.viewServiceCommentVisible = true; },
    viewAppointmentDetail(appointment) { this.currentAppointment = appointment; this.appointmentDetailVisible = true; }
  }
};
</script>

<style scoped>
.orders-page {
  padding: 0;
  background: transparent;
  min-height: 400px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

/* 订单类型栏 */
.orders-type-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.type-tabs {
  display: flex;
  gap: 12px;
}

.type-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 24px;
  border-radius: 30px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  background: #fff;
  border: 1px solid #e0e0e0;
  transition: all 0.3s;
}

.type-tab:hover {
  border-color: #667eea;
  color: #667eea;
}

.type-tab.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 搜索框 */
.search-wrapper {
  position: relative;
  width: 250px;
  flex-shrink: 0;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #909399;
  font-size: 16px;
  z-index: 1;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

/* 订单状态标签页 */
.orders-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  background: #fff;
  border: 1px solid #e0e0e0;
}

.tab-item i {
  font-size: 16px;
}

.tab-item:hover {
  border-color: #667eea;
  color: #667eea;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: transparent;
  color: white;
}

.tab-count {
  margin-left: 6px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 20px;
}

/* 订单网格 */
.orders-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  border: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.order-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
  gap: 12px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.order-no {
  font-weight: 600;
  color: #2c3e50;
  font-size: 12px;
}

.order-no i {
  margin-right: 4px;
  color: #667eea;
}

.order-time {
  font-size: 11px;
  color: #999;
}

.order-time i {
  margin-right: 4px;
}

/* 状态标签 */
.order-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 20px;
  white-space: nowrap;
  flex-shrink: 0;
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

.status-refunded {
  color: #909399;
  background: #f4f4f5;
}

.status-confirmed {
  color: #409EFF;
  background: #ecf5ff;
}

.status-servicing {
  color: #67c23a;
  background: #f0f9f4;
}

.status-rejected {
  color: #f56c6c;
  background: #fef0f0;
}

/* 卡片内容 */
.card-body {
  display: flex;
  padding: 16px;
  gap: 16px;
  flex: 1;
}

.product-image {
  position: relative;
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-count {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa, #e8eaef);
  color: #bbb;
  font-size: 32px;
}

.order-info-detail {
  flex: 1;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 服务元信息 - 与预约页面一致 */
.service-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.service-meta i {
  margin-right: 4px;
  color: #667eea;
}

.service-meta .price {
  color: #ff6b6b;
  font-weight: 500;
}

/* 预约时间信息 */
.appointment-time-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
}

.appointment-time-info i {
  color: #667eea;
}

/* 服务人员信息 */
.staff-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
}

.staff-info i {
  color: #667eea;
}

/* 备注信息 */
.remark-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
}

.remark-info i {
  color: #667eea;
}

.cancel-reason {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 11px;
  color: #f56c6c;
}

.cancel-reason i {
  color: #f56c6c;
}

/* 卡片底部按钮 */
.card-footer {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.card-footer .el-button {
  flex: 1;
  padding: 6px 0;
  font-size: 12px;
  border-radius: 6px;
  transition: all 0.3s;
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

.pay-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.pay-btn:hover {
  color: white;
  background: #409EFF;
  border-color: #409EFF;
}

.refund-btn {
  color: #e6a23c;
  border-color: #fdf6ec;
  background: #fdf6ec;
}

.refund-btn:hover {
  color: white;
  background: #e6a23c;
  border-color: #e6a23c;
}

.confirm-btn {
  color: #67c23a;
  border-color: #f0f9eb;
  background: #f0f9eb;
}

.confirm-btn:hover {
  color: white;
  background: #67c23a;
  border-color: #67c23a;
}

.comment-btn {
  color: #e6a23c;
  border-color: #fdf6ec;
  background: #fdf6ec;
}

.comment-btn:hover {
  color: white;
  background: #e6a23c;
  border-color: #e6a23c;
}

.view-btn {
  color: #67c23a;
  border-color: #f0f9eb;
  background: #f0f9eb;
}

.view-btn:hover {
  color: white;
  background: #67c23a;
  border-color: #67c23a;
}

.buyagain-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.buyagain-btn:hover {
  color: white;
  background: #409EFF;
  border-color: #409EFF;
}

.status-tag {
  flex: 1;
  text-align: center;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #ddd;
}

/* 分页 */
.pagination {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 取消对话框 */
.cancel-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.cancel-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 16px 20px;
  margin: 0;
}

.cancel-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.cancel-content {
  padding: 10px 0;
}

.cancel-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f8f9fc;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #666;
  font-size: 13px;
}

.cancel-info i {
  color: #e6a23c;
  font-size: 18px;
}

.refund-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fef0f0;
  border-radius: 8px;
  margin-top: 16px;
  color: #f56c6c;
  font-size: 12px;
}

.refund-tip i {
  font-size: 16px;
}

/* 评价对话框 */
.comment-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.comment-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 16px 20px;
  margin: 0;
}

.comment-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.comment-dialog-content {
  padding: 10px 0;
}

.comment-product,
.comment-service {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
  margin-bottom: 24px;
}

.comment-product img,
.service-img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
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

.product-option:hover,
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

.product-option i {
  color: #67c23a;
  font-size: 18px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 详情对话框 */
.detail-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 16px 20px;
  margin: 0;
}

.detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.detail-content {
  padding: 10px;
}

.detail-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
}

.section-title i {
  color: #667eea;
  font-size: 18px;
}

.info-item {
  margin-bottom: 12px;
}

.info-label {
  display: inline-block;
  width: 90px;
  font-size: 13px;
  color: #909399;
}

.info-value {
  font-size: 13px;
  color: #2c3e50;
}

.cancel-text {
  color: #f56c6c;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

/* 服务卡片样式 */
.service-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 16px;
}

.service-img-wrapper {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f7fa;
}

.service-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa, #e8eaef);
  color: #bbb;
  font-size: 40px;
}

.service-card-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.price-tag {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
  margin-bottom: 8px;
}

.staff-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: #ecf5ff;
  border-radius: 20px;
  font-size: 12px;
  color: #409EFF;
}

/* 商品清单 */
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
  font-size: 12px;
  color: #999;
}

.item-total {
  font-size: 14px;
  font-weight: 600;
  color: #ff6b6b;
}

/* 费用汇总 */
.amount-summary {
  background: #f8f9fc;
  border-radius: 12px;
  padding: 16px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.amount-row.total {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
  font-weight: 600;
}

.amount-row.total .total-price {
  font-size: 20px;
}

/* 查看评价对话框 */
.view-comment-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.view-comment-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 16px 20px;
  margin: 0;
}

.view-comment-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.view-comment-content {
  padding: 10px;
}

.view-product {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
  margin-bottom: 0;
}

.view-product img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.view-product .product-info h4 {
  font-size: 14px;
  margin-bottom: 6px;
  color: #2c3e50;
}

.view-product .product-price {
  color: #ff6b6b;
  font-weight: 600;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.rating-label {
  font-size: 13px;
  color: #909399;
}

.comment-text {
  background: #f8f9fc;
  padding: 14px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  color: #2c3e50;
  margin-bottom: 12px;
}

.comment-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}

.comment-images {
  margin-top: 15px;
}

.images-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.image-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.comment-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #eef2f6;
}

.reply-content {
  background: #e8f7ef;
  padding: 14px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  color: #2c3e50;
  margin-bottom: 8px;
}

.reply-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}

/* 响应式 */
@media (max-width: 900px) {
  .orders-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .orders-type-bar {
    flex-wrap: wrap;
  }
  .search-wrapper {
    width: 100%;
  }
}

@media (max-width: 600px) {
  .orders-grid {
    grid-template-columns: 1fr;
  }
  .card-body {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  .view-product {
    flex-direction: column;
    text-align: center;
  }
  .view-comment .comment-images {
    justify-content: center;
  }
  .orders-tabs {
    justify-content: center;
  }
  .type-tabs {
    flex-wrap: wrap;
    justify-content: center;
  }
  .service-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}
</style>