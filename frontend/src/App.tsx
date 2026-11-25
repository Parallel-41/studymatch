import { useState } from 'react'
import HealthBadge from './features/health/HealthBadge'
import GroupingContexts from './features/grouping/GroupingContexts'
import StudentList from './features/students/StudentList'

function App() {
  const [activeView, setActiveView] = useState<'students' | 'contexts'>('students')

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
          id="contexts-tab"
          type="button"
          role="tab"
          aria-selected={activeView === 'contexts'}
          aria-controls="app-content"
          onClick={() => setActiveView('contexts')}
        >
          Contexts
        </button>
      </nav>

      <main className="main-panel" id="app-content" role="tabpanel" aria-labelledby={`${activeView}-tab`}>
        {activeView === 'contexts' ? <GroupingContexts /> : <StudentList />}
      </main>
    </div>
  )
}

export default App
