<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Loading } from '@element-plus/icons-vue'
import type { Notification } from '../types/notification'
import { notificationApi } from '../api/notification'
import { useNotifications } from '../composables/useNotifications'

const router = useRouter()
const { unreadCount, refresh } = useNotifications()

const notifications = ref<Notification[]>([])
const loading = ref(false)

const displayCount = computed(() => {
  return unreadCount.value > 99 ? '99+' : unreadCount.value.toString()
})

const hasNotifications = computed(() => {
  return unreadCount.value > 0
})

const fetchNotifications = async () => {
  try {
    loading.value = true
    const response = await notificationApi.getNotifications(0, 5)
    notifications.value = response.notifications
  } catch (error) {
    console.error('获取通知列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleMarkAsRead = async (notification: Notification) => {
  if (!notification.isRead) {
    try {
      await notificationApi.markAsRead(notification.id)
      notification.isRead = true
      refresh()
    } catch (error) {
      ElMessage.error('标记失败')
    }
  }

  if (notification.actionLink) {
    router.push(notification.actionLink)
  }
}

const handleMarkAllAsRead = async () => {
  try {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    refresh()
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getTimeAgo = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  const diffHours = Math.floor(diffMins / 60)
  if (diffHours < 24) return `${diffHours}小时前`
  const diffDays = Math.floor(diffHours / 24)
  return `${diffDays}天前`
}

const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'BOOKING': return 'document'
    case 'CANCELLATION': return 'close'
    case 'CHECK_IN': return 'check'
    case 'CHECK_OUT': return 'house'
    case 'SYSTEM': return 'setting'
    default: return 'bell'
  }
}

const getNotificationColor = (priority: number) => {
  switch (priority) {
    case 2: return 'danger'
    case 1: return 'warning'
    default: return 'primary'
  }
}
</script>

<template>
  <el-dropdown trigger="click" @visible-change="fetchNotifications">
    <div class="notification-bell cursor-pointer">
      <el-badge :value="displayCount" :hidden="!hasNotifications">
        <el-icon :size="20">
          <Bell />
        </el-icon>
      </el-badge>
    </div>

    <template #dropdown>
      <el-dropdown-menu class="notification-dropdown">
        <div class="notification-header">
          <span class="notification-title">通知</span>
          <el-button
            v-if="hasNotifications"
            link
            type="primary"
            @click="handleMarkAllAsRead"
          >
            全部已读
          </el-button>
        </div>

        <div v-if="loading" class="notification-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>

        <div v-else-if="notifications.length === 0" class="notification-empty">
          <el-empty description="暂无通知" :image-size="60" />
        </div>

        <div v-else class="notification-list">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.isRead }"
            @click="handleMarkAsRead(notification)"
          >
            <div class="notification-icon">
              <el-tag :type="getNotificationColor(notification.priority)" size="small">
                {{ getNotificationIcon(notification.type) }}
              </el-tag>
            </div>
            <div class="notification-content">
              <div class="notification-message">{{ notification.title }}</div>
              <div class="notification-desc">{{ notification.message }}</div>
              <div class="notification-time">{{ getTimeAgo(notification.createdAt) }}</div>
            </div>
            <div v-if="!notification.isRead" class="notification-dot"></div>
          </div>
        </div>

        <div v-if="notifications.length >= 5" class="notification-footer">
          <el-button link @click="router.push('/notifications')">
            查看全部
          </el-button>
        </div>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.notification-bell {
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.notification-bell:hover {
  background-color: #f5f5f5;
}

.notification-dropdown {
  width: 320px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.notification-title {
  font-weight: 600;
  color: #303133;
}

.notification-loading,
.notification-empty {
  padding: 20px;
  text-align: center;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  padding: 12px 16px;
  cursor: pointer;
  position: relative;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f5f5;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-icon {
  margin-right: 12px;
}

.notification-content {
  flex: 1;
}

.notification-message {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.notification-desc {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.notification-time {
  font-size: 11px;
  color: #909399;
}

.notification-dot {
  width: 6px;
  height: 6px;
  background-color: #409eff;
  border-radius: 50%;
  position: absolute;
  top: 16px;
  right: 12px;
}

.notification-footer {
  padding: 8px;
  text-align: center;
  border-top: 1px solid #ebeef5;
}
</style>
