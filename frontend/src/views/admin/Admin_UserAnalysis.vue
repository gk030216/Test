<template>
  <div class="user-analysis">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">用户分析</h2>
      <p class="page-desc">用户增长趋势、分布统计与活跃度分析</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-user"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总用户数</div>
          <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
          <div class="stat-sub">平台注册用户</div>
        </div>
      </div>
      <div class="stat-card card-active">
        <div class="stat-icon">
          <i class="el-icon-star-on"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">活跃用户</div>
          <div class="stat-value">{{ statistics.activeUsers || 0 }}</div>
          <div class="stat-sub">近期活跃账号</div>
        </div>
      </div>
      <div class="stat-card card-today">
        <div class="stat-icon">
          <i class="el-icon-date"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日新增</div>
          <div class="stat-value">{{ statistics.todayNew || 0 }}</div>
          <div class="stat-sub">当日注册用户</div>
        </div>
      </div>
      <div class="stat-card card-week">
        <div class="stat-icon">
          <i class="el-icon-s-data"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">近7日新增</div>
          <div class="stat-value">{{ statistics.weekNew || 0 }}</div>
          <div class="stat-sub">一周内注册量</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card chart-card-wide">
        <div class="chart-header">
          <span><i class="el-icon-data-line"></i> 用户增长趋势（近30天）</span>
          <el-radio-group v-model="trendType" size="small" @change="loadTrendChart">
            <el-radio-button label="daily">每日</el-radio-button>
            <el-radio-button label="weekly">每周</el-radio-button>
            <el-radio-button label="monthly">每月</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="trendChart" class="chart-box" v-loading="chartLoading"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 用户角色分布</span>
        </div>
        <div ref="roleChart" class="chart-box"></div>
      </div>
    </div>

    <div class="charts-section">
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 用户性别分布</span>
        </div>
        <div ref="genderChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-s-data"></i> 用户活跃度分布</span>
        </div>
        <div ref="activityChart" class="chart-box"></div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import {
  getUserStatistics,
  getUserTrend,
  getUserRoleDistribution,
  getUserGenderDistribution,
  getUserActivityDistribution
} from '@/api/user';

export default {
  name: 'Admin_UserAnalysis',
  data() {
    return {
      loading: false,
      chartLoading: false,
      trendType: 'daily',
      statistics: {
        totalUsers: 0,
        activeUsers: 0,
        todayNew: 0,
        weekNew: 0
      }
    };
  },
  mounted() {
    this.loadStatistics();
    this.loadAllCharts();
  },
  beforeDestroy() {
    this.charts.forEach(chart => chart.dispose());
    this.charts = [];
  },
  methods: {
    async loadStatistics() {
      this.loading = true;
      try {
        const res = await getUserStatistics();
        if (res.code === 200) {
          this.statistics = res.data;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载统计数据失败');
      } finally {
        this.loading = false;
      }
    },

    async loadAllCharts() {
      this.chartLoading = true;
      try {
        await Promise.all([
          this.loadTrendChart(),
          this.loadRoleChart(),
          this.loadGenderChart(),
          this.loadActivityChart()
        ]);
      } catch (error) {
        this.$message.error(error?.message || '加载图表数据失败');
      } finally {
        this.chartLoading = false;
      }
    },

    async loadTrendChart() {
      try {
        const res = await getUserTrend({ type: this.trendType });
        if (res.code === 200) {
          this.$nextTick(() => this.renderTrendChart(res.data));
        }
      } catch (error) {
        this.$message.error(error?.message || '加载趋势数据失败');
      }
    },

    async loadRoleChart() {
      try {
        const res = await getUserRoleDistribution();
        if (res.code === 200) {
          this.$nextTick(() => this.renderRoleChart(res.data));
        }
      } catch (error) {
        this.$message.error(error?.message || '加载角色分布失败');
      }
    },

    async loadGenderChart() {
      try {
        const res = await getUserGenderDistribution();
        if (res.code === 200) {
          this.$nextTick(() => this.renderGenderChart(res.data));
        }
      } catch (error) {
        this.$message.error(error?.message || '加载性别分布失败');
      }
    },

    async loadActivityChart() {
      try {
        const res = await getUserActivityDistribution();
        if (res.code === 200) {
          this.$nextTick(() => this.renderActivityChart(res.data));
        }
      } catch (error) {
        this.$message.error(error?.message || '加载活跃度分布失败');
      }
    },

    initChart(refName) {
      const el = this.$refs[refName];
      if (!el) return null;
      const exist = echarts.getInstanceByDom(el);
      if (exist) exist.dispose();
      const chart = echarts.init(el);
      this.charts = [...(this.charts || []), chart];
      return chart;
    },

    renderTrendChart(data) {
      const chart = this.initChart('trendChart');
      if (!chart) return;
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', top: 10, bottom: 0, containLabel: true },
        xAxis: {
          type: 'category',
          data: data.map(item => item.date),
          axisLabel: { rotate: 30, color: '#a08c84' },
          axisLine: { lineStyle: { color: '#f5ece6' } }
        },
        yAxis: {
          type: 'value',
          name: '新增用户数',
          nameTextStyle: { color: '#a08c84' },
          splitLine: { lineStyle: { color: '#f5ece6', type: 'dashed' } }
        },
        series: [{
          data: data.map(item => item.count),
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.25, color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#f59e4b' }, { offset: 1, color: 'rgba(245,158,75,0.02)' }] } },
          lineStyle: { color: '#f59e4b', width: 2.5 },
          symbol: 'circle',
          symbolSize: 7,
          itemStyle: { color: '#f59e4b', borderColor: '#fff', borderWidth: 2 }
        }]
      });
    },

    renderRoleChart(data) {
      const chart = this.initChart('roleChart');
      if (!chart) return;
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}人)' },
        legend: { orient: 'vertical', left: 6, top: 'center', textStyle: { color: '#5c4a42' } },
        series: [{
          type: 'pie',
          radius: ['48%', '74%'],
          center: ['58%', '50%'],
          data: data,
          label: { show: true, formatter: '{b}\n{d}%', color: '#5c4a42' },
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }
        }],
        color: ['#f59e4b', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
      });
    },

    renderGenderChart(data) {
      const chart = this.initChart('genderChart');
      if (!chart) return;
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}人)' },
        legend: { orient: 'vertical', left: 6, top: 'center', textStyle: { color: '#5c4a42' } },
        series: [{
          type: 'pie',
          radius: ['48%', '74%'],
          center: ['58%', '50%'],
          data: data,
          label: { show: true, formatter: '{b}\n{d}%', color: '#5c4a42' },
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }
        }],
        color: ['#f59e4b', '#f56c6c', '#909399']
      });
    },

    renderActivityChart(data) {
      const chart = this.initChart('activityChart');
      if (!chart) return;
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', top: 35, bottom: 30, containLabel: true },
        xAxis: {
          type: 'category',
          name: '活跃度',
          nameTextStyle: { color: '#a08c84', fontSize: 12 },
          data: data.map(item => item.level),
          axisLabel: { color: '#a08c84', interval: 0, rotate: 15 },
          axisLine: { lineStyle: { color: '#f5ece6' } }
        },
        yAxis: {
          type: 'value',
          name: '用户数',
          nameTextStyle: { color: '#a08c84' },
          splitLine: { lineStyle: { color: '#f5ece6', type: 'dashed' } }
        },
        series: [{
          data: data.map(item => item.count),
          type: 'bar',
          barWidth: '55%',
          itemStyle: {
            borderRadius: [5, 5, 0, 0],
            color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#f59e4b' }, { offset: 1, color: '#fcd9b4' }] }
          },
          label: { show: true, position: 'top', color: '#a08c84' }
        }]
      });
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.user-analysis {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 页面标题 ===== */
.page-header {
  margin-bottom: 22px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #3d2e2a;
  margin: 0 0 6px;
}
.page-desc {
  font-size: 13px;
  color: #a08c84;
  margin: 0;
}

/* ===== 统计卡片 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.05);
  transition: transform 0.25s, box-shadow 0.25s;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(61, 46, 42, 0.10);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}
.card-total .stat-icon  { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-active .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-today .stat-icon  { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-week .stat-icon   { background: linear-gradient(135deg, #c0c4cc, #909399); }

.stat-info { flex: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 5px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3d2e2a; letter-spacing: -0.5px; }
.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ===== 图表区域 ===== */
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 22px 24px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f5ece6;
  font-weight: 600;
  font-size: 15px;
  color: #3d2e2a;
}
.chart-header i { color: #f59e4b; margin-right: 6px; }

.chart-box {
  height: 350px;
}

/* ===== el-radio-button 暖色覆盖 ===== */
.chart-header ::v-deep .el-radio-button__inner {
  border-color: #f5ece6;
  color: #a08c84;
}
.chart-header ::v-deep .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  border-color: #f59e4b;
  color: #fff;
  box-shadow: none;
}

/* 响应式 */
@media (max-width: 768px) {
  .user-analysis { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .charts-section { grid-template-columns: 1fr; }
}
</style>
