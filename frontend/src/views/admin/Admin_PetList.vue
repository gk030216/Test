<template>
  <div class="pet-list-page">
    <!-- 统计概览卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-files"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">宠物总数</div>
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-sub">平台登记宠物总量</div>
        </div>
      </div>
      <div class="stat-card card-dog">
        <div class="stat-icon">
          <i class="el-icon-star-on"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">狗狗</div>
          <div class="stat-value">{{ stats.dog }}</div>
          <div class="stat-sub">犬类宠物数量</div>
        </div>
      </div>
      <div class="stat-card card-cat">
        <div class="stat-icon">
          <i class="el-icon-s-unfold"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">猫咪</div>
          <div class="stat-value">{{ stats.cat }}</div>
          <div class="stat-sub">猫类宠物数量</div>
        </div>
      </div>
      <div class="stat-card card-other">
        <div class="stat-icon">
          <i class="el-icon-more"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">其他</div>
          <div class="stat-value">{{ stats.other }}</div>
          <div class="stat-sub">其他类型宠物</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索宠物名 / 主人..."
          clearable
          prefix-icon="el-icon-search"
          @keyup.enter="handleSearch"
          class="keyword-input"
        />
        <el-select
          v-model="searchForm.type"
          placeholder="全部类型"
          clearable
          @change="handleSearch"
          class="status-select"
        >
          <el-option label="狗狗" value="dog" />
          <el-option label="猫咪" value="cat" />
          <el-option label="兔子" value="rabbit" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-button type="primary" size="medium" @click="handleSearch" class="warm-btn">搜索</el-button>
        <el-button size="medium" @click="handleReset" class="reset-btn">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button type="primary" size="medium" @click="handleAdd" class="warm-btn">
          <i class="el-icon-plus" /> 新增宠物
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

    <!-- 宠物表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="petList"
        stripe
        row-key="id"
        class="pet-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="宠物信息" min-width="210">
          <template slot-scope="scope">
            <div class="pet-cell">
              <div class="avatar-wrap">
                <el-avatar :size="40" :src="scope.row.avatar">
                  {{ !scope.row.avatar ? (scope.row.name || 'P').charAt(0).toUpperCase() : '' }}
                </el-avatar>
              </div>
              <div class="pet-meta">
                <span class="pet-name">{{ scope.row.name }}</span>
                <span class="pet-type">{{ getTypeName(scope.row.type) }}<span v-if="scope.row.breed"> · {{ scope.row.breed }}</span></span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="主人" min-width="160">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.userNickname || scope.row.userName || '未知' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="年龄" width="90" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ getAge(scope.row.birthday) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="性别" width="72" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.gender === 1 ? '公' : scope.row.gender === 2 ? '母' : '未知' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="体重" width="90" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.weight ? scope.row.weight + ' kg' : '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="color" label="毛色" width="90" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.color || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="简介" min-width="180">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.description" placement="top" :disabled="!scope.row.description || scope.row.description.length <= 30">
              <div class="desc-content">{{ scope.row.description || '--' }}</div>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-text cell-time">{{ formatDate(scope.row.createTime) }}</span>
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
      :visible.sync="showDialog"
      width="700px"
      :close-on-click-modal="false"
      class="pet-dialog"
      center
    >
      <div class="dialog-body">
        <div class="avatar-section">
          <div class="avatar-upload" @click="triggerUpload">
            <el-avatar :size="88" :src="avatarPreview" class="upload-avatar">
              {{ avatarPreview ? '' : (petForm.name || 'P').charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="upload-hint">
              <i class="el-icon-camera" />
              <span>更换头像</span>
            </div>
          </div>
          <input type="file" ref="avatarInput" accept="image/jpeg,image/png,image/jpg" style="display: none" @change="handleAvatarUpload">
          <p class="avatar-tip">支持 JPG、PNG 格式，不超过 2MB</p>
        </div>

        <el-form :model="petForm" :rules="petRules" ref="petForm" label-width="85px" class="pet-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="宠物名称" prop="name">
                <el-input v-model="petForm.name" placeholder="请输入宠物名称" size="medium" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="宠物类型" prop="type">
                <el-select v-model="petForm.type" placeholder="请选择" size="medium" style="width: 100%">
                  <el-option label="狗狗" value="dog" />
                  <el-option label="猫咪" value="cat" />
                  <el-option label="兔子" value="rabbit" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="品种">
                <el-input v-model="petForm.breed" placeholder="如：金毛" size="medium" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="petForm.gender" placeholder="请选择" size="medium" style="width: 100%">
                  <el-option label="公" :value="1" />
                  <el-option label="母" :value="2" />
                  <el-option label="未知" :value="0" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="生日">
                <el-date-picker
                  v-model="petForm.birthday"
                  type="date"
                  placeholder="选择日期"
                  value-format="yyyy-MM-dd"
                  size="medium"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="体重(kg)">
                <el-input-number v-model="petForm.weight" :min="0" :max="100" :precision="1" controls-position="right" size="medium" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="毛色">
            <el-input v-model="petForm.color" placeholder="如：金色" size="medium" />
          </el-form-item>

          <el-form-item label="简介">
            <el-input v-model="petForm.description" type="textarea" :rows="3" placeholder="宠物简介" maxlength="200" show-word-limit size="medium" />
          </el-form-item>

          <el-form-item label="所属用户" prop="userId">
            <el-select
              v-model="petForm.userId"
              placeholder="请输入用户名搜索用户"
              filterable
              remote
              reserve-keyword
              :remote-method="searchUsers"
              :loading="userLoading"
              :disabled="isEdit"
              size="medium"
              style="width: 100%"
            >
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="`${user.nickname || user.username} (${user.username})`"
                :value="user.id"
              >
                <div class="user-option">
                  <span class="nickname">{{ user.nickname || user.username }}</span>
                  <span class="username" v-if="user.nickname && user.nickname !== user.username">({{ user.username }})</span>
                  <span class="user-id">ID: {{ user.id }}</span>
                </div>
              </el-option>
            </el-select>
            <div class="form-tip" v-if="!isEdit">支持通过用户名搜索，至少输入1个字符</div>
            <div class="form-tip" v-else>编辑时不可修改所属用户</div>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showDialog = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium">确定</el-button>
      </span>
    </el-dialog>

    <!-- 宠物详情对话框 -->
    <el-dialog
      :title="currentPet ? currentPet.name : '宠物详情'"
      :visible.sync="showDetailDialog"
      width="700px"
      class="detail-dialog"
      center
    >
      <div class="pet-detail" v-if="currentPet">
        <div class="detail-header">
          <div class="detail-avatar-wrapper">
            <el-avatar :size="80" :src="currentPet.avatar" class="detail-avatar">
              {{ !currentPet.avatar ? (currentPet.name || 'P').charAt(0).toUpperCase() : '' }}
            </el-avatar>
          </div>
          <div class="detail-info">
            <h3>{{ currentPet.name }}</h3>
            <div class="detail-tags">
              <span class="tag">{{ getTypeName(currentPet.type) }}</span>
              <span class="tag" v-if="currentPet.breed">{{ currentPet.breed }}</span>
              <span class="tag" v-if="currentPet.gender === 1">公</span>
              <span class="tag" v-else-if="currentPet.gender === 2">母</span>
            </div>
          </div>
        </div>
        <div class="detail-body detail-grid">
          <div class="info-item"><span class="info-label">主人</span><span class="info-value">{{ currentPet.userNickname || currentPet.userName || '未知' }} (ID: {{ currentPet.userId }})</span></div>
          <div class="info-item"><span class="info-label">生日</span><span class="info-value">{{ formatDate(currentPet.birthday) || '未知' }}</span></div>
          <div class="info-item"><span class="info-label">年龄</span><span class="info-value">{{ getAge(currentPet.birthday) }}</span></div>
          <div class="info-item"><span class="info-label">体重</span><span class="info-value">{{ currentPet.weight ? currentPet.weight + ' kg' : '--' }}</span></div>
          <div class="info-item"><span class="info-label">毛色</span><span class="info-value">{{ currentPet.color || '--' }}</span></div>
          <div class="info-item"><span class="info-label">简介</span><span class="info-value">{{ currentPet.description || '暂无' }}</span></div>
          <div class="info-item"><span class="info-label">创建时间</span><span class="info-value">{{ formatDate(currentPet.createTime) }}</span></div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
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
  name: 'Admin_PetList',
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
      stats: {
        total: 0,
        dog: 0,
        cat: 0,
        other: 0
      },
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
    this.loadStats();
  },
  methods: {
    async loadStats() {
      try {
        const res = await getAdminPetList({ page: 1, pageSize: 9999 });
        if (res.code === 200) {
          const list = res.data.list || [];
          this.stats.total = res.data.total || 0;
          this.stats.dog = list.filter(p => p.type === 'dog').length;
          this.stats.cat = list.filter(p => p.type === 'cat').length;
          this.stats.other = list.filter(p => p.type !== 'dog' && p.type !== 'cat').length;
        }
      } catch (error) {
        // stats load failure is non-critical
      }
    },

    async loadList() {
      this.loading = true;
      try {
        const res = await getAdminPetList({ page: this.page, pageSize: this.pageSize, ...this.searchForm });
        if (res.code === 200) {
          this.petList = (res.data.list || []).map(pet => ({
            ...pet,
            userNickname: pet.userNickname || pet.nickname || null
          }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载失败');
      } finally {
        this.loading = false;
      }
    },

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
        this.$message.error(error?.message || '搜索用户失败');
      } finally {
        this.userLoading = false;
      }
    },

    getTypeName(type) {
      const map = { dog: '狗狗', cat: '猫咪', rabbit: '兔子', other: '其他' };
      return map[type] || '宠物';
    },

    getAge(birthday) {
      if (!birthday) return '--';
      const birth = new Date(birthday);
      const now = new Date();
      let age = now.getFullYear() - birth.getFullYear();
      if (now.getMonth() < birth.getMonth() || (now.getMonth() === birth.getMonth() && now.getDate() < birth.getDate())) age--;
      return age <= 0 ? '不到1岁' : age + '岁';
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      if (isNaN(d.getTime())) return '';
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    formatBirthday(date) {
      if (!date) return '';
      const d = new Date(date);
      if (isNaN(d.getTime())) return '';
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
          this.$message.error(res.message || '上传失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '上传失败');
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
        gender: row.gender != null ? row.gender : 0,
        birthday: this.formatBirthday(row.birthday),
        weight: row.weight,
        color: row.color || '',
        avatar: row.avatar || '',
        description: row.description || '',
        userId: row.userId
      };
      this.avatarPreview = row.avatar || '';
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
      try {
        await this.$confirm(`确定删除宠物「${row.name}」吗？`, '提示', { type: 'warning' });
        const res = await deletePet(row.id);
        if (res.code === 200) {
          this.$message.success(res.message || '删除成功');
          this.loadList();
          this.loadStats();
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
      const ids = this.selectedRows.map(r => r.id).join(',');
      try {
        await this.$confirm(`确定删除选中的 ${this.selectedRows.length} 个宠物吗？`, '提示', { type: 'warning' });
        const res = await batchDeletePets(ids);
        if (res.code === 200) {
          this.$message.success(res.message || '删除成功');
          this.selectedRows = [];
          this.loadList();
          this.loadStats();
        } else {
          this.$message.error(res.message || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error?.message || '删除失败');
        }
      }
    },

    async submitForm() {
      this.$refs.petForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            res = await updatePet({
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
              userId: this.petForm.userId
            });
          } else {
            res = await addPet(this.petForm);
          }
          if (res.code === 200) {
            this.$message.success(res.message || '操作成功');
            this.showDialog = false;
            this.loadList();
            this.loadStats();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '操作失败');
        } finally {
          this.submitLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.pet-list-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 统计卡片 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
.card-total .stat-icon { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-dog .stat-icon   { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-cat .stat-icon   { background: linear-gradient(135deg, #409eff, #66b1ff); }
.card-other .stat-icon { background: linear-gradient(135deg, #c0c4cc, #909399); }

.stat-info { flex: 1; }
.stat-label { font-size: 13px; color: #a08c84; margin-bottom: 5px; }
.stat-value { font-size: 28px; font-weight: 700; color: #3d2e2a; letter-spacing: -0.5px; }
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
.keyword-input { width: 240px; }
.status-select { width: 120px; }

.keyword-input ::v-deep .el-input__inner,
.status-select ::v-deep .el-input__inner {
  border-radius: 8px;
  border: 1px solid #f5ece6;
}
.keyword-input ::v-deep .el-input__inner:focus,
.status-select ::v-deep .el-input__inner:focus {
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
.pet-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #3d2e2a;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
  border-bottom: 1px solid #f5ece6;
}
.pet-table ::v-deep .el-table__body td {
  padding: 12px 0;
  font-size: 13px;
}
.pet-table ::v-deep .el-table__body tr:hover > td {
  background: #fef9f5 !important;
}
.pet-table ::v-deep .el-table__body tr.el-table__row--striped > td {
  background: #fefcfa;
}
.pet-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td {
  background: #fef9f5 !important;
}

.pet-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.avatar-wrap { position: relative; flex-shrink: 0; }
.pet-meta { display: flex; flex-direction: column; gap: 3px; }
.pet-name { font-weight: 600; color: #3d2e2a; font-size: 14px; }
.pet-type { font-size: 12px; color: #a08c84; }

.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { font-size: 12px; color: #a08c84; }
.desc-content {
  color: #5c4a42;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
  font-size: 13px;
}

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
.pet-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}
.pet-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 16px 24px;
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
  padding: 20px 24px;
}

.dialog-body {
  padding: 0;
}

/* 头像上传区域 */
.avatar-section {
  text-align: center;
  margin-bottom: 18px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5ece6;
}
.avatar-upload {
  display: inline-block;
  cursor: pointer;
  position: relative;
}
.upload-avatar {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  color: white;
  font-weight: 600;
  font-size: 34px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}
.upload-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 28px rgba(245, 158, 75, 0.3);
}
.upload-hint {
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.55);
  color: white;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 10px;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.25s;
}
.avatar-upload:hover .upload-hint { opacity: 1; }
.avatar-tip {
  font-size: 12px;
  color: #a08c84;
  margin-top: 8px;
  margin-bottom: 0;
}

/* 表单 */
.pet-form ::v-deep .el-form-item {
  margin-bottom: 14px;
}
.pet-form ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #3d2e2a;
  font-size: 13px;
}
.pet-form ::v-deep .el-input__inner {
  border-radius: 8px;
  border: 1px solid #f5ece6;
  transition: all 0.3s;
}
.pet-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 2px rgba(245, 158, 75, 0.15);
}
.pet-form ::v-deep .el-textarea__inner {
  border-radius: 8px;
  border: 1px solid #f5ece6;
}
.pet-form ::v-deep .el-textarea__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 2px rgba(245, 158, 75, 0.15);
}
.pet-form ::v-deep .el-input-number .el-input__inner {
  border-radius: 8px;
  border: 1px solid #f5ece6;
}
.pet-form ::v-deep .el-input-number__decrease,
.pet-form ::v-deep .el-input-number__increase {
  border-color: #f5ece6;
  color: #a08c84;
}

/* 用户选择器选项 */
.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-option .nickname {
  font-weight: 500;
  color: #3d2e2a;
}
.user-option .username {
  color: #a08c84;
  font-size: 12px;
}
.user-option .user-id {
  color: #c0c4cc;
  font-size: 11px;
  margin-left: auto;
}

.form-tip {
  font-size: 12px;
  color: #a08c84;
  margin-top: 4px;
}

/* 对话框底部 */
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
  padding: 20px 24px;
}
.detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}
.detail-dialog ::v-deep .el-dialog__close {
  color: white;
}

.pet-detail {
  padding: 10px;
}
.detail-header {
  display: flex;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f5ece6;
  margin-bottom: 20px;
}
.detail-avatar-wrapper {
  flex-shrink: 0;
}
.detail-avatar {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  color: white;
  font-size: 32px;
}
.detail-info h3 {
  font-size: 20px;
  margin: 0 0 10px 0;
  color: #3d2e2a;
}
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag {
  padding: 4px 12px;
  background: #fdf6f0;
  border-radius: 20px;
  font-size: 12px;
  color: #a08c84;
}
.detail-body {
  padding: 0 10px;
}
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
  width: 70px;
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
  .pet-list-page { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .filter-card { flex-direction: column; align-items: stretch; }
  .filter-left, .filter-right { justify-content: center; }
  .keyword-input { width: 180px; }
  .pagination-card { justify-content: center; }
  .detail-header { flex-direction: column; text-align: center; }
  .detail-tags { justify-content: center; }
  .detail-grid { grid-template-columns: 1fr; }
}
</style>
