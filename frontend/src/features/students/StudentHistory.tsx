import { type FormEvent, useEffect, useState } from 'react'
import { api } from '../../api/client'
import type { AssessmentSeason, CourseUnit, StudentAttempt } from '../../api/types'
import { currentAcademicYear } from './academicYear'
import { groupAttempts } from './studentHistoryUtils'
import { SEASONS } from './studentOptions'

type StudentHistoryProps = {
  studentId: number
}

const academicYearPattern = /^\d{4}\/(\d{2}|\d{4})$/

function StudentHistory({ studentId }: StudentHistoryProps) {
  const [attempts, setAttempts] = useState<StudentAttempt[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)
  const [units, setUnits] = useState<CourseUnit[]>([])
  const [unitCode, setUnitCode] = useState('')
  const [academicYear, setAcademicYear] = useState(currentAcademicYear())
  const [season, setSeason] = useState<AssessmentSeason>('NORMAL')
  const [grade, setGrade] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let isMounted = true

    const loadAttempts = async () => {
      setLoading(true)
      try {
        const result = await api.studentAttempts(studentId)
        if (isMounted) {
          setAttempts(result)
        }
      } catch (loadError) {
        if (isMounted) {
          setError(loadError instanceof Error ? loadError.message : 'Unable to load student history.')
        }
      } finally {
        if (isMounted) {
          setLoading(false)
        }
      }
    }

    void loadAttempts()

    return () => {
      isMounted = false
    }
  }, [studentId, reloadKey])

  useEffect(() => {
    let isMounted = true

    api.courseUnits()
      .then((result) => {
        if (isMounted) {
          setUnits(result)
          setUnitCode((current) => current || result[0]?.code || '')
        }
      })
      .catch(() => {
        if (isMounted) {
          setError('Unable to load course units.')
        }
      })

    return () => {
      isMounted = false
    }
  }, [])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setNotice(null)
    const parsedGrade = Number(grade)
    const unit = units.find((candidate) => candidate.code === unitCode)

    if (!unit || !academicYearPattern.test(academicYear.trim()) || grade.trim() === ''
      || !Number.isInteger(parsedGrade) || parsedGrade < 0 || parsedGrade > 20) {
      setError('Choose a course unit, an academic year like 2026/27 and a whole grade from 0 to 20.')
      return
    }

    setSaving(true)
    setError(null)
    try {
      await api.addStudentAttempt(studentId, {
        course: unit.code,
        academicYear: academicYear.trim(),
        semester: unit.semester,
        grade: parsedGrade,
        season,
      })
      setGrade('')
      setNotice('Attempt added.')
      setReloadKey((key) => key + 1)
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : 'Unable to add attempt.')
    } finally {
      setSaving(false)
    }
  }

  const groups = groupAttempts(attempts)

  return (
    <section className="student-history" aria-label="Student history">
      <h3>Academic history</h3>

      {loading ? <p role="status">Loading history...</p> : null}
      {error ? <div className="error-box" role="alert">{error}</div> : null}
      {notice ? <p role="status">{notice}</p> : null}
      {!loading && !error && attempts.length === 0 ? <p>No academic attempts recorded.</p> : null}

      {!loading && attempts.length > 0 ? (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Academic year</th>
                <th>Semester</th>
                <th>Course</th>
                <th>Grade</th>
              </tr>
            </thead>
            <tbody>
              {groups.flatMap((group) => group.attempts.map((attempt) => (
                <tr key={attempt.id}>
                  <td>{group.academicYear}</td>
                  <td>{group.semester}</td>
                  <td>{attempt.course}</td>
                  <td>
                    <span className={attempt.grade === group.bestGrade ? 'best-grade' : undefined}>
                      {attempt.grade}
                      {attempt.grade === group.bestGrade ? <span className="best-grade-label"> Best</span> : null}
                    </span>
                  </td>
                </tr>
              ))) }
            </tbody>
          </table>
        </div>
      ) : null}

      <form className="attempt-form" onSubmit={handleSubmit} noValidate>
        <h4>Add an attempt</h4>
        <label>
          <span>Course unit</span>
          <select aria-label="Course unit" value={unitCode} onChange={(event) => setUnitCode(event.target.value)}>
            {units.map((unit) => (
              <option key={unit.code} value={unit.code}>
                {unit.code} — {unit.name} (year {unit.curricularYear}, semester {unit.semester})
              </option>
            ))}
          </select>
        </label>
        <label>
          <span>Academic year</span>
          <input
            aria-label="Academic year"
            value={academicYear}
            placeholder="2026/27"
            onChange={(event) => setAcademicYear(event.target.value)}
          />
        </label>
        <label>
          <span>Season</span>
          <select
            aria-label="Season"
            value={season}
            onChange={(event) => setSeason(event.target.value as AssessmentSeason)}
          >
            {SEASONS.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </label>
        <label>
          <span>Grade (0-20)</span>
          <input
            aria-label="Grade"
            type="number"
            min={0}
            max={20}
            step={1}
            value={grade}
            onChange={(event) => setGrade(event.target.value)}
          />
        </label>
        <button type="submit" disabled={saving || units.length === 0}>{saving ? 'Adding...' : 'Add attempt'}</button>
      </form>
    </section>
  )
}

export default StudentHistory
