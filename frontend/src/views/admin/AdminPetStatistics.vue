<template>
  <div class="admin-pet-statistics">
<!--    &lt;!&ndash; 页面标题 &ndash;&gt;-->
<!--    <div class="page-header">-->
<!--      <h2>宠物数据统计</h2>-->
<!--      <p>宠物档案数据分析与可视化展示</p>-->
<!--    </div>-->

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
  methods: {
    async loadStatistics() {
      try {
        const res = await getPetStatistics();
        if (res.code === 200) {
          this.statistics = res.data;
          this.initCharts();
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
    initTypeChart() {
      const chart = echarts.init(document.getElementById('typeChart'));
      const typeCount = this.statistics.typeCount || {};

      const data = [
        { name: '狗狗', value: typeCount.dog || 0 },
        { name: '猫咪', value: typeCount.cat || 0 },
        { name: '兔子', value: typeCount.rabbit || 0 },
        { name: '其他', value: typeCount.other || 0 }
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

      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: trend.map(item => item.date),
          axisLine: { lineStyle: { color: '#999' } }
        },
        yAxis: {
          type: 'value',
          name: '新增数量',
          nameStyle: { color: '#999' }
        },
        series: [{
          data: trend.map(item => item.count),
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.3, color: '#409EFF' },
          lineStyle: { color: '#409EFF', width: 2 },
          symbol: 'circle',
          symbolSize: 8,
          itemStyle: { color: '#409EFF', borderColor: '#fff', borderWidth: 2 }
        }]
      });

      window.addEventListener('resize', () => chart.resize());
    },
    initGenderChart() {
      const chart = echarts.init(document.getElementById('genderChart'));
      const genderStats = this.statistics.genderStats || { male: 0, female: 0, unknown: 0 };

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
            { name: '公', value: genderStats.male || 0, itemStyle: { color: '#409EFF' } },
            { name: '母', value: genderStats.female || 0, itemStyle: { color: '#f56c6c' } },
            { name: '未知', value: genderStats.unknown || 0, itemStyle: { color: '#909399' } }
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

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
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