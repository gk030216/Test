<template>
  <div class="staff-dashboard">
    <h2>员工工作台</h2>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-number">12</div>
          <div class="stat-label">今日待处理预约</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-number">5</div>
          <div class="stat-label">进行中服务</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-number">98%</div>
          <div class="stat-label">客户满意度</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <div slot="header">今日待处理预约</div>
      <el-table :data="appointments" style="width: 100%">
        <el-table-column prop="time" label="预约时间" width="180"></el-table-column>
        <el-table-column prop="petName" label="宠物名称"></el-table-column>
        <el-table-column prop="service" label="服务项目"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
            <el-tag type="warning">待处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="small" type="primary" @click="handleProcess(scope.row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'StaffDashboard',
  data() {
    return {
      appointments: [
        { time: '09:00', petName: '旺财', service: '宠物美容', status: 'pending' },
        { time: '10:30', petName: '咪咪', service: '宠物医疗', status: 'pending' },
        { time: '14:00', petName: '豆豆', service: '宠物寄养', status: 'pending' }
      ]
    };
  },
  methods: {
    handleProcess(row) {
      this.$message.info(`处理预约: ${row.petName} - ${row.service}`);
    }
  }
};
</script>

<style scoped>
.staff-dashboard h2 {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
}
.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}
.stat-label {
  color: #909399;
}
</style>