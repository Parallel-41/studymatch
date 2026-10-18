import { fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import StudentList from './StudentList'

describe('StudentList', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders rows for loaded students', async () => {
    vi.spyOn(api, 'students').mockResolvedValue([
      {
        id: 1,
        number: 'a20240001',
        name: 'Ana Ribeiro',
        email: 'ana.ribeiro@example.pt',
        currentYear: 3,
      },
    ])

    render(<StudentList />)

    expect(await screen.findByRole('cell', { name: 'a20240001' })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: 'Ana Ribeiro' })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: 'ana.ribeiro@example.pt' })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: '3' })).toBeInTheDocument()
  })

  it('renders the empty state when there are no students', async () => {
    vi.spyOn(api, 'students').mockResolvedValue([])

    render(<StudentList />)

    expect(await screen.findByText('No students yet.')).toBeInTheDocument()
  })

  it('renders the real error message when the request fails', async () => {
    vi.spyOn(api, 'students').mockRejectedValue(new Error('Service unavailable (503)'))

    render(<StudentList />)

    expect(await screen.findByRole('alert')).toHaveTextContent('Service unavailable (503)')
  })

  it('opens a student profile and loads an explanation when a competency is selected', async () => {
    vi.spyOn(api, 'students').mockResolvedValue([
      {
        id: 1,
        number: 'a20240001',
        name: 'Ana Ribeiro',
        email: 'ana.ribeiro@example.pt',
        currentYear: 3,
      },
    ])
    const explanationRequest = vi.spyOn(api, 'competencyExplanation').mockResolvedValue({
      level: 3.6,
      contributions: [
        { courseUnit: 'Algorithms', grade: 16, weight: 0.45, recencyFactor: 0.9, percentageOfLevel: 65 },
      ],
      selfAssessmentShare: 35,
    })
    vi.spyOn(api, 'studentProfile').mockResolvedValue({
      areas: [{ code: 'technical', name: 'Technical', level: 76 }, { code: 'analytical', name: 'Analytical', level: 84 }, { code: 'collaboration', name: 'Collaboration', level: 58 }],
      competencies: [{ code: 'programming', name: 'Programming', areaCode: 'technical', level: 76, confidence: 'HIGH' }],
    })

    render(<StudentList />)

    fireEvent.click(await screen.findByRole('button', { name: 'Profile' }))
    expect(await screen.findByRole('region', { name: 'Ana Ribeiro profile' })).toBeInTheDocument()
    fireEvent.click(await screen.findByRole('button', { name: 'Explain' }))

    expect(await screen.findByRole('heading', { name: 'Programming explanation' })).toBeInTheDocument()
    expect(explanationRequest).toHaveBeenCalledWith(1, 'programming')
  })
})
