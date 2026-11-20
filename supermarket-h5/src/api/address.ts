import { http } from './request'
import type { Address, AddressDTO } from '@/types'

export function getAddresses(): Promise<Address[]> {
  return http.get<Address[]>('/addresses')
}

export function createAddress(data: AddressDTO): Promise<{ id: number }> {
  return http.post<{ id: number }>('/addresses', data)
}

export function updateAddress(id: number, data: AddressDTO): Promise<void> {
  return http.put<void>(`/addresses/${id}`, data)
}

export function removeAddress(id: number): Promise<void> {
  return http.delete<void>(`/addresses/${id}`)
}

export function setDefaultAddress(id: number): Promise<void> {
  return http.put<void>(`/addresses/${id}/default`)
}
