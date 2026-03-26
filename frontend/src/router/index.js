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
import ProductList from '@/views/admin/ProductList.vue';
import OrderList from '@/views/admin/OrderList.vue';
import ProductCategory from '@/views/admin/ProductCategory.vue';

// 员工子页面
import StaffDashboard from '@/views/staff/Dashboard.vue';

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
        {
          path: 'comment-list',
          name: 'CommentManage',
          component: () => import('@/views/admin/CommentManage.vue'),
          meta: { title: '评价管理' }
        },
        {
          path: 'data-analysis',
          name: 'DataAnalysis',
          component: () => import('@/views/admin/DataAnalysis.vue'),
          meta: { title: '数据分析' }
        }
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