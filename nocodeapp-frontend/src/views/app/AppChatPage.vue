<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CloudUploadOutlined,
  CodeOutlined,
  EyeOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getApp } from '@/api/appController'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const app = ref<API.AppVO>()

const appId = computed(() => Number(route.params.id))

const fetchApp = async () => {
  if (!appId.value) {
    message.error('应用 ID 无效')
    router.replace('/')
    return
  }
  loading.value = true
  try {
    const res = await getApp({ id: appId.value })
    if (res?.data?.code === 0 && res.data.data) {
      app.value = res.data.data
    } else {
      message.error(res?.data?.message ?? '获取应用信息失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(fetchApp)
</script>

<template>
  <div class="app-chat-page">
    <a-button class="back-btn" @click="router.push('/')">
      <template #icon>
        <ArrowLeftOutlined />
      </template>
      返回首页
    </a-button>

    <a-spin :spinning="loading">
      <a-card class="chat-card" :bordered="false">
        <div class="app-hero">
          <div class="cover-box">
            <img v-if="app?.cover" :src="app.cover" :alt="app?.appName || '应用封面'" />
            <CodeOutlined v-else />
          </div>
          <div class="hero-content">
            <a-tag color="blue">应用已创建</a-tag>
            <h1>{{ app?.appName || `应用 ${appId}` }}</h1>
            <p>{{ app?.initPrompt || '从首页提示词创建的 NoCode 应用。' }}</p>
            <a-descriptions size="small" :column="2" class="app-meta">
              <a-descriptions-item label="应用 ID">{{ app?.id || appId }}</a-descriptions-item>
              <a-descriptions-item label="创建时间">
                {{ app?.createTime ? dayjs(app.createTime).format('YYYY-MM-DD HH:mm') : '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="生成类型">{{
                app?.codeGenType || '-'
              }}</a-descriptions-item>
              <a-descriptions-item label="部署标识">{{
                app?.deployKey || '未部署'
              }}</a-descriptions-item>
            </a-descriptions>
          </div>
        </div>

        <a-divider />

        <div class="coming-soon-grid">
          <div class="status-item">
            <CodeOutlined />
            <h3>代码生成</h3>
            <p>AI 对话生成网站代码暂未开放。</p>
          </div>
          <div class="status-item">
            <EyeOutlined />
            <h3>网站预览</h3>
            <p>应用预览能力将在后续接入。</p>
          </div>
          <div class="status-item">
            <CloudUploadOutlined />
            <h3>一键部署</h3>
            <p>部署流程本期保留入口说明。</p>
          </div>
        </div>
      </a-card>
    </a-spin>
  </div>
</template>

<style scoped>
.app-chat-page {
  padding: 0;
}

.back-btn {
  margin-bottom: 16px;
}

.chat-card {
  border-radius: 8px;
}

.app-hero {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 28px;
  align-items: center;
}

.cover-box {
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  border-radius: 8px;
  background: #f1f5f9;
  color: #94a3b8;
  font-size: 54px;
}

.cover-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-content h1 {
  margin: 12px 0 10px;
  color: #111827;
  font-size: 30px;
  font-weight: 700;
}

.hero-content p {
  max-width: 720px;
  margin: 0 0 18px;
  color: #475569;
  line-height: 1.7;
}

.app-meta {
  max-width: 680px;
}

.coming-soon-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.status-item {
  min-height: 150px;
  padding: 22px;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  background: #fbfdff;
}

.status-item :deep(.anticon) {
  color: #1677ff;
  font-size: 28px;
}

.status-item h3 {
  margin: 14px 0 8px;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}

.status-item p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .app-hero,
  .coming-soon-grid {
    grid-template-columns: 1fr;
  }
}
</style>
