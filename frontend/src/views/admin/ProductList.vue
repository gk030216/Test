<template>
  <div class="product-list">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 上架商品
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="success"
            plain
            @click="handleBatchEnable"
            class="batch-btn"
        >
          <i class="el-icon-check"></i> 上架 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDisable"
            class="batch-btn"
        >
          <i class="el-icon-close"></i> 下架 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
            class="batch-btn"
        >
          <i class="el-icon-delete"></i> 删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索商品名称"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          >
          </el-input>
        </div>
        <el-cascader
            v-model="searchForm.categoryId"
            :options="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            placeholder="商品分类"
            clearable
            size="medium"
            class="category-cascader"
            @change="handleSearch"
        ></el-cascader>
        <el-select
            v-model="searchForm.status"
            placeholder="状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="上架" :value="1"></el-option>
          <el-option label="下架" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
        <el-button
            type="success"
            plain
            @click="handleExport"
            :loading="exportLoading"
            class="export-btn"
        >
          <i class="el-icon-download"></i> 导出
        </el-button>
      </div>
    </div>

    <!-- 商品表格 -->
    <el-table
        v-loading="loading"
        :data="productList"
        stripe
        style="width: 100%"
        class="product-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <el-table-column label="图片" width="80" align="center">
        <template slot-scope="scope">
          <el-image
              :size="50"
              :src="scope.row.image"
              class="product-image"
              :preview-src-list="[scope.row.image]"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </template>
      </el-table-column>

      <el-table-column prop="name" label="商品名称" min-width="200">
        <template slot-scope="scope">
          <div class="product-name">
            <span class="name-text">{{ scope.row.name }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="price" label="价格" width="120" align="center">
        <template slot-scope="scope">
          <span class="price-text">¥{{ scope.row.price }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="originalPrice" label="原价" width="120" align="center">
        <template slot-scope="scope">
          <span class="original-price">¥{{ scope.row.originalPrice || '--' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="库存" width="100" align="center">
        <template slot-scope="scope">
          <span :class="getStockClass(scope.row.stock)">
            {{ scope.row.stock || 0 }}
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="sales" label="销量" width="100" align="center"></el-table-column>

      <el-table-column prop="categoryName" label="分类" width="120" align="center"></el-table-column>

      <el-table-column label="热门" width="80" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isHot === 1" type="danger" size="small">热门</el-tag>
          <el-tag v-else type="info" size="small">普通</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="新品" width="80" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isNew === 1" type="success" size="small">新品</el-tag>
          <el-tag v-else type="info" size="small">普通</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
              :loading="scope.row.statusLoading"
          >
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="260" fixed="right" align="center">
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
                type="warning"
                plain
                circle
                @click="handleViewComments(scope.row)"
                class="action-icon-btn"
                title="评价分析"
            >
              <i class="el-icon-chat-dot-round"></i>
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
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
      >
      </el-pagination>
    </div>

    <!-- 上架商品对话框（从库存中选择） -->
    <el-dialog
        title="上架商品"
        :visible.sync="dialogVisible"
        width="650px"
        :close-on-click-modal="false"
        class="product-dialog"
        center
    >
      <div class="dialog-content">
        <!-- 主图上传区域 -->
        <div class="main-image-section">
          <div class="main-image-label">商品主图：</div>
          <div class="image-upload-wrapper" @click="triggerUpload">
            <div class="image-preview">
              <el-image
                  :src="imagePreview"
                  fit="cover"
                  style="width: 100%; height: 100%; border-radius: 8px;"
                  v-if="imagePreview"
              >
                <div slot="error" class="image-slot">加载失败</div>
              </el-image>
              <div v-else class="upload-placeholder">
                <i class="el-icon-plus"></i>
                <span>点击上传</span>
              </div>
              <div class="upload-mask" v-if="imagePreview">
                <i class="el-icon-edit"></i>
                <span>更换</span>
              </div>
            </div>
            <input
                type="file"
                ref="imageInput"
                accept="image/jpeg,image/png,image/jpg"
                style="display: none"
                @change="handleImageUpload"
            />
          </div>
          <p class="image-tip">主图（必传）</p>
        </div>

        <!-- 轮播图上传区域 -->
        <div class="images-section">
          <div class="images-label">商品轮播图：</div>
          <div class="images-list">
            <div
                v-for="(img, index) in imageList"
                :key="index"
                class="image-item"
            >
              <el-image :src="img" fit="cover" :preview-src-list="imageList"></el-image>
              <div class="image-remove" @click.stop="removeImage(index)">
                <i class="el-icon-close"></i>
              </div>
            </div>
            <div class="image-add" @click="triggerMultiUpload">
              <i class="el-icon-plus"></i>
              <span>添加</span>
            </div>
            <input
                type="file"
                ref="multiImageInput"
                accept="image/jpeg,image/png,image/jpg"
                style="display: none"
                multiple
                @change="handleMultiImageUpload"
            />
          </div>
          <p class="image-tip">支持多张轮播图，建议尺寸 800x800，≤2MB/张</p>
        </div>

        <!-- 表单区域 -->
        <el-form :model="currentProduct" :rules="formRules" ref="productForm" label-width="100px" class="product-form">
          <!-- ✅ 从库存表选择商品 -->
          <el-form-item label="选择商品" prop="inventoryId">
            <el-select
                v-model="currentProduct.inventoryId"
                placeholder="请选择要上架的商品（从库存中选择）"
                filterable
                style="width: 100%"
                @change="handleInventoryChange"
            >
              <el-option
                  v-for="item in inventoryList"
                  :key="item.id"
                  :label="`${item.itemName} (库存: ${item.stock}${item.unit})`"
                  :value="item.id"
              >
                <div class="inventory-option">
                  <span>{{ item.itemName }}</span>
                  <span :class="item.stock <= 0 ? 'stock-out' : (item.stock <= 5 ? 'stock-low' : 'stock-normal')">
                    库存: {{ item.stock }}{{ item.unit }}
                  </span>
                </div>
              </el-option>
            </el-select>
            <div class="form-tip">提示：只有库存管理中的商品才能上架</div>
          </el-form-item>

          <!-- 商品名称（自动填充，只读） -->
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="currentProduct.name" placeholder="自动获取" disabled></el-input>
          </el-form-item>

          <el-form-item label="商品分类" prop="categoryId">
            <el-cascader
                v-model="currentProduct.categoryId"
                :options="categoryTree"
                :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
                placeholder="请选择分类"
                size="medium"
                clearable
                @change="handleCategoryChange"
            ></el-cascader>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="价格" prop="price">
                <el-input v-model="currentProduct.price" placeholder="请输入价格" size="medium">
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="原价">
                <el-input v-model="currentProduct.originalPrice" placeholder="请输入原价" size="medium">
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="热门推荐">
                <el-switch v-model="currentProduct.isHot" :active-value="1" :inactive-value="0"></el-switch>
                <span class="form-tip">热门商品会在首页展示</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="新品推荐">
                <el-switch v-model="currentProduct.isNew" :active-value="1" :inactive-value="0"></el-switch>
                <span class="form-tip">新品会在首页展示</span>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="商品描述" prop="description">
            <el-input
                v-model="currentProduct.description"
                type="textarea"
                :rows="3"
                placeholder="请输入商品描述"
                size="medium"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium">
          确认上架
        </el-button>
      </span>
    </el-dialog>

    <!-- 编辑商品对话框 -->
    <el-dialog
        title="编辑商品"
        :visible.sync="editDialogVisible"
        width="650px"
        :close-on-click-modal="false"
        class="product-dialog"
        center
    >
      <div class="dialog-content">
        <div class="main-image-section">
          <div class="main-image-label">商品主图：</div>
          <div class="image-upload-wrapper" @click="triggerUploadEdit">
            <div class="image-preview">
              <el-image
                  :src="editImagePreview"
                  fit="cover"
                  style="width: 100%; height: 100%; border-radius: 8px;"
                  v-if="editImagePreview"
              >
                <div slot="error" class="image-slot">加载失败</div>
              </el-image>
              <div v-else class="upload-placeholder">
                <i class="el-icon-plus"></i>
                <span>点击上传</span>
              </div>
              <div class="upload-mask" v-if="editImagePreview">
                <i class="el-icon-edit"></i>
                <span>更换</span>
              </div>
            </div>
            <input
                type="file"
                ref="editImageInput"
                accept="image/jpeg,image/png,image/jpg"
                style="display: none"
                @change="handleEditImageUpload"
            />
          </div>
        </div>

        <div class="images-section">
          <div class="images-label">商品轮播图：</div>
          <div class="images-list">
            <div
                v-for="(img, index) in editImageList"
                :key="index"
                class="image-item"
            >
              <el-image :src="img" fit="cover" :preview-src-list="editImageList"></el-image>
              <div class="image-remove" @click.stop="removeEditImage(index)">
                <i class="el-icon-close"></i>
              </div>
            </div>
            <div class="image-add" @click="triggerMultiUploadEdit">
              <i class="el-icon-plus"></i>
              <span>添加</span>
            </div>
            <input
                type="file"
                ref="editMultiImageInput"
                accept="image/jpeg,image/png,image/jpg"
                style="display: none"
                multiple
                @change="handleEditMultiImageUpload"
            />
          </div>
        </div>

        <el-form :model="editProduct" :rules="formRules" ref="editForm" label-width="100px" class="product-form">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="editProduct.name" disabled></el-input>
          </el-form-item>

          <el-form-item label="商品分类" prop="categoryId">
            <el-cascader
                v-model="editProduct.categoryId"
                :options="categoryTree"
                :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
                placeholder="请选择分类"
                size="medium"
                clearable
            ></el-cascader>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="价格" prop="price">
                <el-input v-model="editProduct.price" placeholder="请输入价格" size="medium">
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="原价">
                <el-input v-model="editProduct.originalPrice" placeholder="请输入原价" size="medium">
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="热门推荐">
                <el-switch v-model="editProduct.isHot" :active-value="1" :inactive-value="0"></el-switch>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="新品推荐">
                <el-switch v-model="editProduct.isNew" :active-value="1" :inactive-value="0"></el-switch>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="商品描述" prop="description">
            <el-input
                v-model="editProduct.description"
                type="textarea"
                :rows="3"
                placeholder="请输入商品描述"
                size="medium"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitLoading" size="medium">
          保存修改
        </el-button>
      </span>
    </el-dialog>

    <!-- 商品详情对话框 -->
    <el-dialog
        title="商品详情"
        :visible.sync="detailVisible"
        width="650px"
        center
        class="product-detail-dialog"
    >
      <div class="detail-content" v-if="currentDetailProduct">
        <!-- 商品图片 -->
        <div class="detail-image">
          <el-image
              :src="currentDetailProduct.image"
              fit="contain"
              style="width: 100%; height: auto;"
              :preview-src-list="[currentDetailProduct.image]"
              :preview-teleported="true"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
              <span>暂无图片</span>
            </div>
          </el-image>
        </div>

        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="section-title">基本信息</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">商品名称：</span>
                <span class="detail-value">{{ currentDetailProduct.name }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">商品分类：</span>
                <span class="detail-value">{{ currentDetailProduct.categoryName || '未分类' }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">价格：</span>
                <span class="detail-value price">¥{{ currentDetailProduct.price }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">原价：</span>
                <span class="detail-value original-price">¥{{ currentDetailProduct.originalPrice || '--' }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">库存：</span>
                <span :class="getStockClass(currentDetailProduct.stock)">{{ currentDetailProduct.stock || 0 }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">销量：</span>
                <span class="detail-value">{{ currentDetailProduct.sales || 0 }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">热门：</span>
                <el-tag :type="currentDetailProduct.isHot === 1 ? 'danger' : 'info'" size="small">
                  {{ currentDetailProduct.isHot === 1 ? '热门' : '普通' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">新品：</span>
                <el-tag :type="currentDetailProduct.isNew === 1 ? 'success' : 'info'" size="small">
                  {{ currentDetailProduct.isNew === 1 ? '新品' : '普通' }}
                </el-tag>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">状态：</span>
                <el-tag :type="currentDetailProduct.status === 1 ? 'success' : 'danger'" size="small">
                  {{ currentDetailProduct.status === 1 ? '上架' : '下架' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">创建时间：</span>
                <span class="detail-value">{{ formatDate(currentDetailProduct.createTime) }}</span>
              </div>
            </el-col>
          </el-row>
          <div class="detail-item full-width">
            <span class="detail-label">商品描述：</span>
            <span class="detail-value">{{ currentDetailProduct.description || '暂无描述' }}</span>
          </div>
        </div>

        <!-- 轮播图 -->
        <div class="detail-section" v-if="currentDetailProduct.images">
          <div class="section-title">轮播图</div>
          <div class="detail-images">
            <el-image
                v-for="(img, idx) in currentDetailProduct.images.split(',')"
                :key="idx"
                :src="img"
                :preview-src-list="currentDetailProduct.images.split(',')"
                fit="contain"
                class="detail-img"
                :preview-teleported="true"
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </div>
        </div>
      </div>

      <span slot="footer">
    <el-button @click="detailVisible = false">关闭</el-button>
  </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminProductList,
  addProduct,
  updateProduct,
  updateProductStatus,
  deleteProduct,
  batchUpdateProductStatus,
  batchDeleteProducts,
  exportProductList
} from '@/api/product';
import { getInventoryList } from '@/api/inventory';
import { getAllCategories } from '@/api/category';
import { uploadProductImage } from '@/api/upload';

export default {
  name: 'ProductList',
  data() {
    return {
      loading: false,
      submitLoading: false,
      detailVisible: false,
      currentDetailProduct: null,
      productList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      exportLoading: false,
      selectedRows: [],
      categoryTree: [],
      inventoryList: [],  // 库存商品列表
      searchForm: {
        keyword: '',
        categoryId: null,
        status: ''
      },
      dialogVisible: false,
      editDialogVisible: false,
      isEdit: false,
      imagePreview: '',
      imageList: [],
      editImagePreview: '',
      editImageList: [],
      currentProduct: {
        id: null,
        inventoryId: null,
        name: '',
        description: '',
        price: null,
        originalPrice: null,
        categoryId: null,
        categoryName: '',
        image: '',
        images: '',
        isHot: 0,
        isNew: 0
      },
      editProduct: {
        id: null,
        name: '',
        description: '',
        price: null,
        originalPrice: null,
        categoryId: null,
        categoryName: '',
        image: '',
        images: '',
        status: 0,
        isHot: 0,
        isNew: 0
      },
      formRules: {
        inventoryId: [{ required: true, message: '请选择商品', trigger: 'change' }],
        price: [
          { required: true, message: '请输入价格', trigger: 'blur' },
          { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的价格', trigger: 'blur' }
        ],
        categoryId: [
          { required: true, message: '请选择分类', trigger: 'change' }
        ]
      }
    };
  },
  computed: {
    dialogTitle() {
      return '上架商品';
    }
  },
  created() {
    this.loadCategories();
    this.loadProductList();
    this.loadInventoryList();
  },
  methods: {
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
          if (children.length) {
            cat.children = children;
          }
          tree.push(cat);
        }
      }
      return tree;
    },

    // 查看商品详情
    handleView(row) {
      this.currentDetailProduct = row;
      this.detailVisible = true;
    },

    async loadCategories() {
      try {
        const res = await getAllCategories();
        if (res.code === 200) {
          this.categoryTree = this.buildCategoryTree(res.data);
        }
      } catch (error) {
        console.error('加载分类失败', error);
      }
    },

    async handleExport() {
      this.exportLoading = true;
      try {
        // 获取当前筛选条件
        let categoryId = this.searchForm.categoryId;
        if (Array.isArray(categoryId) && categoryId.length > 0) {
          categoryId = categoryId[categoryId.length - 1];
        }

        const params = {};
        if (this.searchForm.keyword && this.searchForm.keyword.trim()) {
          params.keyword = this.searchForm.keyword.trim();
        }
        if (categoryId) {
          params.categoryId = categoryId;
        }
        if (this.searchForm.status !== '' && this.searchForm.status !== null) {
          params.status = this.searchForm.status;
        }

        // 调用导出接口
        const res = await exportProductList(params);

        // 创建下载链接（Excel文件）
        const blob = new Blob([res], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.href = url;
        link.setAttribute('download', `商品列表_${new Date().getTime()}.xlsx`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);

        this.$message.success('导出成功');
      } catch (error) {
        console.error('导出失败', error);
        this.$message.error('导出失败');
      } finally {
        this.exportLoading = false;
      }
    },

    async loadInventoryList() {
      try {
        const res = await getInventoryList({
          page: 1,
          pageSize: 100,
          type: 'product'
        });
        if (res.code === 200) {
          // 只显示未上架的商品（即不在 product 表中的）
          this.inventoryList = res.data.list || [];
        }
      } catch (error) {
        console.error('加载库存列表失败', error);
      }
    },

    handleInventoryChange(inventoryId) {
      const selected = this.inventoryList.find(item => item.id === inventoryId);
      if (selected) {
        this.currentProduct.name = selected.itemName;
      }
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
        if (category) {
          this.currentProduct.categoryName = category.name;
        }
      } else {
        this.currentProduct.categoryName = '';
      }
    },

    async loadProductList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize
        };

        if (this.searchForm.keyword && this.searchForm.keyword.trim()) {
          params.keyword = this.searchForm.keyword.trim();
        }

        // 处理 categoryId：如果是数组，取最后一个（子分类ID）
        let categoryId = this.searchForm.categoryId;
        if (Array.isArray(categoryId) && categoryId.length > 0) {
          categoryId = categoryId[categoryId.length - 1];
        }
        if (categoryId) {
          params.categoryId = categoryId;
        }

        if (this.searchForm.status !== '' && this.searchForm.status !== null) {
          params.status = this.searchForm.status;
        }

        console.log('请求参数:', params);

        const res = await getAdminProductList(params);
        if (res.code === 200) {
          this.productList = res.data.list.map(item => ({
            ...item,
            statusLoading: false
          }));
          this.total = res.data.total;
        }
      } catch (error) {
        console.error('加载商品列表失败', error);
        this.$message.error('加载商品列表失败');
      } finally {
        this.loading = false;
      }
    },

    handleSearch() {
      this.page = 1;
      this.loadProductList();
    },
    handleReset() {
      this.searchForm = {
        keyword: '',
        categoryId: null,
        status: ''
      };
      this.page = 1;
      this.loadProductList();
    },
    handlePageChange(page) {
      this.page = page;
      this.loadProductList();
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadProductList();
    },
    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    // 主图上传
    triggerUpload() {
      this.$refs.imageInput.click();
    },
    async handleImageUpload(event) {
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
        const res = await uploadProductImage(formData);
        if (res.code === 200) {
          this.imagePreview = res.data.url;
          this.currentProduct.image = res.data.url;
          this.$message.success('主图上传成功');
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
      event.target.value = '';
    },

    // 轮播图上传
    triggerMultiUpload() {
      this.$refs.multiImageInput.click();
    },
    async handleMultiImageUpload(event) {
      const files = event.target.files;
      if (!files || files.length === 0) return;
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (!file.type.startsWith('image/')) {
          this.$message.error(`文件 ${file.name} 不是图片格式`);
          continue;
        }
        if (file.size > 2 * 1024 * 1024) {
          this.$message.error(`图片 ${file.name} 不能超过2MB`);
          continue;
        }
        const formData = new FormData();
        formData.append('file', file);
        try {
          const res = await uploadProductImage(formData);
          if (res.code === 200) {
            this.imageList.push(res.data.url);
          }
        } catch (error) {
          this.$message.error(`上传 ${file.name} 失败`);
        }
      }
      event.target.value = '';
    },
    removeImage(index) {
      this.imageList.splice(index, 1);
    },

    // 编辑相关上传
    triggerUploadEdit() {
      this.$refs.editImageInput.click();
    },
    async handleEditImageUpload(event) {
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
        const res = await uploadProductImage(formData);
        if (res.code === 200) {
          this.editImagePreview = res.data.url;
          this.editProduct.image = res.data.url;
          this.$message.success('主图上传成功');
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
      event.target.value = '';
    },
    triggerMultiUploadEdit() {
      this.$refs.editMultiImageInput.click();
    },
    async handleEditMultiImageUpload(event) {
      const files = event.target.files;
      if (!files || files.length === 0) return;
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (!file.type.startsWith('image/')) {
          this.$message.error(`文件 ${file.name} 不是图片格式`);
          continue;
        }
        if (file.size > 2 * 1024 * 1024) {
          this.$message.error(`图片 ${file.name} 不能超过2MB`);
          continue;
        }
        const formData = new FormData();
        formData.append('file', file);
        try {
          const res = await uploadProductImage(formData);
          if (res.code === 200) {
            this.editImageList.push(res.data.url);
          }
        } catch (error) {
          this.$message.error(`上传 ${file.name} 失败`);
        }
      }
      event.target.value = '';
    },
    removeEditImage(index) {
      this.editImageList.splice(index, 1);
    },

    handleAdd() {
      this.currentProduct = {
        id: null,
        inventoryId: null,
        name: '',
        description: '',
        price: null,
        originalPrice: null,
        categoryId: null,
        categoryName: '',
        image: '',
        images: '',
        isHot: 0,
        isNew: 0
      };
      this.imagePreview = '';
      this.imageList = [];
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.productForm) {
          this.$refs.productForm.clearValidate();
        }
      });
    },

    handleEdit(row) {
      this.editProduct = {
        id: row.id,
        name: row.name,
        description: row.description || '',
        price: row.price,
        originalPrice: row.originalPrice,
        categoryId: row.categoryId,
        categoryName: row.categoryName,
        image: row.image,
        images: row.images || '',
        status: row.status,
        isHot: row.isHot || 0,
        isNew: row.isNew || 0
      };
      this.editImagePreview = row.image;
      this.editImageList = row.images ? row.images.split(',') : [];
      this.editDialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.editForm) {
          this.$refs.editForm.clearValidate();
        }
      });
    },

    async submitForm() {
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
          status: 1  // 上架状态
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
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('上架失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    async submitEdit() {
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
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('修改失败');
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
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: newStatus === 1 ? 'info' : 'warning'
      }).then(async () => {
        row.statusLoading = true;
        try {
          const res = await updateProductStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(res.message);
            this.loadProductList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error(error.message || '操作失败');
        } finally {
          row.statusLoading = false;
        }
      }).catch(() => {});
    },

    // 查看商品评价分析
    handleViewComments(row) {
      // 跳转到评价分析页面，并传递商品ID和商品名称
      this.$router.push({
        path: '/admin/product-comment-analysis',
        query: {
          productId: row.id,
          productName: row.name
        }
      });
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除商品 "${row.name}" 吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteProduct(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadProductList();
            this.loadInventoryList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    async handleBatchEnable() {
      if (this.selectedRows.length === 0) return;
      const outOfStockItems = this.selectedRows.filter(row => row.stock <= 0);
      if (outOfStockItems.length > 0) {
        this.$message.warning(`以下商品库存不足，无法上架：${outOfStockItems.map(i => i.name).join('、')}`);
        return;
      }
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要上架选中的 ${this.selectedRows.length} 个商品吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const res = await batchUpdateProductStatus(ids, 1);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadProductList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量上架失败');
        }
      }).catch(() => {});
    },

    async handleBatchDisable() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要下架选中的 ${this.selectedRows.length} 个商品吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchUpdateProductStatus(ids, 0);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadProductList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量下架失败');
        }
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个商品吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteProducts(ids);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadProductList();
            this.loadInventoryList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量删除失败');
        }
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.product-list {
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

.category-cascader {
  width: 180px;
}

.category-cascader ::v-deep .el-input__inner {
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

.export-btn {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  color: white;
  font-weight: 500;
  padding: 8px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.export-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
  color: white;
}

.product-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.product-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
}

.product-image {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  background: #f5f5f5;
}

.price-text {
  color: #ff6b6b;
  font-weight: 500;
}

.original-price {
  color: #999;
  text-decoration: line-through;
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

/* 对话框样式 */
.product-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.product-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.product-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

/* 主图区域 */
.main-image-section {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eef2f6;
}

.main-image-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
}

/* 轮播图区域 */
.images-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eef2f6;
}

.images-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
}

.images-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.image-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
  background: #f5f5f5;
}

.image-item .el-image {
  width: 100%;
  height: 100%;
}

.image-remove {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  background: #f56c6c;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  font-size: 12px;
  transition: all 0.3s;
}

.image-remove:hover {
  transform: scale(1.1);
}

.image-add {
  width: 80px;
  height: 80px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #909399;
  background: #fafafa;
}

.image-add:hover {
  border-color: #667eea;
  color: #667eea;
}

.image-add i {
  font-size: 24px;
  margin-bottom: 4px;
}

.image-add span {
  font-size: 12px;
}

.image-upload-wrapper {
  display: inline-block;
  cursor: pointer;
  position: relative;
}

.image-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  background: #f5f5f5;
  margin: 0 auto;
}

.image-preview:hover {
  transform: scale(1.05);
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
}

.upload-placeholder i {
  font-size: 32px;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview:hover .upload-mask {
  opacity: 1;
}

.image-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.product-form {
  margin-top: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.dialog-footer {
  text-align: right;
  padding: 16px 24px 20px;
  border-top: 1px solid #eef2f6;
}

.stock-normal {
  color: #67c23a;
  font-weight: 500;
}

.stock-low {
  color: #e6a23c;
  font-weight: 500;
}

.stock-out {
  color: #f56c6c;
  font-weight: 500;
}

.inventory-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

/* ========== 商品详情对话框样式 ========== */
.product-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.product-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.product-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.product-detail-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.product-detail-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.detail-content {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 8px;
}

/* 主图容器 - 图片等比例缩小 */
.detail-image {
  margin-bottom: 20px;
  background: #f8f9fc;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
}

.detail-image .el-image {
  width: 100%;
  max-width: 200px;
  margin: 0 auto;
}

.detail-image .el-image__inner {
  width: 100%;
  height: auto !important;
  max-height: 200px;
  object-fit: contain;
}

/* 轮播图容器 */
.detail-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  padding: 0 10px;
}

/* 轮播图图片 - 等比例缩小 */
.detail-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  background: #f8f9fc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.detail-img .el-image {
  width: 100%;
  height: 100%;
}

.detail-img .el-image__inner {
  width: 100%;
  height: 100%;
  object-fit: contain;
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
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid #667eea;
}

.detail-item {
  margin-bottom: 12px;
  padding: 0 10px;
}

.detail-item.full-width {
  display: flex;
  align-items: flex-start;
}

.detail-label {
  display: inline-block;
  width: 80px;
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.detail-value {
  font-size: 13px;
  color: #2c3e50;
  word-break: break-word;
}

.detail-value.price {
  color: #ff6b6b;
  font-weight: 600;
}

.detail-value.original-price {
  color: #999;
  text-decoration: line-through;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #999;
  font-size: 12px;
}

.image-slot i {
  font-size: 24px;
  margin-bottom: 4px;
}
</style>