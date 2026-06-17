import { labelEstado } from '../utils/format'

const COLORS: Record<string, string> = {
  BUSCANDO: 'badge-blue',
  LOBBY_ARMADO: 'badge-purple',
  CONFIRMADO: 'badge-green',
  EN_JUEGO: 'badge-orange',
  FINALIZADO: 'badge-gray',
  CANCELADO: 'badge-red',
  PENDIENTE: 'badge-yellow',
  ACEPTADA: 'badge-green',
  RECHAZADA: 'badge-red',
  SUPLENTE: 'badge-purple',
  APROBADO: 'badge-green',
  RECHAZADO: 'badge-red',
}

export function StatusBadge({ estado }: { estado: string }) {
  return (
    <span className={`badge ${COLORS[estado] ?? 'badge-gray'}`}>
      {labelEstado(estado)}
    </span>
  )
}
