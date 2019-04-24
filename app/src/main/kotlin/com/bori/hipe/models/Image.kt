package com.bori.hipe.models

import java.io.Serializable

data class Image(

        val id: Long = 0L,
        val width: Long = 0L,
        val ratio: Double = 0.0,
        val height: Long = 0L,
        val urlMini: String = "",
        val urlSmall: String = "",
        val urlMedium: String = "",
        val urlLarge: String = "",
        val urlHuge: String = "",
        val ownerID: Int = 0,
        val eventID: Long = 0L,
        val creationMills: Long = System.currentTimeMillis()

) : Serializable
