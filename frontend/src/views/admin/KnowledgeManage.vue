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
          <el-option label="服务咨询" value="service"></el-option>
          <el-option label="宠物知识" value="pet_knowledge"></el-option>
          <el-option label="宠物用品" value="pet_product"></el-option>
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
          <el-option label="AI生成" :value="2"></el-option>
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
        row-key="id"
    >
      <el-table-column type="selection" width="45" align="center" :reserve-selection="true"></el-table-column>
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
            <el-tag v-for="kw in scope.row.keywords.split(',')" :key="kw" size="small" style="margin: 2px;">{{ kw }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" align="center">
        <template slot-scope="scope">
          <span class="category-tag">{{ scope.row.categoryName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="来源" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.source === 1 ? 'success' : 'primary'" size="small">
            {{ scope.row.source === 1 ? '人工录入' : 'AI生成' }}
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
      <el-table-column label="操作" width="250" fixed="right" align="center">
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
                type="success"
                plain
                circle
                @click="handleConvertToFaq(scope.row)"
                class="action-icon-btn"
                title="转为常见问题"
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
            <div style="display: flex; gap: 10px; align-items: flex-start;">
              <el-input
                  v-model="currentKnowledge.content"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入知识内容"
                  size="medium"
                  style="flex: 1;"
              ></el-input>
              <el-button
                  type="primary"
                  size="medium"
                  @click="generateContentByAI"
                  :loading="aiContentGenerating"
                  :disabled="!currentKnowledge.title"
                  style="margin-top: 0;"
              >
                <i class="el-icon-magic-stick"></i> AI生成
              </el-button>
            </div>
            <div class="form-tip">点击AI生成按钮可根据标题自动生成内容</div>
          </el-form-item>

          <el-form-item label="关键词" prop="keywords">
            <div style="display: flex; gap: 10px;">
              <el-input
                  v-model="currentKnowledge.keywords"
                  placeholder="多个关键词用英文逗号分隔"
                  size="medium"
                  style="flex: 1;"
              ></el-input>
              <el-button
                  type="primary"
                  size="medium"
                  @click="generateKeywordsByAI"
                  :loading="aiGenerating"
                  :disabled="!currentKnowledge.title && !currentKnowledge.content"
              >
                <i class="el-icon-magic-stick"></i> AI生成
              </el-button>
            </div>
            <div class="form-tip">例如：狗,食物,禁忌,中毒 | 点击AI生成按钮可自动根据标题和内容生成关键词</div>
          </el-form-item>

          <el-form-item label="分类" prop="category">
            <el-select v-model="currentKnowledge.category" placeholder="请选择分类" size="medium">
              <el-option label="服务咨询" value="service"></el-option>
              <el-option label="宠物知识" value="pet_knowledge"></el-option>
              <el-option label="宠物用品" value="pet_product"></el-option>
              <el-option label="其他" value="other"></el-option>
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
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 知识详情对话框 -->
    <el-dialog
        title="知识详情"
        :visible.sync="detailVisible"
        width="650px"
        center
        class="knowledge-detail-dialog"
    >
      <div class="detail-content" v-if="currentDetailKnowledge">
        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="24">
              <div class="detail-item">
                <span class="detail-label">标题：</span>
                <span class="detail-value">{{ currentDetailKnowledge.title }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">分类：</span>
                <span class="detail-value">{{ currentDetailKnowledge.categoryName }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">来源：</span>
                <el-tag :type="currentDetailKnowledge.source === 1 ? 'success' : 'primary'" size="small">
                  {{ currentDetailKnowledge.source === 1 ? '人工录入' : 'AI生成' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">状态：</span>
                <el-tag :type="currentDetailKnowledge.status === 1 ? 'success' : 'danger'" size="small">
                  {{ currentDetailKnowledge.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">使用次数：</span>
                <span class="detail-value">{{ currentDetailKnowledge.useCount || 0 }}</span>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="detail-item">
                <span class="detail-label">关键词：</span>
                <div class="keywords-detail">
                  <el-tag v-for="kw in (currentDetailKnowledge.keywords || '').split(',')" :key="kw" size="small" style="margin: 2px;">
                    {{ kw }}
                  </el-tag>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 内容信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-document"></i>
            <span>内容详情</span>
          </div>
          <div class="detail-content-text">{{ currentDetailKnowledge.content }}</div>
        </div>

        <!-- 时间信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-time"></i>
            <span>时间信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">创建时间：</span>
                <span class="detail-value">{{ formatDate(currentDetailKnowledge.createTime) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">更新时间：</span>
                <span class="detail-value">{{ formatDate(currentDetailKnowledge.updateTime) || '--' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
    <el-button @click="detailVisible = false">关闭</el-button>
  </span>
    </el-dialog>
  </div>
</template>

<script>
import { getKnowledgeList, addKnowledge, updateKnowledge, updateKnowledgeStatus, deleteKnowledge, batchDeleteKnowledge,convertKnowledgeToFaq    } from '@/api/ai';
import request from '@/utils/request';

export default {
  name: 'KnowledgeManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      aiGenerating: false,
      detailVisible: false,
      currentDetailKnowledge: null,
      aiContentGenerating: false,
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
        source: 1,  // 新增的知识默认为人工录入
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
          pageSize: this.pageSize
        };

        // 关键词
        if (this.searchForm.keyword && this.searchForm.keyword.trim()) {
          params.keyword = this.searchForm.keyword.trim();
        }

        // 分类
        if (this.searchForm.category && this.searchForm.category !== '') {
          params.category = this.searchForm.category;
        }

        // 来源
        if (this.searchForm.source !== '' && this.searchForm.source !== null && this.searchForm.source !== undefined) {
          params.source = this.searchForm.source;
        }

        // 状态 - 修复：status 可能为 0，不能使用 || 判断
        if (this.searchForm.status !== '' && this.searchForm.status !== null && this.searchForm.status !== undefined) {
          params.status = this.searchForm.status;
        }

        console.log('请求参数:', params);

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
    // 查看知识详情
    handleView(row) {
      this.currentDetailKnowledge = row;
      this.detailVisible = true;
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
        source: 1,  // 新增的知识默认为人工录入
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

    // AI生成关键词
    async generateKeywordsByAI() {
      // 验证标题和内容是否填写
      if (!this.currentKnowledge.title && !this.currentKnowledge.content) {
        this.$message.warning('请先填写标题或内容，以便AI生成关键词');
        return;
      }

      this.aiGenerating = true;
      try {
        const res = await request({
          url: '/admin/ai/generate-keywords',
          method: 'post',
          data: {
            question: this.currentKnowledge.title || this.currentKnowledge.content,
            answer: this.currentKnowledge.content
          }
        });

        if (res.code === 200 && res.data.keywords) {
          this.currentKnowledge.keywords = res.data.keywords;
          this.$message.success('AI关键词生成成功');
        } else {
          this.$message.error('AI生成失败，请手动输入');
        }
      } catch (error) {
        console.error('AI生成关键词失败', error);
        this.$message.error('AI生成失败，请手动输入');
      } finally {
        this.aiGenerating = false;
      }
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
            // 新增时，来源固定为人工录入 (source: 1)
            const knowledgeData = {
              ...this.currentKnowledge,
              source: 1
            };
            res = await addKnowledge(knowledgeData);
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
    },

    // 转为常见问题
    handleConvertToFaq(row) {
      this.$confirm(`确定要将知识 "${row.title}" 转为常见问题吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const res = await convertKnowledgeToFaq({
            id: row.id,
            title: row.title,
            content: row.content,
            keywords: row.keywords,
            category: row.category
          });
          if (res.code === 200) {
            this.$message.success('转换成功，已添加到常见问题');
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('转换失败', error);
          this.$message.error('转换失败');
        }
      }).catch(() => {});
    },

    // AI生成内容
    async generateContentByAI() {
      if (!this.currentKnowledge.title) {
        this.$message.warning('请先填写标题，以便AI生成内容');
        return;
      }

      this.aiContentGenerating = true;
      try {
        const res = await request({
          url: '/admin/ai/generate-content',
          method: 'post',
          data: {
            title: this.currentKnowledge.title,
            category: this.currentKnowledge.category
          }
        });

        if (res.code === 200 && res.data.content) {
          this.currentKnowledge.content = res.data.content;
          this.$message.success('AI内容生成成功');
        } else {
          this.$message.error('AI生成失败，请手动输入');
        }
      } catch (error) {
        console.error('AI生成内容失败', error);
        this.$message.error('AI生成失败，请手动输入');
      } finally {
        this.aiContentGenerating = false;
      }
    },
  }
};
</script>

<style scoped>
/* 样式保持不变 */
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

@media (max-width: 768px) {
  .knowledge-manage {
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

/* 知识详情对话框样式 */
.knowledge-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.knowledge-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.knowledge-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.knowledge-detail-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.knowledge-detail-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
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
  width: 85px;
}

.detail-value {
  font-size: 13px;
  color: #2c3e50;
}

.keywords-detail {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 5px;
}

.detail-content-text {
  background: #f8f9fc;
  padding: 16px;
  border-radius: 12px;
  color: #5a6874;
  line-height: 1.8;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
}
</style>