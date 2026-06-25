<script setup lang="ts">
import { useRoute } from "vue-router";
import type { MenuItem } from "@/components/GlobalHeader.vue";
import GlobalHeader from "@/components/GlobalHeader.vue";
import GlobalFooter from "@/components/GlobalFooter.vue";

const route = useRoute();

withDefaults(
  defineProps<{
    menuItems?: MenuItem[];
    siteTitle?: string;
    logoUrl?: string;
    copyright?: string;
    footerLinks?: Array<{ label: string; url: string; icon?: string }>;
  }>(),
  {
    menuItems: () => [],
    siteTitle: "NoCodeApp",
    logoUrl: "",
    copyright: "Copyright &copy; 2026 NoCodeApp. All rights reserved.",
    footerLinks: () => [],
  },
);
</script>

<template>
  <template v-if="route.meta?.fullPage">
    <router-view />
  </template>
  <template v-else>
    <a-layout class="basic-layout">
      <a-layout-header class="layout-header">
        <GlobalHeader
          :menuItems="menuItems"
          :siteTitle="siteTitle"
          :logoUrl="logoUrl"
        />
      </a-layout-header>

      <a-layout-content class="layout-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </a-layout-content>

      <a-layout-footer class="layout-footer">
        <GlobalFooter :copyright="copyright" :links="footerLinks" />
      </a-layout-footer>
    </a-layout>
  </template>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.layout-header {
  height: auto;
  line-height: normal;
  padding: 0;
  background: transparent;
}

.layout-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-wrapper {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 24px auto;
  padding: 0 24px;
  box-sizing: border-box;
}

.layout-footer {
  padding: 0;
  background: transparent;
}

@media (max-width: 768px) {
  .content-wrapper {
    margin: 16px auto;
    padding: 0 16px;
  }
}
</style>
