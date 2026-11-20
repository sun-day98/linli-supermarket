import { http } from './request'
import type { LoginDTO, UserVO } from '@/types'

export function login(data: LoginDTO): Promise<UserVO> {
  return http.post<UserVO>('/auth/login', data)
}

export function fetchMe(): Promise<UserVO> {
  return http.get<UserVO>('/auth/me')
}
