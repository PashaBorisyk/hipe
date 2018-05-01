package com.bori.hipe.util.extensions

fun Float.zeroIfNegative() = if(this > 0 ) this else 0f
fun Double.zeroIfNegative() = if(this > 0 ) this else 0.0
