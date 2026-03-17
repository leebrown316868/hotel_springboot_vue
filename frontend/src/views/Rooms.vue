<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadUserFile, UploadProps, UploadRawFile } from 'element-plus'
import Layout from '../components/Layout.vue'
import {
  getRooms,
  getRoomById,
  createRoom,
  updateRoom,
  deleteRoom,
  updateRoomStatus,
  uploadRoomImage
} from '@/api/room'
import { settingsApi } from '@/api/settings'
import type { RoomResponse, RoomRequest } from '@/types/room'
import type { RoomTypeConfig } from '@/types/settings'
import { getUser } from '@/utils/auth'
import axios from 'axios'

const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')
const filterFloor = ref('')
const filterType = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

const rooms = ref<RoomResponse[]>([])
const roomTypesConfig = ref<Record<string, RoomTypeConfig>>({})

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

// 图片上传相关
const imageFileList = ref<UploadUserFile[]>([])
const uploadingImages = ref(false)

// 上传URL
const uploadUrl = computed(() => {
  if (dialogMode.value === 'edit' && currentRoom.value) {
    return `/api/rooms/${currentRoom.value.id}/images`
  }
  return ''
})

// 上传请求头
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    'Authorization': token ? `Bearer ${token}` : ''
  }
})

// 动态房型选项，从系统设置获取
const roomTypeOptions = computed(() => {
  return Object.entries(roomTypesConfig.value).map(([code, config]) => ({
    label: config.name,
    value: code
  }))
})

// 表格 key，用于房型配置更新时强制重新渲染
const tableKey = computed(() => {
  return Object.keys(roomTypesConfig.value).length
})

const roomStatusOptions = [
  { label: '空闲', value: 'AVAILABLE' },
  { label: '已入住', value: 'OCCUPIED' },
  { label: '清洁中', value: 'CLEANING' },
  { label: '维修中', value: 'MAINTENANCE' }
]

const canCreateRoom = computed(() => {
  const role = getUser()?.role
  return role === 'ADMIN' || role === 'STAFF'
})

const isAdmin = computed(() => getUser()?.role === 'ADMIN')

const fetchRooms = async () => {
  loading.value = true
  try {
    const response = await getRooms({
      page: currentPage.value - 1,
      size: pageSize.value,
      number: searchQuery.value || undefined,
      floor: filterFloor.value || undefined,
      type: filterType.value || undefined,
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

const fetchRoomTypesConfig = async () => {
  try {
    roomTypesConfig.value = await settingsApi.getRoomTypesConfig()
    // 配置加载完成后，重新渲染房间列表以更新房型名称显示
    // 通过触发一个微小的更新来强制重新渲染
    rooms.value = [...rooms.value]
  } catch (error) {
    console.error('Failed to fetch room types config:', error)
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  filterFloor.value = ''
  filterType.value = ''
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
    capacity: 1 // 默认值，实际会从房型配置获取
  }
  dialogVisible.value = true
}

// 监听房型变化，自动填充容量和默认价格（仅在创建新房间时）
watch(() => roomForm.value.type, (newType, oldType) => {
  if (newType && roomTypesConfig.value[newType]) {
    const config = roomTypesConfig.value[newType]
    // 容量始终从房型配置获取
    roomForm.value.capacity = config.capacity

    // 只在创建模式且房型从空变为有值时自动填充价格
    // 编辑模式或不改变房型时，保持原有价格不变
    if (dialogMode.value === 'create' && !oldType) {
      roomForm.value.price = config.basePrice
    }
  }
})

const handleEdit = async (row: RoomResponse) => {
  // 每次打开编辑对话框时重新获取最新的房型配置，确保与数据库同步
  await fetchRoomTypesConfig()

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

  // 加载现有图片
  imageFileList.value = []
  if (row.images && row.images !== '') {
    try {
      const images = JSON.parse(row.images)
      if (Array.isArray(images)) {
        imageFileList.value = images.map((url: string, index: number) => ({
          name: `image_${index + 1}`,
          url: url.startsWith('http') ? url : `http://localhost:8080${url}`,
          uid: Date.now() + index
        }))
      }
    } catch (e) {
      console.error('Failed to parse images:', e)
    }
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
      await fetchRooms()
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
      // 收集图片URL
      const images = imageFileList.value
        .filter(file => file.url)
        .map(file => {
          // 如果是上传的图片，URL是完整路径，需要转为相对路径
          if (file.url && file.url.startsWith('http://localhost:8080/uploads')) {
            return file.url.replace('http://localhost:8080', '')
          }
          return file.url || ''
        })

      // 更新房间信息（包含图片）
      const response = await updateRoom(currentRoom.value!.id, {
        ...roomForm.value,
        images: JSON.stringify(images)
      })
      if (response.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        // 刷新房间列表和房型配置，确保显示最新的数据
        await Promise.all([fetchRooms(), fetchRoomTypesConfig()])
      }
    }
  } catch (error) {
    console.error('Failed to save room:', error)
  } finally {
    saving.value = false
  }
}

// 图片上传前验证
const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('只支持 JPG、PNG、WEBP 格式的图片')
    return false
  }

  // 验证文件大小（5MB）
  const maxSize = 5 * 1024 * 1024
  if (rawFile.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }

  // 验证图片数量
  if (imageFileList.value.length >= 5) {
    ElMessage.error('最多只能上传 5 张图片')
    return false
  }

  return true
}

// 图片上传成功
const handleUploadSuccess: UploadProps['onSuccess'] = (response, file) => {
  if (response.code === 200) {
    // 更新文件URL
    file.url = `http://localhost:8080${response.data}`
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
    // 从列表中移除失败的文件
    const index = imageFileList.value.indexOf(file)
    if (index > -1) {
      imageFileList.value.splice(index, 1)
    }
  }
}

// 图片移除
const handleRemove: UploadProps['onRemove'] = (file) => {
  const index = imageFileList.value.indexOf(file)
  if (index > -1) {
    imageFileList.value.splice(index, 1)
  }
}

// 图片预览
const handlePreview: UploadProps['onPreview'] = (file) => {
  window.open(file.url, '_blank')
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
  return roomTypesConfig.value[type]?.name || type
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

watch([currentPage, pageSize], () => {
  fetchRooms()
})

// 监听房型配置更新事件的处理函数
const handleStorageChange = (e: StorageEvent) => {
  if (e.key === 'room-types-config-updated' && e.newValue) {
    fetchRoomTypesConfig()
  }
}

onMounted(async () => {
  // 先加载房型配置，再加载房间列表
  await fetchRoomTypesConfig()
  fetchRooms()

  // 监听房型配置更新事件
  window.addEventListener('storage', handleStorageChange)
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('storage', handleStorageChange)
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
          <el-button v-if="canCreateRoom" type="primary" size="large" class="flex items-center gap-2 shadow-sm" @click="handleCreate">
            <el-icon><Plus /></el-icon> 添加新客房
          </el-button>
        </div>
      </header>

      <section class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 mb-6">
        <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
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
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">房型</label>
            <el-select v-model="filterType" placeholder="所有房型" clearable class="w-full" @change="currentPage = 1; fetchRooms()">
              <el-option v-for="option in roomTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
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
        <el-table :data="rooms" :key="tableKey" style="width: 100%" v-loading="loading">
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
          <el-table-column prop="capacity" label="容量" width="100">
            <template #default="scope">
              <span class="text-gray-600">{{ scope.row.capacity }} 人</span>
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
                <el-button v-if="isAdmin" size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
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
            <div v-if="roomForm.type && roomTypesConfig.value && roomTypesConfig.value[roomForm.type]" class="text-xs text-gray-500 mt-1">
              容量：{{ roomTypesConfig.value[roomForm.type]?.capacity || 0 }} 人（由房型配置决定）
            </div>
          </el-form-item>
          <el-form-item label="价格" required>
            <el-input-number
              v-model="roomForm.price"
              :min="0"
              :precision="2"
              :disabled="!isAdmin"
              class="w-full"
            />
            <div class="text-xs text-gray-500 mt-1">
              <template v-if="isAdmin">
                可手动调整房间价格（创建时默认使用房型配置价格）
              </template>
              <template v-else>
                房间价格由管理员设定，当前显示价格
              </template>
            </div>
          </el-form-item>
          <el-form-item label="房间图片">
            <div v-if="dialogMode === 'create'" class="text-sm text-gray-500">
              创建房间后可以在编辑页面上传图片
            </div>
            <el-upload
              v-else
              v-model:file-list="imageFileList"
              :action="uploadUrl"
              :headers="uploadHeaders"
              list-type="picture-card"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              :limit="5"
              accept=".jpg,.jpeg,.png,.webp"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="text-xs text-gray-500 mt-1">
              支持 JPG、PNG、WEBP 格式，最大 5MB，最多 5 张图片
            </div>
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
