<template>
  <div class="carousel-page">
    <!-- 统计概览卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-picture-outline"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">轮播图总数</div>
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-sub">平台轮播图总量</div>
        </div>
      </div>
      <div class="stat-card card-active">
        <div class="stat-icon">
          <i class="el-icon-check"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">已启用</div>
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-sub">前台正常展示</div>
        </div>
      </div>
      <div class="stat-card card-disabled">
        <div class="stat-icon">
          <i class="el-icon-close"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">已禁用</div>
          <div class="stat-value">{{ stats.disabled }}</div>
          <div class="stat-sub">前台不展示</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索标题..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.status"
          placeholder="全部状态"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button type="primary" size="medium" @click="handleAdd" class="warm-btn">
          <i class="el-icon-plus" /> 新增轮播图
        </el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0 || sortChanged">
        <span v-if="selectedRows.length > 0" class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <template v-if="selectedRows.length > 0">
          <el-button size="small" class="batch-btn-enable" @click="handleBatchEnable">
            <i class="el-icon-check" /> 批量启用
          </el-button>
          <el-button size="small" class="batch-btn-disable" @click="handleBatchDisable">
            <i class="el-icon-close" /> 批量禁用
          </el-button>
          <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
            <i class="el-icon-delete" /> 批量删除
          </el-button>
        </template>
        <el-button v-if="sortChanged" size="small" class="batch-btn-save" @click="saveSortOrder" :loading="sortSaving">
          <i class="el-icon-check" /> 保存排序
        </el-button>
      </div>
    </transition>

    <!-- 轮播图表 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="carouselList"
        stripe
        row-key="id"
        class="carousel-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="图片" width="110" align="center">
          <template slot-scope="scope">
            <div class="img-cell">
              <el-image
                :src="scope.row.image"
                fit="contain"
                class="thumb-img"
                :preview-src-list="[scope.row.image]"
              >
                <div slot="error" class="img-error">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="220">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="linkUrl" label="跳转链接" min-width="200">
          <template slot-scope="scope">
            <span class="link-text">{{ scope.row.linkUrl || '无' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="sortOrder" label="排序" width="90" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.sortOrder }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.status === 1"
              active-color="#f59e4b"
              inactive-color="#c0c4cc"
              @change="(val) => handleStatusChange(scope.row, val)"
              :loading="scope.row.statusLoading"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button type="text" size="small" @click="handleView(scope.row)" class="act-view">
                <i class="el-icon-view" /> 查看
              </el-button>
              <el-button type="text" size="small" @click="handleEdit(scope.row)" class="act-edit">
                <i class="el-icon-edit" /> 编辑
              </el-button>
              <el-button type="text" size="small" @click="handleDelete(scope.row)" class="act-del">
                <i class="el-icon-delete" /> 删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-card">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :current-page="page"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="560px"
      :close-on-click-modal="false"
      class="carousel-dialog"
      center
      @closed="handleDialogClosed"
    >
      <div class="dialog-body">
        <el-form :model="currentCarousel" :rules="rules" ref="carouselForm" label-width="80px" class="carousel-form">
          <el-form-item label="图片" prop="image">
            <div class="upload-wrap">
              <div class="upload-area" @click="triggerUpload" v-if="!imagePreview">
                <i class="el-icon-plus"></i>
                <span>点击上传图片</span>
              </div>
              <div class="upload-area has-image" v-else @click="triggerUpload">
                <el-image :src="imagePreview" fit="contain" class="preview-image">
                  <div slot="error" class="img-error">
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
                style="display:none"
                @change="handleImageUpload"
              />
              <p class="upload-tip">支持 JPG、PNG 格式，建议尺寸 1920x500，≤2MB</p>
            </div>
          </el-form-item>

          <el-form-item label="标题" prop="title">
            <el-input v-model="currentCarousel.title" placeholder="请输入标题" maxlength="100" show-word-limit />
          </el-form-item>

          <el-form-item label="跳转链接">
            <el-input v-model="currentCarousel.linkUrl" placeholder="例如：/product/1 或 https://xxx.com" />
            <span class="form-tip-inline">留空则不跳转</span>
          </el-form-item>

          <el-form-item label="排序">
            <el-input-number v-model="currentCarousel.sortOrder" :min="0" :max="999" controls-position="right" />
            <span class="form-tip-inline">数字越小越靠前</span>
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      title="轮播图详情"
      :visible.sync="detailVisible"
      width="520px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetailCarousel">
        <div class="detail-img-area">
          <el-image
            :src="currentDetailCarousel.image"
            fit="contain"
            class="detail-img"
            :preview-src-list="[currentDetailCarousel.image]"
          >
            <div slot="error" class="img-error">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="d-label">ID</span>
            <span class="d-value">{{ currentDetailCarousel.id }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">标题</span>
            <span class="d-value">{{ currentDetailCarousel.title }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">跳转链接</span>
            <span class="d-value">{{ currentDetailCarousel.linkUrl || '无' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">排序值</span>
            <span class="d-value">{{ currentDetailCarousel.sortOrder }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">状态</span>
            <span class="d-value">
              <span class="status-tag" :class="currentDetailCarousel.status === 1 ? 'active' : 'inactive'">
                {{ currentDetailCarousel.status === 1 ? '启用' : '禁用' }}
              </span>
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">创建时间</span>
            <span class="d-value">{{ formatDate(currentDetailCarousel.createTime) }}</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
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
  name: 'Admin_CarouselManage',
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
      searchForm: { keyword: '', status: '' },
      dialogVisible: false,
      isEdit: false,
      imagePreview: '',
      stats: { total: 0, active: 0, disabled: 0 },
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
    this.computeStats();
  },
  mounted() {
    this.$nextTick(() => this.initSortable());
  },
  methods: {
    async computeStats() {
      try {
        const res = await getAdminCarouselList({ page: 1, pageSize: 500 });
        if (res.code === 200) {
          const list = res.data.list || [];
          this.stats = {
            total: res.data.total || 0,
            active: list.filter(i => i.status === 1).length,
            disabled: list.filter(i => i.status === 0).length
          };
        }
      } catch { /* stats fetch silently fails */ }
    },
    async loadList() {
      this.loading = true;
      try {
        const params = { page: this.page, pageSize: this.pageSize };
        if (this.searchForm.keyword && this.searchForm.keyword.trim()) {
          params.keyword = this.searchForm.keyword.trim();
        }
        if (this.searchForm.status !== '' && this.searchForm.status !== null && this.searchForm.status !== undefined) {
          params.status = this.searchForm.status;
        }
        const res = await getAdminCarouselList(params);
        if (res.code === 200) {
          this.carouselList = (res.data.list || []).map(item => ({
            ...item,
            statusLoading: false
          }));
          this.total = res.data.total || 0;
          this.sortChanged = false;
        }
      } catch (e) {
        this.$message.error(e?.message || '加载失败');
      } finally {
        this.loading = false;
      }
    },

    initSortable() {
      const el = this.$el.querySelector('.el-table__body-wrapper tbody');
      if (!el) return;
      if (this.sortableInstance) this.sortableInstance.destroy();

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
        }
      });
    },

    handleView(row) {
      this.currentDetailCarousel = row;
      this.detailVisible = true;
    },

    async saveSortOrder() {
      if (!this.sortChanged) return;
      this.sortSaving = true;
      try {
        const sortList = this.carouselList.map((item, index) => ({
          id: item.id,
          sortOrder: index
        }));
        const res = await batchUpdateCarouselSort(sortList);
        if (res.code === 200) {
          this.$message.success('排序保存成功');
          this.sortChanged = false;
          this.loadList();
        } else {
          this.$message.error(res.message || '保存排序失败');
          this.loadList();
        }
      } catch (e) {
        this.$message.error(e?.message || '保存排序失败');
        this.loadList();
      } finally {
        this.sortSaving = false;
      }
    },

    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() {
      this.searchForm = { keyword: '', status: '' };
      this.page = 1;
      this.loadList();
    },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    triggerUpload() { this.$refs.imageInput.click(); },

    async handleImageUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)) {
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
            if (this.$refs.carouselForm) this.$refs.carouselForm.clearValidate('image');
          });
        } else {
          this.$message.error(res.message || '上传失败');
        }
      } catch (e) {
        this.$message.error(e?.message || '上传失败');
      }
      event.target.value = '';
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '启用' : '禁用';
      row.statusLoading = true;
      try {
        const res = await updateCarouselStatus(row.id, newStatus);
        if (res.code === 200) {
          row.status = newStatus;
          this.$message.success(`${action}成功`);
          this.computeStats();
        } else {
          this.$message.error(res.message || `${action}失败，请稍后重试`);
        }
      } catch (e) {
        this.$message.error(e?.message || `${action}失败，请稍后重试`);
      } finally {
        row.statusLoading = false;
      }
    },

    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除轮播图"${row.title}"吗？删除后无法恢复！`, '提示', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        });
        const res = await deleteCarousel(row.id);
        if (res.code === 200) {
          this.$message.success('删除成功');
          this.loadList();
          this.computeStats();
        } else {
          this.$message.error(res.message || '删除失败，请稍后重试');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error(error?.message || '删除失败，请稍后重试');
      }
    },

    handleAdd() {
      this.isEdit = false;
      this.currentCarousel = { id: null, title: '', image: '', linkUrl: '', sortOrder: 0, status: 1 };
      this.imagePreview = '';
      this.dialogVisible = true;
      this.$nextTick(() => { if (this.$refs.carouselForm) this.$refs.carouselForm.clearValidate(); });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.currentCarousel = {
        id: row.id, title: row.title, image: row.image,
        linkUrl: row.linkUrl || '', sortOrder: row.sortOrder, status: row.status
      };
      this.imagePreview = row.image;
      this.dialogVisible = true;
      this.$nextTick(() => { if (this.$refs.carouselForm) this.$refs.carouselForm.clearValidate(); });
    },

    handleDialogClosed() {
      this.imagePreview = '';
      this.currentCarousel = { id: null, title: '', image: '', linkUrl: '', sortOrder: 0, status: 1 };
    },

    submitForm() {
      this.$refs.carouselForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          const res = this.isEdit
            ? await updateCarousel(this.currentCarousel)
            : await addCarousel(this.currentCarousel);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.dialogVisible = false;
            this.loadList();
            this.computeStats();
          } else {
            this.$message.error(res.message || (this.isEdit ? '更新失败，请稍后重试' : '添加失败，请稍后重试'));
          }
        } catch (e) {
          this.$message.error(e?.message || (this.isEdit ? '更新失败，请稍后重试' : '添加失败，请稍后重试'));
        } finally {
          this.submitLoading = false;
        }
      });
    },

    async batchAction(action, status) {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      const labels = { enable: '启用', disable: '禁用', delete: '删除' };
      const label = labels[action];
      try {
        await this.$confirm(
          action === 'delete'
            ? `确定要删除选中的 ${this.selectedRows.length} 个轮播图吗？删除后无法恢复！`
            : `确定要${label}选中的 ${this.selectedRows.length} 个轮播图吗？`,
          '提示',
          { confirmButtonText: '确定', cancelButtonText: '取消', type: action === 'enable' ? 'info' : 'warning' }
        );
        let res;
        if (action === 'delete') {
          res = await batchDeleteCarousel(ids);
        } else {
          res = await batchUpdateCarouselStatus(ids, status);
        }
        if (res.code === 200) {
          this.$message.success(res.message || `批量${label}成功`);
          this.selectedRows = [];
          this.loadList();
          this.computeStats();
        } else {
          this.$message.error(res.message || `批量${label}失败，请稍后重试`);
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error(error?.message || `批量${label}失败，请稍后重试`);
      }
    },
    handleBatchEnable() { this.batchAction('enable', 1); },
    handleBatchDisable() { this.batchAction('disable', 0); },
    handleBatchDelete() { this.batchAction('delete'); }
  },

  beforeDestroy() {
    if (this.sortableInstance) this.sortableInstance.destroy();
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.carousel-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 统计卡片行 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}
.card-total .stat-icon    { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-active .stat-icon   { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-disabled .stat-icon { background: linear-gradient(135deg, #c0c4cc, #909399); }

.stat-info { position: relative; z-index: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 6px; }
.stat-value { font-size: 30px; font-weight: 700; color: #3d2e2a; line-height: 1.1; letter-spacing: -0.5px; }
.stat-sub { font-size: 12px; color: #b8a8a0; margin-top: 4px; }

/* ===== 搜索筛选栏 ===== */
.filter-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px 24px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}
.filter-left, .filter-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.keyword-input { width: 220px; }
.keyword-input ::v-deep .el-input__inner { border-radius: 10px; }
.status-select { width: 110px; }
.status-select ::v-deep .el-input__inner { border-radius: 10px; }

.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  padding: 9px 20px !important;
  font-weight: 500;
}
.warm-btn:hover { opacity: 0.88; transform: translateY(-1px); }

.reset-btn { border-radius: 10px; color: #606266; }
.reset-btn:hover { background: #fdf6f0; border-color: #f5d5c0; }

/* ===== 批量操作浮条 ===== */
.batch-bar {
  background: linear-gradient(135deg, #fef5ee, #fff7f0);
  border: 1px solid #f5d5c0;
  border-left: 4px solid #f59e4b;
  border-radius: 12px;
  padding: 10px 20px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #3d2e2a;
}
.batch-info { font-size: 13px; }
.batch-info strong { color: #f59e4b; font-size: 15px; }
.batch-bar .el-button {
  border-radius: 8px; padding: 7px 16px;
  font-weight: 500; transition: all 0.2s;
}
.batch-btn-enable {
  background: rgba(103, 194, 58, 0.15) !important;
  border: 1px solid rgba(103, 194, 58, 0.35) !important;
  color: #67c23a !important;
}
.batch-btn-enable:hover { background: rgba(103, 194, 58, 0.25) !important; border-color: #67c23a !important; }
.batch-btn-disable {
  background: rgba(245, 158, 75, 0.15) !important;
  border: 1px solid rgba(245, 158, 75, 0.35) !important;
  color: #f59e4b !important;
}
.batch-btn-disable:hover { background: rgba(245, 158, 75, 0.25) !important; border-color: #f59e4b !important; }
.batch-btn-delete {
  background: rgba(245, 108, 108, 0.12) !important;
  border: 1px solid rgba(245, 108, 108, 0.35) !important;
  color: #f56c6c !important;
}
.batch-btn-delete:hover { background: rgba(245, 108, 108, 0.22) !important; border-color: #f56c6c !important; }
.batch-btn-save {
  background: rgba(64, 158, 255, 0.15) !important;
  border: 1px solid rgba(64, 158, 255, 0.35) !important;
  color: #409eff !important;
  margin-left: auto;
}
.batch-btn-save:hover { background: rgba(64, 158, 255, 0.25) !important; border-color: #409eff !important; }
.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.carousel-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.carousel-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.carousel-table ::v-deep .el-table__body tr:hover > td {
  background: #fefbf8;
}
.carousel-table ::v-deep .el-table__body tr.el-table__row--striped > td {
  background: #fefcf9;
}
.carousel-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td {
  background: #fefbf8;
}
.carousel-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b;
  border-color: #f59e4b;
}

.img-cell {
  width: 80px;
  height: 55px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  background: #fdf8f4;
  border-radius: 8px;
}
.thumb-img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  cursor: pointer;
}
.thumb-img ::v-deep .el-image__inner { object-fit: contain; }

.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { color: #a08c84; font-size: 12px; }
.link-text { color: #409eff; font-size: 12px; word-break: break-all; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }

/* ===== 分页卡片 ===== */
.pagination-card {
  background: #fff;
  border-radius: 16px;
  margin-top: 16px;
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

/* ===== 对话框 ===== */
.carousel-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.carousel-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.carousel-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.carousel-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.carousel-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.carousel-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }

.carousel-form ::v-deep .el-form-item { margin-bottom: 18px; }
.carousel-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.carousel-form ::v-deep .el-input__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.carousel-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}
.carousel-form ::v-deep .el-radio__input.is-checked .el-radio__inner {
  background: #f59e4b; border-color: #f59e4b;
}

.dialog-footer {
  text-align: right;
  padding: 16px 28px 22px;
  border-top: 1px solid #f5ece6;
}
.dialog-footer .el-button {
  border-radius: 10px; padding: 10px 28px; font-weight: 500; font-size: 13px;
}
.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
}
.dialog-footer .el-button--primary:hover { opacity: 0.88; transform: translateY(-1px); }

/* 上传区域 */
.upload-wrap { text-align: center; }
.upload-area {
  width: 100%;
  height: 150px;
  border: 2px dashed #e8ddd4;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
  background: #fefbf9;
  color: #a08c84;
  transition: border-color 0.25s, color 0.25s;
}
.upload-area:hover { border-color: #f59e4b; color: #f59e4b; }
.upload-area.has-image { border-style: solid; }
.upload-area .preview-image { width: 100%; height: 100%; }
.upload-area .preview-image ::v-deep .el-image__inner { object-fit: contain; }

.upload-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.25s;
}
.upload-area:hover .upload-mask { opacity: 1; }
.upload-mask i { font-size: 24px; margin-bottom: 6px; }
.upload-mask span { font-size: 12px; }
.upload-tip { font-size: 12px; color: #b8a8a0; margin-top: 8px; }
.form-tip-inline { font-size: 12px; color: #b8a8a0; margin-left: 8px; }

.img-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #fdf8f4;
  color: #b8a8a0;
  font-size: 20px;
}

/* ===== 详情对话框 ===== */
.detail-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.detail-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.detail-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.detail-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.detail-dialog ::v-deep .el-dialog__body { padding: 28px; }

.detail-img-area {
  text-align: center; margin-bottom: 24px;
  padding: 20px; background: #fdf8f4;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
}
.detail-img { max-width: 65%; max-height: 280px; border-radius: 8px; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  border: 1px solid #f5ece6;
  border-radius: 12px;
  overflow: hidden;
}
.detail-item {
  display: flex; align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #f5ece6;
  border-right: 1px solid #f5ece6;
}
.detail-item:nth-child(2n) { border-right: none; }
.detail-item:last-child { border-bottom: none; }
.detail-item:nth-last-child(2):nth-child(odd) { border-bottom: none; }
.d-label { width: 68px; font-size: 13px; color: #a08c84; font-weight: 500; flex-shrink: 0; }
.d-value { flex: 1; font-size: 13px; color: #3d2e2a; font-weight: 500; }
.status-tag { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-tag.active   { background: #edf7ee; color: #67c23a; }
.status-tag.inactive { background: #fef0f0; color: #f56c6c; }

/* 响应式 */
@media (max-width: 768px) {
  .carousel-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 100%; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-item { border-right: none; }
  .detail-item:nth-last-child(2):nth-child(odd) { border-bottom: 1px solid #f5ece6; }
  .detail-item:last-child { border-bottom: none; }
}
</style>
