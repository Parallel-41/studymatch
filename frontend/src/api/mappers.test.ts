import { describe, expect, it } from 'vitest'
import {
  confidenceLabel,
  fromBackendExplanation,
  fromBackendHistory,
  fromBackendProfile,
  toBackendAttempt,
} from './mappers'

describe('backend mappers', () => {
  it('flattens the history and skips attempts without a grade', () => {
    const attempts = fromBackendHistory({
      years: [
        {
          academicYear: '2024/25',
          semesters: [
            {
              semester: 1,
              units: [
                {
                  code: 'PROG1',
                  name: 'Programação I',
                  ects: 6,
                  attempts: [
                    { season: 'NORMAL', grade: 12 },
                    { season: 'MELHORIA', grade: 16 },
                    { season: 'RECURSO', grade: null },
                  ],
                  bestGrade: 16,
                },
              ],
            },
          ],
        },
      ],
    })

    expect(attempts).toHaveLength(2)
    expect(attempts[1]).toMatchObject({
      course: 'PROG1 Programação I (MELHORIA)',
      academicYear: '2024/25',
      semester: 1,
      grade: 16,
    })
  })

  it('maps profile area and numeric confidence', () => {
    const profile = fromBackendProfile({
      areas: [{ code: 'PROG', name: 'Programming', level: 59.9, confidence: 0.61 }],
      competencies: [
        { code: 'PROG_OO', name: 'Object-oriented programming', area: 'PROG', level: 59.9, confidence: 0.61 },
        { code: 'SOFT_LEAD', name: 'Leadership', area: 'SOFT', level: null, confidence: 0 },
      ],
    })

    expect(profile.competencies[0]).toMatchObject({ areaCode: 'PROG', confidence: 'MEDIUM' })
    expect(profile.competencies[1].confidence).toBeNull()
  })

  it('classifies confidence at the boundaries', () => {
    expect(confidenceLabel(50, 0.67)).toBe('HIGH')
    expect(confidenceLabel(50, 0.66)).toBe('MEDIUM')
    expect(confidenceLabel(50, 0.34)).toBe('MEDIUM')
    expect(confidenceLabel(50, 0.33)).toBe('LOW')
    expect(confidenceLabel(null, 1)).toBeNull()
  })

  it('maps explanation shares from 0-1 to percentages', () => {
    const explanation = fromBackendExplanation({
      competencyCode: 'PROG_OO',
      level: 59.9,
      selfAssessedShare: 0.39,
      contributions: [{ courseUnitCode: 'PROG1', grade: 16, weight: 0.6, recency: 0.85, share: 0.75 }],
    })

    expect(explanation.selfAssessmentShare).toBe(39)
    expect(explanation.contributions[0]).toEqual({
      courseUnit: 'PROG1',
      grade: 16,
      weight: 0.6,
      recencyFactor: 0.85,
      percentageOfLevel: 75,
    })
  })

  it('builds the attempt request the backend validates', () => {
    expect(
      toBackendAttempt({ course: 'prog1', academicYear: '2026/2027', semester: 1, grade: 18, season: 'MELHORIA' }),
    ).toEqual({ courseUnitCode: 'PROG1', academicYear: '2026/27', season: 'MELHORIA', grade: 18 })
    expect(toBackendAttempt({ course: 'DB1', academicYear: '2025/26', semester: 1, grade: 14 }).season).toBe('NORMAL')
  })
})
