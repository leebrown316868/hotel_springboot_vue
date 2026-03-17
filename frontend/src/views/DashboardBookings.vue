<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '../components/Layout.vue'
import { getRecentBookings } from '@/api/statistics'
import { getBookingByNumber, cancelBookingByNumber, checkInByNumber, checkOutByNumber, deleteBookingByNumber } from '@/api/booking'
import type { RecentBookingSummary } from '@/types/statistics'
import type { BookingResponse } from '@/types/booking'
import { getUser } from '@/utils/auth'

const router = useRouter()

// 权限控制
const isAdmin = computed(() => getUser()?.role === 'ADMIN')

const bookings = ref<RecentBookingSummary[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 订单详情对话框
const bookingDetailDialog = ref(false)
const selectedBooking = ref<BookingResponse | null>(null)
const loadingDetail = ref(false)

const searchQuery = ref('')
const statusFilter = ref('')

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': 'warning',
    'CONFIRMED': 'primary',
    'CHECKED_IN': 'success',
    'CHECKED_OUT': 'info',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CHECKED_IN': '已入住',
    'CHECKED_OUT': '已退房',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const getRoomTypeName = (type: string): string => {
  const names: Record<string, string> = {
    'SINGLE': '单人间',
    'DOUBLE': '双人间',
    'SUITE': '套房',
    'EXECUTIVE_SUITE': '行政套房',
    'PRESIDENTIAL_SUITE': '总统套房'
  }
  return names[type] || type
}

const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const getInitials = (name: string): string => {
  return name.slice(0, 2).toUpperCase()
}

const getBookingStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    'PENDING': 'amber',
    'CONFIRMED': 'emerald',
    'CHECKED_IN': 'blue',
    'CHECKED_OUT': 'slate',
    'CANCELLED': 'red'
  }
  return colors[status] || 'gray'
}

const loadBookings = async () => {
  loading.value = true
  try {
    // 获取所有订单（使用较大的limit）
    const response = await getRecentBookings(100)
    if (response.data.code === 200 && response.data.data) {
      let filtered = response.data.data

      // 前端过滤
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter((b: RecentBookingSummary) =>
          b.bookingNumber.toLowerCase().includes(query) ||
          b.guestName.toLowerCase().includes(query)
        )
      }
      if (statusFilter.value) {
        filtered = filtered.filter((b: RecentBookingSummary) => b.status === statusFilter.value)
      }

      // 分页
      const start = (pagination.current - 1) * pagination.pageSize
      const end = start + pagination.pageSize
      bookings.value = filtered.slice(start, end)
      pagination.total = filtered.length
    }
  } catch (error) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadBookings()
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadBookings()
}

// 订单操作处理
const handleBookingAction = async (command: string, booking: RecentBookingSummary) => {
  switch (command) {
    case 'view':
      await viewBookingDetail(booking.bookingNumber)
      break
    case 'checkin':
      await handleCheckIn(booking)
      break
    case 'checkout':
      await handleCheckOut(booking)
      break
    case 'cancel':
      await handleCancelBooking(booking)
      break
    case 'delete':
      await handleDeleteBooking(booking)
      break
  }
}

// 查看订单详情
const viewBookingDetail = async (bookingNumber: string) => {
  loadingDetail.value = true
  bookingDetailDialog.value = true
  try {
    const response = await getBookingByNumber(bookingNumber)
    if (response.data.code === 200) {
      selectedBooking.value = response.data.data
    } else {
      ElMessage.error('获取订单详情失败')
      bookingDetailDialog.value = false
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    bookingDetailDialog.value = false
  } finally {
    loadingDetail.value = false
  }
}

// 办理入住
const handleCheckIn = async (booking: RecentBookingSummary) => {
  try {
    await ElMessageBox.confirm(
      `确认将订单 ${booking.bookingNumber} 办理入住？`,
      '办理入住',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
    await checkInByNumber(booking.bookingNumber)
    ElMessage.success('办理入住成功')
    await loadBookings()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '办理入住失败')
    }
  }
}

// 办理退房
const handleCheckOut = async (booking: RecentBookingSummary) => {
  try {
    await ElMessageBox.confirm(
      `确认将订单 ${booking.bookingNumber} 办理退房？`,
      '办理退房',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
    await checkOutByNumber(booking.bookingNumber)
    ElMessage.success('办理退房成功')
    await loadBookings()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '办理退房失败')
    }
  }
}

// 取消订单
const handleCancelBooking = async (booking: RecentBookingSummary) => {
  try {
    await ElMessageBox.confirm(
      `确认取消订单 ${booking.bookingNumber}？此操作不可恢复。`,
      '取消订单',
      { confirmButtonText: '确认取消', cancelButtonText: '返回', type: 'warning' }
    )
    await cancelBookingByNumber(booking.bookingNumber)
    ElMessage.success('订单已取消')
    await loadBookings()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '取消订单失败')
    }
  }
}

// 删除订单
const handleDeleteBooking = async (booking: RecentBookingSummary) => {
  try {
    await ElMessageBox.confirm(
      `确认删除订单 ${booking.bookingNumber}？删除后将无法恢复。`,
      '删除订单',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error', confirmButtonClass: 'el-button--danger' }
    )
    await deleteBookingByNumber(booking.bookingNumber)
    ElMessage.success('订单已删除')
    await loadBookings()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除订单失败')
    }
  }
}

// 根据订单状态获取可用操作
const getAvailableActions = (status: string) => {
  const actions = [
    { command: 'view', label: '查看详情', icon: 'View' }
  ]

  if (status === 'PENDING' || status === 'CONFIRMED') {
    if (status === 'CONFIRMED') {
      actions.push({ command: 'checkin', label: '办理入住', icon: 'Check' })
    }
    actions.push({ command: 'cancel', label: '取消订单', icon: 'Close' })
  } else if (status === 'CHECKED_IN') {
    actions.push({ command: 'checkout', label: '办理退房', icon: 'Back' })
  } else if ((status === 'CANCELLED' || status === 'CHECKED_OUT') && isAdmin.value) {
    // 只有管理员才能删除订单
    actions.push({ command: 'delete', label: '删除订单', icon: 'Delete' })
  }

  return actions
}

onMounted(() => {
  loadBookings()
})
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="mb-8 flex justify-between items-end">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">订单管理</h1>
          <p class="text-sm text-gray-500 mt-1">查看和管理所有订单</p>
        </div>
        <router-link to="/dashboard">
          <el-button>返回仪表板</el-button>
        </router-link>
      </header>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 mb-6 flex gap-4">
        <el-input
          v-model="searchQuery"
          placeholder="搜索订单号或客人姓名..."
          class="w-64"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="订单状态" clearable class="w-48" @change="handleSearch">
          <el-option label="待确认" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已入住" value="CHECKED_IN" />
          <el-option label="已退房" value="CHECKED_OUT" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button type="primary" @click="handleSearch">筛选</el-button>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-table :data="bookings" style="width: 100%" v-loading="loading">
          <el-table-column prop="bookingNumber" label="订单编号" width="180" />
          <el-table-column prop="guestName" label="客人姓名" width="120" />
          <el-table-column label="客房" width="180">
            <template #default="{ row }">
              {{ row.roomNumber }} ({{ getRoomTypeName(row.roomType) }})
            </template>
          </el-table-column>
          <el-table-column prop="checkInDate" label="入住日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.checkInDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="checkOutDate" label="退房日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.checkOutDate) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" min-width="180">
            <template #default="{ row }">
              <el-dropdown
                trigger="click"
                @command="(cmd: string) => handleBookingAction(cmd, row)"
              >
                <span class="text-slate-400 hover:text-blue-600 transition-colors cursor-pointer">
                  <el-icon :size="20"><MoreFilled /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="action in getAvailableActions(row.status)"
                      :key="action.command"
                      :command="action.command"
                    >
                      <el-icon>
                        <component :is="action.icon" />
                      </el-icon>
                      {{ action.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        <div class="p-4 flex justify-end border-t border-gray-100">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="pagination.total"
            :page-size="pagination.pageSize"
            :current-page="pagination.current"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 订单详情对话框 -->
      <el-dialog
        v-model="bookingDetailDialog"
        title="订单详情"
        width="600px"
        :close-on-click-modal="false"
      >
        <div v-if="loadingDetail" class="flex justify-center py-8">
          <el-icon class="is-loading text-3xl text-blue-600"><Loading /></el-icon>
        </div>
        <div v-else-if="selectedBooking" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-sm text-slate-500">订单编号</label>
              <div class="font-medium">{{ selectedBooking.bookingNumber }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">订单状态</label>
              <div>
                <el-tag :type="getStatusType(selectedBooking.status)">
                  {{ getStatusLabel(selectedBooking.status) }}
                </el-tag>
              </div>
            </div>
            <div>
              <label class="text-sm text-slate-500">客人姓名</label>
              <div class="font-medium">{{ selectedBooking.guestName }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">联系电话</label>
              <div class="font-medium">{{ selectedBooking.guestPhone || '-' }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">房间号</label>
              <div class="font-medium">{{ selectedBooking.roomNumber }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">房型</label>
              <div class="font-medium">{{ getRoomTypeName(selectedBooking.roomType) }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">入住日期</label>
              <div class="font-medium">{{ selectedBooking.checkInDate }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">退房日期</label>
              <div class="font-medium">{{ selectedBooking.checkOutDate }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">入住人数</label>
              <div class="font-medium">{{ selectedBooking.guestCount }} 人</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">订单金额</label>
              <div class="font-medium text-blue-600">¥{{ selectedBooking.totalAmount }}</div>
            </div>
          </div>
          <div v-if="selectedBooking.notes">
            <label class="text-sm text-slate-500">备注</label>
            <div class="text-slate-700 bg-slate-50 p-3 rounded-lg mt-1">
              {{ selectedBooking.notes }}
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="bookingDetailDialog = false">关闭</el-button>
          <el-button
            v-if="selectedBooking?.status === 'CONFIRMED'"
            type="success"
            @click="bookingDetailDialog = false; handleCheckIn(selectedBooking as any)"
          >
            办理入住
          </el-button>
          <el-button
            v-if="selectedBooking?.status === 'CHECKED_IN'"
            type="warning"
            @click="bookingDetailDialog = false; handleCheckOut(selectedBooking as any)"
          >
            办理退房
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>
