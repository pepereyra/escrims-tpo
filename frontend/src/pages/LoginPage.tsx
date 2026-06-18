import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { GameSelects, defaultGameFormValues } from '../components/GameSelects'
import { RankSelects, defaultRangoValues } from '../components/RankSelects'
import { getDefaultRango } from '../constants/ranks'
import { useAuth } from '../context/AuthContext'

export function LoginPage() {
  const { login, register, user, loading: authLoading } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [tab, setTab] = useState<'login' | 'register'>('login')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const from =
    typeof location.state === 'object'
      && location.state !== null
      && 'from' in location.state
      && typeof location.state.from === 'object'
      && location.state.from !== null
      && 'pathname' in location.state.from
      && typeof location.state.from.pathname === 'string'
      ? `${location.state.from.pathname}${'search' in location.state.from && typeof location.state.from.search === 'string' ? location.state.from.search : ''}`
      : '/'

  const defaults = defaultGameFormValues()
  const rankDefaults = defaultRangoValues(defaults.juego)
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    region: defaults.region,
    juego: defaults.juego,
    rango: rankDefaults.rango,
    latencia: 30,
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: type === 'number' ? Number(value) : value,
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const username = form.username.trim()
      if (tab === 'login') {
        await login(username, form.password)
      } else {
        await register({ ...form, username })
      }
      navigate(from, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al autenticar')
    } finally {
      setLoading(false)
    }
  }

  if (!authLoading && user) {
    return <Navigate to={from} replace />
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Bienvenido a eScrims</h1>
        <p className="auth-subtitle">Organizá y encontrá scrims competitivos</p>

        <div className="tabs">
          <button
            type="button"
            className={tab === 'login' ? 'tab active' : 'tab'}
            onClick={() => setTab('login')}
          >
            Ingresar
          </button>
          <button
            type="button"
            className={tab === 'register' ? 'tab active' : 'tab'}
            onClick={() => setTab('register')}
          >
            Registrarse
          </button>
        </div>

        <form onSubmit={handleSubmit} className="form">
          <label>
            Usuario
            <input
              name="username"
              value={form.username}
              onChange={handleChange}
              required
              autoComplete="username"
            />
          </label>

          {tab === 'register' && (
            <>
              <label>
                Email
                <input
                  name="email"
                  type="email"
                  value={form.email}
                  onChange={handleChange}
                  required
                />
              </label>
              <div className="form-row">
                <GameSelects
                  juego={form.juego}
                  region={form.region}
                  onJuegoChange={(juego) => {
                    if (!juego) return
                    setForm((prev) => ({
                      ...prev,
                      juego,
                      rango: getDefaultRango(juego),
                    }))
                  }}
                  onRegionChange={(region) => setForm((prev) => ({ ...prev, region }))}
                  showFormato={false}
                />
              </div>
              <div className="form-row">
                <RankSelects
                  juego={form.juego}
                  mode="single"
                  rango={form.rango}
                  onRangoChange={(rango) => setForm((prev) => ({ ...prev, rango }))}
                />
                <label>
                  Latencia (ms)
                  <input
                    name="latencia"
                    type="number"
                    value={form.latencia}
                    onChange={handleChange}
                    required
                  />
                </label>
              </div>
            </>
          )}

          <label>
            Contraseña
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              required
              autoComplete={tab === 'login' ? 'current-password' : 'new-password'}
            />
          </label>

          {error && <p className="form-error">{error}</p>}

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Cargando...' : tab === 'login' ? 'Ingresar' : 'Crear cuenta'}
          </button>
        </form>

        <p className="auth-hint">
          Demo: <code>alpha</code> / <code>12345678</code>
        </p>

        <Link to="/" className="auth-back">
          ← Volver a scrims
        </Link>
      </div>
    </div>
  )
}
