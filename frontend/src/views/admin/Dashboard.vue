<template>
  <div class="dashboard">
    <h2>仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">1,234</div>
          <div class="stat-label">总用户数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">567</div>
          <div class="stat-label">今日访问</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">89</div>
          <div class="stat-label">今日预约</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">¥12,345</div>
          <div class="stat-label">今日收入</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <div slot="header">近期预约趋势</div>
          <div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #999">
            图表数据开发中...
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header">热门服务</div>
          <el-table :data="hotServices" style="width: 100%">
            <el-table-column prop="name" label="服务名称"></el-table-column>
            <el-table-column prop="count" label="预约次数"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  data() {
    return {
      hotServices: [
        { name: '宠物美容', count: 156 },
        { name: '宠物寄养', count: 98 },
        { name: '宠物医疗', count: 87 },
        { name: '宠物训练', count: 45 }
      ]
    };
  }
};
</script>

<style scoped>
.dashboard h2 {
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