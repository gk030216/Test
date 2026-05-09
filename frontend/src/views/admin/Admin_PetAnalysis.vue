<template>
  <div class="pet-analysis-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">宠物分析</h2>
      <p class="page-desc">宠物类型分布、增长趋势与性别比例分析</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-s-data"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总宠物数</div>
          <div class="stat-value">{{ statistics.totalPets || 0 }}</div>
          <div class="stat-sub">平台宠物总数</div>
        </div>
      </div>
      <div class="stat-card card-dog">
        <div class="stat-icon">
          <i class="el-icon-star-on"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">狗狗</div>
          <div class="stat-value">{{ statistics.dogCount || 0 }}</div>
          <div class="stat-sub">狗狗数量统计</div>
        </div>
      </div>
      <div class="stat-card card-cat">
        <div class="stat-icon">
          <i class="el-icon-s-unfold"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">猫咪</div>
          <div class="stat-value">{{ statistics.catCount || 0 }}</div>
          <div class="stat-sub">猫咪数量统计</div>
        </div>
      </div>
      <div class="stat-card card-today">
        <div class="stat-icon">
          <i class="el-icon-circle-plus"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日新增</div>
          <div class="stat-value">{{ statistics.todayNew || 0 }}</div>
          <div class="stat-sub">当日新增宠物</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 - 第一行 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 宠物类型分布</span>
          <el-tag size="small" type="info">按类型统计</el-tag>
        </div>
        <div ref="typeChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-data-line"></i> 近7天新增趋势</span>
          <el-tag size="small" type="info">每日新增</el-tag>
        </div>
        <div ref="trendChart" class="chart-box"></div>
      </div>
    </div>

    <!-- 图表区域 - 第二行 -->
    <div class="charts-row">
      <div class="chart-card chart-card-full">
        <div class="chart-header">
          <span><i class="el-icon-s-data"></i> 性别分布</span>
          <el-tag size="small" type="info">公母比例</el-tag>
        </div>
        <div ref="genderChart" class="chart-box chart-box-short"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { getPetStatistics } from '@/api/pet';
import * as echarts from 'echarts';

export default {
  name: 'Admin_PetAnalysis',
  data() {
    return {
      loading: false,
      statistics: {
        totalPets: 0,
        dogCount: 0,
        catCount: 0,
        todayNew: 0,
        typeCount: {},
        weeklyTrend: [],
        genderStats: { male: 0, female: 0, unknown: 0 }
      },
      charts: []
    };
  },
  mounted() {
    this.loadStatistics();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    this.charts.forEach(chart => chart.dispose());
    this.charts = [];
  },
  methods: {
    async loadStatistics() {
      this.loading = true;
      try {
        const res = await getPetStatistics();
        if (res.code === 200) {
          this.statistics = res.data;
          if (!this.statistics.genderStats) {
            this.statistics.genderStats = { male: 0, female: 0, unknown: 0 };
          }
          this.$nextTick(() => this.initCharts());
        }
      } catch (error) {
        this.$message.error(error?.message || '加载统计数据失败');
      } finally {
        this.loading = false;
      }
    },

    initCharts() {
      this.initTypeChart();
      this.initTrendChart();
      this.initGenderChart();
    },

    initChart(refName) {
      const el = this.$refs[refName];
      if (!el) return null;
      const exist = echarts.getInstanceByDom(el);
      if (exist) exist.dispose();
      const chart = echarts.init(el);
      this.charts.push(chart);
      return chart;
    },

    handleResize() {
      this.charts.forEach(chart => {
        if (chart && !chart.isDisposed()) chart.resize();
      });
    },

    initTypeChart() {
      const chart = this.initChart('typeChart');
      if (!chart) return;
      const typeCount = this.statistics.typeCount || {};

      const data = [
        { name: '狗狗', value: typeCount.dog || 0 },
        { name: '猫咪', value: typeCount.cat || 0 },
        { name: '兔子', value: typeCount.rabbit || 0 },
        { name: '其他', value: (typeCount.other || 0) + (typeCount.bird || 0) + (typeCount.fish || 0) }
      ].filter(item => item.value > 0);

      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}只)' },
        legend: { orient: 'vertical', left: 6, top: 'center', textStyle: { color: '#5c4a42' } },
        series: [{
          type: 'pie',
          radius: ['48%', '74%'],
          center: ['58%', '50%'],
          data: data,
          label: { show: true, formatter: '{b}\n{d}%', color: '#5c4a42' },
          emphasis: { scale: true },
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }
        }],
        color: ['#f59e4b', '#67c23a', '#e6a23c', '#909399']
      });
    },

    initTrendChart() {
      const chart = this.initChart('trendChart');
      if (!chart) return;
      const trend = this.statistics.weeklyTrend || [];

      const last7Days = [];
      const last7DaysLabel = [];
      for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        const dateKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        const dateLabel = `${date.getMonth() + 1}/${date.getDate()}`;
        last7Days.push(dateKey);
        last7DaysLabel.push(dateLabel);
      }

      const dataMap = new Map();
      if (trend && trend.length > 0) {
        trend.forEach(item => {
          let dateValue = item.date;
          if (dateValue) {
            if (typeof dateValue === 'number') {
              const d = new Date(dateValue);
              dateValue = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
            }
            dataMap.set(dateValue, item.count);
          }
        });
      }

      const chartData = last7Days.map((dateKey, index) => ({
        date: last7DaysLabel[index],
        count: dataMap.get(dateKey) || 0
      }));

      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: function(params) {
            return `${params[0].axisValue}<br/>新增宠物: ${params[0].value}只`;
          }
        },
        grid: { left: '3%', right: '4%', top: 35, bottom: 25, containLabel: true },
        xAxis: {
          type: 'category',
          data: chartData.map(item => item.date),
          axisLabel: { fontSize: 11, color: '#a08c84' },
          axisLine: { lineStyle: { color: '#f5ece6' } }
        },
        yAxis: {
          type: 'value',
          name: '新增数量',
          nameTextStyle: { color: '#a08c84' },
          minInterval: 1,
          splitLine: { lineStyle: { color: '#f5ece6', type: 'dashed' } }
        },
        series: [{
          data: chartData.map(item => item.count),
          type: 'bar',
          barWidth: '50%',
          itemStyle: {
            borderRadius: [5, 5, 0, 0],
            color: {
              type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#f59e4b' },
                { offset: 1, color: '#fcd9b4' }
              ]
            }
          },
          label: { show: true, position: 'top', color: '#a08c84', formatter: '{c}只' }
        }]
      });
    },

    initGenderChart() {
      const chart = this.initChart('genderChart');
      if (!chart) return;
      const genderStats = this.statistics.genderStats || { male: 0, female: 0, unknown: 0 };

      const maleCount = Number(genderStats.male) || 0;
      const femaleCount = Number(genderStats.female) || 0;
      const unknownCount = Number(genderStats.unknown) || 0;

      if (maleCount === 0 && femaleCount === 0 && unknownCount === 0) {
        chart.setOption({
          title: {
            show: true,
            text: '暂无性别数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#a08c84', fontSize: 14 }
          }
        });
        return;
      }

      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '8%', top: 10, bottom: 0, containLabel: true },
        xAxis: {
          type: 'value',
          name: '数量',
          nameTextStyle: { color: '#a08c84' },
          splitLine: { lineStyle: { color: '#f5ece6', type: 'dashed' } }
        },
        yAxis: {
          type: 'category',
          data: ['公', '母', '未知'],
          axisLabel: { fontSize: 14, color: '#5c4a42' },
          axisLine: { lineStyle: { color: '#f5ece6' } }
        },
        series: [{
          data: [
            { name: '公', value: maleCount, itemStyle: { color: '#f59e4b' } },
            { name: '母', value: femaleCount, itemStyle: { color: '#f56c6c' } },
            { name: '未知', value: unknownCount, itemStyle: { color: '#909399' } }
          ],
          type: 'bar',
          barWidth: '40%',
          label: { show: true, position: 'right', color: '#a08c84', formatter: '{c}只' }
        }]
      });
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.pet-analysis-page {
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
.card-total .stat-icon { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-dog .stat-icon   { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-cat .stat-icon   { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-today .stat-icon { background: linear-gradient(135deg, #e6a23c, #f7c06b); }

.stat-info { flex: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 5px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3d2e2a; letter-spacing: -0.5px; }

.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ===== 图表区域 ===== */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 22px 24px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}
.chart-card-full { grid-column: 1 / -1; }

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
.chart-box-short {
  height: 300px;
}

/* 响应式 */
@media (max-width: 768px) {
  .pet-analysis-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .charts-row { grid-template-columns: 1fr; }
}
</style>
