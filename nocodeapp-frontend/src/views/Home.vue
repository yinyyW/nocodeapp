<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowUpOutlined, PaperClipOutlined, ThunderboltOutlined } from '@ant-design/icons-vue'
import AppGallery from '@/components/AppGallery.vue'
import { addApp, listGoodAppVoByPage, listMyAppVoPage } from '@/api/appController'

const router = useRouter()

const prompt = ref('')
const creating = ref(false)

const quickPrompts = ['波普风电商页面', '企业网站', '电商运营后台', '个人博客']

const myApps = ref<API.AppVO[]>([])
const goodApps = ref<API.AppVO[]>([])
const myLoading = ref(false)
const goodLoading = ref(false)

const myState = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
  keyword: '',
})

const goodState = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
  keyword: '',
})

const fetchMyApps = async () => {
  myLoading.value = true
  try {
    const res = await listMyAppVoPage({
      pageNum: myState.current,
      pageSize: Math.min(myState.pageSize, 20),
      appName: myState.keyword || undefined,
      sortField: 'createTime',
      sortOrder: 'desc',
    })
    if (res?.data?.code === 0 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myState.total = res.data.data.totalRow ? Number(res.data.data.totalRow) : 0
    } else {
      myApps.value = []
      myState.total = 0
    }
  } catch {
    myApps.value = []
    myState.total = 0
  } finally {
    myLoading.value = false
  }
}

const fetchGoodApps = async () => {
  goodLoading.value = true
  try {
    const res = await listGoodAppVoByPage({
      pageNum: goodState.current,
      pageSize: Math.min(goodState.pageSize, 20),
      appName: goodState.keyword || undefined,
      sortField: 'priority',
      sortOrder: 'desc',
    })
    if (res?.data?.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records ?? []
      goodState.total = res.data.data.totalRow ? Number(res.data.data.totalRow) : 0
    } else {
      goodApps.value = []
      goodState.total = 0
      message.error(res?.data?.message ?? '获取精选应用失败')
    }
  } catch (e) {
    goodApps.value = []
    goodState.total = 0
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  } finally {
    goodLoading.value = false
  }
}

const handleCreate = async () => {
  const initPrompt = prompt.value.trim()
  if (!initPrompt) {
    message.warning('请输入你想创建的应用')
    return
  }
  creating.value = true
  try {
    const res = await addApp({ initPrompt })
    if (res?.data?.code === 0 && res.data.data) {
      message.success('应用创建成功')
      router.push(`/app/${res.data.data}/chat`)
    } else {
      message.error(res?.data?.message ?? '创建应用失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  } finally {
    creating.value = false
  }
}

const handlePromptShortcut = (value: string) => {
  prompt.value = `创建一个${value}`
}

const handleOpenApp = (app: API.AppVO) => {
  if (app.id) {
    router.push(`/app/${app.id}/chat`)
  }
}

onMounted(() => {
  fetchMyApps()
  fetchGoodApps()
})
</script>

<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-inner">
        <div class="brand-title">
          <span>一句话</span>
          <img src="@/assets/logo.jpg" alt="NoCodeApp logo" />
          <span>呈所想</span>
        </div>
        <p class="hero-subtitle">与 AI 对话轻松创建应用和网站</p>

        <div class="prompt-panel">
          <a-textarea v-model:value="prompt" class="prompt-input" placeholder="使用 NoCode 创建一个高效的小工具，帮我计算......"
            :bordered="false" :rows="4" @keydown.ctrl.enter.prevent="handleCreate" />
          <div class="prompt-actions">
            <a-space>
              <a-button shape="round">
                <template #icon>
                  <PaperClipOutlined />
                </template>
                上传
              </a-button>
              <a-button shape="round" disabled>
                <template #icon>
                  <ThunderboltOutlined />
                </template>
                优化
              </a-button>
            </a-space>
            <a-button type="primary" shape="circle" :loading="creating" aria-label="创建应用" @click="handleCreate">
              <template #icon>
                <ArrowUpOutlined />
              </template>
            </a-button>
          </div>
        </div>

        <div class="quick-prompts">
          <button v-for="item in quickPrompts" :key="item" type="button" @click="handlePromptShortcut(item)">
            {{ item }}
          </button>
        </div>
      </div>
    </section>

    <main class="gallery-shell">
      <AppGallery title="我的作品" subtitle="你已经创建的应用会展示在这里" :apps="myApps" :loading="myLoading" :total="myState.total"
        :current="myState.current" :page-size="myState.pageSize" empty-text="登录后创建的应用会出现在这里" @search="
          (keyword) => {
            myState.keyword = keyword
            myState.current = 1
            fetchMyApps()
          }
        " @page-change="
          (page, pageSize) => {
            myState.current = page
            myState.pageSize = Math.min(pageSize, 20)
            fetchMyApps()
          }
        " @open="handleOpenApp" />

      <AppGallery title="精选案例" subtitle="来自官方和社区的优秀应用" :apps="goodApps" :loading="goodLoading" :total="goodState.total"
        :current="goodState.current" :page-size="goodState.pageSize" empty-text="暂无精选案例" @search="
          (keyword) => {
            goodState.keyword = keyword
            goodState.current = 1
            fetchGoodApps()
          }
        " @page-change="
          (page, pageSize) => {
            goodState.current = page
            goodState.pageSize = Math.min(pageSize, 20)
            fetchGoodApps()
          }
        " @open="handleOpenApp" />
    </main>
  </div>
</template>

<style scoped>
.home-page {
  margin: -24px -24px 0;
  background:
    radial-gradient(circle at 22% 20%, rgba(255, 255, 255, 0.96) 0, rgba(255, 255, 255, 0) 30%),
    linear-gradient(135deg, #fbfbf5 0%, #e9fbf5 48%, #78d8e3 72%, #6aa5ff 100%);
}

.hero-section {
  min-height: 520px;
  padding: 70px 24px 58px;
}

.hero-inner {
  max-width: 860px;
  margin: 0 auto;
  text-align: center;
}

.brand-title {
  display: inline-flex;
  align-items: center;
  gap: 18px;
  color: #111827;
  font-size: 40px;
  font-weight: 800;
  line-height: 1.2;
}

.brand-title img {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  object-fit: cover;
}

.hero-subtitle {
  margin: 22px 0 38px;
  color: #6b7280;
  font-size: 16px;
  letter-spacing: 0;
}

.prompt-panel {
  overflow: hidden;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.1);
}

.prompt-input {
  padding: 18px 20px 8px;
  color: #111827;
  font-size: 16px;
  resize: none;
}

.prompt-input :deep(textarea) {
  resize: none;
}

.prompt-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 16px 16px;
}

.prompt-actions :deep(.ant-btn-round) {
  color: #475569;
  border-color: #eef2f7;
  background: #f8fafc;
}

.prompt-actions :deep(.ant-btn-circle) {
  width: 36px;
  height: 36px;
  background: #8b949e;
  border-color: #8b949e;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 22px;
}

.quick-prompts button {
  min-height: 34px;
  padding: 0 16px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
  color: #64748b;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    background 0.2s ease;
}

.quick-prompts button:hover,
.quick-prompts button:focus-visible {
  border-color: #38bdf8;
  background: #fff;
  color: #0f172a;
}

.gallery-shell {
  display: flex;
  flex-direction: column;
  gap: 56px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 42px 24px 72px;
  border-radius: 28px 28px 0 0;
  background: #fff;
}

@media (max-width: 768px) {
  .home-page {
    margin: -16px -16px 0;
  }

  .hero-section {
    min-height: auto;
    padding: 48px 16px 40px;
  }

  .brand-title {
    gap: 12px;
    font-size: 30px;
  }

  .brand-title img {
    width: 38px;
    height: 38px;
  }

  .prompt-actions {
    align-items: stretch;
  }

  .gallery-shell {
    gap: 42px;
    padding: 32px 16px 56px;
    border-radius: 22px 22px 0 0;
  }
}
</style>
