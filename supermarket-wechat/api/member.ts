import { http } from '../utils/request'
import type { Address, CouponVO, PageResult, PointsRecord, UserVO } from '../typings/types'

/** 会员资料与积分。 */
export function getProfile(): Promise<UserVO> {
  return http.get<UserVO>('/member/profile')
}

export function getPointsRecords(): Promise<PageResult<PointsRecord>> {
  return http.get<PageResult<PointsRecord>>('/points/records', { current: 1, size: 20 })
}

/** 优惠券（结算选券与我的券共用）。 */
export function getAvailableCoupons(amount: number): Promise<CouponVO[]> {
  return http.get<CouponVO[]>('/coupons/available', { amount })
}

export function getMyCoupons(): Promise<CouponVO[]> {
  return http.get<CouponVO[]>('/coupons/my')
}

/** 地址（结算页内联选择；新用户自动注册时已有默认地址）。 */
export function getAddresses(): Promise<Address[]> {
  return http.get<Address[]>('/addresses')
}
