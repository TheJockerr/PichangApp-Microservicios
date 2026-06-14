import { Search, Bell } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import UserAvatar from './UserAvatar'

/**
 * Barra superior: buscador (visual), notificaciones e identidad del admin.
 */
function displayName(email) {
  if (!email) return 'Administrador'
  const local = email.split('@')[0]
  return local.charAt(0).toUpperCase() + local.slice(1)
}

export default function Topbar() {
  const { adminEmail } = useAuth()
  const name = displayName(adminEmail)

  return (
    <header className="topbar">
      <label className="topbar-search">
        <Search size={18} />
        <input type="text" placeholder="Buscar en el panel…" aria-label="Buscar" />
      </label>

      <div className="topbar-spacer" />

      <div className="topbar-right">
        <button className="topbar-bell" aria-label="Notificaciones" type="button">
          <Bell size={20} strokeWidth={2.2} />
          <span className="topbar-badge" />
        </button>

        <div className="topbar-admin">
          <div className="topbar-admin-meta">
            <div className="topbar-admin-name">{name}</div>
            <div className="topbar-admin-role">Administrador</div>
          </div>
          <UserAvatar name={name} email={adminEmail} size="md" />
        </div>
      </div>
    </header>
  )
}
