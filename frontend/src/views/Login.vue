<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { OfficeBuilding } from '@element-plus/icons-vue'
import { login, register } from '@/utils/auth'
import { settingsApi } from '@/api/settings'

const router = useRouter()
const loading = ref(false)
const isLoginMode = ref(true) // 控制显示登录还是注册表单
const hotelName = ref('酒店管理系统')

// 获取酒店设置
onMounted(async () => {
  try {
    const settings = await settingsApi.getPublicSettings()
    hotelName.value = settings.hotelName || '酒店管理系统'
  } catch (error) {
    console.error('获取酒店设置失败:', error)
  }
})

const loginForm = reactive({
  email: '',
  password: '',
  remember: false
})

const registerForm = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  phone: '',
  country: ''
})

const handleLogin = async () => {
  if (!loginForm.email || !loginForm.password) {
    ElMessage.warning('请输入邮箱和密码')
    return
  }

  loading.value = true

  try {
    const authResponse = await login(loginForm.email, loginForm.password)
    ElMessage.success('登录成功！正在跳转...')

    // 根据用户角色跳转到不同页面
    const userRole = authResponse.user.role
    if (userRole === 'ADMIN' || userRole === 'STAFF') {
      router.push('/dashboard')
    } else {
      // 客户角色跳转到在线预订页面
      router.push('/bookings/new')
    }
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || '登录失败，请检查邮箱和密码'
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  // 验证表单
  if (!registerForm.name || !registerForm.email || !registerForm.password ||
      !registerForm.phone || !registerForm.country) {
    ElMessage.warning('请填写所有必填字段')
    return
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  // 验证密码强度（至少8位，包含大小写字母和数字）
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/
  if (!passwordRegex.test(registerForm.password)) {
    ElMessage.warning('密码必须至少8位，包含大小写字母和数字')
    return
  }

  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }

  // 验证电话号码格式（国际格式）
  const phoneRegex = /^\+?[1-9]\d{1,14}$/
  if (!phoneRegex.test(registerForm.phone)) {
    ElMessage.warning('请输入有效的电话号码（国际格式，如 +1234567890）')
    return
  }

  loading.value = true

  try {
    const authResponse = await register(
      registerForm.email,
      registerForm.password,
      registerForm.name,
      registerForm.phone,
      registerForm.country
    )
    ElMessage.success('注册成功！正在跳转...')

    // 注册成功后跳转到在线预订页面
    router.push('/bookings/new')
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || '注册失败，请稍后重试'
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value
  // 清空表单
  if (isLoginMode.value) {
    Object.assign(registerForm, {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      phone: '',
      country: ''
    })
  } else {
    Object.assign(loginForm, {
      email: '',
      password: '',
      remember: false
    })
  }
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
            <h1 class="text-2xl font-bold text-gray-900">{{ hotelName }}</h1>
            <p class="text-gray-500 mt-2">{{ isLoginMode ? '欢迎回来！请登录您的账户。' : '创建新账户开始您的旅程' }}</p>
          </div>

          <!-- 登录表单 -->
          <form v-if="isLoginMode" @submit.prevent="handleLogin" class="space-y-6">
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

          <!-- 注册表单 -->
          <form v-else @submit.prevent="handleRegister" class="space-y-5">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">姓名 <span class="text-red-500">*</span></label>
              <el-input
                v-model="registerForm.name"
                placeholder="请输入您的姓名"
                clearable
                size="large"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">电子邮箱 <span class="text-red-500">*</span></label>
              <el-input
                v-model="registerForm.email"
                type="email"
                placeholder="your@email.com"
                clearable
                size="large"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">密码 <span class="text-red-500">*</span></label>
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="至少8位，包含大小写字母和数字"
                show-password
                size="large"
              />
              <div class="text-xs text-gray-500 mt-1">密码必须至少8位，包含大小写字母和数字</div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">确认密码 <span class="text-red-500">*</span></label>
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="再次输入密码"
                show-password
                size="large"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">电话号码 <span class="text-red-500">*</span></label>
              <el-input
                v-model="registerForm.phone"
                placeholder="+1234567890"
                clearable
                size="large"
              />
              <div class="text-xs text-gray-500 mt-1">国际格式，如 +1234567890</div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">国家 <span class="text-red-500">*</span></label>
              <el-input
                v-model="registerForm.country"
                placeholder="请输入您的国家"
                clearable
                size="large"
              />
            </div>

            <div>
              <el-button
                type="primary"
                native-type="submit"
                class="w-full"
                :loading="loading"
                size="large"
              >
                注册
              </el-button>
            </div>
          </form>

          <footer class="mt-8 pt-6 border-t border-gray-100 text-center">
            <p class="text-sm text-gray-600">
              {{ isLoginMode ? '还没有账户？' : '已有账户？' }}
              <a @click="toggleMode" class="font-semibold text-blue-600 hover:text-blue-700 transition-colors cursor-pointer">
                {{ isLoginMode ? '注册新账户' : '立即登录' }}
              </a>
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
