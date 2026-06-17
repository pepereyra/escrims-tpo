import { Link } from 'react-router-dom'
import type { Scrim } from '../api/types'
import { getGameLogoPath } from '../constants/gameLogos'
import { isJuego } from '../constants/options'
import { formatRangoRange } from '../constants/ranks'
import { formatFecha } from '../utils/format'
import { StatusBadge } from './StatusBadge'

const GAME_STYLES: Record<string, { color: string }> = {
  Valorant: { color: '#FF4655' },
  LoL: { color: '#4d8dff' },
  CS2: { color: '#e8b923' },
}

function getGameStyle(juego: string) {
  if (isJuego(juego)) return GAME_STYLES[juego]
  return { color: 'var(--primary)' }
}

export function ScrimCard({ scrim }: { scrim: Scrim }) {
  const ocupados = scrim.cuposTotales - scrim.cuposDisponibles
  const fillPct = scrim.cuposTotales > 0 ? (ocupados / scrim.cuposTotales) * 100 : 0
  const isFull = scrim.cuposDisponibles === 0
  const game = getGameStyle(scrim.juego)
  const logoPath = getGameLogoPath(scrim.juego)
  const fecha = formatFecha(scrim.fechaHora)
  const rangoLabel = formatRangoRange(scrim.juego, scrim.rangoMin, scrim.rangoMax)

  return (
    <Link
      to={`/scrims/${scrim.id}`}
      className="scrim-card"
      style={{ '--scrim-game-color': game.color } as React.CSSProperties}
    >
      <div className="scrim-card-accent" />
      <div className="scrim-card-body">
        <div className="scrim-card-top">
          <div className="scrim-card-game">
            <span className="scrim-game-icon" aria-hidden="true">
              {logoPath ? (
                <svg viewBox="0 0 24 24" focusable="false">
                  <path fill={game.color} d={logoPath} />
                </svg>
              ) : (
                <span className="scrim-game-icon-fallback">{scrim.juego.slice(0, 3).toUpperCase()}</span>
              )}
            </span>
            <div>
              <span className="scrim-game-tag">{scrim.juego}</span>
              <h3>{scrim.formato}</h3>
            </div>
          </div>
          <StatusBadge estado={scrim.estado} />
        </div>

        <div className="scrim-card-pills">
          <span className="scrim-pill">{scrim.region}</span>
          <span className="scrim-pill">{scrim.modalidad}</span>
          <span className="scrim-pill scrim-pill-rank" title={`Rango ${rangoLabel}`}>
            {rangoLabel}
          </span>
        </div>

        <div className="scrim-card-cupos">
          <div className="scrim-cupos-header">
            <span className="scrim-cupos-label">Cupos</span>
            <span className={`scrim-cupos-value${isFull ? ' full' : ''}`}>
              {ocupados}/{scrim.cuposTotales}
              {isFull ? ' · Lleno' : ` · ${scrim.cuposDisponibles} libres`}
            </span>
          </div>
          <div className="scrim-cupos-bar" aria-hidden="true">
            <span className="scrim-cupos-fill" style={{ width: `${fillPct}%` }} />
          </div>
        </div>

        <div className="scrim-card-bottom">
          <span className="scrim-fecha">{fecha}</span>
          <span className="scrim-card-link">Ver scrim →</span>
        </div>
      </div>
    </Link>
  )
}
