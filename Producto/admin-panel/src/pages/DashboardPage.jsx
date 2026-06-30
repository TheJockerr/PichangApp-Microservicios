import { useQuery, useQueries } from '@tanstack/react-query'
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts'
import { Users, Flame, Flag, Sparkles, CalendarDays, UserPlus } from 'lucide-react'
import { getUsers } from '../api/usersApi'
import { getEvents } from '../api/eventsApi'
import { getKarma } from '../api/karmaApi'
import StatCard from '../components/StatCard'
import UserAvatar from '../components/UserAvatar'
import KarmaBadge from '../components/KarmaBadge'
import LoadingSpinner from '../components/LoadingSpinner'
import EmptyState from '../components/EmptyState'

export default function DashboardPage() {
  const usersQuery = useQuery({ queryKey: ['users'], queryFn: getUsers })
  const eventsQuery = useQuery({ queryKey: ['events'], queryFn: getEvents })

  const users = usersQuery.data ?? []
  const events = eventsQuery.data ?? []

  // Karma por usuario (reutiliza el mismo endpoint que ya usa la tabla de usuarios)
  const karmaResults = useQueries({
    queries: users.map((u) => ({
      queryKey: ['karma', u.id],
      queryFn: () => getKarma(u.id),
      retry: false,
      staleTime: 60_000,
    })),
  })

  const totalUsers = users.length
  const activos = events.filter((e) => e.status === 'ACTIVO').length
  const finalizados = events.filter((e) => e.status === 'FINALIZADO').length

  // Promedio de karma del sistema
  const karmaScores = karmaResults
    .filter((r) => r.isSuccess && r.data && typeof r.data.karmaScore === 'number')
    .map((r) => r.data.karmaScore)
  const karmaAvg = karmaScores.length
    ? Math.round(karmaScores.reduce((a, b) => a + b, 0) / karmaScores.length)
    : null
  const karmaLoading = totalUsers > 0 && karmaResults.some((r) => r.isLoading)
  const karmaDisplay = karmaLoading ? '…' : karmaAvg ?? '—'

  // Karma por id de usuario, para mostrarlo en la lista de recientes
  const karmaByUser = {}
  users.forEach((u, i) => {
    const r = karmaResults[i]
    if (r?.isSuccess && r.data) karmaByUser[u.id] = r.data
  })

  const series = buildEventSeries(events)
  const recentUsers = [...users].slice(-5).reverse()

  const loading = usersQuery.isLoading || eventsQuery.isLoading
  const error = usersQuery.isError || eventsQuery.isError

  return (
    <div>
      <div className="page-head">
        <h1 className="page-title">Centro de comando 🎯</h1>
        <p className="page-subtitle">Resumen general de la plataforma PichangApp</p>
      </div>

      {loading && <LoadingSpinner label="Cargando estadísticas…" />}
      {error && <div className="alert alert-error">No se pudieron cargar las estadísticas</div>}

      {!loading && !error && (
        <>
          <div className="stat-grid">
            <StatCard
              icon={Users}
              value={totalUsers}
              label="Usuarios registrados"
              from="#42A5F5"
              to="#1565C0"
              delay={0}
            />
            <StatCard
              icon={Flame}
              value={activos}
              label="Eventos activos"
              from="#66BB6A"
              to="#2E7D32"
              delay={0.08}
            />
            <StatCard
              icon={Flag}
              value={finalizados}
              label="Eventos finalizados"
              from="#FFB300"
              to="#FF6F00"
              delay={0.16}
            />
            <StatCard
              icon={Sparkles}
              value={karmaDisplay}
              label="Karma promedio del sistema"
              from="#AB47BC"
              to="#6A1B9A"
              delay={0.24}
            />
          </div>

          <div className="dash-grid">
            <section className="card">
              <h2 className="card-title">
                <span className="card-title-icon">
                  <CalendarDays size={18} />
                </span>
                Eventos por día · últimos 7 días
              </h2>
              <div className="chart-wrap">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={series} margin={{ top: 10, right: 8, left: -18, bottom: 0 }}>
                    <defs>
                      <linearGradient id="barGreen" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#66BB6A" />
                        <stop offset="100%" stopColor="#2E7D32" />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="4 4" vertical={false} stroke="#e6ede6" />
                    <XAxis
                      dataKey="label"
                      tickLine={false}
                      axisLine={false}
                      tick={{ fontSize: 12, fill: '#78909c' }}
                    />
                    <YAxis
                      allowDecimals={false}
                      tickLine={false}
                      axisLine={false}
                      width={34}
                      tick={{ fontSize: 12, fill: '#78909c' }}
                    />
                    <Tooltip
                      cursor={{ fill: 'rgba(46,125,50,0.06)' }}
                      contentStyle={{
                        borderRadius: 12,
                        border: '1px solid #e6ede6',
                        boxShadow: '0 6px 24px rgba(26,26,46,0.1)',
                        fontSize: 13,
                      }}
                      labelStyle={{ fontWeight: 700, color: '#2c3e50' }}
                    />
                    <Bar dataKey="eventos" fill="url(#barGreen)" radius={[8, 8, 0, 0]} maxBarSize={44} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </section>

            <section className="card">
              <h2 className="card-title">
                <span className="card-title-icon">
                  <UserPlus size={18} />
                </span>
                Usuarios recientes
              </h2>
              {recentUsers.length === 0 ? (
                <EmptyState icon="👤" title="Sin usuarios" message="Aún no hay usuarios registrados." />
              ) : (
                <div className="recent-list">
                  {recentUsers.map((u) => {
                    const karma = karmaByUser[u.id]
                    return (
                      <div key={u.correo} className="recent-item">
                        <UserAvatar name={`${u.nombre} ${u.apellido}`} email={u.correo} size="md" />
                        <div className="recent-info" style={{ flex: 1 }}>
                          <div className="recent-name">
                            {u.nombre} {u.apellido}
                          </div>
                          <div className="recent-email">{u.correo}</div>
                        </div>
                        {karma && <KarmaBadge score={karma.karmaScore} category={karma.category} />}
                      </div>
                    )
                  })}
                </div>
              )}
            </section>
          </div>
        </>
      )}
    </div>
  )
}

/** Construye 7 cubos (hoy y los 6 días anteriores) contando eventos por fecha. */
function buildEventSeries(events) {
  const days = []
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  for (let i = 6; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    days.push({
      time: d.getTime(),
      label: d.toLocaleDateString('es-CL', { weekday: 'short' }),
      eventos: 0,
    })
  }

  events.forEach((e) => {
    if (!e.eventDate) return
    const ed = new Date(e.eventDate)
    if (Number.isNaN(ed.getTime())) return
    ed.setHours(0, 0, 0, 0)
    const bucket = days.find((day) => day.time === ed.getTime())
    if (bucket) bucket.eventos += 1
  })

  return days.map(({ label, eventos }) => ({ label, eventos }))
}
