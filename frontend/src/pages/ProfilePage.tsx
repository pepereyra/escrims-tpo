import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../api'
import { AvailabilityPicker } from '../components/AvailabilityPicker'
import {
  buildJuegosFromUser,
  GameProfilesEditor,
  mergeRolesPreferidos,
  toRangosPorJuego,
  type JuegoPerfil,
} from '../components/GameProfilesEditor'
import { VerifiedBadge } from '../components/VerifiedBadge'
import { getRegionShortLabel, isJuego, JUEGOS, REGIONES, type Juego } from '../constants/options'
import { getRangoLabel } from '../constants/ranks'
import { useAuth } from '../context/AuthContext'
import {
  decodeDisponibilidad,
  encodeDisponibilidad,
  type DisponibilidadState,
} from '../utils/availability'
import { getAvatar, getInitials, readImageFile, setAvatar } from '../utils/avatar'

export function ProfilePage() {
  const { user, refreshUser } = useAuth()
  const fileRef = useRef<HTMLInputElement>(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const [verifying, setVerifying] = useState(false)
  const [avatarUrl, setAvatarUrl] = useState<string | null>(null)

  const [form, setForm] = useState({
    region: '',
    juegos: [] as JuegoPerfil[],
    juegoPrincipal: JUEGOS[0] as Juego,
    latencia: 0,
    disponibilidad: emptyDisp(),
  })

  function emptyDisp(): DisponibilidadState {
    return { dias: [], franjas: [] }
  }

  useEffect(() => {
    if (user) {
      setAvatarUrl(getAvatar(user.id))
      const juegos = buildJuegosFromUser(
        user.juegoPrincipal,
        user.rangoPrincipal,
        user.rangosPorJuego,
        user.rolesPreferidos ?? [],
      )
      setForm({
        region: user.region,
        juegos: juegos.length > 0 ? juegos : buildJuegosFromUser(JUEGOS[0], 0, undefined, []),
        juegoPrincipal: isJuego(user.juegoPrincipal) ? user.juegoPrincipal : juegos[0]?.juego ?? JUEGOS[0],
        latencia: user.latenciaPromedio,
        disponibilidad: decodeDisponibilidad(user.disponibilidad ?? ''),
      })
    }
  }, [user])

  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file || !user) return
    setError('')
    try {
      const dataUrl = await readImageFile(file)
      setAvatar(user.id, dataUrl)
      setAvatarUrl(dataUrl)
      setSuccess('Foto de perfil actualizada')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al subir imagen')
    } finally {
      e.target.value = ''
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: type === 'number' ? Number(value) : value,
    }))
  }

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)
    try {
      const principal = form.juegos.find((j) => j.juego === form.juegoPrincipal) ?? form.juegos[0]
      await authApi.updateProfile({
        region: form.region,
        juegoPrincipal: form.juegoPrincipal,
        rango: principal?.rango,
        rangosPorJuego: toRangosPorJuego(form.juegos),
        latencia: form.latencia,
        disponibilidad: encodeDisponibilidad(form.disponibilidad),
        rolesPreferidos: mergeRolesPreferidos(form.juegos),
      })
      await refreshUser()
      setSuccess('Perfil actualizado')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al guardar')
    } finally {
      setLoading(false)
    }
  }

  const handleVerify = async () => {
    setError('')
    setSuccess('')
    setVerifying(true)
    try {
      await authApi.verifyEmail()
      await refreshUser()
      setSuccess('Email verificado correctamente')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al verificar')
    } finally {
      setVerifying(false)
    }
  }

  if (!user) return null

  const principalEntry = form.juegos.find((j) => j.juego === form.juegoPrincipal) ?? form.juegos[0]
  const principalRangoLabel = principalEntry
    ? getRangoLabel(principalEntry.juego, principalEntry.rango)
    : ''

  return (
    <div className="page profile-page">
      <div className="profile-page-layout">
        <aside className="profile-page-aside">
          <section className="profile-hero">
        <div className="profile-hero-main">
          <div className="profile-avatar-wrap">
            <button
              type="button"
              className="profile-avatar-btn"
              onClick={() => fileRef.current?.click()}
              title="Cambiar foto de perfil"
            >
              {avatarUrl ? (
                <img src={avatarUrl} alt="" className="profile-avatar-img" />
              ) : (
                <span className="profile-avatar-fallback">{getInitials(user.username)}</span>
              )}
              <span className="profile-avatar-overlay">Cambiar foto</span>
            </button>
            <input
              ref={fileRef}
              type="file"
              accept="image/*"
              className="profile-avatar-input"
              onChange={handleAvatarChange}
            />
          </div>

          <div className="profile-identity">
            <h1 className="profile-username">
              {user.username}
              {user.verificado && <VerifiedBadge size="lg" />}
            </h1>
            <p className="profile-handle">@{user.username}</p>
            <div className="profile-badges">
              <span className="profile-role-pill">{user.rolSistema}</span>
              {principalEntry && <span className="profile-rank-pill">{principalRangoLabel}</span>}
              {form.juegos.map((entry) => (
                <span
                  key={entry.juego}
                  className={`profile-game-pill ${entry.juego === form.juegoPrincipal ? 'primary' : ''}`}
                >
                  {entry.juego}
                  {entry.juego === form.juegoPrincipal && ' ★'}
                </span>
              ))}
            </div>
          </div>
        </div>

        <div className="profile-hero-stats">
          <div className="profile-mini-stat">
            <span className="profile-mini-stat-value">{user.strikes}</span>
            <span className="profile-mini-stat-label">Strikes</span>
          </div>
          <div className="profile-mini-stat">
            <span className="profile-mini-stat-value">{form.latencia}ms</span>
            <span className="profile-mini-stat-label">Latencia</span>
          </div>
          <div className="profile-mini-stat">
            <span className="profile-mini-stat-value">{getRegionShortLabel(form.region)}</span>
            <span className="profile-mini-stat-label">Región</span>
          </div>
          <div className="profile-mini-stat">
            <span className="profile-mini-stat-value">{form.juegos.length}</span>
            <span className="profile-mini-stat-label">Juegos</span>
          </div>
        </div>
          </section>

          <section className={`profile-email-card ${user.verificado ? 'verified' : 'pending'}`}>
            <div className="profile-email-info">
              <span className="profile-email-label">Email</span>
              <span className="profile-email-value">{user.email}</span>
              <span className={`profile-email-status ${user.verificado ? 'ok' : 'warn'}`}>
                {user.verificado ? '✓ Cuenta verificada' : 'Pendiente de verificación'}
              </span>
            </div>
            {!user.verificado && (
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={handleVerify}
                disabled={verifying}
              >
                {verifying ? 'Verificando...' : 'Verificar email'}
              </button>
            )}
          </section>
        </aside>

        <form onSubmit={handleSave} className="profile-form">
        <section className="form-card profile-section">
          <h2>Competitivo</h2>

          <div className="profile-connection">
            <div className="profile-connection-region">
              <span className="profile-field-label">Región</span>
              <div className="profile-region-options" role="group" aria-label="Región">
                {REGIONES.map((region) => (
                  <button
                    key={region.value}
                    type="button"
                    className={`profile-region-chip${form.region === region.value ? ' active' : ''}`}
                    onClick={() => setForm((prev) => ({ ...prev, region: region.value }))}
                    aria-pressed={form.region === region.value}
                  >
                    <span className="profile-region-chip-code">{region.value}</span>
                    <span className="profile-region-chip-name">
                      {region.label.replace(/\s*\([^)]+\)$/, '')}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            <div className="profile-connection-latency">
              <label className="profile-latency-label" htmlFor="profile-latencia">
                Latencia promedio
              </label>
              <div className="profile-latency-input-wrap">
                <input
                  id="profile-latencia"
                  name="latencia"
                  type="number"
                  min={0}
                  value={form.latencia}
                  onChange={handleChange}
                />
                <span className="profile-latency-suffix">ms</span>
              </div>
            </div>
          </div>

          <GameProfilesEditor
            juegos={form.juegos}
            juegoPrincipal={form.juegoPrincipal}
            onChange={(juegos, juegoPrincipal) =>
              setForm((prev) => ({ ...prev, juegos, juegoPrincipal }))
            }
          />
        </section>

        <section className="form-card profile-section">
          <h2>Disponibilidad</h2>
          <p className="hint">Seleccioná los días y horarios en los que podés jugar scrims.</p>
          <AvailabilityPicker
            value={form.disponibilidad}
            onChange={(disponibilidad) => setForm((prev) => ({ ...prev, disponibilidad }))}
          />
        </section>

        {error && <p className="form-error">{error}</p>}
        {success && <p className="form-success">{success}</p>}

        <div className="profile-form-actions">
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Guardando...' : 'Guardar cambios'}
          </button>
          <Link to="/" className="btn btn-ghost">
            Volver a scrims
          </Link>
        </div>
        </form>
      </div>
    </div>
  )
}
