import { http } from './request'
import type { PageResult, PointsRecord, UserVO } from '@/types'

export function getProfile(): Promise<UserVO> {
  return http.get<UserVO>('/member/profile')
}

export function getPointsRecords(current = 1, size = 20): Promise<PageResult<PointsRecord>> {
  return http.get<PageResult<PointsRecord>>('/points/records', { current, size })
}
