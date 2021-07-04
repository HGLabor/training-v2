package de.hglabor.training.utils

import de.hglabor.training.config.PREFIX
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor

fun broadcastLine(): Int = grayBroadcast("$PREFIX ${ChatColor.BOLD}${"―".repeat(20)}")
fun broadcastBlankLine(): Int = broadcast(PREFIX)

fun grayBroadcast(s: String): Int = broadcast("${ChatColor.GRAY}${s.replace(ChatColor.WHITE.toString(), ChatColor.GRAY.toString())}")