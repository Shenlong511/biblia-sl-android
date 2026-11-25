package com.fran.bibliacatolica

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
                // Con el modo oscuro activo el color de fondo se desactiva:
                // manda el fondo oscuro del tema, para que el modo oscuro
                // se vea oscuro. Al apagarlo, vuelve el color elegido.
                background = if (dark) baseScheme.background else (bgColor ?: baseScheme.background),
                surface = cardColor ?: baseScheme.surface,
                surfaceVariant = cardColor ?: baseScheme.surfaceVariant,
                onBackground = textColor ?: baseScheme.onBackground,
                onSurface = textColor ?: baseScheme.onSurface,
                onSurfaceVariant = textColor ?: baseScheme.onSurfaceVariant,
                // El morado de los botones y del texto de la barra (Buscar,
                // Guardados, Ajustes, Donación) es el color primario: sigue
                // al color de letras elegido, para que también cambie.
                primary = textColor ?: baseScheme.primary,
                onPrimary = textColor?.let {
                    if (it.luminance() > 0.5f) Color.Black else Color.White
                } ?: baseScheme.onPrimary
            )
            MaterialTheme(colorScheme = scheme) {
                BibliaApp(repo, userData, fontScale, translation)
            }
        }
    }
}

// Versión de la app: se muestra en Ajustes y al compartir un versículo.
private const val APP_VERSION = "1.6"

private val DEUTEROCANONICAL = setOf("TOB", "JDT", "WIS", "SIR", "BAR", "1MA", "2MA")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliaApp(repo: BibleRepository, userData: UserData, fontScale: Float, translation: String) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDonation by remember { mutableStateOf(false) }
    // ¿La pantalla está arriba del todo? Cada pantalla lo actualiza al
    // deslizar. Al bajar, se esconden la barra superior (título y
    // opciones) y la barra de estado (batería, señal); al volver al
    // principio, vuelven a salir. Vale en vertical y en horizontal.
    val isAtTop = remember { mutableStateOf(true) }
    LaunchedEffect(isAtTop.value) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (isAtTop.value) {
            controller.show(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
    }
    // Salir de la app con dos toques seguidos en Atrás, solo en la
    // pantalla principal. En las demás pantallas, Atrás vuelve atrás.
    val backStackEntry by nav.currentBackStackEntryAsState()
    val isHome = backStackEntry?.destination?.route == "home"
    var lastBackPress by remember { mutableStateOf(0L) }
    BackHandler(enabled = isHome) {
        val now = System.currentTimeMillis()
        if (now - lastBackPress < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPress = now
            Toast.makeText(context, "Presiona de nuevo para salir", Toast.LENGTH_SHORT).show()
        }
    }
    // En horizontal la pantalla es bajita: si el título llevara su altura
    // completa (proporción de la imagen a todo lo ancho) se comería casi
    // toda la pantalla y el contenido quedaba sin espacio ni deslizamiento,
    // solo funcionaban los botones de la barra. En horizontal el título es
    // solo una tira fina; en vertical sigue completo como antes.
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isAtTop.value,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Column {
                    // Título como imagen a todo lo ancho, completa y sin
                    // recortar: la altura sale de la proporción de la imagen.
                    // Tocar el título vuelve a Inicio.
                    Image(
                        painter = painterResource(id = R.drawable.title_biblia_sl),
                        contentDescription = "Biblia SL",
                        modifier = Modifier.fillMaxWidth()
                            .then(if (isLandscape) Modifier.height(40.dp) else Modifier.aspectRatio(2293f / 946f))
                            .clickable { nav.popBackStack("home", false) },
                        contentScale = if (isLandscape) ContentScale.Crop else ContentScale.FillWidth
                    )
                    // Barra de opciones delgada, con el texto más grande y
                    // deslizable de lado (como las filas de colores), para
                    // que nunca se corte ningún botón por el tamaño.
                    Row(
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                            .horizontalScroll(rememberScrollState())
                            .background(MaterialTheme.colorScheme.surface),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Inicio vuelve directo a la pantalla principal,
                        // sin tener que dar Atrás por todas las pantallas.
                        // Los demás botones no se apilan dos veces.
                        TextButton(
                            onClick = { nav.popBackStack("home", false) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text("Inicio", fontSize = 14.sp) }
                        TextButton(
                            onClick = { showDonation = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text("Donación", fontSize = 14.sp) }
                        TextButton(
                            onClick = { nav.navigate("search") { launchSingleTop = true } },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text("Buscar", fontSize = 14.sp) }
                        TextButton(
                            onClick = { nav.navigate("favorites") { launchSingleTop = true } },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text("Guardados", fontSize = 14.sp) }
                        TextButton(
                            onClick = { nav.navigate("settings") { launchSingleTop = true } },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text("Ajustes", fontSize = 14.sp) }
                    }
                }
            }
        }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(pad)
        ) {
            composable("home") { HomeScreen(repo, userData, nav, translation, isAtTop) }
            composable(
                "books/{testament}",
                arguments = listOf(navArgument("testament") { type = NavType.StringType })
            ) { back ->
                BookListScreen(repo, back.arguments?.getString("testament") ?: "AT", nav, isAtTop)
            }
            composable(
                "chapters/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { back ->
                ChapterListScreen(repo, userData, back.arguments?.getString("bookId") ?: "GEN", nav, translation, isAtTop)
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
                    translation = translation,
                    isAtTop = isAtTop
                )
            }
            composable("search") { SearchScreen(repo, nav, translation, isAtTop) }
            composable("favorites") { FavoritesScreen(repo, userData, nav, translation, isAtTop) }
            composable("settings") { SettingsScreen(userData, isAtTop) }
        }
    }
    if (showDonation) DonationDialog(onClose = { showDonation = false })
}

@Composable
fun HomeScreen(repo: BibleRepository, userData: UserData, nav: NavHostController, translation: String, isAtTop: MutableState<Boolean>) {
    val daily = remember(translation) { repo.dailyVerse(translation) }
    val lastRead by userData.lastReadFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .collect { atTop -> isAtTop.value = atTop }
    }
    LazyColumn(
        state = listState,
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
fun BookListScreen(repo: BibleRepository, testament: String, nav: NavHostController, isAtTop: MutableState<Boolean>) {
    val title = if (testament == "AT") "Antiguo Testamento" else "Nuevo Testamento"
    val books = repo.books.filter { it.testament == testament }
    // El título va dentro de la lista para que también se esconda al bajar.
    val listState = remember(testament) { LazyListState() }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .collect { atTop -> isAtTop.value = atTop }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Text("${books.size} libros · sin internet", style = MaterialTheme.typography.bodySmall)
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
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
@Composable
fun ChapterListScreen(
    repo: BibleRepository,
    userData: UserData,
    bookId: String,
    nav: NavHostController,
    translation: String,
    isAtTop: MutableState<Boolean>
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
    // El título va dentro de la cuadrícula para que también se esconda al bajar.
    val gridState = remember(bookId) { LazyGridState() }
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0 }
            .collect { atTop -> isAtTop.value = atTop }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        state = gridState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(info.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(sourceText, style = MaterialTheme.typography.bodySmall)
        }
        if (info.id in DEUTEROCANONICAL) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "Libro deuterocanónico. Texto: Santa Biblia libre para el mundo, de dominio público.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(4.dp))
        }
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

@Composable
fun ReaderScreen(
    repo: BibleRepository,
    userData: UserData,
    bookId: String,
    chapter: Int,
    nav: NavHostController,
    fontScale: Float,
    scope: kotlinx.coroutines.CoroutineScope,
    translation: String,
    isAtTop: MutableState<Boolean>
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
    // El estado se recrea al cambiar de capítulo para empezar arriba.
    val listState = remember(bookId, chapter) { LazyListState() }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .collect { atTop -> isAtTop.value = atTop }
    }
    Box(Modifier.fillMaxSize()) {
    // El encabezado (titulo, selector y botones) va DENTRO de la lista:
    // al bajar el texto se esconde y el capitulo ocupa toda la pantalla;
    // al subir hasta arriba vuelve a aparecer. En horizontal era lo que
    // dejaba apenas un versiculo visible.
    LazyColumn(
        state = listState,
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
    // Es una ventana de verdad: fondo oscuro detrás (tocarlo la cierra),
    // fondo propio distinto al de las tarjetas para que no se confunda con
    // la lista, altura limitada con deslizamiento y los 4 botones en dos
    // filas para que ninguno se corte fuera de la pantalla.
    val sel = selected
    if (sel != null) {
        val activity = androidx.compose.ui.platform.LocalContext.current as? ComponentActivity
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f))
                    .clickable { selected = null }
            )
            Card(
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                    .fillMaxWidth().padding(16.dp).heightIn(max = 480.dp),
                // Fondo negro fijo: con el color de fondo elegido se veía
                // transparente y el texto de la lista se perdía detrás.
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text(sel.label, fontWeight = FontWeight.Bold)
                    Text(sel.text)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        label = { Text("Mi nota (se guarda en el teléfono)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
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
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${sel.label}\n${sel.text}\n— Biblia SL $APP_VERSION")
                            }
                            activity?.startActivity(Intent.createChooser(send, "Compartir versículo"))
                        }) { Text("Compartir") }
                        TextButton(onClick = { selected = null }) { Text("Cerrar") }
                    }
                    // Las notas se podían agregar pero no quitar: si este
                    // versículo ya tiene nota, se puede borrar aquí.
                    if (sel.key in notes) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                scope.launch { userData.saveNote(sel.key, "") }
                                noteDraft = ""
                            }) { Text("Borrar nota") }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun SearchScreen(repo: BibleRepository, nav: NavHostController, translation: String, isAtTop: MutableState<Boolean>) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<VerseRef>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // Los controles de búsqueda quedan fijos; solo la lista de resultados
    // avisa al bajar para esconder la barra superior y la de estado.
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .collect { atTop -> isAtTop.value = atTop }
    }
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
            state = listState,
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
fun FavoritesScreen(repo: BibleRepository, userData: UserData, nav: NavHostController, translation: String, isAtTop: MutableState<Boolean>) {
    val favorites by userData.favoritesFlow.collectAsState(initial = emptySet())
    val notes by userData.notesFlow.collectAsState(initial = emptyMap())
    val scope = rememberCoroutineScope()
    val favRefs = remember(favorites, translation) {
        favorites.mapNotNull { key ->
            val parts = key.split(":")
            if (parts.size != 3) null
            else repo.getVerse(parts[0], parts[1].toIntOrNull() ?: return@mapNotNull null, parts[2].toIntOrNull() ?: return@mapNotNull null, translation)
        }
    }
    // El título va dentro de la lista para que también se esconda al bajar.
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .collect { atTop -> isAtTop.value = atTop }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Guardados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Text("Favoritos (${favRefs.size}) · Notas (${notes.size}) · ${BibleRepository.translationLabel(translation)} · todo guardado en tu teléfono.")
        }
        item {
            Spacer(Modifier.height(4.dp))
        }
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
                            TextButton(onClick = {
                                scope.launch { userData.saveNote(key, "") }
                            }) { Text("Borrar nota") }
                        }
                    }
                }
            }
        }
}

// Colores para fondo, tarjetas y letras: los 14 neón de antes más 16
// clásicos (rojo, negro, azul y otros, aunque no sean neón), 30 en total.
// "Original" no es un color: es no cambiar nada y quedarse con el color
// del tema.
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
    "Morado neón" to Color(0xFF9C27B0),
    "Rojo" to Color(0xFFD32F2F),
    "Negro" to Color(0xFF000000),
    "Azul" to Color(0xFF1565C0),
    "Azul marino" to Color(0xFF0D47A1),
    "Índigo" to Color(0xFF3F51B5),
    "Violeta" to Color(0xFF7C4DFF),
    "Morado" to Color(0xFF6A1B9A),
    "Blanco" to Color(0xFFFFFFFF),
    "Gris" to Color(0xFF9E9E9E),
    "Gris oscuro" to Color(0xFF424242),
    "Café" to Color(0xFF795548),
    "Verde" to Color(0xFF2E7D32),
    "Lima" to Color(0xFFCDDC39),
    "Naranja" to Color(0xFFEF6C00),
    "Coral" to Color(0xFFFF7043),
    "Rosa claro" to Color(0xFFF8BBD0)
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
fun SettingsScreen(userData: UserData, isAtTop: MutableState<Boolean>) {
    val fontScale by userData.fontFlow.collectAsState(initial = 1.0f)
    val dark by userData.darkFlow.collectAsState(initial = false)
    val bgColorName by userData.bgColorFlow.collectAsState(initial = "Original")
    val bgTransparency by userData.bgTransparencyFlow.collectAsState(initial = 0f)
    val cardColorName by userData.cardColorFlow.collectAsState(initial = "Original")
    val cardTransparency by userData.cardTransparencyFlow.collectAsState(initial = 0f)
    val textColorName by userData.textColorFlow.collectAsState(initial = "Original")
    val textTransparency by userData.textTransparencyFlow.collectAsState(initial = 0f)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .collect { atTop -> isAtTop.value = atTop }
    }
    LazyColumn(
        state = listState,
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
        if (dark) {
            item {
                Text(
                    "Con el modo oscuro activo el color de fondo queda desactivado.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
            Text("Biblia SL · versión $APP_VERSION · 73 libros", style = MaterialTheme.typography.bodySmall)
        }
    }
}
