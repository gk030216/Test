<template>
  <div class="dashboard">

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
          <div class="stat-icon stat-today">
            <i class="el-icon-view"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.todayVisits || 0 }}</div>
            <div class="stat-label">今日访问</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-appointment">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.todayAppointments || 0 }}</div>
            <div class="stat-label">今日预约</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-income">
            <i class="el-icon-money"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">¥{{ statistics.todayIncome || 0 }}</div>
            <div class="stat-label">今日收入</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行统计 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-order">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.pendingAppointments || 0 }}</div>
            <div class="stat-label">待处理预约</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-pet">
            <i class="el-icon-s-custom"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.totalPets || 0 }}</div>
            <div class="stat-label">宠物总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-post">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.todayPosts || 0 }}</div>
            <div class="stat-label">今日帖子</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon stat-comment">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ statistics.todayComments || 0 }}</div>
            <div class="stat-label">今日评论</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>近7天预约趋势</span>
            <el-tag size="small" type="info">最近一周</el-tag>
          </div>
          <div id="appointmentChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>热门服务排行</span>
            <el-tag size="small" type="info">TOP 5</el-tag>
          </div>
          <div class="hot-service-list">
            <div
                class="hot-service-item"
                v-for="(item, index) in hotServices"
                :key="item.id"
            >
              <div class="service-rank" :class="'rank-' + (index + 1)">
                {{ index + 1 }}
              </div>
              <div class="service-info">
                <span class="service-name">{{ item.name }}</span>
                <el-progress
                    :percentage="item.percentage"
                    :stroke-width="8"
                    :show-text="false"
                    :color="getProgressColor(index)"
                ></el-progress>
              </div>
              <div class="service-count">{{ item.count }}次</div>
            </div>
            <div v-if="hotServices.length === 0" class="empty-data">
              暂无数据
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="quick-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>快捷入口</span>
          </div>
          <div class="quick-links">
            <div class="quick-item" @click="$router.push('/admin/user-list')">
              <i class="el-icon-user"></i>
              <span>用户管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/admin/appointment-list')">
              <i class="el-icon-s-order"></i>
              <span>预约管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/admin/service-list')">
              <i class="el-icon-service"></i>
              <span>服务管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/admin/product-list')">
              <i class="el-icon-goods"></i>
              <span>商品管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/admin/order-list')">
              <i class="el-icon-s-order"></i>
              <span>订单管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/admin/post-manage')">
              <i class="el-icon-document"></i>
              <span>帖子管理</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getDashboardStatistics } from '@/api/admin';

export default {
  name: 'AdminDashboard',
  data() {
    return {
      statistics: {
        totalUsers: 0,
        todayVisits: 0,
        todayAppointments: 0,
        todayIncome: 0,
        pendingAppointments: 0,
        totalPets: 0,
        todayPosts: 0,
        todayComments: 0
      },
      hotServices: [],
      weeklyTrend: []
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      try {
        const res = await getDashboardStatistics();
        if (res.code === 200) {
          this.statistics = res.data.statistics || this.statistics;
          this.hotServices = res.data.hotServices || [];
          this.weeklyTrend = res.data.weeklyTrend || [];
          this.initChart();
          this.calculatePercentage();
        }
      } catch (error) {
        console.error('加载仪表盘数据失败', error);
        // 使用模拟数据
        this.initChart();
      }
    },

    calculatePercentage() {
      const maxCount = Math.max(...this.hotServices.map(s => s.count), 1);
      this.hotServices.forEach(service => {
        service.percentage = Math.round((service.count / maxCount) * 100);
      });
    },

    getProgressColor(index) {
      const colors = ['#409EFF', '#67c23a', '#e6a23c', '#f56c6c', '#909399'];
      return colors[index] || '#409EFF';
    },

    initChart() {
      const chart = echarts.init(document.getElementById('appointmentChart'));

      // 生成近7天的日期数组
      const last7Days = [];
      for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        const dateStr = `${date.getMonth() + 1}/${date.getDate()}`;
        last7Days.push(dateStr);
      }

      // 将后端数据转换为 Map
      const dataMap = new Map();
      if (this.weeklyTrend && this.weeklyTrend.length > 0) {
        this.weeklyTrend.forEach(item => {
          // 将 yyyy-mm-dd 转换为 mm/dd 作为 key
          const date = new Date(item.date);
          const dateKey = `${date.getMonth() + 1}/${date.getDate()}`;
          dataMap.set(dateKey, item.count);
        });
      }else {
        // 使用模拟数据
        const mockData = [5, 8, 12, 7, 15, 20, 10];
        last7Days.forEach((date, index) => {
          dataMap.set(date, mockData[index]);
        });
      }

      // 构建完整的7天数据
      const chartData = last7Days.map(date => ({
        date: date,
        count: dataMap.get(date) || 0
      }));

      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
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
          axisLine: { lineStyle: { color: '#999' } }
        },
        yAxis: {
          type: 'value',
          name: '预约数量',
          nameStyle: { color: '#999' }
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
            color: '#333'
          }
        }]
      });

      window.addEventListener('resize', () => {
        chart.resize();
      });
    }
  }
};
</script>

<style scoped>
.dashboard {
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
  color: #999;
}

.stat-card {
  border-radius: 16px;
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
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon i {
  font-size: 28px;
  color: white;
}

.stat-today { background: linear-gradient(135deg, #409EFF, #66b1ff); }
.stat-appointment { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-income { background: linear-gradient(135deg, #e6a23c, #f0b45c); }
.stat-order { background: linear-gradient(135deg, #f56c6c, #ff8e8e); }
.stat-pet { background: linear-gradient(135deg, #909399, #b0b3b9); }
.stat-post { background: linear-gradient(135deg, #ff9a9e, #fad0c4); }
.stat-comment { background: linear-gradient(135deg, #a18cd1, #fbc2eb); }

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  line-height: 1.2;
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

.hot-service-list {
  padding: 10px 0;
}

.hot-service-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.hot-service-item:last-child {
  border-bottom: none;
}

.service-rank {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  margin-right: 16px;
  background: #f0f0f0;
  color: #666;
}

.rank-1 { background: #ffd700; color: white; }
.rank-2 { background: #c0c0c0; color: white; }
.rank-3 { background: #cd7f32; color: white; }

.service-info {
  flex: 1;
  margin-right: 16px;
}

.service-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 8px;
}

.service-count {
  font-weight: 600;
  color: #409EFF;
  font-size: 16px;
}

.empty-data {
  text-align: center;
  padding: 40px;
  color: #999;
}

.quick-card {
  border-radius: 16px;
  overflow: hidden;
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 10px 0;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.quick-item:hover {
  background: #f0f7ff;
  color: #409EFF;
  transform: translateY(-2px);
}

.quick-item i {
  font-size: 28px;
}

.quick-item span {
  font-size: 13px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 12px;
  }

  .stat-card ::v-deep .el-card__body {
    padding: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-icon i {
    font-size: 24px;
  }

  .stat-number {
    font-size: 24px;
  }

  .quick-links {
    gap: 10px;
  }

  .quick-item {
    padding: 12px 16px;
  }
}
</style>