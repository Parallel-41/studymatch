import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import type { CompetencyExplanation, ProfileArea, Student, StudentProfileData } from '../../api/types'
import { SelfAssessmentForm } from './SelfAssessmentForm'

type StudentProfileProps = {
  student: Student
}

const chartCenter = { x: 200, y: 160 }
const chartRadius = 105

function chartPoint(index: number, count: number, radius: number) {
  const angle = -Math.PI / 2 + (index * 2 * Math.PI) / count
  return `${chartCenter.x + Math.cos(angle) * radius},${chartCenter.y + Math.sin(angle) * radius}`
}

function ProfileRadarChart({ areas }: { areas: ProfileArea[] }) {
  const availableAreas = areas.filter((area) => area.level !== null)
  const missingAreas = areas.filter((area) => area.level === null)

  if (availableAreas.length < 3) {
    return (
      <section className="profile-radar" aria-label="Profile by area">
        <h3>Profile by area</h3>
        <p role="status">Not enough data to display the radar chart.</p>
        {missingAreas.length > 0 ? (
          <p className="no-data-areas">No data for: {missingAreas.map((area) => area.name).join(', ')}</p>
        ) : null}
      </section>
    )
  }

  const points = availableAreas.map((area, index) => {
    const normalizedLevel = Math.max(0, Math.min(100, area.level ?? 0))
    const radius = (chartRadius * normalizedLevel) / 100
    return {
      ...area,
      point: chartPoint(index, availableAreas.length, radius),
      labelPoint: chartPoint(index, availableAreas.length, chartRadius + 32).split(',').map(Number),
    }
  })

  return (
    <section className="profile-radar" aria-label="Profile by area">
      <h3>Profile by area</h3>
      <svg viewBox="0 0 400 320" role="img" aria-label="Radar chart showing profile level from 0 to 100 by area">
        {[25, 50, 75, 100].map((step) => (
          <polygon
            key={step}
            points={availableAreas.map((_, index) => chartPoint(index, availableAreas.length, (chartRadius * step) / 100)).join(' ')}
            className="radar-grid"
          />
        ))}
        {availableAreas.map((area, index) => (
          <line
            key={area.code}
            x1={chartCenter.x}
            y1={chartCenter.y}
            x2={chartPoint(index, availableAreas.length, chartRadius).split(',')[0]}
            y2={chartPoint(index, availableAreas.length, chartRadius).split(',')[1]}
            className="radar-axis"
          />
        ))}
        <polygon points={points.map(({ point }) => point).join(' ')} className="radar-value" />
        {points.map(({ code, level, name, labelPoint, point }) => (
          <g key={code}>
            <circle cx={point.split(',')[0]} cy={point.split(',')[1]} r="4" className="radar-point" />
            <text x={labelPoint[0]} y={labelPoint[1]} className="radar-label" textAnchor="middle">
              {name} ({level})
            </text>
          </g>
        ))}
      </svg>
      {missingAreas.length > 0 ? (
        <p className="no-data-areas">No data for: {missingAreas.map((area) => area.name).join(', ')}</p>
      ) : null}
    </section>
  )
}

function StudentProfile({ student }: StudentProfileProps) {
  const [profile, setProfile] = useState<StudentProfileData | null>(null)
  const [profileLoading, setProfileLoading] = useState(true)
  const [profileError, setProfileError] = useState<string | null>(null)
  const [selectedCode, setSelectedCode] = useState<string | null>(null)
  const [explanation, setExplanation] = useState<CompetencyExplanation | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const selectedCompetency = profile?.competencies.find(({ code }) => code === selectedCode)

  useEffect(() => {
    let isCurrent = true

    void api.studentProfile(student.id)
      .then((result) => {
        if (isCurrent) {
          setProfile(result)
        }
      })
      .catch((loadError: unknown) => {
        if (isCurrent) {
          setProfileError(loadError instanceof Error ? loadError.message : 'Unable to load student profile.')
        }
      })
      .finally(() => {
        if (isCurrent) {
          setProfileLoading(false)
        }
      })

    return () => {
      isCurrent = false
    }
  }, [student.id])

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
      {profileLoading ? <p role="status">Loading profile...</p> : null}
      {profileError ? <div className="error-box" role="alert">{profileError}</div> : null}
      {profile ? (
        <>
          <ProfileRadarChart areas={profile.areas} />
          <SelfAssessmentForm studentId={student.id} onSaved={setProfile} />
          <section className="profile-competencies" aria-label="Competency levels">
            <h3>Competencies</h3>
            <div className="table-wrap">
              <table aria-label="Competency levels and confidence">
                <thead>
                <tr>
                  <th>Competency</th>
                  <th>Area</th>
                  <th>Level</th>
                  <th>Confidence</th>
                  <th>Explanation</th>
                </tr>
                </thead>
                <tbody>
                {profile.competencies.map((competency) => {
                  const area = profile.areas.find(({ code }) => code === competency.areaCode)
                  const confidenceLabel = competency.confidence
                    ? `${competency.confidence.charAt(0)}${competency.confidence.slice(1).toLowerCase()} confidence`
                    : 'No data'

                  return (
                    <tr key={competency.code}>
                      <td>{competency.name}</td>
                      <td>{area?.name ?? 'No data'}</td>
                      <td>{competency.level === null ? <span className="no-data-value">No data</span> : competency.level}</td>
                      <td>
                          <span className={competency.confidence === 'LOW' ? 'low-confidence' : 'confidence-label'}>
                            {confidenceLabel}
                          </span>
                      </td>
                      <td>
                        <button
                          type="button"
                          aria-expanded={selectedCode === competency.code}
                          onClick={() => toggleCompetency(competency.code)}
                        >
                          {selectedCode === competency.code ? 'Hide explanation' : 'Explain'}
                        </button>
                      </td>
                    </tr>
                  )
                })}
                </tbody>
              </table>
            </div>
          </section>
        </>
      ) : null}

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
