<template>
  <div class="product-analysis-page" v-loading="loading">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">商品分析</h2>
      <p class="page-desc">商品销量排行、分类分布与价格区间统计</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-product">
        <div class="stat-icon">
          <i class="el-icon-goods"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">商品总数</div>
          <div class="stat-value">{{ stats.totalProducts }}</div>
          <div class="stat-sub">平台商品总量</div>
        </div>
      </div>
      <div class="stat-card card-sales">
        <div class="stat-icon">
          <i class="el-icon-shopping-cart-2"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总销量</div>
          <div class="stat-value">{{ stats.totalSales }}</div>
          <div class="stat-sub">商品销售总量</div>
        </div>
      </div>
      <div class="stat-card card-revenue">
        <div class="stat-icon">
          <i class="el-icon-money"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总销售额</div>
          <div class="stat-value">¥{{ formatPrice(stats.totalRevenue) }}</div>
          <div class="stat-sub">累计销售金额</div>
        </div>
      </div>
      <div class="stat-card card-rating">
        <div class="stat-icon">
          <i class="el-icon-star-on"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">平均评分</div>
          <div class="stat-value">{{ formatRating(stats.avgRating) }}</div>
          <div class="stat-sub">用户综合评分</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 - 第一行 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 分类商品占比</span>
          <el-tag size="small" type="info">按商品数量</el-tag>
        </div>
        <div ref="categoryChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-data-line"></i> 销量排行 Top 10</span>
          <el-tag size="small" type="info">按销量排序</el-tag>
        </div>
        <div ref="salesRankChart" class="chart-box"></div>
      </div>
    </div>

    <!-- 图表区域 - 第二行 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 价格区间分布</span>
          <el-tag size="small" type="info">商品价格</el-tag>
        </div>
        <div ref="priceChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 上架商品状态</span>
          <el-tag size="small" type="info">上架/下架</el-tag>
        </div>
        <div ref="statusChart" class="chart-box"></div>
      </div>
    </div>

    <!-- 热销商品列表 -->
    <div class="charts-row">
      <div class="chart-card chart-card-full">
        <div class="chart-header">
          <span><i class="el-icon-star-on"></i> 热销商品推荐</span>
          <el-button type="text" @click="$router.push('/admin/product-list')" class="view-more">查看更多</el-button>
        </div>
        <div class="hot-products">
          <div class="product-item" v-for="product in hotProducts" :key="product.id" @click="$router.push('/admin/product-list')">
            <el-image :src="product.image" class="product-img" fit="cover">
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
            <div class="product-info">
              <div class="product-name">{{ product.name }}</div>
              <div class="product-price">¥{{ product.price }}</div>
            </div>
            <div class="product-sales">
              <span class="sales-num">{{ product.sales }}</span>
              <span class="sales-label">销量</span>
            </div>
          </div>
          <div v-if="hotProducts.length === 0 && !loading" class="empty-data">
            <i class="el-icon-shopping-cart-2"></i>
            <p>暂无商品数据</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getProductStatistics, getAdminHotProducts } from '@/api/product';

export default {
  name: 'Admin_ProductAnalysis',
  data() {
    return {
      loading: false,
      stats: {
        totalProducts: 0,
        totalSales: 0,
        totalRevenue: 0,
        avgRating: 0
      },
      categoryDistribution: [],
      salesRank: [],
      priceDistribution: [],
      statusDistribution: [],
      hotProducts: [],
      charts: []
    };
  },
  mounted() {
    this.loadData();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    this.disposeCharts();
  },
  methods: {
    async loadData() {
      this.loading = true;
      this.disposeCharts();
      try {
        const statsRes = await getProductStatistics();
        if (statsRes.code === 200) {
          this.stats = statsRes.data;

          if (statsRes.data.salesRank && statsRes.data.salesRank.length > 0) {
            this.salesRank = statsRes.data.salesRank.map(item => ({
              name: item.name,
              sales: item.sales || 0
            }));
          }

          if (statsRes.data.categoryDistribution) {
            this.categoryDistribution = statsRes.data.categoryDistribution;
          }
          if (statsRes.data.priceDistribution) {
            this.priceDistribution = statsRes.data.priceDistribution;
          }
          if (statsRes.data.statusDistribution) {
            this.statusDistribution = statsRes.data.statusDistribution;
          }
        }

        const hotRes = await getAdminHotProducts(8);
        if (hotRes.code === 200 && hotRes.data && hotRes.data.length > 0) {
          this.hotProducts = hotRes.data.map(item => ({
            id: item.id,
            name: item.name,
            price: item.price,
            sales: item.sales || 0,
            image: item.image
          }));
        }

        this.$nextTick(() => this.initCharts());
      } catch (error) {
        this.$message.error(error?.message || '加载数据失败');
      } finally {
        this.loading = false;
      }
    },

    disposeCharts() {
      this.charts.forEach(chart => {
        if (chart && !chart.isDisposed()) chart.dispose();
      });
      this.charts = [];
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
      this.initSalesRankChart();
      this.initPriceChart();
      this.initStatusChart();
    },

    initCategoryChart() {
      const chart = this.initChart('categoryChart');
      if (!chart) return;
      if (this.categoryDistribution.length === 0) {
        chart.setOption({
          title: { show: true, text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#a08c84', fontSize: 14 } }
        });
        return;
      }
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}个)' },
        legend: { orient: 'vertical', left: 6, top: 'center', textStyle: { color: '#5c4a42' }, type: 'scroll' },
        series: [{
          type: 'pie',
          radius: ['48%', '74%'],
          center: ['58%', '50%'],
          data: this.categoryDistribution,
          label: { show: true, formatter: '{b}\n{d}%', color: '#5c4a42' },
          emphasis: { scale: true },
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }
        }],
        color: ['#f59e4b', '#67c23a', '#e6a23c', '#f56c6c', '#409eff', '#909399', '#85ce61', '#f7c06b']
      });
    },

    initSalesRankChart() {
      const chart = this.initChart('salesRankChart');
      if (!chart) return;
      if (this.salesRank.length === 0) {
        chart.setOption({
          title: { show: true, text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#a08c84', fontSize: 14 } }
        });
        return;
      }
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '8%', top: 10, bottom: 0, containLabel: true },
        xAxis: { type: 'value', name: '销量', nameTextStyle: { color: '#a08c84' } },
        yAxis: {
          type: 'category',
          data: this.salesRank.map(item => item.name),
          axisLabel: { fontSize: 11, color: '#5c4a42', width: 100, overflow: 'truncate' },
          axisLine: { lineStyle: { color: '#f5ece6' } }
        },
        series: [{
          data: this.salesRank.map(item => item.sales),
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
    },

    formatRating(val) {
      if (val === undefined || val === null) return '0.0';
      return Number(val).toFixed(1);
    },

    formatPrice(val) {
      if (val === undefined || val === null) return '0';
      return Number(val).toLocaleString('zh-CN');
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.product-analysis-page {
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
.card-product .stat-icon { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-sales .stat-icon   { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-revenue .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-rating .stat-icon  { background: linear-gradient(135deg, #e6a23c, #f7c06b); }

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

/* ===== 热销商品 ===== */
.hot-products { max-height: 400px; overflow-y: auto; }

.product-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 14px 0;
  border-bottom: 1px solid #f5ece6;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}
.product-item:hover {
  background: #fefbf8;
  transform: translateX(4px);
}
.product-item:last-child { border-bottom: none; }

.product-img {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  background: #fdf8f4;
  flex-shrink: 0;
}

.product-info { flex: 1; min-width: 0; }
.product-name {
  font-weight: 500;
  color: #3d2e2a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-price { color: #f56c6c; font-weight: 600; }

.product-sales { text-align: right; min-width: 80px; }
.sales-num { font-size: 18px; font-weight: 700; color: #f59e4b; }
.sales-label { font-size: 12px; color: #a08c84; margin-left: 4px; }

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
  .product-analysis-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .charts-row { grid-template-columns: 1fr; }
}
</style>
