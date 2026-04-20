<template>
  <div class="checkout-container">
    <Navbar />

    <div class="checkout-content">
      <div class="container">
        <!-- 顶部导航 -->
        <div class="top-nav">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/cart' }">购物车</el-breadcrumb-item>
            <el-breadcrumb-item>订单确认</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" class="back-btn">返回</el-button>
        </div>

        <div class="checkout-main" v-loading="loading">
          <!-- 收货地址 -->
          <div class="address-section">
            <div class="section-header">
              <span><i class="el-icon-location"></i> 收货地址</span>
              <el-button type="text" @click="openAddressDialog">
                <i class="el-icon-setting"></i> 管理地址
              </el-button>
            </div>

            <!-- 显示当前选中的地址 -->
            <div class="selected-address" v-if="selectedAddress">
              <div class="address-card">
                <div class="address-card-content">
                  <div class="address-name">
                    <span>{{ selectedAddress.receiverName }}</span>
                    <span class="address-phone">{{ selectedAddress.receiverPhone }}</span>
                    <el-tag v-if="selectedAddress.isDefault === 1" size="mini" type="success" class="default-tag">默认地址</el-tag>
                    <el-tag v-else size="mini" type="info" class="temp-tag">本次使用</el-tag>
                  </div>
                  <div class="address-detail">
                    {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detailAddress }}
                  </div>
                </div>
              </div>
              <div class="address-tip" v-if="selectedAddress.isDefault !== 1">
                <i class="el-icon-info"></i>
                当前为临时地址，仅本次订单使用，不会改变默认地址设置
              </div>
            </div>

            <!-- 无地址时显示提示 -->
            <div class="no-address" v-else>
              <i class="el-icon-location-outline"></i>
              <p>暂无收货地址</p>
              <el-button type="primary" size="small" @click="openAddressDialog">去添加地址</el-button>
            </div>
          </div>

          <!-- 商品列表 -->
          <div class="product-section">
            <div class="section-header">
              <span><i class="el-icon-goods"></i> 商品清单</span>
              <span class="item-count">共 {{ checkoutItems.length }} 件商品</span>
            </div>
            <div class="product-list">
              <div class="product-item" v-for="item in checkoutItems" :key="item.productId">
                <img :src="item.productImage" :alt="item.productName">
                <div class="product-info">
                  <h4>{{ item.productName }}</h4>
                  <div class="product-price">¥{{ item.price }}</div>
                </div>
                <div class="product-quantity">x{{ item.quantity }}</div>
                <div class="product-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              </div>
            </div>
          </div>

          <!-- 订单信息 -->
          <div class="order-info">
            <div class="info-row">
              <span>商品总价</span>
              <span>¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="info-row">
              <span>运费</span>
              <span class="free-shipping">免运费</span>
            </div>
            <div class="info-row total-row">
              <span>实付金额</span>
              <span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="submit-section">
            <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :disabled="!canSubmit"
                @click="createOrder"
                :loading="submitting"
            >
              {{ submitBtnText }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址选择对话框 - 仅用于选择本次订单使用的地址，不修改默认状态 -->
    <el-dialog
        title="选择收货地址"
        :visible.sync="addressDialogVisible"
        width="650px"
        center
        class="address-dialog"
        :close-on-click-modal="false"
    >
      <div class="address-dialog-content">
        <div class="address-list">
          <div
              v-for="addr in addressList"
              :key="addr.id"
              :class="['address-item', { active: tempSelectedId === addr.id }]"
              @click="tempSelectedId = addr.id"
          >
            <div class="address-info">
              <div class="address-name">
                <span>{{ addr.receiverName }}</span>
                <span class="address-phone">{{ addr.receiverPhone }}</span>
                <el-tag v-if="addr.isDefault === 1" size="mini" type="success">默认地址</el-tag>
              </div>
              <div class="address-detail">
                {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
              </div>
            </div>
            <div class="address-actions">
              <el-button type="text" size="small" @click.stop="editAddress(addr)">
                <i class="el-icon-edit"></i> 编辑
              </el-button>
              <el-button type="text" size="small" class="delete-btn" @click.stop="deleteAddress(addr)">
                <i class="el-icon-delete"></i> 删除
              </el-button>
            </div>
          </div>

          <!-- 新增地址按钮 -->
          <div class="add-address-btn" @click="showAddForm = true">
            <i class="el-icon-plus"></i>
            <span>新增地址</span>
          </div>
        </div>

        <!-- 新增/编辑地址表单 -->
        <div class="address-form" v-if="showAddForm || editingAddress">
          <div class="form-header">
            <span>{{ editingAddress ? '编辑地址' : '新增地址' }}</span>
            <i class="el-icon-close" @click="closeForm"></i>
          </div>
          <el-form :model="addressForm" :rules="addressRules" ref="addressForm" label-width="80px" size="small">
            <el-form-item label="收货人" prop="receiverName">
              <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名"></el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="receiverPhone">
              <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号"></el-input>
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
              <el-input v-model="addressForm.detailAddress" type="textarea" :rows="2" placeholder="请输入详细地址"></el-input>
            </el-form-item>
            <el-form-item label="设为默认">
              <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0"></el-switch>
              <span class="form-tip">设为默认后，下次下单会自动选中</span>
            </el-form-item>
          </el-form>
          <div class="form-actions">
            <el-button size="small" @click="closeForm">取消</el-button>
            <el-button type="primary" size="small" @click="submitAddress" :loading="formLoading">确定</el-button>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddressSelect" :disabled="!tempSelectedId">使用此地址</el-button>
      </span>
    </el-dialog>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address';
import { createOrder } from '@/api/order';
import { getCartList } from '@/api/cart';
import { regionData } from 'element-china-area-data';

export default {
  name: 'Checkout',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      submitting: false,
      formLoading: false,
      addressList: [],
      selectedAddress: null,      // 当前订单使用的地址（可能是默认，也可能是临时选择的）
      checkoutItems: [],
      // 地址对话框
      addressDialogVisible: false,
      tempSelectedId: null,
      showAddForm: false,
      editingAddress: null,
      addressForm: {
        id: null,
        receiverName: '',
        receiverPhone: '',
        selectedArea: [],
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: 0
      },
      areaOptions: regionData,
      areaProps: {
        value: 'value',
        label: 'label',
        children: 'children'
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
        detailAddress: [
          { required: true, message: '请输入详细地址', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    totalAmount() {
      return this.checkoutItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
    },
    canSubmit() {
      return this.selectedAddress && this.checkoutItems.length > 0;
    },
    submitBtnText() {
      if (!this.selectedAddress) return '请先选择收货地址';
      if (this.checkoutItems.length === 0) return '请选择商品';
      return '提交订单';
    }
  },
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const items = localStorage.getItem('checkoutItems');
        if (items) {
          this.checkoutItems = JSON.parse(items);
        } else {
          const cartRes = await getCartList();
          if (cartRes.code === 200) {
            this.checkoutItems = cartRes.data.filter(item => item.selected === 1);
          }
        }

        if (this.checkoutItems.length === 0) {
          this.$message.warning('请选择要购买的商品');
          this.$router.push('/cart');
          return;
        }

        await this.loadAddressList();
      } catch (error) {
        console.error('加载数据失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadAddressList() {
      try {
        const res = await getAddressList();
        if (res.code === 200) {
          this.addressList = res.data || [];
          // 默认选中默认地址（如果有）
          const defaultAddr = this.addressList.find(addr => addr.isDefault === 1);
          if (defaultAddr) {
            this.selectedAddress = defaultAddr;
            this.tempSelectedId = defaultAddr.id;
          } else if (this.addressList.length > 0) {
            // 没有默认地址时选中第一个
            this.selectedAddress = this.addressList[0];
            this.tempSelectedId = this.addressList[0].id;
          }
        }
      } catch (error) {
        console.error('加载地址失败', error);
      }
    },

    // 打开地址选择对话框
    openAddressDialog() {
      this.loadAddressList();
      this.tempSelectedId = this.selectedAddress?.id || null;
      this.showAddForm = false;
      this.editingAddress = null;
      this.addressDialogVisible = true;
    },

    // 确认选择地址（仅用于本次订单，不修改默认状态）
    confirmAddressSelect() {
      const selected = this.addressList.find(addr => addr.id === this.tempSelectedId);
      if (selected) {
        this.selectedAddress = selected;
        this.addressDialogVisible = false;
        this.$message.success(`已选择地址：${selected.receiverName} ${selected.receiverPhone}`);
      }
    },

    // 编辑地址（会修改地址信息，可能影响默认状态）
    editAddress(addr) {
      this.editingAddress = addr;
      this.addressForm = {
        id: addr.id,
        receiverName: addr.receiverName,
        receiverPhone: addr.receiverPhone,
        selectedArea: this.findAreaCodes(addr.province, addr.city, addr.district),
        province: addr.province,
        city: addr.city,
        district: addr.district,
        detailAddress: addr.detailAddress,
        isDefault: addr.isDefault || 0
      };
      this.showAddForm = true;
    },

    // 删除地址
    async deleteAddress(addr) {
      if (addr.isDefault === 1 && this.addressList.filter(a => a.isDefault === 1).length === 1) {
        this.$message.warning('只有一个默认地址，不能删除。请先添加其他地址并设为默认');
        return;
      }
      this.$confirm(`确定删除地址"${addr.receiverName} ${addr.receiverPhone}"吗？`, '提示', {
        type: 'warning'
      }).then(async () => {
        await deleteAddress(addr.id);
        this.$message.success('删除成功');
        await this.loadAddressList();
        // 如果删除的是当前选中的地址，重新选中默认地址或第一个
        if (this.selectedAddress?.id === addr.id) {
          const defaultAddr = this.addressList.find(a => a.isDefault === 1);
          this.selectedAddress = defaultAddr || this.addressList[0] || null;
          this.tempSelectedId = this.selectedAddress?.id || null;
        }
      }).catch(() => {});
    },

    // 关闭表单
    closeForm() {
      this.showAddForm = false;
      this.editingAddress = null;
      this.addressForm = {
        id: null,
        receiverName: '',
        receiverPhone: '',
        selectedArea: [],
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
      if (value && value.length > 0) {
        const provinceItem = this.findItemByCode(this.areaOptions, value[0]);
        this.addressForm.province = provinceItem ? provinceItem.label : '';
        if (value.length >= 2 && provinceItem && provinceItem.children) {
          const cityItem = this.findItemByCode(provinceItem.children, value[1]);
          this.addressForm.city = cityItem ? cityItem.label : '';
          if (value.length >= 3 && cityItem && cityItem.children) {
            const districtItem = this.findItemByCode(cityItem.children, value[2]);
            this.addressForm.district = districtItem ? districtItem.label : '';
          }
        }
      }
    },

    findItemByCode(list, code) {
      if (!list || !code) return null;
      for (const item of list) {
        if (item.value === code) return item;
      }
      return null;
    },

    // 提交地址表单（新增或编辑）
    async submitAddress() {
      this.$refs.addressForm.validate(async (valid) => {
        if (!valid) return;

        if (!this.addressForm.province || !this.addressForm.city || !this.addressForm.district) {
          this.$message.warning('请完整选择所在地区');
          return;
        }

        this.formLoading = true;
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

          let res;
          if (this.editingAddress) {
            res = await updateAddress({ ...submitData, id: this.editingAddress.id });
          } else {
            res = await addAddress(submitData);
          }

          if (res.code === 200) {
            this.$message.success(this.editingAddress ? '修改成功' : '添加成功');
            this.closeForm();
            await this.loadAddressList();
            // 如果编辑的是当前选中的地址，更新选中地址
            if (this.editingAddress && this.selectedAddress?.id === this.editingAddress.id) {
              const updated = this.addressList.find(a => a.id === this.editingAddress.id);
              if (updated) this.selectedAddress = updated;
            }
            // 如果是新增且设为默认，刷新后选中新默认地址
            if (!this.editingAddress && this.addressForm.isDefault === 1) {
              const newDefault = this.addressList.find(a => a.isDefault === 1);
              if (newDefault) this.selectedAddress = newDefault;
            }
            this.tempSelectedId = this.selectedAddress?.id || null;
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          this.$message.error(this.editingAddress ? '修改失败' : '添加失败');
        } finally {
          this.formLoading = false;
        }
      });
    },

    async createOrder() {
      if (!this.selectedAddress) {
        this.$message.warning('请选择收货地址');
        return;
      }

      if (this.checkoutItems.length === 0) {
        this.$message.warning('请选择要购买的商品');
        return;
      }

      this.submitting = true;
      try {
        const orderItems = this.checkoutItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity
        }));

        // 使用当前选中的地址创建订单（不修改任何地址的默认状态）
        const res = await createOrder({
          addressId: this.selectedAddress.id,
          items: orderItems,
          remark: ''
        });

        if (res.code === 200) {
          localStorage.removeItem('checkoutItems');
          this.$message.success('订单创建成功，即将跳转支付');
          this.$router.push(`/pay/${res.data.orderNo}`);
        } else {
          this.$message.error(res.message || '创建订单失败');
        }
      } catch (error) {
        console.error('创建订单失败', error);
        this.$message.error(error.message || '创建订单失败');
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
/* 样式与之前相同，略作调整 */
.checkout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.checkout-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.breadcrumb {
  flex: 1;
}

.back-btn {
  border-radius: 8px;
  color: #606266;
  background: white;
  border: 1px solid #eef2f6;
  padding: 8px 16px;
  font-size: 13px;
  transition: all 0.3s;
  flex-shrink: 0;
  margin-left: 16px;
}

.back-btn:hover {
  color: #409EFF;
  border-color: #409EFF;
  background: #ecf5ff;
}

.address-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eef2f6;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
}

.section-header i {
  margin-right: 6px;
  color: #409EFF;
}

.section-header .el-button--text {
  color: #409EFF;
  font-weight: normal;
}

.selected-address {
  background: #ecf5ff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #d9ecff;
}

.address-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.address-card-content {
  flex: 1;
}

.address-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.address-phone {
  font-weight: normal;
  color: #606266;
  font-size: 13px;
}

.default-tag {
  background: #67c23a;
  border: none;
  color: white;
}

.temp-tag {
  background: #909399;
  border: none;
  color: white;
}

.address-detail {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.address-tip {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #d9ecff;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 6px;
}

.address-tip i {
  color: #409EFF;
}

.no-address {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-address i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.no-address p {
  margin-bottom: 16px;
  font-size: 14px;
}

.product-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eef2f6;
}

.item-count {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}

.product-list {
  margin-top: 8px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #eef2f6;
}

.product-item:last-child {
  border-bottom: none;
}

.product-item img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 16px;
  background: #f5f7fa;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.product-price {
  color: #909399;
  font-size: 13px;
}

.product-quantity {
  width: 80px;
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.product-total {
  width: 100px;
  text-align: right;
  color: #f56c6c;
  font-weight: 600;
  font-size: 15px;
}

.order-info {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eef2f6;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  color: #606266;
  font-size: 14px;
}

.free-shipping {
  color: #67c23a;
}

.total-row {
  padding-top: 16px;
  margin-top: 12px;
  border-top: 1px solid #eef2f6;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.total-price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.submit-section {
  text-align: right;
}

.submit-btn {
  background: #409EFF;
  border: none;
  padding: 14px 60px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s;
}

.submit-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

.submit-btn:disabled {
  background: #a0cfff;
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

/* 地址对话框样式 */
.address-dialog ::v-deep .el-dialog {
  border-radius: 16px;
}

.address-dialog ::v-deep .el-dialog__header {
  background: #409EFF;
  padding: 16px 20px;
  margin: 0;
  border-radius: 16px 16px 0 0;
}

.address-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 500;
}

.address-dialog ::v-deep .el-dialog__close {
  color: white;
}

.address-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #eef2f6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}

.address-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.address-info {
  flex: 1;
}

.address-name {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.address-phone {
  font-weight: normal;
  color: #909399;
  font-size: 12px;
}

.address-detail {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.address-actions .el-button--text {
  color: #909399;
  padding: 4px 8px;
}

.address-actions .el-button--text:hover {
  color: #409EFF;
}

.address-actions .delete-btn:hover {
  color: #f56c6c;
}

.add-address-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border: 1px dashed #dcdfe6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  color: #909399;
}

.add-address-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
  background: #ecf5ff;
}

.address-form {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.form-header i {
  cursor: pointer;
  color: #909399;
  font-size: 18px;
}

.form-header i:hover {
  color: #f56c6c;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.dialog-footer {
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
}

/* 响应式 */
@media (max-width: 768px) {
  .checkout-content {
    padding: 20px 0 40px;
  }

  .top-nav {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .back-btn {
    margin-left: 0;
    padding: 6px 12px;
  }

  .product-item {
    flex-wrap: wrap;
    gap: 12px;
  }

  .product-quantity,
  .product-total {
    width: auto;
    margin-left: auto;
  }

  .product-quantity {
    margin-left: 86px;
  }

  .submit-btn {
    width: 100%;
    padding: 12px 20px;
  }

  .address-name {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .address-item {
    flex-direction: column;
    gap: 12px;
  }

  .address-actions {
    align-self: flex-end;
  }

  .address-dialog ::v-deep .el-dialog {
    width: 95% !important;
  }
}
</style>