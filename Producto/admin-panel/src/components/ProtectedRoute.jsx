import { Navigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import Navbar from './Navbar'
import Topbar from './Topbar'

/**
 * Protege las rutas privadas: solo permite el acceso si hay un JWT válido con rol ADMIN.
 * Renderiza el layout con barra lateral + barra superior.
 */
export default function ProtectedRoute({ children }) {
  const { isAuthenticated, isAdmin } = useAuth()

  if (!isAuthenticated || !isAdmin) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="layout">
      <Navbar />
      <div className="main-area">
        <Topbar />
        <main className="content">{children}</main>
      </div>
    </div>
  )
}
