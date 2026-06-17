import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { favoritosApi } from '../api'
import type { AlertaBusqueda, BusquedaFavorita } from '../api/types'
import { GameSelects, defaultGameFormValues } from '../components/GameSelects'
import { RankSelects, defaultRangoValues } from '../components/RankSelects'
import { getDefaultRangoRange } from '../constants/ranks'
import { formatFecha } from '../utils/format'

export function FavoritesPage() {
  const [favoritos, setFavoritos] = useState<BusquedaFavorita[]>([])
  const [alertas, setAlertas] = useState<AlertaBusqueda[]>([])
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(true)

  const defaults = defaultGameFormValues()
  const rankDefaults = defaultRangoValues(defaults.juego)
  const [form, setForm] = useState({
    juego: defaults.juego,
    formato: defaults.formato,
    region: defaults.region,
    rangoMin: rankDefaults.rangoMin,
    rangoMax: rankDefaults.rangoMax,
    latenciaMax: '',
    fecha: '',
  })

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [fav, al] = await Promise.all([favoritosApi.list(), favoritosApi.alertas()])
      setFavoritos(fav)
      setAlertas(al)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    try {
      await favoritosApi.create({
        juego: form.juego,
        formato: form.formato,
        region: form.region,
        rangoMin: form.rangoMin,
        rangoMax: form.rangoMax,
        latenciaMax: form.latenciaMax ? Number(form.latenciaMax) : undefined,
        fecha: form.fecha || undefined,
      })
      setSuccess('Búsqueda guardada')
      load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al guardar')
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Búsquedas favoritas</h1>
        <p className="page-subtitle">Guardá filtros y recibí alertas cuando haya scrims nuevos</p>
      </div>

      <form onSubmit={handleSubmit} className="form-card form">
        <div className="form-row">
          <GameSelects
            juego={form.juego}
            formato={form.formato}
            region={form.region}
            onJuegoChange={(juego) => {
              if (!juego) return
              const range = getDefaultRangoRange(juego)
              setForm((prev) => ({
                ...prev,
                juego,
                rangoMin: range.rangoMin,
                rangoMax: range.rangoMax,
              }))
            }}
            onFormatoChange={(formato) => setForm((prev) => ({ ...prev, formato }))}
            onRegionChange={(region) => setForm((prev) => ({ ...prev, region }))}
          />
        </div>
        <div className="form-row">
          <RankSelects
            juego={form.juego}
            mode="range"
            rangoMin={form.rangoMin}
            rangoMax={form.rangoMax}
            onRangoMinChange={(rangoMin) =>
              rangoMin !== undefined && setForm((prev) => ({ ...prev, rangoMin }))
            }
            onRangoMaxChange={(rangoMax) =>
              rangoMax !== undefined && setForm((prev) => ({ ...prev, rangoMax }))
            }
          />
          <label>
            Latencia máx.
            <input name="latenciaMax" type="number" value={form.latenciaMax} onChange={handleChange} />
          </label>
          <label>
            Fecha objetivo
            <input name="fecha" type="date" value={form.fecha} onChange={handleChange} />
          </label>
        </div>
        {error && <p className="form-error">{error}</p>}
        {success && <p className="form-success">{success}</p>}
        <button type="submit" className="btn btn-primary">
          Guardar búsqueda
        </button>
      </form>

      {loading && <p className="page-loading">Cargando...</p>}

      <section className="form-card">
        <h2>Mis búsquedas</h2>
        {favoritos.length === 0 ? (
          <p className="hint">No tenés búsquedas guardadas.</p>
        ) : (
          <ul className="list">
            {favoritos.map((f) => (
              <li key={f.id} className="list-item">
                <span className="list-main">
                  {f.juego} · {f.formato} · {f.region}
                  {f.fecha && <span className="hint"> · {f.fecha}</span>}
                </span>
                <span className="hint">{formatFecha(f.fechaCreacion)}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="form-card">
        <h2>Alertas</h2>
        {alertas.length === 0 ? (
          <p className="hint">Sin alertas por ahora.</p>
        ) : (
          <ul className="list">
            {alertas.map((a) => (
              <li key={a.id} className="list-item">
                <div>
                  <p className="list-main">{a.mensaje}</p>
                  <span className="hint">{formatFecha(a.fechaCreacion)}</span>
                </div>
                <Link to={`/scrims/${a.scrimId}`} className="btn btn-sm btn-ghost">
                  Ver scrim
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
