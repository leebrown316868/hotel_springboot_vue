<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, OfficeBuilding, Location, Phone, Message } from '@element-plus/icons-vue'
import SimpleHeader from '@/components/SimpleHeader.vue'
import { settingsApi } from '@/api/settings'
import type { PublicSettings } from '@/types/settings'

const router = useRouter()
const loading = ref(false)
const hotelInfo = ref<PublicSettings | null>(null)

const fetchHotelInfo = async () => {
  loading.value = true
  try {
    hotelInfo.value = await settingsApi.getPublicSettings()
  } catch (error) {
    console.error('获取酒店信息失败:', error)
    ElMessage.error('获取酒店信息失败')
  } finally {
    loading.value = false
  }
}

const goHome = () => {
  router.push('/browse-rooms')
}

onMounted(() => {
  fetchHotelInfo()
})
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <SimpleHeader />

    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 页面标题 -->
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">关于我们</h1>
        <p class="text-sm text-gray-500 mt-1">了解 {{ hotelInfo?.hotelName || '酒店' }}</p>
      </header>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-12">
        <el-icon class="is-loading text-4xl text-blue-600"><Loading /></el-icon>
      </div>

      <!-- 酒店信息 -->
      <div v-else-if="hotelInfo" class="space-y-6">
        <!-- 酒店名称卡片 -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <div class="flex items-center mb-4">
            <div class="w-12 h-12 bg-blue-600 rounded-lg flex items-center justify-center text-white mr-4">
              <el-icon :size="24"><OfficeBuilding /></el-icon>
            </div>
            <h2 class="text-2xl font-bold text-gray-900">{{ hotelInfo.hotelName }}</h2>
          </div>
        </div>

        <!-- 酒店简介卡片 -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">酒店简介</h3>
          <p class="text-gray-700 leading-relaxed">{{ hotelInfo.description }}</p>
        </div>

        <!-- 联系方式卡片 -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">联系方式</h3>
          <div class="space-y-4">
            <div class="flex items-center text-gray-700">
              <el-icon class="mr-3 text-blue-600"><Location /></el-icon>
              <span>{{ hotelInfo.address }}</span>
            </div>
            <div class="flex items-center text-gray-700">
              <el-icon class="mr-3 text-blue-600"><Phone /></el-icon>
              <span>{{ hotelInfo.contactPhone }}</span>
            </div>
            <div class="flex items-center text-gray-700">
              <el-icon class="mr-3 text-blue-600"><Message /></el-icon>
              <span>{{ hotelInfo.contactEmail }}</span>
            </div>
          </div>
        </div>

        <!-- 返回首页按钮 -->
        <div class="flex justify-center pt-4">
          <el-button @click="goHome" type="primary" size="large">
            返回首页
          </el-button>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="bg-white rounded-2xl shadow-sm border border-gray-100 p-12 text-center">
        <el-icon class="text-6xl text-gray-300 mb-4"><OfficeBuilding /></el-icon>
        <h3 class="text-xl font-semibold text-gray-900 mb-2">加载失败</h3>
        <p class="text-gray-500 mb-6">无法获取酒店信息，请稍后重试</p>
        <el-button type="primary" @click="fetchHotelInfo">重新加载</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 可以添加额外的样式 */
</style>
