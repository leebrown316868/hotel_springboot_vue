import axios, { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'

// 自定义参数序列化器，将数组参数序列化为逗号分隔的字符串
const paramsSerializer = (params: any) => {
  return Object.keys(params)
    .map(key => {
      const value = params[key]
      if (value === null || value === undefined) return ''
      if (Array.isArray(value)) {
        return `${key}=${value.join(',')}`
      }
      return `${key}=${encodeURIComponent(value)}`
    })
    .filter(Boolean)
    .join('&')
}

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  paramsSerializer: paramsSerializer
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error: AxiosError) => {
    if (error.response) {
      const status = error.response.status

      switch (status) {
        case 401:
          // Unauthorized - 只显示错误消息，不跳转
          ElMessage.error('未授权，请重新登录')
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误，请稍后重试')
          break
        default:
          ElMessage.error((error.response.data as any)?.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

export default api
