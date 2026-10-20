import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import StudentForm from './StudentForm'

describe('StudentForm', () => {
  it('submits valid form data', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn().mockResolvedValue(undefined)

    render(<StudentForm onSubmit={onSubmit} onCancel={() => undefined} />)

    await user.type(screen.getByLabelText('Student number'), 'A20240042')
    await user.type(screen.getByLabelText('Full name'), 'Ana Silva')
    await user.type(screen.getByLabelText('Email'), 'ana@example.com')
    await user.clear(screen.getByLabelText('Current year'))
    await user.type(screen.getByLabelText('Current year'), '3')
    await user.click(screen.getByRole('button', { name: 'Save student' }))

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({
        number: 'A20240042',
        name: 'Ana Silva',
        email: 'ana@example.com',
        currentYear: 3,
      }),
    )
  })

  it('shows the API error message when submit fails', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn().mockRejectedValue(new Error('Student number already exists'))

    render(<StudentForm onSubmit={onSubmit} onCancel={() => undefined} />)

    await user.type(screen.getByLabelText('Student number'), 'A20240042')
    await user.type(screen.getByLabelText('Full name'), 'Ana Silva')
    await user.type(screen.getByLabelText('Email'), 'ana@example.com')
    await user.clear(screen.getByLabelText('Current year'))
    await user.type(screen.getByLabelText('Current year'), '3')
    await user.click(screen.getByRole('button', { name: 'Save student' }))

    expect(await screen.findByText('Student number already exists')).toBeInTheDocument()
  })
})
