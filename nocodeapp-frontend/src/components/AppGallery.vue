<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { AppstoreOutlined, UserOutlined } from '@ant-design/icons-vue'

const props = withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    apps: API.AppVO[]
    loading?: boolean
    total?: number
    current?: number
    pageSize?: number
    searchable?: boolean
    placeholder?: string
    emptyText?: string
  }>(),
  {
    subtitle: '',
    loading: false,
    total: 0,
    current: 1,
    pageSize: 6,
    searchable: true,
    placeholder: '搜索应用名称',
    emptyText: '暂无应用',
  },
)

const emit = defineEmits<{
  (e: 'search', keyword: string): void
  (e: 'pageChange', page: number, pageSize: number): void
  (e: 'open', app: API.AppVO): void
}>()

const keyword = ref('')

watch(
  () => props.current,
  () => {
    keyword.value = keyword.value.trim()
  },
)

const hasApps = computed(() => props.apps.length > 0)

const formatTime = (value?: string) => {
  if (!value) {
    return '刚刚创建'
  }
  return `创建于 ${dayjs(value).format('YYYY-MM-DD')}`
}

const getOwnerName = (app: API.AppVO) => {
  return app.user?.userName || app.user?.userAccount || 'NoCode 用户'
}
</script>

<template>
  <section class="app-gallery">
    <div class="gallery-header">
      <div>
        <h2>{{ title }}</h2>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
      <a-input-search v-if="searchable" v-model:value="keyword" class="gallery-search" :placeholder="placeholder"
        allow-clear @search="emit('search', keyword.trim())" />
    </div>

    <a-spin :spinning="loading">
      <div v-if="hasApps" class="app-grid">
        <button v-for="app in apps" :key="app.id" class="app-card" type="button" @click="emit('open', app)">
          <div class="cover-wrap">
            <img v-if="app.cover" class="cover-img" :src="app.cover" :alt="app.appName || '应用封面'" />
            <div v-else class="cover-placeholder">
              <AppstoreOutlined />
            </div>
          </div>
          <div class="app-info">
            <div class="app-main">
              <h3>{{ app.appName || '未命名应用' }}</h3>
              <p>{{ formatTime(app.createTime) }}</p>
            </div>
            <div class="owner-line">
              <a-avatar :src="app.user?.userAvatar" :size="28">
                <template #icon>
                  <UserOutlined />
                </template>
              </a-avatar>
              <span>{{ getOwnerName(app) }}</span>
              <a-tag v-if="app.priority === 99" color="purple">精选</a-tag>
            </div>
          </div>
        </button>
      </div>
      <a-empty v-else :description="emptyText" />
    </a-spin>

    <div v-if="total > pageSize" class="gallery-pagination">
      <a-pagination :current="current" :page-size="pageSize" :total="total" show-less-items
        @change="(page: number, size: number) => emit('pageChange', page, size)" />
    </div>
  </section>
</template>

<style scoped>
.app-gallery {
  width: 100%;
}

.gallery-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.gallery-header h2 {
  margin: 0;
  color: #111827;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.25;
}

.gallery-header p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
}

.gallery-search {
  width: 260px;
  flex-shrink: 0;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.app-card {
  padding: 0;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.cover-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  background: #f8fafc;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.app-card:hover .cover-wrap,
.app-card:focus-visible .cover-wrap {
  border-color: #7dd3fc;
  box-shadow: 0 12px 28px rgba(14, 165, 233, 0.16);
  transform: translateY(-2px);
}

.app-card:focus-visible {
  outline: 2px solid #0ea5e9;
  outline-offset: 4px;
  border-radius: 8px;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #94a3b8;
  font-size: 44px;
  background:
    linear-gradient(90deg,
      rgba(255, 255, 255, 0),
      rgba(255, 255, 255, 0.72),
      rgba(255, 255, 255, 0)),
    #f1f5f9;
}

.app-info {
  padding: 12px 2px 0;
}

.app-main h3 {
  margin: 0;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-main p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.owner-line {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  margin-top: 12px;
  color: #475569;
  font-size: 13px;
}

.owner-line span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gallery-pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 900px) {
  .app-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .gallery-header {
    align-items: stretch;
    flex-direction: column;
  }

  .gallery-header h2 {
    font-size: 24px;
  }

  .gallery-search {
    width: 100%;
  }

  .app-grid {
    grid-template-columns: 1fr;
  }
}
</style>
