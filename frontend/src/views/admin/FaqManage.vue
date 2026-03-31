<template>
  <div class="faq-manage">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增问题
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
            class="batch-btn"
        >
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索问题"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          >
          </el-input>
        </div>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
      </div>
    </div>

    <!-- 问题表格 -->
    <el-table
        v-loading="loading"
        :data="faqList"
        stripe
        style="width: 100%"
        class="faq-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="question" label="问题" min-width="400">
        <template slot-scope="scope">
          <div class="question-text">{{ scope.row.question }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="answer" label="答案" min-width="500">
        <template slot-scope="scope">
          <div class="answer-text">{{ scope.row.answer || '无固定答案（由AI回答）' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="100" align="center"></el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
          ></el-switch>
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
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
      ></el-pagination>
    </div>

    <!-- 新增/编辑问题对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="550px"
        :close-on-click-modal="false"
        class="faq-dialog"
        center
    >
      <div class="dialog-content">
        <el-form :model="currentFaq" :rules="formRules" ref="faqForm" label-width="80px">
          <el-form-item label="问题" prop="question">
            <el-input v-model="currentFaq.question" placeholder="请输入问题" size="medium"></el-input>
          </el-form-item>

          <el-form-item label="答案">
            <el-input
                v-model="currentFaq.answer"
                type="textarea"
                :rows="4"
                placeholder="留空则使用AI回答"
                size="medium"
            ></el-input>
            <div class="form-tip">如果不填写答案，用户点击后会由AI实时回答</div>
          </el-form-item>

          <el-form-item label="排序">
            <el-input-number v-model="currentFaq.sortOrder" :min="0" :max="999" size="medium" controls-position="right"></el-input-number>
            <span class="form-tip">数字越小越靠前</span>
          </el-form-item>

          <el-form-item label="状态">
            <el-radio-group v-model="currentFaq.status">
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
import { getFaqListAdmin, addFaq, updateFaq, updateFaqStatus, deleteFaq, batchDeleteFaq } from '@/api/ai';

export default {
  name: 'FaqManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      faqList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: ''
      },
      dialogVisible: false,
      isEdit: false,
      currentFaq: {
        id: null,
        question: '',
        answer: '',
        sortOrder: 0,
        status: 1
      },
      formRules: {
        question: [
          { required: true, message: '请输入问题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在2-100个字符', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑问题' : '新增问题';
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
          keyword: this.searchForm.keyword || undefined
        };
        const res = await getFaqListAdmin(params);
        if (res.code === 200) {
          this.faqList = res.data.list;
          this.total = res.data.total;
        }
      } catch (error) {
        console.error('加载常见问题失败', error);
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
      this.searchForm = { keyword: '' };
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
    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      try {
        const res = await updateFaqStatus(row.id, newStatus);
        if (res.code === 200) {
          row.status = newStatus;
          this.$message.success(res.message);
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除问题 "${row.question}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteFaq(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个问题吗？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteFaq(ids);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量删除失败');
        }
      }).catch(() => {});
    },
    handleAdd() {
      this.isEdit = false;
      this.currentFaq = {
        id: null,
        question: '',
        answer: '',
        sortOrder: 0,
        status: 1
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.faqForm) {
          this.$refs.faqForm.clearValidate();
        }
      });
    },
    handleEdit(row) {
      this.isEdit = true;
      this.currentFaq = {
        id: row.id,
        question: row.question,
        answer: row.answer || '',
        sortOrder: row.sortOrder,
        status: row.status
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.faqForm) {
          this.$refs.faqForm.clearValidate();
        }
      });
    },
    submitForm() {
      this.$refs.faqForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            res = await updateFaq(this.currentFaq);
          } else {
            res = await addFaq(this.currentFaq);
          }

          if (res.code === 200) {
            this.$message.success(res.message);
            this.dialogVisible = false;
            this.loadList();
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
  }
};
</script>

<style scoped>
.faq-manage {
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

.faq-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.faq-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
}

.question-text {
  color: #2c3e50;
  font-weight: 500;
}

.answer-text {
  color: #5a6874;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

.faq-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.faq-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.faq-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-footer {
  text-align: right;
  padding: 16px 24px 20px;
  border-top: 1px solid #eef2f6;
}
</style>