<template>
  <div class="order-list-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-s-order"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总订单数</div>
          <div class="stat-value">{{ statistics.totalOrders }}</div>
          <div class="stat-sub">累计订单总量</div>
        </div>
      </div>
      <div class="stat-card card-pending">
        <div class="stat-icon">
          <i class="el-icon-time"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">待支付</div>
          <div class="stat-value">{{ statistics.pendingCount }}</div>
          <div class="stat-sub">等待支付中</div>
        </div>
      </div>
      <div class="stat-card card-completed">
        <div class="stat-icon">
          <i class="el-icon-check"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ statistics.completedCount }}</div>
          <div class="stat-sub">已完成处理</div>
        </div>
      </div>
      <div class="stat-card card-revenue">
        <div class="stat-icon">
          <i class="el-icon-money"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总销售额</div>
          <div class="stat-value">¥{{ statistics.totalAmount }}</div>
          <div class="stat-sub">累计销售金额</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <div class="type-tabs">
          <div class="type-tab" :class="{ active: orderType === 'all' }" @click="orderType = 'all'">
            <i class="el-icon-menu"></i> 全部
          </div>
          <div class="type-tab" :class="{ active: orderType === 'service' }" @click="orderType = 'service'">
            <i class="el-icon-service"></i> 服务
          </div>
          <div class="type-tab" :class="{ active: orderType === 'product' }" @click="orderType = 'product'">
            <i class="el-icon-goods"></i> 商品
          </div>
        </div>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号/用户名"
          clearable
          size="small"
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="filter-search"
        />
        <el-select
          v-model="searchStatus"
          :placeholder="statusPlaceholder"
          clearable
          size="small"
          class="filter-select"
          @change="handleSearch"
        >
          <el-option
            v-for="opt in statusOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <el-button size="small" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="small" @click="handleReset">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button size="small" @click="handleExport" :loading="exportLoading" class="export-btn">
          <i class="el-icon-download"></i> 导出
        </el-button>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <transition name="batch-bar">
      <div v-if="selectedRows.length > 0" class="batch-bar">
        <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
        <el-button size="small" @click="handleBatchDelete" class="batch-btn-delete">批量删除</el-button>
      </div>
    </transition>

    <!-- 订单表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="displayList"
        stripe
        row-key="id"
        :reserve-selection="true"
        @selection-change="handleSelectionChange"
        :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="订单号/预约号" min-width="200">
          <template slot-scope="scope">
            <span class="order-no">{{ scope.row.orderNo || scope.row.appointmentNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row._type === 'product' ? '' : 'success'" size="small">
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
            <div v-if="scope.row._type === 'product' && scope.row.items && scope.row.items[0]" class="item-info">
              <div class="item-name">{{ scope.row.items[0].productName }}</div>
              <div class="item-sub">共 {{ scope.row.items.length }} 件商品</div>
            </div>
            <div v-else class="item-info">
              <div class="item-name">{{ scope.row.serviceName }}</div>
              <div class="item-sub">宠物：{{ scope.row.petName || '未指定' }}</div>
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
            <el-button type="text" @click="handleDetail(scope.row)" class="act-view">查看</el-button>
            <el-button
              v-if="scope.row._type === 'product' && scope.row.orderStatus === 1"
              type="text"
              @click="handleShip(scope.row)"
              class="act-ship"
            >发货</el-button>
            <el-button type="text" @click="handleDelete(scope.row)" class="act-del">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-card" v-if="total > 0">
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

    <!-- 商品订单详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="detailVisible" width="750px" class="order-detail-dialog">
      <div class="order-detail" v-if="currentOrder">
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <div class="detail-grid">
            <div class="d-item"><span class="d-label">订单号</span><span class="d-value">{{ currentOrder.orderNo }}</span></div>
            <div class="d-item"><span class="d-label">下单时间</span><span class="d-value">{{ formatDate(currentOrder.createTime) }}</span></div>
            <div class="d-item">
              <span class="d-label">订单状态</span>
              <el-tag :type="getStatusType(currentOrder.orderStatus)" size="small">{{ getStatusText(currentOrder.orderStatus) }}</el-tag>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-location"></i>
            <span>收货信息</span>
          </div>
          <div class="detail-grid">
            <div class="d-item"><span class="d-label">收货人</span><span class="d-value">{{ currentOrder.userName }}</span></div>
            <div class="d-item"><span class="d-label">手机号</span><span class="d-value">{{ currentOrder.userPhone }}</span></div>
          </div>
          <div class="d-item d-item-full"><span class="d-label">收货地址</span><span class="d-value">{{ currentOrder.userAddress }}</span></div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-goods"></i>
            <span>商品清单</span>
          </div>
          <el-table :data="currentOrder.items || []" border size="small" class="items-table">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="price" label="单价" width="100" align="center">
              <template slot-scope="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
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
        <el-button @click="detailVisible = false" class="warm-btn">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 服务预约详情对话框 -->
    <el-dialog title="预约详情" :visible.sync="appointmentDetailVisible" width="600px" class="order-detail-dialog">
      <div class="order-detail" v-if="currentAppointment">
        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <div class="detail-grid">
            <div class="d-item"><span class="d-label">预约编号</span><span class="d-value">{{ currentAppointment.appointmentNo }}</span></div>
            <div class="d-item">
              <span class="d-label">预约状态</span>
              <el-tag :type="getAppointmentStatusType(currentAppointment.status)" size="small">{{ getAppointmentStatusText(currentAppointment.status) }}</el-tag>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-service"></i>
            <span>服务信息</span>
          </div>
          <div class="detail-grid">
            <div class="d-item"><span class="d-label">服务名称</span><span class="d-value">{{ currentAppointment.serviceName }}</span></div>
            <div class="d-item"><span class="d-label">服务金额</span><span class="d-value price">¥{{ currentAppointment.servicePrice }}</span></div>
            <div class="d-item"><span class="d-label">服务人员</span><span class="d-value">{{ currentAppointment.staffName || '待分配' }}</span></div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-s-custom"></i>
            <span>宠物信息</span>
          </div>
          <div class="detail-grid">
            <div class="d-item"><span class="d-label">宠物名称</span><span class="d-value">{{ currentAppointment.petName || '未指定' }}</span></div>
            <div class="d-item"><span class="d-label">预约时间</span><span class="d-value">{{ formatDate(currentAppointment.appointmentDate) }} {{ currentAppointment.appointmentTime }}</span></div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-user"></i>
            <span>用户信息</span>
          </div>
          <div class="detail-grid">
            <div class="d-item"><span class="d-label">用户昵称</span><span class="d-value">{{ currentAppointment.userNickname || currentAppointment.userName }}</span></div>
            <div class="d-item"><span class="d-label">联系方式</span><span class="d-value">{{ currentAppointment.userPhone || '--' }}</span></div>
            <div class="d-item" v-if="currentAppointment.remark"><span class="d-label">备注</span><span class="d-value">{{ currentAppointment.remark }}</span></div>
            <div class="d-item" v-if="currentAppointment.cancelReason"><span class="d-label">取消原因</span><span class="d-value cancel-text">{{ currentAppointment.cancelReason }}</span></div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <i class="el-icon-time"></i>
            <span>时间信息</span>
          </div>
          <div class="detail-grid">
            <div class="d-item"><span class="d-label">下单时间</span><span class="d-value">{{ formatDate(currentAppointment.createTime) }}</span></div>
            <div class="d-item" v-if="currentAppointment.payTime"><span class="d-label">支付时间</span><span class="d-value">{{ formatDate(currentAppointment.payTime) }}</span></div>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="appointmentDetailVisible = false" class="warm-btn">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminOrderList, getOrderDetail, updateOrderStatus, exportOrderList, deleteOrder } from '@/api/order';
import { getAdminAppointmentList, getAppointmentDetailForAdmin, exportAppointmentList, realDeleteAppointment, batchRealDeleteAppointments } from '@/api/service';

export default {
  name: 'Admin_OrderList',
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
      const start = (this.page - 1) * this.pageSize;
      const end = start + this.pageSize;
      this.total = this.allRawOrders.length;
      return this.allRawOrders.slice(start, end);
    },
    statusOptions() {
      if (this.orderType === 'all') {
        return [
          { label: '全部', value: '' },
          { label: '已支付', value: 1 },
          { label: '未支付', value: 0 }
        ];
      }
      if (this.orderType === 'product') {
        return [
          { label: '全部', value: '' },
          { label: '待支付', value: 0 },
          { label: '已支付', value: 1 },
          { label: '已发货', value: 2 },
          { label: '已完成', value: 3 },
          { label: '已取消', value: 4 },
          { label: '已退款', value: 5 }
        ];
      }
      // service
      return [
        { label: '全部', value: '' },
        { label: '待确认', value: 0 },
        { label: '已确认', value: 1 },
        { label: '服务中', value: 2 },
        { label: '已完成', value: 3 },
        { label: '已取消', value: 4 },
        { label: '已拒绝', value: 5 }
      ];
    },
    statusPlaceholder() {
      if (this.orderType === 'all') return '支付状态';
      if (this.orderType === 'product') return '订单状态';
      return '预约状态';
    }
  },
  watch: {
    orderType() {
      this.searchStatus = '';
      this.page = 1;
      this.selectedRows = [];
      this.loadData();
    },
    searchKeyword() {
      this.page = 1;
    },
    searchStatus() {
      this.page = 1;
    }
  },
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const baseParams = {
          page: 1,
          pageSize: 10000,
          keyword: this.searchKeyword || undefined
        };
        const statusVal = this.searchStatus !== '' ? this.searchStatus : undefined;

        let orders = [];
        let appointments = [];

        const loadProduct = this.orderType === 'all' || this.orderType === 'product';
        const loadService = this.orderType === 'all' || this.orderType === 'service';

        if (loadProduct) {
          const params = { ...baseParams };
          if (this.orderType === 'all') {
            params.payStatus = statusVal;
          } else {
            params.orderStatus = statusVal;
          }
          const res = await getAdminOrderList(params);
          if (res.code === 200) {
            orders = (res.data.list || []).map(o => ({ ...o, _type: 'product' }));
          }
        }

        if (loadService) {
          const params = { ...baseParams };
          if (this.orderType === 'all') {
            params.payStatus = statusVal;
          } else {
            params.status = statusVal;
          }
          const res = await getAdminAppointmentList(params);
          if (res.code === 200) {
            appointments = (res.data.list || []).map(a => ({ ...a, _type: 'service' }));
          }
        }

        const all = [...orders, ...appointments];
        all.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
        this.allRawOrders = all;
        this.updateStatistics();
      } catch (error) {
        this.$message.error(error?.message || '加载失败');
      } finally {
        this.loading = false;
      }
    },

    updateStatistics() {
      let pendingCount = 0;
      let completedCount = 0;
      let totalAmount = 0;

      this.allRawOrders.forEach(item => {
        const status = item.orderStatus !== undefined ? item.orderStatus : item.status;
        if (status === 0) pendingCount++;
        if (status === 3) completedCount++;
        if (item.payStatus === 1) {
          totalAmount += (item.payAmount || item.servicePrice || 0);
        }
      });

      this.statistics = {
        totalOrders: this.allRawOrders.length,
        pendingCount,
        completedCount,
        totalAmount: totalAmount.toFixed(2)
      };
    },

    tableRowClassName({ rowIndex }) {
      return rowIndex % 2 === 0 ? 'even-row' : '';
    },

    handleSearch() {
      this.page = 1;
      this.loadData();
    },

    handleReset() {
      this.searchKeyword = '';
      this.searchStatus = null;
      this.page = 1;
      this.loadData();
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
      const map = { 0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'danger', 5: 'danger' };
      return map[status] || 'info';
    },

    getAppointmentStatusText(status) {
      const map = { 0: '待确认', 1: '已确认', 2: '服务中', 3: '已完成', 4: '已取消', 5: '已拒绝' };
      return map[status] || '未知';
    },

    getAppointmentStatusType(status) {
      const map = { 0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'danger', 5: 'danger' };
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
          this.$message.error(error?.message || '获取详情失败');
        }
      } else {
        try {
          const res = await getAppointmentDetailForAdmin(row.id);
          if (res.code === 200) {
            this.currentAppointment = res.data;
          } else {
            this.currentAppointment = row;
          }
        } catch (error) {
          this.currentAppointment = row;
        }
        this.appointmentDetailVisible = true;
      }
    },

    async handleShip(row) {
      this.$confirm(`确认发货订单 ${row.orderNo} 吗？`, '提示', { type: 'info' }).then(async () => {
        try {
          const res = await updateOrderStatus(row.orderNo, 2);
          if (res.code === 200) {
            this.$message.success('发货成功');
            await this.loadData();
          } else {
            this.$message.error(res.message || '发货失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '发货失败');
        }
      }).catch(() => {});
    },

    async handleDelete(row) {
      this.$confirm('确定要删除吗？删除后无法恢复！', '提示', { type: 'warning' }).then(async () => {
        try {
          if (row._type === 'product') {
            await deleteOrder(row.id);
          } else {
            await realDeleteAppointment(row.id);
          }
          this.$message.success('删除成功');
          await this.loadData();
        } catch (error) {
          this.$message.error(error?.message || '删除失败');
        }
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个订单吗？删除后无法恢复！`, '警告', { type: 'warning' }).then(async () => {
        try {
          const productRows = this.selectedRows.filter(row => row._type === 'product');
          const serviceRows = this.selectedRows.filter(row => row._type === 'service');

          for (const row of productRows) {
            await deleteOrder(row.id);
          }

          if (serviceRows.length > 0) {
            const ids = serviceRows.map(row => row.id).join(',');
            await batchRealDeleteAppointments(ids);
          }

          this.$message.success('删除成功');
          this.selectedRows = [];
          await this.loadData();
        } catch (error) {
          this.$message.error(error?.message || '批量删除失败');
        }
      }).catch(() => {});
    },

    async handleExport() {
      this.exportLoading = true;
      try {
        if (this.orderType === 'product') {
          const res = await exportOrderList({});
          this.downloadBlob(res, '订单列表');
        } else if (this.orderType === 'service') {
          const res = await exportAppointmentList({});
          this.downloadBlob(res, '预约列表');
        } else {
          // 全部导出：使用当前加载的数据导出为 CSV（两个后端格式不统一无法合并 Excel）
          const exportData = this.allRawOrders.map(item => ({
            '订单号/预约号': item.orderNo || item.appointmentNo,
            '类型': item._type === 'product' ? '商品订单' : '服务预约',
            '用户': item.userNickname || item.userName,
            '金额': item.payAmount || item.servicePrice,
            '状态': item._type === 'product' ? this.getStatusText(item.orderStatus) : this.getAppointmentStatusText(item.status),
            '支付状态': item.payStatus === 1 ? '已支付' : '未支付',
            '创建时间': this.formatDate(item.createTime)
          }));
          const blob = new Blob(['﻿' + this.convertToCSV(exportData)], { type: 'text/csv;charset=utf-8;' });
          this.downloadBlob(blob, '全部订单', 'csv');
        }

        this.$message.success('导出成功');
      } catch (error) {
        this.$message.error(error?.message || '导出失败');
      } finally {
        this.exportLoading = false;
      }
    },

    downloadBlob(blob, name, ext) {
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      link.href = url;
      link.setAttribute('download', `${name}_${new Date().getTime()}.${ext || 'xlsx'}`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
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
/* ===== 页面基底 ===== */
.order-list-page {
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
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(61, 46, 42, 0.10);
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
.card-total .stat-icon     { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-pending .stat-icon   { background: linear-gradient(135deg, #e6a23c, #f7c06b); }
.card-completed .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-revenue .stat-icon   { background: linear-gradient(135deg, #409eff, #66b1ff); }

.stat-info { position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 6px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3d2e2a; letter-spacing: -0.5px; }

.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ===== 筛选栏 ===== */
.filter-card {
  background: #fff;
  border-radius: 16px;
  padding: 12px 20px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.filter-left {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-search { width: 200px; }
.filter-select { width: 150px; }

/* ===== 类型切换 ===== */
.type-tabs {
  display: flex;
  gap: 2px;
  background: #fdf6f0;
  border-radius: 10px;
  padding: 3px;
}
.type-tab {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #a08c84;
  transition: all 0.25s;
}
.type-tab i { font-size: 14px; }
.type-tab:hover { color: #f59e4b; }
.type-tab.active {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  color: #fff;
  box-shadow: 0 2px 8px rgba(245, 158, 75, 0.25);
}

/* ===== 按钮 ===== */
.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  font-weight: 500;
}
.warm-btn:hover { opacity: 0.88; }

.export-btn {
  background: linear-gradient(135deg, #67c23a, #85ce61) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  font-weight: 500;
}
.export-btn:hover { opacity: 0.88; }

/* ===== 批量操作栏 ===== */
.batch-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 20px;
  background: #fff;
  border-radius: 14px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.06);
}
.batch-info { font-size: 13px; color: #a08c84; }
.batch-btn-delete {
  background: linear-gradient(135deg, #f56c6c, #e05050) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 8px !important;
}

.batch-bar-enter-active,
.batch-bar-leave-active { transition: all 0.25s ease; }
.batch-bar-enter,
.batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 表格 ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.table-card ::v-deep .el-table__header th {
  background: #fefbf8;
  color: #3d2e2a;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
  border-bottom: 1px solid #f5ece6;
}

.table-card ::v-deep .el-table__body tr:hover > td {
  background: #fefbf8;
}

.table-card ::v-deep .el-table__body td {
  padding: 14px 0;
  font-size: 13px;
}

.order-no {
  font-family: monospace;
  font-size: 13px;
  color: #f59e4b;
  font-weight: 500;
}

.user-info { display: flex; flex-direction: column; gap: 2px; }
.user-name { font-weight: 500; color: #3d2e2a; }
.user-phone { font-size: 11px; color: #a08c84; }

.item-info { display: flex; flex-direction: column; gap: 4px; }
.item-name { font-weight: 500; color: #3d2e2a; }
.item-sub { font-size: 11px; color: #a08c84; }

.amount-text { color: #f56c6c; font-weight: 600; font-size: 15px; }
.time-text { color: #5c4a42; font-size: 12px; }

/* ===== 操作按钮 ===== */
.act-view { color: #f59e4b !important; }
.act-view:hover { color: #e8833a !important; }
.act-ship { color: #67c23a !important; }
.act-ship:hover { color: #529b1e !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #d94343 !important; }

/* ===== 分页 ===== */
.pagination-card {
  margin-top: 16px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 16px;
  display: flex;
  justify-content: flex-end;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

/* ===== 详情对话框 ===== */
.order-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.order-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 20px 24px;
  margin: 0;
}

.order-detail-dialog ::v-deep .el-dialog__title {
  color: #fff;
  font-weight: 600;
  font-size: 18px;
}

.order-detail-dialog ::v-deep .el-dialog__close {
  color: #fff;
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
  border-bottom: 1px solid #f5ece6;
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
  color: #3d2e2a;
  margin-bottom: 12px;
}
.section-title i { color: #f59e4b; font-size: 16px; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 24px;
}

.d-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.d-label {
  font-size: 13px;
  color: #a08c84;
  white-space: nowrap;
}

.d-value {
  font-size: 13px;
  color: #3d2e2a;
}

.price { color: #f56c6c; font-weight: 600; }
.cancel-text { color: #f56c6c; }

.items-table {
  margin-top: 5px;
}

.total-section {
  text-align: right;
  font-size: 16px;
  color: #3d2e2a;
}
.total-section strong {
  color: #f56c6c;
  font-size: 20px;
  margin-left: 10px;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .filter-right { flex-wrap: wrap; }
}

@media (max-width: 768px) {
  .order-list-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .filter-search { width: 180px; }
  .pagination-card { justify-content: center; }
}
</style>
