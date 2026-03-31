<template>
  <div class="knowledge-manage">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增知识
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
              placeholder="搜索标题/内容"
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
          <el-option label="饮食" value="diet"></el-option>
          <el-option label="健康" value="health"></el-option>
          <el-option label="训练" value="training"></el-option>
          <el-option label="行为" value="behavior"></el-option>
          <el-option label="其他" value="other"></el-option>
        </el-select>
        <el-select
            v-model="searchForm.source"
            placeholder="来源"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="人工录入" :value="1"></el-option>
          <el-option label="AI生成待审核" :value="2"></el-option>
        </el-select>
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

    <!-- 知识表格 -->
    <el-table
        v-loading="loading"
        :data="knowledgeList"
        stripe
        style="width: 100%"
        class="knowledge-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="title" label="标题" min-width="200">
        <template slot-scope="scope">
          <div class="knowledge-title">
            <span class="title-text">{{ scope.row.title }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="300">
        <template slot-scope="scope">
          <div class="knowledge-content">{{ scope.row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="keywords" label="关键词" width="150">
        <template slot-scope="scope">
          <div class="keywords">
            <el-tag v-for="kw in scope.row.keywords.split(',')" :key="kw" size="small">{{ kw }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" align="center">
        <template slot-scope="scope">
          <span class="category-tag">{{ scope.row.categoryName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="来源" width="120" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.source === 1 ? 'success' : 'warning'" size="small">
            {{ scope.row.source === 1 ? '人工录入' : 'AI生成待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="useCount" label="使用次数" width="100" align="center"></el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
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

    <!-- 新增/编辑知识对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="600px"
        :close-on-click-modal="false"
        class="knowledge-dialog"
        center
    >
      <div class="dialog-content">
        <el-form :model="currentKnowledge" :rules="formRules" ref="knowledgeForm" label-width="80px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="currentKnowledge.title" placeholder="请输入标题" size="medium"></el-input>
          </el-form-item>

          <el-form-item label="内容" prop="content">
            <el-input
                v-model="currentKnowledge.content"
                type="textarea"
                :rows="5"
                placeholder="请输入知识内容"
                size="medium"
            ></el-input>
          </el-form-item>

          <el-form-item label="关键词" prop="keywords">
            <el-input v-model="currentKnowledge.keywords" placeholder="多个关键词用英文逗号分隔" size="medium"></el-input>
            <div class="form-tip">例如：狗,食物,禁忌,中毒</div>
          </el-form-item>

          <el-form-item label="分类" prop="category">
            <el-select v-model="currentKnowledge.category" placeholder="请选择分类" size="medium">
              <el-option label="饮食" value="diet"></el-option>
              <el-option label="健康" value="health"></el-option>
              <el-option label="训练" value="training"></el-option>
              <el-option label="行为" value="behavior"></el-option>
              <el-option label="其他" value="other"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="来源" v-if="!isEdit">
            <el-radio-group v-model="currentKnowledge.source">
              <el-radio :label="1">人工录入</el-radio>
              <el-radio :label="2">AI生成待审核</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="状态">
            <el-radio-group v-model="currentKnowledge.status">
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
import { getKnowledgeList, addKnowledge, updateKnowledge, updateKnowledgeStatus, deleteKnowledge, batchDeleteKnowledge } from '@/api/ai';

export default {
  name: 'KnowledgeManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      knowledgeList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        category: '',
        source: '',
        status: ''
      },
      dialogVisible: false,
      isEdit: false,
      currentKnowledge: {
        id: null,
        title: '',
        content: '',
        keywords: '',
        category: '',
        source: 1,
        status: 1
      },
      formRules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在2-100个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' }
        ],
        keywords: [
          { required: true, message: '请输入关键词', trigger: 'blur' }
        ],
        category: [
          { required: true, message: '请选择分类', trigger: 'change' }
        ]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑知识' : '新增知识';
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
          keyword: this.searchForm.keyword || undefined,
          category: this.searchForm.category || undefined,
          source: this.searchForm.source || undefined,
          status: this.searchForm.status || undefined
        };
        const res = await getKnowledgeList(params);
        if (res.code === 200) {
          this.knowledgeList = res.data.list;
          this.total = res.data.total;
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
      this.searchForm = { keyword: '', category: '', source: '', status: '' };
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
    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      try {
        const res = await updateKnowledgeStatus(row.id, newStatus);
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
      this.$confirm(`确定要删除知识 "${row.title}" 吗？删除后无法恢复！`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteKnowledge(row.id);
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
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条知识吗？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteKnowledge(ids);
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
      this.currentKnowledge = {
        id: null,
        title: '',
        content: '',
        keywords: '',
        category: '',
        source: 1,
        status: 1
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.knowledgeForm) {
          this.$refs.knowledgeForm.clearValidate();
        }
      });
    },
    handleEdit(row) {
      this.isEdit = true;
      this.currentKnowledge = {
        id: row.id,
        title: row.title,
        content: row.content,
        keywords: row.keywords,
        category: row.category,
        source: row.source,
        status: row.status
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.knowledgeForm) {
          this.$refs.knowledgeForm.clearValidate();
        }
      });
    },
    submitForm() {
      this.$refs.knowledgeForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            res = await updateKnowledge(this.currentKnowledge);
          } else {
            res = await addKnowledge(this.currentKnowledge);
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
/* 样式保持不变... */
.knowledge-manage {
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

.knowledge-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.knowledge-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
}

.knowledge-title .title-text {
  font-weight: 500;
  color: #2c3e50;
}

.knowledge-content {
  color: #5a6874;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.keywords .el-tag {
  background: #f0f2f5;
  border: none;
  color: #667eea;
}

.category-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  background: #f8f9fc;
  color: #667eea;
  font-size: 12px;
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

.knowledge-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.knowledge-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.knowledge-dialog ::v-deep .el-dialog__title {
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