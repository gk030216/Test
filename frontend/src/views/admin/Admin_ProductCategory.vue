<template>
  <div class="category-page">
    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索分类名称"
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.status"
          placeholder="状态"
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

    <!-- 批量操作 + 排序浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0 || sortChanged">
        <template v-if="selectedRows.length > 0">
          <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
          <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
            <i class="el-icon-delete" /> 批量删除
          </el-button>
        </template>
        <template v-if="sortChanged">
          <el-button size="small" type="success" @click="saveSortOrder" :loading="sortSaving" class="batch-btn-save">
            <i class="el-icon-check" /> 保存排序
          </el-button>
        </template>
      </div>
    </transition>

    <!-- 表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="treeData"
        stripe
        row-key="id"
        class="category-table"
        @selection-change="handleSelectionChange"
        row-class-name="draggable-row"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column prop="name" label="分类名称" min-width="250">
          <template slot-scope="scope">
            <div class="category-name-cell">
              <span class="name-text" :class="{ 'disabled-text': scope.row.status === 0 }">
                {{ scope.row.name }}
              </span>
              <el-tag size="mini" type="info" class="level-tag">顶级</el-tag>
              <el-tag v-if="scope.row.hasChildren" size="mini" type="warning" class="child-count-tag">
                子分类: {{ scope.row.childrenCount || 0 }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="sortOrder" label="排序值" width="100" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.sortOrder }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
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
            <span class="cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleEdit(scope.row)" class="act-edit">
                <i class="el-icon-edit" /> 编辑
              </el-button>
              <el-button type="text" size="small" @click="openChildManageDialog(scope.row)" class="act-child">
                <i class="el-icon-s-operation" /> 子分类
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

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
      center
      class="category-dialog"
      @opened="loadAllCategories"
    >
      <div class="dialog-body">
        <el-form :model="currentCategory" :rules="formRules" ref="categoryForm" label-width="80px">
          <el-form-item label="分类名称" prop="name">
            <el-input v-model="currentCategory.name" placeholder="请输入分类名称" />
          </el-form-item>
          <el-form-item label="排序">
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

    <!-- 子分类管理对话框 -->
    <el-dialog
      :title="`${currentParent ? currentParent.name : ''} - 子分类管理`"
      :visible.sync="childManageVisible"
      width="700px"
      center
      class="child-dialog"
    >
      <div class="child-manage-header">
        <el-button type="primary" size="small" @click="openChildAddDialog" :disabled="currentParent && currentParent.status === 0" class="warm-btn">
          <i class="el-icon-plus" /> 新增子分类
        </el-button>
        <span class="child-tip" v-if="currentParent && currentParent.status === 0">
          <i class="el-icon-warning" /> 父分类已禁用，无法新增/启用子分类
        </span>
        <span class="child-tip" v-else>拖拽左侧图标可调整排序</span>
      </div>

      <div class="child-sort-list">
        <div
          v-for="(child, index) in childList"
          :key="child.id"
          class="child-sort-item"
          draggable="true"
          @dragstart="handleDragStart($event, index)"
          @dragover="handleDragOver($event)"
          @drop="handleDrop($event, index)"
        >
          <i class="el-icon-rank drag-icon"></i>
          <span class="child-name" :class="{ 'disabled-text': child.status === 0 }">{{ child.name }}</span>
          <span class="child-sort-order">排序: {{ child.sortOrder }}</span>
          <div class="child-actions">
            <el-switch
              :value="child.status === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleChildStatusChange(child, val)"
              size="small"
              :disabled="currentParent && currentParent.status === 0"
            />
            <el-button size="small" @click="editChild(child)" :disabled="currentParent && currentParent.status === 0" class="act-edit">编辑</el-button>
            <el-button size="small" @click="deleteChild(child)" class="act-del">删除</el-button>
          </div>
        </div>
        <div v-if="childList.length === 0" class="empty-child">
          <i class="el-icon-info" />
          <p>暂无子分类，点击上方按钮添加</p>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="childManageVisible = false">关 闭</el-button>
        <el-button type="primary" @click="saveChildSortOrder" :loading="sortSaving" :disabled="childList.length === 0" class="warm-btn">
          保存排序
        </el-button>
      </span>
    </el-dialog>

    <!-- 新增/编辑子分类对话框 -->
    <el-dialog
      :title="childDialogTitle"
      :visible.sync="childDialogVisible"
      width="450px"
      :close-on-click-modal="false"
      center
      class="category-dialog"
    >
      <div class="dialog-body">
        <el-form :model="childForm" :rules="childFormRules" ref="childForm" label-width="90px">
          <el-form-item label="子分类名称" prop="name">
            <el-input v-model="childForm.name" placeholder="请输入子分类名称" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="childForm.sortOrder" :min="0" :max="999" controls-position="right" />
            <span class="form-tip">数字越小越靠前</span>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="childForm.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="childDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitChildForm" :loading="submitLoading" class="warm-btn">
          {{ isChildEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getCategoryList,
  getAllCategories,
  addCategory,
  updateCategory,
  updateCategoryStatus,
  deleteCategory,
  batchDeleteCategories,
  batchUpdateCategorySort
} from '@/api/category';
import Sortable from 'sortablejs';

export default {
  name: 'Admin_ProductCategory',
  data() {
    return {
      loading: false,
      submitLoading: false,
      sortSaving: false,
      sortChanged: false,
      treeData: [],
      allCategories: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', status: '' },
      dialogVisible: false,
      isEdit: false,
      currentCategory: { id: null, name: '', parentId: 0, sortOrder: 0, status: 1 },
      formRules: {
        name: [
          { required: true, message: '请输入分类名称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ]
      },
      childManageVisible: false,
      currentParent: null,
      childList: [],
      dragStartIndex: null,
      childDialogVisible: false,
      isChildEdit: false,
      editChildId: null,
      childForm: { name: '', sortOrder: 0, status: 1 },
      childFormRules: {
        name: [
          { required: true, message: '请输入子分类名称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ]
      },
      sortableInstance: null
    };
  },
  computed: {
    dialogTitle() { return this.isEdit ? '编辑分类' : '新增分类'; },
    childDialogTitle() { return this.isChildEdit ? '编辑子分类' : '新增子分类'; }
  },
  created() { this.loadCategories(); },
  mounted() {
    this.$nextTick(() => {
      setTimeout(() => this.initSortable(), 300);
    });
  },
  beforeDestroy() {
    if (this.sortableInstance) this.sortableInstance.destroy();
  },
  methods: {
    async loadCategories() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined,
          parentId: 0
        };
        const res = await getCategoryList(params);
        if (res.code === 200) {
          const list = res.data.list || [];
          for (const item of list) {
            const childrenRes = await getCategoryList({ page: 1, pageSize: 1, parentId: item.id });
            item.hasChildren = childrenRes.data.total > 0;
            item.childrenCount = childrenRes.data.total;
          }
          this.treeData = list.map(item => ({ ...item, statusLoading: false }));
          this.total = res.data.total;
          this.sortChanged = false;
        }
        await this.loadAllCategories();
      } catch (error) {
        this.$message.error(error?.message || '加载分类列表失败');
      } finally {
        this.loading = false;
      }
    },

    async loadAllCategories() {
      try {
        const res = await getAllCategories();
        if (res.code === 200) this.allCategories = res.data;
      } catch { /* silent */ }
    },

    initSortable() {
      const el = this.$el.querySelector('.el-table__body-wrapper tbody');
      if (!el) return;
      if (this.sortableInstance) this.sortableInstance.destroy();

      this.sortableInstance = new Sortable(el, {
        animation: 300,
        onEnd: (evt) => {
          const oldIndex = evt.oldIndex;
          const newIndex = evt.newIndex;
          if (oldIndex === newIndex) return;
          const movedItem = this.treeData[oldIndex];
          this.treeData.splice(oldIndex, 1);
          this.treeData.splice(newIndex, 0, movedItem);
          this.treeData.forEach((item, idx) => { item.sortOrder = idx; });
          this.$forceUpdate();
          this.sortChanged = true;
        }
      });
    },

    async saveSortOrder() {
      if (!this.sortChanged) {
        this.$message.info('排序未发生变化');
        return;
      }
      this.sortSaving = true;
      try {
        const allItems = this.treeData.map((item, index) => ({ id: item.id, sortOrder: index }));
        const res = await batchUpdateCategorySort(allItems);
        if (res.code === 200) {
          this.$message.success(`排序保存成功，共更新 ${allItems.length} 条记录`);
          this.sortChanged = false;
          await this.loadCategories();
        } else {
          this.$message.error(res.message || '保存失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '保存排序失败');
      } finally {
        this.sortSaving = false;
      }
    },

    // ===== 子分类管理 =====
    async openChildManageDialog(parent) {
      this.currentParent = parent;
      await this.loadChildList(parent.id);
      this.childManageVisible = true;
    },

    async loadChildList(parentId) {
      const res = await getCategoryList({ page: 1, pageSize: 999, parentId });
      if (res.code === 200) {
        this.childList = (res.data.list || []).sort((a, b) => a.sortOrder - b.sortOrder);
      }
    },

    handleDragStart(event, index) {
      this.dragStartIndex = index;
      event.dataTransfer.effectAllowed = 'move';
    },
    handleDragOver(event) {
      event.preventDefault();
      event.dataTransfer.dropEffect = 'move';
    },
    handleDrop(event, targetIndex) {
      event.preventDefault();
      if (this.dragStartIndex === null) return;
      const movedItem = this.childList.splice(this.dragStartIndex, 1)[0];
      this.childList.splice(targetIndex, 0, movedItem);
      this.childList.forEach((item, idx) => { item.sortOrder = idx; });
      this.dragStartIndex = null;
      this.$forceUpdate();
    },

    async saveChildSortOrder() {
      if (!this.currentParent || this.childList.length === 0) return;
      this.sortSaving = true;
      try {
        const allItems = this.childList.map((item, index) => ({ id: item.id, sortOrder: index }));
        const res = await batchUpdateCategorySort(allItems);
        if (res.code === 200) {
          this.$message.success(`子分类排序保存成功，共更新 ${allItems.length} 条记录`);
          await this.loadChildList(this.currentParent.id);
          await this.loadCategories();
        } else {
          this.$message.error(res.message || '保存失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '保存排序失败');
      } finally {
        this.sortSaving = false;
      }
    },

    async handleChildStatusChange(child, val) {
      if (this.currentParent && this.currentParent.status === 0 && val === true) {
        this.$message.warning('父分类已被禁用，请先启用父分类');
        return;
      }
      const newStatus = val ? 1 : 0;
      try {
        const res = await updateCategoryStatus(child.id, newStatus);
        if (res.code === 200) {
          child.status = newStatus;
          this.$message.success(res.message || '操作成功');
          await this.loadCategories();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '操作失败');
      }
    },

    openChildAddDialog() {
      if (this.currentParent && this.currentParent.status === 0) {
        this.$message.warning('父分类已被禁用，无法新增子分类');
        return;
      }
      this.isChildEdit = false;
      this.editChildId = null;
      this.childForm = { name: '', sortOrder: this.childList.length, status: 1 };
      this.childDialogVisible = true;
    },

    editChild(child) {
      if (this.currentParent && this.currentParent.status === 0) {
        this.$message.warning('父分类已被禁用');
        return;
      }
      this.isChildEdit = true;
      this.editChildId = child.id;
      this.childForm = { name: child.name, sortOrder: child.sortOrder, status: child.status };
      this.childDialogVisible = true;
    },

    deleteChild(child) {
      this.$confirm(`确定要删除子分类 "${child.name}" 吗？`, '警告', { type: 'warning' })
        .then(async () => {
          try {
            const res = await deleteCategory(child.id);
            if (res.code === 200) {
              this.$message.success('删除成功');
              await this.loadChildList(this.currentParent.id);
              await this.loadCategories();
            } else {
              this.$message.error(res.message || '删除失败');
            }
          } catch (error) {
            this.$message.error(error?.message || '删除失败');
          }
        }).catch(() => {});
    },

    submitChildForm() {
      this.$refs.childForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          const data = {
            name: this.childForm.name,
            parentId: this.currentParent.id,
            sortOrder: this.childForm.sortOrder,
            status: this.childForm.status
          };
          if (this.isChildEdit) {
            res = await updateCategory({ ...data, id: this.editChildId });
          } else {
            res = await addCategory(data);
          }
          if (res.code === 200) {
            this.$message.success(this.isChildEdit ? '修改成功' : '添加成功');
            this.childDialogVisible = false;
            await this.loadChildList(this.currentParent.id);
            await this.loadCategories();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || (this.isChildEdit ? '修改失败' : '添加失败'));
        } finally {
          this.submitLoading = false;
        }
      });
    },

    // ===== 顶级分类操作 =====
    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      if (newStatus === 0) {
        this.$confirm(`禁用顶级分类 "${row.name}" 会同时禁用其下所有子分类，确定吗？`, '提示', { type: 'warning' })
          .then(async () => {
            await this.updateCategoryAndChildren(row.id, newStatus);
          }).catch(() => {});
        return;
      }
      await this.updateSingleCategory(row.id, newStatus);
    },

    async updateSingleCategory(id, status) {
      try {
        const res = await updateCategoryStatus(id, status);
        if (res.code === 200) {
          this.$message.success(res.message || '操作成功');
          await this.loadCategories();
          await this.loadAllCategories();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '操作失败');
      }
    },

    async updateCategoryAndChildren(parentId, status) {
      try {
        const childrenRes = await getCategoryList({ page: 1, pageSize: 999, parentId });
        const children = childrenRes.data.list || [];
        for (const child of children) {
          await updateCategoryStatus(child.id, status);
        }
        await updateCategoryStatus(parentId, status);
        this.$message.success(`已${status === 1 ? '启用' : '禁用'}分类及${children.length}个子分类`);
        await this.loadCategories();
        await this.loadAllCategories();
      } catch (error) {
        this.$message.error(error?.message || '操作失败');
      }
    },

    async handleDelete(row) {
      const res = await getCategoryList({ page: 1, pageSize: 1, parentId: row.id });
      if (res.code === 200 && res.data.total > 0) {
        this.$message.warning('请先删除该分类下的子分类');
        return;
      }
      this.$confirm(`确定要删除分类 "${row.name}" 吗？`, '警告', { type: 'warning' })
        .then(async () => {
          try {
            const res = await deleteCategory(row.id);
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadCategories();
              this.loadAllCategories();
            } else {
              this.$message.error(res.message || '删除失败');
            }
          } catch (error) {
            this.$message.error(error?.message || '删除失败');
          }
        }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      let hasChild = false;
      for (const row of this.selectedRows) {
        const res = await getCategoryList({ page: 1, pageSize: 1, parentId: row.id });
        if (res.code === 200 && res.data.total > 0) { hasChild = true; break; }
      }
      if (hasChild) {
        this.$message.warning('请先删除选中分类下的子分类');
        return;
      }
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个分类吗？`, '警告', { type: 'warning' })
        .then(async () => {
          try {
            const res = await batchDeleteCategories(ids);
            if (res.code === 200) {
              this.$message.success(res.message || '批量删除成功');
              this.selectedRows = [];
              this.loadCategories();
              this.loadAllCategories();
            } else {
              this.$message.error(res.message || '批量删除失败');
            }
          } catch (error) {
            this.$message.error(error?.message || '批量删除失败');
          }
        }).catch(() => {});
    },

    handleSearch() { this.page = 1; this.loadCategories(); },
    handleReset() {
      this.searchForm = { keyword: '', status: '' };
      this.page = 1;
      this.loadCategories();
    },
    handlePageChange(page) { this.page = page; this.loadCategories(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadCategories(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    handleAdd() {
      this.isEdit = false;
      this.currentCategory = { id: null, name: '', parentId: 0, sortOrder: 0, status: 1 };
      this.dialogVisible = true;
      this.$nextTick(() => { if (this.$refs.categoryForm) this.$refs.categoryForm.clearValidate(); });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.currentCategory = { id: row.id, name: row.name, parentId: 0, sortOrder: row.sortOrder, status: row.status };
      this.dialogVisible = true;
      this.$nextTick(() => { if (this.$refs.categoryForm) this.$refs.categoryForm.clearValidate(); });
    },

    submitForm() {
      this.$refs.categoryForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            res = await updateCategory(this.currentCategory);
          } else {
            res = await addCategory(this.currentCategory);
          }
          if (res.code === 200) {
            this.$message.success(res.message || '操作成功');
            this.dialogVisible = false;
            this.loadCategories();
            this.loadAllCategories();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || (this.isEdit ? '更新失败' : '添加失败'));
        } finally {
          this.submitLoading = false;
        }
      });
    },

  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.category-page {
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
.batch-info { font-size: 13px; }
.batch-info strong { color: #f59e4b; font-size: 15px; }
.batch-bar .el-button {
  border-radius: 8px; padding: 7px 16px; font-weight: 500;
}
.batch-btn-delete {
  background: rgba(245, 108, 108, 0.12) !important;
  border: 1px solid rgba(245, 108, 108, 0.35) !important;
  color: #f56c6c !important;
}
.batch-btn-delete:hover {
  background: rgba(245, 108, 108, 0.22) !important;
  border-color: #f56c6c !important;
}
.batch-btn-save {
  background: rgba(103, 194, 58, 0.15) !important;
  border: 1px solid rgba(103, 194, 58, 0.35) !important;
  color: #67c23a !important;
}
.batch-btn-save:hover {
  background: rgba(103, 194, 58, 0.25) !important;
  border-color: #67c23a !important;
}
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
.category-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.category-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.category-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.category-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.category-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b;
  border-color: #f59e4b;
}

.category-name-cell { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.name-text { font-weight: 500; color: #3d2e2a; }
.disabled-text { color: #c0c4cc; text-decoration: line-through; }
.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { color: #a08c84; font-size: 12px; }

.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
.act-child { color: #409eff !important; }
.act-child:hover { color: #337ecc !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }

.draggable-row { cursor: move; }

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

.child-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.child-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.child-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px;
}
.child-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.child-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.child-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 50vh; overflow-y: auto; }

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

/* 表单 */
.category-dialog ::v-deep .el-input__inner { border-radius: 10px; }
.category-dialog ::v-deep .el-radio__input.is-checked .el-radio__inner {
  background: #f59e4b; border-color: #f59e4b;
}

/* ===== 子分类管理 ===== */
.child-manage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f5ece6;
}
.child-tip { font-size: 12px; color: #a08c84; }
.child-tip i { margin-right: 4px; }

.child-sort-list { max-height: 400px; overflow-y: auto; }

.child-sort-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 6px 0;
  background: #fdf8f4;
  border-radius: 10px;
  transition: all 0.2s;
}
.child-sort-item:hover {
  background: #fef5ee;
  transform: translateX(4px);
}

.drag-icon { font-size: 18px; color: #a08c84; cursor: grab; }
.drag-icon:active { cursor: grabbing; }
.child-name { flex: 1; font-weight: 500; color: #3d2e2a; }
.child-sort-order { font-size: 12px; color: #a08c84; }
.child-actions { display: flex; gap: 8px; align-items: center; }
.child-actions .act-edit { font-size: 12px; }
.child-actions .act-del { font-size: 12px; }

.empty-child {
  text-align: center;
  padding: 40px;
  color: #a08c84;
}
.empty-child i { font-size: 48px; margin-bottom: 16px; display: block; }

@media (max-width: 768px) {
  .category-page { padding: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 100%; }
  .child-sort-item { flex-wrap: wrap; }
}
</style>
