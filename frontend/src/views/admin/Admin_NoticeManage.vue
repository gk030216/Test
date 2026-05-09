<template>
  <div class="notice-page">
    <!-- 统计概览卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-document"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">公告总数</div>
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-sub">平台公告总量</div>
        </div>
      </div>
      <div class="stat-card card-active">
        <div class="stat-icon">
          <i class="el-icon-check"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">已启用</div>
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-sub">前台正常展示</div>
        </div>
      </div>
      <div class="stat-card card-disabled">
        <div class="stat-icon">
          <i class="el-icon-close"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">已禁用</div>
          <div class="stat-value">{{ stats.disabled }}</div>
          <div class="stat-sub">前台不展示</div>
        </div>
      </div>
      <div class="stat-card card-top">
        <div class="stat-icon">
          <i class="el-icon-top"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">已置顶</div>
          <div class="stat-value">{{ stats.topCount }}</div>
          <div class="stat-sub">优先展示</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.title"
          placeholder="搜索公告标题..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.type"
          placeholder="公告类型"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="服务公告" :value="1" />
          <el-option label="商城公告" :value="2" />
          <el-option label="社区公告" :value="3" />
          <el-option label="系统公告" :value="4" />
          <el-option label="其他公告" :value="5" />
        </el-select>
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
          <i class="el-icon-plus" /> 新增公告
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

    <!-- 公告表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="noticeList"
        stripe
        row-key="id"
        class="notice-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="公告标题" min-width="220">
          <template slot-scope="scope">
            <div class="notice-cell">
              <span class="notice-title">{{ scope.row.title }}</span>
              <span class="notice-summary">{{ truncateText(scope.row.content, 50) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="110" align="center">
          <template slot-scope="scope">
            <span :class="['type-tag', typeClass(scope.row.type)]">
              {{ typeText(scope.row.type) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="置顶" width="80" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.isTop === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleToggleTop(scope.row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.status === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleToggleStatus(scope.row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column prop="sortOrder" label="排序" width="100" align="center">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.sortOrder"
              :min="0"
              :max="999"
              size="small"
              controls-position="right"
              @change="handleUpdateSort(scope.row)"
              class="sort-input"
            />
          </template>
        </el-table-column>

        <el-table-column prop="viewCount" label="浏览" width="80" align="center">
          <template slot-scope="scope">
            <span class="view-count">{{ scope.row.viewCount || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleView(scope.row)" class="act-view">
                <i class="el-icon-view" /> 预览
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
      width="700px"
      :close-on-click-modal="false"
      class="notice-dialog"
      center
    >
      <div class="dialog-body">
        <el-form :model="form" :rules="rules" ref="noticeForm" label-width="85px" class="notice-form">
          <el-form-item label="公告标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="公告类型" prop="type">
                <el-select v-model="form.type" placeholder="请选择公告类型" style="width:100%">
                  <el-option label="服务公告" :value="1" />
                  <el-option label="商城公告" :value="2" />
                  <el-option label="社区公告" :value="3" />
                  <el-option label="系统公告" :value="4" />
                  <el-option label="其他公告" :value="5" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="排序序号">
                <el-input-number v-model="form.sortOrder" :min="0" :max="999" controls-position="right" />
                <span class="form-tip-inline">数字越小越靠前</span>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="置顶">
                <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
                <span class="form-tip-inline">置顶公告显示在最前面</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态">
                <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
                <span class="form-tip-inline">禁用后前台不显示</span>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="公告内容" prop="content">
            <div class="editor-toolbar">
              <el-button size="mini" @click="insertHtmlTag('p')">段落</el-button>
              <el-button size="mini" @click="insertHtmlTag('strong')">加粗</el-button>
              <el-button size="mini" @click="insertHtmlTag('ul')">列表</el-button>
              <el-button size="mini" @click="insertHtmlTag('li')">列表项</el-button>
              <el-button size="mini" @click="insertHtmlTag('br')">换行</el-button>
            </div>
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="10"
              placeholder="请输入公告内容，支持HTML格式"
              maxlength="10000"
              show-word-limit
            />
            <p class="form-tip-block">
              <i class="el-icon-info" /> 支持HTML标签，如：&lt;p&gt;、&lt;strong&gt;、&lt;ul&gt;、&lt;li&gt;等
            </p>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">确定</el-button>
      </span>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      :title="previewNotice.title"
      :visible.sync="previewVisible"
      width="600px"
      center
      class="preview-dialog"
    >
      <div class="preview-content">
        <div class="preview-header">
          <div class="preview-meta">
            <span :class="['type-tag', typeClass(previewNotice.type)]">
              {{ typeText(previewNotice.type) }}
            </span>
            <span><i class="el-icon-date" /> {{ formatDate(previewNotice.createTime) }}</span>
            <span><i class="el-icon-view" /> {{ previewNotice.viewCount || 0 }} 次阅读</span>
          </div>
        </div>
        <div class="preview-body" v-html="previewNotice.content"></div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="previewVisible = false" class="warm-btn">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminNoticePage,
  addAdminNotice,
  updateAdminNotice,
  deleteAdminNotice,
  batchDeleteAdminNotice,
  toggleAdminNoticeTop,
  toggleAdminNoticeStatus,
  updateAdminNoticeSort
} from '@/api/consultationNotice';

export default {
  name: 'Admin_NoticeManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      noticeList: [],
      selectedRows: [],
      total: 0,
      page: 1,
      pageSize: 10,
      searchForm: { title: '', type: null, status: null },
      dialogVisible: false,
      previewVisible: false,
      previewNotice: {},
      stats: { total: 0, active: 0, disabled: 0, topCount: 0 },
      form: {
        id: null, title: '', type: 1, content: '',
        sortOrder: 0, isTop: 0, status: 1
      },
      rules: {
        title: [
          { required: true, message: '请输入公告标题', trigger: 'blur' },
          { min: 2, max: 200, message: '长度在2-200个字符', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择公告类型', trigger: 'change' }
        ],
        content: [
          { required: true, message: '请输入公告内容', trigger: 'blur' },
          { min: 10, message: '内容至少10个字符', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.form.id ? '编辑公告' : '新增公告';
    }
  },
  created() {
    this.loadList();
    this.computeStats();
  },
  methods: {
    async computeStats() {
      try {
        const res = await getAdminNoticePage({ pageNum: 1, pageSize: 500 });
        if (res.code === 200) {
          const list = res.data.list || [];
          this.stats = {
            total: res.data.total || 0,
            active: list.filter(i => i.status === 1).length,
            disabled: list.filter(i => i.status === 0).length,
            topCount: list.filter(i => i.isTop === 1).length
          };
        }
      } catch { /* stats fetch silently fails */ }
    },
    async loadList() {
      this.loading = true;
      try {
        const res = await getAdminNoticePage({
          pageNum: this.page,
          pageSize: this.pageSize,
          title: this.searchForm.title || null,
          type: this.searchForm.type,
          status: this.searchForm.status
        });
        if (res.code === 200) {
          this.noticeList = res.data.list || [];
          this.total = res.data.total || 0;
        } else {
          this.$message.error(res.message || '加载失败');
        }
      } catch {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() {
      this.searchForm = { title: '', type: null, status: null };
      this.page = 1;
      this.loadList();
    },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    handleAdd() {
      this.form = { id: null, title: '', type: 1, content: '', sortOrder: 0, isTop: 0, status: 1 };
      this.dialogVisible = true;
      this.$nextTick(() => { if (this.$refs.noticeForm) this.$refs.noticeForm.clearValidate(); });
    },

    handleEdit(row) {
      this.form = {
        id: row.id, title: row.title, type: row.type, content: row.content,
        sortOrder: row.sortOrder, isTop: row.isTop, status: row.status
      };
      this.dialogVisible = true;
      this.$nextTick(() => { if (this.$refs.noticeForm) this.$refs.noticeForm.clearValidate(); });
    },

    handleView(row) {
      this.previewNotice = row;
      this.previewVisible = true;
    },

    async handleDelete(row) {
      try {
        await this.$confirm(`确定删除公告 "${row.title}" 吗？`, '提示', { type: 'warning' });
        const res = await deleteAdminNotice(row.id);
        if (res.code === 200) {
          this.$message.success('删除成功');
          this.loadList();
          this.computeStats();
        } else {
          this.$message.error(res.message || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败');
      }
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      try {
        await this.$confirm(`确定删除选中的 ${this.selectedRows.length} 条公告吗？`, '提示', { type: 'warning' });
        const ids = this.selectedRows.map(r => r.id);
        const res = await batchDeleteAdminNotice(ids);
        if (res.code === 200) {
          this.$message.success(res.message || '删除成功');
          this.selectedRows = [];
          this.loadList();
          this.computeStats();
        } else {
          this.$message.error(res.message || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('批量删除失败');
      }
    },

    async handleToggleTop(row, val) {
      const isTop = val ? 1 : 0;
      try {
        const res = await toggleAdminNoticeTop(row.id, isTop);
        if (res.code === 200) {
          row.isTop = isTop;
          this.$message.success(isTop === 1 ? '已置顶' : '已取消置顶');
          this.computeStats();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch {
        this.$message.error('操作失败');
      }
    },

    async handleToggleStatus(row, val) {
      const status = val ? 1 : 0;
      try {
        const res = await toggleAdminNoticeStatus(row.id, status);
        if (res.code === 200) {
          row.status = status;
          this.$message.success(status === 1 ? '已启用' : '已禁用');
          this.computeStats();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch {
        this.$message.error('操作失败');
      }
    },

    async handleUpdateSort(row) {
      try {
        const res = await updateAdminNoticeSort(row.id, row.sortOrder);
        if (res.code === 200) {
          this.$message.success('排序已更新');
        } else {
          this.$message.error(res.message || '更新失败');
          this.loadList();
        }
      } catch {
        this.$message.error('更新排序失败');
        this.loadList();
      }
    },

    insertHtmlTag(tag) {
      const map = {
        p: ['<p>', '</p>'],
        strong: ['<strong>', '</strong>'],
        ul: ['<ul>\n  <li>', '</li>\n</ul>'],
        li: ['<li>', '</li>'],
        br: ['<br>', '']
      };
      const [startTag, endTag] = map[tag] || ['', ''];
      if (!startTag && !endTag) return;
      const textarea = this.$refs.noticeForm.$el.querySelector('textarea');
      if (!textarea) return;
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const text = this.form.content;
      const selectedText = text.substring(start, end);
      this.form.content = text.substring(0, start) + startTag + selectedText + endTag + text.substring(end);
      this.$nextTick(() => {
        textarea.focus();
        textarea.setSelectionRange(start + startTag.length, end + startTag.length);
      });
    },

    submitForm() {
      this.$refs.noticeForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          const res = this.form.id
            ? await updateAdminNotice(this.form)
            : await addAdminNotice(this.form);
          if (res.code === 200) {
            this.$message.success(this.form.id ? '更新成功' : '添加成功');
            this.dialogVisible = false;
            this.loadList();
            this.computeStats();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch {
          this.$message.error('操作失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    typeText(type) {
      const map = { 1: '服务公告', 2: '商城公告', 3: '社区公告', 4: '系统公告', 5: '其他公告' };
      return map[type] || '公告';
    },
    typeClass(type) {
      const map = { 1: 'type-service', 2: 'type-shop', 3: 'type-community', 4: 'type-system', 5: 'type-other' };
      return map[type] || 'type-system';
    },
    truncateText(text, length) {
      if (!text) return '';
      const plainText = text.replace(/<[^>]+>/g, '');
      return plainText.length <= length ? plainText : plainText.substring(0, length) + '...';
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.notice-page {
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
.card-total .stat-icon    { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-active .stat-icon   { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-disabled .stat-icon { background: linear-gradient(135deg, #c0c4cc, #909399); }
.card-top .stat-icon      { background: linear-gradient(135deg, #409eff, #66b1ff); }

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
.keyword-input { width: 220px; }
.keyword-input ::v-deep .el-input__inner { border-radius: 10px; }
.status-select { width: 120px; }
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
  border-radius: 8px; padding: 7px 16px;
  font-weight: 500; transition: all 0.2s;
}
.batch-btn-delete {
  background: rgba(245, 108, 108, 0.12) !important;
  border: 1px solid rgba(245, 108, 108, 0.35) !important;
  color: #f56c6c !important;
}
.batch-btn-delete:hover { background: rgba(245, 108, 108, 0.22) !important; border-color: #f56c6c !important; }
.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.notice-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.notice-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.notice-table ::v-deep .el-table__body tr:hover > td {
  background: #fefbf8;
}
.notice-table ::v-deep .el-table__body tr.el-table__row--striped > td {
  background: #fefcf9;
}
.notice-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td {
  background: #fefbf8;
}
.notice-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b;
  border-color: #f59e4b;
}

.notice-cell { display: flex; flex-direction: column; gap: 4px; }
.notice-title { font-weight: 600; color: #3d2e2a; font-size: 13px; }
.notice-summary { font-size: 12px; color: #a08c84; }

.type-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.type-service   { background: #f0f9eb; color: #67c23a; }
.type-shop      { background: #fdf6ec; color: #e6a23c; }
.type-community { background: #ecf5ff; color: #409eff; }
.type-system    { background: #f4f4f5; color: #909399; }
.type-other     { background: #fef0f0; color: #f56c6c; }

.sort-input { width: 100px; }
.sort-input ::v-deep .el-input__inner { text-align: center; }
.view-count { color: #409eff; font-weight: 500; }
.cell-time { color: #a08c84; font-size: 12px; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
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

/* ===== 对话框 ===== */
.notice-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.notice-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.notice-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.notice-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.notice-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.notice-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 60vh; overflow-y: auto; padding-right: 4px; }

.notice-form ::v-deep .el-form-item { margin-bottom: 18px; }
.notice-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.notice-form ::v-deep .el-input__inner,
.notice-form ::v-deep .el-textarea__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.notice-form ::v-deep .el-input__inner:focus,
.notice-form ::v-deep .el-textarea__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}
.notice-form ::v-deep .el-textarea__inner { font-family: monospace; }

.editor-toolbar {
  margin-bottom: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.editor-toolbar .el-button {
  border-radius: 6px;
  font-size: 12px;
  padding: 5px 12px;
}

.form-tip-inline { font-size: 12px; color: #b8a8a0; margin-left: 8px; }
.form-tip-block { font-size: 12px; color: #b8a8a0; margin-top: 6px; }

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

/* ===== 预览对话框 ===== */
.preview-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.preview-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.preview-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.preview-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.preview-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.preview-dialog ::v-deep .el-dialog__body { padding: 28px; }

.preview-content { max-height: 50vh; overflow-y: auto; }
.preview-header {
  text-align: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5ece6;
  margin-bottom: 20px;
}
.preview-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 13px;
  color: #a08c84;
  flex-wrap: wrap;
}
.preview-meta i { margin-right: 4px; }
.preview-body {
  font-size: 14px;
  line-height: 1.8;
  color: #5c4a42;
}
.preview-body ::v-deep p { margin-bottom: 12px; }
.preview-body ::v-deep ul,
.preview-body ::v-deep ol { padding-left: 24px; margin-bottom: 12px; }
.preview-body ::v-deep li { margin-bottom: 6px; }

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .notice-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 100%; }
  .preview-meta { flex-direction: column; align-items: center; gap: 8px; }
}
</style>
