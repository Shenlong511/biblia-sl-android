package com.fran.bibliacatolica

/** Un libro de la Biblia. Los capítulos se cargan bajo demanda desde assets. */
data class BookInfo(
    val id: String,
    val name: String,
    val testament: String, // "AT" o "NT"
    val chapters: Int,
    val source: String,
    val sourceLabel: String
)

data class Book(
    val info: BookInfo,
    /** chapters[capítulo-1][versículo-1] = texto */
    val chapters: List<List<String>>,
    val chapterSources: List<String>
)

data class VerseRef(
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String
) {
    val key: String get() = "$bookId:$chapter:$verse"
    val label: String get() = "$bookName $chapter:$verse"
}

/** Catálogo canónico católico (73 libros) en orden. Los conteos se
 *  verifican contra assets/bible/index.json al arrancar; esta lista es
 *  el orden de presentación. */
object CatholicCanon {
    val books: List<Triple<String, String, String>> = listOf(
        Triple("GEN", "Génesis", "AT"), Triple("EXO", "Éxodo", "AT"),
        Triple("LEV", "Levítico", "AT"), Triple("NUM", "Números", "AT"),
        Triple("DEU", "Deuteronomio", "AT"), Triple("JOS", "Josué", "AT"),
        Triple("JDG", "Jueces", "AT"), Triple("RUT", "Rut", "AT"),
        Triple("1SA", "1 Samuel", "AT"), Triple("2SA", "2 Samuel", "AT"),
        Triple("1KI", "1 Reyes", "AT"), Triple("2KI", "2 Reyes", "AT"),
        Triple("1CH", "1 Crónicas", "AT"), Triple("2CH", "2 Crónicas", "AT"),
        Triple("EZR", "Esdras", "AT"), Triple("NEH", "Nehemías", "AT"),
        Triple("TOB", "Tobías", "AT"), Triple("JDT", "Judit", "AT"),
        Triple("EST", "Ester", "AT"), Triple("1MA", "1 Macabeos", "AT"),
        Triple("2MA", "2 Macabeos", "AT"), Triple("JOB", "Job", "AT"),
        Triple("PSA", "Salmos", "AT"), Triple("PRO", "Proverbios", "AT"),
        Triple("ECC", "Eclesiastés", "AT"), Triple("SNG", "Cantar de los Cantares", "AT"),
        Triple("WIS", "Sabiduría", "AT"), Triple("SIR", "Eclesiástico", "AT"),
        Triple("ISA", "Isaías", "AT"), Triple("JER", "Jeremías", "AT"),
        Triple("LAM", "Lamentaciones", "AT"), Triple("BAR", "Baruc", "AT"),
        Triple("EZK", "Ezequiel", "AT"), Triple("DAN", "Daniel", "AT"),
        Triple("HOS", "Oseas", "AT"), Triple("JOL", "Joel", "AT"),
        Triple("AMO", "Amós", "AT"), Triple("OBA", "Abdías", "AT"),
        Triple("JON", "Jonás", "AT"), Triple("MIC", "Miqueas", "AT"),
        Triple("NAM", "Nahúm", "AT"), Triple("HAB", "Habacuc", "AT"),
        Triple("ZEP", "Sofonías", "AT"), Triple("HAG", "Ageo", "AT"),
        Triple("ZEC", "Zacarías", "AT"), Triple("MAL", "Malaquías", "AT"),
        Triple("MAT", "Mateo", "NT"), Triple("MRK", "Marcos", "NT"),
        Triple("LUK", "Lucas", "NT"), Triple("JHN", "Juan", "NT"),
        Triple("ACT", "Hechos", "NT"), Triple("ROM", "Romanos", "NT"),
        Triple("1CO", "1 Corintios", "NT"), Triple("2CO", "2 Corintios", "NT"),
        Triple("GAL", "Gálatas", "NT"), Triple("EPH", "Efesios", "NT"),
        Triple("PHP", "Filipenses", "NT"), Triple("COL", "Colosenses", "NT"),
        Triple("1TH", "1 Tesalonicenses", "NT"), Triple("2TH", "2 Tesalonicenses", "NT"),
        Triple("1TI", "1 Timoteo", "NT"), Triple("2TI", "2 Timoteo", "NT"),
        Triple("TIT", "Tito", "NT"), Triple("PHM", "Filemón", "NT"),
        Triple("HEB", "Hebreos", "NT"), Triple("JAS", "Santiago", "NT"),
        Triple("1PE", "1 Pedro", "NT"), Triple("2PE", "2 Pedro", "NT"),
        Triple("1JN", "1 Juan", "NT"), Triple("2JN", "2 Juan", "NT"),
        Triple("3JN", "3 Juan", "NT"), Triple("JUD", "Judas", "NT"),
        Triple("REV", "Apocalipsis", "NT")
    )
}
