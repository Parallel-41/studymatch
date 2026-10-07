\# UC-08 — Crear un contexto de agrupamiento



**\*\*Objective:\*\*** Permitir al docente definir una actividad y el tamaño objetivo de los grupos para su asignatura\[cite: 1, 3].

**\*\*Primary actor(s):\*\*** Docente

**\*\*Capability area:\*\*** Grouping context

**\*\*Related issue:\*\*** #10\[cite: 1]



\## Main success scenario

1\. El Docente accede a su unidad curricular y selecciona "Nuevo agrupamiento".

2\. Introduce el título y descripción de la actividad.

3\. Define el tamaño mínimo y máximo del grupo.

4\. El Sistema valida los datos y guarda el contexto de agrupamiento.



\## Alternative / exceptional flows

\- **\*\*3a.\*\*** El tamaño mínimo es mayor que el máximo → El Sistema muestra un error y pide corregir los valores.



\## Business rules / constraints

\- BR-08-1: El tamaño mínimo y máximo de los grupos debe ser al menos 2\[cite: 1].



\## Domain concepts revealed

\- Teacher — El creador del contexto\[cite: 3].

\- CourseUnit — La asignatura donde se hace el grupo\[cite: 3].

\- GroupingContext — La entidad que guarda la configuración del agrupamiento\[cite: 3].

