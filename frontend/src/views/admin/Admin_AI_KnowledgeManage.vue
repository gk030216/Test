<template>
  <div class="knowledge-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon"><i class="el-icon-collection" /></div>
        <div class="stat-info">
          <div class="stat-label">知识总数</div>
          <div class="stat-value">{{ total }}</div>
          <div class="stat-sub">知识库条目</div>
        </div>
      </div>
      <div class="stat-card card-manual">
        <div class="stat-icon"><i class="el-icon-edit" /></div>
        <div class="stat-info">
          <div class="stat-label">人工录入</div>
          <div class="stat-value">{{ manualCount }}</div>
          <div class="stat-sub">手动创建</div>
        </div>
      </div>
      <div class="stat-card card-ai">
        <div class="stat-icon"><i class="el-icon-cpu" /></div>
        <div class="stat-info">
          <div class="stat-label">AI 生成</div>
          <div class="stat-value">{{ aiCount }}</div>
          <div class="stat-sub">对话转换</div>
        </div>
      </div>
      <div class="stat-card card-usage">
        <div class="stat-icon"><i class="el-icon-data-line" /></div>
        <div class="stat-info">
          <div class="stat-label">总使用次数</div>
          <div class="stat-value">{{ totalUsage }}</div>
          <div class="stat-sub">累计被引用</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索标题 / 内容..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.category"
          placeholder="全部分类"
          clearable
          @change="handleSearch"
          class="filter-select"
        >
          <el-option label="服务咨询" value="service" />
          <el-option label="宠物知识" value="pet_knowledge" />
          <el-option label="宠物用品" value="pet_product" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-select
          v-model="searchForm.source"
          placeholder="全部来源"
          clearable
          @change="handleSearch"
          class="filter-select source-select"
        >
          <el-option label="人工录入" :value="1" />
          <el-option label="AI生成" :value="2" />
        </el-select>
        <el-select
          v-model="searchForm.status"
          placeholder="全部状态"
          clearable
          @change="handleSearch"
          class="filter-select status-select"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button type="primary" size="medium" @click="handleAdd" class="warm-btn">
          <i class="el-icon-plus" /> 新增知识
        </el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 知识表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="knowledgeList"
        stripe
        row-key="id"
        class="knowledge-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="ID" width="72" align="center" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template slot-scope="scope">
            <span class="title-text">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="260">
          <template slot-scope="scope">
            <div class="content-preview">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="关键词" width="150">
          <template slot-scope="scope">
            <div class="keywords-wrap">
              <el-tag
                v-for="(kw, i) in (scope.row.keywords || '').split(',').filter(Boolean)"
                :key="i"
                size="small"
                class="kw-tag"
              >{{ kw }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100" align="center">
          <template slot-scope="scope">
            <span class="cat-tag">{{ scope.row.categoryName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="90" align="center">
          <template slot-scope="scope">
            <span class="source-tag" :class="scope.row.source === 1 ? 'src-manual' : 'src-ai'">
              {{ scope.row.source === 1 ? '人工' : 'AI' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="useCount" label="使用次数" width="90" align="center" />
        <el-table-column label="启用" width="75" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.status === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleStatusChange(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-time">{{ formatDate(scope.row.createTime) }}</span>
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
              <el-button type="text" size="small" @click="handleConvertToFaq(scope.row)" class="act-faq">
                <i class="el-icon-star-on" /> 转FAQ
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="620px"
      :close-on-click-modal="false"
      class="knowledge-dialog"
      center
    >
      <div class="dialog-body">
        <el-form :model="currentKnowledge" :rules="formRules" ref="knowledgeForm" label-width="80px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="currentKnowledge.title" placeholder="请输入标题" />
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <div class="ai-row">
              <el-input
                v-model="currentKnowledge.content"
                type="textarea"
                :rows="5"
                placeholder="请输入知识内容"
                style="flex: 1;"
              />
              <el-button
                type="primary" size="medium"
                @click="generateContentByAI"
                :loading="aiContentGenerating"
                :disabled="!currentKnowledge.title"
                class="ai-btn"
              >
                <i class="el-icon-magic-stick" /> AI生成
              </el-button>
            </div>
            <div class="form-tip">点击 AI 生成按钮可根据标题自动生成内容</div>
          </el-form-item>
          <el-form-item label="关键词" prop="keywords">
            <div class="ai-row">
              <el-input
                v-model="currentKnowledge.keywords"
                placeholder="多个关键词用英文逗号分隔"
                style="flex: 1;"
              />
              <el-button
                type="primary" size="medium"
                @click="generateKeywordsByAI"
                :loading="aiGenerating"
                :disabled="!currentKnowledge.title && !currentKnowledge.content"
                class="ai-btn"
              >
                <i class="el-icon-magic-stick" /> AI生成
              </el-button>
            </div>
            <div class="form-tip">例如：狗,食物,禁忌,中毒</div>
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <el-select v-model="currentKnowledge.category" placeholder="请选择分类" style="width:100%">
              <el-option label="服务咨询" value="service" />
              <el-option label="宠物知识" value="pet_knowledge" />
              <el-option label="宠物用品" value="pet_product" />
              <el-option label="其他" value="other" />
            </el-select>
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
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 知识详情对话框 -->
    <el-dialog
      title="知识详情"
      :visible.sync="detailVisible"
      width="600px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetailKnowledge">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="d-label">标题</span>
            <span class="d-value">{{ currentDetailKnowledge.title }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">分类</span>
            <span class="d-value">{{ currentDetailKnowledge.categoryName }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">来源</span>
            <span class="status-tag" :class="currentDetailKnowledge.source === 1 ? 'active' : 'ai'">
              {{ currentDetailKnowledge.source === 1 ? '人工录入' : 'AI生成' }}
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">状态</span>
            <span class="status-tag" :class="currentDetailKnowledge.status === 1 ? 'active' : 'inactive'">
              {{ currentDetailKnowledge.status === 1 ? '启用' : '禁用' }}
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">使用次数</span>
            <span class="d-value">{{ currentDetailKnowledge.useCount || 0 }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">创建时间</span>
            <span class="d-value">{{ formatDate(currentDetailKnowledge.createTime) }}</span>
          </div>
          <div class="detail-item detail-full">
            <span class="d-label">关键词</span>
            <div class="keywords-detail">
              <el-tag v-for="(kw, i) in (currentDetailKnowledge.keywords || '').split(',').filter(Boolean)" :key="i" size="small">{{ kw }}</el-tag>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-label">知识内容</div>
          <div class="detail-content-text">{{ currentDetailKnowledge.content }}</div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="detailVisible = false" class="warm-btn">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getKnowledgeList, addKnowledge, updateKnowledge, updateKnowledgeStatus, deleteKnowledge, batchDeleteKnowledge, convertKnowledgeToFaq } from '@/api/ai';
import request from '@/utils/request';

export default {
  name: 'Admin_AI_KnowledgeManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      aiGenerating: false,
      aiContentGenerating: false,
      detailVisible: false,
      currentDetailKnowledge: null,
      knowledgeList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', category: '', source: '', status: '' },
      dialogVisible: false,
      isEdit: false,
      currentKnowledge: { id: null, title: '', content: '', keywords: '', category: '', source: 1, status: 1 },
      formRules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在2-100个字符', trigger: 'blur' }
        ],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
        keywords: [{ required: true, message: '请输入关键词', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }]
      }
    };
  },
  computed: {
    dialogTitle() { return this.isEdit ? '编辑知识' : '新增知识'; },
    manualCount() { return this.knowledgeList.filter(i => i.source === 1).length; },
    aiCount() { return this.knowledgeList.filter(i => i.source === 2).length; },
    totalUsage() { return this.knowledgeList.reduce((s, i) => s + (i.useCount || 0), 0); }
  },
  created() { this.loadList(); },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = { page: this.page, pageSize: this.pageSize };
        if (this.searchForm.keyword && this.searchForm.keyword.trim()) params.keyword = this.searchForm.keyword.trim();
        if (this.searchForm.category && this.searchForm.category !== '') params.category = this.searchForm.category;
        if (this.searchForm.source !== '' && this.searchForm.source !== null && this.searchForm.source !== undefined) params.source = this.searchForm.source;
        if (this.searchForm.status !== '' && this.searchForm.status !== null && this.searchForm.status !== undefined) params.status = this.searchForm.status;
        const res = await getKnowledgeList(params);
        if (res.code === 200) {
          this.knowledgeList = res.data.list;
          this.total = res.data.total;
        }
      } catch (e) {
        this.$message.error(e?.message || '加载失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() {
      this.searchForm = { keyword: '', category: '', source: '', status: '' };
      this.page = 1; this.loadList();
    },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },
    handleView(row) { this.currentDetailKnowledge = row; this.detailVisible = true; },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    async handleStatusChange(row, val) {
      try {
        const res = await updateKnowledgeStatus(row.id, val ? 1 : 0);
        if (res.code === 200) {
          row.status = val ? 1 : 0;
          this.$message.success(res.message || '操作成功');
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (e) { this.$message.error(e?.message || '状态更新失败，请稍后重试'); }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除知识 "${row.title}" 吗？删除后无法恢复！`, '提示', { type: 'warning' });
        const res = await deleteKnowledge(row.id);
        if (res.code === 200) { this.$message.success('删除成功'); this.loadList(); }
        else { this.$message.error(res.message || '删除失败'); }
      } catch (e) { if (e !== 'cancel') this.$message.error(e?.message || '删除失败，请稍后重试'); }
    },
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条知识吗？`, '警告', { type: 'warning' });
        const res = await batchDeleteKnowledge(ids);
        if (res.code === 200) {
          this.$message.success(res.message || '批量删除成功');
          this.selectedRows = []; this.loadList();
        } else { this.$message.error(res.message || '操作失败'); }
      } catch (e) { if (e !== 'cancel') this.$message.error(e?.message || '批量删除失败，请稍后重试'); }
    },
    handleAdd() {
      this.isEdit = false;
      this.currentKnowledge = { id: null, title: '', content: '', keywords: '', category: '', source: 1, status: 1 };
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.knowledgeForm && this.$refs.knowledgeForm.clearValidate(); });
    },
    handleEdit(row) {
      this.isEdit = true;
      this.currentKnowledge = { id: row.id, title: row.title, content: row.content, keywords: row.keywords, category: row.category, source: row.source, status: row.status };
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.knowledgeForm && this.$refs.knowledgeForm.clearValidate(); });
    },
    async generateKeywordsByAI() {
      if (!this.currentKnowledge.title && !this.currentKnowledge.content) { this.$message.warning('请先填写标题或内容'); return; }
      this.aiGenerating = true;
      try {
        const res = await request({ url: '/admin/ai/generate-keywords', method: 'post', data: { question: this.currentKnowledge.title || this.currentKnowledge.content, answer: this.currentKnowledge.content } });
        if (res.code === 200 && res.data.keywords) { this.currentKnowledge.keywords = res.data.keywords; this.$message.success('AI关键词生成成功'); }
        else { this.$message.error(res.message || 'AI生成失败，请手动输入'); }
      } catch (e) { this.$message.error(e?.message || 'AI生成失败，请手动输入'); }
      finally { this.aiGenerating = false; }
    },
    async generateContentByAI() {
      if (!this.currentKnowledge.title) { this.$message.warning('请先填写标题'); return; }
      this.aiContentGenerating = true;
      try {
        const res = await request({ url: '/admin/ai/generate-content', method: 'post', data: { title: this.currentKnowledge.title, category: this.currentKnowledge.category } });
        if (res.code === 200 && res.data.content) { this.currentKnowledge.content = res.data.content; this.$message.success('AI内容生成成功'); }
        else { this.$message.error(res.message || 'AI生成失败，请手动输入'); }
      } catch (e) { this.$message.error(e?.message || 'AI生成失败，请手动输入'); }
      finally { this.aiContentGenerating = false; }
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
            res = await addKnowledge({ ...this.currentKnowledge, source: 1 });
          }
          if (res.code === 200) { this.$message.success(res.message || (this.isEdit ? '更新成功' : '添加成功')); this.dialogVisible = false; this.loadList(); }
          else { this.$message.error(res.message || '操作失败'); }
        } catch (e) { this.$message.error(e?.message || (this.isEdit ? '更新失败，请稍后重试' : '添加失败，请稍后重试')); }
        finally { this.submitLoading = false; }
      });
    },
    handleConvertToFaq(row) {
      this.$confirm(`确定要将知识 "${row.title}" 转为常见问题吗？`, '提示', { type: 'info' }).then(async () => {
        try {
          const res = await convertKnowledgeToFaq({ id: row.id, title: row.title, content: row.content, keywords: row.keywords, category: row.category });
          if (res.code === 200) { this.$message.success('转换成功'); }
          else { this.$message.error(res.message || '转换失败'); }
        } catch (e) { this.$message.error(e?.message || '转换失败，请稍后重试'); }
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.knowledge-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card {
  position: relative; background: #fff; border-radius: 16px;
  padding: 24px 28px; display: flex; align-items: center; gap: 20px;
  overflow: hidden; box-shadow: 0 2px 12px rgba(61,46,42,0.05);
  transition: transform 0.25s, box-shadow 0.25s; cursor: default;
}
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 12px 28px rgba(61,46,42,0.10); }
.stat-icon {
  width: 52px; height: 52px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: #fff; flex-shrink: 0;
}
.card-total .stat-icon  { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-manual .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-ai .stat-icon     { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-usage .stat-icon  { background: linear-gradient(135deg, #36cfc9, #5cdbd3); }
.stat-info { position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 6px; }
.stat-value { font-size: 30px; font-weight: 700; color: #3d2e2a; line-height: 1.1; letter-spacing: -0.5px; }
.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

.filter-card {
  background: #fff; border-radius: 16px; padding: 18px 24px; margin-bottom: 16px;
  display: flex; justify-content: space-between; align-items: center;
  flex-wrap: wrap; gap: 14px; box-shadow: 0 2px 12px rgba(61,46,42,0.04);
}
.filter-left, .filter-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.keyword-input { width: 220px; }
.keyword-input ::v-deep .el-input__inner { border-radius: 10px; }
.filter-select { width: 110px; }
.filter-select ::v-deep .el-input__inner { border-radius: 10px; }
.source-select { width: 100px; }
.status-select { width: 90px; }

.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
  border-radius: 10px !important; padding: 9px 20px !important; font-weight: 500;
}
.warm-btn:hover { opacity: 0.88; transform: translateY(-1px); }
.reset-btn { border-radius: 10px; color: #606266; }
.reset-btn:hover { background: #fdf6f0; border-color: #f5d5c0; }

.batch-bar {
  background: linear-gradient(135deg, #fef5ee, #fff7f0);
  border: 1px solid #f5d5c0; border-left: 4px solid #f59e4b;
  border-radius: 12px; padding: 10px 20px; margin-bottom: 16px;
  display: flex; align-items: center; gap: 12px; color: #3d2e2a;
}
.batch-info { font-size: 13px; }
.batch-info strong { color: #f59e4b; font-size: 15px; }
.batch-bar .el-button { border-radius: 8px; padding: 7px 16px; font-weight: 500; }
.batch-btn-delete { background: rgba(245,108,108,0.12) !important; border: 1px solid rgba(245,108,108,0.35) !important; color: #f56c6c !important; }
.batch-btn-delete:hover { background: rgba(245,108,108,0.22) !important; border-color: #f56c6c !important; }
.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

.table-card { background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 2px 12px rgba(61,46,42,0.04); }
.knowledge-table ::v-deep .el-table__header th {
  background: #fdf8f4; color: #5c4a42; font-weight: 600;
  font-size: 12px; letter-spacing: 0.4px; padding: 15px 0; border-bottom: 2px solid #f5ece6;
}
.knowledge-table ::v-deep .el-table__body td { padding: 15px 0; font-size: 13px; color: #3d2e2a; }
.knowledge-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.knowledge-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.knowledge-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.knowledge-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner { background: #f59e4b; border-color: #f59e4b; }

.title-text { font-weight: 500; color: #3d2e2a; }
.content-preview { color: #5c4a42; font-size: 13px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.keywords-wrap { display: flex; flex-wrap: wrap; gap: 4px; }
.kw-tag { background: #fdf6f0; border: 1px solid #f5d5c0; color: #f59e4b; }
.cat-tag { display: inline-block; padding: 4px 12px; border-radius: 20px; background: #fdf6f0; color: #f59e4b; font-size: 12px; }
.source-tag { display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 12px; }
.src-manual { background: #edf7ee; color: #67c23a; }
.src-ai { background: #edf4fd; color: #409eff; }
.cell-time { color: #a08c84; font-size: 12px; }

.action-btns { display: flex; align-items: center; justify-content: center; gap: 2px; flex-wrap: wrap; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
.act-faq { color: #409eff !important; }
.act-faq:hover { color: #337ecc !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }

.pagination-card { background: #fff; border-radius: 16px; margin-top: 16px; padding: 16px 24px; display: flex; justify-content: flex-end; box-shadow: 0 2px 12px rgba(61,46,42,0.04); }

.knowledge-dialog ::v-deep .el-dialog, .detail-dialog ::v-deep .el-dialog { border-radius: 20px; overflow: hidden; }
.knowledge-dialog ::v-deep .el-dialog__header, .detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a); padding: 22px 28px; margin: 0;
}
.knowledge-dialog ::v-deep .el-dialog__title, .detail-dialog ::v-deep .el-dialog__title { color: #fff; font-weight: 700; font-size: 17px; }
.knowledge-dialog ::v-deep .el-dialog__close, .detail-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.knowledge-dialog ::v-deep .el-dialog__close:hover, .detail-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.knowledge-dialog ::v-deep .el-dialog__body, .detail-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }
.ai-row { display: flex; gap: 10px; align-items: flex-start; }
.ai-btn { border-radius: 10px; padding: 10px 16px; white-space: nowrap; flex-shrink: 0; }
.form-tip { font-size: 12px; color: #a08c84; margin-top: 4px; }

.detail-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }
.detail-grid {
  display: grid; grid-template-columns: 1fr 1fr;
  border: 1px solid #f5ece6; border-radius: 12px; overflow: hidden; margin-bottom: 20px;
}
.detail-item {
  display: flex; align-items: center; padding: 14px 18px;
  border-bottom: 1px solid #f5ece6; border-right: 1px solid #f5ece6;
}
.detail-item:nth-child(2n) { border-right: none; }
.detail-item:last-child { border-bottom: none; }
.detail-item:nth-last-child(2):nth-child(odd) { border-bottom: none; }
.detail-full { grid-column: 1 / -1; border-right: none; }
.d-label { width: 68px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; font-weight: 500; }
.status-tag { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-tag.active { background: #edf7ee; color: #67c23a; }
.status-tag.inactive { background: #fef0f0; color: #f56c6c; }
.status-tag.ai { background: #edf4fd; color: #409eff; }
.keywords-detail { display: inline-flex; flex-wrap: wrap; gap: 4px; }
.section-label { font-size: 13px; font-weight: 600; color: #5c4a42; margin-bottom: 10px; }
.detail-content-text { background: #fdf8f4; padding: 16px; border-radius: 12px; color: #5c4a42; line-height: 1.8; font-size: 14px; white-space: pre-wrap; word-break: break-word; }

.dialog-footer { text-align: right; padding: 16px 28px 22px; border-top: 1px solid #f5ece6; }
.dialog-footer .el-button { border-radius: 10px; padding: 10px 28px; font-weight: 500; font-size: 13px; }
.dialog-footer .el-button--primary { background: linear-gradient(135deg, #f59e4b, #f0826a) !important; border: none !important; color: #fff !important; }
.dialog-footer .el-button--primary:hover { opacity: 0.88; transform: translateY(-1px); }

@media (max-width: 768px) {
  .knowledge-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 100%; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-item { border-right: none; }
  .detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }
  .detail-item:last-child { border-bottom: none; }
  .detail-full { grid-column: 1; }
}
</style>
