<template>
  <div class="address-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">收货地址</h2>
        <p class="page-desc">管理你的收货地址，最多可添加20个地址</p>
      </div>
      <el-button type="primary" size="small" @click="handleAdd" class="add-btn">
        <i class="el-icon-plus"></i> 新增地址
      </el-button>
    </div>

    <div class="address-list" v-loading="loading">
      <div
          class="address-card"
          v-for="addr in addressList"
          :key="addr.id"
          :class="{ 'is-default': addr.isDefault === 1 }"
          @click="viewAddress(addr)"
      >
        <div class="card-header">
          <div class="address-tags">
            <span class="receiver-name">{{ addr.receiverName }}</span>
            <span class="receiver-phone">{{ addr.receiverPhone }}</span>
            <el-tag v-if="addr.isDefault === 1" size="mini" type="success" effect="dark">默认</el-tag>
          </div>
          <div class="card-actions" @click.stop>
            <el-button
                type="text"
                size="small"
                @click="setDefault(addr.id)"
                v-if="addr.isDefault !== 1"
                class="action-link"
            >
              <i class="el-icon-check"></i> 设为默认
            </el-button>
            <el-button type="text" size="small" @click="editAddress(addr)" class="action-link">
              <i class="el-icon-edit"></i> 编辑
            </el-button>
            <el-button type="text" size="small" @click="deleteAddress(addr.id)" class="action-link delete-link">
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </div>
        </div>

        <div class="card-body">
          <div class="address-full">
            <i class="el-icon-location-outline"></i>
            <span>{{ getFullAddress(addr) }}</span>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && addressList.length === 0">
        <div class="empty-icon">
          <i class="el-icon-location-outline"></i>
        </div>
        <p>暂无收货地址</p>
        <el-button type="primary" size="small" @click="handleAdd">新增地址</el-button>
      </div>
    </div>

    <!-- 新增/编辑地址对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="showAddDialog"
        width="550px"
        center
        class="address-dialog"
        :close-on-click-modal="false"
        @closed="handleDialogClosed"
    >
      <div class="dialog-content">
        <el-form :model="addressForm" :rules="addressRules" ref="addressForm" label-width="80px">
          <el-form-item label="收货人" prop="receiverName">
            <el-input
                v-model="addressForm.receiverName"
                placeholder="请输入收货人姓名"
                prefix-icon="el-icon-user"
            ></el-input>
          </el-form-item>

          <el-form-item label="手机号" prop="receiverPhone">
            <el-input
                v-model="addressForm.receiverPhone"
                placeholder="请输入手机号"
                prefix-icon="el-icon-phone"
            ></el-input>
          </el-form-item>

          <el-form-item label="所在地区" prop="selectedArea">
            <el-cascader
                v-model="addressForm.selectedArea"
                :options="areaOptions"
                :props="areaProps"
                placeholder="请选择省/市/区"
                style="width: 100%"
                clearable
                @change="handleAreaChange"
            ></el-cascader>
          </el-form-item>

          <el-form-item label="详细地址" prop="detailAddress">
            <el-input
                v-model="addressForm.detailAddress"
                type="textarea"
                :rows="3"
                placeholder="请输入详细地址，如街道、门牌号等"
            ></el-input>
          </el-form-item>

          <el-form-item label="设为默认">
            <el-switch
                v-model="addressForm.isDefault"
                :active-value="1"
                :inactive-value="0"
                active-text="是"
                inactive-text="否"
            ></el-switch>
            <span class="form-tip">设为默认后，下单时会自动选中该地址</span>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAddress" :loading="submitLoading">
          {{ isEdit ? '保存修改' : '立即创建' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 查看地址详情对话框 -->
    <el-dialog
        title="地址详情"
        :visible.sync="viewDialogVisible"
        width="500px"
        center
        class="view-dialog"
    >
      <div class="view-content" v-if="currentAddress">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="收货人">
            {{ currentAddress.receiverName }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ currentAddress.receiverPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="所在地区">
            {{ getFullAddress(currentAddress) }}
          </el-descriptions-item>
          <el-descriptions-item label="详细地址">
            {{ currentAddress.detailAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="是否默认">
            <el-tag v-if="currentAddress.isDefault === 1" type="success" size="small">默认地址</el-tag>
            <el-tag v-else type="info" size="small">普通地址</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer">
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="editFromView">编辑</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address';
import { regionData, CodeToText, TextToCode } from 'element-china-area-data';

export default {
  name: 'Address',
  data() {
    return {
      loading: false,
      submitLoading: false,
      addressList: [],
      showAddDialog: false,
      viewDialogVisible: false,
      isEdit: false,
      editId: null,
      currentAddress: null,
      areaOptions: regionData,
      areaProps: {
        value: 'value',
        label: 'label',
        children: 'children'
      },
      addressForm: {
        receiverName: '',
        receiverPhone: '',
        selectedArea: [],
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: 0
      },
      addressRules: {
        receiverName: [
          { required: true, message: '请输入收货人姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
        ],
        receiverPhone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        selectedArea: [
          { required: true, message: '请选择所在地区', trigger: 'change' }
        ],
        detailAddress: [
          { required: true, message: '请输入详细地址', trigger: 'blur' }
        ]
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
          this.addressList = res.data || [];
        }
      } catch (error) {
        console.error('加载地址列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    getFullAddress(addr) {
      if (!addr) return '';
      const parts = [addr.province, addr.city, addr.district];
      return parts.filter(p => p).join(' ') + ' ' + (addr.detailAddress || '');
    },

    async setDefault(id) {
      try {
        const res = await setDefaultAddress(id);
        if (res.code === 200) {
          this.$message.success('设置成功');
          this.loadAddressList();
        } else {
          this.$message.error(res.message || '设置失败');
        }
      } catch (error) {
        this.$message.error('设置失败');
      }
    },

    handleAdd() {
      this.isEdit = false;
      this.editId = null;
      this.addressForm = {
        receiverName: '',
        receiverPhone: '',
        selectedArea: [],
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: 0
      };
      this.showAddDialog = true;
      this.$nextTick(() => {
        if (this.$refs.addressForm) {
          this.$refs.addressForm.clearValidate();
        }
      });
    },

    viewAddress(addr) {
      this.currentAddress = addr;
      this.viewDialogVisible = true;
    },

    editFromView() {
      this.viewDialogVisible = false;
      this.editAddress(this.currentAddress);
    },

    editAddress(addr) {
      this.isEdit = true;
      this.editId = addr.id;

      const areaCodes = this.findAreaCodes(addr.province, addr.city, addr.district);

      this.addressForm = {
        receiverName: addr.receiverName || '',
        receiverPhone: addr.receiverPhone || '',
        selectedArea: areaCodes,
        province: addr.province || '',
        city: addr.city || '',
        district: addr.district || '',
        detailAddress: addr.detailAddress || '',
        isDefault: addr.isDefault || 0
      };
      this.showAddDialog = true;
      this.$nextTick(() => {
        if (this.$refs.addressForm) {
          this.$refs.addressForm.clearValidate();
        }
      });
    },

    findAreaCodes(province, city, district) {
      if (!province) return [];

      for (const p of this.areaOptions) {
        if (p.label === province) {
          if (!city) return [p.value];

          if (p.children) {
            for (const c of p.children) {
              if (c.label === city) {
                if (!district) return [p.value, c.value];

                if (c.children) {
                  for (const d of c.children) {
                    if (d.label === district) {
                      return [p.value, c.value, d.value];
                    }
                  }
                }
                return [p.value, c.value];
              }
            }
          }
          return [p.value];
        }
      }
      return [];
    },

    handleAreaChange(value) {
      console.log('选择的地区代码:', value);

      if (value && value.length > 0) {
        // 从 regionData 中查找对应的文本
        const provinceItem = this.findItemByCode(this.areaOptions, value[0]);
        this.addressForm.province = provinceItem ? provinceItem.label : '';

        if (value.length >= 2 && provinceItem && provinceItem.children) {
          const cityItem = this.findItemByCode(provinceItem.children, value[1]);
          this.addressForm.city = cityItem ? cityItem.label : '';

          if (value.length >= 3 && cityItem && cityItem.children) {
            const districtItem = this.findItemByCode(cityItem.children, value[2]);
            this.addressForm.district = districtItem ? districtItem.label : '';
          } else {
            this.addressForm.district = '';
          }
        } else {
          this.addressForm.city = '';
          this.addressForm.district = '';
        }

        console.log('转换后的地区:', {
          province: this.addressForm.province,
          city: this.addressForm.city,
          district: this.addressForm.district
        });
      } else {
        this.addressForm.province = '';
        this.addressForm.city = '';
        this.addressForm.district = '';
      }
    },

// 辅助方法：根据代码查找项目
    findItemByCode(list, code) {
      if (!list || !code) return null;
      for (const item of list) {
        if (item.value === code) {
          return item;
        }
      }
      return null;
    },

    handleDialogClosed() {
      this.addressForm = {
        receiverName: '',
        receiverPhone: '',
        selectedArea: [],
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: 0
      };
      this.isEdit = false;
      this.editId = null;
    },

    async deleteAddress(id) {
      this.$confirm('确定要删除该地址吗？删除后无法恢复！', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          const res = await deleteAddress(id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadAddressList();
          } else {
            this.$message.error(res.message || '删除失败');
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    submitAddress() {
      this.$refs.addressForm.validate(async (valid) => {
        if (!valid) return;

        // 确保地区数据已正确设置
        if (!this.addressForm.province || !this.addressForm.city || !this.addressForm.district) {
          this.$message.warning('请完整选择所在地区');
          return;
        }

        this.submitLoading = true;
        try {
          const submitData = {
            receiverName: this.addressForm.receiverName,
            receiverPhone: this.addressForm.receiverPhone,
            province: this.addressForm.province,
            city: this.addressForm.city,
            district: this.addressForm.district,
            detailAddress: this.addressForm.detailAddress,
            isDefault: this.addressForm.isDefault
          };

          console.log('提交的地址数据:', submitData); // 调试用

          let res;
          if (this.isEdit) {
            res = await updateAddress({ ...submitData, id: this.editId });
          } else {
            res = await addAddress(submitData);
          }

          if (res.code === 200) {
            this.$message.success(this.isEdit ? '更新成功' : '添加成功');
            this.showAddDialog = false;
            this.loadAddressList();
          } else {
            this.$message.error(res.message || (this.isEdit ? '更新失败' : '添加失败'));
          }
        } catch (error) {
          console.error('提交地址失败:', error);
          this.$message.error(this.isEdit ? '更新失败' : '添加失败');
        } finally {
          this.submitLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.address-page {
  padding: 0;
  background: transparent;
  min-height: 400px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.page-desc {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.add-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  font-weight: 500;
  padding: 8px 20px;
  border-radius: 20px;
  transition: all 0.3s;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
}

.address-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  border-color: #e0e0e0;
}

.address-card.is-default {
  border-color: #67c23a;
  background: #f0f9f4;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.address-tags {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.receiver-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
}

.receiver-phone {
  color: #666;
  font-size: 14px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.action-link {
  color: #909399;
  font-size: 13px;
  padding: 4px 8px;
  transition: all 0.3s;
}

.action-link i {
  margin-right: 4px;
}

.action-link:hover {
  color: #667eea;
}

.delete-link:hover {
  color: #f56c6c;
}

.card-body {
  padding-top: 8px;
}

.address-full {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #5a6874;
  font-size: 14px;
  line-height: 1.6;
}

.address-full i {
  color: #667eea;
  font-size: 16px;
  margin-top: 2px;
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 20px;
  color: #999;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: #f5f7fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon i {
  font-size: 36px;
  color: #c0c4cc;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

/* 对话框样式 */
.address-dialog ::v-deep .el-dialog,
.view-dialog ::v-deep .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.address-dialog ::v-deep .el-dialog__header,
.view-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.address-dialog ::v-deep .el-dialog__title,
.view-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.address-dialog ::v-deep .el-dialog__close,
.view-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 20px;
}

.address-dialog ::v-deep .el-dialog__body,
.view-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.view-content {
  max-height: 60vh;
  overflow-y: auto;
}

.address-dialog ::v-deep .el-form-item {
  margin-bottom: 20px;
}

.address-dialog ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #2c3e50;
}

.address-dialog ::v-deep .el-input__inner,
.address-dialog ::v-deep .el-cascader .el-input__inner {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.address-dialog ::v-deep .el-input__inner:focus,
.address-dialog ::v-deep .el-cascader .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.address-dialog ::v-deep .el-textarea__inner {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.address-dialog ::v-deep .el-textarea__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.address-dialog ::v-deep .el-cascader {
  width: 100%;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-left: 12px;
}

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

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .add-btn {
    width: 100%;
  }

  .address-card {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .card-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .address-tags {
    gap: 8px;
  }

  .receiver-name {
    font-size: 15px;
  }

  .receiver-phone {
    font-size: 13px;
  }
}
</style>