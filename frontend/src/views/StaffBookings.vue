<script setup lang="ts">
import { ref } from 'vue'
import Layout from '../components/Layout.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchQuery = ref('')
const statusFilter = ref('')

const bookings = ref([
  { id: 'BK-20231024-001', guest: '张三', room: '101 (豪华海景房)', checkIn: '2023-10-24', checkOut: '2023-10-27', status: 'CONFIRMED', amount: '¥660' },
  { id: 'BK-20231024-002', guest: '李四', room: '205 (行政商务套房)', checkIn: '2023-10-24', checkOut: '2023-10-25', status: 'CHECKED_IN', amount: '¥350' },
  { id: 'BK-20231025-003', guest: '王五', room: '302 (标准间)', checkIn: '2023-10-25', checkOut: '2023-10-28', status: 'PENDING', amount: '¥450' },
  { id: 'BK-20231020-004', guest: '赵六', room: '401 (总统套房)', checkIn: '2023-10-20', checkOut: '2023-10-22', status: 'CHECKED_OUT', amount: '¥1700' },
])

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

const handleCheckIn = (row: any) => {
  ElMessageBox.confirm(`确认将订单 ${row.id} 办理入住？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info',
  }).then(() => {
    row.status = 'CHECKED_IN'
    ElMessage.success('办理入住成功')
  }).catch(() => {})
}

const handleCheckOut = (row: any) => {
  ElMessageBox.confirm(`确认将订单 ${row.id} 办理退房？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info',
  }).then(() => {
    row.status = 'CHECKED_OUT'
    ElMessage.success('办理退房成功')
  }).catch(() => {})
}
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="mb-8 flex justify-between items-end">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">前台订单管理</h1>
          <p class="text-sm text-gray-500 mt-1">处理入住、退房、预订查询等操作</p>
        </div>
        <el-button type="primary" icon="Plus">新建预订</el-button>
      </header>
      
      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 mb-6 flex gap-4">
        <el-input v-model="searchQuery" placeholder="搜索订单号或客人姓名..." prefix-icon="Search" class="w-64" />
        <el-select v-model="statusFilter" placeholder="订单状态" clearable class="w-48">
          <el-option label="待确认" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已入住" value="CHECKED_IN" />
          <el-option label="已退房" value="CHECKED_OUT" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button type="primary" plain>筛选</el-button>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-table :data="bookings" style="width: 100%">
          <el-table-column prop="id" label="订单编号" width="160" />
          <el-table-column prop="guest" label="客人姓名" width="120" />
          <el-table-column prop="room" label="客房" width="180" />
          <el-table-column prop="checkIn" label="入住日期" width="120" />
          <el-table-column prop="checkOut" label="退房日期" width="120" />
          <el-table-column prop="amount" label="总价" width="100" />
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
          <el-pagination background layout="prev, pager, next" :total="50" />
        </div>
      </div>
    </div>
  </Layout>
</template>
