import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import { groupAttempts } from './studentHistoryUtils'
import StudentHistory from './StudentHistory'

const units = [
  { code: 'DB1', name: 'Bases de Dados I', curricularYear: 2, semester: 1, ects: 6 },
  { code: 'PROG1', name: 'Programação I', curricularYear: 1, semester: 1, ects: 6 },
]

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
    vi.spyOn(api, 'courseUnits').mockResolvedValue(units)

    render(<StudentHistory studentId={1} />)

    expect(await screen.findByText('No academic attempts recorded.')).toBeInTheDocument()
  })

  it('records an improvement for the chosen unit and reloads the history', async () => {
    const user = userEvent.setup()
    const history = vi.spyOn(api, 'studentAttempts')
      .mockResolvedValueOnce([{ id: 1, course: 'DB1 Bases de Dados I (NORMAL)', academicYear: '2024/25', semester: 1, grade: 14 }])
      .mockResolvedValueOnce([
        { id: 1, course: 'DB1 Bases de Dados I (NORMAL)', academicYear: '2024/25', semester: 1, grade: 14 },
        { id: 2, course: 'DB1 Bases de Dados I (MELHORIA)', academicYear: '2024/25', semester: 1, grade: 18 },
      ])
    vi.spyOn(api, 'courseUnits').mockResolvedValue(units)
    const add = vi.spyOn(api, 'addStudentAttempt').mockResolvedValue(
      { id: 99, course: 'DB1', academicYear: '2024/25', semester: 1, grade: 18 },
    )

    render(<StudentHistory studentId={7} />)

    expect(await screen.findByRole('option', { name: /DB1 — Bases de Dados I/ })).toBeInTheDocument()
    await user.clear(screen.getByLabelText('Academic year'))
    await user.type(screen.getByLabelText('Academic year'), '2024/25')
    await user.selectOptions(screen.getByLabelText('Season'), 'MELHORIA')
    await user.type(screen.getByLabelText('Grade'), '18')
    await user.click(screen.getByRole('button', { name: 'Add attempt' }))

    expect(add).toHaveBeenCalledWith(7, {
      course: 'DB1', academicYear: '2024/25', semester: 1, grade: 18, season: 'MELHORIA',
    })
    expect(await screen.findByText('Attempt added.')).toBeInTheDocument()
    expect(history).toHaveBeenCalledTimes(2)
    expect(await screen.findByText('DB1 Bases de Dados I (MELHORIA)')).toBeInTheDocument()
  })

  it('rejects grade 21 without calling the API (boundary)', async () => {
    const user = userEvent.setup()
    vi.spyOn(api, 'studentAttempts').mockResolvedValue([])
    vi.spyOn(api, 'courseUnits').mockResolvedValue(units)
    const add = vi.spyOn(api, 'addStudentAttempt')

    render(<StudentHistory studentId={1} />)

    await screen.findByRole('option', { name: /PROG1/ })
    await user.type(screen.getByLabelText('Grade'), '21')
    await user.click(screen.getByRole('button', { name: 'Add attempt' }))

    expect(screen.getByRole('alert')).toHaveTextContent('whole grade from 0 to 20')
    expect(add).not.toHaveBeenCalled()
  })

  it('shows the API message when the season already exists', async () => {
    const user = userEvent.setup()
    vi.spyOn(api, 'studentAttempts').mockResolvedValue([])
    vi.spyOn(api, 'courseUnits').mockResolvedValue(units)
    vi.spyOn(api, 'addStudentAttempt').mockRejectedValue(new Error('The request conflicts with existing data. (409)'))

    render(<StudentHistory studentId={1} />)

    await screen.findByRole('option', { name: /PROG1/ })
    await user.type(screen.getByLabelText('Grade'), '12')
    await user.click(screen.getByRole('button', { name: 'Add attempt' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('conflicts with existing data')
  })
})
