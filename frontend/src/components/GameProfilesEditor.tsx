import { RankSelects } from './RankSelects'
import { isJuego, JUEGOS, type Juego } from '../constants/options'
import { getDefaultRango, getRangoLabel, nearestRango } from '../constants/ranks'
import { filterRolesPreferidos, getRolesForJuego } from '../constants/roles'

export interface JuegoPerfil {
  juego: Juego
  rango: number
  rolesPreferidos: string[]
}

interface GameProfilesEditorProps {
  juegos: JuegoPerfil[]
  juegoPrincipal: Juego
  onChange: (juegos: JuegoPerfil[], juegoPrincipal: Juego) => void
}

export function buildJuegosFromUser(
  juegoPrincipal: string,
  rangoPrincipal: number,
  rangosPorJuego: Record<string, number> | undefined,
  rolesPreferidos: string[],
): JuegoPerfil[] {
  const rangos =
    rangosPorJuego && Object.keys(rangosPorJuego).length > 0
      ? rangosPorJuego
      : juegoPrincipal
        ? { [juegoPrincipal]: rangoPrincipal }
        : { [JUEGOS[0]]: getDefaultRango(JUEGOS[0]) }

  return Object.entries(rangos)
    .filter(([juego]) => isJuego(juego))
    .map(([juego, rango]) => ({
      juego: juego as Juego,
      rango: nearestRango(juego, rango).value,
      rolesPreferidos: filterRolesPreferidos(juego, rolesPreferidos),
    }))
}

export function mergeRolesPreferidos(juegos: JuegoPerfil[]): string[] {
  return [...new Set(juegos.flatMap((j) => j.rolesPreferidos))]
}

export function toRangosPorJuego(juegos: JuegoPerfil[]): Record<string, number> {
  return Object.fromEntries(juegos.map((j) => [j.juego, j.rango]))
}

export function GameProfilesEditor({ juegos, juegoPrincipal, onChange }: GameProfilesEditorProps) {
  const selected = new Set(juegos.map((j) => j.juego))

  const toggleJuego = (juego: Juego) => {
    if (selected.has(juego)) {
      if (juegos.length === 1) return
      const next = juegos.filter((j) => j.juego !== juego)
      const nextPrincipal = juegoPrincipal === juego ? next[0].juego : juegoPrincipal
      onChange(next, nextPrincipal)
      return
    }

    onChange(
      [
        ...juegos,
        {
          juego,
          rango: getDefaultRango(juego),
          rolesPreferidos: [],
        },
      ],
      juegoPrincipal,
    )
  }

  const updateJuego = (juego: Juego, patch: Partial<JuegoPerfil>) => {
    onChange(
      juegos.map((entry) => (entry.juego === juego ? { ...entry, ...patch } : entry)),
      juegoPrincipal,
    )
  }

  const setPrincipal = (juego: Juego) => {
    onChange(juegos, juego)
  }

  const toggleRol = (juego: Juego, rol: string) => {
    const entry = juegos.find((j) => j.juego === juego)
    if (!entry) return
    const roles = entry.rolesPreferidos.includes(rol)
      ? entry.rolesPreferidos.filter((r) => r !== rol)
      : [...entry.rolesPreferidos, rol]
    updateJuego(juego, { rolesPreferidos: roles })
  }

  return (
    <div className="game-profiles-editor">
      <div className="game-profiles-picker">
        <span className="game-profiles-label">Tus juegos</span>
        <div className="game-profiles-chips">
          {JUEGOS.map((juego) => (
            <button
              key={juego}
              type="button"
              className={`game-profile-chip ${selected.has(juego) ? 'active' : ''}`}
              onClick={() => toggleJuego(juego)}
            >
              {juego}
            </button>
          ))}
        </div>
        <p className="hint">Podés jugar más de un título. Elegí al menos uno.</p>
      </div>

      <div className="game-profiles-list">
        {juegos.map((entry) => (
          <article
            key={entry.juego}
            className={`game-profile-card ${entry.juego === juegoPrincipal ? 'primary' : ''}`}
          >
            <header className="game-profile-card-header">
              <div>
                <h3>{entry.juego}</h3>
                <p className="game-profile-rank-preview">{getRangoLabel(entry.juego, entry.rango)}</p>
              </div>
              <button
                type="button"
                className={`game-profile-primary-btn ${entry.juego === juegoPrincipal ? 'active' : ''}`}
                onClick={() => setPrincipal(entry.juego)}
                title="Marcar como juego principal"
              >
                {entry.juego === juegoPrincipal ? '★ Principal' : 'Marcar principal'}
              </button>
            </header>

            <div className="form-row">
              <RankSelects
                juego={entry.juego}
                mode="single"
                rango={entry.rango}
                onRangoChange={(rango) => updateJuego(entry.juego, { rango })}
              />
            </div>

            <div className="game-profile-roles">
              <span className="game-profile-roles-label">Roles preferidos</span>
              <div className="roles-grid">
                {getRolesForJuego(entry.juego).map((rol) => (
                  <label key={rol.value} className="role-chip">
                    <input
                      type="checkbox"
                      checked={entry.rolesPreferidos.includes(rol.value)}
                      onChange={() => toggleRol(entry.juego, rol.value)}
                    />
                    {rol.label}
                  </label>
                ))}
              </div>
            </div>
          </article>
        ))}
      </div>
    </div>
  )
}
