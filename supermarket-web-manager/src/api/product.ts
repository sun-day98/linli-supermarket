import { http } from './request'
import type { PageResult, ProductDTO, ProductQuery, ProductVO } from '@/types'

export function getProducts(params: ProductQuery = {}): Promise<PageResult<ProductVO>> {
  return http.get<PageResult<ProductVO>>('/products', params as Record<string, unknown>)
}

export function createProduct(data: ProductDTO): Promise<{ id: number }> {
  return http.post<{ id: number }>('/products', data)
}

export function updateProduct(id: number, data: ProductDTO): Promise<void> {
  return http.put<void>(`/products/${id}`, data)
}

export function deleteProduct(id: number): Promise<void> {
  return http.delete<void>(`/products/${id}`)
}

export function changeProductStatus(id: number, status: number): Promise<void> {
  return http.put<void>(`/products/${id}/status?status=${status}`)
}
