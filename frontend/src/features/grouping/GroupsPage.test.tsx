import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../api/client'
import { createDemoGeneration } from '../../api/examples/group-generations'
import GroupsPage from './GroupsPage'

describe('GroupsPage', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders 23 students in five groups and shows the group explanation', async () => {
    const generation = createDemoGeneration(2, '2026-11-08T10:35:00Z')
    vi.spyOn(api, 'groupGenerations').mockResolvedValue([
      { id: generation.id, generatedAt: generation.generatedAt, score: generation.score, groupCount: 5 },
    ])
    vi.spyOn(api, 'groupGeneration').mockResolvedValue(generation)

    render(<GroupsPage contextId={7} />)

    expect(await screen.findByText('5 groups · 23 students')).toBeInTheDocument()
    expect(screen.getAllByRole('article', { name: /Group \d/ })).toHaveLength(5)
    expect(screen.getAllByText(/combines strengths across/i)).toHaveLength(5)
    expect(screen.getAllByText(/strong .* contribution to the team/i)).toHaveLength(23)
  })

  it('opens a past generation from the history list', async () => {
    const user = userEvent.setup()
    const newer = createDemoGeneration(2, '2026-11-08T10:35:00Z')
    const older = { ...createDemoGeneration(1, '2026-11-06T14:10:00Z'), score: 70 }
    const getGeneration = vi.spyOn(api, 'groupGeneration').mockImplementation(async (_contextId, generationId) =>
      generationId === older.id ? older : newer,
    )
    vi.spyOn(api, 'groupGenerations').mockResolvedValue([newer, older].map((generation) => ({
      id: generation.id,
      generatedAt: generation.generatedAt,
      score: generation.score,
      groupCount: generation.groups.length,
    })))

    render(<GroupsPage contextId={7} />)

    const previousGeneration = await screen.findByRole('button', { name: /Nov 6, 2026/i })
    await user.click(previousGeneration)

    expect(previousGeneration).toHaveAttribute('aria-pressed', 'true')
    expect(await screen.findByText('70.0')).toBeInTheDocument()
    expect(getGeneration).toHaveBeenLastCalledWith(7, older.id)
  })

  it('generates groups and displays an impossible-split warning from the API', async () => {
    const user = userEvent.setup()
    vi.spyOn(api, 'groupGenerations').mockResolvedValue([])
    vi.spyOn(api, 'generateGroups').mockResolvedValue({
      ...createDemoGeneration(3, '2026-11-09T10:00:00Z'),
      warning: 'Closest split used: one group of 3 and one group of 4.',
    })

    render(<GroupsPage contextId={7} />)
    await user.click(await screen.findByRole('button', { name: 'Generate groups' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Closest split used')
    expect(screen.getByText('5 groups · 23 students')).toBeInTheDocument()
  })
})