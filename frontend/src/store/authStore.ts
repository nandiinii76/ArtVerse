import { create } from 'zustand'
import type { AuthResponse, User } from '@/types/auth'

const STORAGE_KEY = 'artverse.session'

interface StoredSession {
  accessToken: string
  refreshToken: string
  user: User
}

function loadStoredSession(): StoredSession | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as StoredSession) : null
  } catch {
    return null
  }
}

interface AuthState {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  setSession: (auth: AuthResponse) => void
  logout: () => void
}

const stored = loadStoredSession()

export const useAuthStore = create<AuthState>((set) => ({
  user: stored?.user ?? null,
  accessToken: stored?.accessToken ?? null,
  refreshToken: stored?.refreshToken ?? null,
  isAuthenticated: !!stored,

  setSession: (auth) => {
    const session: StoredSession = {
      accessToken: auth.accessToken,
      refreshToken: auth.refreshToken,
      user: auth.user,
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
    set({
      user: auth.user,
      accessToken: auth.accessToken,
      refreshToken: auth.refreshToken,
      isAuthenticated: true,
    })
  },

  logout: () => {
    localStorage.removeItem(STORAGE_KEY)
    set({ user: null, accessToken: null, refreshToken: null, isAuthenticated: false })
  },
}))
