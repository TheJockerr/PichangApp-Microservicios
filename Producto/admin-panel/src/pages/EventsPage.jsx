import { useState } from 'react'
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query'
import { CalendarDays, Trash2 } from 'lucide-react'
import { getEvents, deleteEvent } from '../api/eventsApi'
import ConfirmModal from '../components/ConfirmModal'
import SportBadge from '../components/SportBadge'
import StatusBadge from '../components/StatusBadge'
import UserAvatar from '../components/UserAvatar'
import LoadingSpinner from '../components/LoadingSpinner'
import EmptyState from '../components/EmptyState'

const STATUS_LABELS = {
  ACTIVO: 'Activos',
  FINALIZADO: 'Finalizados',
  CANCELADO: 'Cancelados',
}

export default function EventsPage() {
  const queryClient = useQueryClient()
  const { data: events, isLoading, isError } = useQuery({ queryKey: ['events'], queryFn: getEvents })

  const [filter, setFilter] = useState('ALL')
  const [toDelete, setToDelete] = useState(null)

  const deleteMutation = useMutation({
    mutationFn: (eventId) => deleteEvent(eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] })
      setToDelete(null)
    },
  })

  const list = events ?? []
  const counts = {
    ALL: list.length,
    ACTIVO: list.filter((e) => e.status === 'ACTIVO').length,
    FINALIZADO: list.filter((e) => e.status === 'FINALIZADO').length,
    CANCELADO: list.filter((e) => e.status === 'CANCELADO').length,
  }
  const filtered = list.filter((e) => filter === 'ALL' || e.status === filter)

  return (
    <div>
      <div className="hero-head hero-orange">
        <div className="hero-head-text">
          <div className="hero-title">Eventos</div>
          <div className="hero-subtitle">Gestión de todos los eventos deportivos</div>
        </div>
        <div className="hero-counter">
          <span className="hero-counter-num">{counts.ACTIVO}</span>
          <span className="hero-counter-label">eventos<br />activos</span>
        </div>
      </div>

      <div className="filter-group" style={{ marginBottom: 'var(--space-lg)' }}>
        {['ALL', 'ACTIVO', 'FINALIZADO', 'CANCELADO'].map((s) => (
          <button
            key={s}
            className={`chip ${filter === s ? 'chip-active' : ''}`}
            onClick={() => setFilter(s)}
          >
            {s === 'ALL' ? 'Todos' : STATUS_LABELS[s]}
            <span className="chip-count">{counts[s]}</span>
          </button>
        ))}
      </div>

      {isLoading && <LoadingSpinner label="Cargando eventos…" />}
      {isError && <div className="alert alert-error">No se pudieron cargar los eventos</div>}

      {!isLoading && !isError && (
        <>
          {filtered.length === 0 ? (
            <div className="table-card">
              <EmptyState
                icon="📅"
                title="Sin eventos"
                message="No hay eventos para este filtro."
              />
            </div>
          ) : (
            <div className="events-grid">
              {filtered.map((e, i) => (
                <EventCard
                  key={e.id}
                  event={e}
                  delay={Math.min(i * 0.05, 0.4)}
                  onDelete={() => setToDelete(e)}
                />
              ))}
            </div>
          )}
        </>
      )}

      <ConfirmModal
        open={Boolean(toDelete)}
        title="Eliminar evento"
        message={
          toDelete
            ? `¿Seguro que deseas eliminar "${toDelete.name}"? Los participantes recibirán sus puntos de karma y una notificación.`
            : ''
        }
        confirmText="Eliminar"
        loading={deleteMutation.isPending}
        onCancel={() => setToDelete(null)}
        onConfirm={() => deleteMutation.mutate(toDelete.id)}
      />
    </div>
  )
}

function EventCard({ event, delay, onDelete }) {
  const max = Number(event.maxPlayers) || 0
  const cur = Number(event.currentPlayers) || 0
  const pct = max > 0 ? Math.min(100, Math.round((cur / max) * 100)) : 0
  const full = max > 0 && cur >= max

  return (
    <article className="event-card animate-fade-in" style={{ animationDelay: `${delay}s` }}>
      <button
        className="btn-icon btn-icon-danger event-delete"
        disabled={event.status === 'CANCELLED'}
        title={event.status === 'CANCELLED' ? 'El evento ya está cancelado' : 'Eliminar'}
        aria-label="Eliminar evento"
        onClick={onDelete}
      >
        <Trash2 size={16} />
      </button>

      <div className="event-card-top">
        <SportBadge sport={event.sport} />
      </div>

      <div className="event-card-name">{event.name}</div>

      <div className="event-meta-row" style={{ gap: 12 }}>
        <StatusBadge status={event.status} />
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
          <CalendarDays size={15} /> {formatDate(event.eventDate)}
        </span>
      </div>

      <div className="event-organizer">
        <UserAvatar email={event.organizerEmail} name={event.organizerEmail || ''} size="sm" />
        <div style={{ minWidth: 0 }}>
          <div className="event-organizer-label">Organizador</div>
          <div className="event-organizer-email">{event.organizerEmail || '—'}</div>
        </div>
      </div>

      <div>
        <div className="progress-head">
          <span>Jugadores</span>
          <span className="progress-count">
            {cur} / {max}
          </span>
        </div>
        <div className="progress-bar">
          <div
            className={`progress-fill ${full ? 'progress-fill-full' : ''}`}
            style={{ width: `${pct}%` }}
          />
        </div>
      </div>
    </article>
  )
}

function formatDate(value) {
  if (!value) return '—'
  try {
    return new Date(value).toLocaleString('es-CL', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return value
  }
}
