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

// 获取房间详情
export const getRoomDetail = (id: number) => {
  return api.get<ApiResponse<RoomResponse>>(`/api/rooms/${id}`)
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

// 通过订单号获取订单详情
export const getBookingByNumber = (bookingNumber: string) => {
  return api.get<ApiResponse<BookingResponse>>(`/api/bookings/number/${bookingNumber}`)
}

// 取消预订
export const cancelBooking = (id: number) => {
  return api.patch<ApiResponse<BookingResponse>>(`/api/bookings/${id}/cancel`)
}

// 通过订单号取消预订
export const cancelBookingByNumber = (bookingNumber: string) => {
  return api.patch<ApiResponse<BookingResponse>>(`/api/bookings/number/${bookingNumber}/cancel`)
}

// 办理入住
export const checkIn = (id: number) => {
  return api.patch<ApiResponse<BookingResponse>>(`/api/bookings/${id}/check-in`)
}

// 通过订单号办理入住
export const checkInByNumber = (bookingNumber: string) => {
  return api.patch<ApiResponse<BookingResponse>>(`/api/bookings/number/${bookingNumber}/check-in`)
}

// 办理退房
export const checkOut = (id: number) => {
  return api.patch<ApiResponse<BookingResponse>>(`/api/bookings/${id}/check-out`)
}

// 通过订单号办理退房
export const checkOutByNumber = (bookingNumber: string) => {
  return api.patch<ApiResponse<BookingResponse>>(`/api/bookings/number/${bookingNumber}/check-out`)
}

// 删除订单
export const deleteBooking = (id: number) => {
  return api.delete<ApiResponse<BookingResponse>>(`/api/bookings/${id}`)
}

// 通过订单号删除订单
export const deleteBookingByNumber = (bookingNumber: string) => {
  return api.delete<ApiResponse<BookingResponse>>(`/api/bookings/number/${bookingNumber}`)
}

// 模拟支付
export const processPayment = (id: number, paymentMethod: string) => {
  return api.post<ApiResponse<BookingResponse>>(`/api/bookings/${id}/pay`, { paymentMethod })
}
