const ESTADO_LABELS: Record<string, string> = {
  BUSCANDO: 'Buscando jugadores',
  LOBBY_ARMADO: 'Lobby armado',
  CONFIRMADO: 'Confirmado',
  EN_JUEGO: 'En juego',
  FINALIZADO: 'Finalizado',
  CANCELADO: 'Cancelado',
  PENDIENTE: 'Pendiente',
  ACEPTADA: 'Aceptada',
  RECHAZADA: 'Rechazada',
  SUPLENTE: 'Suplente',
  APROBADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
}

export function labelEstado(estado: string): string {
  return ESTADO_LABELS[estado] ?? estado
}

export function formatFecha(iso: string): string {
  return new Date(iso).toLocaleString('es-AR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function toDatetimeLocal(iso?: string): string {
  const d = iso ? new Date(iso) : new Date(Date.now() + 3600000)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function fromDateInput(value: string): string {
  return `${value}T00:00:00`
}

export function fromDatetimeLocal(value: string): string {
  return new Date(value).toISOString().slice(0, 19)
}
