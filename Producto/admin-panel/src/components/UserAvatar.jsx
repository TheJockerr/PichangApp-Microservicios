/**
 * Avatar circular con iniciales y color derivado del nombre (hash).
 * Mismo usuario => mismo color, siempre.
 * Props: name, email (fallback), size ('sm' | 'md' | 'lg').
 */
const PALETTE = [
  '#2E7D32', '#1565C0', '#FF6F00', '#6A1B9A', '#00897B', '#C62828',
  '#5E35B1', '#0277BD', '#EF6C00', '#AD1457', '#00838F', '#558B2F',
]

function hashStr(str) {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i)
    hash |= 0
  }
  return Math.abs(hash)
}

function getInitials(name) {
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase()
  return (parts[0][0] + parts[1][0]).toUpperCase()
}

function darken(hex, amount) {
  const h = hex.replace('#', '')
  const r = Math.max(0, parseInt(h.substring(0, 2), 16) - amount)
  const g = Math.max(0, parseInt(h.substring(2, 4), 16) - amount)
  const b = Math.max(0, parseInt(h.substring(4, 6), 16) - amount)
  return `rgb(${r}, ${g}, ${b})`
}

export default function UserAvatar({ name = '', email = '', size = 'md' }) {
  const seed = (name || email || '?').trim().toLowerCase()
  const color = PALETTE[hashStr(seed) % PALETTE.length]
  const text = name.trim()
    ? getInitials(name)
    : email
      ? email.substring(0, 2).toUpperCase()
      : '?'

  return (
    <div
      className={`avatar avatar-${size}`}
      style={{ background: `linear-gradient(135deg, ${color}, ${darken(color, 30)})` }}
      title={name || email}
      aria-hidden="true"
    >
      {text}
    </div>
  )
}
