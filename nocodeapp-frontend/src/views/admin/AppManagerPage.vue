<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { SorterResult } from 'ant-design-vue/es/table/interface'
import {
  DeleteOutlined,
  EditOutlined,
  ReloadOutlined,
  SearchOutlined,
  StarFilled,
  StarOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { deleteAppByAdmin, listAppVoByPageByAdmin, updateAppByAdmin } from '@/api/appController'
import { APP_PRIORITY, APP_PRIORITY_OPTIONS, getAppPriorityLabel } from '@/common/appPriority'
import { CODE_GEN_TYPE_OPTIONS, getCodeGenTypeLabel } from '@/common/codeGenType'

const router = useRouter()

const dataSource = ref<API.AppVO[]>([])
const loading = ref(false)
const total = ref(0)

const searchForm = reactive({
  id: undefined as string | undefined,
  appName: '',
  codeGenType: '',
  priority: undefined as number | undefined,
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

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 90, sorter: true },
  { title: '封面', dataIndex: 'cover', key: 'cover', width: 110 },
  { title: '应用名称', dataIndex: 'appName', key: 'appName', width: 180, ellipsis: true },
  { title: '初始提示词', dataIndex: 'initPrompt', key: 'initPrompt', width: 220, ellipsis: true },
  { title: '生成类型', dataIndex: 'codeGenType', key: 'codeGenType', width: 110 },
  { title: '部署标识', dataIndex: 'deployKey', key: 'deployKey', width: 140, ellipsis: true },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 100, sorter: true },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 170 },
  { title: '操作', key: 'action', width: 240, fixed: 'right' as const },
]

const buildQuery = (): API.AppQueryRequest => ({
  pageNum: pagination.current,
  pageSize: pagination.pageSize,
  sortField: sortInfo.field || undefined,
  sortOrder: sortInfo.order || undefined,
  id: searchForm.id,
  appName: searchForm.appName || undefined,
  codeGenType: searchForm.codeGenType || undefined,
  priority: searchForm.priority,
  userId: searchForm.userId,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin(buildQuery())
    if (res?.data?.code === 0 && res.data.data) {
      dataSource.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ? Number(res.data.data.totalRow) : 0
    } else {
      message.error(res?.data?.message ?? '获取应用列表失败')
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
  searchForm.appName = ''
  searchForm.codeGenType = ''
  searchForm.priority = undefined
  searchForm.userId = undefined
  pagination.current = 1
  sortInfo.field = ''
  sortInfo.order = ''
  fetchData()
}

const handleTableChange = (
  pag: { current?: number; pageSize?: number },
  _filters: unknown,
  sorter: SorterResult<API.AppVO> | SorterResult<API.AppVO>[],
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

const handleEdit = (record: API.AppVO) => {
  if (!record.id) {
    message.error('应用 ID 为空')
    return
  }
  router.push(`/admin/appEdit/${record.id}`)
}

const handleDelete = async (record: API.AppVO) => {
  if (!record.id) {
    message.error('应用 ID 为空')
    return
  }
  try {
    const res = await deleteAppByAdmin({ id: record.id })
    if (res?.data?.code === 0 && res.data.data) {
      message.success('删除成功')
      fetchData()
    } else {
      message.error(res?.data?.message ?? '删除失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  }
}

const handleGood = async (record: API.AppVO, priority: APP_PRIORITY) => {
  if (!record.id) {
    message.error('应用 ID 为空')
    return
  }
  try {
    const res = await updateAppByAdmin({ id: record.id, priority })
    if (res?.data?.code === 0 && res.data.data) {
      message.success('已设为精选')
      fetchData()
    } else {
      message.error(res?.data?.message ?? '设置精选失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="app-manager-page">
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="ID">
          <a-input-number v-model:value="searchForm.id" placeholder="应用ID" :min="1" style="width: 140px" allow-clear />
        </a-form-item>
        <a-form-item label="应用名称">
          <a-input v-model:value="searchForm.appName" placeholder="请输入应用名称" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="生成类型">
          <a-select v-model:value="searchForm.codeGenType" placeholder="请选择生成类型" allow-clear style="width: 150px"
            :options="CODE_GEN_TYPE_OPTIONS" />
        </a-form-item>
        <a-form-item label="优先级">
          <a-select v-model:value="searchForm.priority" placeholder="请选择优先级" allow-clear style="width: 130px"
            :options="APP_PRIORITY_OPTIONS" />
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
          <span>应用列表</span>
        </div>
      </template>
      <a-table :columns="columns" :data-source="dataSource" :loading="loading" :pagination="paginationConfig"
        :row-key="(record: API.AppVO) => record.id ?? 0" :scroll="{ x: 1580 }" @change="handleTableChange">
        <template #bodyCell="{ column, record }: { column: any; record: API.AppVO }">
          <template v-if="column.key === 'cover'">
            <a-image v-if="record.cover" :src="record.cover" :width="64" :height="40"
              style="border-radius: 6px; object-fit: cover" />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'priority'">
            <a-tag :color="record.priority === APP_PRIORITY.GOOD ? 'purple' : 'default'">{{
              getAppPriorityLabel(record.priority)
            }}</a-tag>
          </template>
          <template v-else-if="column.key === 'codeGenType'">
            {{ getCodeGenTypeLabel(record.codeGenType) }}
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ record.createTime ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
          <template v-else-if="column.key === 'updateTime'">
            {{ record.updateTime ? dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon>
                  <EditOutlined />
                </template>
                编辑
              </a-button>
              <a-popconfirm title="确定删除该应用吗？" ok-text="删除" cancel-text="取消" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>
                  <template #icon>
                    <DeleteOutlined />
                  </template>
                  删除
                </a-button>
              </a-popconfirm>
              <a-button v-if="record.priority !== APP_PRIORITY.GOOD" type="link" size="small"
                @click="handleGood(record, APP_PRIORITY.GOOD)">
                <template #icon>
                  <StarOutlined />
                </template>
                精选
              </a-button>
              <a-button v-else type="link" size="small" @click="handleGood(record, APP_PRIORITY.DEFAULT)">
                <template #icon>
                  <StarFilled />
                </template>
                取消精选
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<style scoped>
.app-manager-page {
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
</style>
