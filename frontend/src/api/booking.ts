import api from '@/utils/api'
import type {
  ApiResponse,
  BookingListResponse,
  BookingRequest,
  BookingResponse,
  RoomSearchRequest,
  RoomResponse
} from '@/types/booking'

// 可用房间搜索
export const searchAvailableRooms = (params: RoomSearchRequest) => {
  return api.get<ApiResponse<RoomResponse[]>>('/api/bookings/available-rooms', { params })
}

// 创建预订
export const createBooking = (data: BookingRequest) => {
  return api.post<ApiResponse<BookingResponse>>('/api/bookings', data)
}

// 获取我的订单
export const getMyBookings = (params: any) => {
  return api.get<ApiResponse<BookingListResponse>>('/api/bookings/my', { params })
}

// 获取所有订单（前台/管理员）
export const getAllBookings = (params: any) => {
  return api.get<ApiResponse<BookingListResponse>>('/api/bookings', { params })
}

// 获取订单详情
export const getBookingDetail = (id: number) => {
  return api.get<ApiResponse<BookingResponse>>(`/api/bookings/${id}`)
}

// 取消预订
export const cancelBooking = (id: number) => {
  return api.patch<ApiResponse<BookingResponse>>(`/api/bookings/${id}/cancel`)
}

// 办理入住
export const checkIn = (id: number) => {
  return api.patch<ApiResponse<BookingResponse>>(`/api/bookings/${id}/check-in`)
}

// 办理退房
export const checkOut = (id: number) => {
  return api.patch<ApiResponse<BookingResponse>>(`/api/bookings/${id}/check-out`)
}

// 模拟支付
export const processPayment = (id: number, paymentMethod: string) => {
  return api.post<ApiResponse<BookingResponse>>(`/api/bookings/${id}/pay`, { paymentMethod })
}
