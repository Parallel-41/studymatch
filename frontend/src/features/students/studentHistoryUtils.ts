import type { StudentAttempt } from '../../api/types'

export type AttemptGroup = {
  academicYear: string
  semester: number
  attempts: StudentAttempt[]
  bestGrade: number
}

export function groupAttempts(attempts: StudentAttempt[]): AttemptGroup[] {
  const groups = new Map<string, AttemptGroup>()

  for (const attempt of attempts) {
    const key = `${attempt.academicYear}-${attempt.semester}`
    const group = groups.get(key)

    if (group) {
      group.attempts.push(attempt)
      group.bestGrade = Math.max(group.bestGrade, attempt.grade)
    } else {
      groups.set(key, {
        academicYear: attempt.academicYear,
        semester: attempt.semester,
        attempts: [attempt],
        bestGrade: attempt.grade,
      })
    }
  }

  return [...groups.values()].sort((left, right) =>
    right.academicYear.localeCompare(left.academicYear) || right.semester - left.semester,
  )
}