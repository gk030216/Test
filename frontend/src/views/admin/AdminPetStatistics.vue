<template>
  <div class="admin-pet-statistics">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-s-custom"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.totalPets || 0 }}</div>
            <div class="stat-label">总宠物数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-dog"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.dogCount || 0 }}</div>
            <div class="stat-label">狗狗</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-cat"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.catCount || 0 }}</div>
            <div class="stat-label">猫咪</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-plus"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.todayNew || 0 }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>宠物类型分布</span>
            <el-tag size="small" type="info">按类型统计</el-tag>
          </div>
          <div id="typeChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>近7天新增趋势</span>
            <el-tag size="small" type="info">每日新增宠物数</el-tag>
          </div>
          <div id="trendChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 性别分布 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>性别分布</span>
            <el-tag size="small" type="info">公母比例</el-tag>
          </div>
          <div id="genderChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getPetStatistics } from '@/api/pet';
import * as echarts from 'echarts';

export default {
  name: 'AdminPetStatistics',
  data() {
    return {
      statistics: {
        totalPets: 0,
        dogCount: 0,
        catCount: 0,
        todayNew: 0,
        typeCount: {},
        weeklyTrend: [],
        genderStats: { male: 0, female: 0, unknown: 0 }
      }
    };
  },
  created() {
    this.loadStatistics();
  },
  beforeDestroy() {
    // 销毁图表实例，防止内存泄漏
    const typeChart = echarts.getInstanceByDom(document.getElementById('typeChart'));
    const trendChart = echarts.getInstanceByDom(document.getElementById('trendChart'));
    const genderChart = echarts.getInstanceByDom(document.getElementById('genderChart'));
    if (typeChart) typeChart.dispose();
    if (trendChart) trendChart.dispose();
    if (genderChart) genderChart.dispose();
  },
  methods: {
    async loadStatistics() {
      try {
        const res = await getPetStatistics();
        if (res.code === 200) {
          this.statistics = res.data;
          // 确保 genderStats 有默认值
          if (!this.statistics.genderStats) {
            this.statistics.genderStats = { male: 0, female: 0, unknown: 0 };
          }
          this.$nextTick(() => {
            this.initCharts();
          });
        }
      } catch (error) {
        console.error('加载统计数据失败', error);
        this.$message.error('加载统计数据失败');
      }
    },

    initCharts() {
      this.initTypeChart();
      this.initTrendChart();
      this.initGenderChart();
    },

    // 时间戳转日期格式 (MM-DD)
    formatTimestamp(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${month}-${day}`;
    },

    initTypeChart() {
      const chart = echarts.init(document.getElementById('typeChart'));
      const typeCount = this.statistics.typeCount || {};

      const data = [
        { name: '狗狗', value: typeCount.dog || 0 },
        { name: '猫咪', value: typeCount.cat || 0 },
        { name: '兔子', value: typeCount.rabbit || 0 },
        { name: '其他', value: (typeCount.other || 0) + (typeCount.bird || 0) + (typeCount.fish || 0) }
      ].filter(item => item.value > 0);

      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}只)' },
        legend: { orient: 'vertical', left: 'left', data: data.map(d => d.name) },
        series: [{
          type: 'pie',
          radius: '55%',
          center: ['50%', '50%'],
          data: data,
          label: { show: true, formatter: '{b}: {d}%' },
          emphasis: { scale: true },
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          }
        }],
        color: ['#409EFF', '#67c23a', '#e6a23c', '#909399']
      });

      window.addEventListener('resize', () => chart.resize());
    },

    initTrendChart() {
      const chart = echarts.init(document.getElementById('trendChart'));
      const trend = this.statistics.weeklyTrend || [];

      // 处理日期格式，确保是完整的7天
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

      // 将后端数据转换为 Map
      const dataMap = new Map();
      if (trend && trend.length > 0) {
        trend.forEach(item => {
          // 处理日期格式
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

      // 构建完整的7天数据
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
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: chartData.map(item => item.date),
          axisLine: { lineStyle: { color: '#999' } },
          axisLabel: {
            rotate: 0,
            fontSize: 11
          }
        },
        yAxis: {
          type: 'value',
          name: '新增数量',
          nameStyle: { color: '#999' },
          minInterval: 1
        },
        series: [{
          data: chartData.map(item => item.count),
          type: 'bar',
          barWidth: '50%',
          itemStyle: {
            borderRadius: [4, 4, 0, 0],
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#409EFF' },
                { offset: 1, color: '#66b1ff' }
              ]
            }
          },
          label: {
            show: true,
            position: 'top',
            color: '#333',
            formatter: '{c}只'
          }
        }]
      });

      window.addEventListener('resize', () => {
        chart.resize();
      });
    },

    initGenderChart() {
      const chart = echarts.init(document.getElementById('genderChart'));
      const genderStats = this.statistics.genderStats || { male: 0, female: 0, unknown: 0 };

      const maleCount = Number(genderStats.male) || 0;
      const femaleCount = Number(genderStats.female) || 0;
      const unknownCount = Number(genderStats.unknown) || 0;

      // 如果没有数据，显示提示
      if (maleCount === 0 && femaleCount === 0 && unknownCount === 0) {
        chart.setOption({
          title: {
            show: true,
            text: '暂无性别数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#999', fontSize: 14 }
          }
        });
        return;
      }

      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '10%', right: '10%', top: '10%', bottom: '5%', containLabel: true },
        xAxis: { type: 'value', name: '数量' },
        yAxis: {
          type: 'category',
          data: ['公', '母', '未知'],
          axisLabel: { fontSize: 14 }
        },
        series: [{
          data: [
            { name: '公', value: maleCount, itemStyle: { color: '#409EFF' } },
            { name: '母', value: femaleCount, itemStyle: { color: '#f56c6c' } },
            { name: '未知', value: unknownCount, itemStyle: { color: '#909399' } }
          ],
          type: 'bar',
          barWidth: '40%',
          label: { show: true, position: 'right', formatter: '{c}只' }
        }]
      });

      window.addEventListener('resize', () => chart.resize());
    }
  }
};
</script>

<style scoped>
.admin-pet-statistics {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.stat-card {
  border-radius: 16px;
  overflow: hidden;
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
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-card {
  border-radius: 16px;
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
  .admin-pet-statistics {
    padding: 12px;
  }
}
</style>