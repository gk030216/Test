<template>
  <div class="product-stock">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-goods"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.total || 0 }}</div>
            <div class="stat-label">商品种类</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-shopping-cart-2"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.totalStock || 0 }}</div>
            <div class="stat-label">总库存</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-warning"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.lowStockCount || 0 }}</div>
            <div class="stat-label">低库存预警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-sold-out"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.outOfStockCount || 0 }}</div>
            <div class="stat-label">缺货</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增商品库存
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchKeyword"
              placeholder="搜索商品名称"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input">
          </el-input>
        </div>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
        <el-button type="success" size="medium" @click="handleExport" :loading="exportLoading" plain class="export-btn">
          <i class="el-icon-download"></i> 导出
        </el-button>
      </div>
    </div>

    <!-- 商品表格 -->
    <el-table
        v-loading="loading"
        :data="productList"
        stripe
        class="inventory-table">
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="itemName" label="商品名称" min-width="180"></el-table-column>
      <el-table-column prop="stock" label="库存数量" width="120" align="center">
        <template slot-scope="scope">
          <span :class="getStockClass(scope.row.stock)">
            {{ scope.row.stock }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" align="center"></el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button size="small" type="info" plain circle @click="handleView(scope.row)" title="查看">
              <i class="el-icon-view"></i>
            </el-button>
            <el-button size="small" type="success" plain circle @click="handleAddStock(scope.row)" title="入库">
              <i class="el-icon-upload2"></i>
            </el-button>
            <el-button size="small" type="primary" plain circle @click="handleEdit(scope.row)" title="编辑">
              <i class="el-icon-edit"></i>
            </el-button>
            <el-button size="small" type="danger" plain circle @click="handleDelete(scope.row)" title="删除">
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
          background>
      </el-pagination>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="showDialog" width="500px" center class="product-form-dialog">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px">
        <el-form-item label="商品名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入商品名称"></el-input>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：个、包、箱"></el-input>
        </el-form-item>
        <el-form-item label="初始库存" prop="stock" v-if="!isEdit">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 商品详情对话框 -->
    <el-dialog title="商品详情" :visible.sync="detailVisible" width="600px" center class="product-detail-dialog">
      <div class="product-detail" v-if="currentDetailItem">
        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">ID：</span>
                <span class="detail-value">{{ currentDetailItem.id }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">商品名称：</span>
                <span class="detail-value">{{ currentDetailItem.itemName }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">单位：</span>
                <span class="detail-value">{{ currentDetailItem.unit || '个' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">库存状态：</span>
                <el-tag :type="getStockStatusType(currentDetailItem.stock)" size="small">
                  {{ getStockStatusText(currentDetailItem.stock) }}
                </el-tag>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 库存信息 -->
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-shopping-cart-2"></i>
            <span>库存信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">当前库存：</span>
                <span :class="getStockClass(currentDetailItem.stock)" class="detail-value">
                  {{ currentDetailItem.stock || 0 }}
                </span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">单位：</span>
                <span class="detail-value">{{ currentDetailItem.unit || '个' }}</span>
              </div>
            </el-col>
          </el-row>
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
                <span class="detail-value">{{ formatDateTime(currentDetailItem.createTime) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">更新时间：</span>
                <span class="detail-value">{{ formatDateTime(currentDetailItem.updateTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 入库对话框 -->
    <el-dialog title="入库" :visible.sync="showStockDialog" width="450px" center class="product-stock-dialog">
      <div class="stock-content" v-if="currentItem">
        <div class="info-row">
          <span class="info-label">商品名称：</span>
          <span class="info-value">{{ currentItem.itemName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">当前库存：</span>
          <span :class="getStockClass(currentItem.stock)" class="info-value">{{ currentItem.stock || 0 }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">入库数量：</span>
          <el-input-number v-model="stockForm.quantity" :min="1" :max="9999" controls-position="right"></el-input-number>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showStockDialog = false">取消</el-button>
        <el-button type="primary" @click="submitStock" :loading="stockLoading">确定入库</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getInventoryList, addInventory, updateInventory, addStock, deleteInventory, exportProductInventoryList } from '@/api/inventory';

export default {
  name: 'ProductStock',
  data() {
    return {
      loading: false,
      submitLoading: false,
      stockLoading: false,
      exportLoading: false,
      detailVisible: false,
      currentDetailItem: null,
      productList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      searchKeyword: '',
      statistics: {
        total: 0,
        totalStock: 0,
        lowStockCount: 0,
        outOfStockCount: 0
      },
      showDialog: false,
      showStockDialog: false,
      isEdit: false,
      editId: null,
      currentItem: null,
      form: {
        itemName: '',
        unit: '个',
        stock: 0
      },
      stockForm: {
        quantity: 1
      },
      rules: {
        itemName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑商品' : '新增商品库存';
    }
  },
  created() {
    this.loadList();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const res = await getInventoryList({
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          type: 'product'
        });
        if (res.code === 200) {
          this.productList = res.data.list || [];
          this.total = res.data.total || 0;
          if (res.data.statistics) {
            this.statistics = res.data.statistics;
          }
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    getStockClass(stock) {
      if (stock <= 0) return 'stock-out';
      if (stock <= 5) return 'stock-low';
      return 'stock-normal';
    },

    getStockStatusType(stock) {
      if (stock <= 0) return 'danger';
      if (stock <= 5) return 'warning';
      return 'success';
    },

    getStockStatusText(stock) {
      if (stock <= 0) return '缺货';
      if (stock <= 5) return '库存不足';
      return '充足';
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    formatDateTime(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    handleView(row) {
      this.currentDetailItem = row;
      this.detailVisible = true;
    },

    handleSearch() {
      this.page = 1;
      this.loadList();
    },

    handleReset() {
      this.searchKeyword = '';
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

    handleAdd() {
      this.isEdit = false;
      this.editId = null;
      this.form = { itemName: '', unit: '个', stock: 0 };
      this.showDialog = true;
    },

    handleEdit(row) {
      this.isEdit = true;
      this.editId = row.id;
      this.form = { itemName: row.itemName, unit: row.unit || '个', stock: 0 };
      this.showDialog = true;
    },

    handleAddStock(row) {
      this.currentItem = row;
      this.stockForm = { quantity: 1 };
      this.showStockDialog = true;
    },

    async handleDelete(row) {
      this.$confirm(`确定删除 "${row.itemName}" 吗？`, '提示', { type: 'warning' }).then(async () => {
        await deleteInventory(row.id);
        this.$message.success('删除成功');
        this.loadList();
      }).catch(() => {});
    },

    async handleExport() {
      this.exportLoading = true;
      try {
        const params = {
          keyword: this.searchKeyword || undefined,
          type: 'product'
        };

        const res = await exportProductInventoryList(params);

        const blob = new Blob([res], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.href = url;
        link.setAttribute('download', `商品库存列表_${new Date().getTime()}.xlsx`);
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

    async submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          const submitData = {
            type: 'product',
            itemId: 0,
            itemName: this.form.itemName,
            unit: this.form.unit,
            stock: this.form.stock || 0
          };
          if (this.isEdit) {
            res = await updateInventory({ ...submitData, id: this.editId });
          } else {
            res = await addInventory(submitData);
          }
          if (res.code === 200) {
            this.$message.success(this.isEdit ? '修改成功' : '添加成功');
            this.showDialog = false;
            this.loadList();
          }
        } catch (error) {
          this.$message.error('操作失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    async submitStock() {
      if (!this.stockForm.quantity || this.stockForm.quantity <= 0) {
        this.$message.warning('请输入入库数量');
        return;
      }
      this.stockLoading = true;
      try {
        const res = await addStock(this.currentItem.id, this.stockForm.quantity);
        if (res.code === 200) {
          this.$message.success('入库成功');
          this.showStockDialog = false;
          this.loadList();
        }
      } catch (error) {
        this.$message.error('操作失败');
      } finally {
        this.stockLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.product-stock {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 统计卡片样式 */
.stat-card {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 操作栏样式 */
.action-bar {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin: 20px 0;
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
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.search-input ::v-deep .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  border-radius: 8px;
  padding: 8px 16px;
}

.search-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.reset-btn {
  border-color: #dcdfe6;
  color: #606266;
  border-radius: 8px;
}

.reset-btn:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
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

/* 表格样式 */
.inventory-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.inventory-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f6;
}

.inventory-table ::v-deep .el-table__body tr {
  transition: background 0.2s;
}

.inventory-table ::v-deep .el-table__body tr:hover > td {
  background: #fafbfe;
}

.inventory-table ::v-deep .el-table__body td {
  padding: 14px 0;
  font-size: 13px;
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

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-buttons .el-button {
  width: 32px;
  height: 32px;
  padding: 0;
  transition: all 0.2s;
}

.action-buttons .el-button:hover {
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

/* 新增/编辑对话框样式 */
.product-form-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.product-form-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.product-form-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.product-form-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.product-form-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

/* 商品详情对话框样式 */
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

.product-detail {
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

/* 入库对话框样式 */
.product-stock-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.product-stock-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.product-stock-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.product-stock-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.product-stock-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.stock-content {
  padding: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.info-label {
  width: 85px;
  font-size: 14px;
  color: #606266;
}

.info-value {
  flex: 1;
  font-size: 14px;
  color: #2c3e50;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
}

/* 响应式 */
@media (max-width: 768px) {
  .product-stock {
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
    width: 180px;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>