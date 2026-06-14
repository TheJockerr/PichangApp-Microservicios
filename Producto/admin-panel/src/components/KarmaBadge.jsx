import { Star } from 'lucide-react'

/**
 * Badge de karma con color según categoría.
 * Verde = Excelente, Azul = Bueno, Naranja = Regular, Rojo = Bajo.
 * Si no hay categoría reconocible, colorea por puntaje.
 */
function karmaClass(category, score) {
  const c = (category || '').toLowerCase()
  if (c.includes('excel')) return 'badge-green'
  if (c.includes('buen') || c.includes('good') || c.includes('alto')) return 'badge-blue'
  if (c.includes('regular') || c.includes('medio') || c.includes('normal')) return 'badge-orange'
  if (c.includes('bajo') || c.includes('low') || c.includes('malo') || c.includes('pobre') || c.includes('deficiente'))
    return 'badge-red'

  // Fallback por puntaje
  if (score >= 80) return 'badge-green'
  if (score >= 60) return 'badge-blue'
  if (score >= 40) return 'badge-orange'
  return 'badge-red'
}

export default function KarmaBadge({ score, category }) {
  const cls = karmaClass(category, Number(score))
  return (
    <span className={`badge ${cls}`}>
      <Star size={13} strokeWidth={2.5} fill="currentColor" />
      {score}
      {category ? ` · ${category}` : ''}
    </span>
  )
}
