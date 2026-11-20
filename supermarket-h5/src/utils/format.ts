/** 金额与日期展示工具，金额统一 ¥ + 两位小数。 */

export function yuan(amount: number | string | null | undefined): string {
  const value = Number(amount ?? 0)
  return `¥${(Number.isFinite(value) ? value : 0).toFixed(2)}`
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 19)
}

let toastTimer: number | undefined

/** 轻量移动端 Toast，无需引入 UI 组件库。 */
export function toast(message: string, duration = 2200): void {
  if (typeof document === 'undefined') return
  const old = document.getElementById('linli-toast')
  if (old) old.remove()
  const el = document.createElement('div')
  el.id = 'linli-toast'
  el.className = 'linli-toast'
  el.textContent = message
  document.body.appendChild(el)
  if (toastTimer !== undefined) window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    el.remove()
    toastTimer = undefined
  }, duration)
}
