<template>
  <div class="staff-dashboard">
    <div class="page-header">
      <h2 class="page-title">员工工作台</h2>
      <p class="page-desc">欢迎回来，{{ userInfo.nickname || userInfo.username }}</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-time"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.pending || 0 }}</div>
            <div class="stat-label">待处理预约</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-confirmed">
            <i class="el-icon-check"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.confirmed || 0 }}</div>
            <div class="stat-label">已确认</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-servicing">
            <i class="el-icon-loading"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.inService || 0 }}</div>
            <div class="stat-label">服务中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-completed">
            <i class="el-icon-circle-check"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.completed || 0 }}</div>
            <div class="stat-label">今日完成</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待处理预约列表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card class="table-card" shadow="hover">
          <div slot="header" class="card-header">
            <span><i class="el-icon-s-order"></i> 待处理预约</span>
            <el-button type="text" @click="$router.push('/staff/appointments')">查看更多</el-button>
          </div>
          <el-table :data="pendingAppointments" style="width: 100%" v-loading="loading">
            <el-table-column prop="appointmentNo" label="预约编号" width="180">
              <template slot-scope="scope">
                {{ scope.row.appointmentNo }}
              </template>
            </el-table-column>
            <el-table-column prop="petName" label="宠物名称" width="120">
              <template slot-scope="scope">
                {{ scope.row.petName || '未指定' }}
              </template>
            </el-table-column>
            <el-table-column prop="serviceName" label="服务项目" min-width="150"></el-table-column>
            <el-table-column label="预约时间" width="180">
              <template slot-scope="scope">
                {{ formatDate(scope.row.appointmentDate) }} {{ scope.row.appointmentTime }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag type="warning" size="small">待确认</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template slot-scope="scope">
                <el-button size="small" type="primary" @click="handleConfirm(scope.row)">确认</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="empty-table" v-if="!loading && pendingAppointments.length === 0">
            <i class="el-icon-success"></i>
            <p>暂无待处理预约</p>
          </div>
        </el-card>
      </el-col>

      <!-- 进行中服务 -->
      <el-col :span="8">
        <el-card class="table-card" shadow="hover">
          <div slot="header" class="card-header">
            <span><i class="el-icon-loading"></i> 进行中服务</span>
          </div>
          <div class="servicing-list" v-loading="loading">
            <div class="servicing-item" v-for="item in servicingAppointments" :key="item.id">
              <div class="servicing-info">
                <div class="servicing-pet">
                  <i class="el-icon-s-custom"></i> {{ item.petName }}
                </div>
                <div class="servicing-service">{{ item.serviceName }}</div>
                <div class="servicing-time">
                  <i class="el-icon-time"></i> {{ item.appointmentTime }}
                </div>
              </div>
              <el-button size="small" type="success" @click="handleComplete(item)">完成</el-button>
            </div>
            <div class="empty-list" v-if="!loading && servicingAppointments.length === 0">
              <i class="el-icon-info"></i>
              <p>暂无进行中服务</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日完成服务统计 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="table-card" shadow="hover">
          <div slot="header" class="card-header">
            <span><i class="el-icon-circle-check"></i> 今日已完成服务</span>
          </div>
          <el-table :data="todayCompleted" style="width: 100%" v-loading="loading">
            <el-table-column prop="appointmentNo" label="预约编号" width="180"></el-table-column>
            <el-table-column prop="petName" label="宠物名称" width="120"></el-table-column>
            <el-table-column prop="serviceName" label="服务项目" min-width="150"></el-table-column>
            <el-table-column prop="servicePrice" label="金额" width="100">
              <template slot-scope="scope">
                ¥{{ scope.row.servicePrice }}
              </template>
            </el-table-column>
            <el-table-column label="完成时间" width="180">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.updateTime) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="empty-table" v-if="!loading && todayCompleted.length === 0">
            <i class="el-icon-document"></i>
            <p>今日暂无完成服务</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getAppointmentStatistics, getAdminAppointmentList, confirmAppointment, completeAppointment } from '@/api/service';

export default {
  name: 'StaffDashboard',
  data() {
    return {
      loading: false,
      statistics: {
        pending: 0,
        confirmed: 0,
        inService: 0,
        completed: 0,
        totalIncome: 0
      },
      pendingAppointments: [],
      servicingAppointments: [],
      todayCompleted: [],
      userInfo: {}
    };
  },
  created() {
    this.loadUserInfo();
    this.loadStatistics();
    this.loadAppointments();
  },
  methods: {
    loadUserInfo() {
      const info = localStorage.getItem('userInfo');
      if (info) {
        this.userInfo = JSON.parse(info);
      }
    },

    async loadStatistics() {
      try {
        const staffId = this.userInfo.id;
        const res = await getAppointmentStatistics({ staffId: staffId });
        if (res.code === 200) {
          this.statistics = {
            ...this.statistics,
            ...res.data
          };
        }
      } catch (error) {
        console.error('加载统计数据失败', error);
      }
    },

    async loadAppointments() {
      this.loading = true;
      try {
        const today = new Date().toISOString().split('T')[0];
        const staffId = this.userInfo.id;  // 获取当前员工ID

        // 获取待确认预约（所有员工可见，不传staffId）
        const pendingRes = await getAdminAppointmentList({
          page: 1,
          pageSize: 10,
          status: 0
        });
        if (pendingRes.code === 200) {
          this.pendingAppointments = pendingRes.data.list || [];
        }

        // 获取服务中预约（只显示当前员工）
        const servicingRes = await getAdminAppointmentList({
          page: 1,
          pageSize: 10,
          status: 2,
          staffId: staffId
        });
        if (servicingRes.code === 200) {
          this.servicingAppointments = servicingRes.data.list || [];
        }

        // 获取今日已完成（只显示当前员工）
        const completedRes = await getAdminAppointmentList({
          page: 1,
          pageSize: 10,
          status: 3,
          startDate: today,
          endDate: today,
          staffId: staffId
        });
        if (completedRes.code === 200) {
          this.todayCompleted = completedRes.data.list || [];
          this.statistics.completed = completedRes.data.total || 0;
        }
      } catch (error) {
        console.error('加载预约列表失败', error);
      } finally {
        this.loading = false;
      }
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

    async handleConfirm(row) {
      this.$confirm(`确认预约 ${row.appointmentNo} 吗？`, '提示', { type: 'info' }).then(async () => {
        try {
          // 确保调用方式正确
          await confirmAppointment(row.id);
          this.$message.success('确认成功');
          this.loadStatistics();
          this.loadAppointments();
        } catch (error) {
          this.$message.error('操作失败');
        }
      }).catch(() => {});
    },

    async handleComplete(row) {
      this.$confirm(`完成服务 ${row.appointmentNo} 吗？`, '提示', { type: 'info' }).then(async () => {
        try {
          await completeAppointment(row.id);
          this.$message.success('服务已完成');
          this.loadStatistics();
          this.loadAppointments();
        } catch (error) {
          this.$message.error('操作失败');
        }
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.staff-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: #999;
}

.stat-card {
  border-radius: 16px;
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
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #e6a23c, #f0b45c);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon i {
  font-size: 28px;
  color: white;
}

.stat-confirmed {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
}

.stat-servicing {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-completed {
  background: linear-gradient(135deg, #909399, #b0b3b9);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.table-card {
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #2c3e50;
}

.card-header i {
  margin-right: 8px;
  color: #667eea;
}

.empty-table,
.empty-list {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-table i,
.empty-list i {
  font-size: 48px;
  margin-bottom: 16px;
}

.servicing-list {
  max-height: 400px;
  overflow-y: auto;
}

.servicing-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.servicing-item:last-child {
  border-bottom: none;
}

.servicing-info {
  flex: 1;
}

.servicing-pet {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.servicing-pet i {
  margin-right: 6px;
  color: #667eea;
}

.servicing-service {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.servicing-time {
  font-size: 12px;
  color: #999;
}

.servicing-time i {
  margin-right: 4px;
}

@media (max-width: 768px) {
  .staff-dashboard {
    padding: 12px;
  }

  .stat-card ::v-deep .el-card__body {
    padding: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-icon i {
    font-size: 24px;
  }

  .stat-number {
    font-size: 24px;
  }
}
</style>