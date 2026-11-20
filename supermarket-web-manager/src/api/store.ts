import { http } from './request'
import type { StoreSetting } from '@/types'

export function getStore(): Promise<StoreSetting> {
  return http.get<StoreSetting>('/store')
}

export function updateStore(data: StoreSetting): Promise<void> {
  return http.put<void>('/store', data)
}
