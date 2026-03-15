import api from '@/utils/api'
import type { ApiResponse, Profile, UpdateProfileRequest } from '@/types/profile'

export const getProfile = () => {
  return api.get<ApiResponse<Profile>>('/api/profile')
}

export const updateProfile = (data: UpdateProfileRequest) => {
  return api.put<ApiResponse<Profile>>('/api/profile', data)
}
