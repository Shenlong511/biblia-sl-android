# Verificación — Biblia SL Android (offline)

Fecha de verificación: 2026-11-14. Fuentes: bible-api.com/data/vbl y
eBible.org spablm_usfm.zip. El texto no se inventó: se descargó de
esas fuentes públicas y se comprobó libro por libro.

## Datos

- Libros: 73 (Antiguo Testamento 46, Nuevo Testamento 27)
- Capítulos totales: 1328
- Versículos totales: 35283
- Tamaño de los datos locales (`app/src/main/assets/bible/`): 4,8 MB
- 73 archivos JSON (uno por libro) + `index.json`. La app carga cada
  libro bajo demanda y guarda en memoria como máximo 3 libros.

## Versículos de prueba (texto real, en español)

- Génesis 1:1 (VBL): “En el principio, Dios creó los cielos y la tierra.”
- Juan 3:16 (VBL): ““Porque Dios amó al mundo, y lo hizo de esta manera: entregó a su único Hijo, a fin de que todos los que crean en él no mueran, sino que tengan vida eterna.”
- Tobías 1:1 (Santa Biblia libre para el mundo): “Libro de las palabras de Tobit, hijo de Tobiel, hijo de Ananiel, hijo de Aduel, hijo de Gabael, de la descendencia de Asiel, de la tribu de Neftalí;”
- Daniel 13:1 (Santa Biblia libre, Susana): “Vivía en Babilonia un hombre que se llamaba Joaquín.”
- Daniel 14:1 (Santa Biblia libre, Bel): “El rey Astyages fue reunido con sus padres, y Ciro el persa recibió su reino.”

## Fuentes por libro

- VBL (Versión Biblia Libre, CC BY-SA 4.0): 66 libros, más Daniel 1–12.
- Santa Biblia libre para el mundo (dominio público): Tobías, Judit,
  Sabiduría, Eclesiástico, Baruc, 1 Macabeos, 2 Macabeos, y Daniel 13–14.
- 16 versículos vienen vacíos en la fuente VBL porque esa traducción
  los omite al no estar en los manuscritos más antiguos: Mateo 17:21,
  18:11 y 23:14; Marcos 7:16, 9:44, 9:46, 11:26 y 15:28; Lucas 17:36
  y 23:17; Juan 5:4; Hechos 8:37, 15:34, 24:7 y 28:29; Romanos 16:24.
  La app los salta al mostrar el capítulo, sin cambiar la numeración.
- Ester: 10 capítulos en VBL. Las adiciones griegas de Ester no son
  capítulos aparte, por eso no se añaden como capítulos extra; el conteo
  de 10 capítulos es el del canon católico para Ester.

## Comprobaciones

- OK  Libros totales: 73 (esperados 73)
- OK  Antiguo Testamento: 46 libros (esperados 46)
- OK  Nuevo Testamento: 27 libros (esperados 27)
- OK  Conteos de capítulos por libro correctos
- OK  Capítulos totales: 1328 (esperados 1328)
- INFO Versículos totales: 35283 entradas (35.267 con texto; 16 son los versículos omitidos por la VBL)
- INFO Capítulos totales: 1328
- OK  Génesis 1:1 (VBL): "En el principio, Dios creó los cielos y la tierra."
- OK  Juan 3:16 (VBL): "“Porque Dios amó al mundo, y lo hizo de esta manera: entregó a su único Hijo, a fin de que todos los que crean en él no mueran, sino que tengan vida eterna."
- OK  Tobías 1:1 (Santa Biblia libre): "Libro de las palabras de Tobit, hijo de Tobiel, hijo de Ananiel, hijo de Aduel, hijo de Gabael, de la descendencia de Asiel, de la tribu de Neftalí;"
- OK  Daniel: 14 capítulos (12 VBL + 13–14 spablm)
- OK  Daniel 13:1 (spablm): "Vivía en Babilonia un hombre que se llamaba Joaquín.…" / Daniel 14:1: "El rey Astyages fue reunido con sus padres, y Ciro el persa recibió su reino.…"
- OK  Tobías: fuente spablm (Santa Biblia libre para el mundo)
- OK  Tobías: etiqueta de fuente presente
- OK  Judit: fuente spablm (Santa Biblia libre para el mundo)
- OK  Judit: etiqueta de fuente presente
- OK  Sabiduría: fuente spablm (Santa Biblia libre para el mundo)
- OK  Sabiduría: etiqueta de fuente presente
- OK  Eclesiástico: fuente spablm (Santa Biblia libre para el mundo)
- OK  Eclesiástico: etiqueta de fuente presente
- OK  Baruc: fuente spablm (Santa Biblia libre para el mundo)
- OK  Baruc: etiqueta de fuente presente
- OK  1 Macabeos: fuente spablm (Santa Biblia libre para el mundo)
- OK  1 Macabeos: etiqueta de fuente presente
- OK  2 Macabeos: fuente spablm (Santa Biblia libre para el mundo)
- OK  2 Macabeos: etiqueta de fuente presente
- OK  Archivo presente: settings.gradle.kts
- OK  Archivo presente: build.gradle.kts
- OK  Archivo presente: gradle.properties
- OK  Archivo presente: gradle/wrapper/gradle-wrapper.properties
- OK  Archivo presente: gradle/wrapper/gradle-wrapper.jar
- OK  Archivo presente: gradlew
- OK  Archivo presente: gradlew.bat
- OK  Archivo presente: app/build.gradle.kts
- OK  Archivo presente: app/src/main/AndroidManifest.xml
- OK  Archivo presente: app/src/main/java/com/fran/bibliacatolica/MainActivity.kt
- OK  Archivo presente: app/src/main/java/com/fran/bibliacatolica/Bible.kt
- OK  Archivo presente: app/src/main/java/com/fran/bibliacatolica/BibleRepository.kt
- OK  Archivo presente: app/src/main/java/com/fran/bibliacatolica/UserData.kt
- OK  Archivo presente: .github/workflows/android.yml
- OK  Archivo presente: README.md
- OK  Archivo presente: CREDITS.md
- OK  AndroidManifest: sin permiso de INTERNET (app 100% offline)
- OK  Workflow: ejecuta ./gradlew assembleDebug
- OK  Workflow: sube el APK con actions/upload-artifact
- OK  Workflow: se puede lanzar a mano (workflow_dispatch)
- OK  Kotlin MainActivity.kt: llaves balanceadas (181)
- OK  Kotlin MainActivity.kt: paquete correcto
- OK  Kotlin Bible.kt: llaves balanceadas (2)
- OK  Kotlin Bible.kt: paquete correcto
- OK  Kotlin BibleRepository.kt: llaves balanceadas (27)
- OK  Kotlin BibleRepository.kt: paquete correcto
- OK  Kotlin UserData.kt: llaves balanceadas (23)
- OK  Kotlin UserData.kt: paquete correcto

## Resultado

**Todo correcto.** No hubo fallos en la verificación.
