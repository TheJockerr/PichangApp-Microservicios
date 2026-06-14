/**
 * Estado vacío con emoji ilustrativo y mensaje.
 * Props: icon (emoji), title, message, action (nodo opcional).
 */
export default function EmptyState({ icon = '🤷', title = 'Nada por aquí', message = '', action = null }) {
  return (
    <div className="empty-state">
      <div className="empty-emoji">{icon}</div>
      <div className="empty-title">{title}</div>
      {message && <p className="empty-msg">{message}</p>}
      {action && <div style={{ marginTop: 'var(--space-md)' }}>{action}</div>}
    </div>
  )
}
