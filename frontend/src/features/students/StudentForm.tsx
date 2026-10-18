import { type ChangeEvent, type FormEvent, useState } from 'react'

export type StudentFormValues = {
  number: string
  name: string
  email: string
  currentYear: number
}

type StudentFormProps = {
  initialValues?: Partial<StudentFormValues>
  onSubmit: (values: StudentFormValues) => Promise<void> | void
  onCancel: () => void
  submitLabel?: string
}

const defaultValues: StudentFormValues = {
  number: '',
  name: '',
  email: '',
  currentYear: 1,
}

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function StudentForm({
  initialValues,
  onSubmit,
  onCancel,
  submitLabel = 'Save student',
}: StudentFormProps) {
  const [values, setValues] = useState<StudentFormValues>({
    ...defaultValues,
    ...initialValues,
    currentYear: Number(initialValues?.currentYear ?? defaultValues.currentYear),
  })
  const [errors, setErrors] = useState<Partial<Record<keyof StudentFormValues, string>>>({})
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const updateValue = (field: keyof StudentFormValues, rawValue: string) => {
    setValues((current) => ({
      ...current,
      [field]: field === 'currentYear' ? Number(rawValue) : rawValue,
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

    if (!Number.isInteger(values.currentYear) || values.currentYear < 1 || values.currentYear > 5) {
      nextErrors.currentYear = 'Current year must be between 1 and 5.'
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
        currentYear: Number(values.currentYear),
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
  ) => (event: ChangeEvent<HTMLInputElement>) => {
    updateValue(field, event.target.value)
  }

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
          <span>Current year</span>
          <input
            aria-label="Current year"
            type="number"
            min={1}
            max={5}
            name="currentYear"
            value={values.currentYear}
            onChange={handleInput('currentYear')}
            aria-invalid={Boolean(errors.currentYear)}
          />
          {errors.currentYear ? <small>{errors.currentYear}</small> : null}
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
