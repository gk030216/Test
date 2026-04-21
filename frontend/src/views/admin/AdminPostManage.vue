<template>
  <div class="admin-post-manage">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.totalPosts }}</div>
            <div class="stat-label">总帖子数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.totalComments }}</div>
            <div class="stat-label">总评论数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-star-on"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.todayPosts }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-view"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.totalViews }}</div>
            <div class="stat-label">总浏览量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button
            v-if="selectedRows.length > 0"
            type="success"
            plain
            @click="handleBatchTop"
            :loading="batchLoading"
        >
          <i class="el-icon-top"></i> 批量置顶 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="warning"
            plain
            @click="handleBatchEssence"
            :loading="batchLoading"
        >
          <i class="el-icon-star-on"></i> 批量精华 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
            :loading="batchLoading"
        >
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索标题/内容/用户"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          >
          </el-input>
        </div>
        <el-select
            v-model="searchForm.category"
            placeholder="分类"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="全部" value=""></el-option>
          <el-option label="宠物日常" value="pet_daily"></el-option>
          <el-option label="健康分享" value="health"></el-option>
          <el-option label="美食分享" value="food"></el-option>
          <el-option label="领养信息" value="adopt"></el-option>
          <el-option label="其他" value="other"></el-option>
        </el-select>
        <el-select
            v-model="searchForm.status"
            placeholder="状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="正常" :value="1"></el-option>
          <el-option label="隐藏" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
      </div>
    </div>

    <!-- 帖子表格 -->
    <el-table
        v-loading="loading"
        :data="postList"
        stripe
        style="width: 100%"
        class="post-table"
        @selection-change="handleSelectionChange"
        row-key="id"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <!-- 用户列 - 优先显示昵称 -->
      <el-table-column label="用户" width="150" align="center">
        <template slot-scope="scope">
          <div class="user-info">
            <el-avatar :size="32" :src="scope.row.userAvatar" class="user-avatar">
              {{ getDisplayName(scope.row).charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="user-detail">
              <span class="user-name">{{ getDisplayName(scope.row) }}</span>
              <span class="user-username" v-if="scope.row.userNickname && scope.row.userName">
                {{ scope.row.userName }}
              </span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="标题" min-width="200">
        <template slot-scope="scope">
          <div class="post-title">
            <span class="title-text">{{ scope.row.title }}</span>
            <div class="title-badges">
              <el-tag v-if="scope.row.isTop === 1" size="mini" type="danger" effect="dark">置顶</el-tag>
              <el-tag v-if="scope.row.isEssence === 1" size="mini" type="warning" effect="dark">精华</el-tag>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="content" label="内容预览" min-width="250">
        <template slot-scope="scope">
          <div class="content-preview">{{ truncateText(scope.row.content, 80) }}</div>
        </template>
      </el-table-column>

      <el-table-column prop="category" label="分类" width="100" align="center">
        <template slot-scope="scope">
          <span :class="['category-tag', getCategoryClass(scope.row.category)]">
            {{ getCategoryName(scope.row.category) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="数据" width="150" align="center">
        <template slot-scope="scope">
          <div class="data-stats">
            <span><i class="el-icon-star-on"></i> {{ scope.row.likeCount || 0 }}</span>
            <span><i class="el-icon-chat-dot-round"></i> {{ scope.row.commentCount || 0 }}</span>
            <span><i class="el-icon-view"></i> {{ scope.row.viewCount || 0 }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
          >
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column label="置顶" width="70" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.isTop === 1"
              active-color="#e6a23c"
              inactive-color="#dcdfe6"
              @change="(val) => handleTopChange(scope.row, val)"
          >
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column label="精华" width="70" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.isEssence === 1"
              active-color="#f56c6c"
              inactive-color="#dcdfe6"
              @change="(val) => handleEssenceChange(scope.row, val)"
          >
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="发布时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button
                size="small"
                type="primary"
                plain
                circle
                @click="handleView(scope.row)"
                title="查看详情"
            >
              <i class="el-icon-view"></i>
            </el-button>
            <el-button
                size="small"
                type="danger"
                plain
                circle
                @click="handleDelete(scope.row)"
                title="删除"
            >
              <i class="el-icon-delete"></i>
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
      >
      </el-pagination>
    </div>

    <!-- 帖子详情对话框 -->
    <el-dialog title="帖子详情" :visible.sync="detailVisible" width="750px" center class="post-detail-dialog">
      <div class="post-detail" v-if="currentPost">
        <!--  修改：发布者信息 - 优先显示昵称 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-user"></i>
            <span>发布者信息</span>
          </div>
          <div class="detail-user">
            <el-avatar :size="48" :src="currentPost.userAvatar" class="user-avatar">
              {{ getDetailDisplayName().charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="detail-user-info">
              <div class="detail-user-name">{{ getDetailDisplayName() }}</div>
              <div class="detail-user-username" v-if="currentPost.userNickname && currentPost.userName">
                {{ currentPost.userName }}
              </div>
              <div class="detail-time">{{ formatDate(currentPost.createTime) }}</div>
            </div>
          </div>
        </div>

        <!-- 帖子信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-document"></i>
            <span>帖子信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="24">
              <div class="detail-item">
                <span class="detail-label">标题：</span>
                <span class="detail-value">{{ currentPost.title }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">分类：</span>
                <span :class="['category-tag', getCategoryClass(currentPost.category)]">
                  {{ getCategoryName(currentPost.category) }}
                </span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">状态：</span>
                <el-tag :type="currentPost.status === 1 ? 'success' : 'danger'" size="small">
                  {{ currentPost.status === 1 ? '正常' : '已删除' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">置顶：</span>
                <el-tag :type="currentPost.isTop === 1 ? 'warning' : 'info'" size="small">
                  {{ currentPost.isTop === 1 ? '已置顶' : '未置顶' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">精华：</span>
                <el-tag :type="currentPost.isEssence === 1 ? 'danger' : 'info'" size="small">
                  {{ currentPost.isEssence === 1 ? '精华帖' : '普通帖' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="detail-label">点赞数：</span>
                <span class="detail-value">{{ currentPost.likeCount || 0 }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="detail-label">评论数：</span>
                <span class="detail-value">{{ currentPost.commentCount || 0 }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="detail-label">浏览量：</span>
                <span class="detail-value">{{ currentPost.viewCount || 0 }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 帖子内容 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-edit-outline"></i>
            <span>帖子内容</span>
          </div>
          <div class="detail-content-text">{{ currentPost.content }}</div>
          <div class="detail-images" v-if="currentPost.images">
            <div class="images-title">图片附件：</div>
            <div class="images-list">
              <el-image
                  v-for="(img, idx) in currentPost.images.split(',')"
                  :key="idx"
                  :src="img"
                  fit="cover"
                  class="detail-img"
                  :preview-src-list="currentPost.images.split(',')"
              ></el-image>
            </div>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToPost">查看原帖</el-button>
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
  name: 'AdminPostManage',
  data() {
    return {
      loading: false,
      batchLoading: false,
      postList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        category: '',
        status: ''
      },
      statistics: {
        totalPosts: 0,
        totalComments: 0,
        todayPosts: 0,
        totalViews: 0
      },
      detailVisible: false,
      currentPost: null
    };
  },
  created() {
    this.loadList();
    this.loadStatistics();
  },
  methods: {
    // 获取显示名称（优先昵称，其次用户名）
    getDisplayName(row) {
      return row.userNickname || row.userName || '未知用户';
    },

    // 获取详情页显示名称
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

          this.statistics = {
            totalPosts: this.total,
            totalComments: this.postList.reduce((sum, p) => sum + (p.commentCount || 0), 0),
            todayPosts: this.postList.filter(p => {
              const today = new Date().toDateString();
              return p.createTime && new Date(p.createTime).toDateString() === today;
            }).length,
            totalViews: this.postList.reduce((sum, p) => sum + (p.viewCount || 0), 0)
          };
        }
      } catch (error) {
        console.error('加载失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadStatistics() {
      try {
        const res = await getPostStatistics();
        if (res.code === 200) {
          this.statistics = res.data;
        }
      } catch (error) {
        console.error('加载统计失败', error);
        this.statistics = {
          totalPosts: this.total,
          totalComments: this.postList.reduce((sum, p) => sum + (p.commentCount || 0), 0),
          todayPosts: 0,
          totalViews: this.postList.reduce((sum, p) => sum + (p.viewCount || 0), 0)
        };
      }
    },

    handleSearch() {
      this.page = 1;
      this.loadList();
    },

    handleReset() {
      this.searchForm = { keyword: '', category: '', status: '' };
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

    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    },

    getCategoryName(category) {
      const map = {
        pet_daily: '宠物日常',
        health: '健康分享',
        food: '美食分享',
        adopt: '领养信息',
        other: '其他'
      };
      return map[category] || '其他';
    },

    getCategoryClass(category) {
      const map = {
        pet_daily: 'category-pet',
        health: 'category-health',
        food: 'category-food',
        adopt: 'category-adopt',
        other: 'category-other'
      };
      return map[category] || 'category-other';
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      try {
        const res = await updatePostStatus(row.id, newStatus);
        if (res.code === 200) {
          row.status = newStatus;
          this.$message.success(res.message);
          await this.loadStatistics();
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
          await this.loadStatistics();
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
          await this.loadStatistics();
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除帖子 "${row.title}" 吗？`, '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await adminDeletePost(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadList();
            this.loadStatistics();
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    async handleBatchTop() {
      if (this.selectedRows.length === 0) return;

      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要置顶选中的 ${this.selectedRows.length} 个帖子吗？`, '提示', { type: 'info' })
          .then(async () => {
            this.batchLoading = true;
            try {
              const res = await batchUpdatePostTop(ids, 1);
              if (res.code === 200) {
                this.$message.success(res.message);
                this.selectedRows = [];
                await this.loadList();
                await this.loadStatistics();
              } else {
                this.$message.error(res.message);
              }
            } catch (error) {
              this.$message.error('操作失败');
            } finally {
              this.batchLoading = false;
            }
          }).catch(() => {});
    },

    async handleBatchEssence() {
      if (this.selectedRows.length === 0) return;

      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要将选中的 ${this.selectedRows.length} 个帖子设为精华吗？`, '提示', { type: 'info' })
          .then(async () => {
            this.batchLoading = true;
            try {
              const res = await batchUpdatePostEssence(ids, 1);
              if (res.code === 200) {
                this.$message.success(res.message);
                this.selectedRows = [];
                await this.loadList();
                await this.loadStatistics();
              } else {
                this.$message.error(res.message);
              }
            } catch (error) {
              this.$message.error('操作失败');
            } finally {
              this.batchLoading = false;
            }
          }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;

      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个帖子吗？删除后无法恢复！`, '警告', { type: 'warning' })
          .then(async () => {
            this.batchLoading = true;
            try {
              const res = await batchDeletePosts(ids);
              if (res.code === 200) {
                this.$message.success(res.message);
                this.selectedRows = [];
                await this.loadList();
                await this.loadStatistics();
              } else {
                this.$message.error(res.message);
              }
            } catch (error) {
              this.$message.error('批量删除失败');
            } finally {
              this.batchLoading = false;
            }
          }).catch(() => {});
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
.admin-post-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.stat-card {
  border-radius: 16px;
  overflow: hidden;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

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
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
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
  width: 200px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.status-select {
  width: 110px;
}

.search-btn, .reset-btn {
  border-radius: 8px;
  padding: 8px 16px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.post-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.user-name {
  font-weight: 500;
  font-size: 13px;
  color: #2c3e50;
}

.user-username {
  font-size: 11px;
  color: #909399;
}

.post-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.title-text {
  font-weight: 500;
  color: #2c3e50;
}

.content-preview {
  color: #5a6874;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.category-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
}

.category-pet { background: #e8f4fd; color: #409EFF; }
.category-health { background: #e8f8f0; color: #67c23a; }
.category-food { background: #fef0e6; color: #e6a23c; }
.category-adopt { background: #fde6e6; color: #f56c6c; }
.category-other { background: #f4f4f5; color: #909399; }

.data-stats {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.data-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-buttons .el-button {
  width: 32px;
  height: 32px;
  padding: 0;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
}

/* 帖子详情对话框样式 */
.post-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.post-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.post-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.post-detail-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.post-detail-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.post-detail {
  max-height: 60vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
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
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 15px;
}

.section-title i {
  color: #667eea;
  font-size: 16px;
}

/* 详情页用户信息样式 */
.detail-user {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.detail-user-info {
  flex: 1;
}

.detail-user-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 15px;
}

.detail-user-username {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.detail-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.detail-item {
  margin-bottom: 12px;
}

.detail-label {
  font-size: 13px;
  color: #909399;
  display: inline-block;
  width: 60px;
}

.detail-value {
  font-size: 13px;
  color: #2c3e50;
  margin-left: 4px;
}

.detail-content-text {
  background: #f8f9fc;
  padding: 16px;
  border-radius: 12px;
  color: #5a6874;
  line-height: 1.8;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.images-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.images-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.detail-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  background: #f8f9fc;
  object-fit: cover;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
}
</style>