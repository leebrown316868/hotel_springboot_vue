<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const menuItems = [
  {
    label: '前台员工模块',
    icon: 'Avatar',
    children: [
      { path: '/rooms', label: '客房管理', icon: 'House' },
      { path: '/staff-bookings', label: '订单管理', icon: 'List' },
      { path: '/guests', label: '客户管理', icon: 'User' },
    ]
  },
  {
    label: '管理员模块',
    icon: 'Setting',
    children: [
      { path: '/settings', label: '系统管理', icon: 'Tools' },
      { path: '/rooms-resource', label: '客房资源管理', icon: 'OfficeBuilding' },
      { path: '/dashboard', label: '订单与财务监管', icon: 'Money' },
      { path: '/analytics', label: '数据统计与分析', icon: 'DataLine' },
    ]
  },
  {
    label: '用户模块 (顾客/住客端)',
    icon: 'UserFilled',
    children: [
      { path: '/browse-rooms', label: '房间浏览与搜索', icon: 'Search' },
      { path: '/bookings/new', label: '在线预订与支付', icon: 'ShoppingCart' },
      { path: '/my-bookings', label: '订单管理', icon: 'Tickets' },
      { path: '/profile', label: '个人信息管理', icon: 'Postcard' },
      { path: '/history-feedback', label: '历史记录与评价反馈', icon: 'ChatLineRound' },
    ]
  }
]
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
          <button class="relative text-slate-500 hover:text-blue-600 transition-colors">
            <el-icon :size="24"><Bell /></el-icon>
            <span class="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] w-4 h-4 rounded-full flex items-center justify-center border-2 border-white">3</span>
          </button>
          
          <div class="h-8 w-[1px] bg-slate-200"></div>
          
          <div class="flex items-center gap-3 cursor-pointer group">
            <div class="text-right">
              <p class="text-sm font-semibold text-slate-800">管理员用户</p>
              <p class="text-xs text-slate-500">物业经理</p>
            </div>
            <img 
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuARyyja8Vtf7aKUjI3RZqwTAd7oXclLSrXMU09HBhnKEH0j4oDB6X6nhbYqn57jx3Ez2AKUs1drBlT2JlMXFRCZHry1uEfiCHetbP4_6ZYBdAbqjblf_YDzyG87kWf07Rj_dUFe3gdkWm30Mitc2w99AKAetnD8YgFteZVllRl84_omOUcxs_-bQSPdKuShXFnqHf0uLb8Yg6Puk7YZu--5tOTuW6iXSaoJhC0JXa9LLcm85G0ydg6KgxFT1ehnHtWz-dB1HiPfrL3X" 
              alt="Admin Profile" 
              class="w-10 h-10 rounded-full border border-slate-200 object-cover"
            />
            <el-icon class="text-slate-400 group-hover:text-blue-600 transition-colors"><ArrowDown /></el-icon>
          </div>
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
