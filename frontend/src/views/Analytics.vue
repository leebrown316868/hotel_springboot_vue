<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Layout from '../components/Layout.vue'
import * as echarts from 'echarts'

const revenueChartRef = ref<HTMLElement | null>(null)
const occupancyChartRef = ref<HTMLElement | null>(null)
const guestSourceChartRef = ref<HTMLElement | null>(null)

const kpis = [
  { label: '入住率', value: '78.5%', change: '+4.2%', icon: 'House', color: 'blue' },
  { label: '总收入', value: '¥142,500', change: '+12.8%', icon: 'Money', color: 'emerald' },
  { label: '平均房价', value: '¥185', change: '-2.1%', icon: 'TrendCharts', color: 'indigo' },
  { label: '净利润', value: '¥45,200', change: '+8.5%', icon: 'Wallet', color: 'rose' },
]

onMounted(() => {
  if (revenueChartRef.value) {
    const revenueChart = echarts.init(revenueChartRef.value)
    revenueChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
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
        name: '收入',
        type: 'bar',
        data: [12000, 15000, 18000, 14000, 22000, 28000, 32000, 35000, 29000, 24000, 21000, 26000],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2563eb' },
            { offset: 1, color: '#60a5fa' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }]
    })
  }

  if (occupancyChartRef.value) {
    const occupancyChart = echarts.init(occupancyChartRef.value)
    occupancyChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisLabel: { color: '#94a3b8' }
      },
      yAxis: {
        type: 'value',
        max: 100,
        axisLine: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } },
        axisLabel: { color: '#94a3b8', formatter: '{value}%' }
      },
      series: [{
        name: '入住率',
        type: 'line',
        smooth: true,
        data: [65, 72, 68, 85, 92, 98, 95],
        lineStyle: { width: 4, color: '#10b981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.2)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0)' }
          ])
        }
      }]
    })
  }

  if (guestSourceChartRef.value) {
    const guestSourceChart = echarts.init(guestSourceChartRef.value)
    guestSourceChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center', icon: 'circle' },
      series: [{
        name: '客源分析',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: [
          { value: 45, name: '直接预订', itemStyle: { color: '#2563eb' } },
          { value: 25, name: '携程', itemStyle: { color: '#10b981' } },
          { value: 15, name: '飞猪', itemStyle: { color: '#f59e0b' } },
          { value: 10, name: '美团', itemStyle: { color: '#ef4444' } },
          { value: 5, name: '其他', itemStyle: { color: '#94a3b8' } }
        ]
      }]
    })
  }
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
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            size="large"
          />
          <el-button type="primary" size="large" icon="Download">导出报表</el-button>
        </div>
      </header>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div v-for="kpi in kpis" :key="kpi.label" class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-slate-500 text-sm font-medium uppercase tracking-wider">{{ kpi.label }}</span>
            <div :class="`p-2 bg-${kpi.color}-50 rounded-lg text-${kpi.color}-600`">
              <el-icon :size="20"><component :is="kpi.icon" /></el-icon>
            </div>
          </div>
          <div class="text-2xl font-bold">{{ kpi.value }}</div>
          <div class="flex items-center gap-1 mt-2">
            <span :class="kpi.change.startsWith('+') ? 'text-emerald-600' : 'text-rose-600'" class="text-xs font-bold">
              {{ kpi.change }}
            </span>
            <span class="text-xs text-slate-400">较上月</span>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 class="font-bold text-slate-800 mb-6">收入增长 (月度)</h3>
          <div ref="revenueChartRef" class="w-full h-80"></div>
        </div>
        <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 class="font-bold text-slate-800 mb-6">入住率 (周)</h3>
          <div ref="occupancyChartRef" class="w-full h-80"></div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-1 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 class="font-bold text-slate-800 mb-6">客源分析</h3>
          <div ref="guestSourceChartRef" class="w-full h-80"></div>
        </div>
        <div class="lg:col-span-2 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 class="font-bold text-slate-800 mb-6">表现最佳的房型</h3>
          <div class="overflow-x-auto">
            <table class="w-full text-left">
              <thead>
                <tr class="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider">
                  <th class="px-6 py-4">房型</th>
                  <th class="px-6 py-4">入住率</th>
                  <th class="px-6 py-4">收入</th>
                  <th class="px-6 py-4">趋势</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100 text-sm">
                <tr class="hover:bg-slate-50 transition-colors">
                  <td class="px-6 py-4 font-bold">豪华海景房</td>
                  <td class="px-6 py-4">92%</td>
                  <td class="px-6 py-4 font-bold text-blue-600">¥42,500</td>
                  <td class="px-6 py-4 text-emerald-600 font-bold">↑ 12%</td>
                </tr>
                <tr class="hover:bg-slate-50 transition-colors">
                  <td class="px-6 py-4 font-bold">行政套房</td>
                  <td class="px-6 py-4">88%</td>
                  <td class="px-6 py-4 font-bold text-blue-600">¥38,200</td>
                  <td class="px-6 py-4 text-emerald-600 font-bold">↑ 8%</td>
                </tr>
                <tr class="hover:bg-slate-50 transition-colors">
                  <td class="px-6 py-4 font-bold">标准大床房</td>
                  <td class="px-6 py-4">75%</td>
                  <td class="px-6 py-4 font-bold text-blue-600">¥28,900</td>
                  <td class="px-6 py-4 text-rose-600 font-bold">↓ 3%</td>
                </tr>
                <tr class="hover:bg-slate-50 transition-colors">
                  <td class="px-6 py-4 font-bold">家庭套房</td>
                  <td class="px-6 py-4">82%</td>
                  <td class="px-6 py-4 font-bold text-blue-600">¥31,400</td>
                  <td class="px-6 py-4 text-emerald-600 font-bold">↑ 5%</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>
