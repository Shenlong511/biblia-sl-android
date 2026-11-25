package com.fran.bibliacatolica

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = BibleRepository(this)
        val userData = UserData(this)
        setContent {
            val dark by userData.darkFlow.collectAsState(initial = false)
            val fontScale by userData.fontFlow.collectAsState(initial = 1.0f)
            val translation by userData.translationFlow.collectAsState(initial = "vbl")
            // Colores neón elegidos en Ajustes (fondo, tarjetas y letras).
            // "Original" = sin cambio, se queda el color del tema.
            val bgColorName by userData.bgColorFlow.collectAsState(initial = "Original")
            val bgTransparency by userData.bgTransparencyFlow.collectAsState(initial = 0f)
            val cardColorName by userData.cardColorFlow.collectAsState(initial = "Original")
            val cardTransparency by userData.cardTransparencyFlow.collectAsState(initial = 0f)
            val textColorName by userData.textColorFlow.collectAsState(initial = "Original")
            val textTransparency by userData.textTransparencyFlow.collectAsState(initial = 0f)
            val baseScheme = if (dark) darkColorScheme() else lightColorScheme()
            val bgColor = NeonColorOptions.firstOrNull { it.first == bgColorName }
                ?.second?.copy(alpha = 1f - bgTransparency)
            val cardColor = NeonColorOptions.firstOrNull { it.first == cardColorName }
                ?.second?.copy(alpha = 1f - cardTransparency)
            val textColor = NeonColorOptions.firstOrNull { it.first == textColorName }
                ?.second?.copy(alpha = 1f - textTransparency)
            val scheme = baseScheme.copy(
                background = bgColor ?: baseScheme.background,
                surface = cardColor ?: baseScheme.surface,
                surfaceVariant = cardColor ?: baseScheme.surfaceVariant,
                onBackground = textColor ?: baseScheme.onBackground,
                onSurface = textColor ?: baseScheme.onSurface,
                onSurfaceVariant = textColor ?: baseScheme.onSurfaceVariant
            )
            MaterialTheme(colorScheme = scheme) {
                BibliaApp(repo, userData, fontScale, translation)
            }
        }
    }
}

private val DEUTEROCANONICAL = setOf("TOB", "JDT", "WIS", "SIR", "BAR", "1MA", "2MA")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliaApp(repo: BibleRepository, userData: UserData, fontScale: Float, translation: String) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            Column {
                // Título como imagen a todo lo ancho, en una sola línea,
                // como el título de Shenlong Play Music 2.0.
                Image(
                    painter = painterResource(id = R.drawable.title_biblia_sl),
                    contentDescription = "Biblia SL",
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                    contentScale = ContentScale.Crop
                )
                TopAppBar(
                    title = { },
                    actions = {
                        TextButton(onClick = { nav.navigate("search") }) { Text("Buscar") }
                        TextButton(onClick = { nav.navigate("favorites") }) { Text("Guardados") }
                        TextButton(onClick = { nav.navigate("settings") }) { Text("Ajustes") }
                    }
                )
            }
        }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(pad)
        ) {
            composable("home") { HomeScreen(repo, userData, nav, translation) }
            composable(
                "books/{testament}",
                arguments = listOf(navArgument("testament") { type = NavType.StringType })
            ) { back ->
                BookListScreen(repo, back.arguments?.getString("testament") ?: "AT", nav)
            }
            composable(
                "chapters/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { back ->
                ChapterListScreen(repo, userData, back.arguments?.getString("bookId") ?: "GEN", nav, translation)
            }
            composable(
                "reader/{bookId}/{chapter}",
                arguments = listOf(
                    navArgument("bookId") { type = NavType.StringType },
                    navArgument("chapter") { type = NavType.IntType }
                )
            ) { back ->
                ReaderScreen(
                    repo = repo,
                    userData = userData,
                    bookId = back.arguments?.getString("bookId") ?: "GEN",
                    chapter = back.arguments?.getInt("chapter") ?: 1,
                    nav = nav,
                    fontScale = fontScale,
                    scope = scope,
                    translation = translation
                )
            }
            composable("search") { SearchScreen(repo, nav, translation) }
            composable("favorites") { FavoritesScreen(repo, userData, nav, translation) }
            composable("settings") { SettingsScreen(userData) }
        }
    }
}

@Composable
fun HomeScreen(repo: BibleRepository, userData: UserData, nav: NavHostController, translation: String) {
    val daily = remember(translation) { repo.dailyVerse(translation) }
    val lastRead by userData.lastReadFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var showDonation by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "La Palabra de Dios, siempre contigo.\nSin internet: todo el texto está guardado en tu teléfono.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Versículo del día", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (daily != null) {
                        Text(daily.text, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(daily.label, fontWeight = FontWeight.Bold)
                        Text(
                            "Traducción: ${BibleRepository.translationLabel(translation)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = {
                            nav.navigate("reader/${daily.bookId}/${daily.chapter}")
                        }) { Text("Leer el capítulo") }
                    } else {
                        Text("No se pudo cargar el versículo del día.")
                    }
                }
            }
        }
        if (lastRead != null) {
            item {
                val parts = lastRead!!.split(":")
                val info = repo.bookById(parts[0])
                Button(onClick = {
                    if (parts.size == 2) nav.navigate("reader/${parts[0]}/${parts[1].toIntOrNull() ?: 1}")
                }) {
                    Text("Continuar leyendo: ${info?.name ?: parts[0]} ${parts.getOrNull(1) ?: ""}")
                }
            }
        }
        item {
            Button(
                onClick = { nav.navigate("books/AT") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Antiguo Testamento · 46 libros") }
        }
        item {
            Button(
                onClick = { nav.navigate("books/NT") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Nuevo Testamento · 27 libros") }
        }
        item {
            OutlinedButton(
                onClick = { nav.navigate("search") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Buscar por libro, capítulo o palabra") }
        }
        item {
            Button(
                onClick = { showDonation = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("DONACIÓN") }
        }
        item {
            Text("Traducción", fontWeight = FontWeight.Bold)
        }
        item {
            if (translation == "vbl") {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("✓ Versión Biblia Libre (español moderno)") }
            } else {
                OutlinedButton(
                    onClick = { scope.launch { userData.setTranslation("vbl") } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Versión Biblia Libre (español moderno)") }
            }
        }
        item {
            if (translation == "rv1909") {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("✓ Reina-Valera 1909 (español clásico)") }
            } else {
                OutlinedButton(
                    onClick = { scope.launch { userData.setTranslation("rv1909") } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reina-Valera 1909 (español clásico)") }
            }
        }
        item {
            if (translation == "bes") {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("✓ Biblia en Español Sencillo") }
            } else {
                OutlinedButton(
                    onClick = { scope.launch { userData.setTranslation("bes") } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Biblia en Español Sencillo") }
            }
        }
        item {
            Text(
                "Traducciones: Versión Biblia Libre, en español moderno, " +
                    "Reina-Valera 1909, en español clásico, y Biblia en " +
                    "Español Sencillo, en español muy sencillo. Se cambia aquí " +
                    "o en el lector. Los 7 libros deuterocanónicos se muestran " +
                    "con la Santa Biblia libre para el mundo, de dominio público, " +
                    "y van marcados en la lista.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        item {
            Text(
                "Versículo del día: ${daily?.label ?: ""}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    if (showDonation) DonationDialog(onClose = { showDonation = false })
}

/**
 * Ventana de donación: abre a pantalla completa (también en horizontal,
 * donde todo se desliza y ninguna barra queda fija arriba) y muestra la
 * imagen con las cuentas. Cada cuenta tiene su botón Copiar y hay un
 * botón para copiar las dos juntas. Al copiar se entrega el número
 * sin espacios, listo para pegar en la app del banco.
 */
@Composable
fun DonationDialog(onClose: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClose) { Text("✕ Cerrar") }
            }
            Image(
                painter = painterResource(id = R.drawable.donacion),
                contentDescription = "Donación a la Cuenta Nu de Débito",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Número de cuenta (Nu de Débito)", fontWeight = FontWeight.Bold)
                    Text("5195 3773 1452 7450")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        clipboard.setText(AnnotatedString("5195377314527450"))
                        Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                    }) { Text("Copiar") }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("CLABE SPEI", fontWeight = FontWeight.Bold)
                    Text("638180010171917003")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        clipboard.setText(AnnotatedString("638180010171917003"))
                        Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                    }) { Text("Copiar") }
                }
            }
            Button(
                onClick = {
                    clipboard.setText(
                        AnnotatedString(
                            "Cuenta Nu: 5195377314527450\nCLABE SPEI: 638180010171917003"
                        )
                    )
                    Toast.makeText(context, "Cuentas copiadas", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Copiar las dos cuentas") }
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar") }
        }
    }
}

@Composable
fun BookListScreen(repo: BibleRepository, testament: String, nav: NavHostController) {
    val title = if (testament == "AT") "Antiguo Testamento" else "Nuevo Testamento"
    val books = repo.books.filter { it.testament == testament }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${books.size} libros · sin internet", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(books) { book ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        nav.navigate("chapters/${book.id}")
                    },
                    colors = CardDefaults.cardColors()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row {
                            Text(book.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("${book.chapters} cap.")
                        }
                        if (book.id in DEUTEROCANONICAL) {
                            Text(
                                "Deuterocanónico · Santa Biblia libre para el mundo",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (book.id == "DAN") {
                            Text(
                                "Caps. 1–12: Versión Biblia Libre · Caps. 13–14: Santa Biblia libre",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterListScreen(
    repo: BibleRepository,
    userData: UserData,
    bookId: String,
    nav: NavHostController,
    translation: String
) {
    val info = repo.bookById(bookId)
    val read by userData.readFlow.collectAsState(initial = emptySet())
    val scope = rememberCoroutineScope()
    if (info == null) {
        Text("Libro no encontrado.", Modifier.padding(16.dp))
        return
    }
    // Etiqueta de fuente según la traducción activa. Los deuterocanónicos
    // y Daniel 13-14 siempre son Santa Biblia libre (spablm).
    val sourceText = when {
        info.id in DEUTEROCANONICAL -> info.sourceLabel
        info.id == "DAN" && translation == "rv1909" ->
            "Caps. 1–12: Reina-Valera 1909 · Caps. 13–14: Santa Biblia libre"
        info.id == "DAN" && translation == "bes" ->
            "Caps. 1–12: Biblia en Español Sencillo · Caps. 13–14: Santa Biblia libre"
        info.id == "DAN" -> info.sourceLabel
        translation == "rv1909" -> "Reina-Valera 1909 (dominio público)"
        translation == "bes" -> "Biblia en Español Sencillo (dominio público)"
        else -> info.sourceLabel
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(info.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(sourceText, style = MaterialTheme.typography.bodySmall)
        if (info.id in DEUTEROCANONICAL) {
            Text(
                "Libro deuterocanónico. Texto: Santa Biblia libre para el mundo, de dominio público.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..info.chapters).toList()) { ch ->
                val key = "$bookId:$ch"
                val isRead = key in read
                Card(
                    modifier = Modifier.clickable {
                        scope.launch { userData.setLastRead(key) }
                        nav.navigate("reader/$bookId/$ch")
                    }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(if (isRead) "✓ $ch" else "$ch", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReaderScreen(
    repo: BibleRepository,
    userData: UserData,
    bookId: String,
    chapter: Int,
    nav: NavHostController,
    fontScale: Float,
    scope: kotlinx.coroutines.CoroutineScope,
    translation: String
) {
    val book = remember(bookId, translation) { repo.loadBook(bookId, translation) }
    val favorites by userData.favoritesFlow.collectAsState(initial = emptySet())
    val notes by userData.notesFlow.collectAsState(initial = emptyMap())
    val read by userData.readFlow.collectAsState(initial = emptySet())
    var selected by remember { mutableStateOf<VerseRef?>(null) }
    var noteDraft by remember { mutableStateOf("") }
    if (book == null) {
        Text("No se pudo cargar este libro.", Modifier.padding(16.dp))
        return
    }
    val verses = if (chapter in 1..book.chapters.size) book.chapters[chapter - 1] else emptyList()
    val chapterKey = "$bookId:$chapter"
    val isRead = chapterKey in read
    val chapterSource = book.chapterSources.getOrNull(chapter - 1) ?: book.info.source
    Box(Modifier.fillMaxSize()) {
    // El encabezado (titulo, selector y botones) va DENTRO de la lista:
    // al bajar el texto se esconde y el capitulo ocupa toda la pantalla;
    // al subir hasta arriba vuelve a aparecer. En horizontal era lo que
    // dejaba apenas un versiculo visible.
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Text(
                "${book.info.name} $chapter",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                when (chapterSource) {
                    "spablm" -> "Santa Biblia libre para el mundo (dominio público)"
                    "rv1909" -> "Reina-Valera 1909 (dominio público)"
                    "bes" -> "Biblia en Español Sencillo (dominio público)"
                    else -> "Versión Biblia Libre"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
        item {
            // Selector de traducción: la activa va rellena, las demás en
            // contorno. Cada botón mide lo que ocupa su texto y la fila se
            // desliza de lado si no caben las tres. Al cambiar, se cierra
            // el panel del versículo para no mostrar texto de la traducción
            // anterior.
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "vbl" to "Versión Biblia Libre",
                    "rv1909" to "Reina-Valera 1909",
                    "bes" to "Biblia en Español Sencillo"
                ).forEach { (id, label) ->
                    if (translation == id) {
                        Button(
                            onClick = { },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) { Text(label, maxLines = 1, fontSize = 12.sp) }
                    } else {
                        OutlinedButton(
                            onClick = {
                                selected = null
                                scope.launch { userData.setTranslation(id) }
                            },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) { Text(label, maxLines = 1, fontSize = 12.sp) }
                    }
                }
            }
        }
        item {
            // Los 3 botones miden lo que ocupa su texto (sin estirarse a
            // partes iguales): el del centro puede tomar más ancho si lo
            // necesita y los de los lados quedan a su medida.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { if (chapter > 1) nav.navigate("reader/$bookId/${chapter - 1}") },
                    enabled = chapter > 1,
                    modifier = Modifier.weight(1f, fill = false).height(36.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) { Text("← Anterior", maxLines = 1, fontSize = 12.sp) }
                OutlinedButton(
                    onClick = { scope.launch { userData.toggleRead(chapterKey) } },
                    modifier = Modifier.weight(1f, fill = false).height(36.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) { Text(if (isRead) "✓ Leído" else "Marcar leído", maxLines = 1, fontSize = 12.sp) }
                OutlinedButton(
                    onClick = { if (chapter < book.chapters.size) nav.navigate("reader/$bookId/${chapter + 1}") },
                    enabled = chapter < book.chapters.size,
                    modifier = Modifier.weight(1f, fill = false).height(36.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) { Text("Siguiente →", maxLines = 1, fontSize = 12.sp) }
            }
        }
        // Los versículos que vienen vacíos en la fuente se saltan al
        // mostrar, sin cambiar la numeración: los 16 que la VBL omite
        // por no estar en los manuscritos más antiguos (p. ej. Mateo
        // 17:21), y 18 huecos de numeración de la RV1909 (p. ej. la
        // bendición de 2 Corintios 13:14 va en 13:13 en esa edición;
        // el texto no se pierde, está en el versículo siguiente).
        val visible = verses.indices.filter { verses[it].isNotBlank() }
        items(visible.size) { idx ->
            val i = visible[idx]
            val ref = VerseRef(bookId, book.info.name, chapter, i + 1, verses[i])
            val isFav = ref.key in favorites
            val hasNote = notes.containsKey(ref.key)
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    selected = ref
                    noteDraft = notes[ref.key] ?: ""
                }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "${i + 1}  ${verses[i]}",
                        fontSize = (16 * fontScale).sp,
                        lineHeight = (24 * fontScale).sp
                    )
                    if (isFav || hasNote) {
                        Text(
                            listOfNotNull(
                                if (isFav) "★ Favorito" else null,
                                if (hasNote) "✎ Nota" else null
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    // Panel del versículo seleccionado: favorito, nota y compartir.
    val sel = selected
    if (sel != null) {
        val activity = androidx.compose.ui.platform.LocalContext.current as? ComponentActivity
        Card(
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                .fillMaxWidth().padding(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(sel.label, fontWeight = FontWeight.Bold)
                Text(sel.text)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    label = { Text("Mi nota (se guarda en el teléfono)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch {
                            userData.toggleFavorite(sel.key)
                        }
                    }) {
                        Text(if (sel.key in favorites) "Quitar favorito" else "★ Favorito")
                    }
                    Button(onClick = {
                        scope.launch { userData.saveNote(sel.key, noteDraft) }
                        selected = null
                    }) { Text("Guardar nota") }
                    TextButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${sel.label}\n${sel.text}\n— Biblia SL")
                        }
                        activity?.startActivity(Intent.createChooser(send, "Compartir versículo"))
                    }) { Text("Compartir") }
                    TextButton(onClick = { selected = null }) { Text("Cerrar") }
                }
            }
        }
    }
    }
}

@Composable
fun SearchScreen(repo: BibleRepository, nav: NavHostController, translation: String) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<VerseRef>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Buscar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Escribe una cita (\"Juan 3\", \"Génesis 1:1\") o una palabra. La búsqueda es offline, " +
                "en ${BibleRepository.translationLabel(translation)}.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Juan 3:16") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            searching = true
            scope.launch {
                val res = withContext(Dispatchers.Default) { repo.search(query, translation) }
                results = res
                searching = false
            }
        }) { Text(if (searching) "Buscando…" else "Buscar") }
        Spacer(Modifier.height(8.dp))
        if (results.size >= 100) Text("Mostrando los primeros 100 resultados.")
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(results) { ref ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        nav.navigate("reader/${ref.bookId}/${ref.chapter}")
                    }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(ref.label, fontWeight = FontWeight.Bold)
                        Text(ref.text)
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(repo: BibleRepository, userData: UserData, nav: NavHostController, translation: String) {
    val favorites by userData.favoritesFlow.collectAsState(initial = emptySet())
    val notes by userData.notesFlow.collectAsState(initial = emptyMap())
    val favRefs = remember(favorites, translation) {
        favorites.mapNotNull { key ->
            val parts = key.split(":")
            if (parts.size != 3) null
            else repo.getVerse(parts[0], parts[1].toIntOrNull() ?: return@mapNotNull null, parts[2].toIntOrNull() ?: return@mapNotNull null, translation)
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Guardados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Favoritos (${favRefs.size}) · Notas (${notes.size}) · ${BibleRepository.translationLabel(translation)} · todo guardado en tu teléfono.")
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favRefs) { ref ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        nav.navigate("reader/${ref.bookId}/${ref.chapter}")
                    }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(ref.label, fontWeight = FontWeight.Bold)
                        Text(ref.text)
                        notes[ref.key]?.let { Text("✎ $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
            items(notes.entries.filter { it.key !in favorites }.toList()) { (key, note) ->
                val parts = key.split(":")
                val ref = if (parts.size == 3) repo.getVerse(parts[0], parts[1].toIntOrNull() ?: 0, parts[2].toIntOrNull() ?: 0, translation) else null
                if (ref != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            nav.navigate("reader/${ref.bookId}/${ref.chapter}")
                        }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ref.label, fontWeight = FontWeight.Bold)
                            Text(ref.text)
                            Text("✎ $note", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// Solo colores neón, como pidió David (los mismos tonos que los de
// Shenlong Play Music). "Original" no es un color: es no cambiar nada
// y quedarse con el color del tema.
val NeonColorOptions: List<Pair<String, Color>> = listOf(
    "Azul neón" to Color(0xFF2196F3),
    "Celeste" to Color(0xFF4FC3F7),
    "Turquesa" to Color(0xFF00BCD4),
    "Verde neón" to Color(0xFF4CAF50),
    "Esmeralda" to Color(0xFF00C853),
    "Amarillo neón" to Color(0xFFCCFF00),
    "Amarillo" to Color(0xFFFFC107),
    "Dorado" to Color(0xFFFFD700),
    "Naranja neón" to Color(0xFFFF9800),
    "Rojo neón" to Color(0xFFF44336),
    "Rosa neón" to Color(0xFFFF2E9A),
    "Rosa" to Color(0xFFE91E63),
    "Fucsia" to Color(0xFFE040FB),
    "Morado neón" to Color(0xFF9C27B0)
)

@Composable
fun ColorPickerRow(
    selectedName: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NeonColorOptions.forEach { (name, color) ->
            val selected = name == selectedName
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(
                        width = if (selected) 3.dp else 1.dp,
                        color = if (selected) Color.Black else Color.Gray,
                        shape = CircleShape
                    )
                    .background(color, CircleShape)
                    .clickable { onSelect(name) }
            )
        }
    }
}

@Composable
fun ColorSettingRow(
    title: String,
    selectedName: String,
    transparency: Float,
    onSave: (String, Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$title · Elegido: $selectedName")
        ColorPickerRow(
            selectedName = selectedName,
            onSelect = { onSave(it, transparency) }
        )
        Slider(
            value = transparency,
            onValueChange = { onSave(selectedName, it) },
            valueRange = 0f..1f
        )
        Text(
            "Transparencia: ${(transparency * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall
        )
        if (selectedName != "Original") {
            TextButton(onClick = { onSave("Original", 0f) }) {
                Text("Quitar color (volver al original)")
            }
        }
    }
}

@Composable
fun SettingsScreen(userData: UserData) {
    val fontScale by userData.fontFlow.collectAsState(initial = 1.0f)
    val dark by userData.darkFlow.collectAsState(initial = false)
    val bgColorName by userData.bgColorFlow.collectAsState(initial = "Original")
    val bgTransparency by userData.bgTransparencyFlow.collectAsState(initial = 0f)
    val cardColorName by userData.cardColorFlow.collectAsState(initial = "Original")
    val cardTransparency by userData.cardTransparencyFlow.collectAsState(initial = 0f)
    val textColorName by userData.textColorFlow.collectAsState(initial = "Original")
    val textTransparency by userData.textTransparencyFlow.collectAsState(initial = 0f)
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Ajustes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Text("Tamaño de letra")
            Slider(
                value = fontScale,
                onValueChange = { scope.launch { userData.setFontScale(it) } },
                valueRange = 0.8f..1.6f,
                steps = 7
            )
        }
        item {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Modo oscuro", modifier = Modifier.weight(1f))
                Switch(checked = dark, onCheckedChange = { scope.launch { userData.setDarkMode(it) } })
            }
        }
        item {
            Text("Colores neón", fontWeight = FontWeight.Bold)
        }
        item {
            ColorSettingRow(
                title = "Color de fondo",
                selectedName = bgColorName,
                transparency = bgTransparency,
                onSave = { name, transp -> scope.launch { userData.saveBgColor(name, transp) } }
            )
        }
        item {
            ColorSettingRow(
                title = "Color de tarjetas",
                selectedName = cardColorName,
                transparency = cardTransparency,
                onSave = { name, transp -> scope.launch { userData.saveCardColor(name, transp) } }
            )
        }
        item {
            ColorSettingRow(
                title = "Color de letras",
                selectedName = textColorName,
                transparency = textTransparency,
                onSave = { name, transp -> scope.launch { userData.saveTextColor(name, transp) } }
            )
        }
        item {
            Text("Fuentes del texto", fontWeight = FontWeight.Bold)
        }
        item {
            Text(
                "• Versión Biblia Libre (CC BY-SA 4.0, © 2018–2020 Jonathan Gallagher y Shelly Barrios de Avila): " +
                    "66 libros.\n" +
                    "• Reina-Valera 1909 (dominio público, eBible.org): 66 libros, " +
                    "en español clásico con la ortografía original de 1909.\n" +
                    "• Biblia en Español Sencillo (dominio público, eBible.org): 66 libros, " +
                    "en español muy sencillo.\n" +
                    "• Santa Biblia libre para el mundo (dominio público, eBible.org): " +
                    "Tobías, Judit, Sabiduría, Eclesiástico, Baruc, 1 y 2 Macabeos, y Daniel 13–14 (Susana y Bel).\n" +
                    "• Esta app no usa internet. Todo el texto está dentro de la app.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        item {
            Text("Biblia SL · versión 1.3 · 73 libros", style = MaterialTheme.typography.bodySmall)
        }
    }
}
