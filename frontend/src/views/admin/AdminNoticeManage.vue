<template>
  <div class="admin-notice-manage">
    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增公告
        </el-button>
        <el-button v-if="selectedRows.length > 0" type="danger" plain @click="handleBatchDelete" class="batch-btn">
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input v-model="searchForm.title" placeholder="搜索公告标题" clearable size="medium" @keyup.enter="handleSearch" class="search-input"></el-input>
        </div>
        <el-select v-model="searchForm.type" placeholder="公告类型" clearable size="medium" class="status-select" @change="handleSearch">
          <el-option label="服务公告" :value="1"></el-option>
          <el-option label="商城公告" :value="2"></el-option>
          <el-option label="社区公告" :value="3"></el-option>
          <el-option label="系统公告" :value="4"></el-option>
          <el-option label="其他公告" :value="5"></el-option>
        </el-select>
        <el-select v-model="searchForm.status" placeholder="状态" clearable size="medium" class="status-select" @change="handleSearch">
          <el-option label="启用" :value="1"></el-option>
          <el-option label="禁用" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 公告表格 -->
    <el-table v-loading="loading" :data="noticeList" stripe class="notice-table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column label="公告标题" min-width="220" show-overflow-tooltip>
        <template slot-scope="scope">
          <div class="notice-title">{{ scope.row.title }}</div>
          <div class="notice-summary">{{ truncateText(scope.row.content, 50) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="公告类型" width="110" align="center">
        <template slot-scope="scope">
          <span :class="['notice-type-tag', getNoticeTypeClass(scope.row.type)]">
            {{ getNoticeTypeText(scope.row.type) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
              v-model="scope.row.isTop"
              :active-value="1"
              :inactive-value="0"
              @change="handleToggleTop(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleToggleStatus(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="排序" width="120" align="center">
        <template slot-scope="scope">
          <el-input-number
              v-model="scope.row.sortOrder"
              :min="0"
              :max="999"
              size="small"
              controls-position="right"
              @change="handleUpdateSort(scope.row)"
              class="sort-input"
          ></el-input-number>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览次数" width="100" align="center">
        <template slot-scope="scope">
          <span class="view-count">{{ scope.row.viewCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" align="center">
        <template slot-scope="scope">{{ formatDate(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button size="small" type="primary" plain circle class="action-icon-btn" @click="handleView(scope.row)" title="预览">
              <i class="el-icon-view"></i>
            </el-button>
            <el-button size="small" type="warning" plain circle class="action-icon-btn" @click="handleEdit(scope.row)" title="编辑">
              <i class="el-icon-edit"></i>
            </el-button>
            <el-button size="small" type="danger" plain circle class="action-icon-btn" @click="handleDelete(scope.row)" title="删除">
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
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
      ></el-pagination>
    </div>

    <!-- 新增/编辑公告对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px" center class="notice-dialog" :close-on-click-modal="false">
      <div class="dialog-content">
        <el-form :model="form" :rules="rules" ref="noticeForm" label-width="85px" class="notice-form">
          <el-row :gutter="16">
            <el-col :span="24">
              <el-form-item label="公告标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit size="medium"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="公告类型" prop="type">
                <el-select v-model="form.type" placeholder="请选择公告类型" size="medium" style="width: 100%">
                  <el-option label="服务公告" :value="1"></el-option>
                  <el-option label="商城公告" :value="2"></el-option>
                  <el-option label="社区公告" :value="3"></el-option>
                  <el-option label="系统公告" :value="4"></el-option>
                  <el-option label="其他公告" :value="5"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="排序序号" prop="sortOrder">
                <el-input-number v-model="form.sortOrder" :min="0" :max="999" controls-position="right" size="medium" class="sort-input-dialog"></el-input-number>
                <span class="form-tip">数字越小越靠前</span>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="置顶" prop="isTop">
                <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0"></el-switch>
                <span class="form-tip">置顶公告显示在最前面</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态" prop="status">
                <el-switch v-model="form.status" :active-value="1" :inactive-value="0"></el-switch>
                <span class="form-tip">禁用后前台不显示</span>
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
                size="medium"
            ></el-input>
            <div class="form-tip">
              <i class="el-icon-info"></i> 支持HTML标签，如：&lt;p&gt;、&lt;strong&gt;、&lt;ul&gt;、&lt;li&gt;等
            </div>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium">确定</el-button>
      </span>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog :title="previewNotice.title" :visible.sync="previewVisible" width="600px" center class="preview-dialog">
      <div class="preview-content">
        <div class="preview-header">
          <div class="preview-meta">
            <span :class="['notice-type-tag', getNoticeTypeClass(previewNotice.type)]">
              {{ getNoticeTypeText(previewNotice.type) }}
            </span>
            <span><i class="el-icon-date"></i> {{ formatDate(previewNotice.createTime, 'full') }}</span>
            <span><i class="el-icon-view"></i> {{ previewNotice.viewCount || 0 }} 次阅读</span>
          </div>
        </div>
        <div class="preview-body" v-html="previewNotice.content"></div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="previewVisible = false">关闭</el-button>
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
  updateAdminNoticeSort,
  getAdminNoticeDetail
} from '@/api/consultationNotice';

export default {
  name: 'AdminNoticeManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      noticeList: [],
      selectedRows: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      searchForm: {
        title: '',
        type: null,
        status: null
      },
      dialogVisible: false,
      dialogTitle: '新增公告',
      previewVisible: false,
      previewNotice: {},
      form: {
        id: null,
        title: '',
        type: 1,
        content: '',
        sortOrder: 0,
        isTop: 0,
        status: 1
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
  created() {
    this.loadNoticeList();
  },
  methods: {
    async loadNoticeList() {
      this.loading = true;
      try {
        const res = await getAdminNoticePage({
          pageNum: this.pageNum,
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
      } catch (error) {
        console.error('加载公告列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    handleSearch() {
      this.pageNum = 1;
      this.loadNoticeList();
    },

    handleReset() {
      this.searchForm = {
        title: '',
        type: null,
        status: null
      };
      this.pageNum = 1;
      this.loadNoticeList();
    },

    handlePageChange(page) {
      this.pageNum = page;
      this.loadNoticeList();
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.pageNum = 1;
      this.loadNoticeList();
    },

    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },

    handleAdd() {
      this.dialogTitle = '新增公告';
      this.form = {
        id: null,
        title: '',
        type: 1,
        content: '',
        sortOrder: 0,
        isTop: 0,
        status: 1
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.noticeForm) {
          this.$refs.noticeForm.clearValidate();
        }
      });
    },

    handleEdit(row) {
      this.dialogTitle = '编辑公告';
      this.form = {
        id: row.id,
        title: row.title,
        type: row.type,
        content: row.content,
        sortOrder: row.sortOrder,
        isTop: row.isTop,
        status: row.status
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.noticeForm) {
          this.$refs.noticeForm.clearValidate();
        }
      });
    },

    async handleView(row) {
      try {
        const res = await getAdminNoticeDetail(row.id);
        if (res.code === 200) {
          this.previewNotice = res.data;
          this.previewVisible = true;
        } else {
          this.$message.error('获取预览内容失败');
        }
      } catch (error) {
        this.$message.error('获取预览内容失败');
      }
    },

    async handleDelete(row) {
      this.$confirm(`确定删除公告 "${row.title}" 吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await deleteAdminNotice(row.id);
        if (res.code === 200) {
          this.$message.success('删除成功');
          this.loadNoticeList();
        } else {
          this.$message.error(res.message || '删除失败');
        }
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要删除的公告');
        return;
      }
      this.$confirm(`确定删除选中的 ${this.selectedRows.length} 条公告吗？`, '提示', { type: 'warning' }).then(async () => {
        const ids = this.selectedRows.map(row => row.id);
        const res = await batchDeleteAdminNotice(ids);
        if (res.code === 200) {
          this.$message.success(res.message || '删除成功');
          this.selectedRows = [];
          this.loadNoticeList();
        } else {
          this.$message.error(res.message || '删除失败');
        }
      }).catch(() => {});
    },

    async handleToggleTop(row) {
      const oldValue = row.isTop;
      try {
        const res = await toggleAdminNoticeTop(row.id, row.isTop);
        if (res.code === 200) {
          this.$message.success(res.message || (row.isTop === 1 ? '已置顶' : '已取消置顶'));
          this.loadNoticeList();
        } else {
          row.isTop = oldValue;
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        row.isTop = oldValue;
        this.$message.error('操作失败');
      }
    },

    async handleToggleStatus(row) {
      const oldValue = row.status;
      try {
        const res = await toggleAdminNoticeStatus(row.id, row.status);
        if (res.code === 200) {
          this.$message.success(res.message || (row.status === 1 ? '已启用' : '已禁用'));
        } else {
          row.status = oldValue;
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        row.status = oldValue;
        this.$message.error('操作失败');
      }
    },

    async handleUpdateSort(row) {
      try {
        const res = await updateAdminNoticeSort(row.id, row.sortOrder);
        if (res.code === 200) {
          this.$message.success('排序已更新');
        } else {
          this.$message.error(res.message || '更新排序失败');
          this.loadNoticeList();
        }
      } catch (error) {
        this.$message.error('更新排序失败');
        this.loadNoticeList();
      }
    },

    insertHtmlTag(tag) {
      let startTag = '';
      let endTag = '';
      switch (tag) {
        case 'p':
          startTag = '<p>';
          endTag = '</p>';
          break;
        case 'strong':
          startTag = '<strong>';
          endTag = '</strong>';
          break;
        case 'ul':
          startTag = '<ul>\n  <li>';
          endTag = '</li>\n</ul>';
          break;
        case 'li':
          startTag = '<li>';
          endTag = '</li>';
          break;
        case 'br':
          startTag = '<br>';
          endTag = '';
          break;
        default:
          return;
      }
      const textarea = this.$refs.noticeForm.$el.querySelector('textarea');
      if (!textarea) return;
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const text = this.form.content;
      const selectedText = text.substring(start, end);
      const newText = text.substring(0, start) + startTag + selectedText + endTag + text.substring(end);
      this.form.content = newText;
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
          let res;
          if (this.form.id) {
            res = await updateAdminNotice(this.form);
          } else {
            res = await addAdminNotice(this.form);
          }

          if (res.code === 200) {
            this.$message.success(this.form.id ? '更新成功' : '添加成功');
            this.dialogVisible = false;
            this.loadNoticeList();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          console.error('提交失败', error);
          this.$message.error('操作失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    getNoticeTypeText(type) {
      const typeMap = {
        1: '服务公告',
        2: '商城公告',
        3: '社区公告',
        4: '系统公告',
        5: '其他公告'
      };
      return typeMap[type] || '公告';
    },

    getNoticeTypeClass(type) {
      const typeMap = {
        1: 'type-service',
        2: 'type-shop',
        3: 'type-community',
        4: 'type-system',
        5: 'type-other'
      };
      return typeMap[type] || 'type-system';
    },

    truncateText(text, length) {
      if (!text) return '';
      const plainText = text.replace(/<[^>]+>/g, '');
      if (plainText.length <= length) return plainText;
      return plainText.substring(0, length) + '...';
    },

    formatDate(date, format = 'simple') {
      if (!date) return '';
      const d = new Date(date);
      if (format === 'full') {
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
      }
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.admin-notice-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 操作栏 */
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
  align-items: center;
}

.action-right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.add-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
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
  width: 200px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.status-select {
  width: 110px;
}

.status-select ::v-deep .el-input__inner {
  border-radius: 8px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.search-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.reset-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

/* 表格 */
.notice-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.notice-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f6;
}

.notice-table ::v-deep .el-table__body td {
  padding: 14px 0;
  font-size: 13px;
}

.notice-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
  font-size: 14px;
}

.notice-summary {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

/* 公告类型标签 */
.notice-type-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.type-system {
  background: #ecf5ff;
  color: #409EFF;
}

.type-activity {
  background: #fdf6ec;
  color: #E6A23C;
}

.type-service {
  background: #f0f9eb;
  color: #67C23A;
}

.sort-input {
  width: 100px;
}

.sort-input ::v-deep .el-input__inner {
  text-align: center;
}

.view-count {
  color: #409EFF;
  font-weight: 500;
}

/* 操作按钮 */
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

/* 分页 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 对话框样式 */
.notice-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.notice-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.notice-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.notice-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.notice-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

/* 表单 */
.notice-form {
  margin-top: 8px;
}

.notice-form ::v-deep .el-form-item {
  margin-bottom: 18px;
}

.notice-form ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 13px;
}

.notice-form ::v-deep .el-input__inner {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.notice-form ::v-deep .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.notice-form ::v-deep .el-textarea__inner {
  border-radius: 8px;
  font-family: monospace;
  font-size: 13px;
}

.sort-input-dialog {
  width: 120px;
}

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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

/* 对话框底部 */
.dialog-footer {
  text-align: right;
  padding: 16px 24px 20px;
  border-top: 1px solid #eef2f6;
}

.dialog-footer .el-button {
  border-radius: 8px;
  padding: 9px 24px;
  font-weight: 500;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.dialog-footer .el-button--primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 预览对话框 */
.preview-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.preview-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
}

.preview-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.preview-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

.preview-content {
  max-height: 50vh;
  overflow-y: auto;
}

.preview-header {
  text-align: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #eef2f6;
  margin-bottom: 20px;
}

.preview-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 13px;
  color: #909399;
  flex-wrap: wrap;
}

.preview-meta i {
  margin-right: 4px;
}

.preview-body {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}

.preview-body p {
  margin-bottom: 12px;
}

.preview-body ul, .preview-body ol {
  padding-left: 24px;
  margin-bottom: 12px;
}

.preview-body li {
  margin-bottom: 6px;
}

/* 响应式 */
@media (max-width: 768px) {
  .admin-notice-manage {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .action-left, .action-right {
    justify-content: center;
  }

  .search-input {
    width: 160px;
  }

  .pagination-wrapper {
    justify-content: center;
  }

  .preview-meta {
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }
}

.type-service {
  background: #f0f9eb;
  color: #67C23A;
}

.type-shop {
  background: #fdf6ec;
  color: #E6A23C;
}

.type-community {
  background: #ecf5ff;
  color: #409EFF;
}

.type-system {
  background: #f4f4f5;
  color: #909399;
}

.type-other {
  background: #fef0f0;
  color: #F56C6C;
}
</style>