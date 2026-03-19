<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyCompletedBookings, createReview } from '@/api/review'
import type { BookingReviewResponse } from '@/types/review'
import SimpleHeader from '../components/SimpleHeader.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const history = ref<BookingReviewResponse[]>([])
const reviewDialogVisible = ref(false)
const submittingReview = ref(false)
const currentReview = ref({
  bookingId: 0,
  rating: 5,
  comment: ''
})

const loadCompletedBookings = async () => {
  loading.value = true
  try {
    const response = await getMyCompletedBookings()
    if (response.data.code === 200 && response.data.data) {
      history.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('加载历史订单失败')
  } finally {
    loading.value = false
  }
}

const openReviewDialog = (booking: BookingReviewResponse) => {
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
      // 重新加载订单列表
      loadCompletedBookings()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '提交评价失败')
  } finally {
    submittingReview.value = false
  }
}

const getRoomTypeName = (type: string) => {
  const names: Record<string, string> = {
    'SINGLE': '标准间',
    'DOUBLE': '豪华间',
    'SUITE': '套房',
    'DELUXE': '总统套房',
    'EXECUTIVE': '行政套房'
  }
  return names[type] || type
}

const getRoomImage = (roomType: string) => {
  const images: Record<string, string> = {
    'SINGLE': 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=400',
    'DOUBLE': 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&q=80&w=400',
    'SUITE': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=400',
    'DELUXE': 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&q=80&w=400',
    'EXECUTIVE': 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=400'
  }
  return images[roomType] || images['SINGLE']
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

onMounted(() => {
  loadCompletedBookings()
})
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <SimpleHeader />
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">历史记录与评价反馈</h1>
        <p class="text-sm text-gray-500 mt-1">查看您的入住历史并留下宝贵评价</p>
      </header>

      <div v-if="loading" class="flex justify-center items-center py-12">
        <el-icon class="is-loading text-4xl text-blue-600"><Loading /></el-icon>
      </div>

      <div v-else-if="history.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
        <p class="text-gray-500">暂无已完成订单</p>
      </div>

      <div v-else class="space-y-6">
        <div v-for="booking in history" :key="booking.id" class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <div class="flex flex-col sm:flex-row gap-6">
            <img :src="getRoomImage(booking.roomType)" class="w-full sm:w-32 h-32 object-cover rounded-lg" />
            <div class="flex-1">
              <div class="flex justify-between items-start mb-2">
                <h3 class="text-lg font-bold text-gray-900">{{ getRoomTypeName(booking.roomType) }}</h3>
                <span class="text-sm text-gray-500">{{ formatDate(booking.checkInDate) }} 至 {{ formatDate(booking.checkOutDate) }}</span>
              </div>
              <p class="text-xs text-gray-500 mb-4">订单号: {{ booking.bookingNumber }}</p>

              <div v-if="booking.reviewed" class="bg-gray-50 p-4 rounded-lg">
                <div class="flex items-center gap-2 mb-2">
                  <span class="text-sm font-semibold text-gray-700">您的评价：</span>
                  <el-rate v-model="booking.rating" disabled text-color="#ff9900" />
                </div>
                <p class="text-sm text-gray-600 italic">"{{ booking.comment }}"</p>
              </div>
              <div v-else class="flex justify-end mt-4">
                <el-button type="primary" @click="openReviewDialog(booking)">撰写评价</el-button>
              </div>
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
