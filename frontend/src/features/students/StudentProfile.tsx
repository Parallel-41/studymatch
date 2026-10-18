import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import type { CompetencyExplanation, Student } from '../../api/types'

type StudentProfileProps = {
  student: Student
}

const competencies = [
  { code: 'problem-solving', name: 'Problem solving' },
  { code: 'programming', name: 'Programming' },
  { code: 'teamwork', name: 'Teamwork' },
]

function StudentProfile({ student }: StudentProfileProps) {
  const [selectedCode, setSelectedCode] = useState<string | null>(null)
  const [explanation, setExplanation] = useState<CompetencyExplanation | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const selectedCompetency = competencies.find(({ code }) => code === selectedCode)

  const toggleCompetency = (code: string) => {
    if (selectedCode === code) {
      setSelectedCode(null)
      setExplanation(null)
      setError(null)
      setLoading(false)
      return
    }

    setSelectedCode(code)
    setExplanation(null)
    setError(null)
    setLoading(true)
  }

  useEffect(() => {
    if (!selectedCode) {
      return
    }

    let isCurrent = true

    void api.competencyExplanation(student.id, selectedCode)
      .then((result) => {
        if (isCurrent) {
          setExplanation(result)
        }
      })
      .catch((loadError: unknown) => {
        if (isCurrent) {
          setError(loadError instanceof Error ? loadError.message : 'Unable to load competency explanation.')
        }
      })
      .finally(() => {
        if (isCurrent) {
          setLoading(false)
        }
      })

    return () => {
      isCurrent = false
    }
  }, [student.id, selectedCode])

  return (
    <section className="student-profile-panel" aria-label={`${student.name} profile`}>
      <h2>{student.name}&apos;s profile</h2>
      <p className="profile-identity">Student {student.number} · Year {student.currentYear}</p>
      <h3>Competencies</h3>
      <div className="profile-competency-list">
        {competencies.map((competency) => (
          <button
            key={competency.code}
            type="button"
            aria-expanded={selectedCode === competency.code}
            onClick={() => toggleCompetency(competency.code)}
          >
            {competency.name}
          </button>
        ))}
      </div>

      {selectedCompetency ? (
        <section className="competency-explanation" aria-label={`${selectedCompetency.name} explanation`}>
          <h3>{selectedCompetency.name} explanation</h3>
          {loading ? <p role="status">Loading explanation...</p> : null}
          {error ? <div className="error-box" role="alert">{error}</div> : null}
          {explanation ? (
            <>
              <p className="competency-level">Profile level <strong>{explanation.level}</strong></p>
              <section className="self-assessment-share" aria-label="Self-assessment share">
                <span>Self-assessment</span>
                <strong>{explanation.selfAssessmentShare}%</strong>
                <div className="contribution-track" aria-hidden="true">
                  <span style={{ width: `${Math.max(0, Math.min(100, explanation.selfAssessmentShare))}%` }} />
                </div>
              </section>
              <div className="table-wrap">
                <table aria-label="Course contribution breakdown">
                  <thead>
                    <tr>
                      <th>Course unit</th>
                      <th>Grade</th>
                      <th>Weight</th>
                      <th>Recency</th>
                      <th>Share of level</th>
                    </tr>
                  </thead>
                  <tbody>
                    {explanation.contributions.map((contribution) => (
                      <tr key={contribution.courseUnit}>
                        <td>{contribution.courseUnit}</td>
                        <td>{contribution.grade}</td>
                        <td>{contribution.weight}</td>
                        <td>{contribution.recencyFactor}</td>
                        <td>
                          <span>{contribution.percentageOfLevel}%</span>
                          <div className="contribution-track" aria-hidden="true">
                            <span style={{ width: `${Math.max(0, Math.min(100, contribution.percentageOfLevel))}%` }} />
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          ) : null}
        </section>
      ) : null}
    </section>
  )
}

export default StudentProfile