<template>
  <div class="booking-container">
    <Navbar />

    <div class="booking-content">
      <div class="container">
        <h2 class="page-title">服务预约</h2>

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

          <!-- 选择宠物 -->
          <div class="form-card">
            <div class="form-title">
              <i class="el-icon-s-custom"></i>
              <span>选择宠物</span>
            </div>
            <div class="pet-list">
              <div
                  v-for="pet in petList"
                  :key="pet.id"
                  :class="['pet-item', { active: selectedPetId === pet.id }]"
                  @click="selectedPetId = pet.id"
              >
                <el-avatar :size="50" :src="pet.avatar" class="pet-avatar">
                  {{ pet.name ? pet.name.charAt(0).toUpperCase() : 'P' }}
                </el-avatar>
                <div class="pet-info">
                  <div class="pet-name">{{ pet.name }}</div>
                  <div class="pet-type">{{ getTypeName(pet.type) }}</div>
                </div>
                <i v-if="selectedPetId === pet.id" class="el-icon-check selected-icon"></i>
              </div>
              <div class="empty-pets" v-if="petList.length === 0">
                <i class="el-icon-warning"></i>
                <p>暂无宠物信息，请先添加宠物</p>
                <el-button type="primary" size="small" @click="$router.push('/my-pets')">去添加宠物</el-button>
              </div>
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
              ></el-date-picker>
            </div>
            <div class="time-section">
              <div class="time-label">预约时段</div>
              <div class="time-list">
                <div
                    v-for="time in timeSlots"
                    :key="time.value"
                    :class="['time-item', { active: selectedTime === time.value, disabled: isTimeDisabled(time.value) }]"
                    @click="selectTime(time.value)"
                >
                  {{ time.label }}
                </div>
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

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import { getServiceDetail } from '@/api/service';
import { getPetList } from '@/api/pet';
import { createAppointment } from '@/api/service';

export default {
  name: 'ServiceBooking',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      submitting: false,
      service: {},
      petList: [],
      selectedPetId: null,
      bookingDate: '',
      selectedTime: '',
      remark: '',
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
      return this.selectedPetId && this.bookingDate && this.selectedTime;
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
          this.petList = res.data || [];
          // 默认选中第一个宠物
          if (this.petList.length > 0) {
            const defaultPet = this.petList.find(p => p.isDefault === 1);
            this.selectedPetId = defaultPet ? defaultPet.id : this.petList[0].id;
          }
        }
      } catch (error) {
        console.error('加载宠物列表失败', error);
      }
    },
    getTypeName(type) {
      const map = { dog: '狗狗', cat: '猫咪', rabbit: '兔子', bird: '鸟类', fish: '鱼类', other: '其他' };
      return map[type] || '宠物';
    },
    disabledDate(time) {
      // 不能选择今天之前的日期
      return time.getTime() < Date.now() - 24 * 60 * 60 * 1000;
    },
    isTimeDisabled(time) {
      // 如果日期未选择，不禁用
      if (!this.bookingDate) return false;
      // 如果是今天，检查时间是否已过
      if (this.bookingDate === this.formatDate(new Date())) {
        const hour = parseInt(time.split(':')[0]);
        const currentHour = new Date().getHours();
        return hour <= currentHour;
      }
      return false;
    },
    selectTime(time) {
      if (!this.isTimeDisabled(time)) {
        this.selectedTime = time;
      }
    },
    formatDate(date) {
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
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
          // 跳转到支付页面
          this.$router.push(`/pay/${res.data.appointmentNo}`);
        } else {
          this.$message.error(res.message || '预约失败');
        }
      } catch (error) {
        console.error('预约失败', error);
        this.$message.error('预约失败');
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
  background: #f8f9fa;
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

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 30px;
}

/* 服务卡片 */
.service-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.service-header {
  display: flex;
  gap: 20px;
}

.service-img {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  object-fit: cover;
}

.service-info h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #2c3e50;
}

.service-price {
  color: #ff6b6b;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
}

.service-duration {
  color: #999;
  font-size: 13px;
}

/* 表单卡片 */
.form-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.form-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.form-title i {
  color: #667eea;
  font-size: 18px;
}

/* 宠物列表 */
.pet-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pet-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.pet-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.pet-item.active {
  border-color: #667eea;
  background: #f8f9ff;
}

.pet-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
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
  color: #999;
}

.selected-icon {
  color: #67c23a;
  font-size: 20px;
}

.empty-pets {
  text-align: center;
  padding: 30px;
  color: #999;
}

.empty-pets i {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 时间选择 */
.date-section, .time-section {
  margin-bottom: 20px;
}

.date-label, .time-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  font-weight: 500;
}

.time-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.time-item {
  padding: 8px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.time-item:hover {
  border-color: #667eea;
  color: #667eea;
}

.time-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: transparent;
  color: white;
}

.time-item.disabled {
  color: #ccc;
  cursor: not-allowed;
  background: #f5f5f5;
}

.time-item.disabled:hover {
  border-color: #e0e0e0;
  color: #ccc;
}

/* 订单卡片 */
.order-card {
  background: #f8f9fc;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
}

.order-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  color: #666;
}

.order-row.total {
  padding-top: 15px;
  margin-top: 10px;
  border-top: 1px solid #e0e0e0;
  font-size: 18px;
  font-weight: bold;
}

.total-price {
  color: #ff6b6b;
  font-size: 24px;
}

/* 提交按钮 */
.submit-section {
  text-align: center;
}

.submit-btn {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  border: none;
  padding: 14px 60px;
  font-size: 18px;
  border-radius: 40px;
  width: 100%;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 107, 107, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  transform: none;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #999;
  font-size: 24px;
}

@media (max-width: 768px) {
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
}
</style>