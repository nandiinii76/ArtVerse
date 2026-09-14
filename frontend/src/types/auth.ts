export type Role = 'USER' | 'ARTIST' | 'GALLERY' | 'CURATOR' | 'ADMIN' | 'MODERATOR'

export interface User {
  id: string
  email: string
  displayName: string
  avatarUrl: string | null
  roles: Role[]
  emailVerified: boolean
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  message: string | null
  errorCode: string | null
  timestamp: string
}
