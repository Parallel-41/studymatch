import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import HealthBadge from './HealthBadge'

describe('HealthBadge', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows the API as up when the backend reports it is healthy', async () => {
    vi.spyOn(api, 'health').mockResolvedValue({
      status: 'UP',
      service: 'studymatch-backend',
      timestamp: '2026-10-07T09:00:00Z',
    })

    render(<HealthBadge />)

    expect(await screen.findByText('API up')).toBeInTheDocument()
  })

  it('shows the API as down when the backend reports it is unhealthy', async () => {
    vi.spyOn(api, 'health').mockResolvedValue({
      status: 'DOWN',
      service: 'studymatch-backend',
      timestamp: '2026-10-07T09:00:00Z',
    })

    render(<HealthBadge />)

    expect(await screen.findByText('API down')).toBeInTheDocument()
  })

  it('shows the API as down when the request fails', async () => {
    vi.spyOn(api, 'health').mockRejectedValue(new Error('network error'))

    render(<HealthBadge />)

    expect(await screen.findByText('API down')).toBeInTheDocument()
  })
})
