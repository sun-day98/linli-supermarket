import { http } from '../utils/request'
import type { CartVO } from '../typings/types'

export function getCart(): Promise<CartVO[]> {
  return http.get<CartVO[]>('/cart')
}

/** 后端 CartDTO 校验要求 productId 必填，改数量/勾选时也携带。 */
export function addToCart(productId: number, quantity = 1): Promise<void> {
  return http.post<void>('/cart', { productId, quantity })
}

export function updateCartItem(id: number, productId: number, quantity: number, selected: number): Promise<void> {
  return http.put<void>(`/cart/${id}`, { productId, quantity, selected })
}

export function removeCartItem(id: number): Promise<void> {
  return http.delete<void>(`/cart/${id}`)
}
