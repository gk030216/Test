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
        <!-- 批量删除按钮 -->
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
            class="batch-btn"
        >
          <i class="el-icon-delete"></i> 删除 ({{ selectedRows.length }})
        </el-button>

        <!-- 订单类型切换 -->
        <div class="type-tabs">
          <div class="type-tab" :class="{ active: orderType === 'all' }" @click="orderType = 'all'">
            <i class="el-icon-menu"></i> 全部
          </div>
          <div class="type-tab" :class="{ active: orderType === 'product' }" @click="orderType = 'product'">
            <i class="el-icon-goods"></i> 商品
          </div>
          <div class="type-tab" :class="{ active: orderType === 'service' }" @click="orderType = 'service'">
            <i class="el-icon-service"></i> 服务
          </div>
        </div>
      </div>

      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchKeyword"
              placeholder="搜索订单号/用户名"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          >
          </el-input>
        </div>

        <el-select
            v-model="searchStatus"
            placeholder="订单状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="全部" :value="null"></el-option>
          <el-option label="待支付/待确认" :value="0"></el-option>
          <el-option label="已支付/已确认" :value="1"></el-option>
          <el-option label="已发货/服务中" :value="2"></el-option>
          <el-option label="已完成" :value="3"></el-option>
          <el-option label="已取消" :value="4"></el-option>
          <el-option label="已退款/已拒绝" :value="5"></el-option>
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

    <!-- 统一订单表格 -->
    <el-table
        v-loading="loading"
        :data="displayList"
        stripe
        style="width: 100%"
        class="order-table"
        @selection-change="handleSelectionChange"
        :row-class-name="tableRowClassName"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column label="订单号/预约号" min-width="200">
        <template slot-scope="scope">
          <span class="order-no">{{ scope.row.orderNo || scope.row.appointmentNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row._type === 'product' ? 'primary' : 'success'" size="small">
            {{ scope.row._type === 'product' ? '商品' : '服务' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="用户" width="150">
        <template slot-scope="scope">
          <div class="user-info">
            <span class="user-name">{{ scope.row.userNickname || scope.row.userName }}</span>
            <div class="user-phone">{{ scope.row.userPhone || scope.row.contact || '--' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="商品/服务信息" min-width="180">
        <template slot-scope="scope">
          <div v-if="scope.row._type === 'product' && scope.row.items && scope.row.items[0]" class="product-info">
            <div class="product-name">{{ scope.row.items[0].productName }}</div>
            <div class="product-count">共 {{ scope.row.items.length }} 件商品</div>
          </div>
          <div v-else class="product-info">
            <div class="product-name">{{ scope.row.serviceName }}</div>
            <div class="product-count">宠物：{{ scope.row.petName || '未指定' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="100" align="center">
        <template slot-scope="scope">
          <span class="amount-text">¥{{ scope.row.payAmount || scope.row.servicePrice }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row._type === 'product'" :type="getStatusType(scope.row.orderStatus)" size="small">
            {{ getStatusText(scope.row.orderStatus) }}
          </el-tag>
          <el-tag v-else :type="getAppointmentStatusType(scope.row.status)" size="small">
            {{ getAppointmentStatusText(scope.row.status) }}
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
      <el-table-column label="创建时间" width="160">
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
                v-if="scope.row._type === 'product' && scope.row.orderStatus === 1"
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

    <!-- 商品订单详情对话框 -->
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
          <div class="detail-item">
            <span class="detail-label">收货地址：</span>
            <span class="detail-value">{{ currentOrder.userAddress }}</span>
          </div>
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

    <!-- 服务预约详情对话框 -->
    <el-dialog title="预约详情" :visible.sync="appointmentDetailVisible" width="600px" center class="order-detail-dialog">
      <div class="order-detail" v-if="currentAppointment">
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">预约编号：</span>
                <span class="detail-value">{{ currentAppointment.appointmentNo }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">预约状态：</span>
                <el-tag :type="getAppointmentStatusType(currentAppointment.status)" size="small">{{ getAppointmentStatusText(currentAppointment.status) }}</el-tag>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-service"></i>
            <span>服务信息</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">服务名称：</span>
            <span class="detail-value">{{ currentAppointment.serviceName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">服务金额：</span>
            <span class="detail-value price">¥{{ currentAppointment.servicePrice }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">服务人员：</span>
            <span class="detail-value">{{ currentAppointment.staffName || '待分配' }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-s-custom"></i>
            <span>宠物信息</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">宠物名称：</span>
            <span class="detail-value">{{ currentAppointment.petName || '未指定' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">预约时间：</span>
            <span class="detail-value">{{ formatDate(currentAppointment.appointmentDate) }} {{ currentAppointment.appointmentTime }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-user"></i>
            <span>用户信息</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">用户昵称：</span>
            <span class="detail-value">{{ currentAppointment.userNickname || currentAppointment.userName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">联系方式：</span>
            <span class="detail-value">{{ currentAppointment.contact || '--' }}</span>
          </div>
          <div class="detail-item" v-if="currentAppointment.remark">
            <span class="detail-label">备注：</span>
            <span class="detail-value">{{ currentAppointment.remark }}</span>
          </div>
          <div class="detail-item" v-if="currentAppointment.cancelReason">
            <span class="detail-label">取消原因：</span>
            <span class="detail-value cancel-text">{{ currentAppointment.cancelReason }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-time"></i>
            <span>时间信息</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">下单时间：</span>
            <span class="detail-value">{{ formatDate(currentAppointment.createTime) }}</span>
          </div>
          <div class="detail-item" v-if="currentAppointment.payTime">
            <span class="detail-label">支付时间：</span>
            <span class="detail-value">{{ formatDate(currentAppointment.payTime) }}</span>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="appointmentDetailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminOrderList, getOrderDetail, updateOrderStatus, exportOrderList, deleteOrder } from '@/api/order';
import {getAdminAppointmentList, cancelAppointmentByAdmin, exportAppointmentList, realDeleteAppointment, batchRealDeleteAppointments,} from '@/api/service';

export default {
  name: 'OrderList',
  data() {
    return {
      loading: false,
      exportLoading: false,
      orderType: 'all',
      allRawOrders: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchKeyword: '',
      searchStatus: null,
      statistics: {
        totalOrders: 0,
        pendingCount: 0,
        completedCount: 0,
        totalAmount: 0
      },
      detailVisible: false,
      appointmentDetailVisible: false,
      currentOrder: null,
      currentAppointment: null
    };
  },
  computed: {
    displayList() {
      let filtered = [...this.allRawOrders];

      // 按类型过滤
      if (this.orderType === 'product') {
        filtered = filtered.filter(item => item._type === 'product');
      } else if (this.orderType === 'service') {
        filtered = filtered.filter(item => item._type === 'service');
      }

      // 搜索过滤
      if (this.searchKeyword && this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.toLowerCase().trim();
        filtered = filtered.filter(item => {
          const orderNo = (item.orderNo || item.appointmentNo || '').toLowerCase();
          const userName = (item.userNickname || item.userName || '').toLowerCase();
          return orderNo.includes(keyword) || userName.includes(keyword);
        });
      }

      // 状态过滤
      if (this.searchStatus !== null && this.searchStatus !== '') {
        filtered = filtered.filter(item => {
          const status = item.orderStatus !== undefined ? item.orderStatus : item.status;
          return status === this.searchStatus;
        });
      }

      this.total = filtered.length;
      const start = (this.page - 1) * this.pageSize;
      const end = start + this.pageSize;
      return filtered.slice(start, end);
    }
  },
  watch: {
    orderType() {
      this.page = 1;
      this.selectedRows = [];
    },
    searchKeyword() {
      this.page = 1;
    },
    searchStatus() {
      this.page = 1;
    }
  },
  created() {
    this.loadAllOrders();
  },
  methods: {
    async loadAllOrders() {
      this.loading = true;
      try {
        const [ordersRes, appointmentsRes] = await Promise.all([
          getAdminOrderList({ page: 1, pageSize: 1000 }),
          getAdminAppointmentList({ page: 1, pageSize: 1000 })  // ✅ 修复：使用 getAdminAppointmentList
        ]);

        const orders = (ordersRes.code === 200 ? ordersRes.data.list || [] : []).map(o => ({
          ...o,
          _type: 'product',
          userNickname: o.userNickname || null
        }));
        const appointments = (appointmentsRes.code === 200 ? appointmentsRes.data.list || [] : []).map(a => ({
          ...a,
          _type: 'service',
          userNickname: a.userNickname || null
        }));

        const all = [...orders, ...appointments];
        all.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
        this.allRawOrders = all;
        this.updateStatistics(all);
      } catch (error) {
        console.error('加载失败', error);
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

      list.forEach(item => {
        const status = item.orderStatus !== undefined ? item.orderStatus : item.status;
        if (status === 0) pendingCount++;
        if (status === 3) completedCount++;
        const amount = item.payAmount !== undefined ? item.payAmount : item.servicePrice;
        totalAmount += amount || 0;
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
    },

    handleReset() {
      this.searchKeyword = '';
      this.searchStatus = null;
      this.page = 1;
    },

    handlePageChange(page) {
      this.page = page;
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
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

    getAppointmentStatusText(status) {
      const map = { 0: '待确认', 1: '已确认', 2: '服务中', 3: '已完成', 4: '已取消', 5: '已拒绝' };
      return map[status] || '未知';
    },

    getAppointmentStatusType(status) {
      const map = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger', 5: 'danger' };
      return map[status] || 'info';
    },

    async handleDetail(row) {
      if (row._type === 'product') {
        try {
          const orderNo = row.orderNo;
          if (!orderNo) {
            this.$message.error('订单号不存在');
            return;
          }
          const res = await getOrderDetail(orderNo);
          if (res.code === 200) {
            this.currentOrder = res.data;
            this.detailVisible = true;
          } else {
            this.$message.error(res.message || '获取详情失败');
          }
        } catch (error) {
          console.error('获取订单详情失败', error);
          this.$message.error('获取详情失败');
        }
      } else {
        this.currentAppointment = row;
        this.appointmentDetailVisible = true;
      }
    },

    async handleShip(row) {
      this.$confirm(`确认发货订单 ${row.orderNo} 吗？`, '提示', { type: 'info' }).then(async () => {
        try {
          const res = await updateOrderStatus(row.orderNo, 2);
          if (res.code === 200) {
            this.$message.success('发货成功');
            await this.loadAllOrders();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('发货失败', error);
          this.$message.error('发货失败');
        }
      }).catch(() => {});
    },
// 单个删除
    async handleDelete(row) {
      this.$confirm(`确定要删除吗？删除后无法恢复！`, '提示', { type: 'warning' }).then(async () => {
        try {
          if (row._type === 'product') {
            await deleteOrder(row.id);
          } else {
            await realDeleteAppointment(row.id);  // ✅ 调用真实删除
          }
          this.$message.success('删除成功');
          await this.loadAllOrders();
        } catch (error) {
          console.error('删除失败', error);
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

// 批量删除
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个订单吗？删除后无法恢复！`, '警告', { type: 'warning' }).then(async () => {
        try {
          const productRows = this.selectedRows.filter(row => row._type === 'product');
          const serviceRows = this.selectedRows.filter(row => row._type === 'service');

          // 删除商品订单
          for (const row of productRows) {
            await deleteOrder(row.id);
          }

          // 删除服务预约
          if (serviceRows.length > 0) {
            const ids = serviceRows.map(row => row.id).join(',');
            await batchRealDeleteAppointments(ids);
          }

          this.$message.success('删除成功');
          this.selectedRows = [];
          await this.loadAllOrders();
        } catch (error) {
          console.error('批量删除失败', error);
          this.$message.error('批量删除失败');
        }
      }).catch(() => {});
    },

    async handleExport() {
      this.exportLoading = true;
      try {
        let exportData = [];

        if (this.orderType === 'product') {
          const res = await exportOrderList({});
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
        } else if (this.orderType === 'service') {
          const res = await exportAppointmentList({});
          const blob = new Blob([res], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
          });
          const link = document.createElement('a');
          const url = URL.createObjectURL(blob);
          link.href = url;
          link.setAttribute('download', `预约列表_${new Date().getTime()}.xlsx`);
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(url);
        } else {
          exportData = this.displayList.map(item => ({
            '订单号': item.orderNo || item.appointmentNo,
            '类型': item._type === 'product' ? '商品订单' : '服务预约',
            '用户': item.userNickname || item.userName,
            '金额': item.payAmount || item.servicePrice,
            '状态': item._type === 'product' ? this.getStatusText(item.orderStatus) : this.getAppointmentStatusText(item.status),
            '支付状态': item.payStatus === 1 ? '已支付' : '未支付',
            '创建时间': this.formatDate(item.createTime)
          }));
          const blob = new Blob([this.convertToCSV(exportData)], { type: 'text/csv;charset=utf-8;' });
          const link = document.createElement('a');
          const url = URL.createObjectURL(blob);
          link.href = url;
          link.setAttribute('download', `全部订单_${new Date().getTime()}.csv`);
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(url);
        }

        this.$message.success('导出成功');
      } catch (error) {
        console.error('导出失败', error);
        this.$message.error('导出失败');
      } finally {
        this.exportLoading = false;
      }
    },

    convertToCSV(data) {
      if (!data || data.length === 0) return '';
      const headers = Object.keys(data[0]);
      const csvRows = [];
      csvRows.push(headers.join(','));
      for (const row of data) {
        const values = headers.map(header => {
          let value = row[header] || '';
          if (typeof value === 'string' && (value.includes(',') || value.includes('"'))) {
            value = `"${value.replace(/"/g, '""')}"`;
          }
          return value;
        });
        csvRows.push(values.join(','));
      }
      return csvRows.join('\n');
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

/* 操作栏 */
.action-bar {
  background: #fff;
  border-radius: 16px;
  padding: 12px 20px;
  margin: 20px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-left {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.action-right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

/* 订单类型切换标签 */
.type-tabs {
  display: flex;
  gap: 4px;
  background: #f5f7fa;
  border-radius: 30px;
  padding: 3px;
}

.type-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 30px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s;
}

.type-tab i {
  font-size: 14px;
}

.type-tab:hover {
  color: #667eea;
}

.type-tab.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
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
  border-radius: 30px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.search-input ::v-deep .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.status-select {
  width: 130px;
}

.status-select ::v-deep .el-input__inner {
  border-radius: 30px;
}

.search-btn, .reset-btn, .export-btn {
  border-radius: 30px;
  padding: 7px 18px;
  font-weight: 500;
  transition: all 0.3s;
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
}

.export-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
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
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
}

.user-phone {
  font-size: 11px;
  color: #999;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-weight: 500;
  color: #2c3e50;
}

.product-count {
  font-size: 11px;
  color: #999;
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
  overflow-x: hidden;
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

.price {
  color: #ff6b6b;
  font-weight: 600;
}

.cancel-text {
  color: #f56c6c;
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
@media (max-width: 1200px) {
  .action-right {
    flex-wrap: wrap;
  }
}

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