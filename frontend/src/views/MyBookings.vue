<script setup lang="ts">
import { ref } from 'vue'
import Layout from '../components/Layout.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const myBookings = ref([
  {
    id: 'BK-20231115-089',
    roomType: '豪华海景房',
    checkIn: '2023-11-15',
    checkOut: '2023-11-18',
    status: 'CONFIRMED',
    amount: '¥660',
    image: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=400'
  },
  {
    id: 'BK-20230910-042',
    roomType: '标准间',
    checkIn: '2023-09-10',
    checkOut: '2023-09-12',
    status: 'CHECKED_OUT',
    amount: '¥300',
    image: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=400'
  }
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
    'PENDING': '待支付',
    'CONFIRMED': '预订成功',
    'CHECKED_IN': '入住中',
    'CHECKED_OUT': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const handleCancel = (booking: any) => {
  ElMessageBox.confirm('确定要取消此预订吗？取消后可能产生手续费。', '取消预订', {
    confirmButtonText: '确定取消',
    cancelButtonText: '暂不取消',
    type: 'warning',
  }).then(() => {
    booking.status = 'CANCELLED'
    ElMessage.success('预订已取消')
  }).catch(() => {})
}
</script>

<template>
  <Layout>
    <div class="max-w-4xl mx-auto">
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">我的订单管理</h1>
        <p class="text-sm text-gray-500 mt-1">查看、修改或取消您的历史及当前预订</p>
      </header>
      
      <div class="space-y-6">
        <div v-for="booking in myBookings" :key="booking.id" class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden flex flex-col sm:flex-row">
          <div class="w-full sm:w-48 h-48 sm:h-auto relative">
            <img :src="booking.image" class="w-full h-full object-cover" />
          </div>
          <div class="p-6 flex-1 flex flex-col">
            <div class="flex justify-between items-start mb-4">
              <div>
                <div class="flex items-center gap-3 mb-1">
                  <h3 class="text-lg font-bold text-gray-900">{{ booking.roomType }}</h3>
                  <el-tag :type="getStatusType(booking.status)" size="small">{{ getStatusLabel(booking.status) }}</el-tag>
                </div>
                <p class="text-xs text-gray-500 font-mono">订单号: {{ booking.id }}</p>
              </div>
              <div class="text-right">
                <span class="block text-xl font-black text-blue-600">{{ booking.amount }}</span>
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4 mb-6 bg-gray-50 p-4 rounded-lg">
              <div>
                <p class="text-xs text-gray-500 mb-1">入住日期</p>
                <p class="font-semibold text-gray-900">{{ booking.checkIn }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-500 mb-1">退房日期</p>
                <p class="font-semibold text-gray-900">{{ booking.checkOut }}</p>
              </div>
            </div>
            
            <div class="mt-auto flex justify-end gap-3">
              <el-button v-if="booking.status === 'CONFIRMED' || booking.status === 'PENDING'" plain type="danger" @click="handleCancel(booking)">取消预订</el-button>
              <el-button type="primary" plain>查看详情</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>
