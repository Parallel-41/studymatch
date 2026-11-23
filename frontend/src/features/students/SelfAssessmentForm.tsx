import React, { useState, useEffect } from 'react'
import { api } from '../../api/client'
import type { SelfAssessmentData, StudentProfileData } from '../../api/types'

type Props = {
  studentId: number
  onSaved?: (profile: StudentProfileData) => void
}

const AREAS = [
  { code: 'PROG', label: 'Programación' },
  { code: 'DB', label: 'Bases de Datos' },
  { code: 'WEB', label: 'Desarrollo Web' },
  { code: 'QUAL', label: 'Calidad de Software' },
  { code: 'MATH', label: 'Matemáticas' },
  { code: 'SOFT', label: 'Soft Skills' }
]

export function SelfAssessmentForm({ studentId, onSaved }: Props) {
  const [levels, setLevels] = useState<Record<string, number>>({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.getSelfAssessment(studentId)
      .then(data => {
        const initialLevels = { ...data.levels }
        AREAS.forEach(area => {
          if (initialLevels[area.code] === undefined) {
            initialLevels[area.code] = 50
          }
        })
        setLevels(initialLevels)
        setLoading(false)
      })
      .catch(err => {
        setError(err instanceof Error ? err.message : 'Error al cargar')
        setLoading(false)
      })
  }, [studentId])

  const handleSliderChange = (code: string, value: number) => {
    // Añadido el tipo explícito para prev
    setLevels((prev: Record<string, number>) => ({ ...prev, [code]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError(null)

    try {
      const payload: SelfAssessmentData = { levels }
      await api.saveSelfAssessment(studentId, payload)

      const updatedProfile = await api.studentProfile(studentId)
      if (onSaved) {
        onSaved(updatedProfile)
      }
      alert('¡Autoevaluación guardada con éxito!')
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Error al guardar')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <p>Cargando formulario...</p>

  return (
    <form onSubmit={handleSubmit} style={{ maxWidth: '400px', margin: '0 auto' }}>
      <h3>Tu Autoevaluación Inicial</h3>
      <p>Valora tu nivel de confianza (0-100) en las siguientes áreas:</p>

      {error && <div style={{ color: 'red', marginBottom: '1rem' }}>{error}</div>}

      {AREAS.map(area => (
        <div key={area.code} style={{ marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <label htmlFor={area.code}><strong>{area.label}</strong></label>
            <span>{levels[area.code] ?? 50}</span>
          </div>
          <input
            id={area.code}
            type="range"
            min="0"
            max="100"
            value={levels[area.code] ?? 50}
            // Añadido el tipo explícito para e
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleSliderChange(area.code, Number(e.target.value))}
            style={{ width: '100%' }}
          />
        </div>
      ))}

      <button type="submit" disabled={saving} style={{ width: '100%', padding: '10px' }}>
        {saving ? 'Guardando...' : 'Guardar y Ver Perfil'}
      </button>
    </form>
  )
}
