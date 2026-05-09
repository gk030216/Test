<template>
  <div class="chat-history-page">
    <!-- 统计概览 -->
    <div class="stats-row">
      <div class="stat-card card-users">
        <div class="stat-icon"><i class="el-icon-user" /></div>
        <div class="stat-info">
          <div class="stat-label">咨询用户数</div>
          <div class="stat-value">{{ userGroupList.length }}</div>
          <div class="stat-sub">发起过对话的用户</div>
        </div>
      </div>
      <div class="stat-card card-dialogs">
        <div class="stat-icon"><i class="el-icon-chat-dot-round" /></div>
        <div class="stat-info">
          <div class="stat-label">总对话数</div>
          <div class="stat-value">{{ total }}</div>
          <div class="stat-sub">AI 咨询记录</div>
        </div>
      </div>
      <div class="stat-card card-knowledge">
        <div class="stat-icon"><i class="el-icon-collection" /></div>
        <div class="stat-info">
          <div class="stat-label">知识库来源</div>
          <div class="stat-value">{{ kbCount }}</div>
          <div class="stat-sub">来自知识库的回答</div>
        </div>
      </div>
      <div class="stat-card card-ai">
        <div class="stat-icon"><i class="el-icon-cpu" /></div>
        <div class="stat-info">
          <div class="stat-label">AI 智能</div>
          <div class="stat-value">{{ aiCount }}</div>
          <div class="stat-sub">AI 生成的回答</div>
        </div>
      </div>
    </div>

    <!-- 左右布局 -->
    <div class="chat-layout">
      <!-- 左侧：用户列表 -->
      <div class="user-sidebar">
        <div class="sidebar-header">
          <div class="header-title">
            <i class="el-icon-user" />
            <span>用户列表</span>
            <span class="user-count">({{ filteredUserList.length }})</span>
          </div>
          <div class="sidebar-search">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户昵称 / 账号"
              clearable
              size="small"
              prefix-icon="el-icon-search"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
          </div>
        </div>

        <div class="user-list" v-loading="loading">
          <div
            v-for="user in filteredUserList"
            :key="user.userId"
            :class="['user-item', { active: currentUserId === user.userId }]"
            @click="selectUser(user)"
          >
            <div class="user-avatar-wrap">
              <el-avatar :size="44" :src="getAvatarUrl(user.avatar)" class="avatar">
                {{ getUserInitial(user.userNickname || user.userName) }}
              </el-avatar>
            </div>
            <div class="user-info">
              <div class="user-name">{{ user.userNickname || user.userName || '匿名用户' }}</div>
              <div class="user-preview">{{ truncateText(user.lastQuestion, 28) }}</div>
            </div>
            <div class="user-meta">
              <div class="user-time">{{ formatShortTime(user.lastTime) }}</div>
              <div class="user-count-badge" v-if="user.dialogCount > 0">{{ user.dialogCount }}</div>
            </div>
          </div>

          <div class="empty-users" v-if="filteredUserList.length === 0 && !loading">
            <i class="el-icon-chat-line-round" />
            <p>暂无用户对话</p>
          </div>
        </div>
      </div>

      <!-- 右侧：对话区域 -->
      <div class="chat-area" v-if="currentUser">
        <div class="chat-header">
          <div class="chat-user-info">
            <el-avatar :size="44" :src="getAvatarUrl(currentUser.avatar)" class="chat-avatar">
              {{ getUserInitial(currentUser.userNickname || currentUser.userName) }}
            </el-avatar>
            <div class="chat-user-detail">
              <div class="chat-user-name">{{ currentUser.userNickname || currentUser.userName || '匿名用户' }}</div>
              <div class="chat-user-stats">
                <i class="el-icon-chat-dot-round" /> {{ currentUser.dialogCount }} 条对话
              </div>
            </div>
          </div>
          <div class="chat-actions">
            <el-button size="small" type="danger" plain @click="deleteUserConversation(currentUser.userId)">
              <i class="el-icon-delete" /> 清空对话
            </el-button>
          </div>
        </div>

        <div class="chat-messages" ref="chatMessages" v-loading="chatLoading">
          <div class="message-list" v-if="currentDialogs.length > 0">
            <div
              v-for="msg in currentDialogs"
              :key="msg.id"
              class="message-item"
            >
              <div class="message-time"><span>{{ formatTime(msg.createTime) }}</span></div>

              <div class="message-row message-user">
                <div class="message-avatar">
                  <el-avatar :size="30" :src="getAvatarUrl(currentUser.avatar)" class="user-avatar-small">
                    {{ getUserInitial(currentUser.userNickname || currentUser.userName) }}
                  </el-avatar>
                </div>
                <div class="message-bubble user-bubble">
                  <div class="bubble-text">{{ msg.userQuestion }}</div>
                </div>
              </div>

              <div class="message-row message-ai">
                <div class="message-bubble ai-bubble">
                  <div class="bubble-text">{{ msg.aiAnswer }}</div>
                  <div class="bubble-footer">
                    <span class="bubble-source">
                      <i class="el-icon-info" /> {{ msg.answerSource === 1 ? '知识库' : 'AI 智能' }}
                    </span>
                    <span v-if="msg.rating" class="bubble-rating">
                      <i class="el-icon-star-on" /> {{ msg.rating }}星
                    </span>
                    <div class="bubble-actions">
                      <span class="action-link" @click="convertToKnowledge(msg)">转为知识库</span>
                      <span class="action-link delete-link" @click="deleteSingle(msg.id)">删除</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="empty-messages" v-if="currentDialogs.length === 0 && !chatLoading">
            <i class="el-icon-chat-line-round" />
            <p>暂无对话记录</p>
          </div>
        </div>
      </div>

      <!-- 未选择用户 -->
      <div class="chat-empty" v-else>
        <i class="el-icon-chat-dot-round" />
        <p>请从左侧选择用户查看对话</p>
      </div>
    </div>

    <!-- 转为知识库对话框 -->
    <el-dialog title="转为知识库" :visible.sync="convertDialogVisible" width="500px" center class="convert-dialog">
      <el-form :model="convertForm" label-width="80px" class="convert-form">
        <el-form-item label="标题">
          <el-input v-model="convertForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="convertForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="服务咨询" value="service" />
            <el-option label="宠物知识" value="pet_knowledge" />
            <el-option label="宠物用品" value="pet_product" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="convertDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitConvert" :loading="convertLoading" class="warm-btn">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getChatHistoryList,
  deleteChatHistory,
  batchDeleteChatHistory,
  convertToKnowledge
} from '@/api/ai';

export default {
  name: 'Admin_AI_UserChatHistory',
  data() {
    return {
      loading: false,
      chatLoading: false,
      convertLoading: false,
      historyList: [],
      userGroupList: [],
      currentUserId: null,
      currentUser: null,
      currentDialogs: [],
      total: 0,
      page: 1,
      pageSize: 100,
      searchKeyword: '',
      convertDialogVisible: false,
      convertForm: { title: '', content: '', category: 'other', source: 2 }
    };
  },
  computed: {
    filteredUserList() {
      if (!this.searchKeyword) return this.userGroupList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.userGroupList.filter(user => {
        return (user.userName && user.userName.toLowerCase().includes(keyword)) ||
            (user.userNickname && user.userNickname.toLowerCase().includes(keyword)) ||
            (user.userId && user.userId.toString().includes(keyword));
      });
    },
    kbCount() {
      return this.historyList.filter(item => item.answerSource === 1).length;
    },
    aiCount() {
      return this.historyList.filter(item => item.answerSource !== 1).length;
    }
  },
  created() {
    this.loadList();
  },
  methods: {
    getUserInitial(name) {
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
    },
    getAvatarUrl(avatar) {
      if (!avatar) return '';
      if (avatar.startsWith('http')) return avatar;
      if (avatar.startsWith('/upload')) return avatar;
      return '/api' + avatar;
    },
    async loadList() {
      this.loading = true;
      try {
        const params = { page: this.page, pageSize: this.pageSize, keyword: this.searchKeyword || undefined };
        const res = await getChatHistoryList(params);
        if (res.code === 200) {
          this.historyList = res.data.list || [];
          this.total = res.data.total || 0;
          this.groupByUser();
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    groupByUser() {
      const userMap = new Map();
      if (!this.historyList || this.historyList.length === 0) {
        this.userGroupList = [];
        return;
      }
      this.historyList.forEach(item => {
        const userId = item.userId;
        if (!userId) return;
        if (!userMap.has(userId)) {
          userMap.set(userId, {
            userId: userId,
            userName: item.userName || item.userNickname || `用户${userId}`,
            userNickname: item.userNickname,
            avatar: item.userAvatar || '',
            dialogs: [],
            dialogCount: 0,
            lastTime: item.createTime,
            lastQuestion: item.userQuestion
          });
        }
        const user = userMap.get(userId);
        user.dialogs.push(item);
        user.dialogCount++;
        if (new Date(item.createTime) > new Date(user.lastTime)) {
          user.lastTime = item.createTime;
          user.lastQuestion = item.userQuestion;
        }
      });
      this.userGroupList = Array.from(userMap.values()).sort((a, b) => {
        return new Date(b.lastTime) - new Date(a.lastTime);
      });
    },
    selectUser(user) {
      this.currentUserId = user.userId;
      this.currentUser = { ...user };
      this.currentDialogs = [...user.dialogs].sort((a, b) => {
        return new Date(a.createTime) - new Date(b.createTime);
      });
      this.$nextTick(() => { this.scrollToBottom(); });
    },
    scrollToBottom() {
      const container = this.$refs.chatMessages;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },
    handleSearch() { this.page = 1; this.loadList(); },
    async deleteSingle(id) {
      try {
        await this.$confirm('确定删除该对话记录吗？', '提示', { type: 'warning' });
        await deleteChatHistory(id);
        this.$message.success('删除成功');
        this.loadList();
        if (this.currentUserId) {
          const updatedUser = this.userGroupList.find(u => u.userId === this.currentUserId);
          if (updatedUser) {
            this.currentDialogs = [...updatedUser.dialogs].sort((a, b) => new Date(a.createTime) - new Date(b.createTime));
          }
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败');
      }
    },
    async deleteUserConversation(userId) {
      const user = this.userGroupList.find(u => u.userId === userId);
      try {
        await this.$confirm(`确定清空用户 "${user?.userNickname || user?.userName || userId}" 的所有对话记录吗？`, '警告', { type: 'warning' });
        const ids = this.historyList.filter(item => item.userId === userId).map(item => item.id);
        if (ids.length > 0) {
          await batchDeleteChatHistory(ids.join(','));
          this.$message.success('清空成功');
          this.loadList();
          if (this.currentUserId === userId) {
            this.currentUserId = null;
            this.currentUser = null;
            this.currentDialogs = [];
          }
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('清空失败');
      }
    },
    convertToKnowledge(msg) {
      let title = msg.userQuestion;
      if (title.length > 50) title = title.substring(0, 50);
      this.convertForm = { title: title, content: msg.aiAnswer, category: 'other', source: 2 };
      this.convertDialogVisible = true;
    },
    async submitConvert() {
      if (!this.convertForm.title) {
        this.$message.warning('请输入标题');
        return;
      }
      this.convertLoading = true;
      try {
        const res = await convertToKnowledge(this.convertForm);
        if (res.code === 200) {
          this.$message.success('已添加到知识库');
          this.convertDialogVisible = false;
          this.convertForm = { title: '', content: '', category: 'other', source: 2 };
        } else {
          this.$message.error(res.message || '转换失败');
        }
      } catch (error) {
        this.$message.error('转换失败');
      } finally {
        this.convertLoading = false;
      }
    },
    formatTime(date) {
      if (!date) return '';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    formatShortTime(date) {
      if (!date) return '';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      return `${d.getMonth() + 1}/${d.getDate()}`;
    },
    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.chat-history-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 统计卡片行 ===== */
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
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(61, 46, 42, 0.10);
}
.stat-icon {
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}
.card-users .stat-icon     { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-dialogs .stat-icon   { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-knowledge .stat-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-ai .stat-icon        { background: linear-gradient(135deg, #36cfc9, #5cdbd3); }
.stat-info { position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 6px; }
.stat-value { font-size: 30px; font-weight: 700; color: #3d2e2a; line-height: 1.1; letter-spacing: -0.5px; }
.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ===== 聊天布局 ===== */
.chat-layout {
  display: flex;
  gap: 20px;
  height: calc(100vh - 280px);
  min-height: 500px;
}

/* ===== 左侧用户列表 ===== */
.user-sidebar {
  width: 340px;
  background: #fff;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(61, 46, 42, 0.06);
}
.sidebar-header {
  padding: 20px;
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  color: white;
}
.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 15px;
}
.user-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
}
.sidebar-search ::v-deep .el-input__inner {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
  border-radius: 25px;
  height: 36px;
}
.sidebar-search ::v-deep .el-input__inner::placeholder { color: rgba(255, 255, 255, 0.7); }
.sidebar-search ::v-deep .el-input__prefix { color: rgba(255, 255, 255, 0.7); }

.user-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid #f5ece6;
}
.user-item:hover { background: #fefbf8; }
.user-item.active {
  background: #fef5ee;
  border-left: 3px solid #f59e4b;
}
.user-avatar-wrap { flex-shrink: 0; }
.avatar { background: linear-gradient(135deg, #f59e4b, #f0826a) !important; color: #fff; font-weight: 600; }
.user-info { flex: 1; min-width: 0; }
.user-name { font-weight: 600; color: #3d2e2a; font-size: 14px; margin-bottom: 3px; }
.user-preview {
  font-size: 12px; color: #a08c84;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.user-meta { text-align: right; flex-shrink: 0; }
.user-time { font-size: 11px; color: #c0b8b0; margin-bottom: 3px; }
.user-count-badge {
  background: #f59e4b; color: white;
  font-size: 10px; padding: 2px 7px;
  border-radius: 12px; display: inline-block;
}
.empty-users { text-align: center; padding: 60px 20px; color: #b8a8a0; }
.empty-users i { font-size: 48px; margin-bottom: 16px; display: block; }

/* ===== 右侧对话区域 ===== */
.chat-area {
  flex: 1;
  background: #fff;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(61, 46, 42, 0.06);
}
.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f5ece6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
}
.chat-user-info { display: flex; align-items: center; gap: 14px; }
.chat-avatar { background: linear-gradient(135deg, #f59e4b, #f0826a) !important; color: #fff; font-weight: 600; }
.chat-user-name { font-weight: 600; color: #3d2e2a; font-size: 16px; margin-bottom: 3px; }
.chat-user-stats { font-size: 12px; color: #a08c84; }
.chat-user-stats i { margin-right: 4px; }
.chat-actions .el-button { border-radius: 10px; }

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #fdf8f4;
}
.message-list { display: flex; flex-direction: column; gap: 20px; }
.message-item { display: flex; flex-direction: column; gap: 8px; }
.message-time { text-align: center; font-size: 11px; color: #b8a8a0; margin: 4px 0; }
.message-row { display: flex; align-items: flex-start; gap: 10px; }
.message-user { justify-content: flex-start; }
.message-ai { justify-content: flex-end; }
.message-avatar { flex-shrink: 0; }
.user-avatar-small { background: #e8ddd4; color: #5c4a42; }
.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.6;
}
.user-bubble {
  background: #f5ece6;
  color: #3d2e2a;
  border-radius: 18px 18px 18px 4px;
}
.ai-bubble {
  background: #fef5ee;
  color: #3d2e2a;
  border-radius: 18px 18px 4px 18px;
}
.bubble-text { white-space: pre-wrap; word-break: break-word; }
.bubble-footer {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 11px;
  color: #a08c84;
}
.bubble-source i, .bubble-rating i { margin-right: 2px; }
.bubble-actions { display: flex; gap: 8px; margin-left: auto; }
.action-link { cursor: pointer; color: #f59e4b; font-size: 11px; }
.action-link:hover { text-decoration: underline; }
.delete-link { color: #f56c6c; }

/* 空状态 */
.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #b8a8a0;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(61, 46, 42, 0.06);
}
.empty-messages { text-align: center; padding: 60px 20px; color: #b8a8a0; }
.empty-messages i, .chat-empty i { font-size: 64px; margin-bottom: 20px; display: block; color: #e8ddd4; }

/* ===== 滚动条 ===== */
.user-list::-webkit-scrollbar,
.chat-messages::-webkit-scrollbar { width: 6px; }
.user-list::-webkit-scrollbar-track,
.chat-messages::-webkit-scrollbar-track { background: #f5ece6; border-radius: 3px; }
.user-list::-webkit-scrollbar-thumb,
.chat-messages::-webkit-scrollbar-thumb { background: #c0b8b0; border-radius: 3px; }

/* ===== 对话框 ===== */
.convert-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.convert-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.convert-dialog ::v-deep .el-dialog__title { color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px; }
.convert-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.convert-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.convert-dialog ::v-deep .el-dialog__body { padding: 28px; }

.convert-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.convert-form ::v-deep .el-input__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.convert-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}

.dialog-footer {
  text-align: right;
  padding: 16px 28px 22px;
  border-top: 1px solid #f5ece6;
}
.dialog-footer .el-button { border-radius: 10px; padding: 10px 28px; font-weight: 500; font-size: 13px; }
.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
}
.dialog-footer .el-button--primary:hover { opacity: 0.88; transform: translateY(-1px); }

/* 响应式 */
@media (max-width: 900px) {
  .chat-history-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .chat-layout { flex-direction: column; height: auto; }
  .user-sidebar { width: 100%; max-height: 350px; }
  .chat-area { min-height: 450px; }
  .message-bubble { max-width: 85%; }
}
</style>
