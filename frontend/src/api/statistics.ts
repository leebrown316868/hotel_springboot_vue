import api from '@/utils/api'
import type {
  ApiResponse,
  DashboardStatistics,
  RoomStatusDistribution,
  BookingTrendData,
  RecentBookingSummary
} from '@/types/statistics'

export const getDashboardStatistics = () => {
  return api.get<ApiResponse<DashboardStatistics>>('/api/statistics/dashboard')
}

export const getRoomStatusDistribution = () => {
  return api.get<ApiResponse<RoomStatusDistribution[]>>('/api/statistics/room-status')
}

export const getBookingTrends = (days: number = 7) => {
  return api.get<ApiResponse<BookingTrendData[]>>('/api/statistics/booking-trends', {
    params: { days }
  })
}

export const getRecentBookings = (limit: number = 4) => {
  return api.get<ApiResponse<RecentBookingSummary[]>>('/api/statistics/recent-bookings', {
    params: { limit }
  })
}
