import { isJuego, type Juego } from './options'

export interface RolOption {
  value: string
  label: string
}

export const ROLES_POR_JUEGO: Record<Juego, readonly RolOption[]> = {
  Valorant: [
    { value: 'DUELIST', label: 'Duelist' },
    { value: 'INITIATOR', label: 'Initiator' },
    { value: 'CONTROLLER', label: 'Controller' },
    { value: 'SENTINEL', label: 'Sentinel' },
    { value: 'SUPPORT', label: 'Support' },
  ],
  LoL: [
    { value: 'TOP', label: 'Top' },
    { value: 'JUNGLA', label: 'Jungla' },
    { value: 'MID', label: 'Mid' },
    { value: 'ADC', label: 'ADC' },
    { value: 'SUPPORT', label: 'Support' },
  ],
  CS2: [
    { value: 'AWPER', label: 'AWPer' },
    { value: 'ENTRY', label: 'Entry Fragger' },
    { value: 'IGL', label: 'IGL' },
    { value: 'LURKER', label: 'Lurker' },
    { value: 'SUPPORT', label: 'Support' },
  ],
}

export function getRolesForJuego(juego: string): RolOption[] {
  if (isJuego(juego)) return [...ROLES_POR_JUEGO[juego]]
  return [...ROLES_POR_JUEGO.Valorant]
}

export function getRolValuesForJuego(juego: string): string[] {
  return getRolesForJuego(juego).map((r) => r.value)
}

export function getRolLabel(juego: string, rol: string): string {
  return getRolesForJuego(juego).find((r) => r.value === rol)?.label ?? rol
}

export function filterRolesPreferidos(juego: string, roles: string[]): string[] {
  const valid = new Set(getRolValuesForJuego(juego))
  return roles.filter((r) => valid.has(r))
}
