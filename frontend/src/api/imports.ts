import client from './client'
import type { ApiResponse, ImportJob } from '../types'

export const uploadFileApi = async (file: File, sourceType: string, accountId: string): Promise<ApiResponse<ImportJob>> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('sourceType', sourceType)
  formData.append('accountId', accountId)
  const res = await client.post('/imports/upload', formData)
  return res.data
}

export const getImportJobsApi = async (): Promise<ApiResponse<ImportJob[]>> => {
  const res = await client.get('/imports')
  return res.data
}

export const getImportJobApi = async (id: string): Promise<ApiResponse<ImportJob>> => {
  const res = await client.get(`/imports/${id}`)
  return res.data
}

export const getImportErrorsApi = async (id: string): Promise<ApiResponse<any[]>> => {
  const res = await client.get(`/imports/${id}/errors`)
  return res.data
}
