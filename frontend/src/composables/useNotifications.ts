import { ref, onMounted, onUnmounted } from 'vue'
import { notificationApi } from '../api/notification'

export function useNotifications() {
  const unreadCount = ref(0)
  let pollingTimer: ReturnType<typeof setInterval> | null = null
  const POLLING_INTERVAL = 10000 // 10秒

  const fetchUnreadCount = async () => {
    try {
      unreadCount.value = await notificationApi.getUnreadCount()
    } catch (error) {
      console.error('获取未读通知数量失败:', error)
    }
  }

  const startPolling = () => {
    fetchUnreadCount()
    pollingTimer = setInterval(fetchUnreadCount, POLLING_INTERVAL)
  }

  const stopPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }

  const refresh = () => {
    fetchUnreadCount()
  }

  onMounted(() => {
    startPolling()
  })

  onUnmounted(() => {
    stopPolling()
  })

  return {
    unreadCount,
    refresh,
    startPolling,
    stopPolling
  }
}
