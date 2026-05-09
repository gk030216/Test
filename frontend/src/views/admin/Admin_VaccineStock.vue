<template>
  <div class="vaccine-stock-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-goods"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">疫苗种类</div>
          <div class="stat-value">{{ statistics.total || 0 }}</div>
          <div class="stat-sub">库存疫苗类型</div>
        </div>
      </div>
      <div class="stat-card card-stock">
        <div class="stat-icon">
          <i class="el-icon-shopping-cart-2"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总库存</div>
          <div class="stat-value">{{ statistics.totalStock || 0 }}</div>
          <div class="stat-sub">全部疫苗库存数量</div>
        </div>
      </div>
      <div class="stat-card card-warning">
        <div class="stat-icon">
          <i class="el-icon-warning"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">低库存预警</div>
          <div class="stat-value">{{ statistics.lowStockCount || 0 }}</div>
          <div class="stat-sub">库存 ≤ 5</div>
        </div>
      </div>
      <div class="stat-card card-out">
        <div class="stat-icon">
          <i class="el-icon-sold-out"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">缺货</div>
          <div class="stat-value">{{ statistics.outOfStockCount || 0 }}</div>
          <div class="stat-sub">库存为 0</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索疫苗名称"
            clearable
            size="medium"
            @keyup.enter="handleSearch"
            class="search-input">
          </el-input>
        </div>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button size="medium" @click="handleExport" :loading="exportLoading" class="export-btn">
          <i class="el-icon-download" /> 导出
        </el-button>
        <el-button type="primary" size="medium" @click="handleAdd" class="warm-btn">
          <i class="el-icon-plus" /> 新增疫苗库存
        </el-button>
      </div>
    </div>

    <!-- 疫苗表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="vaccineList"
        stripe
        class="inventory-table">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="itemName" label="疫苗名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="stock" label="库存数量" width="120" align="center">
          <template slot-scope="scope">
            <span :class="getStockClass(scope.row.stock)">
              {{ scope.row.stock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="updateTime" label="更新时间" width="160" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleView(scope.row)" class="act-view">
                <i class="el-icon-view" /> 查看
              </el-button>
              <el-button type="text" size="small" @click="handleAddStock(scope.row)" class="act-stock">
                <i class="el-icon-upload2" /> 入库
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

    <!-- 新增对话框 -->
    <el-dialog
      title="新增疫苗"
      :visible.sync="showDialog"
      width="500px"
      :close-on-click-modal="false"
      class="form-dialog"
      center
    >
      <el-form :model="form" :rules="rules" ref="form" label-width="80px" class="stock-form">
        <el-form-item label="疫苗名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入疫苗名称" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：支" />
        </el-form-item>
        <el-form-item label="初始库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showDialog = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">
          立即创建
        </el-button>
      </span>
    </el-dialog>

    <!-- 疫苗详情对话框 -->
    <el-dialog
      title="疫苗详情"
      :visible.sync="detailVisible"
      width="520px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetailItem">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="d-label">ID</span>
            <span class="d-value">{{ currentDetailItem.id }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">疫苗名称</span>
            <span class="d-value">{{ currentDetailItem.itemName }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">单位</span>
            <span class="d-value">{{ currentDetailItem.unit || '支' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">库存状态</span>
            <span class="d-value">
              <span class="status-tag" :class="getStockStatusType(currentDetailItem.stock)">
                {{ getStockStatusText(currentDetailItem.stock) }}
              </span>
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">当前库存</span>
            <span :class="getStockClass(currentDetailItem.stock)" class="d-value">
              {{ currentDetailItem.stock || 0 }}
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">创建时间</span>
            <span class="d-value">{{ formatDateTime(currentDetailItem.createTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">更新时间</span>
            <span class="d-value">{{ formatDateTime(currentDetailItem.updateTime) }}</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 入库对话框 -->
    <el-dialog
      title="入库"
      :visible.sync="showStockDialog"
      width="450px"
      center
      class="stock-dialog"
      @closed="onStockDialogClosed"
    >
      <div class="stock-content" v-if="currentItem">
        <div class="info-row">
          <span class="info-label">疫苗名称：</span>
          <span class="info-value">{{ currentItem.itemName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">当前库存：</span>
          <span :class="getStockClass(currentItem.stock)" class="info-value">{{ currentItem.stock || 0 }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">入库数量：</span>
          <el-input-number v-model="stockForm.quantity" :min="1" :max="9999" controls-position="right" />
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showStockDialog = false">取 消</el-button>
        <el-button type="primary" @click="submitStock" :loading="stockLoading" class="warm-btn">确定入库</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getInventoryList, addInventory, addStock, deleteInventory, exportVaccineList } from '@/api/inventory';

export default {
  name: 'Admin_VaccineStock',
  data() {
    return {
      loading: false,
      submitLoading: false,
      stockLoading: false,
      detailVisible: false,
      currentDetailItem: null,
      exportLoading: false,
      vaccineList: [],
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
      currentItem: null,
      form: {
        itemName: '',
        unit: '支',
        stock: 0
      },
      stockForm: {
        quantity: 1
      },
      rules: {
        itemName: [{ required: true, message: '请输入疫苗名称', trigger: 'blur' }],
        unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
      }
    };
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
          type: 'vaccine'
        });
        if (res.code === 200) {
          this.vaccineList = res.data.list || [];
          this.total = res.data.total || 0;
          if (res.data.statistics) {
            this.statistics = res.data.statistics;
          }
        }
      } catch (error) {
        this.$message.error(error?.message || '加载失败');
      } finally {
        this.loading = false;
      }
    },

    async handleExport() {
      this.exportLoading = true;
      try {
        const params = {
          keyword: this.searchKeyword || undefined,
          type: 'vaccine'
        };
        const res = await exportVaccineList(params);
        const blob = new Blob([res], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.href = url;
        link.setAttribute('download', `疫苗库存列表_${new Date().getTime()}.xlsx`);
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
    },

    getStockClass(stock) {
      if (stock <= 0) return 'stock-out';
      if (stock <= 5) return 'stock-low';
      return 'stock-normal';
    },

    getStockStatusType(stock) {
      if (stock <= 0) return 'out';
      if (stock <= 5) return 'warn';
      return 'normal';
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
      this.form = { itemName: '', unit: '支', stock: 0 };
      this.showDialog = true;
      this.$nextTick(() => { this.$refs.form && this.$refs.form.clearValidate(); });
    },

    handleAddStock(row) {
      this.currentItem = row;
      this.stockForm = { quantity: 1 };
      this.showStockDialog = true;
    },

    onStockDialogClosed() {
      this.currentItem = null;
    },

    async handleDelete(row) {
      try {
        await this.$confirm(`确定删除 "${row.itemName}" 吗？`, '提示', { type: 'warning' });
        await deleteInventory(row.id);
        this.$message.success('删除成功');
        this.loadList();
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败');
        }
      }
    },

    async submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          const res = await addInventory({
            type: 'vaccine',
            itemId: 0,
            itemName: this.form.itemName,
            unit: this.form.unit,
            stock: this.form.stock || 0
          });
          if (res.code === 200) {
            this.$message.success('添加成功');
            this.showDialog = false;
            this.loadList();
          }
        } catch (error) {
          this.$message.error(error?.message || '操作失败');
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
        this.$message.error(error?.message || '操作失败');
      } finally {
        this.stockLoading = false;
      }
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.vaccine-stock-page {
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
.card-stock .stat-icon   { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-warning .stat-icon { background: linear-gradient(135deg, #e6a23c, #f7c06b); }
.card-out .stat-icon     { background: linear-gradient(135deg, #f56c6c, #f0826a); }

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

.search-wrapper {
  position: relative;
  display: inline-block;
}
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #a08c84;
  font-size: 16px;
  z-index: 1;
}
.search-input { width: 220px; }
.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}
.search-input ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 2px rgba(245, 158, 75, 0.1);
}

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

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.inventory-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.inventory-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.inventory-table ::v-deep .el-table__body tr:hover > td {
  background: #fefbf8;
}
.inventory-table ::v-deep .el-table__body tr.el-table__row--striped > td {
  background: #fefcf9;
}
.inventory-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td {
  background: #fefbf8;
}

.stock-normal { color: #67c23a; font-weight: 500; }
.stock-low    { color: #e6a23c; font-weight: 500; }
.stock-out    { color: #f56c6c; font-weight: 500; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 2px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-stock { color: #67c23a !important; }
.act-stock:hover { color: #4caf50 !important; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }

/* ===== 分页 ===== */
.pagination-card {
  background: #fff;
  border-radius: 16px;
  margin-top: 16px;
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

/* ===== 表单对话框 ===== */
.form-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.form-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.form-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.form-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.form-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.form-dialog ::v-deep .el-dialog__body { padding: 28px; }

.stock-form ::v-deep .el-form-item { margin-bottom: 18px; }
.stock-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.stock-form ::v-deep .el-input__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.stock-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}

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

.status-tag { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-tag.normal { background: #edf7ee; color: #67c23a; }
.status-tag.warn   { background: #fef5ee; color: #e6a23c; }
.status-tag.out    { background: #fef0f0; color: #f56c6c; }

/* ===== 入库对话框 ===== */
.stock-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.stock-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.stock-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.stock-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.stock-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.stock-dialog ::v-deep .el-dialog__body { padding: 28px; }

.stock-content { padding: 10px; }
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
  color: #3d2e2a;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .vaccine-stock-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .search-input { width: 180px; }
  .pagination-card { justify-content: center; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-item { border-right: none; }
  .detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }
  .detail-item:last-child { border-bottom: none; }
}
</style>
