<template>
  <div class="service-category">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增分类
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="success"
            plain
            @click="handleBatchEnable"
            class="batch-btn"
        >
          <i class="el-icon-check"></i> 启用 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDisable"
            class="batch-btn"
        >
          <i class="el-icon-close"></i> 禁用 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
            class="batch-btn"
        >
          <i class="el-icon-delete"></i> 删除 ({{ selectedRows.length }})
        </el-button>
        <!-- 保存排序按钮 -->
        <el-button
            v-if="sortChanged"
            type="success"
            plain
            @click="saveSortOrder"
            :loading="sortSaving"
            class="batch-btn"
        >
          <i class="el-icon-check"></i> 保存排序
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索分类名称"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          ></el-input>
        </div>
        <el-select
            v-model="searchForm.status"
            placeholder="状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="启用" :value="1"></el-option>
          <el-option label="禁用" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
        <el-button type="success" size="medium" @click="handleExport" :loading="exportLoading" plain class="export-btn">
          <i class="el-icon-download"></i> 导出
        </el-button>
      </div>
    </div>

    <!-- 分类表格 - 整行可拖拽 -->
    <el-table
        v-loading="loading"
        :data="categoryList"
        stripe
        row-key="id"
        class="category-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>

      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <el-table-column prop="name" label="分类名称" min-width="200">
        <template slot-scope="scope">
          <span class="name-text">{{ scope.row.name }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="sortOrder" label="排序值" width="100" align="center">
        <template slot-scope="scope">
          <span class="sort-text">{{ scope.row.sortOrder }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="serviceCount" label="服务数量" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="small" :type="scope.row.serviceCount > 0 ? 'success' : 'info'">
            {{ scope.row.serviceCount || 0 }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
              :loading="scope.row.statusLoading"
          ></el-switch>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="240" fixed="right" align="center">
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
                @click="handleEdit(scope.row)"
                class="action-icon-btn"
                title="编辑"
            >
              <i class="el-icon-edit"></i>
            </el-button>
            <el-button
                size="small"
                type="danger"
                plain
                circle
                @click="handleDelete(scope.row)"
                class="action-icon-btn"
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
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
      ></el-pagination>
    </div>

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="500px"
        :close-on-click-modal="false"
        class="category-dialog"
        center
    >
      <div class="dialog-content">
        <el-form :model="currentCategory" :rules="formRules" ref="categoryForm" label-width="80px">
          <el-form-item label="分类名称" prop="name">
            <el-input v-model="currentCategory.name" placeholder="请输入分类名称" size="medium"></el-input>
          </el-form-item>

          <el-form-item label="排序值">
            <el-input-number v-model="currentCategory.sortOrder" :min="0" :max="999" size="medium" controls-position="right"></el-input-number>
            <span class="form-tip">数字越小越靠前</span>
          </el-form-item>

          <el-form-item label="状态">
            <el-radio-group v-model="currentCategory.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 分类详情对话框 -->
    <el-dialog
        title="分类详情"
        :visible.sync="detailVisible"
        width="480px"
        center
        class="category-detail-dialog"
    >
      <div class="detail-content" v-if="currentDetailCategory">
        <div class="detail-item">
          <span class="detail-label">ID：</span>
          <span class="detail-value">{{ currentDetailCategory.id }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">分类名称：</span>
          <span class="detail-value">{{ currentDetailCategory.name }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">排序值：</span>
          <span class="detail-value">{{ currentDetailCategory.sortOrder }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">服务数量：</span>
          <el-tag size="small" :type="currentDetailCategory.serviceCount > 0 ? 'success' : 'info'">
            {{ currentDetailCategory.serviceCount || 0 }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态：</span>
          <el-tag :type="currentDetailCategory.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentDetailCategory.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="detail-label">创建时间：</span>
          <span class="detail-value">{{ formatDate(currentDetailCategory.createTime) }}</span>
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
  getServiceCategoryList,
  addServiceCategory,
  updateServiceCategory,
  updateServiceCategoryStatus,
  deleteServiceCategory,
  batchDeleteServiceCategories,
  batchUpdateServiceCategoryStatus
} from '@/api/service';
import { batchUpdateServiceCategorySort } from '@/api/service';
import Sortable from 'sortablejs';

export default {
  name: 'ServiceCategory',
  data() {
    return {
      loading: false,
      submitLoading: false,
      exportLoading: false,
      sortSaving: false,
      sortChanged: false,
      detailVisible: false,
      currentDetailCategory: null,
      categoryList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        status: ''
      },
      dialogVisible: false,
      isEdit: false,
      currentCategory: {
        id: null,
        name: '',
        sortOrder: 0,
        status: 1
      },
      formRules: {
        name: [
          { required: true, message: '请输入分类名称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑分类' : '新增分类';
    }
  },
  created() {
    this.loadCategoryList();
  },
  mounted() {
    this.initSortable();
  },
  methods: {
    async loadCategoryList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          status: this.searchForm.status !== '' && this.searchForm.status !== null ? this.searchForm.status : undefined
        };
        const res = await getServiceCategoryList(params);
        if (res.code === 200) {
          this.categoryList = res.data.list.map(item => ({
            ...item,
            statusLoading: false
          }));
          this.total = res.data.total;
          this.sortChanged = false;
        }
      } catch (error) {
        console.error('加载分类列表失败', error);
        this.$message.error('加载分类列表失败');
      } finally {
        this.loading = false;
      }
    },

    // 整行可拖拽排序
    initSortable() {
      const el = this.$el.querySelector('.el-table__body-wrapper tbody');
      if (!el) return;

      if (this.sortableInstance) {
        this.sortableInstance.destroy();
      }

      this.sortableInstance = new Sortable(el, {
        animation: 300,
        onEnd: (evt) => {
          const oldIndex = evt.oldIndex;
          const newIndex = evt.newIndex;

          if (oldIndex === newIndex) return;

          const movedItem = this.categoryList.splice(oldIndex, 1)[0];
          this.categoryList.splice(newIndex, 0, movedItem);

          this.categoryList.forEach((item, index) => {
            item.sortOrder = index;
          });

          this.$forceUpdate();
          this.sortChanged = true;

          console.log('拖拽后排序:', this.categoryList.map(i => ({id: i.id, order: i.sortOrder})));
        }
      });
    },

    // 查看分类详情
    handleView(row) {
      this.currentDetailCategory = row;
      this.detailVisible = true;
    },

    async saveSortOrder() {
      if (!this.sortChanged) {
        this.$message.info('排序未发生变化');
        return;
      }

      this.sortSaving = true;
      try {
        const sortList = this.categoryList.map((item, index) => ({
          id: item.id,
          sortOrder: index
        }));

        const res = await batchUpdateServiceCategorySort(sortList);

        if (res.code === 200) {
          this.$message.success('排序保存成功');
          this.sortChanged = false;
          await this.loadCategoryList();
        } else {
          this.$message.error(res.message || '保存失败');
          await this.loadCategoryList();
        }
      } catch (error) {
        console.error('保存排序失败:', error);
        this.$message.error('保存排序失败');
        await this.loadCategoryList();
      } finally {
        this.sortSaving = false;
      }
    },

    // ✅ 批量启用
    async handleBatchEnable() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要操作的数据');
        return;
      }

      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要启用选中的 ${this.selectedRows.length} 个分类吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const res = await batchUpdateServiceCategoryStatus(ids, 1);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadCategoryList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量启用失败');
        }
      }).catch(() => {});
    },

    // ✅ 批量禁用
    async handleBatchDisable() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要操作的数据');
        return;
      }

      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要禁用选中的 ${this.selectedRows.length} 个分类吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchUpdateServiceCategoryStatus(ids, 0);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadCategoryList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量禁用失败');
        }
      }).catch(() => {});
    },

    // ✅ 批量删除
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要操作的数据');
        return;
      }

      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个分类吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteServiceCategories(ids);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadCategoryList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量删除失败');
        }
      }).catch(() => {});
    },

    // ✅ 导出功能
    async handleExport() {
      this.exportLoading = true;
      try {
        const params = new URLSearchParams();
        if (this.searchForm.keyword) params.append('keyword', this.searchForm.keyword);
        if (this.searchForm.status !== '' && this.searchForm.status !== null) params.append('status', this.searchForm.status);

        const token = localStorage.getItem('token');
        const url = `/api/admin/service/category/export?${params.toString()}`;

        const response = await fetch(url, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) throw new Error('导出失败');

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = `服务分类_${new Date().getTime()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);

        this.$message.success('导出成功');
      } catch (error) {
        console.error('导出失败', error);
        this.$message.error('导出失败');
      } finally {
        this.exportLoading = false;
      }
    },

    handleSearch() {
      this.page = 1;
      this.loadCategoryList();
    },

    handleReset() {
      this.searchForm = {
        keyword: '',
        status: ''
      };
      this.page = 1;
      this.loadCategoryList();
    },

    handlePageChange(page) {
      this.page = page;
      this.loadCategoryList();
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadCategoryList();
    },

    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '启用' : '禁用';
      this.$confirm(`确定要${action}分类 "${row.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: newStatus === 1 ? 'info' : 'warning'
      }).then(async () => {
        row.statusLoading = true;
        try {
          const res = await updateServiceCategoryStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(res.message);
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('操作失败');
        } finally {
          row.statusLoading = false;
        }
      }).catch(() => {});
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除分类 "${row.name}" 吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteServiceCategory(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadCategoryList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    handleAdd() {
      this.isEdit = false;
      this.currentCategory = {
        id: null,
        name: '',
        sortOrder: 0,
        status: 1
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.categoryForm) {
          this.$refs.categoryForm.clearValidate();
        }
      });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.currentCategory = {
        id: row.id,
        name: row.name,
        sortOrder: row.sortOrder,
        status: row.status
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.categoryForm) {
          this.$refs.categoryForm.clearValidate();
        }
      });
    },

    submitForm() {
      this.$refs.categoryForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            res = await updateServiceCategory(this.currentCategory);
          } else {
            res = await addServiceCategory(this.currentCategory);
          }

          if (res.code === 200) {
            this.$message.success(res.message);
            this.dialogVisible = false;
            this.loadCategoryList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error(this.isEdit ? '更新失败' : '添加失败');
        } finally {
          this.submitLoading = false;
        }
      });
    }
  },

  beforeDestroy() {
    if (this.sortableInstance) {
      this.sortableInstance.destroy();
    }
  }
};
</script>

<style scoped>
.service-category {
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.action-right {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.add-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-weight: 500;
  padding: 8px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.batch-btn {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s;
}

.export-btn {
  border-color: #67c23a;
  color: #67c23a;
}

.export-btn:hover {
  background: #f0f9ff;
  border-color: #67c23a;
  color: #67c23a;
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
  width: 240px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.status-select {
  width: 100px;
}

.status-select ::v-deep .el-input__inner {
  border-radius: 8px;
}

.search-btn, .reset-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.category-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.category-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
}

.name-text {
  font-weight: 500;
  color: #2c3e50;
}

.sort-text {
  color: #606266;
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

.category-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.category-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.category-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.dialog-content {
  padding: 10px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.dialog-footer {
  text-align: right;
  padding: 16px 24px 20px;
  border-top: 1px solid #eef2f6;
}

/* 拖拽时的行样式 */
.sortable-drag {
  opacity: 0.5;
  background: #f0f2f5 !important;
}
.export-btn {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  color: white;
  font-weight: 500;
  padding: 8px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.export-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
  color: white;
}

/* 分类详情对话框样式 */
.category-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.category-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.category-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.category-detail-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.category-detail-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.detail-content {
  padding: 0 10px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  width: 85px;
  font-size: 14px;
  color: #909399;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  font-size: 14px;
  color: #2c3e50;
}
</style>