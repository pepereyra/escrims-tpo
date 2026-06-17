export const DIAS_DISPONIBLES = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'] as const

export const FRANJAS_DISPONIBLES = [
  { id: 'manana', label: 'Mañana', hint: '08–12h' },
  { id: 'tarde', label: 'Tarde', hint: '12–18h' },
  { id: 'noche', label: 'Noche', hint: '18–24h' },
  { id: 'madrugada', label: 'Madrugada', hint: '00–06h' },
] as const

export type DiaDisponible = (typeof DIAS_DISPONIBLES)[number]
export type FranjaId = (typeof FRANJAS_DISPONIBLES)[number]['id']

export interface DisponibilidadState {
  dias: DiaDisponible[]
  franjas: FranjaId[]
}

export function emptyDisponibilidad(): DisponibilidadState {
  return { dias: [], franjas: [] }
}

export function encodeDisponibilidad({ dias, franjas }: DisponibilidadState): string {
  if (dias.length === 0 && franjas.length === 0) return ''
  const diasStr = dias.join(', ')
  const franjaLabels = franjas
    .map((id) => FRANJAS_DISPONIBLES.find((f) => f.id === id)?.label)
    .filter(Boolean)
    .join(', ')
  if (!diasStr) return franjaLabels
  if (!franjaLabels) return diasStr
  return `${diasStr} · ${franjaLabels}`
}

export function decodeDisponibilidad(value: string): DisponibilidadState {
  if (!value?.trim()) return emptyDisponibilidad()

  const [diasPart, franjasPart] = value.split('·').map((s) => s.trim())
  const dias = DIAS_DISPONIBLES.filter((d) => diasPart?.includes(d))
  const franjas = FRANJAS_DISPONIBLES.filter((f) =>
    franjasPart?.toLowerCase().includes(f.label.toLowerCase()),
  ).map((f) => f.id)

  if (dias.length === 0 && franjas.length === 0) {
    const lower = value.toLowerCase()
    if (lower.includes('lunes') && lower.includes('viernes')) {
      return {
        dias: ['Lun', 'Mar', 'Mié', 'Jue', 'Vie'],
        franjas: lower.includes('noche') || lower.includes('20') ? ['noche'] : [],
      }
    }
    if (lower.includes('finde') || lower.includes('sáb') || lower.includes('sab')) {
      return { dias: ['Sáb', 'Dom'], franjas: [] }
    }
  }

  return { dias, franjas }
}
