package com.bori.hipe.models

data class Trio<out T, out R, out U>(val first: T, val second: R, val third: U)