import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { login as loginRequest } from '../api/authApi'

export function decodeJwt(token) {
  try {
    const payload = token.split('.')[1]
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(normalized)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(json)
  } catch {
    return null
  }
}

function isTokenValid(claims) {
  if (!claims) return false
  if (claims.exp && Date.now() >= claims.exp * 1000) return false
  return true
}

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'))

  const claims = token ? decodeJwt(token) : null
  const isAuthenticated = Boolean(token) && isTokenValid(claims)
  const isAdmin = claims?.role === 'ADMIN'

  // Escucha el evento que dispara el interceptor de axios ante 401
  useEffect(() => {
    const handle = () => setToken(null)
    window.addEventListener('auth:logout', handle)
    return () => window.removeEventListener('auth:logout', handle)
  }, [])

  const login = useCallback(async (correo, password) => {
    const data = await loginRequest(correo, password)
    const jwt = data.token
    const decoded = decodeJwt(jwt)
    if (!decoded || decoded.role !== 'ADMIN') {
      throw new Error('Acceso denegado: esta cuenta no tiene privilegios de administrador')
    }
    localStorage.setItem('token', jwt)
    if (data.user?.correo) localStorage.setItem('adminEmail', data.user.correo)
    setToken(jwt)
    return decoded
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('adminEmail')
    setToken(null)
  }, [])

  return (
    <AuthContext.Provider
      value={{
        token,
        claims,
        isAuthenticated,
        isAdmin,
        adminEmail: localStorage.getItem('adminEmail') || claims?.correo || '',
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return ctx
}
