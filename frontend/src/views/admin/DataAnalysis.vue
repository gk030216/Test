<template>
  <div class="data-analysis">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalComments }}</div>
            <div class="stat-label">总评价数</div>
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
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-thumb"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.positiveRate }}%</div>
            <div class="stat-label">好评率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-message"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.replyRate }}%</div>
            <div class="stat-label">回复率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>评分分布</span>
            <el-tag size="small" type="info">近30天</el-tag>
          </div>
          <div id="ratingChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>评价趋势（近7天）</span>
            <el-tag size="small" type="info">每日新增</el-tag>
          </div>
          <div id="trendChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>热门评价词云</span>
            <el-tag size="small" type="info">基于用户评价内容</el-tag>
          </div>
          <div id="wordcloudChart" style="height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评价列表 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="recent-comment-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span>最新评价</span>
            <el-button type="text" @click="$router.push('/admin/comment-list')">查看更多</el-button>
          </div>
          <div class="recent-comments">
            <div class="comment-item" v-for="comment in recentComments" :key="comment.id">
              <el-avatar :size="40" class="comment-avatar">{{ comment.userName ? comment.userName.charAt(0) : 'U' }}</el-avatar>
              <div class="comment-content">
                <div class="comment-user">
                  <span class="user-name">{{ comment.userName }}</span>
                  <el-rate v-model="comment.rating" disabled show-score text-color="#ff9900"></el-rate>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div v-if="comment.reply" class="comment-reply">
                  <span class="reply-label">商家回复：</span>
                  <span>{{ comment.reply }}</span>
                </div>
              </div>
            </div>
            <div v-if="recentComments.length === 0" class="empty-comment">
              <i class="el-icon-chat-dot-round"></i>
              <p>暂无评价数据</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getCommentStatistics, getRecentComments } from '@/api/comment';

export default {
  name: 'DataAnalysis',
  data() {
    return {
      loading: false,
      stats: {
        totalComments: 0,
        avgRating: 0,
        positiveRate: 0,
        replyRate: 0
      },
      ratingDistribution: [],
      commentTrend: [],
      recentComments: []
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const statsRes = await getCommentStatistics();
        if (statsRes.code === 200) {
          this.stats = statsRes.data;
          this.ratingDistribution = statsRes.data.ratingDistribution || [];
          this.commentTrend = statsRes.data.commentTrend || [];
          this.initCharts();
        }

        const recentRes = await getRecentComments(5);
        if (recentRes.code === 200) {
          this.recentComments = recentRes.data;
        }
      } catch (error) {
        console.error('加载数据失败', error);
        this.$message.error('加载数据失败');
      } finally {
        this.loading = false;
      }
    },

    initCharts() {
      // 评分分布饼图
      const ratingChart = echarts.init(document.getElementById('ratingChart'));
      const ratingData = this.ratingDistribution.map(item => ({
        name: item.rating + '星',
        value: item.count
      }));

      ratingChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}条)' },
        legend: { orient: 'vertical', left: 'left', data: ratingData.map(d => d.name) },
        series: [{
          type: 'pie',
          radius: '55%',
          center: ['50%', '50%'],
          data: ratingData,
          label: { show: true, formatter: '{b}: {d}%' },
          emphasis: { scale: true },
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          }
        }],
        color: ['#f56c6c', '#e6a23c', '#f0b45c', '#67c23a', '#409EFF']
      });

      // 趋势折线图
      const trendChart = echarts.init(document.getElementById('trendChart'));
      trendChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: this.commentTrend.map(item => item.date), axisLine: { lineStyle: { color: '#999' } } },
        yAxis: { type: 'value', name: '评价数量', nameStyle: { color: '#999' } },
        series: [{
          data: this.commentTrend.map(item => item.count),
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.3, color: '#409EFF' },
          lineStyle: { color: '#409EFF', width: 2 },
          symbol: 'circle',
          symbolSize: 8,
          itemStyle: { color: '#409EFF', borderColor: '#fff', borderWidth: 2 }
        }]
      });

      // 词云图
      const wordcloudChart = echarts.init(document.getElementById('wordcloudChart'));
      wordcloudChart.setOption({
        series: [{
          type: 'wordCloud',
          shape: 'circle',
          left: 'center',
          top: 'center',
          width: '90%',
          height: '90%',
          sizeRange: [12, 50],
          rotationRange: [-45, 90],
          rotationStep: 45,
          gridSize: 8,
          drawOutOfBound: false,
          textStyle: {
            fontFamily: 'sans-serif',
            fontWeight: 'normal',
            color: function () {
              const colors = ['#409EFF', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#667eea', '#764ba2'];
              return colors[Math.floor(Math.random() * colors.length)];
            }
          },
          data: this.getWordCloudData()
        }]
      });

      window.addEventListener('resize', () => {
        ratingChart.resize();
        trendChart.resize();
        wordcloudChart.resize();
      });
    },

    getWordCloudData() {
      return [
        { name: '质量很好', value: 128 },
        { name: '发货快', value: 98 },
        { name: '包装好', value: 85 },
        { name: '价格实惠', value: 76 },
        { name: '客服态度好', value: 65 },
        { name: '还会回购', value: 58 },
        { name: '物流慢', value: 42 },
        { name: '尺寸偏小', value: 38 },
        { name: '味道不错', value: 35 },
        { name: '适口性好', value: 32 },
        { name: '性价比高', value: 30 },
        { name: '猫咪爱吃', value: 28 },
        { name: '狗狗喜欢', value: 26 }
      ];
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

.chart-card, .recent-comment-card {
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

.recent-comments {
  max-height: 500px;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-reply {
  background: #f8f9fc;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: #67c23a;
}

.reply-label {
  font-weight: 500;
  color: #67c23a;
}

.empty-comment {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-comment i {
  font-size: 48px;
  margin-bottom: 16px;
}
</style>