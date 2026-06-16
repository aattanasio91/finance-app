import client from './client'
import type { Account, ApiResponse } from '../types'

export const getAccountsApi = async (): Promise<ApiResponse<Account[]>> => {
  const res = await client.get('/accounts')
  return res.data
}

export const createAccountApi = async (data: Partial<Account>): Promise<ApiResponse<Account>> => {
  const res = await client.post('/accounts', data)
  return res.data
}

export const updateAccountApi = async (id: string, data: Partial<Account>): Promise<ApiResponse<Account>> => {
  const res = await client.patch(`/accounts/${id}`, data)
  return res.data
}

export const deleteAccountApi = async (id: string): Promise<ApiResponse<null>> => {
  const res = await client.delete(`/accounts/${id}`)
  return res.data
}
