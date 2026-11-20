import { http } from './request'
import type { PageResult, Review } from '@/types'

export function getReviews(productId: number, current = 1, size = 10): Promise<PageResult<Review>> {
  return http.get<PageResult<Review>>('/reviews', { productId, current, size })
}

export function submitReview(data: {
  productId: number
  orderId?: number
  rating: number
  content: string
}): Promise<{ id: number }> {
  return http.post<{ id: number }>('/reviews', data)
}
