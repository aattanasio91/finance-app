import client from './client'
import type { ApiResponse, RecurringExpense } from '../types'

export const getRecurringApi = async (): Promise<ApiResponse<RecurringExpense[]>> => {
  const res = await client.get('/recurring-expenses')
  return res.data
}

export const createRecurringApi = async (data: Partial<RecurringExpense>): Promise<ApiResponse<RecurringExpense>> => {
  const res = await client.post('/recurring-expenses', data)
  return res.data
}

export const updateRecurringApi = async (id: string, data: Partial<RecurringExpense>): Promise<ApiResponse<RecurringExpense>> => {
  const res = await client.patch(`/recurring-expenses/${id}`, data)
  return res.data
}

export const deleteRecurringApi = async (id: string): Promise<ApiResponse<null>> => {
  const res = await client.delete(`/recurring-expenses/${id}`)
  return res.data
}
