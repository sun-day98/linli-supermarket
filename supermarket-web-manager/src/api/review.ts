import { http } from './request'
import type { PageResult, Review } from '@/types'

export function getAdminReviews(status: number | undefined, current = 1, size = 20): Promise<PageResult<Review>> {
  const params: Record<string, unknown> = { current, size }
  if (status !== undefined) params.status = status
  return http.get<PageResult<Review>>('/admin/reviews', params)
}

export function auditReview(id: number, status: number): Promise<void> {
  return http.put<void>(`/admin/reviews/${id}/audit?status=${status}`)
}

export function deleteReview(id: number): Promise<void> {
  return http.delete<void>(`/admin/reviews/${id}`)
}
