import { useState } from 'react'
import type { GroupingEligibleStudent } from '../../api/types'

type GroupingStudentPickerProps = {
  students: GroupingEligibleStudent[]
  selectedStudentIds: number[]
  onChange: (studentIds: number[]) => void
}

function GroupingStudentPicker({ students, selectedStudentIds, onChange }: GroupingStudentPickerProps) {
  const [query, setQuery] = useState('')
  const normalizedQuery = query.trim().toLowerCase()
  const visibleStudents = students.filter((student) =>
    [student.name, student.number, student.email].join(' ').toLowerCase().includes(normalizedQuery),
  )

  const toggleStudent = (studentId: number) => {
    onChange(selectedStudentIds.includes(studentId)
      ? selectedStudentIds.filter((id) => id !== studentId)
      : [...selectedStudentIds, studentId])
  }

  return (
    <section className="grouping-student-picker" aria-labelledby="student-picker-title">
      <header className="student-picker-heading">
        <div>
          <p className="section-kicker">Participants</p>
          <h3 id="student-picker-title">Choose students</h3>
        </div>
        <span className="participant-count">{selectedStudentIds.length} selected</span>
      </header>

      <label className="student-picker-search">
        <span>Search students</span>
        <input
          aria-label="Search students"
          type="search"
          placeholder="Name, student number or email"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
      </label>

      {visibleStudents.length === 0 ? (
        <p className="student-picker-empty">{students.length ? 'No students match this search.' : 'No students available.'}</p>
      ) : (
        <ul className="grouping-student-list">
          {visibleStudents.map((student) => (
            <li className="grouping-student-row" key={student.id}>
              <input
                type="checkbox"
                aria-label={`Select ${student.name}`}
                checked={selectedStudentIds.includes(student.id)}
                onChange={() => toggleStudent(student.id)}
              />
              <div className="grouping-student-identity">
                <strong>{student.name}</strong>
                <span>{student.number} · Year {student.currentYear}</span>
              </div>
              <div className="grouping-student-status">
                {student.coldStart ? <span className="cold-start-badge">Cold start</span> : null}
                <div className="student-area-summary" aria-label={`${student.name} profile summary`}>
                  {student.profile.areas.map((area) => (
                    <span key={area.code} title={`${area.name}: ${area.level === null ? 'No data' : area.level}`}>
                      {area.code} {area.level === null ? '—' : Math.round(area.level)}
                    </span>
                  ))}
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

export default GroupingStudentPicker