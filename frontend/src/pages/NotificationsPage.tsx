import { useEffect, useState } from 'react'
import { notificacionesApi } from '../api'
import { useAuth } from '../context/AuthContext'
import {
  countActivePrefs,
  getNotifPrefs,
  setNotifPref,
  type NotifChannel,
  type NotifPrefs,
} from '../utils/notifications'

const CANALES = [
  {
    id: 'email' as const,
    label: 'Email',
    description: 'Alertas de scrims, confirmaciones y recordatorios en tu correo.',
    detail: 'Usa el email de tu cuenta',
    api: notificacionesApi.email,
    icon: (
      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          d="M3.5 6.5h17v11h-17v-11Z"
          stroke="currentColor"
          strokeWidth="1.6"
          strokeLinejoin="round"
        />
        <path
          d="m4.5 7.5 7.5 5.5 7.5-5.5"
          stroke="currentColor"
          strokeWidth="1.6"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    ),
  },
  {
    id: 'push' as const,
    label: 'Push',
    description: 'Notificaciones instantáneas en el navegador o dispositivo móvil.',
    detail: 'Ideal para avisos de último momento',
    api: notificacionesApi.push,
    icon: (
      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          d="M12 4.5a4.25 4.25 0 0 0-4.25 4.25v2.9l-1.35 2.2a1 1 0 0 0 .85 1.53h9.5a1 1 0 0 0 .85-1.53l-1.35-2.2v-2.9A4.25 4.25 0 0 0 12 4.5Z"
          stroke="currentColor"
          strokeWidth="1.6"
          strokeLinejoin="round"
        />
        <path d="M10 18.5h4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
      </svg>
    ),
  },
  {
    id: 'discord' as const,
    label: 'Discord',
    description: 'Mensajes automáticos en Discord cuando cambia el estado de un scrim.',
    detail: 'Webhook de servidor o DM',
    api: notificacionesApi.discord,
    icon: (
      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          d="M8.5 8.75c1.45-.65 3-.95 4.5-.95s3.05.3 4.5.95c.75.35 1.35 1 1.75 1.8-.45.2-.9.45-1.3.75-1.15.9-2.55 1.4-4 1.4s-2.85-.5-4-1.4c-.4-.3-.85-.55-1.3-.75.4-.8 1-1.45 1.75-1.8Z"
          stroke="currentColor"
          strokeWidth="1.5"
        />
        <circle cx="9.75" cy="12.25" r="1.1" fill="currentColor" />
        <circle cx="14.25" cy="12.25" r="1.1" fill="currentColor" />
        <path
          d="M6.5 7.5c2.1-1.55 4.55-2.35 7-2.35s4.9.8 7 2.35c.95.7 1.65 1.75 2 2.95-.55 2.45-2.15 4.45-4.35 5.55-1.35.7-2.85 1.05-4.35 1.05s-3-.35-4.35-1.05C5.65 14.85 4.05 12.85 3.5 10.45c.35-1.2 1.05-2.25 2-2.95Z"
          stroke="currentColor"
          strokeWidth="1.5"
          strokeLinejoin="round"
        />
      </svg>
    ),
  },
] as const

export function NotificationsPage() {
  const { user } = useAuth()
  const [prefs, setPrefs] = useState<NotifPrefs>({
    email: false,
    push: false,
    discord: false,
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState<NotifChannel | null>(null)

  useEffect(() => {
    if (user) setPrefs(getNotifPrefs(user.id))
  }, [user])

  const activeCount = countActivePrefs(prefs)

  const toggleCanal = async (canal: (typeof CANALES)[number]) => {
    if (!user) return

    const isActive = prefs[canal.id]
    setError('')
    setSuccess('')

    if (canal.id === 'email' && !isActive && !user.verificado) {
      setError('Verificá tu email en el perfil antes de activar notificaciones por correo.')
      return
    }

    if (isActive) {
      setPrefs(setNotifPref(user.id, canal.id, false))
      setSuccess(`${canal.label} desactivado`)
      return
    }

    setLoading(canal.id)
    try {
      await canal.api([user.username])
      setPrefs(setNotifPref(user.id, canal.id, true))
      setSuccess(`${canal.label} activado correctamente`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al configurar')
    } finally {
      setLoading(null)
    }
  }

  return (
    <div className="page notifications-page">
      <div className="page-header">
        <h1>Notificaciones</h1>
        <p className="page-subtitle">
          Elegí por qué canal querés recibir alertas de scrims y recordatorios
        </p>
      </div>

      <div className="notifications-layout">
        <aside className="notifications-summary form-card">
          <span className="notifications-summary-label">Estado general</span>
          <div className="notifications-summary-count">
            <span className="notifications-summary-value">{activeCount}</span>
            <span className="notifications-summary-total">/ {CANALES.length}</span>
          </div>
          <p className="notifications-summary-text">canales activos</p>

          <div className="notifications-summary-list">
            {CANALES.map((canal) => (
              <div
                key={canal.id}
                className={`notifications-summary-item${prefs[canal.id] ? ' active' : ''}`}
              >
                <span className="notifications-summary-dot" />
                <span>{canal.label}</span>
                <span className="notifications-summary-status">
                  {prefs[canal.id] ? 'Activo' : 'Inactivo'}
                </span>
              </div>
            ))}
          </div>

          {user && (
            <p className="notifications-summary-user">
              Configuración de <strong>{user.username}</strong>
            </p>
          )}
        </aside>

        <section className="notifications-channels">
          <div className="notif-channel-grid">
            {CANALES.map((canal) => {
              const active = prefs[canal.id]
              const busy = loading === canal.id
              const emailBlocked = canal.id === 'email' && !user?.verificado && !active

              return (
                <article
                  key={canal.id}
                  className={`notif-channel-card${active ? ' active' : ''}${emailBlocked ? ' blocked' : ''}`}
                >
                  <div className="notif-channel-accent" />
                  <div className="notif-channel-body">
                    <div className="notif-channel-top">
                      <div className="notif-channel-icon">{canal.icon}</div>
                      <span className={`notif-status-pill${active ? ' on' : ' off'}`}>
                        {active ? 'Activo' : 'Inactivo'}
                      </span>
                    </div>

                    <h2 className="notif-channel-title">{canal.label}</h2>
                    <p className="notif-channel-desc">{canal.description}</p>
                    <p className="notif-channel-detail">{canal.detail}</p>

                    {emailBlocked && (
                      <p className="notif-channel-warn">
                        Verificá tu email en el perfil para activar este canal.
                      </p>
                    )}

                    <button
                      type="button"
                      className={`notif-toggle${active ? ' on' : ''}`}
                      role="switch"
                      aria-checked={active}
                      aria-label={`${active ? 'Desactivar' : 'Activar'} notificaciones por ${canal.label}`}
                      onClick={() => toggleCanal(canal)}
                      disabled={loading !== null || emailBlocked}
                    >
                      <span className="notif-toggle-track">
                        <span className="notif-toggle-thumb" />
                      </span>
                      <span className="notif-toggle-label">
                        {busy ? 'Activando...' : active ? 'Activado' : 'Desactivado'}
                      </span>
                    </button>
                  </div>
                </article>
              )
            })}
          </div>

          {error && <p className="form-error">{error}</p>}
          {success && <p className="form-success">{success}</p>}
        </section>
      </div>
    </div>
  )
}
