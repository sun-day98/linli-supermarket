import { http } from './request'
import type { OrderVO, StatsOverviewVO, StatsTrendVO } from '@/types'

export function getOverview(): Promise<StatsOverviewVO> {
  return http.get<StatsOverviewVO>('/admin/stats/overview')
}

export function getTrend(days = 7): Promise<StatsTrendVO> {
  return http.get<StatsTrendVO>('/admin/stats/trend', { days })
}

export function getLatestOrders(limit = 6): Promise<OrderVO[]> {
  return http.get<OrderVO[]>('/admin/stats/latest-orders', { limit })
}
