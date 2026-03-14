<script setup lang="ts">
import { ref, computed } from 'vue'
import Layout from '../components/Layout.vue'

const loading = ref(false)
const searchQuery = ref('')
const filterFloor = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const rooms = ref([
  { id: 1, number: '101', floor: '1', type: '单人间', status: '空闲', price: 85 },
  { id: 2, number: '102', floor: '1', type: '双人间', status: '已入住', price: 120 },
  { id: 3, number: '201', floor: '2', type: '套房', status: '清洁中', price: 250 },
  { id: 4, number: '205', floor: '2', type: '双人间', status: '空闲', price: 120 },
  { id: 5, number: '302', floor: '3', type: '套房', status: '维修中', price: 300 },
  { id: 6, number: '103', floor: '1', type: '单人间', status: '空闲', price: 85 },
  { id: 7, number: '104', floor: '1', type: '双人间', status: '已入住', price: 120 },
  { id: 8, number: '401', floor: '4', type: '行政套房', status: '空闲', price: 450 },
  { id: 9, number: '202', floor: '2', type: '双人间', status: '清洁中', price: 120 },
  { id: 10, number: '305', floor: '3', type: '单人间', status: '空闲', price: 85 },
  { id: 11, number: '306', floor: '3', type: '双人间', status: '已入住', price: 125 },
  { id: 12, number: '402', floor: '4', type: '套房', status: '空闲', price: 280 },
])

const filteredRooms = computed(() => {
  return rooms.value.filter(room => {
    const matchesSearch = room.number.includes(searchQuery.value)
    const matchesFloor = filterFloor.value ? room.floor === filterFloor.value : true
    const matchesStatus = filterStatus.value ? room.status === filterStatus.value : true
    return matchesSearch && matchesFloor && matchesStatus
  })
})

const paginatedRooms = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRooms.value.slice(start, end)
})

const getStatusClass = (status: string) => {
  const base = 'px-3 py-1 font-medium rounded-full '
  switch(status) {
    case '空闲': return base + 'bg-green-50 text-green-600 border border-green-200'
    case '已入住': return base + 'bg-red-50 text-red-600 border border-red-200'
    case '清洁中': return base + 'bg-yellow-50 text-yellow-600 border border-yellow-200'
    case '维修中': return base + 'bg-orange-50 text-orange-600 border border-orange-200'
    default: return base + 'bg-gray-100 text-gray-600 border border-gray-200'
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  filterFloor.value = ''
  filterStatus.value = ''
}

const handleEdit = (row: any) => {
  console.log('Editing room:', row)
}

const handleView = (row: any) => {
  console.log('Viewing room details:', row)
}
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">客房管理</h1>
          <p class="text-sm text-gray-500 mt-1">管理酒店库存、状态和定价</p>
        </div>
        <div class="flex items-center gap-3">
          <el-button type="primary" size="large" class="flex items-center gap-2 shadow-sm">
            <el-icon><Plus /></el-icon> 添加新客房
          </el-button>
        </div>
      </header>

      <section class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 mb-6">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">搜索</label>
            <el-input v-model="searchQuery" placeholder="房间号..." clearable prefix-icon="Search" />
          </div>
          
          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">楼层</label>
            <el-select v-model="filterFloor" placeholder="所有楼层" clearable class="w-full">
              <el-option label="1 楼" value="1" />
              <el-option label="2 楼" value="2" />
              <el-option label="3 楼" value="3" />
              <el-option label="4 楼" value="4" />
            </el-select>
          </div>
          
          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">状态</label>
            <el-select v-model="filterStatus" placeholder="所有状态" clearable class="w-full">
              <el-option label="空闲" value="空闲" />
              <el-option label="已入住" value="已入住" />
              <el-option label="清洁中" value="清洁中" />
              <el-option label="维修中" value="维修中" />
            </el-select>
          </div>
          
          <div class="flex items-end">
            <el-button @click="resetFilters" class="w-full md:w-auto">重置筛选</el-button>
          </div>
        </div>
      </section>

      <main class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-table :data="paginatedRooms" style="width: 100%" v-loading="loading">
          <el-table-column prop="number" label="房间号" sortable width="120">
            <template #default="scope">
              <span class="font-bold text-gray-700">{{ scope.row.number }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="floor" label="楼层" width="100" />
          <el-table-column prop="type" label="房型">
            <template #default="scope">
              <span class="text-gray-600">{{ scope.row.type }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="150">
            <template #default="scope">
              <span :class="getStatusClass(scope.row.status)">
                {{ scope.row.status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格 / 晚" width="150">
            <template #default="scope">
              <span class="font-semibold text-blue-600">¥{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="right" width="180">
            <template #default="scope">
              <div class="flex justify-end gap-2">
                <el-button size="small" @click="handleView(scope.row)">查看</el-button>
                <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="p-4 border-t border-gray-100 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredRooms.length"
          />
        </div>
      </main>
    </div>
  </Layout>
</template>

<style scoped>
:deep(.el-button--primary) {
  --el-button-bg-color: #2563eb;
  --el-button-border-color: #2563eb;
  --el-button-hover-bg-color: #1d4ed8;
  --el-button-hover-border-color: #1d4ed8;
}
</style>
