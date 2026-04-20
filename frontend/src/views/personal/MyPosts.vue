<template>
  <div class="my-posts-page">
    <h2 class="page-title">我的帖子</h2>

    <div class="posts-grid" v-loading="loading">
      <div class="post-card" v-for="post in postsList" :key="post.id" @click="viewPost(post)">
        <div class="post-image">
          <img v-if="post.images && post.images.split(',')[0]" :src="post.images.split(',')[0]" :alt="post.title">
          <div v-else class="image-placeholder">
            <i class="el-icon-document"></i>
          </div>
          <div class="post-badge">
            <el-tag v-if="post.isTop === 1" size="mini" type="danger" effect="dark">置顶</el-tag>
            <el-tag v-if="post.isEssence === 1" size="mini" type="warning" effect="dark">精华</el-tag>
            <el-tag v-if="post.status === 0" size="mini" type="info">已删除</el-tag>
          </div>
        </div>
        <div class="post-info">
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-content">{{ truncateText(post.content, 80) }}</p>
          <div class="post-meta">
            <span class="post-time"><i class="el-icon-time"></i> {{ formatDate(post.createTime) }}</span>
            <div class="post-stats">
              <span><i class="el-icon-star-on"></i> {{ post.likeCount || 0 }}</span>
              <span><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
              <span><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
            </div>
          </div>
          <div class="post-actions" @click.stop>
            <el-button size="small" plain @click="viewPost(post)">查看</el-button>
            <el-button v-if="post.status === 1" size="small" type="primary" plain @click="editPost(post)">编辑</el-button>
            <el-button v-if="post.status === 1" size="small" type="danger" plain @click="deletePost(post)">删除</el-button>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && postsList.length === 0">
        <i class="el-icon-document"></i>
        <p>暂无帖子</p>
        <el-button type="primary" size="small" @click="$router.push('/community')">去发布</el-button>
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
            @current-change="handlePageChange"
            :current-page="page"
            :page-size="pageSize"
            layout="prev, pager, next"
            :total="total"
            background
        />
      </div>
    </div>

    <!-- 编辑帖子对话框 -->
    <el-dialog title="编辑帖子" :visible.sync="editDialogVisible" width="700px" :close-on-click-modal="false" @closed="resetEditForm">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="editForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="宠物日常" value="pet_daily" />
            <el-option label="健康分享" value="health" />
            <el-option label="美食分享" value="food" />
            <el-option label="领养信息" value="adopt" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" :rows="8" placeholder="分享你的养宠经验、萌宠日常..." maxlength="5000" show-word-limit />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload action="#" :http-request="uploadImage" list-type="picture-card" :on-preview="handlePreview" :on-remove="handleRemove" :file-list="editForm.imageList" :limit="9">
            <i class="el-icon-plus" />
          </el-upload>
          <div class="upload-tip">支持 JPG、PNG 格式，最多9张，每张不超过2MB</div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存修改</el-button>
      </span>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog :visible.sync="previewVisible" width="600px" append-to-body>
      <img :src="previewImage" style="width: 100%" alt="预览图片">
    </el-dialog>
  </div>
</template>

<script>
import { getUserPosts, deletePost, updatePost } from '@/api/community';
import { uploadPostImage } from '@/api/upload';

export default {
  name: 'MyPosts',
  data() {
    return {
      loading: false,
      editLoading: false,
      postsList: [],
      total: 0,
      page: 1,
      pageSize: 12,
      editDialogVisible: false,
      previewVisible: false,
      previewImage: '',
      currentEditId: null,
      editForm: {
        title: '',
        category: 'pet_daily',
        content: '',
        imageList: [],
        imageUrls: []
      },
      editRules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在2-100个字符', trigger: 'blur' }
        ],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      }
    };
  },
  created() {
    this.loadPosts();
  },
  methods: {
    async loadPosts() {
      this.loading = true;
      try {
        const res = await getUserPosts({ page: this.page, pageSize: this.pageSize });
        if (res.code === 200) {
          this.postsList = res.data.list || [];
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    viewPost(post) {
      this.$router.push(`/community/post/${post.id}`);
    },

    editPost(post) {
      this.currentEditId = post.id;
      this.editForm = {
        title: post.title || '',
        category: post.category || 'pet_daily',
        content: post.content || '',
        imageUrls: post.images ? post.images.split(',').filter(img => img) : [],
        imageList: this.buildImageList(post.images)
      };
      this.editDialogVisible = true;
    },

    buildImageList(images) {
      if (!images) return [];
      return images.split(',').filter(img => img).map((url, index) => ({
        uid: index,
        name: `image-${index}.jpg`,
        url: url,
        status: 'success'
      }));
    },

    async uploadImage(file) {
      const formData = new FormData();
      formData.append('file', file.file);
      try {
        const res = await uploadPostImage(formData);
        if (res.code === 200) {
          this.editForm.imageUrls.push(res.data.url);
          this.editForm.imageList.push({
            uid: Date.now(),
            name: file.file.name,
            url: res.data.url,
            status: 'success'
          });
          this.$message.success('上传成功');
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
    },

    handlePreview(file) {
      this.previewImage = file.url;
      this.previewVisible = true;
    },

    handleRemove(file) {
      const index = this.editForm.imageList.findIndex(f => f.uid === file.uid);
      if (index !== -1) {
        this.editForm.imageList.splice(index, 1);
        this.editForm.imageUrls.splice(index, 1);
      }
    },

    async submitEdit() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) return;
        this.editLoading = true;
        try {
          const res = await updatePost({
            id: this.currentEditId,
            title: this.editForm.title,
            content: this.editForm.content,
            category: this.editForm.category,
            images: this.editForm.imageUrls.join(',')
          });
          if (res.code === 200) {
            this.$message.success('修改成功');
            this.editDialogVisible = false;
            this.loadPosts();
          }
        } catch (error) {
          this.$message.error('修改失败');
        } finally {
          this.editLoading = false;
        }
      });
    },

    resetEditForm() {
      this.currentEditId = null;
      this.editForm = {
        title: '',
        category: 'pet_daily',
        content: '',
        imageList: [],
        imageUrls: []
      };
      this.$nextTick(() => {
        this.$refs.editForm?.clearValidate();
      });
    },

    async deletePost(post) {
      this.$confirm(`确定要删除帖子 "${post.title}" 吗？`, '提示', { type: 'warning' })
          .then(async () => {
            const res = await deletePost(post.id);
            if (res.code === 200) {
              this.$message.success('删除成功');
              this.loadPosts();
            }
          })
          .catch(() => {});
    },

    handlePageChange(page) {
      this.page = page;
      this.loadPosts();
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
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

.posts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.post-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eee;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.post-image {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: #f5f5f5;
}

.post-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  gap: 6px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa, #e8eaef);
  color: #bbb;
  font-size: 48px;
}

.post-info {
  padding: 16px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-content {
  font-size: 13px;
  color: #999;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 12px;
  color: #999;
}

.post-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-stats {
  display: flex;
  gap: 12px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.post-actions .el-button {
  flex: 1;
  padding: 6px 0;
  font-size: 12px;
}

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

.pagination {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 对话框样式 */
::v-deep .el-dialog {
  border-radius: 16px;
}

::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 16px 20px;
}

::v-deep .el-dialog__title {
  color: #fff;
  font-weight: 600;
}

::v-deep .el-dialog__close {
  color: #fff;
}

::v-deep .el-dialog__body {
  padding: 20px;
}

::v-deep .el-dialog__footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 900px) {
  .posts-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .posts-grid {
    grid-template-columns: 1fr;
  }
}
</style>