/**
 * Badge de estado para usuarios y eventos.
 * Usuarios: ENABLED / DISABLED.  Eventos: ACTIVE / FINISHED / CANCELLED.
 */
const STATUS_MAP = {
  ACTIVE: { label: 'Activo', cls: 'badge-green', dot: '#2E7D32' },
  FINISHED: { label: 'Finalizado', cls: 'badge-blue', dot: '#1565C0' },
  CANCELLED: { label: 'Cancelado', cls: 'badge-red', dot: '#C62828' },
  ENABLED: { label: 'Activo', cls: 'badge-green', dot: '#2E7D32' },
  DISABLED: { label: 'Inactivo', cls: 'badge-gray', dot: '#90A4AE' },
}

export default function StatusBadge({ status }) {
  const key = String(status || '').toUpperCase()
  const cfg = STATUS_MAP[key] || { label: status || '—', cls: 'badge-gray', dot: '#90A4AE' }

  return (
    <span className={`badge ${cfg.cls}`}>
      <span
        style={{
          width: 7,
          height: 7,
          borderRadius: '50%',
          background: cfg.dot,
          display: 'inline-block',
        }}
      />
      {cfg.label}
    </span>
  )
}
