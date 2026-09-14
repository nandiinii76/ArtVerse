import { api } from '@/lib/api'
import type { ApiResponse, AuthResponse } from '@/types/auth'

export interface RegisterPayload {
  email: string
  password: string
  displayName: string
}

export interface LoginPayload {
  email: string
  password: string
}

function unwrap<T>(response: { data: ApiResponse<T> }): T {
  const body = response.data
  if (!body.success || body.data === null) {
    throw new Error(body.message ?? 'Request failed')
  }
  return body.data
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  const res = await api.post<ApiResponse<AuthResponse>>('/auth/register', payload)
  return unwrap(res)
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const res = await api.post<ApiResponse<AuthResponse>>('/auth/login', payload)
  return unwrap(res)
}

export async function fetchCurrentUser() {
  const res = await api.get<ApiResponse<AuthResponse['user']>>('/auth/me')
  return unwrap(res)
}
