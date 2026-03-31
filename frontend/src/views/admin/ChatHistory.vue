<template>
  <div class="chat-history">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
            class="batch-btn"
        >
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="success"
            plain
            @click="handleBatchConvertToKnowledge"
            class="batch-btn"
        >
          <i class="el-icon-star-on"></i> 批量转为知识库 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索用户/问题/回答"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          >
          </el-input>
        </div>
        <el-select
            v-model="searchForm.answerSource"
            placeholder="答案来源"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="知识库" :value="1"></el-option>
          <el-option label="AI智能" :value="2"></el-option>
        </el-select>
        <el-select
            v-model="searchForm.rating"
            placeholder="评分"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="5星" :value="5"></el-option>
          <el-option label="4星" :value="4"></el-option>
          <el-option label="3星" :value="3"></el-option>
          <el-option label="2星" :value="2"></el-option>
          <el-option label="1星" :value="1"></el-option>
          <el-option label="未评分" :value="0"></el-option>
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

    <!-- 对话表格 -->
    <el-table
        v-loading="loading"
        :data="historyList"
        stripe
        style="width: 100%"
        class="history-table"
        @selection-change="handleSelectionChange"
        row-key="id"
    >
      <el-table-column type="selection" width="45" align="center" :reserve-selection="true"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="userName" label="用户" width="120">
        <template slot-scope="scope">
          <div class="user-info">
            <span class="user-name">{{ scope.row.userName || '未知用户' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="userQuestion" label="用户问题" min-width="250">
        <template slot-scope="scope">
          <div class="question-text">{{ scope.row.userQuestion }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="aiAnswer" label="AI回答" min-width="300">
        <template slot-scope="scope">
          <div class="answer-text">{{ scope.row.aiAnswer }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="answerSource" label="来源" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.answerSource === 1 ? 'success' : 'primary'" size="small">
            {{ scope.row.answerSource === 1 ? '知识库' : 'AI智能' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评分" width="120" align="center">
        <template slot-scope="scope">
          <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900" v-if="scope.row.rating"></el-rate>
          <span v-else class="no-rating">未评分</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="对话时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button
                size="small"
                type="primary"
                plain
                circle
                @click="handleDetail(scope.row)"
                class="action-icon-btn"
                title="详情"
            >
              <i class="el-icon-view"></i>
            </el-button>
            <el-button
                v-if="scope.row.answerSource === 2"
                size="small"
                type="warning"
                plain
                circle
                @click="convertToKnowledge(scope.row)"
                class="action-icon-btn"
                title="转为知识库"
            >
              <i class="el-icon-star-on"></i>
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

    <!-- 详情对话框 -->
    <el-dialog title="对话详情" :visible.sync="detailVisible" width="700px" center class="detail-dialog">
      <div class="detail-content" v-if="currentDetail">
        <div class="detail-item">
          <div class="detail-label">用户：</div>
          <div class="detail-value">{{ currentDetail.userName || '未知用户' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">对话时间：</div>
          <div class="detail-value">{{ formatDate(currentDetail.createTime) }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">用户问题：</div>
          <div class="detail-value question-box">{{ currentDetail.userQuestion }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">AI回答：</div>
          <div class="detail-value answer-box">{{ currentDetail.aiAnswer }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">答案来源：</div>
          <div class="detail-value">
            <el-tag :type="currentDetail.answerSource === 1 ? 'success' : 'primary'">
              {{ currentDetail.answerSource === 1 ? '知识库' : 'AI智能' }}
            </el-tag>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-label">用户评分：</div>
          <div class="detail-value">
            <el-rate v-model="currentDetail.rating" disabled show-score text-color="#ff9900" v-if="currentDetail.rating"></el-rate>
            <span v-else>未评分</span>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 转为知识库对话框 -->
    <el-dialog title="转为知识库" :visible.sync="convertDialogVisible" width="600px" center class="convert-dialog">
      <el-form :model="convertForm" :rules="convertRules" ref="convertForm" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="convertForm.title" placeholder="请输入知识标题"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" v-model="convertForm.content" :rows="5" placeholder="知识内容"></el-input>
        </el-form-item>
        <el-form-item label="关键词" prop="keywords">
          <el-input v-model="convertForm.keywords" placeholder="多个关键词用英文逗号分隔"></el-input>
          <div class="form-tip">例如：狗,食物,禁忌,中毒</div>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="convertForm.category" placeholder="请选择分类">
            <el-option label="饮食" value="diet"></el-option>
            <el-option label="健康" value="health"></el-option>
            <el-option label="训练" value="training"></el-option>
            <el-option label="行为" value="behavior"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="convertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConvert" :loading="convertLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getChatHistoryList,
  deleteChatHistory,
  batchDeleteChatHistory,
  exportChatHistory,
  convertToKnowledge,
  batchConvertToKnowledge
} from '@/api/ai';

export default {
  name: 'ChatHistory',
  data() {
    return {
      loading: false,
      exportLoading: false,
      convertLoading: false,
      historyList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        answerSource: '',
        rating: ''
      },
      detailVisible: false,
      currentDetail: null,
      convertDialogVisible: false,
      currentConvertItem: null,
      convertForm: {
        title: '',
        content: '',
        keywords: '',
        category: 'other'
      },
      convertRules: {
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
          answerSource: this.searchForm.answerSource || undefined,
          rating: this.searchForm.rating || undefined
        };
        const res = await getChatHistoryList(params);
        if (res.code === 200) {
          this.historyList = res.data.list;
          this.total = res.data.total;
        }
      } catch (error) {
        console.error('加载对话记录失败', error);
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
      this.searchForm = { keyword: '', answerSource: '', rating: '' };
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
    handleDetail(row) {
      this.currentDetail = row;
      this.detailVisible = true;
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除该对话记录吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteChatHistory(row.id);
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
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条记录吗？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteChatHistory(ids);
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
    async handleExport() {
      this.exportLoading = true;
      try {
        const params = new URLSearchParams();
        if (this.searchForm.keyword) params.append('keyword', this.searchForm.keyword);
        if (this.searchForm.answerSource) params.append('answerSource', this.searchForm.answerSource);
        if (this.searchForm.rating) params.append('rating', this.searchForm.rating);

        const token = localStorage.getItem('token');
        const url = `/api/admin/ai/history/export?${params.toString()}`;

        const response = await fetch(url, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('导出失败');
        }

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = `对话记录_${new Date().getTime()}.xlsx`;
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

    // 转为知识库
    convertToKnowledge(row) {
      this.currentConvertItem = row;
      // 自动填充表单
      this.convertForm = {
        title: row.userQuestion.length > 50 ? row.userQuestion.substring(0, 50) : row.userQuestion,
        content: row.aiAnswer,
        keywords: this.extractKeywords(row.userQuestion),
        category: 'other'
      };
      this.convertDialogVisible = true;
    },

    // 提取关键词（简单处理）
    extractKeywords(question) {
      // 去除标点符号和常用词
      let keywords = question.replace(/[？?！!。，,、]/g, '');
      // 截取前30个字符作为关键词
      if (keywords.length > 30) {
        keywords = keywords.substring(0, 30);
      }
      return keywords;
    },

    async submitConvert() {
      this.$refs.convertForm.validate(async (valid) => {
        if (!valid) return;

        this.convertLoading = true;
        try {
          const data = {
            title: this.convertForm.title,
            content: this.convertForm.content,
            keywords: this.convertForm.keywords,
            category: this.convertForm.category,
            source: 2  // AI生成待审核
          };
          const res = await convertToKnowledge(data);
          if (res.code === 200) {
            this.$message.success('已添加到知识库，等待审核');
            this.convertDialogVisible = false;
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('转换失败');
        } finally {
          this.convertLoading = false;
        }
      });
    },

    async handleBatchConvertToKnowledge() {
      if (this.selectedRows.length === 0) return;
      // 只转换AI智能来源的记录
      const aiItems = this.selectedRows.filter(row => row.answerSource === 2);
      if (aiItems.length === 0) {
        this.$message.warning('请选择AI智能来源的记录进行转换');
        return;
      }

      this.$confirm(`确定要将选中的 ${aiItems.length} 条AI回答转换为知识库条目吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        const items = aiItems.map(row => ({
          title: row.userQuestion.length > 50 ? row.userQuestion.substring(0, 50) : row.userQuestion,
          content: row.aiAnswer,
          keywords: this.extractKeywords(row.userQuestion),
          category: 'other',
          source: 2
        }));

        try {
          const res = await batchConvertToKnowledge({ items });
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量转换失败');
        }
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
/* 样式保持不变... */
.chat-history {
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

.search-btn, .reset-btn, .export-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.history-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.history-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
}

.question-text {
  color: #2c3e50;
  line-height: 1.5;
}

.answer-text {
  color: #5a6874;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.no-rating {
  color: #909399;
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

/* 详情对话框样式 */
.detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.detail-content {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  margin-bottom: 20px;
}

.detail-label {
  width: 80px;
  font-weight: 600;
  color: #2c3e50;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #5a6874;
  word-break: break-word;
  line-height: 1.6;
}

.question-box {
  background: #f8f9fc;
  padding: 12px;
  border-radius: 8px;
  margin-left: 0;
}

.answer-box {
  background: #fef9e6;
  padding: 12px;
  border-radius: 8px;
  margin-left: 0;
}

/* 转换对话框样式 */
.convert-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.convert-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.convert-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .chat-history {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .action-left, .action-right {
    justify-content: center;
  }

  .detail-item {
    flex-direction: column;
  }

  .detail-label {
    width: auto;
    margin-bottom: 8px;
  }
}
</style>