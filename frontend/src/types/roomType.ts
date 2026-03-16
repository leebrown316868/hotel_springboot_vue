export interface RoomTypeRequest {
  code: string
  name: string
  capacity: number
  basePrice: number
}

export interface RoomTypeResponse {
  id: number
  code: string
  name: string
  capacity: number
  basePrice: number
  active: boolean
  roomCount: number
  createdAt: string
  updatedAt: string
}

export interface RoomTypeListResponse {
  roomTypes: RoomTypeResponse[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface RoomTypeQueryParams {
  page?: number
  size?: number
  search?: string
}
