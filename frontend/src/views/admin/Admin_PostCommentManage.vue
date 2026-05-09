<template>
  <div class="comment-manage-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon"><i class="el-icon-chat-dot-round" /></div>
        <div class="stat-info">
          <div class="stat-label">总评论数</div>
          <div class="stat-value">{{ statistics.total }}</div>
          <div class="stat-sub">社区全部评论</div>
        </div>
      </div>
      <div class="stat-card card-active">
        <div class="stat-icon"><i class="el-icon-check" /></div>
        <div class="stat-info">
          <div class="stat-label">正常显示</div>
          <div class="stat-value">{{ statistics.active }}</div>
          <div class="stat-sub">状态正常</div>
        </div>
      </div>
      <div class="stat-card card-today">
        <div class="stat-icon"><i class="el-icon-circle-plus" /></div>
        <div class="stat-info">
          <div class="stat-label">今日新增</div>
          <div class="stat-value">{{ statistics.todayNew }}</div>
          <div class="stat-sub">{{ todayDate }} 发布</div>
        </div>
      </div>
      <div class="stat-card card-hidden">
        <div class="stat-icon"><i class="el-icon-remove" /></div>
        <div class="stat-info">
          <div class="stat-label">已隐藏</div>
          <div class="stat-value">{{ statistics.hidden }}</div>
          <div class="stat-sub">不可见评论</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索帖子 / 用户 / 内容..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.status"
          placeholder="全部状态"
          clearable
          @change="handleSearch"
          class="filter-select status-select"
        >
          <el-option label="正常" :value="1" />
          <el-option label="已隐藏" :value="0" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 评论表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="commentList"
        stripe
        class="comment-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="所属帖子" min-width="180">
          <template slot-scope="scope">
            <div class="post-info">
              <span class="post-title" @click="goToPost(scope.row.postId)">{{ scope.row.postTitle || '查看原帖' }}</span>
              <span class="post-id">ID: {{ scope.row.postId }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论用户" width="150" align="center">
          <template slot-scope="scope">
            <div class="user-cell">
              <el-avatar :size="36" :src="scope.row.userAvatar" class="user-avatar">
                {{ (scope.row.userNickname || scope.row.userName || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="user-meta">
                <span class="user-name">{{ scope.row.userNickname || scope.row.userName || '匿名用户' }}</span>
                <span class="user-sub">ID: {{ scope.row.userId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论内容" min-width="250">
          <template slot-scope="scope">
            <div class="comment-content">{{ scope.row.content }}</div>
            <div class="reply-tag" v-if="scope.row.parentId !== 0">
              <i class="el-icon-chat-line-round" /> 回复 @{{ scope.row.replyToUserName }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="显示" width="75" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.status === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleStatusChange(scope.row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column label="评论时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="viewDetail(scope.row)" class="act-view">
                <i class="el-icon-view" /> 详情
              </el-button>
              <el-button
                v-if="scope.row.status === 1"
                type="text" size="small"
                @click="handleDelete(scope.row)" class="act-del"
              >
                <i class="el-icon-delete" /> 删除
              </el-button>
              <el-button
                v-else
                type="text" size="small"
                @click="handleRestore(scope.row)" class="act-restore"
              >
                <i class="el-icon-refresh" /> 恢复
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-card">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :current-page="page"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
      />
    </div>

    <!-- 评论详情对话框 -->
    <el-dialog
      title="评论详情"
      :visible.sync="detailVisible"
      width="550px"
      center
      class="comment-dialog"
    >
      <div class="detail-body" v-if="currentComment">
        <div class="detail-section">
          <div class="section-label">所属帖子</div>
          <div class="post-link" @click="goToPost(currentComment.postId)">
            {{ currentComment.postTitle }}
          </div>
        </div>

        <div class="detail-section">
          <div class="section-label">评论信息</div>
          <div class="comment-card">
            <div class="comment-header">
              <div class="comment-user">
                <el-avatar :size="36" :src="currentComment.userAvatar" class="detail-avatar">
                  {{ (currentComment.userNickname || currentComment.userName || 'U').charAt(0).toUpperCase() }}
                </el-avatar>
                <div>
                  <div class="comment-user-name">{{ currentComment.userNickname || currentComment.userName || '匿名用户' }}</div>
                  <div class="comment-user-id">ID: {{ currentComment.userId }}</div>
                </div>
              </div>
              <span class="comment-time">{{ formatDate(currentComment.createTime) }}</span>
            </div>
            <div class="comment-text">{{ currentComment.content }}</div>
            <div class="comment-meta" v-if="currentComment.parentId !== 0">
              <i class="el-icon-chat-line-round" /> 回复 @{{ currentComment.replyToUserName }}
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="replies.length > 0">
          <div class="section-label">回复列表 ({{ replies.length }})</div>
          <div class="replies-list">
            <div class="reply-item" v-for="reply in replies" :key="reply.id">
              <div class="reply-header">
                <span class="reply-user">{{ reply.userNickname || reply.userName || '匿名用户' }}</span>
                <span class="reply-to" v-if="reply.replyToUserName">回复 @{{ reply.replyToUserName }}</span>
                <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
              </div>
              <div class="reply-content">{{ reply.content }}</div>
              <div class="reply-actions">
                <el-button
                  v-if="reply.status === 1"
                  type="text" size="small" class="act-del"
                  @click="handleDelete(reply)"
                >删除</el-button>
                <el-button
                  v-else
                  type="text" size="small" class="act-restore"
                  @click="handleRestore(reply)"
                >恢复</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="detailVisible = false" class="warm-btn">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminCommentList,
  updateCommentStatus,
  adminDeleteComment,
  batchDeleteComments,
  restoreComment,
  getCommentReplies
} from '@/api/community';

export default {
  name: 'Admin_PostCommentManage',
  data() {
    return {
      loading: false,
      commentList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', status: '' },
      statistics: { total: 0, active: 0, todayNew: 0, hidden: 0 },
      detailVisible: false,
      currentComment: null,
      replies: []
    };
  },
  computed: {
    todayDate() {
      const d = new Date();
      return `${d.getMonth() + 1}/${d.getDate()}`;
    }
  },
  created() {
    this.loadList();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined
        };
        const res = await getAdminCommentList(params);
        if (res.code === 200) {
          this.commentList = res.data.list || [];
          this.total = res.data.total || 0;
          this.computeStats();
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async computeStats() {
      try {
        const res = await getAdminCommentList({ page: 1, pageSize: 500, keyword: undefined, status: undefined });
        if (res.code === 200) {
          const list = res.data.list || [];
          const today = new Date().toDateString();
          this.statistics = {
            total: res.data.total || 0,
            active: list.filter(i => i.status === 1).length,
            todayNew: list.filter(i => new Date(i.createTime).toDateString() === today).length,
            hidden: list.filter(i => i.status === 0).length
          };
        }
      } catch (error) {
        console.error('统计加载失败', error);
      }
    },
    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() {
      this.searchForm = { keyword: '', status: '' };
      this.page = 1;
      this.loadList();
    },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      try {
        const res = await updateCommentStatus(row.id, newStatus);
        if (res.code === 200) {
          row.status = newStatus;
          this.$message.success(newStatus === 1 ? '已显示' : '已隐藏');
          this.computeStats();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },
    async viewDetail(row) {
      this.currentComment = row;
      this.detailVisible = true;
      this.replies = [];
      if (row.parentId === 0) {
        try {
          const res = await getCommentReplies(row.id);
          if (res.code === 200) {
            this.replies = res.data || [];
          }
        } catch (error) {
          console.error('加载回复失败', error);
        }
      }
    },
    goToPost(postId) {
      if (postId) {
        window.open(`/community/post/${postId}`, '_blank');
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该评论吗？删除后无法恢复！', '警告', { type: 'warning' });
        const res = await adminDeleteComment(row.id);
        if (res.code === 200) {
          this.$message.success('删除成功');
          this.loadList();
          this.computeStats();
          if (this.detailVisible && this.currentComment) {
            this.viewDetail(this.currentComment);
          }
        } else {
          this.$message.error(res.message || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败');
      }
    },
    async handleRestore(row) {
      try {
        await this.$confirm('确定要恢复该评论吗？', '提示', { type: 'info' });
        const res = await restoreComment(row.id);
        if (res.code === 200) {
          this.$message.success('恢复成功');
          this.loadList();
          this.computeStats();
          if (this.detailVisible && this.currentComment) {
            this.viewDetail(this.currentComment);
          }
        } else {
          this.$message.error(res.message || '恢复失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('恢复失败');
      }
    },
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条评论吗？删除后无法恢复！`, '警告', { type: 'warning' });
        const res = await batchDeleteComments(ids);
        if (res.code === 200) {
          this.$message.success(res.message || '批量删除成功');
          this.selectedRows = [];
          this.loadList();
          this.computeStats();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('批量删除失败');
      }
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.comment-manage-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 统计卡片行 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.05);
  transition: transform 0.25s, box-shadow 0.25s;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(61, 46, 42, 0.10);
}
.stat-icon {
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}
.card-total .stat-icon  { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-active .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-today .stat-icon  { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-hidden .stat-icon { background: linear-gradient(135deg, #c0c4cc, #909399); }
.stat-info { position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 6px; }
.stat-value { font-size: 30px; font-weight: 700; color: #3d2e2a; line-height: 1.1; letter-spacing: -0.5px; }
.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ===== 搜索筛选栏 ===== */
.filter-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px 24px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}
.filter-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.keyword-input { width: 240px; }
.keyword-input ::v-deep .el-input__inner { border-radius: 10px; }
.filter-select { width: 110px; }
.filter-select ::v-deep .el-input__inner { border-radius: 10px; }
.status-select { width: 100px; }

.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  padding: 9px 20px !important;
  font-weight: 500;
}
.warm-btn:hover { opacity: 0.88; transform: translateY(-1px); }
.reset-btn { border-radius: 10px; color: #606266; }
.reset-btn:hover { background: #fdf6f0; border-color: #f5d5c0; }

/* ===== 批量操作浮条 ===== */
.batch-bar {
  background: linear-gradient(135deg, #fef5ee, #fff7f0);
  border: 1px solid #f5d5c0;
  border-left: 4px solid #f59e4b;
  border-radius: 12px;
  padding: 10px 20px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #3d2e2a;
}
.batch-info { font-size: 13px; }
.batch-info strong { color: #f59e4b; font-size: 15px; }
.batch-bar .el-button {
  border-radius: 8px; padding: 7px 16px;
  font-weight: 500; transition: all 0.2s;
}
.batch-btn-delete {
  background: rgba(245, 108, 108, 0.12) !important;
  border: 1px solid rgba(245, 108, 108, 0.35) !important;
  color: #f56c6c !important;
}
.batch-btn-delete:hover { background: rgba(245, 108, 108, 0.22) !important; border-color: #f56c6c !important; }
.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}
.comment-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.comment-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.comment-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.comment-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.comment-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.comment-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b; border-color: #f59e4b;
}

/* 帖子信息 */
.post-info { display: flex; flex-direction: column; gap: 4px; text-align: left; }
.post-title { font-weight: 500; color: #409eff; cursor: pointer; font-size: 13px; }
.post-title:hover { text-decoration: underline; }
.post-id { font-size: 11px; color: #a08c84; }

/* 用户单元格 */
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar { background: linear-gradient(135deg, #f59e4b, #f0826a) !important; color: #fff; font-weight: 600; flex-shrink: 0; }
.user-meta { display: flex; flex-direction: column; gap: 2px; text-align: left; }
.user-name { font-weight: 600; color: #3d2e2a; font-size: 13px; }
.user-sub { font-size: 11px; color: #a08c84; }

/* 评论内容 */
.comment-content { color: #5c4a42; line-height: 1.5; font-size: 13px; }
.reply-tag { margin-top: 6px; font-size: 12px; color: #a08c84; }
.reply-tag i { margin-right: 4px; }

/* 时间 */
.cell-time { color: #a08c84; font-size: 12px; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }
.act-restore { color: #67c23a !important; }
.act-restore:hover { color: #5ab01e !important; }

/* ===== 分页卡片 ===== */
.pagination-card {
  background: #fff;
  border-radius: 16px;
  margin-top: 16px;
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

/* ===== 详情对话框 ===== */
.comment-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.comment-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.comment-dialog ::v-deep .el-dialog__title { color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px; }
.comment-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.comment-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.comment-dialog ::v-deep .el-dialog__body { padding: 28px; }

.detail-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }
.detail-section { margin-bottom: 22px; }
.detail-section:last-child { margin-bottom: 0; }
.section-label { font-size: 13px; font-weight: 600; color: #5c4a42; margin-bottom: 10px; }

.post-link {
  padding: 12px 16px; background: #fdf8f4; border-radius: 10px;
  color: #409eff; cursor: pointer; font-size: 14px; font-weight: 500;
  transition: background 0.3s;
}
.post-link:hover { background: #e8f4fd; }

.comment-card {
  padding: 16px; background: #fdf8f4; border-radius: 12px;
}
.comment-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; flex-wrap: wrap; gap: 10px; }
.comment-user { display: flex; align-items: center; gap: 12px; }
.detail-avatar { background: linear-gradient(135deg, #f59e4b, #f0826a) !important; color: #fff; font-weight: 600; flex-shrink: 0; }
.comment-user-name { font-weight: 600; color: #3d2e2a; font-size: 14px; }
.comment-user-id { font-size: 11px; color: #a08c84; }
.comment-time { font-size: 12px; color: #b8a8a0; }
.comment-text { padding: 12px; background: #fff; border-radius: 8px; color: #5c4a42; line-height: 1.6; font-size: 14px; }
.comment-meta { margin-top: 8px; font-size: 13px; color: #a08c84; }

.replies-list { display: flex; flex-direction: column; gap: 12px; }
.reply-item { padding: 14px 16px; background: #fdf8f4; border-radius: 12px; }
.reply-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; font-size: 13px; flex-wrap: wrap; }
.reply-user { font-weight: 600; color: #3d2e2a; }
.reply-to { color: #f59e4b; }
.reply-time { margin-left: auto; font-size: 12px; color: #b8a8a0; }
.reply-content { color: #5c4a42; line-height: 1.5; margin-bottom: 8px; font-size: 13px; }
.reply-actions { text-align: right; }

.dialog-footer {
  text-align: right;
  padding: 16px 28px 22px;
  border-top: 1px solid #f5ece6;
}
.dialog-footer .el-button { border-radius: 10px; padding: 10px 28px; font-weight: 500; font-size: 13px; }
.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
}
.dialog-footer .el-button--primary:hover { opacity: 0.88; transform: translateY(-1px); }

/* 响应式 */
@media (max-width: 768px) {
  .comment-manage-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left { justify-content: center; }
  .keyword-input { width: 100%; }
  .reply-header { flex-direction: column; align-items: flex-start; gap: 4px; }
  .reply-time { margin-left: 0; }
}
</style>
