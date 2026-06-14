import { NavLink, useNavigate } from 'react-router-dom'
import { LayoutDashboard, Users, CalendarDays, LogOut } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import UserAvatar from './UserAvatar'

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/users', label: 'Usuarios', icon: Users },
  { to: '/events', label: 'Eventos', icon: CalendarDays },
]

export default function Navbar() {
  const navigate = useNavigate()
  const { logout, adminEmail } = useAuth()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <img src="/logo.png" alt="PichangApp" className="sidebar-logo" />
        <div>
          <div className="sidebar-title">PichangApp</div>
          <div className="sidebar-subtitle">Administración</div>
        </div>
      </div>

      <nav className="sidebar-nav">
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => 'nav-item' + (isActive ? ' active' : '')}
          >
            <span className="nav-icon">
              <Icon size={20} strokeWidth={2.4} />
            </span>
            <span className="nav-label">{label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-user">
          <UserAvatar name="Administrador" email={adminEmail} size="sm" />
          <div className="sidebar-user-info">
            <div className="sidebar-user-name">Administrador</div>
            {adminEmail && (
              <div className="sidebar-email" title={adminEmail}>
                {adminEmail}
              </div>
            )}
          </div>
        </div>
        <button className="btn-logout" onClick={handleLogout}>
          <LogOut size={18} strokeWidth={2.4} />
          <span>Cerrar sesión</span>
        </button>
      </div>
    </aside>
  )
}
