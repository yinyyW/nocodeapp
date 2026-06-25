<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue"
import { message } from "ant-design-vue"
import {
  SearchOutlined,
  ReloadOutlined,
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
} from "@ant-design/icons-vue"
import { deleteUsingPost, userVoList, add, update } from "@/api/userController"
import type { SorterResult } from "ant-design-vue/es/table/interface"
import { USER_ROLE } from "@/common/userRole"
import dayjs from "dayjs"

const dataSource = ref<API.UserVO[]>([])
const loading = ref(false)
const total = ref(0)

// ---- search ----
const searchForm = reactive({
  id: undefined as number | undefined,
  userAccount: "",
  userName: "",
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
})

const sortInfo = reactive({
  field: "",
  order: "",
})

const paginationConfig = computed(() => ({
  current: pagination.current,
  pageSize: pagination.pageSize,
  total: total.value,
  showSizeChanger: true,
  pageSizeOptions: ["10", "20", "50"],
  showTotal: (t: number) => `共${t} 条`,
}))

// ---- modal ----
const modalVisible = ref(false)
const modalTitle = ref("")
const modalLoading = ref(false)
const formRef = ref()
const editingId = ref<number | null>(null)

const formData: { userName: string; userAvatar: string; userProfile: string; userRole: "" | USER_ROLE } = reactive({
  userName: "",
  userAvatar: "",
  userProfile: "",
  userRole: "" as "" | USER_ROLE,
})

const formRules = {
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  userRole: [{ required: true, message: "请选择角色", trigger: "change" }],
}

const openAddModal = () => {
  modalTitle.value = "添加用户"
  editingId.value = null
  formData.userName = ""
  formData.userAvatar = ""
  formData.userProfile = ""
  formData.userRole = "" as "" | USER_ROLE
  modalVisible.value = true
}

const openUpdateModal = (record: API.UserVO) => {
  console.log(`id: ${record.id}`)
  modalTitle.value = "更新用户信息"

  editingId.value = record.id ?? null
  formData.userName = record.userName ?? ""
  formData.userAvatar = record.userAvatar ?? ""
  formData.userProfile = record.userProfile ?? ""
  formData.userRole = (record.userRole ?? "") as "" | USER_ROLE
  modalVisible.value = true
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  modalLoading.value = true
  try {
    let res
    if (editingId.value) {
      res = await update({
        id: editingId.value,
        userName: formData.userName,
        userAvatar: formData.userAvatar || undefined,
        userProfile: formData.userProfile || undefined,
        userRole: (formData.userRole || undefined) as USER_ROLE | undefined,
      })
    } else {
      res = await add({
        userName: formData.userName,
        userAvatar: formData.userAvatar || undefined,
        userProfile: formData.userProfile || undefined,
        userRole: (formData.userRole || undefined) as USER_ROLE | undefined,
      })
    }
    if (res?.data?.code === 0) {
      message.success(editingId.value ? "更新成功" : "添加成功")
      modalVisible.value = false
      fetchData()
    } else {
      message.error(res?.data?.message ?? "操作失败")
    }
  } catch (e) {
    if (e instanceof Error) {
      message.error(e.message)
    } else {
      message.error("网络异常，请稍后重试")
    }
  } finally {
    modalLoading.value = false
  }
}

// ---- data fetching ----
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await userVoList({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      sortField: sortInfo.field || undefined,
      sortOrder: sortInfo.order || undefined,
      id: searchForm.id,
      userAccount: searchForm.userAccount || undefined,
      userName: searchForm.userName || undefined,
    });
    if (res?.data?.code === 0 && res?.data?.data) {
      dataSource.value = res.data.data.records ?? [];
      total.value = res.data.data.totalRow ? Number(res.data.data.totalRow) : 0;
    } else {
      message.error(res?.data?.message ?? "获取用户列表失败");
    }
  } catch (e) {
    if (e instanceof Error) {
      message.error(e.message);
    } else {
      message.error("网络异常，请稍后重试");
    }
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const handleReset = () => {
  searchForm.id = undefined;
  searchForm.userAccount = "";
  searchForm.userName = "";
  pagination.current = 1;
  sortInfo.field = "";
  sortInfo.order = "";
  fetchData();
};

const handleTableChange = (
  pag: { current: number; pageSize: number },
  _filters: any,
  sorter: SorterResult | SorterResult[],
) => {
  const s = Array.isArray(sorter) ? sorter[0] : sorter;
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  if (s?.field && s?.order) {
    sortInfo.field = s.field as string;
    sortInfo.order = s.order === "ascend" ? "asc" : "desc";
  } else {
    sortInfo.field = "";
    sortInfo.order = "";
  }
  fetchData();
};

const handleDelete = async (_record: API.UserVO) => {
  if (!_record.id) {
    message.error("用户id为空")
    return
  }
  try {
    const res = await deleteUsingPost({
      userId: _record.id
    })
    if (res.data.code === 0 && res.data.data) {
      message.success("删除成功")
      fetchData()
    }
  } catch (e) {
    message.error(`删除用户异常：{(e as Error).message}`)
  }
};

const handleUpdate = (record: API.UserVO) => {
  openUpdateModal(record)
};

const columns = [
  { title: "ID", dataIndex: "id", key: "id", width: 100 },
  { title: "账号", dataIndex: "userAccount", key: "userAccount", width: 100 },
  { title: "用户名", dataIndex: "userName", key: "userName", width: 100 },
  { title: "头像", dataIndex: "userAvatar", key: "userAvatar", width: 80 },
  { title: "简介", dataIndex: "userProfile", key: "userProfile", width: 100, ellipsis: true },
  { title: "角色", dataIndex: "userRole", key: "userRole", width: 100 },
  { title: "创建时间", dataIndex: "createTime", key: "createTime", width: 180, sorter: true },
  { title: "更新时间", dataIndex: "updateTime", key: "updateTime", width: 180, sorter: true },
  { title: "操作", key: "action", width: 140, fixed: "right" as const },
];

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="user-manager-page">
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="ID">
          <a-input-number v-model:value="searchForm.id" placeholder="用户ID" :min="1" style="width: 160px" allow-clear />
        </a-form-item>
        <a-form-item label="账号">
          <a-input v-model:value="searchForm.userAccount" placeholder="请输入账号" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.userName" placeholder="请输入用户名" allow-clear style="width: 180px" />
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
          <span>用户列表</span>
          <a-button type="primary" @click="openAddModal">
            <template #icon>
              <PlusOutlined />
            </template>
            添加用户
          </a-button>
        </div>
      </template>
      <a-table :columns="columns" :data-source="dataSource" :loading="loading" :pagination="paginationConfig"
        :row-key="(record: API.UserVO) => record.id ?? 0" @change="handleTableChange" :scroll="{ x: 1100 }">
        <template #bodyCell="{ column, record }: { column: any; record: API.UserVO }">
          <template v-if="column.key === 'userAvatar'">
            <a-image v-if="record.userAvatar" :src="record.userAvatar" :width="32" :height="32"
              style="border-radius: 50%; object-fit: cover" />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ dayjs(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
          </template>
          <template v-else-if="column.key === 'updateTime'">
            {{ dayjs(record.updateTime).format("YYYY-MM-DD HH:mm:ss") }}
          </template>
          <template v-else-if="column.key === 'userRole'">
            <a-tag v-if="record.userRole === USER_ROLE.ADMIN" color="green">管理员</a-tag>
            <a-tag v-else color="blue">普通用户</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                <template #icon>
                  <DeleteOutlined />
                </template>
                删除
              </a-button>
              <a-button type="link" size="small" @click="handleUpdate(record)">
                <template #icon>
                  <EditOutlined />
                </template>
                更新
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Add / Update user modal -->
    <a-modal v-model:open="modalVisible" :title="modalTitle" :confirm-loading="modalLoading" @ok="handleModalOk"
      @cancel="modalVisible = false" :destroy-on-close="true" okText="保存" cancelText="关闭">
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical" autocomplete="off">
        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="formData.userName" placeholder="请输入用户名" allow-clear />
        </a-form-item>
        <a-form-item label="头像" name="userAvatar">
          <a-input v-model:value="formData.userAvatar" placeholder="请输入头像图片地址" allow-clear />
        </a-form-item>
        <a-form-item label="简介" name="userProfile">
          <a-textarea v-model:value="formData.userProfile" placeholder="请输入简介" :rows="3" allow-clear />
        </a-form-item>
        <a-form-item label="角色" name="userRole">
          <a-select v-model:value="formData.userRole" placeholder="请选择角色">
            <a-select-option :value="USER_ROLE.USER">普通用户</a-select-option>
            <a-select-option :value="USER_ROLE.ADMIN">管理员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.user-manager-page {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.table-card {
  border-radius: 8px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
