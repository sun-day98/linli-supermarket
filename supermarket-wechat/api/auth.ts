import { http } from '../utils/request'
import type { UserVO } from '../typings/types'

export function login(phone: string, code: string): Promise<UserVO> {
  return http.post<UserVO>('/auth/login', { phone, code })
}

export function fetchMe(): Promise<UserVO> {
  return http.get<UserVO>('/auth/me')
}
