package com.app.development.winter.ui.session.builder

interface SessionActionListeners {
    enum class SessionAction {
        SESSION_END, SYNC_TIME, STOP_CYCLE
    }

    fun onSessionAction(action: SessionAction?, value: Any? = null)
}