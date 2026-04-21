<template>
  <div class="admin-health-record">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-first-aid-kit"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ healthTotal }}</div>
            <div class="stat-label">体检记录</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-pet"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ petCount }}</div>
            <div class="stat-label">涉及宠物数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-medal"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ avgWeight }}kg</div>
            <div class="stat-label">平均体重</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增体检记录
        </el-button>
        <el-button
            v-if="selectedRows.length > 0"
            type="danger"
            plain
            @click="handleBatchDelete"
            class="batch-btn"
        >
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchKeyword"
              placeholder="搜索宠物名/主人"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input">
          </el-input>
        </div>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 体检记录表格 -->
    <el-table
        v-loading="loading"
        :data="healthList"
        stripe
        class="health-table"
        @selection-change="handleSelectionChange"
        row-key="id">
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <el-table-column label="宠物信息" min-width="180">
        <template slot-scope="scope">
          <div class="pet-info-cell">
            <div class="pet-name">{{ scope.row.petName || '--' }}</div>
            <div class="owner-name">主人：{{ scope.row.ownerNickname || scope.row.ownerName || '未知' }}</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="recordDate" label="体检日期" width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.recordDate || '--' }}
        </template>
      </el-table-column>

      <el-table-column prop="weight" label="体重(kg)" width="100" align="center">
        <template slot-scope="scope">
          {{ scope.row.weight || '--' }}
        </template>
      </el-table-column>

      <el-table-column prop="healthStatus" label="健康状况" min-width="120">
        <template slot-scope="scope">
          <el-tag :type="getHealthStatusType(scope.row.healthStatus)" size="small">
            {{ scope.row.healthStatus || '--' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="diagnosis" label="体检结果" min-width="180" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ scope.row.diagnosis || '--' }}
        </template>
      </el-table-column>

      <el-table-column prop="operatorName" label="操作员" width="100">
        <template slot-scope="scope">
          {{ scope.row.operatorName || '--' }}
        </template>
      </el-table-column>

      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ scope.row.remark || '--' }}
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          {{ scope.row.createTime || '--' }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button size="small" type="primary" plain circle class="action-icon-btn" @click="handleView(scope.row)" title="查看">
              <i class="el-icon-view"></i>
            </el-button>
            <el-button size="small" type="warning" plain circle class="action-icon-btn" @click="handleEdit(scope.row)" title="编辑">
              <i class="el-icon-edit"></i>
            </el-button>
            <el-button size="small" type="danger" plain circle class="action-icon-btn" @click="deleteHealth(scope.row.id)" title="删除">
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
          background>
      </el-pagination>
    </div>

    <!-- 新增/编辑体检记录对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="showAddDialog" width="500px" center class="record-dialog" :close-on-click-modal="false">
      <el-form :model="recordForm" :rules="recordRules" ref="recordForm" label-width="90px">
        <el-form-item label="选择宠物" prop="petId">
          <el-select v-model="recordForm.petId" placeholder="请选择宠物" filterable style="width: 100%"  :disabled="isEdit">
            <el-option
                v-for="pet in petOptions"
                :key="pet.id"
                :label="`${pet.name} (主人：${pet.userNickname || pet.userName})`"
                :value="pet.id">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="体检日期" prop="recordDate">
          <el-date-picker v-model="recordForm.recordDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
        </el-form-item>

        <el-form-item label="身高(cm)">
          <el-input-number v-model="recordForm.height" :min="0" :precision="1" style="width: 100%"></el-input-number>
        </el-form-item>

        <el-form-item label="体重(kg)">
          <el-input-number v-model="recordForm.weight" :min="0" :precision="1" style="width: 100%"></el-input-number>
        </el-form-item>

        <el-form-item label="体温(℃)">
          <el-input-number v-model="recordForm.temperature" :min="35" :max="42" :precision="1" style="width: 100%"></el-input-number>
        </el-form-item>

        <!-- 健康状况 - 详细选项 -->
        <el-form-item label="健康状况" prop="healthStatus">
          <el-select v-model="recordForm.healthStatus" placeholder="请选择健康状况" style="width: 100%">
            <el-option label="健康" value="健康">
              <div class="health-option">
                <span class="health-dot success"></span>
                <span>健康</span>
              </div>
            </el-option>
            <el-option label="良好" value="良好">
              <div class="health-option">
                <span class="health-dot success"></span>
                <span>良好</span>
              </div>
            </el-option>
            <el-option label="注意" value="注意">
              <div class="health-option">
                <span class="health-dot warning"></span>
                <span>注意（需观察）</span>
              </div>
            </el-option>
            <el-option label="一般" value="一般">
              <div class="health-option">
                <span class="health-dot warning"></span>
                <span>一般（建议复查）</span>
              </div>
            </el-option>
            <el-option label="异常" value="异常">
              <div class="health-option">
                <span class="health-dot danger"></span>
                <span>异常（需治疗）</span>
              </div>
            </el-option>
            <el-option label="疾病" value="疾病">
              <div class="health-option">
                <span class="health-dot danger"></span>
                <span>疾病（确诊）</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="体检结果">
          <el-input v-model="recordForm.diagnosis" type="textarea" rows="2" placeholder="体检结果"></el-input>
        </el-form-item>

        <el-form-item label="操作员">
          <el-input v-model="recordForm.operatorName" placeholder="请输入操作员姓名"></el-input>
        </el-form-item>

        <!-- ✅ 备注字段 - 放在 </el-form> 之前 -->
        <el-form-item label="备注">
          <el-input v-model="recordForm.remark" type="textarea" rows="2" placeholder="备注信息（选填）" maxlength="200" show-word-limit></el-input>
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="体检记录详情" :visible.sync="detailVisible" width="500px" center class="detail-dialog">
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="宠物名称">{{ currentRecord.petName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="主人">{{ currentRecord.ownerNickname || currentRecord.ownerName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="体检日期">{{ currentRecord.recordDate || '--' }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ currentRecord.height || '--' }} cm</el-descriptions-item>
        <el-descriptions-item label="体重">{{ currentRecord.weight || '--' }} kg</el-descriptions-item>
        <el-descriptions-item label="体温">{{ currentRecord.temperature || '--' }} ℃</el-descriptions-item>
        <el-descriptions-item label="健康状况">{{ currentRecord.healthStatus || '--' }}</el-descriptions-item>
        <el-descriptions-item label="体检结果">{{ currentRecord.diagnosis || '--' }}</el-descriptions-item>
        <el-descriptions-item label="操作员">{{ currentRecord.operatorName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRecord.remark || '--' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRecord.createTime || '--' }}</el-descriptions-item>
      </el-descriptions>
      <span slot="footer">
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAllHealthRecords,getAvgWeight  , deleteHealthRecord,getDistinctPetsWithHealthRecordCount , addHealthRecord, updateHealthRecord, batchDeleteHealthRecords } from '@/api/pet';
import { getAdminPetList } from '@/api/pet';

export default {
  name: 'AdminHealthRecord',
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
      healthTotal: 0,
      petCount: 0,
      avgWeight: 0,
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
        petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
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
          // ✅ 确保 ownerNickname 字段存在
          this.healthList = (res.data.list || []).map(item => ({
            ...item,
            ownerNickname: item.ownerNickname || null
          }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        console.error('加载失败:', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadStatistics() {
      try {
        // 获取体检总数（只统计正常宠物的记录）
        const healthRes = await getAllHealthRecords({ page: 1, pageSize: 1 });
        if (healthRes.code === 200) this.healthTotal = healthRes.data.total || 0;

        // 获取有体检记录的正常宠物数量（去重）
        const petCountRes = await getDistinctPetsWithHealthRecordCount();
        if (petCountRes.code === 200) this.petCount = petCountRes.data || 0;

        // ✅ 获取平均体重（基于每只宠物的最新体重）
        const avgWeightRes = await getAvgWeight();
        if (avgWeightRes.code === 200) {
          this.avgWeight = avgWeightRes.data || 0;
        }
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },

    async loadPetOptions() {
      try {
        const res = await getAdminPetList({ page: 1, pageSize: 100 });
        if (res.code === 200) {
          // ✅ 确保 userNickname 字段存在
          this.petOptions = (res.data.list || []).map(pet => ({
            ...pet,
            userNickname: pet.userNickname || pet.nickname || null
          }));
        }
      } catch (error) {
        console.error('加载宠物列表失败', error);
      }
    },

    handleAdd() {
      this.isEdit = false;
      this.editId = null;
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
        if (this.$refs.recordForm) {
          this.$refs.recordForm.clearValidate();
        }
      });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.editId = row.id;
      this.recordForm = {
        petId: row.petId,
        recordDate: row.recordDate || '',
        height: row.height || null,
        weight: row.weight || null,
        temperature: row.temperature || null,
        healthStatus: row.healthStatus || '',
        diagnosis: row.diagnosis || '',
        operatorName: row.operatorName || '',
        remark: row.remark || ''
      };
      this.showAddDialog = true;
      this.$nextTick(() => {
        if (this.$refs.recordForm) {
          this.$refs.recordForm.clearValidate();
        }
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
            res = await updateHealthRecord({...submitData, id: this.editId});
          } else {
            res = await addHealthRecord(submitData);
          }

          if (res.code === 200) {
            this.$message.success(this.isEdit ? '修改成功' : '添加成功');
            this.showAddDialog = false;
            this.loadData();
            this.loadStatistics();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('提交失败:', error);
          this.$message.error(this.isEdit ? '修改失败' : '添加失败');
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

    handleSearch() {
      this.page = 1;
      this.loadData();
    },
    handleReset() {
      this.searchKeyword = '';
      this.page = 1;
      this.loadData();
    },
    handlePageChange(page) {
      this.page = page;
      this.loadData();
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadData();
    },
    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },

    async deleteHealth(id) {
      this.$confirm('确定删除该体检记录吗？', '提示', {type: 'warning'}).then(async () => {
        await deleteHealthRecord(id);
        this.$message.success('删除成功');
        this.loadData();
        this.loadStatistics();
      }).catch(() => {
      });
    },

    // 批量删除
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要删除的记录');
        return;
      }

      const ids = this.selectedRows.map(row => row.id);
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条体检记录吗？删除后无法恢复！`, '批量删除', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteHealthRecords(ids);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadData();
            this.loadStatistics();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('批量删除失败', error);
          this.$message.error('批量删除失败');
        }
      }).catch(() => {
      });
    }
  }
};
</script>

<style scoped>
.admin-health-record {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.stat-card {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.action-bar {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin: 20px 0;
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
  align-items: center;
}

.action-right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.add-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
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
  width: 220px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.search-btn, .reset-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
}

.health-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.health-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
}

.pet-info-cell {
  line-height: 1.4;
}

.pet-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.owner-name {
  font-size: 12px;
  color: #909399;
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
}

.record-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.record-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.record-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.record-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

.detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

/* 健康选项样式 */
.health-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.health-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.health-dot.success {
  background-color: #67c23a;
  box-shadow: 0 0 4px #67c23a;
}

.health-dot.warning {
  background-color: #e6a23c;
  box-shadow: 0 0 4px #e6a23c;
}

.health-dot.danger {
  background-color: #f56c6c;
  box-shadow: 0 0 4px #f56c6c;
}

@media (max-width: 768px) {
  .admin-health-record {
    padding: 12px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 160px;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>