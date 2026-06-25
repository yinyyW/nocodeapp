<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { MenuOutlined } from "@ant-design/icons-vue";
import { message, type MenuProps } from "ant-design-vue";
import { useLoginUserStore } from "@/stores/loginUser";
import { logout } from "@/api/userController";

export interface MenuItem {
  key: string;
  label: string;
  routeName: string;
  path?: string;
  children?: MenuItem[];
}

const props = withDefaults(
  defineProps<{
    menuItems?: MenuItem[];
    siteTitle?: string;
    logoUrl?: string;
  }>(),
  {
    menuItems: () => [],
    siteTitle: "NoCodeApp",
    logoUrl: "",
  },
);

const emit = defineEmits<{
  (e: "menuClick", item: MenuItem): void;
}>();

const router = useRouter();
const route = useRoute();
const mobileMenuOpen = ref(false);
const loginStore = useLoginUserStore();

const selectedKeys = computed<string[]>(() => {
  const sorted = [...props.menuItems]
    .filter((item) => item.path)
    .sort((a, b) => (b.path?.length ?? 0) - (a.path?.length ?? 0));
  const selected = sorted.find(
    (item) => item.path && route.path.startsWith(item.path),
  );
  return selected ? [selected.key] : [];
});

const handleMenuClick: MenuProps["onClick"] = (info) => {
  const item = props.menuItems.find((i) => i.key === info.key);
  if (item?.path) {
    router.push(item.path);
  }
  if (item) {
    emit("menuClick", item);
  }
  mobileMenuOpen.value = false;
};

const handleLogout = async () => {
  const res = await logout();
  if (res?.data?.code !== 0 || !res?.data?.data) {
    message.error("退出登录失败");
    return;
  }
  message.success("退出登录成功");
  loginStore.userLogout();
  router.push("/user/login");
};
</script>

<template>
  <header class="global-header">
    <div class="header-inner">
      <div class="header-left">
        <MenuOutlined class="mobile-menu-trigger" @click="mobileMenuOpen = true" />
        <div class="logo-area" @click="router.push('/')">
          <img v-if="logoUrl" :src="logoUrl" alt="logo" class="logo-img" />
          <span class="site-title">{{ siteTitle }}</span>
        </div>
      </div>

      <div class="header-center">
        <a-menu v-model:selectedKeys="selectedKeys" mode="horizontal" :items="menuItems" @click="handleMenuClick"
          class="header-menu" />
      </div>

      <div class="header-right">
        <div v-if="loginStore?.loginUser?.id">
          <a-dropdown>
            <a-space>
              <a-avatar :src="loginStore?.loginUser?.userAvatar" />
              {{
                loginStore?.loginUser?.userName ??
                `用户${loginStore?.loginUser?.id}`
              }}
            </a-space>

            <template #overlay>
              <a-menu>
                <a-menu-item>
                  <a @click="handleLogout">退出登录</a>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <a-button type="primary" class="login-btn" href="/user/login">登录</a-button>
        </div>
      </div>
    </div>

    <a-drawer v-model:open="mobileMenuOpen" placement="left" title="菜单" :closable="true">
      <a-menu mode="inline" :items="menuItems" @click="handleMenuClick" class="mobile-drawer-menu" />
    </a-drawer>
  </header>
</template>

<style scoped>
.global-header {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  height: 64px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
}

.header-inner {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.mobile-menu-trigger {
  display: none;
  font-size: 20px;
  margin-right: 12px;
  cursor: pointer;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 24px;
}

.header-menu {
  flex: 1;
  justify-content: center;
  border-bottom: none;
  line-height: 64px;
}

.header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.login-btn {
  border-radius: 6px;
}

.header-menu :deep(.ant-menu-item)::after {
  left: 50%;
  bottom: -2px;
  transform: translateX(-50%);
  transition: all 0.3s ease;
  width: 0;
}

.header-menu :deep(.ant-menu-item-selected)::after {
  width: 100%;
}

/* Responsive */
@media (max-width: 768px) {
  .header-center {
    display: none;
  }

  .mobile-menu-trigger {
    display: inline-block;
  }

  .header-inner {
    padding: 0 16px;
    justify-content: space-between;
  }
}
</style>
