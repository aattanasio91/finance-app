import client from './client'
import type { ApiResponse, Category } from '../types'

export const getCategoriesApi = async (type?: string): Promise<ApiResponse<Category[]>> => {
  const params = type ? { type } : {}
  const res = await client.get('/categories', { params })
  return res.data
}

export const createCategoryApi = async (data: Partial<Category>): Promise<ApiResponse<Category>> => {
  const res = await client.post('/categories', data)
  return res.data
}

export const updateCategoryApi = async (id: string, data: Partial<Category>): Promise<ApiResponse<Category>> => {
  const res = await client.patch(`/categories/${id}`, data)
  return res.data
}

export const deleteCategoryApi = async (id: string): Promise<ApiResponse<null>> => {
  const res = await client.delete(`/categories/${id}`)
  return res.data
}
