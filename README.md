# Biblia SL — APK offline (Android)

App nativa de Android, en español, con la **Biblia Católica completa
(73 libros: 46 del Antiguo Testamento y 27 del Nuevo Testamento)**,
bajo el nombre **Biblia SL**, guardada **dentro de la app**. Después de instalarla **no necesita
internet para nada**: ni para abrirla, ni para leer, buscar, guardar
favoritos o notas.

## Qué contiene

- **Traducción principal:** Versión Biblia Libre (VBL), en español moderno.
- **Los 7 libros deuterocanónicos** (Tobías, Judit, Sabiduría,
  Eclesiástico, Baruc, 1 Macabeos y 2 Macabeos) se muestran con la
  **Santa Biblia libre para el mundo**, de dominio público, porque la
  VBL no los incluye. En la app van marcados claramente.
- **Daniel 13–14** (Susana y Bel) también vienen de la Santa Biblia
  libre para el mundo, por la misma razón.
- Pantallas: Inicio con versículo del día, Antiguo Testamento,
  Nuevo Testamento, lista de capítulos, lector con versículos
  numerados, búsqueda offline, Favoritos, Notas por versículo,
  capítulos marcados como leídos y “Continuar leyendo”.
- Ajustes de tamaño de letra y modo oscuro.
- Favoritos, notas y progreso se guardan solo en tu teléfono.

## Cómo compilarla en GitHub (recomendado, sin instalar nada)

No necesitas Android Studio para esto. Solo necesitas una cuenta
gratuita de GitHub.

### 1. Crea el repositorio
1. Entra a [github.com](https://github.com) e inicia sesión.
2. Pulsa **New repository** (o el botón **+** → *New repository*).
3. Ponle un nombre, por ejemplo `biblia-sl-android`.
4. Déjalo en **Public** o **Private**, como prefieras, y pulsa
   **Create repository**. No marques ninguna casilla extra.

### 2. Sube el proyecto
1. Descarga y descomprime el archivo `biblia-sl-android.zip`
   que te entregué.
2. En la página del repositorio que acabas de crear, pulsa
   **uploading an existing file** (o arrastra los archivos).
3. Sube **todo el contenido** de la carpeta descomprimida
   (las carpetas `app`, `.github`, `gradle`, y los archivos
   `gradlew`, `settings.gradle.kts`, etc.).
4. Pulsa **Commit changes**.

> Consejo: si ya tienes Git instalado, también puedes subirlo con
> `git init`, `git add .`, `git commit -m "Biblia SL"` y
> `git push`.

### 3. Espera a que se compile sola
1. En tu repositorio, entra a la pestaña **Actions**.
2. Verás una ejecución llamada **“Compilar APK (debug)”** en curso
   (un círculo amarillo). Tarda unos minutos la primera vez.
3. Cuando se ponga en verde ✔, entra en esa ejecución.

### 4. Descarga la APK
1. Baja hasta el final de la página de la ejecución, a la sección
   **Artifacts**.
2. Descarga **`biblia-sl-debug-apk`**. Es un archivo `.zip`.
3. Descomprímelo en tu teléfono o computadora: dentro está el
   archivo **`app-debug.apk`**.

### 5. Instálala en tu teléfono
1. Pasa el archivo `app-debug.apk` a tu teléfono Android
   (por WhatsApp, correo, cable USB o descargándolo directo).
2. Tócalo para instalarlo. Android te pedirá permitir
   **“Instalar apps desconocidas”** para esa app (por ejemplo,
   para Chrome o para tu explorador de archivos). Actívalo y vuelve.
3. Pulsa **Instalar**. Aparecerá el ícono **Biblia SL** en tu
   pantalla. Ábrela y lee sin internet.

## Cómo compilarla en Android Studio (opcional)

1. Abre **Android Studio** → **Open** → elige la carpeta del proyecto.
2. Espera a que descargue el SDK y sincronice Gradle (primera vez
   tarda varios minutos y necesita internet solo para eso).
3. Menú **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
4. Cuando termine, pulsa **locate** y encontrarás
   `app/build/outputs/apk/debug/app-debug.apk`.

## Para publicar en Play Store (solo si algún día quieres)

La APK de prueba no sirve para Play Store. Para eso habría que
firmarla y generar un archivo `.aab` (Build → Generate Signed
Bundle / APK), con una cuenta de desarrollador de Google Play.
Eso no es necesario para usarla tú en tu teléfono.

## Datos y licencias

Ver `CREDITS.md` y `VERIFICATION.md`. El texto bíblico no se
inventó: se descargó de fuentes públicas y se verificó libro por
libro (ver `VERIFICATION.md`).

## Datos técnicos (resumen)

- Paquete: `com.fran.bibliacatolica`
- Lenguaje: Kotlin con Jetpack Compose
- minSdk 24 (Android 7.0 o superior), targetSdk 34
- Sin permiso de internet en la app: todo el texto vive en
  `app/src/main/assets/bible/` como archivos JSON, un archivo
  por libro, que la app lee bajo demanda.
- Favoritos, notas y progreso: DataStore, solo en el teléfono.
