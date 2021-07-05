package de.hglabor.training.utils.extensions

import org.bukkit.ChatColor

fun String.col(vararg colorNames: String): String {
    var prefix = ""
    colorNames.forEach { prefix += colorFromName(it) }
    return prefix + this + ChatColor.RESET.toString() + ChatColor.WHITE.toString()
}

fun colorFromName(name: String): ChatColor = ChatColor.valueOf(name.uppercase())