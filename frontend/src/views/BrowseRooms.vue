<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import { searchAvailableRooms } from '@/api/booking'
import { settingsApi } from '@/api/settings'
import type { RoomResponse } from '@/types/booking'
import type { RoomTypeConfig } from '@/types/settings'

const router = useRouter()

const searchData = ref({
  dateRange: [new Date(), new Date(Date.now() + (3 * 24 * 60 * 60 * 1000))] as [Date, Date],
  guests: '2',
  roomTypes: [] as string[]
})

const loading = ref(false)
const availableRooms = ref<RoomResponse[]>([])
const roomTypesConfig = ref<Record<string, RoomTypeConfig>>({})

// 动态房型选项
const roomTypeOptions = computed(() => {
  return Object.entries(roomTypesConfig.value).map(([code, config]) => ({
    label: config.name,
    value: code
  }))
})

// 动态入住人数选项，基于房型最大容量
const guestOptions = computed(() => {
  const maxCapacity = Math.max(...Object.values(roomTypesConfig.value).map(config => config.capacity), 4)
  const options = []
  for (let i = 1; i <= maxCapacity; i++) {
    options.push({
      label: `${i} 位`,
      value: String(i)
    })
  }
  return options
})

// 获取房型配置
const fetchRoomTypesConfig = async () => {
  try {
    roomTypesConfig.value = await settingsApi.getRoomTypesConfig()
  } catch (error) {
    console.error('Failed to fetch room types config:', error)
  }
}

// 格式化日期为API所需格式
const formatSearchDate = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化显示日期
const formatDate = (date: Date) => {
  return formatSearchDate(date)
}

// 搜索可用房间
const handleSearch = async () => {
  loading.value = true
  try {
    const response = await searchAvailableRooms({
      checkInDate: formatSearchDate(searchData.value.dateRange[0]),
      checkOutDate: formatSearchDate(searchData.value.dateRange[1]),
      guestCount: parseInt(searchData.value.guests),
      roomTypes: searchData.value.roomTypes.length > 0 ? searchData.value.roomTypes : undefined
    })

    if (response.data.code === 200 && response.data.data) {
      availableRooms.value = response.data.data
      if (response.data.data.length === 0) {
        ElMessage.warning('未找到可用房间，请调整搜索条件')
      }
    } else {
      ElMessage.warning('未找到可用房间，请调整搜索条件')
      availableRooms.value = []
    }
  } catch (error) {
    ElMessage.error('搜索失败，请重试')
    availableRooms.value = []
  } finally {
    loading.value = false
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
    'SINGLE': 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=800',
    'DOUBLE': 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&q=80&w=800',
    'SUITE': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=800',
    'EXECUTIVE_SUITE': 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&q=80&w=800',
    'PRESIDENTIAL_SUITE': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=800'
  }
  return images[roomType] || 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=800'
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
const bookRoom = (room: RoomResponse) => {
  // 存储选中的房间信息，跳转到预订页面
  sessionStorage.setItem('selectedRoom', JSON.stringify(room))
  router.push('/bookings/new')
}

// 组件挂载时获取数据
onMounted(async () => {
  await fetchRoomTypesConfig()
  handleSearch()
})
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">房间浏览与搜索</h1>
        <p class="text-sm text-gray-500 mt-1">浏览酒店所有可用房型并进行检索</p>
      </header>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase mb-2">入住 / 退房</label>
            <el-date-picker
              v-model="searchData.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              class="w-full"
              style="width: 100%"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase mb-2">入住人数</label>
            <el-select v-model="searchData.guests" placeholder="请选择" class="w-full">
              <el-option v-for="option in guestOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase mb-2">房型筛选</label>
            <el-select v-model="searchData.roomTypes" placeholder="全部房型" multiple class="w-full" collapse-tags>
              <el-option v-for="option in roomTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </div>
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索可用客房</el-button>
        </div>
      </div>

      <div v-if="loading" class="flex justify-center py-12">
        <el-icon class="is-loading text-4xl text-blue-600"><Loading /></el-icon>
      </div>

      <div v-else-if="availableRooms.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
        <h3 class="text-lg font-semibold text-gray-900 mb-2">未找到可用房间</h3>
        <p class="text-gray-500">请调整入住日期或房间类型重新搜索</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="room in availableRooms" :key="room.id" class="bg-white rounded-2xl overflow-hidden shadow-sm border border-gray-100 flex flex-col hover:-translate-y-1 transition-transform duration-300">
          <div class="relative h-48 overflow-hidden">
            <img :src="getRoomImage(room.type)" :alt="room.type" class="w-full h-full object-cover" />
            <div class="absolute top-4 left-4">
              <span class="bg-white/90 backdrop-blur px-3 py-1 rounded-full text-xs font-bold text-blue-600 shadow-sm">
                {{ getRoomCategory(room.type) }}
              </span>
            </div>
          </div>

          <div class="p-5 flex-grow flex flex-col">
            <div class="flex justify-between items-start mb-2">
              <h3 class="text-lg font-bold text-gray-900">{{ getRoomTypeName(room.type) }}</h3>
              <div class="flex items-center text-yellow-500">
                <el-icon><StarFilled /></el-icon>
                <span class="text-xs font-bold ml-1 text-gray-700">4.8</span>
              </div>
            </div>

            <div class="flex items-center gap-4 mb-4 text-sm text-gray-500">
              <div class="flex items-center gap-1">
                <el-icon><User /></el-icon>最多 {{ getRoomCapacity(room.type) }} 人
              </div>
              <div class="flex items-center gap-1">
                <el-icon><House /></el-icon>{{ room.number }}
              </div>
            </div>

            <div class="flex justify-between items-center mt-auto pt-4 border-t">
              <div>
                <span class="block text-2xl font-black text-gray-900">¥{{ room.price }}</span>
                <span class="text-xs text-gray-500 font-medium italic">每晚</span>
              </div>
              <el-button type="primary" @click="bookRoom(room)">立即预订</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>
