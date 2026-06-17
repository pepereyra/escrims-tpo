import {
  DEFAULT_JUEGO,
  DEFAULT_REGION,
  getDefaultFormato,
  getFormatos,
  isJuego,
  JUEGOS,
  normalizeFormato,
  REGIONES,
  type Juego,
} from '../constants/options'

interface GameSelectsProps {
  juego: string
  region: string
  formato?: string
  onJuegoChange: (juego: Juego | '') => void
  onRegionChange: (region: string) => void
  onFormatoChange?: (formato: string) => void
  showFormato?: boolean
  allowEmpty?: boolean
  required?: boolean
}

export function GameSelects({
  juego,
  region,
  formato,
  onJuegoChange,
  onRegionChange,
  onFormatoChange,
  showFormato = true,
  allowEmpty = false,
  required = true,
}: GameSelectsProps) {
  const formatos = juego ? getFormatos(juego) : []
  const formatoValue =
    showFormato && allowEmpty && !juego
      ? ''
      : showFormato
        ? normalizeFormato(juego || DEFAULT_JUEGO, formato ?? '')
        : ''

  const handleJuegoChange = (value: string) => {
    if (value === '' && allowEmpty) {
      onJuegoChange('')
      if (showFormato && onFormatoChange) onFormatoChange('')
      return
    }
    if (!isJuego(value)) return
    onJuegoChange(value)
    if (showFormato && onFormatoChange) {
      onFormatoChange(getDefaultFormato(value))
    }
  }

  return (
    <>
      <label>
        Juego
        <select
          name="juego"
          value={juego}
          onChange={(e) => handleJuegoChange(e.target.value)}
          required={required && !allowEmpty}
        >
          {allowEmpty && <option value="">Todos</option>}
          {JUEGOS.map((j) => (
            <option key={j} value={j}>
              {j}
            </option>
          ))}
        </select>
      </label>

      {showFormato && onFormatoChange && (
        <label>
          Formato
          <select
            name="formato"
            value={formatoValue}
            onChange={(e) => onFormatoChange(e.target.value)}
            required={required && !allowEmpty}
            disabled={allowEmpty && !juego}
          >
            {allowEmpty && <option value="">Todos</option>}
            {formatos.map((f) => (
              <option key={f} value={f}>
                {f}
              </option>
            ))}
          </select>
        </label>
      )}

      <label>
        Región
        <select
          name="region"
          value={region}
          onChange={(e) => onRegionChange(e.target.value)}
          required={required && !allowEmpty}
        >
          {allowEmpty && <option value="">Todas</option>}
          {REGIONES.map((r) => (
            <option key={r.value} value={r.value}>
              {r.label}
            </option>
          ))}
        </select>
      </label>
    </>
  )
}

export function defaultGameFormValues() {
  return {
    juego: DEFAULT_JUEGO,
    formato: getDefaultFormato(DEFAULT_JUEGO),
    region: DEFAULT_REGION,
  }
}
