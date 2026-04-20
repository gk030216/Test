<template>
  <div class="user-analysis">

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-active">
            <i class="el-icon-star-on"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.activeUsers || 0 }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-new">
            <i class="el-icon-date"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.todayNew || 0 }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-week">
            <i class="el-icon-s-data"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.weekNew || 0 }}</div>
            <div class="stat-label">近7日新增</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-data-line"></i> 用户增长趋势（近30天）</span>
            <el-radio-group v-model="trendType" size="small" @change="loadChartData">
              <el-radio-button label="daily">每日</el-radio-button>
              <el-radio-button label="weekly">每周</el-radio-button>
              <el-radio-button label="monthly">每月</el-radio-button>
            </el-radio-group>
          </div>
          <div id="trendChart" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-pie-chart"></i> 用户角色分布</span>
          </div>
          <div id="roleChart" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-pie-chart"></i> 用户性别分布</span>
          </div>
          <div id="genderChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-s-data"></i> 用户活跃度分布</span>
          </div>
          <div id="activityChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getUserStatistics, getUserTrend, getUserRoleDistribution, getUserGenderDistribution, getUserActivityDistribution } from '@/api/user';

export default {
  name: 'UserAnalysis',
  data() {
    return {
      loading: false,
      trendType: 'daily',
      statistics: {
        totalUsers: 0,
        activeUsers: 0,
        todayNew: 0,
        weekNew: 0
      },
      trendData: [],
      roleData: [],
      genderData: [],
      activityData: []
    };
  },
  mounted() {
    this.loadStatistics();
    this.loadChartData();
  },
  methods: {
    async loadStatistics() {
      try {
        const res = await getUserStatistics();
        if (res.code === 200) {
          this.statistics = res.data;
        }
      } catch (error) {
        console.error('加载统计数据失败', error);
      }
    },

    async loadChartData() {
      try {
        // 加载趋势数据
        const trendRes = await getUserTrend({ type: this.trendType });
        if (trendRes.code === 200) {
          this.trendData = trendRes.data;
          this.initTrendChart();
        }

        // 加载角色分布
        const roleRes = await getUserRoleDistribution();
        if (roleRes.code === 200) {
          this.roleData = roleRes.data;
          this.initRoleChart();
        }

        // 加载性别分布
        const genderRes = await getUserGenderDistribution();
        if (genderRes.code === 200) {
          this.genderData = genderRes.data;
          this.initGenderChart();
        }

        // 加载活跃度分布
        const activityRes = await getUserActivityDistribution();
        if (activityRes.code === 200) {
          this.activityData = activityRes.data;
          this.initActivityChart();
        }
      } catch (error) {
        console.error('加载图表数据失败', error);
      }
    },

    initTrendChart() {
      const chart = echarts.init(document.getElementById('trendChart'));
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: this.trendData.map(item => item.date),
          axisLabel: { rotate: 30 }
        },
        yAxis: { type: 'value', name: '新增用户数' },
        series: [{
          data: this.trendData.map(item => item.count),
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.3, color: '#409EFF' },
          lineStyle: { color: '#409EFF', width: 2 },
          symbol: 'circle',
          symbolSize: 6,
          itemStyle: { color: '#409EFF' }
        }]
      });
      window.addEventListener('resize', () => chart.resize());
    },

    initRoleChart() {
      const chart = echarts.init(document.getElementById('roleChart'));
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}人)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: '55%',
          center: ['50%', '50%'],
          data: this.roleData,
          label: { show: true, formatter: '{b}: {d}%' },
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 }
        }],
        color: ['#409EFF', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
      });
      window.addEventListener('resize', () => chart.resize());
    },

    initGenderChart() {
      const chart = echarts.init(document.getElementById('genderChart'));
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}人)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: '55%',
          center: ['50%', '50%'],
          data: this.genderData,
          label: { show: true, formatter: '{b}: {d}%' },
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 }
        }],
        color: ['#409EFF', '#f56c6c', '#909399']
      });
      window.addEventListener('resize', () => chart.resize());
    },

    initActivityChart() {
      const chart = echarts.init(document.getElementById('activityChart'));
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: this.activityData.map(item => item.level) },
        yAxis: { type: 'value', name: '用户数' },
        series: [{
          data: this.activityData.map(item => item.count),
          type: 'bar',
          barWidth: '60%',
          itemStyle: {
            borderRadius: [4, 4, 0, 0],
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [{ offset: 0, color: '#409EFF' }, { offset: 1, color: '#66b1ff' }]
            }
          },
          label: { show: true, position: 'top' }
        }]
      });
      window.addEventListener('resize', () => chart.resize());
    }
  }
};
</script>

<style scoped>
.user-analysis {
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
  color: #909399;
}

.stat-card {
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  padding: 22px 20px;
}

.stat-icon {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon i {
  font-size: 26px;
  color: white;
}

.stat-active { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-new { background: linear-gradient(135deg, #e6a23c, #f0b45c); }
.stat-week { background: linear-gradient(135deg, #909399, #b4b6bc); }

.stat-number {
  font-size: 30px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-card {
  border-radius: 20px;
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #2c3e50;
}

@media (max-width: 768px) {
  .user-analysis {
    padding: 12px;
  }
}
</style>