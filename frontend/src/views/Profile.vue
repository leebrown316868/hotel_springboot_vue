<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProfile, updateProfile } from '@/api/profile'
import type { Profile } from '@/types/profile'
import Layout from '../components/Layout.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const profileForm = reactive<Profile>({
  id: 0,
  email: '',
  name: '',
  phone: '',
  address: '',
  nationality: '中国',
  preferencesEnabled: true,
  role: ''
})

const loadProfile = async () => {
  loading.value = true
  try {
    const response = await getProfile()
    if (response.data.code === 200 && response.data.data) {
      Object.assign(profileForm, response.data.data)
    }
  } catch (error) {
    ElMessage.error('加载个人资料失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const response = await updateProfile({
      name: profileForm.name,
      phone: profileForm.phone || undefined,
      address: profileForm.address || undefined,
      nationality: profileForm.nationality || undefined,
      preferencesEnabled: profileForm.preferencesEnabled
    })

    if (response.data.code === 200) {
      ElMessage.success('个人信息保存成功')
      // 更新localStorage中的用户信息
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
      currentUser.name = profileForm.name
      localStorage.setItem('user', JSON.stringify(currentUser))
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <Layout>
    <div class="max-w-3xl mx-auto">
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">个人信息管理</h1>
        <p class="text-sm text-gray-500 mt-1">更新您的个人资料、联系方式和偏好设置</p>
      </header>

      <div v-if="loading" class="flex justify-center items-center py-12">
        <el-icon class="is-loading text-4xl text-blue-600"><Loading /></el-icon>
      </div>

      <div v-else class="bg-white p-8 rounded-xl shadow-sm border border-gray-100">
        <div class="flex items-center gap-6 mb-8 pb-8 border-b border-gray-100">
          <div class="relative">
            <el-avatar :size="80" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-900">{{ profileForm.name }}</h2>
            <p class="text-gray-500 text-sm">{{ profileForm.role === 'CUSTOMER' ? '客户' : '管理员' }}</p>
          </div>
        </div>

        <el-form :model="profileForm" label-position="top" class="space-y-4">
          <el-form-item label="电子邮箱">
            <el-input v-model="profileForm.email" size="large" disabled />
          </el-form-item>

          <el-form-item label="姓名">
            <el-input v-model="profileForm.name" size="large" />
          </el-form-item>

          <div class="grid grid-cols-2 gap-6">
            <el-form-item label="手机号码">
              <el-input v-model="profileForm.phone" size="large" />
            </el-form-item>

            <el-form-item label="国籍/地区">
              <el-select v-model="profileForm.nationality" size="large" class="w-full">
                <el-option label="中国" value="中国" />
                <el-option label="美国" value="美国" />
                <el-option label="日本" value="日本" />
                <el-option label="韩国" value="韩国" />
                <el-option label="英国" value="英国" />
                <el-option label="法国" value="法国" />
                <el-option label="德国" value="德国" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="地址">
            <el-input v-model="profileForm.address" size="large" />
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="profileForm.preferencesEnabled">接收酒店优惠和促销通知</el-checkbox>
          </el-form-item>

          <div class="pt-4 flex justify-end">
            <el-button type="primary" size="large" class="px-8" :loading="saving" @click="handleSave">保存更改</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </Layout>
</template>
