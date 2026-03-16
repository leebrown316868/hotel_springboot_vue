import api from '@/utils/api'
import type {
  ApiResponse,
  RoomTypeListResponse,
  RoomTypeQueryParams,
  RoomTypeResponse,
  RoomTypeRequest
} from '@/types/roomType'

export const getRoomTypes = async (
  params: RoomTypeQueryParams
): Promise<ApiResponse<RoomTypeListResponse>> => {
  const response = await api.get<ApiResponse<RoomTypeListResponse>>('/api/room-types', { params })
  return response.data
}

export const getActiveRoomTypes = async (): Promise<
  ApiResponse<RoomTypeResponse[]>
> => {
  const response = await api.get<ApiResponse<RoomTypeResponse[]>>('/api/room-types/active')
  return response.data
}

export const getRoomTypeById = async (
  id: number
): Promise<ApiResponse<RoomTypeResponse>> => {
  const response = await api.get<ApiResponse<RoomTypeResponse>>(`/api/room-types/${id}`)
  return response.data
}

export const createRoomType = async (
  data: RoomTypeRequest
): Promise<ApiResponse<RoomTypeResponse>> => {
  const response = await api.post<ApiResponse<RoomTypeResponse>>('/api/room-types', data)
  return response.data
}

export const updateRoomType = async (
  id: number,
  data: RoomTypeRequest
): Promise<ApiResponse<RoomTypeResponse>> => {
  const response = await api.put<ApiResponse<RoomTypeResponse>>(
    `/api/room-types/${id}`,
    data
  )
  return response.data
}

export const deleteRoomType = async (
  id: number
): Promise<ApiResponse<string>> => {
  const response = await api.delete<ApiResponse<string>>(`/api/room-types/${id}`)
  return response.data
}
