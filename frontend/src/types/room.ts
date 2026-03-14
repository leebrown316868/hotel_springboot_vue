export interface Room {
  id: number
  number: string
  floor: string
  type: string
  status: string
  price: number
  createdAt: string
  updatedAt: string
}

export interface RoomRequest {
  number: string
  floor: string
  type: string
  status: string
  price: number
}

export interface RoomResponse {
  id: number
  number: string
  floor: string
  type: string
  status: string
  price: number
  createdAt: string
  updatedAt: string
}

export interface RoomListResponse {
  rooms: RoomResponse[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface RoomQueryParams {
  page?: number
  size?: number
  number?: string
  floor?: string
  status?: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
