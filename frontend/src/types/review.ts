export interface BookingReviewResponse {
  id: number
  bookingNumber: string
  roomType: string
  checkInDate: string
  checkOutDate: string
  reviewed: boolean
  rating?: number
  comment?: string
}

export interface ReviewRequest {
  bookingId: number
  rating: number
  comment: string
}

export interface ReviewResponse {
  id: number
  bookingId: number
  bookingNumber: string
  roomType: string
  rating: number
  comment: string
  createdAt: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
