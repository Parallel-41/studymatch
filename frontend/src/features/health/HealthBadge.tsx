import { useEffect, useState } from 'react'
import { api } from '../../api/client'

function HealthBadge() {
  const [status, setStatus] = useState<'checking' | 'up' | 'down'>('checking')

  useEffect(() => {
    let isMounted = true

    const loadStatus = async () => {
      try {
        await api.health()
        if (isMounted) {
          setStatus('up')
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
