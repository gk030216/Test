<template>
  <div class="booking-container">
    <Navbar />

    <div class="booking-content">
      <div class="container">
        <!-- 顶部导航栏：面包屑 + 返回按钮同一行 -->
        <div class="top-nav">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/services' }">宠物服务</el-breadcrumb-item>
            <el-breadcrumb-item>服务预约</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" class="back-btn">返回</el-button>
        </div>

        <div class="booking-main" v-loading="loading">
          <!-- 服务信息 -->
          <div class="service-card">
            <div class="service-header">
              <el-image :src="service.image" fit="cover" class="service-img">
                <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
              </el-image>
              <div class="service-info">
                <h3>{{ service.name }}</h3>
                <p class="service-price">¥{{ service.price }}</p>
                <p class="service-duration">时长：{{ service.duration }}分钟</p>
              </div>
            </div>
          </div>

          <!-- 选择宠物 - 只显示默认宠物 + 切换按钮 -->
          <div class="form-card">
            <div class="form-title">
              <i class="el-icon-s-custom"></i>
              <span>选择宠物</span>
            </div>

            <!-- 显示当前选中的宠物 -->
            <div class="selected-pet" v-if="currentSelectedPet">
              <div class="selected-pet-info">
                <el-avatar :size="50" :src="currentSelectedPet.avatar" class="pet-avatar">
                  {{ currentSelectedPet.name ? currentSelectedPet.name.charAt(0).toUpperCase() : 'P' }}
                </el-avatar>
                <div class="pet-detail">
                  <div class="pet-name">{{ currentSelectedPet.name }}</div>
                  <div class="pet-type">{{ getTypeName(currentSelectedPet.type) }}</div>
                </div>
              </div>
              <el-button size="small" plain @click="openPetDialog" class="switch-pet-btn">
                <i class="el-icon-switch-button"></i> 切换宠物
              </el-button>
            </div>

            <!-- 无宠物提示 -->
            <div class="empty-pets" v-else>
              <i class="el-icon-warning"></i>
              <p>暂无宠物信息，请先添加宠物</p>
              <el-button type="primary" size="small" @click="$router.push('/my-pets')">去添加宠物</el-button>
            </div>
          </div>

          <!-- 预约时间 -->
          <div class="form-card">
            <div class="form-title">
              <i class="el-icon-date"></i>
              <span>选择时间</span>
            </div>
            <div class="date-section">
              <div class="date-label">预约日期</div>
              <el-date-picker
                  v-model="bookingDate"
                  type="date"
                  placeholder="请选择日期"
                  :disabled-date="disabledDate"
                  format="yyyy年MM月dd日"
                  value-format="yyyy-MM-dd"
                  style="width: 100%"
                  @change="handleDateChange"
              ></el-date-picker>
              <div class="date-tip">可预约未来15天内的服务</div>
            </div>
            <div class="time-section">
              <div class="time-label">预约时段</div>
              <div class="time-list">
                <div
                    v-for="time in timeSlots"
                    :key="time.value"
                    :class="['time-item', {
                      active: selectedTime === time.value,
                      disabled: isTimeDisabled(time.value),
                      conflict: timeConflict === time.value
                    }]"
                    @click="selectTime(time.value)"
                >
                  {{ time.label }}
                  <span v-if="timeConflict === time.value" class="conflict-tag">已预约</span>
                </div>
              </div>
              <div v-if="timeConflict" class="conflict-warning">
                <i class="el-icon-warning"></i> 您在该时间段已有其他预约，请选择其他时间
              </div>
            </div>
          </div>

          <!-- 备注信息 -->
          <div class="form-card">
            <div class="form-title">
              <i class="el-icon-edit"></i>
              <span>备注信息（选填）</span>
            </div>
            <el-input
                v-model="remark"
                type="textarea"
                :rows="3"
                placeholder="请填写特殊需求或备注信息..."
                maxlength="200"
                show-word-limit
            ></el-input>
          </div>

          <!-- 订单信息 -->
          <div class="order-card">
            <div class="order-row">
              <span>服务费用</span>
              <span>¥{{ service.price }}</span>
            </div>
            <div class="order-row total">
              <span>合计</span>
              <span class="total-price">¥{{ service.price }}</span>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="submit-section">
            <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :disabled="!canSubmit"
                @click="submitBooking"
                :loading="submitting"
            >
              提交预约
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 宠物选择对话框 -->
    <el-dialog
        title="选择宠物"
        :visible.sync="petDialogVisible"
        width="500px"
        center
        class="pet-dialog"
        :close-on-click-modal="false"
    >
      <div class="pet-dialog-content">
        <div class="pet-list">
          <div
              v-for="pet in allPetList"
              :key="pet.id"
              :class="['pet-item', { active: tempSelectedPetId === pet.id }]"
              @click="tempSelectedPetId = pet.id"
          >
            <el-avatar :size="50" :src="pet.avatar" class="pet-avatar">
              {{ pet.name ? pet.name.charAt(0).toUpperCase() : 'P' }}
            </el-avatar>
            <div class="pet-info">
              <div class="pet-name">{{ pet.name }}</div>
              <div class="pet-type">{{ getTypeName(pet.type) }}</div>
              <div class="pet-breed" v-if="pet.breed">{{ pet.breed }}</div>
            </div>
            <i v-if="tempSelectedPetId === pet.id" class="el-icon-check selected-icon"></i>
          </div>

          <div class="empty-pets" v-if="allPetList.length === 0">
            <i class="el-icon-warning"></i>
            <p>暂无宠物信息</p>
            <el-button type="primary" size="small" @click="$router.push('/my-pets')">去添加宠物</el-button>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="petDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPetSelect" :disabled="!tempSelectedPetId">确定选择</el-button>
      </span>
    </el-dialog>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getServiceDetail } from '@/api/service';
import { getPetList } from '@/api/pet';
import { createAppointment, checkTimeConflict } from '@/api/service';

export default {
  name: 'ServiceBooking',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      submitting: false,
      service: {},
      allPetList: [],           // 所有宠物列表
      currentSelectedPet: null, // 当前选中的宠物
      selectedPetId: null,
      bookingDate: '',
      selectedTime: '',
      remark: '',
      timeConflict: null,
      // 宠物对话框
      petDialogVisible: false,
      tempSelectedPetId: null,
      timeSlots: [
        { label: '09:00-10:00', value: '09:00-10:00' },
        { label: '10:00-11:00', value: '10:00-11:00' },
        { label: '11:00-12:00', value: '11:00-12:00' },
        { label: '13:00-14:00', value: '13:00-14:00' },
        { label: '14:00-15:00', value: '14:00-15:00' },
        { label: '15:00-16:00', value: '15:00-16:00' },
        { label: '16:00-17:00', value: '16:00-17:00' }
      ]
    };
  },
  computed: {
    serviceId() {
      return this.$route.params.id;
    },
    canSubmit() {
      return this.selectedPetId && this.bookingDate && this.selectedTime && this.timeConflict !== this.selectedTime;
    }
  },
  created() {
    this.loadService();
    this.loadPets();
  },
  methods: {
    async loadService() {
      this.loading = true;
      try {
        const res = await getServiceDetail(this.serviceId);
        if (res.code === 200) {
          this.service = res.data;
        } else {
          this.$message.error('服务不存在');
          this.$router.push('/services');
        }
      } catch (error) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadPets() {
      try {
        const res = await getPetList();
        if (res.code === 200) {
          this.allPetList = res.data || [];
          if (this.allPetList.length > 0) {
            // 默认选中默认宠物
            const defaultPet = this.allPetList.find(p => p.isDefault === 1);
            if (defaultPet) {
              this.currentSelectedPet = defaultPet;
              this.selectedPetId = defaultPet.id;
            } else {
              // 没有默认宠物，选中第一个
              this.currentSelectedPet = this.allPetList[0];
              this.selectedPetId = this.allPetList[0].id;
            }
          }
        }
      } catch (error) {
        console.error('加载宠物列表失败', error);
      }
    },

    // 打开宠物选择对话框
    openPetDialog() {
      this.tempSelectedPetId = this.selectedPetId;
      this.petDialogVisible = true;
    },

    // 确认选择宠物
    confirmPetSelect() {
      const selected = this.allPetList.find(pet => pet.id === this.tempSelectedPetId);
      if (selected) {
        this.currentSelectedPet = selected;
        this.selectedPetId = selected.id;
        this.petDialogVisible = false;
        this.$message.success(`已选择宠物：${selected.name}`);
      }
    },

    // 日期限制：不能选过去，只能选未来15天
    disabledDate(time) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const maxDate = new Date();
      maxDate.setDate(maxDate.getDate() + 15);
      maxDate.setHours(23, 59, 59, 999);

      return time.getTime() < today.getTime() || time.getTime() > maxDate.getTime();
    },

    handleDateChange() {
      this.selectedTime = '';
      this.timeConflict = null;
    },

    async checkTimeConflict(timeValue) {
      if (!this.bookingDate) {
        return false;
      }

      try {
        const res = await checkTimeConflict({
          appointmentDate: this.bookingDate,
          appointmentTime: timeValue
        });

        if (res.code === 200) {
          return res.data.hasConflict === true;
        }
        return false;
      } catch (error) {
        console.error('检查时间冲突失败', error);
        return false;
      }
    },

    async selectTime(time) {
      if (this.isTimeDisabled(time)) {
        return;
      }

      const hasConflict = await this.checkTimeConflict(time);

      if (hasConflict) {
        this.timeConflict = time;
        this.$message.warning('您在该时间段已有其他预约，请选择其他时间');
        return;
      }

      this.timeConflict = null;
      this.selectedTime = time;
    },

    isTimeDisabled(time) {
      if (!this.bookingDate) return false;
      if (this.bookingDate === this.formatDate(new Date())) {
        const hour = parseInt(time.split(':')[0]);
        const currentHour = new Date().getHours();
        return hour <= currentHour;
      }
      return false;
    },

    formatDate(date) {
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    getTypeName(type) {
      const map = { dog: '狗狗', cat: '猫咪', rabbit: '兔子', bird: '鸟类', fish: '鱼类', other: '其他' };
      return map[type] || '宠物';
    },

    async submitBooking() {
      if (!this.canSubmit) {
        this.$message.warning('请完整填写预约信息');
        return;
      }

      this.submitting = true;
      try {
        const data = {
          petId: this.selectedPetId,
          serviceId: this.serviceId,
          appointmentDate: this.bookingDate,
          appointmentTime: this.selectedTime,
          remark: this.remark
        };
        const res = await createAppointment(data);
        if (res.code === 200) {
          this.$message.success('预约成功，即将跳转支付');
          this.$router.push(`/pay/${res.data.appointmentNo}`);
        } else {
          this.$message.error(res.message || '预约失败');
        }
      } catch (error) {
        console.error('预约失败', error);
        this.$message.error(error.response?.data?.message || '预约失败');
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
.booking-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.booking-content {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航栏 */
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

/* 服务卡片 */
.service-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
}

.service-header {
  display: flex;
  gap: 16px;
}

.service-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f7fa;
}

.service-info h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
}

.service-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
}

.service-duration {
  color: #909399;
  font-size: 13px;
}

/* 表单卡片 */
.form-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid #eef2f6;
}

.form-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eef2f6;
}

.form-title i {
  color: #409EFF;
  font-size: 18px;
}

/* 当前选中的宠物 */
.selected-pet {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #ecf5ff;
  border-radius: 12px;
  border: 1px solid #d9ecff;
}

.selected-pet-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.pet-avatar {
  background: #409EFF;
  color: white;
}

.pet-detail {
  flex: 1;
}

.pet-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
  margin-bottom: 4px;
}

.pet-type {
  font-size: 12px;
  color: #909399;
}

.switch-pet-btn {
  border-radius: 8px;
}

/* 宠物对话框 */
.pet-dialog ::v-deep .el-dialog {
  border-radius: 16px;
}

.pet-dialog ::v-deep .el-dialog__header {
  background: #409EFF;
  padding: 16px 20px;
  margin: 0;
  border-radius: 16px 16px 0 0;
}

.pet-dialog ::v-deep .el-dialog__title {
  color: white;
  font-weight: 500;
}

.pet-dialog ::v-deep .el-dialog__close {
  color: white;
}

.pet-dialog-content {
  max-height: 400px;
  overflow-y: auto;
}

.pet-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pet-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 16px;
  border: 1px solid #eef2f6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.pet-item:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}

.pet-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
}

.pet-avatar {
  background: #409EFF;
  color: white;
  flex-shrink: 0;
}

.pet-info {
  flex: 1;
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

.pet-breed {
  font-size: 11px;
  color: #c0c4cc;
}

.selected-icon {
  color: #67c23a;
  font-size: 18px;
  margin-left: 8px;
}

.empty-pets {
  text-align: center;
  padding: 30px;
  color: #909399;
}

.empty-pets i {
  font-size: 48px;
  margin-bottom: 12px;
  color: #c0c4cc;
}

.dialog-footer {
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
}

/* 时间选择 */
.date-section, .time-section {
  margin-bottom: 20px;
}

.date-label, .time-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  font-weight: 500;
}

.date-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.time-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.time-item {
  padding: 8px 20px;
  border: 1px solid #eef2f6;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #606266;
}

.time-item:hover {
  border-color: #409EFF;
  color: #409EFF;
}

.time-item.active {
  background: #409EFF;
  border-color: #409EFF;
  color: white;
}

.time-item.disabled {
  color: #c0c4cc;
  cursor: not-allowed;
  background: #f5f7fa;
}

.time-item.disabled:hover {
  border-color: #eef2f6;
  color: #c0c4cc;
}

/* 订单卡片 */
.order-card {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eef2f6;
}

.order-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  color: #606266;
}

.order-row.total {
  padding-top: 15px;
  margin-top: 10px;
  border-top: 1px solid #eef2f6;
  font-size: 18px;
  font-weight: bold;
}

.total-price {
  color: #f56c6c;
  font-size: 24px;
}

/* 提交按钮 */
.submit-section {
  text-align: center;
}

.submit-btn {
  background: #409EFF;
  border: none;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  width: 100%;
  transition: all 0.3s;
}

.submit-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

.submit-btn:disabled {
  background: #a0cfff;
  opacity: 0.7;
  transform: none;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 24px;
}

/* 冲突样式 */
.time-item.conflict {
  background: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
  cursor: not-allowed;
  position: relative;
}

.conflict-tag {
  font-size: 10px;
  margin-left: 4px;
  background: #f56c6c;
  color: white;
  padding: 2px 4px;
  border-radius: 4px;
}

.conflict-warning {
  margin-top: 12px;
  padding: 8px 12px;
  background: #fef0f0;
  border-radius: 8px;
  color: #f56c6c;
  font-size: 13px;
}

.conflict-warning i {
  margin-right: 6px;
}

/* 响应式 */
@media (max-width: 768px) {
  .booking-content {
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

  .service-header {
    flex-direction: column;
    text-align: center;
  }

  .service-img {
    margin: 0 auto;
  }

  .time-list {
    justify-content: center;
  }

  .selected-pet {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .selected-pet-info {
    flex-direction: column;
  }

  .pet-item {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
  }

  .selected-icon {
    position: absolute;
    top: 8px;
    right: 12px;
  }
}
</style>