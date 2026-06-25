import { ACCESS_ENUM } from "@/access/checkAccess";
import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: () => import("@/views/Home.vue"),
    },
    {
      path: "/user/login",
      name: "userLoginPage",
      component: () => import("@/views/user/UserLoginPage.vue"),
      meta: { fullPage: true },
    },
    {
      path: "/user/register",
      name: "userRegisterPage",
      component: () => import("@/views/user/UserRegisterPage.vue"),
      meta: { fullPage: true },
    },
    {
      path: "/admin/userManage",
      name: "adminUserManagePage",
      component: () => import("@/views/admin/UserManagerPage.vue"),
      meta: {
        access: ACCESS_ENUM.ADMIN
      }
    },
    {
      path: "/noAuth",
      name: "noAuthPage",
      component: () => import("@/views/admin/UserNoAuthPage.vue"),
      meta: { fullPage: true },
    },
  ],
});

export default router;
