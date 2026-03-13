import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';
import ForgetPassword from '@/views/ForgetPassword.vue';

Vue.use(Router);

export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/forget-password',
      name: 'ForgetPassword',
      component: ForgetPassword
    }
  ]
});

