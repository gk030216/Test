import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';
import ForgetPassword from '@/views/ForgetPassword.vue';
import Home from '@/views/Home.vue';
import AdminLayout from '@/views/admin/AdminLayout.vue';
import StaffLayout from '@/views/staff/StaffLayout.vue';

// 商城页面
import Shop from '@/views/Shop.vue';
import ProductDetail from '@/views/ProductDetail.vue';
import Cart from '@/views/Cart.vue';
import Checkout from '@/views/Checkout.vue';
import Pay from '@/views/Pay.vue';
import Orders from '@/views/Orders.vue';
import PayReturn from '@/views/PayReturn.vue';

// 管理员子页面
import AdminDashboard from '@/views/admin/Dashboard.vue';
import UserList from '@/views/admin/UserList.vue';
import StaffList from '@/views/admin/StaffList.vue';
import CarouselManage from '@/views/admin/CarouselManage.vue';
// 以下路由等待创建组件后再启用
// import ServiceList from '@/views/admin/ServiceList.vue';
// import AppointmentList from '@/views/admin/AppointmentList.vue';
// import PetList from '@/views/admin/PetList.vue';
// import ProductList from '@/views/admin/ProductList.vue';
// import OrderList from '@/views/admin/OrderList.vue';
// import PostList from '@/views/admin/PostList.vue';
// import Statistics from '@/views/admin/Statistics.vue';
// import Settings from '@/views/admin/Settings.vue';

// 员工子页面
import StaffDashboard from '@/views/staff/Dashboard.vue';
// 以下路由等待创建组件后再启用
// import StaffAppointments from '@/views/staff/Appointments.vue';
// import StaffPetFiles from '@/views/staff/PetFiles.vue';
// import StaffEvaluations from '@/views/staff/Evaluations.vue';
// import StaffFeedback from '@/views/staff/Feedback.vue';

Vue.use(Router);

const router = new Router({
  mode: 'history',
  routes: [
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
    {
      path: '/pay/return',
      name: 'PayReturn',
      component: PayReturn,
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
        { path: 'carousel', component: CarouselManage, meta: { title: '轮播图管理' } }
        // 以下路由等待创建对应组件后再启用
        // { path: 'service-list', component: ServiceList, meta: { title: '服务列表' } },
        // { path: 'appointment-list', component: AppointmentList, meta: { title: '预约管理' } },
        // { path: 'pet-list', component: PetList, meta: { title: '宠物列表' } },
        // { path: 'product-list', component: ProductList, meta: { title: '商品列表' } },
        // { path: 'order-list', component: OrderList, meta: { title: '订单管理' } },
        // { path: 'post-list', component: PostList, meta: { title: '帖子管理' } },
        // { path: 'statistics', component: Statistics, meta: { title: '数据统计' } },
        // { path: 'settings', component: Settings, meta: { title: '系统设置' } }
      ]
    },
    // ========== 员工路由 ==========
    {
      path: '/staff',
      component: StaffLayout,
      meta: { requiresAuth: true, role: [2, 3] },
      children: [
        { path: '', redirect: 'dashboard' },
        { path: 'dashboard', component: StaffDashboard, meta: { title: '工作台' } }
        // 以下路由等待创建对应组件后再启用
        // { path: 'appointments', component: StaffAppointments, meta: { title: '预约管理' } },
        // { path: 'pet-files', component: StaffPetFiles, meta: { title: '宠物档案' } },
        // { path: 'evaluations', component: StaffEvaluations, meta: { title: '服务评价' } },
        // { path: 'feedback', component: StaffFeedback, meta: { title: '异常反馈' } }
      ]
    }
  ]
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const userInfo = localStorage.getItem('userInfo');
  const user = userInfo ? JSON.parse(userInfo) : null;
  const userRole = user?.role || 1;

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return;
    }

    // 检查角色权限
    if (to.meta.role && !to.meta.role.includes(userRole)) {
      next('/');
      return;
    }
  }

  next();
});

export default router;