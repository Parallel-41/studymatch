import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from './client'

const draft = {
  number: 'PM-1',
  name: 'Postman Student',
  email: 'pm1@example.com',
  registrationYear: 2025,
  degreeProgramCode: 'LEI',
}

const jsonResponse = (status: number, body: unknown) =>
  new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })

describe('api client — students', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('POSTs the StudentRequest body (registrationYear and degreeProgramCode)', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(201, { id: 9, ...draft, currentYear: 2 }))
    vi.stubGlobal('fetch', fetchMock)

    await api.createStudent(draft)

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/students')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body)).toEqual(draft)
  })

  it('PUTs to the student id with the same body', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(200, { id: 9, ...draft, currentYear: 2 }))
    vi.stubGlobal('fetch', fetchMock)

    await api.updateStudent(9, draft)

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/students/9')
    expect(init.method).toBe('PUT')
    expect(JSON.parse(init.body)).toEqual(draft)
  })

  it('throws the API message on a validation error instead of using mocks', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(
      jsonResponse(400, { status: 400, message: 'registrationYear: registrationYear must be 1900 or later' }),
    ))

    await expect(api.createStudent({ ...draft, registrationYear: 1800 })).rejects.toThrow('registrationYear')
  })
})

describe('api client — grouping contexts', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  const context = {
    id: 8,
    name: 'Project groups',
    description: 'Teams for the semester project.',
    createdAt: '2026-11-09T10:00:00Z',
    studentIds: [1, 3],
  }

  it('lists and reads contexts from the API', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse(200, [context]))
      .mockResolvedValueOnce(jsonResponse(200, context))
    vi.stubGlobal('fetch', fetchMock)

    await expect(api.groupingContexts()).resolves.toEqual([context])
    await expect(api.groupingContext(8)).resolves.toEqual(context)
    expect(fetchMock.mock.calls.map(([url]) => url)).toEqual([
      '/api/grouping-contexts',
      '/api/grouping-contexts/8',
    ])
  })

  it('POSTs a new grouping context with its name and description', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(201, context))
    vi.stubGlobal('fetch', fetchMock)

    await api.createGroupingContext({ name: context.name, description: context.description })

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/grouping-contexts')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body)).toEqual({ name: context.name, description: context.description })
  })

  it('PUTs the selected student IDs to the participants endpoint', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(200, context))
    vi.stubGlobal('fetch', fetchMock)

    await api.saveGroupingParticipants(8, [1, 3])

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/grouping-contexts/8/participants')
    expect(init.method).toBe('PUT')
    expect(JSON.parse(init.body)).toEqual({ studentIds: [1, 3] })
  })

  it('marks a student as cold-start when their profile has no evidence confidence', async () => {
    vi.spyOn(api, 'students').mockResolvedValue([
      { id: 22, number: 'S-22', name: 'New student', email: 'new@example.pt', currentYear: 1 },
    ])
    vi.spyOn(api, 'studentProfile').mockResolvedValue({
      areas: [{ code: 'PROG', name: 'Programming', level: 60, confidence: 0 }],
      competencies: [],
    })

    const students = await api.groupingStudents()

    expect(students[0]).toMatchObject({ id: 22, coldStart: true })
    expect(students[0].profile.areas[0].level).toBe(60)
  })

  it('uses the eligible-student example when the students endpoint is unavailable', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(502, { message: 'Bad Gateway' })))

    const students = await api.groupingStudents()

    expect(students).toHaveLength(6)
    expect(students.some((student) => student.coldStart)).toBe(true)
    expect(students.some((student) => !student.coldStart)).toBe(true)
  })
})
