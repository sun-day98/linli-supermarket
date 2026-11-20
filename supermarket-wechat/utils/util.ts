/** 金额统一展示为 ¥xx.xx（仅格式化，不参与计算）。 */
export function money(amount: number | string | null | undefined): string {
  const value = Number(amount ?? 0)
  return `¥${(Number.isFinite(value) ? value : 0).toFixed(2)}`
}

/** 日期时间由后端以 yyyy-MM-dd HH:mm:ss 返回，原样展示。 */
export function dateTime(value: string | null | undefined): string {
  return value ? value.replace('T', ' ').slice(0, 19) : ''
}

/** 订单状态数值对应的中文文案。 */
export function orderStatusText(status: number): string {
  const texts = ['待付款', '待配送', '配送中', '已完成', '已取消']
  return texts[status] || '未知'
}

/** 券状态文案：0未用 1已用 2过期 3锁定。 */
export function couponStatusText(status: number | null | undefined): string {
  if (status === null || status === undefined) return '—'
  const texts = ['未使用', '已使用', '已过期', '锁定中']
  return texts[status] || '未知'
}
