import { useCallback, useEffect, useState } from 'react'
import { auditApi } from '../api'
import type { AuditLog } from '../api/types'
import { formatFecha } from '../utils/format'

export function AuditPage() {
  const [logs, setLogs] = useState<AuditLog[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setLogs(await auditApi.list())
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar auditoría')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  return (
    <div className="page">
      <div className="page-header">
        <h1>Auditoría</h1>
        <p className="page-subtitle">Registro de acciones del sistema (solo ADMIN)</p>
      </div>

      {loading && <p className="page-loading">Cargando logs...</p>}
      {error && <p className="form-error">{error}</p>}

      {!loading && !error && logs.length === 0 && (
        <div className="empty-state">
          <h3>Sin registros</h3>
          <p>No hay eventos de auditoría todavía.</p>
        </div>
      )}

      {!loading && logs.length > 0 && (
        <div className="audit-table-wrap">
          <table className="audit-table">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Actor</th>
                <th>Acción</th>
                <th>Entidad</th>
                <th>Detalle</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td>{formatFecha(log.fecha)}</td>
                  <td>{log.actor}</td>
                  <td>
                    <code>{log.accion}</code>
                  </td>
                  <td>
                    {log.entidadTipo}
                    <span className="hint"> / {log.entidadId}</span>
                  </td>
                  <td>{log.detalle}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
