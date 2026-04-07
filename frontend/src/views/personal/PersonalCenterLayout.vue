<template>
  <div class="personal-center">
    <Navbar />

    <div class="center-content">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h2 class="page-title">个人中心</h2>
          <p class="page-desc">管理你的个人信息和内容</p>
        </div>

        <div class="center-layout">
          <!-- 左侧菜单 -->
          <div class="center-sidebar">
            <div class="user-card">
              <div class="user-avatar-wrapper" @click="showAvatarUpload = true">
                <el-avatar :size="80" :src="userInfo.avatar" class="user-avatar" :style="userInfo.avatar ? {} : { background: getAvatarColor(userInfo.nickname || userInfo.username) }">
                  {{ !userInfo.avatar ? getUserInitial(userInfo.nickname || userInfo.username) : '' }}
                </el-avatar>
                <div class="avatar-mask">
                  <i class="el-icon-camera"></i>
                  <span>更换头像</span>
                </div>
              </div>
              <h3 class="user-name">{{ userInfo.nickname || userInfo.username }}</h3>
              <p class="user-role">{{ getUserRoleName(userInfo.role) }}</p>
            </div>

            <el-menu
                :default-active="activeTab"
                class="sidebar-menu"
                @select="handleMenuSelect"
            >
              <el-menu-item index="profile">
                <i class="el-icon-user"></i>
                <span>个人资料</span>
              </el-menu-item>
              <el-menu-item index="posts">
                <i class="el-icon-document"></i>
                <span>我的帖子</span>
              </el-menu-item>
              <el-menu-item index="favorites">
                <i class="el-icon-star-on"></i>
                <span>我的收藏</span>
              </el-menu-item>
              <el-menu-item index="comments">
                <i class="el-icon-chat-dot-round"></i>
                <span>我的评论</span>
              </el-menu-item>
              <el-menu-item index="orders">
                <i class="el-icon-s-order"></i>
                <span>我的订单</span>
              </el-menu-item>
              <el-menu-item index="address">
                <i class="el-icon-location"></i>
                <span>收货地址</span>
              </el-menu-item>
              <el-menu-item index="security">
                <i class="el-icon-lock"></i>
                <span>安全设置</span>
              </el-menu-item>
            </el-menu>
          </div>

          <!-- 右侧内容区 -->
          <div class="center-content-area">
            <!-- 个人资料 -->
            <div v-show="activeTab === 'profile'" class="content-panel">
              <div class="panel-header">
                <h3>个人资料</h3>
                <el-button type="text" @click="editProfile" v-if="!isEditing">编辑资料</el-button>
                <span v-else>
                  <el-button type="primary" size="small" @click="saveProfile" :loading="saveLoading">保存</el-button>
                  <el-button size="small" @click="cancelEdit">取消</el-button>
                </span>
              </div>

              <el-form :model="profileForm" :rules="profileRules" ref="profileForm" label-width="100px" class="profile-form">
                <el-form-item label="用户名" prop="username">
                  <span v-if="!isEditing">{{ profileForm.username }}</span>
                  <el-input v-else v-model="profileForm.username" disabled></el-input>
                </el-form-item>

                <el-form-item label="昵称" prop="nickname">
                  <span v-if="!isEditing">{{ profileForm.nickname || '--' }}</span>
                  <el-input v-else v-model="profileForm.nickname" placeholder="请输入昵称"></el-input>
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                  <span v-if="!isEditing">{{ profileForm.email }}</span>
                  <el-input v-else v-model="profileForm.email" placeholder="请输入邮箱"></el-input>
                </el-form-item>

                <el-form-item label="手机号" prop="phone">
                  <span v-if="!isEditing">{{ profileForm.phone || '--' }}</span>
                  <el-input v-else v-model="profileForm.phone" placeholder="请输入手机号"></el-input>
                </el-form-item>

                <el-form-item label="注册时间">
                  <span>{{ formatDate(userInfo.createTime) }}</span>
                </el-form-item>

                <el-form-item label="最后登录">
                  <span>{{ formatDate(userInfo.lastLoginTime) || '--' }}</span>
                </el-form-item>
              </el-form>
            </div>

            <!-- 我的帖子 -->
            <div v-show="activeTab === 'posts'" class="content-panel">
              <div class="panel-header">
                <h3>我的帖子</h3>
                <el-button type="primary" size="small" @click="$router.push('/community')">发布新帖</el-button>
              </div>

              <div class="post-list" v-loading="postLoading">
                <div class="post-item" v-for="post in userPosts" :key="post.id" @click="goToPostDetail(post.id)">
                  <div class="post-header">
                    <h4 class="post-title">{{ post.title }}</h4>
                    <div class="post-badges">
                      <el-tag v-if="post.isTop === 1" size="mini" type="danger" effect="dark">置顶</el-tag>
                      <el-tag v-if="post.isEssence === 1" size="mini" type="warning" effect="dark">精华</el-tag>
                    </div>
                  </div>
                  <p class="post-content">{{ truncateText(post.content, 100) }}</p>
                  <div class="post-footer">
                    <span><i class="el-icon-star-on"></i> {{ post.likeCount || 0 }}</span>
                    <span><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
                    <span><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
                    <span class="post-time">{{ formatDate(post.createTime) }}</span>
                    <el-button type="text" size="small" @click.stop="deletePost(post)" class="delete-btn">删除</el-button>
                  </div>
                </div>

                <div class="empty-state" v-if="!postLoading && userPosts.length === 0">
                  <i class="el-icon-document"></i>
                  <p>暂无帖子，快去发布第一条吧！</p>
                  <el-button type="primary" size="small" @click="$router.push('/community')">发布新帖</el-button>
                </div>

                <div class="pagination" v-if="postTotal > postPageSize">
                  <el-pagination
                      @current-change="handlePostPageChange"
                      :current-page="postPage"
                      :page-size="postPageSize"
                      layout="prev, pager, next"
                      :total="postTotal"
                      small
                  ></el-pagination>
                </div>
              </div>
            </div>

            <!-- 我的收藏 -->
            <div v-show="activeTab === 'favorites'" class="content-panel">
              <div class="panel-header">
                <h3>我的收藏</h3>
              </div>

              <div class="post-list" v-loading="favoriteLoading">
                <div class="post-item" v-for="post in favoritePosts" :key="post.id" @click="goToPostDetail(post.id)">
                  <div class="post-header">
                    <h4 class="post-title">{{ post.title }}</h4>
                  </div>
                  <p class="post-content">{{ truncateText(post.content, 100) }}</p>
                  <div class="post-footer">
                    <span><i class="el-icon-star-on"></i> {{ post.likeCount || 0 }}</span>
                    <span><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
                    <span><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
                    <span class="post-time">{{ formatDate(post.createTime) }}</span>
                    <el-button type="text" size="small" @click.stop="removeFavorite(post)" class="delete-btn">取消收藏</el-button>
                  </div>
                </div>

                <div class="empty-state" v-if="!favoriteLoading && favoritePosts.length === 0">
                  <i class="el-icon-star-on"></i>
                  <p>暂无收藏，去逛逛社区吧！</p>
                  <el-button type="primary" size="small" @click="$router.push('/community')">去社区</el-button>
                </div>

                <div class="pagination" v-if="favoriteTotal > favoritePageSize">
                  <el-pagination
                      @current-change="handleFavoritePageChange"
                      :current-page="favoritePage"
                      :page-size="favoritePageSize"
                      layout="prev, pager, next"
                      :total="favoriteTotal"
                      small
                  ></el-pagination>
                </div>
              </div>
            </div>

            <!-- 我的评论 -->
            <div v-show="activeTab === 'comments'" class="content-panel">
              <div class="panel-header">
                <h3>我的评论</h3>
              </div>

              <div class="comment-list" v-loading="commentLoading">
                <div class="comment-item" v-for="comment in userComments" :key="comment.id">
                  <div class="comment-header">
                    <span class="comment-target">评论了帖子：</span>
                    <span class="post-title-link" @click="goToPostDetail(comment.postId)">{{ comment.postTitle }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                  <div class="comment-footer">
                    <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                    <el-button type="text" size="small" @click="deleteComment(comment)" class="delete-btn">删除</el-button>
                  </div>
                </div>

                <div class="empty-state" v-if="!commentLoading && userComments.length === 0">
                  <i class="el-icon-chat-dot-round"></i>
                  <p>暂无评论，去参与讨论吧！</p>
                  <el-button type="primary" size="small" @click="$router.push('/community')">去社区</el-button>
                </div>

                <div class="pagination" v-if="commentTotal > commentPageSize">
                  <el-pagination
                      @current-change="handleCommentPageChange"
                      :current-page="commentPage"
                      :page-size="commentPageSize"
                      layout="prev, pager, next"
                      :total="commentTotal"
                      small
                  ></el-pagination>
                </div>
              </div>
            </div>

            <!-- 收货地址 -->
            <div v-show="activeTab === 'address'" class="content-panel">
              <div class="panel-header">
                <h3>收货地址</h3>
                <el-button type="primary" size="small" @click="showAddressDialog = true">新增地址</el-button>
              </div>

              <div class="address-list" v-loading="addressLoading">
                <div class="address-item" v-for="addr in addressList" :key="addr.id">
                  <div class="address-info">
                    <div class="address-name">
                      <span>{{ addr.receiverName }}</span>
                      <span class="phone">{{ addr.receiverPhone }}</span>
                      <el-tag v-if="addr.isDefault === 1" size="mini" type="success" effect="dark">默认</el-tag>
                    </div>
                    <div class="address-detail">
                      {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
                    </div>
                  </div>
                  <div class="address-actions">
                    <el-button type="text" size="small" @click="setDefaultAddress(addr.id)" v-if="addr.isDefault !== 1">设为默认</el-button>
                    <el-button type="text" size="small" @click="editAddress(addr)">编辑</el-button>
                    <el-button type="text" size="small" class="delete-btn" @click="deleteAddress(addr.id)">删除</el-button>
                  </div>
                </div>

                <div class="empty-state" v-if="!addressLoading && addressList.length === 0">
                  <i class="el-icon-location"></i>
                  <p>暂无收货地址，请添加</p>
                  <el-button type="primary" size="small" @click="showAddressDialog = true">新增地址</el-button>
                </div>
              </div>
            </div>

            <!-- 安全设置 -->
            <div v-show="activeTab === 'security'" class="content-panel">
              <div class="panel-header">
                <h3>安全设置</h3>
              </div>

              <div class="security-item">
                <div class="security-info">
                  <div class="security-title">登录密码</div>
                  <div class="security-desc">用于登录账号的密码</div>
                </div>
                <el-button type="primary" plain size="small" @click="showPasswordDialog = true">修改密码</el-button>
              </div>

              <div class="security-item">
                <div class="security-info">
                  <div class="security-title">绑定邮箱</div>
                  <div class="security-desc">{{ userInfo.email || '未绑定' }}</div>
                </div>
                <el-button type="primary" plain size="small" @click="showEmailDialog = true">修改邮箱</el-button>
              </div>

              <div class="security-item">
                <div class="security-info">
                  <div class="security-title">绑定手机</div>
                  <div class="security-desc">{{ userInfo.phone || '未绑定' }}</div>
                </div>
                <el-button type="primary" plain size="small" @click="showPhoneDialog = true">绑定手机</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog title="更换头像" :visible.sync="showAvatarUpload" width="400px" center>
      <div class="avatar-upload-area">
        <div class="avatar-preview">
          <el-avatar :size="120" :src="avatarPreview" class="preview-avatar">
            {{ !avatarPreview ? getUserInitial(userInfo.nickname || userInfo.username) : '' }}
          </el-avatar>
        </div>
        <div class="upload-btn-wrapper">
          <el-button type="primary" @click="triggerAvatarUpload">选择图片</el-button>
          <input
              type="file"
              ref="avatarInput"
              accept="image/jpeg,image/jpg,image/png"
              style="display: none"
              @change="handleAvatarUpload"
          />
        </div>
        <div class="upload-tip">支持 JPG、PNG 格式，≤2MB</div>
      </div>
      <span slot="footer">
        <el-button @click="showAvatarUpload = false">取消</el-button>
        <el-button type="primary" @click="saveAvatar" :loading="avatarLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" :visible.sync="showPasswordDialog" width="450px" center>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="passwordLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 修改邮箱对话框 -->
    <el-dialog title="修改邮箱" :visible.sync="showEmailDialog" width="450px" center>
      <el-form :model="emailForm" :rules="emailRules" ref="emailForm" label-width="100px">
        <el-form-item label="新邮箱" prop="email">
          <el-input v-model="emailForm.email" placeholder="请输入新邮箱"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-wrapper">
            <el-input v-model="emailForm.code" placeholder="请输入验证码"></el-input>
            <el-button :disabled="!canSendCode" @click="sendEmailCode" :loading="codeSending">{{ codeBtnText }}</el-button>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showEmailDialog = false">取消</el-button>
        <el-button type="primary" @click="changeEmail" :loading="emailLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 地址对话框 -->
    <el-dialog :title="addressDialogTitle" :visible.sync="showAddressDialog" width="500px" center>
      <el-form :model="addressForm" :rules="addressRules" ref="addressForm" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="地区" prop="province">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="addressForm.province" placeholder="省"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="addressForm.city" placeholder="市"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="addressForm.district" placeholder="区/县"></el-input>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressForm.detailAddress" type="textarea" rows="2" placeholder="街道、门牌号等"></el-input>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" :loading="addressSubmitLoading">确定</el-button>
      </span>
    </el-dialog>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getUserInfo, updateUserInfo, changePassword, updateEmail, sendEmailCode } from '@/api/user';
import { getUserPosts, deletePost, getFavorites, removeFavorite, getUserComments, deleteComment } from '@/api/community';
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address';
import { uploadAvatar } from '@/api/upload';

export default {
  name: 'PersonalCenter',
  components: { Navbar, Footer },
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

    return {
      activeTab: 'profile',
      isEditing: false,
      saveLoading: false,
      avatarLoading: false,
      passwordLoading: false,
      emailLoading: false,
      codeSending: false,
      addressSubmitLoading: false,
      countdown: 60,
      timer: null,

      userInfo: {
        id: null,
        username: '',
        nickname: '',
        email: '',
        phone: '',
        avatar: '',
        role: 1,
        createTime: '',
        lastLoginTime: ''
      },

      profileForm: {
        username: '',
        nickname: '',
        email: '',
        phone: ''
      },

      profileRules: {
        nickname: [
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      },

      // 帖子相关
      postLoading: false,
      userPosts: [],
      postTotal: 0,
      postPage: 1,
      postPageSize: 10,

      // 收藏相关
      favoriteLoading: false,
      favoritePosts: [],
      favoriteTotal: 0,
      favoritePage: 1,
      favoritePageSize: 10,

      // 评论相关
      commentLoading: false,
      userComments: [],
      commentTotal: 0,
      commentPage: 1,
      commentPageSize: 10,

      // 地址相关
      addressLoading: false,
      addressList: [],
      showAddressDialog: false,
      isEditAddress: false,
      addressForm: {
        id: null,
        receiverName: '',
        receiverPhone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: 0
      },
      addressRules: {
        receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
        receiverPhone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
        city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
        district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
        detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
      },

      // 头像上传
      showAvatarUpload: false,
      avatarPreview: '',
      newAvatar: '',

      // 密码修改
      showPasswordDialog: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },

      // 邮箱修改
      showEmailDialog: false,
      emailForm: {
        email: '',
        code: ''
      },
      emailRules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码为6位数字', trigger: 'blur' }
        ]
      },

      // 手机修改
      showPhoneDialog: false
    };
  },
  computed: {
    canSendCode() {
      return this.emailForm.email &&
          !this.codeSending &&
          this.countdown === 60 &&
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.emailForm.email);
    },
    codeBtnText() {
      if (this.countdown < 60) {
        return `${this.countdown}秒后重试`;
      }
      if (this.codeSending) {
        return '发送中...';
      }
      return '获取验证码';
    },
    addressDialogTitle() {
      return this.isEditAddress ? '编辑地址' : '新增地址';
    }
  },
  created() {
    this.loadUserInfo();
    this.loadAddressList();
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    // ========== 用户信息 ==========
    loadUserInfo() {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        this.userInfo = JSON.parse(userInfo);
        this.profileForm = {
          username: this.userInfo.username,
          nickname: this.userInfo.nickname || '',
          email: this.userInfo.email || '',
          phone: this.userInfo.phone || ''
        };
      }
    },

    getUserRoleName(role) {
      if (role === 1) return '普通用户';
      if (role === 2) return '员工';
      if (role === 3) return '管理员';
      return '用户';
    },

    getAvatarColor(name) {
      if (!name) return 'linear-gradient(135deg, #667eea, #764ba2)';
      const colors = [
        'linear-gradient(135deg, #667eea, #764ba2)',
        'linear-gradient(135deg, #f093fb, #f5576c)',
        'linear-gradient(135deg, #4facfe, #00f2fe)',
        'linear-gradient(135deg, #43e97b, #38f9d7)',
        'linear-gradient(135deg, #fa709a, #fee140)',
        'linear-gradient(135deg, #a18cd1, #fbc2eb)'
      ];
      let index = 0;
      for (let i = 0; i < name.length; i++) {
        index += name.charCodeAt(i);
      }
      return colors[index % colors.length];
    },

    getUserInitial(name) {
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
    },

    editProfile() {
      this.isEditing = true;
    },

    cancelEdit() {
      this.isEditing = false;
      this.profileForm = {
        username: this.userInfo.username,
        nickname: this.userInfo.nickname || '',
        email: this.userInfo.email || '',
        phone: this.userInfo.phone || ''
      };
    },

    async saveProfile() {
      this.$refs.profileForm.validate(async (valid) => {
        if (!valid) return;

        this.saveLoading = true;
        try {
          const res = await updateUserInfo(this.profileForm);
          if (res.code === 200) {
            this.$message.success('保存成功');
            this.userInfo = { ...this.userInfo, ...this.profileForm };
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
            this.isEditing = false;
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('保存失败');
        } finally {
          this.saveLoading = false;
        }
      });
    },

    // ========== 头像上传 ==========
    triggerAvatarUpload() {
      this.$refs.avatarInput.click();
    },

    handleAvatarUpload(event) {
      const file = event.target.files[0];
      if (!file) return;

      if (!file.type.startsWith('image/')) {
        this.$message.error('只支持图片格式');
        return;
      }
      if (file.size > 2 * 1024 * 1024) {
        this.$message.error('图片不能超过2MB');
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        this.avatarPreview = e.target.result;
      };
      reader.readAsDataURL(file);
      this.newAvatar = file;
    },

    async saveAvatar() {
      if (!this.newAvatar) {
        this.showAvatarUpload = false;
        return;
      }

      this.avatarLoading = true;
      const formData = new FormData();
      formData.append('file', this.newAvatar);

      try {
        const res = await uploadAvatar(formData);
        if (res.code === 200) {
          this.userInfo.avatar = res.data.url;
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
          this.$message.success('头像更新成功');
          this.showAvatarUpload = false;
          this.avatarPreview = '';
          this.newAvatar = '';
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('上传失败');
      } finally {
        this.avatarLoading = false;
      }
    },

    // ========== 密码修改 ==========
    async changePassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (!valid) return;

        this.passwordLoading = true;
        try {
          const res = await changePassword({
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          });
          if (res.code === 200) {
            this.$message.success('密码修改成功，请重新登录');
            setTimeout(() => {
              localStorage.removeItem('token');
              localStorage.removeItem('userInfo');
              this.$router.push('/login');
            }, 1500);
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('修改失败');
        } finally {
          this.passwordLoading = false;
        }
      });
    },

    // ========== 邮箱修改 ==========
    sendEmailCode() {
      this.$refs.emailForm.validateField('email', async (error) => {
        if (error) {
          this.$message.warning('请先填写正确的邮箱地址');
          return;
        }

        this.codeSending = true;
        try {
          const res = await sendEmailCode(this.emailForm.email);
          if (res.code === 200) {
            this.$message.success('验证码已发送');
            this.startCountdown();
          } else {
            this.$message.error(res.message);
            this.codeSending = false;
          }
        } catch (error) {
          this.$message.error('发送失败');
          this.codeSending = false;
        }
      });
    },

    startCountdown() {
      this.countdown = 60;
      this.codeSending = true;

      if (this.timer) clearInterval(this.timer);
      this.timer = setInterval(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          clearInterval(this.timer);
          this.timer = null;
          this.countdown = 60;
          this.codeSending = false;
        }
      }, 1000);
    },

    async changeEmail() {
      this.$refs.emailForm.validate(async (valid) => {
        if (!valid) return;

        this.emailLoading = true;
        try {
          const res = await updateEmail(this.emailForm);
          if (res.code === 200) {
            this.$message.success('邮箱修改成功');
            this.userInfo.email = this.emailForm.email;
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
            this.showEmailDialog = false;
            this.emailForm = { email: '', code: '' };
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('修改失败');
        } finally {
          this.emailLoading = false;
        }
      });
    },

    // ========== 我的帖子 ==========
    async loadUserPosts() {
      this.postLoading = true;
      try {
        const res = await getUserPosts(this.postPage, this.postPageSize);
        if (res.code === 200) {
          this.userPosts = res.data.list || [];
          this.postTotal = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载帖子失败', error);
      } finally {
        this.postLoading = false;
      }
    },

    handlePostPageChange(page) {
      this.postPage = page;
      this.loadUserPosts();
    },

    async deletePost(post) {
      this.$confirm(`确定要删除帖子"${post.title}"吗？`, '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deletePost(post.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadUserPosts();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    // ========== 我的收藏 ==========
    async loadFavorites() {
      this.favoriteLoading = true;
      try {
        const res = await getFavorites(this.favoritePage, this.favoritePageSize);
        if (res.code === 200) {
          this.favoritePosts = res.data.list || [];
          this.favoriteTotal = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载收藏失败', error);
      } finally {
        this.favoriteLoading = false;
      }
    },

    handleFavoritePageChange(page) {
      this.favoritePage = page;
      this.loadFavorites();
    },

    async removeFavorite(post) {
      try {
        const res = await removeFavorite(post.id);
        if (res.code === 200) {
          this.$message.success('已取消收藏');
          this.loadFavorites();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    // ========== 我的评论 ==========
    async loadUserComments() {
      this.commentLoading = true;
      try {
        const res = await getUserComments(this.commentPage, this.commentPageSize);
        if (res.code === 200) {
          this.userComments = res.data.list || [];
          this.commentTotal = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载评论失败', error);
      } finally {
        this.commentLoading = false;
      }
    },

    handleCommentPageChange(page) {
      this.commentPage = page;
      this.loadUserComments();
    },

    async deleteComment(comment) {
      this.$confirm('确定要删除这条评论吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deleteComment(comment.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadUserComments();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    // ========== 收货地址 ==========
    async loadAddressList() {
      this.addressLoading = true;
      try {
        const res = await getAddressList();
        if (res.code === 200) {
          this.addressList = res.data || [];
        }
      } catch (error) {
        console.error('加载地址失败', error);
      } finally {
        this.addressLoading = false;
      }
    },

    editAddress(addr) {
      this.isEditAddress = true;
      this.addressForm = {
        id: addr.id,
        receiverName: addr.receiverName,
        receiverPhone: addr.receiverPhone,
        province: addr.province,
        city: addr.city,
        district: addr.district,
        detailAddress: addr.detailAddress,
        isDefault: addr.isDefault
      };
      this.showAddressDialog = true;
    },

    async saveAddress() {
      this.$refs.addressForm.validate(async (valid) => {
        if (!valid) return;

        this.addressSubmitLoading = true;
        try {
          let res;
          if (this.isEditAddress) {
            res = await updateAddress(this.addressForm);
          } else {
            res = await addAddress(this.addressForm);
          }

          if (res.code === 200) {
            this.$message.success(this.isEditAddress ? '更新成功' : '添加成功');
            this.showAddressDialog = false;
            this.loadAddressList();
            this.resetAddressForm();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error(this.isEditAddress ? '更新失败' : '添加失败');
        } finally {
          this.addressSubmitLoading = false;
        }
      });
    },

    resetAddressForm() {
      this.addressForm = {
        id: null,
        receiverName: '',
        receiverPhone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: 0
      };
      this.isEditAddress = false;
    },

    async setDefaultAddress(id) {
      try {
        const res = await setDefaultAddress(id);
        if (res.code === 200) {
          this.$message.success('设置成功');
          this.loadAddressList();
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('设置失败');
      }
    },

    async deleteAddress(id) {
      this.$confirm('确定要删除该地址吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deleteAddress(id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadAddressList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    // ========== 通用方法 ==========
    handleMenuSelect(index) {
      this.activeTab = index;
      if (index === 'posts') {
        this.loadUserPosts();
      } else if (index === 'favorites') {
        this.loadFavorites();
      } else if (index === 'comments') {
        this.loadUserComments();
      } else if (index === 'address') {
        this.loadAddressList();
      }
    },

    goToPostDetail(id) {
      this.$router.push(`/community/post/${id}`);
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
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
.personal-center {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.center-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.page-desc {
  color: #999;
}

.center-layout {
  display: flex;
  gap: 30px;
}

/* 左侧边栏 */
.center-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.user-card {
  background: white;
  border-radius: 20px;
  padding: 30px 20px;
  text-align: center;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.user-avatar-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 15px;
  cursor: pointer;
}

.user-avatar {
  width: 100%;
  height: 100%;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.user-avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}

.avatar-mask i {
  font-size: 20px;
  margin-bottom: 4px;
}

.avatar-mask span {
  font-size: 12px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.user-role {
  font-size: 13px;
  color: #999;
}

.sidebar-menu {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.sidebar-menu ::v-deep .el-menu-item {
  height: 50px;
  line-height: 50px;
}

.sidebar-menu ::v-deep .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

/* 右侧内容区 */
.center-content-area {
  flex: 1;
}

.content-panel {
  background: white;
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  min-height: 500px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h3 {
  font-size: 18px;
  color: #333;
}

/* 表单样式 */
.profile-form {
  max-width: 500px;
}

/* 帖子列表 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-item {
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.post-item:hover {
  background: #fafbfc;
  border-color: #667eea;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.post-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.post-content {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.post-footer {
  display: flex;
  gap: 20px;
  align-items: center;
  font-size: 13px;
  color: #999;
}

.post-footer i {
  margin-right: 4px;
}

.post-time {
  margin-left: auto;
}

.delete-btn {
  color: #f56c6c;
}

/* 评论列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.comment-item {
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
}

.comment-header {
  margin-bottom: 10px;
}

.comment-target {
  color: #999;
  font-size: 13px;
}

.post-title-link {
  color: #667eea;
  cursor: pointer;
}

.post-title-link:hover {
  text-decoration: underline;
}

.comment-content {
  color: #333;
  line-height: 1.6;
  margin-bottom: 10px;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
}

.address-name {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.address-name .phone {
  color: #999;
  font-size: 13px;
}

.address-detail {
  color: #666;
  font-size: 14px;
}

.address-actions {
  display: flex;
  gap: 10px;
}

/* 安全设置 */
.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.security-item:last-child {
  border-bottom: none;
}

.security-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.security-desc {
  font-size: 13px;
  color: #999;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
}

/* 分页 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 头像上传 */
.avatar-upload-area {
  text-align: center;
}

.avatar-preview {
  margin-bottom: 20px;
}

.preview-avatar {
  margin: 0 auto;
}

.upload-btn-wrapper {
  margin-bottom: 10px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
}

/* 验证码 */
.code-wrapper {
  display: flex;
  gap: 10px;
}

.code-wrapper .el-input {
  flex: 1;
}

.code-wrapper .el-button {
  width: 100px;
}

/* 响应式 */
@media (max-width: 768px) {
  .center-layout {
    flex-direction: column;
  }

  .center-sidebar {
    width: 100%;
  }

  .address-item {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }

  .post-footer {
    flex-wrap: wrap;
  }
}
</style>