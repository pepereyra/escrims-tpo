import type { ScrimFilters } from '../api/types'
import { getFormatos, isJuego, JUEGOS, REGIONES, type Juego } from '../constants/options'
import { getRangoLabel, getRangos } from '../constants/ranks'

const LATENCY_PRESETS = [30, 50, 80, 100, 150] as const

interface ScrimFiltersPanelProps {
  filters: ScrimFilters
  onChange: (filters: ScrimFilters) => void
  onClear: () => void
}

function hasActiveFilters(filters: ScrimFilters) {
  return Object.entries(filters).some(([, value]) => value !== undefined && value !== '')
}

export function ScrimFiltersPanel({ filters, onChange, onClear }: ScrimFiltersPanelProps) {
  const juego = filters.juego ?? ''
  const formatos = juego ? getFormatos(juego) : []
  const rangos = juego ? getRangos(juego) : []
  const filtersActive = hasActiveFilters(filters)

  const set = (patch: Partial<ScrimFilters>) => onChange({ ...filters, ...patch })

  const toggleJuego = (value: Juego | '') => {
    if (value === '' || filters.juego === value) {
      set({ juego: undefined, formato: undefined, rangoMin: undefined, rangoMax: undefined })
      return
    }
    set({ juego: value, formato: undefined, rangoMin: undefined, rangoMax: undefined })
  }

  const toggleRegion = (value: string) => {
    set({ region: filters.region === value ? undefined : value })
  }

  const toggleFormato = (value: string) => {
    set({ formato: filters.formato === value ? undefined : value })
  }

  const setRangoMin = (value: number | undefined) => {
    set({
      rangoMin: value,
      rangoMax:
        value !== undefined && filters.rangoMax !== undefined && filters.rangoMax <= value
          ? undefined
          : filters.rangoMax,
    })
  }

  const setRangoMax = (value: number | undefined) => {
    if (value !== undefined && filters.rangoMin !== undefined && value <= filters.rangoMin) return
    set({ rangoMax: value })
  }

  const setLatencia = (value: number | undefined) => {
    set({ latenciaMax: value })
  }

  const activeTags = buildActiveTags(filters)

  return (
    <section className="filters-panel">
      <div className="filters-panel-header">
        <div>
          <h2>Filtrar scrims</h2>
          <p className="filters-panel-hint">Refiná la búsqueda por juego, región, rango y más</p>
        </div>
        {filtersActive && (
          <button type="button" className="btn btn-ghost btn-sm filters-clear-btn" onClick={onClear}>
            Limpiar todo
          </button>
        )}
      </div>

      {activeTags.length > 0 && (
        <div className="filters-active-tags" aria-label="Filtros aplicados">
          {activeTags.map((tag) => (
            <span key={tag.key} className="filters-active-tag">
              {tag.label}
            </span>
          ))}
        </div>
      )}

      <div className="filters-layout">
        <div className="filters-group">
          <span className="filters-group-label">Juego</span>
          <div className="filters-chip-row" role="group" aria-label="Juego">
            <button
              type="button"
              className={`filters-chip${!juego ? ' active' : ''}`}
              onClick={() => toggleJuego('')}
              aria-pressed={!juego}
            >
              Todos
            </button>
            {JUEGOS.map((game) => (
              <button
                key={game}
                type="button"
                className={`filters-chip filters-chip-game filters-chip-${game.toLowerCase()}${juego === game ? ' active' : ''}`}
                onClick={() => toggleJuego(game)}
                aria-pressed={juego === game}
              >
                {game}
              </button>
            ))}
          </div>
        </div>

        {juego && formatos.length > 0 && (
          <div className="filters-group">
            <span className="filters-group-label">Formato</span>
            <div className="filters-chip-row" role="group" aria-label="Formato">
              <button
                type="button"
                className={`filters-chip${!filters.formato ? ' active' : ''}`}
                onClick={() => set({ formato: undefined })}
                aria-pressed={!filters.formato}
              >
                Todos
              </button>
              {formatos.map((formato) => (
                <button
                  key={formato}
                  type="button"
                  className={`filters-chip${filters.formato === formato ? ' active' : ''}`}
                  onClick={() => toggleFormato(formato)}
                  aria-pressed={filters.formato === formato}
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
            <button
              type="button"
              className={`filters-chip${!filters.region ? ' active' : ''}`}
              onClick={() => set({ region: undefined })}
              aria-pressed={!filters.region}
            >
              Todas
            </button>
            {REGIONES.map((region) => (
              <button
                key={region.value}
                type="button"
                className={`filters-chip filters-chip-region${filters.region === region.value ? ' active' : ''}`}
                onClick={() => toggleRegion(region.value)}
                aria-pressed={filters.region === region.value}
              >
                <span className="filters-chip-code">{region.value}</span>
                <span className="filters-chip-sub">
                  {region.label.replace(/\s*\([^)]+\)$/, '')}
                </span>
              </button>
            ))}
          </div>
        </div>

        <div className={`filters-group filters-group-rank${!juego ? ' disabled' : ''}`}>
          <span className="filters-group-label">Rango</span>
          {!juego ? (
            <p className="filters-group-hint">Elegí un juego para filtrar por rango.</p>
          ) : (
            <>
              <div className="filters-rank-block">
                <span className="filters-sub-label">Mínimo</span>
                <div className="filters-chip-row filters-rank-row" role="group" aria-label="Rango mínimo">
                  <button
                    type="button"
                    className={`filters-chip${filters.rangoMin === undefined ? ' active' : ''}`}
                    onClick={() => setRangoMin(undefined)}
                    aria-pressed={filters.rangoMin === undefined}
                  >
                    Sin mínimo
                  </button>
                  {rangos.map((r) => (
                    <button
                      key={`min-${r.value}`}
                      type="button"
                      className={`filters-chip filters-rank-chip${filters.rangoMin === r.value ? ' active' : ''}`}
                      onClick={() =>
                        setRangoMin(filters.rangoMin === r.value ? undefined : r.value)
                      }
                      aria-pressed={filters.rangoMin === r.value}
                    >
                      {r.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="filters-rank-block">
                <span className="filters-sub-label">Máximo</span>
                <div className="filters-chip-row filters-rank-row" role="group" aria-label="Rango máximo">
                  <button
                    type="button"
                    className={`filters-chip${filters.rangoMax === undefined ? ' active' : ''}`}
                    onClick={() => setRangoMax(undefined)}
                    aria-pressed={filters.rangoMax === undefined}
                  >
                    Sin máximo
                  </button>
                  {rangos
                    .filter((r) => filters.rangoMin === undefined || r.value > filters.rangoMin)
                    .map((r) => (
                      <button
                        key={`max-${r.value}`}
                        type="button"
                        className={`filters-chip filters-rank-chip${filters.rangoMax === r.value ? ' active' : ''}`}
                        onClick={() =>
                          setRangoMax(filters.rangoMax === r.value ? undefined : r.value)
                        }
                        aria-pressed={filters.rangoMax === r.value}
                      >
                        {r.label}
                      </button>
                    ))}
                </div>
              </div>
            </>
          )}
        </div>

        <div className="filters-group filters-group-connection">
          <span className="filters-group-label">Conexión</span>
          <div className="filters-connection-layout">
            <div className="filters-connection-block">
              <span className="filters-sub-label">Latencia máxima</span>
              <div className="filters-chip-row" role="group" aria-label="Latencia máxima">
                <button
                  type="button"
                  className={`filters-chip${filters.latenciaMax === undefined ? ' active' : ''}`}
                  onClick={() => setLatencia(undefined)}
                  aria-pressed={filters.latenciaMax === undefined}
                >
                  Cualquiera
                </button>
                {LATENCY_PRESETS.map((ms) => (
                  <button
                    key={ms}
                    type="button"
                    className={`filters-chip filters-latency-chip${filters.latenciaMax === ms ? ' active' : ''}`}
                    onClick={() => setLatencia(filters.latenciaMax === ms ? undefined : ms)}
                    aria-pressed={filters.latenciaMax === ms}
                  >
                    ≤ {ms}ms
                  </button>
                ))}
              </div>
            </div>

            <div className="filters-connection-block">
              <span className="filters-sub-label">Fecha</span>
              <div className="filters-date-row">
                <button
                  type="button"
                  className={`filters-chip${!filters.fecha ? ' active' : ''}`}
                  onClick={() => set({ fecha: undefined })}
                  aria-pressed={!filters.fecha}
                >
                  Cualquier día
                </button>
                <label className="filters-date-field">
                  <span className="filters-date-field-label">Día específico</span>
                  <input
                    type="date"
                    value={filters.fecha ?? ''}
                    onChange={(e) => set({ fecha: e.target.value || undefined })}
                  />
                </label>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

function buildActiveTags(filters: ScrimFilters) {
  const tags: { key: string; label: string }[] = []

  if (filters.juego) tags.push({ key: 'juego', label: filters.juego })
  if (filters.formato) tags.push({ key: 'formato', label: filters.formato })
  if (filters.region) tags.push({ key: 'region', label: filters.region })
  if (filters.rangoMin !== undefined && filters.juego && isJuego(filters.juego)) {
    tags.push({
      key: 'rangoMin',
      label: `Min ${getRangoLabel(filters.juego, filters.rangoMin)}`,
    })
  }
  if (filters.rangoMax !== undefined && filters.juego && isJuego(filters.juego)) {
    tags.push({
      key: 'rangoMax',
      label: `Máx ${getRangoLabel(filters.juego, filters.rangoMax)}`,
    })
  }
  if (filters.fecha) tags.push({ key: 'fecha', label: filters.fecha })
  if (filters.latenciaMax !== undefined) {
    tags.push({ key: 'latencia', label: `≤ ${filters.latenciaMax}ms` })
  }

  return tags
}
