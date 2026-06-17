import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { scrimsApi } from '../api'
import type { Scrim } from '../api/types'
import { useAuth } from '../context/AuthContext'
import { defaultGameFormValues } from './GameSelects'
import {
  getDefaultFormato,
  getFormatos,
  isJuego,
  JUEGOS,
  REGIONES,
  type Juego,
} from '../constants/options'
import { getDefaultRangoRange, getRangoRangeAround, getRangos } from '../constants/ranks'
import { fromDatetimeLocal, toDatetimeLocal } from '../utils/format'

const LATENCY_PRESETS = [30, 50, 80, 100, 150] as const

const MODALIDADES = [
  { value: 'CASUAL', label: 'Casual' },
  { value: 'RANKED_LIKE', label: 'Ranked-like' },
  { value: 'PRACTICA', label: 'Práctica' },
] as const

type Modalidad = (typeof MODALIDADES)[number]['value']

interface CreateScrimPanelProps {
  open: boolean
  onClose: () => void
  onCreated?: (scrim: Scrim) => void
}

function buildInitialForm(user: ReturnType<typeof useAuth>['user']) {
  const defaults = defaultGameFormValues()
  const initialJuego: Juego =
    user?.juegoPrincipal && isJuego(user.juegoPrincipal)
      ? user.juegoPrincipal
      : defaults.juego
  const initialRangoRange = user?.rangoPrincipal
    ? getRangoRangeAround(initialJuego, user.rangoPrincipal)
    : getDefaultRangoRange(initialJuego)

  return {
    juego: initialJuego,
    formato: getDefaultFormato(initialJuego),
    region: user?.region ?? defaults.region,
    rangoMin: initialRangoRange.rangoMin,
    rangoMax: initialRangoRange.rangoMax,
    latenciaMax: user?.latenciaPromedio ?? 50,
    fechaHora: toDatetimeLocal(),
    duracionMinutos: 60,
    cuposTotales: 10,
    modalidad: 'CASUAL' as Modalidad,
  }
}

export function CreateScrimPanel({ open, onClose, onCreated }: CreateScrimPanelProps) {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState(() => buildInitialForm(user))

  const formatos = getFormatos(form.juego)
  const rangos = getRangos(form.juego)

  const setJuego = (juego: Juego) => {
    const range = getDefaultRangoRange(juego)
    setForm((prev) => ({
      ...prev,
      juego,
      formato: getDefaultFormato(juego),
      rangoMin: range.rangoMin,
      rangoMax: range.rangoMax,
    }))
  }

  const setRangoMin = (value: number) => {
    setForm((prev) => {
      const currentRangos = getRangos(prev.juego)
      return {
        ...prev,
        rangoMin: value,
        rangoMax:
          prev.rangoMax <= value
            ? currentRangos.find((r) => r.value > value)?.value ?? value
            : prev.rangoMax,
      }
    })
  }

  const setRangoMax = (value: number) => {
    if (value <= form.rangoMin) return
    setForm((prev) => ({ ...prev, rangoMax: value }))
  }

  const handleNumberChange = (name: 'duracionMinutos' | 'cuposTotales', value: string) => {
    const parsed = Number(value)
    if (!Number.isFinite(parsed)) return
    setForm((prev) => ({ ...prev, [name]: parsed }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const scrim = await scrimsApi.create({
        ...form,
        fechaHora: fromDatetimeLocal(form.fechaHora),
      })
      if (onCreated) {
        onCreated(scrim)
      } else {
        navigate(`/scrims/${scrim.id}`)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al crear scrim')
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setError('')
    onClose()
  }

  return (
    <div className={`create-scrim-panel${open ? ' open' : ''}`} aria-hidden={!open}>
      <div className="create-scrim-panel-inner">
        <div className="create-scrim-panel-header">
          <div>
            <h2>Nueva partida</h2>
            <p className="create-scrim-panel-hint">Configurá juego, rango, horario y cupos</p>
          </div>
          <button type="button" className="btn btn-ghost btn-sm" onClick={handleClose}>
            Cerrar
          </button>
        </div>

        <form onSubmit={handleSubmit} className="create-scrim-form">
          <div className="create-scrim-layout">
            <div className="filters-group">
              <span className="filters-group-label">Juego</span>
              <div className="filters-chip-row" role="group" aria-label="Juego">
                {JUEGOS.map((game) => (
                  <button
                    key={game}
                    type="button"
                    className={`filters-chip filters-chip-game filters-chip-${game.toLowerCase()}${form.juego === game ? ' active' : ''}`}
                    onClick={() => setJuego(game)}
                    aria-pressed={form.juego === game}
                  >
                    {game}
                  </button>
                ))}
              </div>
            </div>

            {formatos.length > 0 && (
              <div className="filters-group">
                <span className="filters-group-label">Formato</span>
                <div className="filters-chip-row" role="group" aria-label="Formato">
                  {formatos.map((formato) => (
                    <button
                      key={formato}
                      type="button"
                      className={`filters-chip${form.formato === formato ? ' active' : ''}`}
                      onClick={() => setForm((prev) => ({ ...prev, formato }))}
                      aria-pressed={form.formato === formato}
                    >
                      {formato}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <div className="filters-group">
              <span className="filters-group-label">Región</span>
              <div className="filters-chip-row" role="group" aria-label="Región">
                {REGIONES.map((region) => (
                  <button
                    key={region.value}
                    type="button"
                    className={`filters-chip filters-chip-region${form.region === region.value ? ' active' : ''}`}
                    onClick={() => setForm((prev) => ({ ...prev, region: region.value }))}
                    aria-pressed={form.region === region.value}
                  >
                    <span className="filters-chip-code">{region.value}</span>
                    <span className="filters-chip-sub">
                      {region.label.replace(/\s*\([^)]+\)$/, '')}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            <div className="filters-group">
              <span className="filters-group-label">Modalidad</span>
              <div className="filters-chip-row" role="group" aria-label="Modalidad">
                {MODALIDADES.map((modalidad) => (
                  <button
                    key={modalidad.value}
                    type="button"
                    className={`filters-chip${form.modalidad === modalidad.value ? ' active' : ''}`}
                    onClick={() => setForm((prev) => ({ ...prev, modalidad: modalidad.value }))}
                    aria-pressed={form.modalidad === modalidad.value}
                  >
                    {modalidad.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="filters-group filters-group-rank">
              <span className="filters-group-label">Rango</span>
              <div className="filters-rank-block">
                <span className="filters-sub-label">Mínimo</span>
                <div className="filters-chip-row filters-rank-row" role="group" aria-label="Rango mínimo">
                  {rangos.map((r) => (
                    <button
                      key={`min-${r.value}`}
                      type="button"
                      className={`filters-chip filters-rank-chip${form.rangoMin === r.value ? ' active' : ''}`}
                      onClick={() => setRangoMin(r.value)}
                      aria-pressed={form.rangoMin === r.value}
                    >
                      {r.label}
                    </button>
                  ))}
                </div>
              </div>
              <div className="filters-rank-block">
                <span className="filters-sub-label">Máximo</span>
                <div className="filters-chip-row filters-rank-row" role="group" aria-label="Rango máximo">
                  {rangos
                    .filter((r) => r.value > form.rangoMin)
                    .map((r) => (
                      <button
                        key={`max-${r.value}`}
                        type="button"
                        className={`filters-chip filters-rank-chip${form.rangoMax === r.value ? ' active' : ''}`}
                        onClick={() => setRangoMax(r.value)}
                        aria-pressed={form.rangoMax === r.value}
                      >
                        {r.label}
                      </button>
                    ))}
                </div>
              </div>
            </div>

            <div className="filters-group">
              <span className="filters-group-label">Conexión</span>
              <div className="filters-connection-layout">
                <div className="filters-connection-block">
                  <span className="filters-sub-label">Latencia máx.</span>
                  <div className="filters-chip-row" role="group" aria-label="Latencia máxima">
                    {LATENCY_PRESETS.map((ms) => (
                      <button
                        key={ms}
                        type="button"
                        className={`filters-chip filters-latency-chip${form.latenciaMax === ms ? ' active' : ''}`}
                        onClick={() => setForm((prev) => ({ ...prev, latenciaMax: ms }))}
                        aria-pressed={form.latenciaMax === ms}
                      >
                        ≤ {ms}ms
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            <div className="filters-group">
              <span className="filters-group-label">Partida</span>
              <div className="create-scrim-fields">
                <div className="filters-date-field">
                  <span className="filters-date-field-label">Fecha y hora</span>
                  <input
                    name="fechaHora"
                    type="datetime-local"
                    value={form.fechaHora}
                    onChange={(e) => setForm((prev) => ({ ...prev, fechaHora: e.target.value }))}
                    required
                  />
                </div>
                <div className="filters-date-field">
                  <span className="filters-date-field-label">Duración (min)</span>
                  <input
                    name="duracionMinutos"
                    type="number"
                    min={15}
                    step={15}
                    value={form.duracionMinutos}
                    onChange={(e) => handleNumberChange('duracionMinutos', e.target.value)}
                    required
                  />
                </div>
                <div className="filters-date-field">
                  <span className="filters-date-field-label">Cupos</span>
                  <input
                    name="cuposTotales"
                    type="number"
                    min={2}
                    value={form.cuposTotales}
                    onChange={(e) => handleNumberChange('cuposTotales', e.target.value)}
                    required
                  />
                </div>
              </div>
            </div>
          </div>

          {error && <p className="form-error">{error}</p>}

          <div className="create-scrim-actions">
            <button type="button" className="btn btn-ghost" onClick={handleClose}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creando...' : 'Crear scrim'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
