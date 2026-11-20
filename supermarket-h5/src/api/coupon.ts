import { http } from './request'
import type { CouponVO, PageResult } from '@/types'

export function getAvailableCoupons(amount: number): Promise<CouponVO[]> {
  return http.get<CouponVO[]>('/coupons/available', { amount })
}

export function getMyCoupons(): Promise<CouponVO[]> {
  return http.get<CouponVO[]>('/coupons/my')
}

/** 演示环境：券定义列表接口需有效 Token，H5 用户 token 同样可访问。 */
export function getCouponDefinitions(): Promise<CouponVO[]> {
  return http.get<PageResult<CouponVO>>('/coupons/admin', { current: 1, size: 50 }).then((page) => page.records)
}

export function claimCoupon(id: number): Promise<void> {
  return http.post<void>(`/coupons/claim/${id}`)
}
