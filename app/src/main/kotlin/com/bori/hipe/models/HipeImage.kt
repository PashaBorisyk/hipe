package com.bori.hipe.models

import java.io.Serializable

data class HipeImage(

        val id: Long = 0,
        val exist: Boolean = false,
        val width: Long = 0,
        val ratio: Double = 0.0,
        val height: Long = 0,
        val urlMini: String = "",
        val urlSmall: String = "",
        val urlMedium: String = "",
        val urlLarge: String = "",
        val urlHuge: String = "",
        val behaviorId: Long = 0L,
        val eventId: Long = 0L,
        val creationMills: Long = System.currentTimeMillis()

) : Serializable
