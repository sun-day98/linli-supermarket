import { http } from './request'
import type { CheckoutDTO, CheckoutPreviewVO, OrderQuery, OrderVO, PageResult } from '@/types'

export function previewOrder(data: CheckoutDTO): Promise<CheckoutPreviewVO> {
  return http.post<CheckoutPreviewVO>('/orders/preview', data)
}

export function createOrder(data: CheckoutDTO): Promise<OrderVO> {
  return http.post<OrderVO>('/orders', data)
}

export function getOrders(params: OrderQuery = {}): Promise<PageResult<OrderVO>> {
  return http.get<PageResult<OrderVO>>('/orders', params as Record<string, unknown>)
}

export function getOrder(id: number): Promise<OrderVO> {
  return http.get<OrderVO>(`/orders/${id}`)
}

export function payOrder(id: number, payType: 'WECHAT' | 'ALIPAY' = 'WECHAT'): Promise<OrderVO> {
  return http.post<OrderVO>(`/orders/${id}/pay`, { payType })
}

export function cancelOrder(id: number): Promise<void> {
  return http.post<void>(`/orders/${id}/cancel`)
}

export function finishOrder(id: number): Promise<void> {
  return http.post<void>(`/orders/${id}/finish`)
}
