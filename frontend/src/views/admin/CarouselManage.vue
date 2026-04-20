<template>
  <div class="carousel-manage">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增轮播图
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="success"
            plain
            @click="handleBatchEnable"
            class="batch-btn"
        >
          <i class="el-icon-check"></i> 启用 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDisable"
            class="batch-btn"
        >
          <i class="el-icon-close"></i> 禁用 ({{ selectedRows.length }})
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
            class="batch-btn"
        >
          <i class="el-icon-delete"></i> 删除 ({{ selectedRows.length }})
        </el-button>
        <!-- 保存排序按钮 -->
        <el-button
            v-if="sortChanged"
            type="success"
            plain
            @click="saveSortOrder"
            :loading="sortSaving"
            class="batch-btn"
        >
          <i class="el-icon-check"></i> 保存排序
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索标题"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          ></el-input>
        </div>
        <el-select
            v-model="searchForm.status"
            placeholder="状态"
            clearable
            size="medium"
            class="status-select"
            @change="handleSearch"
        >
          <el-option label="正常" :value="1"></el-option>
          <el-option label="禁用" :value="0"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">
          重置
        </el-button>
      </div>
    </div>

    <!-- 轮播图表 - 整行可拖拽 -->
    <el-table
        v-loading="loading"
        :data="carouselList"
        stripe
        row-key="id"
        class="carousel-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <!-- 图片列 -->
      <el-table-column label="图片" width="100" align="center">
        <template slot-scope="scope">
          <div class="carousel-img-wrapper">
            <el-image
                :src="scope.row.image"
                fit="contain"
                class="carousel-img"
                :preview-src-list="[scope.row.image]"
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </div>
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

      <el-table-column prop="sortOrder" label="排序" width="100" align="center">
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
              :loading="scope.row.statusLoading"
          ></el-switch>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="240" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button
                size="small"
                type="info"
                plain
                circle
                @click="handleView(scope.row)"
                class="action-icon-btn"
                title="查看"
            >
              <i class="el-icon-view"></i>
            </el-button>
            <el-button
                size="small"
                type="primary"
                plain
                circle
                @click="handleEdit(scope.row)"
                class="action-icon-btn"
                title="编辑"
            >
              <i class="el-icon-edit"></i>
            </el-button>
            <el-button
                size="small"
                type="danger"
                plain
                circle
                @click="handleDelete(scope.row)"
                class="action-icon-btn"
                title="删除"
            >
              <i class="el-icon-delete"></i>
            </el-button>
          </div>
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
      ></el-pagination>
    </div>

    <!-- 新增/编辑轮播图对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="560px"
        :close-on-click-modal="false"
        class="carousel-dialog"
        @closed="handleDialogClosed"
    >
      <div class="dialog-content">
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
                    fit="contain"
                    class="preview-image"
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
            <el-input-number v-model="currentCarousel.sortOrder" :min="0" :max="999" controls-position="right"></el-input-number>
            <span class="form-tip">数字越小越靠前</span>
          </el-form-item>

          <el-form-item label="状态">
            <el-radio-group v-model="currentCarousel.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 轮播图详情对话框 -->
    <el-dialog
        title="轮播图详情"
        :visible.sync="detailVisible"
        width="500px"
        center
        class="carousel-detail-dialog"
    >
      <div class="detail-content" v-if="currentDetailCarousel">
        <div class="detail-image-wrapper">
          <el-image
              :src="currentDetailCarousel.image"
              fit="contain"
              class="detail-image"
              :preview-src-list="[currentDetailCarousel.image]"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </div>
        <div class="detail-item">
          <span class="detail-label">ID：</span>
          <span class="detail-value">{{ currentDetailCarousel.id }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">标题：</span>
          <span class="detail-value">{{ currentDetailCarousel.title }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">跳转链接：</span>
          <span class="detail-value">{{ currentDetailCarousel.linkUrl || '无' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">排序值：</span>
          <span class="detail-value">{{ currentDetailCarousel.sortOrder }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态：</span>
          <el-tag :type="currentDetailCarousel.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentDetailCarousel.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="detail-label">创建时间：</span>
          <span class="detail-value">{{ formatDate(currentDetailCarousel.createTime) }}</span>
        </div>
      </div>

      <span slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
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
  batchDeleteCarousel,
  batchUpdateCarouselSort
} from '@/api/carousel';
import { uploadCarouselImage } from '@/api/upload';
import Sortable from 'sortablejs';

export default {
  name: 'CarouselManage',
  data() {
    return {
      loading: false,
      submitLoading: false,
      sortSaving: false,
      sortChanged: false,
      detailVisible: false,
      currentDetailCarousel: null,
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
  mounted() {
    this.initSortable();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        // 构建参数，只添加有值的字段
        const params = {
          page: this.page,
          pageSize: this.pageSize
        };

        if (this.searchForm.keyword && this.searchForm.keyword.trim()) {
          params.keyword = this.searchForm.keyword.trim();
        }

        // 重要：status 可能为 0，不能使用 || 判断
        if (this.searchForm.status !== '' && this.searchForm.status !== null && this.searchForm.status !== undefined) {
          params.status = this.searchForm.status;
        }

        console.log('请求参数:', params); // 调试用

        const res = await getAdminCarouselList(params);
        if (res.code === 200) {
          this.carouselList = res.data.list.map(item => ({
            ...item,
            statusLoading: false
          }));
          this.total = res.data.total;
          this.sortChanged = false;
        }
      } catch (error) {
        console.error('加载失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    // 整行可拖拽排序
    initSortable() {
      const el = this.$el.querySelector('.el-table__body-wrapper tbody');
      if (!el) return;

      if (this.sortableInstance) {
        this.sortableInstance.destroy();
      }

      this.sortableInstance = new Sortable(el, {
        animation: 300,
        onEnd: (evt) => {
          const oldIndex = evt.oldIndex;
          const newIndex = evt.newIndex;

          if (oldIndex === newIndex) return;

          const movedItem = this.carouselList.splice(oldIndex, 1)[0];
          this.carouselList.splice(newIndex, 0, movedItem);

          this.carouselList.forEach((item, index) => {
            item.sortOrder = index;
          });

          this.$forceUpdate();
          this.sortChanged = true;

          console.log('拖拽后排序:', this.carouselList.map(i => ({id: i.id, order: i.sortOrder})));
        }
      });
    },

    // 查看详情
    handleView(row) {
      this.currentDetailCarousel = row;
      this.detailVisible = true;
    },

    // 保存排序
    async saveSortOrder() {
      if (!this.sortChanged) {
        this.$message.info('排序未发生变化');
        return;
      }

      this.sortSaving = true;
      try {
        const sortList = this.carouselList.map((item, index) => ({
          id: item.id,
          sortOrder: index
        }));

        console.log('发送的排序数据:', sortList);

        const res = await batchUpdateCarouselSort(sortList);

        if (res.code === 200) {
          this.$message.success('排序保存成功');
          this.sortChanged = false;
          await this.loadList();
        } else {
          this.$message.error(res.message || '保存失败');
          await this.loadList();
        }
      } catch (error) {
        console.error('保存排序失败:', error);
        this.$message.error('保存排序失败');
        await this.loadList();
      } finally {
        this.sortSaving = false;
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

      const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
      if (!allowedTypes.includes(file.type)) {
        this.$message.error('只支持 JPG、PNG 格式的图片');
        return;
      }

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
      if (this.selectedRows.length === 0) return;
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
      if (this.selectedRows.length === 0) return;
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
      if (this.selectedRows.length === 0) return;
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
  },

  beforeDestroy() {
    if (this.sortableInstance) {
      this.sortableInstance.destroy();
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

.add-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-weight: 500;
  padding: 8px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.batch-btn {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s;
}

.search-wrapper {
  position: relative;
  display: inline-block;
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

.search-input {
  width: 240px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.status-select {
  width: 100px;
}

.status-select ::v-deep .el-input__inner {
  border-radius: 8px;
}

.search-btn, .reset-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.reset-btn {
  border-color: #dcdfe6;
  color: #606266;
}

.reset-btn:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
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

/* 图片列样式 */
.carousel-img-wrapper {
  width: 80px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  background: #f8f9fc;
  border-radius: 8px;
}

.carousel-img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  cursor: pointer;
}

.carousel-img .el-image__inner {
  object-fit: contain;
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

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-icon-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  transition: all 0.2s;
}

.action-icon-btn:hover {
  transform: scale(1.1);
}

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
.carousel-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.carousel-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.carousel-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.carousel-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.carousel-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.dialog-content {
  padding: 10px 0;
}

/* 上传区域样式 */
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
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: #fafafa;
}

.upload-preview:hover {
  border-color: #409EFF;
  color: #409EFF;
  background: #f5f7fa;
}

.upload-preview .preview-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-preview .preview-image .el-image__inner {
  object-fit: contain;
  max-width: 100%;
  max-height: 100%;
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
  padding: 16px 24px 20px;
  border-top: 1px solid #eef2f6;
}

/* 拖拽时的行样式 */
.sortable-drag {
  opacity: 0.5;
  background: #f0f2f5 !important;
}

/* 详情对话框样式 */
.carousel-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.carousel-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: 0;
}

.carousel-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.carousel-detail-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.carousel-detail-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.detail-content {
  padding: 0 10px;
}

.detail-image-wrapper {
  margin-bottom: 20px;
  background: #f8f9fc;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 150px;
}

.detail-image {
  width: auto;
  height: auto;
  max-width: 65%;     /* 图片最大宽度为容器的80% */
  max-height: 300px;  /* 图片最大高度300px */
  border-radius: 8px;
}

.detail-image .el-image__inner {
  object-fit: contain;
}

.detail-image .el-image__inner {
  object-fit: contain;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  width: 85px;
  font-size: 14px;
  color: #909399;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  font-size: 14px;
  color: #2c3e50;
  word-break: break-word;
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

  .search-input {
    width: 180px;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>