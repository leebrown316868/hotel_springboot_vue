<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import Layout from '../components/Layout.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllBookings, checkIn, checkOut } from '@/api/booking'
import type { BookingResponse } from '@/types/booking'

const searchQuery = ref('')
const statusFilter = ref('')
const loading = ref(false)

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const bookings = ref<BookingResponse[]>([])

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

const loadBookings = async () => {
  loading.value = true
  try {
    const response = await getAllBookings({
      search: searchQuery.value || undefined,
      status: statusFilter.value || undefined,
      page: pagination.current - 1,
      size: pagination.pageSize
    })

    if (response.data.code === 200 && response.data.data) {
      bookings.value = response.data.data.content
      pagination.total = response.data.data.totalElements
    } else {
      bookings.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error('加载订单失败')
    bookings.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadBookings()
}

const handleCheckIn = async (row: BookingResponse) => {
  try {
    await ElMessageBox.confirm(`确认将订单 ${row.bookingNumber} 办理入住？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info',
    })

    await checkIn(row.id)
    ElMessage.success('办理入住成功')
    loadBookings()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '办理入住失败')
    }
  }
}

const handleCheckOut = async (row: BookingResponse) => {
  try {
    await ElMessageBox.confirm(`确认将订单 ${row.bookingNumber} 办理退房？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info',
    })

    await checkOut(row.id)
    ElMessage.success('办理退房成功')
    loadBookings()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '办理退房失败')
    }
  }
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadBookings()
}

const getRoomTypeName = (type: string) => {
  const names: Record<string, string> = {
    'SINGLE': '标准间',
    'DOUBLE': '豪华间',
    'SUITE': '套房',
    'DELUXE': '总统套房'
  }
  return names[type] || type
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
          <h1 class="text-2xl font-bold text-gray-900">前台订单管理</h1>
          <p class="text-sm text-gray-500 mt-1">处理入住、退房、预订查询等操作</p>
        </div>
        <router-link to="/bookings/new">
          <el-button type="primary">新建预订</el-button>
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
          <el-table-column prop="checkInDate" label="入住日期" width="120" />
          <el-table-column prop="checkOutDate" label="退房日期" width="120" />
          <el-table-column label="总价" width="100">
            <template #default="{ row }">
              ¥{{ row.totalAmount }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" min-width="180">
            <template #default="{ row }">
              <el-button v-if="row.status === 'CONFIRMED'" size="small" type="success" @click="handleCheckIn(row)">入住</el-button>
              <el-button v-if="row.status === 'CHECKED_IN'" size="small" type="warning" @click="handleCheckOut(row)">退房</el-button>
              <el-button size="small" text type="primary">详情</el-button>
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
    </div>
  </Layout>
</template>
