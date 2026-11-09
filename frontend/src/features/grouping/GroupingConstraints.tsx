import { type FormEvent, useEffect, useState } from 'react'
import { api, type GroupingConstraint, type GroupingConstraintType } from '../../api/client'
import type { Student } from '../../api/types'

type GroupingConstraintsProps = {
  contextId: number
  participants: Pick<Student, 'id' | 'name' | 'number'>[]
}

const typeLabels: Record<GroupingConstraintType, string> = {
  TOGETHER: 'Keep together',
  APART: 'Keep apart',
}

function GroupingConstraints({ contextId, participants }: GroupingConstraintsProps) {
  const [constraints, setConstraints] = useState<GroupingConstraint[]>([])
  const [loadedContextId, setLoadedContextId] = useState<number | null>(null)
  const [type, setType] = useState<GroupingConstraintType>('TOGETHER')
  const [studentIds, setStudentIds] = useState<string[]>([])
  const [saving, setSaving] = useState(false)
  const [removingId, setRemovingId] = useState<number | null>(null)
  const [loadError, setLoadError] = useState<{ contextId: number; message: string } | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [removeError, setRemoveError] = useState<string | null>(null)

  useEffect(() => {
    let active = true

    api.groupingConstraints(contextId)
      .then((items) => {
        if (active) {
          setConstraints(items)
          setLoadedContextId(contextId)
          setLoadError(null)
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setLoadError({
            contextId,
            message: error instanceof Error ? error.message : 'Unable to load constraints.',
          })
        }
      })

    return () => {
      active = false
    }
  }, [contextId])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (studentIds.length < 2) {
      setFormError('Choose at least two participants.')
      return
    }

    setSaving(true)
    setFormError(null)
    try {
      const created = await api.addGroupingConstraint(contextId, {
        type,
        studentIds: studentIds.map(Number),
      })
      setConstraints((current) => [...current, created])
      setStudentIds([])
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Unable to add constraint.')
    } finally {
      setSaving(false)
    }
  }

  const handleRemove = async (constraintId: number) => {
    setRemovingId(constraintId)
    setRemoveError(null)
    try {
      await api.removeGroupingConstraint(contextId, constraintId)
      setConstraints((current) => current.filter((constraint) => constraint.id !== constraintId))
    } catch (error) {
      setRemoveError(error instanceof Error ? error.message : 'Unable to remove constraint.')
    } finally {
      setRemovingId(null)
    }
  }

  const participantNames = (ids: number[]) => ids
    .map((id) => participants.find((participant) => participant.id === id)?.name ?? `Student ${id}`)
    .join(', ')
  const loading = loadedContextId !== contextId && loadError?.contextId !== contextId
  const currentLoadError = loadError?.contextId === contextId ? loadError.message : null

  return (
    <section className="grouping-constraints" aria-labelledby="grouping-constraints-title">
      <header className="grouping-constraints-heading">
        <div>
          <p className="section-kicker">Group formation</p>
          <h2 id="grouping-constraints-title">Participant constraints</h2>
        </div>
        <span className="constraint-count">{constraints.length} {constraints.length === 1 ? 'rule' : 'rules'}</span>
      </header>

      {currentLoadError ? <p className="error-box" role="alert">{currentLoadError}</p> : null}
      {loading ? <p role="status">Loading constraints...</p> : null}

      {!loading && !currentLoadError ? (
        <>
          <form className="constraint-form" onSubmit={handleSubmit} noValidate>
            <label>
              <span>Rule</span>
              <select
                aria-label="Rule"
                value={type}
                onChange={(event) => {
                  setType(event.target.value as GroupingConstraintType)
                  setFormError(null)
                }}
              >
                <option value="TOGETHER">Keep together</option>
                <option value="APART">Keep apart</option>
              </select>
            </label>
            <label className="constraint-participants">
              <span>Participants</span>
              <select
                aria-label="Participants"
                multiple
                value={studentIds}
                aria-describedby={formError ? 'constraint-form-error' : undefined}
                aria-invalid={Boolean(formError)}
                onChange={(event) => {
                  setStudentIds(Array.from(event.target.selectedOptions, (option) => option.value))
                  setFormError(null)
                }}
              >
                {participants.map((participant) => (
                  <option key={participant.id} value={participant.id}>
                    {participant.name} ({participant.number})
                  </option>
                ))}
              </select>
            </label>
            <button type="submit" disabled={saving || participants.length < 2}>
              {saving ? 'Adding...' : 'Add rule'}
            </button>
            {formError ? <p className="constraint-form-error" id="constraint-form-error" role="alert">{formError}</p> : null}
          </form>

          <div className="constraint-list" aria-label="Current constraints">
            {constraints.length === 0 ? (
              <p className="constraint-empty">No participant rules yet.</p>
            ) : constraints.map((constraint) => (
              <div className="constraint-row" key={constraint.id}>
                <p>
                  <strong>{typeLabels[constraint.type]}</strong>
                  <span>{participantNames(constraint.studentIds)}</span>
                </p>
                <button
                  type="button"
                  className="constraint-remove"
                  aria-label={`Remove ${typeLabels[constraint.type].toLowerCase()} rule for ${participantNames(constraint.studentIds)}`}
                  disabled={removingId === constraint.id}
                  onClick={() => void handleRemove(constraint.id)}
                >
                  {removingId === constraint.id ? 'Removing...' : 'Remove'}
                </button>
              </div>
            ))}
          </div>
          {removeError ? <p className="error-box" role="alert">{removeError}</p> : null}
        </>
      ) : null}
    </section>
  )
}

export default GroupingConstraints