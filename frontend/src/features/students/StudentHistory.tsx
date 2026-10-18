import { type FormEvent, useEffect, useState } from 'react'
import { api } from '../../api/client'
import type { StudentAttempt } from '../../api/types'
import { groupAttempts } from './studentHistoryUtils'

type StudentHistoryProps = {
  studentId: number
}

function StudentHistory({ studentId }: StudentHistoryProps) {
  const [attempts, setAttempts] = useState<StudentAttempt[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [course, setCourse] = useState('')
  const [academicYear, setAcademicYear] = useState('2026/2027')
  const [semester, setSemester] = useState(1)
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
          setError(null)
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
  }, [studentId])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const parsedGrade = Number(grade)

    if (!course.trim() || !academicYear.trim() || !Number.isFinite(parsedGrade) || parsedGrade < 0 || parsedGrade > 20) {
      setError('Enter a course, academic year, and grade between 0 and 20.')
      return
    }

    setSaving(true)
    setError(null)
    try {
      const created = await api.addStudentAttempt(studentId, {
        course: course.trim(),
        academicYear: academicYear.trim(),
        semester,
        grade: parsedGrade,
      })
      setAttempts((current) => [...current, created])
      setCourse('')
      setGrade('')
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

      <form className="attempt-form" onSubmit={handleSubmit}>
        <h4>Add an attempt</h4>
        <label>
          <span>Course</span>
          <input value={course} onChange={(event) => setCourse(event.target.value)} required />
        </label>
        <label>
          <span>Academic year</span>
          <input value={academicYear} onChange={(event) => setAcademicYear(event.target.value)} required />
        </label>
        <label>
          <span>Semester</span>
          <select value={semester} onChange={(event) => setSemester(Number(event.target.value))}>
            <option value={1}>1</option>
            <option value={2}>2</option>
          </select>
        </label>
        <label>
          <span>Grade (0-20)</span>
          <input
            type="number"
            min={0}
            max={20}
            step={0.1}
            value={grade}
            onChange={(event) => setGrade(event.target.value)}
            required
          />
        </label>
        <button type="submit" disabled={saving}>{saving ? 'Adding...' : 'Add attempt'}</button>
      </form>
    </section>
  )
}

export default StudentHistory