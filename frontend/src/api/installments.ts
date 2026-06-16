import client from './client'
import type { ApiResponse, Installment } from '../types'

export const getInstallmentsByCardApi = async (cardId: string, isPaid?: boolean): Promise<ApiResponse<Installment[]>> => {
  const params = isPaid !== undefined ? { isPaid } : {}
  const res = await client.get(`/cards/${cardId}/installments`, { params })
  return res.data
}

export const getAllInstallmentsApi = async (): Promise<ApiResponse<Installment[]>> => {
  const res = await client.get('/installments')
  return res.data
}

export const markInstallmentAsPaidApi = async (id: string): Promise<ApiResponse<null>> => {
  const res = await client.patch(`/installments/${id}/pay`)
  return res.data
}
