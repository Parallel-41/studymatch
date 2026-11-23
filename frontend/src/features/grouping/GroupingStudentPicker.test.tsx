import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { GroupingEligibleStudent } from '../../api/types'
import GroupingStudentPicker from './GroupingStudentPicker'

const students: GroupingEligibleStudent[] = [
  {
    id: 1,
    number: 'S-001',
    name: 'Ana Ribeiro',
    email: 'ana@example.pt',
    currentYear: 3,
    coldStart: false,
    profile: {
      areas: [{ code: 'PROG', name: 'Programming', level: 78, confidence: 0.72 }],
      competencies: [],
    },
  },
  {
    id: 2,
    number: 'S-002',
    name: 'Carla Costa',
    email: 'carla@example.pt',
    currentYear: 1,
    coldStart: true,
    profile: {
      areas: [{ code: 'SOFT', name: 'Soft skills', level: 71, confidence: 0 }],
      competencies: [],
    },
  },
]

describe('GroupingStudentPicker', () => {
  it('searches student identity and shows profile and cold-start summaries', async () => {
    const user = userEvent.setup()
    render(<GroupingStudentPicker students={students} selectedStudentIds={[]} onChange={vi.fn()} />)

    expect(screen.getByText('PROG 78')).toBeInTheDocument()
    expect(screen.getByText('Cold start')).toBeInTheDocument()
    await user.type(screen.getByRole('searchbox', { name: 'Search students' }), 'S-002')

    expect(screen.getByText('Carla Costa')).toBeInTheDocument()
    expect(screen.queryByText('Ana Ribeiro')).not.toBeInTheDocument()
  })

  it('adds and removes a selected student', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()
    const { rerender } = render(
      <GroupingStudentPicker students={students} selectedStudentIds={[]} onChange={onChange} />,
    )

    await user.click(screen.getByRole('checkbox', { name: 'Select Ana Ribeiro' }))
    expect(onChange).toHaveBeenLastCalledWith([1])

    rerender(<GroupingStudentPicker students={students} selectedStudentIds={[1]} onChange={onChange} />)
    await user.click(screen.getByRole('checkbox', { name: 'Select Ana Ribeiro' }))
    expect(onChange).toHaveBeenLastCalledWith([])
  })
})