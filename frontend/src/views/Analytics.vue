<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import Layout from '../components/Layout.vue'
import * as echarts from 'echarts'
import api from '@/utils/api'
import { ElMessage } from 'element-plus'

const trendsChartRef = ref<HTMLElement | null>(null)

// ... existing ...
const dashboardData = ref<any>({
  totalRooms: 0,
  availableRooms: 0,
  occupancyRate: 0,
  todayCheckIns: 0,
  todayCheckOuts: 0,
  todayRevenue: 0
})
const recentBookings = ref<any[]>([])
const dateRange = ref<[string, string] | null>(null)

const handleDateRangeChange = async (val: [string, string] | null) => {
  try {
    if (val && val.length === 2) {
      const res = await api.get(`/api/statistics/booking-trends-range?startDate=${val[0]}&endDate=${val[1]}`)
      if (res.data.code === 200) {
        renderTrendsChart(res.data.data)
      }
    } else {
      const res = await api.get('/api/statistics/booking-trends?days=7')
      if (res.data.code === 200) {
        renderTrendsChart(res.data.data)
      }
    }
  } catch (error) {
    ElMessage.error('加载趋势数据失败')
  }
}
// ... existing ...

const loadData = async () => {
  try {
    const [dashRes, trendsRes, recentRes] = await Promise.all([
      api.get('/api/statistics/dashboard'),
      api.get('/api/statistics/booking-trends?days=7'),
      api.get('/api/statistics/recent-bookings?limit=6')
    ])
    
    if (dashRes.data.code === 200) dashboardData.value = dashRes.data.data
    if (recentRes.data.code === 200) recentBookings.value = recentRes.data.data
    
    if (trendsRes.data.code === 200) {
      nextTick(() => {
        renderTrendsChart(trendsRes.data.data)
      })
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const renderTrendsChart = (trends: any[]) => {
  if (trendsChartRef.value) {
    const chart = echarts.init(trendsChartRef.value)
    
    const dates = trends.map(t => t.date)
    const counts = trends.map(t => t.count)

    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: dates,
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
        name: '预订数量',
        type: 'line',
        smooth: true,
        data: counts,
        lineStyle: { width: 4, color: '#10b981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.2)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0)' }
          ])
        }
      }]
    })
    
    window.addEventListener('resize', () => chart.resize())
  }
}

const exportReport = () => {
  try {
    const BOM = '\uFEFF'
    let content = BOM

    content += "项目,数值\n"
    content += `今日预估收入,¥${dashboardData.value.todayRevenue}\n`
    content += `入住率,${dashboardData.value.occupancyRate}%\n`
    content += `今日预计入住,${dashboardData.value.todayCheckIns}\n`
    content += `今日待退房,${dashboardData.value.todayCheckOuts}\n\n`

    content += "最新预订动态\n"
    content += "订单号,房型/房号,客户,入住日期,状态\n"
    
    recentBookings.value.forEach(b => {
      const roomStr = b.roomNumber ? `${b.roomType}(${b.roomNumber})` : b.roomType
      content += `${b.bookingNumber},${roomStr},${b.guestName},${b.checkInDate},${b.status}\n`
    })

    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement("a")
    const url = URL.createObjectURL(blob)
    link.setAttribute("href", url)
    
    const dateStr = new Date().toISOString().split('T')[0]
    link.setAttribute("download", `酒店业务报表_${dateStr}.csv`)
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('报表导出成功')
  } catch (error) {
    ElMessage.error('报表导出失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">数据分析与报表</h1>
          <p class="text-sm text-gray-500 mt-1">监控酒店的业绩和增长情况</p>
        </div>
        <div class="flex gap-3">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始趋势日期"
            end-placeholder="结束趋势日期"
            size="large"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
          />
          <el-button type="primary" size="large" icon="Download" @click="exportReport">导出报表</el-button>
        </div>
      </header>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">今日预估收入</span>
            <div class="p-2 bg-emerald-50 rounded-lg text-emerald-600">
              <el-icon :size="20"><Money /></el-icon>
            </div>
          </div>
          <div class="text-2xl font-bold">¥{{ dashboardData.todayRevenue }}</div>
        </div>
        
        <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">入住率</span>
            <div class="p-2 bg-blue-50 rounded-lg text-blue-600">
              <el-icon :size="20"><House /></el-icon>
            </div>
          </div>
          <div class="text-2xl font-bold">{{ dashboardData.occupancyRate }}%</div>
        </div>

        <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">今日预计入住</span>
            <div class="p-2 bg-indigo-50 rounded-lg text-indigo-600">
              <el-icon :size="20"><UserFilled /></el-icon>
            </div>
          </div>
          <div class="text-2xl font-bold">{{ dashboardData.todayCheckIns }}</div>
        </div>

        <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">今日待退房</span>
            <div class="p-2 bg-rose-50 rounded-lg text-rose-600">
              <el-icon :size="20"><List /></el-icon>
            </div>
          </div>
          <div class="text-2xl font-bold">{{ dashboardData.todayCheckOuts }}</div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-1 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 class="font-bold text-slate-800 mb-6">新增预订趋势 (最近7天)</h3>
          <div ref="trendsChartRef" class="w-full h-80"></div>
        </div>

        <div class="lg:col-span-2 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 class="font-bold text-slate-800 mb-6">最新预订动态</h3>
          <div class="overflow-x-auto">
            <table class="w-full text-left">
              <thead>
                <tr class="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider">
                  <th class="px-4 py-3">订单号</th>
                  <th class="px-4 py-3">房型/房号</th>
                  <th class="px-4 py-3">客户</th>
                  <th class="px-4 py-3">入住日期</th>
                  <th class="px-4 py-3">状态</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100 text-sm">
                <tr v-if="recentBookings.length === 0">
                  <td colspan="5" class="text-center py-6 text-gray-500">暂无近期订单</td>
                </tr>
                <tr v-for="booking in recentBookings" :key="booking.bookingNumber" class="hover:bg-slate-50 transition-colors">
                  <td class="px-4 py-3 font-mono text-xs text-gray-500">{{ booking.bookingNumber }}</td>
                  <td class="px-4 py-3 font-semibold">{{ booking.roomType }} <span v-if="booking.roomNumber" class="text-gray-400">({{ booking.roomNumber }})</span></td>
                  <td class="px-4 py-3">{{ booking.guestName }}</td>
                  <td class="px-4 py-3">{{ booking.checkInDate }}</td>
                  <td class="px-4 py-3">
                    <span :class="{
                      'px-2 py-1 text-xs rounded-full font-bold': true,
                      'bg-yellow-100 text-yellow-700': booking.status === 'PENDING',
                      'bg-blue-100 text-blue-700': booking.status === 'CONFIRMED',
                      'bg-emerald-100 text-emerald-700': booking.status === 'CHECKED_IN',
                      'bg-gray-100 text-gray-700': booking.status === 'CHECKED_OUT' || booking.status === 'CANCELLED'
                    }">
                      {{ booking.status }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>
