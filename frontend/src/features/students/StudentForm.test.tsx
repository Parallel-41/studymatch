import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { academicYearStart, yearOfStudy } from './academicYear'
import StudentForm from './StudentForm'

const fillValidStudent = async (user: ReturnType<typeof userEvent.setup>, registrationYear: string) => {
  await user.type(screen.getByLabelText('Student number'), 'A20240042')
  await user.type(screen.getByLabelText('Full name'), 'Ana Silva')
  await user.type(screen.getByLabelText('Email'), 'ana@example.com')
  await user.clear(screen.getByLabelText('Registration year'))
  await user.type(screen.getByLabelText('Registration year'), registrationYear)
}

describe('StudentForm', () => {
  it('submits the fields the API expects, with LEI as the degree programme', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn().mockResolvedValue(undefined)

    render(<StudentForm onSubmit={onSubmit} onCancel={() => undefined} />)

    await fillValidStudent(user, '2024')
    await user.click(screen.getByRole('button', { name: 'Save student' }))

    expect(onSubmit).toHaveBeenCalledWith({
      number: 'A20240042',
      name: 'Ana Silva',
      email: 'ana@example.com',
      registrationYear: 2024,
      degreeProgramCode: 'LEI',
    })
  })

  it('accepts the current academic year and rejects the next one (boundary)', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    const thisYear = academicYearStart()

    render(<StudentForm onSubmit={onSubmit} onCancel={() => undefined} />)

    await fillValidStudent(user, String(thisYear + 1))
    await user.click(screen.getByRole('button', { name: 'Save student' }))
    expect(screen.getByText(`Registration year must be between 1900 and ${thisYear}.`)).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()

    await user.clear(screen.getByLabelText('Registration year'))
    await user.type(screen.getByLabelText('Registration year'), String(thisYear))
    await user.click(screen.getByRole('button', { name: 'Save student' }))
    expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ registrationYear: thisYear }))
  })

  it('shows the year of study derived from the registration year', async () => {
    const user = userEvent.setup()
    render(<StudentForm onSubmit={vi.fn()} onCancel={() => undefined} />)

    await user.clear(screen.getByLabelText('Registration year'))
    await user.type(screen.getByLabelText('Registration year'), '2024')

    expect(screen.getByText(`Year of study: ${yearOfStudy(2024)}`)).toBeInTheDocument()
  })

  it('shows the API error message when submit fails', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn().mockRejectedValue(new Error('A student with the same number or email already exists.'))

    render(<StudentForm onSubmit={onSubmit} onCancel={() => undefined} />)

    await fillValidStudent(user, '2024')
    await user.click(screen.getByRole('button', { name: 'Save student' }))

    expect(await screen.findByText('A student with the same number or email already exists.')).toBeInTheDocument()
  })
})
