import api from '@/utils/api'
import type { Notification, NotificationListResponse } from '@/types/notification'
import type { ApiResponse } from '@/types/api'

export const notificationApi = {
  // 获取通知列表
  getNotifications: async (page = 0, size = 10): Promise<NotificationListResponse> => {
    const response = await api.get<ApiResponse<NotificationListResponse>>(
      `/api/notifications?page=${page}&size=${size}`
    )
    return response.data.data
  },

  // 获取未读通知数量
  getUnreadCount: async (): Promise<number> => {
    const response = await api.get<ApiResponse<number>>('/api/notifications/unread-count')
    return response.data.data
  },

  // 标记为已读
  markAsRead: async (id: number): Promise<Notification> => {
    const response = await api.put<ApiResponse<Notification>>(`/api/notifications/${id}/read`)
    return response.data.data
  },

  // 标记所有为已读
  markAllAsRead: async (): Promise<void> => {
    await api.put('/api/notifications/read-all')
  }
}
