<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import SimpleHeader from '../components/SimpleHeader.vue'
import { settingsApi } from '@/api/settings'
import type { RoomResponse } from '@/types/booking'
import type { RoomTypeConfig } from '@/types/settings'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const room = ref<RoomResponse | null>(null)
const roomTypesConfig = ref<Record<string, RoomTypeConfig>>({})
const currentImageIndex = ref(0)

// 获取房型配置
const fetchRoomTypesConfig = async () => {
  try {
    roomTypesConfig.value = await settingsApi.getRoomTypesConfig()
  } catch (error) {
    console.error('Failed to fetch room types config:', error)
  }
}

// 获取房型名称
const getRoomTypeName = (type: string) => {
  return roomTypesConfig.value[type]?.name || type
}

// 获取房型容量
const getRoomCapacity = (type: string) => {
  return roomTypesConfig.value[type]?.capacity || 2
}

// 获取房型图片
const getRoomImage = (roomType: string) => {
  const images: Record<string, string> = {
    'SINGLE': 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=1200',
    'DOUBLE': 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&q=80&w=1200',
    'SUITE': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=1200',
    'EXECUTIVE_SUITE': 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&q=80&w=1200',
    'PRESIDENTIAL_SUITE': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=1200'
  }
  return images[roomType] || 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=1200'
}

// 房间图片列表（优先使用room.images，否则使用默认图片）
const roomImages = computed(() => {
  if (!room.value) return []

  // 如果room有images字段且不为空，解析JSON
  if (room.value.images && room.value.images !== '') {
    try {
      const parsedImages = JSON.parse(room.value.images)
      if (Array.isArray(parsedImages) && parsedImages.length > 0) {
        // 转换相对路径为完整URL
        return parsedImages.map((img: string) => {
          if (img.startsWith('/uploads/')) {
            return `http://localhost:8080${img}`
          }
          return img
        })
      }
    } catch (e) {
      console.error('Failed to parse images:', e)
    }
  }

  // 否则返回默认图片
  return [getRoomImage(room.value.type)]
})

// 获取房间状态标签
const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'AVAILABLE': '可预订',
    'OCCUPIED': '已入住',
    'MAINTENANCE': '维护中',
    'RESERVED': '已预订'
  }
  return labels[status] || status
}

// 获取房间状态类型
const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'AVAILABLE': 'success',
    'OCCUPIED': 'danger',
    'MAINTENANCE': 'warning',
    'RESERVED': 'info'
  }
  return types[status] || 'info'
}

// 获取房型类别
const getRoomCategory = (type: string) => {
  const categories: Record<string, string> = {
    'SINGLE': '标准',
    'DOUBLE': '豪华',
    'SUITE': '套房',
    'EXECUTIVE_SUITE': '行政',
    'PRESIDENTIAL_SUITE': '奢华'
  }
  return categories[type] || '标准'
}

// 预订房间
const bookRoom = () => {
  if (!room.value) return

  // 检查登录状态
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }

  // 检查房间状态
  if (room.value.status !== 'AVAILABLE') {
    ElMessage.warning('该房间当前不可预订')
    return
  }

  // 存储选中的房间信息，跳转到预订页面
  sessionStorage.setItem('selectedRoom', JSON.stringify(room.value))
  router.push('/bookings/new')
}

// 返回上一页
const goBack = () => {
  router.push('/bookings/new')
}

// 获取房间详情
const fetchRoomDetail = async () => {
  const roomId = route.params.id as string
  if (!roomId) {
    ElMessage.error('房间ID无效')
    goBack()
    return
  }

  loading.value = true
  try {
    // 动态导入api模块以避免循环依赖
    const { getRoomDetail } = await import('@/api/booking')
    const response = await getRoomDetail(parseInt(roomId))

    if (response.data.code === 200 && response.data.data) {
      room.value = response.data.data
    } else {
      ElMessage.error('房间不存在')
      goBack()
    }
  } catch (error: any) {
    console.error('获取房间详情失败:', error)
    if (error.response?.status === 404) {
      ElMessage.error('房间不存在')
    } else {
      ElMessage.error('获取房间详情失败')
    }
    goBack()
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  await fetchRoomTypesConfig()
  await fetchRoomDetail()
})
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <SimpleHeader />

    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 返回按钮 -->
      <button
        @click="goBack"
        class="mb-6 flex items-center text-gray-600 hover:text-blue-600 transition-colors"
      >
        <el-icon :size="20"><ArrowLeft /></el-icon>
        <span class="ml-2">返回房间列表</span>
      </button>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-12">
        <el-icon class="is-loading text-4xl text-blue-600"><Loading /></el-icon>
      </div>

      <!-- 房间详情 -->
      <div v-else-if="room" class="bg-white rounded-2xl shadow-sm overflow-hidden">
        <!-- 图片轮播 -->
        <div class="relative h-96">
          <el-carousel
            v-if="roomImages.length > 1"
            indicator-position="outside"
            height="400px"
            :autoplay="true"
            :interval="5000"
          >
            <el-carousel-item v-for="(image, index) in roomImages" :key="index">
              <img :src="image" :alt="room.type" class="w-full h-full object-cover" />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="w-full h-full">
            <img :src="roomImages[0]" :alt="room.type" class="w-full h-full object-cover" />
          </div>

          <!-- 房型类别标签 -->
          <div class="absolute top-4 left-4">
            <span class="bg-white/90 backdrop-blur px-4 py-2 rounded-full text-sm font-bold text-blue-600 shadow-sm">
              {{ getRoomCategory(room.type) }}
            </span>
          </div>
        </div>

        <!-- 房间信息 -->
        <div class="p-8">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h1 class="text-3xl font-bold text-gray-900 mb-2">
                {{ getRoomTypeName(room.type) }} - {{ room.number }}号
              </h1>
              <p class="text-gray-500">{{ room.floor }}楼</p>
            </div>
            <el-tag :type="getStatusType(room.status)" size="large">
              {{ getStatusLabel(room.status) }}
            </el-tag>
          </div>

          <!-- 价格和容量 -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-6 mb-8">
            <div class="bg-slate-50 rounded-xl p-4">
              <p class="text-sm text-gray-500 mb-1">每晚价格</p>
              <p class="text-2xl font-black text-blue-600">¥{{ room.price }}</p>
            </div>
            <div class="bg-slate-50 rounded-xl p-4">
              <p class="text-sm text-gray-500 mb-1">入住人数</p>
              <p class="text-2xl font-black text-gray-900">{{ getRoomCapacity(room.type) }} 人</p>
            </div>
            <div class="bg-slate-50 rounded-xl p-4">
              <p class="text-sm text-gray-500 mb-1">房间号</p>
              <p class="text-2xl font-black text-gray-900">{{ room.number }}</p>
            </div>
            <div class="bg-slate-50 rounded-xl p-4">
              <p class="text-sm text-gray-500 mb-1">楼层</p>
              <p class="text-2xl font-black text-gray-900">{{ room.floor }}楼</p>
            </div>
          </div>

          <!-- 预订按钮 -->
          <div class="flex justify-center">
            <el-button
              v-if="room.status === 'AVAILABLE'"
              type="primary"
              size="large"
              @click="bookRoom"
              class="px-12 py-3 text-lg font-semibold"
            >
              立即预订
            </el-button>
            <el-button
              v-else
              size="large"
              disabled
              class="px-12 py-3 text-lg font-semibold"
            >
              该房间暂不可预订
            </el-button>
          </div>
        </div>
      </div>

      <!-- 房间不存在 -->
      <div v-else class="bg-white rounded-2xl shadow-sm p-12 text-center">
        <el-icon class="text-6xl text-gray-300 mb-4"><House /></el-icon>
        <h3 class="text-xl font-semibold text-gray-900 mb-2">房间不存在</h3>
        <p class="text-gray-500 mb-6">抱歉，找不到您要查看的房间</p>
        <el-button type="primary" @click="goBack">返回房间列表</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.el-carousel__indicator) {
  background-color: rgba(255, 255, 255, 0.5);
}

:deep(.el-carousel__indicator.is-active) {
  background-color: #fff;
}
</style>
