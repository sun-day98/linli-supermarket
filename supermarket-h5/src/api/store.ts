import { http } from './request'
import type { StoreSetting } from '@/types'

export function getStore(): Promise<StoreSetting> {
  return http.get<StoreSetting>('/store')
}
