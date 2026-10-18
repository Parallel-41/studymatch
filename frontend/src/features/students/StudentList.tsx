import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import type { Student } from '../../api/types'
import StudentForm, { type StudentFormValues } from './StudentForm'
import StudentHistory from './StudentHistory'

function StudentList() {
  const [students, setStudents] = useState<Student[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [editingStudent, setEditingStudent] = useState<Student | null>(null)
  const [historyStudent, setHistoryStudent] = useState<Student | null>(null)

  useEffect(() => {
    let isMounted = true

    const loadStudents = async () => {
      try {
        const data = await api.students(search)
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
  }, [search])

  const handleSubmit = async (values: StudentFormValues) => {
    if (editingStudent) {
      const updated = await api.updateStudent({
        ...editingStudent,
        ...values,
      })

      setStudents((current) =>
        current.map((student) => (student.id === updated.id ? updated : student)),
      )
      setEditingStudent(null)
      return
    }

    const created = await api.createStudent(values)
    setStudents((current) => [created, ...current])
    setEditingStudent(null)
  }

  const handleCancel = () => {
    setEditingStudent(null)
  }

  if (loading) {
    return <p>Loading students...</p>
  }

  return (
    <div className="student-list-shell">
      <div className="student-toolbar">
        <div className="search-box">
          <label htmlFor="student-search">Search students</label>
          <input
            id="student-search"
            type="search"
            value={search}
            placeholder="Search by name, number, or email"
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <button
          type="button"
          onClick={() => setEditingStudent({
            id: 0,
            number: '',
            name: '',
            email: '',
            currentYear: 1,
          })}
        >
          New student
        </button>
      </div>

      {error ? (
        <div className="error-box" role="alert">
          {error}
        </div>
      ) : null}

      {editingStudent ? (
        <div className="student-form-panel">
          <h2>{editingStudent.id ? 'Edit student' : 'Create student'}</h2>
          <StudentForm
            initialValues={
              editingStudent.id
                ? {
                    number: editingStudent.number,
                    name: editingStudent.name,
                    email: editingStudent.email,
                    currentYear: editingStudent.currentYear,
                  }
                : undefined
            }
            onSubmit={handleSubmit}
            onCancel={handleCancel}
            submitLabel={editingStudent.id ? 'Save changes' : 'Create student'}
          />
        </div>
      ) : null}

      {students.length === 0 ? (
        <p>No students yet.</p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Number</th>
                <th>Name</th>
                <th>Email</th>
                <th>Year</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {students.map((student) => (
                <tr key={student.id}>
                  <td>{student.number}</td>
                  <td>{student.name}</td>
                  <td>{student.email}</td>
                  <td>{student.currentYear}</td>
                  <td>
                    <button type="button" onClick={() => setEditingStudent(student)}>
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => setHistoryStudent((current) => current?.id === student.id ? null : student)}
                    >
                      {historyStudent?.id === student.id ? 'Hide history' : 'History'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {historyStudent ? (
        <div className="student-history-panel">
          <h2>{historyStudent.name}</h2>
          <StudentHistory studentId={historyStudent.id} />
        </div>
      ) : null}
    </div>
  )
}

export default StudentList
