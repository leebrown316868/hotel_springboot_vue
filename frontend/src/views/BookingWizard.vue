<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeStep = ref(1)
const sortBy = ref('low')
const selectedRoom = ref<any>(null)

const searchData = reactive({
  dateRange: [new Date(), new Date(Date.now() + (3 * 24 * 60 * 60 * 1000))],
  guests: '2',
  roomTypes: ['标准间', '豪华间']
})

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

const handleSearch = () => {
  ElMessage({
    message: '正在更新房源...',
    type: 'success',
  })
}

const selectRoom = (room: any) => {
  selectedRoom.value = room
}

const nextStep = () => {
  if (activeStep.value < 5) {
    activeStep.value++
    ElMessage({
      message: `正在进入第 ${activeStep.value} 步...`,
      type: 'info',
    })
  }
}
</script>

<template>
  <div class="pb-20 bg-gray-50 min-h-screen font-sans text-gray-900">
    <header class="bg-white border-b sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center gap-2">
            <div class="bg-blue-600 p-2 rounded-lg text-white">
              <el-icon :size="24"><OfficeBuilding /></el-icon>
            </div>
            <span class="text-xl font-bold text-gray-800 tracking-tight">GrandHorizon <span class="text-blue-600 text-sm font-medium">酒店管理系统</span></span>
          </div>
          <div class="flex items-center gap-4">
            <button class="text-gray-500 hover:text-gray-700 font-medium text-sm">需要帮助？</button>
            <div class="h-8 w-8 rounded-full bg-gray-200"></div>
          </div>
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-8">
      <section class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 mb-8">
        <el-steps :active="activeStep" align-center finish-status="success">
          <el-step title="搜索" />
          <el-step title="选择客房" />
          <el-step title="客人信息" />
          <el-step title="支付" />
          <el-step title="确认" />
        </el-steps>
      </section>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <aside class="lg:col-span-1">
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 sticky top-24">
            <h2 class="text-lg font-bold mb-6 flex items-center gap-2">
              <el-icon class="text-blue-600"><Search /></el-icon>
              修改搜索
            </h2>
            
            <div class="space-y-6">
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
                  <el-option label="1 位成人" value="1" />
                  <el-option label="2 位成人" value="2" />
                  <el-option label="2 位成人, 1 位儿童" value="2-1" />
                  <el-option label="2 位成人, 2 位儿童" value="2-2" />
                </el-select>
              </div>
              
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase mb-2">客房类型</label>
                <el-checkbox-group v-model="searchData.roomTypes" class="flex flex-col gap-2">
                  <el-checkbox label="标准间" />
                  <el-checkbox label="豪华间" />
                  <el-checkbox label="套房" />
                  <el-checkbox label="总统套房" />
                </el-checkbox-group>
              </div>
              
              <el-button type="primary" class="w-full h-12 text-base font-semibold" @click="handleSearch">
                更新结果
              </el-button>
            </div>
          </div>
        </aside>

        <section class="lg:col-span-3 space-y-6">
          <div class="flex justify-between items-end">
            <div>
              <h1 class="text-2xl font-extrabold text-gray-900">可用客房</h1>
              <p class="text-gray-500 mt-1">找到 12 间客房 (10月24日 - 10月27日)</p>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">排序方式:</span>
              <el-select v-model="sortBy" size="small" style="width: 120px">
                <el-option label="价格: 从低到高" value="low" />
                <el-option label="价格: 从高到低" value="high" />
              </el-select>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div v-for="room in availableRooms" :key="room.id" class="room-card bg-white rounded-2xl overflow-hidden shadow-sm border border-gray-100 flex flex-col">
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
                    <el-icon><User /></el-icon>
                    最多 {{ room.capacity }} 人
                  </div>
                  <div class="flex items-center gap-1">
                    <el-icon><FullScreen /></el-icon>
                    {{ room.size }} m²
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
                  <el-button type="primary" size="large" class="rounded-xl px-8" @click="selectRoom(room)">
                    选择客房
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>

    <div v-if="selectedRoom" class="fixed bottom-0 inset-x-0 bg-white border-t shadow-lg z-50 p-4 transform transition-transform duration-300">
      <div class="max-w-7xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-4">
          <img :src="selectedRoom.image" class="w-16 h-12 object-cover rounded-md hidden sm:block" />
          <div>
            <span class="text-xs font-bold text-gray-400 uppercase">已选客房</span>
            <p class="font-bold text-gray-800">{{ selectedRoom.type }}</p>
          </div>
        </div>
        <div class="flex items-center gap-8">
          <div class="text-right">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-widest">预估总价</span>
            <p class="text-xl font-bold text-blue-600">¥{{ selectedRoom.price * 3 }} <span class="text-sm font-normal text-gray-500">/ 3 晚</span></p>
          </div>
          <button @click="nextStep" class="bg-blue-600 hover:bg-blue-700 text-white px-8 py-3 rounded-xl font-bold transition-colors">
            继续填写客人信息
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.el-step__title) {
  font-size: 0.875rem !important;
  font-weight: 600 !important;
}
.room-card:hover {
  transform: translateY(-4px);
  transition: all 0.3s ease;
}
:deep(.el-button--primary) {
  --el-button-bg-color: #2563eb;
  --el-button-border-color: #2563eb;
  --el-button-hover-bg-color: #1d4ed8;
  --el-button-hover-border-color: #1d4ed8;
}
</style>
