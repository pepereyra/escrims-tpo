export const JUEGOS = ['Valorant', 'LoL', 'CS2'] as const
export type Juego = (typeof JUEGOS)[number]

export const REGIONES = [
  { value: 'SA', label: 'Sudamérica (SA)' },
  { value: 'BR', label: 'Brasil (BR)' },
  { value: 'NA', label: 'Norteamérica (NA)' },
  { value: 'EU', label: 'Europa (EU)' },
] as const

export const FORMATOS_POR_JUEGO: Record<Juego, readonly string[]> = {
  Valorant: ['5v5', '2v2', '1v1'],
  LoL: ['5v5', '3v3'],
  CS2: ['5v5', '2v2', '1v1'],
}

export const DEFAULT_JUEGO: Juego = 'Valorant'
export const DEFAULT_REGION = 'SA'

export function getRegionLabel(value: string): string {
  return REGIONES.find((r) => r.value === value)?.label ?? value
}

export function getRegionShortLabel(value: string): string {
  return REGIONES.find((r) => r.value === value)?.value ?? value
}

export function isJuego(value: string): value is Juego {
  return (JUEGOS as readonly string[]).includes(value)
}

export function getFormatos(juego: string): string[] {
  if (isJuego(juego)) return [...FORMATOS_POR_JUEGO[juego]]
  return ['5v5']
}

export function getDefaultFormato(juego: string): string {
  return getFormatos(juego)[0]
}

export function normalizeFormato(juego: string, formato: string): string {
  const formatos = getFormatos(juego)
  return formatos.includes(formato) ? formato : formatos[0]
}
