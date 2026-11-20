import { http } from '../utils/request'
import type { DeliveryAreaVO, StoreSetting } from '../typings/types'

export function getStore(): Promise<StoreSetting> {
  return http.get<StoreSetting>('/store')
}

export function getDeliveryAreas(): Promise<DeliveryAreaVO[]> {
  return http.get<DeliveryAreaVO[]>('/delivery/areas')
}
