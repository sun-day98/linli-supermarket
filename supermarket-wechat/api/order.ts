import { http } from '../utils/request'
import type { CheckoutPreviewVO, OrderVO, PageResult } from '../typings/types'

export function previewOrder(data: Record<string, unknown>): Promise<CheckoutPreviewVO> {
  return http.post<CheckoutPreviewVO>('/orders/preview', data)
}

export function createOrder(data: Record<string, unknown>): Promise<OrderVO> {
  return http.post<OrderVO>('/orders', data)
}

export function getOrders(status?: number): Promise<PageResult<OrderVO>> {
  const params: Record<string, unknown> = { current: 1, size: 20 }
  if (status !== undefined) params.status = status
  return http.get<PageResult<OrderVO>>('/orders', params)
}

export function getOrder(id: number): Promise<OrderVO> {
  return http.get<OrderVO>(`/orders/${id}`)
}

export function payOrder(id: number, payType: 'WECHAT' | 'ALIPAY'): Promise<OrderVO> {
  return http.post<OrderVO>(`/orders/${id}/pay`, { payType })
}

export function cancelOrder(id: number): Promise<void> {
  return http.post<void>(`/orders/${id}/cancel`)
}

export function finishOrder(id: number): Promise<void> {
  return http.post<void>(`/orders/${id}/finish`)
}
