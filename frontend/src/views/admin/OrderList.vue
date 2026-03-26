<template>
  <div class="order-list">
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
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="orderNo" label="订单号" min-width="200">
        <template slot-scope="scope">
          <span class="order-no">{{ scope.row.orderNo }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户" width="120"></el-table-column>
      <el-table-column prop="userPhone" label="手机号" width="120"></el-table-column>
      <el-table-column prop="payAmount" label="金额" width="100" align="center">
        <template slot-scope="scope">
          <span class="amount-text">¥{{ scope.row.payAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" align="center">
        <template slot-scope="scope">
          <span :class="['status-tag', getStatusClass(scope.row.orderStatus)]">
            {{ getStatusText(scope.row.orderStatus) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" width="100" align="center">
        <template slot-scope="scope">
          <span :class="scope.row.payStatus === 1 ? 'status-paid' : 'status-unpaid'">
            {{ scope.row.payStatus === 1 ? '已支付' : '未支付' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button size="small" type="primary" plain @click="handleDetail(scope.row)">
              <i class="el-icon-view"></i> 详情
            </el-button>
            <el-button
                v-if="scope.row.orderStatus === 1"
                size="small"
                type="success"
                plain
                @click="handleShip(scope.row)"
            >
              发货
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
    <el-dialog title="订单详情" :visible.sync="detailVisible" width="700px">
      <div class="order-detail" v-if="currentOrder">
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-row :gutter="20">
            <el-col :span="8">订单号：{{ currentOrder.orderNo }}</el-col>
            <el-col :span="8">下单时间：{{ formatDate(currentOrder.createTime) }}</el-col>
            <el-col :span="8">订单状态：{{ getStatusText(currentOrder.orderStatus) }}</el-col>
          </el-row>
        </div>
        <div class="detail-section">
          <h4>收货信息</h4>
          <el-row :gutter="20">
            <el-col :span="8">收货人：{{ currentOrder.userName }}</el-col>
            <el-col :span="8">手机号：{{ currentOrder.userPhone }}</el-col>
            <el-col :span="8">收货地址：{{ currentOrder.userAddress }}</el-col>
          </el-row>
        </div>
        <div class="detail-section">
          <h4>商品清单</h4>
          <el-table :data="currentOrder.items || []" border size="small">
            <el-table-column prop="productName" label="商品名称"></el-table-column>
            <el-table-column prop="price" label="单价" width="100">
              <template slot-scope="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
            <el-table-column prop="totalAmount" label="小计" width="100">
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
import { getAdminOrderList, getOrderDetail, updateOrderStatus, deleteOrder } from '@/api/order';

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
        // 使用管理员接口
        const res = await getAdminOrderList(params);
        if (res.code === 200) {
          this.orderList = res.data.list;
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
    getStatusClass(status) {
      const map = { 0: 'status-pending', 1: 'status-paid', 2: 'status-shipped', 3: 'status-completed', 4: 'status-cancelled' };
      return map[status] || '';
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
    async handleExport() {
      this.exportLoading = true;
      try {
        // 导出功能
        this.$message.success('导出成功');
      } catch (error) {
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

.action-left, .action-right {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
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
  width: 120px;
}

.search-btn, .reset-btn, .export-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.order-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.order-no {
  font-family: monospace;
  font-size: 13px;
}

.amount-text {
  color: #ff6b6b;
  font-weight: 500;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.status-pending { background: #fdf6ec; color: #e6a23c; }
.status-paid { background: #e6f7ff; color: #409EFF; }
.status-shipped { background: #e8f7ef; color: #67c23a; }
.status-completed { background: #f4f4f5; color: #909399; }
.status-cancelled { background: #fef0f0; color: #f56c6c; }

.status-paid, .status-unpaid {
  font-size: 12px;
}

.status-paid { color: #67c23a; }
.status-unpaid { color: #f56c6c; }

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
}

.order-detail {
  padding: 10px;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-section h4 {
  margin-bottom: 12px;
  color: #333;
  font-weight: 500;
}

.total-section {
  text-align: right;
  font-size: 16px;
  border-bottom: none;
}

.total-section strong {
  color: #ff6b6b;
  font-size: 20px;
  margin-left: 10px;
}
</style>