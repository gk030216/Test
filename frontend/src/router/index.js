import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';
import ForgetPassword from '@/views/ForgetPassword.vue';
import Home from '@/views/Home.vue';
import AdminLayout from '@/views/admin/AdminLayout.vue';

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
import AdminPostManage from '@/views/admin/AdminPostManage.vue';

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
        { path: 'carousel', component: CarouselManage, meta: { title: '轮播图管理' } },
        { path: 'product-list', component: ProductList, meta: { title: '商品列表' } },
        { path: 'order-list', component: OrderList, meta: { title: '订单管理' } },
        { path: 'category-list', component: ProductCategory, meta: { title: '商品分类' } },
        { path: 'comment-list', component: CommentManage, meta: { title: '评价管理' } },
        { path: 'data-analysis', component: DataAnalysis, meta: { title: '数据分析' } },
        // 社区管理
        { path: 'post-manage', component: AdminPostManage, meta: { title: '帖子管理' } },
        // AI管理
        { path: 'knowledge-list', component: KnowledgeManage, meta: { title: '知识库管理' } },
        { path: 'faq-list', component: FaqManage, meta: { title: '常见问题管理' } },
        { path: 'chat-history', component: ChatHistory, meta: { title: '对话记录' } }
      ]
    }
  ]
});

// 路由守卫 - 权限验证
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const userInfo = localStorage.getItem('userInfo');
  let userRole = 1; // 默认普通用户

  if (userInfo) {
    try {
      const user = JSON.parse(userInfo);
      userRole = user.role || 1;
    } catch (e) {
      console.error('解析用户信息失败', e);
    }
  }

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    if (!token) {
      // 未登录，跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return;
    }

    // 检查角色权限
    if (to.meta.role && !to.meta.role.includes(userRole)) {
      // 权限不足，跳转到首页
      next('/');
      return;
    }
  }

  next();
});

export default router;