<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logout, getUser } from '../utils/auth'
import NotificationDropdown from './NotificationDropdown.vue'
import type { User } from '../types/auth'

const route = useRoute()
const router = useRouter()

const currentUser = ref<User | null>(null)

// 获取当前用户信息
onMounted(() => {
  currentUser.value = getUser()
})

// 处理登出
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await logout()
    ElMessage.success('登出成功')
    router.push('/login')
  } catch (error) {
    // 用户取消操作
    if (error !== 'cancel') {
      ElMessage.error('登出失败，请重试')
    }
  }
}

// 处理个人信息
const handleProfile = () => {
  router.push('/profile')
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      handleProfile()
      break
    case 'logout':
      handleLogout()
      break
  }
}

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

// 所有菜单项配置
const allMenuItems = [
  {
    label: '前台员工模块',
    icon: 'Avatar',
    roles: ['ADMIN', 'STAFF'],
    children: [
      { path: '/rooms', label: '客房管理', icon: 'House' },
      { path: '/staff-bookings', label: '订单管理', icon: 'List' },
      { path: '/guests', label: '客户管理', icon: 'User' },
    ]
  },
  {
    label: '管理员模块',
    icon: 'Setting',
    roles: ['ADMIN'],
    children: [
      { path: '/settings', label: '系统管理', icon: 'Tools' },
      { path: '/rooms-resource', label: '客房资源管理', icon: 'OfficeBuilding' },
      { path: '/dashboard', label: '订单与财务监管', icon: 'Money' },
      { path: '/analytics', label: '数据统计与分析', icon: 'DataLine' },
    ]
  },
  {
    label: '用户模块',
    icon: 'UserFilled',
    roles: ['ADMIN', 'CUSTOMER'],
    children: [
      { path: '/browse-rooms', label: '房间浏览与搜索', icon: 'Search' },
      { path: '/bookings/new', label: '在线预订与支付', icon: 'ShoppingCart' },
      { path: '/my-bookings', label: '订单管理', icon: 'Tickets' },
      { path: '/profile', label: '个人信息管理', icon: 'Postcard' },
      { path: '/history-feedback', label: '历史记录与评价反馈', icon: 'ChatLineRound' },
    ]
  }
]

// 根据当前用户角色过滤菜单项
const menuItems = computed(() => {
  const userRole = currentUser.value?.role

  if (!userRole) return []

  return allMenuItems.filter(module => module.roles.includes(userRole))
})
</script>

<template>
  <div class="flex min-h-screen bg-slate-50">
    <aside class="w-64 bg-white border-r border-slate-200 flex flex-col fixed h-full z-30">
      <div class="p-6 flex items-center gap-3">
        <div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center text-white">
          <el-icon :size="24"><OfficeBuilding /></el-icon>
        </div>
        <span class="font-bold text-xl tracking-tight">GrandHotel</span>
      </div>
      
      <el-menu
        :default-active="route.path"
        class="flex-1 border-r-0 mt-4"
        router
      >
        <el-sub-menu v-for="(module, index) in menuItems" :key="index" :index="String(index)">
          <template #title>
            <el-icon><component :is="module.icon" /></el-icon>
            <span class="font-semibold">{{ module.label }}</span>
          </template>
          <el-menu-item 
            v-for="item in module.children" 
            :key="item.path" 
            :index="item.path"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
      
      <div class="p-4 border-t border-slate-100">
        <div class="bg-blue-50 rounded-xl p-4">
          <p class="text-xs font-semibold text-blue-600 uppercase mb-2">技术支持</p>
          <p class="text-sm text-slate-600 mb-3">在管理您的物业时需要帮助吗？</p>
          <button class="w-full bg-blue-600 text-white text-sm py-2 rounded-lg font-medium hover:bg-blue-700 transition-colors">
            联系管理员
          </button>
        </div>
      </div>
    </aside>

    <main class="flex-1 ml-64 flex flex-col min-h-screen">
      <header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 sticky top-0 z-20">
        <div class="relative w-96">
          <el-input
            placeholder="搜索预订、客户或房间..."
            prefix-icon="Search"
            class="search-input"
          />
        </div>
        
        <div class="flex items-center gap-6">
          <NotificationDropdown />
          
          <div class="h-8 w-[1px] bg-slate-200"></div>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="flex items-center gap-3 cursor-pointer group">
              <div class="text-right">
                <p class="text-sm font-semibold text-slate-800">{{ currentUser?.name || '用户' }}</p>
                <p class="text-xs text-slate-500">{{ getRoleLabel(currentUser?.role) }}</p>
              </div>
              <img
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuARyyja8Vtf7aKUjI3RZqwTAd7oXclLSrXMU09HBhnKEH0j4oDB6X6nhbYqn57jx3Ez2AKUs1drBlT2JlMXFRCZHry1uEfiCHetbP4_6ZYBdAbqjblf_YDzyG87kWf07Rj_dUFe3gdkWm30Mitc2w99AKAetnD8YgFteZVllRl84_omOUcxs_-bQSPdKuShXFnqHf0uLb8Yg6Puk7YZu--5tOTuW6iXSaoJhC0JXa9LLcm85G0ydg6KgxFT1ehnHtWz-dB1HiPfrL3X"
                alt="Admin Profile"
                class="w-10 h-10 rounded-full border border-slate-200 object-cover"
              />
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
        </div>
      </header>

      <div class="p-8 flex-1">
        <slot></slot>
      </div>

      <footer class="px-8 py-4 border-t border-slate-200 bg-white text-slate-500 text-xs flex justify-between mt-auto">
        <p>© 2023 GrandHotel 管理系统。保留所有权利。</p>
        <div class="flex gap-4">
          <a href="#" class="hover:text-blue-600">隐私政策</a>
          <a href="#" class="hover:text-blue-600">服务条款</a>
        </div>
      </footer>
    </main>
  </div>
</template>

<style scoped>
:deep(.el-menu) {
  border-right: none;
  background-color: transparent;
}
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  color: #475569;
}
:deep(.el-sub-menu__title:hover) {
  background-color: #f1f5f9;
}
:deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  color: #64748b;
  margin: 4px 12px;
  border-radius: 8px;
}
:deep(.el-menu-item:hover) {
  background-color: #f1f5f9;
  color: #0f172a;
}
:deep(.el-menu-item.is-active) {
  background-color: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  font-weight: 500;
}
:deep(.search-input .el-input__wrapper) {
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}
:deep(.search-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2563eb inset;
}
</style>
