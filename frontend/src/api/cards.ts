import client from './client'
import type { ApiResponse, CreditCard } from '../types'

export const getCardsApi = async (): Promise<ApiResponse<CreditCard[]>> => {
  const res = await client.get('/cards')
  return res.data
}

export const createCardApi = async (data: Partial<CreditCard>): Promise<ApiResponse<CreditCard>> => {
  const res = await client.post('/cards', data)
  return res.data
}

export const updateCardApi = async (id: string, data: Partial<CreditCard>): Promise<ApiResponse<CreditCard>> => {
  const res = await client.patch(`/cards/${id}`, data)
  return res.data
}

export const deleteCardApi = async (id: string): Promise<ApiResponse<null>> => {
  const res = await client.delete(`/cards/${id}`)
  return res.data
}
