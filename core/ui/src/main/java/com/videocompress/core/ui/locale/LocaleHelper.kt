package com.videocompress.core.ui.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    fun applyLanguage(languageCode: String) {
        val targetKey = languageKey(languageCode)
        val currentKey = languageKey(AppCompatDelegate.getApplicationLocales())
        if (targetKey == currentKey) return
        AppCompatDelegate.setApplicationLocales(localesForCode(languageCode))
    }

    private fun localesForCode(languageCode: String): LocaleListCompat = when (languageCode) {
        "system" -> LocaleListCompat.getEmptyLocaleList()
        "zh" -> LocaleListCompat.create(Locale.SIMPLIFIED_CHINESE)
        "id" -> LocaleListCompat.create(Locale.forLanguageTag("id"))
        "he" -> LocaleListCompat.create(Locale.forLanguageTag("he"))
        "fil" -> LocaleListCompat.create(Locale.forLanguageTag("fil"))
        else -> LocaleListCompat.create(Locale.forLanguageTag(languageCode))
    }

    private fun languageKey(languageCode: String): String = languageCode

    private fun languageKey(locales: LocaleListCompat): String {
        if (locales.isEmpty) return "system"
        val locale = locales[0] ?: return "system"
        return when (locale.language) {
            "zh" -> "zh"
            "in", "id" -> "id"
            "iw", "he" -> "he"
            "fil" -> "fil"
            "" -> "system"
            else -> locale.language
        }
    }
}

data class AppLanguage(
    val code: String,
    val nameRes: Int,
)

val supportedLanguages = listOf(
    AppLanguage("system", com.videocompress.core.resources.R.string.language_system),
    AppLanguage("en", com.videocompress.core.resources.R.string.language_en),
    AppLanguage("es", com.videocompress.core.resources.R.string.language_es),
    AppLanguage("zh", com.videocompress.core.resources.R.string.language_zh),
    AppLanguage("hi", com.videocompress.core.resources.R.string.language_hi),
    AppLanguage("ar", com.videocompress.core.resources.R.string.language_ar),
    AppLanguage("tr", com.videocompress.core.resources.R.string.language_tr),
    AppLanguage("pt", com.videocompress.core.resources.R.string.language_pt),
    AppLanguage("fr", com.videocompress.core.resources.R.string.language_fr),
    AppLanguage("de", com.videocompress.core.resources.R.string.language_de),
    AppLanguage("id", com.videocompress.core.resources.R.string.language_id),
    AppLanguage("ja", com.videocompress.core.resources.R.string.language_ja),
    AppLanguage("ko", com.videocompress.core.resources.R.string.language_ko),
    AppLanguage("it", com.videocompress.core.resources.R.string.language_it),
    AppLanguage("vi", com.videocompress.core.resources.R.string.language_vi),
    AppLanguage("pl", com.videocompress.core.resources.R.string.language_pl),
    AppLanguage("ru", com.videocompress.core.resources.R.string.language_ru),
    AppLanguage("th", com.videocompress.core.resources.R.string.language_th),
    AppLanguage("bn", com.videocompress.core.resources.R.string.language_bn),
    AppLanguage("nl", com.videocompress.core.resources.R.string.language_nl),
    AppLanguage("uk", com.videocompress.core.resources.R.string.language_uk),
    AppLanguage("ms", com.videocompress.core.resources.R.string.language_ms),
    AppLanguage("fa", com.videocompress.core.resources.R.string.language_fa),
    AppLanguage("ro", com.videocompress.core.resources.R.string.language_ro),
    AppLanguage("cs", com.videocompress.core.resources.R.string.language_cs),
    AppLanguage("sv", com.videocompress.core.resources.R.string.language_sv),
    AppLanguage("fil", com.videocompress.core.resources.R.string.language_fil),
    AppLanguage("el", com.videocompress.core.resources.R.string.language_el),
    AppLanguage("hu", com.videocompress.core.resources.R.string.language_hu),
    AppLanguage("he", com.videocompress.core.resources.R.string.language_he),
    AppLanguage("ur", com.videocompress.core.resources.R.string.language_ur),
)
