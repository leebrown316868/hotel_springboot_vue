export interface DashboardStatistics {
  totalRooms: number
  availableRooms: number
  occupancyRate: number
  todayCheckIns: number
  todayCheckOuts: number
  todayRevenue: number
}

export interface RoomStatusDistribution {
  status: string
  displayName: string
  count: number
}

export interface BookingTrendData {
  date: string
  count: number
}

export interface RecentBookingSummary {
  bookingNumber: string
  guestName: string
  roomNumber: string
  roomType: string
  status: string
  checkInDate: string
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}
