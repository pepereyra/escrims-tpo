import { isJuego, type Juego } from './options'

export interface RankOption {
  value: number
  label: string
}

export const RANGOS_POR_JUEGO: Record<Juego, readonly RankOption[]> = {
  Valorant: [
    { value: 0, label: 'Hierro' },
    { value: 300, label: 'Bronce' },
    { value: 600, label: 'Plata' },
    { value: 900, label: 'Oro' },
    { value: 1200, label: 'Platino' },
    { value: 1500, label: 'Diamante' },
    { value: 1800, label: 'Ascendente' },
    { value: 2100, label: 'Inmortal' },
    { value: 2400, label: 'Radiante' },
  ],
  LoL: [
    { value: 0, label: 'Hierro' },
    { value: 400, label: 'Bronce' },
    { value: 800, label: 'Plata' },
    { value: 1200, label: 'Oro' },
    { value: 1600, label: 'Platino' },
    { value: 2000, label: 'Esmeralda' },
    { value: 2400, label: 'Diamante' },
    { value: 2800, label: 'Maestría' },
    { value: 3200, label: 'Gran Maestro' },
    { value: 3600, label: 'Challenger' },
  ],
  CS2: [
    { value: 100, label: 'Faceit Nivel 1' },
    { value: 200, label: 'Faceit Nivel 2' },
    { value: 300, label: 'Faceit Nivel 3' },
    { value: 400, label: 'Faceit Nivel 4' },
    { value: 500, label: 'Faceit Nivel 5' },
    { value: 600, label: 'Faceit Nivel 6' },
    { value: 700, label: 'Faceit Nivel 7' },
    { value: 800, label: 'Faceit Nivel 8' },
    { value: 900, label: 'Faceit Nivel 9' },
    { value: 1000, label: 'Faceit Nivel 10' },
  ],
}

export function getRangos(juego: string): RankOption[] {
  if (isJuego(juego)) return [...RANGOS_POR_JUEGO[juego]]
  return [...RANGOS_POR_JUEGO.Valorant]
}

export function getRangoLabel(juego: string, value: number): string {
  const rangos = getRangos(juego)
  const exact = rangos.find((r) => r.value === value)
  if (exact) return exact.label
  return nearestRango(juego, value).label
}

export function formatRangoRange(juego: string, rangoMin: number, rangoMax: number): string {
  const minLabel = getRangoLabel(juego, rangoMin)
  const maxLabel = getRangoLabel(juego, rangoMax)
  if (rangoMin === rangoMax) return minLabel
  return `${minLabel} – ${maxLabel}`
}

export function nearestRango(juego: string, value: number): RankOption {
  const rangos = getRangos(juego)
  return rangos.reduce((best, current) =>
    Math.abs(current.value - value) < Math.abs(best.value - value) ? current : best,
  )
}

export function getDefaultRango(juego: Juego): number {
  const rangos = RANGOS_POR_JUEGO[juego]
  const mid = Math.floor(rangos.length / 2)
  return rangos[mid].value
}

export function getDefaultRangoRange(juego: Juego): { rangoMin: number; rangoMax: number } {
  const rangos = RANGOS_POR_JUEGO[juego]
  const mid = Math.floor(rangos.length / 2)
  const minIdx = Math.max(0, mid - 1)
  const maxIdx = Math.min(rangos.length - 1, mid + 1)
  return { rangoMin: rangos[minIdx].value, rangoMax: rangos[maxIdx].value }
}

export function getRangoRangeAround(juego: string, rango: number): { rangoMin: number; rangoMax: number } {
  const rangos = getRangos(juego)
  const nearest = nearestRango(juego, rango)
  const idx = rangos.findIndex((r) => r.value === nearest.value)
  const minIdx = Math.max(0, idx - 1)
  const maxIdx = Math.min(rangos.length - 1, idx + 1)
  return { rangoMin: rangos[minIdx].value, rangoMax: rangos[maxIdx].value }
}

export function normalizeRangoRange(
  juego: string,
  rangoMin: number,
  rangoMax: number,
): { rangoMin: number; rangoMax: number } {
  const rangos = getRangos(juego)
  const minRank = nearestRango(juego, rangoMin)
  const maxRank = nearestRango(juego, rangoMax)
  const minIdx = rangos.findIndex((r) => r.value === minRank.value)
  let maxIdx = rangos.findIndex((r) => r.value === maxRank.value)
  if (maxIdx <= minIdx) maxIdx = Math.min(rangos.length - 1, minIdx + 1)
  return { rangoMin: rangos[minIdx].value, rangoMax: rangos[maxIdx].value }
}

export function nextRangoValue(juego: string, value: number): number {
  const rangos = getRangos(juego)
  const idx = rangos.findIndex((r) => r.value === value)
  if (idx < 0 || idx >= rangos.length - 1) return value
  return rangos[idx + 1].value
}
