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
  const response = await api.get<ApiResponse<RoomListResponse>>('/api/rooms', { params })
  return response.data
}

export const getRoomById = async (
  id: number
): Promise<ApiResponse<RoomResponse>> => {
  const response = await api.get<ApiResponse<RoomResponse>>(`/api/rooms/${id}`)
  return response.data
}

export const createRoom = async (
  data: RoomRequest
): Promise<ApiResponse<RoomResponse>> => {
  const response = await api.post<ApiResponse<RoomResponse>>('/api/rooms', data)
  return response.data
}

export const updateRoom = async (
  id: number,
  data: RoomRequest
): Promise<ApiResponse<RoomResponse>> => {
  const response = await api.put<ApiResponse<RoomResponse>>(`/api/rooms/${id}`, data)
  return response.data
}

export const deleteRoom = async (
  id: number
): Promise<ApiResponse<string>> => {
  const response = await api.delete<ApiResponse<string>>(`/api/rooms/${id}`)
  return response.data
}

export const updateRoomStatus = async (
  id: number,
  status: string
): Promise<ApiResponse<RoomResponse>> => {
  const response = await api.patch<ApiResponse<RoomResponse>>(`/api/rooms/${id}/status`, { status })
  return response.data
}

export const uploadRoomImage = async (
  id: number,
  file: File
): Promise<ApiResponse<string>> => {
  const formData = new FormData()
  formData.append('file', file)
  const response = await api.post<ApiResponse<string>>(`/api/rooms/${id}/images`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data
}

export const batchDeleteRooms = async (
  ids: number[]
): Promise<ApiResponse<string>> => {
  const response = await api.delete<ApiResponse<string>>('/api/rooms/batch', {
    data: ids
  })
  return response.data
}
