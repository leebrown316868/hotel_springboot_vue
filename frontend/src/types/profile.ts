export interface Profile {
  id: number
  email: string
  name: string
  phone?: string
  address?: string
  nationality?: string
  preferencesEnabled?: boolean
  role: string
}

export interface UpdateProfileRequest {
  name: string
  phone?: string
  address?: string
  nationality?: string
  preferencesEnabled?: boolean
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
