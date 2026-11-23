import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { SelfAssessmentForm } from './SelfAssessmentForm'
import { api } from '../../api/client'

// Hacemos un mock de la API
vi.mock('../../api/client', () => ({
  api: {
    getSelfAssessment: vi.fn(),
    saveSelfAssessment: vi.fn(),
    studentProfile: vi.fn(),
  }
}))

describe('SelfAssessmentForm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renderiza correctamente el formulario y guarda los datos (happy path)', async () => {
    // Simulamos que el backend nos devuelve datos vacíos al principio
    vi.mocked(api.getSelfAssessment).mockResolvedValue({ levels: {} })
    vi.mocked(api.saveSelfAssessment).mockResolvedValue({ levels: { 'PROG': 80 } })
    vi.mocked(api.studentProfile).mockResolvedValue({ areas: [], competencies: [] })

    const onSavedMock = vi.fn()
    render(<SelfAssessmentForm studentId={1} onSaved={onSavedMock} />)

    // Esperamos a que termine de cargar
    await waitFor(() => {
      expect(screen.queryByText('Cargando formulario...')).toBeNull()
    })

    // Comprobamos que el slider de Programación está en 50 por defecto
    const progSlider = screen.getByLabelText('Programación')
    expect(progSlider).toBeDefined()

    // Cambiamos el valor del slider
    fireEvent.change(progSlider, { target: { value: '80' } })

    // Enviamos el formulario
    const submitButton = screen.getByText('Guardar y Ver Perfil')
    fireEvent.click(submitButton)

    // Verificamos que se llamó a la API para guardar
    await waitFor(() => {
      expect(api.saveSelfAssessment).toHaveBeenCalledWith(1, {
        levels: expect.objectContaining({ 'PROG': 80 })
      })
      expect(onSavedMock).toHaveBeenCalled()
    })
  })

  it('muestra un mensaje de error si falla la carga', async () => {
    // Simulamos un error de red o del backend
    vi.mocked(api.getSelfAssessment).mockRejectedValue(new Error('Fallo en el servidor'))

    render(<SelfAssessmentForm studentId={1} />)

    // Esperamos a que se muestre el error en pantalla
    await waitFor(() => {
      expect(screen.getByText('Fallo en el servidor')).toBeDefined()
    })
  })
})
