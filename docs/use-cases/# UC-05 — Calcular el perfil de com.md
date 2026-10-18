\# UC-05 — Calcular el perfil de competencias a partir de las notas



**\*\*Objective:\*\*** Calcular automáticamente el nivel de competencia del estudiante (de 0 a 100) basándose en su historial.

**\*\*Primary actor(s):\*\*** Sistema\[cite: 1]

**\*\*Capability area:\*\*** Profile\[cite: 1]

**\*\*Related issue:\*\*** #9\[cite: 1]



\## Main success scenario

1\. El Sistema detecta nuevas notas validadas en el expediente de un Estudiante.

2\. Recupera las competencias y los pesos asignados.

3\. Calcula el nivel alcanzado en cada competencia.

4\. Actualiza el perfil de competencias del estudiante.



\## Alternative / exceptional flows

\- **\*\*1a.\*\*** El estudiante no tiene notas previas → El Sistema mantiene el perfil vacío o usa los datos del quiz inicial.



\## Business rules / constraints

\- BR-05-1: Solo se computan los intentos con nota de aprobado.



\## Domain concepts revealed

\- StudentProfile — El perfil que agrupa los niveles\[cite: 3].

\- CompetencyLevel — El valor numérico de la habilidad\[cite: 3].

\- Attempt — La evaluación que aporta la nota\[cite: 3].

