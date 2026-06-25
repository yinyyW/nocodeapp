<script setup lang="ts">
import { useRouter } from "vue-router";
import {
  LockOutlined,
  HomeOutlined,
  ArrowLeftOutlined,
} from "@ant-design/icons-vue";

const router = useRouter();

const goHome = () => {
  router.push("/");
};

const goBack = () => {
  router.back();
};
</script>

<template>
  <div class="no-auth-page">
    <div class="no-auth-container">
      <div class="no-auth-card">
        <div class="icon-wrapper">
          <LockOutlined class="lock-icon" />
        </div>
        <h1 class="title">403</h1>
        <p class="subtitle">权限不足</p>
        <p class="description">抱歉，您没有访问该页面的权限。请联系管理员获取相应权限。</p>
        <div class="actions">
          <a-button type="primary" size="large" class="action-btn" @click="goHome">
            <template #icon>
              <HomeOutlined />
            </template>
            返回首页
          </a-button>
          <a-button size="large" class="action-btn" @click="goBack">
            <template #icon>
              <ArrowLeftOutlined />
            </template>
            返回上一页
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.no-auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}

.no-auth-container {
  width: 100%;
  max-width: 480px;
}

.no-auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 48px 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  text-align: center;
}

.icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.lock-icon {
  font-size: 36px;
  color: #ff4d4f;
}

.title {
  font-size: 64px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
  line-height: 1;
}

.subtitle {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

.description {
  font-size: 14px;
  color: #999;
  margin: 0 0 32px;
  line-height: 1.6;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-btn {
  min-width: 140px;
  border-radius: 8px;
}

.action-btn:not(.ant-btn-primary) {
  border-color: #d9d9d9;
}

@media (max-width: 480px) {
  .no-auth-page {
    padding: 16px;
  }

  .no-auth-card {
    padding: 32px 20px;
  }

  .title {
    font-size: 48px;
  }

  .actions {
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
  }
}
</style>
