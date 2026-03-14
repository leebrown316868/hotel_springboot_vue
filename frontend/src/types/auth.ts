export interface User {
  id: number
  email: string
  name: string
  role: string
}

export interface AuthRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  name: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface ApiResponse<T = any> {
  success: boolean
  message: string
  data?: T
}
