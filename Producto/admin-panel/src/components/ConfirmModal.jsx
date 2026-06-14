import { AlertTriangle } from 'lucide-react'

/**
 * Modal de confirmación reutilizable, con ícono y animación.
 * Props: open, title, message, confirmText, cancelText, onConfirm, onCancel, loading.
 */
export default function ConfirmModal({
  open,
  title = 'Confirmar acción',
  message,
  confirmText = 'Confirmar',
  cancelText = 'Cancelar',
  onConfirm,
  onCancel,
  loading = false,
}) {
  if (!open) return null

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-icon">
          <AlertTriangle size={32} strokeWidth={2.5} />
        </div>
        <h3 className="modal-title">{title}</h3>
        <p className="modal-message">{message}</p>
        <div className="modal-actions">
          <button className="btn btn-secondary" onClick={onCancel} disabled={loading}>
            {cancelText}
          </button>
          <button className="btn btn-danger" onClick={onConfirm} disabled={loading}>
            {loading ? 'Procesando…' : confirmText}
          </button>
        </div>
      </div>
    </div>
  )
}
