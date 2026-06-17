<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import type { MenuItem } from '@/components/GlobalHeader.vue'
import {healthCheck} from "@/api/healthController.ts";

healthCheck().then((res) => {
  console.log(res)
})

const menuItems: MenuItem[] = [
  { key: 'home', label: '首页', path: '/' },
  { key: 'products', label: '产品', path: '/products' },
  { key: 'about', label: '关于我们', path: '/about' },
]

const footerLinks = [
  { label: '关于我们', url: '/about' },
  { label: '隐私政策', url: '/privacy' },
  { label: '服务条款', url: '/terms' },
]
</script>

<template>
  <BasicLayout
    siteTitle="NoCodeApp"
    :menuItems="menuItems"
    :footerLinks="footerLinks"
    logoUrl="/favicon.ico"
  />
</template>
