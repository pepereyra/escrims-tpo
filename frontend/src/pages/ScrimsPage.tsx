import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { scrimsApi } from '../api'
import type { Scrim, ScrimFilters } from '../api/types'
import { CreateScrimPanel } from '../components/CreateScrimPanel'
import { ScrimCard } from '../components/ScrimCard'
import { ScrimFiltersPanel } from '../components/ScrimFiltersPanel'
import { useAuth } from '../context/AuthContext'
import { fromDateInput } from '../utils/format'

const EMPTY_FILTERS: ScrimFilters = {
  juego: undefined,
  formato: undefined,
  region: undefined,
  rangoMin: undefined,
  rangoMax: undefined,
  latenciaMax: undefined,
  fecha: undefined,
}

function hasActiveFilters(filters: ScrimFilters) {
  return Object.entries(filters).some(([, value]) => value !== undefined && value !== '')
}

export function ScrimsPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const heroBlockRef = useRef<HTMLDivElement>(null)
  const [scrims, setScrims] = useState<Scrim[]>([])
  const [filters, setFilters] = useState<ScrimFilters>(EMPTY_FILTERS)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [createOpen, setCreateOpen] = useState(searchParams.get('crear') === '1')

  useEffect(() => {
    if (searchParams.get('crear') === '1') {
      setCreateOpen(true)
      setSearchParams({}, { replace: true })
    }
  }, [searchParams, setSearchParams])

  const openCreatePanel = useCallback(() => {
    setCreateOpen(true)
    heroBlockRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, [])

  const load = useCallback(async (f: ScrimFilters) => {
    setLoading(true)
    setError('')
    try {
      const query: ScrimFilters = { ...f }
      if (query.fecha) {
        query.fecha = fromDateInput(query.fecha)
      }
      const data = await scrimsApi.list(query)
      setScrims(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar scrims')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load(filters)
  }, [load, filters])

  const clearFilters = () => setFilters(EMPTY_FILTERS)

  const stats = useMemo(() => {
    const buscando = scrims.filter((s) => s.estado === 'BUSCANDO').length
    const conCupos = scrims.filter((s) => s.cuposDisponibles > 0).length
    return { total: scrims.length, buscando, conCupos }
  }, [scrims])

  const filtersActive = hasActiveFilters(filters)

  return (
    <div className="page scrims-page">
      <div className="scrims-hero-block" ref={heroBlockRef}>
        <section className="scrims-hero">
          <div className="scrims-hero-content">
            <p className="scrims-hero-eyebrow">Matchmaking en vivo</p>
            <h1>Scrims disponibles</h1>
            <p className="scrims-hero-subtitle">
              Encontrá partidas que coincidan con tu perfil o creá la tuya
            </p>

            {!loading && !error && (
              <div className="scrims-hero-stats">
                <div className="scrims-hero-stat">
                  <span className="scrims-hero-stat-value">{stats.total}</span>
                  <span className="scrims-hero-stat-label">En listado</span>
                </div>
                <div className="scrims-hero-stat">
                  <span className="scrims-hero-stat-value">{stats.buscando}</span>
                  <span className="scrims-hero-stat-label">Buscando</span>
                </div>
                <div className="scrims-hero-stat">
                  <span className="scrims-hero-stat-value">{stats.conCupos}</span>
                  <span className="scrims-hero-stat-label">Con cupos</span>
                </div>
              </div>
            )}
          </div>

          {user ? (
            <button
              type="button"
              className={`btn btn-primary scrims-hero-cta${createOpen ? ' open' : ''}`}
              onClick={() => setCreateOpen((open) => !open)}
              aria-expanded={createOpen}
            >
              <span className="scrims-hero-cta-icon" aria-hidden="true">
                +
              </span>
              <span className="scrims-hero-cta-text">
                <span className="scrims-hero-cta-title">Crear scrim</span>
                <span className="scrims-hero-cta-hint">
                  {createOpen ? 'Ocultar formulario' : 'Hosteá una partida nueva'}
                </span>
              </span>
              <span className="scrims-hero-cta-chevron" aria-hidden="true" />
            </button>
          ) : (
            <Link to="/login" className="btn btn-primary scrims-hero-cta">
              <span className="scrims-hero-cta-text">
                <span className="scrims-hero-cta-title">Ingresar</span>
                <span className="scrims-hero-cta-hint">Para crear o unirte</span>
              </span>
            </Link>
          )}
        </section>

        {user && (
          <CreateScrimPanel
            open={createOpen}
            onClose={() => setCreateOpen(false)}
            onCreated={(scrim) => navigate(`/scrims/${scrim.id}`)}
          />
        )}
      </div>

      <ScrimFiltersPanel filters={filters} onChange={setFilters} onClear={clearFilters} />

      <section className="scrims-results">
        <div className="scrims-results-header">
          <h2>
            {loading
              ? 'Cargando scrims...'
              : `${scrims.length} scrim${scrims.length === 1 ? '' : 's'} encontrado${scrims.length === 1 ? '' : 's'}`}
          </h2>
          {filtersActive && !loading && (
            <span className="scrims-results-filtered">Filtros activos</span>
          )}
        </div>

        {loading && (
          <div className="scrims-loading">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="scrim-skeleton" />
            ))}
          </div>
        )}

        {error && <p className="form-error">{error}</p>}

        {!loading && !error && scrims.length === 0 && (
          <div className="empty-state scrims-empty">
            <div className="scrims-empty-icon" aria-hidden="true">
              <span />
              <span />
              <span />
            </div>
            <h3>No hay scrims con estos filtros</h3>
            <p>Probá ampliando la búsqueda o creá una partida nueva.</p>
            <div className="scrims-empty-actions">
              {filtersActive && (
                <button type="button" className="btn btn-ghost" onClick={clearFilters}>
                  Limpiar filtros
                </button>
              )}
              {user ? (
                <button type="button" className="btn btn-primary" onClick={openCreatePanel}>
                  + Crear scrim
                </button>
              ) : (
                <Link to="/login" className="btn btn-primary">
                  Ingresar
                </Link>
              )}
            </div>
          </div>
        )}

        {!loading && scrims.length > 0 && (
          <div className="scrim-grid">
            {scrims.map((scrim) => (
              <ScrimCard key={scrim.id} scrim={scrim} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
