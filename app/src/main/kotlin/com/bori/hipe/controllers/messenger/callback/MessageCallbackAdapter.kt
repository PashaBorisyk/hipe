package com.bori.hipe.controllers.messenger.callback

private const val TAG = "MessageCallbacks"

interface MessageCallback {
    fun onOpen()
    fun onClose(code: Int, reason: String)
    fun onTextMessage(payload: String)
    fun onBinaryMessage(payload: ByteArray)
}

abstract class MessageCallbackAdapter : MessageCallback {

    override fun onOpen() {}
    override fun onClose(code: Int, reason: String) {}
    override fun onTextMessage(payload: String) {}
    override fun onBinaryMessage(payload: ByteArray) {}
}
