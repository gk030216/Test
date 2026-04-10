<template>
  <div class="my-posts-page">
    <h2 class="page-title">我的帖子</h2>

    <div class="posts-list" v-loading="loading">
      <div class="post-item" v-for="post in postsList" :key="post.id">
        <div class="post-header">
          <div class="post-info">
            <span class="post-time">{{ formatDate(post.createTime) }}</span>
            <div class="post-badges">
              <el-tag v-if="post.isTop === 1" size="mini" type="danger" effect="dark">置顶</el-tag>
              <el-tag v-if="post.isEssence === 1" size="mini" type="warning" effect="dark">精华</el-tag>
              <el-tag v-if="post.status === 0" size="mini" type="info">已删除</el-tag>
            </div>
          </div>
        </div>

        <div class="post-body" @click="viewPost(post)">
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-content">{{ truncateText(post.content, 120) }}</p>
        </div>

        <div class="post-footer">
          <div class="footer-left">
            <div class="action-item">
              <i class="el-icon-star-on"></i>
              <span>{{ post.likeCount || 0 }}</span>
            </div>
            <div class="action-item">
              <i class="el-icon-chat-dot-round"></i>
              <span>{{ post.commentCount || 0 }}</span>
            </div>
            <div class="action-item">
              <i class="el-icon-view"></i>
              <span>{{ post.viewCount || 0 }}</span>
            </div>
          </div>
          <div class="footer-right">
            <el-button
                type="primary"
                size="small"
                plain
                @click.stop="viewPost(post)"
                class="action-btn view-btn"
            >
              <i class="el-icon-view"></i> 查看
            </el-button>
            <el-button
                v-if="post.status === 1"
                type="warning"
                size="small"
                plain
                @click.stop="editPost(post)"
                class="action-btn edit-btn"
            >
              <i class="el-icon-edit"></i> 编辑
            </el-button>
            <el-button
                v-if="post.status === 1"
                type="danger"
                size="small"
                plain
                @click.stop="deletePost(post)"
                class="action-btn delete-btn"
            >
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && postsList.length === 0">
        <i class="el-icon-document"></i>
        <p>暂无帖子</p>
        <el-button type="primary" size="small" @click="$router.push('/community')">去发布</el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > pageSize">
        <el-pagination
            @current-change="handlePageChange"
            :current-page="page"
            :page-size="pageSize"
            layout="prev, pager, next"
            :total="total"
            background
        ></el-pagination>
      </div>
    </div>

    <!-- 编辑帖子对话框 -->
    <el-dialog
        title="编辑帖子"
        :visible.sync="editDialogVisible"
        width="700px"
        :close-on-click-modal="false"
        @closed="resetEditForm"
    >
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入标题" maxlength="100" show-word-limit></el-input>
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="editForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="宠物日常" value="pet_daily"></el-option>
            <el-option label="健康分享" value="health"></el-option>
            <el-option label="美食分享" value="food"></el-option>
            <el-option label="领养信息" value="adopt"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
              v-model="editForm.content"
              type="textarea"
              :rows="8"
              placeholder="分享你的养宠经验、萌宠日常..."
              maxlength="5000"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="图片">
          <el-upload
              action="#"
              :http-request="uploadImage"
              list-type="picture-card"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :file-list="editForm.imageList"
              :limit="9"
          >
            <i class="el-icon-plus"></i>
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
      pageSize: 10,
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
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' }
        ]
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
            uid: Date.now() + Math.random(),
            name: file.file.name,
            url: res.data.url,
            status: 'success'
          });
          this.$message.success('上传成功');
        } else {
          this.$message.error(res.message || '上传失败');
        }
      } catch (error) {
        console.error('上传失败', error);
        this.$message.error('上传失败');
      }
    },

    handlePreview(file) {
      this.previewImage = file.url;
      this.previewVisible = true;
    },

    handleRemove(file, fileList) {
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
          const data = {
            id: this.currentEditId,
            title: this.editForm.title,
            content: this.editForm.content,
            category: this.editForm.category,
            images: this.editForm.imageUrls.join(',')
          };
          const res = await updatePost(data);
          if (res.code === 200) {
            this.$message.success('修改成功');
            this.editDialogVisible = false;
            this.loadPosts();
          } else {
            this.$message.error(res.message || '修改失败');
          }
        } catch (error) {
          console.error('修改失败', error);
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
      if (this.$refs.editForm) {
        this.$refs.editForm.clearValidate();
      }
    },

    async deletePost(post) {
      this.$confirm(`确定要删除帖子 "${post.title}" 吗？`, '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deletePost(post.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadPosts();
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
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
  margin-bottom: 30px;
  color: #333;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-item {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.post-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.post-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.post-time {
  font-size: 13px;
  color: #999;
}

.post-badges {
  display: flex;
  gap: 6px;
}

.post-body {
  cursor: pointer;
  margin-bottom: 16px;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #2c3e50;
  transition: color 0.3s;
}

.post-body:hover .post-title {
  color: #667eea;
}

.post-content {
  color: #5a6874;
  line-height: 1.7;
  font-size: 14px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.footer-left {
  display: flex;
  gap: 24px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
  transition: color 0.3s;
}

.action-item i {
  font-size: 16px;
}

.action-item:hover {
  color: #667eea;
}

.footer-right {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s;
}

.action-btn i {
  margin-right: 4px;
  font-size: 13px;
}

.view-btn {
  color: #409EFF;
  border-color: #d9ecff;
  background: #ecf5ff;
}

.view-btn:hover {
  color: white;
  background: #409EFF;
  border-color: #409EFF;
}

.edit-btn {
  color: #e6a23c;
  border-color: #faecd8;
  background: #fdf6ec;
}

.edit-btn:hover {
  color: white;
  background: #e6a23c;
  border-color: #e6a23c;
}

.delete-btn {
  color: #f56c6c;
  border-color: #fde2e2;
  background: #fef0f0;
}

.delete-btn:hover {
  color: white;
  background: #f56c6c;
  border-color: #f56c6c;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #dcdfe6;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 对话框样式 */
::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

::v-deep .el-dialog__body {
  padding: 24px;
}

::v-deep .el-dialog__footer {
  padding: 16px 24px 20px;
  border-top: 1px solid #eef2f6;
}

/* 响应式 */
@media (max-width: 768px) {
  .post-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .footer-right {
    width: 100%;
    justify-content: flex-end;
  }

  .post-item {
    padding: 16px;
  }

  .footer-left {
    gap: 16px;
  }
}
</style>