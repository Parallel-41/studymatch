/** Academic years start in September, the same rule as Student.academicYearStart in the backend. */
export function academicYearStart(now: Date = new Date()): number {
  return now.getMonth() >= 8 ? now.getFullYear() : now.getFullYear() - 1
}

/** The current academic year in the format the API expects, e.g. "2026/27". */
export function currentAcademicYear(now: Date = new Date()): string {
  const start = academicYearStart(now)
  return `${start}/${String((start + 1) % 100).padStart(2, '0')}`
}

/** Year of study for a registration year, e.g. registered in 2024 → year 3 in 2026/27. */
export function yearOfStudy(registrationYear: number, now: Date = new Date()): number {
  return academicYearStart(now) - registrationYear + 1
}
