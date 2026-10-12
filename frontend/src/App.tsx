import HealthBadge from './features/health/HealthBadge'
import StudentList from './features/students/StudentList'

function App() {
  return (
    <div className="page-shell">
      <header className="topbar">
        <h1>StudyMatch</h1>
        <HealthBadge />
      </header>

      <main className="main-panel">
        <StudentList />
      </main>
    </div>
  )
}

export default App
