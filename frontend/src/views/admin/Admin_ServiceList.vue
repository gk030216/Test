<template>
  <div class="service-list-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon"><i class="el-icon-s-goods" /></div>
        <div class="stat-info">
          <div class="stat-label">总服务数</div>
          <div class="stat-value">{{ statistics.total || 0 }}</div>
          <div class="stat-sub">平台服务总量</div>
        </div>
      </div>
      <div class="stat-card card-on">
        <div class="stat-icon"><i class="el-icon-s-claim" /></div>
        <div class="stat-info">
          <div class="stat-label">上架中</div>
          <div class="stat-value">{{ statistics.onSale || 0 }}</div>
          <div class="stat-sub">正常售卖</div>
        </div>
      </div>
      <div class="stat-card card-off">
        <div class="stat-icon"><i class="el-icon-remove" /></div>
        <div class="stat-info">
          <div class="stat-label">已下架</div>
          <div class="stat-value">{{ statistics.offSale || 0 }}</div>
          <div class="stat-sub">暂不售卖</div>
        </div>
      </div>
      <div class="stat-card card-hot">
        <div class="stat-icon"><i class="el-icon-star-on" /></div>
        <div class="stat-info">
          <div class="stat-label">热门服务</div>
          <div class="stat-value">{{ statistics.hotCount || 0 }}</div>
          <div class="stat-sub">首页展示</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索服务名称..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchCategoryId"
          placeholder="服务分类"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select
          v-model="searchStatus"
          placeholder="全部状态"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button size="medium" @click="handleExport" :loading="exportLoading" class="export-btn">
          <i class="el-icon-download" /> 导出
        </el-button>
        <el-button type="primary" size="medium" @click="handleAdd" class="warm-btn">
          <i class="el-icon-plus" /> 新增服务
        </el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-enable" @click="handleBatchStatus(1)">
          <i class="el-icon-check" /> 批量上架
        </el-button>
        <el-button size="small" class="batch-btn-disable" @click="handleBatchStatus(0)">
          <i class="el-icon-close" /> 批量下架
        </el-button>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 表格卡片 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="serviceList"
        stripe
        row-key="id"
        class="service-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />
        <el-table-column label="图片" width="80" align="center">
          <template slot-scope="scope">
            <el-image :src="scope.row.image" fit="cover" style="width: 50px; height: 50px; border-radius: 8px;" :preview-src-list="[scope.row.image]">
              <div slot="error" class="image-slot"><i class="el-icon-picture-outline" /></div>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="服务信息" min-width="150">
          <template slot-scope="scope">
            <div class="service-name">{{ scope.row.name }}</div>
            <div class="service-category">{{ scope.row.categoryName || '未分类' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="服务描述" min-width="180">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.description" placement="top" :disabled="!scope.row.description || scope.row.description.length <= 30">
              <div class="desc-content">{{ scope.row.description || '--' }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120" align="center">
          <template slot-scope="scope">
            <span class="price-text">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="100" align="center" />
        <el-table-column prop="sales" label="销量" width="90" align="center" />
        <el-table-column label="热门" width="80" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.isHot === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleHotChange(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.status === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleStatusChange(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150" align="center">
          <template slot-scope="scope">
            <span class="cell-text cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleView(scope.row)" class="act-view">
                <i class="el-icon-view" /> 查看
              </el-button>
              <el-button type="text" size="small" @click="handleEdit(scope.row)" class="act-edit">
                <i class="el-icon-edit" /> 编辑
              </el-button>
              <el-button type="text" size="small" @click="handleViewComments(scope.row)" class="act-comment">
                <i class="el-icon-chat-dot-round" /> 评价
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
      :visible.sync="showDialog"
      width="780px"
      :close-on-click-modal="false"
      :lock-scroll="false"
      class="service-dialog"
      center
    >
      <div class="dialog-body">
        <!-- 主图上传 -->
        <div class="image-section">
          <div class="image-label">服务主图</div>
          <div class="main-image-upload" @click="triggerMainUpload">
            <div class="image-preview" v-if="mainImagePreview">
              <el-image :src="mainImagePreview" fit="cover" style="width: 100%; height: 100%; border-radius: 8px;" />
              <div class="upload-mask"><i class="el-icon-edit" /><span>更换</span></div>
            </div>
            <div v-else class="upload-placeholder"><i class="el-icon-plus" /><span>点击上传</span></div>
            <input type="file" ref="mainImageInput" accept="image/jpeg,image/png,image/jpg" style="display: none" @change="handleMainUpload" />
          </div>
          <div class="image-tip">支持 JPG、PNG 格式，≤2MB</div>
        </div>

        <!-- 轮播图上传 -->
        <div class="images-section">
          <div class="image-label">服务轮播图</div>
          <div class="images-list">
            <div v-for="(img, index) in imageList" :key="index" class="image-item">
              <el-image :src="img" fit="cover" :preview-src-list="imageList" />
              <div class="image-remove" @click.stop="removeImage(index)"><i class="el-icon-close" /></div>
            </div>
            <div class="image-add" @click="triggerMultiUpload">
              <i class="el-icon-plus" /><span>添加</span>
            </div>
            <input type="file" ref="multiImageInput" accept="image/jpeg,image/png,image/jpg" style="display: none" multiple @change="handleMultiImageUpload" />
          </div>
          <div class="image-tip">支持多张轮播图，≤2MB/张</div>
        </div>

        <el-form :model="serviceForm" :rules="serviceRules" ref="serviceForm" label-width="100px" class="service-form">
          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item label="服务名称" prop="name">
                <el-input v-model="serviceForm.name" placeholder="请输入服务名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="服务分类" prop="categoryId">
                <el-select v-model="serviceForm.categoryId" placeholder="请选择分类" style="width: 100%">
                  <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item label="价格" prop="price">
                <el-input v-model="serviceForm.price" placeholder="请输入价格" type="number" step="0.01" min="0">
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="原价">
                <el-input v-model="serviceForm.originalPrice" placeholder="请输入原价" type="number" step="0.01" min="0">
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item label="服务时长(分钟)">
                <el-input-number v-model="serviceForm.duration" :min="10" :max="480" :step="10" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="适用对象">
                <el-select v-model="serviceForm.suitableFor" placeholder="请选择" style="width: 100%">
                  <el-option label="全部" value="all" />
                  <el-option label="狗狗" value="dog" />
                  <el-option label="猫咪" value="cat" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="服务描述" prop="description">
            <el-input v-model="serviceForm.description" type="textarea" :rows="2" placeholder="请描述服务内容" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="热门推荐">
            <el-switch v-model="serviceForm.isHot" :active-value="1" :inactive-value="0" active-color="#f59e4b" />
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
        <el-button @click="showDialog = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      title="服务详情"
      :visible.sync="detailVisible"
      width="680px"
      :lock-scroll="false"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetailService">
        <!-- 主图 -->
        <div class="detail-avatar-area">
          <el-image :src="currentDetailService.image" fit="cover" style="width: 100%; height: 200px; border-radius: 12px;" :preview-src-list="getAllImages(currentDetailService)">
            <div slot="error" class="image-slot"><i class="el-icon-picture-outline" /></div>
          </el-image>
        </div>

        <!-- 轮播图 -->
        <div class="detail-images-section" v-if="getImageList(currentDetailService.images).length > 0">
          <div class="section-title">服务轮播图</div>
          <div class="detail-images">
            <el-image
              v-for="(img, idx) in getImageList(currentDetailService.images)"
              :key="idx"
              :src="img"
              :preview-src-list="getAllImages(currentDetailService)"
              fit="cover"
              class="detail-img"
            >
              <div slot="error" class="image-slot"><i class="el-icon-picture-outline" /></div>
            </el-image>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="d-label">服务名称</span>
              <span class="d-value">{{ currentDetailService.name }}</span>
            </div>
            <div class="detail-item">
              <span class="d-label">服务分类</span>
              <span class="d-value">{{ currentDetailService.categoryName || '未分类' }}</span>
            </div>
            <div class="detail-item">
              <span class="d-label">价格</span>
              <span class="d-value price">¥{{ currentDetailService.price }}</span>
            </div>
            <div class="detail-item">
              <span class="d-label">原价</span>
              <span class="d-value original-price">¥{{ currentDetailService.originalPrice || '--' }}</span>
            </div>
            <div class="detail-item">
              <span class="d-label">服务时长</span>
              <span class="d-value">{{ currentDetailService.duration }}分钟</span>
            </div>
            <div class="detail-item">
              <span class="d-label">销量</span>
              <span class="d-value">{{ currentDetailService.sales || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="d-label">适用对象</span>
              <span class="d-value">{{ getSuitableText(currentDetailService.suitableFor) }}</span>
            </div>
            <div class="detail-item">
              <span class="d-label">热门</span>
              <span class="d-value">
                <span class="status-tag" :class="currentDetailService.isHot === 1 ? 'active' : 'inactive'">
                  {{ currentDetailService.isHot === 1 ? '是' : '否' }}
                </span>
              </span>
            </div>
          </div>
          <div class="detail-item detail-desc" style="border: none; padding-left: 0; padding-right: 0;">
            <span class="d-label">服务描述</span>
            <span class="d-value">{{ currentDetailService.description || '暂无描述' }}</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
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
  getServiceStatistics,
  exportServiceList
} from '@/api/service';
import { uploadServiceImage } from '@/api/upload';

export default {
  name: 'Admin_ServiceList',
  data() {
    return {
      loading: false,
      submitLoading: false,
      exportLoading: false,
      detailVisible: false,
      currentDetailService: null,
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
      imageList: [],
      serviceForm: {
        name: '',
        categoryId: null,
        description: '',
        price: null,
        originalPrice: null,
        duration: 30,
        image: '',
        images: '',
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
    getImageList(images) {
      if (!images) return [];
      if (typeof images === 'string') {
        return images.split(',').filter(img => img && img.trim().length > 0);
      }
      return [];
    },

    getAllImages(service) {
      const images = [service.image];
      if (service.images) images.push(...service.images.split(','));
      return images;
    },

    async loadList() {
      this.loading = true;
      try {
        let statusParam = undefined;
        if (this.searchStatus !== '' && this.searchStatus !== null && this.searchStatus !== undefined) {
          statusParam = this.searchStatus;
        }
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          categoryId: this.searchCategoryId || undefined,
          status: statusParam
        };
        const res = await getAdminItemList(params);
        if (res.code === 200) {
          this.serviceList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载服务列表失败');
      } finally {
        this.loading = false;
      }
    },

    async handleExport() {
      this.exportLoading = true;
      try {
        let statusParam = undefined;
        if (this.searchStatus !== '' && this.searchStatus !== null && this.searchStatus !== undefined) {
          statusParam = this.searchStatus;
        }
        const params = {
          keyword: this.searchKeyword || undefined,
          categoryId: this.searchCategoryId || undefined,
          status: statusParam
        };
        const res = await exportServiceList(params);
        const contentType = res.type;
        if (contentType === 'application/json') {
          const text = await res.text();
          const error = JSON.parse(text);
          this.$message.error(error.message || '导出失败');
          return;
        }
        if (res.size === 0) { this.$message.error('导出失败：无数据'); return; }
        const url = window.URL.createObjectURL(res);
        const link = document.createElement('a');
        link.href = url;
        link.download = `服务列表_${Date.now()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.$message.success('导出成功');
      } catch (error) {
        this.$message.error(error?.message || '导出失败');
      } finally {
        this.exportLoading = false;
      }
    },

    handleView(row) { this.currentDetailService = row; this.detailVisible = true; },

    getSuitableText(suitableFor) {
      if (!suitableFor || suitableFor === 'all') return '全部';
      if (suitableFor === 'dog') return '狗狗';
      if (suitableFor === 'cat') return '猫咪';
      return suitableFor;
    },

    async loadStatistics() {
      try {
        let statusParam = undefined;
        if (this.searchStatus !== '' && this.searchStatus !== null && this.searchStatus !== undefined) {
          statusParam = this.searchStatus;
        }
        const params = {
          keyword: this.searchKeyword || undefined,
          categoryId: this.searchCategoryId || undefined,
          status: statusParam
        };
        const res = await getServiceStatistics(params);
        if (res.code === 200) this.statistics = res.data;
      } catch { /* non-critical */ }
    },

    async loadCategories() {
      try {
        const res = await getAllServiceCategories();
        if (res.code === 200) this.categoryOptions = (res.data || []).filter(item => item.status === 1);
      } catch { /* non-critical */ }
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    handleSearch() { this.page = 1; this.loadList(); this.loadStatistics(); },
    handleReset() {
      this.searchKeyword = '';
      this.searchCategoryId = '';
      this.searchStatus = '';
      this.page = 1;
      this.loadList();
      this.loadStatistics();
    },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    triggerMainUpload() { this.$refs.mainImageInput.click(); },

    async handleMainUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      if (!file.type.startsWith('image/')) { this.$message.error('只支持图片格式'); return; }
      if (file.size > 2 * 1024 * 1024) { this.$message.error('图片不能超过2MB'); return; }
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
        this.$message.error(error?.message || '上传失败');
      }
      event.target.value = '';
    },

    triggerMultiUpload() { this.$refs.multiImageInput.click(); },

    async handleMultiImageUpload(event) {
      const files = event.target.files;
      if (!files || files.length === 0) return;
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (!file.type.startsWith('image/')) { this.$message.error(`文件 ${file.name} 不是图片格式`); continue; }
        if (file.size > 2 * 1024 * 1024) { this.$message.error(`图片 ${file.name} 不能超过2MB`); continue; }
        const formData = new FormData();
        formData.append('file', file);
        try {
          const res = await uploadServiceImage(formData);
          if (res.code === 200) this.imageList.push(res.data.url);
        } catch (error) {
          this.$message.error(error?.message || `上传 ${file.name} 失败`);
        }
      }
      event.target.value = '';
    },

    removeImage(index) { this.imageList.splice(index, 1); },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '上架' : '下架';
      this.$confirm(`确定要${action}服务 "${row.name}" 吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          const res = await updateItemStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(res.message || `${action}成功`);
            this.loadStatistics();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '操作失败');
        }
      }).catch(() => {});
    },

    async handleHotChange(row, val) {
      const newHot = val ? 1 : 0;
      try {
        const res = await updateItem({ ...row, isHot: newHot });
        if (res.code === 200) {
          row.isHot = newHot;
          this.$message.success(res.message || (newHot === 1 ? '设为热门成功' : '取消热门成功'));
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '操作失败');
      }
    },

    handleAdd() {
      this.isEdit = false;
      this.editId = null;
      this.serviceForm = {
        name: '', categoryId: null, description: '', price: null, originalPrice: null,
        duration: 30, image: '', images: '', suitableFor: 'all', isHot: 0, status: 1
      };
      this.mainImagePreview = '';
      this.imageList = [];
      this.showDialog = true;
      this.$nextTick(() => { this.$refs.serviceForm && this.$refs.serviceForm.clearValidate(); });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.editId = row.id;
      this.serviceForm = {
        name: row.name, categoryId: row.categoryId, description: row.description || '',
        price: row.price, originalPrice: row.originalPrice, duration: row.duration,
        image: row.image, images: row.images || '', suitableFor: row.suitableFor || 'all',
        isHot: row.isHot || 0, status: row.status
      };
      this.mainImagePreview = row.image;
      this.imageList = row.images ? row.images.split(',') : [];
      this.showDialog = true;
      this.$nextTick(() => { this.$refs.serviceForm && this.$refs.serviceForm.clearValidate(); });
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除服务 "${row.name}" 吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteItem(row.id);
          if (res.code === 200) {
            this.$message.success(res.message || '删除成功');
            this.loadList();
            this.loadStatistics();
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '删除失败');
        }
      }).catch(() => {});
    },

    async handleBatchStatus(status) {
      if (this.selectedRows.length === 0) return;
      const action = status === 1 ? '上架' : '下架';
      this.$confirm(`确定要${action}选中的 ${this.selectedRows.length} 个服务吗？`, '提示', { type: 'warning' })
        .then(async () => {
          let successCount = 0;
          let failCount = 0;
          for (const row of this.selectedRows) {
            try {
              const res = await updateItemStatus(row.id, status);
              if (res.code === 200) successCount++;
              else failCount++;
            } catch { failCount++; }
          }
          if (failCount > 0) {
            this.$message.warning(`操作完成，成功 ${successCount} 个，失败 ${failCount} 个`);
          } else {
            this.$message.success(`全部${action}成功，共 ${successCount} 个`);
          }
          this.selectedRows = [];
          this.loadList();
          this.loadStatistics();
        }).catch(() => {});
    },

    handleViewComments(row) {
      this.$router.push({
        path: '/admin/service-comment-analysis',
        query: { serviceId: row.id, serviceName: row.name }
      });
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个服务吗？删除后无法恢复！`, '警告', { type: 'warning' })
        .then(async () => {
          try {
            const res = await batchDeleteItems(ids);
            if (res.code === 200) {
              this.$message.success(res.message || '批量删除成功');
              this.selectedRows = [];
              this.loadList();
              this.loadStatistics();
            } else {
              this.$message.error(res.message || '批量删除失败');
            }
          } catch (error) {
            this.$message.error(error?.message || '批量删除失败');
          }
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
            originalPrice: this.serviceForm.originalPrice ? parseFloat(this.serviceForm.originalPrice) : null,
            images: this.imageList.join(',')
          };
          let res;
          if (this.isEdit) {
            res = await updateItem({ ...submitData, id: this.editId });
          } else {
            res = await addItem(submitData);
          }
          if (res.code === 200) {
            this.$message.success(res.message || (this.isEdit ? '更新成功' : '添加成功'));
            this.showDialog = false;
            this.loadList();
            this.loadStatistics();
          } else {
            this.$message.error(res.message || '提交失败');
          }
        } catch (error) {
          this.$message.error(error?.message || (this.isEdit ? '更新失败' : '添加失败'));
        } finally {
          this.submitLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.service-list-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 统计卡片 ===== */
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
.card-total .stat-icon { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-on .stat-icon    { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-off .stat-icon   { background: linear-gradient(135deg, #f56c6c, #f0826a); }
.card-hot .stat-icon   { background: linear-gradient(135deg, #e6a23c, #f7c06b); }

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
.batch-btn-enable {
  background: rgba(103, 194, 58, 0.15) !important;
  border: 1px solid rgba(103, 194, 58, 0.35) !important;
  color: #67c23a !important;
}
.batch-btn-enable:hover { background: rgba(103, 194, 58, 0.25) !important; border-color: #67c23a !important; }
.batch-btn-disable {
  background: rgba(245, 158, 75, 0.15) !important;
  border: 1px solid rgba(245, 158, 75, 0.35) !important;
  color: #f59e4b !important;
}
.batch-btn-disable:hover { background: rgba(245, 158, 75, 0.25) !important; border-color: #f59e4b !important; }
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

.service-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.service-table ::v-deep .el-table__body td {
  padding: 15px 0; font-size: 13px; color: #3d2e2a;
}
.service-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.service-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.service-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.service-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b; border-color: #f59e4b;
}

.service-name { font-weight: 600; color: #3d2e2a; margin-bottom: 4px; }
.service-category { font-size: 12px; color: #a08c84; }
.desc-content {
  color: #5c4a42;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
  font-size: 13px;
}
.price-text { color: #f56c6c; font-weight: 500; }
.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { color: #a08c84; font-size: 12px; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
.act-comment { color: #67c23a !important; }
.act-comment:hover { color: #4a9e1b !important; }
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
.service-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.service-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.service-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.service-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.service-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.service-dialog ::v-deep .el-dialog__body { padding: 20px 28px; }


/* 图片上传区域 */
.image-section { text-align: center; margin-bottom: 14px; padding-bottom: 14px; border-bottom: 1px solid #f5ece6; }
.image-label { font-size: 13px; font-weight: 500; color: #3d2e2a; margin-bottom: 8px; text-align: center; }
.main-image-upload { display: inline-block; cursor: pointer; }
.image-preview { position: relative; width: 100px; height: 100px; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(61, 46, 42, 0.08); }
.upload-placeholder {
  width: 100px; height: 100px;
  border: 2px dashed #d4c5b9; border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #a08c84; background: #fdf8f4;
  transition: border-color 0.3s;
}
.upload-placeholder:hover { border-color: #f59e4b; }
.upload-placeholder i { font-size: 28px; margin-bottom: 4px; }
.upload-mask {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(61, 46, 42, 0.6);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: white; opacity: 0; transition: opacity 0.3s; border-radius: 8px;
}
.image-preview:hover .upload-mask { opacity: 1; }
.image-tip { font-size: 11px; color: #a08c84; margin-top: 6px; }

/* 轮播图上传 */
.images-section { margin-bottom: 14px; padding-bottom: 14px; border-bottom: 1px solid #f5ece6; }
.images-list { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.image-item {
  position: relative; width: 64px; height: 64px;
  border-radius: 8px; overflow: hidden;
  border: 1px solid #f5ece6; background: #fdf8f4;
}
.image-item .el-image { width: 100%; height: 100%; }
.image-remove {
  position: absolute; top: -6px; right: -6px;
  width: 18px; height: 18px;
  background: #f56c6c; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: white; font-size: 10px; transition: all 0.3s;
}
.image-remove:hover { transform: scale(1.1); }
.image-add {
  width: 64px; height: 64px;
  border: 2px dashed #d4c5b9; border-radius: 8px;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  cursor: pointer; color: #a08c84; background: #fdf8f4; transition: all 0.3s;
}
.image-add:hover { border-color: #f59e4b; color: #f59e4b; }
.image-add i { font-size: 20px; margin-bottom: 2px; }
.image-add span { font-size: 11px; }

/* 表单 */
.service-form ::v-deep .el-form-item { margin-bottom: 12px; }
.service-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.service-form ::v-deep .el-input__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.service-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}
.service-form ::v-deep .el-radio__input.is-checked .el-radio__inner {
  background: #f59e4b; border-color: #f59e4b;
}

.form-tip { font-size: 12px; color: #a08c84; margin-left: 8px; }

/* 对话框底部 */
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

.detail-avatar-area {
  text-align: center; margin-bottom: 24px;
  padding-bottom: 20px; border-bottom: 1px solid #f5ece6;
}

.detail-images-section { margin-bottom: 18px; }
.detail-images-section .section-title {
  font-size: 14px; font-weight: 500;
  color: #3d2e2a; margin-bottom: 10px;
  padding-left: 10px; border-left: 3px solid #f59e4b;
}
.detail-images { display: flex; gap: 10px; flex-wrap: wrap; }
.detail-img {
  width: 100px; height: 100px; border-radius: 8px;
  cursor: pointer; border: 1px solid #f5ece6;
  background: #fdf8f4; object-fit: cover;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  border: 1px solid #f5ece6;
  border-radius: 12px;
  overflow: hidden;
}
.detail-item {
  display: flex; align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #f5ece6;
  border-right: 1px solid #f5ece6;
}
.detail-item:nth-child(2n) { border-right: none; }
.detail-item:last-child { border-bottom: none; }
.detail-item:nth-last-child(2):nth-child(odd) { border-bottom: none; }
.d-label { width: 68px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; font-weight: 500; }
.d-value.price { color: #f56c6c; font-weight: 600; }
.d-value.original-price { color: #a08c84; text-decoration: line-through; }
.detail-desc { margin-top: 12px; }

.status-tag { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-tag.active   { background: #edf7ee; color: #67c23a; }
.status-tag.inactive { background: #fef0f0; color: #f56c6c; }

.image-slot {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  width: 100%; height: 100%;
  background: #fdf8f4; color: #a08c84;
}
.image-slot i { font-size: 28px; }

/* 响应式 */
@media (max-width: 768px) {
  .service-list-page { padding: 12px; }
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
