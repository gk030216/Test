<template>
  <div class="ai-chat-container">
    <Navbar />

    <div class="chat-content">
      <div class="container">
        <div class="chat-layout">
          <!-- 左侧：常见问题 -->
          <div class="faq-sidebar">
            <div class="sidebar-header">
              <i class="el-icon-chat-dot-round"></i>
              <span>常见问题</span>
            </div>
            <div class="faq-list">
              <div
                  v-for="faq in faqList"
                  :key="faq.id"
                  class="faq-item"
                  @click="sendQuestion(faq.question)"
              >
                <i class="el-icon-question"></i>
                <span>{{ faq.question }}</span>
              </div>
            </div>
          </div>

          <!-- 右侧：聊天区域 -->
          <div class="chat-main">
            <div class="chat-header">
              <div class="chat-title">
                <i class="el-icon-cpu"></i>
                <span>AI智能顾问 - 宠小伴</span>
                <el-tag size="small" type="success">24小时在线</el-tag>
              </div>
              <div class="chat-actions">
                <el-button size="small" type="text" @click="clearHistory" :loading="clearLoading">
                  <i class="el-icon-delete"></i> 清空对话
                </el-button>
                <el-button size="small" type="text" @click="loadHistory">
                  <i class="el-icon-refresh"></i> 刷新
                </el-button>
              </div>
            </div>

            <div class="chat-messages" ref="messagesContainer">
              <div v-if="messages.length === 0" class="empty-chat">
                <i class="el-icon-cpu"></i>
                <p>你好！我是宠小伴，你的宠物顾问 🐾</p>
                <p>有什么关于宠物的问题都可以问我哦~</p>
                <div class="example-questions">
                  <span class="example-title">试试问：</span>
                  <span v-for="q in exampleQuestions" :key="q" @click="sendQuestion(q)" class="example-item">
                    {{ q }}
                  </span>
                </div>
              </div>

              <div
                  v-for="(msg, idx) in messages"
                  :key="idx"
                  :class="['message', msg.role]"
              >
                <div class="message-avatar">
                  <el-avatar :size="36" :src="msg.avatar">
                    {{ msg.role === 'user' ? (msg.userName ? msg.userName.charAt(0).toUpperCase() : 'U') : 'AI' }}
                  </el-avatar>
                </div>
                <div class="message-content">
                  <div class="message-text" v-html="formatMessage(msg.content)"></div>
                  <div class="message-footer" v-if="msg.role === 'assistant'">
                    <span class="message-source">
                      <i class="el-icon-info"></i> 来源：{{ msg.source || 'AI智能' }}
                    </span>
                    <div class="message-rating" v-if="msg.historyId">
                      <span>有帮助吗？</span>
                      <i
                          v-for="star in 5"
                          :key="star"
                          :class="['el-icon-star-on', { active: msg.rating >= star }]"
                          @click="rateAnswer(msg.historyId, star)"
                      ></i>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="isTyping" class="message assistant typing">
                <div class="message-avatar">
                  <el-avatar :size="36">AI</el-avatar>
                </div>
                <div class="message-content">
                  <div class="typing-indicator">
                    <span></span><span></span><span></span>
                  </div>
                </div>
              </div>
            </div>

            <div class="chat-input">
              <el-input
                  v-model="inputMessage"
                  type="textarea"
                  :rows="3"
                  placeholder="输入你的问题，例如：狗狗可以吃巧克力吗？"
                  @keyup.ctrl.enter="sendMessage"
                  :disabled="isTyping"
              ></el-input>
              <div class="input-actions">
                <div class="input-tip">
                  <i class="el-icon-info"></i> Ctrl + Enter 发送
                </div>
                <el-button
                    type="primary"
                    @click="sendMessage"
                    :loading="isTyping"
                    :disabled="!inputMessage.trim()"
                >
                  <i class="el-icon-position"></i> 发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { aiChat, getFaqList, getChatHistory, rateAnswer, clearChatHistory } from '@/api/ai';

export default {
  name: 'AIChat',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      isTyping: false,
      clearLoading: false,
      inputMessage: '',
      messages: [],
      faqList: [],
      exampleQuestions: [
        '狗狗可以吃巧克力吗？',
        '猫咪多久洗一次澡？',
        '小狗刚到家要注意什么？',
        '猫咪打喷嚏是生病了吗？'
      ],
      userInfo: null
    };
  },
  created() {
    this.userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    this.loadFaq();
    this.loadHistory();
  },
  mounted() {
    this.scrollToBottom();
  },
  updated() {
    this.scrollToBottom();
  },
  methods: {
    async loadFaq() {
      try {
        const res = await getFaqList();
        if (res.code === 200) {
          this.faqList = res.data;
          console.log('常见问题数据:', this.faqList);
        }
      } catch (error) {
        console.error('加载常见问题失败', error);
      }
    },

    async loadHistory() {
      try {
        const res = await getChatHistory(1, 50);
        if (res.code === 200) {
          const history = res.data.list || [];
          const messages = [];
          for (const item of history) {
            messages.push({
              role: 'user',
              content: item.userQuestion,
              timestamp: item.createTime,
              userName: this.userInfo?.nickname || this.userInfo?.username || '用户',
              avatar: this.userInfo?.avatar || ''
            });
            messages.push({
              role: 'assistant',
              content: item.aiAnswer,
              source: item.answerSource === 1 ? '知识库' : 'AI智能',
              historyId: item.id,
              rating: item.rating,
              avatar: ''
            });
          }
          this.messages = messages;
        }
      } catch (error) {
        console.error('加载历史失败', error);
      }
    },

    async sendMessage() {
      const question = this.inputMessage.trim();
      if (!question) return;

      // 添加用户消息
      this.messages.push({
        role: 'user',
        content: question,
        timestamp: new Date(),
        userName: this.userInfo?.nickname || this.userInfo?.username || '用户',
        avatar: this.userInfo?.avatar || ''
      });
      this.inputMessage = '';
      this.isTyping = true;

      try {
        const res = await aiChat({ question });
        if (res.code === 200) {
          this.messages.push({
            role: 'assistant',
            content: res.data.answer,
            source: res.data.source,
            historyId: res.data.historyId,
            avatar: ''
          });
        } else {
          this.messages.push({
            role: 'assistant',
            content: '抱歉，我遇到了一些问题，请稍后再试 🐾',
            avatar: ''
          });
        }
      } catch (error) {
        this.messages.push({
          role: 'assistant',
          content: '网络异常，请稍后再试 🐾',
          avatar: ''
        });
      } finally {
        this.isTyping = false;
      }
    },

    sendQuestion(question) {
      // 查找是否有预设答案
      const faqItem = this.faqList.find(item => item.question === question);
      if (faqItem && faqItem.answer) {
        // 有预设答案，直接显示，不调用AI
        this.messages.push({
          role: 'user',
          content: question,
          timestamp: new Date(),
          userName: this.userInfo?.nickname || this.userInfo?.username || '用户',
          avatar: this.userInfo?.avatar || ''
        });
        this.messages.push({
          role: 'assistant',
          content: faqItem.answer,
          source: '知识库',
          historyId: null,
          avatar: ''
        });
        this.scrollToBottom();
        return;
      }

      // 没有预设答案，调用 AI
      this.inputMessage = question;
      this.sendMessage();
    },

    async rateAnswer(historyId, rating) {
      try {
        const res = await rateAnswer({ historyId, rating });
        if (res.code === 200) {
          const msg = this.messages.find(m => m.historyId === historyId);
          if (msg) msg.rating = rating;
          this.$message.success('感谢您的评价！');
        }
      } catch (error) {
        this.$message.error('评价失败');
      }
    },

    async clearHistory() {
      this.clearLoading = true;
      try {
        const res = await clearChatHistory();
        if (res.code === 200) {
          this.messages = [];
          this.$message.success('对话已清空');
        } else {
          this.$message.error(res.message || '清空失败');
        }
      } catch (error) {
        console.error('清空历史失败', error);
        this.$message.error('清空失败');
      } finally {
        this.clearLoading = false;
      }
    },

    formatMessage(content) {
      if (!content) return '';
      return content.replace(/\n/g, '<br>');
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    }
  }
};
</script>

<style scoped>
.ai-chat-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.chat-content {
  flex: 1;
  padding: 30px 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.chat-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
  height: calc(100vh - 200px);
  min-height: 600px;
}

/* 左侧常见问题 */
.faq-sidebar {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar-header i {
  font-size: 20px;
}

.faq-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.faq-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 15px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}

.faq-item:hover {
  background: #f8f9fc;
  color: #667eea;
  transform: translateX(4px);
}

.faq-item i {
  font-size: 18px;
  color: #667eea;
}

/* 右侧聊天区域 */
.chat-main {
  background: white;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #333;
}

.chat-title i {
  font-size: 24px;
  color: #667eea;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fafbfc;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

.message.user {
  flex-direction: row-reverse;
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 20px 20px 4px 20px;
}

.message.assistant .message-content {
  background: white;
  border-radius: 20px 20px 20px 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.message-avatar {
  flex-shrink: 0;
}

.message-avatar ::v-deep .el-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.message.user .message-avatar ::v-deep .el-avatar {
  background: #e0e0e0;
  color: #999;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
}

.message-text {
  line-height: 1.6;
  word-break: break-word;
}

.message-footer {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.message-source i {
  margin-right: 4px;
}

.message-rating {
  display: flex;
  align-items: center;
  gap: 5px;
}

.message-rating i {
  cursor: pointer;
  color: #ddd;
  transition: color 0.3s;
}

.message-rating i:hover,
.message-rating i.active {
  color: #ff9900;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-8px); opacity: 1; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.empty-chat {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-chat i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #667eea;
}

.example-questions {
  margin-top: 30px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.example-title {
  color: #666;
  font-size: 14px;
}

.example-item {
  padding: 6px 14px;
  background: #f0f0f0;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.example-item:hover {
  background: #667eea;
  color: white;
}

/* 输入区域 */
.chat-input {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  background: white;
}

.chat-input ::v-deep .el-textarea__inner {
  border-radius: 16px;
  resize: none;
  font-size: 14px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.input-tip {
  font-size: 12px;
  color: #999;
}

.input-tip i {
  margin-right: 4px;
}

@media (max-width: 768px) {
  .chat-layout {
    grid-template-columns: 1fr;
    height: auto;
  }

  .faq-sidebar {
    display: none;
  }

  .message-content {
    max-width: 85%;
  }
}
</style>