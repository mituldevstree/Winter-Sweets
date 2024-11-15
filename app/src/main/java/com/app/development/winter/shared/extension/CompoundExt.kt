package com.app.development.winter.shared.extension

import android.widget.CompoundButton
import android.widget.RadioGroup

fun CompoundButton.setCheckedProgram(
    checked: Boolean,
    onCheckChange: (CompoundButton, Boolean) -> Unit
) {
    setOnCheckedChangeListener(null)
    isChecked = checked
    setOnCheckedChangeListener(onCheckChange)
}

fun RadioGroup.setCheckedProgram(checkedViewId: Int, onCheckChange: (RadioGroup, Int) -> Unit) {
    setOnCheckedChangeListener(null)
    check(checkedViewId)
    setOnCheckedChangeListener(onCheckChange)
}
