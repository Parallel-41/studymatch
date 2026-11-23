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
