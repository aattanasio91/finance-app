import client from './client'
import type { ApiResponse, AuthResult, LoginRequest, RegisterRequest, User } from '../types'

export const loginApi = async (data: LoginRequest): Promise<ApiResponse<AuthResult>> => {
  const res = await client.post('/auth/login', data)
  return res.data
}

export const registerApi = async (data: RegisterRequest): Promise<ApiResponse<AuthResult>> => {
  const res = await client.post('/auth/register', data)
  return res.data
}

export const getProfileApi = async (): Promise<ApiResponse<User>> => {
  const res = await client.get('/users/me')
  return res.data
}
