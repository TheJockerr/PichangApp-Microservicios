/**
 * Tarjeta de estadística (KPI) con gradiente, número enorme e ícono circular.
 * Props:
 *  - icon: componente de ícono (lucide-react)
 *  - value: valor a destacar
 *  - label: descripción
 *  - from / to: colores del gradiente (hex)
 *  - delay: retardo de la animación de entrada (segundos)
 */
function hexToRgba(hex, alpha) {
  const h = hex.replace('#', '')
  const r = parseInt(h.substring(0, 2), 16)
  const g = parseInt(h.substring(2, 4), 16)
  const b = parseInt(h.substring(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

export default function StatCard({ icon: Icon, value, label, from, to, delay = 0 }) {
  return (
    <div className="animate-fade-up" style={{ animationDelay: `${delay}s` }}>
      <div
        className="stat-card"
        style={{
          background: `linear-gradient(135deg, ${from} 0%, ${to} 100%)`,
          boxShadow: `0 10px 26px ${hexToRgba(to, 0.4)}`,
        }}
      >
        <div className="stat-card-icon">
          {Icon ? <Icon size={26} strokeWidth={2.5} /> : null}
        </div>
        <div>
          <div className="stat-card-value">{value}</div>
          <div className="stat-card-label">{label}</div>
        </div>
      </div>
    </div>
  )
}
