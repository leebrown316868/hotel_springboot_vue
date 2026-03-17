<script setup lang="ts">
import { useRouter } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'

const router = useRouter()

const handleLogin = () => {
  router.push('/login')
}

const handleHome = () => {
  router.push('/browse-rooms')
}

const handleAbout = () => {
  router.push('/about')
}
</script>

<template>
  <header class="bg-white shadow-sm sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <!-- Logo和品牌名 -->
        <div class="flex items-center cursor-pointer" @click="handleHome">
          <div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center text-white mr-3">
            <el-icon :size="24"><OfficeBuilding /></el-icon>
          </div>
          <span class="font-bold text-xl tracking-tight text-gray-900">GrandHotel</span>
        </div>

        <!-- 导航链接 -->
        <nav class="hidden md:flex items-center space-x-8">
          <a @click="handleHome" class="text-gray-700 hover:text-blue-600 font-medium cursor-pointer transition-colors">
            首页
          </a>
          <a @click="handleAbout" class="text-gray-700 hover:text-blue-600 font-medium cursor-pointer transition-colors">
            关于我们
          </a>
        </nav>

        <!-- 右侧操作 -->
        <div class="flex items-center space-x-4">
          <template v-if="!isAuthenticated()">
            <el-button type="primary" @click="handleLogin">
              登录 / 注册
            </el-button>
          </template>
          <template v-else>
            <router-link to="/dashboard">
              <el-button>进入后台</el-button>
            </router-link>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* 可以添加额外的样式 */
</style>
