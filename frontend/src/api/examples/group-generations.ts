import type { GroupAreaSummary, GroupGeneration, GroupMemberSummary } from '../types'

const areaCatalog = [
  { code: 'PROG', name: 'Programming' },
  { code: 'DB', name: 'Databases' },
  { code: 'WEB', name: 'Web' },
  { code: 'QUAL', name: 'Quality' },
  { code: 'MATH', name: 'Mathematics' },
  { code: 'SOFT', name: 'Soft skills' },
]

const studentNames = [
  'Ana Ribeiro', 'Bruno Silva', 'Carla Costa', 'Diogo Almeida', 'Eva Martins',
  'Filipe Rocha', 'Guilherme Santos', 'Ines Carvalho', 'Joao Ferreira', 'Leonor Sousa',
  'Manuel Lopes', 'Marta Oliveira', 'Nuno Pereira', 'Patricia Gomes', 'Rafael Moreira',
  'Sara Fernandes', 'Tiago Rodrigues', 'Vera Teixeira', 'Andre Monteiro', 'Beatriz Correia',
  'Duarte Pinto', 'Mariana Neves', 'Rui Cardoso',
]

const demoStudents: Omit<GroupMemberSummary, 'explanation'>[] = studentNames.map((name, studentIndex) => ({
  studentId: studentIndex + 1,
  number: `ST-${String(studentIndex + 1).padStart(3, '0')}`,
  name,
  areas: areaCatalog.map((area, areaIndex) => ({
    ...area,
    level: (studentIndex * 19 + areaIndex * 27 + 23) % 101,
    confidence: studentIndex % 6 === 0 ? 0.18 : 0.58 + ((studentIndex + areaIndex) % 4) * 0.1,
  })),
}))

const groupSizes = [5, 5, 5, 4, 4]

export function createDemoGeneration(id: number, generatedAt: string): GroupGeneration {
  let firstMemberIndex = 0
  const groups = groupSizes.map((groupSize, groupIndex) => {
    const members: GroupMemberSummary[] = demoStudents
      .slice(firstMemberIndex, firstMemberIndex + groupSize)
      .map((student) => {
        const strongestArea = [...student.areas].sort(
          (first, second) => (second.level ?? -1) - (first.level ?? -1),
        )[0]
        return {
          ...student,
          explanation: `Brings a strong ${strongestArea.name.toLowerCase()} contribution to the team.`,
        }
      })
    firstMemberIndex += groupSize

    const areas: GroupAreaSummary[] = areaCatalog.map((area) => {
      const levels = members.map((member) => member.areas.find((memberArea) => memberArea.code === area.code)?.level ?? 0)
      return {
        ...area,
        level: Math.round(levels.reduce((sum, level) => sum + level, 0) / levels.length),
      }
    })
    const coveredAreas = areas.filter((area) => (area.level ?? 0) >= 55).map((area) => area.name.toLowerCase())
    const coverageText = coveredAreas.length > 0 ? coveredAreas.join(', ') : 'a range of developing competencies'

    return {
      id: id * 10 + groupIndex + 1,
      number: groupIndex + 1,
      score: Math.round((91 - groupIndex * 2.7) * 10) / 10,
      areas,
      explanation: `The team combines strengths across ${coverageText}, with members balancing one another in less represented areas.`,
      members,
    }
  })

  return {
    id,
    generatedAt,
    score: Math.round(groups.reduce((sum, group) => sum + group.score, 0) / groups.length * 10) / 10,
    groups,
  }
}

export const demoGroupGenerations: GroupGeneration[] = [
  createDemoGeneration(2, '2026-11-08T10:35:00Z'),
  createDemoGeneration(1, '2026-11-06T14:10:00Z'),
]

export function getDemoGenerationHistory() {
  return demoGroupGenerations.map((generation) => ({
    id: generation.id,
    generatedAt: generation.generatedAt,
    score: generation.score,
    groupCount: generation.groups.length,
    warning: generation.warning,
  }))
}