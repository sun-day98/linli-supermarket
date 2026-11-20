import { http } from './request'
import type { Category } from '@/types'

export function getCategories(): Promise<Category[]> {
  return http.get<Category[]>('/categories/all')
}

export function createCategory(data: Omit<Category, 'id'>): Promise<{ id: number }> {
  return http.post<{ id: number }>('/categories', data)
}

export function updateCategory(id: number, data: Omit<Category, 'id'>): Promise<void> {
  return http.put<void>(`/categories/${id}`, data)
}

export function deleteCategory(id: number): Promise<void> {
  return http.delete<void>(`/categories/${id}`)
}
