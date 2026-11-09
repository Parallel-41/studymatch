import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import GroupingConstraints from './GroupingConstraints'

const participants = [
  { id: 1, number: 'S-1', name: 'Ana Ribeiro' },
  { id: 2, number: 'S-2', name: 'Bruno Silva' },
  { id: 3, number: 'S-3', name: 'Carla Costa' },
]

describe('GroupingConstraints', () => {
  it('adds and removes a readable participant rule', async () => {
    const user = userEvent.setup()
    const add = vi.spyOn(api, 'addGroupingConstraint').mockResolvedValue({
      id: 11,
      type: 'TOGETHER',
      studentIds: [1, 2],
    })
    const remove = vi.spyOn(api, 'removeGroupingConstraint').mockResolvedValue()
    vi.spyOn(api, 'groupingConstraints').mockResolvedValue([])

    render(<GroupingConstraints contextId={7} participants={participants} />)

    await screen.findByText('No participant rules yet.')
    await user.selectOptions(screen.getByLabelText('Participants'), ['1', '2'])
    await user.click(screen.getByRole('button', { name: 'Add rule' }))

    expect(add).toHaveBeenCalledWith(7, { type: 'TOGETHER', studentIds: [1, 2] })
    expect(await within(screen.getByLabelText('Current constraints')).findByText('Keep together')).toBeInTheDocument()
    expect(screen.getByText('Ana Ribeiro, Bruno Silva')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Remove keep together rule for Ana Ribeiro, Bruno Silva' }))

    expect(remove).toHaveBeenCalledWith(7, 11)
    expect(await screen.findByText('No participant rules yet.')).toBeInTheDocument()
  })

  it('shows API validation errors next to the form', async () => {
    const user = userEvent.setup()
    vi.spyOn(api, 'groupingConstraints').mockResolvedValue([])
    vi.spyOn(api, 'addGroupingConstraint').mockRejectedValue(new Error('Students must belong to this context.'))

    render(<GroupingConstraints contextId={7} participants={participants} />)

    await screen.findByText('No participant rules yet.')
    await user.selectOptions(screen.getByLabelText('Participants'), ['1', '2'])
    await user.click(screen.getByRole('button', { name: 'Add rule' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Students must belong to this context.')
    expect(screen.getByLabelText('Participants')).toHaveAttribute('aria-invalid', 'true')
  })

  it('requires at least two participants before submitting', async () => {
    const user = userEvent.setup()
    const add = vi.spyOn(api, 'addGroupingConstraint')
    vi.spyOn(api, 'groupingConstraints').mockResolvedValue([])

    render(<GroupingConstraints contextId={7} participants={participants} />)

    await screen.findByText('No participant rules yet.')
    await user.selectOptions(screen.getByLabelText('Participants'), '1')
    await user.click(screen.getByRole('button', { name: 'Add rule' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Choose at least two participants.')
    expect(add).not.toHaveBeenCalled()
  })
})