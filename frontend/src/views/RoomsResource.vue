<script setup lang="ts">
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const activeTab = ref('types')

const roomTypes = ref([
  { id: 1, name: '标准间', capacity: 2, basePrice: '¥150', count: 20 },
  { id: 2, name: '豪华海景房', capacity: 2, basePrice: '¥220', count: 15 },
  { id: 3, name: '行政商务套房', capacity: 2, basePrice: '¥350', count: 10 },
  { id: 4, name: '总统套房', capacity: 4, basePrice: '¥850', count: 2 },
])

const roomInventory = ref([
  { id: '101', type: '标准间', floor: 1, status: 'AVAILABLE' },
  { id: '102', type: '标准间', floor: 1, status: 'OCCUPIED' },
  { id: '201', type: '豪华海景房', floor: 2, status: 'CLEANING' },
  { id: '305', type: '行政商务套房', floor: 3, status: 'MAINTENANCE' },
  { id: '401', type: '总统套房', floor: 4, status: 'AVAILABLE' },
])

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'AVAILABLE': 'success',
    'OCCUPIED': 'danger',
    'CLEANING': 'warning',
    'MAINTENANCE': 'info',
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'AVAILABLE': '空闲可用',
    'OCCUPIED': '已入住',
    'CLEANING': '清洁中',
    'MAINTENANCE': '维护中',
  }
  return map[status] || status
}
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="mb-8 flex justify-between items-end">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">客房资源管理</h1>
          <p class="text-sm text-gray-500 mt-1">管理房型配置、客房详细信息及维护记录</p>
        </div>
        <el-button type="primary" icon="Plus">新增资源</el-button>
      </header>
      
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-tabs v-model="activeTab" class="px-6 pt-4">
          <el-tab-pane label="房型配置" name="types">
            <el-table :data="roomTypes" style="width: 100%" class="mt-4 mb-6">
              <el-table-column prop="name" label="房型名称" />
              <el-table-column prop="capacity" label="容纳人数" />
              <el-table-column prop="basePrice" label="基础价格" />
              <el-table-column prop="count" label="房间数量" />
              <el-table-column label="操作" width="150">
                <template #default>
                  <el-button size="small" text type="primary">编辑</el-button>
                  <el-button size="small" text type="danger">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="客房列表" name="inventory">
            <div class="flex gap-4 mb-4 mt-2">
              <el-input placeholder="搜索房间号..." prefix-icon="Search" class="w-64" />
              <el-select placeholder="房型" clearable class="w-48">
                <el-option v-for="type in roomTypes" :key="type.id" :label="type.name" :value="type.name" />
              </el-select>
            </div>
            <el-table :data="roomInventory" style="width: 100%" class="mb-6">
              <el-table-column prop="id" label="房间号" />
              <el-table-column prop="type" label="房型" />
              <el-table-column prop="floor" label="楼层" />
              <el-table-column label="状态">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default>
                  <el-button size="small" text type="primary">设置状态</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </Layout>
</template>
