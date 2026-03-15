export type NotificationType = 'BOOKING' | 'CANCELLATION' | 'CHECK_IN' | 'CHECK_OUT' | 'SYSTEM'

export interface Notification {
  id: number
  title: string
  message: string
  type: NotificationType
  isRead: boolean
  priority: number
  actionLink: string | null
  createdAt: string
  readAt: string | null
}

export interface NotificationListResponse {
  notifications: Notification[]
  total: number
  unreadCount: number
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}
