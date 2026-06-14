import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query'
import { Search, Eye, Trash2, Crown, ChevronLeft, ChevronRight } from 'lucide-react'
import { getUsers, deleteUser } from '../api/usersApi'
import { getKarma } from '../api/karmaApi'
import ConfirmModal from '../components/ConfirmModal'
import UserAvatar from '../components/UserAvatar'
import KarmaBadge from '../components/KarmaBadge'
import StatusBadge from '../components/StatusBadge'
import LoadingSpinner from '../components/LoadingSpinner'
import EmptyState from '../components/EmptyState'

const PAGE_SIZE = 8

export default function UsersPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { data: users, isLoading, isError } = useQuery({ queryKey: ['users'], queryFn: getUsers })

  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [roleFilter, setRoleFilter] = useState('ALL')
  const [page, setPage] = useState(1)
  const [toDelete, setToDelete] = useState(null)

  // Vuelve a la primera página cuando cambian los filtros
  useEffect(() => {
    setPage(1)
  }, [search, statusFilter, roleFilter])

  const deleteMutation = useMutation({
    mutationFn: (userId) => deleteUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      setToDelete(null)
    },
  })

  const list = users ?? []
  const enabledCount = list.filter((u) => u.enabled).length
  const adminCount = list.filter((u) => u.role === 'ADMIN').length

  const term = search.trim().toLowerCase()
  const filtered = list.filter((u) => {
    const matchesSearch =
      !term || `${u.nombre} ${u.apellido} ${u.correo}`.toLowerCase().includes(term)
    const matchesStatus =
      statusFilter === 'ALL' || (statusFilter === 'ENABLED' ? u.enabled : !u.enabled)
    const matchesRole =
      roleFilter === 'ALL' || (roleFilter === 'ADMIN' ? u.role === 'ADMIN' : u.role !== 'ADMIN')
    return matchesSearch && matchesStatus && matchesRole
  })

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const safePage = Math.min(page, pageCount)
  const pageItems = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE)
  const rangeStart = filtered.length === 0 ? 0 : (safePage - 1) * PAGE_SIZE + 1
  const rangeEnd = Math.min(safePage * PAGE_SIZE, filtered.length)

  return (
    <div>
      <div className="hero-head hero-green">
        <div className="hero-head-text">
          <div className="hero-title">Usuarios</div>
          <div className="hero-subtitle">Gestión de usuarios registrados</div>
        </div>
        <div className="hero-counter">
          <span className="hero-counter-num">{list.length}</span>
          <span className="hero-counter-label">usuarios<br />registrados</span>
        </div>
      </div>

      {isLoading && <LoadingSpinner label="Cargando usuarios…" />}
      {isError && <div className="alert alert-error">No se pudieron cargar los usuarios</div>}

      {!isLoading && !isError && (
        <>
          <div className="toolbar">
            <label className="search-box">
              <Search size={18} />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Buscar por nombre o correo…"
                aria-label="Buscar usuarios"
              />
            </label>

            <div className="filter-group">
              <FilterChip active={statusFilter === 'ALL'} onClick={() => setStatusFilter('ALL')}>
                Todos
              </FilterChip>
              <FilterChip
                active={statusFilter === 'ENABLED'}
                onClick={() => setStatusFilter('ENABLED')}
                count={enabledCount}
              >
                Activos
              </FilterChip>
              <FilterChip
                active={statusFilter === 'DISABLED'}
                onClick={() => setStatusFilter('DISABLED')}
                count={list.length - enabledCount}
              >
                Inactivos
              </FilterChip>
            </div>

            <div className="filter-group">
              <FilterChip active={roleFilter === 'ALL'} onClick={() => setRoleFilter('ALL')}>
                Todo rol
              </FilterChip>
              <FilterChip
                active={roleFilter === 'ADMIN'}
                onClick={() => setRoleFilter('ADMIN')}
                count={adminCount}
              >
                Admins
              </FilterChip>
              <FilterChip
                active={roleFilter === 'USER'}
                onClick={() => setRoleFilter('USER')}
                count={list.length - adminCount}
              >
                Usuarios
              </FilterChip>
            </div>
          </div>

          <div className="table-card">
            {filtered.length === 0 ? (
              <EmptyState
                icon={list.length === 0 ? '👤' : '🔍'}
                title={list.length === 0 ? 'Sin usuarios' : 'Sin resultados'}
                message={
                  list.length === 0
                    ? 'Aún no hay usuarios registrados en la plataforma.'
                    : 'Ningún usuario coincide con la búsqueda o los filtros.'
                }
              />
            ) : (
              <>
                <div className="table-scroll">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Usuario</th>
                        <th>Karma</th>
                        <th>Estado</th>
                        <th>Rol</th>
                        <th className="col-actions">Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pageItems.map((u) => (
                        <tr key={u.correo}>
                          <td>
                            <div className="user-cell">
                              <UserAvatar
                                name={`${u.nombre} ${u.apellido}`}
                                email={u.correo}
                                size="md"
                              />
                              <div>
                                <div className="user-cell-name">
                                  {u.nombre} {u.apellido}
                                </div>
                                <div className="user-cell-email">{u.correo}</div>
                              </div>
                            </div>
                          </td>
                          <KarmaCell userId={u.id} />
                          <td>
                            <StatusBadge status={u.enabled ? 'ENABLED' : 'DISABLED'} />
                          </td>
                          <td>
                            <span className={`badge ${u.role === 'ADMIN' ? 'badge-gold' : 'badge-blue'}`}>
                              {u.role === 'ADMIN' ? (
                                <>
                                  <Crown size={13} strokeWidth={2.5} /> Admin
                                </>
                              ) : (
                                'Usuario'
                              )}
                            </span>
                          </td>
                          <td className="col-actions">
                            <div className="row-actions">
                              <button
                                className="btn-icon btn-icon-blue"
                                title="Ver detalle"
                                aria-label="Ver detalle"
                                onClick={() => navigate(`/users/${u.id}`)}
                              >
                                <Eye size={18} />
                              </button>
                              <button
                                className="btn-icon btn-icon-danger"
                                disabled={u.role === 'ADMIN'}
                                title={
                                  u.role === 'ADMIN'
                                    ? 'No se puede eliminar a un administrador'
                                    : 'Eliminar'
                                }
                                aria-label="Eliminar usuario"
                                onClick={() => setToDelete(u)}
                              >
                                <Trash2 size={18} />
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="pagination">
                  <span className="pagination-info">
                    Mostrando {rangeStart}–{rangeEnd} de {filtered.length}
                  </span>
                  <div className="pagination-controls">
                    <button
                      className="page-btn"
                      disabled={safePage <= 1}
                      onClick={() => setPage(safePage - 1)}
                      aria-label="Página anterior"
                    >
                      <ChevronLeft size={16} />
                    </button>
                    {pageWindow(safePage, pageCount).map((p) => (
                      <button
                        key={p}
                        className={`page-btn ${p === safePage ? 'page-btn-active' : ''}`}
                        onClick={() => setPage(p)}
                      >
                        {p}
                      </button>
                    ))}
                    <button
                      className="page-btn"
                      disabled={safePage >= pageCount}
                      onClick={() => setPage(safePage + 1)}
                      aria-label="Página siguiente"
                    >
                      <ChevronRight size={16} />
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </>
      )}

      <ConfirmModal
        open={Boolean(toDelete)}
        title="Eliminar usuario"
        message={
          toDelete
            ? `¿Seguro que deseas eliminar a ${toDelete.correo}? Esta acción no se puede deshacer.`
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

function FilterChip({ active, onClick, count, children }) {
  return (
    <button className={`chip ${active ? 'chip-active' : ''}`} onClick={onClick}>
      {children}
      {typeof count === 'number' && <span className="chip-count">{count}</span>}
    </button>
  )
}

/** Celda de karma; se carga por usuario de forma independiente. */
function KarmaCell({ userId }) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['karma', userId],
    queryFn: () => getKarma(userId),
    retry: false,
  })

  if (isLoading) return <td><span className="muted">…</span></td>
  if (isError || !data) return <td><span className="muted">—</span></td>
  return (
    <td>
      <KarmaBadge score={data.karmaScore} category={data.category} />
    </td>
  )
}

/** Ventana de páginas (hasta 5) centrada en la página actual. */
function pageWindow(page, pageCount) {
  const pages = []
  let start = Math.max(1, page - 2)
  let end = Math.min(pageCount, start + 4)
  start = Math.max(1, end - 4)
  for (let p = start; p <= end; p++) pages.push(p)
  return pages
}
