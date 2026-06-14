import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Mail, Lock, Users, CalendarCheck, Trophy } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()

  const [correo, setCorreo] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(correo.trim(), password)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      const status = err?.response?.status
      if (status === 401) {
        setError('Credenciales inválidas')
      } else if (status === 403) {
        setError('La cuenta no está verificada')
      } else {
        setError(err.message || 'No se pudo iniciar sesión')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-wrapper">
      {/* Lado izquierdo: hero de marca */}
      <section className="login-hero">
        <div className="login-hero-content animate-fade-up">
          <img src="/logo.png" alt="PichangApp" className="login-hero-logo" />
          <h1 className="login-hero-title">PichangApp</h1>
          <p className="login-hero-slogan">Gestiona tu comunidad deportiva</p>

          <div className="login-hero-features">
            <div className="login-hero-feature">
              <span className="login-hero-feature-icon">
                <Users size={20} strokeWidth={2.4} />
              </span>
              Administra usuarios y su karma
            </div>
            <div className="login-hero-feature">
              <span className="login-hero-feature-icon">
                <CalendarCheck size={20} strokeWidth={2.4} />
              </span>
              Controla todos los eventos
            </div>
            <div className="login-hero-feature">
              <span className="login-hero-feature-icon">
                <Trophy size={20} strokeWidth={2.4} />
              </span>
              Impulsa el juego limpio
            </div>
          </div>
        </div>
      </section>

      {/* Lado derecho: formulario */}
      <section className="login-panel">
        <form className="login-card animate-fade-up" onSubmit={handleSubmit}>
          <div className="login-card-mobile-brand">
            <img
              src="/logo.png"
              alt="PichangApp"
              className="login-hero-logo"
              style={{ width: 84, height: 84, marginBottom: 12 }}
            />
            <h1 className="login-card-title" style={{ color: 'var(--color-primary)' }}>
              PichangApp
            </h1>
          </div>

          <h2 className="login-card-title">¡Bienvenido! 👋</h2>
          <p className="login-card-subtitle">Ingresa con tu cuenta de administrador</p>

          {error && <div className="alert alert-error">{error}</div>}

          <div className="field">
            <span className="field-label">Correo</span>
            <label className="input-icon">
              <Mail size={18} />
              <input
                type="email"
                value={correo}
                onChange={(e) => setCorreo(e.target.value)}
                placeholder="admin@pichangapp.cl"
                required
              />
            </label>
          </div>

          <div className="field">
            <span className="field-label">Contraseña</span>
            <label className="input-icon">
              <Lock size={18} />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
            </label>
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-block btn-lg"
            disabled={loading}
            style={{ marginTop: 'var(--space-sm)' }}
          >
            {loading ? 'Ingresando…' : 'Ingresar'}
          </button>
        </form>
      </section>
    </div>
  )
}
