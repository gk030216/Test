import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';
import ForgetPassword from '@/views/ForgetPassword.vue';
import Home from '@/views/Home.vue';
import Admin_Layout from '@/views/admin/Admin_Layout.vue';
import Personal_Layout from '@/views/personal/Personal_Layout.vue';
import Admin_Profile from '@/views/admin/Admin_Profile.vue';

//维护界面
import Maintenance from '@/views/Maintenance.vue';

// 导入前台服务页面
import FrontServiceList from '@/views/service/ServiceList.vue';
import ServiceDetail from '@/views/service/ServiceDetail.vue';
import ServiceBooking from '@/views/service/ServiceBooking.vue';

// 商城页面
import Shop from '@/views/Shop.vue';
import ProductDetail from '@/views/ProductDetail.vue';
import Cart from '@/views/Cart.vue';
import Checkout from '@/views/Checkout.vue';
import Pay from '@/views/Pay.vue';
import Orders from '@/views/Orders.vue';
import PayReturn from '@/views/PayReturn.vue';
import AIChat from '@/views/AIChat.vue';

// 社区页面
import Community from '@/views/Community.vue';
import PostDetail from '@/views/PostDetail.vue';

// 个人中心子页面
import Personal_Profile from '@/views/personal/Personal_Profile.vue';
import Personal_Address from '@/views/personal/Personal_Address.vue';
import Personal_Favorites from '@/views/personal/Personal_Favorites.vue';
import Personal_Posts from '@/views/personal/Personal_Posts.vue';
import Personal_Comments from '@/views/personal/Personal_Comments.vue';
import Personal_Security from '@/views/personal/Personal_Security.vue';
import MyPets from '@/views/MyPets.vue';
import Personal_Orders from "@/views/personal/Personal_Orders.vue";
import Personal_Appointments from '@/views/MyAppointments.vue';
import Personal_Notifications from '@/views/personal/Personal_Notifications.vue';
import Feedback from '@/views/Feedback.vue';

// 管理员子页面
import Admin_Dashboard from '@/views/admin/Admin_Dashboard.vue';
import Admin_UserList from '@/views/admin/Admin_UserList.vue';
import Admin_StaffList from '@/views/admin/Admin_StaffList.vue';
import Admin_CarouselManage from '@/views/admin/Admin_CarouselManage.vue';
import Admin_ProductList from '@/views/admin/Admin_ProductList.vue';
import Admin_OrderList from '@/views/admin/Admin_OrderList.vue';
import Admin_ProductCategory from '@/views/admin/Admin_ProductCategory.vue';
import Admin_ProductCommentManage from '@/views/admin/Admin_ProductCommentManage.vue';
import Admin_ProductAnalysis from '@/views/admin/Admin_ProductAnalysis.vue';
import Admin_PetList from '@/views/admin/Admin_PetList.vue';

import Admin_PetAnalysis from '@/views/admin/Admin_PetAnalysis.vue';
import Admin_ServiceCategory from '@/views/admin/Admin_ServiceCategory.vue';
import Admin_ServiceCommentManage from "@/views/admin/Admin_ServiceCommentManage.vue";
import Admin_Settings from "@/views/admin/Admin_Settings.vue";
import Admin_PetVaccineRecord from '@/views/admin/Admin_PetVaccineRecord.vue';
import Admin_PetHealthRecord from '@/views/admin/Admin_PetHealthRecord.vue';



// 先导入员工布局组件
import StaffLayout from '@/views/staff/StaffLayout.vue';
import StaffDashboard from '@/views/staff/StaffDashboard.vue';
import StaffAppointmentList from "@/views/staff/StaffAppointmentList.vue";
import StaffPetList from "@/views/staff/StaffPetList.vue";

// 后台服务列表
import Admin_ServiceList from '@/views/admin/Admin_ServiceList.vue';
import Admin_AppointmentList from '@/views/admin/Admin_AppointmentList.vue';

// 社区管理子页面
import Admin_PostList from '@/views/admin/Admin_PostList.vue';
import Admin_PostCommentManage from '@/views/admin/Admin_PostCommentManage.vue';

// AI管理子页面
import Admin_AI_KnowledgeManage from '@/views/admin/Admin_AI_KnowledgeManage.vue';
import Admin_AI_FaqManage from '@/views/admin/Admin_AI_FaqManage.vue';
import Admin_AI_ChatHistory from '@/views/admin/Admin_AI_ChatHistory.vue';
import StaffEvaluations from "@/views/staff/StaffEvaluations.vue";
import StaffFeedback from "@/views/staff/StaffFeedback.vue";
import Admin_VaccineStock from "@/views/admin/Admin_VaccineStock.vue";
import Admin_ProductStock from "@/views/admin/Admin_ProductStock.vue";
import Admin_ProductCommentAnalysis from "@/views/admin/Admin_ProductCommentAnalysis.vue";
import Admin_ServiceCommentAnalysis from "@/views/admin/Admin_ServiceCommentAnalysis.vue";
import Admin_AI_UserChatHistory from "@/views/admin/Admin_AI_UserChatHistory.vue";
import Admin_Feedback from "@/views/admin/Admin_Feedback.vue";
import StaffVaccineRecord from "@/views/staff/StaffVaccineRecord.vue";
import StaffHealthRecord from "@/views/staff/StaffHealthRecord.vue";

Vue.use(Router);

const router = new Router({
  mode: 'history',
  routes: [
    // ========== 公共路由 ==========
    {
      path: '/',
      name: 'Home',
      component: Home,
      meta: { requiresAuth: false }
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: Register,
      meta: { requiresAuth: false }
    },
    {
      path: '/forget-password',
      name: 'ForgetPassword',
      component: ForgetPassword,
      meta: { requiresAuth: false }
    },
    {
      path: '/maintenance',
      name: 'Maintenance',
      component: Maintenance,
      meta: { requiresAuth: false }
    },
    {
      path: '/notices',
      name: 'Notices',
      component: () => import('@/views/Notices.vue')
    },

    // ========== 前台服务路由 ==========
    {
      path: '/services',
      name: 'FrontServiceList',
      component: FrontServiceList,
      meta: { requiresAuth: false }
    },
    {
      path: '/service/:id',
      name: 'ServiceDetail',
      component: ServiceDetail,
      meta: { requiresAuth: false }
    },
    {
      path: '/service/:id/booking',
      name: 'ServiceBooking',
      component: ServiceBooking,
      meta: { requiresAuth: true }
    },

    // ========== 员工路由 ==========
    {
      path: '/staff',
      component: StaffLayout,
      meta: { requiresAuth: true, role: [2] },
      children: [
        { path: '', redirect: 'dashboard' },
        { path: 'dashboard', component: StaffDashboard, meta: { title: '工作台' } },
        { path: 'appointments', component: StaffAppointmentList, meta: { title: '预约管理' } },
        { path: 'pet-list', component: StaffPetList, meta: { title: '宠物列表' } },
        { path: 'evaluations', component: StaffEvaluations, meta: { title: '服务评价' } },
        { path: 'feedback-manage', component: StaffFeedback, meta: { title: '异常反馈' } },
        {
          path: 'Staff-vaccine-record',
          component: StaffVaccineRecord,
          meta: { title: '疫苗记录' }
        },
        {
          path: 'Staff-health-record',
          component: StaffHealthRecord,
          meta: { title: '体检记录' }
        },
        {
          path: 'vaccine-stock',
          component: Admin_VaccineStock,
          meta: { title: '疫苗库存' }
        },
        {
          path: 'product-stock',
          component: Admin_ProductStock,
          meta: { title: '商品库存' }
        },
        { path: 'Profile', component: Admin_Profile, meta: { title: '个人资料' } },

      ]
    },

    // ========== 个人中心路由 ==========
    {
      path: '/personal',
      component: Personal_Layout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: 'profile' },
        { path: 'profile', component: Personal_Profile, meta: { title: '个人资料' } },
        { path: 'orders', component: Personal_Orders, meta: { title: '我的订单' } },
        { path: 'address', component: Personal_Address, meta: { title: '收货地址' } },
        { path: 'favorites', component: Personal_Favorites, meta: { title: '我的收藏' } },
        { path: 'posts', component: Personal_Posts, meta: { title: '我的帖子' } },
        { path: 'comments', component: Personal_Comments, meta: { title: '我的评论' } },
        { path: 'security', component: Personal_Security, meta: { title: '账号安全' } },
        { path: 'notifications', component: Personal_Notifications, meta: { title: '消息通知' } },

      ]
    },

    // ========== 我的预约独立页面 ==========
    {
      path: '/my-appointments',
      name: 'Personal_Appointments',
      component: Personal_Appointments,
      meta: { requiresAuth: true }
    },

    // ========== 意见反馈独立页面 ==========
    {
      path: '/feedback',
      name: 'Feedback',
      component: Feedback,
      meta: { requiresAuth: true }
    },

    // ========== 我的宠物独立路由 ==========
    {
      path: '/my-pets',
      name: 'MyPets',
      component: MyPets,
      meta: { requiresAuth: true }
    },

    // ========== 商城路由 ==========
    {
      path: '/shop',
      name: 'Shop',
      component: Shop,
      meta: { requiresAuth: false }
    },
    {
      path: '/product/:id',
      name: 'ProductDetail',
      component: ProductDetail,
      meta: { requiresAuth: false }
    },
    {
      path: '/cart',
      name: 'Cart',
      component: Cart,
      meta: { requiresAuth: true }
    },
    {
      path: '/checkout',
      name: 'Checkout',
      component: Checkout,
      meta: { requiresAuth: true }
    },
    {
      path: '/pay/return',
      name: 'PayReturn',
      component: PayReturn,
      meta: { requiresAuth: true }
    },
    {
      path: '/pay/:orderNo',
      name: 'Pay',
      component: Pay,
      meta: { requiresAuth: true }
    },
    {
      path: '/orders',
      name: 'Orders',
      component: Orders,
      meta: { requiresAuth: true }
    },

    // ========== AI咨询路由 ==========
    {
      path: '/ai-chat',
      name: 'AIChat',
      component: AIChat,
      meta: { requiresAuth: true }
    },

    // ========== 社区路由 ==========
    {
      path: '/community',
      name: 'Community',
      component: Community,
      meta: { requiresAuth: true }
    },
    {
      path: '/community/post/:id',
      name: 'PostDetail',
      component: PostDetail,
      meta: { requiresAuth: true }
    },

    // ========== 管理员路由 ==========
    {
      path: '/admin',
      component: Admin_Layout,
      meta: { requiresAuth: true, role: [3] },
      children: [
        { path: '', redirect: 'dashboard' },
        { path: 'dashboard', component: Admin_Dashboard, meta: { title: '仪表盘' } },
        { path: 'user-list', component: Admin_UserList, meta: { title: '用户列表' } },
        { path: 'staff-list', component: Admin_StaffList, meta: { title: '员工列表' } },
        { path: 'service-category', component: Admin_ServiceCategory, meta: { title: '服务分类' } },
        { path: 'service-list', component: Admin_ServiceList, meta: { title: '服务列表' } },
        { path: 'appointment-list', component: Admin_AppointmentList, meta: { title: '预约管理' } },
        { path: 'service-comment-list', component: Admin_ServiceCommentManage, meta: { title: '服务评价管理' } },
        { path: 'carousel', component: Admin_CarouselManage, meta: { title: '轮播图管理' } },
        { path: 'product-list', component: Admin_ProductList, meta: { title: '商品列表' } },
        { path: 'order-list', component: Admin_OrderList, meta: { title: '订单管理' } },
        { path: 'category-list', component: Admin_ProductCategory, meta: { title: '商品分类' } },
        { path: 'comment-list', component: Admin_ProductCommentManage, meta: { title: '评价管理' } },
        { path: 'product-analysis', component: Admin_ProductAnalysis, meta: { title: '商品分析' } },
        { path: 'post-manage', component: Admin_PostList, meta: { title: '帖子管理' } },
        { path: 'comment-manage', component: Admin_PostCommentManage, meta: { title: '评论管理' } },
        { path: 'knowledge-list', component: Admin_AI_KnowledgeManage, meta: { title: '知识库管理' } },
        { path: 'faq-list', component: Admin_AI_FaqManage, meta: { title: '常见问题管理' } },
        { path: 'chat-history', component: Admin_AI_ChatHistory, meta: { title: '对话记录' } },
        { path: 'pet-list', component: Admin_PetList, meta: { title: '宠物列表' } },

        { path: 'pet-analysis', component: Admin_PetAnalysis, meta: { title: '宠物分析' } },
        { path: 'feedback', component: Admin_Feedback, meta: { title: '异常反馈' } },
        { path: 'settings', component: Admin_Settings, meta: { title: '系统设置' } },
        { path: 'Profile', component: Admin_Profile, meta: { title: '个人资料' } },
        {
          path: 'vaccine-record',
          name: 'Admin_PetVaccineRecord',
          component: Admin_PetVaccineRecord,
          meta: { title: '疫苗记录管理' }
        },
        {
          path: 'health-record',
          name: 'Admin_PetHealthRecord',
          component: Admin_PetHealthRecord,
          meta: { title: '体检记录管理' }
        },
        {
          path: 'vaccine-stock',
          name: 'Admin_VaccineStock',
          component: Admin_VaccineStock,
          meta: { title: '疫苗库存' }
        },
        {
          path: 'product-stock',
          name: 'Admin_ProductStock',
          component: Admin_ProductStock,
          meta: { title: '商品库存' }
        },
        {
          path: 'product-comment-analysis',
          name: 'Admin_ProductCommentAnalysis',
          component: Admin_ProductCommentAnalysis,
          meta: { title: '商品评价分析' }
        },
        {
          path: 'service-comment-analysis',
          name: 'Admin_ServiceCommentAnalysis',
          component: Admin_ServiceCommentAnalysis,
          meta: { title: '服务评价分析' }
        },
        {
          path: 'user-chat-history',
          name: 'Admin_AI_UserChatHistory',
          component: Admin_AI_UserChatHistory,
          meta: { title: '用户对话记录' }
        },
        {
          path: 'user-analysis',
          name: 'Admin_UserAnalysis',
          component: () => import('@/views/admin/Admin_UserAnalysis.vue'),
          meta: { title: '用户分析' }
        },
        {
          path: 'service-analysis',
          name: 'Admin_ServiceAnalysis',
          component: () => import('@/views/admin/Admin_ServiceAnalysis.vue'),
          meta: { title: '服务数据分析' }
        },
        {
          path: 'notice-manage',
          name: 'Admin_NoticeManage',
          component: () => import('@/views/admin/Admin_NoticeManage.vue'),
          meta: { title: '咨询公告' }
        },
        {
          path: 'appointment-time',
          name: 'Admin_AppointmentTimeList',
          component: () => import('@/views/admin/Admin_AppointmentTimeList.vue'),
          meta: { title: '预约时间' }
        }
      ]
    }
  ]
});

// 路由守卫 - 权限验证
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const userInfo = localStorage.getItem('userInfo');
  let userRole = 1;

  if (userInfo) {
    try {
      const user = JSON.parse(userInfo);
      userRole = user.role || 1;
    } catch (e) {
      console.error('解析用户信息失败', e);
    }
  }

  // 检查维护模式
  const settings = localStorage.getItem('systemSettings');
  if (settings) {
    try {
      const basic = JSON.parse(settings).basic;
      if (basic.maintenanceMode && userRole !== 3) {
        // 维护模式下只允许管理员访问
        if (to.path !== '/maintenance' && to.path !== '/login') {
          next('/maintenance');
          return;
        }
      }
    } catch (e) {
      console.error('解析维护模式设置失败', e);
    }
  }

  if (to.meta.requiresAuth) {
    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return;
    }

    if (to.meta.role && !to.meta.role.includes(userRole)) {
      // 根据角色跳转到对应的工作台
      if (userRole === 2) {
        next('/staff');
      } else if (userRole === 3) {
        next('/admin');
      } else {
        next('/');
      }
      return;
    }
  }

  next();
});

export default router;