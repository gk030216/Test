<template>
  <div class="product-comment-analysis-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" class="back-btn">返回</el-button>
      <span class="back-title">{{ productName }} - 评价分析</span>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row" v-loading="loading">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总评价数</div>
          <div class="stat-value">{{ stats.totalComments }}</div>
          <div class="stat-sub">商品评价总数</div>
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
      <div class="stat-card card-positive">
        <div class="stat-icon">
          <i class="el-icon-thumb"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">好评率</div>
          <div class="stat-value">{{ stats.positiveRate }}%</div>
          <div class="stat-sub">满意评价占比</div>
        </div>
      </div>
      <div class="stat-card card-reply">
        <div class="stat-icon">
          <i class="el-icon-message"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">回复率</div>
          <div class="stat-value">{{ stats.replyRate }}%</div>
          <div class="stat-sub">商家回复占比</div>
        </div>
      </div>
    </div>

    <!-- 图表区域：趋势图（全宽） -->
    <div class="charts-row charts-row-single">
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-data-line"></i> 评价趋势（近7天）</span>
        </div>
        <div ref="trendChart" class="chart-box chart-box-wide"></div>
      </div>
    </div>

    <!-- 图表区域：评分分布 + 词云 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-pie-chart"></i> 评分分布</span>
        </div>
        <div ref="ratingChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span><i class="el-icon-cloudy"></i> 评论热词</span>
          <el-tag size="small" type="info">高频关键词</el-tag>
        </div>
        <div ref="wordCloudChart" class="chart-box"></div>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="chart-card comment-list-card">
      <div class="chart-header">
        <span><i class="el-icon-chat-dot-round"></i> 用户评价</span>
        <el-select
          v-model="ratingFilter"
          placeholder="筛选评分"
          size="small"
          clearable
          @change="handleRatingChange"
        >
          <el-option label="全部" :value="null" />
          <el-option label="5星" :value="5" />
          <el-option label="4星" :value="4" />
          <el-option label="3星" :value="3" />
          <el-option label="2星" :value="2" />
          <el-option label="1星" :value="1" />
        </el-select>
      </div>
      <div class="comment-list" v-loading="commentLoading">
        <div class="comment-item" v-for="comment in commentList" :key="comment.id">
          <el-avatar :size="40" :src="comment.userAvatar" class="comment-avatar">
            {{ (comment.userNickname || comment.userName || 'U').charAt(0).toUpperCase() }}
          </el-avatar>
          <div class="comment-body">
            <div class="comment-meta">
              <span class="user-name">{{ comment.userNickname || comment.userName || '匿名用户' }}</span>
              <el-rate v-model="comment.rating" disabled show-score text-color="#f59e4b" />
              <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
            </div>
            <div class="comment-text">{{ comment.content }}</div>
            <div v-if="comment.images && comment.images.length" class="comment-images">
              <el-image
                v-for="(img, idx) in getImageList(comment.images)"
                :key="idx"
                :src="img"
                :preview-src-list="getImageList(comment.images)"
                fit="cover"
                class="comment-img"
              />
            </div>
            <div v-if="comment.reply" class="comment-reply">
              <span class="reply-label">商家回复：</span>
              <span>{{ comment.reply }}</span>
            </div>
            <div v-if="!comment.reply" class="comment-reply-inline">
              <el-input
                v-model="replyContent[comment.id]"
                placeholder="输入回复内容..."
                size="small"
                class="reply-input"
              />
              <el-button size="small" @click="handleReply(comment)" class="warm-btn-small">回复</el-button>
            </div>
          </div>
        </div>
        <div v-if="commentList.length === 0 && !commentLoading" class="empty-comment">
          <i class="el-icon-chat-dot-round"></i>
          <p>暂无评价数据</p>
        </div>
      </div>
      <div class="comment-pagination" v-if="total > 0">
        <el-pagination
          @current-change="handlePageChange"
          :current-page="page"
          :page-size="pageSize"
          layout="prev, pager, next"
          :total="total"
          small
          background
        />
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import 'echarts-wordcloud';
import { getProductCommentStatistics, getProductCommentsList, replyComment } from '@/api/comment';

export default {
  name: 'Admin_ProductCommentAnalysis',
  data() {
    return {
      productId: null,
      productName: '',
      loading: false,
      commentLoading: false,
      stats: {
        totalComments: 0,
        avgRating: 0,
        positiveRate: 0,
        replyRate: 0,
        ratingDistribution: []
      },
      commentTrend: [],
      commentList: [],
      page: 1,
      pageSize: 10,
      total: 0,
      ratingFilter: null,
      replyContent: {},
      wordCloudData: [],
      charts: []
    };
  },
  mounted() {
    this.productId = this.$route.query.productId;
    this.productName = this.$route.query.productName || '商品';
    if (this.productId) {
      this.loadStatistics();
      this.loadComments();
    } else {
      this.$message.error('商品ID不存在');
      this.$router.back();
    }
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    this.charts.forEach(chart => chart.dispose());
    this.charts = [];
  },
  methods: {
    async loadStatistics() {
      this.loading = true;
      try {
        const res = await getProductCommentStatistics(this.productId);
        if (res.code === 200) {
          this.stats = res.data;
          if (res.data.commentTrend && res.data.commentTrend.length > 0) {
            this.commentTrend = res.data.commentTrend.map(item => {
              let date = item.date;
              if (typeof date === 'number') {
                const d = new Date(date);
                date = `${d.getMonth() + 1}/${d.getDate()}`;
              } else if (date) {
                const parts = date.split('-');
                if (parts.length === 3) {
                  date = `${parseInt(parts[1])}/${parseInt(parts[2])}`;
                }
              }
              return { date, count: item.count };
            });
          } else {
            this.commentTrend = this.generateEmptyTrend();
          }
          this.$nextTick(() => this.initCharts());
        }
      } catch (error) {
        this.$message.error(error?.message || '加载统计数据失败');
      } finally {
        this.loading = false;
      }
    },

    generateEmptyTrend() {
      const trend = [];
      for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        trend.push({
          date: `${date.getMonth() + 1}/${date.getDate()}`,
          count: 0
        });
      }
      return trend;
    },

    async loadComments() {
      this.commentLoading = true;
      try {
        const params = { page: this.page, pageSize: this.pageSize };
        if (this.ratingFilter) params.rating = this.ratingFilter;
        const res = await getProductCommentsList(this.productId, params);
        if (res.code === 200) {
          this.commentList = (res.data.list || []).map(comment => ({
            ...comment,
            userNickname: comment.userNickname || null
          }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载评价列表失败');
      } finally {
        this.commentLoading = false;
      }
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
      this.initRatingChart();
      this.initTrendChart();
      this.loadWordCloudData();
    },

    initRatingChart() {
      const chart = this.initChart('ratingChart');
      if (!chart) return;
      const ratingData = (this.stats.ratingDistribution || []).map(item => ({
        name: item.rating + '星',
        value: item.count
      }));
      if (ratingData.length === 0) {
        chart.setOption({
          title: { show: true, text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#a08c84', fontSize: 14 } }
        });
        return;
      }
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {d}% ({c}条)' },
        legend: { orient: 'vertical', left: 6, top: 'center', textStyle: { color: '#5c4a42' } },
        series: [{
          type: 'pie',
          radius: ['48%', '74%'],
          center: ['58%', '50%'],
          data: ratingData,
          label: { show: true, formatter: '{b}\n{d}%', color: '#5c4a42' },
          emphasis: { scale: true },
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }
        }],
        color: ['#f56c6c', '#e6a23c', '#f7c06b', '#67c23a', '#409eff']
      });
    },

    initTrendChart() {
      const chart = this.initChart('trendChart');
      if (!chart) return;
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', top: 10, bottom: 0, containLabel: true },
        xAxis: {
          type: 'category',
          data: this.commentTrend.map(item => item.date),
          axisLabel: { color: '#a08c84' },
          axisLine: { lineStyle: { color: '#f5ece6' } }
        },
        yAxis: {
          type: 'value',
          name: '评价数量',
          nameTextStyle: { color: '#a08c84' },
          minInterval: 1,
          splitLine: { lineStyle: { color: '#f5ece6', type: 'dashed' } }
        },
        series: [{
          data: this.commentTrend.map(item => item.count),
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.25, color: '#f59e4b' },
          lineStyle: { color: '#f59e4b', width: 2 },
          itemStyle: { color: '#f59e4b' },
          symbol: 'circle',
          symbolSize: 8
        }]
      });
    },

    async loadWordCloudData() {
      try {
        const res = await getProductCommentsList(this.productId, { page: 1, pageSize: 200 });
        if (res.code === 200 && res.data.list && res.data.list.length > 0) {
          const wordMap = this.extractWordFrequency(res.data.list.map(c => c.content).join(' '));
          this.wordCloudData = wordMap;
        }
      } catch { /* silent */ }
      this.$nextTick(() => this.initWordCloudChart());
    },

    extractWordFrequency(text) {
      if (!text) return [];
      const stopWords = new Set([
        '的', '了', '在', '是', '我', '有', '和', '就', '不', '人', '都', '一', '一个',
        '上', '也', '很', '到', '说', '要', '去', '你', '会', '着', '没有', '看', '好',
        '自己', '这', '他', '她', '它', '们', '那', '些', '什么', '怎么', '如何', '为什么',
        '可以', '这个', '那个', '还', '被', '把', '让', '给', '从', '对', '与', '及',
        '但', '而', '或', '因为', '所以', '如果', '虽然', '然后', '已经', '正在',
        '非常', '比较', '真的', '挺', '太', '特别', '更', '最', '比较',
        '不错', '还行', '一般', '感觉', '觉得', '应该', '可能', '已经', '还是',
        '不过', '但是', '只是', '而且', '并且', '所以', '因此'
      ]);

      const words = [];
      const segments = text.split(/[\s,，。！？、；：""''（）\(\)\[\]【】《》\.\!\?\;\:\"\'\)\]\>]+/);
      for (const segment of segments) {
        if (!segment || segment.length < 2) continue;
        for (let len = 2; len <= 4; len++) {
          for (let i = 0; i <= segment.length - len; i++) {
            const word = segment.substring(i, i + len);
            if (!stopWords.has(word) && word.length >= 2) {
              words.push(word);
            }
          }
        }
      }

      const freqMap = {};
      words.forEach(word => {
        freqMap[word] = (freqMap[word] || 0) + 1;
      });

      return Object.entries(freqMap)
        .filter(([, count]) => count >= 2)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 60)
        .map(([name, value]) => ({ name, value }));
    },

    initWordCloudChart() {
      const chart = this.initChart('wordCloudChart');
      if (!chart) return;
      if (this.wordCloudData.length === 0) {
        chart.setOption({
          title: { show: true, text: '暂无足够评论数据', left: 'center', top: 'center', textStyle: { color: '#a08c84', fontSize: 14 } }
        });
        return;
      }
      chart.setOption({
        tooltip: { show: true, formatter: '{b}: {c}次' },
        series: [{
          type: 'wordCloud',
          shape: 'circle',
          left: 'center',
          top: 'center',
          width: '90%',
          height: '90%',
          sizeRange: [14, 48],
          rotationRange: [-45, 45],
          rotationStep: 45,
          gridSize: 8,
          drawOutOfBound: false,
          textStyle: {
            fontFamily: '"Microsoft YaHei", "PingFang SC", sans-serif',
            fontWeight: 'normal',
            color() {
              const colors = ['#f59e4b', '#e6a23c', '#67c23a', '#409eff', '#f56c6c', '#909399', '#f0826a', '#85ce61', '#66b1ff', '#f7c06b'];
              return colors[Math.floor(Math.random() * colors.length)];
            }
          },
          emphasis: {
            focus: 'self',
            textStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.25)' }
          },
          data: this.wordCloudData
        }]
      });
    },

    handleRatingChange() {
      this.page = 1;
      this.loadComments();
    },

    async handleReply(comment) {
      const content = this.replyContent[comment.id];
      if (!content || !content.trim()) {
        this.$message.warning('请输入回复内容');
        return;
      }
      try {
        const res = await replyComment(comment.id, content);
        if (res.code === 200) {
          this.$message.success('回复成功');
          this.replyContent[comment.id] = '';
          this.loadComments();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '回复失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '回复失败');
      }
    },

    handlePageChange(page) {
      this.page = page;
      this.loadComments();
    },

    formatRating(val) {
      if (val === undefined || val === null) return '0.0';
      return Number(val).toFixed(1);
    },

    getImageList(images) {
      if (!images) return [];
      return typeof images === 'string' ? images.split(',') : images;
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
/* ===== 页面基底 ===== */
.product-comment-analysis-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 返回栏 ===== */
.back-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.back-btn { border-radius: 10px; }
.back-title { font-size: 17px; font-weight: 700; color: #3d2e2a; }

/* ===== 统计卡片 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.05);
  transition: transform 0.25s, box-shadow 0.25s;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(61, 46, 42, 0.10);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}
.card-total .stat-icon    { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-rating .stat-icon   { background: linear-gradient(135deg, #e6a23c, #f7c06b); }
.card-positive .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-reply .stat-icon    { background: linear-gradient(135deg, #409eff, #66b1ff); }

.stat-info { position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 6px; }
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
.comment-list-card { margin-top: 0; }

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

.chart-box { height: 320px; }
.chart-box-wide { height: 260px; }
.charts-row-single { grid-template-columns: 1fr; }

/* ===== 评价列表 ===== */
.comment-list { max-height: 500px; overflow-y: auto; }

.comment-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid #f5ece6;
}
.comment-item:last-child { border-bottom: none; }

.comment-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  color: #fff;
}

.comment-body { flex: 1; min-width: 0; }

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.user-name { font-weight: 500; color: #3d2e2a; }
.comment-time { font-size: 12px; color: #a08c84; }

.comment-text { color: #5c4a42; line-height: 1.5; margin-bottom: 8px; }

.comment-images { display: flex; gap: 8px; margin-bottom: 10px; }
.comment-img {
  width: 60px; height: 60px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #f5ece6;
}

.comment-reply {
  background: #fdf6f0;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
  color: #67c23a;
}
.reply-label { font-weight: 500; }

.comment-reply-inline {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 10px;
}
.reply-input { width: 280px; }
.reply-input ::v-deep .el-input__inner { border-radius: 8px; }

.warm-btn-small {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 8px !important;
  padding: 7px 16px !important;
  font-weight: 500;
}
.warm-btn-small:hover { opacity: 0.88; }

.empty-comment {
  text-align: center;
  padding: 40px;
  color: #a08c84;
}
.empty-comment i { font-size: 48px; margin-bottom: 16px; display: block; }

.comment-pagination {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #f5ece6;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .product-comment-analysis-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .charts-row { grid-template-columns: 1fr; }
}
</style>
