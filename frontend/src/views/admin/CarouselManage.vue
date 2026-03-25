<template>
  <div class="carousel-manage">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd">
          <i class="el-icon-plus"></i> 新增轮播图
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="success"
            plain
            @click="handleBatchEnable"
        >
          <i class="el-icon-check"></i> 启用 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDisable"
        >
          <i class="el-icon-close"></i> 禁用 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
        >
          <i class="el-icon-delete"></i> 删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <el-input
            v-model="searchForm.keyword"
            placeholder="搜索标题"
            clearable
            size="medium"
            style="width: 200px"
            @keyup.enter="handleSearch"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
        <el-select v-model="searchForm.status" placeholder="状态" clearable size="medium" style="width: 100px" @change="handleSearch">
          <el-option label="正常" :value="1"></el-option>
          <el-option label="禁用" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch">搜索</el-button>
        <el-button size="medium" @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
        v-loading="loading"
        :data="carouselList"
        stripe
        @selection-change="handleSelectionChange"
        class="carousel-table"
        row-key="id"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <el-table-column label="图片" width="120" align="center">
        <template slot-scope="scope">
          <el-image
              :src="scope.row.image"
              fit="cover"
              style="width: 80px; height: 50px; border-radius: 8px; cursor: pointer;"
              :preview-src-list="[scope.row.image]"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="标题" min-width="200">
        <template slot-scope="scope">
          <span class="title-text">{{ scope.row.title }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="linkUrl" label="跳转链接" min-width="250">
        <template slot-scope="scope">
          <span class="link-text">{{ scope.row.linkUrl || '无' }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="sortOrder" label="排序" width="80" align="center">
        <template slot-scope="scope">
          <span class="sort-text">{{ scope.row.sortOrder }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
          >
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">
            <i class="el-icon-edit"></i> 编辑
          </el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">
            <i class="el-icon-delete"></i> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          :current-page="page"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
      >
      </el-pagination>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="560px"
        :close-on-click-modal="false"
        @closed="handleDialogClosed"
    >
      <el-form :model="currentCarousel" :rules="rules" ref="carouselForm" label-width="80px">
        <el-form-item label="图片" prop="image">
          <div class="upload-wrapper">
            <div class="upload-preview" @click="triggerUpload" v-if="!imagePreview">
              <i class="el-icon-plus"></i>
              <span>点击上传图片</span>
            </div>
            <div class="upload-preview" v-else @click="triggerUpload">
              <el-image
                  :src="imagePreview"
                  fit="cover"
                  style="width: 100%; height: 100%; border-radius: 8px;"
              >
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
              <div class="upload-mask">
                <i class="el-icon-edit"></i>
                <span>更换图片</span>
              </div>
            </div>
            <input
                type="file"
                ref="imageInput"
                accept="image/jpeg,image/jpg,image/png"
                style="display: none"
                @change="handleImageUpload"
            />
            <div class="upload-tip">支持 JPG、PNG 格式，建议尺寸 1920x500，≤2MB</div>
          </div>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="currentCarousel.title" placeholder="请输入标题" maxlength="100" show-word-limit></el-input>
        </el-form-item>

        <el-form-item label="跳转链接">
          <el-input v-model="currentCarousel.linkUrl" placeholder="例如：/product/1 或 https://xxx.com"></el-input>
          <div class="form-tip">留空则不跳转</div>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="currentCarousel.sortOrder" :min="0" :max="999" :step="1" controls-position="right"></el-input-number>
          <span class="form-tip">数字越小越靠前</span>
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="currentCarousel.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminCarouselList,
  addCarousel,
  updateCarousel,
  updateCarouselStatus,
  deleteCarousel,
  batchUpdateCarouselStatus,
  batchDeleteCarousel
} from '@/api/carousel';
import { uploadCarouselImage } from '@/api/upload';

export default {
  name: 'CarouselManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      carouselList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: {
        keyword: '',
        status: ''
      },
      dialogVisible: false,
      isEdit: false,
      imagePreview: '',
      currentCarousel: {
        id: null,
        title: '',
        image: '',
        linkUrl: '',
        sortOrder: 0,
        status: 1
      },
      rules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { min: 1, max: 100, message: '长度在1-100个字符', trigger: 'blur' }
        ],
        image: [
          { required: true, message: '请上传图片', trigger: 'change' }
        ]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑轮播图' : '新增轮播图';
    }
  },
  created() {
    this.loadList();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          status: this.searchForm.status || undefined
        };
        const res = await getAdminCarouselList(params);
        if (res.code === 200) {
          this.carouselList = res.data.list;
          this.total = res.data.total;
        }
      } catch (error) {
        console.error('加载失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    handleSearch() {
      this.page = 1;
      this.loadList();
    },

    handleReset() {
      this.searchForm = { keyword: '', status: '' };
      this.page = 1;
      this.loadList();
    },

    handlePageChange(page) {
      this.page = page;
      this.loadList();
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadList();
    },

    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    triggerUpload() {
      this.$refs.imageInput.click();
    },

    async handleImageUpload(event) {
      const file = event.target.files[0];
      if (!file) return;

      // 验证文件类型
      const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
      if (!allowedTypes.includes(file.type)) {
        this.$message.error('只支持 JPG、PNG 格式的图片');
        return;
      }

      // 验证文件大小
      if (file.size > 2 * 1024 * 1024) {
        this.$message.error('图片大小不能超过 2MB');
        return;
      }

      const formData = new FormData();
      formData.append('file', file);

      try {
        const res = await uploadCarouselImage(formData);
        if (res.code === 200) {
          this.imagePreview = res.data.url;
          this.currentCarousel.image = res.data.url;
          this.$message.success('上传成功');
          // 清除验证
          this.$nextTick(() => {
            if (this.$refs.carouselForm) {
              this.$refs.carouselForm.clearValidate('image');
            }
          });
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        console.error('上传失败', error);
        this.$message.error('上传失败');
      }
      event.target.value = '';
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '启用' : '禁用';

      try {
        const res = await updateCarouselStatus(row.id, newStatus);
        if (res.code === 200) {
          row.status = newStatus;
          this.$message.success(`${action}成功`);
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除轮播图"${row.title}"吗？删除后无法恢复！`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteCarousel(row.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    handleAdd() {
      this.isEdit = false;
      this.currentCarousel = {
        id: null,
        title: '',
        image: '',
        linkUrl: '',
        sortOrder: 0,
        status: 1
      };
      this.imagePreview = '';
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.carouselForm) {
          this.$refs.carouselForm.clearValidate();
        }
      });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.currentCarousel = {
        id: row.id,
        title: row.title,
        image: row.image,
        linkUrl: row.linkUrl || '',
        sortOrder: row.sortOrder,
        status: row.status
      };
      this.imagePreview = row.image;
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.carouselForm) {
          this.$refs.carouselForm.clearValidate();
        }
      });
    },

    handleDialogClosed() {
      this.imagePreview = '';
      this.currentCarousel = {
        id: null,
        title: '',
        image: '',
        linkUrl: '',
        sortOrder: 0,
        status: 1
      };
    },

    submitForm() {
      this.$refs.carouselForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            res = await updateCarousel(this.currentCarousel);
          } else {
            res = await addCarousel(this.currentCarousel);
          }

          if (res.code === 200) {
            this.$message.success(res.message);
            this.dialogVisible = false;
            this.loadList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('提交失败', error);
          this.$message.error(this.isEdit ? '更新失败' : '添加失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    async handleBatchEnable() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要操作的数据');
        return;
      }
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要启用选中的 ${this.selectedRows.length} 个轮播图吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const res = await batchUpdateCarouselStatus(ids, 1);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量启用失败');
        }
      }).catch(() => {});
    },

    async handleBatchDisable() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要操作的数据');
        return;
      }
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要禁用选中的 ${this.selectedRows.length} 个轮播图吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchUpdateCarouselStatus(ids, 0);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量禁用失败');
        }
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要操作的数据');
        return;
      }
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个轮播图吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteCarousel(ids);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量删除失败');
        }
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.carousel-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 顶部操作栏 */
.action-bar {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.action-right {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

/* 表格样式 */
.carousel-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.carousel-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f6;
}

.carousel-table ::v-deep .el-table__body tr:hover > td {
  background: #fafbfe;
}

.carousel-table ::v-deep .el-table__body td {
  padding: 14px 0;
  font-size: 13px;
}

.title-text {
  color: #2c3e50;
  font-weight: 500;
}

.link-text {
  color: #409EFF;
  word-break: break-all;
  font-size: 12px;
}

.sort-text {
  color: #606266;
  font-weight: 500;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 对话框样式 */
::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: #fff;
}

/* 上传组件 */
.upload-wrapper {
  text-align: center;
}

.upload-preview {
  width: 100%;
  height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  background: #fafafa;
}

.upload-preview:hover {
  border-color: #409EFF;
  color: #409EFF;
  background: #f5f7fa;
}

.upload-preview i {
  font-size: 32px;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.upload-preview:hover .upload-mask {
  opacity: 1;
}

.upload-mask i {
  font-size: 24px;
  margin-bottom: 6px;
}

.upload-mask span {
  font-size: 12px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.dialog-footer {
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
}

.dialog-footer .el-button {
  border-radius: 8px;
  padding: 9px 24px;
  font-weight: 500;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.dialog-footer .el-button--primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 响应式 */
@media (max-width: 768px) {
  .carousel-manage {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .action-left, .action-right {
    justify-content: center;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>