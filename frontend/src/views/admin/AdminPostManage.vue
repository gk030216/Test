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
        >
          <i class="el-icon-top"></i> 批量置顶 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="warning"
            plain
            @click="handleBatchEssence"
        >
          <i class="el-icon-star-on"></i> 批量精华 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
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
          <el-option label="已删除" :value="0"></el-option>
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

      <el-table-column label="用户" width="120" align="center">
        <template slot-scope="scope">
          <div class="user-info">
            <el-avatar :size="32" :src="scope.row.userAvatar" class="user-avatar">
              {{ scope.row.userName ? scope.row.userName.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
            <span class="user-name">{{ scope.row.userName || '未知用户' }}</span>
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
    <el-dialog :title="'帖子详情'" :visible.sync="detailVisible" width="700px" center>
      <div class="detail-content" v-if="currentPost">
        <div class="detail-user">
          <el-avatar :size="40" :src="currentPost.userAvatar">
            {{ currentPost.userName ? currentPost.userName.charAt(0).toUpperCase() : 'U' }}
          </el-avatar>
          <div class="detail-user-info">
            <div class="detail-user-name">{{ currentPost.userName || '匿名用户' }}</div>
            <div class="detail-time">{{ formatDate(currentPost.createTime) }}</div>
          </div>
        </div>
        <div class="detail-title">{{ currentPost.title }}</div>
        <div class="detail-text">{{ currentPost.content }}</div>
        <div class="detail-images" v-if="currentPost.images">
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
      <span slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToPost">查看原帖</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminPostList, updatePostStatus, updatePostTop, updatePostEssence, adminDeletePost } from '@/api/community';

export default {
  name: 'AdminPostManage',
  data() {
    return {
      loading: false,
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
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          category: this.searchForm.category || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined
        };
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
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    async handleBatchTop() {
      this.$message.info('批量置顶功能开发中');
    },

    async handleBatchEssence() {
      this.$message.info('批量精华功能开发中');
    },

    async handleBatchDelete() {
      this.$message.info('批量删除功能开发中');
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

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 13px;
  color: #2c3e50;
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

.detail-user {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eef2f6;
}

.detail-user-name {
  font-weight: 600;
  color: #2c3e50;
}

.detail-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.detail-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
}

.detail-text {
  color: #5a6874;
  line-height: 1.8;
  margin-bottom: 20px;
}

.detail-images {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.detail-img {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
}
</style>