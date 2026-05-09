<template>
  <div class="product-list-page">
    <!-- 统计概览卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-goods"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">商品总数</div>
          <div class="stat-value">{{ stats.totalProducts }}</div>
          <div class="stat-sub">已上架商品</div>
        </div>
      </div>
      <div class="stat-card card-sales">
        <div class="stat-icon">
          <i class="el-icon-shopping-cart-2"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总销量</div>
          <div class="stat-value">{{ stats.totalSales }}</div>
          <div class="stat-sub">累计销售件数</div>
        </div>
      </div>
      <div class="stat-card card-revenue">
        <div class="stat-icon">
          <i class="el-icon-money"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总销售额</div>
          <div class="stat-value">¥{{ formatMoney(stats.totalRevenue) }}</div>
          <div class="stat-sub">累计收入</div>
        </div>
      </div>
      <div class="stat-card card-rating">
        <div class="stat-icon">
          <i class="el-icon-star-on"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">平均评分</div>
          <div class="stat-value">{{ formatRating(stats.avgRating) }}</div>
          <div class="stat-sub">用户评价</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索商品名称"
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-cascader
          v-model="searchForm.categoryId"
          :options="categoryTree"
          :props="{ value: 'id', label: 'name', children: 'children' }"
          placeholder="商品分类"
          clearable
          class="category-cascader"
          @change="handleSearch"
        />
        <el-select
          v-model="searchForm.status"
          placeholder="状态"
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
          <i class="el-icon-plus" /> 上架商品
        </el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-enable" @click="handleBatchEnable">
          <i class="el-icon-check" /> 批量上架
        </el-button>
        <el-button size="small" class="batch-btn-disable" @click="handleBatchDisable">
          <i class="el-icon-close" /> 批量下架
        </el-button>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 商品表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="productList"
        stripe
        row-key="id"
        class="product-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="图片" width="80" align="center">
          <template slot-scope="scope">
            <el-image :src="scope.row.image" class="product-img" :preview-src-list="[scope.row.image]">
              <div slot="error" class="image-slot"><i class="el-icon-picture-outline" /></div>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="商品名称" min-width="160">
          <template slot-scope="scope">
            <span class="cell-text name-text">{{ scope.row.name }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="描述" min-width="140" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="cell-text cell-desc">{{ scope.row.description || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="price" label="价格" width="100" align="center">
          <template slot-scope="scope">
            <span class="price-text">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="originalPrice" label="原价" width="110" align="center">
          <template slot-scope="scope">
            <span class="original-price">{{ scope.row.originalPrice ? '¥' + scope.row.originalPrice : '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="库存" width="90" align="center">
          <template slot-scope="scope">
            <span :class="getStockClass(scope.row.stock)">{{ scope.row.stock || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="sales" label="销量" width="80" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.sales || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="categoryName" label="分类" width="110" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.categoryName || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="热门" width="70" align="center">
          <template slot-scope="scope">
            <span class="status-tag" :class="scope.row.isHot === 1 ? 'hot' : ''">
              {{ scope.row.isHot === 1 ? '热门' : '普通' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="新品" width="70" align="center">
          <template slot-scope="scope">
            <span class="status-tag" :class="scope.row.isNew === 1 ? 'new' : ''">
              {{ scope.row.isNew === 1 ? '新品' : '普通' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.status === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleStatusChange(scope.row, val)"
              :loading="scope.row.statusLoading"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right" align="center">
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
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
      />
    </div>

    <!-- 上架商品对话框 -->
    <el-dialog
      title="上架商品"
      :visible.sync="dialogVisible"
      width="800px"
      :close-on-click-modal="false"
      center
      class="product-dialog"
    >
      <div class="dialog-layout">
        <div class="dialog-images-panel">
          <div class="main-image-section" @click="triggerUpload">
            <div class="image-preview">
              <el-image :src="imagePreview" fit="cover" class="preview-img" v-if="imagePreview">
                <div slot="error" class="image-slot">加载失败</div>
              </el-image>
              <div v-else class="upload-placeholder">
                <i class="el-icon-plus" /><span>主图</span>
              </div>
              <div class="upload-mask" v-if="imagePreview"><i class="el-icon-edit" /></div>
            </div>
            <input type="file" ref="imageInput" accept="image/jpeg,image/png,image/jpg" style="display:none" @change="handleImageUpload" />
          </div>
          <div class="images-section">
            <div class="images-list">
              <div v-for="(img, index) in imageList" :key="index" class="image-item">
                <el-image :src="img" fit="cover" :preview-src-list="imageList" />
                <div class="image-remove" @click.stop="removeImage(index)"><i class="el-icon-close" /></div>
              </div>
              <div class="image-add" @click="triggerMultiUpload">
                <i class="el-icon-plus" />
              </div>
              <input type="file" ref="multiImageInput" accept="image/jpeg,image/png,image/jpg" style="display:none" multiple @change="handleMultiImageUpload" />
            </div>
            <p class="image-tip">轮播图（可选）</p>
          </div>
        </div>
        <div class="dialog-form-panel">
          <el-form :model="currentProduct" :rules="formRules" ref="productForm" label-width="90px" class="product-form">
            <el-form-item label="选择商品" prop="inventoryId">
              <el-select v-model="currentProduct.inventoryId" placeholder="从库存中选择" filterable @change="handleInventoryChange" style="width:100%">
                <el-option v-for="item in inventoryList" :key="item.id" :label="`${item.itemName} (库存: ${item.stock}${item.unit})`" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="currentProduct.name" placeholder="自动获取" disabled />
            </el-form-item>
            <el-form-item label="商品分类" prop="categoryId">
              <el-cascader
                v-model="currentProduct.categoryId"
                :options="categoryTree"
                :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
                placeholder="请选择分类"
                clearable
                @change="handleCategoryChange"
                style="width:100%"
              />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="价格" prop="price">
                  <el-input v-model="currentProduct.price" placeholder="价格"><template slot="prepend">¥</template></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="原价">
                  <el-input v-model="currentProduct.originalPrice" placeholder="原价"><template slot="prepend">¥</template></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="热门推荐">
                  <el-switch v-model="currentProduct.isHot" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="新品推荐">
                  <el-switch v-model="currentProduct.isNew" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="商品描述">
              <el-input v-model="currentProduct.description" type="textarea" :rows="2" placeholder="请输入商品描述" maxlength="200" show-word-limit />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">确认上架</el-button>
      </span>
    </el-dialog>

    <!-- 编辑商品对话框 -->
    <el-dialog
      title="编辑商品"
      :visible.sync="editDialogVisible"
      width="800px"
      :close-on-click-modal="false"
      center
      class="product-dialog"
    >
      <div class="dialog-layout">
        <div class="dialog-images-panel">
          <div class="main-image-section" @click="triggerUploadEdit">
            <div class="image-preview">
              <el-image :src="editImagePreview" fit="cover" class="preview-img" v-if="editImagePreview">
                <div slot="error" class="image-slot">加载失败</div>
              </el-image>
              <div v-else class="upload-placeholder">
                <i class="el-icon-plus" /><span>主图</span>
              </div>
              <div class="upload-mask" v-if="editImagePreview"><i class="el-icon-edit" /></div>
            </div>
            <input type="file" ref="editImageInput" accept="image/jpeg,image/png,image/jpg" style="display:none" @change="handleEditImageUpload" />
          </div>
          <div class="images-section">
            <div class="images-list">
              <div v-for="(img, index) in editImageList" :key="index" class="image-item">
                <el-image :src="img" fit="cover" :preview-src-list="editImageList" />
                <div class="image-remove" @click.stop="removeEditImage(index)"><i class="el-icon-close" /></div>
              </div>
              <div class="image-add" @click="triggerMultiUploadEdit">
                <i class="el-icon-plus" />
              </div>
              <input type="file" ref="editMultiImageInput" accept="image/jpeg,image/png,image/jpg" style="display:none" multiple @change="handleEditMultiImageUpload" />
            </div>
            <p class="image-tip">轮播图（可选）</p>
          </div>
        </div>
        <div class="dialog-form-panel">
          <el-form :model="editProduct" :rules="editFormRules" ref="editForm" label-width="90px" class="product-form">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="editProduct.name" disabled />
            </el-form-item>
            <el-form-item label="商品分类" prop="categoryId">
              <el-cascader
                v-model="editProduct.categoryId"
                :options="categoryTree"
                :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
                placeholder="请选择分类"
                clearable
                style="width:100%"
              />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="价格" prop="price">
                  <el-input v-model="editProduct.price" placeholder="价格"><template slot="prepend">¥</template></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="原价">
                  <el-input v-model="editProduct.originalPrice" placeholder="原价"><template slot="prepend">¥</template></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="热门推荐">
                  <el-switch v-model="editProduct.isHot" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="新品推荐">
                  <el-switch v-model="editProduct.isNew" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="商品描述">
              <el-input v-model="editProduct.description" type="textarea" :rows="2" placeholder="请输入商品描述" maxlength="200" show-word-limit />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitLoading" class="warm-btn">保存修改</el-button>
      </span>
    </el-dialog>

    <!-- 商品详情对话框 -->
    <el-dialog
      title="商品详情"
      :visible.sync="detailVisible"
      width="700px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetailProduct">
        <div class="detail-layout">
          <div class="detail-avatar-area">
            <el-image :src="currentDetailProduct.image" fit="contain" :preview-src-list="[currentDetailProduct.image]" class="detail-main-img">
              <div slot="error" class="image-slot"><i class="el-icon-picture-outline" /></div>
            </el-image>
            <div class="detail-images-section" v-if="currentDetailProduct.images">
              <div class="detail-images">
                <el-image
                  v-for="(img, idx) in currentDetailProduct.images.split(',')"
                  :key="idx"
                  :src="img"
                  :preview-src-list="currentDetailProduct.images.split(',')"
                  fit="cover"
                  class="detail-img"
                >
                  <div slot="error" class="image-slot"><i class="el-icon-picture-outline" /></div>
                </el-image>
              </div>
            </div>
          </div>
          <div class="detail-grid">
            <div class="detail-item"><span class="d-label">商品名称</span><span class="d-value">{{ currentDetailProduct.name }}</span></div>
            <div class="detail-item"><span class="d-label">分类</span><span class="d-value">{{ currentDetailProduct.categoryName || '未分类' }}</span></div>
            <div class="detail-item"><span class="d-label">价格</span><span class="d-value price">¥{{ currentDetailProduct.price }}</span></div>
            <div class="detail-item"><span class="d-label">原价</span><span class="d-value original">{{ currentDetailProduct.originalPrice ? '¥' + currentDetailProduct.originalPrice : '--' }}</span></div>
            <div class="detail-item"><span class="d-label">库存</span><span class="d-value" :class="getStockClass(currentDetailProduct.stock)">{{ currentDetailProduct.stock || 0 }}</span></div>
            <div class="detail-item"><span class="d-label">销量</span><span class="d-value">{{ currentDetailProduct.sales || 0 }}</span></div>
            <div class="detail-item"><span class="d-label">热门</span><span class="d-value"><span class="status-tag" :class="currentDetailProduct.isHot === 1 ? 'hot' : ''">{{ currentDetailProduct.isHot === 1 ? '热门' : '普通' }}</span></span></div>
            <div class="detail-item"><span class="d-label">新品</span><span class="d-value"><span class="status-tag" :class="currentDetailProduct.isNew === 1 ? 'new' : ''">{{ currentDetailProduct.isNew === 1 ? '新品' : '普通' }}</span></span></div>
            <div class="detail-item"><span class="d-label">状态</span><span class="d-value"><span class="status-tag" :class="currentDetailProduct.status === 1 ? 'active' : 'inactive'">{{ currentDetailProduct.status === 1 ? '上架' : '下架' }}</span></span></div>
            <div class="detail-item"><span class="d-label">创建时间</span><span class="d-value">{{ formatDate(currentDetailProduct.createTime) }}</span></div>
            <div class="detail-item full-width" v-if="currentDetailProduct.description"><span class="d-label">描述</span><span class="d-value">{{ currentDetailProduct.description }}</span></div>
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
  getAdminProductList, addProduct, updateProduct, updateProductStatus,
  deleteProduct, batchUpdateProductStatus, batchDeleteProducts, exportProductList,
  getProductStatistics
} from '@/api/product';
import { getInventoryList } from '@/api/inventory';
import { getAllCategories } from '@/api/category';
import { uploadProductImage } from '@/api/upload';

export default {
  name: 'Admin_ProductList',
  data() {
    return {
      loading: false,
      submitLoading: false,
      exportLoading: false,
      detailVisible: false,
      currentDetailProduct: null,
      productList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      categoryTree: [],
      inventoryList: [],
      stats: { totalProducts: 0, totalSales: 0, totalRevenue: 0, avgRating: 0 },
      searchForm: { keyword: '', categoryId: null, status: '' },
      dialogVisible: false,
      editDialogVisible: false,
      imagePreview: '',
      imageList: [],
      editImagePreview: '',
      editImageList: [],
      currentProduct: {
        id: null, inventoryId: null, name: '', description: '',
        price: null, originalPrice: null, categoryId: null, categoryName: '',
        image: '', images: '', isHot: 0, isNew: 0
      },
      editProduct: {
        id: null, name: '', description: '', price: null, originalPrice: null,
        categoryId: null, categoryName: '', image: '', images: '',
        status: 0, isHot: 0, isNew: 0
      },
      formRules: {
        inventoryId: [{ required: true, message: '请选择商品', trigger: 'change' }],
        price: [
          { required: true, message: '请输入价格', trigger: 'blur' },
          { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的价格', trigger: 'blur' }
        ],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
      },
      editFormRules: {
        price: [
          { required: true, message: '请输入价格', trigger: 'blur' },
          { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的价格', trigger: 'blur' }
        ],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
      }
    };
  },
  created() {
    this.loadStats();
    this.loadCategories();
    this.loadProductList();
    this.loadInventoryList();
  },
  methods: {
    async loadStats() {
      try {
        const res = await getProductStatistics();
        if (res.code === 200) {
          this.stats = {
            totalProducts: res.data.totalProducts || 0,
            totalSales: res.data.totalSales || 0,
            totalRevenue: res.data.totalRevenue || 0,
            avgRating: res.data.avgRating || 0
          };
        }
      } catch { /* silent */ }
    },

    formatMoney(val) {
      if (val === undefined || val === null) return '0';
      return Number(val).toLocaleString('zh-CN');
    },

    formatRating(val) {
      if (val === undefined || val === null) return '0.0';
      return Number(val).toFixed(1);
    },

    getStockClass(stock) {
      if (stock <= 0) return 'stock-out';
      if (stock <= 5) return 'stock-low';
      return 'stock-normal';
    },

    buildCategoryTree(categories, parentId = 0) {
      const tree = [];
      for (const cat of categories) {
        if (cat.parentId === parentId) {
          const children = this.buildCategoryTree(categories, cat.id);
          if (children.length) cat.children = children;
          tree.push(cat);
        }
      }
      return tree;
    },

    handleView(row) {
      this.currentDetailProduct = row;
      this.detailVisible = true;
    },

    async loadCategories() {
      try {
        const res = await getAllCategories();
        if (res.code === 200) this.categoryTree = this.buildCategoryTree(res.data);
      } catch { /* silent */ }
    },

    async loadInventoryList() {
      try {
        const res = await getInventoryList({ page: 1, pageSize: 100, type: 'product' });
        if (res.code === 200) this.inventoryList = res.data.list || [];
      } catch { /* silent */ }
    },

    handleInventoryChange(inventoryId) {
      const selected = this.inventoryList.find(item => item.id === inventoryId);
      if (selected) this.currentProduct.name = selected.itemName;
    },

    handleCategoryChange(value) {
      if (value) {
        const findCategory = (list, id) => {
          for (const item of list) {
            if (item.id === id) return item;
            if (item.children) {
              const found = findCategory(item.children, id);
              if (found) return found;
            }
          }
          return null;
        };
        const category = findCategory(this.categoryTree, value);
        if (category) this.currentProduct.categoryName = category.name;
      } else {
        this.currentProduct.categoryName = '';
      }
    },

    async loadProductList() {
      this.loading = true;
      try {
        const params = { page: this.page, pageSize: this.pageSize };
        if (this.searchForm.keyword && this.searchForm.keyword.trim()) {
          params.keyword = this.searchForm.keyword.trim();
        }
        let categoryId = this.searchForm.categoryId;
        if (Array.isArray(categoryId) && categoryId.length > 0) {
          categoryId = categoryId[categoryId.length - 1];
        }
        if (categoryId) params.categoryId = categoryId;
        if (this.searchForm.status !== '' && this.searchForm.status !== null) {
          params.status = this.searchForm.status;
        }
        const res = await getAdminProductList(params);
        if (res.code === 200) {
          this.productList = (res.data.list || []).map(item => ({ ...item, statusLoading: false }));
          this.total = res.data.total;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载商品列表失败');
      } finally {
        this.loading = false;
      }
    },

    handleSearch() { this.page = 1; this.loadProductList(); },
    handleReset() {
      this.searchForm = { keyword: '', categoryId: null, status: '' };
      this.page = 1;
      this.loadProductList();
    },
    handlePageChange(page) { this.page = page; this.loadProductList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadProductList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    // ===== 图片上传 =====
    triggerUpload() { this.$refs.imageInput.click(); },
    async handleImageUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      if (!file.type.startsWith('image/')) { this.$message.error('只支持图片格式'); return; }
      if (file.size > 2 * 1024 * 1024) { this.$message.error('图片不能超过2MB'); return; }
      const formData = new FormData(); formData.append('file', file);
      try {
        const res = await uploadProductImage(formData);
        if (res.code === 200) {
          this.imagePreview = res.data.url;
          this.currentProduct.image = res.data.url;
          this.$message.success('主图上传成功');
        }
      } catch (error) { this.$message.error(error?.message || '上传失败'); }
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
        const formData = new FormData(); formData.append('file', file);
        try {
          const res = await uploadProductImage(formData);
          if (res.code === 200) this.imageList.push(res.data.url);
        } catch (error) { this.$message.error(error?.message || `上传 ${file.name} 失败`); }
      }
      event.target.value = '';
    },
    removeImage(index) { this.imageList.splice(index, 1); },

    triggerUploadEdit() { this.$refs.editImageInput.click(); },
    async handleEditImageUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      if (!file.type.startsWith('image/')) { this.$message.error('只支持图片格式'); return; }
      if (file.size > 2 * 1024 * 1024) { this.$message.error('图片不能超过2MB'); return; }
      const formData = new FormData(); formData.append('file', file);
      try {
        const res = await uploadProductImage(formData);
        if (res.code === 200) {
          this.editImagePreview = res.data.url;
          this.editProduct.image = res.data.url;
          this.$message.success('主图上传成功');
        }
      } catch (error) { this.$message.error(error?.message || '上传失败'); }
      event.target.value = '';
    },
    triggerMultiUploadEdit() { this.$refs.editMultiImageInput.click(); },
    async handleEditMultiImageUpload(event) {
      const files = event.target.files;
      if (!files || files.length === 0) return;
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (!file.type.startsWith('image/')) { this.$message.error(`文件 ${file.name} 不是图片格式`); continue; }
        if (file.size > 2 * 1024 * 1024) { this.$message.error(`图片 ${file.name} 不能超过2MB`); continue; }
        const formData = new FormData(); formData.append('file', file);
        try {
          const res = await uploadProductImage(formData);
          if (res.code === 200) this.editImageList.push(res.data.url);
        } catch (error) { this.$message.error(error?.message || `上传 ${file.name} 失败`); }
      }
      event.target.value = '';
    },
    removeEditImage(index) { this.editImageList.splice(index, 1); },

    // ===== 新建/编辑 =====
    handleAdd() {
      this.currentProduct = {
        id: null, inventoryId: null, name: '', description: '',
        price: null, originalPrice: null, categoryId: null, categoryName: '',
        image: '', images: '', isHot: 0, isNew: 0
      };
      this.imagePreview = '';
      this.imageList = [];
      this.dialogVisible = true;
      this.$nextTick(() => { if (this.$refs.productForm) this.$refs.productForm.clearValidate(); });
    },

    handleEdit(row) {
      this.editProduct = {
        id: row.id, name: row.name, description: row.description || '',
        price: row.price, originalPrice: row.originalPrice,
        categoryId: row.categoryId, categoryName: row.categoryName,
        image: row.image, images: row.images || '',
        status: row.status, isHot: row.isHot || 0, isNew: row.isNew || 0
      };
      this.editImagePreview = row.image;
      this.editImageList = row.images ? row.images.split(',') : [];
      this.editDialogVisible = true;
      this.$nextTick(() => { if (this.$refs.editForm) this.$refs.editForm.clearValidate(); });
    },

    submitForm() {
      this.$refs.productForm.validate(async (valid) => {
        if (!valid) return;
        const productData = {
          inventoryId: this.currentProduct.inventoryId,
          name: this.currentProduct.name,
          description: this.currentProduct.description,
          price: this.currentProduct.price,
          originalPrice: this.currentProduct.originalPrice,
          categoryId: this.currentProduct.categoryId,
          categoryName: this.currentProduct.categoryName,
          image: this.currentProduct.image,
          images: this.imageList.join(','),
          isHot: this.currentProduct.isHot,
          isNew: this.currentProduct.isNew,
          status: 1
        };
        this.submitLoading = true;
        try {
          const res = await addProduct(productData);
          if (res.code === 200) {
            this.$message.success('上架成功');
            this.dialogVisible = false;
            this.loadProductList();
            this.loadInventoryList();
          } else {
            this.$message.error(res.message || '上架失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '上架失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    submitEdit() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) return;
        const productData = {
          id: this.editProduct.id,
          description: this.editProduct.description,
          price: this.editProduct.price,
          originalPrice: this.editProduct.originalPrice,
          categoryId: this.editProduct.categoryId,
          categoryName: this.editProduct.categoryName,
          image: this.editProduct.image,
          images: this.editImageList.join(','),
          isHot: this.editProduct.isHot,
          isNew: this.editProduct.isNew
        };
        this.submitLoading = true;
        try {
          const res = await updateProduct(productData);
          if (res.code === 200) {
            this.$message.success('修改成功');
            this.editDialogVisible = false;
            this.loadProductList();
          } else {
            this.$message.error(res.message || '修改失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '修改失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '上架' : '下架';
      if (newStatus === 1 && row.stock <= 0) {
        this.$message.warning('库存不足，无法上架，请先入库');
        return;
      }
      this.$confirm(`确定要${action}商品 "${row.name}" 吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消',
        type: newStatus === 1 ? 'info' : 'warning'
      }).then(async () => {
        row.statusLoading = true;
        try {
          const res = await updateProductStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(res.message || `${action}成功`);
            this.loadProductList();
          } else {
            this.$message.error(res.message || `${action}失败`);
          }
        } catch (error) {
          this.$message.error(error?.message || `${action}失败`);
        } finally {
          row.statusLoading = false;
        }
      }).catch(() => {});
    },

    handleViewComments(row) {
      this.$router.push({ path: '/admin/product-comment-analysis', query: { productId: row.id, productName: row.name } });
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除商品 "${row.name}" 吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteProduct(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadProductList();
            this.loadInventoryList();
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '删除失败');
        }
      }).catch(() => {});
    },

    async handleBatchEnable() {
      if (this.selectedRows.length === 0) return;
      const outOfStock = this.selectedRows.filter(row => row.stock <= 0);
      if (outOfStock.length > 0) {
        this.$message.warning(`以下商品库存不足：${outOfStock.map(i => i.name).join('、')}`);
        return;
      }
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要上架选中的 ${this.selectedRows.length} 个商品吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
      }).then(async () => {
        try {
          const res = await batchUpdateProductStatus(ids, 1);
          if (res.code === 200) {
            this.$message.success(res.message || '批量上架成功');
            this.selectedRows = [];
            this.loadProductList();
          } else {
            this.$message.error(res.message || '批量上架失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '批量上架失败');
        }
      }).catch(() => {});
    },

    async handleBatchDisable() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要下架选中的 ${this.selectedRows.length} 个商品吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          const res = await batchUpdateProductStatus(ids, 0);
          if (res.code === 200) {
            this.$message.success(res.message || '批量下架成功');
            this.selectedRows = [];
            this.loadProductList();
          } else {
            this.$message.error(res.message || '批量下架失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '批量下架失败');
        }
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个商品吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteProducts(ids);
          if (res.code === 200) {
            this.$message.success(res.message || '批量删除成功');
            this.selectedRows = [];
            this.loadProductList();
            this.loadInventoryList();
          } else {
            this.$message.error(res.message || '批量删除失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '批量删除失败');
        }
      }).catch(() => {});
    },

    async handleExport() {
      this.exportLoading = true;
      try {
        let categoryId = this.searchForm.categoryId;
        if (Array.isArray(categoryId) && categoryId.length > 0) categoryId = categoryId[categoryId.length - 1];
        const params = {};
        if (this.searchForm.keyword && this.searchForm.keyword.trim()) params.keyword = this.searchForm.keyword.trim();
        if (categoryId) params.categoryId = categoryId;
        if (this.searchForm.status !== '' && this.searchForm.status !== null) params.status = this.searchForm.status;

        const res = await exportProductList(params);
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `商品列表_${new Date().getTime()}.xlsx`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
        this.$message.success('导出成功');
      } catch (error) {
        this.$message.error(error?.message || '导出失败');
      } finally {
        this.exportLoading = false;
      }
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.product-list-page {
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
.card-total .stat-icon   { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-sales .stat-icon   { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-revenue .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-rating .stat-icon  { background: linear-gradient(135deg, #e6a23c, #f7c06b); }

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
.category-cascader { width: 180px; }
.category-cascader ::v-deep .el-input__inner { border-radius: 10px; }
.status-select { width: 100px; }
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
.batch-bar .el-button { border-radius: 8px; padding: 7px 16px; font-weight: 500; }
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

.product-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.product-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.product-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.product-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.product-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.product-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b;
  border-color: #f59e4b;
}

.product-img { width: 50px; height: 50px; border-radius: 8px; }
.name-text { font-weight: 500; color: #3d2e2a; }
.cell-text { color: #5c4a42; font-size: 13px; }
.cell-desc { color: #a08c84; font-size: 12px; }
.cell-time { color: #a08c84; font-size: 12px; }
.price-text { color: #f56c6c; font-weight: 500; }
.original-price { color: #a08c84; text-decoration: line-through; font-size: 12px; }

.status-tag { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; background: #fdf8f4; color: #a08c84; }
.status-tag.hot { background: #fef0f0; color: #f56c6c; }
.status-tag.new { background: #edf7ee; color: #67c23a; }
.status-tag.active { background: #edf7ee; color: #67c23a; }
.status-tag.inactive { background: #fef0f0; color: #f56c6c; }

.stock-out { color: #f56c6c; font-weight: 500; }
.stock-low { color: #e6a23c; font-weight: 500; }
.stock-normal { color: #67c23a; font-weight: 500; }

.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
.act-comment { color: #409eff !important; }
.act-comment:hover { color: #337ecc !important; }
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
.product-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.product-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.product-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.product-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.product-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.product-dialog ::v-deep .el-dialog__body { padding: 24px 28px; }

/* 双栏布局 */
.dialog-layout {
  display: flex;
  gap: 24px;
}

.dialog-images-panel {
  flex-shrink: 0;
  width: 220px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dialog-form-panel {
  flex: 1;
  min-width: 0;
}

/* 主图上传 */
.main-image-section { text-align: center; }
.image-preview {
  position: relative; width: 140px; height: 140px;
  border-radius: 12px; overflow: hidden; margin: 0 auto;
  background: #fdf8f4;
  box-shadow: 0 4px 12px rgba(61, 46, 42, 0.08);
  cursor: pointer;
  transition: transform 0.25s;
}
.image-preview:hover { transform: scale(1.04); }
.preview-img { width: 100%; height: 100%; }
.upload-placeholder {
  width: 100%; height: 100%;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 4px; color: #a08c84;
}
.upload-placeholder i { font-size: 28px; }
.upload-placeholder span { font-size: 12px; }
.upload-mask {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.6);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: #fff; opacity: 0;
  transition: opacity 0.25s;
}
.upload-mask i { font-size: 24px; }
.image-preview:hover .upload-mask { opacity: 1; }
.image-tip { font-size: 11px; color: #a08c84; margin-top: 6px; }

/* 轮播图 */
.images-section { text-align: center; }
.images-list { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.image-item {
  position: relative; width: 62px; height: 62px;
  border-radius: 6px; overflow: hidden;
  border: 1px solid #f5ece6; background: #fdf8f4;
}
.image-item .el-image { width: 100%; height: 100%; }
.image-remove {
  position: absolute; top: -8px; right: -8px;
  width: 18px; height: 18px;
  background: #f56c6c; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #fff; font-size: 10px;
}
.image-add {
  width: 62px; height: 62px;
  border: 2px dashed #f5d5c0; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #a08c84; background: #fefbf8;
  transition: all 0.2s;
}
.image-add:hover { border-color: #f59e4b; color: #f59e4b; }
.image-add i { font-size: 20px; }

.product-form ::v-deep .el-form-item { margin-bottom: 14px; }
.product-form ::v-deep .el-form-item__label { font-size: 13px; font-weight: 500; color: #5c4a42; }
.product-form ::v-deep .el-input__inner { border-radius: 10px; }
.product-form ::v-deep .el-switch.is-checked .el-switch__core { background: #f59e4b; border-color: #f59e4b; }
.product-form ::v-deep .el-cascader { width: 100%; }

.form-tip { font-size: 12px; color: #a08c84; margin-left: 8px; }

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
.detail-dialog ::v-deep .el-dialog__body { padding: 24px 28px; }

.detail-layout {
  display: flex;
  gap: 24px;
}

.detail-avatar-area {
  flex-shrink: 0;
  width: 180px;
  text-align: center;
}
.detail-main-img {
  width: 180px;
  height: 180px;
  border-radius: 12px;
  background: #fdf8f4;
  margin-bottom: 12px;
}

.detail-images-section { margin-top: 0; }
.detail-images { display: flex; gap: 8px; flex-wrap: wrap; justify-content: center; }
.detail-img {
  width: 56px; height: 56px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #f5ece6;
  background: #fdf8f4;
}

.detail-grid {
  flex: 1;
  min-width: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  border: 1px solid #f5ece6;
  border-radius: 12px;
  overflow: hidden;
  align-self: start;
}
.detail-item {
  display: flex; align-items: flex-start;
  padding: 12px 14px;
  border-bottom: 1px solid #f5ece6;
  border-right: 1px solid #f5ece6;
}
.detail-item:nth-child(2n) { border-right: none; }
.detail-item:last-child { border-bottom: none; }
.detail-item:nth-last-child(2):nth-child(odd) { border-bottom: none; }
.detail-item.full-width { grid-column: 1 / -1; border-right: none; }
.d-label { width: 64px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; font-weight: 500; }
.d-value.price { color: #f56c6c; font-weight: 600; }
.d-value.original { color: #a08c84; text-decoration: line-through; }

.image-slot {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  width: 100%; height: 100%;
  background: #fdf8f4; color: #a08c84;
  font-size: 12px;
}
.image-slot i { font-size: 24px; margin-bottom: 4px; }

@media (max-width: 768px) {
  .product-list-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 100%; }
  .dialog-layout { flex-direction: column; }
  .dialog-images-panel { width: 100%; flex-direction: row; gap: 16px; }
  .dialog-images-panel .image-preview { width: 100px; height: 100px; }
  .dialog-images-panel .image-item, .dialog-images-panel .image-add { width: 50px; height: 50px; }
  .detail-layout { flex-direction: column; align-items: center; }
  .detail-avatar-area { width: 100%; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-item { border-right: none; }
  .detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }
  .detail-item:last-child { border-bottom: none; }
}
</style>
