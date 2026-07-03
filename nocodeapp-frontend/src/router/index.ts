import { ACCESS_ENUM } from '@/access/checkAccess'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
    },
    {
      path: '/app/:id/chat',
      name: 'appChatPage',
      component: () => import('@/views/app/AppChatPage.vue'),
      meta: {
        access: ACCESS_ENUM.USER,
        fullPage: true,
      },
    },
    {
      path: '/user/login',
      name: 'userLoginPage',
      component: () => import('@/views/user/UserLoginPage.vue'),
      meta: { fullPage: true },
    },
    {
      path: '/user/register',
      name: 'userRegisterPage',
      component: () => import('@/views/user/UserRegisterPage.vue'),
      meta: { fullPage: true },
    },
    {
      path: '/admin/userManage',
      name: 'adminUserManagePage',
      component: () => import('@/views/admin/UserManagerPage.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/admin/appManage',
      name: 'adminAppManagePage',
      component: () => import('@/views/admin/AppManagerPage.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/admin/appEdit/:id',
      name: 'adminAppEditPage',
      component: () => import('@/views/admin/AppEditPage.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/noAuth',
      name: 'noAuthPage',
      component: () => import('@/views/admin/UserNoAuthPage.vue'),
      meta: { fullPage: true },
    },
  ],
})

export default router
