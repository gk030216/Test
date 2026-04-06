<template>
  <div class="admin-post-manage">
    <div class="action-bar">
      <div class="action-left">
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
        >
          批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <el-input
            v-model="searchForm.keyword"
            placeholder="搜索标题/内容"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 100px" @change="handleSearch">
          <el-option label="正常" :value="1"></el-option>
          <el-option label="已删除" :value="0"></el-option>
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <el-table
        v-loading="loading"
        :data="postList"
        stripe
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70"></el-table-column>
      <el-table-column prop="userName" label="用户" width="120"></el-table-column>
      <el-table-column prop="title" label="标题" min-width="200"></el-table-column>
      <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip></el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              @change="(val) => updateStatus(scope.row, val)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.isTop === 1"
              @change="(val) => updateTop(scope.row, val)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="精华" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.isEssence === 1"
              @change="(val) => updateEssence(scope.row, val)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="160"></el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template slot-scope="scope">
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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
        status: ''
      }
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
          status: this.searchForm.status || undefined
        };
        const res = await getAdminPostList(params);
        if (res.code === 200) {
          this.postList = res.data.list;
          this.total = res.data.total;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() { this.searchForm = { keyword: '', status: '' }; this.page = 1; this.loadList(); },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },
    async updateStatus(row, val) {
      try {
        const res = await updatePostStatus(row.id, val ? 1 : 0);
        if (res.code === 200) {
          row.status = val ? 1 : 0;
          this.$message.success(res.message);
        }
      } catch (error) { this.$message.error('操作失败'); }
    },
    async updateTop(row, val) {
      try {
        const res = await updatePostTop(row.id, val ? 1 : 0);
        if (res.code === 200) {
          row.isTop = val ? 1 : 0;
          this.$message.success(res.message);
        }
      } catch (error) { this.$message.error('操作失败'); }
    },
    async updateEssence(row, val) {
      try {
        const res = await updatePostEssence(row.id, val ? 1 : 0);
        if (res.code === 200) {
          row.isEssence = val ? 1 : 0;
          this.$message.success(res.message);
        }
      } catch (error) { this.$message.error('操作失败'); }
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除帖子 "${row.title}" 吗？`, '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await adminDeletePost(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadList();
          }
        } catch (error) { this.$message.error('删除失败'); }
      }).catch(() => {});
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
.action-bar {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
}
</style>