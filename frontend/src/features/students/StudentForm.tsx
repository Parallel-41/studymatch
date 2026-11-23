import { type ChangeEvent, type FormEvent, useState } from 'react'
import type { StudentDraft } from '../../api/client'
import { academicYearStart, yearOfStudy } from './academicYear'
import { DEGREE_PROGRAMMES } from './studentOptions'

export type StudentFormValues = StudentDraft

type StudentFormProps = {
  initialValues?: Partial<StudentFormValues>
  onSubmit: (values: StudentFormValues) => Promise<void> | void
  onCancel: () => void
  submitLabel?: string
}

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function StudentForm({
  initialValues,
  onSubmit,
  onCancel,
  submitLabel = 'Save student',
}: StudentFormProps) {
  const firstYear = academicYearStart()
  const [values, setValues] = useState<StudentFormValues>({
    number: '',
    name: '',
    email: '',
    registrationYear: firstYear,
    degreeProgramCode: DEGREE_PROGRAMMES[0].code,
    ...initialValues,
  })
  const [errors, setErrors] = useState<Partial<Record<keyof StudentFormValues, string>>>({})
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const updateValue = (field: keyof StudentFormValues, rawValue: string) => {
    setValues((current) => ({
      ...current,
      [field]: field === 'registrationYear' ? Number(rawValue) : rawValue,
    }))
    setErrors((current) => ({ ...current, [field]: undefined }))
    setSubmitError(null)
  }

  const validate = () => {
    const nextErrors: Partial<Record<keyof StudentFormValues, string>> = {}

    if (!values.number.trim()) {
      nextErrors.number = 'Student number is required.'
    }

    if (!values.name.trim()) {
      nextErrors.name = 'Full name is required.'
    }

    if (!emailPattern.test(values.email.trim())) {
      nextErrors.email = 'Enter a valid email address.'
    }

    const year = values.registrationYear
    if (!Number.isInteger(year) || year < 1900 || year > firstYear) {
      nextErrors.registrationYear = `Registration year must be between 1900 and ${firstYear}.`
    }

    if (!values.degreeProgramCode) {
      nextErrors.degreeProgramCode = 'Choose a degree programme.'
    }

    return nextErrors
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const nextErrors = validate()
    setErrors(nextErrors)

    if (Object.keys(nextErrors).length > 0) {
      return
    }

    setSubmitting(true)
    try {
      await onSubmit({
        number: values.number.trim(),
        name: values.name.trim(),
        email: values.email.trim(),
        registrationYear: Number(values.registrationYear),
        degreeProgramCode: values.degreeProgramCode,
      })
      setSubmitError(null)
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : 'Unable to save student.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleInput = (
    field: keyof StudentFormValues,
  ) => (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    updateValue(field, event.target.value)
  }

  const validYear = Number.isInteger(values.registrationYear) && values.registrationYear >= 1900
    && values.registrationYear <= firstYear

  return (
    <form className="student-form" onSubmit={handleSubmit} noValidate>
      <div className="student-form-grid">
        <label>
          <span>Student number</span>
          <input
            aria-label="Student number"
            name="number"
            value={values.number}
            onChange={handleInput('number')}
            aria-invalid={Boolean(errors.number)}
          />
          {errors.number ? <small>{errors.number}</small> : null}
        </label>

        <label>
          <span>Full name</span>
          <input
            aria-label="Full name"
            name="name"
            value={values.name}
            onChange={handleInput('name')}
            aria-invalid={Boolean(errors.name)}
          />
          {errors.name ? <small>{errors.name}</small> : null}
        </label>

        <label>
          <span>Email</span>
          <input
            aria-label="Email"
            type="email"
            name="email"
            value={values.email}
            onChange={handleInput('email')}
            aria-invalid={Boolean(errors.email)}
          />
          {errors.email ? <small>{errors.email}</small> : null}
        </label>

        <label>
          <span>Registration year</span>
          <input
            aria-label="Registration year"
            type="number"
            min={1900}
            max={firstYear}
            name="registrationYear"
            value={values.registrationYear}
            onChange={handleInput('registrationYear')}
            aria-invalid={Boolean(errors.registrationYear)}
          />
          {errors.registrationYear ? (
            <small>{errors.registrationYear}</small>
          ) : validYear ? (
            <small className="field-hint">Year of study: {yearOfStudy(values.registrationYear)}</small>
          ) : null}
        </label>

        <label>
          <span>Degree programme</span>
          <select
            aria-label="Degree programme"
            name="degreeProgramCode"
            value={values.degreeProgramCode}
            onChange={handleInput('degreeProgramCode')}
            aria-invalid={Boolean(errors.degreeProgramCode)}
          >
            {DEGREE_PROGRAMMES.map((programme) => (
              <option key={programme.code} value={programme.code}>
                {programme.code} — {programme.name}
              </option>
            ))}
          </select>
          {errors.degreeProgramCode ? <small>{errors.degreeProgramCode}</small> : null}
        </label>
      </div>

      {submitError ? (
        <div className="error-box" role="alert">
          {submitError}
        </div>
      ) : null}

      <div className="student-form-actions">
        <button type="submit" disabled={submitting}>
          {submitting ? 'Saving...' : submitLabel}
        </button>
        <button type="button" className="secondary-button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  )
}

export default StudentForm
