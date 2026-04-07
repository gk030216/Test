<template>
  <div class="my-pets-container">
    <!-- 导航栏 -->
    <Navbar />

    <div class="my-pets-wrapper">
      <div class="container">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="header-content">
            <div class="header-left">
              <h2 class="page-title">我的宠物</h2>
              <p class="page-desc">记录宝贝的成长点滴，管理健康档案</p>
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
                <el-avatar :size="90" :src="pet.avatar" class="pet-avatar-img">
                  <i class="el-icon-camera"></i>
                </el-avatar>
                <span class="default-badge" v-if="pet.isDefault === 1">
                  <i class="el-icon-check"></i> 默认
                </span>
              </div>
              <div class="pet-info">
                <h3 class="pet-name">{{ pet.name }}</h3>
                <div class="pet-meta">
                  <span class="pet-type" :class="'type-' + pet.type">
                    <i class="el-icon-cpu"></i> {{ getTypeName(pet.type) }}
                  </span>
                  <span class="pet-breed" v-if="pet.breed">
                    <i class="el-icon-medal"></i> {{ pet.breed }}
                  </span>
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
                <el-tooltip content="设为默认" placement="top" v-if="pet.isDefault !== 1">
                  <el-button type="success" circle size="small" @click="setDefault(pet)">
                    <i class="el-icon-star-on"></i>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div class="empty-state" v-if="!loading && petList.length === 0">
            <div class="empty-icon">
              <i class="el-icon-s-custom"></i>
            </div>
            <h3>还没有添加宠物</h3>
            <p>添加你的第一只宠物，记录它的成长点滴</p>
            <el-button type="primary" size="medium" @click="showAddDialog = true">
              <i class="el-icon-plus"></i> 添加宠物
            </el-button>
          </div>
        </div>

        <!-- 疫苗提醒卡片 -->
        <div class="reminder-card" v-if="upcomingVaccines && upcomingVaccines.length > 0">
          <div class="reminder-header">
            <div class="reminder-icon">
              <i class="el-icon-warning-outline"></i>
            </div>
            <div class="reminder-info">
              <h4>疫苗提醒</h4>
              <p>以下宠物即将需要接种疫苗</p>
            </div>
          </div>
          <div class="reminder-list">
            <div class="reminder-item" v-for="item in upcomingVaccines" :key="item.id">
              <div class="reminder-pet-icon">
                <i class="el-icon-cpu"></i>
              </div>
              <div class="reminder-content">
                <div class="reminder-pet-name">{{ item.petName || '未知宠物' }}</div>
                <div class="reminder-vaccine">{{ item.vaccineName || '未知疫苗' }}</div>
              </div>
              <div class="reminder-date">
                <i class="el-icon-date"></i>
                {{ formatDate(item.nextDate) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑宠物对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="showAddDialog" width="580px" center class="pet-dialog" :close-on-click-modal="false">
      <el-form :model="petForm" :rules="petRules" ref="petForm" label-width="90px" class="pet-form">
        <el-form-item label="宠物头像">
          <div class="avatar-upload" @click="triggerUpload">
            <el-avatar :size="80" :src="avatarPreview" class="upload-avatar">
              <i class="el-icon-plus"></i>
            </el-avatar>
            <div class="upload-tip">点击更换头像</div>
            <input type="file" ref="avatarInput" accept="image/*" style="display: none" @change="handleAvatarUpload">
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="宠物名称" prop="name">
              <el-input v-model="petForm.name" placeholder="给宝贝起个可爱的名字" prefix-icon="el-icon-edit"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="宠物类型" prop="type">
              <el-select v-model="petForm.type" placeholder="请选择" style="width: 100%">
                <el-option label="🐕 狗狗" value="dog"></el-option>
                <el-option label="🐈 猫咪" value="cat"></el-option>
                <el-option label="🐇 兔子" value="rabbit"></el-option>
                <el-option label="🐹 其他" value="other"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品种">
              <el-input v-model="petForm.breed" placeholder="如：金毛、布偶猫" prefix-icon="el-icon-medal"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="petForm.gender" placeholder="请选择性别" style="width: 100%" class="gender-select">
                <el-option label="公" :value="1">
                  <div class="gender-option">
                    <i class="el-icon-male" style="color: #409EFF"></i>
                    <span>公</span>
                  </div>
                </el-option>
                <el-option label="母" :value="2">
                  <div class="gender-option">
                    <i class="el-icon-female" style="color: #f56c6c"></i>
                    <span>母</span>
                  </div>
                </el-option>
                <el-option label="未知" :value="0">
                  <div class="gender-option">
                    <i class="el-icon-question"></i>
                    <span>未知</span>
                  </div>
                </el-option>
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
                  format="yyyy年MM月dd日"
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
          <el-input v-model="petForm.color" placeholder="如：金色、黑白、橘色" prefix-icon="el-icon-color"></el-input>
        </el-form-item>

        <el-form-item label="简介">
          <el-input v-model="petForm.description" type="textarea" :rows="3" placeholder="介绍一下你的宝贝，让大家更好地认识它～"></el-input>
        </el-form-item>

        <!-- 设为默认开关 -->
        <el-form-item label="设为默认">
          <el-switch
              v-model="petForm.isDefault"
              :active-value="1"
              :inactive-value="0"
              active-text="是"
              inactive-text="否">
          </el-switch>
          <span class="form-tip" style="margin-left: 12px; color: #909399; font-size: 12px;">设为默认后，下单时会自动选中该宠物</span>
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="submit-btn">确定</el-button>
      </span>
    </el-dialog>

    <!-- 宠物详情对话框 -->
    <el-dialog :title="currentPet && currentPet.name" :visible.sync="showDetailDialog" width="800px" center class="pet-detail-dialog">
      <div class="pet-detail" v-if="currentPet">
        <div class="detail-header">
          <div class="detail-avatar-wrapper">
            <el-avatar :size="100" :src="currentPet.avatar" class="detail-avatar">
              <i class="el-icon-camera"></i>
            </el-avatar>
          </div>
          <div class="detail-info">
            <h2>{{ currentPet.name }}</h2>
            <div class="detail-tags">
              <span class="tag tag-type">{{ getTypeName(currentPet.type) }}</span>
              <span class="tag tag-breed" v-if="currentPet.breed">{{ currentPet.breed }}</span>
              <span class="tag tag-gender" v-if="currentPet.gender === 1">♂ 公</span>
              <span class="tag tag-gender" v-else-if="currentPet.gender === 2">♀ 母</span>
            </div>
            <div class="detail-stats">
              <div class="stat">
                <i class="el-icon-cake"></i>
                <span>{{ getAge(currentPet.birthday) }}</span>
              </div>
              <div class="stat">
                <i class="el-icon-weight"></i>
                <span>{{ currentPet.weight || '--' }} kg</span>
              </div>
              <div class="stat">
                <i class="el-icon-color"></i>
                <span>{{ currentPet.color || '--' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-tabs">
          <el-tabs v-model="activeTab" type="border-card">
            <el-tab-pane label="📋 基本信息" name="basic">
              <div class="basic-info">
                <div class="info-item">
                  <div class="info-label">📝 简介</div>
                  <div class="info-value">{{ currentPet.description || '暂无简介' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">🎂 生日</div>
                  <div class="info-value">{{ formatDate(currentPet.birthday) || '未知' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">🎨 毛色</div>
                  <div class="info-value">{{ currentPet.color || '未知' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">⚥ 性别</div>
                  <div class="info-value">
                    <span v-if="currentPet.gender === 1"><i class="el-icon-male"></i> 公</span>
                    <span v-else-if="currentPet.gender === 2"><i class="el-icon-female"></i> 母</span>
                    <span v-else>未知</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 疫苗记录 Tab - 根据权限显示不同按钮 -->
            <el-tab-pane label="💉 疫苗记录" name="vaccine">
              <div class="vaccine-section">
                <div class="section-header">
                  <span>免疫记录</span>
                  <el-tag v-if="!canEdit" type="info" size="small">仅可查看</el-tag>
                  <el-button v-if="canEdit" type="primary" size="small" @click="showVaccineDialog = true">
                    <i class="el-icon-plus"></i> 添加记录
                  </el-button>
                </div>
                <div class="vaccine-timeline">
                  <div class="timeline-item" v-for="item in (currentPet.vaccineRecords || [])" :key="item.id">
                    <div class="timeline-dot"></div>
                    <div class="timeline-content">
                      <div class="timeline-title">{{ item.vaccineName }}</div>
                      <div class="timeline-info">
                        <span><i class="el-icon-date"></i> 接种：{{ formatDate(item.vaccineDate) }}</span>
                        <span v-if="item.nextDate"><i class="el-icon-bell"></i> 下次：{{ formatDate(item.nextDate) }}</span>
                        <span v-if="item.hospital"><i class="el-icon-location"></i> {{ item.hospital }}</span>
                        <span v-if="item.doctor"><i class="el-icon-user"></i> 医生：{{ item.doctor }}</span>
                      </div>
                      <!-- 只有员工和管理员才能删除 -->
                      <div class="timeline-actions" v-if="canEdit">
                        <el-button type="text" class="delete-btn" @click="deleteVaccine(item.id)">删除</el-button>
                      </div>
                    </div>
                  </div>
                  <div class="empty-timeline" v-if="!currentPet.vaccineRecords || currentPet.vaccineRecords.length === 0">
                    <i class="el-icon-document"></i>
                    <span>{{ canEdit ? '暂无疫苗记录，点击上方按钮添加' : '暂无疫苗记录' }}</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 体检记录 Tab - 根据权限显示不同按钮 -->
            <el-tab-pane label="🏥 体检记录" name="health">
              <div class="health-section">
                <div class="section-header">
                  <span>健康档案</span>
                  <el-tag v-if="!canEdit" type="info" size="small">仅可查看</el-tag>
                  <el-button v-if="canEdit" type="primary" size="small" @click="showHealthDialog = true">
                    <i class="el-icon-plus"></i> 添加记录
                  </el-button>
                </div>
                <div class="health-timeline">
                  <div class="timeline-item" v-for="item in (currentPet.healthRecords || [])" :key="item.id">
                    <div class="timeline-dot health"></div>
                    <div class="timeline-content">
                      <div class="timeline-title">{{ formatDate(item.recordDate) }} 体检报告</div>
                      <div class="timeline-info">
                        <span><i class="el-icon-weight"></i> 体重：{{ item.weight }} kg</span>
                        <span v-if="item.healthStatus"><i class="el-icon-medal"></i> 状况：{{ item.healthStatus }}</span>
                        <span v-if="item.doctor"><i class="el-icon-user"></i> 医生：{{ item.doctor }}</span>
                      </div>
                      <div class="timeline-desc" v-if="item.diagnosis">
                        <strong>诊断结果：</strong>{{ item.diagnosis }}
                      </div>
                      <!-- 只有员工和管理员才能删除 -->
                      <div class="timeline-actions" v-if="canEdit">
                        <el-button type="text" class="delete-btn" @click="deleteHealth(item.id)">删除</el-button>
                      </div>
                    </div>
                  </div>
                  <div class="empty-timeline" v-if="!currentPet.healthRecords || currentPet.healthRecords.length === 0">
                    <i class="el-icon-document"></i>
                    <span>{{ canEdit ? '暂无体检记录，点击上方按钮添加' : '暂无体检记录' }}</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-dialog>

    <!-- 添加疫苗记录对话框 - 只有员工和管理员可见 -->
    <el-dialog v-if="canEdit" title="添加疫苗记录" :visible.sync="showVaccineDialog" width="500px" center class="record-dialog">
      <el-form :model="vaccineForm" label-width="90px">
        <el-form-item label="疫苗名称" required>
          <el-input v-model="vaccineForm.vaccineName" placeholder="如：狂犬疫苗"></el-input>
        </el-form-item>
        <el-form-item label="接种日期" required>
          <el-date-picker v-model="vaccineForm.vaccineDate" type="date" placeholder="选择日期" format="yyyy年MM月dd日" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="下次接种">
          <el-date-picker v-model="vaccineForm.nextDate" type="date" placeholder="选择日期" format="yyyy年MM月dd日" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="接种医院">
          <el-input v-model="vaccineForm.hospital" placeholder="请输入医院名称"></el-input>
        </el-form-item>
        <el-form-item label="医生">
          <el-input v-model="vaccineForm.doctor" placeholder="请输入医生姓名"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="vaccineForm.remark" type="textarea" rows="2" placeholder="备注信息"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showVaccineDialog = false">取消</el-button>
        <el-button type="primary" @click="submitVaccine" :loading="vaccineLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 添加体检记录对话框 - 只有员工和管理员可见 -->
    <el-dialog v-if="canEdit" title="添加体检记录" :visible.sync="showHealthDialog" width="500px" center class="record-dialog">
      <el-form :model="healthForm" label-width="90px">
        <el-form-item label="体检日期" required>
          <el-date-picker v-model="healthForm.recordDate" type="date" placeholder="选择日期" format="yyyy年MM月dd日" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="身高(cm)">
          <el-input-number v-model="healthForm.height" :min="0" :precision="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="healthForm.weight" :min="0" :precision="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="体温(℃)">
          <el-input-number v-model="healthForm.temperature" :min="35" :max="42" :precision="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="健康状况">
          <el-input v-model="healthForm.healthStatus" placeholder="如：良好"></el-input>
        </el-form-item>
        <el-form-item label="诊断结果">
          <el-input v-model="healthForm.diagnosis" type="textarea" rows="2" placeholder="诊断结果"></el-input>
        </el-form-item>
        <el-form-item label="医生">
          <el-input v-model="healthForm.doctor" placeholder="请输入医生姓名"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showHealthDialog = false">取消</el-button>
        <el-button type="primary" @click="submitHealth" :loading="healthLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 页脚 -->
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
  getUpcomingVaccines,
  addVaccineRecord,
  deleteVaccineRecord,
  addHealthRecord,
  deleteHealthRecord
} from '@/api/pet';
import { uploadPetAvatar } from '@/api/upload';

export default {
  name: 'MyPets',
  components: {
    Navbar,
    Footer
  },
  data() {
    return {
      loading: false,
      submitLoading: false,
      vaccineLoading: false,
      healthLoading: false,
      petList: [],
      upcomingVaccines: [],
      showAddDialog: false,
      showDetailDialog: false,
      showVaccineDialog: false,
      showHealthDialog: false,
      isEdit: false,
      editId: null,
      avatarPreview: '',
      currentPet: null,
      activeTab: 'basic',
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
        isDefault: 0  // 默认值设为 0（不是默认宠物）
      },
      petRules: {
        name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择宠物类型', trigger: 'change' }]
      },
      vaccineForm: {
        petId: null,
        vaccineName: '',
        vaccineDate: '',
        nextDate: '',
        hospital: '',
        doctor: '',
        remark: ''
      },
      healthForm: {
        petId: null,
        recordDate: '',
        height: null,
        weight: null,
        temperature: null,
        healthStatus: '',
        diagnosis: '',
        doctor: ''
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑宠物信息' : '添加新宠物';
    },
    // 判断当前用户是否可以编辑疫苗/体检记录
    // role: 1-普通用户, 2-员工, 3-管理员
    canEdit() {
      const userInfo = localStorage.getItem('userInfo');
      if (!userInfo) return false;
      try {
        const user = JSON.parse(userInfo);
        return user.role === 2 || user.role === 3;
      } catch (e) {
        return false;
      }
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
      const map = {
        dog: '🐕 狗狗',
        cat: '🐈 猫咪',
        rabbit: '🐇 兔子',
        other: '🐹 其他'
      };
      return map[type] || '🐾 宠物';
    },
    getGenderName(gender) {
      const map = { 0: '未知', 1: '公 ♂', 2: '母 ♀' };
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
      return `${d.getFullYear()}年${String(d.getMonth() + 1).padStart(2, '0')}月${String(d.getDate()).padStart(2, '0')}日`;
    },
    // 日期格式转换（处理后端返回的日期格式）
    formatDateForPicker(dateStr) {
      if (!dateStr) return '';
      // 如果已经是 yyyy-MM-dd 格式，直接返回
      if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
        return dateStr;
      }
      // 如果是 yyyy-MM-dd HH:mm:ss 格式，截取前10位
      if (dateStr.includes(' ')) {
        return dateStr.substring(0, 10);
      }
      return dateStr;
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
      console.log('=== 编辑宠物 ===');
      console.log('原始数据:', pet);

      this.isEdit = true;
      this.editId = pet.id;

      // 安全地处理生日字段
      let birthdayValue = '';
      if (pet.birthday) {
        if (pet.birthday instanceof Date) {
          birthdayValue = pet.birthday;
        } else if (typeof pet.birthday === 'string') {
          birthdayValue = pet.birthday.split(' ')[0];
        }
      }

      // 设置表单数据
      this.petForm = {
        name: pet.name || '',
        type: pet.type || 'dog',
        breed: pet.breed || '',
        gender: (pet.gender !== undefined && pet.gender !== null) ? pet.gender : 0,
        birthday: birthdayValue,
        weight: (pet.weight !== undefined && pet.weight !== null) ? pet.weight : null,
        color: pet.color || '',
        avatar: pet.avatar || '',
        description: pet.description || '',
        isDefault: pet.isDefault !== undefined ? pet.isDefault : 0
      };

      console.log('转换后表单数据:', this.petForm);

      this.avatarPreview = pet.avatar || '';
      this.showAddDialog = true;

      // 对话框打开后清除验证
      this.$nextTick(() => {
        if (this.$refs.petForm) {
          this.$refs.petForm.clearValidate();
        }
      });
    },
    async deletePet(pet) {
      this.$confirm(`确定要删除宠物 "${pet.name}" 吗？删除后无法恢复！`, '提示', {type: 'warning'}).then(async () => {
        try {
          const res = await deletePet(pet.id);
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadPets();
          }
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {
      });
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
          // 确保疫苗记录和体检记录是数组
          this.currentPet.vaccineRecords = this.currentPet.vaccineRecords || [];
          this.currentPet.healthRecords = this.currentPet.healthRecords || [];
          this.vaccineForm.petId = id;
          this.healthForm.petId = id;
          this.showDetailDialog = true;
        }
      } catch (error) {
        this.$message.error('获取宠物详情失败');
      }
    },
    submitForm() {
      this.$refs.petForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          let res;
          if (this.isEdit) {
            res = await updatePet({...this.petForm, id: this.editId});
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
          this.$message.error(this.isEdit ? '更新失败' : '添加失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },
    async submitVaccine() {
      if (!this.vaccineForm.vaccineName || !this.vaccineForm.vaccineDate) {
        this.$message.warning('请填写疫苗名称和接种日期');
        return;
      }
      this.vaccineLoading = true;
      try {
        const res = await addVaccineRecord(this.vaccineForm);
        if (res.code === 200) {
          this.$message.success('添加成功');
          this.showVaccineDialog = false;
          this.vaccineForm = {
            petId: this.currentPet.id,
            vaccineName: '',
            vaccineDate: '',
            nextDate: '',
            hospital: '',
            doctor: '',
            remark: ''
          };
          this.viewPetDetail(this.currentPet.id);
        }
      } catch (error) {
        this.$message.error('添加失败');
      } finally {
        this.vaccineLoading = false;
      }
    },
    async deleteVaccine(id) {
      this.$confirm('确定删除该疫苗记录吗？', '提示', {type: 'warning'}).then(async () => {
        try {
          await deleteVaccineRecord(id);
          this.$message.success('删除成功');
          this.viewPetDetail(this.currentPet.id);
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {
      });
    },
    async submitHealth() {
      if (!this.healthForm.recordDate) {
        this.$message.warning('请填写体检日期');
        return;
      }
      this.healthLoading = true;
      try {
        const res = await addHealthRecord(this.healthForm);
        if (res.code === 200) {
          this.$message.success('添加成功');
          this.showHealthDialog = false;
          this.healthForm = {
            petId: this.currentPet.id,
            recordDate: '',
            height: null,
            weight: null,
            temperature: null,
            healthStatus: '',
            diagnosis: '',
            doctor: ''
          };
          this.viewPetDetail(this.currentPet.id);
        }
      } catch (error) {
        this.$message.error('添加失败');
      } finally {
        this.healthLoading = false;
      }
    },
    async deleteHealth(id) {
      this.$confirm('确定删除该体检记录吗？', '提示', {type: 'warning'}).then(async () => {
        try {
          await deleteHealthRecord(id);
          this.$message.success('删除成功');
          this.viewPetDetail(this.currentPet.id);
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {
      });
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
        isDefault: 0  // 重置时默认值为 0
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
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f6 100%);
}

.my-pets-wrapper {
  flex: 1;
  padding: 40px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 40px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-desc {
  font-size: 15px;
  color: #7f8c8d;
  margin: 0;
}

.add-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 12px;
  transition: all 0.3s;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

/* 宠物卡片 */
.pets-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 24px;
}

.pet-card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
  overflow: hidden;
}

.pet-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
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
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-size: 36px;
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
  border-radius: 20px;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 3px;
}

.default-badge i {
  font-size: 10px;
}

.pet-info {
  flex: 1;
}

.pet-name {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.pet-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.pet-type {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 20px;
  background: #f0f2f5;
  color: #667eea;
}

.pet-type i {
  margin-right: 4px;
}

.pet-breed {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 20px;
  background: #fef0e6;
  color: #e6a23c;
}

.pet-breed i {
  margin-right: 4px;
}

.pet-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #7f8c8d;
}

.pet-stats i {
  margin-right: 4px;
  color: #667eea;
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
  padding: 80px 20px;
  background: white;
  border-radius: 24px;
}

.empty-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #f0f2f5, #e8eaef);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon i {
  font-size: 48px;
  color: #a0a5b0;
}

.empty-state h3 {
  font-size: 20px;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.empty-state p {
  color: #7f8c8d;
  margin-bottom: 24px;
}

/* 疫苗提醒卡片 */
.reminder-card {
  margin-top: 40px;
  background: linear-gradient(135deg, #fff8f0, #fff0e6);
  border-radius: 20px;
  padding: 20px;
  border-left: 4px solid #e6a23c;
}

.reminder-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.reminder-icon {
  width: 48px;
  height: 48px;
  background: #e6a23c;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reminder-icon i {
  font-size: 24px;
  color: white;
}

.reminder-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #e6a23c;
  margin: 0 0 4px 0;
}

.reminder-info p {
  font-size: 13px;
  color: #b8860b;
  margin: 0;
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reminder-item {
  display: flex;
  align-items: center;
  gap: 15px;
  background: white;
  border-radius: 16px;
  padding: 12px 20px;
  transition: all 0.3s;
}

.reminder-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.reminder-pet-icon {
  width: 40px;
  height: 40px;
  background: #fef0e6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reminder-pet-icon i {
  font-size: 20px;
  color: #e6a23c;
}

.reminder-content {
  flex: 1;
}

.reminder-pet-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.reminder-vaccine {
  font-size: 12px;
  color: #7f8c8d;
}

.reminder-date {
  font-size: 13px;
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 对话框样式 */
.pet-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
  margin: 0;
}

.pet-dialog ::v-deep .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.pet-dialog ::v-deep .el-dialog__close {
  color: white;
}

.pet-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

.pet-detail-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px 24px;
}

.pet-detail-dialog ::v-deep .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.avatar-upload {
  text-align: center;
  cursor: pointer;
}

.upload-avatar {
  background: #f0f2f5;
  color: #a0a5b0;
  transition: all 0.3s;
  margin-bottom: 8px;
}

.upload-avatar:hover {
  opacity: 0.8;
  transform: scale(1.02);
}

.upload-tip {
  font-size: 12px;
  color: #7f8c8d;
}

/* 详情对话框样式 */
.pet-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  gap: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
}

.detail-avatar-wrapper {
  flex-shrink: 0;
}

.detail-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-size: 40px;
}

.detail-info {
  flex: 1;
}

.detail-info h2 {
  font-size: 24px;
  color: #2c3e50;
  margin: 0 0 12px 0;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tag {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 20px;
}

.tag-type {
  background: #f0f2f5;
  color: #667eea;
}

.tag-breed {
  background: #fef0e6;
  color: #e6a23c;
}

.tag-gender {
  background: #e8f7ef;
  color: #67c23a;
}

.detail-stats {
  display: flex;
  gap: 24px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #7f8c8d;
}

.stat i {
  color: #667eea;
}

/* 时间线样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header span {
  font-weight: 600;
  color: #2c3e50;
}

.timeline-item {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  position: relative;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  background: #667eea;
  border-radius: 50%;
  margin-top: 4px;
  flex-shrink: 0;
}

.timeline-dot.health {
  background: #67c23a;
}

.timeline-content {
  flex: 1;
  background: #f8f9fc;
  border-radius: 16px;
  padding: 16px;
}

.timeline-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.timeline-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.timeline-info i {
  margin-right: 4px;
}

.timeline-desc {
  font-size: 13px;
  color: #5a6874;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e0e0e0;
}

.timeline-actions {
  text-align: right;
  margin-top: 8px;
}

.delete-btn {
  color: #f56c6c;
}

.empty-timeline {
  text-align: center;
  padding: 40px;
  color: #a0a5b0;
}

.empty-timeline i {
  font-size: 48px;
  margin-bottom: 12px;
  display: block;
}

.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  width: 80px;
  font-weight: 500;
  color: #2c3e50;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #5a6874;
}

.gender-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.gender-group .el-radio {
  margin-right: 0;
}

.gender-group .el-radio.is-bordered {
  padding: 8px 16px;
  border-radius: 24px;
}

.gender-group .el-radio.is-bordered.is-checked {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.gender-select .el-input__inner {
  border-radius: 8px;
}

.gender-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.gender-option i {
  font-size: 16px;
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
    font-size: 26px;
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

  .gender-group {
    justify-content: center;
  }
}
</style>