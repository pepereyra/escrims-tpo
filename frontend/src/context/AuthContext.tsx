import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { authApi } from '../api'
import { clearToken, getToken, setToken } from '../api/client'
import type { Usuario } from '../api/types'

interface AuthContextValue {
  user: Usuario | null
  loading: boolean
  login: (username: string, password: string) => Promise<void>
  register: (data: {
    username: string
    email: string
    password: string
    region: string
    juego: string
    rango: number
    latencia: number
  }) => Promise<void>
  logout: () => void
  refreshUser: () => Promise<void>
  isMod: boolean
  isAdmin: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Usuario | null>(null)
  const [loading, setLoading] = useState(true)

  const refreshUser = useCallback(async () => {
    if (!getToken()) {
      setUser(null)
      return
    }
    try {
      const me = await authApi.me()
      setUser(me)
    } catch {
      clearToken()
      setUser(null)
    }
  }, [])

  useEffect(() => {
    refreshUser().finally(() => setLoading(false))
  }, [refreshUser])

  const login = useCallback(async (username: string, password: string) => {
    const res = await authApi.login(username, password)
    setToken(res.token)
    setUser(res.usuario)
  }, [])

  const register = useCallback(
    async (data: {
      username: string
      email: string
      password: string
      region: string
      juego: string
      rango: number
      latencia: number
    }) => {
      const res = await authApi.register(data)
      setToken(res.token)
      setUser(res.usuario)
    },
    [],
  )

  const logout = useCallback(() => {
    clearToken()
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      loading,
      login,
      register,
      logout,
      refreshUser,
      isMod: user?.rolSistema === 'MOD' || user?.rolSistema === 'ADMIN',
      isAdmin: user?.rolSistema === 'ADMIN',
    }),
    [user, loading, login, register, logout, refreshUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return ctx
}
