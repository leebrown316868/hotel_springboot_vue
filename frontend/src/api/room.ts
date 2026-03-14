import api from '@/utils/api'
import type {
  ApiResponse,
  RoomListResponse,
  RoomQueryParams,
  RoomResponse,
  RoomRequest
} from '@/types/room'

export const getRooms = async (
  params: RoomQueryParams
): Promise<ApiResponse<RoomListResponse>> => {
  const response = await api.get<ApiResponse<RoomListResponse>>('/rooms', { params })
  return response.data
}

export const getRoomById = async (
  id: number
): Promise<ApiResponse<RoomResponse>> => {
  const response = await api.get<ApiResponse<RoomResponse>>(`/rooms/${id}`)
  return response.data
}

export const createRoom = async (
  data: RoomRequest
): Promise<ApiResponse<RoomResponse>> => {
  const response = await api.post<ApiResponse<RoomResponse>>('/rooms', data)
  return response.data
}

export const updateRoom = async (
  id: number,
  data: RoomRequest
): Promise<ApiResponse<RoomResponse>> => {
  const response = await api.put<ApiResponse<RoomResponse>>(`/rooms/${id}`, data)
  return response.data
}

export const deleteRoom = async (
  id: number
): Promise<ApiResponse<string>> => {
  const response = await api.delete<ApiResponse<string>>(`/rooms/${id}`)
  return response.data
}

export const updateRoomStatus = async (
  id: number,
  status: string
): Promise<ApiResponse<RoomResponse>> => {
  const response = await api.patch<ApiResponse<RoomResponse>>(`/rooms/${id}/status`, { status })
  return response.data
}
