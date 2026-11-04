import { describe, expect, it } from 'vitest'
import { academicYearStart, currentAcademicYear, yearOfStudy } from './academicYear'

describe('academic year helpers', () => {
  it('starts the academic year in September (boundary: 31 Aug vs 1 Sep)', () => {
    expect(academicYearStart(new Date(2026, 7, 31))).toBe(2025)
    expect(academicYearStart(new Date(2026, 8, 1))).toBe(2026)
  })

  it('formats the academic year as the API expects', () => {
    expect(currentAcademicYear(new Date(2026, 10, 4))).toBe('2026/27')
    expect(currentAcademicYear(new Date(2099, 10, 4))).toBe('2099/00')
  })

  it('derives the year of study like the backend', () => {
    expect(yearOfStudy(2024, new Date(2026, 10, 4))).toBe(3)
    expect(yearOfStudy(2026, new Date(2026, 10, 4))).toBe(1)
  })
})
