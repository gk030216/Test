<template>
  <div class="admin-pet-health">
<!--    &lt;!&ndash; 页面头部 &ndash;&gt;-->
<!--    <div class="page-header">-->
<!--      <h2>健康记录管理</h2>-->
<!--      <p>管理所有宠物的疫苗和体检记录</p>-->
<!--    </div>-->

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-s-custom"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ vaccineTotal }}</div>
            <div class="stat-label">疫苗记录</div>
          </div>
        </el-card>
      </el-col>
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
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增记录
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
        <el-select
            v-model="activeTab"
            placeholder="记录类型"
            size="medium"
            class="status-select"
            @change="handleTabChange">
          <el-option label="疫苗记录" value="vaccine"></el-option>
          <el-option label="体检记录" value="health"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 疫苗记录表格 -->
    <div v-if="activeTab === 'vaccine'">
      <el-table
          v-loading="loading"
          :data="vaccineList"
          stripe
          class="health-table"
          @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

        <el-table-column label="宠物信息" min-width="180">
          <template slot-scope="scope">
            <div class="pet-info-cell">
              <div class="pet-name">{{ scope.row.petName || '--' }}</div>
              <div class="owner-name">主人：{{ scope.row.ownerName || '未知' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="vaccineName" label="疫苗名称" min-width="150">
          <template slot-scope="scope">
            <span class="vaccine-name">{{ scope.row.vaccineName || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="接种日期" width="120" align="center">
          <template slot-scope="scope">
            <span class="date-text">{{ scope.row.vaccineDate || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="下次接种" width="120" align="center">
          <template slot-scope="scope">
            <span class="date-text" :class="{ 'urgent': isUrgent(scope.row.nextDate) }">
              {{ scope.row.nextDate || '--' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="hospital" label="接种医院" min-width="150">
          <template slot-scope="scope">
            {{ scope.row.hospital || '--' }}
          </template>
        </el-table-column>

        <el-table-column prop="doctor" label="医生" width="100">
          <template slot-scope="scope">
            {{ scope.row.doctor || '--' }}
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ scope.row.createTime || '--' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-buttons">
              <el-button size="small" type="danger" plain circle class="action-icon-btn" @click="deleteVaccine(scope.row.id)" title="删除">
                <i class="el-icon-delete"></i>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 体检记录表格 -->
    <div v-else>
      <el-table
          v-loading="loading"
          :data="healthList"
          stripe
          class="health-table"
          @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

        <el-table-column label="宠物信息" min-width="180">
          <template slot-scope="scope">
            <div class="pet-info-cell">
              <div class="pet-name">{{ scope.row.petName || '--' }}</div>
              <div class="owner-name">主人：{{ scope.row.ownerName || '未知' }}</div>
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

        <el-table-column prop="diagnosis" label="诊断结果" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            {{ scope.row.diagnosis || '--' }}
          </template>
        </el-table-column>

        <el-table-column prop="doctor" label="医生" width="100">
          <template slot-scope="scope">
            {{ scope.row.doctor || '--' }}
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ scope.row.createTime || '--' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template slot-scope="scope">
            <div class="action-buttons">
              <el-button size="small" type="danger" plain circle class="action-icon-btn" @click="deleteHealth(scope.row.id)" title="删除">
                <i class="el-icon-delete"></i>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

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

    <!-- 新增记录对话框 -->
    <el-dialog title="新增记录" :visible.sync="showAddDialog" width="500px" center class="record-dialog">
      <el-form :model="recordForm" :rules="recordRules" ref="recordForm" label-width="90px">
        <el-form-item label="记录类型" prop="type">
          <el-radio-group v-model="recordForm.type">
            <el-radio label="vaccine">疫苗记录</el-radio>
            <el-radio label="health">体检记录</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选择宠物" prop="petId">
          <el-select v-model="recordForm.petId" placeholder="请选择宠物" filterable style="width: 100%">
            <el-option
                v-for="pet in petOptions"
                :key="pet.id"
                :label="`${pet.name} (${pet.userName})`"
                :value="pet.id">
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 疫苗记录表单 -->
        <template v-if="recordForm.type === 'vaccine'">
          <el-form-item label="疫苗名称" prop="vaccineName">
            <el-input v-model="recordForm.vaccineName" placeholder="请输入疫苗名称"></el-input>
          </el-form-item>
          <el-form-item label="接种日期" prop="vaccineDate">
            <el-date-picker v-model="recordForm.vaccineDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
          </el-form-item>
          <el-form-item label="下次接种">
            <el-date-picker v-model="recordForm.nextDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
          </el-form-item>
          <el-form-item label="接种医院">
            <el-input v-model="recordForm.hospital" placeholder="请输入医院名称"></el-input>
          </el-form-item>
          <el-form-item label="医生">
            <el-input v-model="recordForm.doctor" placeholder="请输入医生姓名"></el-input>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="recordForm.remark" type="textarea" rows="2" placeholder="备注信息"></el-input>
          </el-form-item>
        </template>

        <!-- 体检记录表单 -->
        <template v-else>
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
          <el-form-item label="健康状况">
            <el-input v-model="recordForm.healthStatus" placeholder="如：良好"></el-input>
          </el-form-item>
          <el-form-item label="诊断结果">
            <el-input v-model="recordForm.diagnosis" type="textarea" rows="2" placeholder="诊断结果"></el-input>
          </el-form-item>
          <el-form-item label="医生">
            <el-input v-model="recordForm.doctor" placeholder="请输入医生姓名"></el-input>
          </el-form-item>
        </template>
      </el-form>

      <span slot="footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAllVaccineRecords, getAllHealthRecords, deleteVaccineRecord, deleteHealthRecord, addVaccineRecord, addHealthRecord } from '@/api/pet';
import { getAdminPetList } from '@/api/pet';

export default {
  name: 'AdminPetHealth',
  data() {
    return {
      loading: false,
      submitLoading: false,
      activeTab: 'vaccine',
      vaccineList: [],
      healthList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      searchKeyword: '',
      selectedRows: [],
      vaccineTotal: 0,
      healthTotal: 0,
      petCount: 0,
      showAddDialog: false,
      petOptions: [],
      recordForm: {
        type: 'vaccine',
        petId: null,
        vaccineName: '',
        vaccineDate: '',
        nextDate: '',
        hospital: '',
        doctor: '',
        remark: '',
        recordDate: '',
        height: null,
        weight: null,
        temperature: null,
        healthStatus: '',
        diagnosis: ''
      },
      recordRules: {
        petId: [{ required: true, message: '请选择宠物', trigger: 'change' }]
      }
    };
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
        if (this.activeTab === 'vaccine') {
          const res = await getAllVaccineRecords({
            page: this.page,
            pageSize: this.pageSize,
            keyword: this.searchKeyword || undefined
          });
          if (res.code === 200) {
            this.vaccineList = res.data.list || [];
            this.total = res.data.total || 0;
          }
        } else {
          const res = await getAllHealthRecords({
            page: this.page,
            pageSize: this.pageSize,
            keyword: this.searchKeyword || undefined
          });
          if (res.code === 200) {
            this.healthList = res.data.list || [];
            this.total = res.data.total || 0;
          }
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
        const vaccineRes = await getAllVaccineRecords({ page: 1, pageSize: 1 });
        if (vaccineRes.code === 200) this.vaccineTotal = vaccineRes.data.total || 0;

        const healthRes = await getAllHealthRecords({ page: 1, pageSize: 1 });
        if (healthRes.code === 200) this.healthTotal = healthRes.data.total || 0;

        const petRes = await getAdminPetList({ page: 1, pageSize: 100 });
        if (petRes.code === 200) this.petCount = petRes.data.total || 0;
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },
    async loadPetOptions() {
      try {
        const res = await getAdminPetList({ page: 1, pageSize: 100 });
        if (res.code === 200) {
          this.petOptions = res.data.list || [];
        }
      } catch (error) {
        console.error('加载宠物列表失败', error);
      }
    },
    handleAdd() {
      this.recordForm = {
        type: this.activeTab,
        petId: null,
        vaccineName: '',
        vaccineDate: '',
        nextDate: '',
        hospital: '',
        doctor: '',
        remark: '',
        recordDate: '',
        height: null,
        weight: null,
        temperature: null,
        healthStatus: '',
        diagnosis: ''
      };
      this.showAddDialog = true;
    },
    async submitRecord() {
      this.$refs.recordForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          if (this.recordForm.type === 'vaccine') {
            res = await addVaccineRecord({
              petId: this.recordForm.petId,
              vaccineName: this.recordForm.vaccineName,
              vaccineDate: this.recordForm.vaccineDate,
              nextDate: this.recordForm.nextDate,
              hospital: this.recordForm.hospital,
              doctor: this.recordForm.doctor,
              remark: this.recordForm.remark
            });
          } else {
            res = await addHealthRecord({
              petId: this.recordForm.petId,
              recordDate: this.recordForm.recordDate,
              height: this.recordForm.height,
              weight: this.recordForm.weight,
              temperature: this.recordForm.temperature,
              healthStatus: this.recordForm.healthStatus,
              diagnosis: this.recordForm.diagnosis,
              doctor: this.recordForm.doctor
            });
          }
          if (res.code === 200) {
            this.$message.success('添加成功');
            this.showAddDialog = false;
            this.loadData();
            this.loadStatistics();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('添加失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },
    isUrgent(date) {
      if (!date) return false;
      const today = new Date();
      const nextDate = new Date(date);
      const diffDays = Math.ceil((nextDate - today) / (1000 * 60 * 60 * 24));
      return diffDays <= 7 && diffDays >= 0;
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
    handleTabChange() { this.page = 1; this.loadData(); },
    handlePageChange(page) { this.page = page; this.loadData(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadData(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },
    async deleteVaccine(id) {
      this.$confirm('确定删除该疫苗记录吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteVaccineRecord(id);
        this.$message.success('删除成功');
        this.loadData();
        this.loadStatistics();
      }).catch(() => {});
    },
    async deleteHealth(id) {
      this.$confirm('确定删除该体检记录吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteHealthRecord(id);
        this.$message.success('删除成功');
        this.loadData();
        this.loadStatistics();
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.admin-pet-health {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 页面头部 */
.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 统计卡片 */
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

/* 操作栏 */
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

.status-select {
  width: 120px;
}

.status-select ::v-deep .el-input__inner {
  border-radius: 8px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.search-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.reset-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

/* 表格样式 */
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
  border-bottom: 1px solid #eef2f6;
}

.health-table ::v-deep .el-table__body td {
  padding: 14px 0;
  font-size: 13px;
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

.vaccine-name {
  font-weight: 500;
  color: #409EFF;
}

.date-text {
  color: #606266;
}

.date-text.urgent {
  color: #f56c6c;
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

.record-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.record-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

/* 响应式 */
@media (max-width: 768px) {
  .admin-pet-health {
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
    width: 160px;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>