import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import type { GroupingContext, GroupingContextDraft, GroupingEligibleStudent } from '../../api/types'
import GroupingContextForm from './GroupingContextForm'
import GroupingStudentPicker from './GroupingStudentPicker'

function GroupingContexts() {
  const [contexts, setContexts] = useState<GroupingContext[]>([])
  const [contextsLoaded, setContextsLoaded] = useState(false)
  const [contextError, setContextError] = useState<string | null>(null)
  const [students, setStudents] = useState<GroupingEligibleStudent[]>([])
  const [studentsLoaded, setStudentsLoaded] = useState(false)
  const [studentsError, setStudentsError] = useState<string | null>(null)
  const [selectedContextId, setSelectedContextId] = useState<number | null>(null)
  const [selectedContext, setSelectedContext] = useState<GroupingContext | null>(null)
  const [loadedContextId, setLoadedContextId] = useState<number | null>(null)
  const [detailError, setDetailError] = useState<{ contextId: number; message: string } | null>(null)
  const [selectedStudentIds, setSelectedStudentIds] = useState<number[]>([])
  const [isCreating, setIsCreating] = useState(false)
  const [savingParticipants, setSavingParticipants] = useState(false)
  const [participantsError, setParticipantsError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    api.groupingContexts()
      .then((items) => {
        if (active) {
          setContexts(items)
          setSelectedContextId((current) => current ?? items[0]?.id ?? null)
          setContextError(null)
        }
      })
      .catch((error: unknown) => {
        if (active) setContextError(error instanceof Error ? error.message : 'Unable to load contexts.')
      })
      .finally(() => {
        if (active) setContextsLoaded(true)
      })

    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    let active = true
    api.groupingStudents()
      .then((items) => {
        if (active) {
          setStudents(items)
          setStudentsError(null)
        }
      })
      .catch((error: unknown) => {
        if (active) setStudentsError(error instanceof Error ? error.message : 'Unable to load students.')
      })
      .finally(() => {
        if (active) setStudentsLoaded(true)
      })

    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    if (selectedContextId === null) return
    let active = true
    api.groupingContext(selectedContextId)
      .then((context) => {
        if (active && context) {
          setSelectedContext(context)
          setLoadedContextId(context.id)
          setSelectedStudentIds(context.studentIds)
          setDetailError(null)
        } else if (active) {
          setDetailError({ contextId: selectedContextId, message: 'This context is no longer available.' })
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setDetailError({
            contextId: selectedContextId,
            message: error instanceof Error ? error.message : 'Unable to load this context.',
          })
        }
      })

    return () => {
      active = false
    }
  }, [selectedContextId])

  const createContext = async (draft: GroupingContextDraft) => {
    const created = await api.createGroupingContext(draft)
    setContexts((current) => [created, ...current.filter((context) => context.id !== created.id)])
    setSelectedContext(created)
    setLoadedContextId(created.id)
    setSelectedContextId(created.id)
    setSelectedStudentIds(created.studentIds)
    setIsCreating(false)
  }

  const saveParticipants = async () => {
    if (selectedContextId === null) return
    setSavingParticipants(true)
    setParticipantsError(null)
    try {
      const updated = await api.saveGroupingParticipants(selectedContextId, selectedStudentIds)
      setSelectedContext(updated)
      setLoadedContextId(updated.id)
      setContexts((current) => current.map((context) => context.id === updated.id ? updated : context))
    } catch (error) {
      setParticipantsError(error instanceof Error ? error.message : 'Unable to save participants.')
    } finally {
      setSavingParticipants(false)
    }
  }

  const currentContext = loadedContextId === selectedContextId ? selectedContext : null
  const currentDetailError = detailError?.contextId === selectedContextId ? detailError.message : null
  const contextLoading = !contextsLoaded && !contextError
  const studentsLoading = !studentsLoaded && !studentsError
  const detailLoading = selectedContextId !== null && !currentContext && !currentDetailError
  const participantsChanged = currentContext !== null
    && (selectedStudentIds.length !== currentContext.studentIds.length
      || selectedStudentIds.some((id) => !currentContext.studentIds.includes(id)))

  return (
    <section className="grouping-contexts-page" aria-labelledby="grouping-contexts-title">
      <header className="grouping-contexts-heading">
        <div>
          <p className="section-kicker">Team formation</p>
          <h2 id="grouping-contexts-title">Grouping contexts</h2>
          <p>Define a group activity and choose its participants.</p>
        </div>
        {!isCreating ? (
          <button type="button" className="new-context-button" onClick={() => setIsCreating(true)}>
            New context
          </button>
        ) : null}
      </header>

      {isCreating ? (
        <div className="grouping-context-form-panel">
          <GroupingContextForm onSubmit={createContext} onCancel={() => setIsCreating(false)} />
        </div>
      ) : null}

      <div className="grouping-contexts-layout">
        <aside className="grouping-context-list" aria-labelledby="context-list-title">
          <div className="context-list-heading">
            <h3 id="context-list-title">Contexts</h3>
            <span>{contexts.length}</span>
          </div>
          {contextLoading ? <p role="status">Loading contexts...</p> : null}
          {contextError ? <p className="error-box" role="alert">{contextError}</p> : null}
          {!contextLoading && !contextError && contexts.length === 0 ? (
            <p className="context-list-empty">No contexts yet.</p>
          ) : null}
          <ul>
            {contexts.map((context) => (
              <li key={context.id}>
                <button
                  type="button"
                  className="grouping-context-list-item"
                  aria-pressed={selectedContextId === context.id}
                  onClick={() => {
                    setSelectedContextId(context.id)
                    setParticipantsError(null)
                  }}
                >
                  <strong>{context.name}</strong>
                  <span>{context.studentIds.length} participants</span>
                </button>
              </li>
            ))}
          </ul>
        </aside>

        <div className="grouping-context-detail">
          {detailLoading ? <p role="status">Loading context...</p> : null}
          {currentDetailError ? <p className="error-box" role="alert">{currentDetailError}</p> : null}
          {!contextLoading && !contextError && !contexts.length && !isCreating ? (
            <p className="context-detail-empty">Create a context to choose its participants.</p>
          ) : null}
          {currentContext ? (
            <>
              <header className="selected-context-heading">
                <div>
                  <p className="section-kicker">Selected context</p>
                  <h3>{currentContext.name}</h3>
                  <p>{currentContext.description || 'No description provided.'}</p>
                </div>
                <span className="context-date">
                  Created {new Intl.DateTimeFormat('en', { dateStyle: 'medium' }).format(new Date(currentContext.createdAt))}
                </span>
              </header>

              {studentsLoading ? <p role="status">Loading student profiles...</p> : null}
              {studentsError ? <p className="error-box" role="alert">{studentsError}</p> : null}
              {studentsLoaded && !studentsError ? (
                <>
                  <GroupingStudentPicker
                    students={students}
                    selectedStudentIds={selectedStudentIds}
                    onChange={setSelectedStudentIds}
                  />
                  {participantsError ? <p className="error-box" role="alert">{participantsError}</p> : null}
                  <div className="participant-save-row">
                    <p>{selectedStudentIds.length} participants selected</p>
                    <button
                      type="button"
                      className="save-participants-button"
                      disabled={savingParticipants || !participantsChanged}
                      onClick={() => void saveParticipants()}
                    >
                      {savingParticipants ? 'Saving...' : 'Save participants'}
                    </button>
                  </div>
                </>
              ) : null}
            </>
          ) : null}
        </div>
      </div>
    </section>
  )
}

export default GroupingContexts