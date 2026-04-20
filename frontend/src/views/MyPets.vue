<template>
  <div class="my-pets-container">
    <Navbar />

    <div class="my-pets-wrapper">
      <div class="container">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="header-content">
            <div class="header-left">
              <h2 class="page-title">我的宠物</h2>
              <p class="page-desc">管理宠物档案，记录健康信息</p>
            </div>
            <el-button type="primary" size="large" @click="showAddDialog = true" class="add-btn">
              <i class="el-icon-plus"></i> 添加宠物
            </el-button>
          </div>
        </div>

        <!-- 宠物列表 -->
        <div class="pets-list" v-loading="loading">
          <div class="pet-card" v-for="pet in petList" :key="pet.id" @click="viewPetDetail(pet.id)">
            <div class="pet-card-inner">
              <div class="pet-avatar">
                <el-avatar :size="80" :src="pet.avatar" class="pet-avatar-img">
                  {{ !pet.avatar ? (pet.name ? pet.name.charAt(0).toUpperCase() : 'P') : '' }}
                </el-avatar>
                <span class="default-badge" v-if="pet.isDefault === 1">默认</span>
              </div>
              <div class="pet-info">
                <h3 class="pet-name">{{ pet.name }}</h3>
                <div class="pet-meta">
                  <span class="pet-type">{{ getTypeName(pet.type) }}</span>
                  <span class="pet-breed" v-if="pet.breed">{{ pet.breed }}</span>
                </div>
                <div class="pet-stats">
                  <span><i class="el-icon-cake"></i> {{ getAge(pet.birthday) }}</span>
                  <span><i class="el-icon-weight"></i> {{ pet.weight || '--' }} kg</span>
                  <span><i class="el-icon-heart"></i> {{ getGenderName(pet.gender) }}</span>
                </div>
              </div>
              <div class="pet-actions" @click.stop>
                <el-tooltip content="编辑" placement="top">
                  <el-button type="primary" circle size="small" @click="editPet(pet)">
                    <i class="el-icon-edit"></i>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button type="danger" circle size="small" @click="deletePet(pet)">
                    <i class="el-icon-delete"></i>
                  </el-button>
                </el-tooltip>
                <!--  恢复星形图标 -->
                <el-tooltip content="设为默认" placement="top" v-if="pet.isDefault !== 1">
                  <el-button type="warning" circle size="small" @click="setDefault(pet)">
                    <i class="el-icon-star-on"></i>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div class="empty-state" v-if="!loading && petList.length === 0">
            <i class="el-icon-document"></i>
            <p>暂无宠物</p>
            <el-button type="primary" size="small" @click="showAddDialog = true">添加宠物</el-button>
          </div>
        </div>

        <!-- 疫苗提醒卡片 -->
        <div class="reminder-card" v-if="upcomingVaccines && upcomingVaccines.length > 0">
          <div class="reminder-header">
            <i class="el-icon-warning-outline"></i>
            <span>疫苗提醒</span>
          </div>
          <div class="reminder-list">
            <div class="reminder-item" v-for="item in upcomingVaccines" :key="item.id">
              <div class="reminder-content">
                <div class="reminder-pet-name">{{ item.petName || '未知宠物' }}</div>
                <div class="reminder-vaccine">{{ item.vaccineName || '未知疫苗' }}</div>
              </div>
              <div class="reminder-date">
                <i class="el-icon-date"></i> {{ formatDate(item.nextDate) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑宠物对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="showAddDialog" width="560px" center class="pet-dialog" :close-on-click-modal="false">
      <el-form :model="petForm" :rules="petRules" ref="petForm" label-width="80px">
        <el-form-item label="宠物头像">
          <div class="avatar-upload" @click="triggerUpload">
            <el-avatar :size="80" :src="avatarPreview" class="upload-avatar">
              {{ !avatarPreview ? (petForm.name ? petForm.name.charAt(0).toUpperCase() : 'P') : '' }}
            </el-avatar>
            <div class="upload-tip">点击更换头像</div>
            <input type="file" ref="avatarInput" accept="image/*" style="display: none" @change="handleAvatarUpload">
          </div>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="宠物名称" prop="name">
              <el-input v-model="petForm.name" placeholder="请输入宠物名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="宠物类型" prop="type">
              <el-select v-model="petForm.type" placeholder="请选择" style="width: 100%">
                <el-option label="狗狗" value="dog"></el-option>
                <el-option label="猫咪" value="cat"></el-option>
                <el-option label="兔子" value="rabbit"></el-option>
                <el-option label="其他" value="other"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="品种">
              <el-input v-model="petForm.breed" placeholder="如：金毛"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="petForm.gender" placeholder="请选择" style="width: 100%">
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
              <el-date-picker
                  v-model="petForm.birthday"
                  type="date"
                  placeholder="选择日期"
                  value-format="yyyy-MM-dd"
                  style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input-number v-model="petForm.weight" :min="0" :max="100" :precision="1" controls-position="right" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="毛色">
          <el-input v-model="petForm.color" placeholder="如：金色"></el-input>
        </el-form-item>

        <el-form-item label="简介">
          <el-input v-model="petForm.description" type="textarea" :rows="3" placeholder="介绍一下你的宠物"></el-input>
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch
              v-model="petForm.isDefault"
              :active-value="1"
              :inactive-value="0">
          </el-switch>
          <span class="form-tip">设为默认后，下单时会自动选中</span>
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 宠物详情对话框 -->
    <el-dialog :title="currentPet && currentPet.name" :visible.sync="showDetailDialog" width="750px" center class="pet-detail-dialog" :close-on-click-modal="true" @close="handleDetailDialogClose">
      <!-- 内容保持不变 -->
      <div class="pet-detail" v-if="currentPet">
        <div class="detail-header">
          <div class="detail-avatar-wrapper">
            <el-avatar :size="80" :src="currentPet.avatar" class="detail-avatar">
              {{ !currentPet.avatar ? (currentPet.name ? currentPet.name.charAt(0).toUpperCase() : 'P') : '' }}
            </el-avatar>
          </div>
          <div class="detail-info">
            <h2>{{ currentPet.name }}</h2>
            <div class="detail-tags">
              <span class="tag">{{ getTypeName(currentPet.type) }}</span>
              <span class="tag" v-if="currentPet.breed">{{ currentPet.breed }}</span>
              <span class="tag" v-if="currentPet.gender === 1">公</span>
              <span class="tag" v-else-if="currentPet.gender === 2">母</span>
            </div>
            <div class="detail-stats">
              <span><i class="el-icon-cake"></i> {{ getAge(currentPet.birthday) }}</span>
              <span><i class="el-icon-weight"></i> {{ currentPet.weight || '--' }} kg</span>
              <span><i class="el-icon-color"></i> {{ currentPet.color || '--' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-tabs">
          <el-tabs v-model="activeTab" type="border-card">
            <el-tab-pane label="基本信息" name="basic">
              <div class="basic-info">
                <div class="info-item">
                  <div class="info-label">简介</div>
                  <div class="info-value">{{ currentPet.description || '暂无简介' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">生日</div>
                  <div class="info-value">{{ formatDate(currentPet.birthday) || '未知' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">毛色</div>
                  <div class="info-value">{{ currentPet.color || '未知' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">性别</div>
                  <div class="info-value">{{ getGenderName(currentPet.gender) }}</div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="疫苗记录" name="vaccine">
              <div class="vaccine-section">
                <div class="section-header">
                  <span>免疫记录</span>
                </div>
                <div class="vaccine-timeline">
                  <div class="timeline-item" v-for="item in (currentPet.vaccineRecords || [])" :key="item.id" @click="showVaccineDetail(item)">
                    <div class="timeline-dot"></div>
                    <div class="timeline-content">
                      <div class="timeline-title">{{ item.vaccineName }}</div>
                      <div class="timeline-info">
                        <span><i class="el-icon-date"></i> {{ formatDate(item.vaccineDate) }}</span>
                        <span v-if="item.nextDate"><i class="el-icon-bell"></i> {{ formatDate(item.nextDate) }}</span>
                        <span v-if="item.operatorName"><i class="el-icon-user"></i> {{ item.operatorName }}</span>
                      </div>
                      <div class="timeline-expand" v-if="item.remark">
                        <i class="el-icon-edit"></i> {{ truncateText(item.remark, 50) }}
                      </div>
                    </div>
                  </div>
                  <div class="empty-timeline" v-if="!currentPet.vaccineRecords || currentPet.vaccineRecords.length === 0">
                    <i class="el-icon-document"></i>
                    <span>暂无疫苗记录</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="体检记录" name="health">
              <div class="health-section">
                <div class="section-header">
                  <span>健康档案</span>
                </div>
                <div class="health-timeline">
                  <div class="timeline-item" v-for="item in (currentPet.healthRecords || [])" :key="item.id" @click="showHealthDetail(item)">
                    <div class="timeline-dot health"></div>
                    <div class="timeline-content">
                      <div class="timeline-title">{{ formatDate(item.recordDate) }} 体检</div>
                      <div class="timeline-info">
                        <span v-if="item.weight"><i class="el-icon-weight"></i> {{ item.weight }} kg</span>
                        <span v-if="item.healthStatus"><i class="el-icon-medal"></i> {{ item.healthStatus }}</span>
                        <span v-if="item.operatorName"><i class="el-icon-user"></i> {{ item.operatorName }}</span>
                      </div>
                      <div class="timeline-desc" v-if="item.diagnosis">
                        <strong>诊断：</strong>{{ truncateText(item.diagnosis, 50) }}
                      </div>
                      <div class="timeline-expand" v-if="item.remark">
                        <i class="el-icon-edit"></i> {{ truncateText(item.remark, 50) }}
                      </div>
                    </div>
                  </div>
                  <div class="empty-timeline" v-if="!currentPet.healthRecords || currentPet.healthRecords.length === 0">
                    <i class="el-icon-document"></i>
                    <span>暂无体检记录</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-dialog>

    <!-- 疫苗记录详情对话框 -->
    <el-dialog title="疫苗记录详情" :visible.sync="vaccineDetailVisible" width="500px" center class="detail-dialog">
      <el-descriptions :column="1" border v-if="currentVaccine">
        <el-descriptions-item label="疫苗名称">{{ currentVaccine.vaccineName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="接种日期">{{ formatDate(currentVaccine.vaccineDate) || '--' }}</el-descriptions-item>
        <el-descriptions-item label="下次接种">{{ formatDate(currentVaccine.nextDate) || '--' }}</el-descriptions-item>
        <el-descriptions-item label="操作员">{{ currentVaccine.operatorName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentVaccine.remark || '--' }}</el-descriptions-item>
      </el-descriptions>
      <span slot="footer">
        <el-button type="primary" @click="vaccineDetailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 体检记录详情对话框 -->
    <el-dialog title="体检记录详情" :visible.sync="healthDetailVisible" width="500px" center class="detail-dialog">
      <el-descriptions :column="1" border v-if="currentHealth">
        <el-descriptions-item label="体检日期">{{ formatDate(currentHealth.recordDate) || '--' }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ currentHealth.height || '--' }} cm</el-descriptions-item>
        <el-descriptions-item label="体重">{{ currentHealth.weight || '--' }} kg</el-descriptions-item>
        <el-descriptions-item label="体温">{{ currentHealth.temperature || '--' }} ℃</el-descriptions-item>
        <el-descriptions-item label="健康状况">{{ currentHealth.healthStatus || '--' }}</el-descriptions-item>
        <el-descriptions-item label="诊断结果">{{ currentHealth.diagnosis || '--' }}</el-descriptions-item>
        <el-descriptions-item label="操作员">{{ currentHealth.operatorName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentHealth.remark || '--' }}</el-descriptions-item>
      </el-descriptions>
      <span slot="footer">
        <el-button type="primary" @click="healthDetailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <Footer />
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue';
import Footer from '@/components/Footer.vue';
import {
  getPetList,
  getPetDetail,
  addPet,
  updatePet,
  deletePet,
  setDefaultPet,
  getUpcomingVaccines
} from '@/api/pet';
import { uploadPetAvatar } from '@/api/upload';

export default {
  name: 'MyPets',
  components: { Navbar, Footer },
  data() {
    return {
      loading: false,
      submitLoading: false,
      petList: [],
      upcomingVaccines: [],
      showAddDialog: false,
      showDetailDialog: false,
      isEdit: false,
      editId: null,
      avatarPreview: '',
      currentPet: null,
      activeTab: 'basic',
      vaccineDetailVisible: false,
      healthDetailVisible: false,
      currentVaccine: null,
      currentHealth: null,
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
        isDefault: 0
      },
      petRules: {
        name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择宠物类型', trigger: 'change' }]
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑宠物' : '添加宠物';
    }
  },
  created() {
    this.loadPets();
    this.loadUpcomingVaccines();
  },
  methods: {
    async loadPets() {
      this.loading = true;
      try {
        const res = await getPetList();
        if (res.code === 200) {
          this.petList = res.data || [];
        }
      } catch (error) {
        console.error('加载宠物列表失败', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },

    async loadUpcomingVaccines() {
      try {
        const res = await getUpcomingVaccines();
        if (res.code === 200) {
          this.upcomingVaccines = res.data || [];
        }
      } catch (error) {
        console.error('加载疫苗提醒失败', error);
      }
    },

    getTypeName(type) {
      const map = { dog: '狗狗', cat: '猫咪', rabbit: '兔子', other: '其他' };
      return map[type] || '宠物';
    },

    getGenderName(gender) {
      const map = { 0: '未知', 1: '公', 2: '母' };
      return map[gender] || '未知';
    },

    getAge(birthday) {
      if (!birthday) return '年龄未知';
      const birth = new Date(birthday);
      const now = new Date();
      let age = now.getFullYear() - birth.getFullYear();
      if (now.getMonth() < birth.getMonth() || (now.getMonth() === birth.getMonth() && now.getDate() < birth.getDate())) {
        age--;
      }
      if (age <= 0) return '不到1岁';
      return age + '岁';
    },

    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

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
        }
      } catch (error) {
        this.$message.error('上传失败');
      }
      event.target.value = '';
    },

    editPet(pet) {
      this.isEdit = true;
      this.editId = pet.id;

      let birthdayValue = '';
      if (pet.birthday) {
        if (pet.birthday instanceof Date) {
          birthdayValue = pet.birthday;
        } else if (typeof pet.birthday === 'string') {
          birthdayValue = pet.birthday.split(' ')[0];
        }
      }

      this.petForm = {
        name: pet.name || '',
        type: pet.type || 'dog',
        breed: pet.breed || '',
        gender: pet.gender !== undefined ? pet.gender : 0,
        birthday: birthdayValue,
        weight: pet.weight !== undefined ? pet.weight : null,
        color: pet.color || '',
        avatar: pet.avatar || '',
        description: pet.description || '',
        isDefault: pet.isDefault !== undefined ? pet.isDefault : 0
      };

      this.avatarPreview = pet.avatar || '';
      this.showAddDialog = true;

      this.$nextTick(() => {
        if (this.$refs.petForm) {
          this.$refs.petForm.clearValidate();
        }
      });
    },

    async deletePet(pet) {
      this.$confirm(`确定删除宠物 "${pet.name}" 吗？`, '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deletePet(pet.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadPets();
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    async setDefault(pet) {
      try {
        const res = await setDefaultPet(pet.id);
        if (res.code === 200) {
          this.$message.success('设置成功');
          this.loadPets();
        }
      } catch (error) {
        this.$message.error('设置失败');
      }
    },

    async viewPetDetail(id) {
      try {
        const res = await getPetDetail(id);
        if (res.code === 200) {
          this.currentPet = res.data;
          this.currentPet.vaccineRecords = this.currentPet.vaccineRecords || [];
          this.currentPet.healthRecords = this.currentPet.healthRecords || [];
          this.showDetailDialog = true;
        }
      } catch (error) {
        this.$message.error('获取宠物详情失败');
      }
    },

    handleDetailDialogClose() {
      this.showDetailDialog = false;
    },

    // 添加宠物时，如果设为默认，需要先将其他宠物的默认状态清除
    async submitForm() {
      this.$refs.petForm.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          let res;

          // 如果新添加的宠物要设为默认，且已有其他默认宠物
          if (this.petForm.isDefault === 1 && !this.isEdit) {
            // 检查是否已有默认宠物
            const hasDefault = this.petList.some(pet => pet.isDefault === 1);
            if (hasDefault) {
              // 先将所有宠物的默认状态清除
              for (const pet of this.petList) {
                if (pet.isDefault === 1) {
                  await setDefaultPet(pet.id);
                }
              }
            }
          }

          if (this.isEdit) {
            res = await updatePet({ ...this.petForm, id: this.editId });
          } else {
            res = await addPet(this.petForm);
          }

          if (res.code === 200) {
            this.$message.success(this.isEdit ? '更新成功' : '添加成功');
            this.showAddDialog = false;
            this.resetForm();
            this.loadPets();
          } else {
            this.$message.error(res.message);
          }
        } catch (error) {
          console.error('提交失败:', error);
          this.$message.error(this.isEdit ? '更新失败' : '添加失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },

    showVaccineDetail(item) {
      this.currentVaccine = item;
      this.vaccineDetailVisible = true;
    },

    showHealthDetail(item) {
      this.currentHealth = item;
      this.healthDetailVisible = true;
    },

    truncateText(text, length) {
      if (!text) return '';
      if (text.length <= length) return text;
      return text.substring(0, length) + '...';
    },

    resetForm() {
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
        isDefault: 0
      };
      this.avatarPreview = '';
      this.$nextTick(() => {
        if (this.$refs.petForm) this.$refs.petForm.clearValidate();
      });
    }
  }
};
</script>

<style scoped>
.my-pets-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.my-pets-wrapper {
  flex: 1;
  padding: 30px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.add-btn {
  background: #409EFF;
  border: none;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
}

.add-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

/* 宠物卡片 */
.pets-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.pet-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  cursor: pointer;
  overflow: hidden;
  border: 1px solid #eef2f6;
}

.pet-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #e0e0e0;
}

.pet-card-inner {
  display: flex;
  align-items: center;
  padding: 20px;
  gap: 16px;
}

.pet-avatar {
  position: relative;
  flex-shrink: 0;
}

.pet-avatar-img {
  background: #409EFF;
  color: white;
  font-size: 32px;
}

.default-badge {
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  background: #67c23a;
  color: white;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 12px;
  white-space: nowrap;
}

.pet-info {
  flex: 1;
}

.pet-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 6px 0;
}

.pet-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.pet-type {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  background: #ecf5ff;
  color: #409EFF;
}

.pet-breed {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  background: #f5f7fa;
  color: #606266;
}

.pet-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.pet-stats i {
  margin-right: 4px;
  color: #409EFF;
}

.pet-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pet-actions .el-button {
  margin: 0;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  color: #909399;
  border: 1px solid #eef2f6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  grid-column: 1 / -1;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.empty-state p {
  margin-bottom: 16px;
  font-size: 14px;
}

.empty-state .el-button {
  margin-top: 0;
}

/* 疫苗提醒卡片 */
.reminder-card {
  margin-top: 30px;
  background: #fef0e6;
  border-radius: 12px;
  padding: 16px 20px;
  border-left: 3px solid #e6a23c;
}

.reminder-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #e6a23c;
  font-size: 14px;
  font-weight: 500;
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reminder-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-radius: 8px;
  padding: 10px 16px;
}

.reminder-pet-name {
  font-weight: 500;
  color: #2c3e50;
  font-size: 13px;
}

.reminder-vaccine {
  font-size: 12px;
  color: #909399;
}

.reminder-date {
  font-size: 12px;
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 对话框样式 */
.pet-dialog ::v-deep .el-dialog {
  border-radius: 16px;
}

.pet-dialog ::v-deep .el-dialog__header {
  background: #409EFF;
  padding: 20px 24px;
  margin: 0;
  border-radius: 16px 16px 0 0;
}

.pet-dialog ::v-deep .el-dialog__title {
  color: white;
  font-size: 16px;
  font-weight: 500;
}

.pet-dialog ::v-deep .el-dialog__close {
  color: white;
}

.pet-dialog ::v-deep .el-dialog__body {
  padding: 20px;
}

.pet-detail-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.pet-detail-dialog ::v-deep .el-dialog__header {
  background: #409EFF;
  padding: 20px 24px;
  margin: 0;
  border-radius: 0;
}

.pet-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-size: 16px;
  font-weight: 500;
}

/* ✅ 关闭按钮样式 - 白色明显可见 */
.pet-detail-dialog ::v-deep .el-dialog__close {
  color: white;
  font-size: 18px;
  font-weight: bold;
}

.avatar-upload {
  text-align: center;
  cursor: pointer;
}

.upload-avatar {
  background: #f5f7fa;
  color: #909399;
  transition: all 0.3s;
  margin-bottom: 8px;
}

.upload-avatar:hover {
  opacity: 0.8;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

/* 详情对话框样式 */
.pet-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eef2f6;
  margin-bottom: 20px;
}

.detail-avatar {
  background: #409EFF;
  color: white;
  font-size: 32px;
}

.detail-info h2 {
  font-size: 20px;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 12px;
  background: #f5f7fa;
  color: #606266;
}

.detail-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.detail-stats i {
  margin-right: 4px;
  color: #409EFF;
}

/* 时间线样式 */
.section-header {
  margin-bottom: 16px;
}

.section-header span {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.timeline-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.timeline-dot {
  width: 8px;
  height: 8px;
  background: #409EFF;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.timeline-dot.health {
  background: #67c23a;
}

.timeline-content {
  flex: 1;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
}

.timeline-title {
  font-weight: 500;
  color: #2c3e50;
  font-size: 13px;
  margin-bottom: 6px;
}

.timeline-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 11px;
  color: #909399;
}

.timeline-info i {
  margin-right: 2px;
}

.timeline-desc {
  font-size: 12px;
  color: #606266;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid #e8eaef;
}

.timeline-expand {
  margin-top: 6px;
  font-size: 11px;
  color: #909399;
  border-left: 2px solid #409EFF;
  padding-left: 8px;
}

.empty-timeline {
  text-align: center;
  padding: 30px;
  color: #909399;
}

.empty-timeline i {
  font-size: 40px;
  margin-bottom: 10px;
  display: block;
}

.info-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #eef2f6;
}

.info-label {
  width: 60px;
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
  font-size: 13px;
}

.info-value {
  flex: 1;
  color: #2c3e50;
  font-size: 13px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .my-pets-wrapper {
    padding: 20px 0 40px;
  }

  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .page-title {
    font-size: 20px;
  }

  .pets-list {
    grid-template-columns: 1fr;
  }

  .pet-card-inner {
    flex-direction: column;
    text-align: center;
  }

  .pet-actions {
    flex-direction: row;
    justify-content: center;
  }

  .detail-header {
    flex-direction: column;
    text-align: center;
  }

  .detail-stats {
    justify-content: center;
  }

  .detail-tags {
    justify-content: center;
  }

  .reminder-item {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
}

.timeline-item {
  cursor: pointer;
  transition: all 0.3s;
}

.timeline-item:hover .timeline-content {
  background: #ecf5ff;
}
</style>