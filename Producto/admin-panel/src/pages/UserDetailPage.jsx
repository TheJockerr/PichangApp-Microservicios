import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query'
import {
  ArrowLeft,
  Mail,
  Shield,
  Trophy,
  ArrowUp,
  ArrowDown,
  Trash2,
  User as UserIcon,
  CircleCheck,
} from 'lucide-react'
import { getUser, deleteUser } from '../api/usersApi'
import { getKarma, adjustKarma } from '../api/karmaApi'
import ConfirmModal from '../components/ConfirmModal'
import UserAvatar from '../components/UserAvatar'
import StatusBadge from '../components/StatusBadge'
import LoadingSpinner from '../components/LoadingSpinner'

const KARMA_MAX = 100

export default function UserDetailPage() {
  const { userId } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const userQuery = useQuery({ queryKey: ['user', userId], queryFn: () => getUser(userId) })
  const karmaQuery = useQuery({
    queryKey: ['karma', userId],
    queryFn: () => getKarma(userId),
    retry: false,
  })

  const [newScore, setNewScore] = useState('')
  const [reason, setReason] = useState('')
  const [feedback, setFeedback] = useState(null)
  const [showDelete, setShowDelete] = useState(false)

  const user = userQuery.data
  const karma = karmaQuery.data

  // Pre-rellena el slider con el karma actual la primera vez que carga
  useEffect(() => {
    if (karma && newScore === '') setNewScore(String(karma.karmaScore))
  }, [karma]) // eslint-disable-line react-hooks/exhaustive-deps

  const adjustMutation = useMutation({
    mutationFn: () => adjustKarma(userId, Number(newScore), reason.trim()),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['karma', userId] })
      setFeedback({ type: 'success', text: 'Karma actualizado correctamente' })
      setReason('')
    },
    onError: () => setFeedback({ type: 'error', text: 'No se pudo actualizar el karma' }),
  })

  const deleteMutation = useMutation({
    mutationFn: () => deleteUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      navigate('/users', { replace: true })
    },
  })

  const submitAdjust = (e) => {
    e.preventDefault()
    setFeedback(null)
    if (newScore === '' || Number(newScore) < 0) {
      setFeedback({ type: 'error', text: 'Ingresa un puntaje válido (mayor o igual a 0)' })
      return
    }
    if (!reason.trim()) {
      setFeedback({ type: 'error', text: 'Ingresa una razón para el ajuste' })
      return
    }
    adjustMutation.mutate()
  }

  const sliderVal = newScore === '' ? karma?.karmaScore ?? 0 : Number(newScore)

  return (
    <div>
      <button className="btn-back" onClick={() => navigate('/users')}>
        <ArrowLeft size={18} /> Volver a usuarios
      </button>

      {userQuery.isLoading && <LoadingSpinner label="Cargando usuario…" />}
      {userQuery.isError && <div className="alert alert-error">No se pudo cargar el usuario</div>}

      {user && (
        <>
          <header className="detail-header">
            <UserAvatar name={`${user.nombre} ${user.apellido}`} email={user.correo} size="lg" />
            <div className="detail-header-info">
              <div className="detail-name">
                {user.nombre} {user.apellido}
              </div>
              <div className="detail-meta">
                <span className="detail-email">
                  <Mail size={16} /> {user.correo}
                </span>
              </div>
              <div className="detail-meta">
                <StatusBadge status={user.enabled ? 'ENABLED' : 'DISABLED'} />
                <span className={`badge ${user.role === 'ADMIN' ? 'badge-gold' : 'badge-blue'}`}>
                  {user.role === 'ADMIN' ? 'Admin' : 'Usuario'}
                </span>
              </div>
            </div>
          </header>

          <div className="detail-grid">
            <section className="card">
              <h2 className="card-title">
                <span className="card-title-icon">
                  <UserIcon size={18} />
                </span>
                Información
              </h2>
              <InfoRow icon={<UserIcon size={15} />} label="Nombre" value={`${user.nombre} ${user.apellido}`} />
              <InfoRow icon={<Mail size={15} />} label="Correo" value={user.correo} />
              <InfoRow
                icon={<Shield size={15} />}
                label="Rol"
                value={user.role === 'ADMIN' ? 'Administrador' : 'Usuario'}
              />
              <InfoRow
                icon={<CircleCheck size={15} />}
                label="Estado"
                value={user.enabled ? 'Activo' : 'Inactivo'}
              />
            </section>

            <section className="card">
              <h2 className="card-title">
                <span className="card-title-icon">
                  <Trophy size={18} />
                </span>
                Karma
              </h2>

              {karmaQuery.isLoading && <p className="muted">Cargando karma…</p>}
              {karmaQuery.isError && <p className="muted">Sin información de karma todavía.</p>}

              {karma && (
                <>
                  <KarmaGauge score={karma.karmaScore} category={karma.category} />

                  <form className="adjust-form" onSubmit={submitAdjust}>
                    <h3 className="card-title" style={{ fontSize: 15, marginTop: 8 }}>
                      Modificar karma
                    </h3>
                    <span className="field-label">Nuevo puntaje</span>
                    <div className="slider-row">
                      <span className="slider-value">{sliderVal}</span>
                      <input
                        type="range"
                        className="range-input"
                        min="0"
                        max={KARMA_MAX}
                        value={sliderVal}
                        onChange={(e) => setNewScore(e.target.value)}
                      />
                    </div>

                    <div className="field" style={{ marginTop: 12 }}>
                      <span className="field-label">Razón</span>
                      <input
                        type="text"
                        className="field-input"
                        value={reason}
                        onChange={(e) => setReason(e.target.value)}
                        placeholder="Ej: Ajuste manual por administrador"
                      />
                    </div>

                    {feedback && (
                      <div
                        className={`alert ${
                          feedback.type === 'success' ? 'alert-success' : 'alert-error'
                        }`}
                      >
                        {feedback.text}
                      </div>
                    )}

                    <button
                      type="submit"
                      className="btn btn-primary"
                      disabled={adjustMutation.isPending}
                    >
                      {adjustMutation.isPending ? 'Guardando…' : 'Guardar ajuste'}
                    </button>
                  </form>
                </>
              )}
            </section>

            <section className="card panel-wide">
              <h2 className="card-title">
                <span className="card-title-icon">
                  <ArrowUp size={18} />
                </span>
                Historial de karma
              </h2>
              {karma?.history?.length ? (
                <div className="timeline">
                  {karma.history.map((h, i) => {
                    const up = h.amount >= 0
                    return (
                      <div className="timeline-item" key={i}>
                        <div className="timeline-rail">
                          <div
                            className={`timeline-icon ${up ? 'timeline-icon-up' : 'timeline-icon-down'}`}
                          >
                            {up ? <ArrowUp size={18} strokeWidth={2.6} /> : <ArrowDown size={18} strokeWidth={2.6} />}
                          </div>
                          <div className="timeline-line" />
                        </div>
                        <div className="timeline-body">
                          <div className="timeline-top">
                            <span
                              className="timeline-amount"
                              style={{
                                color: up ? 'var(--color-primary)' : 'var(--color-danger)',
                              }}
                            >
                              {up ? `+${h.amount}` : h.amount}
                            </span>
                            <span className="timeline-date">{formatDate(h.createdAt)}</span>
                          </div>
                          <div className="timeline-reason">{h.reason}</div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              ) : (
                <p className="muted">Sin movimientos de karma.</p>
              )}
            </section>

            <section className="card panel-wide danger-zone">
              <h2 className="card-title">
                <span className="card-title-icon">
                  <Trash2 size={18} />
                </span>
                Zona peligrosa
              </h2>
              <p className="muted" style={{ marginBottom: 'var(--space-md)' }}>
                Eliminar al usuario es una acción permanente.
              </p>
              <button
                className="btn btn-danger"
                disabled={user.role === 'ADMIN'}
                title={
                  user.role === 'ADMIN'
                    ? 'No se puede eliminar a un administrador'
                    : 'Eliminar usuario'
                }
                onClick={() => setShowDelete(true)}
              >
                <Trash2 size={18} /> Eliminar usuario
              </button>
            </section>
          </div>
        </>
      )}

      <ConfirmModal
        open={showDelete}
        title="Eliminar usuario"
        message={user ? `¿Seguro que deseas eliminar a ${user.correo}?` : ''}
        confirmText="Eliminar"
        loading={deleteMutation.isPending}
        onCancel={() => setShowDelete(false)}
        onConfirm={() => deleteMutation.mutate()}
      />
    </div>
  )
}

function InfoRow({ icon, label, value }) {
  return (
    <div className="info-row">
      <span className="info-label">
        {icon} {label}
      </span>
      <span className="info-value">{value}</span>
    </div>
  )
}

/** Medidor circular de karma con color por categoría. */
function KarmaGauge({ score, category }) {
  const color = karmaColor(category, Number(score))
  const value = Number(score) || 0
  const pct = Math.max(0, Math.min(1, value / KARMA_MAX))
  const R = 54
  const C = 2 * Math.PI * R
  const offset = C * (1 - pct)

  return (
    <div className="karma-gauge">
      <div className="gauge">
        <svg width="130" height="130" viewBox="0 0 130 130">
          <circle className="gauge-track" cx="65" cy="65" r={R} />
          <circle
            className="gauge-fill"
            cx="65"
            cy="65"
            r={R}
            stroke={color}
            strokeDasharray={C}
            strokeDashoffset={offset}
          />
        </svg>
        <div className="gauge-center">
          <span className="gauge-number" style={{ color }}>
            {score}
          </span>
          <span className="gauge-max">/ {KARMA_MAX}</span>
        </div>
      </div>
      <div className="karma-side">
        <span
          className="karma-category-chip"
          style={{ background: hexToRgba(color, 0.14), color }}
        >
          <Trophy size={18} strokeWidth={2.4} /> {category}
        </span>
      </div>
    </div>
  )
}

function karmaColor(category, score) {
  const c = (category || '').toLowerCase()
  if (c.includes('excel')) return '#2E7D32'
  if (c.includes('buen') || c.includes('good') || c.includes('alto')) return '#1565C0'
  if (c.includes('regular') || c.includes('medio') || c.includes('normal')) return '#FF6F00'
  if (c.includes('bajo') || c.includes('low') || c.includes('malo') || c.includes('pobre') || c.includes('deficiente'))
    return '#C62828'
  if (score >= 80) return '#2E7D32'
  if (score >= 60) return '#1565C0'
  if (score >= 40) return '#FF6F00'
  return '#C62828'
}

function hexToRgba(hex, alpha) {
  const h = hex.replace('#', '')
  const r = parseInt(h.substring(0, 2), 16)
  const g = parseInt(h.substring(2, 4), 16)
  const b = parseInt(h.substring(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

function formatDate(value) {
  if (!value) return '—'
  try {
    return new Date(value).toLocaleString('es-CL')
  } catch {
    return value
  }
}
