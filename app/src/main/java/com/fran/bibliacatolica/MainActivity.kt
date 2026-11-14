package com.fran.bibliacatolica

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                BibliaApp(repo, userData, fontScale)
            }
        }
    }
}

private val DEUTEROCANONICAL = setOf("TOB", "JDT", "WIS", "SIR", "BAR", "1MA", "2MA")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliaApp(repo: BibleRepository, userData: UserData, fontScale: Float) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biblia SL") },
                actions = {
                    TextButton(onClick = { nav.navigate("search") }) { Text("Buscar") }
                    TextButton(onClick = { nav.navigate("favorites") }) { Text("Guardados") }
                    TextButton(onClick = { nav.navigate("settings") }) { Text("Ajustes") }
                }
            )
        }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(pad)
        ) {
            composable("home") { HomeScreen(repo, userData, nav) }
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
                ChapterListScreen(repo, userData, back.arguments?.getString("bookId") ?: "GEN", nav)
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
                    scope = scope
                )
            }
            composable("search") { SearchScreen(repo, nav) }
            composable("favorites") { FavoritesScreen(repo, userData, nav) }
            composable("settings") { SettingsScreen(userData) }
        }
    }
}

@Composable
fun HomeScreen(repo: BibleRepository, userData: UserData, nav: NavHostController) {
    val daily = remember { repo.dailyVerse() }
    val lastRead by userData.lastReadFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
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
            Text(
                "Traducción principal: Versión Biblia Libre, en español moderno. " +
                    "Los 7 libros deuterocanónicos se muestran con la Santa Biblia libre " +
                    "para el mundo, de dominio público, y van marcados en la lista.",
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
}

@Composable
fun BookListScreen(repo: BibleRepository, testament: String, nav: NavHostController) {
    val title = if (testament == "AT") "Antiguo Testamento" else "Nuevo Testamento"
    val books = repo.books.filter { it.testament == testament }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${books.size} libros · sin internet", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
    nav: NavHostController
) {
    val info = repo.bookById(bookId)
    val read by userData.readFlow.collectAsState(initial = emptySet())
    val scope = rememberCoroutineScope()
    if (info == null) {
        Text("Libro no encontrado.", Modifier.padding(16.dp))
        return
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(info.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(info.sourceLabel, style = MaterialTheme.typography.bodySmall)
        if (info.id in DEUTEROCANONICAL) {
            Text(
                "Libro deuterocanónico. Texto: Santa Biblia libre para el mundo, de dominio público.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
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
    scope: kotlinx.coroutines.CoroutineScope
) {
    val book = remember(bookId) { repo.loadBook(bookId) }
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
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "${book.info.name} $chapter",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (chapterSource == "spablm") "Santa Biblia libre para el mundo (dominio público)"
            else "Versión Biblia Libre",
            style = MaterialTheme.typography.bodySmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { if (chapter > 1) nav.navigate("reader/$bookId/${chapter - 1}") },
                enabled = chapter > 1
            ) { Text("← Anterior") }
            OutlinedButton(
                onClick = { scope.launch { userData.toggleRead(chapterKey) } }
            ) { Text(if (isRead) "✓ Leído" else "Marcar leído") }
            OutlinedButton(
                onClick = { if (chapter < book.chapters.size) nav.navigate("reader/$bookId/${chapter + 1}") },
                enabled = chapter < book.chapters.size
            ) { Text("Siguiente →") }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Los 16 versículos que la VBL omite por no estar en los
            // manuscritos más antiguos (p. ej. Mateo 17:21) vienen vacíos
            // en la fuente; se saltan al mostrar, sin cambiar la numeración.
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
fun SearchScreen(repo: BibleRepository, nav: NavHostController) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<VerseRef>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Buscar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Escribe una cita (\"Juan 3\", \"Génesis 1:1\") o una palabra. La búsqueda es offline.",
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
                val res = withContext(Dispatchers.Default) { repo.search(query) }
                results = res
                searching = false
            }
        }) { Text(if (searching) "Buscando…" else "Buscar") }
        Spacer(Modifier.height(8.dp))
        if (results.size >= 100) Text("Mostrando los primeros 100 resultados.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
fun FavoritesScreen(repo: BibleRepository, userData: UserData, nav: NavHostController) {
    val favorites by userData.favoritesFlow.collectAsState(initial = emptySet())
    val notes by userData.notesFlow.collectAsState(initial = emptyMap())
    val favRefs = remember(favorites) {
        favorites.mapNotNull { key ->
            val parts = key.split(":")
            if (parts.size != 3) null
            else repo.getVerse(parts[0], parts[1].toIntOrNull() ?: return@mapNotNull null, parts[2].toIntOrNull() ?: return@mapNotNull null)
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Guardados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Favoritos (${favRefs.size}) · Notas (${notes.size}) · todo guardado en tu teléfono.")
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                val ref = if (parts.size == 3) repo.getVerse(parts[0], parts[1].toIntOrNull() ?: 0, parts[2].toIntOrNull() ?: 0) else null
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

@Composable
fun SettingsScreen(userData: UserData) {
    val fontScale by userData.fontFlow.collectAsState(initial = 1.0f)
    val dark by userData.darkFlow.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Tamaño de letra")
        Slider(
            value = fontScale,
            onValueChange = { scope.launch { userData.setFontScale(it) } },
            valueRange = 0.8f..1.6f,
            steps = 7
        )
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Modo oscuro", modifier = Modifier.weight(1f))
            Switch(checked = dark, onCheckedChange = { scope.launch { userData.setDarkMode(it) } })
        }
        Text("Fuentes del texto", fontWeight = FontWeight.Bold)
        Text(
            "• Versión Biblia Libre (CC BY-SA 4.0, © 2018–2020 Jonathan Gallagher y Shelly Barrios de Avila): " +
                "66 libros.\n" +
                "• Santa Biblia libre para el mundo (dominio público, eBible.org): " +
                "Tobías, Judit, Sabiduría, Eclesiástico, Baruc, 1 y 2 Macabeos, y Daniel 13–14 (Susana y Bel).\n" +
                "• Esta app no usa internet. Todo el texto está dentro de la app.",
            style = MaterialTheme.typography.bodySmall
        )
        Text("Biblia SL · versión 1.0 · 73 libros", style = MaterialTheme.typography.bodySmall)
    }
}
