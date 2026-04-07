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
            type="danger"
            plain
            @click="handleBatchDelete"
            class="batch-btn"
        >
          <i class="el-icon-delete"></i> 删除 ({{ selectedRows.length }})
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
          >
          </el-input>
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
      </div>
    </div>

    <!-- 分类表格 - 树形结构 -->
    <el-table
        v-loading="loading"
        :data="treeData"
        stripe
        style="width: 100%"
        class="category-table"
        @selection-change="handleSelectionChange"
        row-key="id"
        default-expand-all
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="85" align="center"></el-table-column>

      <el-table-column prop="name" label="分类名称" min-width="200">
        <template slot-scope="scope">
          <div class="category-name" :style="{ paddingLeft: (scope.row.level * 24) + 'px' }">
            <span class="name-text">{{ scope.row.name }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="父分类" width="150" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.parentId === 0" class="parent-text">顶级分类</span>
          <span v-else class="parent-text">{{ getParentName(scope.row) }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="sortOrder" label="排序" width="100" align="center">
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
          >
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button
                size="small"
                type="primary"
                plain
                circle
                @click="handleEdit(scope.row)"
                class="action-icon-btn"
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
      >
      </el-pagination>
    </div>

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="500px"
        :close-on-click-modal="false"
        class="category-dialog"
        center
        @opened="loadParentOptions"
    >
      <div class="dialog-content">
        <el-form :model="currentCategory" :rules="formRules" ref="categoryForm" label-width="80px">
          <el-form-item label="分类名称" prop="name">
            <el-input v-model="currentCategory.name" placeholder="请输入分类名称" size="medium"></el-input>
          </el-form-item>

          <el-form-item label="父分类">
            <el-select v-model="currentCategory.parentId" placeholder="请选择父分类" size="medium" clearable>
              <el-option label="顶级分类" :value="0"></el-option>
              <el-option
                  v-for="item in parentOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="item.id === currentCategory.id"
              ></el-option>
            </el-select>
            <div class="form-tip">选择父分类后，该分类将成为子分类</div>
          </el-form-item>

          <el-form-item label="排序">
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
  </div>
</template>

<script>
import {
  getServiceCategoryList,
  getAllServiceCategories,
  addServiceCategory,
  updateServiceCategory,
  updateServiceCategoryStatus,
  deleteServiceCategory,
  batchDeleteServiceCategories
} from '@/api/service';

export default {
  name: 'ServiceCategory',
  data() {
    return {
      loading: false,
      submitLoading: false,
      categoryList: [],
      treeData: [],
      allCategories: [],
      parentOptions: [],
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
        parentId: 0,
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
            statusLoading: false,
            level: 0
          }));
          this.total = res.data.total;
          this.buildTree();
        }
      } catch (error) {
        console.error('加载分类列表失败', error);
        this.$message.error('加载分类列表失败');
      } finally {
        this.loading = false;
      }
    },

    buildTree() {
      const list = [...this.categoryList];
      const tree = [];
      const map = {};

      list.forEach(item => {
        map[item.id] = { ...item, children: [], level: 0 };
      });

      list.forEach(item => {
        if (item.parentId === 0) {
          map[item.id].level = 0;
          tree.push(map[item.id]);
        } else if (map[item.parentId]) {
          map[item.parentId].children.push(map[item.id]);
          map[item.id].level = (map[item.parentId].level || 0) + 1;
        } else {
          map[item.id].level = 0;
          tree.push(map[item.id]);
        }
      });

      const sortFn = (a, b) => (a.sortOrder || 0) - (b.sortOrder || 0);
      tree.sort(sortFn);

      const sortChildren = (node) => {
        if (node.children && node.children.length) {
          node.children.sort(sortFn);
          node.children.forEach(sortChildren);
        }
      };
      tree.forEach(sortChildren);

      this.treeData = tree;
    },

    async loadParentOptions() {
      try {
        const res = await getAllServiceCategories();
        if (res.code === 200) {
          // 只显示启用的顶级分类作为父分类选项
          let options = res.data.filter(item => item.parentId === 0 && item.status === 1);

          if (this.isEdit && this.currentCategory.id) {
            options = options.filter(item => item.id !== this.currentCategory.id);
          }

          this.parentOptions = options;
        }
      } catch (error) {
        console.error('加载父分类失败', error);
      }
    },

    getParentName(row) {
      if (row.parentId === 0) return '顶级分类';
      // 优先使用后端返回的 parentName
      if (row.parentName) {
        return row.parentName;
      }
      const parent = this.categoryList.find(item => item.id === row.parentId);
      return parent ? parent.name : '顶级分类';
    },

    // 递归获取所有子分类ID
    getAllChildIds(parentId, list) {
      const childIds = [];
      const children = list.filter(item => item.parentId === parentId);
      for (const child of children) {
        childIds.push(child.id);
        childIds.push(...this.getAllChildIds(child.id, list));
      }
      return childIds;
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;

      // 如果要启用子分类，检查父分类是否启用
      if (newStatus === 1 && row.parentId !== 0) {
        const parent = this.categoryList.find(item => item.id === row.parentId);
        if (parent && parent.status !== 1) {
          this.$message.warning('父分类已禁用，无法启用子分类，请先启用父分类');
          return;
        }
      }

      const action = newStatus === 1 ? '启用' : '禁用';

      // 如果要禁用顶级分类，提示是否同时禁用子分类
      if (newStatus === 0 && row.parentId === 0) {
        const childIds = this.getAllChildIds(row.id, this.categoryList);
        if (childIds.length > 0) {
          this.$confirm(`禁用顶级分类 "${row.name}" 将会同时禁用其下的 ${childIds.length} 个子分类，是否继续？`, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(async () => {
            await this.doStatusChange(row, newStatus, action, childIds);
          }).catch(() => {});
          return;
        }
      }

      await this.doStatusChange(row, newStatus, action, []);
    },

    async doStatusChange(row, newStatus, action, childIds) {
      row.statusLoading = true;
      try {
        // 先更新当前分类
        const res = await updateServiceCategoryStatus(row.id, newStatus);
        if (res.code === 200) {
          row.status = newStatus;

          // 如果有子分类需要禁用，逐个更新
          for (const childId of childIds) {
            await updateServiceCategoryStatus(childId, 0);
          }

          this.$message.success(`${action}成功`);
          this.loadCategoryList();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
      } finally {
        row.statusLoading = false;
      }
    },

    async handleDelete(row) {
      // 检查是否有子分类
      const childIds = this.getAllChildIds(row.id, this.categoryList);
      if (childIds.length > 0) {
        this.$message.warning(`该分类下存在 ${childIds.length} 个子分类，请先删除子分类`);
        return;
      }

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

    handleAdd() {
      this.isEdit = false;
      this.currentCategory = {
        id: null,
        name: '',
        parentId: 0,
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
      // 如果父分类已禁用，不允许编辑子分类
      if (row.parentId !== 0) {
        const parent = this.categoryList.find(item => item.id === row.parentId);
        if (parent && parent.status !== 1) {
          this.$message.warning('父分类已禁用，无法编辑子分类，请先启用父分类');
          return;
        }
      }

      this.isEdit = true;
      this.currentCategory = {
        id: row.id,
        name: row.name,
        parentId: row.parentId,
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
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;

      // 检查是否有子分类
      let hasChild = false;
      for (const row of this.selectedRows) {
        const childIds = this.getAllChildIds(row.id, this.categoryList);
        if (childIds.length > 0) {
          hasChild = true;
          break;
        }
      }

      if (hasChild) {
        this.$message.warning('选中的分类中存在子分类，请先删除子分类');
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

.category-name {
  display: flex;
  align-items: center;
}

.name-text {
  font-weight: 500;
  color: #2c3e50;
}

.parent-text {
  color: #909399;
  font-size: 13px;
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
</style>