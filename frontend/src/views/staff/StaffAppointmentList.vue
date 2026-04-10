<template>
  <div class="appointment-list">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-s-order"></i></div>
          <div class="stat-info"><div class="stat-number">{{ statistics.total || 0 }}</div><div class="stat-label">总预约</div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-pending"><i class="el-icon-time"></i></div>
          <div class="stat-info"><div class="stat-number">{{ statistics.pending || 0 }}</div><div class="stat-label">待确认</div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-confirmed"><i class="el-icon-check"></i></div>
          <div class="stat-info"><div class="stat-number">{{ statistics.confirmed || 0 }}</div><div class="stat-label">已确认</div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-income"><i class="el-icon-money"></i></div>
          <div class="stat-info"><div class="stat-number">¥{{ statistics.totalIncome || 0 }}</div><div class="stat-label">总收入</div></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left"></div>
      <div class="action-right">
        <div class="search-wrapper"><i class="el-icon-search search-icon"></i><el-input v-model="searchKeyword" placeholder="搜索订单号/用户/宠物" clearable size="medium" @keyup.enter="handleSearch" class="search-input"></el-input></div>
        <el-select v-model="searchStatus" placeholder="预约状态" clearable size="medium" class="status-select" @change="handleSearch">
          <el-option label="待确认" :value="0"></el-option><el-option label="已确认" :value="1"></el-option>
          <el-option label="服务中" :value="2"></el-option><el-option label="已完成" :value="3"></el-option>
          <el-option label="已取消" :value="4"></el-option><el-option label="已拒绝" :value="5"></el-option>
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="medium" @change="handleSearch"></el-date-picker>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 预约表格 -->
    <el-table v-loading="loading" :data="appointmentList" stripe class="appointment-table">
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="appointmentNo" label="预约编号" width="180"></el-table-column>
      <el-table-column label="用户信息" width="150">
        <template slot-scope="scope"><div class="user-name">{{ scope.row.userName }}</div><div class="user-id">ID: {{ scope.row.userId }}</div></template>
      </el-table-column>
      <el-table-column label="宠物" width="120"><template slot-scope="scope">{{ scope.row.petName || '--' }}</template></el-table-column>
      <el-table-column label="服务" min-width="150"><template slot-scope="scope">{{ scope.row.serviceName }}</template></el-table-column>
      <el-table-column label="预约时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.appointmentDate) }} {{ scope.row.appointmentTime }}
        </template>
      </el-table-column>
      <el-table-column prop="servicePrice" label="金额" width="100" align="center"><template slot-scope="scope">¥{{ scope.row.servicePrice }}</template></el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope"><span :class="getStatusClass(scope.row.status)">{{ getStatusText(scope.row.status) }}</span></template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160"><template slot-scope="scope">{{ formatDate(scope.row.createTime) }}</template></el-table-column>
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button size="small" type="primary" plain circle @click="handleDetail(scope.row)" title="详情"><i class="el-icon-view"></i></el-button>
            <el-button v-if="scope.row.status === 0" size="small" type="success" plain circle @click="handleConfirm(scope.row)" title="确认"><i class="el-icon-check"></i></el-button>
            <el-button v-if="scope.row.status === 1" size="small" type="primary" plain circle @click="handleStart(scope.row)" title="开始服务"><i class="el-icon-caret-right"></i></el-button>
            <el-button v-if="scope.row.status === 2" size="small" type="success" plain circle @click="handleComplete(scope.row)" title="完成"><i class="el-icon-circle-check"></i></el-button>
            <el-button v-if="scope.row.status === 0" size="small" type="danger" plain circle @click="handleReject(scope.row)" title="拒绝"><i class="el-icon-close"></i></el-button>
            <el-button v-if="scope.row.status === 0 || scope.row.status === 1" size="small" type="warning" plain circle @click="handleCancel(scope.row)" title="取消"><i class="el-icon-switch-button"></i></el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper"><el-pagination @size-change="handleSizeChange" @current-change="handlePageChange" :current-page="page" :page-sizes="[10, 20, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total" background></el-pagination></div>

    <!-- 详情对话框 -->
    <el-dialog title="预约详情" :visible.sync="detailVisible" width="600px" center>
      <div class="detail-content" v-if="currentAppointment">
        <div class="detail-row"><span class="label">预约编号：</span><span class="value">{{ currentAppointment.appointmentNo }}</span></div>
        <div class="detail-row"><span class="label">用户信息：</span><span class="value">{{ currentAppointment.userName }} (ID: {{ currentAppointment.userId }})</span></div>
        <div class="detail-row"><span class="label">宠物：</span><span class="value">{{ currentAppointment.petName || '--' }}</span></div>
        <div class="detail-row"><span class="label">服务：</span><span class="value">{{ currentAppointment.serviceName }}</span></div>
        <div class="detail-row"><span class="label">金额：</span><span class="value price">¥{{ currentAppointment.servicePrice }}</span></div>
        <div class="detail-row"><span class="label">预约时间：</span><span class="value">{{ formatDate(currentAppointment.appointmentDate) }} {{ currentAppointment.appointmentTime }}</span></div>
        <div class="detail-row"><span class="label">状态：</span><span class="value" :class="getStatusClass(currentAppointment.status)">{{ getStatusText(currentAppointment.status) }}</span></div>
        <div class="detail-row"><span class="label">下单时间：</span><span class="value">{{ formatDate(currentAppointment.createTime) }}</span></div>
        <div class="detail-row" v-if="currentAppointment.remark"><span class="label">备注：</span><span class="value">{{ currentAppointment.remark }}</span></div>
        <div class="detail-row" v-if="currentAppointment.cancelReason"><span class="label">取消/拒绝原因：</span><span class="value">{{ currentAppointment.cancelReason }}</span></div>
        <div class="detail-row" v-if="currentAppointment.staffName"><span class="label">处理员工：</span><span class="value">{{ currentAppointment.staffName }}</span></div>
      </div>
      <span slot="footer"><el-button type="primary" @click="detailVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog title="拒绝预约" :visible.sync="rejectVisible" width="400px" center>
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因（选填）"></el-input>
      <span slot="footer"><el-button @click="rejectVisible = false">取消</el-button><el-button type="primary" @click="submitReject" :loading="rejectLoading">确定</el-button></span>
    </el-dialog>

    <!-- 取消对话框 -->
    <el-dialog title="取消预约" :visible.sync="cancelVisible" width="400px" center>
      <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因（选填）"></el-input>
      <span slot="footer"><el-button @click="cancelVisible = false">取消</el-button><el-button type="primary" @click="submitCancel" :loading="cancelLoading">确定</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminAppointmentList, getAppointmentDetailForAdmin, confirmAppointment, startAppointment, completeAppointment, rejectAppointment, cancelAppointmentByAdmin, getAppointmentStatistics } from '@/api/service';

export default {
  name: 'AppointmentList',
  data() {
    return {
      loading: false, rejectLoading: false, cancelLoading: false,
      appointmentList: [], total: 0, page: 1, pageSize: 10,
      searchKeyword: '', searchStatus: '', dateRange: [],
      statistics: { total: 0, pending: 0, confirmed: 0, totalIncome: 0 },
      detailVisible: false, rejectVisible: false, cancelVisible: false,
      currentAppointment: null, currentId: null, rejectReason: '', cancelReason: ''
    };
  },
  created() { this.loadList(); this.loadStatistics(); },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = { page: this.page, pageSize: this.pageSize, keyword: this.searchKeyword || undefined, status: this.searchStatus !== '' ? this.searchStatus : undefined, startDate: this.dateRange?.[0], endDate: this.dateRange?.[1] };
        const res = await getAdminAppointmentList(params);
        if (res.code === 200) { this.appointmentList = res.data.list || []; this.total = res.data.total || 0; }
      } catch (error) { this.$message.error('加载失败'); }
      finally { this.loading = false; }
    },
    async loadStatistics() {
      try { const res = await getAppointmentStatistics(); if (res.code === 200) this.statistics = res.data; } catch (error) {}
    },
    getStatusText(status) { const map = { 0: '待确认', 1: '已确认', 2: '服务中', 3: '已完成', 4: '已取消', 5: '已拒绝' }; return map[status] || '未知'; },
    getStatusClass(status) { const map = { 0: 'status-pending', 1: 'status-confirmed', 2: 'status-service', 3: 'status-completed', 4: 'status-cancelled', 5: 'status-rejected' }; return map[status] || ''; },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      // 只返回：YYYY-MM-DD，不返回后面的 00:00
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },
    handleSearch() { this.page = 1; this.loadList(); this.loadStatistics(); },
    handleReset() { this.searchKeyword = ''; this.searchStatus = ''; this.dateRange = []; this.page = 1; this.loadList(); },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    async handleDetail(row) { this.currentAppointment = row; this.detailVisible = true; },
    async handleConfirm(row) { this.$confirm('确认该预约吗？', '提示', { type: 'info' }).then(async () => { await confirmAppointment(row.id); this.$message.success('确认成功'); this.loadList(); this.loadStatistics(); }).catch(() => {}); },
    async handleStart(row) { this.$confirm('开始服务吗？', '提示', { type: 'info' }).then(async () => { await startAppointment(row.id); this.$message.success('服务已开始'); this.loadList(); }).catch(() => {}); },
    async handleComplete(row) { this.$confirm('完成该服务吗？', '提示', { type: 'info' }).then(async () => { await completeAppointment(row.id); this.$message.success('已完成'); this.loadList(); this.loadStatistics(); }).catch(() => {}); },
    handleReject(row) { this.currentId = row.id; this.rejectReason = ''; this.rejectVisible = true; },
    handleCancel(row) { this.currentId = row.id; this.cancelReason = ''; this.cancelVisible = true; },
    async submitReject() { this.rejectLoading = true; try { await rejectAppointment(this.currentId, this.rejectReason); this.$message.success('已拒绝'); this.rejectVisible = false; this.loadList(); this.loadStatistics(); } catch (error) { this.$message.error('操作失败'); } finally { this.rejectLoading = false; } },
    async submitCancel() { this.cancelLoading = true; try { await cancelAppointmentByAdmin(this.currentId, this.cancelReason); this.$message.success('已取消'); this.cancelVisible = false; this.loadList(); this.loadStatistics(); } catch (error) { this.$message.error('操作失败'); } finally { this.cancelLoading = false; } }
  }
};
</script>

<style scoped>
.appointment-list { padding: 20px; background: #f5f7fa; min-height: 100%; }
.stat-card { border-radius: 16px; }
.stat-card ::v-deep .el-card__body { display: flex; align-items: center; padding: 20px; }
.stat-icon { width: 50px; height: 50px; border-radius: 12px; background: linear-gradient(135deg, #667eea, #764ba2); display: flex; align-items: center; justify-content: center; margin-right: 15px; }
.stat-icon i { font-size: 24px; color: white; }
.stat-pending { background: linear-gradient(135deg, #e6a23c, #f0b45c); }
.stat-confirmed { background: linear-gradient(135deg, #409EFF, #66b1ff); }
.stat-income { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-number { font-size: 28px; font-weight: bold; color: #2c3e50; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.action-bar { background: #fff; border-radius: 16px; padding: 16px 24px; margin: 20px 0; display: flex; justify-content: flex-end; flex-wrap: wrap; gap: 16px; }
.action-right { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.search-wrapper { position: relative; display: inline-block; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #909399; font-size: 16px; z-index: 1; }
.search-input { width: 200px; }
.search-input ::v-deep .el-input__inner { padding-left: 36px; border-radius: 8px; }
.status-select { width: 120px; }
.search-btn { background: linear-gradient(135deg, #667eea, #764ba2); border: none; color: white; border-radius: 8px; padding: 8px 16px; }
.appointment-table { background: #fff; border-radius: 16px; overflow: hidden; }
.appointment-table ::v-deep .el-table__header th { background: #f8f9fc; color: #2c3e50; font-weight: 600; font-size: 13px; padding: 14px 0; }
.user-name { font-weight: 600; color: #2c3e50; }
.user-id { font-size: 11px; color: #c0c4cc; }
.status-pending { color: #e6a23c; }
.status-confirmed { color: #409EFF; }
.status-service { color: #67c23a; }
.status-completed { color: #909399; }
.status-cancelled { color: #f56c6c; }
.status-rejected { color: #f56c6c; }
.action-buttons { display: flex; justify-content: center; gap: 8px; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; background: #fff; padding: 16px 24px; border-radius: 16px; }
.detail-content { padding: 10px; }
.detail-row { display: flex; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.detail-row .label { width: 100px; color: #909399; }
.detail-row .value { flex: 1; color: #2c3e50; }
.detail-row .value.price { color: #ff6b6b; font-weight: bold; }
@media (max-width: 768px) { .appointment-list { padding: 12px; } .action-bar { justify-content: center; } .search-input { width: 160px; } .action-right { justify-content: center; } }
</style>