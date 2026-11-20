import axios from 'axios'
import type { AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import type { Result } from '@/types'

const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' },
})

instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

instance.interceptors.response.use(
  (response) => {
    const body = response.data as Result<unknown>
    if (!body || typeof body.code !== 'number') {
      ElMessage.error('服务响应格式错误')
      return Promise.reject(new Error('服务响应格式错误'))
    }
    if (body.code === 0) return body.data as never
    if (body.code === 401) {
      removeToken()
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login'
      }
    }
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    const message = error?.response?.data?.message || error?.message || '网络异常，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

/** 已拆包 Result 的 HTTP 助手，直接返回后端 data。 */
export const http = {
  get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
    return instance.get(url, { params }) as unknown as Promise<T>
  },
  post<T>(url: string, data?: unknown): Promise<T> {
    return instance.post(url, data) as unknown as Promise<T>
  },
  put<T>(url: string, data?: unknown): Promise<T> {
    return instance.put(url, data) as unknown as Promise<T>
  },
  delete<T>(url: string): Promise<T> {
    return instance.delete(url) as unknown as Promise<T>
  },
}

export default instance
