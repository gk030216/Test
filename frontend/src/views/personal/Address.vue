<template>
  <div class="address-page">
    <div class="page-header">
      <h2 class="page-title">收货地址</h2>
      <el-button type="primary" size="small" @click="showAddDialog = true">
        <i class="el-icon-plus"></i> 新增地址
      </el-button>
    </div>

    <div class="address-list" v-loading="loading">
      <div class="address-item" v-for="addr in addressList" :key="addr.id">
        <div class="address-info">
          <div class="address-name">
            <span class="receiver">{{ addr.receiverName }}</span>
            <span class="phone">{{ addr.receiverPhone }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small" type="success" effect="dark">默认</el-tag>
          </div>
          <div class="address-detail">
            {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
          </div>
        </div>
        <div class="address-actions">
          <el-button type="text" @click="setDefault(addr.id)" v-if="addr.isDefault !== 1">设为默认</el-button>
          <el-button type="text" @click="editAddress(addr)">编辑</el-button>
          <el-button type="text" class="delete-btn" @click="deleteAddress(addr.id)">删除</el-button>
        </div>
      </div>

      <div class="empty-address" v-if="!loading && addressList.length === 0">
        <i class="el-icon-location-outline"></i>
        <p>暂无收货地址</p>
        <el-button type="primary" size="small" @click="showAddDialog = true">新增地址</el-button>
      </div>
    </div>

    <!-- 新增/编辑地址对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="showAddDialog" width="500px" center>
      <el-form :model="addressForm" :rules="addressRules" ref="addressForm" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="地区" prop="province">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="addressForm.province" placeholder="省"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="addressForm.city" placeholder="市"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="addressForm.district" placeholder="区/县"></el-input>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressForm.detailAddress" type="textarea" rows="2" placeholder="街道、门牌号等"></el-input>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAddress" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address';

export default {
  name: 'Address',
  data() {
    return {
      loading: false,
      submitLoading: false,
      addressList: [],
      showAddDialog: false,
      isEdit: false,
      editId: null,
      addressForm: {
        receiverName: '',
        receiverPhone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: 0
      },
      addressRules: {
        receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
        receiverPhone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
        city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
        district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
        detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑地址' : '新增地址';
    }
  },
  created() {
    this.loadAddressList();
  },
  methods: {
    async loadAddressList() {
      this.loading = true;
      try {
        const res = await getAddressList();
        if (res.code === 200) {
          this.addressList = res.data;
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async setDefault(id) {
      try {
        const res = await setDefaultAddress(id);
        if (res.code === 200) {
          this.$message.success('设置成功');
          this.loadAddressList();
        }
      } catch (error) {
        this.$message.error('设置失败');
      }
    },
    editAddress(addr) {
      this.isEdit = true;
      this.editId = addr.id;
      this.addressForm = {
        receiverName: addr.receiverName,
        receiverPhone: addr.receiverPhone,
        province: addr.province,
        city: addr.city,
        district: addr.district,
        detailAddress: addr.detailAddress,
        isDefault: addr.isDefault
      };
      this.showAddDialog = true;
    },
    async deleteAddress(id) {
      this.$confirm('确定要删除该地址吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deleteAddress(id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadAddressList();
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },
    submitAddress() {
      this.$refs.addressForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            const data = { ...this.addressForm, id: this.editId };
            res = await updateAddress(data);
          } else {
            res = await addAddress(this.addressForm);
          }
          if (res.code === 200) {
            this.$message.success(this.isEdit ? '更新成功' : '添加成功');
            this.showAddDialog = false;
            this.resetForm();
            this.loadAddressList();
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
    resetForm() {
      this.isEdit = false;
      this.editId = null;
      this.addressForm = {
        receiverName: '',
        receiverPhone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: 0
      };
      this.$nextTick(() => {
        if (this.$refs.addressForm) {
          this.$refs.addressForm.clearValidate();
        }
      });
    }
  }
};
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  font-size: 20px;
  margin: 0;
  color: #333;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  background: #f8f9fc;
  border-radius: 12px;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
}

.address-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.address-info {
  flex: 1;
}

.address-name {
  margin-bottom: 8px;
}

.receiver {
  font-weight: 600;
  color: #333;
  margin-right: 12px;
}

.phone {
  color: #999;
  font-size: 13px;
  margin-right: 12px;
}

.address-detail {
  color: #666;
  font-size: 13px;
}

.address-actions {
  display: flex;
  gap: 15px;
}

.address-actions .el-button {
  padding: 0;
  font-size: 13px;
}

.delete-btn {
  color: #f56c6c;
}

.empty-address {
  text-align: center;
  padding: 60px;
  color: #999;
}

.empty-address i {
  font-size: 64px;
  margin-bottom: 20px;
}
</style>