<template>
  <div class="service-category-page">
    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索分类名称..."
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
          class="status-select"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button type="primary" size="medium" @click="handleAdd" class="warm-btn">
          <i class="el-icon-plus" /> 新增分类
        </el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-enable" @click="handleBatchEnable">
          <i class="el-icon-check" /> 批量启用
        </el-button>
        <el-button size="small" class="batch-btn-disable" @click="handleBatchDisable">
          <i class="el-icon-close" /> 批量禁用
        </el-button>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 排序提示 -->
    <transition name="batch-bar">
      <div class="batch-bar sort-bar" v-if="sortChanged">
        <span class="batch-info">排序已变更，请保存</span>
        <el-button size="small" type="success" @click="saveSortOrder" :loading="sortSaving">
          <i class="el-icon-check" /> 保存排序
        </el-button>
      </div>
    </transition>

    <!-- 表格卡片 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="categoryList"
        stripe
        row-key="id"
        class="category-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="200">
          <template slot-scope="scope">
            <span class="name-text">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序值" width="100" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.sortOrder }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="serviceCount" label="服务数量" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.serviceCount > 0 ? 'success' : 'info'">
              {{ scope.row.serviceCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.status === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleStatusChange(scope.row, val)"
              :loading="scope.row.statusLoading"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-text cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleView(scope.row)" class="act-view">
                <i class="el-icon-view" /> 查看
              </el-button>
              <el-button type="text" size="small" @click="handleEdit(scope.row)" class="act-edit">
                <i class="el-icon-edit" /> 编辑
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
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="520px"
      :close-on-click-modal="false"
      class="category-dialog"
      center
    >
      <div class="dialog-body">
        <el-form :model="currentCategory" :rules="formRules" ref="categoryForm" label-width="80px" class="category-form">
          <el-form-item label="分类名称" prop="name">
            <el-input v-model="currentCategory.name" placeholder="请输入分类名称" />
          </el-form-item>
          <el-form-item label="排序值">
            <el-input-number v-model="currentCategory.sortOrder" :min="0" :max="999" controls-position="right" />
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
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      title="分类详情"
      :visible.sync="detailVisible"
      width="480px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetailCategory">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="d-label">ID</span>
            <span class="d-value">{{ currentDetailCategory.id }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">分类名称</span>
            <span class="d-value">{{ currentDetailCategory.name }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">排序值</span>
            <span class="d-value">{{ currentDetailCategory.sortOrder }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">服务数量</span>
            <span class="d-value">
              <el-tag size="small" :type="currentDetailCategory.serviceCount > 0 ? 'success' : 'info'">
                {{ currentDetailCategory.serviceCount || 0 }}
              </el-tag>
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">状态</span>
            <span class="d-value">
              <span class="status-tag" :class="currentDetailCategory.status === 1 ? 'active' : 'inactive'">
                {{ currentDetailCategory.status === 1 ? '启用' : '禁用' }}
              </span>
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">创建时间</span>
            <span class="d-value">{{ formatDate(currentDetailCategory.createTime) }}</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
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
  batchUpdateServiceCategoryStatus,
  batchUpdateServiceCategorySort
} from '@/api/service';
import Sortable from 'sortablejs';

export default {
  name: 'Admin_ServiceCategory',
  data() {
    return {
      loading: false,
      submitLoading: false,
      sortSaving: false,
      sortChanged: false,
      detailVisible: false,
      currentDetailCategory: null,
      categoryList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', status: '' },
      dialogVisible: false,
      isEdit: false,
      currentCategory: { id: null, name: '', sortOrder: 0, status: 1 },
      formRules: {
        name: [
          { required: true, message: '请输入分类名称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ]
      },
      sortableInstance: null
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
          this.categoryList = res.data.list.map(item => ({ ...item, statusLoading: false }));
          this.total = res.data.total;
          this.sortChanged = false;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载分类列表失败');
      } finally {
        this.loading = false;
      }
    },

    initSortable() {
      const el = this.$el.querySelector('.el-table__body-wrapper tbody');
      if (!el) return;
      if (this.sortableInstance) {
        this.sortableInstance.destroy();
        this.sortableInstance = null;
      }
      this.sortableInstance = new Sortable(el, {
        animation: 300,
        onEnd: (evt) => {
          const oldIndex = evt.oldIndex;
          const newIndex = evt.newIndex;
          if (oldIndex === newIndex) return;
          const movedItem = this.categoryList.splice(oldIndex, 1)[0];
          this.categoryList.splice(newIndex, 0, movedItem);
          this.categoryList.forEach((item, index) => { item.sortOrder = index; });
          this.$forceUpdate();
          this.sortChanged = true;
        }
      });
    },

    handleView(row) {
      this.currentDetailCategory = row;
      this.detailVisible = true;
    },

    async saveSortOrder() {
      if (!this.sortChanged) { this.$message.info('排序未发生变化'); return; }
      this.sortSaving = true;
      try {
        const sortList = this.categoryList.map((item, index) => ({ id: item.id, sortOrder: index }));
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
        this.$message.error(error?.message || '保存排序失败');
        await this.loadCategoryList();
      } finally {
        this.sortSaving = false;
      }
    },

    async handleBatchEnable() {
      if (this.selectedRows.length === 0) { this.$message.warning('请选择要操作的数据'); return; }
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要启用选中的 ${this.selectedRows.length} 个分类吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
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
          this.$message.error(error?.message || '批量启用失败');
        }
      }).catch(() => {});
    },

    async handleBatchDisable() {
      if (this.selectedRows.length === 0) { this.$message.warning('请选择要操作的数据'); return; }
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要禁用选中的 ${this.selectedRows.length} 个分类吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
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
          this.$message.error(error?.message || '批量禁用失败');
        }
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) { this.$message.warning('请选择要操作的数据'); return; }
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个分类吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
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
          this.$message.error(error?.message || '批量删除失败');
        }
      }).catch(() => {});
    },

    handleSearch() { this.page = 1; this.loadCategoryList(); },
    handleReset() {
      this.searchForm = { keyword: '', status: '' };
      this.page = 1;
      this.loadCategoryList();
    },
    handlePageChange(page) { this.page = page; this.loadCategoryList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadCategoryList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '启用' : '禁用';
      this.$confirm(`确定要${action}分类 "${row.name}" 吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: newStatus === 1 ? 'info' : 'warning'
      }).then(async () => {
        row.statusLoading = true;
        try {
          const res = await updateServiceCategoryStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(res.message || '操作成功');
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '操作失败');
        } finally {
          row.statusLoading = false;
        }
      }).catch(() => {});
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除分类 "${row.name}" 吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteServiceCategory(row.id);
          if (res.code === 200) {
            this.$message.success(res.message || '删除成功');
            this.loadCategoryList();
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '删除失败');
        }
      }).catch(() => {});
    },

    handleAdd() {
      this.isEdit = false;
      this.currentCategory = { id: null, name: '', sortOrder: 0, status: 1 };
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.categoryForm && this.$refs.categoryForm.clearValidate(); });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.currentCategory = {
        id: row.id, name: row.name, sortOrder: row.sortOrder, status: row.status
      };
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.categoryForm && this.$refs.categoryForm.clearValidate(); });
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
            this.$message.success(res.message || (this.isEdit ? '更新成功' : '添加成功'));
            this.dialogVisible = false;
            this.loadCategoryList();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || (this.isEdit ? '更新失败' : '添加失败'));
        } finally {
          this.submitLoading = false;
        }
      });
    }
  },

  beforeDestroy() {
    if (this.sortableInstance) {
      this.sortableInstance.destroy();
      this.sortableInstance = null;
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.service-category-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

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
.filter-left, .filter-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.keyword-input { width: 220px; }
.keyword-input ::v-deep .el-input__inner { border-radius: 10px; }
.status-select { width: 100px; }
.status-select ::v-deep .el-input__inner { border-radius: 10px; }

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
.sort-bar {
  background: linear-gradient(135deg, #edf7ee, #f0f9ef);
  border-color: #b3e0b3;
  border-left-color: #67c23a;
}
.batch-info { font-size: 13px; }
.batch-info strong { color: #f59e4b; font-size: 15px; }
.batch-bar .el-button {
  border-radius: 8px; padding: 7px 16px;
  font-weight: 500; transition: all 0.2s;
}
.batch-btn-enable {
  background: rgba(103, 194, 58, 0.15) !important;
  border: 1px solid rgba(103, 194, 58, 0.35) !important;
  color: #67c23a !important;
}
.batch-btn-enable:hover { background: rgba(103, 194, 58, 0.25) !important; border-color: #67c23a !important; }
.batch-btn-disable {
  background: rgba(245, 158, 75, 0.15) !important;
  border: 1px solid rgba(245, 158, 75, 0.35) !important;
  color: #f59e4b !important;
}
.batch-btn-disable:hover { background: rgba(245, 158, 75, 0.25) !important; border-color: #f59e4b !important; }
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

.category-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.category-table ::v-deep .el-table__body td { padding: 15px 0; font-size: 13px; color: #3d2e2a; }
.category-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.category-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.category-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.category-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b; border-color: #f59e4b;
}

.name-text { font-weight: 600; color: #3d2e2a; }
.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { color: #a08c84; font-size: 12px; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
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

/* ===== 对话框 ===== */
.category-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.category-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.category-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.category-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.category-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.category-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }

.category-form ::v-deep .el-form-item { margin-bottom: 18px; }
.category-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.category-form ::v-deep .el-input__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.category-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}
.category-form ::v-deep .el-radio__input.is-checked .el-radio__inner {
  background: #f59e4b; border-color: #f59e4b;
}

.form-tip { font-size: 12px; color: #a08c84; margin-left: 8px; }

.dialog-footer {
  text-align: right;
  padding: 16px 28px 22px;
  border-top: 1px solid #f5ece6;
}
.dialog-footer .el-button {
  border-radius: 10px; padding: 10px 28px; font-weight: 500; font-size: 13px;
}
.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
}
.dialog-footer .el-button--primary:hover { opacity: 0.88; transform: translateY(-1px); }

/* ===== 详情对话框 ===== */
.detail-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.detail-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.detail-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.detail-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.detail-dialog ::v-deep .el-dialog__body { padding: 28px; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
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
.d-label { width: 68px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; font-weight: 500; }
.status-tag { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-tag.active   { background: #edf7ee; color: #67c23a; }
.status-tag.inactive { background: #fef0f0; color: #f56c6c; }

/* ===== 拖拽样式 ===== */
.sortable-drag { opacity: 0.5; background: #f5ece6 !important; }

/* 响应式 */
@media (max-width: 768px) {
  .service-category-page { padding: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 100%; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-item { border-right: none; }
  .detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }
  .detail-item:last-child { border-bottom: none; }
}
</style>
