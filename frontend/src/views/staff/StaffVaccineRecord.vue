<template>
  <div class="admin-vaccine-record">
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
          <div class="stat-icon"><i class="el-icon-pet"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ petCount }}</div>
            <div class="stat-label">涉及宠物数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon"><i class="el-icon-warning"></i></div>
          <div class="stat-info">
            <div class="stat-number">{{ upcomingCount }}</div>
            <div class="stat-label">即将到期</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增疫苗记录
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
              placeholder="搜索宠物名/主人/疫苗"
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

    <!-- 疫苗记录表格 -->
    <el-table
        v-loading="loading"
        :data="vaccineList"
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

      <el-table-column prop="operatorName" label="操作员" width="100">
        <template slot-scope="scope">
          {{ scope.row.operatorName || '--' }}
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
            <el-button size="small" type="danger" plain circle class="action-icon-btn" @click="deleteVaccine(scope.row.id)" title="删除">
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

    <!-- 新增/编辑疫苗记录对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="showAddDialog" width="550px" center class="record-dialog" :close-on-click-modal="false">
      <el-form :model="recordForm" :rules="recordRules" ref="recordForm" label-width="90px">
        <!-- 选择宠物 -->
        <el-form-item label="选择宠物" prop="petId">
          <el-select v-model="recordForm.petId" placeholder="请选择宠物" filterable style="width: 100%">
            <el-option
                v-for="pet in petOptions"
                :key="pet.id"
                :label="`${pet.name} (主人：${pet.userName})`"
                :value="pet.id">
            </el-option>
          </el-select>
        </el-form-item>

        <!-- ✅ 选择疫苗（从库存中选择） -->
        <el-form-item label="选择疫苗" prop="inventoryId">
          <el-select
              v-model="recordForm.inventoryId"
              placeholder="请选择疫苗"
              filterable
              style="width: 100%"
              @change="handleVaccineChange"
              :disabled="isEdit">
            <el-option
                v-for="vaccine in vaccineOptions"
                :key="vaccine.id"
                :label="`${vaccine.itemName} (库存: ${vaccine.stock})`"
                :value="vaccine.id"
                :disabled="vaccine.stock <= 0">
              <div class="vaccine-option">
                <span>{{ vaccine.itemName }}</span>
                <span :class="vaccine.stock <= 0 ? 'stock-out' : (vaccine.stock <= 5 ? 'stock-low' : 'stock-normal')">
                  库存: {{ vaccine.stock }}
                </span>
              </div>
            </el-option>
          </el-select>
          <div class="form-tip" v-if="selectedVaccineStock !== null">
            当前库存: <span :class="selectedVaccineStock <= 0 ? 'stock-out' : (selectedVaccineStock <= 5 ? 'stock-low' : 'stock-normal')">{{ selectedVaccineStock }}</span>
          </div>
        </el-form-item>

        <!-- ✅ 疫苗名称（自动填充，只读） -->
        <el-form-item label="疫苗名称" prop="vaccineName">
          <el-input v-model="recordForm.vaccineName" placeholder="自动获取" disabled></el-input>
        </el-form-item>

        <el-form-item label="接种日期" prop="vaccineDate">
          <el-date-picker v-model="recordForm.vaccineDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
        </el-form-item>

        <el-form-item label="下次接种">
          <el-date-picker v-model="recordForm.nextDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
        </el-form-item>

        <el-form-item label="操作员">
          <el-input v-model="recordForm.operatorName" placeholder="请输入操作员姓名"></el-input>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="recordForm.remark" type="textarea" rows="2" placeholder="备注信息"></el-input>
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="疫苗记录详情" :visible.sync="detailVisible" width="500px" center class="detail-dialog">
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="宠物名称">{{ currentRecord.petName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="主人">{{ currentRecord.ownerName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="疫苗名称">{{ currentRecord.vaccineName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="接种日期">{{ currentRecord.vaccineDate || '--' }}</el-descriptions-item>
        <el-descriptions-item label="下次接种">{{ currentRecord.nextDate || '--' }}</el-descriptions-item>
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
import { getAllVaccineRecords, deleteVaccineRecord, addVaccineRecord, getDistinctPetsWithVaccineCount, updateVaccineRecord, getUpcomingPetsCount, batchDeleteVaccineRecords } from '@/api/pet';
import { getInventoryList } from '@/api/inventory';
import { getAdminPetList } from '@/api/pet';

export default {
  name: 'AdminVaccineRecord',
  data() {
    return {
      loading: false,
      submitLoading: false,
      vaccineList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      searchKeyword: '',
      selectedRows: [],
      vaccineTotal: 0,
      petCount: 0,
      upcomingCount: 0,
      showAddDialog: false,
      detailVisible: false,
      isEdit: false,
      editId: null,
      currentRecord: null,
      petOptions: [],
      vaccineOptions: [],      // ✅ 疫苗库存选项
      selectedVaccineStock: null,  // ✅ 当前选中疫苗的库存
      recordForm: {
        petId: null,
        inventoryId: null,     // ✅ 新增：库存ID
        vaccineName: '',
        vaccineDate: '',
        nextDate: '',
        operatorName: '',
        remark: ''
      },
      recordRules: {
        petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
        inventoryId: [{ required: true, message: '请选择疫苗', trigger: 'change' }],
        vaccineDate: [{ required: true, message: '请选择接种日期', trigger: 'change' }]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑疫苗记录' : '新增疫苗记录';
    }
  },
  created() {
    this.loadData();
    this.loadPetOptions();
    this.loadStatistics();
    this.loadVaccineOptions();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const res = await getAllVaccineRecords({
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined
        });
        if (res.code === 200) {
          this.vaccineList = res.data.list || [];
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
        const vaccineRes = await getAllVaccineRecords({ page: 1, pageSize: 1 });
        if (vaccineRes.code === 200) this.vaccineTotal = vaccineRes.data.total || 0;

        const petCountRes = await getDistinctPetsWithVaccineCount();
        if (petCountRes.code === 200) this.petCount = petCountRes.data || 0;

        const upcomingRes = await getUpcomingPetsCount();
        if (upcomingRes.code === 200) this.upcomingCount = upcomingRes.data || 0;
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

    // ✅ 加载疫苗库存选项
    async loadVaccineOptions() {
      try {
        const res = await getInventoryList({
          page: 1,
          pageSize: 100,
          type: 'vaccine'
        });
        if (res.code === 200) {
          this.vaccineOptions = res.data.list || [];
        }
      } catch (error) {
        console.error('加载疫苗列表失败', error);
      }
    },

    // ✅ 选择疫苗时触发
    handleVaccineChange(inventoryId) {
      const selected = this.vaccineOptions.find(v => v.id === inventoryId);
      if (selected) {
        this.recordForm.vaccineName = selected.itemName;
        this.selectedVaccineStock = selected.stock;
      } else {
        this.recordForm.vaccineName = '';
        this.selectedVaccineStock = null;
      }
    },

    handleAdd() {
      this.isEdit = false;
      this.editId = null;
      this.recordForm = {
        petId: null,
        inventoryId: null,
        vaccineName: '',
        vaccineDate: '',
        nextDate: '',
        operatorName: '',
        remark: ''
      };
      this.selectedVaccineStock = null;
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
        inventoryId: row.inventoryId || null,
        vaccineName: row.vaccineName || '',
        vaccineDate: row.vaccineDate || '',
        nextDate: row.nextDate || '',
        operatorName: row.operatorName || '',
        remark: row.remark || ''
      };
      // 设置选中的库存显示
      if (row.inventoryId) {
        const selected = this.vaccineOptions.find(v => v.id === row.inventoryId);
        this.selectedVaccineStock = selected ? selected.stock : null;
      } else {
        this.selectedVaccineStock = null;
      }
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

        // 检查库存
        if (this.recordForm.inventoryId) {
          const selected = this.vaccineOptions.find(v => v.id === this.recordForm.inventoryId);
          if (selected && selected.stock <= 0) {
            this.$message.error('该疫苗库存不足，无法添加记录');
            return;
          }
        }

        this.submitLoading = true;
        try {
          let res;
          const submitData = {
            petId: this.recordForm.petId,
            inventoryId: this.recordForm.inventoryId,
            vaccineName: this.recordForm.vaccineName,
            vaccineDate: this.recordForm.vaccineDate,
            nextDate: this.recordForm.nextDate,
            operatorName: this.recordForm.operatorName,
            remark: this.recordForm.remark
          };

          if (this.isEdit) {
            res = await updateVaccineRecord({ ...submitData, id: this.editId });
          } else {
            res = await addVaccineRecord(submitData);
          }

          if (res.code === 200) {
            this.$message.success(this.isEdit ? '修改成功' : '添加成功');
            this.showAddDialog = false;
            this.loadData();
            this.loadStatistics();
            this.loadVaccineOptions();  // ✅ 刷新库存列表
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('提交失败:', error);
          this.$message.error(error.message || (this.isEdit ? '修改失败' : '添加失败'));
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

    handleSearch() { this.page = 1; this.loadData(); },
    handleReset() { this.searchKeyword = ''; this.page = 1; this.loadData(); },
    handlePageChange(page) { this.page = page; this.loadData(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadData(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    async deleteVaccine(id) {
      this.$confirm('确定删除该疫苗记录吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteVaccineRecord(id);
        this.$message.success('删除成功');
        this.loadData();
        this.loadStatistics();
        this.loadVaccineOptions();
      }).catch(() => {});
    },

    async handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要删除的记录');
        return;
      }

      const ids = this.selectedRows.map(row => row.id);
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条疫苗记录吗？删除后无法恢复！`, '批量删除', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteVaccineRecords(ids);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadData();
            this.loadStatistics();
            this.loadVaccineOptions();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('批量删除失败', error);
          this.$message.error('批量删除失败');
        }
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.admin-vaccine-record {
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

.action-left { display: flex; gap: 12px; align-items: center; }
.action-right { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }

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

.search-input { width: 220px; }
.search-input ::v-deep .el-input__inner { padding-left: 36px; border-radius: 8px; }

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

.pet-info-cell { line-height: 1.4; }
.pet-name { font-weight: 600; color: #2c3e50; margin-bottom: 4px; }
.owner-name { font-size: 12px; color: #909399; }
.vaccine-name { font-weight: 500; color: #409EFF; }
.date-text { color: #606266; }
.date-text.urgent { color: #f56c6c; font-weight: 500; }

.action-buttons { display: flex; justify-content: center; gap: 8px; }
.action-icon-btn { width: 32px; height: 32px; padding: 0; transition: all 0.2s; }
.action-icon-btn:hover { transform: scale(1.1); }

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 24px;
  border-radius: 16px;
}

.record-dialog ::v-deep .el-dialog { border-radius: 20px; overflow: hidden; }
.record-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}
.record-dialog ::v-deep .el-dialog__title { color: white; font-weight: 600; font-size: 18px; }
.record-dialog ::v-deep .el-dialog__body { padding: 24px; }

.detail-dialog ::v-deep .el-dialog { border-radius: 20px; overflow: hidden; }
.detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}
.detail-dialog ::v-deep .el-dialog__title { color: white; font-weight: 600; font-size: 18px; }

/* ✅ 疫苗选项样式 */
.vaccine-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.stock-normal {
  color: #67c23a;
}

.stock-low {
  color: #e6a23c;
}

.stock-out {
  color: #f56c6c;
}

.form-tip {
  font-size: 12px;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .admin-vaccine-record { padding: 12px; }
  .action-bar { flex-direction: column; align-items: stretch; }
  .search-input { width: 160px; }
  .pagination-wrapper { justify-content: center; }
}
</style>