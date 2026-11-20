import { http } from '../utils/request'
import type { Category, PageResult, ProductVO, Review } from '../typings/types'

export function getProducts(params: Record<string, unknown> = {}): Promise<PageResult<ProductVO>> {
  return http.get<PageResult<ProductVO>>('/products', params)
}

export function getProduct(id: number): Promise<ProductVO> {
  return http.get<ProductVO>(`/products/${id}`)
}

export function getCategories(): Promise<Category[]> {
  return http.get<Category[]>('/categories')
}

export function getReviews(productId: number): Promise<PageResult<Review>> {
  return http.get<PageResult<Review>>('/reviews', { productId, current: 1, size: 10 })
}
