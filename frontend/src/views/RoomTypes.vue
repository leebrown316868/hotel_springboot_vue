<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Layout from '../components/Layout.vue'
import {
  getRoomTypes,
  createRoomType,
  updateRoomType,
  deleteRoomType
} from '@/api/roomType'
import type { RoomTypeResponse, RoomTypeRequest } from '@/types/roomType'

const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

const roomTypes = ref<RoomTypeResponse[]>([])

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentRoomType = ref<RoomTypeResponse | null>(null)

const roomTypeForm = ref<RoomTypeRequest>({
  code: '',
  name: '',
  capacity: 2,
  basePrice: 100
})

const fetchRoomTypes = async () => {
  loading.value = true
  try {
    const response = await getRoomTypes({
      page: currentPage.value - 1,
      size: pageSize.value,
      search: searchQuery.value || undefined
    })

    if (response.code === 200 && response.data) {
      roomTypes.value = response.data.roomTypes
      total.value = response.data.total
      totalPages.value = response.data.totalPages
    }
  } catch (error) {
    console.error('Failed to fetch room types:', error)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  currentPage.value = 1
  fetchRoomTypes()
}

const handleCreate = () => {
  dialogMode.value = 'create'
  roomTypeForm.value = {
    code: '',
    name: '',
    capacity: 2,
    basePrice: 100
  }
  dialogVisible.value = true
}

const handleEdit = (row: RoomTypeResponse) => {
  dialogMode.value = 'edit'
  currentRoomType.value = row
  roomTypeForm.value = {
    code: row.code,
    name: row.name,
    capacity: row.capacity,
    basePrice: row.basePrice
  }
  dialogVisible.value = true
}

const handleDelete = async (row: RoomTypeResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除房型 "${row.name}" 吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const response = await deleteRoomType(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchRoomTypes()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete room type:', error)
      if (error.response?.data?.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('删除失败')
      }
    }
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  // 验证表单
  if (!roomTypeForm.value.code || !roomTypeForm.value.name) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (roomTypeForm.value.capacity < 1 || roomTypeForm.value.capacity > 20) {
    ElMessage.warning('容量必须在1-20之间')
    return
  }

  if (roomTypeForm.value.basePrice < 0) {
    ElMessage.warning('基础价格必须大于0')
    return
  }

  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      const response = await createRoomType(roomTypeForm.value)
      if (response.code === 200) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        fetchRoomTypes()
      } else if (response.message) {
        ElMessage.error(response.message)
      }
    } else {
      const response = await updateRoomType(currentRoomType.value!.id, roomTypeForm.value)
      if (response.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchRoomTypes()
      } else if (response.message) {
        ElMessage.error(response.message)
      }
    }
  } catch (error: any) {
    console.error('Failed to save room type:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const getActiveClass = (active: boolean) => {
  return active
    ? 'px-3 py-1 font-medium rounded-full bg-green-50 text-green-600 border border-green-200'
    : 'px-3 py-1 font-medium rounded-full bg-gray-100 text-gray-500 border border-gray-200'
}

const getActiveLabel = (active: boolean) => {
  return active ? '启用' : '禁用'
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

watch([currentPage, pageSize], () => {
  fetchRoomTypes()
})

onMounted(() => {
  fetchRoomTypes()
})
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">房型管理</h1>
          <p class="text-sm text-gray-500 mt-1">管理酒店房型配置、容量和定价</p>
        </div>
        <div class="flex items-center gap-3">
          <el-button type="primary" size="large" class="flex items-center gap-2 shadow-sm" @click="handleCreate">
            <el-icon><Plus /></el-icon> 添加新房型
          </el-button>
        </div>
      </header>

      <section class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 mb-6">
        <div class="flex gap-4">
          <div class="flex-1">
            <el-input
              v-model="searchQuery"
              placeholder="搜索房型代码或名称..."
              clearable
              prefix-icon="Search"
              @input="currentPage = 1; fetchRoomTypes()"
            />
          </div>
          <div>
            <el-button @click="resetFilters">重置筛选</el-button>
          </div>
        </div>
      </section>

      <main class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-table :data="roomTypes" style="width: 100%" v-loading="loading">
          <el-table-column prop="code" label="房型代码" width="180">
            <template #default="scope">
              <span class="font-mono font-bold text-gray-700">{{ scope.row.code }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="房型名称">
            <template #default="scope">
              <span class="text-gray-900 font-medium">{{ scope.row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="capacity" label="容量" width="100">
            <template #default="scope">
              {{ scope.row.capacity }} 人
            </template>
          </el-table-column>
          <el-table-column prop="basePrice" label="基础价格" width="150">
            <template #default="scope">
              <span class="font-semibold text-blue-600">¥{{ scope.row.basePrice }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="roomCount" label="房间数" width="100">
            <template #default="scope">
              <span class="text-gray-600">{{ scope.row.roomCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="active" label="状态" width="100">
            <template #default="scope">
              <span :class="getActiveClass(scope.row.active)">{{ getActiveLabel(scope.row.active) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="right" width="180">
            <template #default="scope">
              <div class="flex justify-end gap-2">
                <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  :disabled="scope.row.roomCount > 0"
                  @click="handleDelete(scope.row)"
                >
                  删除
                </el-button>
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
        :title="dialogMode === 'create' ? '添加新房型' : '编辑房型'"
        width="500px"
      >
        <el-form :model="roomTypeForm" label-width="100px">
          <el-form-item label="房型代码" required>
            <el-input
              v-model="roomTypeForm.code"
              placeholder="例如：SINGLE"
              :disabled="dialogMode === 'edit'"
            />
            <span class="text-xs text-gray-500">只能包含大写字母、下划线和数字</span>
          </el-form-item>
          <el-form-item label="房型名称" required>
            <el-input v-model="roomTypeForm.name" placeholder="例如：单人间" />
          </el-form-item>
          <el-form-item label="容量" required>
            <el-input-number v-model="roomTypeForm.capacity" :min="1" :max="20" class="w-full" />
            <span class="text-xs text-gray-500">该房型可容纳的最大人数</span>
          </el-form-item>
          <el-form-item label="基础价格" required>
            <el-input-number v-model="roomTypeForm.basePrice" :min="0.01" :precision="2" class="w-full" />
            <span class="text-xs text-gray-500">每晚的基础价格（元）</span>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="saving">确定</el-button>
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
