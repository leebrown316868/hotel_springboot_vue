export interface GuestResponse {
  id: number
  name: string
  email: string
  phone: string
  country: string
  status: string
  totalBookings: number
  lastStay: string
  createdAt: string
  updatedAt: string
}

export interface GuestRequest {
  name: string
  email: string
  phone: string
  country: string
  status: string
  lastStay?: string
}

export interface GuestListResponse {
  guests: GuestResponse[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface GuestQueryParams {
  page?: number
  size?: number
  search?: string
}

export interface BookingSummary {
  id: number
  checkInDate: string
  checkOutDate: string
  status: string
  roomNumber: string
  totalPrice: number
  reviewed?: boolean
  rating?: number
  comment?: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
