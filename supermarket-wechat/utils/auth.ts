/** Token 本地存储，key 与 H5 / 后台统一。 */
const TOKEN_KEY = 'linli_token'

export function getToken(): string {
  return String(wx.getStorageSync(TOKEN_KEY) || '')
}

export function setToken(token: string): void {
  if (token) wx.setStorageSync(TOKEN_KEY, token)
}

export function removeToken(): void {
  wx.removeStorageSync(TOKEN_KEY)
  const app = getApp<IAppOption>()
  if (app) app.globalData.token = ''
}

export function isLogin(): boolean {
  return getToken().length > 0
}

/** 未登录时跳转登录页并返回 false。 */
export function ensureLogin(): boolean {
  if (isLogin()) return true
  wx.navigateTo({ url: '/pages/login/login' })
  return false
}
