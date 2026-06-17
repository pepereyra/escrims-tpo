export interface Usuario {
  id: string
  username: string
  email: string
  region: string
  juegoPrincipal: string
  rangoPrincipal: number
  rangosPorJuego?: Record<string, number>
  latenciaPromedio: number
  rolesPreferidos: string[]
  disponibilidad: string
  verificado: boolean
  rolSistema: 'USER' | 'MOD' | 'ADMIN'
  strikes: number
  cooldownHasta: string | null
}

export interface AuthResponse {
  token: string
  usuario: Usuario
}

export interface Postulacion {
  username: string
  rol: string
  estado: string
}

export interface ParticipanteScrim {
  username: string
  rol: string
  estadoPostulacion: string
  confirmado: boolean
  fechaConfirmacion: string | null
  rangoEnJuego: number
  latenciaPromedio: number
}

export interface EquipoScrim {
  lado: string
  jugadores: ParticipanteScrim[]
}

export interface Scrim {
  id: string
  juego: string
  formato: string
  region: string
  estado: string
  cuposTotales: number
  modalidad: string
  cuposDisponibles: number
  rangoMin: number
  rangoMax: number
  fechaHora: string
  equipos: EquipoScrim[]
  postulaciones: Postulacion[]
}

export interface Feedback {
  id: string
  scrimId: string
  autor: string
  destinatario: string
  rating: number
  comentario: string
  estado: string
  fechaCreacion: string
}

export interface Reporte {
  id: string
  scrimId: string
  reportante: string
  reportado: string
  motivo: string
  estado: string
  sancion: string | null
  etapaResolucion: string
  strikesReportado: number
  fechaCreacion: string
}

export interface BusquedaFavorita {
  id: string
  username: string
  juego: string
  formato: string
  region: string
  rangoMin: number | null
  rangoMax: number | null
  latenciaMax: number | null
  fecha: string | null
  fechaCreacion: string
}

export interface AlertaBusqueda {
  id: string
  busquedaId: string
  username: string
  scrimId: string
  mensaje: string
  fechaCreacion: string
}

export interface ScrimFilters {
  juego?: string
  formato?: string
  region?: string
  rangoMin?: number
  rangoMax?: number
  latenciaMax?: number
  fecha?: string
}

export interface Estadistica {
  username: string
  kills: number
  deaths: number
  assists: number
  kda: number
  mvp: boolean
}

export interface AuditLog {
  id: string
  actor: string
  accion: string
  entidadTipo: string
  entidadId: string
  detalle: string
  fecha: string
}
