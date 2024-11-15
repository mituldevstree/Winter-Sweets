package com.app.development.winter.shared.callback

interface OnLibSessionEstablished {
    fun onSessionEstablished()
    fun onSessionEstablishedFailed()
    fun onStartFaceTecVerification() {}
}