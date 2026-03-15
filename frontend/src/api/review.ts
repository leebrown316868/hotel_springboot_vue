import api from '@/utils/api'
import type {
  ApiResponse,
  BookingReviewResponse,
  ReviewRequest,
  ReviewResponse
} from '@/types/review'

export const getMyCompletedBookings = () => {
  return api.get<ApiResponse<BookingReviewResponse[]>>('/api/reviews/my')
}

export const createReview = (data: ReviewRequest) => {
  return api.post<ApiResponse<ReviewResponse>>('/api/reviews', data)
}
