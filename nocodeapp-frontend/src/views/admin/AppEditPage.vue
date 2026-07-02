<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getAppVoByIdByAdmin, updateAppByAdmin } from '@/api/appController'
import { APP_PRIORITY_OPTIONS } from '@/common/appPriority'
import { getCodeGenTypeLabel } from '@/common/codeGenType'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const formRef = ref()
const app = ref<API.AppVO>()

const formData = reactive({
  appName: '',
  cover: '',
  priority: 0 as number | undefined,
})

const rules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'change' }],
}

const appId = computed(() => String(route.params.id))

const fetchApp = async () => {
  if (!appId.value) {
    message.error('应用 ID 无效')
    router.replace('/admin/appManage')
    return
  }
  loading.value = true
  try {
    const res = await getAppVoByIdByAdmin({ id: appId.value })
    if (res?.data?.code === 0 && res.data.data) {
      app.value = res.data.data
      formData.appName = res.data.data.appName ?? ''
      formData.cover = res.data.data.cover ?? ''
      formData.priority = res.data.data.priority ?? 0
    } else {
      message.error(res?.data?.message ?? '获取应用详情失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    const res = await updateAppByAdmin({
      id: appId.value,
      appName: formData.appName,
      cover: formData.cover || undefined,
      priority: formData.priority,
    })
    if (res?.data?.code === 0 && res.data.data) {
      message.success('保存成功')
      router.push('/admin/appManage')
    } else {
      message.error(res?.data?.message ?? '保存失败')
    }
  } catch (e) {
    message.error((e as Error)?.message ?? '网络异常，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(fetchApp)
</script>

<template>
  <div class="app-edit-page">
    <div class="page-header">
      <a-button @click="router.push('/admin/appManage')">
        <template #icon>
          <ArrowLeftOutlined />
        </template>
        返回列表
      </a-button>
      <h1>编辑应用</h1>
    </div>

    <a-spin :spinning="loading">
      <div class="edit-layout">
        <a-card class="form-card" :bordered="false">
          <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical" autocomplete="off">
            <a-form-item label="应用名称" name="appName">
              <a-input v-model:value="formData.appName" placeholder="请输入应用名称" allow-clear />
            </a-form-item>
            <a-form-item label="应用封面" name="cover">
              <a-input v-model:value="formData.cover" placeholder="请输入封面图片地址" allow-clear />
            </a-form-item>
            <a-form-item label="优先级">
              <a-select v-model:value="formData.priority" placeholder="请选择优先级" allow-clear style="width: 130px"
                :options="APP_PRIORITY_OPTIONS" />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" :loading="saving" @click="handleSave">
                  <template #icon>
                    <SaveOutlined />
                  </template>
                  保存
                </a-button>
                <a-button @click="router.push('/admin/appManage')">取消</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-card>

        <a-card class="detail-card" :bordered="false" title="应用详情">
          <div class="cover-preview">
            <img v-if="formData.cover" :src="formData.cover" alt="应用封面预览" />
            <span v-else>暂无封面</span>
          </div>
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="应用 ID">{{ app?.id || appId }}</a-descriptions-item>
            <a-descriptions-item label="用户 ID">{{ app?.userId || '-' }}</a-descriptions-item>
            <a-descriptions-item label="生成类型">{{
              app?.codeGenType ? getCodeGenTypeLabel(app?.codeGenType) : '-'
            }}</a-descriptions-item>
            <a-descriptions-item label="部署标识">{{
              app?.deployKey || '未部署'
            }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">
              {{ app?.createTime ? dayjs(app.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="更新时间">
              {{ app?.updateTime ? dayjs(app.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.app-edit-page {
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.page-header h1 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 700;
}

.edit-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
}

.form-card,
.detail-card {
  border-radius: 8px;
}

.field-tip {
  margin-left: 12px;
  color: #64748b;
  font-size: 13px;
}

.cover-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  margin-bottom: 18px;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  background: #f8fafc;
  color: #94a3b8;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 900px) {
  .edit-layout {
    grid-template-columns: 1fr;
  }
}
</style>
