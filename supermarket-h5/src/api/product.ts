import { http } from './request'
import type { Category, PageResult, ProductQuery, ProductVO } from '@/types'

export function getProducts(params: ProductQuery = {}): Promise<PageResult<ProductVO>> {
  return http.get<PageResult<ProductVO>>('/products', params as Record<string, unknown>)
}

export function getProduct(id: number): Promise<ProductVO> {
  return http.get<ProductVO>(`/products/${id}`)
}

/** 前端只展示启用分类。 */
export function getCategories(): Promise<Category[]> {
  return http.get<Category[]>('/categories')
}
