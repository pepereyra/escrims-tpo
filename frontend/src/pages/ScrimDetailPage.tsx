import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { feedbackApi, reportesApi, scrimsApi } from '../api'
import type { Estadistica, Feedback, Reporte, Scrim } from '../api/types'
import { StatusBadge } from '../components/StatusBadge'
import { useAuth } from '../context/AuthContext'
import { getGameLogoPath } from '../constants/gameLogos'
import { isJuego } from '../constants/options'
import { formatRangoRange } from '../constants/ranks'
import { getRolLabel, getRolesForJuego } from '../constants/roles'
import { getInitials } from '../utils/avatar'
import { formatFecha } from '../utils/format'

const LOBBY_STATES = ['BUSCANDO', 'LOBBY_ARMADO', 'CONFIRMADO']
const RATING_OPTIONS = [1, 2, 3, 4, 5] as const

const GAME_COLORS: Record<string, string> = {
  Valorant: '#FF4655',
  LoL: '#4d8dff',
  CS2: '#e8b923',
}

type StatInput = { username: string; kills: number; deaths: number; assists: number }

function getGameColor(juego: string) {
  if (isJuego(juego)) return GAME_COLORS[juego]
  return 'var(--primary)'
}

function RatingStars({ rating }: { rating: number }) {
  return (
    <span className="detail-rating" aria-label={`${rating} de 5`}>
      {RATING_OPTIONS.map((value) => (
        <span key={value} className={`detail-rating-star${value <= rating ? ' active' : ''}`}>
          ★
        </span>
      ))}
    </span>
  )
}

export function ScrimDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { user, isMod } = useAuth()
  const [scrim, setScrim] = useState<Scrim | null>(null)
  const [feedback, setFeedback] = useState<Feedback[]>([])
  const [reportes, setReportes] = useState<Reporte[]>([])
  const [estadisticas, setEstadisticas] = useState<Estadistica[]>([])
  const [error, setError] = useState('')
  const [actionMsg, setActionMsg] = useState('')
  const [loading, setLoading] = useState(true)
  const [rol, setRol] = useState('DUELIST')

  const [fbForm, setFbForm] = useState({ destinatario: '', rating: 5, comentario: '' })
  const [repForm, setRepForm] = useState({ reportado: '', motivo: '' })
  const [sanciones, setSanciones] = useState<Record<string, string>>({})

  const [roleEditUser, setRoleEditUser] = useState<string | null>(null)
  const [roleEditValue, setRoleEditValue] = useState('')
  const [swapSlots, setSwapSlots] = useState<[string | null, string | null]>([null, null])
  const [lobbyBusyUser, setLobbyBusyUser] = useState<string | null>(null)

  const aceptados = useMemo(
    () => scrim?.postulaciones.filter((p) => p.estado === 'ACEPTADA') ?? [],
    [scrim],
  )

  const suplentes = useMemo(
    () => scrim?.postulaciones.filter((p) => p.estado === 'SUPLENTE') ?? [],
    [scrim],
  )

  const [statsForm, setStatsForm] = useState<StatInput[]>([])

  const load = useCallback(async () => {
    if (!id) return
    setLoading(true)
    setError('')
    try {
      const [s, fb, rep] = await Promise.all([
        scrimsApi.get(id),
        feedbackApi.list(id),
        reportesApi.list(id),
      ])
      setScrim(s)
      setFeedback(fb)
      setReportes(rep)
      setStatsForm(
        s.postulaciones
          .filter((p) => p.estado === 'ACEPTADA')
          .map((p) => ({ username: p.username, kills: 0, deaths: 0, assists: 0 })),
      )
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar')
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    if (!scrim) return
    const defaultRol = getRolesForJuego(scrim.juego)[0]?.value ?? 'DUELIST'
    setRol(defaultRol)
    setRoleEditUser(null)
    setSwapSlots([null, null])
  }, [scrim?.id, scrim?.juego])

  const runAction = async (fn: () => Promise<Scrim>, msg: string) => {
    setActionMsg('')
    setError('')
    try {
      const updated = await fn()
      setScrim(updated)
      setActionMsg(msg)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error en la acción')
    }
  }

  const handlePostular = () => {
    if (!user || !id) return
    runAction(() => scrimsApi.postular(id, user.username, rol), 'Postulación enviada')
  }

  const handleConfirmar = () => {
    if (!user || !id) return
    runAction(() => scrimsApi.confirmar(id, user.username), 'Confirmación registrada')
  }

  const handleIniciar = () => id && runAction(() => scrimsApi.iniciar(id), 'Scrim iniciado')
  const handleFinalizar = () => id && runAction(() => scrimsApi.finalizar(id), 'Scrim finalizado')
  const handleCancelar = () => id && runAction(() => scrimsApi.cancelar(id), 'Scrim cancelado')

  const handleIcal = async () => {
    if (!id || !scrim) return
    setError('')
    try {
      const { contenido } = await scrimsApi.getIcal(id)
      const blob = new Blob([contenido], { type: 'text/calendar' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `scrim-${scrim.juego}-${id.slice(0, 8)}.ics`
      a.click()
      URL.revokeObjectURL(url)
      setActionMsg('Calendario descargado')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al descargar iCal')
    }
  }

  const handleCambiarRol = async (username: string, nuevoRol: string) => {
    if (!id) return
    await runAction(() => scrimsApi.cambiarRol(id, username, nuevoRol), 'Rol actualizado')
    setRoleEditUser(null)
  }

  const handleIntercambiar = async () => {
    const [a, b] = swapSlots
    if (!id || !a || !b) return
    await runAction(() => scrimsApi.intercambiarRoles(id, a, b), 'Roles intercambiados')
    setSwapSlots([null, null])
  }

  const handleSuplente = async (username: string) => {
    if (!id || lobbyBusyUser) return
    if (!confirm(`¿Mover a ${username} a suplente?`)) return
    setLobbyBusyUser(username)
    try {
      await runAction(() => scrimsApi.moverASuplente(id, username), 'Jugador movido a suplente')
      setSwapSlots(([a, b]) => [a === username ? null : a, b === username ? null : b])
      if (roleEditUser === username) setRoleEditUser(null)
    } finally {
      setLobbyBusyUser(null)
    }
  }

  const handleReactivarTitular = async (username: string) => {
    if (!id || lobbyBusyUser) return
    setLobbyBusyUser(username)
    try {
      await runAction(
        () => scrimsApi.reactivarTitular(id, username),
        'Jugador reactivado en el equipo',
      )
    } finally {
      setLobbyBusyUser(null)
    }
  }

  const assignSwapSlot = (username: string) => {
    setSwapSlots(([a, b]) => {
      if (a === username) return [null, b]
      if (b === username) return [a, null]
      if (!a) return [username, b]
      if (!b) return [a, username]
      return [a, username]
    })
  }

  const clearSwapSlots = () => setSwapSlots([null, null])

  const getPlayerByUsername = (username: string) =>
    aceptados.find((p) => p.username === username)

  const handleStats = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!id) return
    setError('')
    setActionMsg('')
    try {
      const result = await scrimsApi.registrarEstadisticas(id, statsForm)
      setEstadisticas(result)
      setActionMsg('Estadísticas registradas')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al registrar estadísticas')
    }
  }

  const updateStat = (username: string, field: keyof Omit<StatInput, 'username'>, value: number) => {
    setStatsForm((prev) =>
      prev.map((s) => (s.username === username ? { ...s, [field]: value } : s)),
    )
  }

  const handleFeedback = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user || !id) return
    setError('')
    try {
      await feedbackApi.create(id, {
        autor: user.username,
        destinatario: fbForm.destinatario,
        rating: fbForm.rating,
        comentario: fbForm.comentario,
      })
      setFbForm({ destinatario: '', rating: 5, comentario: '' })
      setFeedback(await feedbackApi.list(id))
      setActionMsg('Feedback enviado')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al enviar feedback')
    }
  }

  const handleReporte = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user || !id) return
    setError('')
    try {
      await reportesApi.create(id, {
        reportante: user.username,
        reportado: repForm.reportado,
        motivo: repForm.motivo,
      })
      setRepForm({ reportado: '', motivo: '' })
      setReportes(await reportesApi.list(id))
      setActionMsg('Reporte enviado')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al enviar reporte')
    }
  }

  const moderarFeedback = async (fbId: string, aprobar: boolean) => {
    try {
      if (aprobar) await feedbackApi.aprobar(fbId)
      else await feedbackApi.rechazar(fbId)
      if (id) setFeedback(await feedbackApi.list(id))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de moderación')
    }
  }

  const moderarReporte = async (repId: string, aprobar: boolean) => {
    try {
      if (aprobar) await reportesApi.aprobar(repId, sanciones[repId])
      else await reportesApi.rechazar(repId)
      if (id) setReportes(await reportesApi.list(id))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de moderación')
    }
  }

  if (loading) return <p className="page-loading">Cargando scrim...</p>
  if (!scrim) return <p className="form-error">Scrim no encontrado</p>

  const miPostulacion = scrim.postulaciones.find((p) => p.username === user?.username)
  const jugadores = scrim.postulaciones.map((p) => p.username)
  const puedeGestionarLobby =
    user && LOBBY_STATES.includes(scrim.estado) && (aceptados.length > 0 || suplentes.length > 0)
  const puedeReactivarSuplente = scrim.cuposDisponibles > 0
  const gameColor = getGameColor(scrim.juego)
  const logoPath = getGameLogoPath(scrim.juego)
  const ocupados = scrim.cuposTotales - scrim.cuposDisponibles
  const fillPct = scrim.cuposTotales > 0 ? (ocupados / scrim.cuposTotales) * 100 : 0
  const roles = getRolesForJuego(scrim.juego)
  const rangoLabel = formatRangoRange(scrim.juego, scrim.rangoMin, scrim.rangoMax)
  const otrosJugadores = jugadores.filter((j) => j !== user?.username)

  return (
    <div
      className="page scrim-detail-page"
      style={{ '--scrim-game-color': gameColor } as React.CSSProperties}
    >
      <Link to="/" className="scrim-detail-back">
        ← Volver a scrims
      </Link>

      <section className="scrim-detail-hero">
        <div className="scrim-detail-hero-accent" />
        <div className="scrim-detail-hero-body">
          <div className="scrim-detail-hero-top">
            <div className="scrim-detail-hero-game">
              <span className="scrim-game-icon" aria-hidden="true">
                {logoPath ? (
                  <svg viewBox="0 0 24 24" focusable="false">
                    <path fill={gameColor} d={logoPath} />
                  </svg>
                ) : (
                  <span className="scrim-game-icon-fallback">{scrim.juego.slice(0, 3).toUpperCase()}</span>
                )}
              </span>
              <div>
                <p className="scrim-detail-eyebrow">Detalle del scrim</p>
                <h1>
                  {scrim.juego} · {scrim.formato}
                </h1>
                <p className="scrim-detail-subtitle">{formatFecha(scrim.fechaHora)}</p>
              </div>
            </div>
            <div className="scrim-detail-hero-actions">
              <StatusBadge estado={scrim.estado} />
              <button type="button" className="btn btn-ghost btn-sm" onClick={handleIcal}>
                Descargar iCal
              </button>
            </div>
          </div>

          <div className="scrim-detail-pills">
            <span className="scrim-pill">{scrim.region}</span>
            <span className="scrim-pill">{scrim.modalidad}</span>
            <span className="scrim-pill scrim-pill-rank">{rangoLabel}</span>
            <span className="scrim-pill">{scrim.postulaciones.length} postulaciones</span>
          </div>

          <div className="scrim-detail-cupos">
            <div className="scrim-cupos-header">
              <span className="scrim-cupos-label">Cupos</span>
              <span className={`scrim-cupos-value${scrim.cuposDisponibles === 0 ? ' full' : ''}`}>
                {ocupados}/{scrim.cuposTotales}
                {scrim.cuposDisponibles === 0
                  ? ' · Lleno'
                  : ` · ${scrim.cuposDisponibles} libres`}
              </span>
            </div>
            <div className="scrim-cupos-bar" aria-hidden="true">
              <span className="scrim-cupos-fill" style={{ width: `${fillPct}%` }} />
            </div>
          </div>
        </div>
      </section>

      {error && <div className="detail-alert detail-alert-error">{error}</div>}
      {actionMsg && <div className="detail-alert detail-alert-success">{actionMsg}</div>}

      {!user && (
        <div className="detail-alert detail-alert-info">
          <Link to="/login">Ingresá</Link> para postularte o participar en este scrim.
        </div>
      )}

      {user && (
        <section className="detail-panel">
          <header className="detail-panel-header">
            <div>
              <h2>Acciones</h2>
              <p className="detail-panel-hint">Postulate, confirmá asistencia o gestioná el estado</p>
            </div>
          </header>

          <div className="detail-panel-body">
            {!miPostulacion && scrim.estado === 'BUSCANDO' && (
              <div className="filters-group">
                <span className="filters-group-label">Postularme como</span>
                <div className="filters-chip-row" role="group" aria-label="Rol">
                  {roles.map((r) => (
                    <button
                      key={r.value}
                      type="button"
                      className={`filters-chip${rol === r.value ? ' active' : ''}`}
                      onClick={() => setRol(r.value)}
                      aria-pressed={rol === r.value}
                    >
                      {r.label}
                    </button>
                  ))}
                </div>
                <button type="button" className="btn btn-primary" onClick={handlePostular}>
                  Enviar postulación
                </button>
              </div>
            )}

            {miPostulacion && (
              <div className="detail-status-card">
                <span className="detail-status-card-label">Tu postulación</span>
                <div className="detail-status-card-row">
                  <span className="detail-player-chip">
                    <span className="detail-player-avatar">{getInitials(miPostulacion.username)}</span>
                    {miPostulacion.username}
                  </span>
                  <span className="detail-role-tag">{getRolLabel(scrim.juego, miPostulacion.rol)}</span>
                  <StatusBadge estado={miPostulacion.estado} />
                </div>
                {miPostulacion.estado === 'ACEPTADA' && (
                  <button type="button" className="btn btn-primary btn-sm" onClick={handleConfirmar}>
                    Confirmar asistencia
                  </button>
                )}
              </div>
            )}

            <div className="detail-action-bar">
              <button type="button" className="btn btn-ghost btn-sm" onClick={handleIniciar}>
                Iniciar
              </button>
              <button type="button" className="btn btn-ghost btn-sm" onClick={handleFinalizar}>
                Finalizar
              </button>
              <button type="button" className="btn btn-ghost btn-sm btn-danger" onClick={handleCancelar}>
                Cancelar
              </button>
            </div>
          </div>
        </section>
      )}

      {puedeGestionarLobby && (
        <section className="detail-panel lobby-panel">
          <header className="detail-panel-header">
            <div>
              <h2>Gestión de lobby</h2>
              <p className="detail-panel-hint">
                Revisá la composición del equipo y ajustá roles o posiciones
              </p>
            </div>
            <span className="detail-count-badge">
              {aceptados.length} titular{aceptados.length === 1 ? '' : 'es'}
              {suplentes.length > 0 ? ` · ${suplentes.length} suplente${suplentes.length === 1 ? '' : 's'}` : ''}
            </span>
          </header>

          <div className="lobby-roster">
            <div className="lobby-roster-head">
              <span className="filters-group-label">Composición actual</span>
              <span className="lobby-roster-tip">Cada fila muestra el rol asignado hoy</span>
            </div>

            <div className="lobby-roster-list">
              {aceptados.map((p) => {
                const isEditing = roleEditUser === p.username
                const inSwap = swapSlots.includes(p.username)
                return (
                  <article
                    key={p.username}
                    className={`lobby-roster-row${isEditing ? ' editing' : ''}${inSwap ? ' in-swap' : ''}`}
                  >
                    <div className="lobby-roster-main">
                      <span className="detail-player-avatar">{getInitials(p.username)}</span>
                      <div className="lobby-roster-info">
                        <strong>{p.username}</strong>
                        <span className="lobby-role-pill">
                          {getRolLabel(scrim.juego, p.rol)}
                        </span>
                      </div>
                      <div className="lobby-row-actions">
                        <button
                          type="button"
                          className={`btn btn-ghost btn-sm lobby-row-btn${isEditing ? ' active' : ''}`}
                          onClick={() => {
                            if (isEditing) {
                              setRoleEditUser(null)
                            } else {
                              setRoleEditUser(p.username)
                              setRoleEditValue(p.rol)
                            }
                          }}
                        >
                          {isEditing ? 'Cerrar' : 'Cambiar rol'}
                        </button>
                        <button
                          type="button"
                          className={`btn btn-ghost btn-sm lobby-row-btn${inSwap ? ' active' : ''}`}
                          onClick={() => assignSwapSlot(p.username)}
                          title="Agregar al intercambio"
                        >
                          Intercambiar
                        </button>
                        <button
                          type="button"
                          className="btn btn-ghost btn-sm btn-danger lobby-row-btn"
                          onClick={() => handleSuplente(p.username)}
                        >
                          Suplente
                        </button>
                      </div>
                    </div>

                    {isEditing && (
                      <div className="lobby-role-editor">
                        <span className="filters-sub-label">Nuevo rol para {p.username}</span>
                        <div className="filters-chip-row" role="group" aria-label="Nuevo rol">
                          {roles.map((r) => (
                            <button
                              key={r.value}
                              type="button"
                              className={`filters-chip${roleEditValue === r.value ? ' active' : ''}`}
                              onClick={() => setRoleEditValue(r.value)}
                              aria-pressed={roleEditValue === r.value}
                            >
                              {r.label}
                            </button>
                          ))}
                        </div>
                        <div className="lobby-role-editor-actions">
                          <button
                            type="button"
                            className="btn btn-primary btn-sm"
                            onClick={() => handleCambiarRol(p.username, roleEditValue)}
                            disabled={roleEditValue === p.rol}
                          >
                            Guardar rol
                          </button>
                          {roleEditValue !== p.rol && (
                            <span className="lobby-role-preview">
                              {getRolLabel(scrim.juego, p.rol)} → {getRolLabel(scrim.juego, roleEditValue)}
                            </span>
                          )}
                        </div>
                      </div>
                    )}
                  </article>
                )
              })}
            </div>
          </div>

          {suplentes.length > 0 && (
            <div className="lobby-bench">
              <div className="lobby-roster-head">
                <span className="filters-group-label">Banco de suplentes</span>
                <span className="lobby-roster-tip">
                  {puedeReactivarSuplente
                    ? `${scrim.cuposDisponibles} cupo${scrim.cuposDisponibles === 1 ? '' : 's'} libre${scrim.cuposDisponibles === 1 ? '' : 's'} para reactivar`
                    : 'No hay cupos libres para reactivar jugadores'}
                </span>
              </div>
              <div className="lobby-bench-list">
                {suplentes.map((p) => (
                  <article key={p.username} className="lobby-bench-row">
                    <div className="lobby-roster-main">
                      <span className="detail-player-avatar">{getInitials(p.username)}</span>
                      <div className="lobby-roster-info">
                        <strong>{p.username}</strong>
                        <span className="lobby-role-pill lobby-role-pill-muted">
                          {getRolLabel(scrim.juego, p.rol)}
                        </span>
                      </div>
                      <StatusBadge estado={p.estado} />
                    </div>
                    <div className="lobby-bench-actions">
                      <button
                        type="button"
                        className="btn btn-primary btn-sm"
                        onClick={() => void handleReactivarTitular(p.username)}
                        disabled={!puedeReactivarSuplente || lobbyBusyUser !== null}
                      >
                        {lobbyBusyUser === p.username ? 'Reactivando...' : 'Volver al equipo'}
                      </button>
                      {!puedeReactivarSuplente && (
                        <span className="lobby-bench-hint">
                          Liberá un cupo moviendo a otro jugador a suplente
                        </span>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            </div>
          )}

          {aceptados.length >= 2 && (
          <div
            className={`lobby-swap-board${swapSlots[0] && swapSlots[1] ? ' ready' : ''}`}
          >
            <header className="lobby-swap-head">
              <div className="lobby-swap-title-row">
                <span className="lobby-swap-icon" aria-hidden="true">
                  ⇄
                </span>
                <span className="filters-group-label">Intercambiar posiciones</span>
              </div>
              <p className="lobby-swap-hint">
                Usá el botón &quot;Intercambiar&quot; en cada fila o seleccioná los slots para
                armar el cambio.
              </p>
            </header>

            {(() => {
              const renderSwapSlot = (index: 0 | 1) => {
                const username = swapSlots[index]
                const player = username ? getPlayerByUsername(username) : null
                return (
                  <button
                    key={index}
                    type="button"
                    className={`lobby-swap-slot${player ? ' filled' : ''}`}
                    onClick={() => {
                      if (!player) return
                      assignSwapSlot(username!)
                    }}
                    disabled={!player}
                    aria-label={
                      player
                        ? `${player.username}, ${getRolLabel(scrim.juego, player.rol)}. Clic para quitar.`
                        : `Slot jugador ${index + 1}, vacío`
                    }
                  >
                    <span className="lobby-swap-slot-label">Jugador {index + 1}</span>
                    {player ? (
                      <div className="lobby-swap-slot-body">
                        <span className="detail-player-avatar lobby-swap-avatar">
                          {getInitials(player.username)}
                        </span>
                        <div className="lobby-swap-slot-info">
                          <strong>{player.username}</strong>
                          <span className="lobby-role-pill">
                            {getRolLabel(scrim.juego, player.rol)}
                          </span>
                        </div>
                        <span className="lobby-swap-slot-remove" aria-hidden="true">
                          ×
                        </span>
                      </div>
                    ) : (
                      <div className="lobby-swap-slot-empty">
                        <span className="lobby-swap-slot-plus" aria-hidden="true">
                          +
                        </span>
                        <span className="lobby-swap-placeholder">Seleccionar jugador</span>
                      </div>
                    )}
                  </button>
                )
              }

              return (
                <div className="lobby-swap-stage">
                  {renderSwapSlot(0)}
                  <div className="lobby-swap-connector" aria-hidden="true">
                    <span className="lobby-swap-connector-ring">
                      <span className="lobby-swap-connector-icon">⇄</span>
                    </span>
                  </div>
                  {renderSwapSlot(1)}
                </div>
              )
            })()}

            {swapSlots[0] && swapSlots[1] && (() => {
              const playerA = getPlayerByUsername(swapSlots[0]!)
              const playerB = getPlayerByUsername(swapSlots[1]!)
              if (!playerA || !playerB) return null
              return (
                <div className="lobby-swap-preview">
                  <span className="lobby-swap-preview-item">
                    <strong>{playerA.username}</strong>
                    <span className="lobby-role-pill">
                      {getRolLabel(scrim.juego, playerA.rol)}
                    </span>
                  </span>
                  <span className="lobby-swap-preview-arrow">→</span>
                  <span className="lobby-swap-preview-item">
                    <strong>{playerB.username}</strong>
                    <span className="lobby-role-pill">
                      {getRolLabel(scrim.juego, playerB.rol)}
                    </span>
                  </span>
                  <span className="lobby-swap-preview-divider" aria-hidden="true" />
                  <span className="lobby-swap-preview-item lobby-swap-preview-result">
                    <strong>{playerB.username}</strong>
                    <span className="lobby-role-pill">
                      {getRolLabel(scrim.juego, playerA.rol)}
                    </span>
                  </span>
                  <span className="lobby-swap-preview-arrow">→</span>
                  <span className="lobby-swap-preview-item lobby-swap-preview-result">
                    <strong>{playerA.username}</strong>
                    <span className="lobby-role-pill">
                      {getRolLabel(scrim.juego, playerB.rol)}
                    </span>
                  </span>
                </div>
              )
            })()}

            <div className="lobby-swap-actions">
              <button
                type="button"
                className="btn btn-primary lobby-swap-confirm"
                onClick={handleIntercambiar}
                disabled={!swapSlots[0] || !swapSlots[1]}
              >
                Confirmar intercambio
              </button>
              {(swapSlots[0] || swapSlots[1]) && (
                <button type="button" className="btn btn-ghost btn-sm" onClick={clearSwapSlots}>
                  Limpiar selección
                </button>
              )}
            </div>
          </div>
          )}
        </section>
      )}

      <section className="detail-panel">
        <header className="detail-panel-header">
          <div>
            <h2>Postulaciones</h2>
            <p className="detail-panel-hint">
              {scrim.postulaciones.length === 0
                ? 'Todavía no hay jugadores postulados'
                : `${scrim.postulaciones.length} jugador${scrim.postulaciones.length === 1 ? '' : 'es'} en el lobby`}
            </p>
          </div>
        </header>

        {scrim.postulaciones.length === 0 ? (
          <div className="detail-empty">Sin postulaciones aún.</div>
        ) : (
          <div className="detail-player-grid">
            {scrim.postulaciones.map((p) => (
              <article
                key={`${p.username}-${p.rol}`}
                className={`detail-player-card detail-player-card-${p.estado.toLowerCase()}`}
              >
                <div className="detail-player-card-top">
                  <span className="detail-player-avatar">{getInitials(p.username)}</span>
                  <div className="detail-player-info">
                    <strong>{p.username}</strong>
                    <span className="detail-role-tag">{getRolLabel(scrim.juego, p.rol)}</span>
                  </div>
                  <StatusBadge estado={p.estado} />
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      {scrim.estado === 'FINALIZADO' && user && statsForm.length > 0 && (
        <section className="detail-panel">
          <header className="detail-panel-header">
            <div>
              <h2>Estadísticas post-partida</h2>
              <p className="detail-panel-hint">Registrá K/D/A de cada jugador aceptado</p>
            </div>
          </header>

          <form onSubmit={handleStats} className="detail-stats-form">
            <div className="stats-table-wrap">
              <table className="stats-table">
                <thead>
                  <tr>
                    <th>Jugador</th>
                    <th>Kills</th>
                    <th>Deaths</th>
                    <th>Assists</th>
                  </tr>
                </thead>
                <tbody>
                  {statsForm.map((s) => (
                    <tr key={s.username}>
                      <td>
                        <span className="detail-player-chip">
                          <span className="detail-player-avatar detail-player-avatar-sm">
                            {getInitials(s.username)}
                          </span>
                          {s.username}
                        </span>
                      </td>
                      <td>
                        <input
                          type="number"
                          min={0}
                          value={s.kills}
                          onChange={(e) => updateStat(s.username, 'kills', Number(e.target.value))}
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          min={0}
                          value={s.deaths}
                          onChange={(e) => updateStat(s.username, 'deaths', Number(e.target.value))}
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          min={0}
                          value={s.assists}
                          onChange={(e) => updateStat(s.username, 'assists', Number(e.target.value))}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <button type="submit" className="btn btn-primary">
              Registrar estadísticas
            </button>
          </form>

          {estadisticas.length > 0 && (
            <div className="detail-stats-results">
              <span className="filters-group-label">Resultados</span>
              <div className="detail-player-grid">
                {estadisticas.map((e) => (
                  <article key={e.username} className="detail-stat-card">
                    <div className="detail-player-card-top">
                      <span className="detail-player-avatar">{getInitials(e.username)}</span>
                      <div className="detail-player-info">
                        <strong>{e.username}</strong>
                        <span className="detail-stat-kda">
                          {e.kills}/{e.deaths}/{e.assists} · KDA {e.kda.toFixed(2)}
                        </span>
                      </div>
                      {e.mvp && <span className="badge badge-orange">MVP</span>}
                    </div>
                  </article>
                ))}
              </div>
            </div>
          )}
        </section>
      )}

      <div className="detail-community-grid">
        <section className="detail-panel">
          <header className="detail-panel-header">
            <div>
              <h2>Feedback</h2>
              <p className="detail-panel-hint">Valorá a tus compañeros después de la partida</p>
            </div>
            {feedback.length > 0 && (
              <span className="detail-count-badge">{feedback.length}</span>
            )}
          </header>

          {feedback.length > 0 ? (
            <div className="detail-feedback-list">
              {feedback.map((fb) => (
                <article key={fb.id} className="detail-feedback-card">
                  <div className="detail-feedback-top">
                    <div>
                      <p className="detail-feedback-route">
                        <strong>{fb.autor}</strong>
                        <span aria-hidden="true">→</span>
                        <strong>{fb.destinatario}</strong>
                      </p>
                      <RatingStars rating={fb.rating} />
                    </div>
                    <StatusBadge estado={fb.estado} />
                  </div>
                  <p className="detail-feedback-comment">{fb.comentario}</p>
                  {isMod && fb.estado === 'PENDIENTE' && (
                    <div className="detail-moderation-bar">
                      <button
                        type="button"
                        className="btn btn-sm btn-ghost"
                        onClick={() => moderarFeedback(fb.id, true)}
                      >
                        Aprobar
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-ghost btn-danger"
                        onClick={() => moderarFeedback(fb.id, false)}
                      >
                        Rechazar
                      </button>
                    </div>
                  )}
                </article>
              ))}
            </div>
          ) : (
            <div className="detail-empty">Sin feedback todavía.</div>
          )}

          {user && otrosJugadores.length > 0 && (
            <form onSubmit={handleFeedback} className="detail-form-block">
              <span className="filters-group-label">Enviar feedback</span>
              <div className="filters-group">
                <span className="filters-sub-label">Destinatario</span>
                <div className="filters-chip-row" role="group" aria-label="Destinatario">
                  {otrosJugadores.map((j) => (
                    <button
                      key={j}
                      type="button"
                      className={`filters-chip${fbForm.destinatario === j ? ' active' : ''}`}
                      onClick={() => setFbForm((f) => ({ ...f, destinatario: j }))}
                      aria-pressed={fbForm.destinatario === j}
                    >
                      {j}
                    </button>
                  ))}
                </div>
              </div>
              <div className="filters-group">
                <span className="filters-sub-label">Valoración</span>
                <div className="filters-chip-row" role="group" aria-label="Valoración">
                  {RATING_OPTIONS.map((value) => (
                    <button
                      key={value}
                      type="button"
                      className={`filters-chip detail-rating-chip${fbForm.rating === value ? ' active' : ''}`}
                      onClick={() => setFbForm((f) => ({ ...f, rating: value }))}
                      aria-pressed={fbForm.rating === value}
                    >
                      {value} ★
                    </button>
                  ))}
                </div>
              </div>
              <div className="filters-date-field">
                <span className="filters-date-field-label">Comentario</span>
                <input
                  placeholder="¿Cómo jugó tu compañero?"
                  value={fbForm.comentario}
                  onChange={(e) => setFbForm((f) => ({ ...f, comentario: e.target.value }))}
                  required
                />
              </div>
              <button
                type="submit"
                className="btn btn-primary btn-sm"
                disabled={!fbForm.destinatario}
              >
                Enviar feedback
              </button>
            </form>
          )}
        </section>

        <section className="detail-panel detail-panel-reports">
          <header className="detail-panel-header">
            <div>
              <h2>Reportes</h2>
              <p className="detail-panel-hint">Reportá conductas inapropiadas durante el scrim</p>
            </div>
            {reportes.length > 0 && (
              <span className="detail-count-badge detail-count-badge-danger">{reportes.length}</span>
            )}
          </header>

          {reportes.length > 0 ? (
            <div className="detail-report-list">
              {reportes.map((rep) => (
                <article key={rep.id} className="detail-report-card">
                  <div className="detail-report-top">
                    <div>
                      <p className="detail-report-route">
                        <strong>{rep.reportante}</strong> reportó a <strong>{rep.reportado}</strong>
                      </p>
                      <p className="detail-report-motivo">{rep.motivo}</p>
                      {rep.sancion && (
                        <p className="detail-report-sancion">Sanción: {rep.sancion}</p>
                      )}
                    </div>
                    <StatusBadge estado={rep.estado} />
                  </div>
                  {isMod && rep.estado === 'PENDIENTE' && (
                    <div className="detail-moderation-bar">
                      <div className="filters-date-field detail-sancion-field">
                        <span className="filters-date-field-label">Sanción (opcional)</span>
                        <input
                          placeholder="Ej. strike, cooldown..."
                          value={sanciones[rep.id] ?? ''}
                          onChange={(e) =>
                            setSanciones((s) => ({ ...s, [rep.id]: e.target.value }))
                          }
                        />
                      </div>
                      <button
                        type="button"
                        className="btn btn-sm btn-ghost"
                        onClick={() => moderarReporte(rep.id, true)}
                      >
                        Aprobar
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-ghost btn-danger"
                        onClick={() => moderarReporte(rep.id, false)}
                      >
                        Rechazar
                      </button>
                    </div>
                  )}
                </article>
              ))}
            </div>
          ) : (
            <div className="detail-empty">Sin reportes.</div>
          )}

          {user && otrosJugadores.length > 0 && (
            <form onSubmit={handleReporte} className="detail-form-block">
              <span className="filters-group-label">Nuevo reporte</span>
              <div className="filters-group">
                <span className="filters-sub-label">Jugador reportado</span>
                <div className="filters-chip-row" role="group" aria-label="Jugador reportado">
                  {otrosJugadores.map((j) => (
                    <button
                      key={j}
                      type="button"
                      className={`filters-chip${repForm.reportado === j ? ' active' : ''}`}
                      onClick={() => setRepForm((f) => ({ ...f, reportado: j }))}
                      aria-pressed={repForm.reportado === j}
                    >
                      {j}
                    </button>
                  ))}
                </div>
              </div>
              <div className="filters-date-field">
                <span className="filters-date-field-label">Motivo</span>
                <input
                  placeholder="Describí el incidente"
                  value={repForm.motivo}
                  onChange={(e) => setRepForm((f) => ({ ...f, motivo: e.target.value }))}
                  required
                />
              </div>
              <button
                type="submit"
                className="btn btn-ghost btn-sm btn-danger"
                disabled={!repForm.reportado}
              >
                Enviar reporte
              </button>
            </form>
          )}
        </section>
      </div>
    </div>
  )
}
