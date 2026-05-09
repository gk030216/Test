<template>
  <div class="post-manage-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon"><i class="el-icon-document" /></div>
        <div class="stat-info">
          <div class="stat-label">总帖子数</div>
          <div class="stat-value">{{ statistics.totalPosts }}</div>
          <div class="stat-sub">社区全部帖子</div>
        </div>
      </div>
      <div class="stat-card card-comment">
        <div class="stat-icon"><i class="el-icon-chat-dot-round" /></div>
        <div class="stat-info">
          <div class="stat-label">总评论数</div>
          <div class="stat-value">{{ statistics.totalComments }}</div>
          <div class="stat-sub">帖子累计评论</div>
        </div>
      </div>
      <div class="stat-card card-today">
        <div class="stat-icon"><i class="el-icon-circle-plus" /></div>
        <div class="stat-info">
          <div class="stat-label">今日新增</div>
          <div class="stat-value">{{ statistics.todayPosts }}</div>
          <div class="stat-sub">{{ todayDate }} 发布</div>
        </div>
      </div>
      <div class="stat-card card-views">
        <div class="stat-icon"><i class="el-icon-view" /></div>
        <div class="stat-info">
          <div class="stat-label">总浏览量</div>
          <div class="stat-value">{{ statistics.totalViews }}</div>
          <div class="stat-sub">帖子累计浏览</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索标题 / 内容 / 用户..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.category"
          placeholder="全部分类"
          clearable
          @change="handleSearch"
          class="filter-select"
        >
          <el-option label="宠物日常" value="pet_daily" />
          <el-option label="健康分享" value="health" />
          <el-option label="美食分享" value="food" />
          <el-option label="领养信息" value="adopt" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-select
          v-model="searchForm.status"
          placeholder="全部状态"
          clearable
          @change="handleSearch"
          class="filter-select status-select"
        >
          <el-option label="正常" :value="1" />
          <el-option label="隐藏" :value="0" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-top" @click="handleBatchTop" :loading="batchLoading">
          <i class="el-icon-top" /> 批量置顶
        </el-button>
        <el-button size="small" class="batch-btn-essence" @click="handleBatchEssence" :loading="batchLoading">
          <i class="el-icon-star-on" /> 批量精华
        </el-button>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete" :loading="batchLoading">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 帖子表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="postList"
        stripe
        row-key="id"
        class="post-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="用户" width="150" align="center">
          <template slot-scope="scope">
            <div class="user-cell">
              <el-avatar :size="36" :src="scope.row.userAvatar" class="user-avatar">
                {{ getDisplayName(scope.row).charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="user-meta">
                <span class="user-name">{{ getDisplayName(scope.row) }}</span>
                <span class="user-sub" v-if="scope.row.userNickname && scope.row.userName">{{ scope.row.userName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="标题" min-width="200">
          <template slot-scope="scope">
            <div class="title-cell">
              <span class="title-text">{{ scope.row.title }}</span>
              <span class="title-badges">
                <el-tag v-if="scope.row.isTop === 1" size="mini" type="danger" effect="dark">置顶</el-tag>
                <el-tag v-if="scope.row.isEssence === 1" size="mini" type="warning" effect="dark">精华</el-tag>
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="内容预览" min-width="220">
          <template slot-scope="scope">
            <div class="content-preview">{{ truncateText(scope.row.content, 60) }}</div>
          </template>
        </el-table-column>

        <el-table-column label="分类" width="100" align="center">
          <template slot-scope="scope">
            <span :class="['category-tag', getCategoryClass(scope.row.category)]">
              {{ getCategoryName(scope.row.category) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="互动数据" width="150" align="center">
          <template slot-scope="scope">
            <div class="data-stats">
              <span><i class="el-icon-star-on" /> {{ scope.row.likeCount || 0 }}</span>
              <span><i class="el-icon-chat-dot-round" /> {{ scope.row.commentCount || 0 }}</span>
              <span><i class="el-icon-view" /> {{ scope.row.viewCount || 0 }}</span>
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

        <el-table-column label="置顶" width="75" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.isTop === 1"
              active-color="#e6a23c"
              inactive-color="#dcdfe6"
              @change="(val) => handleTopChange(scope.row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column label="精华" width="75" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.isEssence === 1"
              active-color="#f56c6c"
              inactive-color="#dcdfe6"
              @change="(val) => handleEssenceChange(scope.row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column label="发布时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleView(scope.row)" class="act-view">
                <i class="el-icon-view" /> 查看
              </el-button>
              <el-button type="text" size="small" @click="handleDelete(scope.row)" class="act-del">
                <i class="el-icon-delete" /> 删除
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

    <!-- 帖子详情对话框 -->
    <el-dialog
      title="帖子详情"
      :visible.sync="detailVisible"
      width="700px"
      center
      class="post-dialog"
    >
      <div class="detail-body" v-if="currentPost">
        <div class="detail-user-area">
          <el-avatar :size="52" :src="currentPost.userAvatar" class="detail-avatar">
            {{ getDetailDisplayName().charAt(0).toUpperCase() }}
          </el-avatar>
          <div class="detail-user-meta">
            <div class="detail-user-name">{{ getDetailDisplayName() }}</div>
            <div class="detail-user-sub" v-if="currentPost.userNickname && currentPost.userName">
              @{{ currentPost.userName }}
            </div>
            <div class="detail-user-time">{{ formatDate(currentPost.createTime) }}</div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="d-label">标题</span>
              <span class="d-value">{{ currentPost.title }}</span>
            </div>
            <div class="detail-item">
              <span class="d-label">分类</span>
              <span :class="['category-tag', getCategoryClass(currentPost.category)]">
                {{ getCategoryName(currentPost.category) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="d-label">状态</span>
              <span class="status-tag" :class="currentPost.status === 1 ? 'active' : 'inactive'">
                {{ currentPost.status === 1 ? '正常' : '已隐藏' }}
              </span>
            </div>
            <div class="detail-item">
              <span class="d-label">置顶</span>
              <span class="status-tag" :class="currentPost.isTop === 1 ? 'top' : 'plain'">
                {{ currentPost.isTop === 1 ? '已置顶' : '未置顶' }}
              </span>
            </div>
            <div class="detail-item">
              <span class="d-label">精华</span>
              <span class="status-tag" :class="currentPost.isEssence === 1 ? 'essence' : 'plain'">
                {{ currentPost.isEssence === 1 ? '精华帖' : '普通帖' }}
              </span>
            </div>
            <div class="detail-item">
              <span class="d-label">点赞</span>
              <span class="d-value">{{ currentPost.likeCount || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="d-label">评论</span>
              <span class="d-value">{{ currentPost.commentCount || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="d-label">浏览</span>
              <span class="d-value">{{ currentPost.viewCount || 0 }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-label">帖子内容</div>
          <div class="detail-content">{{ currentPost.content }}</div>
          <div class="detail-images" v-if="currentPost.images">
            <div class="images-label">图片附件</div>
            <div class="images-list">
              <el-image
                v-for="(img, idx) in currentPost.images.split(',')"
                :key="idx"
                :src="img"
                fit="cover"
                class="detail-img"
                :preview-src-list="currentPost.images.split(',')"
              />
            </div>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
        <el-button type="primary" @click="goToPost" class="warm-btn">查看原帖</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminPostList,
  updatePostStatus,
  updatePostTop,
  updatePostEssence,
  adminDeletePost,
  batchUpdatePostTop,
  batchUpdatePostEssence,
  batchDeletePosts,
  getPostStatistics
} from '@/api/community';

export default {
  name: 'Admin_PostList',
  data() {
    return {
      loading: false,
      batchLoading: false,
      postList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', category: '', status: '' },
      statistics: { totalPosts: 0, totalComments: 0, todayPosts: 0, totalViews: 0 },
      detailVisible: false,
      currentPost: null
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
    this.loadStatistics();
  },
  methods: {
    getDisplayName(row) {
      return row.userNickname || row.userName || '未知用户';
    },
    getDetailDisplayName() {
      if (!this.currentPost) return '匿名用户';
      return this.currentPost.userNickname || this.currentPost.userName || '匿名用户';
    },
    async loadList() {
      this.loading = true;
      try {
        const params = { page: this.page, pageSize: this.pageSize };
        if (this.searchForm.keyword && this.searchForm.keyword.trim()) {
          params.keyword = this.searchForm.keyword.trim();
        }
        if (this.searchForm.category && this.searchForm.category !== '') {
          params.category = this.searchForm.category;
        }
        if (this.searchForm.status !== '' && this.searchForm.status !== null) {
          params.status = this.searchForm.status;
        }
        const res = await getAdminPostList(params);
        if (res.code === 200) {
          this.postList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async loadStatistics() {
      try {
        const res = await getPostStatistics();
        if (res.code === 200 && res.data) {
          this.statistics = res.data;
        }
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },
    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() {
      this.searchForm = { keyword: '', category: '', status: '' };
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
    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    },
    getCategoryName(category) {
      const map = { pet_daily: '宠物日常', health: '健康分享', food: '美食分享', adopt: '领养信息', other: '其他' };
      return map[category] || '其他';
    },
    getCategoryClass(category) {
      const map = { pet_daily: 'cat-pet', health: 'cat-health', food: 'cat-food', adopt: 'cat-adopt', other: 'cat-other' };
      return map[category] || 'cat-other';
    },
    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      try {
        const res = await updatePostStatus(row.id, newStatus);
        if (res.code === 200) {
          row.status = newStatus;
          this.$message.success(res.message || '操作成功');
          this.loadStatistics();
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },
    async handleTopChange(row, val) {
      const newTop = val ? 1 : 0;
      try {
        const res = await updatePostTop(row.id, newTop);
        if (res.code === 200) {
          row.isTop = newTop;
          this.$message.success(newTop === 1 ? '置顶成功' : '取消置顶');
          this.loadStatistics();
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },
    async handleEssenceChange(row, val) {
      const newEssence = val ? 1 : 0;
      try {
        const res = await updatePostEssence(row.id, newEssence);
        if (res.code === 200) {
          row.isEssence = newEssence;
          this.$message.success(newEssence === 1 ? '设为精华成功' : '取消精华');
          this.loadStatistics();
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除帖子 "${row.title}" 吗？`, '提示', { type: 'warning' });
        const res = await adminDeletePost(row.id);
        if (res.code === 200) {
          this.$message.success('删除成功');
          this.loadList();
          this.loadStatistics();
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败');
      }
    },
    async handleBatchTop() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      try {
        await this.$confirm(`确定要置顶选中的 ${this.selectedRows.length} 个帖子吗？`, '提示', { type: 'info' });
        this.batchLoading = true;
        const res = await batchUpdatePostTop(ids, 1);
        if (res.code === 200) {
          this.$message.success(res.message || '批量置顶成功');
          this.selectedRows = [];
          this.loadList();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('操作失败');
      } finally {
        this.batchLoading = false;
      }
    },
    async handleBatchEssence() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      try {
        await this.$confirm(`确定要将选中的 ${this.selectedRows.length} 个帖子设为精华吗？`, '提示', { type: 'info' });
        this.batchLoading = true;
        const res = await batchUpdatePostEssence(ids, 1);
        if (res.code === 200) {
          this.$message.success(res.message || '批量设精成功');
          this.selectedRows = [];
          this.loadList();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('操作失败');
      } finally {
        this.batchLoading = false;
      }
    },
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个帖子吗？删除后无法恢复！`, '警告', { type: 'warning' });
        this.batchLoading = true;
        const res = await batchDeletePosts(ids);
        if (res.code === 200) {
          this.$message.success(res.message || '批量删除成功');
          this.selectedRows = [];
          this.loadList();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('批量删除失败');
      } finally {
        this.batchLoading = false;
      }
    },
    handleView(row) {
      this.currentPost = row;
      this.detailVisible = true;
    },
    goToPost() {
      if (this.currentPost) {
        window.open(`/community/post/${this.currentPost.id}`, '_blank');
      }
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.post-manage-page {
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
.card-total .stat-icon   { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-comment .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-today .stat-icon   { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-views .stat-icon   { background: linear-gradient(135deg, #36cfc9, #5cdbd3); }
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
.filter-select { width: 120px; }
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
.batch-btn-top {
  background: rgba(230, 162, 60, 0.12) !important;
  border: 1px solid rgba(230, 162, 60, 0.35) !important;
  color: #e6a23c !important;
}
.batch-btn-top:hover { background: rgba(230, 162, 60, 0.22) !important; border-color: #e6a23c !important; }
.batch-btn-essence {
  background: rgba(245, 108, 108, 0.12) !important;
  border: 1px solid rgba(245, 108, 108, 0.35) !important;
  color: #f56c6c !important;
}
.batch-btn-essence:hover { background: rgba(245, 108, 108, 0.22) !important; border-color: #f56c6c !important; }
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
.post-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.post-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.post-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.post-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.post-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.post-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b; border-color: #f59e4b;
}

/* 用户单元格 */
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar { background: linear-gradient(135deg, #f59e4b, #f0826a) !important; color: #fff; font-weight: 600; flex-shrink: 0; }
.user-meta { display: flex; flex-direction: column; gap: 2px; text-align: left; }
.user-name { font-weight: 600; color: #3d2e2a; font-size: 13px; }
.user-sub { font-size: 11px; color: #a08c84; }

/* 标题 */
.title-cell { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.title-text { font-weight: 500; color: #3d2e2a; }

/* 内容预览 */
.content-preview {
  color: #5c4a42; font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 分类标签 */
.category-tag { display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 12px; }
.cat-pet    { background: #edf4fd; color: #409eff; }
.cat-health { background: #edf8f0; color: #67c23a; }
.cat-food   { background: #fef5ee; color: #e6a23c; }
.cat-adopt  { background: #fef0f0; color: #f56c6c; }
.cat-other  { background: #f5f5f5; color: #909399; }

/* 互动数据 */
.data-stats { display: flex; gap: 12px; justify-content: center; }
.data-stats span { display: flex; align-items: center; gap: 3px; font-size: 12px; color: #a08c84; }

/* 时间 */
.cell-time { color: #a08c84; font-size: 12px; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }

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
.post-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.post-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.post-dialog ::v-deep .el-dialog__title { color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px; }
.post-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.post-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.post-dialog ::v-deep .el-dialog__body { padding: 28px; }

.detail-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }
.detail-user-area {
  display: flex; align-items: center; gap: 16px;
  margin-bottom: 24px; padding-bottom: 20px;
  border-bottom: 1px solid #f5ece6;
}
.detail-avatar { background: linear-gradient(135deg, #f59e4b, #f0826a) !important; color: #fff; font-weight: 600; font-size: 22px; flex-shrink: 0; }
.detail-user-name { font-weight: 600; color: #3d2e2a; font-size: 15px; }
.detail-user-sub { font-size: 12px; color: #a08c84; margin-top: 2px; }
.detail-user-time { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

.detail-section { margin-bottom: 20px; }
.detail-section:last-child { margin-bottom: 0; }
.section-label { font-size: 13px; font-weight: 600; color: #5c4a42; margin-bottom: 10px; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  border: 1px solid #f5ece6;
  border-radius: 12px;
  overflow: hidden;
}
.detail-item {
  display: flex; align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #f5ece6;
  border-right: 1px solid #f5ece6;
}
.detail-item:nth-child(2n) { border-right: none; }
.detail-item:last-child { border-bottom: none; }
.detail-item:nth-last-child(2):nth-child(odd) { border-bottom: none; }
.d-label { width: 52px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; font-weight: 500; word-break: break-all; }

.status-tag { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-tag.active  { background: #edf7ee; color: #67c23a; }
.status-tag.inactive{ background: #fef0f0; color: #f56c6c; }
.status-tag.top     { background: #fef5ee; color: #e6a23c; }
.status-tag.essence { background: #fef0f0; color: #f56c6c; }
.status-tag.plain   { background: #f5f5f5; color: #909399; }

.detail-content {
  background: #fdf8f4; padding: 16px;
  border-radius: 12px; color: #5c4a42;
  line-height: 1.8; font-size: 14px;
  white-space: pre-wrap; word-break: break-word;
}
.images-label { font-size: 13px; color: #a08c84; margin: 12px 0 8px; }
.images-list { display: flex; gap: 12px; flex-wrap: wrap; }
.detail-img { width: 100px; height: 100px; border-radius: 10px; cursor: pointer; border: 1px solid #f5ece6; background: #fdf8f4; object-fit: cover; }

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
  .post-manage-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left { justify-content: center; }
  .keyword-input { width: 100%; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-item { border-right: none; }
  .detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }
  .detail-item:last-child { border-bottom: none; }
}
</style>
