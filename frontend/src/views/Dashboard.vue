<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Action } from 'element-plus'
import Layout from '../components/Layout.vue'
import * as echarts from 'echarts'
import {
  getDashboardStatistics,
  getRoomStatusDistribution,
  getBookingTrends,
  getRecentBookings
} from '@/api/statistics'
import { getBookingByNumber, cancelBookingByNumber, checkInByNumber, checkOutByNumber, deleteBookingByNumber } from '@/api/booking'
import type {
  DashboardStatistics,
  RoomStatusDistribution,
  BookingTrendData,
  RecentBookingSummary
} from '@/types/statistics'
import type { BookingResponse } from '@/types/booking'

const router = useRouter()

// 订单详情对话框
const bookingDetailDialog = ref(false)
const selectedBooking = ref<BookingResponse | null>(null)
const loadingDetail = ref(false)

const statusChartRef = ref<HTMLElement | null>(null)
const trendsChartRef = ref<HTMLElement | null>(null)
let statusChart: echarts.ECharts | null = null
let trendsChart: echarts.ECharts | null = null

const statistics = ref<DashboardStatistics | null>(null)
const roomStatusData = ref<RoomStatusDistribution[]>([])
const bookingTrendData = ref<BookingTrendData[]>([])
const recentBookings = ref<RecentBookingSummary[]>([])
const loading = ref(true)

// 图表 resize 处理函数
const handleResize = () => {
  statusChart?.resize()
  trendsChart?.resize()
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    const [statsRes, statusRes, trendsRes, bookingsRes] = await Promise.all([
      getDashboardStatistics(),
      getRoomStatusDistribution(),
      getBookingTrends(7),
      getRecentBookings(4)
    ])

    statistics.value = statsRes.data.data
    roomStatusData.value = statusRes.data.data
    bookingTrendData.value = trendsRes.data.data
    recentBookings.value = bookingsRes.data.data
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const updateCharts = () => {
  // 更新客房状态分布饼图
  if (statusChart) {
    // 确保 resize 以获取正确的容器尺寸
    statusChart.resize()

    if (roomStatusData.value.length > 0) {
      const statusData = roomStatusData.value.map(item => ({
        value: item.count,
        name: item.displayName,
        itemStyle: {
          color: getStatusColor(item.status)
        }
      }))

      statusChart.setOption({
        series: [{
          data: statusData
        }],
        legend: {
          data: roomStatusData.value.map(item => item.displayName)
        }
      })
    } else {
      // 数据为空时显示空状态
      statusChart.setOption({
        series: [{
          data: []
        }],
        legend: {
          data: []
        }
      })
    }
  }

  // 更新预订趋势折线图
  if (trendsChart) {
    // 确保 resize 以获取正确的容器尺寸
    trendsChart.resize()

    if (bookingTrendData.value.length > 0) {
      const xAxisData = bookingTrendData.value.map(item => item.date)
      const seriesData = bookingTrendData.value.map(item => item.count)

      trendsChart.setOption({
        xAxis: {
          data: xAxisData
        },
        series: [{
          data: seriesData
        }]
      })
    } else {
      // 数据为空时显示空状态
      trendsChart.setOption({
        xAxis: {
          data: []
        },
        series: [{
          data: []
        }]
      })
    }
  }
}

const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    'AVAILABLE': '#10b981',
    'OCCUPIED': '#2563eb',
    'CLEANING': '#f59e0b',
    'MAINTENANCE': '#ef4444'
  }
  return colors[status] || '#94a3b8'
}

const getStatusDisplayName = (status: string): string => {
  const names: Record<string, string> = {
    'PENDING': '待处理',
    'CONFIRMED': '已确认',
    'CHECKED_IN': '已入住',
    'CHECKED_OUT': '已退房',
    'CANCELLED': '已取消'
  }
  return names[status] || status
}

const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const getInitials = (name: string): string => {
  return name.slice(0, 2).toUpperCase()
}

const getBookingStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    'PENDING': 'amber',
    'CONFIRMED': 'emerald',
    'CHECKED_IN': 'blue',
    'CHECKED_OUT': 'slate',
    'CANCELLED': 'red'
  }
  return colors[status] || 'gray'
}

const getStatusLabel = (status: string): string => {
  const labels: Record<string, string> = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CHECKED_IN': '已入住',
    'CHECKED_OUT': '已退房',
    'CANCELLED': '已取消'
  }
  return labels[status] || status
}

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

const getRoomTypeName = (type: string): string => {
  const names: Record<string, string> = {
    'SINGLE': '单人间',
    'DOUBLE': '双人间',
    'SUITE': '套房',
    'EXECUTIVE_SUITE': '行政套房',
    'PRESIDENTIAL_SUITE': '总统套房'
  }
  return names[type] || type
}

// 按钮点击处理函数
const handleManageRooms = () => {
  router.push('/rooms')
}

const handleCreateBooking = () => {
  router.push('/bookings/new')
}

// 订单操作处理
const handleBookingAction = async (command: string, booking: RecentBookingSummary) => {
  switch (command) {
    case 'view':
      await viewBookingDetail(booking.bookingNumber)
      break
    case 'edit':
      router.push(`/staff-bookings?search=${booking.bookingNumber}`)
      break
    case 'checkin':
      await handleCheckIn(booking)
      break
    case 'checkout':
      await handleCheckOut(booking)
      break
    case 'cancel':
      await handleCancelBooking(booking)
      break
    case 'delete':
      await handleDeleteBooking(booking)
      break
  }
}

// 查看订单详情
const viewBookingDetail = async (bookingNumber: string) => {
  loadingDetail.value = true
  bookingDetailDialog.value = true
  try {
    const response = await getBookingByNumber(bookingNumber)
    if (response.data.code === 200) {
      selectedBooking.value = response.data.data
    } else {
      ElMessage.error('获取订单详情失败')
      bookingDetailDialog.value = false
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    bookingDetailDialog.value = false
  } finally {
    loadingDetail.value = false
  }
}

// 办理入住
const handleCheckIn = async (booking: RecentBookingSummary) => {
  try {
    await ElMessageBox.confirm(
      `确认将订单 ${booking.bookingNumber} 办理入住？`,
      '办理入住',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
    await checkInByNumber(booking.bookingNumber)
    ElMessage.success('办理入住成功')
    await loadDashboardData() // 刷新数据
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '办理入住失败')
    }
  }
}

// 办理退房
const handleCheckOut = async (booking: RecentBookingSummary) => {
  try {
    await ElMessageBox.confirm(
      `确认将订单 ${booking.bookingNumber} 办理退房？`,
      '办理退房',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
    await checkOutByNumber(booking.bookingNumber)
    ElMessage.success('办理退房成功')
    await loadDashboardData() // 刷新数据
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '办理退房失败')
    }
  }
}

// 取消订单
const handleCancelBooking = async (booking: RecentBookingSummary) => {
  try {
    await ElMessageBox.confirm(
      `确认取消订单 ${booking.bookingNumber}？此操作不可恢复。`,
      '取消订单',
      { confirmButtonText: '确认取消', cancelButtonText: '返回', type: 'warning' }
    )
    await cancelBookingByNumber(booking.bookingNumber)
    ElMessage.success('订单已取消')
    await loadDashboardData() // 刷新数据
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '取消订单失败')
    }
  }
}

// 删除订单
const handleDeleteBooking = async (booking: RecentBookingSummary) => {
  try {
    await ElMessageBox.confirm(
      `确认删除订单 ${booking.bookingNumber}？删除后将无法恢复。`,
      '删除订单',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error', confirmButtonClass: 'el-button--danger' }
    )
    await deleteBookingByNumber(booking.bookingNumber)
    ElMessage.success('订单已删除')
    await loadDashboardData() // 刷新数据
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除订单失败')
    }
  }
}

// 根据订单状态获取可用操作
const getAvailableActions = (status: string) => {
  const actions = [
    { command: 'view', label: '查看详情', icon: 'View' }
  ]

  if (status === 'PENDING' || status === 'CONFIRMED') {
    if (status === 'CONFIRMED') {
      actions.push({ command: 'checkin', label: '办理入住', icon: 'Check' })
    }
    actions.push({ command: 'cancel', label: '取消订单', icon: 'Close' })
  } else if (status === 'CHECKED_IN') {
    actions.push({ command: 'checkout', label: '办理退房', icon: 'Back' })
  } else if (status === 'CANCELLED' || status === 'CHECKED_OUT') {
    actions.push({ command: 'delete', label: '删除订单', icon: 'Delete' })
  }

  actions.push({ command: 'edit', label: '更多操作', icon: 'MoreFilled' })
  return actions
}

onMounted(async () => {
  // 添加窗口 resize 监听
  window.addEventListener('resize', handleResize)

  // 先加载数据
  await loadDashboardData()

  // 数据加载完成后，等待 DOM 更新并初始化图表
  await nextTick()
  await nextTick() // 双重 nextTick 确保完全渲染

  // 初始化客房状态图表
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
    statusChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center', icon: 'circle' },
      series: [{
        name: '客房状态',
        type: 'pie',
        radius: ['50%', '80%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: []
      }]
    })
  }

  // 初始化预订趋势图表
  if (trendsChartRef.value) {
    trendsChart = echarts.init(trendsChartRef.value)
    trendsChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: [],
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisLabel: { color: '#94a3b8' }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } },
        axisLabel: { color: '#94a3b8' }
      },
      series: [{
        name: '预订量',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: [],
        lineStyle: { width: 4, color: '#2563eb' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.2)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0)' }
          ])
        }
      }]
    })
  }

  // 初始化后更新图表数据
  updateCharts()
})

onBeforeUnmount(() => {
  // 移除窗口 resize 监听
  window.removeEventListener('resize', handleResize)

  // 销毁图表实例
  statusChart?.dispose()
  trendsChart?.dispose()
})
</script>

<template>
  <Layout>
    <div v-if="loading" class="flex items-center justify-center py-20">
      <el-icon class="is-loading text-4xl text-blue-600"><Loading /></el-icon>
    </div>

    <div v-else>
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-bold text-slate-900">仪表板概览</h1>
          <p class="text-slate-500">欢迎回来，这是今天的实时数据。</p>
        </div>
        <div class="flex gap-3">
          <button @click="handleManageRooms" class="inline-flex items-center gap-2 px-4 py-2 border border-slate-200 bg-white rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">
            <el-icon><Setting /></el-icon> 管理客房
          </button>
          <button @click="handleCreateBooking" class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 rounded-lg text-sm font-medium text-white hover:bg-blue-700 transition-colors shadow-sm shadow-blue-200">
            <el-icon><Plus /></el-icon> 创建新预订
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-6 mb-8">
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">总客房数</span>
            <div class="p-2 bg-slate-100 rounded-lg text-slate-600"><el-icon :size="20"><House /></el-icon></div>
          </div>
          <div class="text-2xl font-bold">{{ statistics?.totalRooms || 0 }}</div>
          <div class="text-xs text-slate-400 mt-2">包含所有房型</div>
        </div>

        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">空闲客房</span>
            <div class="p-2 bg-emerald-50 rounded-lg text-emerald-600"><el-icon :size="20"><Select /></el-icon></div>
          </div>
          <div class="text-2xl font-bold text-emerald-600">{{ statistics?.availableRooms || 0 }}</div>
          <div class="text-xs text-emerald-500 mt-2 font-medium">当前入住率 {{ statistics?.occupancyRate || 0 }}%</div>
        </div>

        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">今日入住</span>
            <div class="p-2 bg-blue-50 rounded-lg text-blue-600"><el-icon :size="20"><Right /></el-icon></div>
          </div>
          <div class="text-2xl font-bold">{{ statistics?.todayCheckIns || 0 }}</div>
          <div class="text-xs text-slate-400 mt-2">预计今日到达</div>
        </div>

        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">今日退房</span>
            <div class="p-2 bg-orange-50 rounded-lg text-orange-600"><el-icon :size="20"><Back /></el-icon></div>
          </div>
          <div class="text-2xl font-bold">{{ statistics?.todayCheckOuts || 0 }}</div>
          <div class="text-xs text-slate-400 mt-2">待办理离店</div>
        </div>

        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">今日收入</span>
            <div class="p-2 bg-indigo-50 rounded-lg text-indigo-600"><el-icon :size="20"><Money /></el-icon></div>
          </div>
          <div class="text-2xl font-bold">¥{{ statistics?.todayRevenue?.toLocaleString() || 0 }}</div>
          <div class="text-xs text-emerald-600 mt-2 font-medium">今日已完成订单</div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
        <div class="lg:col-span-1 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 class="font-bold text-slate-800 mb-6 flex items-center justify-between">
            客房状态分布
            <el-icon class="text-slate-400"><InfoFilled /></el-icon>
          </h3>
          <div ref="statusChartRef" style="width: 100%; height: 280px;"></div>
        </div>

        <div class="lg:col-span-2 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-6">
            <h3 class="font-bold text-slate-800">预订趋势 (最近 7 天)</h3>
            <select class="text-sm border border-slate-200 rounded-md focus:ring-blue-500 px-2 py-1">
              <option>周视图</option>
              <option>月视图</option>
            </select>
          </div>
          <div ref="trendsChartRef" style="width: 100%; height: 280px;"></div>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <h3 class="font-bold text-slate-800">最近预订</h3>
          <router-link to="/dashboard/bookings" class="text-blue-600 text-sm font-medium hover:underline">查看全部</router-link>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left">
            <thead>
              <tr class="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider">
                <th class="px-6 py-4">预订编号</th>
                <th class="px-6 py-4">客人姓名</th>
                <th class="px-6 py-4">房间</th>
                <th class="px-6 py-4">状态</th>
                <th class="px-6 py-4">入住日期</th>
                <th class="px-6 py-4 text-right">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 text-sm">
              <tr v-for="booking in recentBookings" :key="booking.bookingNumber" class="hover:bg-slate-50 transition-colors">
                <td class="px-6 py-4 font-mono text-slate-600">{{ booking.bookingNumber }}</td>
                <td class="px-6 py-4">
                  <div class="flex items-center gap-3">
                    <div :class="`w-8 h-8 rounded-full bg-${getBookingStatusColor(booking.status)}-100 text-${getBookingStatusColor(booking.status)}-600 flex items-center justify-center font-bold text-xs`">
                      {{ getInitials(booking.guestName) }}
                    </div>
                    <span class="font-medium text-slate-800">{{ booking.guestName }}</span>
                  </div>
                </td>
                <td class="px-6 py-4">{{ booking.roomNumber }}</td>
                <td class="px-6 py-4">
                  <span :class="`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-${getBookingStatusColor(booking.status)}-100 text-${getBookingStatusColor(booking.status)}-800`">
                    <span :class="`w-1.5 h-1.5 rounded-full bg-${getBookingStatusColor(booking.status)}-500`"></span>
                    {{ getStatusDisplayName(booking.status) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-slate-600">{{ formatDate(booking.checkInDate) }}</td>
                <td class="px-6 py-4 text-right">
                  <el-dropdown
                    trigger="click"
                    @command="(cmd: string) => handleBookingAction(cmd, booking)"
                  >
                    <span class="text-slate-400 hover:text-blue-600 transition-colors cursor-pointer">
                      <el-icon :size="20"><MoreFilled /></el-icon>
                    </span>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item
                          v-for="action in getAvailableActions(booking.status)"
                          :key="action.command"
                          :command="action.command"
                          :divided="action.command === 'edit'"
                        >
                          <el-icon>
                            <component :is="action.icon" />
                          </el-icon>
                          {{ action.label }}
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="px-6 py-4 bg-slate-50 border-t border-slate-100 text-xs text-slate-500">
          显示最近 {{ recentBookings.length }} 条记录
        </div>
      </div>

      <!-- 订单详情对话框 -->
      <el-dialog
        v-model="bookingDetailDialog"
        title="订单详情"
        width="600px"
        :close-on-click-modal="false"
      >
        <div v-if="loadingDetail" class="flex justify-center py-8">
          <el-icon class="is-loading text-3xl text-blue-600"><Loading /></el-icon>
        </div>
        <div v-else-if="selectedBooking" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-sm text-slate-500">订单编号</label>
              <div class="font-medium">{{ selectedBooking.bookingNumber }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">订单状态</label>
              <div>
                <el-tag :type="getStatusType(selectedBooking.status)">
                  {{ getStatusLabel(selectedBooking.status) }}
                </el-tag>
              </div>
            </div>
            <div>
              <label class="text-sm text-slate-500">客人姓名</label>
              <div class="font-medium">{{ selectedBooking.guestName }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">联系电话</label>
              <div class="font-medium">{{ selectedBooking.guestPhone || '-' }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">房间号</label>
              <div class="font-medium">{{ selectedBooking.roomNumber }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">房型</label>
              <div class="font-medium">{{ getRoomTypeName(selectedBooking.roomType) }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">入住日期</label>
              <div class="font-medium">{{ selectedBooking.checkInDate }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">退房日期</label>
              <div class="font-medium">{{ selectedBooking.checkOutDate }}</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">入住人数</label>
              <div class="font-medium">{{ selectedBooking.guestCount }} 人</div>
            </div>
            <div>
              <label class="text-sm text-slate-500">订单金额</label>
              <div class="font-medium text-blue-600">¥{{ selectedBooking.totalAmount }}</div>
            </div>
          </div>
          <div v-if="selectedBooking.notes">
            <label class="text-sm text-slate-500">备注</label>
            <div class="text-slate-700 bg-slate-50 p-3 rounded-lg mt-1">
              {{ selectedBooking.notes }}
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="bookingDetailDialog = false">关闭</el-button>
          <el-button
            v-if="selectedBooking?.status === 'CONFIRMED'"
            type="success"
            @click="bookingDetailDialog = false; handleCheckIn(selectedBooking as any)"
          >
            办理入住
          </el-button>
          <el-button
            v-if="selectedBooking?.status === 'CHECKED_IN'"
            type="warning"
            @click="bookingDetailDialog = false; handleCheckOut(selectedBooking as any)"
          >
            办理退房
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>
