import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import type { GroupingEligibleStudent } from '../../api/types'
import GroupingContexts from './GroupingContexts'

const student: GroupingEligibleStudent = {
  id: 8,
  number: 'S-008',
  name: 'Marta Lopes',
  email: 'marta@example.pt',
  currentYear: 1,
  coldStart: true,
  profile: {
    areas: [{ code: 'PROG', name: 'Programming', level: null, confidence: 0 }],
    competencies: [],
  },
}

const context = {
  id: 4,
  name: 'Software project teams',
  description: 'Project groups.',
  createdAt: '2026-11-08T10:35:00Z',
  studentIds: [],
}

describe('GroupingContexts', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('lists contexts and shows the selected context student picker', async () => {
    vi.spyOn(api, 'groupingContexts').mockResolvedValue([context])
    vi.spyOn(api, 'groupingContext').mockResolvedValue(context)
    vi.spyOn(api, 'groupingStudents').mockResolvedValue([student])

    render(<GroupingContexts />)

    expect(await screen.findByRole('heading', { name: context.name })).toBeInTheDocument()
    expect(screen.getByText('Cold start')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save participants' })).toBeDisabled()
  })

  it('creates a context, selects a participant, and saves the selection', async () => {
    const user = userEvent.setup()
    vi.spyOn(api, 'groupingContexts').mockResolvedValue([])
    vi.spyOn(api, 'groupingStudents').mockResolvedValue([student])
    const create = vi.spyOn(api, 'createGroupingContext').mockResolvedValue(context)
    const save = vi.spyOn(api, 'saveGroupingParticipants').mockResolvedValue({ ...context, studentIds: [8] })

    render(<GroupingContexts />)

    await user.click(await screen.findByRole('button', { name: 'New context' }))
    await user.type(screen.getByLabelText('Context name'), context.name)
    await user.type(screen.getByLabelText('Context description'), context.description)
    await user.click(screen.getByRole('button', { name: 'Create context' }))

    expect(create).toHaveBeenCalledWith({ name: context.name, description: context.description })
    await user.click(await screen.findByRole('checkbox', { name: 'Select Marta Lopes' }))
    await user.click(screen.getByRole('button', { name: 'Save participants' }))

    expect(save).toHaveBeenCalledWith(context.id, [8])
    expect(await screen.findByText('1 participants')).toBeInTheDocument()
  })
})