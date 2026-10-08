\# UC-03 — Corregir datos de un estudiante



**\*\*Objective:\*\*** Permitir al administrador modificar información incorrecta en el registro académico de un estudiante.

**\*\*Primary actor(s):\*\*** Administrador.

**\*\*Capability area:\*\*** Student and academic identity\[cite: 2].

**\*\*Related issue:\*\*** #8.



\## Main success scenario

1\. El Administrador busca a un estudiante por su número de matrícula.

2\. El Sistema muestra los datos actuales del estudiante.

3\. El Administrador modifica los datos necesarios (ej. nombre, email).

4\. El Sistema valida que el nuevo email o número no pertenezcan a otro estudiante.

5\. El Sistema guarda los cambios y muestra un mensaje de confirmación.



\## Alternative / exceptional flows

\- **\*\*4a.\*\*** El email introducido ya está en uso → El Sistema bloquea el guardado y muestra un error de duplicidad.



\## Business rules / constraints

\- BR-03-1: El número de estudiante es un identificador único y no puede ser modificado una vez creado.



\## Domain concepts revealed

\- Student — La entidad que representa al estudiante con sus atributos como nombre y email\[cite: 3].

