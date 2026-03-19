export interface User {
  id: number
  email: string
  name: string
  phone: string
  country: string
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
  phone: string
  country: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
