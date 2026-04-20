<template>
  <div class="user-chat-history">

    <!-- 左右布局 -->
    <div class="chat-layout">
      <!-- 左侧：用户列表 -->
      <div class="user-sidebar">
        <div class="sidebar-header">
          <div class="header-title">
            <i class="el-icon-user"></i>
            <span>用户列表</span>
            <span class="user-count">({{ filteredUserList.length }})</span>
          </div>
          <div class="sidebar-search">
            <el-input
                v-model="searchKeyword"
                placeholder="搜索用户昵称/账号"
                clearable
                size="small"
                prefix-icon="el-icon-search"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
            ></el-input>
          </div>
        </div>

        <div class="user-list" v-loading="loading">
          <div
              v-for="user in filteredUserList"
              :key="user.userId"
              :class="['user-item', { active: currentUserId === user.userId }]"
              @click="selectUser(user)"
          >
            <div class="user-avatar">
              <el-avatar :size="48" :src="getAvatarUrl(user.avatar)" class="avatar">
                {{ getUserInitial(user.userName) }}
              </el-avatar>
            </div>
            <div class="user-info">
              <div class="user-name">{{ user.userName || '匿名用户' }}</div>
              <div class="user-preview">{{ truncateText(user.lastQuestion, 30) }}</div>
            </div>
            <div class="user-meta">
              <div class="user-time">{{ formatShortTime(user.lastTime) }}</div>
              <div class="user-count-badge" v-if="user.dialogCount > 0">{{ user.dialogCount }}</div>
            </div>
          </div>

          <div class="empty-users" v-if="filteredUserList.length === 0 && !loading">
            <i class="el-icon-chat-line-round"></i>
            <p>暂无用户对话</p>
          </div>
        </div>
      </div>

      <!-- 右侧：对话区域 -->
      <div class="chat-area" v-if="currentUser">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-user-info">
            <el-avatar :size="44" :src="getAvatarUrl(currentUser.avatar)" class="chat-avatar">
              {{ getUserInitial(currentUser.userName) }}
            </el-avatar>
            <div class="chat-user-detail">
              <div class="chat-user-name">{{ currentUser.userName || '匿名用户' }}</div>
              <div class="chat-user-stats">
                <span><i class="el-icon-chat-dot-round"></i> {{ currentUser.dialogCount }} 条对话</span>
              </div>
            </div>
          </div>
          <div class="chat-actions">
            <el-button size="small" type="danger" plain @click="deleteUserConversation(currentUser.userId)">
              <i class="el-icon-delete"></i> 清空对话
            </el-button>
          </div>
        </div>

        <!-- 聊天消息区域 -->
        <div class="chat-messages" ref="chatMessages" v-loading="chatLoading">
          <div class="message-list">
            <div
                v-for="msg in currentDialogs"
                :key="msg.id"
                class="message-item"
            >
              <div class="message-time">
                <span>{{ formatTime(msg.createTime) }}</span>
              </div>

              <!-- 用户问题（左侧） -->
              <div class="message-row message-user">
                <div class="message-avatar">
                  <el-avatar :size="32" :src="getAvatarUrl(currentUser.avatar)" class="user-avatar-small">
                    {{ getUserInitial(currentUser.userName) }}
                  </el-avatar>
                </div>
                <div class="message-bubble user-bubble">
                  <div class="bubble-text">{{ msg.userQuestion }}</div>
                </div>
              </div>

              <!-- AI回答（右侧） -->
              <div class="message-row message-ai">
                <div class="message-bubble ai-bubble">
                  <div class="bubble-text">{{ msg.aiAnswer }}</div>
                  <div class="bubble-footer">
                    <span class="bubble-source">
                      <i class="el-icon-info"></i> {{ msg.answerSource === 1 ? '知识库' : 'AI智能' }}
                    </span>
                    <span v-if="msg.rating" class="bubble-rating">
                      <i class="el-icon-star-on"></i> {{ msg.rating }}星
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
            <i class="el-icon-chat-line-round"></i>
            <p>暂无对话记录</p>
          </div>
        </div>

      </div>

      <!-- 未选择用户时的占位 -->
      <div class="chat-empty" v-else>
        <i class="el-icon-chat-dot-round"></i>
        <p>请从左侧选择用户查看对话</p>
      </div>
    </div>

    <!-- 转为知识库对话框 -->
    <el-dialog title="转为知识库" :visible.sync="convertDialogVisible" width="500px" center>
      <el-form :model="convertForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="convertForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="convertForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="服务咨询" value="service"></el-option>
            <el-option label="宠物知识" value="pet_knowledge"></el-option>
            <el-option label="宠物用品" value="pet_product"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="convertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConvert" :loading="convertLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getChatHistoryList,
  deleteChatHistory,
  batchDeleteChatHistory,
  convertToKnowledge,
} from '@/api/ai';

export default {
  name: 'UserChatHistory',
  data() {
    return {
      loading: false,
      chatLoading: false,
      convertLoading: false,
      sending: false,
      historyList: [],
      userGroupList: [],
      currentUserId: null,
      currentUser: null,
      currentDialogs: [],
      total: 0,
      page: 1,
      pageSize: 100,
      searchKeyword: '',
      replyMessage: '',
      convertDialogVisible: false,
      convertForm: {
        title: '',
        content: '',
        category: 'other',
        source: 2
      }
    };
  },
  computed: {
    filteredUserList() {
      if (!this.searchKeyword) {
        return this.userGroupList;
      }
      const keyword = this.searchKeyword.toLowerCase();
      return this.userGroupList.filter(user => {
        return (user.userName && user.userName.toLowerCase().includes(keyword)) ||
            (user.userId && user.userId.toString().includes(keyword));
      });
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
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined
        };
        const res = await getChatHistoryList(params);

        if (res.code === 200) {
          this.historyList = res.data.list || [];
          this.total = res.data.total || 0;
          this.groupByUser();
        }
      } catch (error) {
        console.error('加载失败', error);
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
            userName: item.userName || `用户${userId}`,
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
      this.currentUser = user;
      // 按时间升序排序（最早在上，最新在下）
      this.currentDialogs = [...user.dialogs].sort((a, b) => {
        return new Date(a.createTime) - new Date(b.createTime);
      });
      this.replyMessage = '';
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },

    scrollToBottom() {
      const container = this.$refs.chatMessages;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },

    handleSearch() {
      this.page = 1;
      this.loadList();
    },

    async deleteSingle(id) {
      this.$confirm('确定删除该对话记录吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteChatHistory(id);
        this.$message.success('删除成功');
        this.loadList();
        if (this.currentUserId) {
          const updatedUser = this.userGroupList.find(u => u.userId === this.currentUserId);
          if (updatedUser) {
            this.currentDialogs = [...updatedUser.dialogs];
          }
        }
      }).catch(() => {});
    },

    async deleteUserConversation(userId) {
      const user = this.userGroupList.find(u => u.userId === userId);
      this.$confirm(`确定清空用户 "${user?.userName || userId}" 的所有对话记录吗？`, '警告', { type: 'warning' })
          .then(async () => {
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
          }).catch(() => {});
    },

    convertToKnowledge(msg) {
      let title = msg.userQuestion;
      if (title.length > 50) {
        title = title.substring(0, 50);
      }
      this.convertForm = {
        title: title,
        content: msg.aiAnswer,
        category: 'other',
        source: 2
      };
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
          this.convertForm = {
            title: '',
            content: '',
            category: 'other',
            source: 2
          };
        } else {
          this.$message.error(res.message);
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
.user-chat-history {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: #909399;
}

.chat-layout {
  display: flex;
  gap: 20px;
  height: calc(100vh - 120px);
  min-height: 550px;
}

/* 左侧用户列表 */
.user-sidebar {
  width: 340px;
  background: #fff;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.sidebar-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 12px;
}

.sidebar-search {
  margin-top: 4px;
}

.sidebar-search ::v-deep .el-input__inner {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
  border-radius: 25px;
  height: 36px;
}

.sidebar-search ::v-deep .el-input__inner::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.sidebar-search ::v-deep .el-input__prefix {
  color: rgba(255, 255, 255, 0.7);
}

.user-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.user-item:hover {
  background: #f8f9fc;
}

.user-item.active {
  background: #e8f0fe;
  border-left: 3px solid #667eea;
}

.user-avatar {
  flex-shrink: 0;
}

.avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
  margin-bottom: 4px;
}

.user-preview {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-meta {
  text-align: right;
  flex-shrink: 0;
}

.user-time {
  font-size: 11px;
  color: #ccc;
  margin-bottom: 4px;
}

.user-count-badge {
  background: #667eea;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 12px;
  display: inline-block;
}

.empty-users {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-users i {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 右侧对话区域 */
.chat-area {
  flex: 1;
  background: #fff;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.chat-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.chat-user-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
  margin-bottom: 4px;
}

.chat-user-stats {
  font-size: 12px;
  color: #999;
}

.chat-user-stats i {
  margin-right: 4px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f8f9fa;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-time {
  text-align: center;
  font-size: 11px;
  color: #999;
  margin: 4px 0;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.message-user {
  justify-content: flex-start;
}

.message-ai {
  justify-content: flex-end;
}

.message-avatar {
  flex-shrink: 0;
}

.user-avatar-small {
  background: #e8eaef;
  color: #666;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
}

.user-bubble {
  background: #f1f3f5;
  color: #333;
  border-radius: 18px 18px 18px 4px;
}

.ai-bubble {
  background: #e8f4fd;
  color: #333;
  border-radius: 18px 18px 4px 18px;
}

.bubble-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble-footer {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 11px;
  color: #999;
}

.bubble-source i, .bubble-rating i {
  margin-right: 2px;
}

.bubble-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.action-link {
  cursor: pointer;
  color: #667eea;
}

.action-link:hover {
  text-decoration: underline;
}

.delete-link {
  color: #f56c6c;
}

.delete-link:hover {
  color: #ff6b6b;
}

/* 消息输入框区域 */
.chat-input-area {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}

.chat-input-area ::v-deep .el-textarea__inner {
  border-radius: 20px;
  resize: none;
  font-size: 14px;
}

.input-actions {
  margin-top: 10px;
  text-align: right;
}

.input-actions .el-button {
  border-radius: 20px;
}

/* 空状态 - 居中显示 */
.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #999;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.empty-messages i, .chat-empty i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #ddd;
}

/* 滚动条 */
.user-list::-webkit-scrollbar,
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.user-list::-webkit-scrollbar-track,
.chat-messages::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 3px;
}

.user-list::-webkit-scrollbar-thumb,
.chat-messages::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

/* 响应式 */
@media (max-width: 900px) {
  .chat-layout {
    flex-direction: column;
    height: auto;
  }

  .user-sidebar {
    width: 100%;
    max-height: 350px;
  }

  .chat-area {
    min-height: 450px;
  }

  .message-bubble {
    max-width: 85%;
  }
}
</style>