import axios from 'axios'
import { useAuthStore } from '@/store/authStore'
import type { ApiResponse, AuthResponse } from '@/types/auth'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1',
})

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshingPromise: Promise<string | null> | null = null

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const status = error.response?.status

    if (status !== 401 || original._retry) {
      return Promise.reject(error)
    }

    const refreshToken = useAuthStore.getState().refreshToken
    if (!refreshToken) {
      useAuthStore.getState().logout()
      return Promise.reject(error)
    }

    original._retry = true

    if (!refreshingPromise) {
      refreshingPromise = axios
        .post<ApiResponse<AuthResponse>>(
          `${api.defaults.baseURL}/auth/refresh`,
          { refreshToken },
        )
        .then((res) => {
          const auth = res.data.data
          if (!auth) throw new Error('No auth payload on refresh')
          useAuthStore.getState().setSession(auth)
          return auth.accessToken
        })
        .catch(() => {
          useAuthStore.getState().logout()
          return null
        })
        .finally(() => {
          refreshingPromise = null
        })
    }

    const newToken = await refreshingPromise
    if (!newToken) return Promise.reject(error)

    original.headers.Authorization = `Bearer ${newToken}`
    return api(original)
  },
)

/** Pulls a human-readable message out of a failed axios/ApiResponse error. */
export function extractErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message
    if (typeof message === 'string' && message.length > 0) return message
  }
  return fallback
}
