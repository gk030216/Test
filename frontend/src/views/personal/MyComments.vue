<template>
  <div class="my-comments-page">
    <h2 class="page-title">我的评论</h2>

    <!-- 筛选栏：评论类型切换 + 搜索框 同一行 -->
    <div class="filters-bar">
      <div class="comment-tabs">
        <div
            class="tab-item"
            :class="{ active: commentType === 'all' }"
            @click="switchCommentType('all')"
        >
          <i class="el-icon-menu"></i> 全部评论
        </div>
        <div
            class="tab-item"
            :class="{ active: commentType === 'service' }"
            @click="switchCommentType('service')"
        >
          <i class="el-icon-service"></i> 服务评价
        </div>
        <div
            class="tab-item"
            :class="{ active: commentType === 'product' }"
            @click="switchCommentType('product')"
        >
          <i class="el-icon-goods"></i> 商品评价
        </div>
        <div
            class="tab-item"
            :class="{ active: commentType === 'post' }"
            @click="switchCommentType('post')"
        >
          <i class="el-icon-document"></i> 帖子评论
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="search-wrapper">
        <i class="el-icon-search search-icon"></i>
        <el-input
            v-model="searchKeyword"
            placeholder="搜索评论内容"
            size="medium"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
            class="search-input"
        ></el-input>
      </div>
    </div>

    <!-- 全部评论列表 -->
    <div class="comments-grid" v-loading="allLoading" v-if="commentType === 'all'">
      <template v-if="filteredAllList.length > 0">
        <div class="comment-card" v-for="comment in filteredAllList" :key="comment.id">
          <div class="comment-content-wrapper" @click="viewCommentDetail(comment)">
            <div class="section-header">
              <div class="section-author">
                <el-avatar :size="32" :src="comment.authorAvatar" class="author-avatar">
                  {{ getAuthorInitial(comment.authorName) }}
                </el-avatar>
                <div class="author-info">
                  <!-- 帖子评论列表中的作者名称 -->
                  <span class="author-name">{{ comment.authorName || '匿名用户' }}</span>
                  <span class="section-tag">
                    {{ comment.type === 'post' ? '评论了帖子' : (comment.type === 'service' ? '评价了服务' : '评价了商品') }}
                  </span>
                </div>
              </div>
              <span class="section-time">{{ formatRelativeTime(comment.createTime) }}</span>
            </div>
            <div class="target-info">
              <h4 class="target-title">{{ comment.targetTitle || comment.targetName || '无标题' }}</h4>
              <p class="target-content">{{ truncateText(comment.targetContent || comment.content, 60) }}</p>
            </div>
            <div class="my-comment-preview">
              <div class="comment-label">
                <i class="el-icon-chat-line-round"></i>
                <span>我的评论</span>
              </div>
              <div class="comment-preview-text">{{ truncateText(comment.content, 50) }}</div>
            </div>
          </div>

          <div class="comment-actions" @click.stop>
            <el-button size="small" plain @click="viewTarget(comment)">
              <i class="el-icon-view"></i> 查看{{ comment.type === 'post' ? '帖子' : (comment.type === 'service' ? '服务' : '商品') }}
            </el-button>
            <el-button size="small" type="danger" plain @click="deleteComment(comment)">删除</el-button>
          </div>
        </div>
      </template>

      <div class="empty-state" v-if="!allLoading && filteredAllList.length === 0">
        <i class="el-icon-chat-dot-round"></i>
        <p>{{ searchKeyword ? '没有找到相关评论' : '暂无评论' }}</p>
        <el-button type="primary" size="small" @click="$router.push('/community')">去逛逛</el-button>
      </div>

      <div class="pagination" v-if="allTotal > allPageSize">
        <el-pagination
            @current-change="handleAllPageChange"
            :current-page="allPage"
            :page-size="allPageSize"
            layout="prev, pager, next"
            :total="allTotal"
            background
        />
      </div>
    </div>

    <!-- 帖子评论列表 -->
    <div class="comments-grid" v-loading="postLoading" v-if="commentType === 'post'">
      <template v-if="filteredPostList.length > 0">
        <div class="comment-card" v-for="comment in filteredPostList" :key="comment.id">
          <div class="comment-content-wrapper" @click="viewPostCommentDetail(comment)">
            <div class="section-header">
              <div class="section-author">
                <el-avatar :size="32" :src="comment.postAuthorAvatar" class="author-avatar">
                  {{ getAuthorInitial(comment.postAuthorName) }}
                </el-avatar>
                <div class="author-info">
                  <span class="author-name">{{ comment.postAuthorName || '匿名用户' }}</span>
                  <span class="section-tag">的原帖</span>
                </div>
              </div>
              <span class="section-time">{{ formatRelativeTime(comment.postCreateTime) }}</span>
            </div>
            <div class="target-info">
              <h4 class="target-title">{{ comment.postTitle || '无标题' }}</h4>
              <p class="target-content">{{ truncateText(comment.postContent, 60) }}</p>
            </div>
            <div class="my-comment-preview">
              <div class="comment-label">
                <i class="el-icon-chat-line-round"></i>
                <span>我的评论</span>
              </div>
              <div class="comment-preview-text">{{ truncateText(comment.content, 50) }}</div>
            </div>
          </div>

          <div class="comment-actions" @click.stop>
            <el-button size="small" plain @click="goToPostDetail(comment.postId)">
              <i class="el-icon-view"></i> 查看帖子
            </el-button>
            <el-button size="small" type="danger" plain @click="deletePostComment(comment)">删除</el-button>
          </div>
        </div>
      </template>

      <div class="empty-state" v-if="!postLoading && filteredPostList.length === 0">
        <i class="el-icon-document"></i>
        <p>{{ searchKeyword ? '没有找到相关帖子评论' : '暂无帖子评论' }}</p>
        <el-button type="primary" size="small" @click="$router.push('/community')">去逛逛</el-button>
      </div>

      <div class="pagination" v-if="postTotal > postPageSize">
        <el-pagination
            @current-change="handlePostPageChange"
            :current-page="postPage"
            :page-size="postPageSize"
            layout="prev, pager, next"
            :total="postTotal"
            background
        />
      </div>
    </div>

    <!-- 服务评价列表 -->
    <div class="comments-grid" v-loading="serviceLoading" v-if="commentType === 'service'">
      <template v-if="filteredServiceList.length > 0">
        <div class="comment-card" v-for="comment in filteredServiceList" :key="comment.id">
          <div class="comment-content-wrapper" @click="viewServiceCommentDetail(comment)">
            <div class="section-header">
              <div class="section-author">
                <div class="service-avatar-small" v-if="comment.serviceImage">
                  <img :src="comment.serviceImage" :alt="comment.serviceName">
                </div>
                <div class="service-avatar-placeholder-small" v-else>
                  <i class="el-icon-service"></i>
                </div>
                <div class="author-info">
                  <span class="author-name">{{ comment.serviceName }}</span>
                  <span class="section-tag">的服务</span>
                </div>
              </div>
              <span class="section-time">{{ formatRelativeTime(comment.createTime) }}</span>
            </div>
            <div class="target-info">
              <div class="target-meta">
                <span class="target-price">¥{{ comment.servicePrice || 0 }}</span>
                <el-rate v-model="comment.rating" disabled :size="12" class="target-rate"></el-rate>
              </div>
            </div>
            <div class="my-comment-preview">
              <div class="comment-label">
                <i class="el-icon-star-on"></i>
                <span>我的评价</span>
              </div>
              <div class="comment-preview-text">{{ truncateText(comment.content, 50) }}</div>
            </div>
          </div>

          <div class="comment-actions" @click.stop>
            <el-button size="small" plain @click="goToServiceDetail(comment.serviceId)">
              <i class="el-icon-view"></i> 查看服务
            </el-button>
            <el-button size="small" type="danger" plain @click="deleteServiceComment(comment)">删除</el-button>
          </div>
        </div>
      </template>

      <div class="empty-state" v-if="!serviceLoading && filteredServiceList.length === 0">
        <i class="el-icon-service"></i>
        <p>{{ searchKeyword ? '没有找到相关服务评价' : '暂无服务评价' }}</p>
        <el-button type="primary" size="small" @click="$router.push('/services')">去预约服务</el-button>
      </div>

      <div class="pagination" v-if="serviceTotal > servicePageSize">
        <el-pagination
            @current-change="handleServicePageChange"
            :current-page="servicePage"
            :page-size="servicePageSize"
            layout="prev, pager, next"
            :total="serviceTotal"
            background
        />
      </div>
    </div>

    <!-- 商品评价列表 -->
    <div class="comments-grid" v-loading="productLoading" v-if="commentType === 'product'">
      <template v-if="filteredProductList.length > 0">
        <div class="comment-card" v-for="comment in filteredProductList" :key="comment.id">
          <div class="comment-content-wrapper" @click="viewProductCommentDetail(comment)">
            <div class="section-header">
              <div class="section-author">
                <div class="product-avatar-small" v-if="comment.productImage">
                  <img :src="comment.productImage" :alt="comment.productName">
                </div>
                <div class="product-avatar-placeholder-small" v-else>
                  <i class="el-icon-goods"></i>
                </div>
                <div class="author-info">
                  <span class="author-name">{{ comment.productName }}</span>
                  <span class="section-tag">的商品</span>
                </div>
              </div>
              <span class="section-time">{{ formatRelativeTime(comment.createTime) }}</span>
            </div>
            <div class="target-info">
              <div class="target-meta">
                <span class="target-price">¥{{ comment.productPrice || 0 }}</span>
                <el-rate v-model="comment.rating" disabled :size="12" class="target-rate"></el-rate>
              </div>
            </div>
            <div class="my-comment-preview">
              <div class="comment-label">
                <i class="el-icon-star-on"></i>
                <span>我的评价</span>
              </div>
              <div class="comment-preview-text">{{ truncateText(comment.content, 50) }}</div>
            </div>
          </div>

          <div class="comment-actions" @click.stop>
            <el-button size="small" plain @click="goToProductDetail(comment.productId)">
              <i class="el-icon-view"></i> 查看商品
            </el-button>
            <el-button size="small" type="danger" plain @click="deleteProductComment(comment)">删除</el-button>
          </div>
        </div>
      </template>

      <div class="empty-state" v-if="!productLoading && filteredProductList.length === 0">
        <i class="el-icon-goods"></i>
        <p>{{ searchKeyword ? '没有找到相关商品评价' : '暂无商品评价' }}</p>
        <el-button type="primary" size="small" @click="$router.push('/shop')">去逛逛</el-button>
      </div>

      <div class="pagination" v-if="productTotal > productPageSize">
        <el-pagination
            @current-change="handleProductPageChange"
            :current-page="productPage"
            :page-size="productPageSize"
            layout="prev, pager, next"
            :total="productTotal"
            background
        />
      </div>
    </div>

    <!-- ========== 详情模态框 ========== -->

    <!-- 帖子评论详情模态框 -->
    <el-dialog title="评论详情" :visible.sync="postDetailVisible" width="600px" center class="detail-dialog">
      <div class="detail-content" v-if="currentPostComment">
        <div class="detail-section">
          <div class="detail-label">原帖信息</div>
          <div class="post-detail-card" @click="goToPostDetail(currentPostComment.postId)">
            <div class="post-detail-header">
              <el-avatar :size="28" :src="currentPostComment.postAuthorAvatar" class="detail-avatar">
                {{ getAuthorInitial(currentPostComment.postAuthorName) }}
              </el-avatar>
              <span class="post-detail-author">{{ currentPostComment.postAuthorName || '匿名用户' }}</span>
              <span class="post-detail-time">{{ formatRelativeTime(currentPostComment.postCreateTime) }}</span>
            </div>
            <h4 class="post-detail-title">{{ currentPostComment.postTitle || '无标题' }}</h4>
            <p class="post-detail-content">{{ currentPostComment.postContent }}</p>
          </div>
        </div>

        <div class="detail-section" v-if="currentPostComment.parentId !== 0 && currentPostComment.parentId !== null">
          <div class="detail-label">回复的评论</div>
          <div class="parent-detail-card">
            <div class="parent-detail-header">
              <i class="el-icon-chat-dot-round"></i>
              <span class="parent-detail-author">@{{ currentPostComment.parentCommentAuthorName || currentPostComment.replyToUserName || '用户' }}</span>
            </div>
            <div class="parent-detail-content">{{ currentPostComment.parentCommentContent || '原评论内容已不可见' }}</div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-label">我的评论</div>
          <div class="my-detail-card">
            <div class="my-detail-content">{{ currentPostComment.content }}</div>
          </div>
        </div>

        <div class="detail-section" v-if="currentPostComment.reply">
          <div class="detail-label">作者回复</div>
          <div class="reply-detail-card">
            <div class="reply-detail-header">
              <i class="el-icon-chat-dot-round"></i>
              <span class="reply-detail-author">{{ currentPostComment.postAuthorName || '作者' }}</span>
            </div>
            <div class="reply-detail-content">{{ currentPostComment.reply }}</div>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="postDetailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 服务评价详情模态框 -->
    <el-dialog title="评价详情" :visible.sync="serviceDetailVisible" width="600px" center class="detail-dialog">
      <div class="detail-content" v-if="currentServiceComment">
        <div class="detail-section">
          <div class="detail-label">服务信息</div>
          <div class="service-detail-card" @click="goToServiceDetail(currentServiceComment.serviceId)">
            <div class="service-detail-header">
              <div class="service-detail-avatar" v-if="currentServiceComment.serviceImage">
                <img :src="currentServiceComment.serviceImage" :alt="currentServiceComment.serviceName">
              </div>
              <div class="service-detail-avatar-placeholder" v-else>
                <i class="el-icon-service"></i>
              </div>
              <div class="service-detail-info">
                <h4 class="service-detail-name">{{ currentServiceComment.serviceName }}</h4>
                <div class="service-detail-meta">
                  <span class="service-detail-price">¥{{ currentServiceComment.servicePrice || 0 }}</span>
                  <el-rate v-model="currentServiceComment.rating" disabled :size="12"></el-rate>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-label">我的评价</div>
          <div class="my-detail-card">
            <div class="my-detail-content">{{ currentServiceComment.content }}</div>
            <div class="detail-images" v-if="currentServiceComment.images">
              <el-image v-for="(img, idx) in currentServiceComment.images.split(',')" :key="idx" :src="img" fit="cover" class="detail-img" :preview-src-list="currentServiceComment.images.split(',')"></el-image>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentServiceComment.reply">
          <div class="detail-label">商家回复</div>
          <div class="reply-detail-card">
            <div class="reply-detail-content">{{ currentServiceComment.reply }}</div>
            <div class="reply-detail-time" v-if="currentServiceComment.replyTime">{{ formatDate(currentServiceComment.replyTime) }}</div>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="serviceDetailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 商品评价详情模态框 -->
    <el-dialog title="评价详情" :visible.sync="productDetailVisible" width="600px" center class="detail-dialog">
      <div class="detail-content" v-if="currentProductComment">
        <div class="detail-section">
          <div class="detail-label">商品信息</div>
          <div class="product-detail-card" @click="goToProductDetail(currentProductComment.productId)">
            <div class="product-detail-header">
              <div class="product-detail-avatar" v-if="currentProductComment.productImage">
                <img :src="currentProductComment.productImage" :alt="currentProductComment.productName">
              </div>
              <div class="product-detail-avatar-placeholder" v-else>
                <i class="el-icon-goods"></i>
              </div>
              <div class="product-detail-info">
                <h4 class="product-detail-name">{{ currentProductComment.productName }}</h4>
                <div class="product-detail-meta">
                  <span class="product-detail-price">¥{{ currentProductComment.productPrice || 0 }}</span>
                  <el-rate v-model="currentProductComment.rating" disabled :size="12"></el-rate>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-label">我的评价</div>
          <div class="my-detail-card">
            <div class="my-detail-time">{{ formatDate(currentProductComment.createTime) }}</div>
            <div class="my-detail-content">{{ currentProductComment.content }}</div>
            <div class="detail-images" v-if="currentProductComment.images">
              <el-image v-for="(img, idx) in currentProductComment.images.split(',')" :key="idx" :src="img" fit="cover" class="detail-img" :preview-src-list="currentProductComment.images.split(',')"></el-image>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentProductComment.reply">
          <div class="detail-label">商家回复</div>
          <div class="reply-detail-card">
            <div class="reply-detail-content">{{ currentProductComment.reply }}</div>
            <div class="reply-detail-time" v-if="currentProductComment.replyTime">{{ formatDate(currentProductComment.replyTime) }}</div>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="productDetailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getUserComments, adminDeleteComment } from '@/api/community';
import { getUserServiceComments, deleteServiceComment } from '@/api/service';
import { getUserProductComments, deleteComment as deleteProductCommentApi } from '@/api/comment';

export default {
  name: 'MyComments',
  data() {
    return {
      allRawList: [],
      // 搜索关键词
      searchKeyword: '',

      commentType: 'all',

      // 全部评论
      allLoading: false,
      allCommentsList: [],
      allTotal: 0,
      allPage: 1,
      allPageSize: 9,

      // 帖子评论
      postLoading: false,
      postCommentsList: [],
      postTotal: 0,
      postPage: 1,
      postPageSize: 9,

      // 服务评价
      serviceLoading: false,
      serviceCommentsList: [],
      serviceTotal: 0,
      servicePage: 1,
      servicePageSize: 9,

      // 商品评价
      productLoading: false,
      productCommentsList: [],
      productTotal: 0,
      productPage: 1,
      productPageSize: 9,

      // 详情模态框
      postDetailVisible: false,
      serviceDetailVisible: false,
      productDetailVisible: false,
      currentPostComment: null,
      currentServiceComment: null,
      currentProductComment: null
    };
  },
  computed: {
    // 全部评论过滤
    filteredAllList() {
      // 从缓存的全量数据中过滤
      let filtered = this.allRawList;

      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        filtered = this.allRawList.filter(comment => {
          const title = (comment.targetTitle || comment.targetName || '').toLowerCase();
          const content = (comment.content || '').toLowerCase();
          return title.includes(keyword) || content.includes(keyword);
        });
      }

      // 更新总数（用于分页）
      this.allTotal = filtered.length;

      // 前端分页
      const start = (this.allPage - 1) * this.allPageSize;
      const end = start + this.allPageSize;
      return filtered.slice(start, end);
    },
    // 帖子评论过滤
    filteredPostList() {
      if (!this.searchKeyword) return this.postCommentsList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.postCommentsList.filter(comment => {
        return (comment.postTitle || '').toLowerCase().includes(keyword) ||
            (comment.content || '').toLowerCase().includes(keyword);
      });
    },
    // 服务评价过滤
    filteredServiceList() {
      if (!this.searchKeyword) return this.serviceCommentsList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.serviceCommentsList.filter(comment => {
        return (comment.serviceName || '').toLowerCase().includes(keyword) ||
            (comment.content || '').toLowerCase().includes(keyword);
      });
    },
    // 商品评价过滤
    filteredProductList() {
      if (!this.searchKeyword) return this.productCommentsList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.productCommentsList.filter(comment => {
        return (comment.productName || '').toLowerCase().includes(keyword) ||
            (comment.content || '').toLowerCase().includes(keyword);
      });
    }
  },
  created() {
    this.loadAllComments();
  },
  methods: {
    switchCommentType(type) {
      this.commentType = type;
      this.searchKeyword = '';
      // 切换类型时清空全部评论缓存
      this.allRawList = [];
      this.allPage = 1;
      if (type === 'all') {
        this.loadAllComments();
      } else if (type === 'post') {
        this.loadPostComments();
      } else if (type === 'service') {
        this.loadServiceComments();
      } else if (type === 'product') {
        this.loadProductComments();
      }
    },

    handleSearch() {
      // 搜索逻辑由计算属性自动处理
    },

    // ========== 全部评论 ==========
    async loadAllComments() {
      this.allLoading = true;
      try {
        if (this.allPage === 1 || this.allRawList.length === 0) {
          const [postRes, serviceRes, productRes] = await Promise.all([
            getUserComments(1, 100).catch(() => ({ code: 200, data: { list: [] } })),
            getUserServiceComments({ page: 1, pageSize: 100 }).catch(() => ({ code: 200, data: { list: [] } })),
            getUserProductComments({ page: 1, pageSize: 100 }).catch(() => ({ code: 200, data: { list: [] } }))
          ]);

          const postList = (postRes.code === 200 ? (postRes.data.list || []) : [])
              .filter(item => item.status === 1)
              .map(item => ({
                ...item,
                type: 'post',
                authorName: item.userNickname || item.userName,
                authorAvatar: item.userAvatar,
                targetId: item.postId,
                targetTitle: item.postTitle,
                targetContent: item.postContent
              }));

          const serviceList = (serviceRes.code === 200 ? (serviceRes.data.list || []) : [])
              .filter(item => item.status === 1)
              .map(item => ({
                ...item,
                type: 'service',
                authorName: item.userNickname || item.userName,  // ✅ 添加
                authorAvatar: item.userAvatar,                   // ✅ 添加
                targetId: item.serviceId,
                targetName: item.serviceName,
                targetPrice: item.servicePrice
              }));

          const productList = (productRes.code === 200 ? (productRes.data.list || []) : [])
              .filter(item => item.status === 1 && item.productName && item.productName.trim() !== '')
              .map(item => ({
                ...item,
                type: 'product',
                authorName: item.userNickname || item.userName,  // ✅ 添加
                authorAvatar: item.userAvatar,                   // ✅ 添加
                targetId: item.productId,
                targetName: item.productName,
                targetPrice: item.productPrice
              }));

          let allList = [...postList, ...serviceList, ...productList];
          allList.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));

          this.allRawList = allList;
        }
      } catch (error) {
        console.error('加载全部评论失败', error);
        this.allRawList = [];
        this.allTotal = 0;
      } finally {
        this.allLoading = false;
      }
    },

    // ========== 帖子评论 ==========
    async loadPostComments() {
      this.postLoading = true;
      try {
        const res = await getUserComments(this.postPage, this.postPageSize);
        if (res.code === 200) {
          // ✅ 添加映射，确保使用昵称
          this.postCommentsList = (res.data.list || [])
              .filter(item => item.status === 1)
              .map(item => ({
                ...item,
                authorName: item.userNickname || item.userName,  // 使用昵称
                authorAvatar: item.userAvatar
              }));
          this.postTotal = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载帖子评论失败', error);
        this.$message.error('加载失败');
      } finally {
        this.postLoading = false;
      }
    },

    async deletePostComment(comment) {
      this.$confirm('确定要删除这条评论吗？', '提示', {type: 'warning'})
          .then(async () => {
            const res = await adminDeleteComment(comment.id);
            if (res.code === 200) {
              this.$message.success('删除成功');
              if (this.commentType === 'all') {
                // 清空缓存，重新加载
                this.allRawList = [];
                this.allPage = 1;
                this.loadAllComments();
              } else {
                this.loadPostComments();
              }
            } else {
              this.$message.error(res.message || '删除失败');
            }
          })
          .catch(() => {});
    },

    viewPostCommentDetail(comment) {
      this.currentPostComment = comment;
      this.postDetailVisible = true;
    },

    // ========== 服务评价 ==========
    async loadServiceComments() {
      this.serviceLoading = true;
      try {
        const res = await getUserServiceComments({page: this.servicePage, pageSize: this.servicePageSize});
        if (res.code === 200) {
          this.serviceCommentsList = (res.data.list || [])
              .filter(item => item.status === 1)
              .map(item => ({
                id: item.id,
                serviceId: item.serviceId,
                serviceName: item.serviceName,
                servicePrice: item.servicePrice,
                serviceImage: item.serviceImage,
                rating: item.rating,
                content: item.content,
                images: item.images,
                reply: item.reply,
                createTime: item.createTime,
                replyTime: item.replyTime,
                authorName: item.userNickname || item.userName,  // ✅ 添加
                authorAvatar: item.userAvatar                    // ✅ 添加
              }));
          this.serviceTotal = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载服务评价失败', error);
        this.serviceCommentsList = [];
        this.serviceTotal = 0;
      } finally {
        this.serviceLoading = false;
      }
    },

    async deleteServiceComment(comment) {
      this.$confirm('确定要删除这条评价吗？', '提示', {type: 'warning'})
          .then(async () => {
            const res = await deleteServiceComment(comment.id);
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadServiceComments();
              if (this.commentType === 'all') this.loadAllComments();
            } else {
              this.$message.error(res.message || '删除失败');
            }
          })
          .catch(() => {
          });
    },

    viewServiceCommentDetail(comment) {
      this.currentServiceComment = comment;
      this.serviceDetailVisible = true;
    },

    // ========== 商品评价 ==========
    async loadProductComments() {
      this.productLoading = true;
      try {
        const res = await getUserProductComments({page: this.productPage, pageSize: this.productPageSize});
        if (res.code === 200) {
          this.productCommentsList = (res.data.list || [])
              .filter(item => item.status === 1 && item.productName && item.productName.trim() !== '')
              .map(item => ({
                id: item.id,
                productId: item.productId,
                productName: item.productName,
                productPrice: item.productPrice,
                productImage: item.productImage,
                rating: item.rating,
                content: item.content,
                images: item.images,
                reply: item.reply,
                createTime: item.createTime,
                replyTime: item.replyTime,
                authorName: item.userNickname || item.userName,  // ✅ 添加
                authorAvatar: item.userAvatar                    // ✅ 添加
              }));
          this.productTotal = this.productCommentsList.length;
        }
      } catch (error) {
        console.error('加载商品评价失败', error);
        this.productCommentsList = [];
        this.productTotal = 0;
      } finally {
        this.productLoading = false;
      }
    },

    async deleteProductComment(comment) {
      this.$confirm('确定要删除这条评价吗？', '提示', {type: 'warning'})
          .then(async () => {
            const res = await deleteProductCommentApi(comment.id);
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadProductComments();
              if (this.commentType === 'all') this.loadAllComments();
            } else {
              this.$message.error(res.message || '删除失败');
            }
          })
          .catch(() => {
          });
    },

    viewProductCommentDetail(comment) {
      this.currentProductComment = comment;
      this.productDetailVisible = true;
    },

    viewCommentDetail(comment) {
      if (comment.type === 'post') {
        this.viewPostCommentDetail(comment);
      } else if (comment.type === 'service') {
        this.viewServiceCommentDetail(comment);
      } else if (comment.type === 'product') {
        this.viewProductCommentDetail(comment);
      }
    },

    async deleteComment(comment) {
      if (comment.type === 'post') {
        await this.deletePostComment(comment);
      } else if (comment.type === 'service') {
        await this.deleteServiceComment(comment);
      } else if (comment.type === 'product') {
        await this.deleteProductComment(comment);
      }
    },

    // 查看目标（服务/商品/帖子）
    viewTarget(comment) {
      if (comment.type === 'post') {
        this.goToPostDetail(comment.targetId);
      } else if (comment.type === 'service') {
        this.goToServiceDetail(comment.targetId);
      } else if (comment.type === 'product') {
        this.goToProductDetail(comment.targetId);
      }
    },

    // ========== 分页 ==========
    handleAllPageChange(page) {
      this.allPage = page;
      // 计算属性会自动重新计算
      this.$forceUpdate();
    },

    handlePostPageChange(page) {
      this.postPage = page;
      this.loadPostComments();
    },

    handleServicePageChange(page) {
      this.servicePage = page;
      this.loadServiceComments();
    },

    handleProductPageChange(page) {
      this.productPage = page;
      this.loadProductComments();
    },

    // ========== 导航 ==========
    goToPostDetail(postId) {
      if (postId) {
        this.postDetailVisible = false;
        this.$router.push(`/community/post/${postId}`);
      }
    },

    goToServiceDetail(serviceId) {
      if (serviceId) {
        this.serviceDetailVisible = false;
        this.$router.push(`/service/${serviceId}`);
      }
    },

    goToProductDetail(productId) {
      if (productId) {
        this.productDetailVisible = false;
        this.$router.push(`/product/${productId}`);
      }
    },

    // ========== 工具方法 ==========
    getAuthorInitial(name) {
      if (!name) return 'U';
      return name.charAt(0).toUpperCase();
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    formatRelativeTime(date) {
      if (!date) return '';
      const d = new Date(date);
      const now = new Date();
      const diff = now - d;

      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;

      return this.formatDate(date);
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
.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

/* 筛选栏 - 切换按钮和搜索框在同一行 */
.filters-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

/* 切换标签样式 */
.comment-tabs {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 30px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  background: #fff;
  border: 1px solid #e0e0e0;
  transition: all 0.3s;
}

.tab-item i {
  font-size: 16px;
}

.tab-item:hover {
  border-color: #667eea;
  color: #667eea;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: transparent;
  color: white;
}

/* 搜索栏样式 */
.search-wrapper {
  position: relative;
  width: 200px;
  flex-shrink: 0;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #909399;
  font-size: 16px;
  z-index: 1;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

/* 评论网格 */
.comments-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.comment-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  border: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.comment-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

/* 可点击的内容区域 */
.comment-content-wrapper {
  cursor: pointer;
  flex: 1;
}

.comment-content-wrapper:hover {
  background: #fafbfc;
}

/* 公共区域样式 */
.post-section,
.service-section,
.product-section {
  padding: 16px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
  flex: 1;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.section-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  flex-shrink: 0;
}

.author-info {
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-wrap: wrap;
}

.author-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 13px;
}

.section-tag {
  color: #999;
  font-size: 12px;
}

.section-time {
  font-size: 11px;
  color: #999;
  flex-shrink: 0;
}

/* 目标信息 */
.target-info {
  margin-bottom: 12px;
  padding-left: 42px;
}

.target-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 6px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.target-content {
  color: #999;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.target-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.target-price {
  font-size: 14px;
  font-weight: bold;
  color: #ff6b6b;
}

.target-rate {
  transform: scale(0.8);
  transform-origin: left;
}

/* 服务/商品小头像 */
.service-avatar-small,
.product-avatar-small {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.service-avatar-small img,
.product-avatar-small img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.service-avatar-placeholder-small,
.product-avatar-placeholder-small {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.service-avatar-placeholder-small i,
.product-avatar-placeholder-small i {
  font-size: 18px;
  color: white;
}

/* 我的评论预览 */
.my-comment-preview {
  padding-left: 42px;
}

.comment-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  color: #667eea;
  font-weight: 500;
  font-size: 12px;
}

.comment-label i {
  font-size: 14px;
}

.comment-time {
  margin-left: auto;
  font-weight: normal;
  color: #999;
  font-size: 11px;
}

.comment-preview-text {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 操作按钮 */
.comment-actions {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.comment-actions .el-button {
  flex: 1;
  padding: 6px 0;
  font-size: 12px;
  border-radius: 6px;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #ddd;
}

/* 分页 */
.pagination {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* ========== 详情模态框样式 ========== */
.detail-dialog ::v-deep .el-dialog {
  border-radius: 16px;
}

.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 16px 20px;
}

.detail-dialog ::v-deep .el-dialog__title {
  color: #fff;
  font-weight: 600;
}

.detail-dialog ::v-deep .el-dialog__close {
  color: #fff;
}

.detail-dialog ::v-deep .el-dialog__body {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-label {
  font-size: 13px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}

/* 帖子详情卡片 */
.post-detail-card,
.parent-detail-card,
.my-detail-card,
.reply-detail-card,
.service-detail-card,
.product-detail-card {
  background: #f8f9fc;
  border-radius: 10px;
  padding: 12px;
  cursor: pointer;
  transition: background 0.3s;
}

.post-detail-card:hover,
.service-detail-card:hover,
.product-detail-card:hover {
  background: #ecf5ff;
}

.post-detail-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.detail-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.post-detail-author {
  font-weight: 600;
  color: #2c3e50;
  font-size: 13px;
}

.post-detail-time {
  font-size: 11px;
  color: #999;
  margin-left: auto;
}

.post-detail-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.post-detail-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

/* 父评论卡片 */
.parent-detail-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.parent-detail-author {
  font-size: 12px;
  font-weight: 500;
  color: #667eea;
}

.parent-detail-content {
  font-size: 12px;
  color: #888;
  font-style: italic;
  line-height: 1.4;
}

/* 我的评论卡片 */
.my-detail-time {
  font-size: 11px;
  color: #999;
  margin-bottom: 8px;
}

.my-detail-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 回复卡片 */
.reply-detail-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.reply-detail-author {
  font-size: 12px;
  font-weight: 600;
  color: #67c23a;
}

.reply-detail-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.reply-detail-time {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
}

/* 服务/商品详情卡片 */
.service-detail-header,
.product-detail-header {
  display: flex;
  gap: 12px;
}

.service-detail-avatar,
.product-detail-avatar {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.service-detail-avatar img,
.product-detail-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.service-detail-avatar-placeholder,
.product-detail-avatar-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.service-detail-avatar-placeholder i,
.product-detail-avatar-placeholder i {
  font-size: 24px;
  color: white;
}

.service-detail-info,
.product-detail-info {
  flex: 1;
}

.service-detail-name,
.product-detail-name {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 6px 0;
}

.service-detail-meta,
.product-detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.service-detail-price,
.product-detail-price {
  font-size: 14px;
  font-weight: bold;
  color: #ff6b6b;
}

.detail-images {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.detail-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
}

/* 响应式 */
@media (max-width: 768px) {
  .filters-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-wrapper {
    width: 100%;
  }

  .comment-tabs {
    justify-content: center;
  }

  .comments-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .target-info,
  .my-comment-preview {
    padding-left: 0;
  }

  .service-detail-header,
  .product-detail-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .service-detail-meta,
  .product-detail-meta {
    justify-content: center;
  }
}
</style>