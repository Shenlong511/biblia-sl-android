package com.fran.bibliacatolica

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/**
 * Lee la Biblia desde assets (JSON locales, sin internet).
 * Dos traducciones offline:
 *  - "vbl": Versión Biblia Libre (+ Santa Biblia libre para los
 *    deuterocanónicos y Daniel 13-14), en assets/bible/.
 *  - "rv1909": Reina-Valera 1909 (dominio público), en
 *    assets/bible_rv1909/. Solo tiene 66 libros: los 7
 *    deuterocanónicos y Daniel 13-14 se leen de assets/bible/
 *    (Santa Biblia libre para el mundo), igual que con la VBL.
 * Cada libro se carga bajo demanda y se guarda en una caché pequeña
 * (máximo 3 libros en memoria) para no cargar toda la Biblia a la vez.
 */
class BibleRepository(private val context: Context) {

    companion object {
        const val TRANSLATION_VBL = "vbl"
        const val TRANSLATION_RV1909 = "rv1909"

        /** Libros que la RV1909 no tiene: siempre se leen de spablm. */
        val DEUTEROCANONICAL_IDS = setOf(
            "TOB", "JDT", "WIS", "SIR", "BAR", "1MA", "2MA"
        )

        fun translationLabel(translation: String): String =
            if (translation == TRANSLATION_RV1909) "Reina-Valera 1909"
            else "Versión Biblia Libre"
    }

    val books: List<BookInfo> by lazy { loadIndex() }

    private val cache = LinkedHashMap<String, Book>()

    private fun loadIndex(): List<BookInfo> {
        val text = context.assets.open("bible/index.json").bufferedReader().readText()
        val root = JSONObject(text)
        val arr = root.getJSONArray("books")
        val out = mutableListOf<BookInfo>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                BookInfo(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    testament = o.getString("testament"),
                    chapters = o.getInt("chapters"),
                    source = o.getString("source"),
                    sourceLabel = o.getString("sourceLabel")
                )
            )
        }
        return out
    }

    fun bookById(id: String): BookInfo? = books.firstOrNull { it.id == id }

    /**
     * Carga un libro en la traducción pedida.
     *  - RV1909 + libro deuterocanónico: se devuelve el libro de
     *    assets/bible/ (Santa Biblia libre), con su fuente spablm.
     *  - RV1909 + Daniel: 12 capítulos RV1909 + Daniel 13-14 de
     *    assets/bible/ (Santa Biblia libre), igual que la VBL.
     *  - Cualquier fallo al leer RV1909: se usa la VBL, para que el
     *    lector nunca quede vacío.
     */
    fun loadBook(id: String, translation: String = TRANSLATION_VBL): Book? {
        if (translation == TRANSLATION_RV1909) {
            if (id in DEUTEROCANONICAL_IDS) return loadBookFromAssets("bible/$id.json", "vbl:$id")
            val rv = loadBookFromAssets("bible_rv1909/$id.json", "rv1909:$id")
                ?: return loadBookFromAssets("bible/$id.json", "vbl:$id")
            if (id == "DAN") {
                val spablm = loadBookFromAssets("bible/DAN.json", "vbl:DAN")
                if (spablm != null && spablm.chapters.size == 14) {
                    val chapters = rv.chapters + spablm.chapters.subList(12, 14)
                    val sources = rv.chapterSources + listOf("spablm", "spablm")
                    val info = rv.info.copy(
                        chapters = 14,
                        source = "rv1909+spablm",
                        sourceLabel = "Reina-Valera 1909 (caps. 1–12) + Santa Biblia libre para el mundo (caps. 13–14, Susana y Bel)"
                    )
                    val merged = Book(info, chapters, sources)
                    cache["rv1909:DAN"] = merged
                    return merged
                }
            }
            return rv
        }
        return loadBookFromAssets("bible/$id.json", "vbl:$id")
    }

    private fun loadBookFromAssets(assetPath: String, cacheKey: String): Book? {
        cache[cacheKey]?.let { return it }
        return try {
            val text = context.assets.open(assetPath).bufferedReader().readText()
            val root = JSONObject(text)
            val info = BookInfo(
                id = root.getString("id"),
                name = root.getString("name"),
                testament = root.getString("testament"),
                chapters = root.getJSONArray("chapters").length(),
                source = root.getString("source"),
                sourceLabel = root.getString("sourceLabel")
            )
            val chaptersJson = root.getJSONArray("chapters")
            val chapters = mutableListOf<List<String>>()
            for (c in 0 until chaptersJson.length()) {
                val versesJson = chaptersJson.getJSONArray(c)
                val verses = mutableListOf<String>()
                for (v in 0 until versesJson.length()) verses.add(versesJson.getString(v))
                chapters.add(verses)
            }
            val sources = mutableListOf<String>()
            if (root.has("chapterSources")) {
                val s = root.getJSONArray("chapterSources")
                for (i in 0 until s.length()) sources.add(s.getString(i))
            }
            val book = Book(info, chapters, sources)
            if (cache.size >= 3) {
                val oldest = cache.keys.first()
                cache.remove(oldest)
            }
            cache[cacheKey] = book
            book
        } catch (e: Exception) {
            null
        }
    }

    fun getVerse(bookId: String, chapter: Int, verse: Int, translation: String = TRANSLATION_VBL): VerseRef? {
        val book = loadBook(bookId, translation) ?: return null
        if (chapter !in 1..book.chapters.size) return null
        val verses = book.chapters[chapter - 1]
        if (verse !in 1..verses.size) return null
        return VerseRef(bookId, book.info.name, chapter, verse, verses[verse - 1])
    }

    /** Versículo del día: rota por el día del año, 100% local. */
    fun dailyVerse(translation: String = TRANSLATION_VBL): VerseRef? {
        val refs = listOf(
            Triple("GEN", 1, 1), Triple("JHN", 3, 16), Triple("PSA", 23, 1),
            Triple("PSA", 119, 105), Triple("PRO", 3, 5), Triple("ISA", 41, 10),
            Triple("JER", 29, 11), Triple("MAT", 11, 28), Triple("MAT", 6, 33),
            Triple("LUK", 1, 37), Triple("ROM", 8, 28), Triple("ROM", 15, 13),
            Triple("PHP", 4, 13), Triple("PHP", 4, 6), Triple("HEB", 11, 1),
            Triple("JAS", 1, 5), Triple("1JN", 4, 8), Triple("PSA", 27, 1),
            Triple("PSA", 46, 1), Triple("ISA", 40, 31), Triple("MAT", 28, 20),
            Triple("JHN", 14, 27), Triple("ROM", 12, 2), Triple("GAL", 5, 22),
            Triple("EPH", 2, 8), Triple("COL", 3, 23), Triple("1PE", 5, 7),
            Triple("PSA", 34, 8), Triple("PRO", 18, 10), Triple("MIC", 6, 8)
        )
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val (b, c, v) = refs[day % refs.size]
        return getVerse(b, c, v, translation)
    }

    /**
     * Búsqueda offline. Primero intenta interpretar la consulta como una
     * cita ("Juan 3", "Génesis 1:1", "Salmos 23"). Si no es una cita,
     * busca el texto versículo por versículo, libro por libro, y se
     * detiene al llegar a 100 resultados para no tardar demasiado.
     */
    fun search(query: String, translation: String = TRANSLATION_VBL): List<VerseRef> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        parseReference(q, translation)?.let { return it }
        val needle = q.lowercase()
        val out = mutableListOf<VerseRef>()
        for (info in books) {
            val book = loadBook(info.id, translation) ?: continue
            for ((ci, verses) in book.chapters.withIndex()) {
                for ((vi, text) in verses.withIndex()) {
                    if (text.lowercase().contains(needle)) {
                        out.add(VerseRef(info.id, info.name, ci + 1, vi + 1, text))
                        if (out.size >= 100) return out
                    }
                }
            }
        }
        return out
    }

    private fun parseReference(q: String, translation: String = TRANSLATION_VBL): List<VerseRef>? {
        // Formatos: "Juan 3", "Juan 3:16", "Génesis 1:1", "Salmos 23:1-3"
        val m = Regex("""^(.+?)\s+(\d+)(?::(\d+)(?:-(\d+))?)?$""").matchEntire(q) ?: return null
        val namePart = m.groupValues[1].trim()
        val chapter = m.groupValues[2].toIntOrNull() ?: return null
        val vFrom = m.groupValues[3].toIntOrNull()
        val vTo = m.groupValues[4].toIntOrNull() ?: vFrom
        val info = books.firstOrNull { it.name.equals(namePart, ignoreCase = true) }
            ?: books.firstOrNull { it.name.lowercase().startsWith(namePart.lowercase()) }
            ?: return null
        val book = loadBook(info.id, translation) ?: return null
        if (chapter !in 1..book.chapters.size) return null
        val verses = book.chapters[chapter - 1]
        val out = mutableListOf<VerseRef>()
        if (vFrom == null) {
            for ((i, t) in verses.withIndex()) out.add(VerseRef(info.id, info.name, chapter, i + 1, t))
        } else {
            for (v in vFrom..(vTo ?: vFrom)) {
                if (v in 1..verses.size) out.add(VerseRef(info.id, info.name, chapter, v, verses[v - 1]))
            }
        }
        return out
    }

    @Suppress("unused")
    fun toJsonArray(a: JSONArray): List<String> {
        val out = mutableListOf<String>()
        for (i in 0 until a.length()) out.add(a.getString(i))
        return out
    }
}
