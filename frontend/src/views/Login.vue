<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const loginForm = reactive({
  email: '',
  password: '',
  remember: false
})

const handleLogin = () => {
  if (!loginForm.email || !loginForm.password) {
    ElMessage.warning('请输入邮箱和密码')
    return
  }

  loading.value = true
  
  setTimeout(() => {
    loading.value = false
    ElMessage.success('登录成功！正在跳转...')
    router.push('/dashboard')
  }, 1000)
}
</script>

<template>
  <div class="h-screen w-full overflow-hidden">
    <div class="login-bg h-full w-full flex items-center justify-center relative">
      <div class="overlay absolute inset-0"></div>
      
      <main class="relative z-10 w-full max-w-md px-6">
        <div class="bg-white rounded-2xl shadow-2xl p-8 md:p-10">
          <div class="text-center mb-8">
            <div class="inline-flex items-center justify-center w-16 h-16 bg-blue-50 rounded-full mb-4">
              <el-icon class="text-blue-600" :size="32"><OfficeBuilding /></el-icon>
            </div>
            <h1 class="text-2xl font-bold text-gray-900">酒店管理系统</h1>
            <p class="text-gray-500 mt-2">欢迎回来！请登录您的账户。</p>
          </div>
          
          <form @submit.prevent="handleLogin" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">电子邮箱</label>
              <el-input 
                v-model="loginForm.email" 
                type="email" 
                placeholder="admin@hotelname.com" 
                clearable
                size="large"
              />
            </div>
            
            <div>
              <div class="flex justify-between items-center mb-1">
                <label class="block text-sm font-medium text-gray-700">密码</label>
                <a href="#" class="text-sm font-semibold text-blue-600 hover:text-blue-700 transition-colors">忘记密码？</a>
              </div>
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="••••••••" 
                show-password
                size="large"
              />
            </div>
            
            <div class="flex items-center">
              <el-checkbox v-model="loginForm.remember" label="记住我 30 天" size="large" />
            </div>
            
            <div>
              <el-button 
                type="primary" 
                native-type="submit" 
                class="w-full" 
                :loading="loading"
                size="large"
              >
                登录
              </el-button>
            </div>
          </form>
          
          <footer class="mt-8 pt-6 border-t border-gray-100 text-center">
            <p class="text-sm text-gray-600">
              还没有账户？ 
              <a href="#" class="font-semibold text-blue-600 hover:text-blue-700 transition-colors">注册新账户</a>
            </p>
          </footer>
        </div>
        
        <p class="text-center text-white/60 text-xs mt-8">
          © 2023 Grand Horizon 酒店集团。保留所有权利。
        </p>
      </main>
    </div>
  </div>
</template>

<style scoped>
.login-bg {
  background-image: url('https://lh3.googleusercontent.com/aida-public/AB6AXuBQIPDB_LrZx481Ogr-QUb0skZ4JzSK_xP4dQfqoxkAkYaGuORxcJXb5yIMeL34iTMHPx-F-m4ZWgsjo2bmXyYtG5DS0JyIfRQ9n0hcdhIysLipK08i1VHLhGWCgcQEBlKBTd_IiRAI02kAzoJpXuIswy9vOHXN7wzyH-yRfPyZoS-Z6LhQbRo8MLcilRfNuiOBJjp9xGvQOl5LwQSg97v4P_S8vg9-KoPKmmKeDxBoihDX-iVwR6gk7lmxIpCZkEHJvkC2ehT8mPmk');
  background-size: cover;
  background-position: center;
}
.overlay {
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
}
:deep(.el-button--primary) {
  --el-button-bg-color: #2563eb;
  --el-button-border-color: #2563eb;
  --el-button-hover-bg-color: #1d4ed8;
  --el-button-hover-border-color: #1d4ed8;
  font-weight: 600;
}
</style>
