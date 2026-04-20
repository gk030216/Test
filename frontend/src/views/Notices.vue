<template>
  <div class="notices-page">
    <Navbar />

    <div class="notices-content">
      <div class="container">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="header-content">
            <div class="header-left">
              <h2 class="page-title">公告消息</h2>
              <p class="page-desc">了解最新动态和服务信息</p>
            </div>
          </div>
        </div>

        <!-- 分类导航 - 按新的5种类型分类 -->
        <div class="category-nav">
          <div
              v-for="cat in categories"
              :key="cat.value"
              :class="['category-item', { active: currentCategory === cat.value }]"
              @click="handleCategoryChange(cat.value)"
          >
            <i :class="cat.icon"></i>
            <span>{{ cat.label }}</span>
          </div>
        </div>

        <!-- 公告列表 -->
        <div class="notice-list" v-loading="loading">
          <div class="notice-item" v-for="notice in notices" :key="notice.id" @click="showDetail(notice)">
            <div class="notice-header">
              <div class="notice-info">
                <span :class="['notice-type', getNoticeTypeClass(notice.type)]">
                  <i :class="getNoticeTypeIcon(notice.type)"></i>
                  {{ getNoticeTypeText(notice.type) }}
                </span>
                <span class="notice-time">{{ formatDate(notice.createTime) }}</span>
              </div>
              <div class="notice-badges" v-if="notice.isTop === 1">
                <el-tag size="mini" type="danger" effect="dark">置顶</el-tag>
              </div>
            </div>

            <div class="notice-content">
              <h3 class="notice-title">{{ notice.title }}</h3>
              <p class="notice-text">{{ truncateText(notice.content, 150) }}</p>
            </div>

            <div class="notice-footer">
              <div class="action-item">
                <i class="el-icon-view"></i>
                <span>{{ notice.viewCount || 0 }} 次阅读</span>
              </div>
              <div class="read-more">
                阅读全文 <i class="el-icon-arrow-right"></i>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div class="empty-state" v-if="!loading && notices.length === 0">
            <i class="el-icon-document"></i>
            <p>暂无公告</p>
          </div>
        </div>
      </div>
    </div>

    <Footer />

    <!-- 公告详情弹窗 -->
    <el-dialog
        :title="currentNotice.title"
        :visible.sync="dialogVisible"
        width="700px"
        center
        class="notice-dialog"
    >
      <div class="notice-detail">
        <div class="notice-meta">
          <span :class="['notice-type', getNoticeTypeClass(currentNotice.type)]">
            <i :class="getNoticeTypeIcon(currentNotice.type)"></i>
            {{ getNoticeTypeText(currentNotice.type) }}
          </span>
          <span class="notice-date">
            <i class="el-icon-date"></i>
            {{ formatDate(currentNotice.createTime, 'full') }}
          </span>
          <span class="notice-view">
            <i class="el-icon-view"></i>
            {{ currentNotice.viewCount || 0 }} 次阅读
          </span>
        </div>
        <div class="notice-content-detail" v-html="currentNotice.content"></div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getNoticeList, getNoticeDetail } from '@/api/consultationNotice';

export default {
  name: 'Notices',
  components: {
    Navbar,
    Footer
  },
  data() {
    return {
      notices: [],
      loading: false,
      dialogVisible: false,
      currentNotice: {},
      currentCategory: 'all',
      // 分类导航 - 5种类型
      categories: [
        {value: 'all', label: '全部', icon: 'el-icon-menu'},
        {value: '1', label: '服务公告', icon: 'el-icon-service'},
        {value: '2', label: '商城公告', icon: 'el-icon-shopping-cart-2'},
        {value: '3', label: '社区公告', icon: 'el-icon-chat-dot-round'},
        {value: '4', label: '系统公告', icon: 'el-icon-info'},
        {value: '5', label: '其他公告', icon: 'el-icon-more'}
      ]
    };
  },
  computed: {
    // 判断是否登录
    isLoggedIn() {
      return !!localStorage.getItem('token');
    }
  },
  created() {
    this.loadNotices();
  },
  methods: {
    async loadNotices() {
      this.loading = true;
      try {
        const res = await getNoticeList();
        if (res.code === 200) {
          let notices = res.data || [];
          // 过滤掉状态为0（禁用）的公告
          notices = notices.filter(n => n.status === 1);
          // 根据分类筛选
          if (this.currentCategory !== 'all') {
            notices = notices.filter(n => n.type === parseInt(this.currentCategory));
          }
          // 按置顶和时间排序
          this.notices = notices.sort((a, b) => {
            if (a.isTop === 1 && b.isTop !== 1) return -1;
            if (a.isTop !== 1 && b.isTop === 1) return 1;
            return new Date(b.createTime) - new Date(a.createTime);
          });
        }
      } catch (error) {
        console.error('加载公告列表失败', error);
      } finally {
        this.loading = false;
      }
    },

    handleCategoryChange(category) {
      this.currentCategory = category;
      this.loadNotices();
    },

    async showDetail(notice) {
      try {
        const res = await getNoticeDetail(notice.id);
        if (res.code === 200) {
          this.currentNotice = res.data;
          this.dialogVisible = true;

          // 更新列表中的浏览次数
          const index = this.notices.findIndex(n => n.id === notice.id);
          if (index !== -1) {
            this.$set(this.notices[index], 'viewCount', res.data.viewCount);
          }
        }
      } catch (error) {
        console.error('获取公告详情失败', error);
        this.currentNotice = notice;
        this.dialogVisible = true;
      }
    },

    // 获取公告类型文本
    getNoticeTypeText(type) {
      const typeMap = {
        1: '服务公告',
        2: '商城公告',
        3: '社区公告',
        4: '系统公告',
        5: '其他公告'
      };
      return typeMap[type] || '公告';
    },

    // 获取公告类型样式类
    getNoticeTypeClass(type) {
      const typeMap = {
        1: 'type-service',
        2: 'type-shop',
        3: 'type-community',
        4: 'type-system',
        5: 'type-other'
      };
      return typeMap[type] || 'type-system';
    },

    // 获取公告类型图标
    getNoticeTypeIcon(type) {
      const typeMap = {
        1: 'el-icon-service',
        2: 'el-icon-shopping-cart-2',
        3: 'el-icon-chat-dot-round',
        4: 'el-icon-info',
        5: 'el-icon-more'
      };
      return typeMap[type] || 'el-icon-megaphone';
    },

    formatDate(date, format = 'simple') {
      if (!date) return '';
      const d = new Date(date);
      if (format === 'full') {
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
      }
      const now = new Date();
      const diff = now - d;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    truncateText(text, length) {
      if (!text) return '';
      // 去除HTML标签
      const plainText = text.replace(/<[^>]+>/g, '');
      if (plainText.length <= length) return plainText;
      return plainText.substring(0, length) + '...';
    },
  }
};
</script>

<style scoped>
.notices-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.notices-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  flex-shrink: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* 分类导航 */
.category-nav {
  display: flex;
  gap: 8px;
  background: white;
  padding: 12px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
  flex-wrap: wrap;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: #606266;
  font-size: 13px;
  background: #f5f7fa;
}

.category-item:hover {
  background: #ecf5ff;
  color: #409EFF;
}

.category-item.active {
  background: #409EFF;
  color: white;
}

/* 公告列表 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
}

.notice-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #d0d0d0;
}

/* 公告头部 */
.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.notice-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* 公告类型标签样式 - 5种类型 */
.notice-type {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.notice-type i {
  font-size: 12px;
}

/* 服务公告 - 绿色 */
.type-service {
  background: #f0f9eb;
  color: #67C23A;
}

/* 商城公告 - 橙色 */
.type-shop {
  background: #fdf6ec;
  color: #E6A23C;
}

/* 社区公告 - 蓝色 */
.type-community {
  background: #ecf5ff;
  color: #409EFF;
}

/* 系统公告 - 灰色 */
.type-system {
  background: #f4f4f5;
  color: #909399;
}

/* 其他公告 - 红色 */
.type-other {
  background: #fef0f0;
  color: #F56C6C;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.notice-badges {
  display: flex;
  gap: 6px;
}

/* 公告内容 */
.notice-content {
  margin-bottom: 12px;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #2c3e50;
  line-height: 1.4;
}

.notice-text {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 0;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 公告底部 */
.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef2f6;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

.read-more {
  font-size: 12px;
  color: #409EFF;
  transition: all 0.3s;
}

.read-more i {
  margin-left: 4px;
  transition: transform 0.3s;
}

.notice-item:hover .read-more i {
  transform: translateX(3px);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 12px;
  color: #909399;
  border: 1px solid #eef2f6;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

/* 详情弹窗样式 */
.notice-dialog ::v-deep .el-dialog {
  border-radius: 16px;
}

.notice-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  padding: 20px;
  margin: 0;
  border-radius: 16px 16px 0 0;
}

.notice-dialog ::v-deep .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.notice-dialog ::v-deep .el-dialog__headerbtn {
  position: absolute;
  top: 20px;
  right: 20px;
}

.notice-dialog ::v-deep .el-dialog__headerbtn .el-dialog__close {
  color: white !important;
  font-size: 18px;
  font-weight: bold;
}

.notice-dialog ::v-deep .el-dialog__headerbtn .el-dialog__close:hover {
  color: #f0f0f0 !important;
}

.notice-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

.notice-detail .notice-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eef2f6;
  flex-wrap: wrap;
}

.notice-detail .notice-type {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.notice-date, .notice-view {
  font-size: 13px;
  color: #909399;
}

.notice-date i, .notice-view i {
  margin-right: 4px;
}

.notice-content-detail {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}

.notice-content-detail p {
  margin-bottom: 12px;
}

.notice-content-detail ul, .notice-content-detail ol {
  padding-left: 24px;
  margin-bottom: 12px;
}

.notice-content-detail li {
  margin-bottom: 6px;
}

/* 响应式 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .header-left {
    text-align: center;
  }

  .header-right {
    margin-top: 12px;
  }

  .category-nav {
    border-radius: 8px;
    padding: 10px 16px;
  }

  .category-item {
    padding: 4px 12px;
    font-size: 12px;
  }

  .notice-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .notice-info {
    flex-wrap: wrap;
  }

  .notice-footer {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .empty-state {
    padding: 40px;
  }

  .notice-dialog ::v-deep .el-dialog {
    width: 95% !important;
  }
}
</style>