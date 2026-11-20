/** 管理员 Token 存储，与 H5 共用 key。 */
const TOKEN_KEY = 'linli_token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  if (token) localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function isLogin(): boolean {
  return getToken().length > 0
}
