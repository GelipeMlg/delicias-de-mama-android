# Diseño U2 — Delicias de Mamá

Versión: 13 de octubre de 2026.

El informe editable está en `../../02 Informes Word/Informe_U2_Diseno_Delicias_de_Mama_2026.docx`.
Las nueve figuras de esta carpeta se elaboraron a partir del código Android y las migraciones SQL revisadas. El prototipo es conceptual, no una captura ni evidencia de aceptación.

## Reproducción

Desde la raíz del repositorio, con Python y Pillow, python-docx y lxml instalados:

```powershell
python scripts/figuras_u2.py
python scripts/generar_informe_u2_2026.py
```

El generador conserva las partes institucionales de Carta-1-vert.docx y añade el contenido y las figuras del informe. Regenerarlo reemplaza el Word de U2; respaldar primero cualquier edición manual.

El plan de pruebas requiere ejecución y registro posterior. No contiene resultados inventados ni confirma el estado actual de servicios remotos.
