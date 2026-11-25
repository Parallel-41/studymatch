import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import GroupingContextForm from './GroupingContextForm'

describe('GroupingContextForm', () => {
  it('submits a trimmed name and description', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    render(<GroupingContextForm onSubmit={onSubmit} onCancel={vi.fn()} />)

    await user.type(screen.getByLabelText('Context name'), '  Semester teams  ')
    await user.type(screen.getByLabelText('Context description'), '  Final project  ')
    await user.click(screen.getByRole('button', { name: 'Create context' }))

    expect(onSubmit).toHaveBeenCalledWith({ name: 'Semester teams', description: 'Final project' })
  })

  it('requires a context name', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn()
    render(<GroupingContextForm onSubmit={onSubmit} onCancel={vi.fn()} />)

    await user.click(screen.getByRole('button', { name: 'Create context' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Enter a context name.')
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('shows an API error without closing the form', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn().mockRejectedValue(new Error('A context with this name already exists.'))
    render(<GroupingContextForm onSubmit={onSubmit} onCancel={vi.fn()} />)

    await user.type(screen.getByLabelText('Context name'), 'Teams')
    await user.click(screen.getByRole('button', { name: 'Create context' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('A context with this name already exists.')
    expect(screen.getByLabelText('Context name')).toBeInTheDocument()
  })
})