<script setup lang="ts">
import { ref } from 'vue'
import Layout from '../components/Layout.vue'
import { ElMessage } from 'element-plus'

const history = ref([
  {
    id: 'BK-20230910-042',
    roomType: '标准间',
    checkIn: '2023-09-10',
    checkOut: '2023-09-12',
    reviewed: false,
    image: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=400'
  },
  {
    id: 'BK-20221205-011',
    roomType: '豪华海景房',
    checkIn: '2022-12-05',
    checkOut: '2022-12-08',
    reviewed: true,
    rating: 5,
    comment: '非常棒的体验，海景无敌，服务也很周到！',
    image: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=400'
  }
])

const reviewDialogVisible = ref(false)
const currentReview = ref({
  bookingId: '',
  rating: 5,
  comment: ''
})

const openReviewDialog = (booking: any) => {
  currentReview.value = {
    bookingId: booking.id,
    rating: 5,
    comment: ''
  }
  reviewDialogVisible.value = true
}

const submitReview = () => {
  const booking = history.value.find(b => b.id === currentReview.value.bookingId)
  if (booking) {
    booking.reviewed = true
    booking.rating = currentReview.value.rating
    booking.comment = currentReview.value.comment
  }
  reviewDialogVisible.value = false
  ElMessage.success('感谢您的评价！')
}
</script>

<template>
  <Layout>
    <div class="max-w-4xl mx-auto">
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">历史记录与评价反馈</h1>
        <p class="text-sm text-gray-500 mt-1">查看您的入住历史并留下宝贵评价</p>
      </header>
      
      <div class="space-y-6">
        <div v-for="booking in history" :key="booking.id" class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <div class="flex flex-col sm:flex-row gap-6">
            <img :src="booking.image" class="w-full sm:w-32 h-32 object-cover rounded-lg" />
            <div class="flex-1">
              <div class="flex justify-between items-start mb-2">
                <h3 class="text-lg font-bold text-gray-900">{{ booking.roomType }}</h3>
                <span class="text-sm text-gray-500">{{ booking.checkIn }} 至 {{ booking.checkOut }}</span>
              </div>
              <p class="text-xs text-gray-500 mb-4">订单号: {{ booking.id }}</p>
              
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
          <el-button type="primary" @click="submitReview">提交评价</el-button>
        </span>
      </template>
    </el-dialog>
  </Layout>
</template>
