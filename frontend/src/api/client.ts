import type {
  CompetencyExplanation,
  HealthResponse,
  StudentProfileData,
  Student,
  StudentAttempt,
  StudentAttemptDraft,
} from './types'
import {
  fromBackendExplanation,
  fromBackendHistory,
  fromBackendProfile,
  toBackendAttempt,
  type BackendExplanation,
  type BackendHistory,
  type BackendProfile,
} from './mappers'

export type StudentDraft = Omit<Student, 'id'>

export class ApiError extends Error {
  status?: number
  code?: string
  details?: unknown

  constructor(message: string, status?: number, code?: string, details?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.details = details
  }
}

const mockHealth: HealthResponse = {
  status: 'UP',
  service: 'studymatch-backend',
  timestamp: '2026-10-07T09:00:00Z',
}

const mockStudents: Student[] = [
  { id: 1, number: 'a20240001', name: 'Ana Ribeiro', email: 'ana.ribeiro@example.pt', currentYear: 3 },
  { id: 2, number: 'a20240002', name: 'Bruno Silva', email: 'bruno.silva@example.pt', currentYear: 2 },
  { id: 3, number: 'a20240003', name: 'Carla Costa', email: 'carla.costa@example.pt', currentYear: 1 },
  { id: 4, number: 'a20240004', name: 'Diogo Almeida', email: 'diogo.almeida@example.pt', currentYear: 4 },
  { id: 5, number: 'a20240005', name: 'Eva Martins', email: 'eva.martins@example.pt', currentYear: 3 },
  { id: 6, number: 'a20240006', name: 'Filipe Rocha', email: 'filipe.rocha@example.pt', currentYear: 2 },
]

const mockAttempts: Record<number, StudentAttempt[]> = {
  1: [
    { id: 1, course: 'Algorithms', academicYear: '2024/2025', semester: 1, grade: 15 },
    { id: 2, course: 'Databases', academicYear: '2024/2025', semester: 1, grade: 18 },
    { id: 3, course: 'Software Engineering', academicYear: '2024/2025', semester: 2, grade: 17 },
  ],
}

const mockCompetencyExplanation: CompetencyExplanation = {
  level: 3.6,
  contributions: [
    { courseUnit: 'Algorithms', grade: 16, weight: 0.45, recencyFactor: 0.9, percentageOfLevel: 35 },
    { courseUnit: 'Software Engineering', grade: 17, weight: 0.35, recencyFactor: 0.8, percentageOfLevel: 30 },
  ],
  selfAssessmentShare: 35,
}

const mockStudentProfile: StudentProfileData = {
  areas: [
    { code: 'technical', name: 'Technical', level: 76 },
    { code: 'analytical', name: 'Analytical', level: 84 },
    { code: 'collaboration', name: 'Collaboration', level: 58 },
    { code: 'communication', name: 'Communication', level: 0 },
  ],
  competencies: [
    { code: 'problem-solving', name: 'Problem solving', areaCode: 'analytical', level: 84, confidence: 'HIGH' },
    { code: 'programming', name: 'Programming', areaCode: 'technical', level: 76, confidence: 'MEDIUM' },
    { code: 'teamwork', name: 'Teamwork', areaCode: 'collaboration', level: 58, confidence: 'LOW' },
    { code: 'presentation', name: 'Presentation', areaCode: 'communication', level: 0, confidence: 'LOW' },
  ],
}

const matchQuery = (student: Student, query: string) => {
  const normalizedQuery = query.trim().toLowerCase()

  if (!normalizedQuery) {
    return true
  }

  return [student.number, student.name, student.email]
    .join(' ')
    .toLowerCase()
    .includes(normalizedQuery)
}

const buildStudentUrl = (query?: string) => {
  if (!query) {
    return '/api/students'
  }

  return `/api/students?q=${encodeURIComponent(query)}`
}

async function getJson<T>(path: string, fallback: T): Promise<T> {
  try {
    const response = await fetch(path)
    const payload = (await response.json().catch(() => null)) as
      | { code?: string; message?: string; details?: unknown }
      | null

    if (!response.ok) {
      const responseMessage =
        typeof payload?.message === 'string'
          ? payload.message
          : `${response.status}: ${response.statusText}`

      throw new ApiError(`${responseMessage} (${response.status})`, response.status, payload?.code, payload?.details)
    }

    return ((payload as T) ?? fallback) as T
  } catch (error) {
    if (error instanceof ApiError) {
      throw error
    }

    return fallback
  }
}

export const api = {
  health: () => getJson<HealthResponse>('/api/health', mockHealth),
  students: async (query = '') => {
    const path = buildStudentUrl(query)
    const fallback = mockStudents.filter((student) => matchQuery(student, query))

    return getJson<Student[]>(path, fallback)
  },
  createStudent: async (student: StudentDraft) => {
    const payload = { ...student, id: Date.now() }

    try {
      const response = await fetch('/api/students', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      const body = (await response.json().catch(() => null)) as
        | (Student & { message?: string; code?: string; details?: unknown })
        | null

      if (!response.ok) {
        throw new ApiError(body?.message ?? 'Unable to create student.', response.status, body?.code, body?.details)
      }

      return (body ?? payload) as Student
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }

      const createdStudent = { ...payload, id: payload.id ?? Date.now() }
      mockStudents.unshift(createdStudent)
      return createdStudent
    }
  },
  updateStudent: async (student: Student) => {
    const payload = { ...student }

    try {
      const response = await fetch(`/api/students/${student.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      const body = (await response.json().catch(() => null)) as
        | (Student & { message?: string; code?: string; details?: unknown })
        | null

      if (!response.ok) {
        throw new ApiError(body?.message ?? 'Unable to update student.', response.status, body?.code, body?.details)
      }

      return (body ?? payload) as Student
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }

      const index = mockStudents.findIndex((currentStudent) => currentStudent.id === student.id)
      if (index >= 0) {
        mockStudents[index] = payload
      }
      return payload
    }
  },
  studentAttempts: async (studentId: number) => {
    const history = await getJson<BackendHistory | null>(`/api/students/${studentId}/history`, null)
    return history ? fromBackendHistory(history) : (mockAttempts[studentId] ?? [])
  },
  competencyExplanation: async (studentId: number, competencyCode: string) => {
    const explanation = await getJson<BackendExplanation | null>(
      `/api/students/${studentId}/profile/competencies/${encodeURIComponent(competencyCode)}/explanation`,
      null,
    )
    return explanation ? fromBackendExplanation(explanation) : mockCompetencyExplanation
  },
  studentProfile: async (studentId: number) => {
    const profile = await getJson<BackendProfile | null>(`/api/students/${studentId}/profile`, null)
    return profile ? fromBackendProfile(profile) : mockStudentProfile
  },
  addStudentAttempt: async (studentId: number, attempt: StudentAttemptDraft) => {
    const { season, ...fields } = attempt
    const payload: StudentAttempt = {
      ...fields,
      id: Date.now(),
      course: season && season !== 'NORMAL' ? `${fields.course} (${season})` : fields.course,
    }

    try {
      const response = await fetch(`/api/students/${studentId}/attempts`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(toBackendAttempt(attempt)),
      })
      const body = (await response.json().catch(() => null)) as
        | (StudentAttempt & { message?: string; code?: string; details?: unknown })
        | null

      if (!response.ok) {
        throw new ApiError(body?.message ?? 'Unable to add attempt.', response.status, body?.code, body?.details)
      }

      return payload
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }

      mockAttempts[studentId] ??= []
      mockAttempts[studentId].push(payload)
      return payload
    }
  },
}
