import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';
import ForgetPassword from '@/views/ForgetPassword.vue';
import Home from '@/views/Home.vue';
import AdminLayout from '@/views/admin/AdminLayout.vue';
import PersonalCenterLayout from '@/views/personal/PersonalCenterLayout.vue';

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
import Profile from '@/views/personal/Profile.vue';
import Address from '@/views/personal/Address.vue';
import Favorites from '@/views/personal/Favorites.vue';
import MyPosts from '@/views/personal/MyPosts.vue';
import MyComments from '@/views/personal/MyComments.vue';
import Security from '@/views/personal/Security.vue';
import MyPets from '@/views/MyPets.vue';

// 管理员子页面
import AdminDashboard from '@/views/admin/Dashboard.vue';
import UserList from '@/views/admin/UserList.vue';
import StaffList from '@/views/admin/StaffList.vue';
import CarouselManage from '@/views/admin/CarouselManage.vue';
import ProductList from '@/views/admin/ProductList.vue';
import OrderList from '@/views/admin/OrderList.vue';
import ProductCategory from '@/views/admin/ProductCategory.vue';
import CommentManage from '@/views/admin/CommentManage.vue';
import DataAnalysis from '@/views/admin/DataAnalysis.vue';
import AdminPetList from '@/views/admin/AdminPetList.vue';
import AdminPetHealth from '@/views/admin/AdminPetHealth.vue';
import AdminPetStatistics from '@/views/admin/AdminPetStatistics.vue';
import ServiceCategory from '@/views/admin/ServiceCategory.vue';
import ServiceList from '@/views/admin/ServiceList.vue';
import AppointmentList from '@/views/admin/AppointmentList.vue';

// 社区管理子页面
import AdminPostManage from '@/views/admin/AdminPostManage.vue';
import AdminCommentManage from '@/views/admin/AdminCommentManage.vue';

// AI管理子页面
import KnowledgeManage from '@/views/admin/KnowledgeManage.vue';
import FaqManage from '@/views/admin/FaqManage.vue';
import ChatHistory from '@/views/admin/ChatHistory.vue';

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

    // ========== 个人中心路由 ==========
    {
      path: '/personal',
      component: PersonalCenterLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: 'profile' },
        { path: 'profile', component: Profile, meta: { title: '个人资料' } },
        { path: 'orders', component: Orders, meta: { title: '我的订单' } },
        { path: 'address', component: Address, meta: { title: '收货地址' } },
        { path: 'favorites', component: Favorites, meta: { title: '我的收藏' } },
        { path: 'posts', component: MyPosts, meta: { title: '我的帖子' } },
        { path: 'comments', component: MyComments, meta: { title: '我的评论' } },
        { path: 'security', component: Security, meta: { title: '账号安全' } }
      ]
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
      component: AdminLayout,
      meta: { requiresAuth: true, role: [3] },
      children: [
        { path: '', redirect: 'dashboard' },
        { path: 'dashboard', component: AdminDashboard, meta: { title: '仪表盘' } },
        { path: 'user-list', component: UserList, meta: { title: '用户列表' } },
        { path: 'staff-list', component: StaffList, meta: { title: '员工列表' } },
        { path: 'service-category', component: ServiceCategory, meta: { title: '服务分类' } },
        { path: 'service-list', component: ServiceList, meta: { title: '服务列表' } },
        { path: 'appointment-list', component: AppointmentList, meta: { title: '预约管理' } },
        { path: 'carousel', component: CarouselManage, meta: { title: '轮播图管理' } },
        { path: 'product-list', component: ProductList, meta: { title: '商品列表' } },
        { path: 'order-list', component: OrderList, meta: { title: '订单管理' } },
        { path: 'category-list', component: ProductCategory, meta: { title: '商品分类' } },
        { path: 'comment-list', component: CommentManage, meta: { title: '评价管理' } },
        { path: 'data-analysis', component: DataAnalysis, meta: { title: '数据分析' } },
        // 社区管理路由
        { path: 'post-manage', component: AdminPostManage, meta: { title: '帖子管理' } },
        { path: 'comment-manage', component: AdminCommentManage, meta: { title: '评论管理' } },
        // AI管理路由
        { path: 'knowledge-list', component: KnowledgeManage, meta: { title: '知识库管理' } },
        { path: 'faq-list', component: FaqManage, meta: { title: '常见问题管理' } },
        { path: 'chat-history', component: ChatHistory, meta: { title: '对话记录' } },
        { path: 'pet-list', component: AdminPetList, meta: { title: '宠物列表' } },
        { path: 'pet-health', component: AdminPetHealth, meta: { title: '健康记录' } },
        { path: 'pet-statistics', component: AdminPetStatistics, meta: { title: '宠物数据统计' } }
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

  if (to.meta.requiresAuth) {
    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return;
    }

    if (to.meta.role && !to.meta.role.includes(userRole)) {
      next('/');
      return;
    }
  }

  next();
});

export default router;