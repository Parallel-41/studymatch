import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import { groupAttempts } from './studentHistoryUtils'
import StudentHistory from './StudentHistory'

describe('StudentHistory', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('groups attempts by academic year and semester and finds each best grade', () => {
    const groups = groupAttempts([
      { id: 1, course: 'Algorithms', academicYear: '2025/2026', semester: 1, grade: 14 },
      { id: 2, course: 'Databases', academicYear: '2025/2026', semester: 1, grade: 18 },
      { id: 3, course: 'Networks', academicYear: '2025/2026', semester: 2, grade: 16 },
    ])

    expect(groups).toHaveLength(2)
    expect(groups[0]).toMatchObject({ academicYear: '2025/2026', semester: 2, bestGrade: 16 })
    expect(groups[1]).toMatchObject({ academicYear: '2025/2026', semester: 1, bestGrade: 18 })
    expect(groups[1].attempts).toHaveLength(2)
  })

  it('renders the empty state when a student has no attempts', async () => {
    vi.spyOn(api, 'studentAttempts').mockResolvedValue([])

    render(<StudentHistory studentId={1} />)

    expect(await screen.findByText('No academic attempts recorded.')).toBeInTheDocument()
  })
})