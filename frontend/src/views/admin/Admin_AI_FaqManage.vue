<template>
  <div class="faq-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon"><i class="el-icon-question" /></div>
        <div class="stat-info">
          <div class="stat-label">问题总数</div>
          <div class="stat-value">{{ total }}</div>
          <div class="stat-sub">常见问题条目</div>
        </div>
      </div>
      <div class="stat-card card-active">
        <div class="stat-icon"><i class="el-icon-check" /></div>
        <div class="stat-info">
          <div class="stat-label">已启用</div>
          <div class="stat-value">{{ enabledCount }}</div>
          <div class="stat-sub">状态正常</div>
        </div>
      </div>
      <div class="stat-card card-disabled">
        <div class="stat-icon"><i class="el-icon-remove" /></div>
        <div class="stat-info">
          <div class="stat-label">已禁用</div>
          <div class="stat-value">{{ disabledCount }}</div>
          <div class="stat-sub">暂不显示</div>
        </div>
      </div>
      <div class="stat-card card-answer">
        <div class="stat-icon"><i class="el-icon-document" /></div>
        <div class="stat-info">
          <div class="stat-label">有固定答案</div>
          <div class="stat-value">{{ hasAnswerCount }}</div>
          <div class="stat-sub">非 AI 回答</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索问题 / 答案..."
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
          <i class="el-icon-plus" /> 新增问题
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

    <!-- 问题表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="faqList"
        stripe
        class="faq-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="ID" width="72" align="center" />
        <el-table-column prop="question" label="问题" min-width="260">
          <template slot-scope="scope">
            <span class="question-text">{{ scope.row.question }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100" align="center">
          <template slot-scope="scope">
            <span :class="['cat-tag', getCategoryClass(scope.row.category)]">
              {{ getCategoryName(scope.row.category) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="答案" min-width="300">
          <template slot-scope="scope">
            <div class="answer-preview">{{ scope.row.answer || '无（AI 实时回答）' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="75" align="center" />
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
        <el-table-column label="操作" width="160" fixed="right" align="center">
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
      class="faq-dialog"
      center
    >
      <div class="dialog-body">
        <el-form :model="currentFaq" :rules="formRules" ref="faqForm" label-width="80px">
          <el-form-item label="问题" prop="question">
            <el-input v-model="currentFaq.question" placeholder="请输入问题" />
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <el-select v-model="currentFaq.category" placeholder="请选择分类" style="width:100%">
              <el-option label="服务咨询" value="service" />
              <el-option label="宠物知识" value="pet_knowledge" />
              <el-option label="宠物用品" value="pet_product" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="答案" prop="answer">
            <div class="ai-row">
              <el-input
                v-model="currentFaq.answer"
                type="textarea"
                :rows="4"
                placeholder="留空则使用 AI 回答"
                style="flex: 1;"
              />
              <el-button
                type="primary" size="medium"
                @click="generateAnswerByAI"
                :loading="aiGenerating"
                :disabled="!currentFaq.question"
                class="ai-btn"
              >
                <i class="el-icon-magic-stick" /> AI生成
              </el-button>
            </div>
            <div class="form-tip">不填写答案时，用户点击后由 AI 实时回答</div>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="currentFaq.sortOrder" :min="0" :max="999" controls-position="right" />
            <span class="form-tip-inline">数字越小越靠前</span>
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
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      title="常见问题详情"
      :visible.sync="detailVisible"
      width="550px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetailFaq">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="d-label">问题</span>
            <span class="d-value">{{ currentDetailFaq.question }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">分类</span>
            <span class="d-value">{{ getCategoryName(currentDetailFaq.category) }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">排序</span>
            <span class="d-value">{{ currentDetailFaq.sortOrder }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">状态</span>
            <span class="status-tag" :class="currentDetailFaq.status === 1 ? 'active' : 'inactive'">
              {{ currentDetailFaq.status === 1 ? '启用' : '禁用' }}
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">创建时间</span>
            <span class="d-value">{{ formatDate(currentDetailFaq.createTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">更新时间</span>
            <span class="d-value">{{ formatDate(currentDetailFaq.updateTime) || '--' }}</span>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-label">答案内容</div>
          <div class="detail-answer">{{ currentDetailFaq.answer || '无固定答案（由 AI 回答）' }}</div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="detailVisible = false" class="warm-btn">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getFaqListAdmin, addFaq, updateFaq, updateFaqStatus, deleteFaq, batchDeleteFaq, aiChat } from '@/api/ai';

export default {
  name: 'Admin_AI_FaqManage',
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
      searchForm: { keyword: '', category: '', status: '' },
      dialogVisible: false,
      detailVisible: false,
      isEdit: false,
      currentDetailFaq: null,
      currentFaq: { id: null, question: '', answer: '', category: 'pet_knowledge', sortOrder: 0, status: 1 },
      formRules: {
        question: [{ required: true, message: '请输入问题', trigger: 'blur' }, { min: 2, max: 200, message: '长度在2-200个字符', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }]
      }
    };
  },
  computed: {
    dialogTitle() { return this.isEdit ? '编辑问题' : '新增问题'; },
    enabledCount() { return this.faqList.filter(i => i.status === 1).length; },
    disabledCount() { return this.faqList.filter(i => i.status === 0).length; },
    hasAnswerCount() { return this.faqList.filter(i => i.answer && i.answer.trim()).length; }
  },
  created() { this.loadList(); },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page, pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          category: this.searchForm.category || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined
        };
        const res = await getFaqListAdmin(params);
        if (res.code === 200) { this.faqList = res.data.list; this.total = res.data.total; }
      } catch (e) {
        this.$message.error(e?.message || '加载失败，请稍后重试');
      } finally { this.loading = false; }
    },
    handleView(row) { this.currentDetailFaq = row; this.detailVisible = true; },
    getCategoryName(cat) { const m = { service: '服务咨询', pet_knowledge: '宠物知识', pet_product: '宠物用品', other: '其他' }; return m[cat] || '其他'; },
    getCategoryClass(cat) { const m = { service: 'cat-service', pet_knowledge: 'cat-knowledge', pet_product: 'cat-product', other: 'cat-other' }; return m[cat] || 'cat-other'; },
    async generateAnswerByAI() {
      if (!this.currentFaq.question) { this.$message.warning('请先填写问题'); return; }
      this.aiGenerating = true;
      try {
        const res = await aiChat({ question: this.currentFaq.question });
        if (res.code === 200 && res.data.answer) { this.currentFaq.answer = res.data.answer; this.$message.success('AI答案生成成功'); }
        else { this.$message.error(res.message || 'AI生成失败，请手动输入'); }
      } catch (e) { this.$message.error(e?.message || 'AI生成失败，请手动输入'); }
      finally { this.aiGenerating = false; }
    },
    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() { this.searchForm = { keyword: '', category: '', status: '' }; this.page = 1; this.loadList(); },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    async handleStatusChange(row, val) {
      try {
        const res = await updateFaqStatus(row.id, val ? 1 : 0);
        if (res.code === 200) { row.status = val ? 1 : 0; this.$message.success(res.message || '操作成功'); }
        else { this.$message.error(res.message || '状态更新失败，请稍后重试'); }
      } catch (e) { this.$message.error(e?.message || '状态更新失败，请稍后重试'); }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除问题 "${row.question}" 吗？`, '提示', { type: 'warning' });
        const res = await deleteFaq(row.id);
        if (res.code === 200) { this.$message.success('删除成功'); this.loadList(); }
        else { this.$message.error(res.message || '删除失败'); }
      } catch (e) { if (e !== 'cancel') this.$message.error(e?.message || '删除失败，请稍后重试'); }
    },
    async batchUpdateStatus(status) {
      if (this.selectedRows.length === 0) return;
      const label = status === 1 ? '启用' : '禁用';
      try {
        await this.$confirm(`确定要${label}选中的 ${this.selectedRows.length} 个问题吗？`, '提示', { type: status === 1 ? 'info' : 'warning' });
        for (const row of this.selectedRows) { await updateFaqStatus(row.id, status); }
        this.$message.success(`批量${label}成功`);
        this.selectedRows = []; this.loadList();
      } catch (e) { if (e !== 'cancel') this.$message.error(e?.message || `批量${label}失败，请稍后重试`); }
    },
    handleBatchEnable() { this.batchUpdateStatus(1); },
    handleBatchDisable() { this.batchUpdateStatus(0); },
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个问题吗？`, '警告', { type: 'warning' });
        const res = await batchDeleteFaq(ids);
        if (res.code === 200) { this.$message.success(res.message || '批量删除成功'); this.selectedRows = []; this.loadList(); }
        else { this.$message.error(res.message || '操作失败'); }
      } catch (e) { if (e !== 'cancel') this.$message.error(e?.message || '批量删除失败，请稍后重试'); }
    },
    handleAdd() {
      this.isEdit = false;
      this.currentFaq = { id: null, question: '', answer: '', category: 'pet_knowledge', sortOrder: 0, status: 1 };
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.faqForm && this.$refs.faqForm.clearValidate(); });
    },
    handleEdit(row) {
      this.isEdit = true;
      this.currentFaq = { id: row.id, question: row.question, answer: row.answer || '', category: row.category || 'pet_knowledge', sortOrder: row.sortOrder, status: row.status };
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.faqForm && this.$refs.faqForm.clearValidate(); });
    },
    submitForm() {
      this.$refs.faqForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) { res = await updateFaq(this.currentFaq); }
          else { res = await addFaq(this.currentFaq); }
          if (res.code === 200) { this.$message.success(res.message || (this.isEdit ? '更新成功' : '添加成功')); this.dialogVisible = false; this.loadList(); }
          else { this.$message.error(res.message || '操作失败'); }
        } catch (e) { this.$message.error(e?.message || (this.isEdit ? '更新失败，请稍后重试' : '添加失败，请稍后重试')); }
        finally { this.submitLoading = false; }
      });
    }
  }
};
</script>

<style scoped>
.faq-page {
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
.card-total .stat-icon    { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-active .stat-icon   { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-disabled .stat-icon { background: linear-gradient(135deg, #c0c4cc, #909399); }
.card-answer .stat-icon   { background: linear-gradient(135deg, #409eff, #66b1ff); }
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
.filter-select { width: 120px; }
.filter-select ::v-deep .el-input__inner { border-radius: 10px; }
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
.batch-btn-enable { background: rgba(103,194,58,0.15) !important; border: 1px solid rgba(103,194,58,0.35) !important; color: #67c23a !important; }
.batch-btn-enable:hover { background: rgba(103,194,58,0.25) !important; border-color: #67c23a !important; }
.batch-btn-disable { background: rgba(245,158,75,0.15) !important; border: 1px solid rgba(245,158,75,0.35) !important; color: #f59e4b !important; }
.batch-btn-disable:hover { background: rgba(245,158,75,0.25) !important; border-color: #f59e4b !important; }
.batch-btn-delete { background: rgba(245,108,108,0.12) !important; border: 1px solid rgba(245,108,108,0.35) !important; color: #f56c6c !important; }
.batch-btn-delete:hover { background: rgba(245,108,108,0.22) !important; border-color: #f56c6c !important; }
.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

.table-card { background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 2px 12px rgba(61,46,42,0.04); }
.faq-table ::v-deep .el-table__header th {
  background: #fdf8f4; color: #5c4a42; font-weight: 600;
  font-size: 12px; letter-spacing: 0.4px; padding: 15px 0; border-bottom: 2px solid #f5ece6;
}
.faq-table ::v-deep .el-table__body td { padding: 15px 0; font-size: 13px; color: #3d2e2a; }
.faq-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.faq-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.faq-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.faq-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner { background: #f59e4b; border-color: #f59e4b; }

.question-text { font-weight: 500; color: #3d2e2a; }
.cat-tag { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; }
.cat-service   { background: #edf4fd; color: #409eff; }
.cat-knowledge { background: #edf8f0; color: #67c23a; }
.cat-product   { background: #fef5ee; color: #e6a23c; }
.cat-other     { background: #f5f5f5; color: #909399; }
.answer-preview { color: #5c4a42; font-size: 13px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }

.pagination-card { background: #fff; border-radius: 16px; margin-top: 16px; padding: 16px 24px; display: flex; justify-content: flex-end; box-shadow: 0 2px 12px rgba(61,46,42,0.04); }

.faq-dialog ::v-deep .el-dialog, .detail-dialog ::v-deep .el-dialog { border-radius: 20px; overflow: hidden; }
.faq-dialog ::v-deep .el-dialog__header, .detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a); padding: 22px 28px; margin: 0;
}
.faq-dialog ::v-deep .el-dialog__title, .detail-dialog ::v-deep .el-dialog__title { color: #fff; font-weight: 700; font-size: 17px; }
.faq-dialog ::v-deep .el-dialog__close, .detail-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.faq-dialog ::v-deep .el-dialog__close:hover, .detail-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.faq-dialog ::v-deep .el-dialog__body, .detail-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }
.ai-row { display: flex; gap: 10px; align-items: flex-start; }
.ai-btn { border-radius: 10px; padding: 10px 16px; white-space: nowrap; flex-shrink: 0; }
.form-tip { font-size: 12px; color: #a08c84; margin-top: 4px; }
.form-tip-inline { font-size: 12px; color: #a08c84; margin-left: 10px; }

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
.d-label { width: 68px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; font-weight: 500; }
.status-tag { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-tag.active { background: #edf7ee; color: #67c23a; }
.status-tag.inactive { background: #fef0f0; color: #f56c6c; }
.section-label { font-size: 13px; font-weight: 600; color: #5c4a42; margin-bottom: 10px; }
.detail-answer { background: #fdf8f4; padding: 16px; border-radius: 12px; color: #5c4a42; line-height: 1.8; font-size: 14px; white-space: pre-wrap; word-break: break-word; }

.dialog-footer { text-align: right; padding: 16px 28px 22px; border-top: 1px solid #f5ece6; }
.dialog-footer .el-button { border-radius: 10px; padding: 10px 28px; font-weight: 500; font-size: 13px; }
.dialog-footer .el-button--primary { background: linear-gradient(135deg, #f59e4b, #f0826a) !important; border: none !important; color: #fff !important; }
.dialog-footer .el-button--primary:hover { opacity: 0.88; transform: translateY(-1px); }

@media (max-width: 768px) {
  .faq-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 100%; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-item { border-right: none; }
  .detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }
  .detail-item:last-child { border-bottom: none; }
}
</style>
