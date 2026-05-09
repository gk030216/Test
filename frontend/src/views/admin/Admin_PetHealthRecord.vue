<template>
  <div class="health-record-page">
    <!-- 统计概览卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-first-aid-kit"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">体检记录</div>
          <div class="stat-value">{{ stats.healthTotal }}</div>
          <div class="stat-sub">平台体检记录总量</div>
        </div>
      </div>
      <div class="stat-card card-pet">
        <div class="stat-icon">
          <i class="el-icon-s-data"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">涉及宠物数</div>
          <div class="stat-value">{{ stats.petCount }}</div>
          <div class="stat-sub">有体检记录的宠物</div>
        </div>
      </div>
      <div class="stat-card card-weight">
        <div class="stat-icon">
          <i class="el-icon-medal"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">平均体重</div>
          <div class="stat-value">{{ stats.avgWeight }}<small>kg</small></div>
          <div class="stat-sub">最新体重平均值</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索宠物名 / 主人..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button type="primary" size="medium" @click="handleAdd" class="warm-btn">
          <i class="el-icon-plus" /> 新增体检记录
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

    <!-- 体检记录表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="healthList"
        stripe
        row-key="id"
        class="health-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="宠物信息" min-width="110">
          <template slot-scope="scope">
            <div class="pet-cell">
              <span class="pet-name">{{ scope.row.petName || '--' }}</span>
              <span class="pet-owner">主人：{{ scope.row.ownerNickname || scope.row.ownerName || '未知' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="体检日期" width="120" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.recordDate || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="体重" width="90" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.weight ? scope.row.weight + ' kg' : '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="健康状况" min-width="120">
          <template slot-scope="scope">
            <el-tag :type="getHealthStatusType(scope.row.healthStatus)" size="small" effect="plain">
              {{ scope.row.healthStatus || '--' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="体检结果" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.diagnosis || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="operatorName" label="操作员" width="100" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.operatorName || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="备注" min-width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.remark || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-text cell-time">{{ scope.row.createTime || '--' }}</span>
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
              <el-button type="text" size="small" @click="deleteHealth(scope.row.id)" class="act-del">
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
      :visible.sync="showAddDialog"
      width="650px"
      :close-on-click-modal="false"
      class="record-dialog"
      center
    >
      <div class="dialog-body">
        <el-form :model="recordForm" :rules="recordRules" ref="recordForm" label-width="90px" class="record-form">
          <el-form-item label="选择宠物" prop="petId">
            <el-select v-model="recordForm.petId" placeholder="请选择宠物" filterable style="width: 100%" :disabled="isEdit">
              <el-option
                v-for="pet in petOptions"
                :key="pet.id"
                :label="`${pet.name} (主人：${pet.userNickname || pet.userName})`"
                :value="pet.id"
              />
            </el-select>
            <div class="form-tip" v-if="isEdit">编辑时不可修改宠物</div>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="体检日期" prop="recordDate">
                <el-date-picker v-model="recordForm.recordDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="体重(kg)">
                <el-input-number v-model="recordForm.weight" :min="0" :precision="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="身高(cm)">
                <el-input-number v-model="recordForm.height" :min="0" :precision="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="体温(℃)">
                <el-input-number v-model="recordForm.temperature" :min="35" :max="42" :precision="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="健康状况" prop="healthStatus">
            <el-select v-model="recordForm.healthStatus" placeholder="请选择健康状况" style="width: 100%">
              <el-option label="健康" value="健康" />
              <el-option label="良好" value="良好" />
              <el-option label="注意（需观察）" value="注意" />
              <el-option label="一般（建议复查）" value="一般" />
              <el-option label="异常（需治疗）" value="异常" />
              <el-option label="疾病（确诊）" value="疾病" />
            </el-select>
          </el-form-item>

          <el-form-item label="体检结果">
            <el-input v-model="recordForm.diagnosis" type="textarea" :rows="2" placeholder="体检结果" maxlength="200" show-word-limit />
          </el-form-item>

          <el-form-item label="操作员">
            <el-input v-model="recordForm.operatorName" placeholder="请输入操作员姓名" />
          </el-form-item>

          <el-form-item label="备注">
            <el-input v-model="recordForm.remark" type="textarea" :rows="2" placeholder="备注信息（选填）" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showAddDialog = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitLoading" size="medium">确定</el-button>
      </span>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      title="体检记录详情"
      :visible.sync="detailVisible"
      width="680px"
      class="detail-dialog"
      center
    >
      <div class="pet-detail" v-if="currentRecord">
        <div class="detail-body detail-grid">
          <div class="info-item"><span class="info-label">宠物名称</span><span class="info-value">{{ currentRecord.petName || '--' }}</span></div>
          <div class="info-item"><span class="info-label">主人</span><span class="info-value">{{ currentRecord.ownerNickname || currentRecord.ownerName || '--' }}</span></div>
          <div class="info-item"><span class="info-label">体检日期</span><span class="info-value">{{ currentRecord.recordDate || '--' }}</span></div>
          <div class="info-item"><span class="info-label">身高</span><span class="info-value">{{ currentRecord.height ? currentRecord.height + ' cm' : '--' }}</span></div>
          <div class="info-item"><span class="info-label">体重</span><span class="info-value">{{ currentRecord.weight ? currentRecord.weight + ' kg' : '--' }}</span></div>
          <div class="info-item"><span class="info-label">体温</span><span class="info-value">{{ currentRecord.temperature ? currentRecord.temperature + ' ℃' : '--' }}</span></div>
          <div class="info-item"><span class="info-label">健康状况</span><span class="info-value">{{ currentRecord.healthStatus || '--' }}</span></div>
          <div class="info-item"><span class="info-label">体检结果</span><span class="info-value">{{ currentRecord.diagnosis || '--' }}</span></div>
          <div class="info-item"><span class="info-label">操作员</span><span class="info-value">{{ currentRecord.operatorName || '--' }}</span></div>
          <div class="info-item"><span class="info-label">备注</span><span class="info-value">{{ currentRecord.remark || '--' }}</span></div>
          <div class="info-item"><span class="info-label">创建时间</span><span class="info-value">{{ currentRecord.createTime || '--' }}</span></div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAllHealthRecords, getAvgWeight, deleteHealthRecord, getDistinctPetsWithHealthRecordCount, addHealthRecord, updateHealthRecord, batchDeleteHealthRecords } from '@/api/pet';
import { getAdminPetList } from '@/api/pet';

export default {
  name: 'Admin_PetHealthRecord',
  data() {
    return {
      loading: false,
      submitLoading: false,
      healthList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      searchKeyword: '',
      selectedRows: [],
      stats: {
        healthTotal: 0,
        petCount: 0,
        avgWeight: 0
      },
      showAddDialog: false,
      detailVisible: false,
      isEdit: false,
      editId: null,
      currentRecord: null,
      petOptions: [],
      recordForm: {
        petId: null,
        recordDate: '',
        height: null,
        weight: null,
        temperature: null,
        healthStatus: '',
        diagnosis: '',
        operatorName: '',
        remark: ''
      },
      recordRules: {
        recordDate: [{ required: true, message: '请选择体检日期', trigger: 'change' }]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑体检记录' : '新增体检记录';
    }
  },
  created() {
    this.loadData();
    this.loadPetOptions();
    this.loadStatistics();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const res = await getAllHealthRecords({
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined
        });
        if (res.code === 200) {
          this.healthList = (res.data.list || []).map(item => ({
            ...item,
            ownerNickname: item.ownerNickname || null
          }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadStatistics() {
      try {
        const healthRes = await getAllHealthRecords({ page: 1, pageSize: 1 });
        if (healthRes.code === 200) this.stats.healthTotal = healthRes.data.total || 0;

        const petCountRes = await getDistinctPetsWithHealthRecordCount();
        if (petCountRes.code === 200) this.stats.petCount = petCountRes.data || 0;

        const avgWeightRes = await getAvgWeight();
        if (avgWeightRes.code === 200) this.stats.avgWeight = avgWeightRes.data || 0;
      } catch (error) {
        // stats failure is non-critical
      }
    },

    async loadPetOptions() {
      try {
        const res = await getAdminPetList({ page: 1, pageSize: 100 });
        if (res.code === 200) {
          this.petOptions = (res.data.list || []).map(pet => ({
            ...pet,
            userNickname: pet.userNickname || pet.nickname || null
          }));
        }
      } catch (error) {
        this.$message.error(error?.message || '加载宠物列表失败');
      }
    },

    handleAdd() {
      this.isEdit = false;
      this.editId = null;
      this.recordRules = {
        petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
        recordDate: [{ required: true, message: '请选择体检日期', trigger: 'change' }]
      };
      this.recordForm = {
        petId: null,
        recordDate: '',
        height: null,
        weight: null,
        temperature: null,
        healthStatus: '',
        diagnosis: '',
        operatorName: '',
        remark: ''
      };
      this.showAddDialog = true;
      this.$nextTick(() => {
        if (this.$refs.recordForm) this.$refs.recordForm.clearValidate();
      });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.editId = row.id;
      this.recordRules = {
        recordDate: [{ required: true, message: '请选择体检日期', trigger: 'change' }]
      };
      this.recordForm = {
        petId: row.petId,
        recordDate: row.recordDate || '',
        height: row.height != null ? row.height : null,
        weight: row.weight != null ? row.weight : null,
        temperature: row.temperature != null ? row.temperature : null,
        healthStatus: row.healthStatus || '',
        diagnosis: row.diagnosis || '',
        operatorName: row.operatorName || '',
        remark: row.remark || ''
      };
      this.showAddDialog = true;
      this.$nextTick(() => {
        if (this.$refs.recordForm) this.$refs.recordForm.clearValidate();
      });
    },

    handleView(row) {
      this.currentRecord = row;
      this.detailVisible = true;
    },

    async submitRecord() {
      this.$refs.recordForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          const submitData = {
            petId: this.recordForm.petId,
            recordDate: this.recordForm.recordDate,
            height: this.recordForm.height,
            weight: this.recordForm.weight,
            temperature: this.recordForm.temperature,
            healthStatus: this.recordForm.healthStatus,
            diagnosis: this.recordForm.diagnosis,
            operatorName: this.recordForm.operatorName,
            remark: this.recordForm.remark || ''
          };
          if (this.isEdit) {
            res = await updateHealthRecord({ ...submitData, id: this.editId });
          } else {
            res = await addHealthRecord(submitData);
          }
          if (res.code === 200) {
            this.$message.success(res.message || (this.isEdit ? '修改成功' : '添加成功'));
            this.showAddDialog = false;
            this.loadData();
            this.loadStatistics();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || (this.isEdit ? '修改失败' : '添加失败'));
        } finally {
          this.submitLoading = false;
        }
      });
    },

    getHealthStatusType(status) {
      if (!status) return 'info';
      if (status.includes('良好') || status.includes('健康')) return 'success';
      if (status.includes('注意') || status.includes('一般')) return 'warning';
      if (status.includes('异常') || status.includes('疾病')) return 'danger';
      return 'info';
    },

    handleSearch() { this.page = 1; this.loadData(); },
    handleReset() { this.searchKeyword = ''; this.page = 1; this.loadData(); },
    handlePageChange(page) { this.page = page; this.loadData(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadData(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    async deleteHealth(id) {
      try {
        await this.$confirm('确定删除该体检记录吗？', '提示', { type: 'warning' });
        const res = await deleteHealthRecord(id);
        if (res.code === 200) {
          this.$message.success(res.message || '删除成功');
          this.loadData();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error?.message || '删除失败');
        }
      }
    },

    async handleBatchDelete() {
      const ids = this.selectedRows.map(row => row.id);
      try {
        await this.$confirm(`确定删除选中的 ${this.selectedRows.length} 条体检记录吗？`, '提示', { type: 'warning' });
        const res = await batchDeleteHealthRecords(ids);
        if (res.code === 200) {
          this.$message.success(res.message || '删除成功');
          this.selectedRows = [];
          this.loadData();
          this.loadStatistics();
        } else {
          this.$message.error(res.message || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error?.message || '删除失败');
        }
      }
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.health-record-page {
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
  background: #fff;
  border-radius: 16px;
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.05);
  transition: transform 0.25s, box-shadow 0.25s;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(61, 46, 42, 0.10);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}
.card-total .stat-icon  { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-pet .stat-icon    { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-weight .stat-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }

.stat-info { flex: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 5px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3d2e2a; letter-spacing: -0.5px; }
.stat-value small { font-size: 16px; font-weight: 500; color: #a08c84; margin-left: 4px; }
.stat-sub { font-size: 12px; color: #c0c4cc; margin-top: 3px; }

/* ===== 搜索筛选栏 ===== */
.filter-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}
.filter-left, .filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.keyword-input { width: 260px; }

.keyword-input ::v-deep .el-input__inner {
  border-radius: 8px;
  border: 1px solid #f5ece6;
}
.keyword-input ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 2px rgba(245, 158, 75, 0.12);
}

.warm-btn {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  border: none !important;
  color: white !important;
  border-radius: 8px !important;
  font-weight: 500;
  padding: 9px 20px;
  transition: all 0.3s;
}
.warm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 158, 75, 0.35);
}

.reset-btn {
  border-radius: 8px;
  padding: 9px 20px;
  font-weight: 500;
  color: #a08c84;
  border: 1px solid #f5ece6;
}

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
.batch-btn-delete {
  background: rgba(245, 108, 108, 0.12) !important;
  border: 1px solid rgba(245, 108, 108, 0.35) !important;
  color: #f56c6c !important;
}
.batch-btn-delete:hover {
  background: rgba(245, 108, 108, 0.22) !important;
  border-color: #f56c6c !important;
}
.batch-bar-enter-active, .batch-bar-leave-active { transition: all 0.3s; }
.batch-bar-enter, .batch-bar-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
}
.health-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #3d2e2a;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
  border-bottom: 1px solid #f5ece6;
}
.health-table ::v-deep .el-table__body td {
  padding: 12px 0;
  font-size: 13px;
}
.health-table ::v-deep .el-table__body tr:hover > td {
  background: #fef9f5 !important;
}
.health-table ::v-deep .el-table__body tr.el-table__row--striped > td {
  background: #fefcfa;
}

.pet-cell { display: flex; flex-direction: column; gap: 3px; }
.pet-name { font-weight: 600; color: #3d2e2a; font-size: 14px; }
.pet-owner { font-size: 12px; color: #a08c84; }

.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { font-size: 12px; color: #a08c84; }

.action-btns { display: flex; justify-content: center; gap: 4px; }
.act-view { color: #909399 !important; }
.act-view:hover { color: #f59e4b !important; }
.act-edit { color: #f59e4b !important; }
.act-edit:hover { color: #f0826a !important; }
.act-del { color: #f56c6c !important; }
.act-del:hover { opacity: 0.8; }

/* ===== 分页卡片 ===== */
.pagination-card {
  margin-top: 20px;
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  box-shadow: 0 2px 12px rgba(61, 46, 42, 0.04);
  display: flex;
  justify-content: flex-end;
}

/* ===== 对话框样式 ===== */
.record-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}
.record-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 16px 24px;
  margin: 0;
}
.record-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}
.record-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}
.record-dialog ::v-deep .el-dialog__body {
  padding: 20px 24px;
}

.dialog-body { padding: 0; }

.record-form ::v-deep .el-form-item {
  margin-bottom: 14px;
}
.record-form ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #3d2e2a;
  font-size: 13px;
}
.record-form ::v-deep .el-input__inner {
  border-radius: 8px;
  border: 1px solid #f5ece6;
  transition: all 0.3s;
}
.record-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 2px rgba(245, 158, 75, 0.15);
}
.record-form ::v-deep .el-textarea__inner {
  border-radius: 8px;
  border: 1px solid #f5ece6;
}
.record-form ::v-deep .el-textarea__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 2px rgba(245, 158, 75, 0.15);
}

.form-tip {
  font-size: 12px;
  color: #a08c84;
  margin-top: 4px;
}

.dialog-footer {
  text-align: right;
  padding: 12px 24px 16px;
  border-top: 1px solid #f5ece6;
}
.dialog-footer .el-button {
  border-radius: 8px;
  padding: 9px 24px;
  font-weight: 500;
}
.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  border: none;
}
.dialog-footer .el-button--primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 详情对话框 */
.detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}
.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 16px 24px;
}
.detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}
.detail-dialog ::v-deep .el-dialog__close {
  color: white;
}

.detail-body { padding: 0 10px; }
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 24px;
}
.info-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #f5ece6;
}
.info-label {
  width: 75px;
  color: #a08c84;
  flex-shrink: 0;
  font-size: 13px;
}
.info-value {
  flex: 1;
  color: #3d2e2a;
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 768px) {
  .health-record-page { padding: 12px; }
  .stats-row { grid-template-columns: 1fr; gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 200px; }
  .pagination-card { justify-content: center; }
  .detail-grid { grid-template-columns: 1fr; }
}
</style>
