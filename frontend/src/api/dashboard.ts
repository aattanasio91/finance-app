import client from './client'
import type { ApiResponse, DashboardData, CategoryExpense, MonthlyEvolution } from '../types'

export const getDashboardApi = async (): Promise<ApiResponse<DashboardData>> => {
  const res = await client.get('/dashboard')
  return res.data
}

export const getExpensesByCategoryApi = async (from?: string, to?: string): Promise<ApiResponse<CategoryExpense[]>> => {
  const params: Record<string, string> = {}
  if (from) params.from = from
  if (to) params.to = to
  const res = await client.get('/dashboard/expenses-by-category', { params })
  return res.data
}

export const getMonthlyEvolutionApi = async (from?: string, to?: string): Promise<ApiResponse<MonthlyEvolution[]>> => {
  const params: Record<string, string> = {}
  if (from) params.from = from
  if (to) params.to = to
  const res = await client.get('/dashboard/monthly-evolution', { params })
  return res.data
}
