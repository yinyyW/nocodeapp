<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { SorterResult } from 'ant-design-vue/es/table/interface'
import {
  DeleteOutlined,
  EyeOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'

const router = useRouter()

const dataSource = ref<API.ChatHistory[]>([])
const loading = ref(false)
const total = ref(0)

const searchForm = reactive({
  id: undefined as string | undefined,
  message: '',
  messageType: '',
  appId: undefined as number | undefined,
  userId: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
})

const sortInfo = reactive({
  field: '',
  order: '',
})

const paginationConfig = computed(() => ({
  current: pagination.current,
  pageSize: pagination.pageSize,
  total: total.value,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50', '100'],
  showTotal: (t: number) => `共${t} 条`,
}))

const messageTypeOptions = [
  { value: 'user', label: '用户' },
  { value: 'assistant', label: 'AI' },
]

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 90, sorter: true },
  { title: '消息内容', dataIndex: 'message', key: 'message', width: 320, ellipsis: true },
  { title: '消息类型', dataIndex: 'messageType', key: 'messageType', width: 100 },
  { title: '应用ID', dataIndex: 'appId', key: 'appId', width: 100, sorter: true },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 100, sorter: true },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170, sorter: true },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 170 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const },
]

const buildQuery = (): API.ChatHistoryQueryRequest => ({
  pageNum: pagination.current,
  pageSize: pagination.pageSize,
  sortField: sortInfo.field || undefined,
  sortOrder: sortInfo.order || undefined,
  id: searchForm.id,
  message: searchForm.message || undefined,
  messageType: searchForm.messageType || undefined,
  appId: searchForm.appId,
  userId: searchForm.userId,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin(buildQuery())
    if (res?.data?.code === 0 && res.data.data) {
      dataSource.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ? Number(res.data.data.totalRow) : 0
    } else {
      message.error(res?.data?.message ?? '获取对话列表失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.id = undefined
  searchForm.message = ''
  searchForm.messageType = ''
  searchForm.appId = undefined
  searchForm.userId = undefined
  pagination.current = 1
  sortInfo.field = ''
  sortInfo.order = ''
  fetchData()
}

const handleTableChange = (
  pag: { current?: number; pageSize?: number },
  _filters: unknown,
  sorter: SorterResult<API.ChatHistory> | SorterResult<API.ChatHistory>[],
) => {
  const s = Array.isArray(sorter) ? sorter[0] : sorter
  pagination.current = pag.current ?? 1
  pagination.pageSize = pag.pageSize ?? 10
  if (s?.field && s?.order) {
    sortInfo.field = s.field as string
    sortInfo.order = s.order === 'ascend' ? 'asc' : 'desc'
  } else {
    sortInfo.field = ''
    sortInfo.order = ''
  }
  fetchData()
}

const handleViewApp = (record: API.ChatHistory) => {
  if (!record.appId) {
    message.error('应用 ID 为空')
    return
  }
  router.push(`/app/${record.appId}/chat`)
}

onMounted(fetchData)
</script>

<template>
  <div class="chat-manager-page">
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="ID">
          <a-input v-model:value="searchForm.id" placeholder="对话ID" style="width: 140px" allow-clear />
        </a-form-item>
        <a-form-item label="消息内容">
          <a-input v-model:value="searchForm.message" placeholder="请输入消息内容" allow-clear style="width: 200px" />
        </a-form-item>
        <a-form-item label="消息类型">
          <a-select v-model:value="searchForm.messageType" placeholder="请选择消息类型" allow-clear style="width: 130px"
            :options="messageTypeOptions" />
        </a-form-item>
        <a-form-item label="应用ID">
          <a-input-number v-model:value="searchForm.appId" placeholder="应用ID" :min="1" style="width: 130px"
            allow-clear />
        </a-form-item>
        <a-form-item label="用户ID">
          <a-input-number v-model:value="searchForm.userId" placeholder="用户ID" :min="1" style="width: 130px"
            allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <SearchOutlined />
              </template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="table-card" :bordered="false">
      <template #title>
        <div class="table-header">
          <span>对话列表</span>
        </div>
      </template>
      <a-table :columns="columns" :data-source="dataSource" :loading="loading" :pagination="paginationConfig"
        :row-key="(record: API.ChatHistory) => record.id ?? ''" :scroll="{ x: 1200 }" @change="handleTableChange">
        <template #bodyCell="{ column, record }: { column: any; record: API.ChatHistory }">
          <template v-if="column.key === 'message'">
            <a-tooltip :title="record.message">
              <span class="message-cell">{{ record.message }}</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'messageType'">
            <a-tag v-if="record.messageType === 'user'" color="blue">用户</a-tag>
            <a-tag v-else-if="record.messageType === 'assistant'" color="green">AI</a-tag>
            <span v-else>{{ record.messageType }}</span>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ record.createTime ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
          <template v-else-if="column.key === 'updateTime'">
            {{ record.updateTime ? dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleViewApp(record)">
              <template #icon>
                <EyeOutlined />
              </template>
              查看对话
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<style scoped>
.chat-manager-page {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.search-form {
  row-gap: 12px;
}

.table-card {
  border-radius: 8px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.message-cell {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>