/**
 * Spinner con colores de la marca.
 * Props: label (texto opcional bajo el spinner).
 */
export default function LoadingSpinner({ label = 'Cargando…' }) {
  return (
    <div className="loading-wrap">
      <div className="spinner">
        <div className="spinner-ring" />
      </div>
      {label && <div className="spinner-label">{label}</div>}
    </div>
  )
}
