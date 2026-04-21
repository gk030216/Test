<template>
  <div class="admin-pet-list">
    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增宠物
        </el-button>
        <el-button v-if="selectedRows.length > 0" type="danger" plain @click="handleBatchDelete" class="batch-btn">
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input v-model="searchForm.keyword" placeholder="搜索宠物名/主人" clearable size="medium" @keyup.enter="handleSearch" class="search-input"></el-input>
        </div>
        <el-select v-model="searchForm.type" placeholder="宠物类型" clearable size="medium" class="status-select" @change="handleSearch">
          <el-option label="狗狗" value="dog"></el-option>
          <el-option label="猫咪" value="cat"></el-option>
          <el-option label="兔子" value="rabbit"></el-option>
          <el-option label="其他" value="other"></el-option>
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 宠物表格 -->
    <el-table v-loading="loading" :data="petList" stripe class="pet-table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column label="头像" width="70" align="center">
        <template slot-scope="scope">
          <el-avatar :size="42" :src="scope.row.avatar" class="pet-avatar">
            {{ !scope.row.avatar ? (scope.row.name ? scope.row.name.charAt(0).toUpperCase() : 'P') : '' }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="宠物信息" min-width="180">
        <template slot-scope="scope">
          <div class="pet-name">{{ scope.row.name }}</div>
          <div class="pet-type">{{ getTypeName(scope.row.type) }} <span v-if="scope.row.breed">· {{ scope.row.breed }}</span></div>
        </template>
      </el-table-column>
      <el-table-column label="主人" width="180">
        <template slot-scope="scope">
          <div class="owner-name">{{ scope.row.userNickname || scope.row.userName || '未知' }}</div>
          <div class="owner-id">用户ID: {{ scope.row.userId }}</div>
        </template>
      </el-table-column>
      <el-table-column label="基本信息" width="160">
        <template slot-scope="scope">
          <div class="basic-info"><i class="el-icon-cake"></i> {{ getAge(scope.row.birthday) }}</div>
          <div class="basic-info"><i class="el-icon-weight"></i> {{ scope.row.weight || '--' }} kg</div>
          <div class="basic-info"><i class="el-icon-heart"></i> {{ scope.row.gender === 1 ? '公' : scope.row.gender === 2 ? '母' : '未知' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="color" label="毛色" width="100">
        <template slot-scope="scope">
          <span class="color-text">{{ scope.row.color || '--' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">{{ formatDate(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button size="small" type="primary" plain circle class="action-icon-btn" @click="handleView(scope.row)" title="详情">
              <i class="el-icon-view"></i>
            </el-button>
            <el-button size="small" type="warning" plain circle class="action-icon-btn" @click="handleEdit(scope.row)" title="编辑">
              <i class="el-icon-edit"></i>
            </el-button>
            <el-button size="small" type="danger" plain circle class="action-icon-btn" @click="handleDelete(scope.row)" title="删除">
              <i class="el-icon-delete"></i>
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination @size-change="handleSizeChange" @current-change="handlePageChange"
                     :current-page="page" :page-sizes="[10, 20, 50]" :page-size="pageSize"
                     layout="total, sizes, prev, pager, next, jumper" :total="total" background>
      </el-pagination>
    </div>

    <!-- 新增/编辑宠物对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="showDialog" width="600px" center class="pet-dialog" :close-on-click-modal="false">
      <div class="dialog-content">
        <!-- 头像上传区域 -->
        <div class="avatar-section">
          <div class="avatar-upload-wrapper" @click="triggerUpload">
            <div class="avatar-preview">
              <el-avatar :size="100" :src="avatarPreview" class="preview-avatar">
                {{ !avatarPreview ? (petForm.name ? petForm.name.charAt(0).toUpperCase() : 'P') : '' }}
              </el-avatar>
              <div class="upload-mask">
                <i class="el-icon-camera"></i>
                <span>更换头像</span>
              </div>
            </div>
            <input type="file" ref="avatarInput" accept="image/jpeg,image/png,image/jpg" style="display: none" @change="handleAvatarUpload">
          </div>
          <p class="avatar-tip">支持 JPG、PNG 格式，≤2MB</p>
        </div>

        <el-form :model="petForm" :rules="petRules" ref="petForm" label-width="85px" class="pet-form">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="宠物名称" prop="name">
                <el-input v-model="petForm.name" placeholder="请输入宠物名称" size="medium"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="宠物类型" prop="type">
                <el-select v-model="petForm.type" placeholder="请选择" size="medium" style="width: 100%">
                  <el-option label="🐕 狗狗" value="dog"></el-option>
                  <el-option label="🐈 猫咪" value="cat"></el-option>
                  <el-option label="🐇 兔子" value="rabbit"></el-option>
                  <el-option label="🐹 其他" value="other"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="品种">
                <el-input v-model="petForm.breed" placeholder="如：金毛" size="medium"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="petForm.gender" placeholder="请选择" size="medium" style="width: 100%">
                  <el-option label="公" :value="1"></el-option>
                  <el-option label="母" :value="2"></el-option>
                  <el-option label="未知" :value="0"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="生日">
                <el-date-picker v-model="petForm.birthday" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" size="medium" style="width: 100%"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="体重(kg)">
                <el-input-number v-model="petForm.weight" :min="0" :max="100" :precision="1" controls-position="right" size="medium" style="width: 100%"></el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="毛色">
            <el-input v-model="petForm.color" placeholder="如：金色" size="medium"></el-input>
          </el-form-item>

          <el-form-item label="简介">
            <el-input v-model="petForm.description" type="textarea" :rows="3" placeholder="宠物简介" size="medium"></el-input>
          </el-form-item>

          <!-- 用户选择器 - 通过用户名搜索 -->
          <el-form-item label="所属用户" prop="userId">
            <el-select
                v-model="petForm.userId"
                placeholder="请输入用户名搜索用户"
                filterable
                remote
                reserve-keyword
                :remote-method="searchUsers"
                :loading="userLoading"
                size="medium"
                style="width: 100%">
              <el-option
                  v-for="user in userList"
                  :key="user.id"
                  :label="`${user.nickname || user.username} (${user.username})`"
                  :value="user.id">
                <div class="user-option">
                  <span class="nickname">{{ user.nickname || user.username }}</span>
                  <span class="username" v-if="user.nickname && user.nickname !== user.username">({{ user.username }})</span>
                  <span class="user-id">ID: {{ user.id }}</span>
                </div>
              </el-option>
            </el-select>
            <div class="form-tip">支持通过用户名搜索，至少输入1个字符</div>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showDialog = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium">确定</el-button>
      </span>
    </el-dialog>

    <!-- 宠物详情对话框 -->
    <el-dialog :title="currentPet && currentPet.name" :visible.sync="showDetailDialog" width="600px" center class="detail-dialog">
      <div class="pet-detail" v-if="currentPet">
        <div class="detail-header">
          <div class="detail-avatar-wrapper">
            <el-avatar :size="80" :src="currentPet.avatar" class="detail-avatar">
              {{ !currentPet.avatar ? (currentPet.name ? currentPet.name.charAt(0).toUpperCase() : 'P') : '' }}
            </el-avatar>
          </div>
          <div class="detail-info">
            <h3>{{ currentPet.name }}</h3>
            <div class="detail-tags">
              <span class="tag">{{ getTypeName(currentPet.type) }}</span>
              <span class="tag" v-if="currentPet.breed">{{ currentPet.breed }}</span>
              <span class="tag" v-if="currentPet.gender === 1">♂ 公</span>
              <span class="tag" v-else-if="currentPet.gender === 2">♀ 母</span>
            </div>
          </div>
        </div>
        <div class="detail-body">
          <div class="info-row">
            <span class="info-label">主人：</span>
            <span class="info-value">{{ currentPet.userNickname || currentPet.userName || '未知' }} (ID: {{ currentPet.userId }})</span>
          </div>
          <div class="info-row">
            <span class="info-label">生日：</span>
            <span class="info-value">{{ formatDate(currentPet.birthday) || '未知' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">年龄：</span>
            <span class="info-value">{{ getAge(currentPet.birthday) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">体重：</span>
            <span class="info-value">{{ currentPet.weight || '--' }} kg</span>
          </div>
          <div class="info-row">
            <span class="info-label">毛色：</span>
            <span class="info-value">{{ currentPet.color || '--' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">简介：</span>
            <span class="info-value">{{ currentPet.description || '暂无' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">创建时间：</span>
            <span class="info-value">{{ formatDate(currentPet.createTime) }}</span>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="showDetailDialog = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminPetList, addPet, updatePet, deletePet, batchDeletePets } from '@/api/pet';
import { getUserList } from '@/api/user';
import { uploadPetAvatar } from '@/api/upload';

export default {
  name: 'AdminPetList',
  data() {
    return {
      loading: false,
      submitLoading: false,
      petList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', type: '' },
      showDialog: false,
      showDetailDialog: false,
      isEdit: false,
      editId: null,
      avatarPreview: '',
      currentPet: null,
      userList: [],
      userLoading: false,
      petForm: {
        name: '',
        type: 'dog',
        breed: '',
        gender: 0,
        birthday: '',
        weight: null,
        color: '',
        avatar: '',
        description: '',
        userId: null
      },
      petRules: {
        name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择宠物类型', trigger: 'change' }],
        userId: [{ required: true, message: '请选择所属用户', trigger: 'change' }]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑宠物' : '新增宠物';
    }
  },
  created() {
    this.loadList();
  },
  methods: {
    async loadList() {
      this.loading = true;
      try {
        const res = await getAdminPetList({ page: this.page, pageSize: this.pageSize, ...this.searchForm });
        if (res.code === 200) {
          // ✅ 确保 userNickname 字段存在
          this.petList = (res.data.list || []).map(pet => ({
            ...pet,
            userNickname: pet.userNickname || pet.nickname || null
          }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    // 搜索用户（远程搜索）
    async searchUsers(query) {
      if (!query || query.length < 1) {
        this.userList = [];
        return;
      }
      this.userLoading = true;
      try {
        const res = await getUserList({ keyword: query, page: 1, pageSize: 10 });
        if (res.code === 200) {
          this.userList = res.data.list || [];
        }
      } catch (error) {
        console.error('搜索用户失败', error);
      } finally {
        this.userLoading = false;
      }
    },

    getTypeName(type) {
      const map = {
        dog: '🐕 狗狗',
        cat: '🐈 猫咪',
        rabbit: '🐇 兔子',
        other: '🐹 其他'
      };
      return map[type] || '🐾 宠物';
    },

    getAge(birthday) {
      if (!birthday) return '年龄未知';
      const birth = new Date(birthday);
      const now = new Date();
      let age = now.getFullYear() - birth.getFullYear();
      if (now.getMonth() < birth.getMonth() || (now.getMonth() === birth.getMonth() && now.getDate() < birth.getDate())) age--;
      return age <= 0 ? '不到1岁' : age + '岁';
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    handleSearch() { this.page = 1; this.loadList(); },
    handleReset() { this.searchForm = { keyword: '', type: '' }; this.page = 1; this.loadList(); },
    handlePageChange(page) { this.page = page; this.loadList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },

    triggerUpload() {
      this.$refs.avatarInput.click();
    },

    async handleAvatarUpload(event) {
      const file = event.target.files[0];
      if (!file) return;

      if (!file.type.startsWith('image/')) {
        this.$message.error('只支持图片格式');
        return;
      }
      if (file.size > 2 * 1024 * 1024) {
        this.$message.error('图片不能超过2MB');
        return;
      }

      const formData = new FormData();
      formData.append('file', file);

      try {
        const res = await uploadPetAvatar(formData);
        if (res.code === 200) {
          this.avatarPreview = res.data.url;
          this.petForm.avatar = res.data.url;
          this.$message.success('上传成功');
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
      event.target.value = '';
    },

    handleAdd() {
      this.isEdit = false;
      this.editId = null;
      this.petForm = {
        name: '',
        type: 'dog',
        breed: '',
        gender: 0,
        birthday: '',
        weight: null,
        color: '',
        avatar: '',
        description: '',
        userId: null
      };
      this.avatarPreview = '';
      this.userList = [];
      this.showDialog = true;
      this.$nextTick(() => {
        if (this.$refs.petForm) this.$refs.petForm.clearValidate();
      });
    },

    handleEdit(row) {
      this.isEdit = true;
      this.editId = row.id;
      this.petForm = {
        name: row.name || '',
        type: row.type || 'dog',
        breed: row.breed || '',
        gender: row.gender || 0,
        birthday: row.birthday || '',
        weight: row.weight,
        color: row.color || '',
        avatar: row.avatar || '',
        description: row.description || '',
        userId: row.userId,
        isDefault: row.isDefault || 0
      };
      this.avatarPreview = row.avatar || '';

      // 设置用户显示（用于编辑时显示已选择的用户）
      if (row.userId) {
        this.userList = [{
          id: row.userId,
          username: row.userName,
          nickname: row.userNickname
        }];
      } else {
        this.userList = [];
      }

      this.showDialog = true;
      this.$nextTick(() => {
        if (this.$refs.petForm) this.$refs.petForm.clearValidate();
      });
    },

    handleView(row) {
      this.currentPet = row;
      this.showDetailDialog = true;
    },

    async handleDelete(row) {
      this.$confirm(`确定删除宠物 "${row.name}" 吗？`, '提示', { type: 'warning' }).then(async () => {
        await deletePet(row.id);
        this.$message.success('删除成功');
        this.loadList();
      }).catch(() => {});
    },

    async handleBatchDelete() {
      const ids = this.selectedRows.map(r => r.id).join(',');
      this.$confirm(`确定删除选中的 ${this.selectedRows.length} 个宠物吗？`, '提示', { type: 'warning' }).then(async () => {
        await batchDeletePets(ids);
        this.$message.success('删除成功');
        this.selectedRows = [];
        this.loadList();
      }).catch(() => {});
    },

    async submitForm() {
      this.$refs.petForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            const updateData = {
              id: this.editId,
              name: this.petForm.name,
              type: this.petForm.type,
              breed: this.petForm.breed,
              gender: this.petForm.gender,
              birthday: this.petForm.birthday,
              weight: this.petForm.weight,
              color: this.petForm.color,
              avatar: this.petForm.avatar,
              description: this.petForm.description,
              userId: this.petForm.userId,
              isDefault: this.petForm.isDefault || 0
            };
            res = await updatePet(updateData);
          } else {
            res = await addPet(this.petForm);
          }
          if (res.code === 200) {
            this.$message.success(res.message);
            this.showDialog = false;
            this.loadList();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          console.error('操作失败:', error);
          this.$message.error(error.message || '操作失败');
        } finally {
          this.submitLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.admin-pet-list {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 操作栏 */
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
  width: 200px;
}

.search-input ::v-deep .el-input__inner {
  padding-left: 36px;
  border-radius: 8px;
}

.status-select {
  width: 110px;
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

/* 表格 */
.pet-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.pet-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f6;
}

.pet-table ::v-deep .el-table__body td {
  padding: 14px 0;
  font-size: 13px;
}

.pet-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: 500;
}

.pet-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.pet-type {
  font-size: 12px;
  color: #909399;
}

.owner-name {
  font-weight: 500;
  color: #2c3e50;
}

.owner-id {
  font-size: 11px;
  color: #c0c4cc;
}

.basic-info {
  font-size: 12px;
  color: #666;
  margin: 2px 0;
}

.basic-info i {
  margin-right: 4px;
  color: #667eea;
}

.color-text {
  color: #606266;
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
.pet-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.pet-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.pet-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.pet-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.pet-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

/* 头像上传区域 */
.avatar-section {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eef2f6;
}

.avatar-upload-wrapper {
  display: inline-block;
  cursor: pointer;
  position: relative;
}

.avatar-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.avatar-preview:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.3);
}

.preview-avatar {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f0f2f5, #e8eaef);
  color: #667eea;
  font-weight: 600;
  font-size: 36px;
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
  border-radius: 50%;
}

.avatar-preview:hover .upload-mask {
  opacity: 1;
}

.upload-mask i {
  font-size: 24px;
  margin-bottom: 6px;
}

.upload-mask span {
  font-size: 12px;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 12px;
  margin-bottom: 0;
}

/* 表单 */
.pet-form {
  margin-top: 8px;
}

.pet-form ::v-deep .el-form-item {
  margin-bottom: 18px;
}

.pet-form ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 13px;
}

.pet-form ::v-deep .el-input__inner {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.pet-form ::v-deep .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

/* 用户选择器选项样式 */
.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-option .username {
  font-weight: 500;
  color: #2c3e50;
}

.user-option .nickname {
  color: #909399;
  font-size: 12px;
}

.user-option .user-id {
  color: #c0c4cc;
  font-size: 11px;
  margin-left: auto;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 对话框底部 */
.dialog-footer {
  text-align: right;
  padding: 16px 24px 20px;
  border-top: 1px solid #eef2f6;
}

.dialog-footer .el-button {
  border-radius: 8px;
  padding: 9px 24px;
  font-weight: 500;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
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
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
}

.detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.pet-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.detail-avatar-wrapper {
  flex-shrink: 0;
}

.detail-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-size: 32px;
}

.detail-info h3 {
  font-size: 20px;
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 12px;
  background: #f0f2f5;
  border-radius: 20px;
  font-size: 12px;
  color: #606266;
}

.detail-body {
  padding: 0 10px;
}

.info-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  width: 80px;
  color: #909399;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #2c3e50;
}

/* 响应式 */
@media (max-width: 768px) {
  .admin-pet-list {
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

  .detail-header {
    flex-direction: column;
    text-align: center;
  }

  .detail-tags {
    justify-content: center;
  }
}
</style>