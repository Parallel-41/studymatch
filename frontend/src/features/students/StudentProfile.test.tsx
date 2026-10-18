import { fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import StudentProfile from './StudentProfile'

describe('StudentProfile', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows course contributions and a separate self-assessment share for a selected competency', async () => {
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

    fireEvent.click(screen.getByRole('button', { name: 'Programming' }))

    expect(explanationRequest).toHaveBeenCalledWith(1, 'programming')
    expect(await screen.findByRole('heading', { name: 'Programming explanation' })).toBeInTheDocument()
    expect(screen.getByRole('table', { name: 'Course contribution breakdown' })).toHaveTextContent('Algorithms')
    expect(screen.getByRole('region', { name: 'Self-assessment share' })).toHaveTextContent('35%')
    expect(screen.getByText('3.6')).toBeInTheDocument()
  })
})