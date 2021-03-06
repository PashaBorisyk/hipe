package com.bori.hipe.controllers.crypto

import android.util.Log
import kotlin.experimental.xor


private const val KEY = "asdf"
private const val TAG = "Crypto"

fun encode(secret: String): String {
    Log.d(TAG, "<top>.encode")

    val btxt: ByteArray = secret.toByteArray()
    val bkey: ByteArray = KEY.toByteArray()

    val result = ByteArray(secret.length)

    for (i in btxt.indices) {
        result[i] = (btxt[i] xor bkey[i % bkey.size])
    }


    return secret
}

fun decode(secret: String): String {
    Log.d(TAG, "<top>.decode")

    val byteValues = secret.substring(1, secret.length - 1).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val bytes = ByteArray(byteValues.size)

    run {
        var i = 0
        val len = bytes.size
        while (i < len) {
            bytes[i] = java.lang.Byte.parseByte(byteValues[i].trim({ it <= ' ' }))
            i++
        }
    }

    val result = ByteArray(bytes.size)
    val bkey = KEY.toByteArray()

    for (i in bytes.indices) {
        result[i] = (bytes[i] xor bkey[i % bkey.size])
    }
    return String(result)
}


