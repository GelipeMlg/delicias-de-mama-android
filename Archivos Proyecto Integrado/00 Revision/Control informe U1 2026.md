# Control del informe U1

Versión: 7 de octubre de 2026. Equipo: Felipe Alvarado y Jafet Lavados.

## Documento

`../02 Informes Word/Informe_U1_Diagnostico_Delicias_de_Mama_2026.docx`

Se redactó sobre la plantilla institucional `../01 Pautas/Carta-1-vert.docx`. Se conservaron las partes del paquete Word salvo document.xml; geometría de sección, estilos, imágenes, relaciones, encabezados y pies permanecen iguales. Las 16 páginas se revisaron visualmente después de renderizar. La ampliación de contenido respecto de las cuatro páginas de ejemplo es intencional.

## Contenido y alcance

Se cubren los criterios U1 1.1.1–1.1.5: necesidades TI, seis historias de usuario, requisitos y prioridades, metodologías y seguridad/ética. Incluye justificación de viabilidad y plan propuesto de tres semanas. Utiliza el estado Android/Supabase/Firebase y excluye trabajos de otras asignaturas. El enfoque es incremental.

Las evidencias técnicas conservan su fecha y alcance. No se afirman nuevas entrevistas, ahorros medidos, aceptación de la dueña, pruebas remotas ni restauraciones verificadas. El anexo es un instrumento propuesto para validación.

## Antes de entregar

- Confirmar docente, sede, sección y plazo oficial. El 27 de octubre es tentativo.
- Acordar distribución de responsabilidades del equipo.
- Registrar validación con la contraparte y completar información real del negocio.
- Revalidar referencias normativas al emitir la versión final.
- Revisar en Word antes de cargar el archivo; no entregar versiones históricas por error.

## Mantenimiento

El generador `scripts/generar_informe_u1_2026.py` permite reproducir el documento y requiere lxml. Regenerar sobrescribe el Word: si se edita manualmente, conservar esos cambios antes de ejecutar el script. Los renders y herramientas de revisión están en .tmp y no forman parte de la entrega.
