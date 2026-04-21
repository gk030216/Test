<template>
  <div class="staff-evaluations">

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.total || 0 }}</div>
            <div class="stat-label">总评价数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-rating">
            <i class="el-icon-star-on"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.avgRating || 0 }}</div>
            <div class="stat-label">平均评分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-reply">
            <i class="el-icon-message"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.replyCount || 0 }}</div>
            <div class="stat-label">已回复</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-pending">
            <i class="el-icon-chat-line-round"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.unreplyCount || 0 }}</div>
            <div class="stat-label">待回复</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索服务/用户/内容"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          ></el-input>
        </div>
        <el-select
            v-model="searchForm.rating"
            placeholder="评分"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="5星" :value="5"></el-option>
          <el-option label="4星" :value="4"></el-option>
          <el-option label="3星" :value="3"></el-option>
          <el-option label="2星" :value="2"></el-option>
          <el-option label="1星" :value="1"></el-option>
        </el-select>
        <el-select
            v-model="searchForm.replyStatus"
            placeholder="回复状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="已回复" :value="1"></el-option>
          <el-option label="待回复" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
      </div>
    </div>

    <!-- 评价表格 -->
    <el-table
        v-loading="loading"
        :data="commentList"
        stripe
        class="comment-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <el-table-column label="服务信息" min-width="200">
        <template slot-scope="scope">
          <div class="service-info">
            <span class="service-name">{{ scope.row.serviceName || '--' }}</span>
            <span class="service-id">ID: {{ scope.row.serviceId }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="用户" width="150">
        <template slot-scope="scope">
          <div class="user-info">
            <el-avatar :size="32" :src="scope.row.userAvatar" class="user-avatar">
              {{ scope.row.userNickname ? scope.row.userNickname.charAt(0).toUpperCase() : (scope.row.userName ? scope.row.userName.charAt(0).toUpperCase() : 'U') }}
            </el-avatar>
            <span class="user-name">{{ scope.row.userNickname || scope.row.userName }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="评分" width="120" align="center">
        <template slot-scope="scope">
          <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900"></el-rate>
        </template>
      </el-table-column>

      <el-table-column label="评价内容" min-width="250">
        <template slot-scope="scope">
          <div class="comment-content">{{ scope.row.content }}</div>
        </template>
      </el-table-column>

      <el-table-column label="回复状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.reply ? 'success' : 'info'" size="small">
            {{ scope.row.reply ? '已回复' : '待回复' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="评价时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button
                size="small"
                type="info"
                plain
                circle
                @click="handleView(scope.row)"
                class="action-icon-btn"
                title="查看"
            >
              <i class="el-icon-view"></i>
            </el-button>
            <el-button
                size="small"
                type="primary"
                plain
                circle
                @click="openReplyDialog(scope.row)"
                class="action-icon-btn"
                title="回复"
            >
              <i class="el-icon-chat-dot-round"></i>
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          :current-page="page"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
      ></el-pagination>
    </div>

    <!-- 回复对话框 -->
    <el-dialog
        :title="currentComment && currentComment.reply ? '查看/编辑回复' : '回复评价'"
        :visible.sync="replyVisible"
        width="550px"
        center
        class="reply-dialog"
    >
      <div class="dialog-content" v-if="currentComment">
        <div class="original-comment">
          <div class="comment-header">
            <div class="user-info">
              <el-avatar :size="36" :src="currentComment.userAvatar">
                {{ currentComment.userNickname ? currentComment.userNickname.charAt(0).toUpperCase() : (currentComment.userName ? currentComment.userName.charAt(0).toUpperCase() : 'U') }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ currentComment.userNickname || currentComment.userName }}</span>
                <el-rate v-model="currentComment.rating" disabled text-color="#ff9900"></el-rate>
              </div>
            </div>
            <span class="comment-time">{{ formatDate(currentComment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ currentComment.content }}</div>
          <div class="service-tag">
            <i class="el-icon-service"></i>
            {{ currentComment.serviceName }}
          </div>
        </div>

        <div class="reply-section">
          <div class="section-title">
            <i class="el-icon-edit"></i>
            <span>商家回复</span>
          </div>
          <el-input
              v-model="replyContent"
              type="textarea"
              :rows="4"
              :placeholder="currentComment.reply ? '修改回复内容...' : '输入回复内容...'"
              maxlength="500"
              show-word-limit
          ></el-input>
          <div v-if="currentComment.reply" class="old-reply">
            <span class="old-reply-label">原回复：</span>
            <span>{{ currentComment.reply }}</span>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replyLoading">
          <i class="el-icon-check"></i> 提交回复
        </el-button>
      </span>
    </el-dialog>

    <!-- 评价详情对话框 -->
    <el-dialog
        title="评价详情"
        :visible.sync="detailVisible"
        width="550px"
        center
        class="comment-detail-dialog"
    >
      <div class="detail-content" v-if="currentDetailComment">
        <!-- 服务信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-service"></i>
            <span>服务信息</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">服务名称：</span>
            <span class="detail-value">{{ currentDetailComment.serviceName || '--' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">服务ID：</span>
            <span class="detail-value">{{ currentDetailComment.serviceId }}</span>
          </div>
        </div>

        <!-- 用户信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-user"></i>
            <span>用户信息</span>
          </div>
          <div class="detail-user">
            <el-avatar :size="50" :src="currentDetailComment.userAvatar" class="detail-avatar">
              {{ currentDetailComment.userNickname ? currentDetailComment.userNickname.charAt(0).toUpperCase() : (currentDetailComment.userName ? currentDetailComment.userName.charAt(0).toUpperCase() : 'U') }}
            </el-avatar>
            <div class="user-detail">
              <span class="user-name">{{ currentDetailComment.userNickname || currentDetailComment.userName }}</span>
              <span class="user-id">用户ID: {{ currentDetailComment.userId }}</span>
            </div>
          </div>
        </div>

        <!-- 评价内容 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-star-on"></i>
            <span>评价内容</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">评分：</span>
            <el-rate v-model="currentDetailComment.rating" disabled show-score text-color="#ff9900"></el-rate>
          </div>
          <div class="detail-item">
            <span class="detail-label">评价内容：</span>
            <span class="detail-value comment-text">{{ currentDetailComment.content }}</span>
          </div>
          <!-- ✅ 添加评价图片展示 -->
          <div class="detail-item" v-if="currentDetailComment.images">
            <span class="detail-label">评价图片：</span>
            <div class="detail-images">
              <el-image
                  v-for="(img, idx) in currentDetailComment.images.split(',')"
                  :key="idx"
                  :src="img"
                  :preview-src-list="currentDetailComment.images.split(',')"
                  fit="cover"
                  class="detail-img"
              >
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </div>
          </div>
          <div class="detail-item">
            <span class="detail-label">评价时间：</span>
            <span class="detail-value">{{ formatDate(currentDetailComment.createTime) }}</span>
          </div>
        </div>

        <!-- 回复内容 -->
        <div class="detail-section" v-if="currentDetailComment.reply">
          <div class="section-title">
            <i class="el-icon-chat-dot-round"></i>
            <span>商家回复</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">回复内容：</span>
            <span class="detail-value reply-text">{{ currentDetailComment.reply }}</span>
          </div>
          <div class="detail-item" v-if="currentDetailComment.replyTime">
            <span class="detail-label">回复时间：</span>
            <span class="detail-value">{{ formatDate(currentDetailComment.replyTime) }}</span>
          </div>
        </div>

        <!-- 状态信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-info"></i>
            <span>状态信息</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">显示状态：</span>
            <el-tag :type="currentDetailComment.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentDetailComment.status === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">回复状态：</span>
            <el-tag :type="currentDetailComment.reply ? 'success' : 'info'" size="small">
              {{ currentDetailComment.reply ? '已回复' : '待回复' }}
            </el-tag>
          </div>
        </div>
      </div>

      <span slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getServiceCommentList,
  replyServiceComment,
  getStaffServiceCommentStatistics
} from '@/api/service';

export default {
  name: 'StaffEvaluations',
  data() {
    return {
      loading: false,
      replyLoading: false,
      detailVisible: false,
      currentDetailComment: null,
      commentList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        rating: '',
        replyStatus: ''
      },
      statistics: {
        total: 0,
        avgRating: 0,
        replyCount: 0,
        unreplyCount: 0
      },
      replyVisible: false,
      replyContent: '',
      currentComment: null
    };
  },
  created() {
    this.loadList();
    this.loadStatistics();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          rating: this.searchForm.rating || undefined,
          replyStatus: this.searchForm.replyStatus !== '' ? this.searchForm.replyStatus : undefined
        };
        const res = await getServiceCommentList(params);
        if (res.code === 200) {
          this.commentList = (res.data.list || []).map(item => ({
            ...item,
            userNickname: item.userNickname || null
          }));
          this.total = res.data.total || 0;
          this.updateStatistics();
        }
      } catch (error) {
        console.error('加载评价列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadStatistics() {
      try {
        const res = await getStaffServiceCommentStatistics();
        if (res.code === 200) {
          this.statistics = res.data || {};
        }
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },

    updateStatistics() {
      const list = this.commentList;
      const total = list.length;
      let sumRating = 0;
      let replyCount = 0;

      list.forEach(item => {
        sumRating += item.rating || 0;
        if (item.reply) replyCount++;
      });

      this.statistics = {
        ...this.statistics,
        replyCount: replyCount,
        unreplyCount: total - replyCount
      };
    },

    handleSearch() {
      this.page = 1;
      this.loadList();
    },

    handleReset() {
      this.searchForm = { keyword: '', rating: '', replyStatus: '' };
      this.page = 1;
      this.loadList();
    },

    handlePageChange(page) {
      this.page = page;
      this.loadList();
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadList();
    },

    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    // 查看评价详情
    handleView(row) {
      this.currentDetailComment = row;
      this.detailVisible = true;
    },

    openReplyDialog(row) {
      this.currentComment = row;
      this.replyContent = row.reply || '';
      this.replyVisible = true;
    },

    async submitReply() {
      if (!this.replyContent.trim()) {
        this.$message.warning('请输入回复内容');
        return;
      }

      this.replyLoading = true;
      try {
        const res = await replyServiceComment(this.currentComment.id, this.replyContent);
        if (res.code === 200) {
          this.$message.success('回复成功');
          this.currentComment.reply = this.replyContent;
          this.replyVisible = false;
          this.loadList();
          this.loadStatistics();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('回复失败');
      } finally {
        this.replyLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.staff-evaluations {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
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

.stat-card {
  border-radius: 16px;
}

.stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

.stat-rating { background: linear-gradient(135deg, #e6a23c, #f0b45c); }
.stat-reply { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-pending { background: linear-gradient(135deg, #f56c6c, #ff8e8e); }

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.action-bar {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin: 20px 0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.action-right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-wrapper {
  position: relative;
  display: inline-block;
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

.search-input {
  width: 220px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.status-select {
  width: 110px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  border-radius: 8px;
  padding: 8px 16px;
}

.comment-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}

.service-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.service-name {
  font-weight: 500;
  color: #2c3e50;
}

.service-id {
  font-size: 11px;
  color: #999;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
}

.comment-content {
  color: #5a6874;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-icon-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  transition: all 0.2s;
}

.action-icon-btn:hover {
  transform: scale(1.1);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
}

/* 回复对话框 */
.reply-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.reply-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.reply-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.dialog-content {
  padding: 10px 0;
}

.original-comment {
  background: #f8f9fc;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.comment-header .user-info {
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.original-comment .comment-content {
  margin: 12px 0;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
}

.service-tag {
  display: inline-block;
  padding: 4px 12px;
  background: #e8f4fd;
  color: #409EFF;
  border-radius: 20px;
  font-size: 12px;
}

.service-tag i {
  margin-right: 4px;
}

.reply-section {
  margin-top: 8px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 12px;
}

.section-title i {
  color: #667eea;
}

.old-reply {
  margin-top: 12px;
  padding: 12px;
  background: #fef9e6;
  border-radius: 8px;
  font-size: 13px;
}

.old-reply-label {
  color: #e6a23c;
  font-weight: 500;
  margin-right: 8px;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
}

/* 评价详情对话框样式 */
.comment-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.comment-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.comment-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.comment-detail-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.comment-detail-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
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
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.section-title i {
  color: #667eea;
  font-size: 16px;
}

.detail-user {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 8px 0;
}

.detail-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-detail .user-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 15px;
}

.user-detail .user-id {
  font-size: 12px;
  color: #909399;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  padding: 8px 0;
}

.detail-label {
  width: 85px;
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  font-size: 13px;
  color: #2c3e50;
}

.comment-text {
  line-height: 1.6;
  word-break: break-word;
}

.reply-text {
  color: #67c23a;
}

@media (max-width: 768px) {
  .staff-evaluations {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
  }

  .action-right {
    justify-content: center;
  }
}

/* 评价图片样式 */
.detail-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 5px;
}

.detail-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #eef2f6;
  transition: transform 0.2s;
}

.detail-img:hover {
  transform: scale(1.05);
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}
</style>