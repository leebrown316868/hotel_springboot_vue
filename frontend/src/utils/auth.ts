import api from './api'
import type { AuthRequest, AuthResponse, RegisterRequest, User } from '../types/auth'

export const login = async (email: string, password: string): Promise<AuthResponse> => {
  const response = await api.post<ApiResponse<AuthResponse>>('/api/auth/login', { email, password })
  const { token, user } = response.data.data

  // Store token and user info
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))

  return response.data.data
}

export const register = async (email: string, password: string, name: string): Promise<AuthResponse> => {
  const response = await api.post<ApiResponse<AuthResponse>>('/api/auth/register', { email, password, name })
  const { token, user } = response.data.data

  // Store token and user info
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))

  return response.data.data
}

export const logout = async (): Promise<void> => {
  try {
    await api.post('/api/auth/logout')
  } finally {
    // Always clear local storage
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
}

export const getCurrentUser = async (): Promise<User> => {
  const response = await api.get<ApiResponse<User>>('/api/auth/me')
  return response.data.data
}

export const getToken = (): string | null => {
  return localStorage.getItem('token')
}

export const getUser = (): User | null => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  }
  return null
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}

// 更新localStorage中的用户信息
export const updateCurrentUser = (updates: any): void => {
  const user = getUser()
  if (user) {
    const updatedUser = { ...user, ...updates }
    localStorage.setItem('user', JSON.stringify(updatedUser))
  }
}
