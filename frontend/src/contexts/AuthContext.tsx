import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { loginApi, registerApi, getProfileApi } from '../api/auth'
import type { User } from '../types'

interface AuthContextType {
  user: User | null
  token: string | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'))
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const login = useCallback(async (email: string, password: string) => {
    setLoading(true)
    try {
      const res = await loginApi({ email, password })
      const { token: newToken, userId } = res.data
      localStorage.setItem('token', newToken)
      localStorage.setItem('userId', userId)
      setToken(newToken)
      const profile = await getProfileApi()
      setUser(profile.data)
      navigate('/')
    } finally {
      setLoading(false)
    }
  }, [navigate])

  const register = useCallback(async (name: string, email: string, password: string) => {
    setLoading(true)
    try {
      const res = await registerApi({ name, email, password })
      const { token: newToken, userId } = res.data
      localStorage.setItem('token', newToken)
      localStorage.setItem('userId', userId)
      setToken(newToken)
      const profile = await getProfileApi()
      setUser(profile.data)
      navigate('/')
    } finally {
      setLoading(false)
    }
  }, [navigate])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    setToken(null)
    setUser(null)
    navigate('/login')
  }, [navigate])

  return (
    <AuthContext.Provider value={{
      user, token, loading,
      login, register, logout,
      isAuthenticated: !!token,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
