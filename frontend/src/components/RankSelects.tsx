import { DEFAULT_JUEGO, isJuego } from '../constants/options'
import {
  getDefaultRango,
  getDefaultRangoRange,
  getRangos,
  nearestRango,
  nextRangoValue,
  type RankOption,
} from '../constants/ranks'

interface RankSelectsProps {
  juego: string
  mode: 'single' | 'range'
  rango?: number
  rangoMin?: number
  rangoMax?: number
  onRangoChange?: (rango: number) => void
  onRangoMinChange?: (rangoMin: number | undefined) => void
  onRangoMaxChange?: (rangoMax: number | undefined) => void
  allowEmpty?: boolean
  required?: boolean
}

function rankValue(
  juego: string,
  value: number | undefined,
  allowEmpty: boolean,
): number | '' {
  if (allowEmpty && (value === undefined || value === null)) return ''
  if (value === undefined) return getDefaultRango(isJuego(juego) ? juego : DEFAULT_JUEGO)
  return nearestRango(juego || DEFAULT_JUEGO, value).value
}

export function RankSelects({
  juego,
  mode,
  rango,
  rangoMin,
  rangoMax,
  onRangoChange,
  onRangoMinChange,
  onRangoMaxChange,
  allowEmpty = false,
  required = true,
}: RankSelectsProps) {
  const activeJuego = juego || DEFAULT_JUEGO
  const rangos = juego ? getRangos(juego) : []

  const handleMinChange = (value: string) => {
    if (!onRangoMinChange) return
    if (value === '' && allowEmpty) {
      onRangoMinChange(undefined)
      return
    }
    const min = Number(value)
    onRangoMinChange(min)
    if (onRangoMaxChange && rangoMax !== undefined && min >= rangoMax) {
      onRangoMaxChange(nextRangoValue(activeJuego, min))
    }
  }

  const handleMaxChange = (value: string) => {
    if (!onRangoMaxChange) return
    if (value === '' && allowEmpty) {
      onRangoMaxChange(undefined)
      return
    }
    const max = Number(value)
    if (rangoMin !== undefined && max <= rangoMin) return
    onRangoMaxChange(max)
  }

  if (mode === 'single' && onRangoChange) {
    return (
      <label>
        Rango
        <select
          name="rango"
          value={rankValue(activeJuego, rango, false)}
          onChange={(e) => onRangoChange(Number(e.target.value))}
          required={required}
          disabled={!juego}
        >
          {rangos.map((r: RankOption) => (
            <option key={r.value} value={r.value}>
              {r.label}
            </option>
          ))}
        </select>
      </label>
    )
  }

  return (
    <>
      <label>
        Rango mín.
        <select
          name="rangoMin"
          value={allowEmpty && rangoMin === undefined ? '' : rankValue(activeJuego, rangoMin, false)}
          onChange={(e) => handleMinChange(e.target.value)}
          required={required && !allowEmpty}
          disabled={allowEmpty && !juego}
        >
          {allowEmpty && <option value="">Todos</option>}
          {rangos.map((r) => (
            <option key={r.value} value={r.value}>
              {r.label}
            </option>
          ))}
        </select>
      </label>
      <label>
        Rango máx.
        <select
          name="rangoMax"
          value={allowEmpty && rangoMax === undefined ? '' : rankValue(activeJuego, rangoMax, false)}
          onChange={(e) => handleMaxChange(e.target.value)}
          required={required && !allowEmpty}
          disabled={allowEmpty && !juego}
        >
          {allowEmpty && <option value="">Todos</option>}
          {rangos
            .filter((r) => rangoMin === undefined || r.value > rangoMin)
            .map((r) => (
              <option key={r.value} value={r.value}>
                {r.label}
              </option>
            ))}
        </select>
      </label>
    </>
  )
}

export function defaultRangoValues(juego: string = DEFAULT_JUEGO) {
  if (!isJuego(juego)) {
    return { rango: getDefaultRango(DEFAULT_JUEGO), ...getDefaultRangoRange(DEFAULT_JUEGO) }
  }
  return { rango: getDefaultRango(juego), ...getDefaultRangoRange(juego) }
}
