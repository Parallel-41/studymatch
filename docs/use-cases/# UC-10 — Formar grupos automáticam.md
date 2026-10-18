\# UC-10 — Formar grupos automáticamente



**\*\*Objective:\*\*** Ejecutar el proceso que asigna a los estudiantes matriculados en equipos respetando las restricciones y buscando el mejor perfil\[cite: 1, 2].

**\*\*Primary actor(s):\*\*** Docente\[cite: 1]

**\*\*Capability area:\*\*** Grouping context\[cite: 1]

**\*\*Related issue:\*\*** #10\[cite: 1]



\## Main success scenario

1\. El Docente pulsa el botón "Generar Grupos" en un contexto de agrupamiento.

2\. El Sistema recupera la lista de estudiantes matriculados en la unidad curricular.

3\. El Sistema ejecuta el algoritmo de matching teniendo en cuenta perfiles y restricciones.

4\. El Sistema genera los grupos y muestra un resumen con la puntuación de emparejamiento (matching score).

5\. El Docente aprueba y publica los grupos.



\## Alternative / exceptional flows

\- **\*\*3a.\*\*** Es imposible cumplir todas las restricciones obligatorias → El Sistema genera los mejores grupos posibles y levanta una alerta sobre los alumnos no asignados.



\## Business rules / constraints

\- BR-10-1: Un estudiante solo puede ser asignado a un grupo si tiene una matrícula activa en la unidad curricular\[cite: 3].



\## Domain concepts revealed

\- GroupingContext — El contexto base\[cite: 3].

\- Group — El equipo generado y su puntuación\[cite: 3].

\- Student — El alumno asignado\[cite: 3].

