<template>
  <div class="appointment-list-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon"><i class="el-icon-s-order" /></div>
        <div class="stat-info">
          <div class="stat-label">总预约</div>
          <div class="stat-value">{{ statistics.total || 0 }}</div>
          <div class="stat-sub">全部预约记录</div>
        </div>
      </div>
      <div class="stat-card card-pending">
        <div class="stat-icon"><i class="el-icon-time" /></div>
        <div class="stat-info">
          <div class="stat-label">待确认</div>
          <div class="stat-value">{{ statistics.pending || 0 }}</div>
          <div class="stat-sub">等待处理</div>
        </div>
      </div>
      <div class="stat-card card-confirmed">
        <div class="stat-icon"><i class="el-icon-check" /></div>
        <div class="stat-info">
          <div class="stat-label">已确认</div>
          <div class="stat-value">{{ statistics.confirmed || 0 }}</div>
          <div class="stat-sub">已分配员工</div>
        </div>
      </div>
      <div class="stat-card card-income">
        <div class="stat-icon"><i class="el-icon-money" /></div>
        <div class="stat-info">
          <div class="stat-label">总收入</div>
          <div class="stat-value">¥{{ statistics.totalIncome || 0 }}</div>
          <div class="stat-sub">累计收入金额</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号/用户/宠物..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchStatus"
          placeholder="全部状态"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="待确认" :value="0" />
          <el-option label="已确认" :value="1" />
          <el-option label="服务中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
          <el-option label="已拒绝" :value="5" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="medium"
          @change="handleSearch"
        />
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button size="medium" @click="handleExport" :loading="exportLoading" class="export-btn">
          <i class="el-icon-download" /> 导出
        </el-button>
      </div>
    </div>

    <!-- 表格卡片 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="appointmentList"
        stripe
        row-key="id"
        class="appointment-table"
      >
        <el-table-column prop="id" label="ID" width="72" align="center" />
        <el-table-column prop="appointmentNo" label="预约编号" width="180" />
        <el-table-column label="用户信息" width="150">
          <template slot-scope="scope">
            <div class="user-name">{{ scope.row.userNickname || scope.row.userName || '--' }}</div>
            <div class="user-id">ID: {{ scope.row.userId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="宠物" width="120">
          <template slot-scope="scope">{{ scope.row.petName || '--' }}</template>
        </el-table-column>
        <el-table-column label="服务" min-width="150">
          <template slot-scope="scope">{{ scope.row.serviceName }}</template>
        </el-table-column>
        <el-table-column label="预约时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.appointmentDate) }} {{ scope.row.appointmentTime }}
          </template>
        </el-table-column>
        <el-table-column prop="servicePrice" label="金额" width="100" align="center">
          <template slot-scope="scope">¥{{ scope.row.servicePrice }}</template>
        </el-table-column>
        <el-table-column label="服务人员" width="120" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.staffName" class="staff-name">{{ scope.row.staffName }}</span>
            <span v-else class="no-staff">待分配</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <span :class="getStatusClass(scope.row.status)">{{ getStatusText(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-text cell-time">{{ formatDateTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleDetail(scope.row)" class="act-view">
                <i class="el-icon-view" /> 详情
              </el-button>
              <el-button
                v-if="scope.row.status === 0"
                type="text" size="small"
                @click="handleConfirm(scope.row)" class="act-confirm"
              >
                <i class="el-icon-check" /> 确认
              </el-button>
              <el-button
                v-if="scope.row.status === 1"
                type="text" size="small"
                @click="handleStart(scope.row)" class="act-start"
              >
                <i class="el-icon-caret-right" /> 开始
              </el-button>
              <el-button
                v-if="scope.row.status === 2"
                type="text" size="small"
                @click="handleComplete(scope.row)" class="act-complete"
              >
                <i class="el-icon-circle-check" /> 完成
              </el-button>
              <el-button
                v-if="scope.row.status === 0 && (isAdmin || isStaff)"
                type="text" size="small"
                @click="handleReject(scope.row)" class="act-del"
              >
                <i class="el-icon-close" /> 拒绝
              </el-button>
              <el-button
                v-if="(scope.row.status === 0 || scope.row.status === 1) && (isAdmin || isStaff)"
                type="text" size="small"
                @click="handleCancel(scope.row)" class="act-cancel"
              >
                <i class="el-icon-switch-button" /> 取消
              </el-button>
              <el-button
                v-if="isAdmin && (scope.row.status === 1 || scope.row.status === 2)"
                type="text" size="small"
                @click="handleReassign(scope.row)" class="act-reassign"
              >
                <i class="el-icon-refresh" /> 改派
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

    <!-- 详情对话框 -->
    <el-dialog
      title="预约详情"
      :visible.sync="detailVisible"
      width="580px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentAppointment">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="d-label">预约编号</span>
            <span class="d-value">{{ currentAppointment.appointmentNo }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">用户信息</span>
            <span class="d-value">{{ currentAppointment.userNickname || currentAppointment.userName || '--' }} (ID: {{ currentAppointment.userId }})</span>
          </div>
          <div class="detail-item">
            <span class="d-label">宠物</span>
            <span class="d-value">{{ currentAppointment.petName || '--' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">服务</span>
            <span class="d-value">{{ currentAppointment.serviceName }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">金额</span>
            <span class="d-value price">¥{{ currentAppointment.servicePrice }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">预约时间</span>
            <span class="d-value">{{ formatDate(currentAppointment.appointmentDate) }} {{ currentAppointment.appointmentTime }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">状态</span>
            <span class="d-value" :class="getStatusClass(currentAppointment.status)">{{ getStatusText(currentAppointment.status) }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">下单时间</span>
            <span class="d-value">{{ formatDateTime(currentAppointment.createTime) }}</span>
          </div>
          <div class="detail-item" v-if="currentAppointment.staffName">
            <span class="d-label">服务人员</span>
            <span class="d-value">{{ currentAppointment.staffName }}</span>
          </div>
          <div class="detail-item" v-if="currentAppointment.remark">
            <span class="d-label">备注</span>
            <span class="d-value">{{ currentAppointment.remark }}</span>
          </div>
          <div class="detail-item" v-if="currentAppointment.cancelReason">
            <span class="d-label">取消原因</span>
            <span class="d-value">{{ currentAppointment.cancelReason }}</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 确认/分配对话框 -->
    <el-dialog
      title="确认预约"
      :visible.sync="confirmVisible"
      width="620px"
      center
      class="confirm-dialog"
      @opened="loadStaffList"
    >
      <div class="dialog-body">
        <p class="confirm-desc">确认该预约后，需要分配给服务人员</p>
        <div v-if="staffListWithScore.length === 0 && !loadingStaff" class="empty-staff">
          <i class="el-icon-info" />
          <span>暂无可用员工</span>
        </div>
        <el-form label-width="80px" v-else class="confirm-form">
          <el-form-item label="分配给">
            <el-select
              v-model="selectedStaffId"
              placeholder="请选择员工"
              style="width: 100%"
              filterable
              :loading="loadingStaff"
              popper-class="staff-select-popper"
            >
              <el-option
                v-for="staff in staffListWithScore"
                :key="staff.id"
                :label="`${staff.nickname || staff.username} ${staff.hasConflict ? '时间冲突' : ''}`"
                :value="staff.id"
                :disabled="staff.hasConflict"
              >
                <div class="staff-option">
                  <div class="staff-header">
                    <span class="staff-name">{{ staff.nickname || staff.username }}</span>
                    <el-tag :type="staff.hasConflict ? 'danger' : 'success'" size="mini" v-if="staff.hasConflict">
                      时间冲突
                    </el-tag>
                    <el-tag v-else-if="staff.matchScore >= 80" size="mini" type="success">推荐</el-tag>
                  </div>
                  <div class="match-score-row" :class="{ 'no-match': staff.matchScore === 0 }">
                    <span class="match-label">匹配度：</span>
                    <el-progress :percentage="staff.matchScore" :color="getMatchColor(staff.matchScore)" :stroke-width="8" :show-text="false" style="flex: 1; margin: 0 10px;" />
                    <span class="match-value">{{ staff.matchScore || 0 }}%</span>
                  </div>
                  <div class="staff-stats">
                    <el-tag size="mini" type="primary"><i class="el-icon-s-order" /> 服务{{ staff.serviceCount || 0 }}次</el-tag>
                    <el-tag size="mini" type="warning"><i class="el-icon-star-on" /> 评分{{ staff.ratingAvg || 0 }}星</el-tag>
                    <el-tag size="mini" type="success"><i class="el-icon-circle-check" /> 完成{{ staff.completedCount || 0 }}单</el-tag>
                  </div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="confirmVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitConfirm" :loading="confirmLoading" :disabled="!selectedStaffId" class="warm-btn">
          确认分配
        </el-button>
      </span>
    </el-dialog>

    <!-- 重新分配对话框 -->
    <el-dialog
      title="重新分配"
      :visible.sync="reassignVisible"
      width="600px"
      center
      class="reassign-dialog"
      @opened="loadStaffList"
    >
      <div class="dialog-body">
        <p class="reassign-desc">将预约重新分配给其他员工</p>
        <el-form label-width="80px" class="confirm-form">
          <el-form-item label="当前员工">
            <el-input :value="currentAppointment?.staffName || '待分配'" disabled />
          </el-form-item>
          <el-form-item label="分配给">
            <el-select
              v-model="selectedStaffId"
              placeholder="请选择员工"
              style="width: 100%"
              filterable
              :loading="loadingStaff"
            >
              <el-option
                v-for="staff in staffListWithScore"
                :key="staff.id"
                :label="`${staff.nickname || staff.username} ${staff.hasConflict ? '时间冲突' : ''}`"
                :value="staff.id"
                :disabled="staff.hasConflict"
              >
                <div class="staff-option">
                  <div class="staff-header">
                    <span class="staff-name">{{ staff.nickname || staff.username }}</span>
                    <el-tag :type="staff.hasConflict ? 'danger' : 'success'" size="mini" v-if="staff.hasConflict">时间冲突</el-tag>
                  </div>
                  <div class="match-score-row" v-if="staff.matchScore > 0">
                    <span class="match-label">匹配度：</span>
                    <el-progress :percentage="staff.matchScore" :color="getMatchColor(staff.matchScore)" :stroke-width="8" :show-text="false" style="flex: 1; margin: 0 10px;" />
                    <span class="match-value">{{ staff.matchScore }}%</span>
                  </div>
                  <div class="staff-stats">
                    <el-tag size="mini" type="primary">服务{{ staff.serviceCount || 0 }}次</el-tag>
                    <el-tag size="mini" type="warning">评分{{ staff.ratingAvg || 0 }}星</el-tag>
                    <el-tag size="mini" type="success">完成{{ staff.completedCount || 0 }}单</el-tag>
                  </div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="reassignVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitReassign" :loading="reassignLoading" class="warm-btn">确认分配</el-button>
      </span>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog title="拒绝预约" :visible.sync="rejectVisible" width="460px" center>
      <div class="dialog-body">
        <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因（选填）" maxlength="200" show-word-limit />
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitReject" :loading="rejectLoading" class="warm-btn">确定拒绝</el-button>
      </span>
    </el-dialog>

    <!-- 取消对话框 -->
    <el-dialog title="取消预约" :visible.sync="cancelVisible" width="460px" center>
      <div class="dialog-body">
        <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因（选填）" maxlength="200" show-word-limit />
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancelVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitCancel" :loading="cancelLoading" class="warm-btn">确定取消</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminAppointmentList,
  confirmAppointment,
  startAppointment,
  completeAppointment,
  rejectAppointment,
  cancelAppointmentByAdmin,
  getAppointmentStatistics,
  exportAppointmentList,
  getStaffWithMatchScore
} from '@/api/service';
import { getAllStaff } from '@/api/user';

export default {
  name: 'Admin_AppointmentList',
  data() {
    return {
      loading: false,
      confirmLoading: false,
      exportLoading: false,
      reassignLoading: false,
      rejectLoading: false,
      cancelLoading: false,
      loadingStaff: false,
      appointmentList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      searchKeyword: '',
      searchStatus: '',
      dateRange: [],
      statistics: { total: 0, pending: 0, confirmed: 0, totalIncome: 0 },
      detailVisible: false,
      confirmVisible: false,
      reassignVisible: false,
      rejectVisible: false,
      cancelVisible: false,
      currentAppointment: null,
      currentId: null,
      rejectReason: '',
      cancelReason: '',
      staffList: [],
      staffListWithScore: [],
      selectedStaffId: null
    };
  },
  computed: {
    isAdmin() {
      const userInfo = localStorage.getItem('userInfo');
      if (!userInfo) return false;
      try { return JSON.parse(userInfo).role === 3; } catch { return false; }
    },
    isStaff() {
      const userInfo = localStorage.getItem('userInfo');
      if (!userInfo) return false;
      try { return JSON.parse(userInfo).role === 2; } catch { return false; }
    }
  },
  created() {
    this.loadList();
    this.loadStatistics();
    if (this.isAdmin) this.loadStaffList();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          status: this.searchStatus !== '' ? this.searchStatus : undefined,
          startDate: this.dateRange?.[0],
          endDate: this.dateRange?.[1]
        };
        const res = await getAdminAppointmentList(params);
        if (res.code === 200) {
          this.appointmentList = (res.data.list || []).map(item => ({
            ...item,
            userNickname: item.userNickname || null
          }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载失败');
      } finally {
        this.loading = false;
      }
    },

    getMatchColor(score) {
      if (score >= 80) return '#67c23a';
      if (score >= 60) return '#409EFF';
      if (score >= 40) return '#e6a23c';
      return '#f56c6c';
    },

    async handleExport() {
      this.exportLoading = true;
      try {
        const params = {
          keyword: this.searchKeyword || undefined,
          status: this.searchStatus !== '' ? this.searchStatus : undefined,
          startDate: this.dateRange?.[0],
          endDate: this.dateRange?.[1]
        };
        const res = await exportAppointmentList(params);
        const url = window.URL.createObjectURL(res);
        const link = document.createElement('a');
        link.href = url;
        link.download = `预约列表_${Date.now()}.xlsx`;
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

    async loadStatistics() {
      try {
        const res = await getAppointmentStatistics();
        if (res.code === 200) this.statistics = res.data;
      } catch { /* non-critical */ }
    },

    async loadStaffList() {
      if (!this.currentAppointment || !this.currentAppointment.id) {
        try {
          const res = await getAllStaff();
          if (res.code === 200) {
            this.staffList = res.data || [];
            this.staffListWithScore = this.staffList.map(s => ({ ...s, matchScore: 0, hasConflict: false }));
          }
        } catch { /* non-critical */ }
        return;
      }

      this.loadingStaff = true;
      try {
        const appointment = this.currentAppointment;
        let formattedDate = appointment.appointmentDate;
        if (typeof appointment.appointmentDate === 'number') {
          const date = new Date(appointment.appointmentDate);
          formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        }

        const res = await getStaffWithMatchScore({
          appointmentId: appointment.id,
          appointmentDate: formattedDate,
          appointmentTime: appointment.appointmentTime,
          serviceId: appointment.serviceId
        });

        if (res.code === 200) {
          this.staffListWithScore = res.data;
          this.staffList = res.data.map(s => ({ id: s.id, username: s.username, nickname: s.nickname }));
        } else {
          const fallbackRes = await getAllStaff();
          if (fallbackRes.code === 200) {
            this.staffList = fallbackRes.data || [];
            this.staffListWithScore = this.staffList.map(s => ({ ...s, matchScore: 0, hasConflict: false }));
          }
        }
      } catch {
        const fallbackRes = await getAllStaff();
        if (fallbackRes.code === 200) {
          this.staffList = fallbackRes.data || [];
          this.staffListWithScore = this.staffList.map(s => ({ ...s, matchScore: 0, hasConflict: false }));
        }
      } finally {
        this.loadingStaff = false;
      }
    },

    getStatusText(status) {
      const map = { 0: '待确认', 1: '已确认', 2: '服务中', 3: '已完成', 4: '已取消', 5: '已拒绝' };
      return map[status] || '未知';
    },

    getStatusClass(status) {
      const map = { 0: 'status-pending', 1: 'status-confirmed', 2: 'status-servicing', 3: 'status-completed', 4: 'status-cancelled', 5: 'status-rejected' };
      return map[status] || '';
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

    handleSearch() { this.page = 1; this.loadList(); this.loadStatistics(); },
    handleReset() {
      this.searchKeyword = '';
      this.searchStatus = '';
      this.dateRange = [];
      this.page = 1;
      this.loadList();
    },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },

    async handleDetail(row) {
      this.currentAppointment = row;
      this.detailVisible = true;
    },

    handleConfirm(row) {
      this.currentId = row.id;
      this.currentAppointment = row;
      this.selectedStaffId = null;
      if (this.isAdmin) {
        this.confirmVisible = true;
      } else {
        this.$confirm('确认该预约吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }).then(async () => {
          try {
            const res = await confirmAppointment(row.id);
            if (res.code === 200) {
              this.$message.success(res.message || '确认成功');
              this.loadList();
              this.loadStatistics();
            } else {
              this.$message.error(res.message || '确认失败');
            }
          } catch (error) {
            this.$message.error(error?.message || '操作失败');
          }
        }).catch(() => {});
      }
    },

    async submitConfirm() {
      if (this.isAdmin && !this.selectedStaffId) {
        this.$message.warning('请选择要分配的员工');
        return;
      }
      this.confirmLoading = true;
      try {
        const res = await confirmAppointment(this.currentId, this.selectedStaffId);
        if (res.code === 200) {
          this.$message.success(res.message || '确认成功，已分配给员工');
          this.confirmVisible = false;
          this.loadList();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '操作失败');
      } finally {
        this.confirmLoading = false;
      }
    },

    handleReassign(row) {
      this.currentId = row.id;
      this.currentAppointment = row;
      this.selectedStaffId = null;
      this.reassignVisible = true;
    },

    async submitReassign() {
      if (!this.selectedStaffId) {
        this.$message.warning('请选择要分配的员工');
        return;
      }
      this.reassignLoading = true;
      try {
        const res = await confirmAppointment(this.currentId, this.selectedStaffId);
        if (res.code === 200) {
          this.$message.success(res.message || '重新分配成功');
          this.reassignVisible = false;
          this.loadList();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '操作失败');
      } finally {
        this.reassignLoading = false;
      }
    },

    async handleStart(row) {
      this.$confirm('确定要开始该服务吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }).then(async () => {
        try {
          const res = await startAppointment(row.id);
          if (res.code === 200) {
            this.$message.success(res.message || '服务已开始');
            this.loadList();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '操作失败');
        }
      }).catch(() => {});
    },

    async handleComplete(row) {
      this.$confirm('确定完成该服务吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }).then(async () => {
        try {
          const res = await completeAppointment(row.id);
          if (res.code === 200) {
            this.$message.success(res.message || '已完成');
            this.loadList();
            this.loadStatistics();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '操作失败');
        }
      }).catch(() => {});
    },

    handleReject(row) {
      this.currentId = row.id;
      this.rejectReason = '';
      this.rejectVisible = true;
    },

    handleCancel(row) {
      this.currentId = row.id;
      this.cancelReason = '';
      this.cancelVisible = true;
    },

    async submitReject() {
      this.rejectLoading = true;
      try {
        const res = await rejectAppointment(this.currentId, this.rejectReason);
        if (res.code === 200) {
          this.$message.success(res.message || '已拒绝');
          this.rejectVisible = false;
          this.loadList();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '操作失败');
      } finally {
        this.rejectLoading = false;
      }
    },

    async submitCancel() {
      this.cancelLoading = true;
      try {
        const res = await cancelAppointmentByAdmin(this.currentId, this.cancelReason);
        if (res.code === 200) {
          this.$message.success(res.message || '已取消');
          this.cancelVisible = false;
          this.loadList();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '操作失败');
      } finally {
        this.cancelLoading = false;
      }
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.appointment-list-page {
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
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: #fff;
  flex-shrink: 0;
}
.card-total .stat-icon     { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-pending .stat-icon   { background: linear-gradient(135deg, #e6a23c, #f7c06b); }
.card-confirmed .stat-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-income .stat-icon    { background: linear-gradient(135deg, #67c23a, #85ce61); }

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
.keyword-input { width: 240px; }
.keyword-input ::v-deep .el-input__inner { border-radius: 10px; }
.status-select { width: 110px; }
.status-select ::v-deep .el-input__inner { border-radius: 10px; }

.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
  border-radius: 10px !important; padding: 9px 20px !important; font-weight: 500;
}
.warm-btn:hover { opacity: 0.88; transform: translateY(-1px); }

.reset-btn { border-radius: 10px; color: #606266; }
.reset-btn:hover { background: #fdf6f0; border-color: #f5d5c0; }

.export-btn {
  background: linear-gradient(135deg, #67c23a, #85ce61) !important;
  border: none !important; color: #fff !important;
  border-radius: 10px !important; padding: 9px 20px !important; font-weight: 500;
}
.export-btn:hover { opacity: 0.88; transform: translateY(-1px); }

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.appointment-table ::v-deep .el-table__header th {
  background: #fdf8f4; color: #5c4a42; font-weight: 600;
  font-size: 12px; letter-spacing: 0.4px; padding: 15px 0; border-bottom: 2px solid #f5ece6;
}
.appointment-table ::v-deep .el-table__body td { padding: 15px 0; font-size: 13px; color: #3d2e2a; }
.appointment-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.appointment-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.appointment-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }

.user-name { font-weight: 600; color: #3d2e2a; }
.user-id { font-size: 11px; color: #a08c84; }
.staff-name { color: #409eff; font-weight: 500; }
.no-staff { color: #e6a23c; }
.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { color: #a08c84; font-size: 12px; }

/* 状态标签 */
.status-pending { color: #e6a23c; font-weight: 500; }
.status-confirmed { color: #409eff; font-weight: 500; }
.status-servicing { color: #67c23a; font-weight: 500; }
.status-completed { color: #909399; font-weight: 500; }
.status-cancelled { color: #f56c6c; font-weight: 500; }
.status-rejected { color: #f56c6c; font-weight: 500; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 2px; flex-wrap: wrap; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-confirm { color: #67c23a !important; }
.act-confirm:hover { color: #4a9e1b !important; }
.act-start { color: #409eff !important; }
.act-start:hover { color: #2d7fdb !important; }
.act-complete { color: #67c23a !important; }
.act-complete:hover { color: #4a9e1b !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }
.act-cancel { color: #e6a23c !important; }
.act-cancel:hover { color: #c48a1e !important; }
.act-reassign { color: #909399 !important; }
.act-reassign:hover { color: #606266 !important; }

/* ===== 分页卡片 ===== */
.pagination-card {
  background: #fff; border-radius: 16px; margin-top: 16px;
  padding: 16px 24px; display: flex; justify-content: flex-end;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

/* ===== 详情对话框 ===== */
.detail-dialog ::v-deep .el-dialog { border-radius: 20px; overflow: hidden; }
.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.detail-dialog ::v-deep .el-dialog__title { color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px; }
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
.d-label { width: 72px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; font-weight: 500; }
.d-value.price { color: #f56c6c; font-weight: 600; }

/* ===== 确认/改派对话框 ===== */
.confirm-dialog ::v-deep .el-dialog,
.reassign-dialog ::v-deep .el-dialog { border-radius: 20px; overflow: hidden; }
.confirm-dialog ::v-deep .el-dialog__header,
.reassign-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.confirm-dialog ::v-deep .el-dialog__title,
.reassign-dialog ::v-deep .el-dialog__title { color: #fff; font-weight: 700; font-size: 17px; }
.confirm-dialog ::v-deep .el-dialog__close,
.reassign-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.confirm-dialog ::v-deep .el-dialog__close:hover,
.reassign-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.confirm-dialog ::v-deep .el-dialog__body,
.reassign-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }
.confirm-desc, .reassign-desc { margin-bottom: 20px; color: #a08c84; font-size: 14px; }

.confirm-form ::v-deep .el-form-item { margin-bottom: 18px; }
.confirm-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.confirm-form ::v-deep .el-input__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.confirm-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}

/* ===== 员工选项 ===== */
.staff-option { padding: 12px 8px; border-bottom: 1px solid #f5ece6; }
.staff-option:last-child { border-bottom: none; }
.staff-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.staff-name { font-weight: 600; color: #3d2e2a; font-size: 14px; }
.match-score-row {
  display: flex; align-items: center; margin-bottom: 10px;
  background: #fdf8f4; padding: 6px 10px; border-radius: 8px;
}
.match-score-row.no-match { background: #fef0f0; }
.match-label { font-size: 12px; color: #a08c84; width: 50px; font-weight: 500; }
.match-value { font-size: 13px; font-weight: 600; color: #f56c6c; width: 45px; text-align: right; }
.staff-stats { display: flex; gap: 8px; flex-wrap: wrap; }
.staff-stats .el-tag { font-size: 11px; padding: 0 8px; height: 22px; line-height: 21px; }
.empty-staff { text-align: center; padding: 40px 20px; color: #a08c84; }
.empty-staff i { font-size: 48px; margin-bottom: 16px; display: block; }

/* ===== 对话框底部 ===== */
.dialog-footer {
  text-align: right; padding: 16px 28px 22px; border-top: 1px solid #f5ece6;
}
.dialog-footer .el-button {
  border-radius: 10px; padding: 10px 28px; font-weight: 500; font-size: 13px;
}
.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
}
.dialog-footer .el-button--primary:hover { opacity: 0.88; transform: translateY(-1px); }

/* ===== 下拉选项 ===== */
.el-select-dropdown__item {
  height: auto !important; padding: 8px 12px !important;
  white-space: normal !important; line-height: 1.4 !important;
}

/* 响应式 */
@media (max-width: 768px) {
  .appointment-list-page { padding: 12px; }
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
