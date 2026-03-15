<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Layout from '../components/Layout.vue'
import {
  getRooms,
  getRoomById,
  createRoom,
  updateRoom,
  deleteRoom,
  updateRoomStatus
} from '@/api/room'
import type { RoomResponse, RoomRequest } from '@/types/room'

const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')
const filterFloor = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

const rooms = ref<RoomResponse[]>([])

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const viewDialogVisible = ref(false)
const currentRoom = ref<RoomResponse | null>(null)

const roomForm = ref<RoomRequest>({
  number: '',
  floor: '',
  type: '',
  status: 'AVAILABLE',
  price: 0,
  capacity: 1
})

const roomTypeOptions = [
  { label: '单人间', value: 'SINGLE' },
  { label: '双人间', value: 'DOUBLE' },
  { label: '套房', value: 'SUITE' },
  { label: '行政套房', value: 'EXECUTIVE_SUITE' },
  { label: '总统套房', value: 'PRESIDENTIAL_SUITE' }
]

const roomStatusOptions = [
  { label: '空闲', value: 'AVAILABLE' },
  { label: '已入住', value: 'OCCUPIED' },
  { label: '清洁中', value: 'CLEANING' },
  { label: '维修中', value: 'MAINTENANCE' }
]

const fetchRooms = async () => {
  loading.value = true
  try {
    const response = await getRooms({
      page: currentPage.value - 1,
      size: pageSize.value,
      number: searchQuery.value || undefined,
      floor: filterFloor.value || undefined,
      status: filterStatus.value || undefined
    })

    if (response.code === 200 && response.data) {
      // 按房间号排序（字符串排序，支持数字房间号）
      rooms.value = response.data.rooms.sort((a, b) => {
        const numA = parseInt(a.number)
        const numB = parseInt(b.number)
        if (!isNaN(numA) && !isNaN(numB)) {
          return numA - numB
        }
        return a.number.localeCompare(b.number)
      })
      total.value = response.data.total
      totalPages.value = response.data.totalPages
    }
  } catch (error) {
    console.error('Failed to fetch rooms:', error)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  filterFloor.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  fetchRooms()
}

const handleCreate = () => {
  dialogMode.value = 'create'
  roomForm.value = {
    number: '',
    floor: '',
    type: '',
    status: 'AVAILABLE',
    price: 0,
    capacity: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row: RoomResponse) => {
  dialogMode.value = 'edit'
  currentRoom.value = row
  roomForm.value = {
    number: row.number,
    floor: row.floor,
    type: row.type,
    status: row.status,
    price: row.price,
    capacity: row.capacity
  }
  dialogVisible.value = true
}

const handleView = async (row: RoomResponse) => {
  loading.value = true
  try {
    const response = await getRoomById(row.id)
    if (response.code === 200 && response.data) {
      currentRoom.value = response.data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('Failed to fetch room details:', error)
    ElMessage.error('获取房间详情失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row: RoomResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除房间 ${row.number} 吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const response = await deleteRoom(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchRooms()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete room:', error)
      ElMessage.error('删除失败')
    }
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (row: RoomResponse, newStatus: string) => {
  loading.value = true
  try {
    const response = await updateRoomStatus(row.id, newStatus)
    if (response.code === 200) {
      ElMessage.success('状态更新成功')
      fetchRooms()
    }
  } catch (error) {
    console.error('Failed to update room status:', error)
    ElMessage.error('状态更新失败')
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      const response = await createRoom(roomForm.value)
      if (response.code === 200) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        fetchRooms()
      }
    } else {
      const response = await updateRoom(currentRoom.value!.id, roomForm.value)
      if (response.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchRooms()
      }
    }
  } catch (error) {
    console.error('Failed to save room:', error)
  } finally {
    saving.value = false
  }
}

const getStatusClass = (status: string) => {
  const base = 'px-3 py-1 font-medium rounded-full '
  switch(status) {
    case 'AVAILABLE': return base + 'bg-green-50 text-green-600 border border-green-200'
    case 'OCCUPIED': return base + 'bg-red-50 text-red-600 border border-red-200'
    case 'CLEANING': return base + 'bg-yellow-50 text-yellow-600 border border-yellow-200'
    case 'MAINTENANCE': return base + 'bg-orange-50 text-orange-600 border border-orange-200'
    default: return base + 'bg-gray-100 text-gray-600 border border-gray-200'
  }
}

const getStatusLabel = (status: string) => {
  switch(status) {
    case 'AVAILABLE': return '空闲'
    case 'OCCUPIED': return '已入住'
    case 'CLEANING': return '清洁中'
    case 'MAINTENANCE': return '维修中'
    default: return status
  }
}

const getTypeLabel = (type: string) => {
  switch(type) {
    case 'SINGLE': return '单人间'
    case 'DOUBLE': return '双人间'
    case 'SUITE': return '套房'
    case 'EXECUTIVE_SUITE': return '行政套房'
    case 'PRESIDENTIAL_SUITE': return '总统套房'
    default: return type
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

watch([currentPage, pageSize], () => {
  fetchRooms()
})

onMounted(() => {
  fetchRooms()
})
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
          <el-button type="primary" size="large" class="flex items-center gap-2 shadow-sm" @click="handleCreate">
            <el-icon><Plus /></el-icon> 添加新客房
          </el-button>
        </div>
      </header>

      <section class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 mb-6">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">搜索</label>
            <el-input v-model="searchQuery" placeholder="房间号..." clearable prefix-icon="Search" @input="currentPage = 1; fetchRooms()" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">楼层</label>
            <el-select v-model="filterFloor" placeholder="所有楼层" clearable class="w-full" @change="currentPage = 1; fetchRooms()">
              <el-option label="1 楼" value="1" />
              <el-option label="2 楼" value="2" />
              <el-option label="3 楼" value="3" />
              <el-option label="4 楼" value="4" />
              <el-option label="5 楼" value="5" />
            </el-select>
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">状态</label>
            <el-select v-model="filterStatus" placeholder="所有状态" clearable class="w-full" @change="currentPage = 1; fetchRooms()">
              <el-option label="空闲" value="AVAILABLE" />
              <el-option label="已入住" value="OCCUPIED" />
              <el-option label="清洁中" value="CLEANING" />
              <el-option label="维修中" value="MAINTENANCE" />
            </el-select>
          </div>

          <div class="flex items-end">
            <el-button @click="resetFilters" class="w-full md:w-auto">重置筛选</el-button>
          </div>
        </div>
      </section>

      <main class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-table :data="rooms" style="width: 100%" v-loading="loading">
          <el-table-column prop="number" label="房间号" sortable width="120">
            <template #default="scope">
              <span class="font-bold text-gray-700">{{ scope.row.number }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="floor" label="楼层" width="100">
            <template #default="scope">
              {{ scope.row.floor }} 楼
            </template>
          </el-table-column>
          <el-table-column prop="type" label="房型">
            <template #default="scope">
              <span class="text-gray-600">{{ getTypeLabel(scope.row.type) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="150">
            <template #default="scope">
              <el-select
                :model-value="scope.row.status"
                @change="(val: string) => handleStatusChange(scope.row, val)"
                size="small"
                :class="getStatusClass(scope.row.status)"
              >
                <el-option v-for="option in roomStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格 / 晚" width="150">
            <template #default="scope">
              <span class="font-semibold text-blue-600">¥{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="right" width="220">
            <template #default="scope">
              <div class="flex justify-end gap-2">
                <el-button size="small" @click="handleView(scope.row)">查看</el-button>
                <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
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
            :total="total"
          />
        </div>
      </main>

      <!-- Create/Edit Dialog -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogMode === 'create' ? '添加新客房' : '编辑客房'"
        width="500px"
      >
        <el-form :model="roomForm" label-width="80px">
          <el-form-item label="房间号" required>
            <el-input v-model="roomForm.number" placeholder="例如：101" />
          </el-form-item>
          <el-form-item label="楼层" required>
            <el-input v-model="roomForm.floor" placeholder="例如：1" />
          </el-form-item>
          <el-form-item label="房型" required>
            <el-select v-model="roomForm.type" placeholder="请选择房型" class="w-full">
              <el-option v-for="option in roomTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" required>
            <el-select v-model="roomForm.status" placeholder="请选择状态" class="w-full">
              <el-option v-for="option in roomStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="价格" required>
            <el-input-number v-model="roomForm.price" :min="0" :precision="2" class="w-full" />
          </el-form-item>
          <el-form-item label="容量" required>
            <el-input-number v-model="roomForm.capacity" :min="1" :max="10" class="w-full" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="saving">确定</el-button>
        </template>
      </el-dialog>

      <!-- View Details Dialog -->
      <el-dialog
        v-model="viewDialogVisible"
        title="客房详情"
        width="500px"
      >
        <div v-if="currentRoom" class="space-y-4">
          <div class="flex justify-between items-center py-2 border-b">
            <span class="text-gray-500">房间号</span>
            <span class="font-semibold">{{ currentRoom.number }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b">
            <span class="text-gray-500">楼层</span>
            <span class="font-semibold">{{ currentRoom.floor }} 楼</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b">
            <span class="text-gray-500">房型</span>
            <span class="font-semibold">{{ getTypeLabel(currentRoom.type) }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b">
            <span class="text-gray-500">状态</span>
            <span :class="getStatusClass(currentRoom.status)">{{ getStatusLabel(currentRoom.status) }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b">
            <span class="text-gray-500">价格</span>
            <span class="font-semibold text-blue-600">¥{{ currentRoom.price }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b">
            <span class="text-gray-500">容量</span>
            <span class="font-semibold">{{ currentRoom.capacity }} 人</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b">
            <span class="text-gray-500">创建时间</span>
            <span class="text-sm text-gray-600">{{ formatDate(currentRoom.createdAt as string) }}</span>
          </div>
          <div class="flex justify-between items-center py-2">
            <span class="text-gray-500">更新时间</span>
            <span class="text-sm text-gray-600">{{ formatDate(currentRoom.updatedAt as string) }}</span>
          </div>
        </div>
        <template #footer>
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
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
