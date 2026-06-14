/**
 * Badge de deporte con emoji. Normaliza acentos para reconocer el deporte.
 */
function sportEmoji(sport) {
  const s = (sport || '')
    .toLowerCase()
    .replace(/[áàä]/g, 'a')
    .replace(/[éèë]/g, 'e')
    .replace(/[íìï]/g, 'i')
    .replace(/[óòö]/g, 'o')
    .replace(/[úùü]/g, 'u')

  if (s.includes('futbol') || s.includes('soccer') || s.includes('futsal')) return '⚽'
  if (s.includes('basquet') || s.includes('basket') || s.includes('baloncesto')) return '🏀'
  if (s.includes('voley') || s.includes('voleibol') || s.includes('volley')) return '🏐'
  if (s.includes('padel') || s.includes('tenis de mesa') || s.includes('ping')) return '🏓'
  if (s.includes('tenis') || s.includes('tennis')) return '🎾'
  if (s.includes('beis') || s.includes('baseball')) return '⚾'
  if (s.includes('rugby')) return '🏉'
  if (s.includes('golf')) return '⛳'
  if (s.includes('nata') || s.includes('swim')) return '🏊'
  if (s.includes('ciclis') || s.includes('cycl') || s.includes('bike')) return '🚴'
  if (s.includes('run') || s.includes('atlet') || s.includes('correr')) return '🏃'
  if (s.includes('hockey')) return '🏑'
  if (s.includes('box')) return '🥊'
  return '🏅'
}

export default function SportBadge({ sport }) {
  return (
    <span className="badge badge-green">
      <span style={{ fontSize: '14px', lineHeight: 1 }}>{sportEmoji(sport)}</span>
      {sport || 'Deporte'}
    </span>
  )
}
