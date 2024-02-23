package com.Meditation.Sounds.frequencies.utils

import android.content.Context
import android.os.LocaleList
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.Language
import java.util.*


class LanguageUtils {
    companion object {
        fun getLanguages(context: Context): List<Language> {
            return listOf(
                Language(
                    R.drawable.ic_england_flag,
                    "English",
                    context.getString(R.string.lang_en),
                    "en"
                ),
                Language(
                    R.drawable.ic_spain_flag,
                    "Spanish",
                    context.getString(R.string.lang_es),
                    "es"
                ),
                Language(
                    R.drawable.ic_france_flag,
                    "French",
                    context.getString(R.string.lang_fr),
                    "fr"
                ),
                Language(
                    R.drawable.ic_china_flag,
                    "China",
                    context.getString(R.string.lang_zh),
                    "zh"
                ),
            )
        }

        fun getLanguage(languageCode: String, context: Context): Language {
            return getLanguages(context).firstOrNull { it.code == languageCode } ?: Language(
                R.drawable.ic_england_flag, "English", context.getString(R.string.lang_en), "en"
            )
        }

        fun changeLanguage(context: Context, codeLanguage: String) {
//            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(codeLanguage)
//            AppCompatDelegate.setApplicationLocales(appLocale)
            val locale = Locale(codeLanguage)
            Locale.setDefault(locale)
            val res = context.resources
            val dm: DisplayMetrics = res.displayMetrics
            val conf = res.configuration
            conf.locale = locale
            res.updateConfiguration(conf, dm)
        }

        fun getLocaleList(context: Context): LocaleListCompat {
            return LocaleListCompat.forLanguageTags(getLanguages(context).joinToString(",") {
                it.code
            })
        }
    }
}