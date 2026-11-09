import { type FormEvent, useState } from 'react'
import type { GroupingContextDraft } from '../../api/types'

type GroupingContextFormProps = {
  onSubmit: (draft: GroupingContextDraft) => Promise<void>
  onCancel: () => void
}

function GroupingContextForm({ onSubmit, onCancel }: GroupingContextFormProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const trimmedName = name.trim()
    if (!trimmedName) {
      setError('Enter a context name.')
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      await onSubmit({ name: trimmedName, description: description.trim() })
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Unable to create context.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="grouping-context-form" onSubmit={handleSubmit} noValidate>
      <div className="grouping-context-form-heading">
        <div>
          <p className="section-kicker">New grouping context</p>
          <h3>Create a context</h3>
        </div>
      </div>
      <label>
        <span>Name</span>
        <input
          aria-label="Context name"
          autoComplete="off"
          aria-invalid={Boolean(error && !name.trim())}
          aria-describedby={error ? 'context-form-error' : undefined}
          maxLength={120}
          value={name}
          onChange={(event) => {
            setName(event.target.value)
            setError(null)
          }}
        />
      </label>
      <label>
        <span>Description</span>
        <textarea
          aria-label="Context description"
          rows={3}
          maxLength={500}
          value={description}
          onChange={(event) => setDescription(event.target.value)}
        />
      </label>
      {error ? <p className="grouping-form-error" id="context-form-error" role="alert">{error}</p> : null}
      <div className="grouping-context-form-actions">
        <button type="submit" disabled={submitting}>{submitting ? 'Creating...' : 'Create context'}</button>
        <button type="button" className="secondary-button" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  )
}

export default GroupingContextForm