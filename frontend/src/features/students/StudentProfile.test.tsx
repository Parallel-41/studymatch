import { fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import StudentProfile from './StudentProfile'

describe('StudentProfile', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows no data as distinct from a computed zero level', async () => {
    vi.spyOn(api, 'studentProfile').mockResolvedValue({
      areas: [
        { code: 'technical', name: 'Technical', level: null },
        { code: 'analytical', name: 'Analytical', level: null },
        { code: 'communication', name: 'Communication', level: null },
      ],
      competencies: [
        { code: 'programming', name: 'Programming', areaCode: 'technical', level: null, confidence: null },
      ],
    })

    render(
      <StudentProfile
        student={{ id: 1, number: 'a20240001', name: 'Ana Ribeiro', email: 'ana@example.pt', currentYear: 3 }}
      />,
    )

    expect(await screen.findAllByRole('cell', { name: 'No data' })).toHaveLength(2)
    expect(screen.getByText('No data for: Technical, Analytical, Communication')).toBeInTheDocument()
    expect(screen.queryByRole('cell', { name: '0' })).not.toBeInTheDocument()
  })

  it('renders a computed zero level and clearly labels low confidence', async () => {
    vi.spyOn(api, 'studentProfile').mockResolvedValue({
      areas: [
        { code: 'technical', name: 'Technical', level: 0 },
        { code: 'analytical', name: 'Analytical', level: 0 },
        { code: 'communication', name: 'Communication', level: 0 },
      ],
      competencies: [
        { code: 'programming', name: 'Programming', areaCode: 'technical', level: 0, confidence: 'LOW' },
      ],
    })

    render(
      <StudentProfile
        student={{ id: 1, number: 'a20240001', name: 'Ana Ribeiro', email: 'ana@example.pt', currentYear: 3 }}
      />,
    )

    expect(await screen.findByRole('cell', { name: '0' })).toBeInTheDocument()
    expect(screen.getByText('Low confidence')).toBeInTheDocument()
    expect(screen.getByRole('img', { name: /radar chart/i })).toBeInTheDocument()
  })

  it('shows course contributions and a separate self-assessment share for a selected competency', async () => {
    vi.spyOn(api, 'studentProfile').mockResolvedValue({
      areas: [{ code: 'technical', name: 'Technical', level: 76 }, { code: 'analytical', name: 'Analytical', level: 84 }, { code: 'collaboration', name: 'Collaboration', level: 58 }],
      competencies: [{ code: 'programming', name: 'Programming', areaCode: 'technical', level: 76, confidence: 'HIGH' }],
    })
    const explanation = {
      level: 3.6,
      contributions: [
        { courseUnit: 'Algorithms', grade: 16, weight: 0.45, recencyFactor: 0.9, percentageOfLevel: 35 },
        { courseUnit: 'Software Engineering', grade: 17, weight: 0.35, recencyFactor: 0.8, percentageOfLevel: 30 },
      ],
      selfAssessmentShare: 35,
    }
    const explanationRequest = vi.spyOn(api, 'competencyExplanation').mockResolvedValue(explanation)

    render(
      <StudentProfile
        student={{ id: 1, number: 'a20240001', name: 'Ana Ribeiro', email: 'ana@example.pt', currentYear: 3 }}
      />,
    )

    fireEvent.click(await screen.findByRole('button', { name: 'Explain' }))

    expect(explanationRequest).toHaveBeenCalledWith(1, 'programming')
    expect(await screen.findByRole('heading', { name: 'Programming explanation' })).toBeInTheDocument()
    expect(screen.getByRole('table', { name: 'Course contribution breakdown' })).toHaveTextContent('Algorithms')
    expect(screen.getByRole('region', { name: 'Self-assessment share' })).toHaveTextContent('35%')
    expect(screen.getByText('3.6')).toBeInTheDocument()
  })
})