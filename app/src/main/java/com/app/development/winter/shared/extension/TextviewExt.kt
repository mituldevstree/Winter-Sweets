package com.app.development.winter.shared.extension

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.ParcelableSpan
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.util.regex.Pattern
import kotlin.math.ceil


inline fun TextView.setOnDoneActionListener(crossinline callback: (TextView, Int, KeyEvent?) -> Unit) {
    setOnEditorActionListener { view, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback(view, actionId, event)
            true
        } else false
    }
}

inline fun TextView.doOnActionSend(crossinline callback: (view: TextView) -> Unit) {
    setOnEditorActionListener { view, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            callback(view)
            true
        } else false
    }
}

inline fun TextView.doOnActionSearch(crossinline callback: (view: TextView) -> Unit) {
    setOnEditorActionListener { view, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            callback(view)
            true
        } else false
    }
}

fun AppCompatTextView.doTextTrim(): String = text.toString().trim()

inline fun TextView.doOnActionDelete(crossinline callback: (view: TextView) -> Unit) {
    setOnKeyListener { view, keyCode, event ->
        if (event.keyCode == KeyEvent.KEYCODE_DEL) {
            callback(view as TextView)
        }
        false
    }
}

fun EditText.textChanges(): Flow<CharSequence?> {
    return callbackFlow {
        val listener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                trySend(s)
            }
        }
        addTextChangedListener(listener)
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}

fun TextView.setDrawableStart(@DrawableRes id: Int = 0) {
    val drawable = if (id != 0) context.getDrawableCompat(id) else null
    setDrawableStart(drawable)
}

fun TextView.setDrawableStart(drawable: Drawable? = null) {
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun TextView.setDrawableEnd(@DrawableRes id: Int = 0) {
    val drawable = if (id != 0) context.getDrawableCompat(id) else null
    setDrawableEnd(drawable)
}

fun TextView.setDrawableEnd(drawable: Drawable? = null) {
    setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

fun TextView.setDrawableTop(@DrawableRes id: Int = 0) {
    val drawable = if (id != 0) context.getDrawableCompat(id) else null
    setDrawableTop(drawable)
}

fun TextView.setDrawableTop(drawable: Drawable? = null) {
    setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
}


fun TextView.setGradientTextColor(colors: IntArray) {
    post {
        val lineCount = lineCount
        paint?.let { paint ->
            val width = paint.measureText(text.toString())
            val textShader: Shader = LinearGradient(
                width,
                0f,
                width,
                if (lineCount > 0) (textSize * lineCount) else textSize,
                colors,
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = textShader
        }
        invalidate()
    }
}

fun TextView.setDrawables(
    @DrawableRes left: Int = 0,
    @DrawableRes top: Int = 0,
    @DrawableRes right: Int = 0,
    @DrawableRes bottom: Int = 0,
) {
    setDrawables(
        left = context.getDrawableOrNull(left),
        top = context.getDrawableOrNull(top),
        right = context.getDrawableOrNull(right),
        bottom = context.getDrawableOrNull(bottom),
    )
}

private fun Context.getDrawableOrNull(@DrawableRes res: Int) =
    if (res != 0) getDrawableCompat(res) else null

fun TextView.setDrawables(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null,
) {
    setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
}

fun TextView.setTextOrGone(text: CharSequence?) {
    isVisible = text.isNotNullOrEmpty()
    this.text = text
}

fun TextView.setTextResOrGone(@StringRes res: Int) {
    isVisible = res != 0
    if (res != 0) {
        setText(res)
    }
}

fun TextView.addClickablePartAndSet(
    fullText: String,
    listClickableWord: List<ClickableWord>,
) {
    val ssb = SpannableStringBuilder(fullText)
    for (clickableWord in listClickableWord) {
        var idx1: Int = fullText.indexOf(clickableWord.word)
        var idx2: Int
        while (idx1 != -1) {
            idx2 = idx1 + clickableWord.word.length
            ssb.setSpan(clickableWord.clickableSpan, idx1, idx2, 0)
            val foregroundSpan = ForegroundColorSpan(Color.BLACK)
            ssb.setSpan(foregroundSpan, idx1, idx2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            idx1 = clickableWord.word.indexOf(clickableWord.word, idx2)
        }
    }
    setText(ssb, TextView.BufferType.SPANNABLE)
}

fun String.fromHtml(): Spanned? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}

fun String.getColoredSpanned(@ColorInt highlightedColor: Int, value: String): SpannableString {
    return SpannableString(this).getColoredSpanned(highlightedColor, value)
}

fun SpannableString.getColoredSpanned(
    @ColorInt highlightedColor: Int,
    value: String,
): SpannableString {
    val ssb = this
    val foregroundSpan = ForegroundColorSpan(highlightedColor)

    val start = indexOf(value)

    if (start != -1) {
        ssb.setSpan(
            foregroundSpan, start, start + value.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return ssb
}

fun SpannableString.getBoldSpanned(value: String): SpannableString {
    val ssb = this
    val span = StyleSpan(Typeface.BOLD)

    val start = indexOf(value)

    if (start != -1) {
        ssb.setSpan(span, start, start + value.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return ssb
}

fun String.getBoldSpanned(value: String): SpannableString {
    return SpannableString(this).getBoldSpanned(value)
}

fun String.getColoredSpanned(
    context: Context,
    listWords: List<FormatWord>,
): SpannableStringBuilder {
    val ssb = SpannableStringBuilder(this)
    for (word in listWords) {
        var idx1: Int = if (word.ignoreCase) {
            lowercase().indexOf(word.word.lowercase())
        } else {
            indexOf(word.word)
        }

        var idx2: Int
        while (idx1 != -1) {
            idx2 = idx1 + word.word.length
            if (idx2 == idx1) break
            ssb.setSpan(word, idx1, idx2, 0)
            val foregroundSpan = ForegroundColorSpan(context.getColor(word.colorRes))
            if (word.isBold) {
                ssb.setSpan(
                    StyleSpan(Typeface.BOLD), idx1, idx2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                ssb.setSpan(foregroundSpan, idx1, idx2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            idx1 = word.word.indexOf(word.word, idx2)
        }
    }
    return ssb
}

fun String.toDifferentSizeTextAndColor(
    smallText: String,
    proportion: Float = .8f,
    span: ParcelableSpan,
): SpannableString {
    val ss1 = SpannableString(this)
    val start = indexOf(smallText)
    if (start >= 0) {
        ss1.setSpan(span, start, length, 0)
        ss1.setSpan(RelativeSizeSpan(proportion), start, length, 0)
    } // set size
    return ss1
}

data class ClickableWord(
    val word: String,
    val clickableSpan: ClickableSpan,
)

data class FormatWord(
    val word: String,
    val colorRes: Int,
    val isBold: Boolean,
    val ignoreCase: Boolean = false,
)

fun TextView.setOnHashtagClickListener(callback: (TextView, CharSequence) -> Unit) {
    if (movementMethod !is LinkMovementMethod) {
        movementMethod = LinkMovementMethod.getInstance()
    }
    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateHashtagSpans(s ?: "", callback)
        }

        override fun afterTextChanged(s: Editable?) = Unit
    }
    addTextChangedListener(textWatcher)
}

private fun updateHashtagSpans(text: CharSequence, callback: (TextView, CharSequence) -> Unit) {
    if (text.isEmpty()) {
        return
    }
    check(text is Spannable) {
        "Attached text is not a Spannable," + "add TextView.BufferType.SPANNABLE when setting text to this TextView."
    }
    val spannable: Spannable = text
    for (span in spannable.getSpans(0, text.length, CharacterStyle::class.java)) {
        spannable.removeSpan(span)
    }
    val pattern = Pattern.compile("#(\\w+)")
    val matcher = pattern.matcher(spannable)
    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()
        val span = HashtagSpan(callback)
        spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.text = spannable.subSequence(start, end)
    }
}

private class HashtagSpan(
    private val callback: (TextView, CharSequence) -> Unit,
) : ClickableSpan() {

    var text: CharSequence? = null

    override fun onClick(widget: View) {
        val textNonNull = text ?: return
        val symbol = textNonNull.findAnyOf(HASHTAG_SYMBOLS_ARRAY.toList())?.second ?: return
        val hashtag = textNonNull.removePrefix(symbol)
        if (widget is TextView && textNonNull.isNotEmpty()) {
            callback(widget, hashtag)
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = Color.WHITE
        ds.isUnderlineText = false
    }
}

fun TextView.setTextPadded(text: CharSequence) {
    Log.d("", "[PAD TEXT] setTextPadded 1 text = $text view width = $width")
    if (this.text.trim() == text) return
    val newText = getPaddedText(text)
    if (newText != this.text) {
        Log.d("", "[PAD TEXT] setTextPadded 2 text = $text view width = $width")
        this.text = newText
    }
}

fun TextView.setTextPaddedOrGone(text: CharSequence) {
    if (this.text.trim() == text) return
    if (text.isBlank()) {
        isVisible = false
        return
    }

    isVisible = true
    doOnNextLayout { setTextPadded(text) }
}

private fun TextView.getPaddedText(text: CharSequence): CharSequence {
    val textBounds = Rect()
    paint.getTextBounds(text.toString(), 0, text.length, textBounds)
    Log.d("", "[PAD TEXT] text width = ${textBounds.width()} view width = $width")
    if (textBounds.width() > width) {
        return text
    }

    val workaroundString = "a a"
    val spaceBounds = Rect()
    paint.getTextBounds(workaroundString, 0, workaroundString.length, spaceBounds)
    val abounds = Rect()
    paint.getTextBounds("a", 0, 1, abounds)
    val spaceWidth = spaceBounds.width() - abounds.width() * 2.0
    val amountOfSpacesNeeded = ceil((width - textBounds.width()) / spaceWidth).toInt() + 20

    Log.d(
        "", """[PAD TEXT] 
        |text width = ${textBounds.width()} 
        |view width = $width 
        |spaces width = ${spaceWidth * amountOfSpacesNeeded}""".trimMargin()
    )

    return if (amountOfSpacesNeeded > 0) text.padRight(
        text.toString().length + amountOfSpacesNeeded
    ) else text
}

private fun CharSequence.padRight(n: Int): String {
    return String.format("%1$-" + n + "s", this)
}

data class ClickableText(val text: String, val action: () -> Unit)

fun TextView.setLinks(
    fullText: String,
    words: List<ClickableText>,
    underLine: Boolean = false,
    @ColorRes linkColorRes: Int = 0,
    bold: Boolean = false,
) {
    val spannableString = SpannableString(fullText)
    var startIndexOfLink = -1
    for (link in words) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color =
                    if (linkColorRes != 0) getColor(linkColorRes) else textPaint.linkColor
                textPaint.isUnderlineText = underLine
                textPaint.isFakeBoldText = bold
            }

            override fun onClick(view: View) {
                link.action()
            }
        }
        startIndexOfLink = fullText.indexOf(link.text, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue
        spannableString.setSpan(
            clickableSpan,
            startIndexOfLink,
            startIndexOfLink + link.text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    movementMethod = LinkMovementMethod.getInstance()
    setText(spannableString, TextView.BufferType.SPANNABLE)
}
