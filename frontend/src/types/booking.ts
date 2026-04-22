export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface RoomResponse {
  id: number
  number: string
  floor: string
  type: string
  status: string
  price: number
  capacity?: number
  createdAt?: string
  updatedAt?: string
}

export interface BookingRequest {
  guestId?: number // 可选，如果提供 guestInfo 则不需要
  roomId: number
  checkInDate: string
  checkOutDate: string
  guestCount: number
  guestInfo?: {
    name: string
    phone: string
    email: string
    country: string
    notes?: string
  }
}

export interface BookingResponse {
  id: number
  bookingNumber: string
  guestName: string
  roomNumber: string
  roomType: string
  checkInDate: string
  checkOutDate: string
  guestCount: number
  status: string
  totalAmount: number
  paymentStatus: string
  createdAt: string
  qrCode?: string
}

export interface BookingListResponse {
  content: BookingResponse[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
}

export interface RoomSearchRequest {
  checkInDate: string
  checkOutDate: string
  guestCount: number
  roomTypesStr?: string // 逗号分隔的房型字符串，如："DOUBLE,EXECUTIVE_SUITE"
}
