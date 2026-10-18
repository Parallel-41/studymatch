import { useEffect, useState } from 'react'
import { api } from '../../api/client'

function HealthBadge() {
  const [status, setStatus] = useState<'checking' | 'up' | 'down'>('checking')

  useEffect(() => {
    let isMounted = true

    const loadStatus = async () => {
      try {
        const response = await api.health()
        if (isMounted) {
          setStatus(response.status === 'UP' ? 'up' : 'down')
        }
      } catch {
        if (isMounted) {
          setStatus('down')
        }
      }
    }

    void loadStatus()

    return () => {
      isMounted = false
    }
  }, [])

  const label =
    status === 'checking' ? 'checking...' : status === 'up' ? 'API up' : 'API down'

  return (
    <div className="status-panel" aria-live="polite">
      <span className={`status-badge status-${status}`}>{label}</span>
    </div>
  )
}

export default HealthBadge
