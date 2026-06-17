import {
  DIAS_DISPONIBLES,
  encodeDisponibilidad,
  FRANJAS_DISPONIBLES,
  type DisponibilidadState,
  type DiaDisponible,
  type FranjaId,
} from '../utils/availability'

interface AvailabilityPickerProps {
  value: DisponibilidadState
  onChange: (value: DisponibilidadState) => void
}

export function AvailabilityPicker({ value, onChange }: AvailabilityPickerProps) {
  const toggleDia = (dia: DiaDisponible) => {
    const dias = value.dias.includes(dia)
      ? value.dias.filter((d) => d !== dia)
      : [...value.dias, dia]
    onChange({ ...value, dias })
  }

  const toggleFranja = (franja: FranjaId) => {
    const franjas = value.franjas.includes(franja)
      ? value.franjas.filter((f) => f !== franja)
      : [...value.franjas, franja]
    onChange({ ...value, franjas })
  }

  const preview = encodeDisponibilidad(value)

  return (
    <div className="availability-picker">
      <div className="availability-section">
        <span className="availability-label">Días</span>
        <div className="availability-days">
          {DIAS_DISPONIBLES.map((dia) => (
            <button
              key={dia}
              type="button"
              className={`availability-chip ${value.dias.includes(dia) ? 'active' : ''}`}
              onClick={() => toggleDia(dia)}
            >
              {dia}
            </button>
          ))}
        </div>
      </div>

      <div className="availability-section">
        <span className="availability-label">Horarios</span>
        <div className="availability-slots">
          {FRANJAS_DISPONIBLES.map((franja) => (
            <button
              key={franja.id}
              type="button"
              className={`availability-slot ${value.franjas.includes(franja.id) ? 'active' : ''}`}
              onClick={() => toggleFranja(franja.id)}
            >
              <span className="availability-slot-label">{franja.label}</span>
              <span className="availability-slot-hint">{franja.hint}</span>
            </button>
          ))}
        </div>
      </div>

      {preview && <p className="availability-preview">Resumen: {preview}</p>}
    </div>
  )
}
