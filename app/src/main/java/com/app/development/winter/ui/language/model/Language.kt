package com.app.development.winter.ui.language.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.app.development.winter.BR
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Language(
    @SerializedName("id") var id: String,
    @SerializedName("name") var name: String,
    @SerializedName("locale") var locale: String,
    @SerializedName("selected") private var selected: Boolean = false,
) : BaseObservable(), Parcelable {
    var selectedCountry: Boolean
        @Bindable get() = selected
        set(value) {
            selected = value
            notifyPropertyChanged(BR.selectedCountry)
        }

    companion object {
        const val EN = "en"
        const val ES = "es"
        const val PT = "pt"
        const val BG = "bg"
        const val HI = "hi"
        const val DE = "de"
        const val ZH = "zh"
        const val BN = "bn"
        const val FR = "fr"
        const val RU = "ru"
        const val IN = "in"

        const val ENGLISH = "English"
        const val SPANISH = "Spanish"
        const val PORTUGUESE = "Portuguese"
        const val BULGARIAN = "Bulgarian"

        const val HINDI = "Hindi"
        const val GERMAN = "German"
        const val MANDARIN = "Mandarin"
        const val BENGALI = "Bengali"
        const val FRENCH = "French"
        const val RUSSIAN = "Russian"
        const val INDONESIAN = "Indonesian"

        fun getLanguagesList(): ArrayList<Language> {
            val arrayList: ArrayList<Language> = ArrayList()
            arrayList.add(Language(EN, ENGLISH, EN))
            arrayList.add(Language(BG, BULGARIAN, BG))
            arrayList.add(Language(ES, SPANISH, ES))
            arrayList.add(Language(PT, PORTUGUESE, PT))

            arrayList.add(Language(HI, HINDI, HI))
            arrayList.add(Language(DE, GERMAN, DE))
            arrayList.add(Language(ZH, MANDARIN, ZH))
            arrayList.add(Language(BN, BENGALI, BN))
            arrayList.add(Language(FR, FRENCH, FR))
            arrayList.add(Language(RU, RUSSIAN, RU))
            arrayList.add(Language(IN, INDONESIAN, IN))
            return arrayList
        }
    }
}