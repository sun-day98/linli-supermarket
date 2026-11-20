/** Token 本地存储，key 与后端及管理后台统一。 */
const TOKEN_KEY = 'linli_token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  }
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function isLogin(): boolean {
  return getToken().length > 0
}
