<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { isAuthenticated, getUser, logout } from '@/utils/auth'
import { settingsApi } from '@/api/settings'

const router = useRouter()
const route = useRoute()
const hotelName = ref('GrandHotel')
const currentUser = ref(getUser())

// 获取酒店设置
onMounted(async () => {
  try {
    const settings = await settingsApi.getPublicSettings()
    hotelName.value = settings.hotelName || 'GrandHotel'
  } catch (error) {
    console.error('获取酒店设置失败:', error)
  }
})

// 判断是否为管理员或员工
const isStaffOrAdmin = computed(() => {
  const role = currentUser.value?.role
  return role === 'ADMIN' || role === 'STAFF'
})

// 判断是否为客户
const isCustomer = computed(() => {
  const role = currentUser.value?.role
  return role === 'CUSTOMER'
})

// 获取角色标签
const getRoleLabel = (role?: string): string => {
  const roleMap: Record<string, string> = {
    'ADMIN': '系统管理员',
    'MANAGER': '物业经理',
    'RECEPTIONIST': '前台员工',
    'GUEST': '住客',
    'STAFF': '前台员工',
    'CUSTOMER': '客户'
  }
  return role ? (roleMap[role] || role) : '访客'
}

// 用户模块菜单项配置
const customerMenuItems = [
  { path: '/bookings/new', label: '在线预订', icon: 'ShoppingCart' },
  { path: '/my-bookings', label: '我的订单', icon: 'Tickets' },
  { path: '/profile', label: '个人信息', icon: 'Postcard' },
  { path: '/history-feedback', label: '历史记录', icon: 'ChatLineRound' },
]

const handleLogin = () => {
  router.push('/login')
}

const handleHome = () => {
  router.push('/bookings/new')
}

const handleAbout = () => {
  router.push('/about')
}

const handleLogout = async () => {
  try {
    await logout()
    ElMessage.success('登出成功')
    router.push('/about')
  } catch (error) {
    ElMessage.error('登出失败，请重试')
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      handleLogout()
      break
  }
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
          <span class="font-bold text-xl tracking-tight text-gray-900">{{ hotelName }}</span>
        </div>

        <!-- 导航菜单 -->
        <nav class="hidden md:flex items-center flex-1 mx-8">
          <template v-if="isAuthenticated() && isCustomer">
            <el-menu
              :default-active="route.path"
              mode="horizontal"
              class="flex-1 border-0"
              router
            >
              <el-menu-item
                v-for="item in customerMenuItems"
                :key="item.path"
                :index="item.path"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </el-menu-item>
            </el-menu>
          </template>
          <template v-else>
            <div class="flex items-center space-x-8">
              <a @click="handleHome" class="text-gray-700 hover:text-blue-600 font-medium cursor-pointer transition-colors">
                首页
              </a>
              <a @click="handleAbout" class="text-gray-700 hover:text-blue-600 font-medium cursor-pointer transition-colors">
                关于我们
              </a>
            </div>
          </template>
        </nav>

        <!-- 右侧操作 -->
        <div class="flex items-center space-x-4">
          <template v-if="!isAuthenticated()">
            <el-button type="primary" @click="handleLogin">
              登录 / 注册
            </el-button>
          </template>
          <template v-else>
            <!-- 员工/管理员：进入后台按钮 -->
            <router-link to="/dashboard" v-if="isStaffOrAdmin">
              <el-button>进入后台</el-button>
            </router-link>
            <!-- 客户：用户信息下拉菜单 -->
            <el-dropdown v-else-if="isCustomer" trigger="click" @command="handleCommand">
              <div class="flex items-center gap-2 cursor-pointer group">
                <div class="text-right">
                  <p class="text-sm font-semibold text-slate-800">{{ currentUser?.name || '用户' }}</p>
                  <p class="text-xs text-slate-500">{{ getRoleLabel(currentUser?.role) }}</p>
                </div>
                <el-icon class="text-slate-400 group-hover:text-blue-600 transition-colors"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人信息</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
:deep(.el-menu) {
  border-bottom: none;
  background-color: transparent;
}

:deep(.el-menu-item) {
  color: #64748b;
  font-weight: 500;
  border-bottom: 2px solid transparent;
}

:deep(.el-menu-item:hover) {
  background-color: transparent;
  color: #2563eb;
}

:deep(.el-menu-item.is-active) {
  color: #2563eb;
  border-bottom-color: #2563eb;
  background-color: transparent;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 4px;
}
</style>
