import { http } from './request'
import type { MemberSummary, OrderVO, PageResult, PointsRecord } from '@/types'

/**
 * 演示后端未提供独立的会员列表接口，这里按订单聚合出会员概览：
 * userId / 收货人 / 电话 / 订单数 / 累计消费 / 最近下单时间。
 */
export async function getMembers(): Promise<MemberSummary[]> {
  const first = await http.get<PageResult<OrderVO>>('/admin/orders', { current: 1, size: 100 })
  const all = first.records.slice()
  const pages = Math.ceil(first.total / 100)
  for (let page = 2; page <= pages; page += 1) {
    const next = await http.get<PageResult<OrderVO>>('/admin/orders', { current: page, size: 100 })
    all.push(...next.records)
  }
  const map = new Map<number, MemberSummary>()
  for (const order of all) {
    const existing = map.get(order.userId)
    if (existing) {
      existing.orderCount += 1
      existing.totalAmount += order.payAmount
      if (order.createdAt > existing.lastOrderAt) existing.lastOrderAt = order.createdAt
    } else {
      map.set(order.userId, {
        userId: order.userId,
        receiver: order.receiver,
        phone: order.phone,
        orderCount: 1,
        totalAmount: order.payAmount,
        lastOrderAt: order.createdAt,
      })
    }
  }
  return Array.from(map.values()).sort((a, b) => b.totalAmount - a.totalAmount)
}

/** 积分明细（管理端查看当前登录管理员自身时无业务数据，保留接口封装）。 */
export function getPointsRecords(current = 1, size = 20): Promise<PageResult<PointsRecord>> {
  return http.get<PageResult<PointsRecord>>('/points/records', { current, size })
}
