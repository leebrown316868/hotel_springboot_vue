<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Layout from '../components/Layout.vue'
import * as echarts from 'echarts'

const statusChartRef = ref<HTMLElement | null>(null)
const trendsChartRef = ref<HTMLElement | null>(null)

const recentBookings = ref([
  { id: '#BK-9281', initials: 'JS', name: '张三', room: '套房 405', status: '已确认', date: '2023-10-24', color: 'blue' },
  { id: '#BK-9282', initials: 'EW', name: '李四', room: '豪华房 201', status: '待处理', date: '2023-10-25', color: 'indigo' },
  { id: '#BK-9283', initials: 'RJ', name: '王五', room: '标准房 102', status: '已确认', date: '2023-10-25', color: 'slate' },
  { id: '#BK-9284', initials: 'ML', name: '赵六', room: '总统套房 801', status: '已确认', date: '2023-10-26', color: 'rose' },
])

onMounted(() => {
  if (statusChartRef.value) {
    const statusChart = echarts.init(statusChartRef.value)
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
        data: [
          { value: 42, name: '空闲', itemStyle: { color: '#10b981' } },
          { value: 65, name: '已入住', itemStyle: { color: '#2563eb' } },
          { value: 8, name: '清洁中', itemStyle: { color: '#f59e0b' } },
          { value: 5, name: '维修中', itemStyle: { color: '#ef4444' } }
        ]
      }]
    })
  }

  if (trendsChartRef.value) {
    const trendsChart = echarts.init(trendsChartRef.value)
    trendsChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
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
        data: [12, 19, 15, 22, 30, 45, 38],
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
})
</script>

<template>
  <Layout>
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">仪表板概览</h1>
        <p class="text-slate-500">欢迎回来，这是今天的实时数据。</p>
      </div>
      <div class="flex gap-3">
        <button class="inline-flex items-center gap-2 px-4 py-2 border border-slate-200 bg-white rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">
          <el-icon><Setting /></el-icon> 管理客房
        </button>
        <button class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 rounded-lg text-sm font-medium text-white hover:bg-blue-700 transition-colors shadow-sm shadow-blue-200">
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
        <div class="text-2xl font-bold">120</div>
        <div class="text-xs text-slate-400 mt-2">包含所有房型</div>
      </div>

      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex items-center justify-between mb-2">
          <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">空闲客房</span>
          <div class="p-2 bg-emerald-50 rounded-lg text-emerald-600"><el-icon :size="20"><Select /></el-icon></div>
        </div>
        <div class="text-2xl font-bold text-emerald-600">42</div>
        <div class="text-xs text-emerald-500 mt-2 font-medium">当前入住率 65%</div>
      </div>

      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex items-center justify-between mb-2">
          <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">今日入住</span>
          <div class="p-2 bg-blue-50 rounded-lg text-blue-600"><el-icon :size="20"><Right /></el-icon></div>
        </div>
        <div class="text-2xl font-bold">18</div>
        <div class="text-xs text-slate-400 mt-2">预计今日到达</div>
      </div>

      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex items-center justify-between mb-2">
          <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">今日退房</span>
          <div class="p-2 bg-orange-50 rounded-lg text-orange-600"><el-icon :size="20"><Back /></el-icon></div>
        </div>
        <div class="text-2xl font-bold">12</div>
        <div class="text-xs text-slate-400 mt-2">待办理离店</div>
      </div>

      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex items-center justify-between mb-2">
          <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">今日收入</span>
          <div class="p-2 bg-indigo-50 rounded-lg text-indigo-600"><el-icon :size="20"><Money /></el-icon></div>
        </div>
        <div class="text-2xl font-bold">¥12,450</div>
        <div class="text-xs text-emerald-600 mt-2 font-medium">较昨日 +12%</div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
      <div class="lg:col-span-1 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h3 class="font-bold text-slate-800 mb-6 flex items-center justify-between">
          客房状态分布
          <el-icon class="text-slate-400"><InfoFilled /></el-icon>
        </h3>
        <div ref="statusChartRef" class="w-full h-64"></div>
      </div>

      <div class="lg:col-span-2 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex items-center justify-between mb-6">
          <h3 class="font-bold text-slate-800">预订趋势 (最近 7 天)</h3>
          <select class="text-sm border border-slate-200 rounded-md focus:ring-blue-500 px-2 py-1">
            <option>周视图</option>
            <option>月视图</option>
          </select>
        </div>
        <div ref="trendsChartRef" class="w-full h-64"></div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
        <h3 class="font-bold text-slate-800">最近预订</h3>
        <button class="text-blue-600 text-sm font-medium hover:underline">查看全部</button>
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
            <tr v-for="booking in recentBookings" :key="booking.id" class="hover:bg-slate-50 transition-colors">
              <td class="px-6 py-4 font-mono text-slate-600">{{ booking.id }}</td>
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div :class="`w-8 h-8 rounded-full bg-${booking.color}-100 text-${booking.color}-600 flex items-center justify-center font-bold text-xs`">
                    {{ booking.initials }}
                  </div>
                  <span class="font-medium text-slate-800">{{ booking.name }}</span>
                </div>
              </td>
              <td class="px-6 py-4">{{ booking.room }}</td>
              <td class="px-6 py-4">
                <span v-if="booking.status === '已确认'" class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800">
                  <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> 已确认
                </span>
                <span v-else class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">
                  <span class="w-1.5 h-1.5 rounded-full bg-amber-500"></span> 待处理
                </span>
              </td>
              <td class="px-6 py-4 text-slate-600">{{ booking.date }}</td>
              <td class="px-6 py-4 text-right">
                <button class="text-slate-400 hover:text-blue-600 transition-colors">
                  <el-icon :size="20"><MoreFilled /></el-icon>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="px-6 py-4 bg-slate-50 border-t border-slate-100 text-xs text-slate-500">
        显示最近 4 条记录
      </div>
    </div>
  </Layout>
</template>
