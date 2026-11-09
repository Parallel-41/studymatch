import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import type { GeneratedGroup, GroupGeneration, GroupGenerationHistoryItem } from '../../api/types'

type GroupsPageProps = {
  contextId: number
}

function formatGeneratedAt(value: string) {
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function GroupCard({ group }: { group: GeneratedGroup }) {
  return (
    <article className="group-card" aria-labelledby={`group-${group.id}-title`}>
      <header className="group-card-heading">
        <div>
          <p className="section-kicker">Team {String(group.number).padStart(2, '0')}</p>
          <h3 id={`group-${group.id}-title`}>Group {group.number}</h3>
        </div>
        <span className="group-score" aria-label={`Group score ${group.score.toFixed(1)} out of 100`}>
          {group.score.toFixed(1)} <small>/ 100</small>
        </span>
      </header>

      <p className="group-explanation">{group.explanation}</p>

      <div className="group-area-summary" aria-label={`Group ${group.number} area profile`}>
        {group.areas.map((area) => (
          <div className="group-area" key={area.code}>
            <div className="group-area-label"><span>{area.name}</span><strong>{area.level ?? 'No data'}</strong></div>
            <div className="group-area-track" aria-hidden="true">
              <span style={{ width: `${area.level ?? 0}%` }} />
            </div>
          </div>
        ))}
      </div>

      <ul className="group-members">
        {group.members.map((member) => (
          <li className="group-member" key={member.studentId}>
            <div className="group-member-identity">
              <strong>{member.name}</strong>
              <span>{member.number}</span>
            </div>
            <div className="member-area-list" aria-label={`${member.name} profile areas`}>
              {member.areas.map((area) => (
                <span key={area.code} title={`${area.name}: ${area.level} (${Math.round(area.confidence * 100)}% confidence)`}>
                  {area.code}
                </span>
              ))}
            </div>
            <p className="member-explanation">{member.explanation}</p>
          </li>
        ))}
      </ul>
    </article>
  )
}

function GroupsPage({ contextId }: GroupsPageProps) {
  const [generations, setGenerations] = useState<GroupGenerationHistoryItem[]>([])
  const [selectedGeneration, setSelectedGeneration] = useState<GroupGeneration | null>(null)
  const [selectedGenerationId, setSelectedGenerationId] = useState<number | null>(null)
  const [loadedContextId, setLoadedContextId] = useState<number | null>(null)
  const [loadError, setLoadError] = useState<{ contextId: number; message: string } | null>(null)
  const [generateError, setGenerateError] = useState<string | null>(null)
  const [detailError, setDetailError] = useState<string | null>(null)
  const [generating, setGenerating] = useState(false)
  const [openingGenerationId, setOpeningGenerationId] = useState<number | null>(null)

  useEffect(() => {
    let active = true
    api.groupGenerations(contextId)
      .then(async (items) => {
        const latest = items[0] ? await api.groupGeneration(contextId, items[0].id) : null
        if (active) {
          setGenerations(items)
          setSelectedGeneration(latest)
          setSelectedGenerationId(latest?.id ?? null)
          setLoadedContextId(contextId)
          setLoadError(null)
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setLoadError({
            contextId,
            message: error instanceof Error ? error.message : 'Unable to load generation history.',
          })
        }
      })
    return () => {
      active = false
    }
  }, [contextId])

  const loading = loadedContextId !== contextId && loadError?.contextId !== contextId
  const currentLoadError = loadError?.contextId === contextId ? loadError.message : null

  const handleOpenGeneration = async (generationId: number) => {
    setSelectedGenerationId(generationId)
    setOpeningGenerationId(generationId)
    setDetailError(null)
    try {
      const generation = await api.groupGeneration(contextId, generationId)
      if (generation) {
        setSelectedGeneration(generation)
      } else {
        setDetailError('This generation is no longer available.')
      }
    } catch (error) {
      setDetailError(error instanceof Error ? error.message : 'Unable to open this generation.')
    } finally {
      setOpeningGenerationId(null)
    }
  }

  const handleGenerate = async () => {
    setGenerating(true)
    setGenerateError(null)
    try {
      const generation = await api.generateGroups(contextId)
      setGenerations((current) => [{
        id: generation.id,
        generatedAt: generation.generatedAt,
        score: generation.score,
        groupCount: generation.groups.length,
        warning: generation.warning,
      }, ...current.filter((item) => item.id !== generation.id)])
      setSelectedGeneration(generation)
      setSelectedGenerationId(generation.id)
    } catch (error) {
      setGenerateError(error instanceof Error ? error.message : 'Unable to generate groups.')
    } finally {
      setGenerating(false)
    }
  }

  return (
    <section className="groups-page" aria-labelledby="groups-title">
      <header className="groups-page-heading">
        <div>
          <p className="section-kicker">Grouping context · 2026/27</p>
          <h2 id="groups-title">Software project teams</h2>
          <p className="groups-context-meta">LEI · Semester project <span>23 participants</span></p>
        </div>
        <button type="button" className="generate-groups-button" disabled={generating || loading} onClick={() => void handleGenerate()}>
          {generating ? 'Generating...' : 'Generate groups'}
        </button>
      </header>

      {loading ? <p role="status">Loading generations...</p> : null}
      {currentLoadError ? <p className="error-box" role="alert">{currentLoadError}</p> : null}
      {generateError ? <p className="group-warning" role="alert">{generateError}</p> : null}
      {detailError ? <p className="error-box" role="alert">{detailError}</p> : null}

      {!loading && !currentLoadError ? (
        <div className="groups-workspace">
          <aside className="generation-history" aria-labelledby="generation-history-title">
            <div className="generation-history-heading">
              <h3 id="generation-history-title">Generation history</h3>
              <span>{generations.length}</span>
            </div>
            {generations.length === 0 ? <p>No generations yet.</p> : (
              <ul>
                {generations.map((generation) => (
                  <li key={generation.id}>
                    <button
                      type="button"
                      className="generation-history-item"
                      aria-pressed={generation.id === selectedGenerationId}
                      onClick={() => void handleOpenGeneration(generation.id)}
                    >
                      <strong>{formatGeneratedAt(generation.generatedAt)}</strong>
                      <span>{generation.groupCount} groups · score {generation.score.toFixed(1)}</span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </aside>

          <div className="generation-detail">
            {openingGenerationId !== null ? <p role="status">Opening generation...</p> : null}
            {openingGenerationId === null && selectedGeneration ? (
              <>
                {selectedGeneration.warning ? (
                  <p className="group-warning" role="status">{selectedGeneration.warning}</p>
                ) : null}
                <header className="generation-summary">
                  <div>
                    <h3>Generated teams</h3>
                    <p>{selectedGeneration.groups.length} groups · {selectedGeneration.groups.reduce((count, group) => count + group.members.length, 0)} students</p>
                  </div>
                  <span className="generation-total-score">Overall score <strong>{selectedGeneration.score.toFixed(1)}</strong></span>
                </header>
                <div className="groups-grid">
                  {selectedGeneration.groups.map((group) => <GroupCard group={group} key={group.id} />)}
                </div>
              </>
            ) : openingGenerationId === null && !loading ? <p className="groups-empty">Generate groups to see the proposed teams.</p> : null}
          </div>
        </div>
      ) : null}
    </section>
  )
}

export default GroupsPage