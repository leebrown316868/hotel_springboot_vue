<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import SimpleHeader from '../components/SimpleHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBookings, cancelBooking } from '@/api/booking'
import { createReview } from '@/api/review'
import type { BookingResponse } from '@/types/booking'

const router = useRouter()
const loading = ref(false)

const reviewDialogVisible = ref(false)
const submittingReview = ref(false)
const currentReview = ref({
  bookingId: 0,
  rating: 5,
  comment: ''
})
const myBookings = ref<BookingResponse[]>([])

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

const canCancel = (status: string) => {
  return ['PENDING', 'CONFIRMED'].includes(status)
}

const getRoomImage = (roomType: string) => {
  const images: Record<string, string> = {
    'SINGLE': 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=400',
    'DOUBLE': 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&q=80&w=400',
    'SUITE': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=400',
    'DELUXE': 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&q=80&w=400'
  }
  return images[roomType] || images['SINGLE']
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

const loadMyBookings = async () => {
  loading.value = true
  try {
    const response = await getMyBookings({
      page: 0,
      size: 100
    })

    if (response.data.code === 200 && response.data.data) {
      myBookings.value = response.data.data.content
    }
  } catch (error) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = async (booking: BookingResponse) => {
  try {
    await ElMessageBox.confirm('确定要取消此预订吗？取消后可能产生手续费。', '取消预订', {
      confirmButtonText: '确定取消',
      cancelButtonText: '暂不取消',
      type: 'warning',
    })

    await cancelBooking(booking.id)
    ElMessage.success('预订已取消')
    loadMyBookings()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }
}

const openReviewDialog = (booking: BookingResponse) => {
  currentReview.value = {
    bookingId: booking.id,
    rating: 5,
    comment: ''
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!currentReview.value.comment.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }

  submittingReview.value = true
  try {
    const response = await createReview({
      bookingId: currentReview.value.bookingId,
      rating: currentReview.value.rating,
      comment: currentReview.value.comment
    })

    if (response.data.code === 200 || response.status === 201) {
      ElMessage.success('感谢您的评价！')
      reviewDialogVisible.value = false
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '提交评价失败')
  } finally {
    submittingReview.value = false
  }
}

const goToNewBooking = () => {
  // 检查登录状态
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }

  router.push('/bookings/new')
}

onMounted(() => {
  loadMyBookings()
})
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <SimpleHeader />
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <header class="mb-8 flex justify-between items-end">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">我的订单管理</h1>
          <p class="text-sm text-gray-500 mt-1">查看、修改或取消您的历史及当前预订</p>
        </div>
        <el-button type="primary" @click="goToNewBooking">新建预订</el-button>
      </header>

      <div v-if="loading" class="flex justify-center py-12">
        <el-icon class="is-loading" :size="32">
          <svg class="animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </el-icon>
      </div>

      <div v-else-if="myBookings.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
        <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
        </svg>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">暂无订单</h3>
        <p class="text-gray-500 mb-4">您还没有任何预订记录</p>
        <el-button type="primary" @click="goToNewBooking">立即预订</el-button>
      </div>

      <div v-else class="space-y-6">
        <div v-for="booking in myBookings" :key="booking.id" class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden flex flex-col sm:flex-row">
          <div class="w-full sm:w-48 h-48 sm:h-auto relative">
            <img :src="getRoomImage(booking.roomType)" class="w-full h-full object-cover" />
          </div>
          <div class="p-6 flex-1 flex flex-col">
            <div class="flex justify-between items-start mb-4">
              <div>
                <div class="flex items-center gap-3 mb-1">
                  <h3 class="text-lg font-bold text-gray-900">{{ getRoomTypeName(booking.roomType) }}</h3>
                  <el-tag :type="getStatusType(booking.status)" size="small">{{ getStatusLabel(booking.status) }}</el-tag>
                </div>
                <p class="text-xs text-gray-500 font-mono">订单号: {{ booking.bookingNumber }}</p>
              </div>
              <div class="text-right">
                <span class="block text-xl font-black text-blue-600">¥{{ booking.totalAmount }}</span>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4 mb-6 bg-gray-50 p-4 rounded-lg">
              <div>
                <p class="text-xs text-gray-500 mb-1">入住日期</p>
                <p class="font-semibold text-gray-900">{{ booking.checkInDate }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-500 mb-1">退房日期</p>
                <p class="font-semibold text-gray-900">{{ booking.checkOutDate }}</p>
              </div>
            </div>

            <div class="mt-auto flex justify-end gap-3">
              <el-button v-if="canCancel(booking.status)" plain type="danger" @click="handleCancel(booking)">取消预订</el-button>
              <el-button v-if="booking.status === 'CHECKED_OUT'" type="primary" plain @click="openReviewDialog(booking)">撰写评价</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="reviewDialogVisible" title="撰写评价" width="500px">
      <div class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">总体评分</label>
          <el-rate v-model="currentReview.rating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">您的评价内容</label>
          <el-input
            v-model="currentReview.comment"
            type="textarea"
            :rows="4"
            placeholder="分享您的入住体验..."
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submittingReview" @click="submitReview">提交评价</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
