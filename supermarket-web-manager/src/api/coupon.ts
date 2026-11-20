import { http } from './request'
import type { CouponDTO, CouponVO, PageResult } from '@/types'

export function getAdminCoupons(current = 1, size = 20): Promise<PageResult<CouponVO>> {
  return http.get<PageResult<CouponVO>>('/coupons/admin', { current, size })
}

export function createCoupon(data: CouponDTO): Promise<{ id: number }> {
  return http.post<{ id: number }>('/coupons/admin', data)
}

export function updateCoupon(id: number, data: CouponDTO): Promise<void> {
  return http.put<void>(`/coupons/admin/${id}`, data)
}

export function deleteCoupon(id: number): Promise<void> {
  return http.delete<void>(`/coupons/admin/${id}`)
}
