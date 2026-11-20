import { http } from './request'
import type { OrderQuery, OrderVO, PageResult } from '@/types'

export function getAdminOrders(params: OrderQuery = {}): Promise<PageResult<OrderVO>> {
  return http.get<PageResult<OrderVO>>('/admin/orders', params as Record<string, unknown>)
}

export function getAdminOrderDetail(id: number): Promise<OrderVO> {
  return http.get<OrderVO>(`/admin/orders/${id}`)
}

export function payOrder(id: number): Promise<OrderVO> {
  return http.post<OrderVO>(`/orders/${id}/pay`, { payType: 'WECHAT' })
}

export function deliverOrder(id: number, deliverymanId?: number): Promise<void> {
  return http.post<void>(`/orders/${id}/deliver`, deliverymanId ? { deliverymanId } : {})
}

export function finishOrder(id: number): Promise<void> {
  return http.post<void>(`/orders/${id}/finish`)
}

export function cancelOrder(id: number): Promise<void> {
  return http.post<void>(`/orders/${id}/cancel`)
}
