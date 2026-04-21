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
          <el-option label="待确认" :value="0"></el-option>
          <el-option label="已确认" :value="1"></el-option>
          <el-option label="服务中" :value="2"></el-option>
          <el-option label="已完成" :value="3"></el-option>
          <el-option label="已取消" :value="4"></el-option>
          <el-option label="已拒绝" :value="5"></el-option>
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="medium" @change="handleSearch"></el-date-picker>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
        <el-button type="success" size="medium" @click="handleExport" :loading="exportLoading" plain class="export-btn">
          <i class="el-icon-download"></i> 导出
        </el-button>
      </div>
    </div>

    <!-- 预约表格 -->
    <el-table v-loading="loading" :data="appointmentList" stripe class="appointment-table">
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="appointmentNo" label="预约编号" width="180"></el-table-column>
      <el-table-column label="用户信息" width="150">
        <template slot-scope="scope">
          <div class="user-name">{{ scope.row.userNickname || scope.row.userName || '--' }}</div>
          <div class="user-id">ID: {{ scope.row.userId }}</div>
        </template>
      </el-table-column>
      <el-table-column label="宠物" width="120"><template slot-scope="scope">{{ scope.row.petName || '--' }}</template></el-table-column>
      <el-table-column label="服务" min-width="150"><template slot-scope="scope">{{ scope.row.serviceName }}</template></el-table-column>
      <el-table-column label="预约时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.appointmentDate) }} {{ scope.row.appointmentTime }}
        </template>
      </el-table-column>
      <el-table-column prop="servicePrice" label="金额" width="100" align="center"><template slot-scope="scope">¥{{ scope.row.servicePrice }}</template></el-table-column>
      <el-table-column label="服务人员" width="120" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.staffName" class="staff-name">{{ scope.row.staffName }}</span>
          <span v-else class="no-staff">待分配</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope"><span :class="getStatusClass(scope.row.status)">{{ getStatusText(scope.row.status) }}</span></template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160"><template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template></el-table-column>
      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <!-- 详情 -->
            <el-button size="small" type="primary" plain circle @click="handleDetail(scope.row)" title="详情">
              <i class="el-icon-view"></i>
            </el-button>

            <!-- 确认（待确认状态） -->
            <el-button v-if="scope.row.status === 0" size="small" type="success" plain circle @click="handleConfirm(scope.row)" title="确认">
              <i class="el-icon-check"></i>
            </el-button>

            <!-- 开始服务（已确认状态） -->
            <el-button v-if="scope.row.status === 1" size="small" type="primary" plain circle @click="handleStart(scope.row)" title="开始服务">
              <i class="el-icon-caret-right"></i>
            </el-button>

            <!-- 完成服务（服务中状态） -->
            <el-button v-if="scope.row.status === 2" size="small" type="success" plain circle @click="handleComplete(scope.row)" title="完成">
              <i class="el-icon-circle-check"></i>
            </el-button>

            <!-- 拒绝（待确认状态）- 管理员/员工专用 -->
            <el-button v-if="scope.row.status === 0 && (isAdmin || isStaff)" size="small" type="danger" plain circle @click="handleReject(scope.row)" title="拒绝">
              <i class="el-icon-close"></i>
            </el-button>

            <!-- 取消（待确认/已确认状态）- 管理员/员工专用 -->
            <el-button v-if="(scope.row.status === 0 || scope.row.status === 1) && (isAdmin || isStaff)" size="small" type="warning" plain circle @click="handleCancel(scope.row)" title="取消">
              <i class="el-icon-switch-button"></i>
            </el-button>

            <!-- 重新分配（管理员专属） -->
            <el-button v-if="isAdmin && (scope.row.status === 1 || scope.row.status === 2)" size="small" type="info" plain circle @click="handleReassign(scope.row)" title="重新分配">
              <i class="el-icon-refresh"></i>
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination @size-change="handleSizeChange" @current-change="handlePageChange" :current-page="page" :page-sizes="[10, 20, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total" background></el-pagination>
    </div>

    <!-- 详情对话框 -->
    <el-dialog title="预约详情" :visible.sync="detailVisible" width="600px" center>
      <div class="detail-content" v-if="currentAppointment">
        <div class="detail-row"><span class="label">预约编号：</span><span class="value">{{ currentAppointment.appointmentNo }}</span></div>
        <div class="detail-row">
          <span class="label">用户信息：</span>
          <span class="value">{{ currentAppointment.userNickname || currentAppointment.userName || '--' }} (ID: {{ currentAppointment.userId }})</span>
        </div>
        <div class="detail-row"><span class="label">宠物：</span><span class="value">{{ currentAppointment.petName || '--' }}</span></div>
        <div class="detail-row"><span class="label">服务：</span><span class="value">{{ currentAppointment.serviceName }}</span></div>
        <div class="detail-row"><span class="label">金额：</span><span class="value price">¥{{ currentAppointment.servicePrice }}</span></div>
        <div class="detail-row"><span class="label">预约时间：</span><span class="value">{{ formatDate(currentAppointment.appointmentDate) }} {{ currentAppointment.appointmentTime }}</span></div>
        <div class="detail-row"><span class="label">状态：</span><span class="value" :class="getStatusClass(currentAppointment.status)">{{ getStatusText(currentAppointment.status) }}</span></div>
        <div class="detail-row"><span class="label">下单时间：</span><span class="value">{{ formatDateTime(currentAppointment.createTime) }}</span></div>
        <div class="detail-row" v-if="currentAppointment.remark"><span class="label">备注：</span><span class="value">{{ currentAppointment.remark }}</span></div>
        <div class="detail-row" v-if="currentAppointment.cancelReason"><span class="label">取消/拒绝原因：</span><span class="value">{{ currentAppointment.cancelReason }}</span></div>
        <div class="detail-row" v-if="currentAppointment.staffName"><span class="label">服务人员：</span><span class="value">{{ currentAppointment.staffName }}</span></div>
      </div>
      <span slot="footer"><el-button type="primary" @click="detailVisible = false">关闭</el-button></span>
    </el-dialog>

    <!-- 确认预约对话框 -->
    <el-dialog title="确认预约" :visible.sync="confirmVisible" width="580px" center @opened="loadStaffList">
      <div class="confirm-content">
        <p class="confirm-desc">确认该预约后，需要分配给服务人员</p>

        <!-- 员工列表为空时的提示 -->
        <div v-if="staffListWithScore.length === 0 && !loadingStaff" class="empty-staff">
          <i class="el-icon-info"></i>
          <span>暂无可用员工</span>
        </div>

        <el-form label-width="80px" v-else>
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
                  :label="`${staff.nickname || staff.username} ${staff.hasConflict ? '⚠️时间冲突' : ''}`"
                  :value="staff.id"
                  :disabled="staff.hasConflict"
              >
                <div class="staff-option">
                  <div class="staff-header">
                    <span class="staff-name">{{ staff.nickname || staff.username }}</span>
                    <el-tag
                        :type="staff.hasConflict ? 'danger' : 'success'"
                        size="mini"
                        v-if="staff.hasConflict"
                    >
                      时间冲突
                    </el-tag>
                    <el-tag
                        v-else-if="staff.matchScore >= 80"
                        size="mini"
                        type="success"
                    >
                      推荐
                    </el-tag>
                  </div>

                  <!-- 匹配度进度条 -->
                  <div class="match-score-row" :class="{ 'no-match': staff.matchScore === 0 }">
                    <span class="match-label">匹配度：</span>
                    <el-progress
                        :percentage="staff.matchScore"
                        :color="getMatchColor(staff.matchScore)"
                        :stroke-width="8"
                        :show-text="false"
                        style="flex: 1; margin: 0 10px;"
                    ></el-progress>
                    <span class="match-value">{{ staff.matchScore || 0 }}%</span>
                  </div>

                  <!-- 员工统计信息 -->
                  <div class="staff-stats">
                    <el-tag size="mini" type="primary">
                      <i class="el-icon-s-order"></i> 服务{{ staff.serviceCount || 0 }}次
                    </el-tag>
                    <el-tag size="mini" type="warning">
                      <i class="el-icon-star-on"></i> 评分{{ staff.ratingAvg || 0 }}星
                    </el-tag>
                    <el-tag size="mini" type="success">
                      <i class="el-icon-circle-check"></i> 完成{{ staff.completedCount || 0 }}单
                    </el-tag>
                  </div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer">
    <el-button @click="confirmVisible = false">取消</el-button>
    <el-button type="primary" @click="submitConfirm" :loading="confirmLoading" :disabled="!selectedStaffId">
      确认分配
    </el-button>
  </span>
    </el-dialog>
    <!-- 重新分配对话框 -->
    <el-dialog title="重新分配" :visible.sync="reassignVisible" width="550px" center @opened="loadStaffList">
      <div class="reassign-content">
        <p class="reassign-desc">将预约重新分配给其他员工</p>
        <el-form label-width="80px">
          <el-form-item label="当前员工">
            <el-input :value="currentAppointment?.staffName || '待分配'" disabled></el-input>
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
                  :label="`${staff.nickname || staff.username} ${staff.hasConflict ? '⚠️时间冲突' : ''}`"
                  :value="staff.id"
                  :disabled="staff.hasConflict"
              >
                <div class="staff-option">
                  <div class="staff-header">
                    <span class="staff-name">{{ staff.nickname || staff.username }}</span>
                    <el-tag
                        :type="staff.hasConflict ? 'danger' : 'success'"
                        size="mini"
                        v-if="staff.hasConflict"
                    >
                      时间冲突
                    </el-tag>
                  </div>

                  <div class="match-score-row" v-if="staff.matchScore > 0">
                    <span class="match-label">匹配度：</span>
                    <el-progress
                        :percentage="staff.matchScore"
                        :color="getMatchColor(staff.matchScore)"
                        :stroke-width="8"
                        :show-text="false"
                        style="flex: 1; margin: 0 10px;"
                    ></el-progress>
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
      <span slot="footer">
    <el-button @click="reassignVisible = false">取消</el-button>
    <el-button type="primary" @click="submitReassign" :loading="reassignLoading">
      确认分配
    </el-button>
  </span>
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
import {
  getAdminAppointmentList,
  getAppointmentDetailForAdmin,
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
  name: 'AppointmentList',
  data() {
    return {
      loading: false,
      confirmLoading: false,
      exportLoading: false,
      reassignLoading: false,
      rejectLoading: false,
      cancelLoading: false,
      staffListWithScore: [],  // ✅ 添加带匹配度的员工列表
      loadingStaff: false,      // ✅ 加载员工列表的状态
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
      selectedStaffId: null
    };
  },

  created() {
    this.loadList();
    this.loadStatistics();
    if (this.isAdmin) {
      this.loadStaffList();
    }
  },
  computed: {
    isAdmin() {
      const userInfo = localStorage.getItem('userInfo');
      if (!userInfo) return false;
      try {
        return JSON.parse(userInfo).role === 3;
      } catch (e) {
        return false;
      }
    },
    // ✅ 添加 isStaff 判断
    isStaff() {
      const userInfo = localStorage.getItem('userInfo');
      if (!userInfo) return false;
      try {
        return JSON.parse(userInfo).role === 2;
      } catch (e) {
        return false;
      }
    }
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
          // ✅ 确保 userNickname 字段存在
          this.appointmentList = (res.data.list || []).map(item => ({
            ...item,
            userNickname: item.userNickname || null
          }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    // 根据匹配度返回颜色
    getMatchColor(score) {
      if (score >= 80) return '#67c23a';  // 绿色 - 优秀
      if (score >= 60) return '#409EFF';  // 蓝色 - 良好
      if (score >= 40) return '#e6a23c';  // 橙色 - 一般
      return '#f56c6c';                    // 红色 - 待提升
    },

    // ✅ 导出预约列表
    async handleExport() {
      this.exportLoading = true;
      try {
        // 构建查询参数
        const params = {
          keyword: this.searchKeyword || undefined,
          status: this.searchStatus !== '' ? this.searchStatus : undefined,
          startDate: this.dateRange?.[0],
          endDate: this.dateRange?.[1]
        };

        const res = await exportAppointmentList(params);

        // 创建下载链接
        const url = window.URL.createObjectURL(res);
        const link = document.createElement('a');
        link.href = url;
        link.download = `预约列表_${new Date().getTime()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        this.$message.success('导出成功');
      } catch (error) {
        console.error('导出失败', error);
        this.$message.error('导出失败');
      } finally {
        this.exportLoading = false;
      }
    },

    async loadStatistics() {
      try {
        const res = await getAppointmentStatistics();
        if (res.code === 200) this.statistics = res.data;
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },

    async loadStaffList() {
      // 如果没有当前预约，只加载普通员工列表
      if (!this.currentAppointment || !this.currentAppointment.id) {
        try {
          const res = await getAllStaff();
          if (res.code === 200) {
            this.staffList = res.data || [];
            this.staffListWithScore = this.staffList.map(s => ({ ...s, matchScore: 0, hasConflict: false }));
          }
        } catch (error) {
          console.error('加载员工列表失败', error);
        }
        return;
      }

      // 有预约时，加载带匹配度的员工列表
      this.loadingStaff = true;
      try {
        const appointment = this.currentAppointment;

        // ✅ 修复：将时间戳转换为正确的日期格式 yyyy-MM-dd
        let formattedDate = appointment.appointmentDate;
        if (typeof appointment.appointmentDate === 'number') {
          const date = new Date(appointment.appointmentDate);
          formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        }

        console.log('请求参数:', {
          appointmentId: appointment.id,
          appointmentDate: formattedDate,
          appointmentTime: appointment.appointmentTime,
          serviceId: appointment.serviceId
        });

        const res = await getStaffWithMatchScore({
          appointmentId: appointment.id,
          appointmentDate: formattedDate,
          appointmentTime: appointment.appointmentTime,
          serviceId: appointment.serviceId
        });

        if (res.code === 200) {
          this.staffListWithScore = res.data;
          this.staffList = res.data.map(s => ({
            id: s.id,
            username: s.username,
            nickname: s.nickname
          }));
        } else {
          // 降级处理
          const fallbackRes = await getAllStaff();
          if (fallbackRes.code === 200) {
            this.staffList = fallbackRes.data || [];
            this.staffListWithScore = this.staffList.map(s => ({ ...s, matchScore: 0, hasConflict: false }));
          }
        }
      } catch (error) {
        console.error('加载员工匹配度失败', error);
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
      const map = { 0: 'status-pending', 1: 'status-confirmed', 2: 'status-service', 3: 'status-completed', 4: 'status-cancelled', 5: 'status-rejected' };
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

    handleSearch() {
      this.page = 1;
      this.loadList();
      this.loadStatistics();
    },

    handleReset() {
      this.searchKeyword = '';
      this.searchStatus = '';
      this.dateRange = [];
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

    async handleDetail(row) {
      this.currentAppointment = row;
      this.detailVisible = true;
    },

    handleConfirm(row) {
      this.currentId = row.id;
      this.currentAppointment = row;
      this.selectedStaffId = null;

      if (this.isAdmin) {
        // 管理员：弹出选择员工对话框（会触发 @opened 事件加载匹配度）
        this.confirmVisible = true;
      } else {
        // 员工：直接确认（分配给自己）
        this.$confirm('确认该预约吗？', '提示', { type: 'info' }).then(async () => {
          await confirmAppointment(row.id);
          this.$message.success('确认成功');
          this.loadList();
          this.loadStatistics();
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
        await confirmAppointment(this.currentId, this.selectedStaffId);
        this.$message.success('确认成功，已分配给员工');
        this.confirmVisible = false;
        this.loadList();
        this.loadStatistics();
      } catch (error) {
        this.$message.error('操作失败');
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
        await confirmAppointment(this.currentId, this.selectedStaffId);
        this.$message.success('重新分配成功');
        this.reassignVisible = false;
        this.loadList();
      } catch (error) {
        this.$message.error('操作失败');
      } finally {
        this.reassignLoading = false;
      }
    },

    async handleStart(row) {
      this.$confirm('开始服务吗？', '提示', { type: 'info' }).then(async () => {
        await startAppointment(row.id);
        this.$message.success('服务已开始');
        this.loadList();
      }).catch(() => {});
    },

    async handleComplete(row) {
      this.$confirm('完成该服务吗？', '提示', { type: 'info' }).then(async () => {
        await completeAppointment(row.id);
        this.$message.success('已完成');
        this.loadList();
        this.loadStatistics();
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
        await rejectAppointment(this.currentId, this.rejectReason);
        this.$message.success('已拒绝');
        this.rejectVisible = false;
        this.loadList();
        this.loadStatistics();
      } catch (error) {
        this.$message.error('操作失败');
      } finally {
        this.rejectLoading = false;
      }
    },

    async submitCancel() {
      this.cancelLoading = true;
      try {
        await cancelAppointmentByAdmin(this.currentId, this.cancelReason);
        this.$message.success('已取消');
        this.cancelVisible = false;
        this.loadList();
        this.loadStatistics();
      } catch (error) {
        this.$message.error('操作失败');
      } finally {
        this.cancelLoading = false;
      }
    }
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
.staff-name { color: #409EFF; font-weight: 500; }
.no-staff { color: #e6a23c; }
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

.confirm-content,
.reassign-content { padding: 10px 0; }
.confirm-desc,
.reassign-desc { margin-bottom: 20px; color: #666; font-size: 14px; }

@media (max-width: 768px) {
  .appointment-list { padding: 12px; }
  .action-bar { justify-content: center; }
  .search-input { width: 160px; }
  .action-right { justify-content: center; }
}
.staff-option {
  padding: 8px 0;
}

.staff-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.staff-name {
  font-weight: 600;
  color: #2c3e50;
}

.match-score-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.match-label {
  font-size: 12px;
  color: #666;
  width: 50px;
}

.match-value {
  font-size: 12px;
  font-weight: 600;
  color: #ff6b6b;
  width: 40px;
  text-align: right;
}

.staff-stats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
/* 员工选项样式优化 */
.staff-option {
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
}

.staff-option:last-child {
  border-bottom: none;
}

.staff-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.staff-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.match-score-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  background: #f8f9fc;
  padding: 6px 10px;
  border-radius: 8px;
}

.match-label {
  font-size: 12px;
  color: #666;
  width: 50px;
  font-weight: 500;
}

.match-value {
  font-size: 13px;
  font-weight: 600;
  color: #ff6b6b;
  width: 45px;
  text-align: right;
}

.staff-stats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.staff-stats .el-tag {
  font-size: 11px;
  padding: 0 8px;
  height: 22px;
  line-height: 21px;
}

/* 进度条样式优化 */
.match-score-row .el-progress {
  line-height: 1;
}

.match-score-row .el-progress-bar__outer {
  background-color: #e8eaef;
}

/* 对话框内容区域 */
.confirm-content, .reassign-content {
  max-height: 500px;
  overflow-y: auto;
}

/* 下拉框选项高度优化 */
.el-select-dropdown__item {
  height: auto !important;
  padding: 8px 12px !important;
  white-space: normal !important;
  line-height: 1.4 !important;
}

/* 无匹配度时的样式 */
.match-score-row.no-match {
  background: #fef0f0;
}

.no-match .match-value {
  color: #f56c6c;
}
.empty-staff {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-staff i {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
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
</style>