import type { AssessmentSeason } from '../../api/types'

/** Degree programmes in the database (Flyway V3). */
export const DEGREE_PROGRAMMES = [{ code: 'LEI', name: 'Licenciatura em Engenharia Informática' }]

/** Assessment seasons accepted by POST /api/students/{id}/attempts. */
export const SEASONS: { value: AssessmentSeason; label: string }[] = [
  { value: 'NORMAL', label: 'Regular' },
  { value: 'RECURSO', label: 'Resit' },
  { value: 'ESPECIAL', label: 'Special' },
  { value: 'MELHORIA', label: 'Improvement' },
]
