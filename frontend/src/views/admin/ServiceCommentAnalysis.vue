<template>
  <div class="service-comment-analysis">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()">返回</el-button>
      <h2>
        <i class="el-icon-star-on" style="color: #e6a23c; margin-right: 8px;"></i>
        {{ serviceName }} - 评价分析
      </h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #409EFF, #66b1ff);">
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
          <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #f0b45c);">
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
          <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61);">
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
          <div class="stat-icon" style="background: linear-gradient(135deg, #909399, #b4b6bc);">
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
            <span><i class="el-icon-pie-chart" style="color: #409EFF; margin-right: 6px;"></i>评分分布</span>
          </div>
          <div id="ratingChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-data-line" style="color: #67c23a; margin-right: 6px;"></i>评价趋势（近7天）</span>
          </div>
          <div id="trendChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评价列表 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="comment-list-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-chat-line-round" style="color: #e6a23c; margin-right: 6px;"></i>用户评价</span>
            <div class="filter-area">
              <el-select
                  v-model="ratingFilter"
                  placeholder="筛选评分"
                  size="small"
                  clearable
                  @change="handleRatingChange"
              >
                <el-option label="全部" :value="null"></el-option>
                <el-option label="5星" :value="5"></el-option>
                <el-option label="4星" :value="4"></el-option>
                <el-option label="3星" :value="3"></el-option>
                <el-option label="2星" :value="2"></el-option>
                <el-option label="1星" :value="1"></el-option>
              </el-select>
            </div>
          </div>
          <div class="comment-list" v-loading="commentLoading">
            <div class="comment-item" v-for="comment in commentList" :key="comment.id">
              <el-avatar :size="40" :src="comment.userAvatar" class="comment-avatar">
                {{ comment.userName ? comment.userName.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <div class="comment-content">
                <div class="comment-user">
                  <span class="user-name">{{ comment.userName }}</span>
                  <el-rate v-model="comment.rating" disabled show-score text-color="#ff9900"></el-rate>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div v-if="comment.images && comment.images.length" class="comment-images">
                  <el-image
                      v-for="(img, idx) in comment.images.split(',')"
                      :key="idx"
                      :src="img"
                      :preview-src-list="comment.images.split(',')"
                      fit="cover"
                      class="comment-img"
                  ></el-image>
                </div>
                <div v-if="comment.reply" class="comment-reply">
                  <span class="reply-label">商家回复：</span>
                  <span>{{ comment.reply }}</span>
                </div>
                <div v-if="!comment.reply" class="comment-reply-input">
                  <el-input
                      v-model="replyContent[comment.id]"
                      placeholder="回复用户..."
                      size="small"
                      style="width: 300px"
                  ></el-input>
                  <el-button size="small" type="primary" @click="handleReply(comment)">回复</el-button>
                </div>
              </div>
            </div>
            <div v-if="commentList.length === 0" class="empty-comment">
              <i class="el-icon-chat-dot-round"></i>
              <p>暂无评价数据</p>
            </div>
          </div>
          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
                @current-change="handlePageChange"
                :current-page="page"
                :page-size="pageSize"
                layout="prev, pager, next"
                :total="total"
                small
            ></el-pagination>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getServiceCommentStatisticsById, getServiceCommentsById, replyServiceComment } from '@/api/service';

export default {
  name: 'ServiceCommentAnalysis',
  data() {
    return {
      serviceId: null,
      serviceName: '',
      loading: false,
      commentLoading: false,
      stats: {
        totalComments: 0,
        avgRating: 0,
        positiveRate: 0,
        replyRate: 0,
        ratingDistribution: [],
        commentTrend: []
      },
      commentList: [],
      page: 1,
      pageSize: 10,
      total: 0,
      ratingFilter: null,
      replyContent: {}
    };
  },
  mounted() {
    this.serviceId = this.$route.query.serviceId;
    this.serviceName = this.$route.query.serviceName || '服务';

    if (this.serviceId) {
      this.loadStatistics();
      this.loadComments();
    } else {
      this.$message.error('服务ID不存在');
      this.$router.back();
    }
  },
  methods: {
    async loadStatistics() {
      this.loading = true;
      try {
        const res = await getServiceCommentStatisticsById(this.serviceId);
        if (res.code === 200) {
          this.stats = res.data;

          // 处理 commentTrend 数据，只保留月/日
          if (this.stats.commentTrend && this.stats.commentTrend.length > 0) {
            this.stats.commentTrend = this.stats.commentTrend.map(item => {
              let date = item.date;
              if (date) {
                // 如果是完整日期格式 yyyy-MM-dd，只取 MM/dd
                const parts = date.split('-');
                if (parts.length === 3) {
                  date = `${parseInt(parts[1])}/${parseInt(parts[2])}`;
                }
              }
              return {
                date: date,
                count: item.count
              };
            });
          }

          this.initCharts();
        }
      } catch (error) {
        console.error('加载统计数据失败', error);
      } finally {
        this.loading = false;
      }
    },

    async loadComments() {
      this.commentLoading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize
        };
        if (this.ratingFilter) {
          params.rating = this.ratingFilter;
        }
        const res = await getServiceCommentsById(this.serviceId, params);
        if (res.code === 200) {
          this.commentList = res.data.list;
          this.total = res.data.total;
        }
      } catch (error) {
        console.error('加载评价列表失败', error);
      } finally {
        this.commentLoading = false;
      }
    },

    initCharts() {
      // 评分分布饼图
      const ratingChart = echarts.init(document.getElementById('ratingChart'));
      const ratingData = (this.stats.ratingDistribution || []).map(item => ({
        name: item.rating + '星',
        value: item.count
      }));

      ratingChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}条)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: '55%',
          center: ['50%', '50%'],
          data: ratingData,
          label: { show: true, formatter: '{b}: {d}%' },
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 }
        }],
        color: ['#f56c6c', '#e6a23c', '#f0b45c', '#67c23a', '#409EFF']
      });

      // 趋势折线图
      const trendData = (this.stats.commentTrend || []).map(item => ({
        date: item.date,
        count: item.count
      }));

      const trendChart = echarts.init(document.getElementById('trendChart'));
      trendChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: trendData.map(item => item.date) },
        yAxis: { type: 'value', name: '评价数量' },
        series: [{
          data: trendData.map(item => item.count),
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.3, color: '#409EFF' },
          lineStyle: { color: '#409EFF', width: 2 },
          symbol: 'circle',
          symbolSize: 8
        }]
      });

      window.addEventListener('resize', () => {
        ratingChart.resize();
        trendChart.resize();
      });
    },

    handleRatingChange() {
      this.page = 1;
      this.loadComments();
    },

    handlePageChange(page) {
      this.page = page;
      this.loadComments();
    },

    async handleReply(comment) {
      const content = this.replyContent[comment.id];
      if (!content || !content.trim()) {
        this.$message.warning('请输入回复内容');
        return;
      }
      try {
        const res = await replyServiceComment(comment.id, content);
        if (res.code === 200) {
          this.$message.success('回复成功');
          this.replyContent[comment.id] = '';
          this.loadComments();
          this.loadStatistics();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('回复失败');
      }
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.service-comment-analysis {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.back-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  background: #fff;
  padding: 12px 20px;
  border-radius: 12px;
}

.back-bar h2 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
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

.chart-card, .comment-list-card {
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

.filter-area {
  display: flex;
  gap: 10px;
}

.comment-list {
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

.comment-images {
  display: flex;
  gap: 8px;
  margin: 10px 0;
}

.comment-img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  cursor: pointer;
}

.comment-reply {
  background: #f8f9fc;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: #67c23a;
}

.comment-reply-input {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 10px;
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

.pagination-wrapper {
  margin-top: 15px;
  text-align: right;
}
</style>