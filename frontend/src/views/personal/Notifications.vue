<template>
  <div class="notifications-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">消息通知</h2>
        <p class="page-desc">查看系统通知和提醒</p>
      </div>
      <el-button
          v-if="unreadCount > 0"
          type="primary"
          size="small"
          plain
          @click="handleMarkAllRead"
          class="mark-all-btn"
      >
        <i class="el-icon-check"></i> 全部已读
      </el-button>
    </div>

    <!-- 类型筛选标签页 -->
    <div class="notification-tabs">
      <div
          v-for="tab in typeTabs"
          :key="tab.value"
          :class="['tab-item', { active: activeType === tab.value }]"
          @click="handleTypeChange(tab.value)"
      >
        <i :class="tab.icon"></i>
        <span>{{ tab.label }}</span>
        <span class="tab-count" v-if="getTypeCount(tab.value) > 0">{{ getTypeCount(tab.value) }}</span>
      </div>
    </div>

    <div class="notifications-list" v-loading="loading">
      <div class="empty-state" v-if="!loading && paginatedList.length === 0">
        <i class="el-icon-bell"></i>
        <p>暂无{{ getTypeName(activeType) }}消息</p>
      </div>

      <div
          v-for="item in paginatedList"
          :key="item.id"
          :class="['notification-item', { unread: item.isRead === 0 }]"
      >
        <div class="notification-icon" :class="'icon-' + item.type">
          <i :class="getNotificationIcon(item.type)"></i>
        </div>
        <div class="notification-content" @click="handleNotificationClick(item)">
          <div class="notification-title">{{ item.title }}</div>
          <div class="notification-desc">{{ item.content }}</div>
          <div class="notification-time">{{ formatTime(item.createTime) }}</div>
        </div>
        <div class="notification-actions">
          <el-button
              v-if="item.isRead === 0"
              type="text"
              size="small"
              @click="handleMarkRead(item.id)"
              class="read-btn"
          >
            标记已读
          </el-button>
          <el-button
              type="text"
              size="small"
              @click="handleDelete(item.id)"
              class="delete-btn"
          >
            删除
          </el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="filteredTotal > pageSize">
        <el-pagination
            @current-change="handlePageChange"
            :current-page="page"
            :page-size="pageSize"
            layout="prev, pager, next"
            :total="filteredTotal"
            background
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import { getNotificationList, markAsRead, markAllAsRead, deleteNotification, getUnreadCount } from '@/api/notification';

export default {
  name: 'Notifications',
  data() {
    return {
      loading: false,
      allNotificationList: [],  // 存储所有消息（不分页）
      notificationList: [],     // 当前页的消息
      total: 0,
      page: 1,
      pageSize: 10,
      unreadCount: 0,
      activeType: 'all',
      typeTabs: [
        { value: 'all', label: '全部', icon: 'el-icon-bell' },
        { value: 'appointment', label: '预约', icon: 'el-icon-s-order' },
        { value: 'order', label: '订单', icon: 'el-icon-shopping-cart-2' },
        { value: 'comment', label: '评论', icon: 'el-icon-chat-dot-round' },
        { value: 'system', label: '系统', icon: 'el-icon-info' }
      ]
    };
  },
  computed: {
    // 根据类型过滤后的消息列表
    filteredList() {
      if (this.activeType === 'all') {
        return this.allNotificationList;
      }
      return this.allNotificationList.filter(item => item.type === this.activeType);
    },
    // 过滤后的总数
    filteredTotal() {
      return this.filteredList.length;
    },
    // 当前页的消息
    paginatedList() {
      const start = (this.page - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredList.slice(start, end);
    }
  },
  watch: {
    activeType() {
      this.page = 1;
      // 切换类型时不需要重新请求，直接从 allNotificationList 中过滤
    }
  },
  created() {
    this.loadAllNotifications();
    this.loadUnreadCount();
  },
  methods: {
    async loadAllNotifications() {
      this.loading = true;
      try {
        // 一次性加载所有消息（不分页）
        const res = await getNotificationList({ page: 1, pageSize: 9999 });
        if (res.code === 200) {
          this.allNotificationList = res.data.list || [];
          this.total = res.data.total || 0;
          // 更新当前页显示
          this.updateCurrentPageList();
        }
      } catch (error) {
        console.error('加载消息列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    // 更新当前页显示（当 allNotificationList 变化时调用）
    updateCurrentPageList() {
      const start = (this.page - 1) * this.pageSize;
      const end = start + this.pageSize;
      this.notificationList = this.filteredList.slice(start, end);
    },

    async loadUnreadCount() {
      try {
        const res = await getUnreadCount();
        if (res.code === 200) {
          this.unreadCount = res.data || 0;
        }
      } catch (error) {
        console.error('获取未读数量失败', error);
      }
    },

    getTypeCount(type) {
      if (type === 'all') {
        return this.total;
      }
      return this.allNotificationList.filter(item => item.type === type).length;
    },

    getTypeName(type) {
      const map = {
        all: '',
        appointment: '预约',
        order: '订单',
        comment: '评论',
        system: '系统'
      };
      return map[type] || '';
    },

    handleTypeChange(type) {
      this.activeType = type;
      this.page = 1;
      this.updateCurrentPageList();
    },

    async handleMarkRead(id) {
      try {
        const res = await markAsRead(id);
        if (res.code === 200) {
          this.$message.success('已标记为已读');
          const item = this.allNotificationList.find(n => n.id === id);
          if (item) {
            item.isRead = 1;
          }
          this.unreadCount = Math.max(0, this.unreadCount - 1);
          this.updateCurrentPageList();
          this.$bus.$emit('new-notification');
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('标记已读失败', error);
        this.$message.error('操作失败');
      }
    },

    async handleMarkAllRead() {
      if (this.unreadCount === 0) {
        this.$message.info('没有未读消息');
        return;
      }

      this.$confirm('确定要将所有消息标记为已读吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const res = await markAllAsRead();
          if (res.code === 200) {
            this.$message.success('已全部标记为已读');
            this.allNotificationList.forEach(item => {
              item.isRead = 1;
            });
            this.unreadCount = 0;
            this.updateCurrentPageList();
            this.$bus.$emit('new-notification');
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          console.error('全部标记已读失败', error);
          this.$message.error('操作失败');
        }
      }).catch(() => {});
    },

    async handleDelete(id) {
      this.$confirm('确定要删除这条消息吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteNotification(id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            const index = this.allNotificationList.findIndex(n => n.id === id);
            if (index !== -1) {
              const item = this.allNotificationList[index];
              if (item.isRead === 0) {
                this.unreadCount = Math.max(0, this.unreadCount - 1);
              }
              this.allNotificationList.splice(index, 1);
              this.total--;
            }
            // 如果当前页没有数据了，回到上一页
            if (this.paginatedList.length === 0 && this.page > 1) {
              this.page--;
            }
            this.updateCurrentPageList();
            this.$bus.$emit('new-notification');
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          console.error('删除消息失败', error);
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    handleNotificationClick(item) {
      if (item.isRead === 0) {
        this.handleMarkRead(item.id);
      }
      switch (item.type) {
        case 'appointment':
          this.$router.push('/personal/appointments');
          break;
        case 'order':
          this.$router.push('/personal/orders');
          break;
        case 'comment':
          this.$router.push('/personal/comments');
          break;
        case 'system':
        default:
          break;
      }
    },

    handlePageChange(page) {
      this.page = page;
      this.updateCurrentPageList();
    },

    getNotificationIcon(type) {
      const iconMap = {
        appointment: 'el-icon-s-order',
        order: 'el-icon-shopping-cart-2',
        comment: 'el-icon-chat-dot-round',
        system: 'el-icon-info'
      };
      return iconMap[type] || 'el-icon-bell';
    },

    formatTime(date) {
      if (!date) return '';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;

      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;

      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.notifications-page {
  padding: 0;
  background: transparent;
  min-height: 400px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.page-desc {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.mark-all-btn {
  border-radius: 20px;
  padding: 8px 20px;
}

/* 类型筛选标签页 */
.notification-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 30px;
  transition: all 0.3s;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  background: #f5f5f5;
}

.tab-item i {
  font-size: 14px;
}

.tab-item:hover {
  background: #e8e8e8;
  color: #667eea;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.tab-count {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 2px 6px;
  font-size: 11px;
  margin-left: 4px;
}

.notifications-list {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s;
}

.notification-item:hover {
  background: #fafbfc;
}

.notification-item.unread {
  background: #f0f7ff;
}

.notification-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-icon i {
  font-size: 22px;
  color: white;
}

.icon-appointment {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.icon-order {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
}

.icon-comment {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.icon-system {
  background: linear-gradient(135deg, #909399, #b0b3b9);
}

.notification-content {
  flex: 1;
  cursor: pointer;
}

.notification-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
}

.notification-desc {
  font-size: 14px;
  color: #5a6874;
  line-height: 1.5;
  margin-bottom: 8px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.read-btn {
  color: #667eea;
}

.read-btn:hover {
  color: #409EFF;
}

.delete-btn {
  color: #f56c6c;
}

.delete-btn:hover {
  color: #ff6b6b;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #dcdfe6;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

.pagination {
  padding: 16px 24px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 768px) {
  .notification-item {
    flex-direction: column;
    padding: 16px;
  }

  .notification-actions {
    align-self: flex-end;
    margin-top: 8px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .mark-all-btn {
    width: 100%;
  }

  .notification-tabs {
    justify-content: center;
  }
}
</style>