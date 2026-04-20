<template>
  <div class="order-list">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.totalOrders }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #f0b45c);">
            <i class="el-icon-time"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.pendingCount }}</div>
            <div class="stat-label">待支付</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61);">
            <i class="el-icon-check"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.completedCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #909399, #b4b6bc);">
            <i class="el-icon-money"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">¥{{ statistics.totalAmount }}</div>
            <div class="stat-label">总销售额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
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
              placeholder="搜索订单号/用户名"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          >
          </el-input>
        </div>
        <el-select
            v-model="searchForm.orderStatus"
            placeholder="订单状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="全部" :value="null"></el-option>
          <el-option label="待支付" :value="0"></el-option>
          <el-option label="已支付" :value="1"></el-option>
          <el-option label="已发货" :value="2"></el-option>
          <el-option label="已完成" :value="3"></el-option>
          <el-option label="已取消" :value="4"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
        <el-button type="success" size="medium" @click="handleExport" :loading="exportLoading" plain class="export-btn">
          <i class="el-icon-download"></i> 导出
        </el-button>
      </div>
    </div>

    <!-- 订单表格 -->
    <el-table
        v-loading="loading"
        :data="orderList"
        stripe
        style="width: 100%"
        class="order-table"
        @selection-change="handleSelectionChange"
        :row-class-name="tableRowClassName"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="orderNo" label="订单号" min-width="200">
        <template slot-scope="scope">
          <span class="order-no">{{ scope.row.orderNo }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户" width="120">
        <template slot-scope="scope">
          <div class="user-info">
            <span>{{ scope.row.userName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="userPhone" label="手机号" width="120"></el-table-column>
      <el-table-column prop="payAmount" label="金额" width="100" align="center">
        <template slot-scope="scope">
          <span class="amount-text">¥{{ scope.row.payAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.orderStatus)" size="small">
            {{ getStatusText(scope.row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.payStatus === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.payStatus === 1 ? '已支付' : '未支付' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160">
        <template slot-scope="scope">
          <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button
                size="small"
                type="info"
                plain
                circle
                @click="handleDetail(scope.row)"
                class="action-icon-btn"
                title="查看详情"
            >
              <i class="el-icon-view"></i>
            </el-button>
            <el-button
                v-if="scope.row.orderStatus === 1"
                size="small"
                type="success"
                plain
                circle
                @click="handleShip(scope.row)"
                class="action-icon-btn"
                title="发货"
            >
              <i class="el-icon-truck"></i>
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

    <!-- 订单详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="detailVisible" width="750px" center class="order-detail-dialog">
      <div class="order-detail" v-if="currentOrder">
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="8">
              <div class="detail-item">
                <span class="detail-label">订单号：</span>
                <span class="detail-value">{{ currentOrder.orderNo }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="detail-label">下单时间：</span>
                <span class="detail-value">{{ formatDate(currentOrder.createTime) }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="detail-label">订单状态：</span>
                <el-tag :type="getStatusType(currentOrder.orderStatus)" size="small">{{ getStatusText(currentOrder.orderStatus) }}</el-tag>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-location"></i>
            <span>收货信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="8">
              <div class="detail-item">
                <span class="detail-label">收货人：</span>
                <span class="detail-value">{{ currentOrder.userName }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="detail-label">手机号：</span>
                <span class="detail-value">{{ currentOrder.userPhone }}</span>
              </div>
            </el-col>
          </el-row>
          <el-col :span="8">
            <div class="detail-item">
              <span class="detail-label">收货地址：</span>
              <span class="detail-value">{{ currentOrder.userAddress }}</span>
            </div>
          </el-col>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-goods"></i>
            <span>商品清单</span>
          </div>
          <el-table :data="currentOrder.items || []" border size="small" class="items-table">
            <el-table-column prop="productName" label="商品名称"></el-table-column>
            <el-table-column prop="price" label="单价" width="100" align="center">
              <template slot-scope="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" align="center"></el-table-column>
            <el-table-column prop="totalAmount" label="小计" width="100" align="center">
              <template slot-scope="scope">¥{{ scope.row.totalAmount }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-section total-section">
          <span>合计：<strong>¥{{ currentOrder.payAmount }}</strong></span>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminOrderList, getOrderDetail, updateOrderStatus, exportOrderList, deleteOrder } from '@/api/order';

export default {
  name: 'OrderList',
  data() {
    return {
      loading: false,
      exportLoading: false,
      orderList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        orderStatus: null
      },
      statistics: {
        totalOrders: 0,
        pendingCount: 0,
        completedCount: 0,
        totalAmount: 0
      },
      detailVisible: false,
      currentOrder: null
    };
  },
  created() {
    this.loadOrderList();
  },
  methods: {
    async loadOrderList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          orderStatus: this.searchForm.orderStatus || undefined
        };
        const res = await getAdminOrderList(params);
        if (res.code === 200) {
          this.orderList = res.data.list;
          this.total = res.data.total;
          this.updateStatistics(res.data.list);
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    updateStatistics(list) {
      let totalOrders = list.length;
      let pendingCount = 0;
      let completedCount = 0;
      let totalAmount = 0;

      list.forEach(order => {
        if (order.orderStatus === 0) pendingCount++;
        if (order.orderStatus === 3) completedCount++;
        totalAmount += order.payAmount || 0;
      });

      this.statistics = {
        totalOrders,
        pendingCount,
        completedCount,
        totalAmount: totalAmount.toFixed(2)
      };
    },

    tableRowClassName({ rowIndex }) {
      if (rowIndex % 2 === 0) {
        return 'even-row';
      }
      return '';
    },

    handleSearch() {
      this.page = 1;
      this.loadOrderList();
    },
    handleReset() {
      this.searchForm = { keyword: '', orderStatus: null };
      this.page = 1;
      this.loadOrderList();
    },
    handlePageChange(page) {
      this.page = page;
      this.loadOrderList();
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadOrderList();
    },
    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    getStatusText(status) {
      const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消', 5: '已退款' };
      return map[status] || '未知';
    },
    getStatusType(status) {
      const map = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger', 5: 'danger' };
      return map[status] || 'info';
    },
    async handleDetail(row) {
      try {
        const res = await getOrderDetail(row.orderNo);
        if (res.code === 200) {
          this.currentOrder = res.data;
          this.detailVisible = true;
        }
      } catch (error) {
        this.$message.error('获取详情失败');
      }
    },
    async handleShip(row) {
      this.$confirm(`确认发货订单 ${row.orderNo} 吗？`, '提示', { type: 'info' }).then(async () => {
        try {
          const res = await updateOrderStatus(row.orderNo, 2);
          if (res.code === 200) {
            this.$message.success('发货成功');
            this.loadOrderList();
          }
        } catch (error) {
          this.$message.error('发货失败');
        }
      }).catch(() => {});
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除订单 ${row.orderNo} 吗？`, '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deleteOrder(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadOrderList();
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
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个订单吗？`, '警告', { type: 'warning' }).then(async () => {
        try {
          const res = await deleteOrder(ids);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadOrderList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量删除失败');
        }
      }).catch(() => {});
    },
    async handleExport() {
      this.exportLoading = true;
      try {
        const params = {};
        if (this.searchForm.keyword && this.searchForm.keyword.trim()) {
          params.keyword = this.searchForm.keyword.trim();
        }
        if (this.searchForm.orderStatus !== null && this.searchForm.orderStatus !== '') {
          params.orderStatus = this.searchForm.orderStatus;
        }

        const res = await exportOrderList(params);

        const blob = new Blob([res], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.href = url;
        link.setAttribute('download', `订单列表_${new Date().getTime()}.xlsx`);
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
    }
  }
};
</script>

<style scoped>
.order-list {
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
  width: 55px;
  height: 55px;
  border-radius: 14px;
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 26px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 26px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 13px;
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

.action-left, .action-right {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
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
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.search-input ::v-deep .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.status-select {
  width: 120px;
}

.status-select ::v-deep .el-input__inner {
  border-radius: 8px;
}

.search-btn, .reset-btn, .export-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.search-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.reset-btn {
  border-color: #dcdfe6;
  color: #606266;
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
.order-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.order-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f6;
}

.order-table ::v-deep .el-table__body tr {
  transition: background 0.2s;
}

.order-table ::v-deep .el-table__body tr:hover > td {
  background: #fafbfe;
}

.order-table ::v-deep .el-table__body td {
  padding: 14px 0;
  font-size: 13px;
}

.order-no {
  font-family: monospace;
  font-size: 13px;
  color: #409EFF;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.amount-text {
  color: #ff6b6b;
  font-weight: 600;
  font-size: 15px;
}

.time-text {
  color: #5a6874;
  font-size: 12px;
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

/* 订单详情对话框 */
.order-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.order-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.order-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.order-detail-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.order-detail-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.order-detail {
  max-height: 60vh;
  overflow-y: auto;
  overflow-x: hidden;  /* 添加这行，禁止左右滚动 */
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
  margin-bottom: 12px;
}

.section-title i {
  color: #667eea;
  font-size: 16px;
}

.detail-item {
  margin-bottom: 10px;
}

.detail-label {
  font-size: 13px;
  color: #909399;
}

.detail-value {
  font-size: 13px;
  color: #2c3e50;
  margin-left: 4px;
}

.items-table {
  margin-top: 5px;
}

.total-section {
  text-align: right;
  font-size: 16px;
}

.total-section strong {
  color: #ff6b6b;
  font-size: 20px;
  margin-left: 10px;
}

/* 响应式 */
@media (max-width: 768px) {
  .order-list {
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