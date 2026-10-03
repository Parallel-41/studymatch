import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import type { Student } from '../../api/types'

function StudentList() {
  const [students, setStudents] = useState<Student[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isMounted = true

    const loadStudents = async () => {
      try {
        const data = await api.students()
        if (isMounted) {
          setStudents(data)
          setError(null)
        }
      } catch (err) {
        if (isMounted) {
          setError(err instanceof Error ? err.message : 'Unable to load students')
        }
      } finally {
        if (isMounted) {
          setLoading(false)
        }
      }
    }

    void loadStudents()

    return () => {
      isMounted = false
    }
  }, [])

  if (loading) {
    return <p>Loading students...</p>
  }

  if (error) {
    return (
      <div className="error-box" role="alert">
        Unable to load students. Please try again later.
      </div>
    )
  }

  if (students.length === 0) {
    return <p>No students yet.</p>
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Number</th>
            <th>Name</th>
            <th>Email</th>
            <th>Year</th>
          </tr>
        </thead>
        <tbody>
          {students.map((student) => (
            <tr key={student.id}>
              <td>{student.number}</td>
              <td>{student.name}</td>
              <td>{student.email}</td>
              <td>{student.currentYear}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default StudentList
