package com.app.development.winter.shared.extension

import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import java.util.regex.Pattern

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}
fun isValidEmail(text: String?): Boolean {
    return text != null && Patterns.EMAIL_ADDRESS.matcher(text).matches()
}

fun isValidMobile(phone: String): Boolean {
    return if (!Pattern.matches("[a-zA-Z]+", phone)) {
        phone.length in 8..8
    } else false
}

fun isValidNumber(number: String, min: Int, max: Int): Boolean {
    return if (!Pattern.matches("[a-zA-Z]+", number)) {
        number.length in min..max
    } else false
}