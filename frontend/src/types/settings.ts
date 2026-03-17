export interface RoomTypeConfig {
  name: string
  capacity: number
  basePrice: number
}

export interface RoomTypeStats {
  code: string
  roomCount: number
  availableCount: number
  occupiedCount: number
  cleaningCount: number
  maintenanceCount: number
}

export interface RoomTypeWithStats extends RoomTypeConfig {
  code: string
  roomCount: number
  availableCount: number
}

export interface Settings {
  id: number
  hotelName: string
  description: string
  contactEmail: string
  contactPhone: string
  address: string
  currency: string
  timezone: string
  language: string
  twoFactorEnabled: boolean
  sessionTimeout: number
  passwordExpiry: number
  emailNotificationBookings: boolean
  emailNotificationCancellations: boolean
  pushNotificationsEnabled: boolean
  updatedAt: string
  updatedBy: string
}

export interface SettingsRequest {
  hotelName?: string
  description?: string
  contactEmail?: string
  contactPhone?: string
  address?: string
  currency?: string
  timezone?: string
  language?: string
  twoFactorEnabled?: boolean
  sessionTimeout?: number
  passwordExpiry?: number
  emailNotificationBookings?: boolean
  emailNotificationCancellations?: boolean
  pushNotificationsEnabled?: boolean
}

export interface PublicSettings {
  hotelName: string
  description: string
  address: string
  contactPhone: string
  contactEmail: string
}
