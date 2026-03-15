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
  guestId: number
  roomId: number
  checkInDate: string
  checkOutDate: string
  guestCount: number
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
  roomTypes?: string[]
}
