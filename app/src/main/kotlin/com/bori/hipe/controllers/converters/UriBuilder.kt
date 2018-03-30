package com.bori.hipe.controllers.converters


import android.net.Uri

class UriBuilder {

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

        if (withQueries)
            uriAsString += "&$key=$value"
        else {
            uriAsString += "?$key=$value"
            withQueries = true
        }

    }

    fun addQuery(key: String, value: Int) {

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
