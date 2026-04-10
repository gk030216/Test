<template>
  <div class="my-appointments">
    <!-- 标签页切换 -->
    <div class="appointment-tabs">
      <div
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab-item', { active: activeTab === tab.value }]"
          @click="activeTab = tab.value"
      >
        {{ tab.label }}
        <span class="tab-count" v-if="getCount(tab.value) > 0">{{ getCount(tab.value) }}</span>
      </div>
    </div>

    <!-- 预约列表 -->
    <div class="appointment-list" v-loading="loading">
      <div class="empty-state" v-if="appointmentList.length === 0">
        <i class="el-icon-s-order"></i>
        <p>暂无预约记录</p>
        <el-button type="primary" size="small" @click="$router.push('/services')">去预约服务</el-button>
      </div>

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
          <div class="service-info">
            <div class="service-image-wrapper">
              <el-image
                  v-if="item.serviceImage"
                  :src="item.serviceImage"
                  fit="cover"
                  class="service-image"
              >
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
              <div v-else class="service-image-placeholder">
                <i class="el-icon-service"></i>
              </div>
            </div>
            <div class="service-detail">
              <h4>{{ item.serviceName }}</h4>
              <div class="service-meta">
                <span><i class="el-icon-s-custom"></i> {{ item.petName || '未指定' }}</span>
                <span class="price"><i class="el-icon-money"></i> ¥{{ item.servicePrice }}</span>
              </div>
            </div>
          </div>

          <div class="appointment-detail">
            <div class="detail-item highlight">
              <i class="el-icon-date"></i>
              <span>{{ formatDate(item.appointmentDate) }} {{ item.appointmentTime }}</span>
            </div>
            <div class="detail-item" v-if="item.staffName">
              <i class="el-icon-user"></i>
              <span>{{ item.staffName }}</span>
            </div>
            <div class="detail-item" v-if="item.remark">
              <i class="el-icon-edit"></i>
              <span class="remark-text">{{ item.remark }}</span>
            </div>
            <div class="detail-item cancel-reason" v-if="item.cancelReason">
              <i class="el-icon-info"></i>
              <span>{{ item.cancelReason }}</span>
            </div>
          </div>
        </div>

        <!-- 卡片底部 -->
        <div class="card-footer">
          <div class="footer-left">
            <span class="create-date">{{ formatDate(item.createTime) }}</span>
          </div>
          <div class="footer-right">
            <el-button
                v-if="item.status === 0"
                type="danger"
                size="small"
                plain
                @click="cancelAppointment(item)"
                class="action-btn cancel-btn"
            >
              <i class="el-icon-close"></i> 取消预约
            </el-button>

            <!-- 已完成且未评价 -->
            <el-button
                v-if="item.status === 3 && !item.comment"
                type="warning"
                size="small"
                @click="openCommentDialog(item)"
                class="action-btn comment-btn"
            >
              <i class="el-icon-edit"></i> 评价服务
            </el-button>

            <!-- 已评价 - 显示查看评价按钮 -->
            <el-button
                v-if="item.status === 3 && item.comment"
                type="info"
                size="small"
                plain
                @click="viewComment(item)"
                class="action-btn view-comment-btn"
            >
              <i class="el-icon-view"></i> 查看评价
            </el-button>

            <el-button
                type="primary"
                size="small"
                plain
                @click="viewDetail(item)"
                class="action-btn view-btn"
            >
              <i class="el-icon-document"></i> 查看详情
            </el-button>
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
            <el-image
                v-if="currentAppointment.serviceImage"
                :src="currentAppointment.serviceImage"
                fit="cover"
                class="service-image"
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
            <div v-else class="service-image-placeholder">
              <i class="el-icon-service"></i>
            </div>
          </div>
          <div class="service-info">
            <h4>{{ currentAppointment.serviceName }}</h4>
            <div class="service-meta">
              <span><i class="el-icon-s-custom"></i> {{ currentAppointment.petName }}</span>
              <span class="price"><i class="el-icon-money"></i> ¥{{ currentAppointment.servicePrice }}</span>
            </div>
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
                placeholder="说说你的服务体验..."
                maxlength="500"
                show-word-limit
            ></el-input>
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
        :visible.sync="viewCommentDialogVisible"
        width="450px"
        center
        class="view-comment-dialog"
    >
      <div class="view-comment-content" v-if="currentAppointment && currentAppointment.comment">
        <div class="comment-rating">
          <el-rate
              v-model="currentAppointment.comment.rating"
              disabled
              show-score
              text-color="#ff9900"
          ></el-rate>
        </div>
        <div class="comment-text">{{ currentAppointment.comment.content }}</div>
        <div class="comment-time">{{ formatDateTime(currentAppointment.comment.createTime) }}</div>
        <div class="comment-reply" v-if="currentAppointment.comment.reply">
          <div class="reply-header">
            <i class="el-icon-chat-dot-round"></i>
            <span>商家回复</span>
          </div>
          <div class="reply-content">{{ currentAppointment.comment.reply }}</div>
          <div class="reply-time">{{ formatDateTime(currentAppointment.comment.replyTime) }}</div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="viewCommentDialogVisible = false">关闭</el-button>
      </span>
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
            <el-option label="时间冲突，改天再约" value="时间冲突，改天再约"></el-option>
            <el-option label="宠物身体不适" value="宠物身体不适"></el-option>
            <el-option label="已经找到其他服务" value="已经找到其他服务"></el-option>
            <el-option label="其他原因" value="其他原因"></el-option>
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
          ></el-input>
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
      pageSize: 10,
      activeTab: '',
      tabs: [
        { value: '', label: '全部' },
        { value: 0, label: '待确认' },
        { value: 1, label: '已确认' },
        { value: 2, label: '服务中' },
        { value: 3, label: '已完成' },
        { value: 4, label: '已取消' }
      ],
      cancelDialogVisible: false,
      detailDialogVisible: false,
      commentDialogVisible: false,
      viewCommentDialogVisible: false,
      currentAppointment: null,
      cancelForm: {
        reason: '',
        customReason: ''
      },
      commentForm: {
        rating: 5,
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
    async loadAppointments() {
      this.loading = true;
      try {
        const res = await getUserAppointments({
          page: this.page,
          pageSize: this.pageSize,
          status: this.activeTab !== '' ? this.activeTab : undefined
        });
        if (res.code === 200) {
          this.appointmentList = res.data.list || [];
          this.total = res.data.total || 0;
          await this.loadAppointmentComments();
        }
      } catch (error) {
        console.error('加载预约列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
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
            }
          } catch (error) {
            console.error('获取评价失败', error);
          }
        }
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
      let reason = this.cancelForm.reason;
      if (reason === '其他原因') {
        reason = this.cancelForm.customReason;
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
        rating: 5,
        content: ''
      };
      this.commentDialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.commentForm) {
          this.$refs.commentForm.clearValidate();
        }
      });
    },

    viewComment(item) {
      this.currentAppointment = item;
      this.viewCommentDialogVisible = true;
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
            this.currentAppointment.comment = {
              rating: this.commentForm.rating,
              content: this.commentForm.content,
              createTime: new Date()
            };
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

.appointment-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.tab-item {
  padding: 8px 20px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-size: 14px;
  background: #f5f5f5;
  font-weight: 500;
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
  margin-left: 6px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 20px;
}

.appointment-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 20px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.appointment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.appointment-info {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 13px;
}

.appointment-info i {
  margin-right: 6px;
  color: #667eea;
}

.appointment-no {
  font-weight: 600;
  color: #2c3e50;
}

.appointment-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 20px;
  background: #f5f5f5;
}

.status-pending { color: #e6a23c; background: #fdf6ec; }
.status-confirmed { color: #409EFF; background: #ecf5ff; }
.status-servicing { color: #67c23a; background: #f0f9f4; }
.status-completed { color: #909399; background: #f4f4f5; }
.status-cancelled, .status-rejected { color: #f56c6c; background: #fef0f0; }

.card-body {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.service-info {
  display: flex;
  gap: 16px;
  flex: 2;
  min-width: 280px;
}

.service-image-wrapper {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
}

.service-image {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  background: #f5f5f5;
}

.service-image-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f5f5, #e8e8e8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 28px;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #999;
  font-size: 28px;
}

.service-detail h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #2c3e50;
}

.service-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #666;
  font-size: 13px;
}

.service-meta i {
  margin-right: 6px;
  color: #667eea;
  width: 18px;
}

.service-meta .price {
  color: #ff6b6b;
  font-weight: 600;
}

.appointment-detail {
  flex: 3;
  min-width: 280px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  color: #666;
  font-size: 13px;
}

.detail-item i {
  width: 20px;
  color: #667eea;
}

.detail-item.highlight {
  color: #2c3e50;
  font-weight: 500;
}

.detail-item.highlight i {
  color: #409EFF;
}

.detail-item .remark-text {
  color: #909399;
  font-style: italic;
}

.detail-item.cancel-reason {
  color: #f56c6c;
}

.detail-item.cancel-reason i {
  color: #f56c6c;
}

.card-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left .create-date {
  font-size: 12px;
  color: #999;
}

.footer-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s;
}

.action-btn i {
  margin-right: 4px;
  font-size: 13px;
}

.view-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.view-btn:hover {
  color: white;
  background: #409EFF;
  border-color: #409EFF;
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
  border-color: #faecd8;
  background: #fdf6ec;
}

.comment-btn:hover {
  color: white;
  background: #e6a23c;
  border-color: #e6a23c;
}

.view-comment-btn {
  color: #67c23a;
  border-color: #e1f3d8;
  background: #f0f9f4;
}

.view-comment-btn:hover {
  color: white;
  background: #67c23a;
  border-color: #67c23a;
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
  margin-bottom: 20px;
  font-size: 16px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
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

.status-badge.status-pending { background: #fdf6ec; color: #e6a23c; }
.status-badge.status-confirmed { background: #ecf5ff; color: #409EFF; }
.status-badge.status-servicing { background: #f0f9f4; color: #67c23a; }
.status-badge.status-completed { background: #f4f4f5; color: #909399; }
.status-badge.status-cancelled,
.status-badge.status-rejected { background: #fef0f0; color: #f56c6c; }

.cancel-reason-text {
  color: #f56c6c;
}

/* 评价对话框 */
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

.comment-service {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 16px;
  margin-bottom: 24px;
}

.comment-service .service-image-wrapper {
  width: 70px;
  height: 70px;
}

.comment-service .service-info h4 {
  font-size: 15px;
  margin-bottom: 6px;
  color: #2c3e50;
}

.comment-service .service-meta {
  gap: 4px;
}

.rating-wrapper {
  padding: 5px 0;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
}

.view-comment-content {
  padding: 10px 0;
}

.view-comment-content .comment-rating {
  margin-bottom: 16px;
}

.view-comment-content .comment-text {
  color: #5a6874;
  line-height: 1.6;
  font-size: 14px;
  padding: 12px;
  background: #f8f9fc;
  border-radius: 12px;
  margin-bottom: 12px;
}

.view-comment-content .comment-time {
  font-size: 12px;
  color: #999;
  text-align: right;
  margin-bottom: 16px;
}

.view-comment-content .comment-reply {
  background: #f0f9f4;
  border-radius: 12px;
  padding: 12px 16px;
  border-left: 3px solid #67c23a;
}

.view-comment-content .reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #67c23a;
  margin-bottom: 8px;
  font-size: 13px;
}

.view-comment-content .reply-content {
  color: #5a6874;
  line-height: 1.5;
  margin-bottom: 6px;
  font-size: 13px;
}

.view-comment-content .reply-time {
  font-size: 11px;
  color: #999;
  text-align: right;
}

/* 对话框样式 */
.cancel-dialog ::v-deep .el-dialog,
.detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.cancel-dialog ::v-deep .el-dialog__header,
.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.cancel-dialog ::v-deep .el-dialog__title,
.detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.cancel-dialog ::v-deep .el-dialog__close,
.detail-dialog ::v-deep .el-dialog__close {
  color: white;
}

/* 响应式 */
@media (max-width: 768px) {
  .appointment-card {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .appointment-info {
    flex-direction: column;
    gap: 8px;
  }

  .card-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .footer-right {
    width: 100%;
    justify-content: flex-end;
  }

  .service-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .service-meta {
    align-items: center;
  }

  .comment-service {
    flex-direction: column;
    text-align: center;
  }

  .comment-service .service-image-wrapper {
    margin: 0 auto;
  }
}
</style>