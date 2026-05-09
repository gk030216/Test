<template>
  <div class="service-analysis-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">服务分析</h2>
      <p class="page-desc">服务预约排行、分类分布与价格区间统计</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-service"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">服务总数</div>
          <div class="stat-value">{{ stats.totalServices || 0 }}</div>
          <div class="stat-sub">平台服务总量</div>
        </div>
      </div>
      <div class="stat-card card-appointment">
        <div class="stat-icon">
          <i class="el-icon-s-order"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总预约数</div>
          <div class="stat-value">{{ stats.totalAppointments || 0 }}</div>
          <div class="stat-sub">累计预约总量</div>
        </div>
      </div>
      <div class="stat-card card-income">
        <div class="stat-icon">
          <i class="el-icon-money"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总收入</div>
          <div class="stat-value">¥{{ formatMoney(stats.totalRevenue) }}</div>
          <div class="stat-sub">服务收入合计</div>
        </div>
      </div>
      <div class="stat-card card-rating">
        <div class="stat-icon">
          <i class="el-icon-star-on"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">平均评分</div>
          <div class="stat-value">{{ stats.avgRating || 0 }}</div>
          <div class="stat-sub">综合评价分数</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 - 第一行 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 分类服务占比</span>
          <el-tag size="small" type="info">按服务数量</el-tag>
        </div>
        <div ref="categoryChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-data-line"></i> 预约排行 Top 10</span>
          <el-tag size="small" type="info">按预约次数</el-tag>
        </div>
        <div ref="rankChart" class="chart-box"></div>
      </div>
    </div>

    <!-- 图表区域 - 第二行 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 价格区间分布</span>
          <el-tag size="small" type="info">服务价格</el-tag>
        </div>
        <div ref="priceChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 服务状态</span>
          <el-tag size="small" type="info">上架/下架</el-tag>
        </div>
        <div ref="statusChart" class="chart-box"></div>
      </div>
    </div>

    <!-- 热门服务列表 -->
    <div class="charts-row">
      <div class="chart-card chart-card-full">
        <div class="chart-header">
          <span><i class="el-icon-star-on"></i> 热门服务推荐</span>
          <el-button type="text" @click="$router.push('/admin/service-list')" class="view-more">查看更多</el-button>
        </div>
        <div class="hot-services" v-loading="hotLoading">
          <div class="service-item" v-for="service in hotServices" :key="service.id" @click="$router.push('/admin/service-list')">
            <el-image :src="service.image" class="service-img" fit="cover">
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
            <div class="service-info">
              <div class="service-name">{{ service.name }}</div>
              <div class="service-price">¥{{ service.price }}</div>
            </div>
            <div class="service-stats">
              <span class="stat-num">{{ service.appointmentCount || service.sales || 0 }}</span>
              <span class="stat-text">预约</span>
            </div>
          </div>
          <div v-if="!hotLoading && hotServices.length === 0" class="empty-data">
            <i class="el-icon-service"></i>
            <p>暂无服务数据</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import {
  getServiceStatistics,
  getHotServices,
  getAdminItemList,
  getAllServiceCategories,
  getAppointmentStatistics,
  getServiceCommentList
} from '@/api/service';

export default {
  name: 'Admin_ServiceAnalysis',
  data() {
    return {
      loading: false,
      hotLoading: false,
      stats: {
        totalServices: 0,
        totalAppointments: 0,
        totalRevenue: 0,
        avgRating: 0
      },
      categoryDistribution: [],
      appointmentRank: [],
      priceDistribution: [],
      statusDistribution: [],
      hotServices: [],
      serviceList: [],
      categories: [],
      charts: []
    };
  },
  mounted() {
    this.loadData();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    this.charts.forEach(chart => chart.dispose());
    this.charts = [];
  },
  methods: {
    async loadData() {
      this.loading = true;
      this.hotLoading = true;

      try {
        const [statsRes, hotRes, serviceListRes, categoriesRes, appointmentStatsRes] = await Promise.all([
          getServiceStatistics(),
          getHotServices(8),
          getAdminItemList({ page: 1, pageSize: 1000 }),
          getAllServiceCategories(),
          getAppointmentStatistics()
        ]);

        if (statsRes.code === 200) {
          const data = statsRes.data;
          this.stats.totalServices = data.totalServices || data.total || 0;
        }

        if (appointmentStatsRes.code === 200) {
          const appointmentData = appointmentStatsRes.data;
          this.stats.totalAppointments = appointmentData.total || 0;
          this.stats.totalRevenue = appointmentData.totalIncome || 0;
        }

        if (serviceListRes.code === 200) {
          this.serviceList = serviceListRes.data.list || [];
          if (this.stats.totalServices === 0) {
            this.stats.totalServices = this.serviceList.length;
          }
          this.priceDistribution = this.calcPriceDistribution(this.serviceList);
          this.statusDistribution = this.calcStatusDistribution(this.serviceList);
          this.appointmentRank = this.calcAppointmentRank(this.serviceList);
        }

        if (categoriesRes.code === 200) {
          this.categories = categoriesRes.data || [];
          this.categoryDistribution = this.calcCategoryDistribution(this.serviceList, this.categories);
        }

        await this.loadAvgRating();

        if (hotRes.code === 200) {
          const hotData = hotRes.data || [];
          this.hotServices = hotData.map(item => ({
            id: item.id,
            name: item.name,
            price: item.price || 0,
            image: item.image || '',
            appointmentCount: item.count || item.appointmentCount || item.sales || 0,
            sales: item.sales || 0
          }));
        }

        this.$nextTick(() => this.initCharts());
      } catch (error) {
        this.$message.error(error?.message || '加载数据失败');
      } finally {
        this.loading = false;
        this.hotLoading = false;
      }
    },

    async loadAvgRating() {
      try {
        const res = await getServiceCommentList({ page: 1, pageSize: 1000 });
        if (res.code === 200 && res.data.list && res.data.list.length > 0) {
          const comments = res.data.list;
          let totalRating = 0;
          let commentCount = 0;
          comments.forEach(comment => {
            if (comment.rating) {
              totalRating += comment.rating;
              commentCount++;
            }
          });
          this.stats.avgRating = commentCount > 0 ? (totalRating / commentCount).toFixed(1) : 0;
        } else {
          this.stats.avgRating = 0;
        }
      } catch {
        this.stats.avgRating = 0;
      }
    },

    calcCategoryDistribution(services, categories) {
      const categoryMap = new Map();
      categories.forEach(cat => categoryMap.set(cat.id, { name: cat.name, count: 0 }));
      services.forEach(service => {
        const categoryId = service.categoryId;
        if (categoryId && categoryMap.has(categoryId)) {
          categoryMap.get(categoryId).count++;
        } else {
          if (!categoryMap.has('uncategorized')) {
            categoryMap.set('uncategorized', { name: '未分类', count: 0 });
          }
          categoryMap.get('uncategorized').count++;
        }
      });
      const result = [];
      for (const [, value] of categoryMap) {
        if (value.count > 0) result.push({ name: value.name, value: value.count });
      }
      return result;
    },

    calcPriceDistribution(services) {
      const ranges = [
        { name: '0-50元', min: 0, max: 50, count: 0 },
        { name: '50-100元', min: 50, max: 100, count: 0 },
        { name: '100-200元', min: 100, max: 200, count: 0 },
        { name: '200-500元', min: 200, max: 500, count: 0 },
        { name: '500元以上', min: 500, max: Infinity, count: 0 }
      ];
      services.forEach(service => {
        const price = service.price;
        if (price !== undefined && price !== null) {
          for (const range of ranges) {
            if (price >= range.min && price < range.max) {
              range.count++;
              break;
            }
          }
        }
      });
      return ranges.filter(r => r.count > 0).map(r => ({ name: r.name, value: r.count }));
    },

    calcStatusDistribution(services) {
      let onShelf = 0;
      let offShelf = 0;
      services.forEach(service => {
        if (service.status === 1) onShelf++;
        else offShelf++;
      });
      return [
        { name: '上架', value: onShelf },
        { name: '下架', value: offShelf }
      ];
    },

    calcAppointmentRank(services) {
      const sorted = [...services].sort((a, b) => (b.sales || 0) - (a.sales || 0));
      return sorted.slice(0, 10).map(service => ({
        name: service.name,
        count: service.sales || 0
      }));
    },

    formatMoney(value) {
      if (value === undefined || value === null) return '0.00';
      if (typeof value === 'number') return value.toFixed(2);
      return value;
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

    initCharts() {
      this.initCategoryChart();
      this.initRankChart();
      this.initPriceChart();
      this.initStatusChart();
    },

    initCategoryChart() {
      const chart = this.initChart('categoryChart');
      if (!chart) return;
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}个)' },
        legend: { orient: 'vertical', left: 6, top: 'center', textStyle: { color: '#5c4a42' } },
        series: [{
          type: 'pie',
          radius: ['48%', '74%'],
          center: ['58%', '50%'],
          data: this.categoryDistribution,
          label: { show: true, formatter: '{b}\n{d}%', color: '#5c4a42' },
          emphasis: { scale: true },
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }
        }],
        color: ['#f59e4b', '#67c23a', '#e6a23c', '#f56c6c', '#409eff', '#909399', '#85ce61']
      });
    },

    initRankChart() {
      const chart = this.initChart('rankChart');
      if (!chart) return;

      if (this.appointmentRank.length === 0) {
        chart.setOption({
          title: { show: true, text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#a08c84', fontSize: 14 } }
        });
        return;
      }

      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '8%', top: 10, bottom: 0, containLabel: true },
        xAxis: { type: 'value', name: '预约次数', nameTextStyle: { color: '#a08c84' } },
        yAxis: {
          type: 'category',
          data: this.appointmentRank.map(item => item.name),
          axisLabel: { fontSize: 11, color: '#5c4a42', width: 100, overflow: 'truncate' },
          axisLine: { lineStyle: { color: '#f5ece6' } }
        },
        series: [{
          data: this.appointmentRank.map(item => item.count),
          type: 'bar',
          barWidth: '55%',
          itemStyle: {
            borderRadius: [0, 4, 4, 0],
            color: {
              type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
              colorStops: [
                { offset: 0, color: '#f59e4b' },
                { offset: 1, color: '#f7c06b' }
              ]
            }
          },
          label: { show: true, position: 'right', color: '#a08c84', formatter: '{c}' }
        }]
      });
    },

    initPriceChart() {
      const chart = this.initChart('priceChart');
      if (!chart) return;

      if (this.priceDistribution.length === 0) {
        chart.setOption({
          title: { show: true, text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#a08c84', fontSize: 14 } }
        });
        return;
      }

      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}个)' },
        legend: { orient: 'vertical', left: 6, top: 'center', textStyle: { color: '#5c4a42' } },
        series: [{
          type: 'pie',
          radius: ['48%', '74%'],
          center: ['58%', '50%'],
          data: this.priceDistribution,
          label: { show: true, formatter: '{b}\n{d}%', color: '#5c4a42' },
          emphasis: { scale: true },
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }
        }],
        color: ['#67c23a', '#f59e4b', '#e6a23c', '#409eff', '#909399']
      });
    },

    initStatusChart() {
      const chart = this.initChart('statusChart');
      if (!chart) return;

      if (this.statusDistribution.length === 0) {
        chart.setOption({
          title: { show: true, text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#a08c84', fontSize: 14 } }
        });
        return;
      }

      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}个)' },
        legend: { orient: 'vertical', left: 6, top: 'center', textStyle: { color: '#5c4a42' } },
        series: [{
          type: 'pie',
          radius: ['48%', '74%'],
          center: ['58%', '50%'],
          data: this.statusDistribution,
          label: { show: true, formatter: '{b}\n{d}%', color: '#5c4a42' },
          emphasis: { scale: true },
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }
        }],
        color: ['#67c23a', '#f56c6c']
      });
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.service-analysis-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
  overflow-x: hidden;
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
.card-total .stat-icon       { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-appointment .stat-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-income .stat-icon      { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-rating .stat-icon      { background: linear-gradient(135deg, #e6a23c, #f7c06b); }

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

.view-more { color: #f59e4b !important; font-size: 13px; }
.view-more:hover { color: #e8833a !important; }

.chart-box { height: 350px; }

/* ===== 热门服务 ===== */
.hot-services { max-height: 400px; overflow-y: auto; }

.service-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 14px 0;
  border-bottom: 1px solid #f5ece6;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}
.service-item:hover {
  background: #fefbf8;
  transform: translateX(4px);
}
.service-item:last-child { border-bottom: none; }

.service-img {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  background: #fdf8f4;
  flex-shrink: 0;
}

.service-info { flex: 1; min-width: 0; }
.service-name {
  font-weight: 500;
  color: #3d2e2a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.service-price { color: #f56c6c; font-weight: 600; }

.service-stats { text-align: right; min-width: 80px; }
.stat-num { font-size: 18px; font-weight: 700; color: #f59e4b; }
.stat-text { font-size: 12px; color: #a08c84; margin-left: 4px; }

.empty-data {
  text-align: center;
  padding: 48px;
  color: #a08c84;
}
.empty-data i { font-size: 48px; margin-bottom: 16px; display: block; }

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #fdf8f4;
  color: #a08c84;
}

@media (max-width: 768px) {
  .service-analysis-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .charts-row { grid-template-columns: 1fr; }
}
</style>
