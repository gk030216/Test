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
            type="success"
            plain
            @click="handleBatchEnable"
            class="batch-btn"
        >
          <i class="el-icon-check"></i> 批量启用 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDisable"
            class="batch-btn"
        >
          <i class="el-icon-close"></i> 批量禁用 ({{ selectedRows.length }})
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
              placeholder="搜索问题/答案"
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
          <el-option label="服务咨询" value="service"></el-option>
          <el-option label="宠物知识" value="pet_knowledge"></el-option>
          <el-option label="宠物用品" value="pet_product"></el-option>
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
      <el-table-column prop="question" label="问题" min-width="300">
        <template slot-scope="scope">
          <div class="question-text">{{ scope.row.question }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getCategoryType(scope.row.category)" size="small">
            {{ getCategoryName(scope.row.category) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="answer" label="答案" min-width="350">
        <template slot-scope="scope">
          <div class="answer-text">{{ scope.row.answer || '无固定答案（由AI回答）' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" align="center"></el-table-column>
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
      <el-table-column label="操作" width="220" fixed="right" align="center">
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
        width="650px"
        :close-on-click-modal="false"
        class="faq-dialog"
        center
    >
      <div class="dialog-content">
        <el-form :model="currentFaq" :rules="formRules" ref="faqForm" label-width="80px">
          <el-form-item label="问题" prop="question">
            <el-input v-model="currentFaq.question" placeholder="请输入问题" size="medium"></el-input>
          </el-form-item>

          <el-form-item label="分类" prop="category">
            <el-select v-model="currentFaq.category" placeholder="请选择分类" size="medium" style="width: 100%">
              <el-option label="服务咨询" value="service"></el-option>
              <el-option label="宠物知识" value="pet_knowledge"></el-option>
              <el-option label="宠物用品" value="pet_product"></el-option>
              <el-option label="其他" value="other"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="答案" prop="answer">
            <div style="display: flex; gap: 10px; align-items: flex-start;">
              <el-input
                  v-model="currentFaq.answer"
                  type="textarea"
                  :rows="4"
                  placeholder="留空则使用AI回答"
                  size="medium"
                  style="flex: 1;"
              ></el-input>
              <el-button
                  type="primary"
                  size="medium"
                  @click="generateAnswerByAI"
                  :loading="aiGenerating"
                  :disabled="!currentFaq.question"
                  style="margin-top: 0;"
              >
                <i class="el-icon-magic-stick"></i> AI生成
              </el-button>
            </div>
            <div class="form-tip">如果不填写答案，用户点击后会由AI实时回答 | 点击AI生成按钮可根据问题自动生成答案</div>
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

    <!-- 查看详情对话框 -->
    <el-dialog
        title="常见问题详情"
        :visible.sync="detailVisible"
        width="600px"
        center
        class="faq-detail-dialog"
    >
      <div class="detail-content" v-if="currentDetailFaq">
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-question"></i>
            <span>问题信息</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">问题：</span>
            <span class="detail-value">{{ currentDetailFaq.question }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">分类：</span>
            <el-tag :type="getCategoryType(currentDetailFaq.category)" size="small">
              {{ getCategoryName(currentDetailFaq.category) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">排序：</span>
            <span class="detail-value">{{ currentDetailFaq.sortOrder }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态：</span>
            <el-tag :type="currentDetailFaq.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentDetailFaq.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-document"></i>
            <span>答案内容</span>
          </div>
          <div class="detail-answer">
            {{ currentDetailFaq.answer || '无固定答案（由AI回答）' }}
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-time"></i>
            <span>时间信息</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">创建时间：</span>
            <span class="detail-value">{{ formatDate(currentDetailFaq.createTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">更新时间：</span>
            <span class="detail-value">{{ formatDate(currentDetailFaq.updateTime) || '--' }}</span>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getFaqListAdmin, addFaq, updateFaq, updateFaqStatus, deleteFaq, batchDeleteFaq } from '@/api/ai';
import request from '@/utils/request';
import { aiChat } from '@/api/ai';

export default {
  name: 'FaqManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      aiGenerating: false,
      faqList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        category: '',
        status: ''
      },
      dialogVisible: false,
      detailVisible: false,
      isEdit: false,
      currentDetailFaq: null,
      currentFaq: {
        id: null,
        question: '',
        answer: '',
        category: 'pet_knowledge',
        sortOrder: 0,
        status: 1
      },
      formRules: {
        question: [
          { required: true, message: '请输入问题', trigger: 'blur' },
          { min: 2, max: 200, message: '长度在2-200个字符', trigger: 'blur' }
        ],
        category: [
          { required: true, message: '请选择分类', trigger: 'change' }
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
          keyword: this.searchForm.keyword || undefined,
          category: this.searchForm.category || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined
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

    // 查看详情
    handleView(row) {
      this.currentDetailFaq = row;
      this.detailVisible = true;
    },

    // 获取分类名称
    getCategoryName(category) {
      const map = {
        service: '服务咨询',
        pet_knowledge: '宠物知识',
        pet_product: '宠物用品',
        other: '其他'
      };
      return map[category] || '其他';
    },

    // 获取分类标签类型
    getCategoryType(category) {
      const map = {
        service: 'primary',
        pet_knowledge: 'success',
        pet_product: 'warning',
        other: 'info'
      };
      return map[category] || 'info';
    },

    // AI生成答案
    async generateAnswerByAI() {
      if (!this.currentFaq.question) {
        this.$message.warning('请先填写问题，以便AI生成答案');
        return;
      }

      this.aiGenerating = true;
      try {
        // ✅ 复用已有的 aiChat 接口
        const res = await aiChat({ question: this.currentFaq.question });
        if (res.code === 200 && res.data.answer) {
          this.currentFaq.answer = res.data.answer;
          this.$message.success('AI答案生成成功');
        } else {
          this.$message.error('AI生成失败，请手动输入');
        }
      } catch (error) {
        console.error('AI生成答案失败', error);
        this.$message.error('AI生成失败，请手动输入');
      } finally {
        this.aiGenerating = false;
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

    // 批量启用
    async handleBatchEnable() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要启用选中的 ${this.selectedRows.length} 个问题吗？`, '提示', { type: 'info' })
          .then(async () => {
            try {
              // 逐个更新状态
              for (const row of this.selectedRows) {
                await updateFaqStatus(row.id, 1);
              }
              this.$message.success('批量启用成功');
              this.selectedRows = [];
              this.loadList();
            } catch (error) {
              this.$message.error('批量启用失败');
            }
          }).catch(() => {});
    },

    // 批量禁用
    async handleBatchDisable() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要禁用选中的 ${this.selectedRows.length} 个问题吗？`, '提示', { type: 'warning' })
          .then(async () => {
            try {
              for (const row of this.selectedRows) {
                await updateFaqStatus(row.id, 0);
              }
              this.$message.success('批量禁用成功');
              this.selectedRows = [];
              this.loadList();
            } catch (error) {
              this.$message.error('批量禁用失败');
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
        category: 'pet_knowledge',
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
        category: row.category || 'pet_knowledge',
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

/* 详情对话框样式 */
.faq-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.faq-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.faq-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 10px;
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

.detail-item {
  margin-bottom: 12px;
}

.detail-label {
  font-size: 13px;
  color: #909399;
  display: inline-block;
  width: 70px;
}

.detail-value {
  font-size: 13px;
  color: #2c3e50;
}

.detail-answer {
  background: #f8f9fc;
  padding: 16px;
  border-radius: 12px;
  color: #5a6874;
  line-height: 1.8;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 768px) {
  .faq-manage {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .action-left, .action-right {
    justify-content: center;
  }
}
</style>