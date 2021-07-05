package de.hglabor.training.utils.extensions

import java.util.concurrent.TimeUnit

fun Long.asTime(): String {
    val minutes = TimeUnit.SECONDS.toMinutes(this)
    return String.format("%02d:%02d", minutes, TimeUnit.SECONDS.toSeconds(this)-60*minutes)
}