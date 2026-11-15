# Créditos y licencias

Esta app usa únicamente traducciones libres o de dominio público.
No usa la Biblia Latinoamericana ni la Biblia de Jerusalén, que
tienen derechos de autor.

## Versión Biblia Libre (VBL) — 66 libros

- **Texto:** Versión Biblia Libre / Spanish Free Bible Version.
- **Licencia:** CC BY-SA 4.0
  (https://creativecommons.org/licenses/by-sa/4.0/)
- **© 2018–2020** Jonathan Gallagher y Shelly Barrios de Avila.
- **Fuente de descarga:** https://bible-api.com/data/vbl
  (API pública de bible-api.com; cada libro y capítulo se descargó
  de `https://bible-api.com/data/vbl/<LIBRO>/<CAPÍTULO>`).
- Se usa para los 66 libros del canon común, y para Daniel 1–12.

## Reina-Valera 1909 (RV1909) — 66 libros

- **Texto:** Reina-Valera 1909.
- **Licencia:** dominio público.
- **Fuente de descarga:** https://ebible.org/Scriptures/spaRV1909_usfm.zip
  (traducción identificada como `spaRV1909` en eBible.org;
  https://ebible.org/details.php?id=spaRV1909).
  Nota: bible-api.com lista la RV1909 pero su endpoint
  `https://bible-api.com/data/rv1909` devolvía 404 al descargar, por
  eso se usó el USFM de eBible.org.
- Conversión al formato JSON de la app con `tools/convert_rv1909.py`
  (reproducible: descarga el ZIP, descomprime los 66 USFM y ejecuta el
  script). Se conservan la ortografía original de 1909 ("crió", "fué",
  "á", "EN el principio…") y las palabras añadidas por los traductores
  (`\add` en USFM); se quitaron solo los marcadores de Strong
  (`\w …|strong="…" \w*`) y de formato.
- Se usa como segunda traducción, a elegir en Ajustes o en el lector.
  La VBL sigue siendo la traducción por defecto.

## Santa Biblia libre para el mundo — deuterocanónicos

- **Texto:** Santa Biblia libre para el mundo.
- **Licencia:** dominio público.
- **Fuente de descarga:** https://ebible.org/Scriptures/spablm_usfm.zip
  (traducción identificada como `spablm` en eBible.org;
  https://ebible.org/details.php?id=spablm).
- Se usa para los 7 libros deuterocanónicos que la VBL no incluye:
  Tobías, Judit, Sabiduría, Eclesiástico (Sirácide), Baruc,
  1 Macabeos y 2 Macabeos; y para Daniel 13–14 (Susana y Bel).
- En la app estos libros y capítulos van etiquetados con su fuente,
  para que nadie los confunda con la VBL.

## Código de la app

- Proyecto Android en Kotlin con Jetpack Compose, creado para Fran.
- El código de la app es original para este proyecto.
