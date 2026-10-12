import type { HealthResponse, Student } from './types'

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path)

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(`${response.status}: ${errorText || 'Request failed'}`)
  }

  return (await response.json()) as T
}

export const api = {
  health: () => getJson<HealthResponse>('/api/health'),
  students: () => getJson<Student[]>('/api/students'),
}
