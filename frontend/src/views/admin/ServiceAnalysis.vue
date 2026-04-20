<template>
  <div class="service-analysis">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-service"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalServices || 0 }}</div>
            <div class="stat-label">服务总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-sales">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalAppointments || 0 }}</div>
            <div class="stat-label">总预约数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-income">
            <i class="el-icon-money"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">¥{{ formatMoney(stats.totalRevenue) }}</div>
            <div class="stat-label">总收入</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-rating">
            <i class="el-icon-star-on"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.avgRating || 0 }}</div>
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
            <span>分类服务占比</span>
            <el-tag size="small" type="info">按服务数量</el-tag>
          </div>
          <div id="categoryChart" style="height: 320px" v-loading="chartLoading"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>预约排行 Top 10</span>
            <el-tag size="small" type="info">按预约次数</el-tag>
          </div>
          <div id="appointmentRankChart" style="height: 320px" v-loading="chartLoading"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>价格区间分布</span>
            <el-tag size="small" type="info">服务价格</el-tag>
          </div>
          <div id="priceChart" style="height: 320px" v-loading="chartLoading"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>服务状态</span>
            <el-tag size="small" type="info">上架/下架</el-tag>
          </div>
          <div id="statusChart" style="height: 320px" v-loading="chartLoading"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门服务列表 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="hot-service-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>🔥 热门服务推荐</span>
            <el-button type="text" @click="$router.push('/admin/service-list')">查看更多</el-button>
          </div>
          <div class="hot-services" v-loading="hotLoading">
            <div class="service-item" v-for="service in hotServices" :key="service.id" @click="goToServiceDetail(service.id)">
              <el-image
                  :src="service.image"
                  class="service-img"
                  fit="cover"
              >
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
                <span class="stat-label">预约</span>
              </div>
            </div>
            <div v-if="!hotLoading && hotServices.length === 0" class="empty-data">
              <i class="el-icon-service"></i>
              <p>暂无服务数据</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getServiceStatistics, getHotServices, getAdminItemList, getAllServiceCategories } from '@/api/service';
import { getAppointmentStatistics } from '@/api/service';

export default {
  name: 'ServiceAnalysis',
  data() {
    return {
      loading: false,
      hotLoading: false,
      chartLoading: false,
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
      // 缓存数据
      serviceList: [],
      categories: []
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      this.chartLoading = true;
      this.hotLoading = true;

      try {
        // 并行请求多个接口
        const [statsRes, hotRes, serviceListRes, categoriesRes, appointmentStatsRes] = await Promise.all([
          getServiceStatistics(),
          getHotServices(8),
          getAdminItemList({ page: 1, pageSize: 1000 }),
          getAllServiceCategories(),
          getAppointmentStatistics()
        ]);

        // 1. 处理基础统计
        if (statsRes.code === 200) {
          const data = statsRes.data;
          this.stats.totalServices = data.totalServices || data.total || 0;
        }

        // 2. 处理预约统计（获取总预约数和总收入）
        if (appointmentStatsRes.code === 200) {
          const appointmentData = appointmentStatsRes.data;
          this.stats.totalAppointments = appointmentData.total || 0;
          this.stats.totalRevenue = appointmentData.totalIncome || 0;
        }

        // 3. 处理服务列表
        if (serviceListRes.code === 200) {
          this.serviceList = serviceListRes.data.list || [];

          // 如果没有从 stats 获取到服务总数，从列表获取
          if (this.stats.totalServices === 0) {
            this.stats.totalServices = this.serviceList.length;
          }

          // 计算价格区间分布
          this.priceDistribution = this.calculatePriceDistribution(this.serviceList);

          // 计算状态分布
          this.statusDistribution = this.calculateStatusDistribution(this.serviceList);

          // 计算预约排行
          this.appointmentRank = this.calculateAppointmentRank(this.serviceList);
        }

        // 4. 处理分类数据
        if (categoriesRes.code === 200) {
          this.categories = categoriesRes.data || [];
          // 计算分类服务占比
          this.categoryDistribution = this.calculateCategoryDistribution(this.serviceList, this.categories);
        }

        // 5. ✅ 获取平均评分（调用评价统计接口）
        await this.loadAvgRating();

        // 6. 处理热门服务
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

        // 初始化图表
        this.$nextTick(() => {
          this.initCharts();
        });

      } catch (error) {
        console.error('加载数据失败', error);
        this.$message.error('加载数据失败');
      } finally {
        this.loading = false;
        this.chartLoading = false;
        this.hotLoading = false;
      }
    },

    // 获取平均评分
    async loadAvgRating() {
      try {
        // 临时方案：调用评价列表接口获取所有评价，计算平均分
        // 注意：这个接口可能需要分页，这里只取第一页
        const { getServiceCommentList } = await import('@/api/service');

        // 获取所有评价（不分页，取足够多的数据）
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

          if (commentCount > 0) {
            this.stats.avgRating = (totalRating / commentCount).toFixed(1);
          } else {
            this.stats.avgRating = 0;
          }
        } else {
          this.stats.avgRating = 0;
        }
      } catch (error) {
        console.error('加载平均评分失败', error);
        // 如果获取失败，设置为0
        this.stats.avgRating = 0;
      }
    },

    // 计算分类服务占比
    calculateCategoryDistribution(services, categories) {
      const categoryMap = new Map();

      // 初始化分类计数
      categories.forEach(cat => {
        categoryMap.set(cat.id, {
          name: cat.name,
          count: 0
        });
      });

      // 统计每个分类的服务数量
      services.forEach(service => {
        const categoryId = service.categoryId;
        if (categoryId && categoryMap.has(categoryId)) {
          const cat = categoryMap.get(categoryId);
          cat.count++;
        } else {
          // 未分类
          if (!categoryMap.has('uncategorized')) {
            categoryMap.set('uncategorized', { name: '未分类', count: 0 });
          }
          categoryMap.get('uncategorized').count++;
        }
      });

      // 转换为图表数据格式，过滤掉数量为0的分类
      const result = [];
      for (const [id, value] of categoryMap) {
        if (value.count > 0) {
          result.push({
            name: value.name,
            value: value.count
          });
        }
      }

      return result;
    },

    // 计算价格区间分布
    calculatePriceDistribution(services) {
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

      return ranges.filter(r => r.count > 0).map(r => ({
        name: r.name,
        value: r.count
      }));
    },

    // 计算服务状态分布
    calculateStatusDistribution(services) {
      let onShelf = 0;
      let offShelf = 0;

      services.forEach(service => {
        if (service.status === 1) {
          onShelf++;
        } else {
          offShelf++;
        }
      });

      return [
        { name: '上架', value: onShelf },
        { name: '下架', value: offShelf }
      ];
    },

    // 计算预约排行（按销量排序）
    calculateAppointmentRank(services) {
      const sorted = [...services].sort((a, b) => (b.sales || 0) - (a.sales || 0));
      const top10 = sorted.slice(0, 10);

      return top10.map(service => ({
        name: service.name,
        count: service.sales || 0
      }));
    },

    formatMoney(value) {
      if (value === undefined || value === null) return '0.00';
      if (typeof value === 'number') {
        return value.toFixed(2);
      }
      return value;
    },

    initCharts() {
      this.initCategoryChart();
      this.initAppointmentRankChart();
      this.initPriceChart();
      this.initStatusChart();
    },

    initCategoryChart() {
      const chartDom = document.getElementById('categoryChart');
      if (!chartDom) return;

      let chart = echarts.getInstanceByDom(chartDom);
      if (chart) {
        chart.dispose();
      }

      chart = echarts.init(chartDom);
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}个)' },
        legend: { orient: 'vertical', left: 'left', type: 'scroll' },
        series: [{
          type: 'pie',
          radius: '55%',
          center: ['50%', '50%'],
          data: this.categoryDistribution,
          label: { show: true, formatter: '{b}: {d}%' },
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 }
        }],
        color: ['#409EFF', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#667eea', '#764ba2']
      });

      window.addEventListener('resize', () => chart.resize());
    },

    initAppointmentRankChart() {
      const chartDom = document.getElementById('appointmentRankChart');
      if (!chartDom) return;

      let chart = echarts.getInstanceByDom(chartDom);
      if (chart) {
        chart.dispose();
      }

      chart = echarts.init(chartDom);

      if (this.appointmentRank.length === 0) {
        chart.setOption({
          title: {
            show: true,
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#999', fontSize: 14 }
          }
        });
        return;
      }

      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '15%', right: '5%', bottom: '5%', containLabel: true },
        xAxis: { type: 'value', name: '预约次数' },
        yAxis: {
          type: 'category',
          data: this.appointmentRank.map(item => item.name),
          axisLabel: { rotate: 0, interval: 0, fontSize: 11 }
        },
        series: [{
          data: this.appointmentRank.map(item => item.count),
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

      window.addEventListener('resize', () => chart.resize());
    },

    initPriceChart() {
      const chartDom = document.getElementById('priceChart');
      if (!chartDom) return;

      let chart = echarts.getInstanceByDom(chartDom);
      if (chart) {
        chart.dispose();
      }

      chart = echarts.init(chartDom);

      if (this.priceDistribution.length === 0) {
        chart.setOption({
          title: {
            show: true,
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#999', fontSize: 14 }
          }
        });
        return;
      }

      chart.setOption({
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

      window.addEventListener('resize', () => chart.resize());
    },

    initStatusChart() {
      const chartDom = document.getElementById('statusChart');
      if (!chartDom) return;

      let chart = echarts.getInstanceByDom(chartDom);
      if (chart) {
        chart.dispose();
      }

      chart = echarts.init(chartDom);

      if (this.statusDistribution.length === 0) {
        chart.setOption({
          title: {
            show: true,
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#999', fontSize: 14 }
          }
        });
        return;
      }

      chart.setOption({
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

      window.addEventListener('resize', () => chart.resize());
    },

    goToServiceDetail(id) {
      this.$router.push(`/admin/service-list`);
    }
  }
};
</script>

<style scoped>
.service-analysis {
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

.stat-sales { background: linear-gradient(135deg, #409EFF, #66b1ff); }
.stat-income { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-rating { background: linear-gradient(135deg, #e6a23c, #f0b45c); }

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

.chart-card, .hot-service-card {
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

.hot-services {
  max-height: 400px;
  overflow-y: auto;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.service-item:hover {
  background: #f8f9fc;
  transform: translateX(4px);
}

.service-item:last-child {
  border-bottom: none;
}

.service-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #f5f5f5;
  flex-shrink: 0;
}

.service-info {
  flex: 1;
}

.service-name {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 4px;
}

.service-price {
  color: #ff6b6b;
  font-weight: 600;
}

.service-stats {
  text-align: right;
  min-width: 80px;
}

.stat-num {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
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

@media (max-width: 768px) {
  .service-analysis {
    padding: 12px;
  }
}
</style>