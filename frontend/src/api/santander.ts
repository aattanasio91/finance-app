import client from './client'
import type { ApiResponse, ImportJob } from '../types'

export const uploadSantanderBankPdfApi = async (
  accountId: string, file: File
): Promise<ApiResponse<ImportJob>> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('accountId', accountId)
  const res = await client.post('/imports/santander/bank-pdf', formData)
  return res.data
}

export const uploadSantanderCardPdfApi = async (
  accountId: string, file: File
): Promise<ApiResponse<ImportJob>> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('accountId', accountId)
  const res = await client.post('/imports/santander/card-pdf', formData)
  return res.data
}

export const uploadSantanderCardExcelApi = async (
  accountId: string, file: File
): Promise<ApiResponse<ImportJob>> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('accountId', accountId)
  const res = await client.post('/imports/santander/card-excel', formData)
  return res.data
}

export const uploadSantanderInstallmentsXlsApi = async (
  accountId: string, cardId: string, file: File
): Promise<ApiResponse<ImportJob>> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('accountId', accountId)
  formData.append('cardId', cardId)
  const res = await client.post('/imports/santander/installments-xls', formData)
  return res.data
}
