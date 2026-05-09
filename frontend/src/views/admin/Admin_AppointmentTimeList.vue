<template>
  <div class="appointment-time-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon"><i class="el-icon-time" /></div>
        <div class="stat-info">
          <div class="stat-label">时间段总数</div>
          <div class="stat-value">{{ total }}</div>
          <div class="stat-sub">全部时段</div>
        </div>
      </div>
      <div class="stat-card card-enabled">
        <div class="stat-icon"><i class="el-icon-check" /></div>
        <div class="stat-info">
          <div class="stat-label">已启用</div>
          <div class="stat-value">{{ enabledCount }}</div>
          <div class="stat-sub">可预约时段</div>
        </div>
      </div>
      <div class="stat-card card-disabled">
        <div class="stat-icon"><i class="el-icon-close" /></div>
        <div class="stat-info">
          <div class="stat-label">已禁用</div>
          <div class="stat-value">{{ disabledCount }}</div>
          <div class="stat-sub">暂不可用</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索时间段..."
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
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button type="primary" size="medium" @click="handleAdd" class="warm-btn">
          <i class="el-icon-plus" /> 新增时间段
        </el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 排序变更提示 -->
    <transition name="batch-bar">
      <div class="batch-bar sort-bar" v-if="sortChanged">
        <span class="batch-info">排序已变更，请保存</span>
        <el-button size="small" type="success" @click="saveSortOrder" :loading="sortSaving">
          <i class="el-icon-check" /> 保存排序
        </el-button>
      </div>
    </transition>

    <!-- 表格卡片 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="timeList"
        stripe
        row-key="id"
        class="time-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />
        <el-table-column prop="timeSlot" label="时间段" min-width="200">
          <template slot-scope="scope">
            <span class="time-slot-text">{{ scope.row.timeSlot }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" align="center">
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
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-text cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
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
      width="520px"
      :close-on-click-modal="false"
      class="time-dialog"
      center
    >
      <div class="dialog-body">
        <el-form :model="form" :rules="rules" ref="timeForm" label-width="80px" class="time-form">
          <el-form-item label="时间段" prop="timeSlot">
            <el-select v-model="form.timeSlot" placeholder="请选择时间段" style="width: 100%" filterable allow-create>
              <el-option
                v-for="slot in presetTimeSlots"
                :key="slot"
                :label="slot"
                :value="slot"
                :disabled="isSlotDisabled(slot)"
              />
            </el-select>
            <div class="form-tip">可选择预设时段，或手动输入（格式：HH:MM-HH:MM）</div>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" :max="999" controls-position="right" />
            <span class="form-tip">数字越小越靠前</span>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Sortable from 'sortablejs';
import {
  getAppointmentTimeList,
  addAppointmentTime,
  updateAppointmentTime,
  updateAppointmentTimeStatus,
  deleteAppointmentTime,
  batchDeleteAppointmentTime,
  batchUpdateAppointmentTimeSort
} from '@/api/appointment-time';

export default {
  name: 'Admin_AppointmentTimeList',
  data() {
    const validateTimeSlot = (rule, value, callback) => {
      if (!value) { callback(new Error('请选择或输入时间段')); return; }
      const regex = /^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$/;
      if (!regex.test(value)) { callback(new Error('时间段格式不正确，请使用 HH:MM-HH:MM 格式')); return; }
      const [start, end] = value.split('-');
      if (start >= end) { callback(new Error('开始时间必须小于结束时间')); return; }
      callback();
    };

    return {
      loading: false,
      submitLoading: false,
      sortSaving: false,
      sortChanged: false,
      timeList: [],
      total: 0,
      enabledCount: 0,
      disabledCount: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', status: '' },
      dialogVisible: false,
      isEdit: false,
      editId: null,
      presetTimeSlots: [
        '08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00',
        '13:00-14:00', '14:00-15:00', '15:00-16:00', '16:00-17:00',
        '17:00-18:00', '18:00-19:00', '19:00-20:00'
      ],
      form: { id: null, timeSlot: '', sortOrder: 0, status: 1, remark: '' },
      rules: {
        timeSlot: [
          { required: true, message: '请输入时间段', trigger: 'blur' },
          { validator: validateTimeSlot, trigger: 'blur' }
        ]
      },
      sortableInstance: null
    };
  },
  computed: {
    dialogTitle() { return this.isEdit ? '编辑时间段' : '新增时间段'; }
  },
  created() { this.loadList(); },
  mounted() { this.initSortable(); },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined
        };
        const res = await getAppointmentTimeList(params);
        if (res.code === 200) {
          this.timeList = (res.data.list || []).map(item => ({ ...item, statusLoading: false }));
          this.total = res.data.total || 0;
          this.enabledCount = this.timeList.filter(item => item.status === 1).length;
          this.disabledCount = this.timeList.filter(item => item.status === 0).length;
          this.sortChanged = false;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载失败');
      } finally {
        this.loading = false;
      }
    },

    initSortable() {
      const el = this.$el.querySelector('.el-table__body-wrapper tbody');
      if (!el) return;
      if (this.sortableInstance) { this.sortableInstance.destroy(); this.sortableInstance = null; }
      this.sortableInstance = new Sortable(el, {
        animation: 300,
        onEnd: (evt) => {
          const oldIndex = evt.oldIndex;
          const newIndex = evt.newIndex;
          if (oldIndex === newIndex) return;
          const movedItem = this.timeList.splice(oldIndex, 1)[0];
          this.timeList.splice(newIndex, 0, movedItem);
          this.timeList.forEach((item, index) => { item.sortOrder = index; });
          this.$forceUpdate();
          this.sortChanged = true;
        }
      });
    },

    async saveSortOrder() {
      if (!this.sortChanged) { this.$message.info('排序未发生变化'); return; }
      this.sortSaving = true;
      try {
        const sortList = this.timeList.map((item, index) => ({ id: item.id, sortOrder: index }));
        const res = await batchUpdateAppointmentTimeSort(sortList);
        if (res.code === 200) {
          this.$message.success('排序保存成功');
          this.sortChanged = false;
          await this.loadList();
        } else {
          this.$message.error(res.message || '保存失败');
          await this.loadList();
        }
      } catch (error) {
        this.$message.error(error?.message || '保存排序失败');
        await this.loadList();
      } finally {
        this.sortSaving = false;
      }
    },

    isSlotDisabled(slotValue) {
      if (!this.isEdit) return this.timeList.some(item => item.timeSlot === slotValue);
      return this.timeList.some(item => item.timeSlot === slotValue && item.id !== this.form.id);
    },

    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() { this.searchForm = { keyword: '', status: '' }; this.page = 1; this.loadList(); },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },

    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '启用' : '禁用';
      this.$confirm(`确定要${action}时间段 "${row.timeSlot}" 吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: newStatus === 1 ? 'info' : 'warning'
      }).then(async () => {
        row.statusLoading = true;
        try {
          const res = await updateAppointmentTimeStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(res.message || `${action}成功`);
            this.loadList();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '操作失败');
        } finally {
          row.statusLoading = false;
        }
      }).catch(() => {});
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除时间段 "${row.timeSlot}" 吗？`, '警告', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteAppointmentTime(row.id);
          if (res.code === 200) {
            this.$message.success(res.message || '删除成功');
            this.loadList();
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '删除失败');
        }
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个时间段吗？`, '警告', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteAppointmentTime(ids);
          if (res.code === 200) {
            this.$message.success(res.message || '批量删除成功');
            this.selectedRows = [];
            this.loadList();
          } else {
            this.$message.error(res.message || '批量删除失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '批量删除失败');
        }
      }).catch(() => {});
    },

    handleAdd() {
      this.isEdit = false;
      this.editId = null;
      this.form = { id: null, timeSlot: '', sortOrder: this.timeList.length, status: 1, remark: '' };
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.timeForm && this.$refs.timeForm.clearValidate(); });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.editId = row.id;
      this.form = { id: row.id, timeSlot: row.timeSlot, sortOrder: row.sortOrder, status: row.status, remark: row.remark || '' };
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.timeForm && this.$refs.timeForm.clearValidate(); });
    },

    submitForm() {
      this.$refs.timeForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            res = await updateAppointmentTime(this.form);
          } else {
            res = await addAppointmentTime(this.form);
          }
          if (res.code === 200) {
            this.$message.success(res.message || (this.isEdit ? '更新成功' : '添加成功'));
            this.dialogVisible = false;
            this.loadList();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || (this.isEdit ? '更新失败' : '添加失败'));
        } finally {
          this.submitLoading = false;
        }
      });
    }
  },

  beforeDestroy() {
    if (this.sortableInstance) { this.sortableInstance.destroy(); this.sortableInstance = null; }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.appointment-time-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 统计卡片 ===== */
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
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: #fff;
  flex-shrink: 0;
}
.card-total .stat-icon    { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-enabled .stat-icon  { background: linear-gradient(135deg, #67c23a, #85ce61); }
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
.status-select { width: 100px; }
.status-select ::v-deep .el-input__inner { border-radius: 10px; }

.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
  border-radius: 10px !important; padding: 9px 20px !important; font-weight: 500;
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
  padding: 10px 20px; margin-bottom: 16px;
  display: flex; align-items: center; gap: 12px;
  color: #3d2e2a;
}
.sort-bar {
  background: linear-gradient(135deg, #edf7ee, #f0f9ef);
  border-color: #b3e0b3;
  border-left-color: #67c23a;
}
.batch-info { font-size: 13px; }
.batch-info strong { color: #f59e4b; font-size: 15px; }
.batch-bar .el-button { border-radius: 8px; padding: 7px 16px; font-weight: 500; transition: all 0.2s; }
.batch-btn-delete {
  background: rgba(245, 108, 108, 0.12) !important;
  border: 1px solid rgba(245, 108, 108, 0.35) !important;
  color: #f56c6c !important;
}
.batch-btn-delete:hover { background: rgba(245, 108, 108, 0.22) !important; border-color: #f56c6c !important; }

.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

.time-table ::v-deep .el-table__header th {
  background: #fdf8f4; color: #5c4a42; font-weight: 600;
  font-size: 12px; letter-spacing: 0.4px; padding: 15px 0; border-bottom: 2px solid #f5ece6;
}
.time-table ::v-deep .el-table__body td { padding: 15px 0; font-size: 13px; color: #3d2e2a; }
.time-table ::v-deep .el-table__body tr:hover > td { background: #fefbf8; }
.time-table ::v-deep .el-table__body tr.el-table__row--striped > td { background: #fefcf9; }
.time-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td { background: #fefbf8; }
.time-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b; border-color: #f59e4b;
}

.time-slot-text { font-weight: 600; color: #3d2e2a; }
.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { color: #a08c84; font-size: 12px; }

/* 操作按钮 */
.action-btns { display: flex; align-items: center; justify-content: center; gap: 4px; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #e8833a !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { color: #e04444 !important; }

/* 拖拽 */
.sortable-drag { opacity: 0.5; background: #f5ece6 !important; }

/* ===== 分页卡片 ===== */
.pagination-card {
  background: #fff; border-radius: 16px; margin-top: 16px;
  padding: 16px 24px; display: flex; justify-content: flex-end;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}

/* ===== 对话框 ===== */
.time-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.time-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px; margin: 0;
}
.time-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.time-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.time-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.time-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }

.time-form ::v-deep .el-form-item { margin-bottom: 18px; }
.time-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.time-form ::v-deep .el-input__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.time-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}
.time-form ::v-deep .el-radio__input.is-checked .el-radio__inner {
  background: #f59e4b; border-color: #f59e4b;
}

.form-tip { font-size: 12px; color: #a08c84; margin-left: 8px; }

.dialog-footer {
  text-align: right; padding: 16px 28px 22px; border-top: 1px solid #f5ece6;
}
.dialog-footer .el-button {
  border-radius: 10px; padding: 10px 28px; font-weight: 500; font-size: 13px;
}
.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important; color: #fff !important;
}
.dialog-footer .el-button--primary:hover { opacity: 0.88; transform: translateY(-1px); }

/* 响应式 */
@media (max-width: 768px) {
  .appointment-time-page { padding: 12px; }
  .stats-row { grid-template-columns: 1fr; gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 100%; }
}
</style>
