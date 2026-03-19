<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '../components/Layout.vue'
import {
  getGuests,
  getGuestById,
  createGuest,
  updateGuest,
  deleteGuest,
  getGuestBookings
} from '@/api/guest'
import type { GuestResponse, GuestRequest, BookingSummary } from '@/types/guest'
import { getUser } from '@/utils/auth'

const searchQuery = ref('')

// 权限控制
const isAdmin = computed(() => getUser()?.role === 'ADMIN')
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const total = ref(0)
const totalPages = ref(0)

const guests = ref<GuestResponse[]>([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentGuest = ref<GuestResponse | null>(null)

// 编辑对话框
const editDialogVisible = ref(false)
const editFormRef = ref()
const isEditing = ref(false)
const editForm = ref<GuestRequest>({
  name: '',
  email: '',
  phone: '',
  country: '',
  status: 'ACTIVE',
  lastStay: undefined
})

// 表单验证规则
const editRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [{ required: true, message: '请输入电话号码', trigger: 'blur' }],
  country: [{ required: true, message: '请输入国家/地区', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 预订历史对话框
const bookingsDialogVisible = ref(false)
const bookings = ref<BookingSummary[]>([])
const bookingsLoading = ref(false)

// 加载客户列表
const loadGuests = async () => {
  loading.value = true
  try {
    const response = await getGuests({
      page: currentPage.value - 1,  // 后端页码从0开始
      size: pageSize.value,
      search: searchQuery.value || undefined
    })

    if (response.code === 200 && response.data) {
      guests.value = response.data.guests || []
      total.value = response.data.total || 0
      totalPages.value = response.data.totalPages || 0
    }
  } catch (error) {
    console.error('Load guests error:', error)
    ElMessage.error('加载客户列表失败')
  } finally {
    loading.value = false
  }
}

// 防抖搜索
let searchTimer: NodeJS.Timeout | null = null
watch(searchQuery, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadGuests()
  }, 300)
})

// 重置搜索
const resetSearch = () => {
  searchQuery.value = ''
  currentPage.value = 1
  loadGuests()
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadGuests()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadGuests()
}

// 获取状态标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'VIP':
      return 'warning'
    case 'ACTIVE':
      return 'success'
    case 'INACTIVE':
      return 'info'
    default:
      return 'info' as const
  }
}

// 获取状态显示文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'VIP':
      return 'VIP'
    case 'ACTIVE':
      return '活跃'
    case 'INACTIVE':
      return '不活跃'
    default:
      return status
  }
}

// 查看详情
const handleViewProfile = async (guest: GuestResponse) => {
  try {
    const response = await getGuestById(guest.id)
    if (response.code === 200 && response.data) {
      currentGuest.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取客户详情失败')
  }
}

// 打开编辑对话框
const handleEdit = (guest: GuestResponse) => {
  isEditing.value = true
  editForm.value = {
    name: guest.name,
    email: guest.email,
    phone: guest.phone,
    country: guest.country,
    status: guest.status,
    lastStay: guest.lastStay
  }
  currentGuest.value = guest
  editDialogVisible.value = true
}

// 添加新客户
const handleAdd = () => {
  isEditing.value = false
  editForm.value = {
    name: '',
    email: '',
    phone: '',
    country: '',
    status: 'ACTIVE',
    lastStay: undefined
  }
  editDialogVisible.value = true
}

// 保存客户
const saveGuest = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()
    if (isEditing.value && currentGuest.value) {
      const response = await updateGuest(currentGuest.value.id, editForm.value)
      if (response.code === 200) {
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        loadGuests()
      }
    } else {
      const response = await createGuest(editForm.value)
      if (response.code === 200) {
        ElMessage.success('创建成功')
        editDialogVisible.value = false
        loadGuests()
      }
    }
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(isEditing.value ? '更新失败' : '创建失败')
    }
  }
}

// 删除客户
const handleDelete = async (guest: GuestResponse) => {
  try {
    await ElMessageBox.confirm(`确定要删除客户 "${guest.name}" 吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await deleteGuest(guest.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadGuests()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 查看预订历史
const handleViewBookings = async (guest: GuestResponse) => {
  bookingsLoading.value = true
  bookingsDialogVisible.value = true
  currentGuest.value = guest
  try {
    const response = await getGuestBookings(guest.id)
    if (response.code === 200 && response.data) {
      bookings.value = response.data
    } else {
      bookings.value = []
    }
  } catch (error) {
    bookings.value = []
  } finally {
    bookingsLoading.value = false
  }
}

// 获取预订状态文本
const getBookingStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    CHECKED_IN: '已入住',
    CHECKED_OUT: '已退房',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

// 获取预订状态类型
const getBookingStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'warning',
    CONFIRMED: 'success',
    CHECKED_IN: 'primary',
    CHECKED_OUT: 'info',
    CANCELLED: 'danger'
  }
  return typeMap[status] || ''
}

onMounted(() => {
  loadGuests()
})
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">客户管理</h1>
          <p class="text-sm text-gray-500 mt-1">查看并管理您的酒店客户资料</p>
        </div>
        <el-button type="primary" size="large" :icon="'Plus'" @click="handleAdd">
          添加新客户
        </el-button>
      </header>

      <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 mb-6">
        <div class="max-w-md flex gap-2">
          <el-input
            v-model="searchQuery"
            placeholder="按姓名或邮箱搜索客户..."
            :prefix-icon="'Search'"
            clearable
            @clear="resetSearch"
          />
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-table :data="guests" v-loading="loading" style="width: 100%">
          <el-table-column label="客户" min-width="200">
            <template #default="scope">
              <div class="flex items-center gap-3">
                <el-avatar :size="32" class="bg-blue-100 text-blue-600 font-bold">
                  {{ scope.row.name[0] }}
                </el-avatar>
                <div>
                  <div class="font-bold text-gray-900">{{ scope.row.name }}</div>
                  <div class="text-xs text-gray-500">{{ scope.row.email }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="电话" width="160" />
          <el-table-column prop="country" label="国家/地区" width="120" />
          <el-table-column prop="totalBookings" label="预订次数" width="100" align="center" />
          <el-table-column prop="lastStay" label="上次入住" width="120">
            <template #default="scope">
              {{ scope.row.lastStay || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" effect="light" round>
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="right" width="120">
            <template #default="scope">
              <el-dropdown trigger="click">
                <el-button link :icon="'MoreFilled'" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :icon="'View'" @click="handleViewProfile(scope.row)">
                      查看资料
                    </el-dropdown-item>
                    <el-dropdown-item :icon="'Edit'" @click="handleEdit(scope.row)">
                      编辑详情
                    </el-dropdown-item>
                    <el-dropdown-item :icon="'Calendar'" @click="handleViewBookings(scope.row)">
                      预订历史
                    </el-dropdown-item>
                    <el-dropdown-item v-if="isAdmin" divided :icon="'Delete'" @click="handleDelete(scope.row)">
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <div class="p-4 border-t border-gray-100 flex justify-between items-center">
          <div class="text-sm text-gray-500">
            共 {{ total }} 条记录
          </div>
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="sizes, prev, pager, next"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="客户详情" width="500px">
      <div v-if="currentGuest" class="space-y-4">
        <div class="flex items-center gap-4 pb-4 border-b">
          <el-avatar :size="64" class="bg-blue-100 text-blue-600 font-bold text-2xl">
            {{ currentGuest.name[0] }}
          </el-avatar>
          <div>
            <h3 class="text-lg font-bold text-gray-900">{{ currentGuest.name }}</h3>
            <el-tag :type="getStatusType(currentGuest.status)" class="mt-1">
              {{ getStatusText(currentGuest.status) }}
            </el-tag>
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div>
            <label class="text-gray-500">邮箱</label>
            <p class="font-medium">{{ currentGuest.email }}</p>
          </div>
          <div>
            <label class="text-gray-500">电话</label>
            <p class="font-medium">{{ currentGuest.phone }}</p>
          </div>
          <div>
            <label class="text-gray-500">国家/地区</label>
            <p class="font-medium">{{ currentGuest.country }}</p>
          </div>
          <div>
            <label class="text-gray-500">预订次数</label>
            <p class="font-medium">{{ currentGuest.totalBookings }} 次</p>
          </div>
          <div>
            <label class="text-gray-500">上次入住</label>
            <p class="font-medium">{{ currentGuest.lastStay || '-' }}</p>
          </div>
          <div>
            <label class="text-gray-500">注册时间</label>
            <p class="font-medium">{{ new Date(currentGuest.createdAt).toLocaleDateString('zh-CN') }}</p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEditing ? '编辑客户' : '添加客户'" width="500px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="国家/地区" prop="country">
          <el-input v-model="editForm.country" placeholder="请输入国家/地区" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择状态">
            <el-option label="VIP" value="VIP" />
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="不活跃" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="上次入住">
          <el-date-picker
            v-model="editForm.lastStay"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGuest">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预订历史对话框 -->
    <el-dialog v-model="bookingsDialogVisible" title="预订历史" width="700px">
      <div v-if="currentGuest" class="mb-4">
        <span class="text-gray-500">客户：</span>
        <span class="font-medium">{{ currentGuest.name }}</span>
      </div>
      <el-table :data="bookings" v-loading="bookingsLoading">
        <el-table-column prop="id" label="预订号" width="100" />
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="退房日期" width="120" />
        <el-table-column prop="totalPrice" label="总价" width="100">
          <template #default="scope">
            ¥{{ scope.row.totalPrice }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getBookingStatusType(scope.row.status)" size="small">
              {{ getBookingStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评价" width="150">
          <template #default="scope">
            <template v-if="scope.row.reviewed">
              <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900" />
              <div v-if="scope.row.comment" class="text-xs text-gray-500 mt-1">{{ scope.row.comment }}</div>
            </template>
            <template v-else>
              <span class="text-gray-400 text-sm">未评价</span>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!bookingsLoading && bookings.length === 0" class="text-center py-8 text-gray-500">
        暂无预订数据
      </div>
    </el-dialog>
  </Layout>
</template>
