import api from '@/utils/api'
import type { ApiResponse, Profile, UpdateProfileRequest } from '@/types/profile'
import type { ChangePasswordRequest } from '@/types/notification'

export const getProfile = () => {
  return api.get<ApiResponse<Profile>>('/api/profile')
}

export const updateProfile = (data: UpdateProfileRequest) => {
  return api.put<ApiResponse<Profile>>('/api/profile', data)
}

export const changePassword = async (request: ChangePasswordRequest): Promise<void> => {
  await api.put<ApiResponse<void>>('/api/profile/password', request)
}
