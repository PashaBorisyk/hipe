package com.bori.hipe.controllers.messenger

import android.util.Log
import com.bori.hipe.controllers.messenger.callback.MessageCallbackAdapter
import com.bori.hipe.controllers.receiver.BootReciever
import com.bori.hipe.models.ChatMessageNOSQL
import com.google.gson.Gson
import com.orm.SugarRecord
import de.tavendo.autobahn.WebSocketConnection
import de.tavendo.autobahn.WebSocketHandler

object WebSocketConnector : WebSocketConnection() {

    private const val TAG = "WebSocketConnector.kt"
    const val URL = "ws://192.168.100.41:8081/"
    private val gson = Gson()

    private val messageCallBacks = linkedSetOf<MessageCallbackAdapter>()

    fun createConncetion() {

        Log.d(TAG,"Creating connection with : $URL")

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

    fun sendMessage(chatMessageNOSQL: ChatMessageNOSQL): Boolean {

        Log.d(TAG, "WebSocketConnector.sendMessage")

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

    fun registerCallback(messageCallbackAdapter: MessageCallbackAdapter) {
        Log.d(TAG, "WebSocketConnector.registerCallback")

        messageCallBacks.add(messageCallbackAdapter)
    }

    fun unregisterCallback(messageCallbackAdapter: MessageCallbackAdapter) {
        Log.d(TAG, "WebSocketConnector.unregisterCallback")
        messageCallBacks.remove(messageCallbackAdapter)
    }

}