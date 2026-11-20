import { http } from './request'
import type { DeliveryArea, DeliveryAreaDTO, Deliveryman } from '@/types'

export function getAreas(): Promise<DeliveryArea[]> {
  return http.get<DeliveryArea[]>('/admin/delivery/areas')
}

export function createArea(data: DeliveryAreaDTO): Promise<{ id: number }> {
  return http.post<{ id: number }>('/admin/delivery/areas', data)
}

export function updateArea(id: number, data: DeliveryAreaDTO): Promise<void> {
  return http.put<void>(`/admin/delivery/areas/${id}`, data)
}

export function deleteArea(id: number): Promise<void> {
  return http.delete<void>(`/admin/delivery/areas/${id}`)
}

export function getDeliverymen(): Promise<Deliveryman[]> {
  return http.get<Deliveryman[]>('/admin/deliverymen')
}

export function createDeliveryman(data: Deliveryman): Promise<{ id: number }> {
  return http.post<{ id: number }>('/admin/deliverymen', data)
}

export function updateDeliveryman(id: number, data: Deliveryman): Promise<void> {
  return http.put<void>(`/admin/deliverymen/${id}`, data)
}

export function deleteDeliveryman(id: number): Promise<void> {
  return http.delete<void>(`/admin/deliverymen/${id}`)
}
