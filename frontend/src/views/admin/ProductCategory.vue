<template>
  <div class="category-list">
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
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
        <el-button type="success" size="medium" @click="handleExport" :loading="exportLoading" plain class="export-btn">
          <i class="el-icon-download"></i> 导出
        </el-button>
      </div>
    </div>

    <!-- 只显示顶级分类的表格 -->
    <el-table
        v-loading="loading"
        :data="treeData"
        stripe
        class="category-table"
        @selection-change="handleSelectionChange"
        row-class-name="draggable-row"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>

      <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>

      <el-table-column prop="name" label="分类名称" min-width="250">
        <template slot-scope="scope">
          <div class="category-name-cell">
            <span class="name-text" :class="{ 'disabled-text': scope.row.status === 0 }">
              {{ scope.row.name }}
            </span>
            <el-tag size="mini" type="info" class="level-tag">顶级</el-tag>
            <el-tag
                v-if="scope.row.hasChildren"
                size="mini"
                type="warning"
                class="child-count-tag">
              子分类: {{ scope.row.childrenCount || 0 }}
            </el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="sortOrder" label="排序值" width="100" align="center">
        <template slot-scope="scope">
          <span class="sort-text">{{ scope.row.sortOrder }}</span>
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

      <el-table-column label="操作" width="260" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button size="small" type="primary" plain circle @click="handleEdit(scope.row)" title="编辑">
              <i class="el-icon-edit"></i>
            </el-button>
            <el-button
                size="small" type="warning" plain circle
                @click="openChildManageDialog(scope.row)"
                title="子分类管理">
              <i class="el-icon-s-operation"></i>
            </el-button>
            <el-button size="small" type="danger" plain circle @click="handleDelete(scope.row)" title="删除">
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
        @opened="loadParentOptions"
    >
      <div class="dialog-content">
        <el-form :model="currentCategory" :rules="formRules" ref="categoryForm" label-width="80px">
          <el-form-item label="分类名称" prop="name">
            <el-input v-model="currentCategory.name" placeholder="请输入分类名称" size="medium"></el-input>
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

    <!-- 子分类管理对话框 -->
    <el-dialog :title="`${currentParent?.name} - 子分类管理`" :visible.sync="childManageVisible" width="700px" center>
      <div class="child-manage-header">
        <el-button type="primary" size="small" @click="openChildAddDialog" :disabled="currentParent?.status === 0">
          <i class="el-icon-plus"></i> 新增子分类
        </el-button>
        <span class="child-manage-tip" v-if="currentParent?.status === 0">
          <i class="el-icon-warning"></i> 父分类已禁用，无法新增/启用子分类
        </span>
        <span class="child-manage-tip" v-else>拖拽左侧图标可调整排序</span>
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
                active-color="#67c23a"
                inactive-color="#f56c6c"
                @change="(val) => handleChildStatusChange(child, val)"
                size="small"
                :disabled="currentParent?.status === 0"
            ></el-switch>
            <el-button size="small" type="primary" plain circle @click="editChild(child)" title="编辑" :disabled="currentParent?.status === 0">
              <i class="el-icon-edit"></i>
            </el-button>
            <el-button size="small" type="danger" plain circle @click="deleteChild(child)" title="删除">
              <i class="el-icon-delete"></i>
            </el-button>
          </div>
        </div>
        <div v-if="childList.length === 0" class="empty-child">
          <i class="el-icon-info"></i> 暂无子分类，点击上方按钮添加
        </div>
      </div>

      <div class="child-sort-tip">
        <i class="el-icon-info"></i> 提示：拖拽左侧图标调整顺序，点击"保存排序"后生效
      </div>

      <span slot="footer">
        <el-button @click="childManageVisible = false">关闭</el-button>
        <el-button type="primary" @click="saveChildSortOrder" :loading="sortSaving" :disabled="childList.length === 0">保存排序</el-button>
      </span>
    </el-dialog>

    <!-- 新增/编辑子分类对话框 -->
    <el-dialog
        :title="childDialogTitle"
        :visible.sync="childDialogVisible"
        width="450px"
        :close-on-click-modal="false"
        class="category-dialog"
        center
    >
      <div class="dialog-content">
        <el-form :model="childForm" :rules="childFormRules" ref="childForm" label-width="80px">
          <el-form-item label="子分类名称" prop="name">
            <el-input v-model="childForm.name" placeholder="请输入子分类名称" size="medium"></el-input>
          </el-form-item>

          <el-form-item label="排序">
            <el-input-number v-model="childForm.sortOrder" :min="0" :max="999" size="medium" controls-position="right"></el-input-number>
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
        <el-button @click="childDialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitChildForm" :loading="submitLoading" size="medium">
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
  batchUpdateCategorySort,
  exportCategoryList
} from '@/api/category';
import Sortable from 'sortablejs';

export default {
  name: 'ProductCategory',
  data() {
    return {
      loading: false,
      submitLoading: false,
      sortSaving: false,
      exportLoading: false,
      sortChanged: false,
      treeData: [],
      allCategories: [],
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
      },
      // 子分类管理相关
      childManageVisible: false,
      currentParent: null,
      childList: [],
      dragStartIndex: null,
      // 子分类新增/编辑
      childDialogVisible: false,
      isChildEdit: false,
      editChildId: null,
      childForm: {
        name: '',
        sortOrder: 0,
        status: 1
      },
      childFormRules: {
        name: [
          { required: true, message: '请输入子分类名称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑分类' : '新增分类';
    },
    childDialogTitle() {
      return this.isChildEdit ? '编辑子分类' : '新增子分类';
    },
    parentOptions() {
      return this.allCategories.filter(item => item.parentId === 0 && item.status === 1);
    }
  },
  created() {
    this.loadCategories();
  },
  mounted() {
    this.$nextTick(() => {
      setTimeout(() => {
        this.initSortable();
      }, 300);
    });
  },
  beforeDestroy() {
    if (this.sortableInstance) {
      this.sortableInstance.destroy();
    }
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
          // 获取每个顶级分类的子分类数量
          for (const item of list) {
            const childrenRes = await getCategoryList({ page: 1, pageSize: 1, parentId: item.id });
            item.hasChildren = childrenRes.data.total > 0;
            item.childrenCount = childrenRes.data.total;
          }
          this.treeData = list.map(item => ({
            ...item,
            statusLoading: false
          }));
          this.total = res.data.total;
          this.sortChanged = false;
        }
        await this.loadAllCategories();
      } catch (error) {
        console.error('加载分类列表失败', error);
        this.$message.error('加载分类列表失败');
      } finally {
        this.loading = false;
      }
    },

    async loadAllCategories() {
      try {
        const res = await getAllCategories();
        if (res.code === 200) {
          this.allCategories = res.data;
        }
      } catch (error) {
        console.error('加载所有分类失败', error);
      }
    },

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

          // 获取拖拽的元素
          const movedItem = this.treeData[oldIndex];

          // 从原位置删除
          this.treeData.splice(oldIndex, 1);
          // 插入到新位置
          this.treeData.splice(newIndex, 0, movedItem);

          // 重新计算所有项的 sortOrder
          this.treeData.forEach((item, idx) => {
            item.sortOrder = idx;
          });

          // ✅ 关键：强制刷新表格显示
          this.$forceUpdate();

          // ✅ 标记排序已修改
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
        const allItems = this.treeData.map((item, index) => ({
          id: item.id,
          sortOrder: index
        }));

        const res = await batchUpdateCategorySort(allItems);
        if (res.code === 200) {
          this.$message.success(`排序保存成功，共更新 ${allItems.length} 条记录`);
          this.sortChanged = false;
          await this.loadCategories();
        } else {
          this.$message.error(res.message || '保存失败');
        }
      } catch (error) {
        console.error('保存排序失败', error);
        this.$message.error('保存排序失败');
      } finally {
        this.sortSaving = false;
      }
    },

    // ========== 子分类管理相关方法 ==========
    async openChildManageDialog(parent) {
      this.currentParent = parent;
      await this.loadChildList(parent.id);
      this.childManageVisible = true;
    },

    async loadChildList(parentId) {
      const res = await getCategoryList({ page: 1, pageSize: 999, parentId: parentId });
      if (res.code === 200) {
        this.childList = res.data.list || [];
        this.childList.sort((a, b) => a.sortOrder - b.sortOrder);
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

      this.childList.forEach((item, idx) => {
        item.sortOrder = idx;
      });

      this.dragStartIndex = null;
      this.$forceUpdate();
    },

    async saveChildSortOrder() {
      if (!this.currentParent) return;
      if (this.childList.length === 0) return;

      this.sortSaving = true;
      try {
        const allItems = this.childList.map((item, index) => ({
          id: item.id,
          sortOrder: index
        }));

        const res = await batchUpdateCategorySort(allItems);
        if (res.code === 200) {
          this.$message.success(`子分类排序保存成功，共更新 ${allItems.length} 条记录`);
          // 刷新子分类列表
          await this.loadChildList(this.currentParent.id);
          // 刷新顶级分类列表以更新子分类数量显示
          await this.loadCategories();
        } else {
          this.$message.error(res.message || '保存失败');
        }
      } catch (error) {
        console.error('保存子分类排序失败', error);
        this.$message.error('保存排序失败');
      } finally {
        this.sortSaving = false;
      }
    },

    // 子分类状态切换（检查父分类状态）
    async handleChildStatusChange(child, val) {
      // 如果父分类已被禁用，不允许启用子分类
      if (this.currentParent && this.currentParent.status === 0 && val === true) {
        this.$message.warning('父分类已被禁用，请先启用父分类');
        return;
      }

      const newStatus = val ? 1 : 0;
      try {
        const res = await updateCategoryStatus(child.id, newStatus);
        if (res.code === 200) {
          child.status = newStatus;
          this.$message.success(res.message);
          // 刷新父分类的统计
          await this.loadCategories();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    // 新增子分类
    openChildAddDialog() {
      if (this.currentParent && this.currentParent.status === 0) {
        this.$message.warning('父分类已被禁用，无法新增子分类');
        return;
      }
      this.isChildEdit = false;
      this.editChildId = null;
      this.childForm = {
        name: '',
        sortOrder: this.childList.length,
        status: 1
      };
      this.childDialogVisible = true;
    },

    // 编辑子分类
    editChild(child) {
      if (this.currentParent && this.currentParent.status === 0) {
        this.$message.warning('父分类已被禁用，无法编辑子分类');
        return;
      }
      this.isChildEdit = true;
      this.editChildId = child.id;
      this.childForm = {
        name: child.name,
        sortOrder: child.sortOrder,
        status: child.status
      };
      this.childDialogVisible = true;
    },

    // 删除子分类
    async deleteChild(child) {
      this.$confirm(`确定要删除子分类 "${child.name}" 吗？`, '警告')
          .then(async () => {
            try {
              const res = await deleteCategory(child.id);
              if (res.code === 200) {
                this.$message.success('删除成功');
                await this.loadChildList(this.currentParent.id);
                await this.loadCategories();
              }
            } catch (error) {
              this.$message.error('删除失败');
            }
          }).catch(() => {});
    },

    // 提交子分类表单
    submitChildForm() {
      this.$refs.childForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          let res;
          const submitData = {
            name: this.childForm.name,
            parentId: this.currentParent.id,
            sortOrder: this.childForm.sortOrder,
            status: this.childForm.status
          };

          if (this.isChildEdit) {
            res = await updateCategory({ ...submitData, id: this.editChildId });
          } else {
            res = await addCategory(submitData);
          }

          if (res.code === 200) {
            this.$message.success(this.isChildEdit ? '修改成功' : '添加成功');
            this.childDialogVisible = false;
            await this.loadChildList(this.currentParent.id);
            await this.loadCategories();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('提交失败:', error);
          this.$message.error(this.isChildEdit ? '修改失败' : '添加失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    // ========== 顶级分类操作方法 ==========
    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;

      if (newStatus === 0) {
        this.$confirm(`禁用顶级分类 "${row.name}" 会同时禁用其下的所有子分类，确定吗？`, '提示', {
          type: 'warning'
        }).then(async () => {
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
          this.$message.success(res.message);
          await this.loadCategories();
          await this.loadAllCategories();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    async updateCategoryAndChildren(parentId, status) {
      try {
        const childrenRes = await getCategoryList({ page: 1, pageSize: 999, parentId: parentId });
        const children = childrenRes.data.list || [];

        for (const child of children) {
          await updateCategoryStatus(child.id, status);
        }
        await updateCategoryStatus(parentId, status);

        this.$message.success(`已${status === 1 ? '启用' : '禁用'}分类及${children.length}个子分类`);
        await this.loadCategories();
        await this.loadAllCategories();
      } catch (error) {
        console.error('操作失败', error);
        this.$message.error('操作失败');
      }
    },

    async handleDelete(row) {
      const res = await getCategoryList({ page: 1, pageSize: 1, parentId: row.id });
      if (res.code === 200 && res.data.total > 0) {
        this.$message.warning('请先删除该分类下的子分类');
        return;
      }

      this.$confirm(`确定要删除分类 "${row.name}" 吗？`, '警告')
          .then(async () => {
            try {
              const res = await deleteCategory(row.id);
              if (res.code === 200) {
                this.$message.success('删除成功');
                this.loadCategories();
                this.loadAllCategories();
              }
            } catch (error) {
              this.$message.error('删除失败');
            }
          }).catch(() => {});
    },

    async handleExport() {
      this.exportLoading = true;
      try {
        const params = {
          keyword: this.searchForm.keyword || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined
        };
        const res = await exportCategoryList(params);

        const url = window.URL.createObjectURL(res);
        const link = document.createElement('a');
        link.href = url;
        link.download = `商品分类_${new Date().getTime()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

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
      this.loadCategories();
    },

    handleReset() {
      this.searchForm = { keyword: '', status: '' };
      this.page = 1;
      this.loadCategories();
    },

    handlePageChange(page) {
      this.page = page;
      this.loadCategories();
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadCategories();
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
        if (this.$refs.categoryForm) this.$refs.categoryForm.clearValidate();
      });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.currentCategory = {
        id: row.id,
        name: row.name,
        parentId: 0,
        sortOrder: row.sortOrder,
        status: row.status
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.categoryForm) this.$refs.categoryForm.clearValidate();
      });
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
            this.$message.success(res.message);
            this.dialogVisible = false;
            this.loadCategories();
            this.loadAllCategories();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('提交失败:', error);
          this.$message.error(this.isEdit ? '更新失败' : '添加失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;

      let hasChild = false;
      for (const row of this.selectedRows) {
        const res = await getCategoryList({ page: 1, pageSize: 1, parentId: row.id });
        if (res.code === 200 && res.data.total > 0) {
          hasChild = true;
          break;
        }
      }
      if (hasChild) {
        this.$message.warning('请先删除选中分类下的子分类');
        return;
      }

      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个分类吗？`, '警告')
          .then(async () => {
            try {
              const res = await batchDeleteCategories(ids);
              if (res.code === 200) {
                this.$message.success(res.message);
                this.selectedRows = [];
                this.loadCategories();
                this.loadAllCategories();
              }
            } catch (error) {
              this.$message.error('批量删除失败');
            }
          }).catch(() => {});
    },

    loadParentOptions() {
      this.loadAllCategories();
    }
  }
};
</script>

<style scoped>
.category-list {
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

.export-btn {
  border-color: #67c23a;
  color: #67c23a;
}

.export-btn:hover {
  background: #f0f9f4;
  border-color: #67c23a;
  color: #67c23a;
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

.category-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.name-text {
  font-weight: 500;
  color: #2c3e50;
}

.disabled-text {
  color: #c0c4cc;
  text-decoration: line-through;
}

.level-tag, .child-count-tag {
  margin-left: 4px;
}

.sort-text {
  color: #606266;
}

.draggable-row {
  cursor: move;
}

.draggable-row:hover {
  background: #fafbfe;
}

.sortable-drag {
  opacity: 0.5;
  background: #f0f2f5 !important;
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

/* 子分类管理对话框样式 */
.child-manage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f6;
}

.child-manage-tip {
  font-size: 12px;
  color: #909399;
}

.child-sort-list {
  max-height: 400px;
  overflow-y: auto;
}

.child-sort-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 8px 0;
  background: #f8f9fc;
  border-radius: 8px;
  transition: all 0.3s;
}

.child-sort-item:hover {
  background: #e8f4fd;
  transform: translateX(4px);
}

.drag-icon {
  font-size: 18px;
  color: #909399;
  cursor: grab;
}

.drag-icon:active {
  cursor: grabbing;
}

.child-name {
  flex: 1;
  font-weight: 500;
  color: #2c3e50;
}

.child-sort-order {
  font-size: 12px;
  color: #999;
}

.child-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.empty-child {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-child i {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
}

.child-sort-tip {
  margin-top: 16px;
  padding: 8px 12px;
  background: #ecf5ff;
  border-radius: 8px;
  color: #409EFF;
  font-size: 12px;
}

.child-sort-tip i {
  margin-right: 4px;
}

@media (max-width: 768px) {
  .category-list {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 160px;
  }

  .pagination-wrapper {
    justify-content: center;
  }

  .category-name-cell {
    flex-wrap: wrap;
  }

  .child-sort-item {
    flex-wrap: wrap;
  }

  .child-actions {
    margin-left: auto;
  }
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
</style>