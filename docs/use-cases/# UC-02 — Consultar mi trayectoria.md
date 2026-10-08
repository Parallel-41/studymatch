\# UC-02 — Consultar mi trayectoria académica



**\*\*Objective:\*\*** Permitir al estudiante visualizar su historial de asignaturas cursadas, matrículas y notas obtenidas\[cite: 1, 2].

**\*\*Primary actor(s):\*\*** Estudiante\[cite: 1].

**\*\*Capability area:\*\*** Academic trajectory\[cite: 1].

**\*\*Related issue:\*\*** #8\[cite: 1].



\## Main success scenario

1\. El Estudiante inicia sesión y accede a la sección de trayectoria académica.

2\. El Sistema recupera todas las matrículas asociadas a su número de estudiante.

3\. El Sistema muestra una lista de unidades curriculares agrupadas.

4\. El Estudiante selecciona una unidad curricular para ver sus intentos de evaluación y notas.



\## Alternative / exceptional flows

\- **\*\*2a.\*\*** El estudiante no tiene historial académico previo (Cold start) → El Sistema muestra un mensaje indicando que no hay datos y sugiere realizar la autoevaluación inicial\[cite: 2, 3].



\## Business rules / constraints

\- BR-02-1: Un estudiante solo puede visualizar su propia trayectoria académica.



\## Domain concepts revealed

\- Student — La identidad del alumno que hace la consulta\[cite: 3].

\- Enrolment — La matrícula en un año académico\[cite: 3].

\- CourseUnit — La asignatura consultada\[cite: 3].

\- Attempt — El intento de evaluación con la nota final\[cite: 3].

