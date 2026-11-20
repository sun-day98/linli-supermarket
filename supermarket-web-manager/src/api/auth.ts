import { http } from './request'
import type { AdminLoginDTO, AdminProfile } from '@/types'

export function adminLogin(data: AdminLoginDTO): Promise<AdminProfile> {
  return http.post<AdminProfile>('/auth/admin-login', data)
}

export function fetchMe(): Promise<AdminProfile> {
  return http.get<AdminProfile>('/auth/me')
}
