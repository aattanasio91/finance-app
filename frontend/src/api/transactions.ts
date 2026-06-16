import client from './client'
import type { ApiResponse, Transaction } from '../types'

export interface TransactionFilters {
  accountId?: string
  categoryId?: string
  merchantId?: string
  type?: string
  from?: string
  to?: string
  isManual?: boolean
  isRecurring?: boolean
  page?: number
  size?: number
  sort?: string
}

export const getTransactionsApi = async (filters?: TransactionFilters): Promise<ApiResponse<any>> => {
  const res = await client.get('/transactions', { params: filters })
  return res.data
}

export const getTransactionApi = async (id: string): Promise<ApiResponse<Transaction>> => {
  const res = await client.get(`/transactions/${id}`)
  return res.data
}

export const createTransactionApi = async (data: Partial<Transaction>): Promise<ApiResponse<Transaction>> => {
  const res = await client.post('/transactions', data)
  return res.data
}

export const updateTransactionApi = async (id: string, data: Partial<Transaction>): Promise<ApiResponse<Transaction>> => {
  const res = await client.patch(`/transactions/${id}`, data)
  return res.data
}

export const deleteTransactionApi = async (id: string): Promise<ApiResponse<null>> => {
  const res = await client.delete(`/transactions/${id}`)
  return res.data
}
