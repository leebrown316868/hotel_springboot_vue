<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import { settingsApi } from '../api/settings'
import { getRooms } from '../api/room'
import { getActiveRoomTypes, updateRoomType } from '../api/roomType'
import type { RoomTypeConfig, RoomTypeWithStats } from '../types/settings'
import type { RoomResponse } from '../types/room'
import type { RoomTypeResponse } from '../types/roomType'
import { getUser } from '@/utils/auth'

const activeTab = ref('types')
const loading = ref(false)
const roomTypesWithStats = ref<RoomTypeWithStats[]>([])
const roomTypesList = ref<RoomTypeResponse[]>([])
const rooms = ref<RoomResponse[]>([])
const editDialogVisible = ref(false)
const editingRoomType = ref<{ id: number; code: string } & RoomTypeConfig | null>(null)

const searchQuery = ref('')
const filterFloor = ref('')
const filterType = ref('')
const filterStatus = ref('')

const isAdmin = computed(() => getUser()?.role === 'ADMIN')

const roomTypeCodeNames: Record<string, string> = {
  SINGLE: '单人间', DOUBLE: '双人间', SUITE: '套房',
  EXECUTIVE_SUITE: '行政套房', PRESIDENTIAL_SUITE: '总统套房'
}

const fetchRoomTypesConfig = async () => {
  loading.value = true
  try {
    const response = await getActiveRoomTypes()
    const roomTypes = response.data
    roomTypesList.value = roomTypes

    const typesWithStats: RoomTypeWithStats[] = []
    for (const rt of roomTypes) {
      try {
        const stats = await settingsApi.getRoomTypeStats(rt.code)
        typesWithStats.push({ code: rt.code, name: rt.name, capacity: rt.capacity, basePrice: rt.basePrice, roomCount: stats.roomCount, availableCount: stats.availableCount })
      } catch {
        typesWithStats.push({ code: rt.code, name: rt.name, capacity: rt.capacity, basePrice: rt.basePrice, roomCount: 0, availableCount: 0 })
      }
    }
    roomTypesWithStats.value = typesWithStats
  } catch {
    ElMessage.error('加载房型配置失败')
  } finally {
    loading.value = false
  }
}

const handleEditRoomType = (code: string) => {
  const roomType = roomTypesList.value.find(rt => rt.code === code)
  const config = roomTypesWithStats.value.find(t => t.code === code)
  if (roomType && config) {
    editingRoomType.value = { id: roomType.id, code: config.code, name: config.name, capacity: config.capacity, basePrice: config.basePrice }
    editDialogVisible.value = true
  }
}

const handleSaveRoomType = async () => {
  if (!editingRoomType.value) return
  loading.value = true
  try {
    const { id, code, name, capacity, basePrice } = editingRoomType.value
    await updateRoomType(id, { code, name, capacity, basePrice })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await fetchRoomTypesConfig()
    // 通知其他页面房型配置已更新
    localStorage.setItem('room-types-config-updated', Date.now().toString())
  } catch {
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

const fetchRooms = async () => {
  loading.value = true
  try {
    const response = await getRooms({ page: 0, size: 100, number: searchQuery.value || undefined, floor: filterFloor.value || undefined, status: filterStatus.value || undefined })
    if (response.code === 200) {
      rooms.value = response.data.rooms.filter(r => !filterType.value || r.type === filterType.value).sort((a, b) => parseInt(a.number) - parseInt(b.number))
    }
  } finally {
    loading.value = false
  }
}

const resetFilters = () => { searchQuery.value = ''; filterFloor.value = ''; filterType.value = ''; filterStatus.value = ''; fetchRooms() }

const getStatusType = (s: string) => ({ AVAILABLE: 'success', OCCUPIED: 'danger', CLEANING: 'warning', MAINTENANCE: 'info' }[s] || 'info')
const getStatusLabel = (s: string) => ({ AVAILABLE: '空闲', OCCUPIED: '已入住', CLEANING: '清洁中', MAINTENANCE: '维护中' }[s] || s)
const getTypeLabel = (t: string) => roomTypeCodeNames[t] || t

onMounted(() => { fetchRoomTypesConfig(); fetchRooms() })
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto p-6">
      <h1 class="text-2xl font-bold mb-6">客房资源管理</h1>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="房型配置" name="types">
          <el-table :data="roomTypesWithStats" v-loading="loading">
            <el-table-column prop="code" label="代码" width="180" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="capacity" label="容量" width="100">
              <template #default="{ row }">{{ row.capacity }} 人</template>
            </el-table-column>
            <el-table-column prop="basePrice" label="价格" width="120">
              <template #default="{ row }">¥{{ row.basePrice }}</template>
            </el-table-column>
            <el-table-column label="房间数" width="120">
              <template #default="{ row }">{{ row.availableCount }}/{{ row.roomCount }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" :disabled="!isAdmin" @click="handleEditRoomType(row.code)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="客房列表" name="inventory">
          <div class="flex gap-2 mb-4">
            <el-input v-model="searchQuery" placeholder="搜索房间号" clearable @input="fetchRooms" style="width: 200px" />
            <el-select v-model="filterFloor" placeholder="楼层" clearable @change="fetchRooms" style="width: 120px">
              <el-option label="1楼" value="1" /><el-option label="2楼" value="2" /><el-option label="3楼" value="3" /><el-option label="4楼" value="4" /><el-option label="5楼" value="5" />
            </el-select>
            <el-select v-model="filterType" placeholder="房型" clearable @change="fetchRooms" style="width: 140px">
              <el-option label="单人间" value="SINGLE" /><el-option label="双人间" value="DOUBLE" /><el-option label="套房" value="SUITE" /><el-option label="行政套房" value="EXECUTIVE_SUITE" /><el-option label="总统套房" value="PRESIDENTIAL_SUITE" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable @change="fetchRooms" style="width: 120px">
              <el-option label="空闲" value="AVAILABLE" /><el-option label="已入住" value="OCCUPIED" /><el-option label="清洁中" value="CLEANING" /><el-option label="维护中" value="MAINTENANCE" />
            </el-select>
            <el-button @click="resetFilters">重置</el-button>
          </div>
          <el-table :data="rooms" v-loading="loading">
            <el-table-column prop="number" label="房间号" width="100" />
            <el-table-column prop="type" label="房型" width="120">
              <template #default="{ row }">{{ getTypeLabel(row.type) }}</template>
            </el-table-column>
            <el-table-column prop="floor" label="楼层" width="80">
              <template #default="{ row }">{{ row.floor }}楼</template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
    <el-dialog v-model="editDialogVisible" title="编辑房型" width="400px">
      <el-form v-if="editingRoomType" label-width="100px">
        <el-form-item label="代码"><el-input v-model="editingRoomType.code" disabled /></el-form-item>
        <el-form-item label="名称"><el-input v-model="editingRoomType.name" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="editingRoomType.capacity" :min="1" :max="10" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="editingRoomType.basePrice" :min="0" :max="10000" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editDialogVisible = false">取消</el-button><el-button type="primary" :loading="loading" @click="handleSaveRoomType">确定</el-button></template>
    </el-dialog>
  </Layout>
</template>
