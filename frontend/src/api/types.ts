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

export type CompetencyContribution = {
  courseUnit: string
  grade: number
  weight: number
  recencyFactor: number
  percentageOfLevel: number
}

export type CompetencyExplanation = {
  level: number
  contributions: CompetencyContribution[]
  selfAssessmentShare: number
}
