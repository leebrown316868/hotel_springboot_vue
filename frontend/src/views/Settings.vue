<script setup lang="ts">
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const activeTab = ref('general')

const generalSettings = ref({
  hotelName: 'GrandHorizon 酒店及水疗中心',
  email: 'contact@grandhorizon.com',
  phone: '+1 (555) 123-4567',
  address: '123 Ocean Drive, Miami, FL 33139',
  currency: 'CNY',
  timezone: 'UTC+8 (北京时间)',
  language: '中文'
})

const securitySettings = ref({
  twoFactor: true,
  sessionTimeout: 30,
  passwordExpiry: 90
})

const notificationSettings = ref({
  emailBookings: true,
  emailCancellations: true,
  smsAlerts: false,
  pushNotifications: true
})
</script>

<template>
  <Layout>
    <div class="max-w-4xl mx-auto">
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">设置</h1>
        <p class="text-sm text-gray-500 mt-1">配置您的酒店管理系统偏好</p>
      </header>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-tabs v-model="activeTab" class="settings-tabs">
          <el-tab-pane label="常规" name="general">
            <div class="p-8">
              <h3 class="text-lg font-bold text-gray-900 mb-6">酒店信息</h3>
              <el-form :model="generalSettings" label-position="top">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <el-form-item label="酒店名称">
                    <el-input v-model="generalSettings.hotelName" />
                  </el-form-item>
                  <el-form-item label="联系邮箱">
                    <el-input v-model="generalSettings.email" />
                  </el-form-item>
                  <el-form-item label="电话号码">
                    <el-input v-model="generalSettings.phone" />
                  </el-form-item>
                  <el-form-item label="货币">
                    <el-select v-model="generalSettings.currency" class="w-full">
                      <el-option label="人民币 (¥)" value="CNY" />
                      <el-option label="美元 ($)" value="USD" />
                      <el-option label="欧元 (€)" value="EUR" />
                      <el-option label="英镑 (£)" value="GBP" />
                    </el-select>
                  </el-form-item>
                </div>
                <el-form-item label="地址">
                  <el-input v-model="generalSettings.address" type="textarea" :rows="2" />
                </el-form-item>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <el-form-item label="时区">
                    <el-select v-model="generalSettings.timezone" class="w-full">
                      <el-option label="UTC+8 (北京时间)" value="UTC+8" />
                      <el-option label="UTC+0 (格林威治标准时间)" value="UTC+0" />
                      <el-option label="UTC-5 (东部时间)" value="UTC-5" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="系统语言">
                    <el-select v-model="generalSettings.language" class="w-full">
                      <el-option label="中文" value="Chinese" />
                      <el-option label="英语" value="English" />
                      <el-option label="西班牙语" value="Spanish" />
                    </el-select>
                  </el-form-item>
                </div>
                <div class="flex justify-end mt-4">
                  <el-button type="primary">保存更改</el-button>
                </div>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane label="安全" name="security">
            <div class="p-8">
              <h3 class="text-lg font-bold text-gray-900 mb-6">安全偏好</h3>
              <div class="space-y-8">
                <div class="flex items-center justify-between">
                  <div>
                    <div class="font-bold text-gray-900">双重身份验证</div>
                    <div class="text-sm text-gray-500">为您的账户添加额外安全层</div>
                  </div>
                  <el-switch v-model="securitySettings.twoFactor" />
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <div class="font-bold text-gray-900">会话超时</div>
                    <div class="text-sm text-gray-500">无操作后自动登出（分钟）</div>
                  </div>
                  <el-input-number v-model="securitySettings.sessionTimeout" :min="5" :max="120" />
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <div class="font-bold text-gray-900">密码有效期</div>
                    <div class="text-sm text-gray-500">强制每 X 天更改一次密码</div>
                  </div>
                  <el-input-number v-model="securitySettings.passwordExpiry" :min="30" :max="365" />
                </div>
                <div class="pt-6 border-t border-gray-100">
                  <el-button type="primary" plain>更改管理员密码</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="通知" name="notifications">
            <div class="p-8">
              <h3 class="text-lg font-bold text-gray-900 mb-6">通知渠道</h3>
              <div class="space-y-6">
                <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div class="flex items-center gap-4">
                    <div class="p-2 bg-blue-100 text-blue-600 rounded-lg">
                      <el-icon :size="20"><Message /></el-icon>
                    </div>
                    <div>
                      <div class="font-bold text-gray-900">邮件通知</div>
                      <div class="text-xs text-gray-500">通过邮件接收预订和取消的更新</div>
                    </div>
                  </div>
                  <div class="flex flex-col gap-2">
                    <el-checkbox v-model="notificationSettings.emailBookings">新预订</el-checkbox>
                    <el-checkbox v-model="notificationSettings.emailCancellations">取消预订</el-checkbox>
                  </div>
                </div>

                <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div class="flex items-center gap-4">
                    <div class="p-2 bg-emerald-100 text-emerald-600 rounded-lg">
                      <el-icon :size="20"><Iphone /></el-icon>
                    </div>
                    <div>
                      <div class="font-bold text-gray-900">短信警报</div>
                      <div class="text-xs text-gray-500">将关键警报直接发送到您的手机</div>
                    </div>
                  </div>
                  <el-switch v-model="notificationSettings.smsAlerts" />
                </div>

                <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div class="flex items-center gap-4">
                    <div class="p-2 bg-indigo-100 text-indigo-600 rounded-lg">
                      <el-icon :size="20"><Bell /></el-icon>
                    </div>
                    <div>
                      <div class="font-bold text-gray-900">推送通知</div>
                      <div class="text-xs text-gray-500">实时更新的浏览器通知</div>
                    </div>
                  </div>
                  <el-switch v-model="notificationSettings.pushNotifications" />
                </div>
              </div>
              <div class="flex justify-end mt-8">
                <el-button type="primary">保存偏好</el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </Layout>
</template>

<style scoped>
.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  padding: 0 2rem;
  background-color: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}

.settings-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.settings-tabs :deep(.el-tabs__item) {
  height: 60px;
  line-height: 60px;
  font-weight: 600;
}
</style>
