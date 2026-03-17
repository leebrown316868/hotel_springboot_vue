<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchAvailableRooms, createBooking, processPayment } from '@/api/booking'
import { settingsApi } from '@/api/settings'
import type { RoomResponse, BookingResponse } from '@/types/booking'
import type { RoomTypeConfig } from '@/types/settings'

const router = useRouter()
const activeStep = ref(1)
const sortBy = ref('low')
const selectedRoom = ref<RoomResponse | null>(null)
const currentBooking = ref<BookingResponse | null>(null)
const loading = ref(false)
const paymentLoading = ref(false)
const roomTypesConfig = ref<Record<string, RoomTypeConfig>>({})

const searchData = reactive({
  dateRange: [new Date(), new Date(Date.now() + (3 * 24 * 60 * 60 * 1000))],
  guests: '2',
  roomTypes: [] as string[]
})

const guestForm = reactive({
  name: '',
  phone: '',
  email: '',
  notes: ''
})

const guestRules = {
  name: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }]
}

const paymentForm = reactive({
  method: 'ALIPAY'
})

const availableRooms = ref<RoomResponse[]>([])

// 计算总价
const totalAmount = computed(() => {
  if (!selectedRoom.value) return 0
  const days = calculateDays()
  return selectedRoom.value.price * days
})

// 计算住宿天数
const calculateDays = () => {
  if (!searchData.dateRange[0] || !searchData.dateRange[1]) return 1
  const start = new Date(searchData.dateRange[0])
  const end = new Date(searchData.dateRange[1])
  const diffTime = Math.abs(end.getTime() - start.getTime())
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
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

// 获取房间图片URL
const getRoomImage = (roomType: string) => {
  const images: Record<string, string> = {
    'SINGLE': 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=800',
    'DOUBLE': 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&q=80&w=800',
    'SUITE': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=800',
    'DELUXE': 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&q=80&w=800'
  }
  return images[roomType] || 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&q=80&w=800'
}

// 获取房型配置
const fetchRoomTypesConfig = async () => {
  try {
    roomTypesConfig.value = await settingsApi.getRoomTypesConfig()
  } catch (error) {
    console.error('Failed to fetch room types config:', error)
  }
}

// 动态房型选项
const roomTypeOptions = computed(() => {
  return Object.entries(roomTypesConfig.value).map(([code, config]) => ({
    label: config.name,
    value: code,
    capacity: config.capacity,
    basePrice: config.basePrice
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

// 获取房间类型中文名
const getRoomTypeName = (type: string) => {
  return roomTypesConfig.value[type]?.name || type
}

// 获取房间类别
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

// 获取房间容量
const getRoomCapacity = (type: string) => {
  return roomTypesConfig.value[type]?.capacity || 2
}

// 搜索可用房间
const handleSearch = async () => {
  loading.value = true
  try {
    const response = await searchAvailableRooms({
      checkInDate: formatSearchDate(searchData.dateRange[0]),
      checkOutDate: formatSearchDate(searchData.dateRange[1]),
      guestCount: parseInt(searchData.guests),
      roomTypesStr: searchData.roomTypes.length > 0 ? searchData.roomTypes.join(',') : undefined
    })

    if (response.data.code === 200 && response.data.data) {
      availableRooms.value = response.data.data
      ElMessage.success(`找到 ${response.data.data.length} 间可用客房`)
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

// 选择房间
const selectRoom = (room: RoomResponse) => {
  selectedRoom.value = room
}

// 下一步
const nextStep = () => {
  if (activeStep.value < 5) {
    activeStep.value++
  }
}

// 上一步
const prevStep = () => {
  if (activeStep.value > 1) {
    activeStep.value--
  }
}

// 处理支付
const handlePayment = async () => {
  if (!selectedRoom.value) {
    ElMessage.error('请先选择房间')
    return
  }

  paymentLoading.value = true
  try {
    // 先创建预订
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    if (!user || !user.id) {
      ElMessage.error('请先登录')
      router.push('/login')
      return
    }

    const bookingResponse = await createBooking({
      roomId: selectedRoom.value.id,
      checkInDate: formatSearchDate(searchData.dateRange[0]),
      checkOutDate: formatSearchDate(searchData.dateRange[1]),
      guestCount: parseInt(searchData.guests),
      guestInfo: {
        name: guestForm.name,
        phone: guestForm.phone,
        email: guestForm.email,
        notes: guestForm.notes
      }
    })

    if (bookingResponse.data.code === 200 && bookingResponse.data.data) {
      const booking = bookingResponse.data.data

      // 执行支付
      const paymentResponse = await processPayment(booking.id, paymentForm.method)

      if (paymentResponse.data.code === 200) {
        currentBooking.value = paymentResponse.data.data
        activeStep.value = 5
        ElMessage.success('预订成功！')
      } else {
        ElMessage.error('支付失败，请重试')
      }
    } else {
      ElMessage.error('创建预订失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败，请重试')
  } finally {
    paymentLoading.value = false
  }
}

// 跳转到我的订单
const goToMyBookings = () => {
  router.push('/my-bookings')
}

// 重新预订
const bookAgain = () => {
  activeStep.value = 1
  selectedRoom.value = null
  currentBooking.value = null
  handleSearch()
}

// 组件挂载时自动搜索
onMounted(async () => {
  await fetchRoomTypesConfig()
  handleSearch()
})
</script>

<template>
  <div class="pb-20 bg-gray-50 min-h-screen font-sans text-gray-900">
    <header class="bg-white border-b sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center gap-2">
            <div class="bg-blue-600 p-2 rounded-lg text-white">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
              </svg>
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
        <el-steps :active="activeStep - 1" align-center finish-status="success">
          <el-step title="搜索" />
          <el-step title="选择客房" />
          <el-step title="客人信息" />
          <el-step title="支付" />
          <el-step title="确认" />
        </el-steps>
      </section>

      <!-- Step 1 & 2: 搜索和选择客房 -->
      <div v-if="activeStep <= 2" class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <aside class="lg:col-span-1">
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 sticky top-24">
            <h2 class="text-lg font-bold mb-6 flex items-center gap-2">
              <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
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
                  <el-option v-for="option in guestOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </div>

              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase mb-2">客房类型</label>
                <el-checkbox-group v-model="searchData.roomTypes" class="flex flex-col gap-2">
                  <el-checkbox v-for="option in roomTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-checkbox-group>
              </div>

              <el-button type="primary" class="w-full h-12 text-base font-semibold" @click="handleSearch" :loading="loading">
                更新结果
              </el-button>
            </div>
          </div>
        </aside>

        <section class="lg:col-span-3 space-y-6">
          <div class="flex justify-between items-end">
            <div>
              <h1 class="text-2xl font-extrabold text-gray-900">可用客房</h1>
              <p class="text-gray-500 mt-1">找到 {{ availableRooms.length }} 间客房 ({{ formatDate(searchData.dateRange[0]) }} - {{ formatDate(searchData.dateRange[1]) }})</p>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">排序方式:</span>
              <el-select v-model="sortBy" size="small" style="width: 120px">
                <el-option label="价格: 从低到高" value="low" />
                <el-option label="价格: 从高到低" value="high" />
              </el-select>
            </div>
          </div>

          <div v-if="availableRooms.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
            <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">未找到可用房间</h3>
            <p class="text-gray-500">请调整入住日期或房间类型重新搜索</p>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div v-for="room in availableRooms" :key="room.id" class="room-card bg-white rounded-2xl overflow-hidden shadow-sm border border-gray-100 flex flex-col">
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
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path>
                    </svg>
                    <span class="text-xs font-bold ml-1 text-gray-700">4.8</span>
                  </div>
                </div>

                <div class="flex items-center gap-4 mb-4 text-sm text-gray-500">
                  <div class="flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                    最多 {{ getRoomCapacity(room.type) }} 人
                  </div>
                  <div class="flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"></path>
                    </svg>
                    {{ room.number }}
                  </div>
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

      <!-- Step 3: 客人信息 -->
      <div v-if="activeStep === 3" class="max-w-2xl mx-auto">
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
          <h2 class="text-2xl font-bold text-gray-900 mb-6">客人信息</h2>

          <div v-if="selectedRoom" class="bg-blue-50 rounded-lg p-4 mb-6 flex gap-4">
            <img :src="getRoomImage(selectedRoom.type)" class="w-24 h-24 object-cover rounded-lg" />
            <div>
              <h3 class="font-bold text-gray-900">{{ getRoomTypeName(selectedRoom.type) }}</h3>
              <p class="text-sm text-gray-600">{{ formatDate(searchData.dateRange[0]) }} - {{ formatDate(searchData.dateRange[1]) }}</p>
              <p class="text-sm text-gray-600">{{ calculateDays() }} 晚 / {{ searchData.guests }} 位客人</p>
            </div>
          </div>

          <el-form :model="guestForm" label-width="100px">
            <el-form-item label="客人姓名">
              <el-input v-model="guestForm.name" placeholder="请输入客人姓名" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="guestForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="guestForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="特殊要求">
              <el-input v-model="guestForm.notes" type="textarea" :rows="3" placeholder="如有特殊要求请在此填写（可选）" />
            </el-form-item>
          </el-form>

          <div class="flex justify-between mt-8">
            <el-button @click="prevStep">返回上一步</el-button>
            <el-button type="primary" @click="nextStep">继续支付</el-button>
          </div>
        </div>
      </div>

      <!-- Step 4: 支付 -->
      <div v-if="activeStep === 4" class="max-w-2xl mx-auto">
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
          <h2 class="text-2xl font-bold text-gray-900 mb-6">支付信息</h2>

          <div class="bg-gray-50 rounded-lg p-6 mb-6">
            <h3 class="font-bold text-gray-900 mb-4">订单摘要</h3>
            <div class="space-y-3">
              <div class="flex justify-between">
                <span class="text-gray-600">房型</span>
                <span class="font-medium">{{ selectedRoom ? getRoomTypeName(selectedRoom.type) : '' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">房间号</span>
                <span class="font-medium">{{ selectedRoom?.number }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">入住日期</span>
                <span class="font-medium">{{ formatDate(searchData.dateRange[0]) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">退房日期</span>
                <span class="font-medium">{{ formatDate(searchData.dateRange[1]) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">住宿天数</span>
                <span class="font-medium">{{ calculateDays() }} 晚</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">每晚价格</span>
                <span class="font-medium">¥{{ selectedRoom?.price }}</span>
              </div>
              <div class="border-t pt-3 mt-3">
                <div class="flex justify-between">
                  <span class="text-lg font-bold text-gray-900">总价</span>
                  <span class="text-xl font-black text-blue-600">¥{{ totalAmount }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-form label-width="100px">
            <el-form-item label="支付方式">
              <el-radio-group v-model="paymentForm.method" class="flex flex-col gap-3">
                <el-radio value="ALIPAY" border class="w-full p-4">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 bg-blue-500 rounded flex items-center justify-center text-white font-bold">支</div>
                    <span>支付宝</span>
                  </div>
                </el-radio>
                <el-radio value="WECHAT" border class="w-full p-4">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 bg-green-500 rounded flex items-center justify-center text-white font-bold">微</div>
                    <span>微信支付</span>
                  </div>
                </el-radio>
                <el-radio value="CREDIT_CARD" border class="w-full p-4">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 bg-purple-500 rounded flex items-center justify-center text-white font-bold">卡</div>
                    <span>信用卡</span>
                  </div>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>

          <div class="flex justify-between mt-8">
            <el-button @click="prevStep">返回上一步</el-button>
            <el-button type="primary" @click="handlePayment" :loading="paymentLoading">确认支付</el-button>
          </div>
        </div>
      </div>

      <!-- Step 5: 确认 -->
      <div v-if="activeStep === 5" class="max-w-2xl mx-auto">
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-8 text-center">
          <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-10 h-10 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </div>
          <h2 class="text-3xl font-bold text-gray-900 mb-2">预订成功！</h2>
          <p class="text-gray-500 mb-8">感谢您的预订，我们已发送确认邮件到您的邮箱</p>

          <div v-if="currentBooking" class="bg-gray-50 rounded-lg p-6 mb-8 text-left">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <p class="text-xs text-gray-500 uppercase">订单编号</p>
                <p class="font-mono font-bold text-lg">{{ currentBooking.bookingNumber }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-500 uppercase">状态</p>
                <p class="font-bold text-green-600">已确认</p>
              </div>
              <div>
                <p class="text-xs text-gray-500 uppercase">入住日期</p>
                <p class="font-medium">{{ currentBooking.checkInDate }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-500 uppercase">退房日期</p>
                <p class="font-medium">{{ currentBooking.checkOutDate }}</p>
              </div>
            </div>
          </div>

          <div class="flex gap-4 justify-center">
            <el-button type="primary" @click="goToMyBookings">查看我的订单</el-button>
            <el-button @click="bookAgain">再次预订</el-button>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部选择栏 -->
    <div v-if="selectedRoom && activeStep <= 2" class="fixed bottom-0 inset-x-0 bg-white border-t shadow-lg z-50 p-4 transform transition-transform duration-300">
      <div class="max-w-7xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-4">
          <img :src="getRoomImage(selectedRoom.type)" class="w-16 h-12 object-cover rounded-md hidden sm:block" />
          <div>
            <span class="text-xs font-bold text-gray-400 uppercase">已选客房</span>
            <p class="font-bold text-gray-800">{{ getRoomTypeName(selectedRoom.type) }} ({{ selectedRoom.number }})</p>
          </div>
        </div>
        <div class="flex items-center gap-8">
          <div class="text-right">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-widest">预估总价</span>
            <p class="text-xl font-bold text-blue-600">¥{{ totalAmount }} <span class="text-sm font-normal text-gray-500">/ {{ calculateDays() }} 晚</span></p>
          </div>
          <button @click="nextStep" class="bg-blue-600 hover:bg-blue-700 text-white px-8 py-3 rounded-xl font-bold transition-colors">
            继续预订
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
