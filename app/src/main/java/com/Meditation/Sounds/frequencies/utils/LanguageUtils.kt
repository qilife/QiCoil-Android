package com.Meditation.Sounds.frequencies.utils

import android.content.Context
import android.os.LocaleList
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.codeLanguage
import com.Meditation.Sounds.frequencies.models.Language
import java.util.*


class LanguageUtils {
    companion object {
        fun getLanguages(context: Context): List<Language> {
            return listOf(
                Language(
                    R.drawable.ic_united_kingdom,
                    "English",
                    context.getString(R.string.lang_en),
                    "en"
                ),
                Language(
                    R.drawable.ic_spain,
                    "Spanish",
                    context.getString(R.string.lang_es),
                    "es"
                ),
                Language(
                    R.drawable.ic_france,
                    "French",
                    context.getString(R.string.lang_fr),
                    "fr"
                ),
                Language(
                    R.drawable.ic_china,
                    "China",
                    context.getString(R.string.lang_zh),
                    "zh"
                ),
            )
        }

        fun getLanguage(languageCode: String, context: Context): Language {
            return getLanguages(context).firstOrNull { it.code == languageCode } ?: Language(
                R.drawable.ic_united_kingdom, "English", context.getString(R.string.lang_en), "en"
            )
        }

        fun changeLanguage(context: Context, codeLanguage: String) {
            PreferenceHelper.preference(context).codeLanguage = codeLanguage
            val locale = Locale(codeLanguage)
            Locale.setDefault(locale)
            val otherLanguages = getLanguages(context).filter { it.code!= codeLanguage }.joinToString(",")
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("$codeLanguage,$otherLanguages"))
        }
    }
}