<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import type { MenuItem } from '@/components/GlobalHeader.vue'
import { useLoginUserStore } from './stores/loginUser'
import { computed } from 'vue'
import router from './router'
import { checkAccess } from './access/checkAccess'

const allMenuItems: MenuItem[] = [
  { key: 'home', label: '首页', routeName: 'home', path: '/' },
  {
    key: 'appManage',
    label: '应用管理',
    routeName: 'adminAppManagePage',
    path: '/admin/appManage',
  },
  {
    key: 'userManage',
    label: '用户管理',
    routeName: 'adminUserManagePage',
    path: '/admin/userManage',
  },
]

const footerLinks = [
  { label: '关于我们', url: '/about' },
  { label: '隐私政策', url: '/privacy' },
  { label: '服务条款', url: '/terms' },
]

const menuItems = computed(() => {
  const filteredMenuItems = allMenuItems.filter((menuItem: MenuItem) => {
    const menuItemRoute = router.getRoutes().find((route) => route.name === menuItem.routeName)
    if (!menuItemRoute) {
      return false
    }
    if (menuItemRoute.meta?.access) {
      const loginUserStore = useLoginUserStore()
      return checkAccess(loginUserStore.loginUser, menuItemRoute.meta?.access as string)
    }
    return true
  })
  return filteredMenuItems
})
</script>

<template>
  <BasicLayout
    siteTitle="NoCodeApp"
    :menuItems="menuItems"
    :footerLinks="footerLinks"
    logoUrl="/favicon.ico"
  />
</template>
