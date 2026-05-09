<template>
  <div class="dashboard">
    <!-- 欢迎区 -->
    <div class="welcome-section">
      <div class="welcome-info">
        <h2 class="welcome-title">{{ greeting }}</h2>
        <p class="welcome-date">{{ currentDateTime }}</p>
      </div>
      <div class="welcome-stats">
        <div class="welcome-stat-item">
          <span class="ws-value">{{ statistics.pendingAppointments || 0 }}</span>
          <span class="ws-label">待处理</span>
        </div>
        <div class="welcome-stat-divider"></div>
        <div class="welcome-stat-item">
          <span class="ws-value">{{ statistics.todayIncome ? '¥' + statistics.todayIncome : '¥0' }}</span>
          <span class="ws-label">今日收入</span>
        </div>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="stats-row">
      <div class="stat-card card-users" @click="$router.push('/admin/user-list')">
        <div class="stat-icon">
          <i class="el-icon-user-solid"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总用户数</div>
          <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
          <div class="stat-sub">今日新增 {{ statistics.newUsersToday || 0 }}</div>
        </div>
      </div>

      <div class="stat-card card-appointments" @click="$router.push('/admin/appointment-list')">
        <div class="stat-icon">
          <i class="el-icon-s-order"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">预约总数</div>
          <div class="stat-value">{{ statistics.totalAppointments || 0 }}</div>
          <div class="stat-sub">今日 {{ statistics.todayAppointments || 0 }} 预约</div>
        </div>
      </div>

      <div class="stat-card card-orders" @click="$router.push('/admin/order-list')">
        <div class="stat-icon">
          <i class="el-icon-shopping-cart-2"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">订单总数</div>
          <div class="stat-value">{{ statistics.totalOrders || 0 }}</div>
          <div class="stat-sub">今日 {{ statistics.todayOrders || 0 }} 订单</div>
        </div>
      </div>

      <div class="stat-card card-pets" @click="$router.push('/admin/pet-list')">
        <div class="stat-icon">
          <i class="el-icon-s-custom"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">宠物总数</div>
          <div class="stat-value">{{ statistics.totalPets || 0 }}</div>
          <div class="stat-sub">平台宠物总量</div>
        </div>
      </div>

      <div class="stat-card card-posts" @click="$router.push('/admin/post-manage')">
        <div class="stat-icon">
          <i class="el-icon-chat-dot-square"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">帖子总数</div>
          <div class="stat-value">{{ statistics.totalPosts || 0 }}</div>
          <div class="stat-sub">今日新增 {{ statistics.todayPosts || 0 }}</div>
        </div>
      </div>

      <div class="stat-card card-income">
        <div class="stat-icon">
          <i class="el-icon-money"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">累计收入</div>
          <div class="stat-value">¥{{ statistics.totalIncome || 0 }}</div>
          <div class="stat-sub">今日 ¥{{ statistics.todayIncome || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <div class="chart-panel">
          <div class="panel-header">
            <div class="panel-title">
              <i class="el-icon-s-data"></i>
              <span>近7天预约与收入趋势</span>
            </div>
            <el-radio-group v-model="chartType" size="mini" @change="updateChart">
              <el-radio-button label="appointment">预约</el-radio-button>
              <el-radio-button label="income">收入</el-radio-button>
            </el-radio-group>
          </div>
          <div id="trendChart" class="chart-body"></div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="chart-panel">
          <div class="panel-header">
            <div class="panel-title">
              <i class="el-icon-star-on"></i>
              <span>热门服务 TOP 5</span>
            </div>
          </div>
          <div class="hot-service-list">
            <div
                class="hot-service-item"
                v-for="(item, index) in hotServices"
                :key="item.id || index"
            >
              <div class="hs-rank" :class="'hs-rank-' + (index + 1)">
                <span v-if="index === 0">👑</span>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <div class="hs-info">
                <div class="hs-name">{{ item.name }}</div>
                <div class="hs-bar-wrap">
                  <div
                      class="hs-bar"
                      :class="'hs-bar-' + (index + 1)"
                      :style="{ width: (item.count / maxServiceCount) * 100 + '%' }"
                  ></div>
                </div>
              </div>
              <div class="hs-count">{{ item.count }}单</div>
            </div>
            <div v-if="hotServices.length === 0" class="empty-data">暂无数据</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷入口 + 最近预约 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="12">
        <div class="chart-panel">
          <div class="panel-header">
            <div class="panel-title">
              <i class="el-icon-thumb"></i>
              <span>快捷入口</span>
            </div>
          </div>
          <div class="quick-grid">
            <div class="quick-item" v-for="link in quickLinks" :key="link.path" @click="$router.push(link.path)">
              <div class="quick-icon" :style="{ background: link.gradient }">
                <i :class="link.icon"></i>
              </div>
              <span>{{ link.label }}</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="chart-panel">
          <div class="panel-header">
            <div class="panel-title">
              <i class="el-icon-s-claim"></i>
              <span>待办概览</span>
            </div>
          </div>
          <div class="todo-grid">
            <div class="todo-item" @click="$router.push('/admin/appointment-list?status=0')">
              <div class="todo-icon todo-pending">
                <i class="el-icon-s-order"></i>
              </div>
              <div class="todo-info">
                <div class="todo-label">待确认预约</div>
                <div class="todo-value">{{ statistics.pendingAppointments || 0 }}</div>
              </div>
            </div>
            <div class="todo-item" @click="$router.push('/admin/user-list')">
              <div class="todo-icon todo-users">
                <i class="el-icon-user-solid"></i>
              </div>
              <div class="todo-info">
                <div class="todo-label">今日新增用户</div>
                <div class="todo-value">{{ statistics.newUsersToday || 0 }}</div>
              </div>
            </div>
            <div class="todo-item" @click="$router.push('/admin/appointment-list')">
              <div class="todo-icon todo-today">
                <i class="el-icon-date"></i>
              </div>
              <div class="todo-info">
                <div class="todo-label">今日预约</div>
                <div class="todo-value">{{ statistics.todayAppointments || 0 }}</div>
              </div>
            </div>
            <div class="todo-item" @click="$router.push('/admin/order-list')">
              <div class="todo-icon todo-orders">
                <i class="el-icon-shopping-cart-2"></i>
              </div>
              <div class="todo-info">
                <div class="todo-label">今日订单</div>
                <div class="todo-value">{{ statistics.todayOrders || 0 }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getDashboardStatistics } from '@/api/admin';

export default {
  name: 'Admin_Dashboard',
  data() {
    return {
      statistics: {
        totalUsers: 0,
        newUsersToday: 0,
        todayAppointments: 0,
        totalAppointments: 0,
        todayIncome: 0,
        totalIncome: 0,
        pendingAppointments: 0,
        totalPets: 0,
        todayPosts: 0,
        totalPosts: 0,
        todayOrders: 0,
        totalOrders: 0
      },
      hotServices: [],
      weeklyTrend: [],
      incomeTrend: [],
      chartType: 'appointment',
      currentDateTime: '',
      timer: null,
      quickLinks: [
        { label: '用户管理', path: '/admin/user-list', icon: 'el-icon-user-solid', gradient: 'linear-gradient(135deg, #e8a87c, #f0826a)' },
        { label: '预约管理', path: '/admin/appointment-list', icon: 'el-icon-s-order', gradient: 'linear-gradient(135deg, #f59e4b, #e8a87c)' },
        { label: '服务管理', path: '/admin/service-list', icon: 'el-icon-s-cooperation', gradient: 'linear-gradient(135deg, #67c23a, #95d475)' },
        { label: '商品列表', path: '/admin/product-list', icon: 'el-icon-goods', gradient: 'linear-gradient(135deg, #ff6b6b, #ff8e8e)' },
        { label: '订单管理', path: '/admin/order-list', icon: 'el-icon-s-order', gradient: 'linear-gradient(135deg, #e6a23c, #f0b45c)' },
        { label: '商品分析', path: '/admin/product-analysis', icon: 'el-icon-s-data', gradient: 'linear-gradient(135deg, #909399, #b0b3b9)' }
      ]
    };
  },
  computed: {
    greeting() {
      const hour = new Date().getHours();
      if (hour < 9) return '早上好，管理员 👋';
      if (hour < 12) return '上午好，管理员 ☀️';
      if (hour < 14) return '中午好，管理员 🌤';
      if (hour < 18) return '下午好，管理员 🌈';
      return '晚上好，管理员 🌙';
    },
    maxServiceCount() {
      return Math.max(...this.hotServices.map(s => s.count || 1), 1);
    }
  },
  mounted() {
    this.updateDateTime();
    this.timer = setInterval(() => {
      this.updateDateTime();
    }, 30000);
    this.loadData();
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    updateDateTime() {
      const now = new Date();
      const weekdays = ['日', '一', '二', '三', '四', '五', '六'];
      const y = now.getFullYear();
      const m = now.getMonth() + 1;
      const d = now.getDate();
      const w = weekdays[now.getDay()];
      const hh = String(now.getHours()).padStart(2, '0');
      const mm = String(now.getMinutes()).padStart(2, '0');
      this.currentDateTime = `${y}年${m}月${d}日 星期${w} ${hh}:${mm}`;
    },

    async loadData() {
      try {
        const res = await getDashboardStatistics();
        if (res.code === 200) {
          const data = res.data;
          this.statistics = { ...this.statistics, ...(data.statistics || {}) };
          this.hotServices = data.hotServices || [];
          this.weeklyTrend = data.weeklyTrend || [];
          this.incomeTrend = data.incomeTrend || [];
        }
      } catch (error) {
        this.$message.error(error?.message || '加载仪表盘数据失败');
        // 后端不可用时使用模拟数据展示骨架
        this.hotServices = [
          { name: '宠物洗澡美容', count: 48 },
          { name: '宠物健康检查', count: 36 },
          { name: '宠物寄养服务', count: 28 },
          { name: '宠物训练课程', count: 19 },
          { name: '宠物摄影写真', count: 12 }
        ];
      } finally {
        this.$nextTick(() => {
          this.initChart();
        });
      }
    },

    initChart() {
      const el = document.getElementById('trendChart');
      if (!el) return;
      const chart = echarts.init(el);

      // 生成近7天的日期数组
      const last7Days = [];
      for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        last7Days.push(`${date.getMonth() + 1}/${date.getDate()}`);
      }

      const dataMap = new Map();
      const source = this.chartType === 'income' ? this.incomeTrend : this.weeklyTrend;
      if (source && source.length > 0) {
        source.forEach(item => {
          const dateKey = this.formatDateKey(item.date);
          dataMap.set(dateKey, item.count || item.amount || 0);
        });
      } else {
        const fallback = this.chartType === 'income'
          ? [320, 480, 560, 420, 680, 520, 720]
          : [5, 8, 12, 7, 15, 20, 10];
        last7Days.forEach((date, i) => dataMap.set(date, fallback[i]));
      }

      const chartData = last7Days.map(date => dataMap.get(date) || 0);
      const isIncome = this.chartType === 'income';

      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: '#fff',
          borderColor: '#f5ece6',
          textStyle: { color: '#3d2e2a' },
          formatter: isIncome
            ? params => `${params[0].axisValue}<br/>收入: <b>¥${params[0].value}</b>`
            : params => `${params[0].axisValue}<br/>预约: <b>${params[0].value}单</b>`
        },
        grid: { left: '3%', right: '5%', bottom: '5%', top: '8%', containLabel: true },
        xAxis: {
          type: 'category',
          data: last7Days,
          axisLine: { lineStyle: { color: '#e0e0e0' } },
          axisTick: { show: false },
          axisLabel: { color: '#a08c84', fontSize: 12 }
        },
        yAxis: {
          type: 'value',
          name: isIncome ? '收入 (¥)' : '预约 (单)',
          nameTextStyle: { color: '#a08c84', fontSize: 12 },
          splitLine: { lineStyle: { color: '#f5ece6', type: 'dashed' } },
          axisLabel: { color: '#a08c84', fontSize: 12 }
        },
        series: [{
          data: chartData,
          type: 'bar',
          barWidth: '45%',
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#f59e4b' },
                { offset: 1, color: '#f0826a' }
              ]
            }
          },
          label: {
            show: true,
            position: 'top',
            color: '#7a6a62',
            fontSize: 12,
            fontWeight: 500
          }
        }]
      });

      const handleResize = () => chart.resize();
      window.addEventListener('resize', handleResize);
      this._chartResizeHandler = handleResize;
      this._chartInstance = chart;
    },

    updateChart() {
      if (this._chartInstance) {
        this._chartInstance.dispose();
      }
      this.initChart();
    },

    formatDateKey(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getMonth() + 1}/${date.getDate()}`;
    },

    formatTime(date) {
      if (!date) return '';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
/* ========== 整体 ========== */
.dashboard {
  padding: 0;
  background: transparent;
}

/* ========== 欢迎区 ========== */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px 28px;
  background: linear-gradient(135deg, #ffffff, #fefbf9);
  border-radius: 16px;
  border: 1px solid #f5ece6;
  box-shadow: 0 2px 8px rgba(180, 120, 90, 0.04);
}

.welcome-title {
  font-size: 22px;
  font-weight: 700;
  color: #3d2e2a;
  margin: 0 0 4px;
}

.welcome-date {
  font-size: 13px;
  color: #a08c84;
  margin: 0;
}

.welcome-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}

.welcome-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.ws-value {
  font-size: 20px;
  font-weight: 700;
  color: #f0826a;
}

.ws-label {
  font-size: 12px;
  color: #a08c84;
}

.welcome-stat-divider {
  width: 1px;
  height: 36px;
  background: #f5ece6;
}

/* ========== 核心指标卡片网格 ========== */
/* ========== 统计卡片（与用户分析一致）========== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 24px;
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
  cursor: pointer;
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

.card-users .stat-icon    { background: linear-gradient(135deg, #e8a87c, #f0826a); }
.card-appointments .stat-icon { background: linear-gradient(135deg, #f59e4b, #e8a87c); }
.card-orders .stat-icon   { background: linear-gradient(135deg, #67c23a, #95d475); }
.card-income .stat-icon   { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-pets .stat-icon     { background: linear-gradient(135deg, #909399, #b0b3b9); }
.card-posts .stat-icon    { background: linear-gradient(135deg, #a18cd1, #d4a5d8); }

.stat-info { flex: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 5px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3d2e2a; letter-spacing: -0.5px; }
.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ========== 图表面板 ========== */
.chart-row {
  margin-bottom: 20px;
}

.chart-panel {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #f5ece6;
  box-shadow: 0 2px 6px rgba(180, 120, 90, 0.03);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f5ece6;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: #3d2e2a;
}

.panel-title i {
  color: #f0826a;
  font-size: 18px;
}

.chart-body {
  height: 340px;
  padding: 8px 0;
}

/* ========== 热门服务 ========== */
.hot-service-list {
  padding: 8px 20px 16px;
}

.hot-service-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
}

.hot-service-item + .hot-service-item {
  border-top: 1px solid #fefbf9;
}

.hs-rank {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
  background: #f4f4f5;
  color: #909399;
}

.hs-rank-1 { background: linear-gradient(135deg, #ffd700, #ffb800); color: #fff; }
.hs-rank-2 { background: linear-gradient(135deg, #c0c0c0, #a8a8a8); color: #fff; }
.hs-rank-3 { background: linear-gradient(135deg, #cd7f32, #b87333); color: #fff; }

.hs-info {
  flex: 1;
  min-width: 0;
}

.hs-name {
  font-size: 13px;
  font-weight: 500;
  color: #3d2e2a;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hs-bar-wrap {
  height: 6px;
  background: #f5f5f5;
  border-radius: 3px;
  overflow: hidden;
}

.hs-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}

.hs-bar-1 { background: linear-gradient(90deg, #ffd700, #ffb800); }
.hs-bar-2 { background: linear-gradient(90deg, #c0c0c0, #a8a8a8); }
.hs-bar-3 { background: linear-gradient(90deg, #cd7f32, #b87333); }
.hs-bar-4 { background: linear-gradient(90deg, #f59e4b, #f0826a); }
.hs-bar-5 { background: linear-gradient(90deg, #909399, #b0b3b9); }

.hs-count {
  font-weight: 600;
  font-size: 14px;
  color: #f0826a;
  flex-shrink: 0;
  min-width: 36px;
  text-align: right;
}

/* ========== 底部区 ========== */
.bottom-row {
  margin-bottom: 0;
}

/* --- 快捷入口 --- */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 16px 20px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s;
  border: 1px solid transparent;
}

.quick-item:hover {
  background: #fefbf9;
  border-color: #fde2d0;
  transform: translateY(-2px);
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #ffffff;
  box-shadow: 0 3px 10px rgba(0,0,0,0.08);
  transition: transform 0.25s;
}

.quick-item:hover .quick-icon {
  transform: scale(1.08);
}

.quick-item span {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

/* --- 待办概览 --- */
.todo-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px 20px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 12px;
  background: #fefbf9;
  cursor: pointer;
  transition: all 0.25s;
  border: 1px solid transparent;
}

.todo-item:hover {
  background: #fff;
  border-color: #fde2d0;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(180, 120, 90, 0.08);
}

.todo-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
}

.todo-pending { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.todo-users   { background: linear-gradient(135deg, #e8a87c, #d4956b); }
.todo-today   { background: linear-gradient(135deg, #67c23a, #95d475); }
.todo-orders  { background: linear-gradient(135deg, #a18cd1, #c4a8d8); }

.todo-info {
  flex: 1;
  min-width: 0;
}

.todo-label {
  font-size: 12px;
  color: #a08c84;
  margin-bottom: 4px;
}

.todo-value {
  font-size: 24px;
  font-weight: 700;
  color: #3d2e2a;
  letter-spacing: -0.5px;
}

/* --- 空数据 --- */
.empty-data {
  text-align: center;
  padding: 40px 20px;
  color: #a08c84;
  font-size: 14px;
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 22px;
  }

  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .quick-item {
    padding: 14px 8px;
  }

  .quick-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
}
</style>
