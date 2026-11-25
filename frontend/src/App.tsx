import { useEffect, useState } from 'react'
import { api } from './api/client'
import HealthBadge from './features/health/HealthBadge'
import GroupsPage from './features/grouping/GroupsPage'
import StudentList from './features/students/StudentList'

function App() {
  const [activeView, setActiveView] = useState<'students' | 'groups'>('students')
  const [contextId, setContextId] = useState<number | null>(null)

  useEffect(() => {
    let active = true
    api.groupingContexts()
      .then((contexts) => {
        if (active) setContextId(contexts[0]?.id ?? null)
      })
      .catch(() => {
        if (active) setContextId(null)
      })
    return () => { active = false }
  }, [])

  return (
    <div className="page-shell">
      <header className="topbar">
        <h1>StudyMatch</h1>
        <HealthBadge />
      </header>

      <nav className="app-navigation" aria-label="Main navigation" role="tablist">
        <button
          id="students-tab"
          type="button"
          role="tab"
          aria-selected={activeView === 'students'}
          aria-controls="app-content"
          onClick={() => setActiveView('students')}
        >
          Students
        </button>
        <button
          id="groups-tab"
          type="button"
          role="tab"
          aria-selected={activeView === 'groups'}
          aria-controls="app-content"
          onClick={() => setActiveView('groups')}
        >
          Groups
        </button>
      </nav>

      <main className="main-panel" id="app-content" role="tabpanel" aria-labelledby={`${activeView}-tab`}>
        {activeView === 'groups'
          ? contextId === null
            ? <p role="status">Loading grouping contexts...</p>
            : <GroupsPage contextId={contextId} />
          : <StudentList />}
      </main>
    </div>
  )
}

export default App
