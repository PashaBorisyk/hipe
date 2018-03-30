package com.bori.hipe.controllers.messenger

import com.bori.hipe.controllers.messenger.callback.MessageCallbackAdapter
import com.bori.hipe.controllers.receiver.BootReciever
import com.bori.hipe.models.ChatMessageNOSQL
import com.google.gson.Gson
import com.orm.SugarRecord
import de.tavendo.autobahn.WebSocketConnection
import de.tavendo.autobahn.WebSocketHandler

object WebSocketConnector : WebSocketConnection() {

    const val URL = "ws://192.168.0.31:9000/sock/?userId=1"
    private val gson = Gson()

    private val messageCallBacks = linkedSetOf<MessageCallbackAdapter>()

    fun createConncetion() {

        if (isConnected)
            return

        connect(URL, object : WebSocketHandler() {
            override fun onBinaryMessage(payload: ByteArray?) {
                super.onBinaryMessage(payload)
                messageCallBacks.forEach { it.onBinaryMessage(payload!!) }
            }

            override fun onTextMessage(payload: String) {
                super.onTextMessage(payload)
                messageCallBacks.forEach { it.onTextMessage(payload) }
            }

            override fun onOpen() {
                super.onOpen()
                val s = arrayOf("false")
                s.size
//                val unsentMessages = SugarRecord.find(
//                        ChatMessageNOSQL::class.java,
//                        "unsent = ?",
//                        s,"id","mills","1000"
//                )
//                unsentMessages.forEach {
//                    sendMessage(it)
//                }
//                SugarRecord.deleteInTx(unsentMessages)
//
//                messageCallBacks.forEach { it.onOpen() }
            }

            override fun onClose(code: Int, reason: String) {
                super.onClose(code, reason)
                messageCallBacks.forEach { it.onClose(code, reason) }

                if (BootReciever.isConnected) {
                    createConncetion()
                }
            }

        })
    }

    fun disposeConnection() {
        disconnect()
    }

    fun sendMessage(chatMessageNOSQL: ChatMessageNOSQL): Boolean {

        if (!(isConnected && BootReciever.isConnected)) {
            SugarRecord.save(chatMessageNOSQL)
            return false
        }
        try {
            sendTextMessage(gson.toJson(chatMessageNOSQL))
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun registerCallback(messageCallbackAdapter: MessageCallbackAdapter) = messageCallBacks.add(messageCallbackAdapter)
    fun unregisterCallback(messageCallbackAdapter: MessageCallbackAdapter) = messageCallBacks.remove(messageCallbackAdapter)

}