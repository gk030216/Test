<template>
  <div class="staff-list">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <i class="el-icon-plus"></i> 新增员工
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
      </div>
      <div class="action-right">
        <div class="search-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
              v-model="searchForm.keyword"
              placeholder="搜索员工名/昵称/邮箱"
              clearable
              size="medium"
              @keyup.enter="handleSearch"
              class="search-input"
          >
          </el-input>
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
        <el-button type="success" size="medium" @click="handleExport" :loading="exportLoading" plain class="export-btn">
          <i class="el-icon-download"></i> 导出
        </el-button>
      </div>
    </div>

    <!-- 员工表格 -->
    <el-table
        v-loading="loading"
        :data="staffList"
        stripe
        style="width: 100%"
        class="staff-table"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>

      <!-- 头像列 -->
      <el-table-column label="头像" width="70" align="center">
        <template slot-scope="scope">
          <el-avatar
              :size="42"
              :src="scope.row.avatar"
              class="staff-avatar"
          >
            {{ !scope.row.avatar ? (scope.row.nickname || scope.row.username).charAt(0).toUpperCase() : '' }}
          </el-avatar>
        </template>
      </el-table-column>

      <el-table-column prop="username" label="员工账号" min-width="120">
        <template slot-scope="scope">
          <div class="staff-name">
            <span class="name-text">{{ scope.row.username }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="nickname" label="昵称" min-width="120">
        <template slot-scope="scope">
          <span class="nickname-text">{{ scope.row.nickname || '--' }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="email" label="邮箱" min-width="200">
        <template slot-scope="scope">
          <span class="email-text">{{ scope.row.email }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="phone" label="手机号" min-width="130">
        <template slot-scope="scope">
          <span class="phone-text">{{ scope.row.phone || '--' }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="gender" label="性别" width="80" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.gender || '保密' }}</span>
        </template>
      </el-table-column>

      <!-- 状态切换按钮 -->
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-switch
              :value="scope.row.status === 1"
              active-color="#67c23a"
              inactive-color="#f56c6c"
              @change="(val) => handleStatusChange(scope.row, val)"
              :loading="scope.row.statusLoading"
          >
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="入职时间" width="160">
        <template slot-scope="scope">
          <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="lastLoginTime" label="最后登录" width="160">
        <template slot-scope="scope">
          <span class="time-text">{{ formatDate(scope.row.lastLoginTime) || '--' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right" align="center">
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
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
      >
      </el-pagination>
    </div>

    <!-- 新增/编辑员工对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="560px"
        :close-on-click-modal="false"
        class="staff-dialog"
        center
    >
      <div class="dialog-content">
        <!-- 头像上传区域 -->
        <div class="avatar-section">
          <div class="avatar-upload-wrapper" @click="triggerUpload">
            <div class="avatar-preview">
              <el-avatar
                  :size="100"
                  :src="avatarPreview"
                  class="preview-avatar"
              >
                {{ !avatarPreview ? (currentUser.nickname || currentUser.username || 'U').charAt(0).toUpperCase() : '' }}
              </el-avatar>
              <div class="upload-mask">
                <i class="el-icon-camera"></i>
                <span>更换头像</span>
              </div>
            </div>
            <input
                type="file"
                ref="avatarInput"
                accept="image/jpeg,image/png,image/jpg"
                style="display: none"
                @change="handleAvatarUpload"
            />
          </div>
          <p class="avatar-tip">支持 JPG、PNG 格式，≤2MB</p>
        </div>

        <!-- 表单区域 -->
        <el-form :model="currentUser" :rules="formRules" ref="userForm" label-width="85px" class="staff-form">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="员工账号" prop="username">
                <el-input v-model="currentUser.username" placeholder="请输入员工账号" :disabled="isEdit" size="medium"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="昵称">
                <el-input v-model="currentUser.nickname" placeholder="请输入昵称" size="medium"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="密码" prop="password" v-if="!isEdit">
                <el-input v-model="currentUser.password" type="password" placeholder="请输入密码" size="medium"></el-input>
              </el-form-item>
              <el-form-item label="新密码" v-else>
                <el-input v-model="currentUser.password" type="password" placeholder="不修改请留空" size="medium"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEdit">
                <el-input v-model="currentUser.confirmPassword" type="password" placeholder="请再次输入密码" size="medium"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="currentUser.email" placeholder="请输入邮箱" size="medium"></el-input>
          </el-form-item>

          <el-form-item label="手机号">
            <el-input v-model="currentUser.phone" placeholder="请输入手机号" size="medium"></el-input>
          </el-form-item>

          <!-- 性别选择 - 移到这里，在 el-form 内部 -->
          <el-form-item label="性别">
            <el-radio-group v-model="currentUser.gender">
              <el-radio label="男">男</el-radio>
              <el-radio label="女">女</el-radio>
              <el-radio label="保密">保密</el-radio>
            </el-radio-group>
          </el-form-item>

        </el-form>
        <!-- 性别选择 -->
        <el-form-item label="性别">
          <el-radio-group v-model="currentUser.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
            <el-radio label="保密">保密</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 员工详情对话框 -->
    <el-dialog
        title="员工详情"
        :visible.sync="detailVisible"
        width="550px"
        center
        class="staff-detail-dialog"
    >
      <div class="detail-content" v-if="currentDetailStaff">
        <!-- 头像区域 -->
        <div class="detail-avatar">
          <el-avatar
              :size="100"
              :src="currentDetailStaff.avatar"
              class="detail-avatar-img"
          >
            {{ !currentDetailStaff.avatar ? (currentDetailStaff.nickname || currentDetailStaff.username).charAt(0).toUpperCase() : '' }}
          </el-avatar>
        </div>

        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="detail-item">
            <span class="detail-label">员工账号：</span>
            <span class="detail-value">{{ currentDetailStaff.username }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">昵称：</span>
            <span class="detail-value">{{ currentDetailStaff.nickname || '--' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">邮箱：</span>
            <span class="detail-value">{{ currentDetailStaff.email }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">手机号：</span>
            <span class="detail-value">{{ currentDetailStaff.phone || '--' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">性别：</span>
            <span class="detail-value">{{ currentDetailStaff.gender || '保密' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态：</span>
            <el-tag :type="currentDetailStaff.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentDetailStaff.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">入职时间：</span>
            <span class="detail-value">{{ formatDate(currentDetailStaff.createTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">最后登录：</span>
            <span class="detail-value">{{ formatDate(currentDetailStaff.lastLoginTime) || '--' }}</span>
          </div>
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
  name: 'StaffList',
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
      gender: '保密',
      currentDetailStaff: null,
      staffList: [],
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
      avatarPreview: '',
      currentUser: {
        id: null,
        username: '',
        password: '',
        confirmPassword: '',
        email: '',
        phone: '',
        nickname: '',
        avatar: '',
        role: 2,
        status: 1
      },
      formRules: {
        username: [
          { required: true, message: '请输入员工账号', trigger: 'blur' },
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
      return this.isEdit ? '编辑员工' : '新增员工';
    }
  },
  created() {
    this.loadStaffList();
  },
  methods: {
    async loadStaffList() {
      this.loading = true;
      try {
        // 处理状态参数：确保 0 能正确传递
        let statusParam = undefined;
        if (this.searchForm.status !== '' && this.searchForm.status !== null && this.searchForm.status !== undefined) {
          statusParam = this.searchForm.status;
        }

        const params = {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword || undefined,
          status: statusParam,
          role: 2
        };
        const res = await getUserList(params);
        if (res.code === 200) {
          this.staffList = res.data.list.map(item => ({
            ...item,
            statusLoading: false
          }));
          this.total = res.data.total;
        }
      } catch (error) {
        console.error('加载员工列表失败', error);
        this.$message.error('加载员工列表失败');
      } finally {
        this.loading = false;
      }
    },

    // 查看员工详情
    handleView(row) {
      this.currentDetailStaff = row;
      this.detailVisible = true;
    },

    handleSearch() {
      this.page = 1;
      this.loadStaffList();
    },
    handleReset() {
      this.searchForm = {
        keyword: '',
        status: ''
      };
      this.page = 1;
      this.loadStaffList();
    },
    handlePageChange(page) {
      this.page = page;
      this.loadStaffList();
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.page = 1;
      this.loadStaffList();
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
      this.$refs.avatarInput.click();
    },
    async handleAvatarUpload(event) {
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
        const res = await uploadAvatar(formData);
        if (res.code === 200) {
          this.avatarPreview = res.data.url;
          this.currentUser.avatar = res.data.url;

          if (this.isEdit && this.currentUser.id) {
            const index = this.staffList.findIndex(u => u.id === this.currentUser.id);
            if (index !== -1) {
              this.$set(this.staffList[index], 'avatar', res.data.url);
            }
          }

          this.$message.success('上传成功');
        } else {
          this.$message.error(res.message);
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
      event.target.value = '';
    },
    async handleStatusChange(row, val) {
      const newStatus = val ? 1 : 0;
      const action = newStatus === 1 ? '启用' : '禁用';
      const type = newStatus === 1 ? 'info' : 'warning';

      this.$confirm(`确定要${action}员工 ${row.username} 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: type
      }).then(async () => {
        row.statusLoading = true;
        try {
          const res = await updateUserStatus(row.id, newStatus);
          if (res.code === 200) {
            row.status = newStatus;
            this.$message.success(res.message);
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('操作失败');
        } finally {
          row.statusLoading = false;
        }
      }).catch(() => {});
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除员工 ${row.username} 吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteUser(row.id);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.loadStaffList();
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
      this.currentUser = {
        id: null,
        username: '',
        password: '',
        confirmPassword: '',
        email: '',
        phone: '',
        gender: '保密',
        nickname: '',
        avatar: '',
        role: 2,
        status: 1
      };
      this.avatarPreview = '';
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.userForm) {
          this.$refs.userForm.clearValidate();
        }
      });
    },
    handleEdit(row) {
      this.isEdit = true;
      this.currentUser = {
        id: row.id,
        username: row.username,
        password: '',
        confirmPassword: '',
        email: row.email,
        phone: row.phone || '',
        gender: row.gender || '保密',
        nickname: row.nickname || '',
        avatar: row.avatar || '',
        role: 2,
        status: row.status
      };
      this.avatarPreview = row.avatar || '';
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.userForm) {
          this.$refs.userForm.clearValidate();
        }
      });
    },
    submitForm() {
      this.$refs.userForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            const updateData = { ...this.currentUser };
            if (!updateData.password) {
              delete updateData.password;
            }
            delete updateData.confirmPassword;
            res = await updateUser(updateData);
          } else {
            const addData = { ...this.currentUser };
            delete addData.confirmPassword;
            res = await addUser(addData);
          }

          if (res.code === 200) {
            this.$message.success(res.message);
            this.dialogVisible = false;
            this.loadStaffList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error(this.isEdit ? '更新失败' : '添加失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },
    async handleBatchEnable() {
      if (this.selectedRows.length === 0) return;
      const ids = this.selectedRows.map(row => row.id).join(',');
      this.$confirm(`确定要启用选中的 ${this.selectedRows.length} 个员工吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const res = await batchUpdateUserStatus(ids, 1);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadStaffList();
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
      this.$confirm(`确定要禁用选中的 ${this.selectedRows.length} 个员工吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchUpdateUserStatus(ids, 0);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadStaffList();
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
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个员工吗？删除后无法恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await batchDeleteUsers(ids);
          if (res.code === 200) {
            this.$message.success(res.message);
            this.selectedRows = [];
            this.loadStaffList();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error('批量删除失败');
        }
      }).catch(() => {});
    },
    async handleExport() {
      this.exportLoading = true;
      try {
        const params = new URLSearchParams();
        if (this.searchForm.keyword) params.append('keyword', this.searchForm.keyword);
        if (this.searchForm.status) params.append('status', this.searchForm.status);
        params.append('role', 2);

        const token = localStorage.getItem('token');
        const url = `/api/admin/user/export?${params.toString()}`;

        const response = await fetch(url, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) throw new Error('导出失败');

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = `员工数据_${new Date().getTime()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);

        this.$message.success('导出成功');
      } catch (error) {
        this.$message.error('导出失败');
      } finally {
        this.exportLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.staff-list {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 顶部操作栏 */
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

/* 新增按钮 */
.add-btn {
  background: linear-gradient(135deg, #e6a23c 0%, #f0b45c 100%);
  border: none;
  color: white;
  font-weight: 500;
  padding: 8px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.4);
}

/* 批量操作按钮 */
.batch-btn {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s;
}

.batch-btn:hover {
  transform: translateY(-1px);
}

/* 搜索框 */
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
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.search-input ::v-deep .el-input__inner:focus {
  border-color: #e6a23c;
  box-shadow: 0 0 0 2px rgba(230, 162, 60, 0.1);
}

.status-select {
  width: 100px;
}

.status-select ::v-deep .el-input__inner {
  border-radius: 8px;
}

.search-btn, .reset-btn, .export-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #e6a23c 0%, #f0b45c 100%);
  border: none;
  color: white;
}

.search-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.reset-btn {
  border-color: #dcdfe6;
  color: #606266;
}

.reset-btn:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
}

.export-btn {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  color: white;
  font-weight: 500;
  padding: 8px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.export-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
  color: white;
}

/* 表格样式 */
.staff-table {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.staff-table ::v-deep .el-table__header th {
  background: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f6;
}

.staff-table ::v-deep .el-table__body tr {
  transition: background 0.2s;
}

.staff-table ::v-deep .el-table__body tr:hover > td {
  background: #fafbfe;
}

.staff-table ::v-deep .el-table__body td {
  padding: 14px 0;
  font-size: 13px;
}

/* 员工头像 */
.staff-avatar {
  background: linear-gradient(135deg, #e6a23c 0%, #f0b45c 100%);
  color: white;
  font-weight: 500;
  font-size: 16px;
}

/* 员工名 */
.staff-name .name-text {
  font-weight: 600;
  color: #e6a23c;
}

.nickname-text, .email-text, .phone-text, .time-text {
  color: #5a6874;
}

/* 操作按钮 */
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
.staff-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.staff-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #e6a23c 0%, #f0b45c 100%);
  padding: 20px 24px;
  margin: 0;
}

.staff-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.staff-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.staff-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
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
  box-shadow: 0 12px 28px rgba(230, 162, 60, 0.3);
}

.preview-avatar {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #fff5e6 0%, #ffefd9 100%);
  color: #e6a23c;
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

/* 表单样式 */
.staff-form {
  margin-top: 8px;
}

.staff-form ::v-deep .el-form-item {
  margin-bottom: 18px;
}

.staff-form ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #2c3e50;
  font-size: 13px;
}

.staff-form ::v-deep .el-input__inner {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.staff-form ::v-deep .el-input__inner:focus {
  border-color: #e6a23c;
  box-shadow: 0 0 0 2px rgba(230, 162, 60, 0.1);
}

/* 对话框底部 */
.dialog-footer {
  text-align: right;
  padding: 16px 24px 20px;
  border-top: 1px solid #eef2f6;
  background: #fff;
}

.dialog-footer .el-button {
  border-radius: 8px;
  padding: 9px 24px;
  font-weight: 500;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #e6a23c 0%, #f0b45c 100%);
  border: none;
}

.dialog-footer .el-button--primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 响应式 */
@media (max-width: 768px) {
  .staff-list {
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
/* 员工详情对话框样式 */
.staff-detail-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.staff-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #e6a23c 0%, #f0b45c 100%);
  padding: 20px 24px;
  margin: 0;
}

.staff-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.staff-detail-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.staff-detail-dialog ::v-deep .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-avatar {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eef2f6;
}

.detail-avatar-img {
  background: linear-gradient(135deg, #e6a23c 0%, #f0b45c 100%);
  color: white;
  font-weight: 600;
  font-size: 36px;
}

.detail-section {
  padding: 0 10px;
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
}
</style>