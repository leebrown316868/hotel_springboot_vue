import api from '@/utils/api'
import type {
  ApiResponse,
  GuestListResponse,
  GuestQueryParams,
  GuestResponse,
  GuestRequest,
  BookingSummary
} from '@/types/guest'

export const getGuests = async (
  params: GuestQueryParams
): Promise<ApiResponse<GuestListResponse>> => {
  const response = await api.get<ApiResponse<GuestListResponse>>('/guests', { params })
  return response.data
}

export const getGuestById = async (
  id: number
): Promise<ApiResponse<GuestResponse>> => {
  const response = await api.get<ApiResponse<GuestResponse>>(`/guests/${id}`)
  return response.data
}

export const createGuest = async (
  data: GuestRequest
): Promise<ApiResponse<GuestResponse>> => {
  const response = await api.post<ApiResponse<GuestResponse>>('/guests', data)
  return response.data
}

export const updateGuest = async (
  id: number,
  data: GuestRequest
): Promise<ApiResponse<GuestResponse>> => {
  const response = await api.put<ApiResponse<GuestResponse>>(`/guests/${id}`, data)
  return response.data
}

export const deleteGuest = async (
  id: number
): Promise<ApiResponse<string>> => {
  const response = await api.delete<ApiResponse<string>>(`/guests/${id}`)
  return response.data
}

export const getGuestBookings = async (
  id: number
): Promise<ApiResponse<BookingSummary[]>> => {
  const response = await api.get<ApiResponse<BookingSummary[]>>(`/guests/${id}/bookings`)
  return response.data
}
