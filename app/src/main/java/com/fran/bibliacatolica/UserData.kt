package com.fran.bibliacatolica

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "biblia_catolica")

/**
 * Favoritos, notas, capítulos leídos, última lectura y ajustes.
 * Todo se guarda en el teléfono con DataStore (sin internet, sin cuenta).
 * Claves:
 *  - favorito: "GEN:1:1"
 *  - nota: "GEN:1:1" -> texto de la nota
 *  - capítulo leído: "GEN:1"
 *  - última lectura: "GEN:1"
 */
class UserData(private val context: Context) {
    private val favoritesKey = stringSetPreferencesKey("favorites")
    private val notesKey = stringPreferencesKey("notes_json")
    private val readKey = stringSetPreferencesKey("read_chapters")
    private val lastReadKey = stringPreferencesKey("last_read")
    private val fontKey = floatPreferencesKey("font_scale")
    private val darkKey = booleanPreferencesKey("dark_mode")
    private val translationKey = stringPreferencesKey("translation")
    private val bgColorKey = stringPreferencesKey("bg_color_name")
    private val bgTranspKey = floatPreferencesKey("bg_transparency")
    private val cardColorKey = stringPreferencesKey("card_color_name")
    private val cardTranspKey = floatPreferencesKey("card_transparency")
    private val textColorKey = stringPreferencesKey("text_color_name")
    private val textTranspKey = floatPreferencesKey("text_transparency")

    /** Traducción activa: "vbl" (Versión Biblia Libre, por defecto),
     *  "rv1909" (Reina-Valera 1909) o "bes" (Biblia en Español Sencillo).
     *  Se guarda en el teléfono. */
    val translationFlow: Flow<String> =
        context.dataStore.data.map { it[translationKey] ?: "vbl" }

    /** Color neón del fondo ("Original" = sin cambio, color del tema). */
    val bgColorFlow: Flow<String> =
        context.dataStore.data.map { it[bgColorKey] ?: "Original" }
    val bgTransparencyFlow: Flow<Float> =
        context.dataStore.data.map { (it[bgTranspKey] ?: 0f).coerceIn(0f, 1f) }

    /** Color neón de las tarjetas ("Original" = sin cambio). */
    val cardColorFlow: Flow<String> =
        context.dataStore.data.map { it[cardColorKey] ?: "Original" }
    val cardTransparencyFlow: Flow<Float> =
        context.dataStore.data.map { (it[cardTranspKey] ?: 0f).coerceIn(0f, 1f) }

    /** Color neón de las letras ("Original" = sin cambio). */
    val textColorFlow: Flow<String> =
        context.dataStore.data.map { it[textColorKey] ?: "Original" }
    val textTransparencyFlow: Flow<Float> =
        context.dataStore.data.map { (it[textTranspKey] ?: 0f).coerceIn(0f, 1f) }

    val favoritesFlow: Flow<Set<String>> =
        context.dataStore.data.map { it[favoritesKey] ?: emptySet() }
    val readFlow: Flow<Set<String>> =
        context.dataStore.data.map { it[readKey] ?: emptySet() }
    val lastReadFlow: Flow<String?> =
        context.dataStore.data.map { it[lastReadKey] }
    val fontFlow: Flow<Float> =
        context.dataStore.data.map { it[fontKey] ?: 1.0f }
    val darkFlow: Flow<Boolean> =
        context.dataStore.data.map { it[darkKey] ?: false }
    val notesFlow: Flow<Map<String, String>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[notesKey] ?: return@map emptyMap<String, String>()
            try {
                val obj = JSONObject(raw)
                val out = mutableMapOf<String, String>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    out[k] = obj.getString(k)
                }
                out
            } catch (e: Exception) {
                emptyMap()
            }
        }

    suspend fun toggleFavorite(ref: String) {
        context.dataStore.edit { prefs ->
            val cur = (prefs[favoritesKey] ?: emptySet()).toMutableSet()
            if (cur.contains(ref)) cur.remove(ref) else cur.add(ref)
            prefs[favoritesKey] = cur
        }
    }

    suspend fun saveNote(ref: String, text: String) {
        val current = notesFlow.first().toMutableMap()
        if (text.isBlank()) current.remove(ref) else current[ref] = text
        val obj = JSONObject()
        current.forEach { (k, v) -> obj.put(k, v) }
        context.dataStore.edit { it[notesKey] = obj.toString() }
    }

    suspend fun toggleRead(chapterKey: String) {
        context.dataStore.edit { prefs ->
            val cur = (prefs[readKey] ?: emptySet()).toMutableSet()
            if (cur.contains(chapterKey)) cur.remove(chapterKey) else cur.add(chapterKey)
            prefs[readKey] = cur
        }
    }

    suspend fun setLastRead(chapterKey: String) {
        context.dataStore.edit { it[lastReadKey] = chapterKey }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[fontKey] = scale.coerceIn(0.8f, 1.6f) }
    }

    suspend fun setDarkMode(dark: Boolean) {
        context.dataStore.edit { it[darkKey] = dark }
    }

    suspend fun setTranslation(translation: String) {
        val value = when (translation) {
            "rv1909" -> "rv1909"
            "bes" -> "bes"
            else -> "vbl"
        }
        context.dataStore.edit { it[translationKey] = value }
    }

    suspend fun saveBgColor(name: String, transparency: Float) {
        context.dataStore.edit {
            it[bgColorKey] = name
            it[bgTranspKey] = transparency.coerceIn(0f, 1f)
        }
    }

    suspend fun saveCardColor(name: String, transparency: Float) {
        context.dataStore.edit {
            it[cardColorKey] = name
            it[cardTranspKey] = transparency.coerceIn(0f, 1f)
        }
    }

    suspend fun saveTextColor(name: String, transparency: Float) {
        context.dataStore.edit {
            it[textColorKey] = name
            it[textTranspKey] = transparency.coerceIn(0f, 1f)
        }
    }
}
