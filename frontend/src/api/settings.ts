import api from '@/utils/api'
import type { Settings, SettingsRequest } from '@/types/settings'
import type { ApiResponse } from '@/types/api'

export const settingsApi = {
  // 获取系统设置
  getSettings: async (): Promise<Settings> => {
    const response = await api.get<ApiResponse<Settings>>('/api/settings')
    return response.data.data
  },

  // 更新系统设置
  updateSettings: async (request: SettingsRequest): Promise<Settings> => {
    const response = await api.put<ApiResponse<Settings>>('/api/settings', request)
    return response.data.data
  }
}
