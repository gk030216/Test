<template>
  <div class="service-list">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-goods"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.total || 0 }}</div>
            <div class="stat-label">总服务数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-check"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.onSale || 0 }}</div>
            <div class="stat-label">上架中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-close"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.offSale || 0 }}</div>
            <div class="stat-label">已下架</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-hot-water"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.hotCount || 0 }}</div>
            <div class="stat-label">热门服务</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增服务
        </el-button>
        <el-button v-if="selectedRows.length > 0" type="success" plain @click="handleBatchStatus(1)" class="batch-btn">
          <i class="el-icon-check"></i> 批量上架 ({{ selectedRows.length }})
        </el-button>
        <el-button v-if="selectedRows.length > 0" type="warning" plain @click="handleBatchStatus(0)" class="batch-btn">
          <i class="el-icon-close"></i> 批量下架 ({{ selectedRows.length }})
        </el-button>
        <el-button v-if="selectedRows.length > 0" type="danger" plain @click="handleBatchDelete" class="batch-btn">
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input v-model="searchKeyword" placeholder="搜索服务名称" clearable size="medium" @keyup.enter="handleSearch" class="search-input"></el-input>
        </div>
        <el-select v-model="searchCategoryId" placeholder="服务分类" clearable size="medium" class="status-select" @change="handleSearch">
          <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
        <el-select v-model="searchStatus" placeholder="状态" clearable size="medium" class="status-select" @change="handleSearch">
          <el-option label="上架" :value="1"></el-option>
          <el-option label="下架" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 服务表格 -->
    <el-table v-loading="loading" :data="serviceList" stripe class="service-table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column label="图片" width="80" align="center">
        <template slot-scope="scope">
          <el-image :src="scope.row.image" fit="cover" style="width: 50px; height: 50px; border-radius: 8px;" :preview-src-list="[scope.row.image]">
            <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="服务信息" min-width="200">
        <template slot-scope="scope">
          <div class="service-name">{{ scope.row.name }}</div>
          <div class="service-category">{{ scope.row.categoryName || '未分类' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="120" align="center">
        <template slot-scope="scope"><span class="price-text">¥{{ scope.row.price }}</span></template>
      </el-table-column>
      <el-table-column prop="duration" label="时长(分钟)" width="100" align="center"></el-table-column>
      <el-table-column prop="sales" label="销量" width="100" align="center"></el-table-column>
      <el-table-column label="热门" width="80" align="center">
        <template slot-scope="scope">
          <el-switch :value="scope.row.isHot === 1" @change="(val) => handleHotChange(scope.row, val)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-switch :value="scope.row.status === 1" active-color="#67c23a" inactive-color="#f56c6c"
                     @change="(val) => handleStatusChange(scope.row, val)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">{{ formatDate(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button size="small" type="primary" plain circle class="action-icon-btn" @click="handleEdit(scope.row)" title="编辑">
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
      <el-pagination @size-change="handleSizeChange" @current-change="handlePageChange"
                     :current-page="page" :page-sizes="[10, 20, 50]" :page-size="pageSize"
                     layout="total, sizes, prev, pager, next, jumper" :total="total" background>
      </el-pagination>
    </div>

    <!-- 新增/编辑服务对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="showDialog" width="700px" center class="service-dialog" :close-on-click-modal="false">
      <div class="dialog-content">
        <!-- 图片上传区域 -->
        <div class="image-section">
          <div class="image-label">服务主图</div>
          <div class="main-image-upload" @click="triggerMainUpload">
            <div class="image-preview" v-if="mainImagePreview">
              <el-image :src="mainImagePreview" fit="cover" style="width: 100%; height: 100%; border-radius: 8px;"></el-image>
              <div class="upload-mask"><i class="el-icon-edit"></i><span>更换</span></div>
            </div>
            <div v-else class="upload-placeholder"><i class="el-icon-plus"></i><span>点击上传</span></div>
            <input type="file" ref="mainImageInput" accept="image/jpeg,image/png,image/jpg" style="display: none" @change="handleMainUpload">
          </div>
          <div class="image-tip">支持 JPG、PNG 格式，≤2MB</div>
        </div>

        <el-form :model="serviceForm" :rules="serviceRules" ref="serviceForm" label-width="100px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="服务名称" prop="name">
                <el-input v-model="serviceForm.name" placeholder="请输入服务名称" size="medium"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="服务分类" prop="categoryId">
                <el-select v-model="serviceForm.categoryId" placeholder="请选择分类" size="medium" style="width: 100%">
                  <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="价格" prop="price">
                <el-input v-model="serviceForm.price" placeholder="请输入价格" size="medium" type="number" step="0.01" min="0">
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="原价">
                <el-input v-model="serviceForm.originalPrice" placeholder="请输入原价" size="medium" type="number" step="0.01" min="0">
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="服务时长(分钟)">
                <el-input-number v-model="serviceForm.duration" :min="10" :max="480" :step="10" size="medium" style="width: 100%"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="适用对象">
                <el-select v-model="serviceForm.suitableFor" placeholder="请选择" size="medium" style="width: 100%">
                  <el-option label="全部" value="all"></el-option>
                  <el-option label="狗狗" value="dog"></el-option>
                  <el-option label="猫咪" value="cat"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="服务描述" prop="description">
            <el-input v-model="serviceForm.description" type="textarea" :rows="3" placeholder="请描述服务内容" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="热门推荐">
            <el-switch v-model="serviceForm.isHot" :active-value="1" :inactive-value="0"></el-switch>
            <span class="form-tip">热门服务会在首页展示</span>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="serviceForm.status">
              <el-radio :label="1">上架</el-radio>
              <el-radio :label="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showDialog = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminItemList,
  addItem,
  updateItem,
  updateItemStatus,
  deleteItem,
  batchDeleteItems,
  getAllServiceCategories,
  getServiceStatistics
} from '@/api/service';
import { uploadServiceImage } from '@/api/upload';

export default {
  name: 'ServiceList',
  data() {
    return {
      loading: false,
      submitLoading: false,
      serviceList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchKeyword: '',
      searchCategoryId: '',
      searchStatus: '',
      categoryOptions: [],
      statistics: { total: 0, onSale: 0, offSale: 0, hotCount: 0 },
      showDialog: false,
      isEdit: false,
      editId: null,
      mainImagePreview: '',
      serviceForm: {
        name: '',
        categoryId: null,
        description: '',
        price: null,
        originalPrice: null,
        duration: 30,
        image: '',
        suitableFor: 'all',
        isHot: 0,
        status: 1
      },
      serviceRules: {
        name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择服务分类', trigger: 'change' }],
        price: [
          { required: true, message: '请输入价格', trigger: 'blur' },
          { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的价格格式', trigger: 'blur' }
        ],
        description: [{ required: true, message: '请输入服务描述', trigger: 'blur' }]
      }
    };
  },
  computed: {
    dialogTitle() { return this.isEdit ? '编辑服务' : '新增服务'; }
  },
  created() {
    this.loadList();
    this.loadCategories();
    this.loadStatistics();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          categoryId: this.searchCategoryId || undefined,
          status: this.searchStatus || undefined
        };
        const res = await getAdminItemList(params);
        if (res.code === 200) {
          this.serviceList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadStatistics() {
      try {
        const params = {
          keyword: this.searchKeyword || undefined,
          categoryId: this.searchCategoryId || undefined,
          status: this.searchStatus || undefined
        };
        const res = await getServiceStatistics(params);
        if (res.code === 200) {
          this.statistics = res.data;
        }
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },

    async loadCategories() {
      try {
        const res = await getAllServiceCategories();
        if (res.code === 200) {
          // 只显示启用的分类
          this.categoryOptions = (res.data || []).filter(item => item.status === 1);
        }
      } catch (error) {
        console.error('加载分类失败', error);
      }
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    handleSearch() {
      this.page = 1;
      this.loadList();
      this.loadStatistics();
    },

    handleReset() {
      this.searchKeyword = '';
      this.searchCategoryId = '';
      this.searchStatus = '';
      this.page = 1;
      this.loadList();
      this.loadStatistics();
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

    triggerMainUpload() {
      this.$refs.mainImageInput.click();
    },

    async handleMainUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      if (!file.type.startsWith('image/')) {
        this.$message.error('只支持图片格式');
        return;
      }
      if (file.size > 2 * 1024 * 1024) {
        this.$message.error('图片不能超过2MB');
        return;
      }
      const formData = new FormData();
      formData.append('file', file);
      try {
        const res = await uploadServiceImage(formData);
        if (res.code === 200) {
          this.mainImagePreview = res.data.url;
          this.serviceForm.image = res.data.url;
          this.$message.success('上传成功');
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
      event.target.value = '';
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      try {
        await updateItemStatus(row.id, newStatus);
        row.status = newStatus;
        this.$message.success(newStatus === 1 ? '上架成功' : '下架成功');
        this.loadList();
        this.loadStatistics();
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    async handleHotChange(row, val) {
      const newHot = val ? 1 : 0;
      try {
        await updateItem({ ...row, isHot: newHot });
        row.isHot = newHot;
        this.$message.success(newHot === 1 ? '设为热门成功' : '取消热门成功');
        this.loadList();
        this.loadStatistics();
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    handleAdd() {
      this.isEdit = false;
      this.editId = null;
      this.serviceForm = {
        name: '',
        categoryId: null,
        description: '',
        price: null,
        originalPrice: null,
        duration: 30,
        image: '',
        suitableFor: 'all',
        isHot: 0,
        status: 1
      };
      this.mainImagePreview = '';
      this.showDialog = true;
      this.$nextTick(() => {
        if (this.$refs.serviceForm) this.$refs.serviceForm.clearValidate();
      });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.editId = row.id;
      this.serviceForm = {
        name: row.name,
        categoryId: row.categoryId,
        description: row.description || '',
        price: row.price,
        originalPrice: row.originalPrice,
        duration: row.duration,
        image: row.image,
        suitableFor: row.suitableFor || 'all',
        isHot: row.isHot || 0,
        status: row.status
      };
      this.mainImagePreview = row.image;
      this.showDialog = true;
      this.$nextTick(() => {
        if (this.$refs.serviceForm) this.$refs.serviceForm.clearValidate();
      });
    },

    async handleDelete(row) {
      this.$confirm(`确定删除服务 "${row.name}" 吗？`, '提示', { type: 'warning' }).then(async () => {
        await deleteItem(row.id);
        this.$message.success('删除成功');
        this.loadList();
        this.loadStatistics();
      }).catch(() => {});
    },

    async handleBatchStatus(status) {
      if (this.selectedRows.length === 0) return;
      this.$confirm(`确定${status === 1 ? '上架' : '下架'}选中的 ${this.selectedRows.length} 个服务吗？`, '提示', { type: 'warning' })
          .then(async () => {
            let successCount = 0;
            for (const row of this.selectedRows) {
              try {
                await updateItemStatus(row.id, status);
                successCount++;
              } catch (e) {}
            }
            this.$message.success(`操作完成，成功：${successCount}个`);
            this.loadList();
            this.loadStatistics();
          }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      this.$confirm(`确定删除选中的 ${this.selectedRows.length} 个服务吗？`, '提示', { type: 'warning' })
          .then(async () => {
            await batchDeleteItems(ids);
            this.$message.success('删除成功');
            this.selectedRows = [];
            this.loadList();
            this.loadStatistics();
          }).catch(() => {});
    },

    async submitForm() {
      this.$refs.serviceForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          const submitData = {
            ...this.serviceForm,
            price: parseFloat(this.serviceForm.price),
            originalPrice: this.serviceForm.originalPrice ? parseFloat(this.serviceForm.originalPrice) : null
          };
          if (this.isEdit) {
            await updateItem({ ...submitData, id: this.editId });
            this.$message.success('更新成功');
          } else {
            await addItem(submitData);
            this.$message.success('添加成功');
          }
          this.showDialog = false;
          this.loadList();
          this.loadStatistics();
        } catch (error) {
          console.error('提交失败:', error);
          this.$message.error(error.message || (this.isEdit ? '更新失败' : '添加失败'));
        } finally {
          this.submitLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.service-list { padding: 20px; background: #f5f7fa; min-height: 100%; }
.stat-card { border-radius: 16px; }
.stat-card ::v-deep .el-card__body { display: flex; align-items: center; padding: 20px; }
.stat-icon { width: 50px; height: 50px; border-radius: 12px; background: linear-gradient(135deg, #667eea, #764ba2); display: flex; align-items: center; justify-content: center; margin-right: 15px; }
.stat-icon i { font-size: 24px; color: white; }
.stat-number { font-size: 28px; font-weight: bold; color: #2c3e50; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.action-bar { background: #fff; border-radius: 16px; padding: 16px 24px; margin: 20px 0; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.action-left { display: flex; gap: 12px; align-items: center; }
.action-right { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.add-btn { background: linear-gradient(135deg, #667eea, #764ba2); border: none; color: white; font-weight: 500; padding: 8px 20px; border-radius: 8px; }
.search-wrapper { position: relative; display: inline-block; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #909399; font-size: 16px; z-index: 1; }
.search-input { width: 200px; }
.search-input ::v-deep .el-input__inner { padding-left: 36px; border-radius: 8px; }
.status-select { width: 120px; }
.search-btn { background: linear-gradient(135deg, #667eea, #764ba2); border: none; color: white; border-radius: 8px; padding: 8px 16px; }
.service-table { background: #fff; border-radius: 16px; overflow: hidden; }
.service-table ::v-deep .el-table__header th { background: #f8f9fc; color: #2c3e50; font-weight: 600; font-size: 13px; padding: 14px 0; }
.service-name { font-weight: 600; color: #2c3e50; margin-bottom: 4px; }
.service-category { font-size: 12px; color: #909399; }
.price-text { color: #ff6b6b; font-weight: 500; }
.action-buttons { display: flex; justify-content: center; gap: 8px; }
.action-icon-btn { width: 32px; height: 32px; padding: 0; transition: all 0.2s; }
.action-icon-btn:hover { transform: scale(1.1); }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; background: #fff; padding: 16px 24px; border-radius: 16px; }
.service-dialog ::v-deep .el-dialog { border-radius: 20px; }
.service-dialog ::v-deep .el-dialog__header { background: linear-gradient(135deg, #667eea, #764ba2); padding: 20px 24px; margin: 0; }
.service-dialog ::v-deep .el-dialog__title { color: white; font-weight: 600; }
.dialog-content { max-height: 60vh; overflow-y: auto; padding-right: 8px; }
.image-section { text-align: center; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #eef2f6; }
.main-image-upload { display: inline-block; cursor: pointer; }
.image-preview { position: relative; width: 120px; height: 120px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.upload-placeholder { width: 120px; height: 120px; border: 2px dashed #dcdfe6; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #909399; background: #fafafa; }
.upload-placeholder i { font-size: 32px; margin-bottom: 8px; }
.upload-mask { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); display: flex; flex-direction: column; align-items: center; justify-content: center; color: white; opacity: 0; transition: opacity 0.3s; }
.image-preview:hover .upload-mask { opacity: 1; }
.image-tip { font-size: 12px; color: #909399; margin-top: 8px; }
.form-tip { font-size: 12px; color: #909399; margin-left: 8px; }
.dialog-footer { text-align: right; padding: 16px 24px 20px; border-top: 1px solid #eef2f6; }
@media (max-width: 768px) { .service-list { padding: 12px; } .action-bar { flex-direction: column; } .search-input { width: 160px; } }
</style>