export type HealthResponse = {
  status: string
  service: string
  timestamp: string
}

export type Student = {
  id: number
  number: string
  name: string
  email: string
  currentYear: number
}

export type StudentAttempt = {
  id: number
  course: string
  academicYear: string
  semester: number
  grade: number
}

export type StudentAttemptDraft = Omit<StudentAttempt, 'id'>
