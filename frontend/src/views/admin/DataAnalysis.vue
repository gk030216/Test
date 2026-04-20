<template>
  <div class="data-analysis">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-goods"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalProducts }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-shopping-cart-2"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalSales }}</div>
            <div class="stat-label">总销量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-money"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">¥{{ stats.totalRevenue }}</div>
            <div class="stat-label">总销售额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-star-on"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.avgRating }}</div>
            <div class="stat-label">平均评分</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>分类商品占比</span>
            <el-tag size="small" type="info">按商品数量</el-tag>
          </div>
          <div id="categoryChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>销量排行 Top 10</span>
            <el-tag size="small" type="info">按销量排序</el-tag>
          </div>
          <div id="salesRankChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>价格区间分布</span>
            <el-tag size="small" type="info">商品价格</el-tag>
          </div>
          <div id="priceChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>上架商品状态</span>
            <el-tag size="small" type="info">上架/下架</el-tag>
          </div>
          <div id="statusChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热销商品列表 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="hot-product-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>🔥 热销商品推荐</span>
            <el-button type="text" @click="$router.push('/admin/product-list')">查看更多</el-button>
          </div>
          <div class="hot-products">
            <div class="product-item" v-for="product in hotProducts" :key="product.id">
              <el-image
                  :src="product.image"
                  class="product-img"
                  fit="cover"
              >
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
            <div v-if="hotProducts.length === 0" class="empty-data">
              <i class="el-icon-shopping-cart-2"></i>
              <p>暂无商品数据</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getProductStatistics, getAdminHotProducts  } from '@/api/product';

export default {
  name: 'DataAnalysis',
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
      hotProducts: []
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        // 获取统计数据
        const statsRes = await getProductStatistics();
        if (statsRes.code === 200) {
          this.stats = statsRes.data;

          // 处理销量排行数据
          if (statsRes.data.salesRank && statsRes.data.salesRank.length > 0) {
            this.salesRank = statsRes.data.salesRank.map(item => ({
              name: item.name || item.productName,
              sales: item.sales || item.count || 0
            }));
          }

          // 处理分类分布
          if (statsRes.data.categoryDistribution && statsRes.data.categoryDistribution.length > 0) {
            this.categoryDistribution = statsRes.data.categoryDistribution;
          }

          // 处理价格分布
          if (statsRes.data.priceDistribution && statsRes.data.priceDistribution.length > 0) {
            this.priceDistribution = statsRes.data.priceDistribution;
          }

          // 处理状态分布
          if (statsRes.data.statusDistribution && statsRes.data.statusDistribution.length > 0) {
            this.statusDistribution = statsRes.data.statusDistribution;
          }

          this.initCharts();
        }

        // 获取热销商品
        const hotRes = await getAdminHotProducts(8);
        if (hotRes.code === 200) {
          // 处理热销商品数据
          if (hotRes.data && hotRes.data.length > 0) {
            this.hotProducts = hotRes.data.map(item => ({
              id: item.id,
              name: item.name,
              price: item.price,
              sales: item.sales || 0,
              image: item.image
            }));
          }
        }
      } catch (error) {
        console.error('加载数据失败', error);
        this.$message.error('加载数据失败');
      } finally {
        this.loading = false;
      }
    },

    initCharts() {
      // 分类占比饼图
      const categoryChart = echarts.init(document.getElementById('categoryChart'));
      categoryChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}个)' },
        legend: { orient: 'vertical', left: 'left', type: 'scroll' },
        series: [{
          type: 'pie',
          radius: '55%',
          center: ['50%', '50%'],
          data: this.categoryDistribution,
          label: { show: true, formatter: '{b}: {d}%' },
          emphasis: { scale: true },
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 }
        }],
        color: ['#409EFF', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#667eea', '#764ba2', '#85ce61']
      });

      // 销量排行柱状图
      const salesChart = echarts.init(document.getElementById('salesRankChart'));
      salesChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '15%', right: '5%', bottom: '5%', containLabel: true },
        xAxis: { type: 'value', name: '销量', nameStyle: { color: '#999' } },
        yAxis: {
          type: 'category',
          data: this.salesRank.map(item => item.name),
          axisLabel: { rotate: 0, interval: 0, fontSize: 11 }
        },
        series: [{
          data: this.salesRank.map(item => item.sales),
          type: 'bar',
          barWidth: '60%',
          itemStyle: {
            borderRadius: [0, 4, 4, 0],
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 1, y2: 0,
              colorStops: [{ offset: 0, color: '#409EFF' }, { offset: 1, color: '#67c23a' }]
            }
          },
          label: { show: true, position: 'right', formatter: '{c}' }
        }]
      });

      // 价格区间分布饼图
      const priceChart = echarts.init(document.getElementById('priceChart'));
      priceChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}个)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '50%'],
          data: this.priceDistribution,
          label: { show: true, formatter: '{b}: {d}%' },
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 }
        }],
        color: ['#67c23a', '#e6a23c', '#f56c6c', '#409EFF', '#909399']
      });

      // 商品状态饼图
      const statusChart = echarts.init(document.getElementById('statusChart'));
      statusChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}个)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: '55%',
          center: ['50%', '50%'],
          data: this.statusDistribution,
          label: { show: true, formatter: '{b}: {d}%' },
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 }
        }],
        color: ['#67c23a', '#f56c6c']
      });

      window.addEventListener('resize', () => {
        categoryChart.resize();
        salesChart.resize();
        priceChart.resize();
        statusChart.resize();
      });
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.data-analysis {
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
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.stat-icon i {
  font-size: 28px;
  color: white;
}

.stat-info {
  flex: 1;
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

.chart-card, .hot-product-card {
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

.hot-products {
  max-height: 400px;
  overflow-y: auto;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.product-item:last-child {
  border-bottom: none;
}

.product-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #f5f5f5;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 4px;
}

.product-price {
  color: #ff6b6b;
  font-weight: 600;
}

.product-sales {
  text-align: right;
  min-width: 80px;
}

.sales-num {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.sales-label {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.empty-data {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-data i {
  font-size: 48px;
  margin-bottom: 16px;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #999;
}
</style>