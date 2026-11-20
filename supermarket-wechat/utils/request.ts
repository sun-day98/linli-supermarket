import { BASE_URL } from '../config'
import { getToken, removeToken } from './auth'
import type { Result } from '../typings/types'

export interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, unknown>
}

/** Promise 封装 wx.request：注入 Bearer Token、拆包 Result、统一报错。 */
export function request<T>(options: RequestOptions): Promise<T> {
  const token = getToken()
  return new Promise<T>((resolve, reject) => {
    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      success(res) {
        const body = res.data as Result<T>
        if (res.statusCode === 200 && body && body.code === 0) {
          resolve(body.data)
          return
        }
        const code = body ? body.code : res.statusCode
        if (code === 401) {
          removeToken()
          wx.navigateTo({ url: '/pages/login/login' })
        }
        const message = body?.message || `请求失败（${res.statusCode}）`
        wx.showToast({ title: message, icon: 'none', duration: 2200 })
        reject(new Error(message))
      },
      fail(error) {
        wx.showToast({ title: '网络异常，请检查后端地址', icon: 'none' })
        reject(error)
      },
    })
  })
}

/** 已拆包 Result 的 HTTP 助手，直接返回后端 data。 */
export const http = {
  get<T>(url: string, data?: Record<string, unknown>): Promise<T> {
    return request<T>({ url, method: 'GET', data })
  },
  post<T>(url: string, data?: Record<string, unknown>): Promise<T> {
    return request<T>({ url, method: 'POST', data })
  },
  put<T>(url: string, data?: Record<string, unknown>): Promise<T> {
    return request<T>({ url, method: 'PUT', data })
  },
  delete<T>(url: string, data?: Record<string, unknown>): Promise<T> {
    return request<T>({ url, method: 'DELETE', data })
  },
}

export default request
