<template>
  <div class="chat-history-page">
    <!-- 统计概览卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">对话总数</div>
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-sub">AI对话总量</div>
        </div>
      </div>
      <div class="stat-card card-knowledge">
        <div class="stat-icon">
          <i class="el-icon-collection"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">知识库来源</div>
          <div class="stat-value">{{ stats.knowledgeCount }}</div>
          <div class="stat-sub">基于知识库回答</div>
        </div>
      </div>
      <div class="stat-card card-ai">
        <div class="stat-icon">
          <i class="el-icon-cpu"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">AI 智能来源</div>
          <div class="stat-value">{{ stats.aiCount }}</div>
          <div class="stat-sub">AI直接生成</div>
        </div>
      </div>
      <div class="stat-card card-rating">
        <div class="stat-icon">
          <i class="el-icon-star-on"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">平均评分</div>
          <div class="stat-value">{{ stats.avgRating }}</div>
          <div class="stat-sub">用户评价均分</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索用户 / 问题 / 回答..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.answerSource"
          placeholder="答案来源"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="知识库" :value="1" />
          <el-option label="AI智能" :value="2" />
        </el-select>
        <el-select
          v-model="searchForm.rating"
          placeholder="评分"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="5星" :value="5" />
          <el-option label="4星" :value="4" />
          <el-option label="3星" :value="3" />
          <el-option label="2星" :value="2" />
          <el-option label="1星" :value="1" />
          <el-option label="未评分" :value="0" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button size="medium" @click="handleExport" :loading="exportLoading" class="export-btn">
          <i class="el-icon-download" /> 导出
        </el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-convert" @click="handleBatchConvertToKnowledge">
          <i class="el-icon-star-on" /> 批量转为知识库
        </el-button>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 对话表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="historyList"
        stripe
        row-key="id"
        class="history-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="用户" min-width="160">
          <template slot-scope="scope">
            <div class="user-cell">
              <div class="avatar-wrap">
                <el-avatar :size="36" :src="scope.row.userAvatar">
                  {{ scope.row.userAvatar ? '' : (scope.row.userNickname || scope.row.userName || '?').charAt(0).toUpperCase() }}
                </el-avatar>
              </div>
              <div class="user-meta">
                <span class="user-name">{{ scope.row.userName || '未知用户' }}</span>
                <span class="user-nick">{{ scope.row.userNickname || '--' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="userQuestion" label="用户问题" min-width="220">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.userQuestion }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="aiAnswer" label="AI回答" min-width="280">
          <template slot-scope="scope">
            <span class="cell-text answer-clamp">{{ scope.row.aiAnswer }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="answerSource" label="来源" width="90" align="center">
          <template slot-scope="scope">
            <span class="source-tag" :class="scope.row.answerSource === 1 ? 'source-kb' : 'source-ai'">
              {{ scope.row.answerSource === 1 ? '知识库' : 'AI智能' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="评分" width="160" align="center">
          <template slot-scope="scope">
            <div v-if="scope.row.rating && scope.row.rating > 0" class="rating-cell">
              <el-rate
                v-model="scope.row.rating"
                disabled
                :max="5"
                :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
              />
            </div>
            <span v-else class="no-rating">未评分</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="对话时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleDetail(scope.row)" class="act-view">
                <i class="el-icon-view" /> 详情
              </el-button>
              <el-button
                v-if="scope.row.answerSource === 2"
                type="text"
                size="small"
                @click="convertToKnowledge(scope.row)"
                class="act-convert"
              >
                <i class="el-icon-star-on" /> 转知识库
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

    <!-- 详情对话框 -->
    <el-dialog
      title="对话详情"
      :visible.sync="detailVisible"
      width="700px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetail">
        <div class="detail-item">
          <span class="d-label">用户</span>
          <span class="d-value">{{ currentDetail.userName || '未知用户' }}{{ currentDetail.userNickname ? ' (' + currentDetail.userNickname + ')' : '' }}</span>
        </div>
        <div class="detail-item">
          <span class="d-label">对话时间</span>
          <span class="d-value">{{ formatDate(currentDetail.createTime) }}</span>
        </div>
        <div class="detail-item">
          <span class="d-label">用户问题</span>
          <span class="d-value d-box d-box-question">{{ currentDetail.userQuestion }}</span>
        </div>
        <div class="detail-item">
          <span class="d-label">AI回答</span>
          <span class="d-value d-box d-box-answer">{{ currentDetail.aiAnswer }}</span>
        </div>
        <div class="detail-item">
          <span class="d-label">答案来源</span>
          <span class="d-value">
            <span class="source-tag" :class="currentDetail.answerSource === 1 ? 'source-kb' : 'source-ai'">
              {{ currentDetail.answerSource === 1 ? '知识库' : 'AI智能' }}
            </span>
          </span>
        </div>
        <div class="detail-item">
          <span class="d-label">用户评分</span>
          <span class="d-value">
            <template v-if="currentDetail.rating && currentDetail.rating > 0">
              <el-rate
                v-model="currentDetail.rating"
                disabled
                :max="5"
                :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
              />
            </template>
            <span v-else class="no-rating">未评分</span>
          </span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="detailVisible = false" class="warm-btn">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 转为知识库对话框 -->
    <el-dialog
      title="转为知识库"
      :visible.sync="convertDialogVisible"
      width="600px"
      center
      class="convert-dialog"
    >
      <el-form :model="convertForm" :rules="convertRules" ref="convertForm" label-width="80px" class="convert-form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="convertForm.title" placeholder="请输入知识标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" v-model="convertForm.content" :rows="5" placeholder="知识内容" />
        </el-form-item>
        <el-form-item label="关键词" prop="keywords">
          <div class="keywords-row">
            <el-input
              v-model="convertForm.keywords"
              placeholder="多个关键词用英文逗号分隔"
            />
            <el-button
              type="primary"
              @click="generateKeywordsForConvert"
              :loading="aiGenerating"
              :disabled="!convertForm.title && !convertForm.content"
              class="warm-btn"
            >
              <i class="el-icon-magic-stick" /> AI生成
            </el-button>
          </div>
          <p class="form-tip">例如：狗,食物,禁忌,中毒 | 点击AI生成按钮可根据标题和内容自动生成关键词</p>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="convertForm.category" placeholder="请选择分类">
            <el-option label="服务" value="service" />
            <el-option label="宠物知识" value="pet_knowledge" />
            <el-option label="宠物产品" value="pet_product" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="convertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConvert" :loading="convertLoading" class="warm-btn">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getChatHistoryList,
  deleteChatHistory,
  batchDeleteChatHistory,
  convertToKnowledge,
  batchConvertToKnowledge
} from '@/api/ai';
import request from '@/utils/request';

export default {
  name: 'Admin_AI_ChatHistory',
  data() {
    return {
      loading: false,
      exportLoading: false,
      convertLoading: false,
      aiGenerating: false,
      historyList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', answerSource: '', rating: '' },
      detailVisible: false,
      currentDetail: null,
      convertDialogVisible: false,
      currentConvertItem: null,
      stats: { total: 0, knowledgeCount: 0, aiCount: 0, avgRating: '--' },
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
    this.computeStats();
  },
  methods: {
    normalizeRating(rating) {
      if (rating === null || rating === undefined || rating === '') return null;
      const n = Number(rating);
      if (isNaN(n) || n < 1) return null;
      return n > 5 ? 5 : n;
    },
    async computeStats() {
      try {
        const res = await getChatHistoryList({
          page: 1,
          pageSize: 500,
          keyword: this.searchForm.keyword || undefined,
          answerSource: this.searchForm.answerSource || undefined,
          rating: this.searchForm.rating || undefined
        });
        if (res.code === 200) {
          const list = res.data.list || [];
          const knowledgeList = list.filter(i => i.answerSource === 1);
          const aiList = list.filter(i => i.answerSource === 2);
          const rated = list
            .map(i => this.normalizeRating(i.rating))
            .filter(r => r !== null);
          this.stats.total = res.data.total || 0;
          this.stats.knowledgeCount = knowledgeList.length;
          this.stats.aiCount = aiList.length;
          this.stats.avgRating = rated.length > 0
            ? (rated.reduce((a, b) => a + b, 0) / rated.length).toFixed(1)
            : '--';
        }
      } catch { /* stats fetch silently fails */ }
    },
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
          this.historyList = (res.data.list || []).map(item => ({
            ...item,
            rating: this.normalizeRating(item.rating)
          }));
          this.total = res.data.total || 0;
        }
      } catch {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() { this.page = 1; this.loadList(); this.computeStats(); },
    handleReset() {
      this.searchForm = { keyword: '', answerSource: '', rating: '' };
      this.page = 1;
      this.loadList();
      this.computeStats();
    },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    handleDetail(row) {
      this.currentDetail = {
        ...row,
        rating: this.normalizeRating(row.rating)
      };
      this.detailVisible = true;
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该对话记录吗？', '提示', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        });
        const res = await deleteChatHistory(row.id);
        if (res.code === 200) {
          this.$message.success('删除成功');
          this.loadList();
          this.computeStats();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败');
      }
    },
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条记录吗？`, '警告', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        });
        const res = await batchDeleteChatHistory(ids);
        if (res.code === 200) {
          this.$message.success(res.message);
          this.selectedRows = [];
          this.loadList();
          this.computeStats();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('批量删除失败');
      }
    },
    async handleExport() {
      this.exportLoading = true;
      try {
        const params = new URLSearchParams();
        if (this.searchForm.keyword) params.append('keyword', this.searchForm.keyword);
        if (this.searchForm.answerSource) params.append('answerSource', this.searchForm.answerSource);
        if (this.searchForm.rating) params.append('rating', this.searchForm.rating);
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/admin/ai/history/export?${params.toString()}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('导出失败');
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `对话记录_${Date.now()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.$message.success('导出成功');
      } catch {
        this.$message.error('导出失败');
      } finally {
        this.exportLoading = false;
      }
    },
    async generateKeywordsForConvert() {
      if (!this.convertForm.title && !this.convertForm.content) {
        this.$message.warning('请先填写标题或内容，以便AI生成关键词');
        return;
      }
      this.aiGenerating = true;
      try {
        const res = await request({
          url: '/admin/ai/generate-keywords',
          method: 'post',
          data: {
            question: this.convertForm.title || this.convertForm.content,
            answer: this.convertForm.content
          }
        });
        if (res.code === 200 && res.data.keywords) {
          this.convertForm.keywords = res.data.keywords;
          this.$message.success('AI关键词生成成功');
        } else {
          this.$message.error('AI生成失败，请手动输入');
        }
      } catch {
        this.$message.error('AI生成失败，请手动输入');
      } finally {
        this.aiGenerating = false;
      }
    },
    convertToKnowledge(row) {
      this.currentConvertItem = row;
      this.convertForm = {
        title: row.userQuestion && row.userQuestion.length > 50 ? row.userQuestion.substring(0, 50) : (row.userQuestion || ''),
        content: row.aiAnswer || '',
        keywords: '',
        category: 'other'
      };
      this.convertDialogVisible = true;
    },
    async submitConvert() {
      if (!this.convertForm.keywords || this.convertForm.keywords.trim() === '') {
        try {
          await this.$confirm('关键词为空，是否继续保存？', '提示', {
            confirmButtonText: '继续保存', cancelButtonText: '取消', type: 'warning'
          });
        } catch {
          return;
        }
      }
      this.$refs.convertForm.validate(async (valid) => {
        if (!valid) return;
        this.convertLoading = true;
        try {
          const res = await convertToKnowledge({
            title: this.convertForm.title,
            content: this.convertForm.content,
            keywords: this.convertForm.keywords,
            category: this.convertForm.category,
            source: 2
          });
          if (res.code === 200) {
            this.$message.success('已添加到知识库');
            this.convertDialogVisible = false;
            this.loadList();
            this.computeStats();
          } else {
            this.$message.error(res.message);
          }
        } catch (e) {
          this.$message.error(e?.message || '转换失败，请稍后重试');
        } finally {
          this.convertLoading = false;
        }
      });
    },
    extractKeywordsSimple(question) {
      let keywords = question.replace(/[？?！!。，,、]/g, '');
      if (keywords.length > 30) keywords = keywords.substring(0, 30);
      return keywords;
    },
    async extractKeywordsWithAI(question, answer) {
      try {
        const res = await request({
          url: '/admin/ai/generate-keywords',
          method: 'post',
          data: { question, answer }
        });
        if (res.code === 200 && res.data.keywords) return res.data.keywords;
      } catch { /* fall through */ }
      return null;
    },
    async handleBatchConvertToKnowledge() {
      if (this.selectedRows.length === 0) return;
      const aiItems = this.selectedRows.filter(row => row.answerSource === 2);
      if (aiItems.length === 0) {
        this.$message.warning('请选择AI智能来源的记录进行转换');
        return;
      }
      try {
        await this.$confirm(`确定要将选中的 ${aiItems.length} 条AI回答转换为知识库条目吗？`, '提示', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
        });
      } catch {
        return;
      }
      this.$message.info('正在使用AI批量生成关键词...');
      const items = [];
      let aiSuccessCount = 0;
      for (const row of aiItems) {
        let keywords;
        try {
          const aiKeywords = await this.extractKeywordsWithAI(row.userQuestion, row.aiAnswer);
          if (aiKeywords && aiKeywords !== '') {
            keywords = aiKeywords;
            aiSuccessCount++;
          } else {
            keywords = this.extractKeywordsSimple(row.userQuestion);
          }
        } catch {
          keywords = this.extractKeywordsSimple(row.userQuestion);
        }
        items.push({
          title: row.userQuestion && row.userQuestion.length > 50 ? row.userQuestion.substring(0, 50) : (row.userQuestion || ''),
          content: row.aiAnswer || '',
          keywords,
          category: 'other',
          source: 2
        });
      }
      if (aiSuccessCount > 0) {
        this.$message.success(`成功使用AI生成 ${aiSuccessCount} 条关键词`);
      }
      try {
        const res = await batchConvertToKnowledge({ items });
        if (res.code === 200) {
          this.$message.success(res.message);
          this.selectedRows = [];
          this.loadList();
          this.computeStats();
        } else {
          this.$message.error(res.message);
        }
      } catch (e) {
        this.$message.error(e?.message || '批量转换失败，请稍后重试');
      }
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.chat-history-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 统计卡片行 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.05);
  transition: transform 0.25s, box-shadow 0.25s;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(61, 46, 42, 0.10);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}
.card-total .stat-icon     { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-knowledge .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-ai .stat-icon        { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-rating .stat-icon    { background: linear-gradient(135deg, #e6a23c, #f7ba2a); }

.stat-info { position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 6px; }
.stat-value { font-size: 30px; font-weight: 700; color: #3d2e2a; line-height: 1.1; letter-spacing: -0.5px; }
.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

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
.keyword-input { width: 260px; }
.keyword-input ::v-deep .el-input__inner { border-radius: 10px; }
.status-select { width: 110px; }
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

.export-btn {
  background: linear-gradient(135deg, #67c23a, #85ce61) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  padding: 9px 20px !important;
  font-weight: 500;
}
.export-btn:hover { opacity: 0.88; transform: translateY(-1px); }

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
  border-radius: 8px; padding: 7px 16px;
  font-weight: 500; transition: all 0.2s;
}
.batch-btn-convert {
  background: rgba(245, 158, 75, 0.15) !important;
  border: 1px solid rgba(245, 158, 75, 0.35) !important;
  color: #f59e4b !important;
}
.batch-btn-convert:hover {
  background: rgba(245, 158, 75, 0.25) !important;
  border-color: #f59e4b !important;
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
.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.history-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.history-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.history-table ::v-deep .el-table__body tr:hover > td {
  background: #fefbf8;
}
.history-table ::v-deep .el-table__body tr.el-table__row--striped > td {
  background: #fefcf9;
}
.history-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td {
  background: #fefbf8;
}
.history-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b;
  border-color: #f59e4b;
}

/* 用户信息单元格 */
.user-cell { display: flex; align-items: center; gap: 12px; }
.avatar-wrap { flex-shrink: 0; }
.avatar-wrap .el-avatar {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  color: #fff; font-weight: 600;
}
.user-meta { display: flex; flex-direction: column; gap: 2px; }
.user-name { font-weight: 600; color: #3d2e2a; font-size: 13px; }
.user-nick { font-size: 12px; color: #a08c84; }
.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { color: #a08c84; font-size: 12px; }
.answer-clamp {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 来源标签 */
.source-tag {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.source-kb { background: #edf7ee; color: #67c23a; }
.source-ai { background: #eaf2fd; color: #409eff; }

/* 评分 */
.rating-cell { display: flex; justify-content: center; }
.no-rating { color: #a08c84; font-size: 12px; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-convert { color: #f59e4b !important; }
.act-convert:hover { color: #e8833a !important; }
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

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 0;
  border: 1px solid #f5ece6;
  border-radius: 12px;
  overflow: hidden;
}
.detail-item {
  display: flex;
  align-items: flex-start;
  padding: 14px 18px;
  border-bottom: 1px solid #f5ece6;
}
.detail-item:last-child { border-bottom: none; }
.d-label { width: 80px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; line-height: 1.6; word-break: break-word; }
.d-box { display: block; padding: 12px; border-radius: 8px; }
.d-box-question { background: #fdf8f4; }
.d-box-answer { background: #fef9e6; }

/* ===== 转换对话框 ===== */
.convert-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.convert-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.convert-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.convert-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.convert-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.convert-dialog ::v-deep .el-dialog__body { padding: 28px; }

.convert-form ::v-deep .el-form-item { margin-bottom: 18px; }
.convert-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.convert-form ::v-deep .el-input__inner,
.convert-form ::v-deep .el-textarea__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.convert-form ::v-deep .el-input__inner:focus,
.convert-form ::v-deep .el-textarea__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}

.keywords-row {
  display: flex; gap: 10px;
}
.keywords-row .el-input { flex: 1; }

.form-tip { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ===== 对话框底部 ===== */
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

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .chat-history-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 100%; }
}
</style>
