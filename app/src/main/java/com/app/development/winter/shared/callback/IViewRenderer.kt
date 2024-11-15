package com.app.development.winter.shared.callback

interface IViewRenderer<STATE> {
    fun render(state: STATE)
}