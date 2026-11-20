import { http } from './request'
import type { CartDTO, CartVO } from '@/types'

export function getCart(): Promise<CartVO[]> {
  return http.get<CartVO[]>('/cart')
}

export function addToCart(data: CartDTO): Promise<void> {
  return http.post<void>('/cart', data)
}

/** 后端 CartDTO 校验要求 productId 必填，因此更新时也携带该字段。 */
export function updateCartItem(id: number, data: CartDTO): Promise<void> {
  return http.put<void>(`/cart/${id}`, data)
}

export function removeCartItem(id: number): Promise<void> {
  return http.delete<void>(`/cart/${id}`)
}

export function clearSelectedCart(): Promise<void> {
  return http.delete<void>('/cart')
}
