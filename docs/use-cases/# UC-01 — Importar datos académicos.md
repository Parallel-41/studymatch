\# UC-01 — Importar datos académicos



**\*\*Objective:\*\*** Cargar masivamente en el sistema la información de estudiantes, unidades curriculares, matrículas y notas mediante un archivo CSV\[cite: 1, 2].

**\*\*Primary actor(s):\*\*** Administrador.

**\*\*Capability area:\*\*** Identity / Academic trajectory.

**\*\*Related issue:\*\*** #8\[cite: 1].



\## Main success scenario

1\. El Administrador accede a la sección de importación de datos.

2\. Sube un archivo CSV estructurado con el historial académico.

3\. El Sistema valida el formato del archivo.

4\. El Sistema crea o actualiza los registros en la base de datos.

5\. El Sistema muestra un mensaje de éxito con los registros importados.



\## Alternative / exceptional flows

\- **\*\*3a.\*\*** El archivo CSV tiene un formato incorrecto → El Sistema rechaza el archivo y muestra un error.



\## Business rules / constraints

\- BR-01-1: Las notas importadas deben ser valores entre 0 y 20\[cite: 2, 3].



\## Domain concepts revealed

\- Student — La identidad del alumno\[cite: 3].

\- CourseUnit — La asignatura o unidad curricular\[cite: 3].

\- Enrolment — La matrícula de un estudiante\[cite: 3].

\- Attempt — El intento de evaluación con la nota\[cite: 3].

