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

export type AssessmentSeason = 'NORMAL' | 'RECURSO' | 'ESPECIAL' | 'MELHORIA'

export type StudentAttemptDraft = Omit<StudentAttempt, 'id'> & { season?: AssessmentSeason }

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

export type ProfileConfidence = 'HIGH' | 'MEDIUM' | 'LOW'

export type ProfileArea = {
  code: string
  name: string
  level: number | null
}

export type ProfileCompetency = {
  code: string
  name: string
  areaCode: string
  level: number | null
  confidence: ProfileConfidence | null
}

export type StudentProfileData = {
  areas: ProfileArea[]
  competencies: ProfileCompetency[]
}
export type SelfAssessmentData = {
  levels: Record<string, number>
}
