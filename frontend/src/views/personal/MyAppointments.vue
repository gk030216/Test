<template>
  <div class="my-appointments">
    <h2 class="page-title">我的预约</h2>

    <!-- 标签页切换 -->
    <div class="appointment-tabs">
      <div
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab-item', { active: activeTab === tab.value }]"
          @click="activeTab = tab.value"
      >
        <i :class="getTabIcon(tab.value)"></i>
        {{ tab.label }}
        <span class="tab-count" v-if="getCount(tab.value) > 0">{{ getCount(tab.value) }}</span>
      </div>
    </div>

    <!-- 预约网格 -->
    <div class="appointments-grid" v-loading="loading">
      <div class="appointment-card" v-for="item in appointmentList" :key="item.id">
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="appointment-info">
            <span class="appointment-no">
              <i class="el-icon-document"></i> {{ item.appointmentNo }}
            </span>
            <span class="appointment-time">
              <i class="el-icon-time"></i> {{ formatRelativeTime(item.createTime) }}
            </span>
          </div>
          <div class="appointment-status" :class="getStatusClass(item.status)">
            <i :class="getStatusIcon(item.status)"></i>
            {{ getStatusText(item.status) }}
          </div>
        </div>

        <!-- 卡片内容 -->
        <div class="card-body">
          <div class="service-image">
            <img
                v-if="item.serviceImage"
                :src="item.serviceImage"
                :alt="item.serviceName"
                @error="e => e.target.src = 'data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' viewBox=\'0 0 100 100\'%3E%3Crect width=\'100\' height=\'100\' fill=\'%23f0f0f0\'/%3E%3Ctext x=\'50\' y=\'55\' text-anchor=\'middle\' fill=\'%23999\' font-size=\'14\'%3E暂无图片%3C/text%3E%3C/svg%3E'"
            >
            <div v-else class="image-placeholder">
              <i class="el-icon-service"></i>
            </div>
          </div>
          <div class="service-info">
            <h4 class="service-name">{{ item.serviceName }}</h4>
            <div class="service-meta">
              <span><i class="el-icon-s-custom"></i> {{ item.petName || '未指定' }}</span>
              <span class="price"><i class="el-icon-money"></i> ¥{{ item.servicePrice }}</span>
            </div>
            <div class="appointment-time-info">
              <i class="el-icon-date"></i>
              <span>{{ formatDate(item.appointmentDate) }} {{ item.appointmentTime }}</span>
            </div>
            <div class="staff-info" v-if="item.staffName">
              <i class="el-icon-user"></i>
              <span>{{ item.staffName }}</span>
            </div>
            <div class="remark-info" v-if="item.remark">
              <i class="el-icon-edit"></i>
              <span>{{ item.remark }}</span>
            </div>
            <div class="cancel-reason" v-if="item.status === 4 && item.cancelReason">
              <i class="el-icon-info"></i>
              <span>取消原因：{{ item.cancelReason }}</span>
            </div>
            <div class="cancel-reason" v-if="item.status === 5 && item.cancelReason">
              <i class="el-icon-info"></i>
              <span>拒绝原因：{{ item.cancelReason }}</span>
            </div>
          </div>
        </div>

        <!-- 卡片底部按钮 -->
        <div class="card-footer">
          <el-button
              v-if="item.status === 0 && item.payStatus !== 1"
              size="small"
              plain
              @click="cancelAppointment(item)"
              class="cancel-btn"
          >
            <i class="el-icon-close"></i> 取消预约
          </el-button>
          <el-button
              v-if="(item.status === 0 || item.status === 1) && item.payStatus === 1"
              size="small"
              plain
              @click="cancelAppointment(item)"
              class="cancel-btn"
          >
            <i class="el-icon-close"></i> 申请退款
          </el-button>
          <el-button
              v-if="item.status === 3 && !item.comment"
              size="small"
              plain
              @click="openCommentDialog(item)"
              class="comment-btn"
          >
            <i class="el-icon-edit"></i> 评价服务
          </el-button>
          <el-button
              v-if="item.status === 3 && item.comment"
              size="small"
              plain
              @click="viewServiceComment(item)"
              class="view-btn"
          >
            <i class="el-icon-view"></i> 查看评价
          </el-button>
          <el-button
              size="small"
              plain
              @click="viewDetail(item)"
              class="detail-btn"
          >
            <i class="el-icon-document"></i> 查看详情
          </el-button>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && appointmentList.length === 0">
        <i class="el-icon-s-order"></i>
        <p>暂无预约记录</p>
        <el-button type="primary" size="small" @click="$router.push('/services')">去预约服务</el-button>
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
            @current-change="handlePageChange"
            :current-page="page"
            :page-size="pageSize"
            layout="prev, pager, next"
            :total="total"
            background
        />
      </div>
    </div>

    <!-- 评价对话框 -->
    <el-dialog
        title="评价服务"
        :visible.sync="commentDialogVisible"
        width="500px"
        center
        class="comment-dialog"
        :close-on-click-modal="false"
    >
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

        <el-form :model="commentForm" :rules="commentRules" ref="commentForm" label-width="80px">
          <el-form-item label="评分" prop="rating">
            <div class="rating-wrapper"><el-rate v-model="commentForm.rating" :texts="['很差', '较差', '一般', '推荐', '超赞']" show-text :colors="['#f56c6c', '#e6a23c', '#67c23a']" /></div>
          </el-form-item>
          <el-form-item label="评价内容" prop="content">
            <el-input type="textarea" v-model="commentForm.content" rows="4" placeholder="说说你的服务体验..." maxlength="500" show-word-limit />
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="commentSubmitting">提交评价</el-button>
      </span>
    </el-dialog>

    <!-- 服务预约查看评价对话框 -->
    <el-dialog
        title="我的评价"
        :visible.sync="viewServiceCommentVisible"
        width="500px"
        center
        class="view-comment-dialog"
    >
      <div class="view-comment-content" v-if="viewServiceCommentData">
        <div class="view-product">
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

    <!-- 取消预约对话框 -->
    <el-dialog
        title="取消预约"
        :visible.sync="cancelDialogVisible"
        width="450px"
        center
        class="cancel-dialog"
    >
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因">
          <el-select v-model="cancelForm.reason" placeholder="请选择取消原因" style="width: 100%">
            <el-option label="时间冲突，改天再约" value="时间冲突，改天再约" />
            <el-option label="宠物身体不适" value="宠物身体不适" />
            <el-option label="已经找到其他服务" value="已经找到其他服务" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" v-if="cancelForm.reason === '其他原因'">
          <el-input
              v-model="cancelForm.customReason"
              type="textarea"
              :rows="3"
              placeholder="请输入取消原因"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCancel" :loading="cancelLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 预约详情对话框 -->
    <el-dialog
        title="预约详情"
        :visible.sync="detailDialogVisible"
        width="500px"
        center
        class="detail-dialog"
    >
      <div class="detail-content" v-if="currentAppointment">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="预约编号">{{ currentAppointment.appointmentNo }}</el-descriptions-item>
          <el-descriptions-item label="服务名称">{{ currentAppointment.serviceName }}</el-descriptions-item>
          <el-descriptions-item label="服务金额">
            <span class="detail-price">¥{{ currentAppointment.servicePrice }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="预约宠物">{{ currentAppointment.petName }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ formatDate(currentAppointment.appointmentDate) }} {{ currentAppointment.appointmentTime }}
          </el-descriptions-item>
          <el-descriptions-item label="服务人员">
            {{ currentAppointment.staffName || '待分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="预约状态">
            <span :class="['status-badge', getStatusClass(currentAppointment.status)]">
              {{ getStatusText(currentAppointment.status) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDateTime(currentAppointment.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentAppointment.remark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" v-if="currentAppointment.cancelReason">
            <span class="cancel-reason-text">{{ currentAppointment.cancelReason }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getUserAppointments, cancelAppointment, addServiceComment, getServiceCommentByAppointment } from '@/api/service';

export default {
  name: 'MyAppointments',
  data() {
    return {
      loading: false,
      cancelLoading: false,
      commentSubmitting: false,
      appointmentList: [],
      total: 0,
      page: 1,
      pageSize: 9,
      activeTab: '',
      tabs: [
        { value: '', label: '全部' },
        { value: 0, label: '待确认' },
        { value: 1, label: '已确认' },
        { value: 2, label: '服务中' },
        { value: 3, label: '已完成' },
        { value: 4, label: '已取消' },
        { value: 5, label: '已拒绝' }
      ],
      cancelDialogVisible: false,
      detailDialogVisible: false,
      commentDialogVisible: false,
      viewServiceCommentVisible: false,
      currentAppointment: null,
      viewServiceCommentData: null,
      cancelForm: {
        reason: '',
        customReason: ''
      },
      commentForm: {
        rating: null,
        content: ''
      },
      commentRules: {
        rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
        content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
      }
    };
  },
  created() {
    this.loadAppointments();
  },
  watch: {
    activeTab() {
      this.page = 1;
      this.loadAppointments();
    }
  },
  methods: {
    getTabIcon(value) {
      const map = {
        '': 'el-icon-menu',
        0: 'el-icon-time',
        1: 'el-icon-success',
        2: 'el-icon-loading',
        3: 'el-icon-circle-check',
        4: 'el-icon-circle-close',
        5: 'el-icon-close'
      };
      return map[value] || 'el-icon-info';
    },

    async loadAppointments() {
      this.loading = true;
      try {
        console.log('========== MyAppointments.vue 加载预约 ==========');
        console.log('当前页码:', this.page);
        console.log('每页大小:', this.pageSize);
        console.log('当前状态筛选:', this.activeTab !== '' ? this.activeTab : '全部');

        const statusParam = this.activeTab !== '' ? this.activeTab : undefined;
        const res = await getUserAppointments({
          page: this.page,
          pageSize: this.pageSize,
          status: statusParam
        });

        console.log('接口返回状态:', res.code);
        console.log('接口返回总数:', res.data?.total);
        console.log('接口返回列表长度:', res.data?.list?.length);
        console.log('==============================================');

        if (res.code === 200) {
          this.appointmentList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载预约列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    getCount(status) {
      return 0;
    },

    getStatusText(status) {
      const map = {
        0: '待确认',
        1: '已确认',
        2: '服务中',
        3: '已完成',
        4: '已取消',
        5: '已拒绝'
      };
      return map[status] || '未知';
    },

    getStatusClass(status) {
      const map = {
        0: 'status-pending',
        1: 'status-confirmed',
        2: 'status-servicing',
        3: 'status-completed',
        4: 'status-cancelled',
        5: 'status-rejected'
      };
      return map[status] || '';
    },

    getStatusIcon(status) {
      const map = {
        0: 'el-icon-time',
        1: 'el-icon-success',
        2: 'el-icon-loading',
        3: 'el-icon-circle-check',
        4: 'el-icon-circle-close',
        5: 'el-icon-close'
      };
      return map[status] || 'el-icon-info';
    },

    handlePageChange(page) {
      this.page = page;
      this.loadAppointments();
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    formatDateTime(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
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

    cancelAppointment(item) {
      this.currentAppointment = item;
      this.cancelForm = { reason: '', customReason: '' };
      this.cancelDialogVisible = true;
    },

    async confirmCancel() {
      let reason = '';

      if (this.cancelForm.reason === '其他原因') {
        reason = this.cancelForm.customReason;
        if (!reason || reason.trim() === '') {
          this.$message.warning('请输入取消原因');
          return;
        }
      } else if (this.cancelForm.reason) {
        reason = this.cancelForm.reason;
      } else {
        this.$message.warning('请选择取消原因');
        return;
      }

      this.cancelLoading = true;
      try {
        const res = await cancelAppointment(this.currentAppointment.id, reason);
        if (res.code === 200) {
          this.$message.success('取消成功');
          this.cancelDialogVisible = false;
          this.loadAppointments();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('取消失败');
      } finally {
        this.cancelLoading = false;
      }
    },

    viewDetail(item) {
      this.currentAppointment = item;
      this.detailDialogVisible = true;
    },

    openCommentDialog(item) {
      this.currentAppointment = item;
      this.commentForm = {
        rating: null,
        content: ''
      };
      this.commentDialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.commentForm) {
          this.$refs.commentForm.clearValidate();
        }
      });
    },

    async viewServiceComment(appointment) {
      if (appointment.comment) {
        this.viewServiceCommentData = {
          ...appointment.comment,
          serviceImage: appointment.serviceImage,
          serviceName: appointment.serviceName,
          servicePrice: appointment.servicePrice
        };
        this.viewServiceCommentVisible = true;
        return;
      }

      const loading = this.$loading({
        text: '加载评价中...',
        target: document.querySelector('.my-appointments'),
        background: 'rgba(0, 0, 0, 0.5)'
      });

      try {
        const res = await getServiceCommentByAppointment(appointment.id);
        if (res.code === 200 && res.data) {
          appointment.comment = res.data;
          this.viewServiceCommentData = {
            ...res.data,
            serviceImage: appointment.serviceImage,
            serviceName: appointment.serviceName,
            servicePrice: appointment.servicePrice
          };
          this.viewServiceCommentVisible = true;
        } else {
          this.$message.warning('暂无评价内容');
        }
      } catch (error) {
        console.error('获取评价失败', error);
        this.$message.error('获取评价失败');
      } finally {
        loading.close();
      }
    },

    async submitComment() {
      this.$refs.commentForm.validate(async (valid) => {
        if (!valid) return;

        this.commentSubmitting = true;
        try {
          const res = await addServiceComment({
            appointmentId: this.currentAppointment.id,
            serviceId: this.currentAppointment.serviceId,
            rating: this.commentForm.rating,
            content: this.commentForm.content
          });

          if (res.code === 200) {
            this.$message.success('感谢你的评价！');
            this.commentDialogVisible = false;
            await this.loadAppointments();
          } else {
            this.$message.error(res.message || '评价失败');
          }
        } catch (error) {
          console.error('提交评价失败', error);
          this.$message.error('评价失败');
        } finally {
          this.commentSubmitting = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.my-appointments {
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

/* 标签页切换样式 - 与我的评论一致 */
.appointment-tabs {
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

/* 预约网格 */
.appointments-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.appointment-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  border: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.appointment-card:hover {
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
}

.appointment-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.appointment-no {
  font-weight: 600;
  color: #2c3e50;
  font-size: 12px;
}

.appointment-no i {
  margin-right: 4px;
  color: #667eea;
}

.appointment-time {
  font-size: 11px;
  color: #999;
}

.appointment-time i {
  margin-right: 4px;
}

.appointment-status {
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

.status-confirmed {
  color: #409EFF;
  background: #ecf5ff;
}

.status-servicing {
  color: #67c23a;
  background: #f0f9f4;
}

.status-completed {
  color: #909399;
  background: #f4f4f5;
}

.status-cancelled, .status-rejected {
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

.service-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.service-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

.service-info {
  flex: 1;
}

.service-name {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.service-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.service-meta i {
  margin-right: 4px;
  color: #667eea;
}

.service-meta .price {
  color: #ff6b6b;
  font-weight: 500;
}

.appointment-time-info,
.staff-info,
.remark-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
}

.appointment-time-info i,
.staff-info i,
.remark-info i {
  color: #667eea;
}

.cancel-reason {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-size: 11px;
  color: #f56c6c;
}

.cancel-reason i {
  color: #f56c6c;
}

/* 卡片底部按钮 - 与我的评论样式一致 */
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

.detail-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.detail-btn:hover {
  color: white;
  background: #409EFF;
  border-color: #409EFF;
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

/* 对话框样式 */
.comment-dialog ::v-deep .el-dialog,
.view-comment-dialog ::v-deep .el-dialog,
.cancel-dialog ::v-deep .el-dialog,
.detail-dialog ::v-deep .el-dialog {
  border-radius: 16px;
}

.comment-dialog ::v-deep .el-dialog__header,
.view-comment-dialog ::v-deep .el-dialog__header,
.cancel-dialog ::v-deep .el-dialog__header,
.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 16px 20px;
  margin: 0;
  border-radius: 16px 16px 0 0;
}

.comment-dialog ::v-deep .el-dialog__title,
.view-comment-dialog ::v-deep .el-dialog__title,
.cancel-dialog ::v-deep .el-dialog__title,
.detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.comment-dialog ::v-deep .el-dialog__close,
.view-comment-dialog ::v-deep .el-dialog__close,
.cancel-dialog ::v-deep .el-dialog__close,
.detail-dialog ::v-deep .el-dialog__close {
  color: white;
}

.comment-dialog-content {
  padding: 10px 0;
}

.comment-service {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
  margin-bottom: 24px;
}

.comment-service .service-image-wrapper {
  width: 70px;
  height: 70px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.comment-service .service-info h4 {
  font-size: 15px;
  margin-bottom: 6px;
  color: #2c3e50;
}

.rating-wrapper {
  padding: 5px 0;
}

.detail-content {
  padding: 10px;
}

.detail-price {
  color: #ff6b6b;
  font-weight: 600;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.cancel-reason-text {
  color: #f56c6c;
}

/* 查看评价对话框 */
.view-comment-dialog ::v-deep .view-product img {
  width: 70px !important;
  height: 70px !important;
  min-width: 70px;
  max-width: 70px;
  min-height: 70px;
  max-height: 70px;
  border-radius: 12px;
  object-fit: cover;
  flex-shrink: 0;
}

.view-comment-dialog ::v-deep .view-product .image-placeholder {
  width: 70px;
  height: 70px;
  min-width: 70px;
  max-width: 70px;
  min-height: 70px;
  max-height: 70px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa, #e8eaef);
  color: #bbb;
  font-size: 32px;
  flex-shrink: 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .appointments-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .appointments-grid {
    grid-template-columns: 1fr;
  }

  .card-body {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .service-meta {
    justify-content: center;
  }

  .appointment-time-info,
  .staff-info,
  .remark-info,
  .cancel-reason {
    justify-content: center;
  }
}
</style>