import { useState } from 'react'
import HealthBadge from './features/health/HealthBadge'
import GroupsPage from './features/grouping/GroupsPage'
import StudentList from './features/students/StudentList'

function App() {
  const [activeView, setActiveView] = useState<'students' | 'groups'>('students')

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
        {activeView === 'groups' ? <GroupsPage contextId={1} /> : <StudentList />}
      </main>
    </div>
  )
}

export default App
