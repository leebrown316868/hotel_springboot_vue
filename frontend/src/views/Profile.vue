<script setup lang="ts">
import { reactive } from 'vue'
import Layout from '../components/Layout.vue'
import { ElMessage } from 'element-plus'

const profileForm = reactive({
  firstName: '张',
  lastName: '三',
  email: 'zhangsan@example.com',
  phone: '13800138000',
  nationality: '中国',
  notifications: true
})

const handleSave = () => {
  ElMessage.success('个人信息保存成功')
}
</script>

<template>
  <Layout>
    <div class="max-w-3xl mx-auto">
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">个人信息管理</h1>
        <p class="text-sm text-gray-500 mt-1">更新您的个人资料、联系方式和偏好设置</p>
      </header>
      
      <div class="bg-white p-8 rounded-xl shadow-sm border border-gray-100">
        <div class="flex items-center gap-6 mb-8 pb-8 border-b border-gray-100">
          <div class="relative">
            <el-avatar :size="80" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <button class="absolute bottom-0 right-0 bg-blue-600 text-white p-1.5 rounded-full border-2 border-white hover:bg-blue-700 transition-colors">
              <el-icon><Camera /></el-icon>
            </button>
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-900">{{ profileForm.firstName }}{{ profileForm.lastName }}</h2>
            <p class="text-gray-500 text-sm">普通会员</p>
          </div>
        </div>

        <el-form :model="profileForm" label-position="top" class="space-y-4">
          <div class="grid grid-cols-2 gap-6">
            <el-form-item label="姓氏">
              <el-input v-model="profileForm.firstName" size="large" />
            </el-form-item>
            <el-form-item label="名字">
              <el-input v-model="profileForm.lastName" size="large" />
            </el-form-item>
          </div>
          
          <div class="grid grid-cols-2 gap-6">
            <el-form-item label="电子邮箱">
              <el-input v-model="profileForm.email" size="large" />
            </el-form-item>
            <el-form-item label="手机号码">
              <el-input v-model="profileForm.phone" size="large" />
            </el-form-item>
          </div>

          <el-form-item label="国籍/地区">
            <el-select v-model="profileForm.nationality" size="large" class="w-full">
              <el-option label="中国" value="中国" />
              <el-option label="美国" value="美国" />
              <el-option label="日本" value="日本" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="profileForm.notifications">接收酒店优惠和促销通知</el-checkbox>
          </el-form-item>

          <div class="pt-4 flex justify-end">
            <el-button type="primary" size="large" class="px-8" @click="handleSave">保存更改</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </Layout>
</template>
