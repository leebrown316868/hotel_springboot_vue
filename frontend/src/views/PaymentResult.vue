<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getBookingByNumber } from '@/api/booking'
import SimpleHeader from '../components/SimpleHeader.vue'

const router = useRouter()
const route = useRoute()

const checking = ref(true)
const paid = ref(false)
const bookingNumber = ref('')
let timer: number | null = null

onMounted(() => {
  const outTradeNo = route.query.out_trade_no as string
  bookingNumber.value = outTradeNo || ''

  if (!outTradeNo) {
    checking.value = false
    return
  }

  // 轮询确认支付状态
  timer = window.setInterval(async () => {
    try {
      const res = await getBookingByNumber(outTradeNo)
      if (res.data.data && res.data.data.paymentStatus === 'PAID') {
        paid.value = true
        checking.value = false
        clearInterval(timer!)
      }
    } catch {
      // ignore
    }
  }, 2000)

  // 30秒超时
  setTimeout(() => {
    if (timer) clearInterval(timer)
    checking.value = false
  }, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="pb-20 bg-gray-50 min-h-screen font-sans text-gray-900">
    <SimpleHeader />

    <main class="max-w-2xl mx-auto px-4 mt-16">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-8 text-center">
        <!-- 检查中 -->
        <div v-if="checking">
          <div class="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-10 h-10 text-blue-500 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-900 mb-2">正在确认支付结果...</h2>
          <p class="text-gray-500">请稍候</p>
        </div>

        <!-- 支付成功 -->
        <div v-else-if="paid">
          <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-10 h-10 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-green-600 mb-2">支付成功</h2>
          <p class="text-gray-500 mb-2">您的预订已确认</p>
          <p v-if="bookingNumber" class="text-sm text-gray-400 mb-8">订单号: {{ bookingNumber }}</p>
          <div class="flex gap-4 justify-center">
            <el-button type="primary" @click="router.push('/my-bookings')">查看我的订单</el-button>
            <el-button @click="router.push('/bookings/new')">继续预订</el-button>
          </div>
        </div>

        <!-- 未完成 -->
        <div v-else>
          <div class="w-20 h-20 bg-orange-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-10 h-10 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-orange-600 mb-2">支付结果确认中</h2>
          <p class="text-gray-500 mb-8">如已完成支付，请前往"我的订单"查看</p>
          <div class="flex gap-4 justify-center">
            <el-button type="primary" @click="router.push('/my-bookings')">查看我的订单</el-button>
            <el-button @click="router.push('/bookings/new')">继续预订</el-button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
