import client from './client'
import type { ApiResponse, Budget, BudgetSummary } from '../types'

export const getBudgetsApi = async (): Promise<ApiResponse<Budget[]>> => {
  const res = await client.get('/budgets')
  return res.data
}

export const createBudgetApi = async (data: Partial<Budget>): Promise<ApiResponse<Budget>> => {
  const res = await client.post('/budgets', data)
  return res.data
}

export const updateBudgetApi = async (id: string, data: Partial<Budget>): Promise<ApiResponse<Budget>> => {
  const res = await client.patch(`/budgets/${id}`, data)
  return res.data
}

export const deleteBudgetApi = async (id: string): Promise<ApiResponse<null>> => {
  const res = await client.delete(`/budgets/${id}`)
  return res.data
}

export const getBudgetSummaryApi = async (): Promise<ApiResponse<BudgetSummary[]>> => {
  const res = await client.get('/budgets/summary')
  return res.data
}
