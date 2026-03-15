export interface Settings {
  id: number
  hotelName: string
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
  hotelName: string
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
}

export type Currency = 'CNY' | 'USD' | 'EUR' | 'GBP'
export type Timezone = 'UTC+8' | 'UTC+0' | 'UTC-5'
export type Language = 'Chinese' | 'English' | 'Spanish'
