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

---

## Novedades versión 1.1 — 2026-11-14 (sin compilar aquí: no hay Android SDK en esta máquina)

### 1. Arreglo de la barra del lector (reporte de Fran, captura 18:06 CST)

- Causa: en `ReaderScreen` la fila de 3 botones (Anterior / Marcar leído /
  Siguiente) no tenía `fillMaxWidth()` ni peso, así que no cabía en el
  ancho del teléfono y el tercer botón se deformaba como un óvalo
  vertical vacío y muy alto. La `LazyColumn` de versículos tampoco
  tenía `weight(1f)`, así que no ocupaba toda la altura restante y al
  subir el texto solo se veía en la mitad de la pantalla.
- Corrección (`MainActivity.kt`): fila con `Modifier.fillMaxWidth()`,
  cada botón con `Modifier.weight(1f)`, texto en una sola línea
  (`maxLines = 1`, 13 sp, padding reducido); `LazyColumn` con
  `Modifier.weight(1f).fillMaxWidth()`.
- Mismo patrón corregido en las otras pantallas con `Column` +
  lista sin peso: `BookListScreen` (LazyColumn), `ChapterListScreen`
  (LazyVerticalGrid), `SearchScreen` (LazyColumn) y `FavoritesScreen`
  (LazyColumn). `SettingsScreen` pasó a `LazyColumn` para que todo
  su contenido quepa con desplazamiento. Diseño general y colores sin
  cambios.

### 2. Reina-Valera 1909 como segunda traducción offline

- Fuente: eBible.org `spaRV1909_usfm.zip` (66 archivos USFM, dominio
  público). Conversión con `tools/convert_rv1909.py` al mismo formato
  JSON por libro que la VBL (`id`, `name`, `testament`, `source`,
  `sourceLabel`, `chapterSources`, `chapters`), en
  `app/src/main/assets/bible_rv1909/` (66 JSON + `index.json`).
- Conteos verificados: 66 libros, 1.189 capítulos, 31.102 versículos
  (31.084 con texto; ver huecos abajo). Los capítulos por libro
  coinciden con la VBL en los 66 libros, salvo Daniel: 12 en RV1909
  frente a 14 en el índice católico (Daniel 13–14 no existen en RV1909).
- VBL sin cambios: 35.283 versículos (35.267 con texto), 73 libros,
  1.328 capítulos.
- Ejemplos verificados (texto real de cada fuente):
  - Génesis 1:1 (VBL): "En el principio, Dios creó los cielos y la tierra."
  - Génesis 1:1 (RV1909): "EN el principio crió Dios los cielos y la tierra."
  - Juan 3:16 (VBL): ""Porque Dios amó al mundo, y lo hizo de esta manera: entregó a su único Hijo, a fin de que todos los que crean en él no mueran, sino que tengan vida eterna."
  - Juan 3:16 (RV1909): "Porque de tal manera amó Dios al mundo, que ha dado á su Hijo unigénito, para que todo aquel que en él cree, no se pierda, mas tenga vida eterna."
  - La ortografía de la RV1909 es la original de 1909 ("crió", "fué",
    "á"); no se modernizó.
- Los 16 versículos que la VBL omite (Mateo 17:21, 18:11, 23:14;
  Marcos 7:16, 9:44, 9:46, 11:26, 15:28; Lucas 17:36, 23:17;
  Juan 5:4; Hechos 8:37, 15:34, 24:7, 28:29; Romanos 16:24) SÍ
  existen con texto en la RV1909 (verificados uno por uno).
- 18 versículos vacíos en la RV1909 (1 Samuel 23:29; 2 Samuel 20:26;
  2 Crónicas 33:25; Números 12:16 y 29:40; Job 35:16, 38:39, 38:40,
  38:41, 40:20–24; Jonás 1:17; Oseas 11:12; Hechos 19:41;
  2 Corintios 13:14): son huecos de numeración de la propia fuente
  USFM (p. ej. la bendición de 2 Corintios 13:14 está en 13:13 en
  esa edición; el pez de Jonás 1:17 está en 2:1; el león de Job 38:39
  está en 39:1). El texto no se pierde. La app los salta al mostrar,
  sin cambiar la numeración, igual que hace con los 16 de la VBL.
- Sin marcadores USFM restantes en los JSON de RV1909: 0 versículos
  con `\w`, `strong=` o `\add`.
- Deuterocanónicos con RV1909 activa (simulación de la lógica de
  `BibleRepository.loadBook`): Tobías, Judit, Sabiduría, Eclesiástico,
  Baruc, 1 y 2 Macabeos se leen de `assets/bible/` con fuente
  `spablm` y su etiqueta, sin romperse. Daniel con RV1909: 14
  capítulos (12 RV1909 + 13–14 spablm), con `chapterSources`
  `rv1909 × 12, spablm × 2`.
- Selector: en Ajustes (dos botones a ancho completo, la activa
  rellena con ✓) y en el lector (dos botones arriba de Anterior /
  Marcar leído / Siguiente). La elección persiste en DataStore
  (clave `translation`, "vbl" por defecto) y se aplica al lector,
  versículo del día, búsqueda y favoritos/notas. Cada pantalla
  muestra la traducción activa y los deuterocanónicos muestran
  "Santa Biblia libre para el mundo".
- Búsqueda en RV1909: "Jehová" da 5.795 resultados (palabra típica
  de la RV1909); la búsqueda por cita ("Juan 3:16") devuelve el
  texto RV1909.
- `AndroidManifest`: sigue sin permiso de INTERNET. Datos locales
  totales: 8,9 MB (4,8 MB VBL + 4,1 MB RV1909); 141 archivos JSON.
- Versión de la app: 1.1 (versionCode 2). Nombre visible Biblia SL y
  paquete `com.fran.bibliacatolica` sin cambios.

### Comprobaciones (versión 1.1)

- OK  RV1909: 66 libros, 1.189 capítulos, 31.102 versículos
- OK  RV1909: capítulos por libro iguales a VBL salvo Daniel (12 vs 14)
- OK  RV1909: Génesis 1:1 y Juan 3:16 con el texto clásico
- OK  RV1909: los 16 versículos omitidos por la VBL existen con texto
- OK  RV1909: 0 versículos con marcadores USFM restantes
- OK  RV1909: JSON con las mismas claves que los JSON de la VBL
- OK  VBL sin cambios: 35.283 versículos (35.267 con texto)
- OK  Deuterocanónicos con RV1909: fuente spablm, sin vacíos
- OK  Daniel con RV1909: 14 capítulos (12 RV1909 + 13–14 spablm)
- OK  `loadBook`/`getVerse`/`dailyVerse`/`search`: todas las llamadas
  en `MainActivity.kt` pasan la traducción activa (sin llamadas viejas)
- OK  `MainActivity.kt` / `BibleRepository.kt` / `UserData.kt`:
  llaves y paréntesis balanceados
- OK  AndroidManifest: sin permiso de INTERNET
- NO VERIFICADO AQUÍ  Compilación de la APK: no hay Java ni Android
  SDK en esta máquina. La compilación real es en GitHub Actions
  (workflow `android.yml`, `./gradlew assembleDebug`), como siempre.
  Tampoco se pudo probar en un teléfono que la barra del lector ya
  se vea bien ni cambiar la traducción en la app instalada: eso lo
  tiene que comprobar Fran tras instalar la APK nueva.
