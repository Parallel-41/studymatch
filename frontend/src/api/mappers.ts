import type {
  CompetencyExplanation,
  ProfileConfidence,
  StudentAttempt,
  StudentAttemptDraft,
  StudentProfileData,
} from './types'

/** Backend contracts (see backend/.../api/*Response.java). Kept here so components keep their own types. */
export type BackendHistory = {
  years: {
    academicYear: string
    semesters: {
      semester: number
      units: {
        code: string
        name: string
        ects: number
        attempts: { season: string; grade: number | null }[]
        bestGrade: number | null
      }[]
    }[]
  }[]
}

export type BackendProfile = {
  areas: { code: string; name: string; level: number | null; confidence: number }[]
  competencies: { code: string; name: string; area: string; level: number | null; confidence: number }[]
}

export type BackendExplanation = {
  competencyCode: string
  level: number | null
  selfAssessedShare: number
  contributions: {
    courseUnitCode: string
    grade: number
    weight: number
    recency: number
    share: number
  }[]
}

export type BackendAttemptRequest = {
  courseUnitCode: string
  academicYear: string
  season: string
  grade: number
}

const toPercent = (share: number) => Math.round(share * 100)

export function confidenceLabel(level: number | null, confidence: number): ProfileConfidence | null {
  if (level === null) {
    return null
  }
  if (confidence >= 0.67) {
    return 'HIGH'
  }
  return confidence >= 0.34 ? 'MEDIUM' : 'LOW'
}

export function fromBackendHistory(history: BackendHistory): StudentAttempt[] {
  let id = 0
  return history.years.flatMap((year) =>
    year.semesters.flatMap((semester) =>
      semester.units.flatMap((unit) =>
        unit.attempts
          .filter((attempt) => attempt.grade !== null)
          .map((attempt) => ({
            id: ++id,
            course: `${unit.code} ${unit.name} (${attempt.season})`,
            academicYear: year.academicYear,
            semester: semester.semester,
            grade: attempt.grade as number,
          })),
      ),
    ),
  )
}

export function fromBackendProfile(profile: BackendProfile): StudentProfileData {
  return {
    areas: profile.areas.map(({ code, name, level }) => ({ code, name, level })),
    competencies: profile.competencies.map((competency) => ({
      code: competency.code,
      name: competency.name,
      areaCode: competency.area,
      level: competency.level,
      confidence: confidenceLabel(competency.level, competency.confidence),
    })),
  }
}

export function fromBackendExplanation(explanation: BackendExplanation): CompetencyExplanation {
  return {
    level: explanation.level ?? 0,
    selfAssessmentShare: toPercent(explanation.selfAssessedShare),
    contributions: explanation.contributions.map((contribution) => ({
      courseUnit: contribution.courseUnitCode,
      grade: contribution.grade,
      weight: contribution.weight,
      recencyFactor: contribution.recency,
      percentageOfLevel: toPercent(contribution.share),
    })),
  }
}

/** The backend expects the course unit code, a "2024/25" academic year and an assessment season. */
export function toBackendAttempt(attempt: StudentAttemptDraft): BackendAttemptRequest {
  const year = attempt.academicYear.trim()
  const longForm = /^(\d{4})\/\d{2}(\d{2})$/.exec(year)
  return {
    courseUnitCode: attempt.course.trim().split(/\s+/)[0].toUpperCase(),
    academicYear: longForm ? `${longForm[1]}/${longForm[2]}` : year,
    season: attempt.season ?? 'NORMAL',
    grade: attempt.grade,
  }
}
