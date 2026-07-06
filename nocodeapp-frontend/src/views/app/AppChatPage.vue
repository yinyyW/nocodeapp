<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CloudUploadOutlined,
  DeleteOutlined,
  EyeOutlined,
  InfoCircleOutlined,
  RobotOutlined,
  SaveOutlined,
  SendOutlined,
  UserOutlined,
  UpOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { cancelDeployApp, deleteApp, deployApp, getApp, updateApp } from '@/api/appController'
import { getCodeGenTypeLabel } from '@/common/codeGenType'
import MdRenderer from '@/components/MdRenderer.vue'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'


type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  content: string
  loading?: boolean
  createTime?: string
}

const API_BASE_URL = 'http://localhost:8123/api'

const route = useRoute()
const router = useRouter()
const loginStore = useLoginUserStore();

const app = ref<API.AppVO>()
const loading = ref(false)
const generating = ref(false)
const deploying = ref(false)
const detailOpen = ref(false)
const savingName = ref(false)
const hasMoreHistory = ref(false)
const loadingHistroy = ref(false)
const isOwner = ref(false)
const userInput = ref('')
const deployUrl = ref('')
const previewReady = ref(false)
const lastCreateTime = ref<string>()
const messagesBodyRef = ref<HTMLElement>()
const eventSourceRef = ref<EventSource | null>(null)
const messages = ref<ChatMessage[]>([])

const editForm = reactive({
  appName: '',
})

const appId = computed(() => String(route.params.id || ''))
const appName = computed(() => app.value?.appName || `应用 ${appId.value}`)
const previewUrl = computed(() => {
  if (!app.value?.codeGenType || !appId.value) {
    return ''
  }
  return `${API_BASE_URL}/static/${app.value.codeGenType}_${appId.value}/`
})

const scrollToBottom = async () => {
  await nextTick()
  const el = messagesBodyRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

const closeEventSource = () => {
  if (eventSourceRef.value) {
    eventSourceRef.value.close()
    eventSourceRef.value = null
  }
}

const appendAssistantChunk = (messageId: string, chunk: string) => {
  const target = messages.value.find((item) => item.id === messageId)
  if (target) {
    target.content += chunk
    target.loading = false
  }
  scrollToBottom()
}

const refreshApp = async () => {
  const res = await getApp({ id: appId.value })
  if (res?.data?.code === 0 && res.data.data) {
    app.value = res.data.data
    editForm.appName = res.data.data.appName ?? ''
    deployUrl.value = res.data.data.deployKey
      ? `http://localhost/${res.data.data.deployKey}`
      : deployUrl.value
    isOwner.value = res.data.data.userId === loginStore.loginUser.id
    return res.data.data
  }
  throw new Error(res?.data?.message ?? '获取应用信息失败')
}

const finishGenerate = async () => {
  closeEventSource()
  generating.value = false
  try {
    await refreshApp()
  } catch {
    // 生成完成后刷新失败不阻断预览地址拼接。
  }
  previewReady.value = Boolean(previewUrl.value)
  message.success('网站生成完成')
}

const loadChatHistory = async (isLoadMore: boolean = false) => {
  loadingHistroy.value = isLoadMore
  const params: API.listAppChatHistoryParams = {
    appId: appId.value,
    pageSize: 2
  }
  if (isLoadMore && lastCreateTime) {
    params.lastCreateTime = lastCreateTime.value
  }
  try {
    const res = await listAppChatHistory(params)
    if (!res.data.data || res?.data?.code !== 0) {
      throw new Error(res?.data?.message ?? '获取会话消息失败')
    }
    const chatHistories = res.data.data.records || []

    if (chatHistories.length > 0) {
      const chatMessages: ChatMessage[] = chatHistories.map(chat => ({
        id: chat.id || "",
        role: (chat.messageType === 'user' ? 'user' : 'assistant') as 'user' | 'assistant',
        content: chat.message || "",
        createTime: chat.createTime
      })).reverse()
      if (isLoadMore) {
        // 动态更新scrollTop
        const el = messagesBodyRef.value
        const prevScrollHeight = el?.scrollHeight ?? 0
        console.log(`prev scroll height: ${prevScrollHeight}, scrollTop: ${el?.scrollTop}`)
        messages.value.unshift(...chatMessages)
        await nextTick()
        if (el) {
          el.scrollTop = el.scrollHeight - prevScrollHeight
        }
      } else {
        messages.value = chatMessages
        // 已有会话消息自动下拉消息记录至底部
        scrollToBottom()
      }
      lastCreateTime.value = chatHistories[chatHistories.length - 1]?.createTime
      // 是否有更多的消息数据
      const totalRows = Number(res.data.data.totalRow) || chatHistories.length
      hasMoreHistory.value = totalRows > messages.value.length
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '获取会话消息失败')
  } finally {
    loadingHistroy.value = false
  }
}

const sendMessage = async (content: string, auto = false) => {
  const userMessage = content.trim()
  if (!userMessage || generating.value) {
    return
  }
  closeEventSource()
  previewReady.value = false
  generating.value = true

  const now = Date.now()
  const assistantId = `assistant-${now}`
  messages.value.push({
    id: `user-${now}`,
    role: 'user',
    content: userMessage,
  })
  messages.value.push({
    id: assistantId,
    role: 'assistant',
    content: '',
    loading: true,
  })
  userInput.value = ''
  scrollToBottom()

  if (auto) {
    sessionStorage.setItem(`nocodeapp:auto-gen:${appId.value}`, '1')
  }

  const url = `${API_BASE_URL}/app/chat/gen/code?appId=${encodeURIComponent(appId.value)}&userMessage=${encodeURIComponent(userMessage)}`
  const source = new EventSource(url, { withCredentials: true })
  eventSourceRef.value = source

  source.onmessage = (event) => {
    console.log(`onmessage: ${event.data}`);
    try {
      const data = JSON.parse(event.data) as { d?: string }
      appendAssistantChunk(assistantId, data.d ?? '')
    } catch {
      appendAssistantChunk(assistantId, event.data)
    }
  }

  source.addEventListener('done', () => {
    finishGenerate()
  })

  source.onerror = () => {
    closeEventSource()
    generating.value = false
    const target = messages.value.find((item) => item.id === assistantId)
    if (target && !target.content) {
      target.content = '生成过程连接中断，请稍后重试。'
      target.loading = false
    }
    message.error('AI 生成连接异常')
  }
}

const loadAppAndAutoGenerate = async () => {
  if (!appId.value) {
    message.error('应用 ID 无效')
    router.replace('/')
    return ``
  }
  loading.value = true
  try {
    await Promise.all([loadChatHistory(), refreshApp()])
    if (app.value?.initPrompt && isOwner.value && !loadingHistroy.value && !messages.value?.length) {
      await sendMessage(app.value?.initPrompt, true)
    } else if (app.value?.codeGenType) {
      previewReady.value = true
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '获取应用信息失败')
  } finally {
    loading.value = false
  }
}

const handleSend = () => {
  sendMessage(userInput.value)
}

const handleDeploy = async () => {
  deploying.value = true
  try {
    const res = await deployApp({ appId: appId.value })
    if (res?.data?.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      message.success('部署成功')
      await refreshApp()
    } else {
      message.error(res?.data?.message ?? '部署失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '部署异常，请确认网站已生成')
  } finally {
    deploying.value = false
  }
}

const handleCancelDeploy = async () => {
  deploying.value = true
  try {
    const res = await cancelDeployApp({ appId: appId.value })
    if (res?.data?.code === 0 && res.data.data) {
      message.success('取消部署成功')
      deployUrl.value = ""
      await refreshApp()
    } else {
      message.error(res?.data?.message ?? '取消部署失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '取消部署异常')
  } finally {
    deploying.value = false
  }
}

const handleSaveName = async () => {
  const name = editForm.appName.trim()
  if (!name) {
    message.warning('请输入应用名称')
    return
  }
  savingName.value = true
  try {
    const res = await updateApp({ id: appId.value, appName: name })
    if (res?.data?.code === 0 && res.data.data) {
      message.success('应用名称已更新')
      await refreshApp()
    } else {
      message.error(res?.data?.message ?? '保存失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  } finally {
    savingName.value = false
  }
}

const handleDelete = async () => {
  try {
    const res = await deleteApp({ id: appId.value })
    if (res?.data?.code === 0 && res.data.data) {
      message.success('应用已删除')
      router.push('/')
    } else {
      message.error(res?.data?.message ?? '删除失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  }
}

onMounted(loadAppAndAutoGenerate)

onBeforeUnmount(() => {
  closeEventSource()
})
</script>

<template>
  <div class="app-chat-page">
    <header class="workspace-header">
      <div class="app-title-area">
        <a-button type="text" class="back-button" @click="router.push('/')">
          <template #icon>
            <ArrowLeftOutlined />
          </template>
        </a-button>
        <img src="@/assets/logo.jpg" alt="NoCodeApp logo" class="app-logo" />
        <div class="title-text">
          <h1>{{ appName }}</h1>
          <span>{{ getCodeGenTypeLabel(app?.codeGenType) }}</span>
        </div>
      </div>
      <a-space>
        <a-button @click="detailOpen = true">
          <template #icon>
            <InfoCircleOutlined />
          </template>
          应用详情
        </a-button>
        <a-button v-if="app?.deployKey" type="primary" :loading="deploying" @click="handleCancelDeploy">
          <template #icon>
            <CloudUploadOutlined />
          </template>
          取消部署
        </a-button>
        <a-button v-else type="primary" :loading="deploying" @click="handleDeploy">
          <template #icon>
            <CloudUploadOutlined />
          </template>
          部署
        </a-button>
      </a-space>
    </header>

    <main class="workspace-body">
      <section class="chat-panel">
        <div ref="messagesBodyRef" class="messages-body">
          <div v-if="hasMoreHistory" class="messages-load-more-container">
            <div class="load-more-divider">
              <span class="load-more-line"></span>
            </div>
            <span v-if="!loadingHistroy" class="load-more-btn" @click="() => loadChatHistory(true)">
              <UpOutlined /> 加载更多
            </span>
            <a-spin v-else :spinning="loadingHistroy" size="small" />
          </div>
          <a-spin :spinning="loading">
            <a-empty v-if="!messages.length" description="应用加载后会自动发送初始提示词" />
            <div v-for="item in messages" :key="item.id" class="message-row"
              :class="item.role === 'user' ? 'message-row-user' : 'message-row-ai'">
              <a-avatar v-if="item.role === 'assistant'" class="message-avatar">
                <template #icon>
                  <RobotOutlined />
                </template>
              </a-avatar>
              <div class="message-bubble">
                <a-spin v-if="item.loading && !item.content" size="small" />
                <MdRenderer v-else :content="item.content" />
              </div>
              <a-avatar v-if="item.role === 'user'" class="message-avatar user-avatar">
                <template #icon>
                  <UserOutlined />
                </template>
              </a-avatar>
            </div>
          </a-spin>
        </div>

        <div class="composer">
          <a-textarea v-model:value="userInput" :rows="3" :disabled="generating" placeholder="描述越详细，页面越具体，可以一步一步完善生成效果"
            @keydown.ctrl.enter.prevent="handleSend" />
          <div class="composer-footer">
            <a-space>
              <a-button size="small" disabled>上传</a-button>
              <a-button size="small" disabled>编辑</a-button>
              <a-button size="small" disabled>优化</a-button>
            </a-space>
            <a-button type="primary" shape="circle" :disabled="!userInput.trim() || generating" :loading="generating"
              aria-label="发送消息" @click="handleSend">
              <template #icon>
                <SendOutlined />
              </template>
            </a-button>
          </div>
        </div>
      </section>

      <section class="preview-panel">
        <div class="preview-toolbar">
          <div>
            <h2>生成后的网站展示</h2>
            <p>{{ previewReady ? previewUrl : '生成完成后自动展示页面效果' }}</p>
          </div>
          <a-button v-if="previewReady && previewUrl" :href="previewUrl" target="_blank">
            <template #icon>
              <EyeOutlined />
            </template>
            新窗口打开
          </a-button>
        </div>
        <div class="preview-frame-wrap">
          <iframe v-if="previewReady && previewUrl" :src="previewUrl" title="生成网站预览" />
          <div v-else class="preview-empty">
            <RobotOutlined />
            <h3>{{ generating ? '正在生成网站文件' : '等待生成结果' }}</h3>
            <p>AI 回复全部返回后，会在这里加载本地静态预览。</p>
          </div>
        </div>
      </section>
    </main>

    <a-drawer v-model:open="detailOpen" title="应用详情" width="420">
      <a-form layout="vertical">
        <a-form-item label="应用名称">
          <a-input v-model:value="editForm.appName" placeholder="请输入应用名称" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="savingName" @click="handleSaveName">
            <template #icon>
              <SaveOutlined />
            </template>
            保存名称
          </a-button>
        </a-form-item>
      </a-form>

      <a-divider />

      <a-descriptions :column="1" size="small">
        <a-descriptions-item label="应用 ID">{{ app?.id || appId }}</a-descriptions-item>
        <a-descriptions-item label="初始提示词">{{ app?.initPrompt || '-' }}</a-descriptions-item>
        <a-descriptions-item label="生成类型">{{
          getCodeGenTypeLabel(app?.codeGenType)
          }}</a-descriptions-item>
        <a-descriptions-item label="部署地址">
          <a v-if="deployUrl" :href="deployUrl" target="_blank">{{ deployUrl }}</a>
          <span v-else>{{ app?.deployKey ? `http://localhost/${app.deployKey}` : '未部署' }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ app?.createTime ? dayjs(app.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ app?.updateTime ? dayjs(app.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <template #footer>
        <div class="drawer-footer">
          <a-popconfirm title="确定删除该应用吗？" ok-text="删除" cancel-text="取消" @confirm="handleDelete">
            <a-button danger>
              <template #icon>
                <DeleteOutlined />
              </template>
              删除应用
            </a-button>
          </a-popconfirm>
          <a-button @click="detailOpen = false">关闭</a-button>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<style scoped>
.app-chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #f3f6fb;
}

.workspace-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
}

.app-title-area {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 10px;
}

.back-button {
  flex-shrink: 0;
}

.app-logo {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 50%;
  object-fit: cover;
}

.title-text {
  min-width: 0;
}

.title-text h1 {
  max-width: 360px;
  margin: 0;
  overflow: hidden;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.title-text span {
  color: #64748b;
  font-size: 12px;
}

.workspace-body {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 12px;
  flex: 1;
  min-height: 0;
  padding: 12px;
}

.chat-panel,
.preview-panel {
  min-height: 0;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.chat-panel {
  display: flex;
  flex-direction: column;
}

.messages-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 14px;
}

.messages-load-more-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.load-more-divider {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 12px;
}

.load-more-divider span {
  flex: 1;
  height: 1px;
  background: #e5e7eb;
}

.load-more-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  background: #fff;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.load-more-btn:hover {
  border-color: #1677ff;
  color: #1677ff;
  background: #f0f5ff;
}

.load-more-btn:active {
  transform: scale(0.97);
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 14px;
}

.message-row-user {
  justify-content: flex-end;
}

.message-avatar {
  flex-shrink: 0;
  background: #64748b;
}

.user-avatar {
  background: #1677ff;
}

.message-bubble {
  max-width: 78%;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f8fafc;
  color: #334155;
  line-height: 1.65;
  word-break: break-word;
}

.message-row-user .message-bubble {
  background: #eef6ff;
  color: #0f172a;
}


.composer {
  padding: 10px;
  border-top: 1px solid #e5e7eb;
  background: #fff;
}

.composer :deep(textarea) {
  resize: none;
}

.composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
}

.preview-panel {
  display: flex;
  flex-direction: column;
  padding: 10px;
}

.preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 2px 10px;
}

.preview-toolbar h2 {
  margin: 0;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}

.preview-toolbar p {
  max-width: 680px;
  margin: 4px 0 0;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-frame-wrap {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.preview-frame-wrap iframe {
  width: 100%;
  height: 100%;
  border: 0;
  background: #fff;
}

.preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 420px;
  padding: 32px;
  color: #64748b;
  text-align: center;
}

.preview-empty :deep(.anticon) {
  color: #94a3b8;
  font-size: 42px;
}

.preview-empty h3 {
  margin: 18px 0 8px;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
}

.preview-empty p {
  margin: 0;
}

.drawer-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

@media (max-width: 900px) {
  .workspace-header {
    height: auto;
    align-items: flex-start;
    flex-direction: column;
    padding: 12px;
  }

  .workspace-body {
    grid-template-columns: 1fr;
  }

  .chat-panel {
    min-height: 520px;
  }

  .preview-panel {
    min-height: 520px;
  }
}

@media (max-width: 520px) {
  .workspace-body {
    padding: 8px;
  }

  .title-text h1 {
    max-width: 220px;
  }

  .message-bubble {
    max-width: 84%;
  }

  .preview-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
