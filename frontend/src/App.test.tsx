import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from './api/client'
import App from './App'

describe('App navigation', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('opens the grouping contexts page from the main navigation', async () => {
    const user = userEvent.setup()
    vi.spyOn(api, 'health').mockResolvedValue({ status: 'UP', service: 'studymatch-backend', timestamp: '' })
    vi.spyOn(api, 'students').mockResolvedValue([])
    vi.spyOn(api, 'groupingContexts').mockResolvedValue([])
    vi.spyOn(api, 'groupingStudents').mockResolvedValue([])

    render(<App />)

    await user.click(screen.getByRole('tab', { name: 'Contexts' }))

    expect(await screen.findByRole('heading', { name: 'Grouping contexts' })).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: 'Contexts' })).toHaveAttribute('aria-selected', 'true')
  })
})