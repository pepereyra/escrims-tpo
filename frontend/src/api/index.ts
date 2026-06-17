import { api } from './client'
import type {
  AlertaBusqueda,
  AuditLog,
  AuthResponse,
  BusquedaFavorita,
  Estadistica,
  Feedback,
  Reporte,
  Scrim,
  ScrimFilters,
  Usuario,
} from './types'

export const authApi = {
  login: (username: string, password: string) =>
    api<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),

  register: (data: {
    username: string
    email: string
    password: string
    region: string
    juego: string
    rango: number
    latencia: number
    rolesPreferidos?: string[]
    disponibilidad?: string
  }) =>
    api<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  me: () => api<Usuario>('/auth/me'),

  updateProfile: (data: {
    region?: string
    juegoPrincipal?: string
    rango?: number
    rangosPorJuego?: Record<string, number>
    latencia?: number
    rolesPreferidos?: string[]
    disponibilidad?: string
  }) =>
    api<Usuario>('/auth/me/perfil', {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  verifyEmail: () =>
    api<Usuario>('/auth/me/verificar-email', { method: 'POST' }),
}

export const scrimsApi = {
  list: (filters: ScrimFilters = {}) => {
    const params = new URLSearchParams()
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== '') {
        params.set(key, String(value))
      }
    })
    const query = params.toString()
    return api<Scrim[]>(`/scrims${query ? `?${query}` : ''}`)
  },

  get: (id: string) => api<Scrim>(`/scrims/${id}`),

  create: (data: {
    juego: string
    formato: string
    region: string
    rangoMin: number
    rangoMax: number
    latenciaMax: number
    fechaHora: string
    duracionMinutos: number
    cuposTotales: number
    modalidad?: string
  }) =>
    api<Scrim>('/scrims', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  postular: (id: string, username: string, rol: string) =>
    api<Scrim>(`/scrims/${id}/postulaciones`, {
      method: 'POST',
      body: JSON.stringify({ username, rol }),
    }),

  confirmar: (id: string, username: string) =>
    api<Scrim>(`/scrims/${id}/confirmaciones`, {
      method: 'POST',
      body: JSON.stringify({ username }),
    }),

  iniciar: (id: string) =>
    api<Scrim>(`/scrims/${id}/iniciar`, { method: 'POST' }),

  finalizar: (id: string) =>
    api<Scrim>(`/scrims/${id}/finalizar`, { method: 'POST' }),

  cancelar: (id: string) =>
    api<Scrim>(`/scrims/${id}/cancelar`, { method: 'POST' }),

  cambiarRol: (id: string, username: string, nuevoRol: string) =>
    api<Scrim>(`/scrims/${id}/roles/cambiar`, {
      method: 'POST',
      body: JSON.stringify({ username, nuevoRol }),
    }),

  intercambiarRoles: (id: string, usernameA: string, usernameB: string) =>
    api<Scrim>(`/scrims/${id}/roles/intercambiar`, {
      method: 'POST',
      body: JSON.stringify({ usernameA, usernameB }),
    }),

  moverASuplente: (id: string, username: string) =>
    api<Scrim>(`/scrims/${id}/suplentes`, {
      method: 'POST',
      body: JSON.stringify({ username }),
    }),

  reactivarTitular: (id: string, username: string) =>
    api<Scrim>(`/scrims/${id}/suplentes/reactivar`, {
      method: 'POST',
      body: JSON.stringify({ username }),
    }),

  registrarEstadisticas: (
    id: string,
    resultados: { username: string; kills: number; deaths: number; assists: number }[],
  ) =>
    api<Estadistica[]>(`/scrims/${id}/estadisticas`, {
      method: 'POST',
      body: JSON.stringify({ resultados }),
    }),

  getIcal: (id: string) => api<{ scrimId: string; contenido: string }>(`/scrims/${id}/ical`),
}

export const notificacionesApi = {
  email: (usernames: string[]) =>
    api<void>('/notificaciones/email', {
      method: 'POST',
      body: JSON.stringify({ usernames }),
    }),

  push: (usernames: string[]) =>
    api<void>('/notificaciones/push', {
      method: 'POST',
      body: JSON.stringify({ usernames }),
    }),

  discord: (usernames: string[]) =>
    api<void>('/notificaciones/discord', {
      method: 'POST',
      body: JSON.stringify({ usernames }),
    }),
}

export const auditApi = {
  list: () => api<AuditLog[]>('/audit'),
}

export const feedbackApi = {
  list: (scrimId: string) => api<Feedback[]>(`/scrims/${scrimId}/feedback`),

  create: (
    scrimId: string,
    data: { autor: string; destinatario: string; rating: number; comentario: string },
  ) =>
    api<Feedback>(`/scrims/${scrimId}/feedback`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  aprobar: (id: string) =>
    api<Feedback>(`/feedback/${id}/aprobar`, { method: 'POST' }),

  rechazar: (id: string) =>
    api<Feedback>(`/feedback/${id}/rechazar`, { method: 'POST' }),
}

export const reportesApi = {
  list: (scrimId: string) => api<Reporte[]>(`/scrims/${scrimId}/reportes`),

  create: (
    scrimId: string,
    data: { reportante: string; reportado: string; motivo: string },
  ) =>
    api<Reporte>(`/scrims/${scrimId}/reportes`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  aprobar: (id: string, sancion?: string) =>
    api<Reporte>(`/reportes/${id}/aprobar`, {
      method: 'POST',
      body: JSON.stringify({ sancion: sancion ?? '' }),
    }),

  rechazar: (id: string) =>
    api<Reporte>(`/reportes/${id}/rechazar`, { method: 'POST' }),
}

export const favoritosApi = {
  list: () => api<BusquedaFavorita[]>('/busquedas-favoritas'),

  create: (data: {
    juego: string
    formato: string
    region: string
    rangoMin?: number
    rangoMax?: number
    latenciaMax?: number
    fecha?: string
  }) =>
    api<BusquedaFavorita>('/busquedas-favoritas', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  alertas: () => api<AlertaBusqueda[]>('/alertas'),
}
