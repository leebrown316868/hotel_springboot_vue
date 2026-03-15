import api from '@/utils/api'
import type { Settings, SettingsRequest, RoomTypeConfig, RoomTypeStats } from '@/types/settings'
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
  },

  // 获取房型配置
  getRoomTypesConfig: async (): Promise<Record<string, RoomTypeConfig>> => {
    const response = await api.get<ApiResponse<Record<string, RoomTypeConfig>>>('/api/settings/room-types')
    return response.data.data
  },

  // 更新房型配置
  updateRoomTypesConfig: async (config: Record<string, RoomTypeConfig>): Promise<Record<string, RoomTypeConfig>> => {
    const response = await api.put<ApiResponse<Record<string, RoomTypeConfig>>>('/api/settings/room-types', config)
    return response.data.data
  },

  // 获取房型统计
  getRoomTypeStats: async (code: string): Promise<RoomTypeStats> => {
    const response = await api.get<ApiResponse<RoomTypeStats>>(`/api/settings/room-types/${code}/stats`)
    return response.data.data
  }
}
