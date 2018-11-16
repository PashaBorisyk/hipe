package com.bori.hipe.controllers.converters


import android.net.Uri
import android.util.Log

class UriBuilder {

    private companion object {
        private const val TAG = "UriBuilder"
    }

    var uriAsString: String? = null
        private set
    private var withQueries = false

    constructor(baseUri: String) {
        this.uriAsString = baseUri
    }

    constructor(baseUri: Uri) {
        this.uriAsString = baseUri.toString()
    }

    fun addQuery(key: String, value: String) {
        Log.d(TAG, "UriBuilder.addQuery")

        if (withQueries)
            uriAsString += "&$key=$value"
        else {
            uriAsString += "?$key=$value"
            withQueries = true
        }

    }

    fun addQuery(key: String, value: Int) {
        Log.d(TAG, "UriBuilder.addQuery")

        if (withQueries)
            uriAsString += "&$key=$value"
        else {
            uriAsString += "?$key=$value"
            withQueries = true
        }

    }

    val uriAsUri: Uri
        get() = Uri.parse(uriAsString)

}
