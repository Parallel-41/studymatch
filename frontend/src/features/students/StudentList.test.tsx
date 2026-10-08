import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import StudentList from './StudentList'

describe('StudentList', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders rows for loaded students', async () => {
    vi.spyOn(api, 'students').mockResolvedValue([
      {
        id: 1,
        number: 'a20240001',
        name: 'Ana Ribeiro',
        email: 'ana.ribeiro@example.pt',
        currentYear: 3,
      },
    ])

    render(<StudentList />)

    expect(await screen.findByRole('cell', { name: 'a20240001' })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: 'Ana Ribeiro' })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: 'ana.ribeiro@example.pt' })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: '3' })).toBeInTheDocument()
  })

  it('renders the empty state when there are no students', async () => {
    vi.spyOn(api, 'students').mockResolvedValue([])

    render(<StudentList />)

    expect(await screen.findByText('No students yet.')).toBeInTheDocument()
  })

  it('renders the error state when the request fails', async () => {
    vi.spyOn(api, 'students').mockRejectedValue(new Error('Request failed'))

    render(<StudentList />)

    expect(await screen.findByRole('alert')).toHaveTextContent('Unable to load students')
  })
})
