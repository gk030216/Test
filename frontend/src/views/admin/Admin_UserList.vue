<template>
  <div class="user-list-page">
    <!-- 统计概览卡片 -->
    <div class="stats-row">
      <div class="stat-card card-total">
        <div class="stat-icon">
          <i class="el-icon-user"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">用户总数</div>
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-sub">平台注册用户总量</div>
        </div>
      </div>
      <div class="stat-card card-active">
        <div class="stat-icon">
          <i class="el-icon-user-solid"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">活跃用户</div>
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-sub">状态正常</div>
        </div>
      </div>
      <div class="stat-card card-today">
        <div class="stat-icon">
          <i class="el-icon-circle-plus"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日新增</div>
          <div class="stat-value">{{ stats.todayNew }}</div>
          <div class="stat-sub">{{ todayDate }} 注册</div>
        </div>
      </div>
      <div class="stat-card card-disabled">
        <div class="stat-icon">
          <i class="el-icon-remove"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">已禁用</div>
          <div class="stat-value">{{ stats.disabled }}</div>
          <div class="stat-sub">无法登录使用</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-card">
      <div class="filter-left">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索用户名 / 昵称 / 邮箱..."
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
        <el-button size="medium" @click="handleExport" :loading="exportLoading" class="export-btn">
          <i class="el-icon-download" /> 导出
        </el-button>
        <el-button type="primary" size="medium" @click="handleAdd" class="warm-btn">
          <i class="el-icon-plus" /> 新增用户
        </el-button>
      </div>
    </div>

    <!-- 批量操作浮条 -->
    <transition name="batch-bar">
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button size="small" class="batch-btn-enable" @click="handleBatchEnable">
          <i class="el-icon-check" /> 批量启用
        </el-button>
        <el-button size="small" class="batch-btn-disable" @click="handleBatchDisable">
          <i class="el-icon-close" /> 批量禁用
        </el-button>
        <el-button size="small" class="batch-btn-delete" @click="handleBatchDelete">
          <i class="el-icon-delete" /> 批量删除
        </el-button>
      </div>
    </transition>

    <!-- 用户表格 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        row-key="id"
        class="user-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="72" align="center" />

        <el-table-column label="用户信息" min-width="210">
          <template slot-scope="scope">
            <div class="user-cell">
              <div class="avatar-wrap">
                <el-avatar :size="40" :src="scope.row.avatar">
                  {{ scope.row.avatar ? '' : (scope.row.nickname || scope.row.username).charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="online-dot" v-if="scope.row.status === 1"></span>
              </div>
              <div class="user-meta">
                <span class="user-name">{{ scope.row.username }}</span>
                <span class="user-nick">{{ scope.row.nickname || '--' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="email" label="邮箱" min-width="190">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.email }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" width="130" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.phone || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="gender" label="性别" width="72" align="center">
          <template slot-scope="scope">
            <span class="cell-text">{{ scope.row.gender || '保密' }}</span>
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

        <el-table-column label="注册时间" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-text cell-time">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="最后登录" width="165" align="center">
          <template slot-scope="scope">
            <span class="cell-text cell-time">{{ formatDate(scope.row.lastLoginTime) }}</span>
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
        :page-sizes="[10, 20, 50, 100]"
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
      width="580px"
      :close-on-click-modal="false"
      class="user-dialog"
      center
    >
      <div class="dialog-body">
        <div class="avatar-section">
          <div class="avatar-upload" @click="triggerUpload">
            <el-avatar :size="88" :src="avatarPreview" class="upload-avatar">
              {{ avatarPreview ? '' : (currentUser.nickname || currentUser.username || 'U').charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="upload-hint">
              <i class="el-icon-camera" />
              <span>更换头像</span>
            </div>
            <input
              type="file"
              ref="avatarInput"
              accept="image/jpeg,image/png,image/jpg"
              style="display:none"
              @change="handleAvatarUpload"
            />
          </div>
          <p class="avatar-tip">支持 JPG、PNG，≤ 2MB</p>
        </div>

        <el-form :model="currentUser" :rules="formRules" ref="userForm" label-width="80px" class="user-form">
          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="currentUser.username" placeholder="请输入用户名" :disabled="isEdit" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="昵称">
                <el-input v-model="currentUser.nickname" placeholder="请输入昵称" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item :label="isEdit ? '新密码' : '密码'" prop="password">
                <el-input v-model="currentUser.password" type="password" :placeholder="isEdit ? '不修改请留空' : '请输入密码'" />
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="!isEdit">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="currentUser.confirmPassword" type="password" placeholder="请再次输入密码" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="currentUser.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input v-model="currentUser.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="currentUser.gender" placeholder="请选择性别">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                  <el-option label="保密" value="保密" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="warm-btn">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog
      title="用户详情"
      :visible.sync="detailVisible"
      width="520px"
      center
      class="detail-dialog"
    >
      <div class="detail-body" v-if="currentDetailUser">
        <div class="detail-avatar-area">
          <el-avatar :size="88" :src="currentDetailUser.avatar">
            {{ currentDetailUser.avatar ? '' : (currentDetailUser.nickname || currentDetailUser.username).charAt(0).toUpperCase() }}
          </el-avatar>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="d-label">用户名</span>
            <span class="d-value">{{ currentDetailUser.username }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">昵称</span>
            <span class="d-value">{{ currentDetailUser.nickname || '--' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">邮箱</span>
            <span class="d-value">{{ currentDetailUser.email }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">手机号</span>
            <span class="d-value">{{ currentDetailUser.phone || '--' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">性别</span>
            <span class="d-value">{{ currentDetailUser.gender || '保密' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">状态</span>
            <span class="d-value">
              <span class="status-tag" :class="currentDetailUser.status === 1 ? 'active' : 'inactive'">
                {{ currentDetailUser.status === 1 ? '正常' : '禁用' }}
              </span>
            </span>
          </div>
          <div class="detail-item">
            <span class="d-label">注册时间</span>
            <span class="d-value">{{ formatDate(currentDetailUser.createTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">最后登录</span>
            <span class="d-value">{{ formatDate(currentDetailUser.lastLoginTime) || '--' }}</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getUserList,
  updateUserStatus,
  deleteUser,
  addUser,
  updateUser,
  batchUpdateUserStatus,
  batchDeleteUsers
} from '@/api/user';
import { uploadAvatar } from '@/api/upload';

export default {
  name: 'Admin_UserList',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.currentUser.password) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

    return {
      loading: false,
      exportLoading: false,
      submitLoading: false,
      detailVisible: false,
      currentDetailUser: null,
      userList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      selectedRows: [],
      searchForm: { keyword: '', status: '' },
      dialogVisible: false,
      isEdit: false,
      avatarPreview: '',
      currentUser: {
        id: null, username: '', password: '', confirmPassword: '',
        email: '', phone: '', nickname: '', avatar: '',
        gender: '保密', role: 1, status: 1
      },
      stats: { total: 0, active: 0, todayNew: 0, disabled: 0 },
      formRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在3-20个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑用户' : '新增用户';
    },
    todayDate() {
      const d = new Date();
      return `${d.getMonth() + 1}/${d.getDate()}`;
    }
  },
  created() {
    this.loadUserList();
    this.loadStats();
  },
  methods: {
    async loadStats() {
      try {
        const res = await getUserList({ page: 1, pageSize: 9999, role: 1 });
        if (res.code === 200) {
          const list = res.data.list || [];
          const today = new Date().toDateString();
          this.stats = {
            total: res.data.total || 0,
            active: list.filter(i => i.status === 1).length,
            todayNew: list.filter(i => new Date(i.createTime).toDateString() === today).length,
            disabled: list.filter(i => i.status === 0).length
          };
        }
      } catch { /* stats fetch silently fails */ }
    },
    async loadUserList() {
      this.loading = true;
      try {
        let statusParam = undefined;
        if (this.searchForm.status !== '' && this.searchForm.status !== null && this.searchForm.status !== undefined) {
          statusParam = this.searchForm.status;
        }
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          status: statusParam,
          role: 1
        };
        const res = await getUserList(params);
        if (res.code === 200) {
          this.userList = (res.data.list || []).map(item => ({ ...item, statusLoading: false }));
          this.total = res.data.total || 0;
        }
      } catch (error) {
        this.$message.error(error?.message || '加载用户列表失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() { this.page = 1; this.loadUserList(); },
    handleReset() {
      this.searchForm = { keyword: '', status: '' };
      this.page = 1;
      this.loadUserList();
    },
    handlePageChange(page) { this.page = page; this.loadUserList(); },
    handleSizeChange(size) { this.pageSize = size; this.page = 1; this.loadUserList(); },
    handleSelectionChange(rows) { this.selectedRows = rows; },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    triggerUpload() { this.$refs.avatarInput.click(); },
    handleView(row) {
      this.currentDetailUser = row;
      this.detailVisible = true;
    },
    async handleAvatarUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)) {
        this.$message.error('只支持 JPG、PNG 格式');
        return;
      }
      if (file.size > 2 * 1024 * 1024) {
        this.$message.error('图片大小不能超过 2MB');
        return;
      }
      const formData = new FormData();
      formData.append('file', file);
      try {
        const res = await uploadAvatar(formData);
        if (res.code === 200) {
          this.avatarPreview = res.data.url;
          this.currentUser.avatar = res.data.url;
          if (this.isEdit && this.currentUser.id) {
            const idx = this.userList.findIndex(u => u.id === this.currentUser.id);
            if (idx !== -1) this.$set(this.userList[idx], 'avatar', res.data.url);
          }
          this.$message.success('上传成功');
        } else {
          this.$message.error(res.message || '上传失败');
        }
      } catch (error) {
        this.$message.error(error?.message || '上传失败');
      }
      event.target.value = '';
    },
    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '启用' : '禁用';
      this.$confirm(`确定要${action}用户 ${row.username} 吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消',
        type: newStatus === 1 ? 'info' : 'warning'
      }).then(async () => {
        row.statusLoading = true;
        try {
          const res = await updateUserStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(res.message || '操作成功');
            this.loadStats();
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
      this.$confirm(`确定要删除用户 ${row.username} 吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteUser(row.id);
          if (res.code === 200) {
            this.$message.success(res.message || '删除成功');
            this.loadUserList();
            this.loadStats();
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          this.$message.error(error?.message || '删除失败');
        }
      }).catch(() => {});
    },
    handleAdd() {
      this.isEdit = false;
      this.currentUser = {
        id: null, username: '', password: '', confirmPassword: '',
        email: '', phone: '', nickname: '', avatar: '', gender: '保密', role: 1, status: 1
      };
      this.avatarPreview = '';
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.userForm && this.$refs.userForm.clearValidate(); });
    },
    handleEdit(row) {
      this.isEdit = true;
      this.currentUser = {
        id: row.id, username: row.username, password: '',
        confirmPassword: '', email: row.email, phone: row.phone || '',
        nickname: row.nickname || '', avatar: row.avatar || '',
        gender: row.gender || '保密', role: 1, status: row.status
      };
      this.avatarPreview = row.avatar || '';
      this.dialogVisible = true;
      this.$nextTick(() => { this.$refs.userForm && this.$refs.userForm.clearValidate(); });
    },
    submitForm() {
      this.$refs.userForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            const data = { ...this.currentUser };
            if (!data.password) delete data.password;
            delete data.confirmPassword;
            res = await updateUser(data);
          } else {
            const data = { ...this.currentUser };
            delete data.confirmPassword;
            res = await addUser(data);
          }
          if (res.code === 200) {
            this.$message.success(res.message || (this.isEdit ? '更新成功' : '添加成功'));
            this.dialogVisible = false;
            this.loadUserList();
            this.loadStats();
          } else {
            this.$message.error(res.message || '操作失败');
          }
        } catch (error) {
          this.$message.error(error?.message || (this.isEdit ? '更新失败' : '添加失败'));
        } finally {
          this.submitLoading = false;
        }
      });
    },
    async batchAction(action, status = undefined) {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(r => r.id).join(',');
      const labels = { enable: '启用', disable: '禁用', delete: '删除' };
      const label = labels[action];
      const suffix = action === 'delete' ? '？删除后无法恢复！' : '吗？';
      try {
        await this.$confirm(`确定要${label}选中的 ${this.selectedRows.length} 个用户${suffix}`, '提示', {
          confirmButtonText: '确定', cancelButtonText: '取消',
          type: action === 'enable' ? 'info' : 'warning'
        });
        let res;
        if (action === 'delete') {
          res = await batchDeleteUsers(ids);
        } else {
          res = await batchUpdateUserStatus(ids, status);
        }
        if (res.code === 200) {
          this.$message.success(res.message || `批量${label}成功`);
          this.selectedRows = [];
          this.loadUserList();
          this.loadStats();
        } else {
          this.$message.error(res.message || `批量${label}失败`);
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error?.message || `批量${label}失败`);
        }
      }
    },
    handleBatchEnable() { this.batchAction('enable', 1); },
    handleBatchDisable() { this.batchAction('disable', 0); },
    handleBatchDelete() { this.batchAction('delete'); },
    async handleExport() {
      this.exportLoading = true;
      try {
        const params = new URLSearchParams();
        if (this.searchForm.keyword) params.append('keyword', this.searchForm.keyword);
        if (this.searchForm.status) params.append('status', this.searchForm.status);
        params.append('role', '1');
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/admin/user/export?${params.toString()}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('导出失败');
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `用户数据_${Date.now()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.$message.success('导出成功');
      } catch (error) {
        this.$message.error(error?.message || '导出失败');
      } finally {
        this.exportLoading = false;
      }
    }
  }
};
</script>

<style scoped>
/* ===== 页面基底 ===== */
.user-list-page {
  padding: 24px;
  background: linear-gradient(180deg, #fefbf9 0%, #fdf6f0 100%);
  min-height: 100%;
}

/* ===== 统计卡片行 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
.card-total .stat-icon  { background: linear-gradient(135deg, #f59e4b, #f0826a); }
.card-active .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.card-today .stat-icon  { background: linear-gradient(135deg, #409eff, #66b1ff); }
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
.keyword-input { width: 260px; }
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

.export-btn {
  background: linear-gradient(135deg, #67c23a, #85ce61) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  padding: 9px 20px !important;
  font-weight: 500;
}
.export-btn:hover { opacity: 0.88; transform: translateY(-1px); }

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
.batch-btn-enable:hover {
  background: rgba(103, 194, 58, 0.25) !important;
  border-color: #67c23a !important;
}
.batch-btn-disable {
  background: rgba(245, 158, 75, 0.15) !important;
  border: 1px solid rgba(245, 158, 75, 0.35) !important;
  color: #f59e4b !important;
}
.batch-btn-disable:hover {
  background: rgba(245, 158, 75, 0.25) !important;
  border-color: #f59e4b !important;
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

.user-table ::v-deep .el-table__header th {
  background: #fdf8f4;
  color: #5c4a42;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 15px 0;
  border-bottom: 2px solid #f5ece6;
}
.user-table ::v-deep .el-table__body td {
  padding: 15px 0;
  font-size: 13px;
  color: #3d2e2a;
}
.user-table ::v-deep .el-table__body tr:hover > td {
  background: #fefbf8;
}
.user-table ::v-deep .el-table__body tr.el-table__row--striped > td {
  background: #fefcf9;
}
.user-table ::v-deep .el-table__body tr.el-table__row--striped:hover > td {
  background: #fefbf8;
}
.user-table ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background: #f59e4b;
  border-color: #f59e4b;
}

/* 用户信息单元格 */
.user-cell { display: flex; align-items: center; gap: 12px; }
.avatar-wrap { position: relative; flex-shrink: 0; }
.avatar-wrap .el-avatar {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  color: #fff; font-weight: 600;
}
.online-dot {
  position: absolute; right: 0; bottom: 0;
  width: 10px; height: 10px;
  background: #67c23a;
  border: 2px solid #fff;
  border-radius: 50%;
}
.user-meta { display: flex; flex-direction: column; gap: 2px; }
.user-name { font-weight: 600; color: #3d2e2a; font-size: 13px; }
.user-nick { font-size: 12px; color: #a08c84; }
.cell-text { color: #5c4a42; font-size: 13px; }
.cell-time { color: #a08c84; font-size: 12px; }

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
.user-dialog ::v-deep .el-dialog {
  border-radius: 20px; overflow: hidden;
}
.user-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f59e4b, #f0826a);
  padding: 22px 28px;
  margin: 0;
}
.user-dialog ::v-deep .el-dialog__title {
  color: #fff; font-weight: 700; font-size: 17px; letter-spacing: 0.5px;
}
.user-dialog ::v-deep .el-dialog__close { color: #fff; font-size: 22px; }
.user-dialog ::v-deep .el-dialog__close:hover { color: rgba(255,255,255,0.7); }
.user-dialog ::v-deep .el-dialog__body { padding: 28px; }

.dialog-body { max-height: 55vh; overflow-y: auto; padding-right: 4px; }

/* 头像区域 */
.avatar-section {
  text-align: center; margin-bottom: 24px;
  padding-bottom: 22px; border-bottom: 1px solid #f5ece6;
}
.avatar-upload { display: inline-block; position: relative; cursor: pointer; }
.upload-avatar {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  color: #fff; font-weight: 600; font-size: 32px;
  transition: transform 0.25s;
}
.avatar-upload:hover .upload-avatar { transform: scale(1.06); }
.upload-hint {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,0.55);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: #fff; font-size: 12px;
  opacity: 0; transition: opacity 0.25s;
}
.avatar-upload:hover .upload-hint { opacity: 1; }
.upload-hint i { font-size: 22px; margin-bottom: 4px; }
.avatar-tip { font-size: 12px; color: #b8a8a0; margin: 10px 0 0; }

/* 表单 */
.user-form ::v-deep .el-form-item { margin-bottom: 18px; }
.user-form ::v-deep .el-form-item__label { font-weight: 500; color: #5c4a42; font-size: 13px; }
.user-form ::v-deep .el-input__inner {
  border-radius: 10px; border: 1px solid #e8ddd4;
  transition: all 0.25s; font-size: 13px;
}
.user-form ::v-deep .el-input__inner:focus {
  border-color: #f59e4b;
  box-shadow: 0 0 0 3px rgba(245, 158, 75, 0.08);
}
.user-form ::v-deep .el-radio__input.is-checked .el-radio__inner {
  background: #f59e4b; border-color: #f59e4b;
}

/* 对话框底部 */
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

.detail-avatar-area {
  text-align: center; margin-bottom: 24px;
  padding-bottom: 20px; border-bottom: 1px solid #f5ece6;
}
.detail-avatar-area .el-avatar {
  background: linear-gradient(135deg, #f59e4b, #f0826a) !important;
  color: #fff; font-weight: 600; font-size: 32px;
}

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
  .user-list-page { padding: 12px; }
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
