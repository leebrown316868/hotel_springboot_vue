<script setup lang="ts">
import { ref } from 'vue'
import Layout from '../components/Layout.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const availableRooms = ref([
  {
    id: 1,
    type: '豪华海景房',
    category: '热门',
    capacity: 2,
    size: 45,
    price: 220,
    rating: 4.8,
    amenities: ['特大床', '阳台', '免费 WiFi', '迷你吧'],
    image: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=800'
  },
  {
    id: 2,
    type: '行政商务套房',
    category: '商务',
    capacity: 2,
    size: 60,
    price: 350,
    rating: 4.9,
    amenities: ['办公桌', '智能电视', '客房服务', '特大床'],
    image: 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&q=80&w=800'
  },
  {
    id: 3,
    type: '家庭花园房',
    category: '家庭',
    capacity: 4,
    size: 55,
    price: 280,
    rating: 4.6,
    amenities: ['2 张大床', '露台', '儿童友好', '浴缸'],
    image: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=800'
  },
  {
    id: 4,
    type: '总统顶层公寓',
    category: '奢华',
    capacity: 2,
    size: 120,
    price: 850,
    rating: 5.0,
    amenities: ['私人泳池', '管家', '小厨房', '全景视野'],
    image: 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&q=80&w=800'
  }
])

const bookRoom = () => {
  router.push('/bookings/new')
}
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">房间浏览与搜索</h1>
        <p class="text-sm text-gray-500 mt-1">浏览酒店所有可用房型并进行检索</p>
      </header>
      
      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 mb-8 flex gap-4 items-end">
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase mb-2">入住 / 退房</label>
          <el-date-picker type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        </div>
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase mb-2">入住人数</label>
          <el-select placeholder="请选择" class="w-40">
            <el-option label="1 位成人" value="1" />
            <el-option label="2 位成人" value="2" />
          </el-select>
        </div>
        <el-button type="primary" class="h-8">搜索可用客房</el-button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="room in availableRooms" :key="room.id" class="bg-white rounded-2xl overflow-hidden shadow-sm border border-gray-100 flex flex-col hover:-translate-y-1 transition-transform duration-300">
          <div class="relative h-48 overflow-hidden">
            <img :src="room.image" :alt="room.type" class="w-full h-full object-cover" />
            <div class="absolute top-4 left-4">
              <span class="bg-white/90 backdrop-blur px-3 py-1 rounded-full text-xs font-bold text-blue-600 shadow-sm">
                {{ room.category }}
              </span>
            </div>
          </div>
          
          <div class="p-5 flex-grow flex flex-col">
            <div class="flex justify-between items-start mb-2">
              <h3 class="text-lg font-bold text-gray-900">{{ room.type }}</h3>
              <div class="flex items-center text-yellow-500">
                <el-icon><StarFilled /></el-icon>
                <span class="text-xs font-bold ml-1 text-gray-700">{{ room.rating }}</span>
              </div>
            </div>
            
            <div class="flex items-center gap-4 mb-4 text-sm text-gray-500">
              <div class="flex items-center gap-1">
                <el-icon><User /></el-icon>最多 {{ room.capacity }} 人
              </div>
              <div class="flex items-center gap-1">
                <el-icon><FullScreen /></el-icon>{{ room.size }} m²
              </div>
            </div>
            
            <div class="flex flex-wrap gap-2 mb-6">
              <span v-for="amenity in room.amenities" :key="amenity" class="bg-gray-100 text-gray-600 px-2 py-1 rounded text-[10px] uppercase font-bold tracking-wider">
                {{ amenity }}
              </span>
            </div>
            
            <div class="flex justify-between items-center mt-auto pt-4 border-t">
              <div>
                <span class="block text-2xl font-black text-gray-900">¥{{ room.price }}</span>
                <span class="text-xs text-gray-500 font-medium italic">每晚</span>
              </div>
              <el-button type="primary" @click="bookRoom">立即预订</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>
